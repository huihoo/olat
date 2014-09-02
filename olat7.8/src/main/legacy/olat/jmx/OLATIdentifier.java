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
 * Copyright (c) 2005-2006 by JGS goodsolutions GmbH, Switzerland<br>
 * http://www.goodsolutions.ch All rights reserved.
 * <p>
 */
package ch.goodsolutions.olat.jmx;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.core.helpers.Settings;
import org.olat.core.util.UserSession;
import org.olat.core.util.WebappHelper;

public class OLATIdentifier implements OLATIdentifierMBean {

	public static final String IDENTIFIER = "genuineOLAT";
	private static MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();

	private OLATIdentifier() {
		//
	}

	@Override
	public String getInstanceID() {
		return WebappHelper.getInstanceId();
	}

	@Override
	public String getVersion() {
		return Settings.getVersion();
	}

	@Override
	public String getBuild() {
		return Settings.getBuildIdentifier();
	}

	@Override
	public String getContextPath() {
		return WebappHelper.getServletContextPath();
	}

	@Override
	public String getWebAppUri() {
		return Settings.getServerContextPathURI();
	}

	@Override
	public int getAuthenticatedUsersCount() {
		return UserSession.getAuthenticatedUserSessions().size();
	}

	@Override
	public long getMemoryHeapUsageKB() {
		return mbean.getHeapMemoryUsage().getUsed() / 1024;
	}

	@Override
	public long getControllerCount() {
		return DefaultController.getControllerCount();
	}

	@Override
	public long getThreadCount() {
		final ThreadGroup tg = Thread.currentThread().getThreadGroup();
		return tg.activeCount();
	}
}
