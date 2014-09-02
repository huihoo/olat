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

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.olat.core.configuration.AbstractOLATModule;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.logging.OLog;
import org.olat.core.logging.StartupException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.WebappHelper;

public class JMXModule extends AbstractOLATModule {

	private static final Logger log = LoggerHelper.getLogger();

	private OLATIdentifierMBean jmxBeanClass;
	private boolean enableJMXsupport;
	private static int port;
	private static String user, pass;

	/**
	 * [used by spring]
	 */
	private JMXModule() {
		//
	}

	public void setOlatIdentifier(final OLATIdentifierMBean jmxBeanClass) {
		this.jmxBeanClass = jmxBeanClass;
	}

	@Override
	public void init() {
		if (!enableJMXsupport) {
			log.info("JMX support disabled.");
			return;
		} else {
			log.info("JMX support enabled.");
		}
		// expose MBeans
		final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try {
			final ObjectName name = new ObjectName(JMXHelper.buildRegisteredObjectName(jmxBeanClass.getClass(), WebappHelper.getServletContextPath()));
			if (!mbs.isRegistered(name)) {
				mbs.registerMBean(jmxBeanClass, name);
			}
		} catch (final Exception e) {
			throw new StartupException("Error instantiating JMX bean: ", e);
		}
	}

	@Override
	protected void initDefaultProperties() {
		enableJMXsupport = getBooleanConfigParameter("enableJMXsupport", false);
		port = getIntConfigParameter("jmxserverPort", 8999);
		user = getStringConfigParameter("jmxserverUser", "", true);
		pass = getStringConfigParameter("jmxserverPass", "", true);
	}

	@Override
	protected void initFromChangedProperties() {
		// TODO Auto-generated method stub

	}

	public static String getPass() {
		return pass;
	}

	public static int getPort() {
		return port;
	}

	public static String getUser() {
		return user;
	}

	@Override
	public void setPersistedProperties(final PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;
	}

}
