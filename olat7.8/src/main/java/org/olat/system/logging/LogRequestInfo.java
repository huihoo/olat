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
 * Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.system.logging;

public class LogRequestInfo {
    private String remoteIp;
    private String userAgent;
    private String referer;
    private String identityName;
    private static final String N_A = "n/a";

    /**
     * @param remoteIp
     * @param userAgent
     * @param referer
     * @param identityName
     */
    public LogRequestInfo(String remoteIp, String userAgent, String referer, String identityName) {
        super();
        this.remoteIp = remoteIp;
        this.userAgent = userAgent;
        this.referer = referer;
        this.identityName = identityName;
    }

    /**
	 * 
	 */
    public LogRequestInfo() {
    }

    /**
     * @return Returns the remoteIp.
     */
    public String getRemoteIp() {
        return remoteIp == null ? N_A : remoteIp;
    }

    /**
     * @return Returns the userAgent.
     */
    public String getUserAgent() {
        return userAgent == null ? N_A : userAgent;
    }

    /**
     * @return Returns the referer.
     */
    public String getReferer() {
        return referer == null ? N_A : referer;
    }

    /**
     * @return Returns the identityName.
     */
    public String getIdentityName() {
        return identityName == null ? N_A : identityName;
    }

}
