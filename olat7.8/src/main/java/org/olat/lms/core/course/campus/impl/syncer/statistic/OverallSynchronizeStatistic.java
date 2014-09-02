/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.lms.core.course.campus.impl.syncer.statistic;

import java.util.ArrayList;
import java.util.List;

/**
 * Initial Date: 25.06.2012 <br>
 * 
 * @author cg
 */
public class OverallSynchronizeStatistic {

    List<SynchronizedGroupStatistic> courseStatisticList;

    public OverallSynchronizeStatistic() {
        courseStatisticList = new ArrayList<SynchronizedGroupStatistic>();
    }

    public void add(SynchronizedGroupStatistic courseSynchronizeStatistic) {
        courseStatisticList.add(courseSynchronizeStatistic);
    }

    public String calculateOverallStatistic() {
        return "overallAddedOwners=" + getAddedOwners() + " , overallRemovedOwners=" + getRemovedOwners() + " ; overallAddedParticipants=" + getAddedParticipants()
                + " , overallRemovedParticipants=" + getRemovedParticipants();
    }

    public int getAddedOwners() {
        int overallAddedOwners = 0;
        for (SynchronizedGroupStatistic groupStatistic : courseStatisticList) {
            if (groupStatistic.getOwnerGroupStatistic() != null) {
                overallAddedOwners += groupStatistic.getOwnerGroupStatistic().getAddedStatistic();
            }
        }
        return overallAddedOwners;
    }

    public int getRemovedOwners() {
        int overallRemovedOwners = 0;
        for (SynchronizedGroupStatistic groupStatistic : courseStatisticList) {
            if (groupStatistic.getOwnerGroupStatistic() != null) {
                overallRemovedOwners += groupStatistic.getOwnerGroupStatistic().getRemovedStatistic();
            }
        }
        return overallRemovedOwners;
    }

    public int getAddedParticipants() {
        int overallAddedParticipants = 0;
        for (SynchronizedGroupStatistic groupStatistic : courseStatisticList) {
            overallAddedParticipants += groupStatistic.getParticipantGroupStatistic().getAddedStatistic();
        }
        return overallAddedParticipants;
    }

    public int getRemovedParticipants() {
        int overallRemovedParticipants = 0;
        for (SynchronizedGroupStatistic groupStatistic : courseStatisticList) {
            overallRemovedParticipants += groupStatistic.getParticipantGroupStatistic().getRemovedStatistic();
        }
        return overallRemovedParticipants;
    }

    public int getNumberOfSynchronizedCourses() {
        return courseStatisticList.size();
    }

}
