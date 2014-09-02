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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.lms.calendar;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.calendar.CalendarDao;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.properties.NarrowedPropertyManager;
import org.olat.system.commons.Settings;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<BR>
 * Constants and helper methods for the OLAT iCal feeds
 * <P>
 * Initial Date: June 2, 2008
 * 
 * @author Udit Sajjanhar
 */
public class ICalTokenGenerator {

    private static final Logger log = LoggerHelper.getLogger();

    /** Authentication provider name for iCal authentication **/
    public static final String ICAL_AUTH_PROVIDER = "ICAL-OLAT";
    /** Key under which the users iCal token is beeing kept in the http session **/
    public static final String ICAL_AUTH_TOKEN_KEY = "icaltoken";

    /** OLAT server URL **/
    public static final String URI_SERVER = Settings.getServerContextPathURI() + "/";
    /** path prefix for personal iCal feed **/
    public static final String ICAL_PREFIX_PERSONAL = "/user/";
    /** path prefix for course iCal feed **/
    public static final String ICAL_PREFIX_COURSE = "/course/";
    /** path prefix for group iCal feed **/
    public static final String ICAL_PREFIX_GROUP = "/group/";

    public static final int ICAL_PATH_SHIFT = 1;
    /** Expected number of tokens in the course/group calendar link **/
    public static final int ICAL_PATH_TOKEN_LENGTH = 4;
    /** Expected number of tokens in the personal calendar link **/
    public static final int ICAL_PERSONAL_PATH_TOKEN_LENGTH = ICAL_PATH_TOKEN_LENGTH - 1;

    /** collection of iCal feed prefixs **/
    public static final String[] ICAL_PREFIX_COLLECTION = { ICAL_PREFIX_PERSONAL, ICAL_PREFIX_COURSE, ICAL_PREFIX_GROUP };

    /** category for the iCal AUTH_TOKEN property **/
    public static final String PROP_CAT_ICALTOKEN = "icalAuthToken";
    /** name for the iCal AUTH_TOKEN property **/
    public static final String PROP_NAME_ICALTOKEN = "authToken";

    private static String createIcalAuthToken(final OLATResourceable resourceable, final Identity identity) {
        // generate the random alpha numeric token
        final String token = RandomStringUtils.randomAlphanumeric(6);

        // save token as a property of resourceable
        final NarrowedPropertyManager npm = NarrowedPropertyManager.getInstance(resourceable);
        final PropertyImpl tokenProperty = npm.createPropertyInstance(identity, null, PROP_CAT_ICALTOKEN, PROP_NAME_ICALTOKEN, null, null, token, null);
        npm.saveProperty(tokenProperty);

        // return the token generated
        return token;
    }

    private static String createIcalAuthToken(final Identity identity) {
        // generate the random alpha numeric token
        final String token = RandomStringUtils.randomAlphabetic(6);

        // save token as a property of resourceable
        final PropertyManager pm = PropertyManager.getInstance();
        final PropertyImpl tokenProperty = pm.createPropertyInstance(identity, null, null, PROP_CAT_ICALTOKEN, PROP_NAME_ICALTOKEN, null, null, token, null);
        pm.saveProperty(tokenProperty);

        // return the generated token
        return token;
    }

    private static String getIcalAuthToken(final OLATResourceable resourceable, final Identity identity, final boolean create) {
        // find the property for the resourceable
        final NarrowedPropertyManager npm = NarrowedPropertyManager.getInstance(resourceable);
        final PropertyImpl tokenProperty = npm.findProperty(identity, null, PROP_CAT_ICALTOKEN, PROP_NAME_ICALTOKEN);

        String token;
        if (tokenProperty == null && create) {
            token = createIcalAuthToken(resourceable, identity);
        } else {
            token = tokenProperty.getStringValue();
        }

        // return the string value for the property
        return token;
    }

    private static String getIcalAuthToken(final Identity identity, final boolean create) {
        // find the property for the identity
        final PropertyManager pm = PropertyManager.getInstance();
        final PropertyImpl tokenProperty = pm.findProperty(identity, null, null, PROP_CAT_ICALTOKEN, PROP_NAME_ICALTOKEN);

        String token;
        if (tokenProperty == null && create) {
            token = createIcalAuthToken(identity);
        } else {
            token = tokenProperty.getStringValue();
        }

        // return the string value for the property
        return token;
    }

    private static String regenerateIcalAuthToken(final OLATResourceable resourceable, final Identity identity) {
        // find the property for the resourceable
        final NarrowedPropertyManager npm = NarrowedPropertyManager.getInstance(resourceable);
        final PropertyImpl tokenProperty = npm.findProperty(identity, null, PROP_CAT_ICALTOKEN, PROP_NAME_ICALTOKEN);

        // genearate the new token
        final String authToken = RandomStringUtils.randomAlphanumeric(6);

        // set new auth token as the string value of the property
        tokenProperty.setStringValue(authToken);

        // update the property
        npm.updateProperty(tokenProperty);

        // return the new token
        return authToken;
    }

    private static String regenerateIcalAuthToken(final Identity identity) {
        // find the property for the identity
        final PropertyManager pm = PropertyManager.getInstance();
        final PropertyImpl tokenProperty = pm.findProperty(identity, null, null, PROP_CAT_ICALTOKEN, PROP_NAME_ICALTOKEN);

        // genearate the new token
        final String authToken = RandomStringUtils.randomAlphanumeric(6);

        // set new auth token as the string value of the property
        tokenProperty.setStringValue(authToken);

        // update the property
        pm.updateProperty(tokenProperty);

        // return the new token
        return authToken;
    }

    public static void destroyIcalAuthToken(final String calendarType, final String calendarID, final Identity identity) {
        if (!calendarType.equals(CalendarDao.TYPE_USER)) {
            // find the property for the resourceable
            final OLATResourceable resourceable = getResourceable(calendarType, calendarID);
            final NarrowedPropertyManager npm = NarrowedPropertyManager.getInstance(resourceable);
            final PropertyImpl tokenProperty = npm.findProperty(identity, null, PROP_CAT_ICALTOKEN, PROP_NAME_ICALTOKEN);
            if (tokenProperty != null) {
                npm.deleteProperty(tokenProperty);
            }
        } else {
            final PropertyManager pm = PropertyManager.getInstance();
            final PropertyImpl tokenProperty = pm.findProperty(identity, null, null, PROP_CAT_ICALTOKEN, PROP_NAME_ICALTOKEN);
            if (tokenProperty != null) {
                pm.deleteProperty(tokenProperty);
            }
        }
    }

    private static void destroyIcalAuthToken(final Identity identity) {
        // find the property for the identity
        final PropertyManager pm = PropertyManager.getInstance();
        final PropertyImpl tokenProperty = pm.findProperty(identity, null, null, PROP_CAT_ICALTOKEN, PROP_NAME_ICALTOKEN);

        // return the string value of the property
        pm.deleteProperty(tokenProperty);
    }

    private static Identity getIdentity(final String userName) {
        final Identity identity = getBaseSecurity().findIdentityByName(userName);
        if (identity == null) {
            // error - abort
            log.error("Identity not found for the username: " + userName);
        }
        return identity;
    }

    private static BaseSecurity getBaseSecurity() {
        return (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
    }

    private static OLATResourceable getResourceable(final String calendarType, final String calendarID) {
        OLATResourceable resourceable;

        if (calendarType.equals(CalendarDao.TYPE_GROUP)) {
            // get the group
            BusinessGroupService businessGroupService = (BusinessGroupService) CoreSpringFactory.getBean(BusinessGroupService.class);
            resourceable = businessGroupService.loadBusinessGroup(new Long(Long.parseLong(calendarID)), false);
            if (resourceable == null) {
                // error
                log.error("Group not found for the Resourceableid: " + calendarID);
                return null;
            }
        } else if ((calendarType.equals(CalendarDao.TYPE_COURSE))) {
            try {
                // get the course
                resourceable = CourseFactory.loadCourse(new Long(Long.parseLong(calendarID)));
            } catch (final Exception e) {
                log.error("Course not found for the Resourceableid: " + calendarID);
                return null;
            }
        } else {
            // error - abort
            log.error("Unmatching Calendar Type: " + calendarType);
            return null;
        }

        return resourceable;
    }

    private static String constructIcalFeedPath(final String calendarType, final String userName, final String authToken, final String calendarID) {
        if (calendarType.equals(CalendarDao.TYPE_USER)) {
            return URI_SERVER + "ical" + "/" + calendarType + "/" + userName + "/" + authToken + ".ics";
        } else {
            return URI_SERVER + "ical" + "/" + calendarType + "/" + userName + "/" + authToken + "/" + calendarID + ".ics";
        }
    }

    /**
     * returns the authentication token for the calendar type and calendar id. authentication token is stored as a property.
     * 
     * @param calendarType
     * @param calendarID
     * @param userName
     * @param createToken
     *            create a new token if it doesn't exist
     * @return authentication token
     */
    public static String getIcalAuthToken(final String calendarType, final String calendarID, final String userName, final boolean createToken) {

        // get the identity of the user
        final Identity identity = getIdentity(userName);
        if (identity == null) {
            return null;
        }

        return getIcalAuthToken(calendarType, calendarID, identity, createToken);
    }

    /**
     * returns the authentication token for the calendar type and calendar id. authentication token is stored as a property.
     * 
     * @param calendarType
     * @param calendarID
     * @param identity
     * @param createToken
     *            createToken create a new token if it doesn't exist
     * @return authentication token
     */
    public static String getIcalAuthToken(final String calendarType, final String calendarID, final Identity identity, final boolean createToken) {

        if (!calendarType.equals(CalendarDao.TYPE_USER)) {
            // get the resourceable
            final OLATResourceable resourceable = getResourceable(calendarType, calendarID);
            if (resourceable == null) {
                return null;
            }
            return getIcalAuthToken(resourceable, identity, createToken);
        } else {
            return getIcalAuthToken(identity, createToken);
        }
    }

    /**
     * regenerates the authentication token for the calendar type and calendar id. returns the generated token
     * 
     * @param calendarType
     * @param calendarID
     * @param identity
     * @return authentication token
     */
    public static String regenerateIcalAuthToken(final String calendarType, final String calendarID, final Identity identity) {

        if (!calendarType.equals(CalendarDao.TYPE_USER)) {
            // get the resourceable
            final OLATResourceable resourceable = getResourceable(calendarType, calendarID);
            if (resourceable == null) {
                return null;
            }
            return regenerateIcalAuthToken(resourceable, identity);
        } else {
            return regenerateIcalAuthToken(identity);
        }
    }

    /**
     * return the ical feed link for the calendar. authentication token is created if it doesn't exist already.
     * 
     * @param calendarType
     * @param calendarID
     * @param identity
     * @return
     */
    public static String getIcalFeedLink(final String calendarType, final String calendarID, final Identity identity) {

        // get the authentication token
        final String authToken = getIcalAuthToken(calendarType, calendarID, identity, true);

        return constructIcalFeedPath(calendarType, identity.getName(), authToken, calendarID);
    }

    /**
     * Check if iCalFeedLink exist
     * 
     * @param calendarType
     * @param calendarID
     * @param identity
     * @return
     */
    public static boolean existIcalFeedLink(final String calendarType, final String calendarID, final Identity identity) {
        PropertyImpl tokenProperty = null;
        if (!calendarType.equals(CalendarDao.TYPE_USER)) {
            // find the property for the resourceable
            final OLATResourceable resourceable = getResourceable(calendarType, calendarID);
            final NarrowedPropertyManager npm = NarrowedPropertyManager.getInstance(resourceable);
            tokenProperty = npm.findProperty(identity, null, PROP_CAT_ICALTOKEN, PROP_NAME_ICALTOKEN);
        } else {
            final PropertyManager pm = PropertyManager.getInstance();
            tokenProperty = pm.findProperty(identity, null, null, PROP_CAT_ICALTOKEN, PROP_NAME_ICALTOKEN);
        }
        return tokenProperty != null;
    }
}
