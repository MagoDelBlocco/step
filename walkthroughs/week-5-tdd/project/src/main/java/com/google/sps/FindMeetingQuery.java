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

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import com.google.sps.TimeRange;

/**
 *  This class exposes the {@code query} method, which finds available time slots
 * for a meeting with a list of mandatory and optional attendees.
 */
public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    TimeTablePair timeTablePair = registerRelevantEvents(events, request.getAttendees(),
                                                                 request.getOptionalAttendees());
    List<TimeRange> mandatoryTimeTable = timeTablePair.getFirst();
    List<TimeRange> optionalAndMandatoryTimeTable = timeTablePair.getSecond();

    Collection<TimeRange> mandatory = searchSuitableSlots(mandatoryTimeTable,
                                                          request.getDuration());
    Collection<TimeRange> mandatoryAndOptional = searchSuitableSlots(optionalAndMandatoryTimeTable,
                                                                     request.getDuration());
    
    return mandatoryAndOptional.isEmpty() ?
           mandatory : mandatoryAndOptional;
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
     * all the people invited
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

  private void splitTimeTable(final List<TimeRange> originalTimeTable,
                              final TimeRange timeSplitter) {
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
     * Mention: for this method, it will only matter if the splitter is contained or not
     */
    int firstRelevantTimeslotIdx = TimeRange.lowerBound(originalTimeTable, timeSplitter);

    if (originalTimeTable.get(firstRelevantTimeslotIdx).contains(timeSplitter)) {  // Case 1
      splitAtTimeslot(originalTimeTable, timeSplitter, firstRelevantTimeslotIdx);
    } else {  // Case 2, 3, 4
      modifyOverlappingSlots(originalTimeTable, timeSplitter, firstRelevantTimeslotIdx);
    }
  }

  private void splitAtTimeslot(final List<TimeRange> timeTable, final TimeRange splitter,
                                                                final int splitIdx) {
    TimeRange originalSlot = timeTable.remove(splitIdx);

    if (originalSlot.end() > splitter.end()) {
      timeTable.add(splitIdx,
                    TimeRange.fromStartEnd(splitter.end(), originalSlot.end(), false));
    }
    
    if (originalSlot.start() < splitter.start()) {
      timeTable.add(splitIdx,
                    TimeRange.fromStartEnd(originalSlot.start(), splitter.start(), false));
    }
  }

  private void modifyOverlappingSlots(final List<TimeRange> timeTable, final TimeRange overlapper,
                                                                       int overlapIdx) {
    TimeRange aux;

    if (timeTable.get(overlapIdx).overlaps(overlapper)) {
      aux = timeTable.remove(overlapIdx);
      --overlapIdx;

      if (aux.start() < overlapper.start()) {
        timeTable.add(++overlapIdx, TimeRange.fromStartEnd(aux.start(), overlapper.start(), false));
      }
    } else {
      ++overlapIdx;
    }

    for (; overlapIdx < timeTable.size() && overlapper.contains(timeTable.get(overlapIdx));) {
      timeTable.remove(overlapIdx);
    }

    if (overlapIdx < timeTable.size() && timeTable.get(overlapIdx).overlaps(overlapper)) {
      aux = timeTable.remove(overlapIdx);

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
