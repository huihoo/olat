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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.log4j.Logger;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;

public class JMXHelper {

	public static final String JMX_DOMAIN = "ch.goodsolutions.olat.jmx";

	private final Logger log = Tracing.getLogger(JMXHelper.class);
	private final String serviceURI;
	private final String user, pass;
	private MBeanServerConnection connection;
	private static JMXHelper INSTANCE;

	public static JMXHelper getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new JMXHelper(Settings.getServerconfig("server_fqdn"), JMXModule.getPort(), JMXModule.getUser(), JMXModule.getPass());
		}
		return INSTANCE;
	}

	private JMXHelper(final String host, final int port, final String user, final String pass) {
		this.serviceURI = "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi";
		this.user = user;
		this.pass = pass;
	}

	public List findWebappInstances() {
		final List instances = new ArrayList();
		final Set webappBeanSet = queryNames("Catalina:j2eeType=WebModule,*");

		for (final Iterator iter = webappBeanSet.iterator(); iter.hasNext();) {
			final ObjectName objName = (ObjectName) iter.next();
			final String context = getAttribute(objName, "path");
			final String basePath = getAttribute(objName, "docBase");
			final String state = getAttribute(objName, "state");
			String instanceID = null;
			String version = null;
			String build = null;
			final ObjectName identifierBean = findIdentifierBean(context);
			if (identifierBean != null) {
				instanceID = getAttribute(identifierBean, "InstanceID");
				version = getAttribute(identifierBean, "Version");
				build = getAttribute(identifierBean, "Build");
			}
			instances.add(new AppDescriptor(context, basePath, state, instanceID, version, build));
		}
		return instances;
	}

	private ObjectName findIdentifierBean(final String contextPath) {
		final Set names = queryNames(JMXHelper.buildRegisteredObjectName(OLATIdentifier.class, contextPath));
		if (names.size() == 1) { return (ObjectName) names.iterator().next(); }
		return null;
	}

	public static String buildRegisteredObjectName(final Class clazz, final String contextPath) {
		String className = clazz.getName();
		if (className.indexOf('.') > 0) {
			className = className.substring(className.lastIndexOf('.') + 1);
		}
		final String foo = JMX_DOMAIN + ":class=" + clazz.getName() + ",path=" + contextPath;
		return foo;
	}

	public String getAttribute(final String objectName, final String attribute) {
		try {
			return getAttribute(new ObjectName(objectName), attribute);
		} catch (final Exception e) {
			return null;
		}
	}

	public String getAttribute(final ObjectName objectName, final String attribute) {
		final MBeanServerConnection conn = getConnection();
		if (conn == null) { return null; }
		try {
			return conn.getAttribute(objectName, attribute).toString();
		} catch (final Exception e) {
			return null;
		}
	}

	public Set queryNames(final String query) {
		Set results = new HashSet();
		final MBeanServerConnection conn = getConnection();
		if (conn == null) { return results; }
		try {
			results = conn.queryNames(new ObjectName(query), null);
		} catch (final Exception e) {
			// ignore
		}
		return results;
	}

	public MBeanServerConnection getConnection() {
		if (connection != null) { return connection; }
		try {
			final JMXServiceURL url = new JMXServiceURL(serviceURI);
			final Map map = new HashMap();
			if (user != null) {
				final String[] credentials = new String[] { user, pass };
				map.put("jmx.remote.credentials", credentials);
			}
			final JMXConnector conn = JMXConnectorFactory.connect(url, map);
			connection = conn.getMBeanServerConnection();
		} catch (final Exception e) {
			log.error("Unable to get JMX connection: ", e);
			return null;
		}
		return connection;
	}

}
