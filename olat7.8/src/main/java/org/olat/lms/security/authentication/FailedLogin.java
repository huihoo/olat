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
package org.olat.lms.security.authentication;

import java.io.Serializable;

/**
 * The number of login attempts should be limited, to prevent password brute-force attacks.
 * <p>
 * This contains the information about a failed login, how many times has failed, and the time stamp when it got blocked.
 * 
 * 
 * Initial Date: 14.03.2014 <br>
 * 
 * @author lavinia
 */
public class FailedLogin implements Serializable {

    private static final long serialVersionUID = 5854949016235489863L;

    private Integer numAttempts;
    private Long blockedTimestamp;

    FailedLogin(Integer numAttempts_, Long blockedTimestamp_) {
        numAttempts = numAttempts_;
        blockedTimestamp = blockedTimestamp_;
    }

    public boolean isTooManyAttempts(int attackPreventionMaxAttempts) {
        return getNumAttempts().intValue() > attackPreventionMaxAttempts;
    }

    public Integer getNumAttempts() {
        return numAttempts;
    }

    public void setNumAttempts(Integer numAttempts) {
        this.numAttempts = numAttempts;
    }

    /**
     * @ Returns true, if the elapsed time since blockedTimestamp is smaller that the blockingTimeMin, else false.
     */
    public boolean isLoginBlocked(Long blockingTimeMin) {
        if (blockedTimestamp == null) {
            return false;
        }
        Long elapsedTimeMills = System.currentTimeMillis() - blockedTimestamp.longValue();
        if (elapsedTimeMills <= blockingTimeMin * 60000) {
            return true;
        }
        return false;
    }

    public void setBlockedTimestamp(Long blockedTimestamp) {
        this.blockedTimestamp = blockedTimestamp;
    }

}
