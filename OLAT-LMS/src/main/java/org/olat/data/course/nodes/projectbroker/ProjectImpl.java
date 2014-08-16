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

package org.olat.data.course.nodes.projectbroker;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.commons.database.PersistentObject;
import org.olat.data.group.BusinessGroup;
import org.olat.data.lifecycle.LifeCycleEntry;
import org.olat.data.lifecycle.LifeCycleManager;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * @author guretzki
 */

public class ProjectImpl extends PersistentObject implements Project {
    private static final String CUSTOMFIELD_KEY = "customfield_";

    private static final String EVENT_START = "event_start";
    private static final String EVENT_END = "event_end";

    private static final Logger log = LoggerHelper.getLogger();

    private String title;
    private String description;
    private String state;
    private int maxMembers;
    private BusinessGroup projectGroup;
    private String attachmentFileName;
    private ProjectBroker projectBroker;
    private SecurityGroup candidateGroup;
    private boolean mailNotificationEnabled;

    private Map<String, String> customfields;

    /**
     * Default constructor needs by hibernate
     */
    public ProjectImpl() {
    }

    /**
     * Used to create a new project. Do not call directly, use ProjectBrokerManager.createProjectFor(..) Default value : state = Project.STATE_NOT_ASSIGNED, maxMembers =
     * Project.MAX_MEMBERS_UNLIMITED, mailNotificationEnabled = true, remarks = "", attachmentFileName = ""
     * 
     * @param title
     * @param description
     * @param state
     * @param maxMembers
     * @param remarks
     * @param projectGroup
     * @param attachmentFileName
     * @param projectBroker
     */
    public ProjectImpl(final String title, final String description, final BusinessGroup projectGroup, final ProjectBroker projectBroker) {
        this.title = title;
        this.description = description;
        this.state = Project.STATE_NOT_ASSIGNED;
        this.maxMembers = Project.MAX_MEMBERS_UNLIMITED;
        this.projectGroup = projectGroup;
        this.attachmentFileName = "";
        this.mailNotificationEnabled = true;
        this.projectBroker = projectBroker;
        this.candidateGroup = getBaseSecurity().createAndPersistSecurityGroup();
    }

    private BaseSecurity getBaseSecurity() {
        return (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
    }

    // ////////
    // GETTER
    // ////////
    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getAttachmentFileName() {
        return attachmentFileName;
    }

    /**
     * @return List of Identity objects
     */
    @Override
    public List<Identity> getProjectLeaders() {
        return getBaseSecurity().getIdentitiesOfSecurityGroup(getProjectGroup().getOwnerGroup());
    }

    @Override
    public SecurityGroup getProjectLeaderGroup() {
        return getProjectGroup().getOwnerGroup();
    }

    /**
     * @return SecurityGroup with project participants
     */
    @Override
    public SecurityGroup getProjectParticipantGroup() {
        return getProjectGroup().getPartipiciantGroup();
    }

    /**
     * @return List of Identity objects
     */
    @Override
    public SecurityGroup getCandidateGroup() {
        return candidateGroup;
    }

    @Override
    public String getState() {
        return state;
    }

    @Override
    public int getSelectedPlaces() {
        return getBaseSecurity().countIdentitiesOfSecurityGroup(getProjectParticipantGroup()) + getBaseSecurity().countIdentitiesOfSecurityGroup(getCandidateGroup());
    }

    @Override
    public int getMaxMembers() {
        return maxMembers;
    }

    @Override
    public BusinessGroup getProjectGroup() {
        return projectGroup;
    }

    @Override
    public ProjectBroker getProjectBroker() {
        return projectBroker;
    }

    /**
     * Do not use this method to access the customfields. Hibernate Getter.
     * 
     * @return Map containing the raw properties data
     */
    Map<String, String> getCustomfields() {
        if (customfields == null) {
            setCustomfields(new HashMap<String, String>());
        }
        return customfields;
    }

    // ////////
    // SETTER
    // ////////
    @Override
    public void setTitle(final String title) {
        this.title = title;
    }

    @Override
    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public void setState(final String state) {
        this.state = state;
    }

    @Override
    public void setMaxMembers(final int maxMembers) {
        this.maxMembers = maxMembers;
    }

    public void setProjectGroup(final BusinessGroup projectGroup) {
        this.projectGroup = projectGroup;
    }

    public void setCandidateGroup(final SecurityGroup candidateGroup) {
        this.candidateGroup = candidateGroup;
    }

    public void setAttachmentFileName(final String attachmentFileName) {
        this.attachmentFileName = attachmentFileName;
    }

    public void setProjectBroker(final ProjectBroker projectBroker) {
        this.projectBroker = projectBroker;
    }

    @Override
    public String toString() {
        return "Project [title=" + getTitle() + ", description=" + getDescription() + ", state=" + getState() + "] " + super.toString();
    }

    @Override
    public void setAttachedFileName(final String attachmentFileName) {
        this.attachmentFileName = attachmentFileName;
    }

    @Override
    public int getCustomFieldSize() {
        return customfields.size();
    }

    @Override
    public String getCustomFieldValue(final int index) {
        final String value = customfields.get(CUSTOMFIELD_KEY + index);
        if (value != null) {
            return value;
        } else {
            return "";
        }
    }

    /**
     * Hibernate setter
     * 
     * @param fields
     */
    private void setCustomfields(final Map<String, String> customfields) {
        this.customfields = customfields;
    }

    @Override
    public void setCustomFieldValue(final int index, final String value) {
        log.debug("setValue index=" + index + " : " + value);
        customfields.put(CUSTOMFIELD_KEY + index, value);
    }

    @Override
    public ProjectEvent getProjectEvent(final Project.EventType eventType) {
        final LifeCycleManager lifeCycleManager = LifeCycleManager.createInstanceFor(this);
        final LifeCycleEntry startLifeCycleEntry = lifeCycleManager.lookupLifeCycleEntry(eventType.toString(), EVENT_START);
        final LifeCycleEntry endLifeCycleEntry = lifeCycleManager.lookupLifeCycleEntry(eventType.toString(), EVENT_END);
        Date startDate = null;
        if (startLifeCycleEntry != null) {
            startDate = startLifeCycleEntry.getLcTimestamp();
        }
        Date endDate = null;
        if (endLifeCycleEntry != null) {
            endDate = endLifeCycleEntry.getLcTimestamp();
        }

        final ProjectEvent projectEvent = new ProjectEvent(eventType, startDate, endDate);
        log.debug("getProjectEvent projectEvent=" + projectEvent);
        return projectEvent;
    }

    @Override
    public void setProjectEvent(final ProjectEvent projectEvent) {
        final LifeCycleManager lifeCycleManager = LifeCycleManager.createInstanceFor(this);
        log.debug("setProjectEvent projectEvent=" + projectEvent);
        if (projectEvent.getStartDate() != null) {
            lifeCycleManager.markTimestampFor(projectEvent.getStartDate(), projectEvent.getEventType().toString(), EVENT_START);
        } else {
            lifeCycleManager.deleteTimestampFor(projectEvent.getEventType().toString(), EVENT_START);
            log.debug("delete timestamp for " + projectEvent.getEventType().toString() + EVENT_START);
        }
        if (projectEvent.getEndDate() != null) {
            lifeCycleManager.markTimestampFor(projectEvent.getEndDate(), projectEvent.getEventType().toString(), EVENT_END);
        } else {
            lifeCycleManager.deleteTimestampFor(projectEvent.getEventType().toString(), EVENT_END);
            log.debug("delete timestamp for " + projectEvent.getEventType().toString() + EVENT_END);
        }

    }

    @Override
    public boolean isMailNotificationEnabled() {
        return mailNotificationEnabled;
    }

    /**
     * Hibernate setter
     * 
     * @param mailNotificationEnabled
     */
    @Override
    public void setMailNotificationEnabled(final boolean mailNotificationEnabled) {
        this.mailNotificationEnabled = mailNotificationEnabled;
    }

}
