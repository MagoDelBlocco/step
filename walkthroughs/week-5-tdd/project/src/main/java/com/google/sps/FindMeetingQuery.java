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

import java.util.Collection;
import java.util.LinkedList;
import com.google.sps.TimeRange;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<TimeRange> timeTable =
                                            registerRelevantEvents(events,
                                                                   request.getAttendees());

    return searchSuitableSlots(timeTable, request.getDuration());
  }

  private Collection<TimeRange>
              registerRelevantEvents(final Collection<Event> events,
                                     final Collection<String> invited) {
    Collection<TimeRange> timeTable = new LinkedList<>();
    timeTable.add(TimeRange.WHOLE_DAY);

    for (Event event : events) {
      if (relevantEvent(event, invited)) {
        timeTable = splitTimeTable(timeTable, event.getWhen());
      }
    }

    return timeTable;
  }

  private Boolean relevantEvent(final Event event, final Collection<String> crucialPeople) {
    for (String crucialPerson : crucialPeople) {
      if (event.getAttendees().contains(crucialPerson)) {
        return true;
      }
    }

    return false;
  }

  private Collection<TimeRange>
              splitTimeTable(final Collection<TimeRange> originalTimeTable,
                             final TimeRange splittingTime) {
    /** 
     *  TODO: splits the timetable like this:
     *  originalTimeTable: [---------------------------]
     *  splitter:                       [----]
     *  returns:           [------------]    [---------]
     *  !!! USE TIMERANGE METHODS FOR OVERLAPPING
     */

     return originalTimeTable;
  }

  private Collection<TimeRange>
              searchSuitableSlots(final Collection<TimeRange> timeTable,
                                  final long duration) {
    Collection<TimeRange> suitableSlots = new LinkedList<>();

    for (TimeRange emptySlot : timeTable) {
      if (emptySlot.duration() >= duration) {
        suitableSlots.add(emptySlot);
      }
    }

    return suitableSlots;
  }
}
