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

package org.olat.lms.ims.qti;

/**
 * Description:<br>
 * Editing a qti V1.2 resource is allowed since releas 4.1.x for non-structural changes. These changes are captured as change logs and stored along with the qti resource.
 * <p>
 * A a change log message can be made public or kept private. Whereas public means visible for everyone able to access the resource. In contrast to private, restricting
 * change log message visibility to the resource owners and owners of a course referencing the qti resource.
 * <p>
 * Initial Date: Feb 1, 2006 <br>
 * 
 * @author patrick
 */
public class QTIChangeLogMessage implements Comparable {

    private final boolean isPublic;
    private final String logMessage;
    private final long timestmp;

    /**
     * @param logMessage
     * @param isPublic
     */
    public QTIChangeLogMessage(final String logMessage, final boolean isPublic) {
        this(logMessage, isPublic, System.currentTimeMillis());
    }

    /**
     * @param logMessage
     * @param isPublic
     * @param timestmp
     */
    public QTIChangeLogMessage(final String logMessage, final boolean isPublic, final long timestmp) {
        this.logMessage = logMessage;
        this.isPublic = isPublic;
        this.timestmp = timestmp;
    }

    /**
     * @return Returns the isPublic.
     */
    public boolean isPublic() {
        return isPublic;
    }

    /**
     * @return Returns the logMessage.
     */
    public String getLogMessage() {
        return logMessage;
    }

    /**
     * @return Returns the timestmp.
     */
    public long getTimestmp() {
        return timestmp;
    }

    /**
	 */
    @Override
    public int compareTo(final Object arg0) {
        final QTIChangeLogMessage b = (QTIChangeLogMessage) arg0;
        final long diff = this.getTimestmp() - b.getTimestmp();
        // this ordering makes Arrays.sort(..) to sort the change log messages ascending
        // whereas ascending means older timestamp before newer timestamp
        if (diff < 0) {
            // this is older then b
            return -1;
        } else if (diff == 0) {
            return 0;
        } else {
            // this is newer then b
            return 1;
        }
    }
}
