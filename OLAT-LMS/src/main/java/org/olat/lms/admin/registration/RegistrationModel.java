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

import org.olat.system.commons.StringHelper;
import org.olat.system.commons.configuration.PropertyLocator;
import org.olat.system.commons.configuration.SystemPropertiesService;
import org.olat.system.mail.MailHelper;

/**
 * model to pass between Controller and Service
 * 
 * <P>
 * Initial Date: 19.07.2011 <br>
 * 
 * @author guido
 */
public class RegistrationModel {

    private SystemPropertiesService propertiesService;
    private SystemRegistrationManager registrationManager;

    /**
     * access via system registration service
     */
    protected RegistrationModel(SystemRegistrationManager registrationManager, SystemPropertiesService propertiesService) {
        this.registrationManager = registrationManager;
        this.propertiesService = propertiesService;
    }

    public String getWebsiteDescription() {
        return propertiesService.getStringProperty(PropertyLocator.SYSTEM_REG_DESC_WEBSITE);
    }

    public void setWebsiteDescription(String websiteDescription) {
        propertiesService.setProperty(PropertyLocator.SYSTEM_REG_NOTIFY_NEW_RELEASES, websiteDescription);
    }

    public boolean publishWebsite() {
        return propertiesService.getBooleanProperty(PropertyLocator.SYSTEM_REG_PUBLISH_WEBSITE);
    }

    public void setPublishWebsite(boolean publishWebsite) {
        propertiesService.setProperty(PropertyLocator.SYSTEM_REG_NOTIFY_NEW_RELEASES, Boolean.valueOf(publishWebsite).toString());
    }

    public String getLocation() {
        return propertiesService.getStringProperty(PropertyLocator.SYSTEM_REG_LOCATION);
    }

    public void setLocation(String location) {
        propertiesService.setProperty(PropertyLocator.SYSTEM_REG_NOTIFY_NEW_RELEASES, location);
    }

    public String getLocationCoordinates() {
        return propertiesService.getStringProperty(PropertyLocator.SYSTEM_REG_LOCATION_COORD);
    }

    public void setLocationCoordinates(String location) {
        String locationCoordinates = registrationManager.getLocationCoordinates(location);

        propertiesService.setProperty(PropertyLocator.SYSTEM_REG_NOTIFY_NEW_RELEASES, locationCoordinates);
    }

    public boolean notifyAboutNewReleases() {
        return propertiesService.getBooleanProperty(PropertyLocator.SYSTEM_REG_NOTIFY_NEW_RELEASES);
    }

    public void setNotifyAboutNewReleases(boolean notifyAboutNewReleases) {
        propertiesService.setProperty(PropertyLocator.SYSTEM_REG_NOTIFY_NEW_RELEASES, Boolean.valueOf(notifyAboutNewReleases).toString());
    }

    public String getNotificationEmail() {
        return propertiesService.getStringProperty(PropertyLocator.SYSTEM_REG_EMAIL);
    }

    public void setNotificationEmail(String notificationEmail) {
        propertiesService.setProperty(PropertyLocator.SYSTEM_REG_EMAIL, notificationEmail);
    }

    public boolean isValidEmail() {
        String email = propertiesService.getStringProperty(PropertyLocator.SYSTEM_REG_EMAIL);
        return (MailHelper.isValidEmailAddress(email) && StringHelper.containsNonWhitespace(email));
    }

}
