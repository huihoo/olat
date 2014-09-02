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
 * Copyright (c) 2009 frentix GmbH, Switzerland<br>
 * <p>
 */
package org.olat.lms.scorm.archiver;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Description:<br>
 * Hold the sco's datamodel of a user.
 * <P>
 * Initial Date: 17 august 2009 <br>
 * 
 * @author srosse
 */
public class ScoDatas {
    private final String itemId;
    private final String username;
    private String rawScore;
    private String lessonStatus;
    private String comments;
    private String totalTime;
    private Date lastModifiedDate;

    private final List<ScoInteraction> interactions = new ArrayList<ScoInteraction>();
    private final List<ScoObjective> objectives = new ArrayList<ScoObjective>();

    public ScoDatas(final String itemId, final String username) {
        this.itemId = itemId;
        this.username = username;
    }

    public String getItemId() {
        return itemId;
    }

    public String getUsername() {
        return username;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(final String totalTime) {
        this.totalTime = totalTime;
    }

    public String getLessonStatus() {
        return lessonStatus;
    }

    public void setLessonStatus(final String lessonStatus) {
        this.lessonStatus = lessonStatus;
    }

    public String getRawScore() {
        return rawScore;
    }

    public void setRawScore(final String rawScore) {
        this.rawScore = rawScore;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(final String comments) {
        this.comments = comments;
    }

    public int getNumOfInteractions() {
        return interactions.size();
    }

    public int getNumOfObjectives() {
        return objectives.size();
    }

    public List<ScoObjective> getObjectives() {
        return objectives;
    }

    public ScoObjective getObjective(final String objectiveId) {
        if (objectiveId == null) {
            return null;
        }
        for (final ScoObjective objective : objectives) {
            if (objectiveId.equals(objective.getId())) {
                return objective;
            }
        }
        return null;
    }

    public ScoObjective getObjective(final int i) {
        if (objectives.size() <= i) {
            for (int j = objectives.size(); j <= i; j++) {
                objectives.add(j, new ScoObjective(j));
            }
        }
        return objectives.get(i);
    }

    public List<ScoInteraction> getInteractions() {
        return interactions;
    }

    public ScoInteraction getInteraction(final int i) {
        if (interactions.size() <= i) {
            for (int j = interactions.size(); j <= i; j++) {
                interactions.add(j, new ScoInteraction(j));
            }
        }
        return interactions.get(i);
    }
}
