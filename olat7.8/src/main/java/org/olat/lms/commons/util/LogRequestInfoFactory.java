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
package org.olat.lms.commons.util;

import javax.servlet.http.HttpServletRequest;

import org.olat.presentation.commons.session.UserSession;
import org.olat.system.logging.LogRequestInfo;

/**
 * TODO: Class Description for Snippet
 * 
 * <P>
 * Initial Date: 18.04.2011 <br>
 * 
 * @author guretzki
 */
public class LogRequestInfoFactory {

    /**
     * @param request
     * @return
     */
    public static LogRequestInfo createFrom(HttpServletRequest request) {
        UserSession usess = UserSession.getUserSessionIfAlreadySet(request);
        if (usess != null) {
            String identityName = null;
            if (usess.getIdentity() != null) {
                identityName = usess.getIdentity().getName();
            }
            String remoteIp = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");
            String referer = request.getHeader("Referer");
            return new LogRequestInfo(remoteIp, userAgent, referer, identityName);
        }
        return new LogRequestInfo();
    }
}
