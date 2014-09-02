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
package org.olat.connectors.httpclient;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Initial Date: 27.05.2013 <br>
 * 
 * @author oliver.buehler@agility-informatik.ch
 */
public class OLATSessionTrackingFilter implements Filter {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String LOGGING_KEY_SESSIONID = "sessionid";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // no initialization necessary
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        MDC.put(LOGGING_KEY_SESSIONID, httpRequest.getSession().getId());

        if (log.isDebugEnabled()) {
            final StringBuilder requestedUrl = new StringBuilder();
            requestedUrl.append(httpRequest.getRequestURL());
            if (httpRequest.getQueryString() != null) {
                requestedUrl.append(httpRequest.getQueryString());
            }
            log.debug("request: " + requestedUrl + " for session: " + httpRequest.getRequestedSessionId());
        }

        chain.doFilter(request, response);

        MDC.remove(LOGGING_KEY_SESSIONID);
    }

    @Override
    public void destroy() {
        // not necessary
    }

}
