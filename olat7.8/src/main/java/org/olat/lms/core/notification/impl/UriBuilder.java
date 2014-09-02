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
package org.olat.lms.core.notification.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.notification.Publisher;
import org.olat.data.notification.Publisher.ContextType;
import org.olat.lms.commons.context.BusinessControlFactory;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.system.commons.Settings;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Context==Course <br/>
 * EventSource==Forum/Wiki/etc. <br/>
 * SourceEntry==Forum Message/etc. <br/>
 * 
 * Initial Date: 20.03.2012 <br>
 * 
 * @author cg
 */
@Component
public class UriBuilder {

    @Autowired
    PublisherTypeHandlerSelector typeHandlerSelector;

    private static final Logger log = LoggerHelper.getLogger();

    String serverContextPathURI;

    private static final String NOTIFICATION_SETTINGS_TAB_ID = "1";
    private static final String NOTIFICATION_NEWS_TAB_ID = "0";
    private static final String REPOSITORY_ENTRY_CONTEXT = "RepositoryEntry";
    private static final String IDENTITY_CONTEXT = "Identity";
    private static final String ASSESSMENT_TOOL_CONTEXT = "assessmentTool";
    private static final String ASSESSMENT_DETAIL_CONTEXT = "assessmentDetail";
    private static final String NOTIFICATION_TAB_CONTEXT = "adminnotifications";
    private static final String URL_PATH_SEPARATOR = "/";
    private static final String URL_CONTEXT = URL_PATH_SEPARATOR + "url" + URL_PATH_SEPARATOR;
    private static final String NOTIFICATION_TAB_CONTEXT_SEPARATOR = ".";
    private static final String TOPIC_ASSIGNMENT_TAB_CONTEXT = "tabId";
    private static final String COURSE_NODE_CONTEXT = "CourseNode";
    private static final String TOPIC_ASSIGNMENT_PROJECT_CONTEXT = "Project";
    private static final String TOPIC_ASSIGNMENT_FOLDER_TAB_ID = "1";
    private static final String TOPIC_ASSIGNMENT_DESCRIPTION_TAB_ID = "0";
    private static final String BUSINESS_GROUP_CONTEXT = "BusinessGroup";

    public UriBuilder() {
    }

    public String getURIToContext(Publisher publisher) {
        return getURI(getBusinessPathToContext(publisher));
    }

    public String getURIToCourse(Long courseRepositoryEntryId) {
        return getURI(getBusinessPathToCourse(courseRepositoryEntryId));
    }

    public String getURIToGroup(Long groupId) {
        return getServerContextPathURI() + URL_CONTEXT + BUSINESS_GROUP_CONTEXT + URL_PATH_SEPARATOR + String.valueOf(groupId);
    }

    public String getURIToCourseNode(Long courseRepositoryEntryId, Long courseNodeId) {
        return getURI(getBusinessPathToCourseNode(courseRepositoryEntryId, courseNodeId));
    }

    private String getBusinessPathToCourseNode(Long courseRepositoryEntryId, Long courseNodeId) {
        return "[RepositoryEntry:" + courseRepositoryEntryId + "][CourseNode:" + courseNodeId + "]";
    }

    public String getURIToEventSource(Publisher publisher) {
        return getURI(getBusinessPathToSource(publisher));
    }

    public String getURIToSourceEntry(Publisher publisher, String sourceEntryId) {
        return getURIToEventSource(publisher) + typeHandlerSelector.getTypeHandler(publisher.getSourceType()).getSourceEntryPath(sourceEntryId);
    }

    /**
     * The implementation uses the legacy jump-in-links semantics. <br>
     * Currently there is only one ContextType (Course), we must extend this when more ContextTypes should be supported.
     * 
     * @param publisher
     */
    private String getBusinessPathToContext(Publisher publisher) {
        String businessPath = "";
        if (ContextType.COURSE.equals(publisher.getContextType())) {
            businessPath = getBusinessPathToCourse(publisher.getContextId());
        } else {
            log.error("Could not build BusinessPathToContext, unkown contextType=" + publisher.getContextType());
            businessPath = "[UNKOWN]";
        }
        return businessPath;
    }

    private String getBusinessPathToCourse(Long courseRepositoryEntryId) {
        return "[RepositoryEntry:" + String.valueOf(courseRepositoryEntryId) + "]";
    }

    private String getBusinessPathToSource(Publisher publisher) {
        return typeHandlerSelector.getTypeHandler(publisher.getSourceType()).getBusinessPathToSource(publisher);
    }

    private String getURI(String businessPath) {
        BusinessControlFactory bCF = BusinessControlFactory.getInstance();
        List<ContextEntry> ceList = bCF.createCEListFromString(businessPath);
        String busPath = getBusPathStringAsURIFromCEList(ceList);
        return getServerContextPathURI() + URL_CONTEXT + busPath;
    }

    private String getBusPathStringAsURIFromCEList(List<ContextEntry> ceList) {
        if (ceList == null || ceList.isEmpty())
            return "";

        StringBuilder retVal = new StringBuilder();
        // see code in JumpInManager, cannot be used, as it needs BusinessControl-Elements, not the path
        for (ContextEntry contextEntry : ceList) {
            String ceStr = contextEntry != null ? contextEntry.toString() : "NULL_ENTRY";
            if (ceStr.startsWith("[path")) {
                // the %2F make a problem on browsers.
                // make the change only for path which is generally used
                // TODO: find a better method or a better separator as |
                ceStr = ceStr.replace("%2F", "~~");
            }
            ceStr = ceStr.replace(':', '/');
            ceStr = ceStr.replaceFirst("\\]", "/");
            ceStr = ceStr.replaceFirst("\\[", "");
            retVal.append(ceStr);
        }
        return retVal.substring(0, retVal.length() - 1);
    }

    /**
     * return the one set (e.g. for tests) if not null, or a default one.
     */
    private String getServerContextPathURI() {
        if (serverContextPathURI != null) {
            return serverContextPathURI;
        }
        return Settings.getServerContextPathURI();
    }

    public void setServerContextPathURI(String serverContextPathURI) {
        this.serverContextPathURI = serverContextPathURI;
    }

    public String getNotificationTabContext() {
        return NOTIFICATION_TAB_CONTEXT;
    }

    // due to existing OLAT implementation this must be simple so built to get correct link to notification settings
    public String getUriToNotificationSettings() {
        return getServerContextPathURI() + URL_CONTEXT + getNotificationTabContext() + URL_PATH_SEPARATOR + NOTIFICATION_SETTINGS_TAB_ID + URL_PATH_SEPARATOR
                + getNotificationTabContext() + NOTIFICATION_TAB_CONTEXT_SEPARATOR + getNotificationTabContext() + URL_PATH_SEPARATOR + NOTIFICATION_SETTINGS_TAB_ID
                + URL_PATH_SEPARATOR;
    }

    // due to existing OLAT implementation this must be simple so built to get correct link to notification news
    public String getUriToNotificationNews() {
        return getServerContextPathURI() + URL_CONTEXT + getNotificationTabContext() + URL_PATH_SEPARATOR + NOTIFICATION_NEWS_TAB_ID + URL_PATH_SEPARATOR
                + getNotificationTabContext() + NOTIFICATION_TAB_CONTEXT_SEPARATOR + getNotificationTabContext() + URL_PATH_SEPARATOR + NOTIFICATION_NEWS_TAB_ID
                + URL_PATH_SEPARATOR;
    }

    public String getUriToAssessmentDetail(Long courseRepositoryEntryId, Long identityId, Long courseNodeId) {
        return getServerContextPathURI() + URL_CONTEXT + REPOSITORY_ENTRY_CONTEXT + URL_PATH_SEPARATOR + String.valueOf(courseRepositoryEntryId) + URL_PATH_SEPARATOR
                + ASSESSMENT_TOOL_CONTEXT + URL_PATH_SEPARATOR + String.valueOf(courseRepositoryEntryId) + URL_PATH_SEPARATOR + IDENTITY_CONTEXT + URL_PATH_SEPARATOR
                + String.valueOf(identityId) + URL_PATH_SEPARATOR + getAssessmentDetailContext() + URL_PATH_SEPARATOR + String.valueOf(courseNodeId);
    }

    public String getUriToAssessmentView(Long courseRepositoryEntryId, Long identityId) {
        return getServerContextPathURI() + URL_CONTEXT + REPOSITORY_ENTRY_CONTEXT + URL_PATH_SEPARATOR + String.valueOf(courseRepositoryEntryId) + URL_PATH_SEPARATOR
                + ASSESSMENT_TOOL_CONTEXT + URL_PATH_SEPARATOR + String.valueOf(courseRepositoryEntryId) + URL_PATH_SEPARATOR + IDENTITY_CONTEXT + URL_PATH_SEPARATOR
                + String.valueOf(identityId);
    }

    public String getAssessmentDetailContext() {
        return ASSESSMENT_DETAIL_CONTEXT;
    }

    public String getTopicAssignmentTabContext() {
        return TOPIC_ASSIGNMENT_TAB_CONTEXT;
    }

    public String getUriToTopicAssignmentFolderTab(Long courseRepositoryEntryId, Long courseNodeId, Long topicId) {
        return getUriToTopicAssignmentTab(courseRepositoryEntryId, courseNodeId, topicId, TOPIC_ASSIGNMENT_FOLDER_TAB_ID);
    }

    public String getUriToTopicAssignmentDescriptionTab(Long courseRepositoryEntryId, Long courseNodeId, Long topicId) {
        return getUriToTopicAssignmentTab(courseRepositoryEntryId, courseNodeId, topicId, TOPIC_ASSIGNMENT_DESCRIPTION_TAB_ID);
    }

    public String getUriToTopicAssignmentTab(Long courseRepositoryEntryId, Long courseNodeId, Long projectId, String tabId) {
        return getServerContextPathURI() + URL_CONTEXT + REPOSITORY_ENTRY_CONTEXT + URL_PATH_SEPARATOR + String.valueOf(courseRepositoryEntryId) + URL_PATH_SEPARATOR
                + COURSE_NODE_CONTEXT + URL_PATH_SEPARATOR + String.valueOf(courseNodeId) + URL_PATH_SEPARATOR + TOPIC_ASSIGNMENT_PROJECT_CONTEXT + URL_PATH_SEPARATOR
                + String.valueOf(projectId) + URL_PATH_SEPARATOR + TOPIC_ASSIGNMENT_TAB_CONTEXT + URL_PATH_SEPARATOR + tabId;
    }

    public String getURIToProject(Long courseRepositoryEntryId, Long courseNodeId, Long projectId) {
        return getServerContextPathURI() + URL_CONTEXT + REPOSITORY_ENTRY_CONTEXT + URL_PATH_SEPARATOR + String.valueOf(courseRepositoryEntryId) + URL_PATH_SEPARATOR
                + COURSE_NODE_CONTEXT + URL_PATH_SEPARATOR + String.valueOf(courseNodeId) + URL_PATH_SEPARATOR + TOPIC_ASSIGNMENT_PROJECT_CONTEXT + URL_PATH_SEPARATOR
                + String.valueOf(projectId);
    }

}
