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

package org.olat.data.qti;

import java.util.Date;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.PersistentObject;

/**
 * Initial Date: 14.05.2004
 * 
 * @author gnaegi
 */
public class QTIResultSet extends PersistentObject {

    private long olatResource;
    private String olatResourceDetail;
    private long repositoryRef;

    private Identity identity;
    private int qtiType;
    private long assessmentID;
    private boolean isPassed;
    private float score;
    private Long duration;
    private Date lastModified;

    public QTIResultSet() {
        //
    }

    /**
	 */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * @param date
     */
    public void setLastModified(final Date date) {
        lastModified = date;
    }

    /**
     * @return
     */
    public long getAssessmentID() {
        return assessmentID;
    }

    /**
     * @return
     */
    public Identity getIdentity() {
        return identity;
    }

    /**
     * @return
     */
    public boolean getIsPassed() {
        return isPassed;
    }

    /**
     * @return
     */
    public long getOlatResource() {
        return olatResource;
    }

    /**
     * @return
     */
    public String getOlatResourceDetail() {
        return olatResourceDetail;
    }

    /**
     * @return
     */
    public int getQtiType() {
        return qtiType;
    }

    /**
     * @return
     */
    public long getRepositoryRef() {
        return repositoryRef;
    }

    /**
     * @return
     */
    public float getScore() {
        return score;
    }

    /**
     * @param l
     */
    public void setAssessmentID(final long l) {
        assessmentID = l;
    }

    /**
     * @param identity
     */
    public void setIdentity(final Identity identity) {
        this.identity = identity;
    }

    /**
     * @param b
     */
    public void setIsPassed(final boolean b) {
        isPassed = b;
    }

    /**
     * @param l
     */
    public void setOlatResource(final long l) {
        olatResource = l;
    }

    /**
     * @param string
     */
    public void setOlatResourceDetail(final String string) {
        olatResourceDetail = string;
    }

    /**
     * @param i
     */
    public void setQtiType(final int i) {
        qtiType = i;
    }

    /**
     * @param l
     */
    public void setRepositoryRef(final long l) {
        repositoryRef = l;
    }

    /**
     * @param f
     */
    public void setScore(final float f) {
        score = f;
    }

    /**
     * @return Returns the duration or null if not available (only the case by old testsets that have been generated befor the introduction of the duration field)
     */
    public Long getDuration() {
        return duration;
    }

    /**
     * @param duration
     *            The duration to set.
     */
    public void setDuration(final Long duration) {
        this.duration = duration;
    }
}
