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
import java.util.Map;

/**
 * The ServiceObject is used to store the assignment of a service name to pairs of institutions and addresses for which the service is available.
 */
public class Service {

	public static final String ALL_INSTITUTIONS = "serviceAvailableForAllInstitutions";

	private final String serviceName;
	private Map<String, String> mapIdToInstName = new HashMap<String, String>();
	private Map<String, String> mapInstNameToAddress = new HashMap<String, String>();

	/**
	 * Construct a service with mapping between institution and address
	 * 
	 * @param Service name
	 * @param IDs
	 * @param Institutional names
	 * @param Addresses
	 */
	Service(final String serviceName, final String[] id, final String[] inst_name, final String[] address) {
		this.serviceName = serviceName;
		for (int cnt = 0; cnt < inst_name.length; cnt++) {
			this.mapIdToInstName.put(id[cnt], inst_name[cnt]);
			this.mapInstNameToAddress.put(inst_name[cnt], address[cnt]);
		}
	}

	/**
	 * Construct a service with mapping between institution and address
	 * 
	 * @param Service name
	 * @param Map with ID to InstName
	 * @param Map with InstName to Address
	 */
	Service(final String serviceName, final Map<String, String> mapIdToInstName, final Map<String, String> mapInstNameToAddress) {
		this.serviceName = serviceName;
		this.mapIdToInstName = mapIdToInstName;
		this.mapInstNameToAddress = mapInstNameToAddress;
	}

	/**
	 * Construct a service, that isn't specific for any institution
	 * 
	 * @param id
	 * @param serviceName
	 * @param adress
	 */
	Service(final String id, final String serviceName, final String adress) {
		this.serviceName = serviceName;
		this.mapIdToInstName.put(id, ALL_INSTITUTIONS);
		this.mapInstNameToAddress.put(ALL_INSTITUTIONS, adress);
	}

	/**
	 * @return Service name
	 */
	public String getName() {
		return serviceName;
	}

	/**
	 * @return the URL to the webservice for every institution
	 */
	public String getAddress() {
		return getAddressForInstitution(ALL_INSTITUTIONS);
	}

	/**
	 * @return the URL to the webservice for a given institution, null if the service is not available for this institution
	 */
	public String getAddressForInstitution(final String institution) {
		return mapInstNameToAddress.get(institution);
	}

	/**
	 * @param id
	 * @return address of the corresponding web service
	 */
	public String getAddressForId(final String id) {
		return getAddressForInstitution(getInstitutionForId(id));
	}

	/**
	 * @param id
	 * @return name of the corresponding institution
	 */
	public String getInstitutionForId(final String id) {
		return mapIdToInstName.get(id);
	}

	public String getIdForInstitution(final String institution) {
		String id = null;
		if (mapIdToInstName.containsValue(institution)) {
			for (final String tmpId : mapIdToInstName.keySet()) {
				final String tmpInst = mapIdToInstName.get(tmpId);
				if (tmpInst.equals(institution)) {
					id = tmpId;
					break;
				}
			}
		}
		return id;
	}
}
