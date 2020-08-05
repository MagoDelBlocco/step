// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import com.google.sps.TimeRange;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * This class exposes the {@code query} method, which finds available time slots
 * for a meeting with a list of mandatory and optional attendees.
 */
public final class FindMeetingQuery {
  /**
   * If @param request is null, returns null.
   * If @param events is null, considers an empty collection of events.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    if (request == null) {
      returns null;
    } else if (events == null) {
      events = LinkedList<>();
    }

    TimeTablePair timeTablePair = registerRelevantEvents(events, request.getAttendees(),
                                                                 request.getOptionalAttendees());
    List<TimeRange> mandatoryTimeTable = timeTablePair.getFirst();
    List<TimeRange> optionalAndMandatoryTimeTable = timeTablePair.getSecond();

    Collection<TimeRange> mandatoryAndOptional = searchSuitableSlots(optionalAndMandatoryTimeTable,
                                                                     request.getDuration());
    
    return mandatoryAndOptional.isEmpty() ?
           searchSuitableSlots(mandatoryTimeTable, request.getDuration()) : mandatoryAndOptional;
  }

  private Collection<TimeRange> searchSuitableSlots(final List<TimeRange> timeTable,
                                                    final long duration) {
    Collection<TimeRange> suitableSlots = new LinkedList<>();

    for (TimeRange emptySlot : timeTable) {
      if (emptySlot.duration() >= duration) {
        suitableSlots.add(emptySlot);
      }
    }

    return suitableSlots;
  }

  private TimeTablePair registerRelevantEvents(final Collection<Event> events,
                                               final Collection<String> mandatoryAttendees,
                                               final Collection<String> optionalAttendees) {
    /**
     * The timeTable collection holds TimeRanges which represent empty time slots for
     * all the people invited.
     */
    List<TimeRange> timeTable = new ArrayList<>();
    List<TimeRange> optionalTimeTable = new ArrayList<>();
    timeTable.add(TimeRange.WHOLE_DAY);
    optionalTimeTable.add(TimeRange.WHOLE_DAY);

    for (Event event : events) {
      if (relevantEvent(event, mandatoryAttendees)) {
        splitTimeTable(timeTable, event.getWhen());
        splitTimeTable(optionalTimeTable, event.getWhen());
      }

      if (relevantEvent(event, optionalAttendees)) {
        splitTimeTable(optionalTimeTable, event.getWhen());
      }
    }

    return new TimeTablePair(timeTable, optionalTimeTable);
  }

  private Boolean relevantEvent(final Event event, final Collection<String> relevantPeople) {
    for (String person : relevantPeople) {
      if (event.getAttendees().contains(person)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Possible cases:
   * 1. originalTimeTable:  [--------------------------]
   *    splitter:                     [-----]
   *    result:             [---------]     [----------]
   *
   * 2. originalTimeTable:  [------]   ...   [---------]
   *    splitter:                [-------------]
   *    result:             [----]             [-------]
   *
   * 3. originalTimeTable:  [------]   ...  [----------]
   *    splitter:                     [-----]
   *    result:             [------]        [----------]
   *
   * 4. originalTimeTable:  [------]           [-------]
   *    splitter:                     [-----]
   *    result:             [------]           [-------]
   *
   * Mention: for this method, it will only matter if the splitter is contained or not.
   */
  private void splitTimeTable(final List<TimeRange> originalTimeTable,
                              final TimeRange timeSplitter) {
    if (originalTimeTable.isEmpty()) {
      return;
    }

    /**
     * Find the element with the largest starting point <= the starting point
     * of @param timeSplitter. This will determine in which case the program is in.
     */
    int firstRelevantTimeslotIdx = TimeRange.lowerBound(originalTimeTable, timeSplitter);

    if (originalTimeTable.get(firstRelevantTimeslotIdx).contains(timeSplitter)) {  // Case 1
      splitAtTimeslot(originalTimeTable, timeSplitter, firstRelevantTimeslotIdx);
    } else {  // Case 2, 3, 4
      modifyOverlappingSlots(originalTimeTable, timeSplitter, firstRelevantTimeslotIdx);
    }
  }

  /**
   * For case 1, there could be 4 variations:
   * a) The initial slot in split in two non-null distinct time slots
   * b) The initial slot shares the starting point with the @param splitter, and as such
   *    it becomes only the slot from @param splitter's end and initial slot's end
   * c) Same as b), but with the slot becoming the time from initial slot's begin and
   *    @param splitter's begin.
   * d) The initial slot is the same as the splitter, and there are no resulting slots.
   */
  private void splitAtTimeslot(final List<TimeRange> timeTable, final TimeRange splitter,
                                                                final int splitIdx) {
    TimeRange originalSlot = timeTable.remove(splitIdx);

    /**
     * Inserting the second slot before the first one, since in subcase a)
     * they will remain in order because of how {@code add} works.
     */
    if (originalSlot.end() > splitter.end()) {
      timeTable.add(splitIdx,
                    TimeRange.fromStartEnd(splitter.end(), originalSlot.end(), false));
    }
    
    if (originalSlot.start() < splitter.start()) {
      timeTable.add(splitIdx,
                    TimeRange.fromStartEnd(originalSlot.start(), splitter.start(), false));
    }
  }

  /**
   * This method handles cases 2, 3, and 4, where the occupied slot may or may not modify
   * the last closest slot that starts before it, contain a number of consecutive slots
   * after it, and may or may not modify the first slot that ends after it. 
   */
  private void modifyOverlappingSlots(final List<TimeRange> timeTable, final TimeRange overlapper,
                                                                       int overlapIdx) {
    TimeRange aux;

    // Checks if it is in case 2.
    if (timeTable.get(overlapIdx).overlaps(overlapper)) {
      aux = timeTable.remove(overlapIdx);

      /** 
       * No need to call {@code contains}, since it is known that
       * {@code aux.end > overlapper.start} (aux.start <= overlapper.start and aux overlaps with 
       * overlapper).
       */
      if (aux.start() < overlapper.start()) {
        timeTable.add(overlapIdx, TimeRange.fromStartEnd(aux.start(), overlapper.start(), false));
      } else {  // timeTable[overlapIdx] already is the next element
        --overlapIdx;
      }
    }
    ++overlapIdx;

    // In case 3, remove any slots contained by @param overlapper.
    for (; overlapIdx < timeTable.size() && overlapper.contains(timeTable.get(overlapIdx));) {
      timeTable.remove(overlapIdx);
    }

    if (overlapIdx < timeTable.size() && timeTable.get(overlapIdx).overlaps(overlapper)) {
      aux = timeTable.remove(overlapIdx);

      // No need for contain check here, since it was already checked in the previous loop.
      timeTable.add(overlapIdx, TimeRange.fromStartEnd(overlapper.end(), aux.end(), false));
    }
  }
}

final class TimeTablePair {
  private List<TimeRange> first;
  private List<TimeRange> second;

  TimeTablePair(final List<TimeRange> first, final List<TimeRange> second) {
    this.first = first;
    this.second = second;
  }

  List<TimeRange> getFirst() {
    return first;
  }

  List<TimeRange> getSecond() {
    return second;
  }
}
