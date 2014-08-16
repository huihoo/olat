/**
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * Copyright (c) 2005-2008 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 * All rights reserved.
 */
package de.bps.webservices;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.configuration.Initializable;
import org.olat.core.logging.Tracing;

/**
 * 
 */
public class WebServiceModule implements Initializable {

	private static Map<String, Service> configuration = new HashMap<String, Service>();

	private static Map<String, List<String>> services;

	/**
	 * @return Returns the services.
	 */
	public static Map<String, List<String>> getServices() {
		return services;
	}

	/**
	 * @param services The services to set.
	 */
	public void setServices(final Map<String, List<String>> services) {
		WebServiceModule.services = services;
	}

	/*
	 * private static final String SERVICE = "service"; private static final String SERVICE_NAME = "name"; private static final String TARGET = "target"; private static
	 * final String ID = "id"; private static final String INSTITUTIONAL_NAME = "institutional_name"; private static final String ADDRESS = "address";
	 */

	/**
	 * [used by spring]
	 */
	private WebServiceModule() {
		//
	}

	@Override
	public void init() {
		try {
			for (final String serviceName : services.keySet()) {
				final Map<String, String> mapIdToInstName = new HashMap<String, String>();
				final Map<String, String> mapInstNameToAddress = new HashMap<String, String>();
				final String id = services.get(serviceName).get(0);
				final String instName = Service.ALL_INSTITUTIONS;
				final String address = services.get(serviceName).get(1);
				mapIdToInstName.put(id, instName);
				mapInstNameToAddress.put(instName, address);
	private static final Logger log = LoggerHelper.getLogger();

				final Service service = new Service(serviceName, mapIdToInstName, mapInstNameToAddress);
				configuration.put(service.getName(), service);
			}
		} catch (final Exception e) {
	private static final Logger log = LoggerHelper.getLogger();

		}
	}

	/**
	 * returns a list of all loaded service objects
	 * 
	 * @see Service return list of service objects
	 */
	public static List<Service> getAllServices() {
		return (List<Service>) configuration.values();
	}

	/**
	 * @return true if a service with this name exists, has no correlation to any institution
	 */
	public static boolean existService(final String service) {
		if (configuration.get(service) != null) { return true; }
		return false;
	}

	/**
	 * @return the service object for the given name
	 */
	public static Service getService(final String serviceKey) {
		return configuration.get(serviceKey);
	}

}
