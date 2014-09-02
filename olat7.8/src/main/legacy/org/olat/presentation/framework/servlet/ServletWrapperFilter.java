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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.presentation.framework.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.data.commons.database.DBFactory;
import org.olat.lms.commons.util.LogRequestInfoFactory;
import org.olat.presentation.framework.dispatcher.DispatcherAction;
import org.olat.system.logging.log4j.LoggerHelper;
import org.apache.log4j.Logger;

/**
 * Description:<br>
 * Use this servlet filter to prepare the servlet request and cleanup database related stuff after the request finished.
 * <P>
 * Initial Date: 29.06.2009 <br>
 * 
 * @author gnaegi
 */
public class ServletWrapperFilter implements Filter {
	private static final Logger log = LoggerHelper.getLogger();


	/**
	 */
	@Override
	public void init(FilterConfig config) throws ServletException {
		// no configuration
	}

	/**
	 */
	@Override
	public void destroy() {
		// nothing to destroy
	}

	/**
	 */
	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain target) throws IOException, ServletException {
		if (!(servletRequest instanceof HttpServletRequest)) {
			// don't know what to do, just execute target and quitt
			target.doFilter(servletRequest, servletResponse);
			return;
		}

		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		Tracing.setLogRequestInfo(LogRequestInfoFactory.createFrom(request));

		boolean success = false;
		try {
			// execute other filters or request
			target.doFilter(request, response);
			// TODO: ORID-1007,DB Bad-Smell: commit to lms layer
			DBFactory.getInstance(false).commitAndCloseSession();
			success = true;
		} catch (Throwable e) {
			log.error("Exception in ServletWrapperFilter", e);
			DispatcherAction.sendBadRequest(request.getPathInfo(), response);
		} finally {
			// execute the cleanup code for this request
			Tracing.setLogRequestInfo(null);

			if (!success) {
				// TODO: ORID-1007,DB Bad-Smell: rollback to lms layer
				DBFactory.getInstance().rollbackAndCloseSession();
			}
		}

	}

}
