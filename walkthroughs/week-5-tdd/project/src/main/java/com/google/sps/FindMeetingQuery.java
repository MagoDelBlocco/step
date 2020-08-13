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
   * This method is used to find all available time slots for a meeting,
   * provided a list of mandatory and optional attendees.
   * @param request provides the Meeting object, along with the attendees list.
   * @param events is the list of all previously scheduled meetings.
   * @return returns null when the request is null, returns an empty list when
   * the event list is null, else returns a list of all available time slots for the
   * meeting.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    if (request == null) {
      return null;
    } else if (events == null) {
      events = new LinkedList<>();
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

  /**
   * This method filters slots that have a timespan less than a given duration
   * @param timeTable a list of available TimeRanges, regardless of their duration. Cannot be null.
   * @param duration the specified duration of the meeting.
   * @return a collection of timeranges that can accomodate a meeting
   * of the specified duration. Can never be null.
   */
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

  /**
   * This method goes through a list of already scheduled events and creates
   * two timetables of available slots, regardless of their duration.
   * @param events a collection of all scheduled events.
   * @param mandatoryAttendees a list of mandatory attendees for the event to be scheduled.
   * @param optionalAttendees a list of optional attendees for the event to be scheduled.
   * @return a pair of lists of TimeRanges which represent empty timespans for either
   * mandatory attendees and optional attendees, or only for mandatory attendees.
   */
  private TimeTablePair registerRelevantEvents(final Collection<Event> events,
                                               final Collection<String> mandatoryAttendees,
                                               final Collection<String> optionalAttendees) {
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
   * This method chooses which strategy to apply for modifying an existing timetable
   * in accordance with a new event. This method stores the result in the modified
   * originalTimeTable.
   *
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
   *
   * @param originalTimeTable a list of all previously registered events. When this
   * method returns, the result is stored in this object. Cannot be null.
   * @param timeSplitter the TimeRange of the event that is to be registered. Cannot
   * be null.
   */
  private void splitTimeTable(final List<TimeRange> originalTimeTable,
                              final TimeRange timeSplitter) {
    if (originalTimeTable.isEmpty()) {
      return;
    }

    /*
     * Find the element with the largest starting point <= the starting point
     * of timeSplitter. This will determine in which case the program is in.
     */
    int firstRelevantTimeslotIdx = TimeRange.lowerBound(originalTimeTable, timeSplitter);

    if (originalTimeTable.get(firstRelevantTimeslotIdx).contains(timeSplitter)) {  // Case 1
      splitAtTimeslot(originalTimeTable, timeSplitter, firstRelevantTimeslotIdx);
    } else {  // Case 2, 3, 4
      modifyOverlappingSlots(originalTimeTable, timeSplitter, firstRelevantTimeslotIdx);
    }
  }

  /**
   * For the case where a TimeRange is entirely contained inside the TimeRange of an
   * already registered event, there could be 4 variations:
   * a) The initial slot is split in two non-null distinct time slots
   * b) The initial slot shares the starting point with the splitter, and as such
   *    it becomes only the slot from splitter's end and initial slot's end
   * c) Same as b), but with the slot becoming the time from initial slot's begin and
   *    splitter's begin.
   * d) The initial slot is the same as the splitter, and there are no resulting slots.
   * @param timeTable a list of all previously registered events. When this
   * method returns, the result is stored in this object. Cannot be null.
   * @param splitter the TimeRange that is contained within an element of the timeTable. Cannot be
   * null.
   * @param splitIdx the index of the timeTable element which contains the splitter.
   */
  private void splitAtTimeslot(final List<TimeRange> timeTable, final TimeRange splitter,
                                                                final int splitIdx) {
    TimeRange originalSlot = timeTable.remove(splitIdx);

    /*
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
   * This method handles the cases where the new event's TimeRange is not entirely
   * contained inside one of the timeTable's elements, but instead the newly occupied slot may
   * or may not modify the last closest slot that starts before it, contain a number of consecutive
   * slots after it, and may or may not modify the first slot that ends after it. 
   * @param timeTable a list of all previously registered events. When this
   * method returns, the result is stored in this object. Cannot be null.
   * @param overlapper the TimeRange that potentially overlaps with at least one element of the
   * timeTable. Cannot be null.
   * @param overlapIdx the index of the timeTable element that has the starting time closest,
   * but less than the starting time of the overlapping event.
   */
  private void modifyOverlappingSlots(final List<TimeRange> timeTable, final TimeRange overlapper,
                                                                       int overlapIdx) {
    TimeRange aux;

    // Checks if it is in case 2.
    if (timeTable.get(overlapIdx).overlaps(overlapper)) {
      aux = timeTable.remove(overlapIdx);

      /*
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
    while (; overlapIdx < timeTable.size() && overlapper.contains(timeTable.get(overlapIdx));) {
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
