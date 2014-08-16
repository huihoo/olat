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
package org.olat.lms.admin.registration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.olat.connectors.httpclient.HttpClientFactory;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.group.BusinessGroup;
import org.olat.data.group.context.BGContextDao;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.i18n.I18nModule;
import org.olat.lms.course.CourseModule;
import org.olat.lms.instantmessaging.InstantMessagingModule;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.system.commons.CodeHelper;
import org.olat.system.commons.Settings;
import org.olat.system.commons.configuration.Initializable;
import org.olat.system.commons.configuration.PropertyLocator;
import org.olat.system.commons.configuration.SystemPropertiesService;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.exception.OLATRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Description:<br>
 * This manager offers methods to store registration preferences and to register the installation on the olat.org server.
 * <P>
 * Initial Date: 12.12.2008 <br>
 * 
 * @author gnaegi
 * @author guido
 */
public class SystemRegistrationManager extends BasicManager implements Processor, Initializable {

    // Version flag for data xml
    private static final String VERSION = "1.0";
    @Autowired
    BaseSecurity baseSecurity;
    @Autowired
    BaseSecurityEBL baseSecurityEBL;

    // via setter
    private Enregister registrationWorker;

    @Autowired
    SystemPropertiesService propertyService;
    @Autowired
    RepositoryService repositoryService;
    @Autowired
    BGContextDao bgCongtextDao;

    public static final String CONF_KEY_PUBLISH_WEBSITE = "publishWebsite";
    public static final String CONF_KEY_WEBSITE_DESCRIPTION = "websiteDescription";
    public static final String CONF_KEY_NOTIFY_RELEASES = "notifyReleases";
    public static final String CONF_KEY_EMAIL = "email";
    public static final String CONF_KEY_IDENTIFYER = "instanceIdentifyer";
    private String instanceIdentifier;
    private RegistrationModel registrationModel;
    // Where to post the registration. Don't move this to a config, it should not
    // be that easy to modify the registration server URL!
    protected static final String REGISTRATION_SERVER = "http://www.olat.org/olatregistration/registrations/";
    // private static final String REGISTRATION_SERVER = "http://localhost:8088/olatregistration/registrations/";
    // location described by language, e.g. "Winterthurerstrasse 190, Zürich", or "Dresden"....
    public static final String CONF_KEY_LOCATION = "location";
    // the geolocation derived with a google maps service for usage to place markers on a google map
    public static final String CONF_KEY_LOCATION_COORDS = "location_coords";

    /**
     * [used by spring] Use getInstance(), this is a singleton
     */
    protected SystemRegistrationManager() {
    }

    public void setWorker(Enregister registrationWorker) {
        this.registrationWorker = registrationWorker;
    }

    // OLAT-6437
    @Override
    @Scheduled(cron = "* * * * * SUN")
    public void process() {
        final String url = REGISTRATION_SERVER + getorCreateInstanceIdentifier() + "/";
        registrationWorker.register(getRegistrationPropertiesMessage(), url, VERSION);
    }

    /**
     * return coordinates from a location as string
     * 
     * @param textLocation
     * @return
     */
    public String getLocationCoordinates(final String textLocation) {
        String csvCoordinates = null;

        if (textLocation == null || textLocation.length() == 0) {
            return null;
        }

        final HttpClient client = HttpClientFactory.getHttpClientInstance();
        final String url = "http://maps.google.com/maps/geo";
        final NameValuePair[] nvps = new NameValuePair[5];
        nvps[0] = new NameValuePair("q", textLocation);
        nvps[1] = new NameValuePair("output", "csv");
        nvps[2] = new NameValuePair("oe", "utf8");
        nvps[3] = new NameValuePair("sensor", "false");
        nvps[4] = new NameValuePair("key", "ABQIAAAAq5BZJrKbG-xh--W4MrciXRQZTOqTGVCcmpRMgrUbtlJvJ3buAhSfG7H7hgE66BCW17_gLyhitMNP4A");

        final GetMethod getCall = new GetMethod(url);
        getCall.setQueryString(nvps);

        try {
            client.executeMethod(getCall);
            String resp = null;
            if (getCall.getStatusCode() == 200) {
                resp = getCall.getResponseBodyAsString();
                final String[] split = resp.split(",");
                csvCoordinates = split[2] + "," + split[3];
            }
        } catch (final HttpException e) {
            //
        } catch (final IOException e) {
            //
        }

        return csvCoordinates;
    }

    public String getRegistrationPropertiesMessage() {

        final boolean website = propertyService.getBooleanProperty(PropertyLocator.SYSTEM_REG_PUBLISH_WEBSITE);
        final boolean notify = propertyService.getBooleanProperty(PropertyLocator.SYSTEM_REG_NOTIFY_NEW_RELEASES);

        Properties msgProperties = new Properties();
        if (website || notify) {

            msgProperties.setProperty("RegistrationVersion", "1.0");

            // OLAT version
            msgProperties.setProperty("olatAppName", Settings.getApplicationName());
            msgProperties.setProperty("olatVersion", Settings.getFullVersionInfo());
            // System config
            msgProperties.setProperty("configInstantMessagingEnabled", String.valueOf(InstantMessagingModule.isEnabled()));
            msgProperties.setProperty("configLanguages", I18nModule.getEnabledLanguageKeys().toString());
            msgProperties.setProperty("configClusterEnabled", propertyService.getStringProperty(PropertyLocator.CLUSTER_MODE));
            msgProperties.setProperty("configDebugginEnabled", String.valueOf(Settings.isDebuging()));
            // Course counts
            final int allCourses = repositoryService.countByTypeLimitAccess(CourseModule.ORES_TYPE_COURSE, RepositoryEntry.ACC_OWNERS);
            final int publishedCourses = repositoryService.countByTypeLimitAccess(CourseModule.ORES_TYPE_COURSE, RepositoryEntry.ACC_USERS);
            msgProperties.setProperty("courseCountAll", String.valueOf(allCourses));
            msgProperties.setProperty("courseCountPublished", String.valueOf(publishedCourses));
            // User counts
            int numActiveUsers = baseSecurityEBL.getActiveUsersCount();
            msgProperties.setProperty("usersEnabled", String.valueOf(numActiveUsers));

            final int authors = baseSecurityEBL.getAuthorsCount();
            msgProperties.setProperty("usersAuthors", String.valueOf(authors));
            // Activity
            final Calendar lastLoginLimit = Calendar.getInstance();
            lastLoginLimit.add(Calendar.DAY_OF_YEAR, -6); // -1 - 6 = -7 for last
                                                          // week
            msgProperties.setProperty("activeUsersLastWeek", String.valueOf(getUserLoginsSinceCount(lastLoginLimit)));
            lastLoginLimit.add(Calendar.MONTH, -1);
            msgProperties.setProperty("activeUsersLastMonth", String.valueOf(getUserLoginsSinceCount(lastLoginLimit)));
            // Groups
            final int buddyGroups = bgCongtextDao.countGroupsOfType(BusinessGroup.TYPE_BUDDYGROUP);
            msgProperties.setProperty("groupCountBuddyGroups", String.valueOf(buddyGroups));
            final int learningGroups = bgCongtextDao.countGroupsOfType(BusinessGroup.TYPE_LEARNINGROUP);
            msgProperties.setProperty("groupCountLearningGroups", String.valueOf(learningGroups));
            final int rightGroups = bgCongtextDao.countGroupsOfType(BusinessGroup.TYPE_RIGHTGROUP);
            msgProperties.setProperty("groupCountRightGroups", String.valueOf(rightGroups));

            if (website) {
                // URL
                msgProperties.setProperty("websiteURL", Settings.getServerContextPathURI());
                // Description
                final String desc = propertyService.getStringProperty(PropertyLocator.SYSTEM_REG_DESC_WEBSITE);
                msgProperties.setProperty("websiteDescription", desc);
            }
            if (notify) {
                // Email
                final String email = propertyService.getStringProperty(PropertyLocator.SUPPORTEMAIL);
                msgProperties.setProperty("email", email);
            }
        }
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            msgProperties.storeToXML(baos, "OLAT Registration Data, since 6.1.1 Release");
        } catch (final IOException e) {
            throw new OLATRuntimeException("OLAT Registration failed", e);
        }
        String retVal = null;
        try {
            retVal = baos.toString("UTF8");
        } catch (final UnsupportedEncodingException e) {
            throw new OLATRuntimeException("OLAT Registration failed", e);
        }
        return retVal;
    }

    /**
     * @param lastLoginLimit
     * @return
     */
    private Long getUserLoginsSinceCount(final Calendar lastLoginLimit) {
        return baseSecurity.countUniqueUserLoginsSince(lastLoginLimit.getTime());
    }

    private String getorCreateInstanceIdentifier() {
        final String uniqueID = CodeHelper.getGlobalForeverUniqueID();

        // Check if instance identifier property exists
        if (instanceIdentifier == null) {
            MessageDigest digester;
            try {
                digester = MessageDigest.getInstance("MD5");
                digester.update(uniqueID.getBytes(), 0, uniqueID.length());
                String id = new BigInteger(1, digester.digest()).toString(16);
                propertyService.setProperty(PropertyLocator.SYSTEM_REG_INSTANCE_IDENTIFYER, id);
                return id;
            } catch (final NoSuchAlgorithmException e) {
                // using no encoding instead
                propertyService.setProperty(PropertyLocator.SYSTEM_REG_INSTANCE_IDENTIFYER, uniqueID);
            }
        }
        return uniqueID;
    }

    /**
     * @return the model holding the values filled in and sent
     */
    public RegistrationModel getRegistrationModel() {
        return registrationModel;
    }

    @Override
    @PostConstruct
    public void init() {
        this.registrationModel = new RegistrationModel(this, propertyService);
    }

}
