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

package org.olat.lms.course.nodes.projectbroker;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.course.nodes.projectbroker.CustomField;
import org.olat.data.course.nodes.projectbroker.Project;
import org.olat.data.course.nodes.projectbroker.ProjectEvent;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.properties.CoursePropertyManager;
import org.olat.lms.user.UserService;
import org.olat.presentation.course.nodes.projectbroker.ProjectListController;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * @author Christian Guretzki
 */
public class ProjectBrokerExportGenerator {
    private static final Logger log = LoggerHelper.getLogger();

    private static final String PACKAGE = PackageUtil.getPackageName(ProjectListController.class);

    private static final String END_OF_LINE = "\t\n";
    private static final String TABLE_DELIMITER = "\t";

    /**
     * The results from assessable nodes are written to one row per user into an excel-sheet. An assessable node will only appear if it is producing at least one of the
     * following variables: score, passed, attempts, comments.
     * 
     * @param identities
     * @param myNodes
     * @param course
     * @param locale
     * @return String
     */
    public static String createCourseResultsOverviewTable(final CourseNode courseNode, final ICourse course, final Locale locale) {
        final Translator translator = new PackageTranslator(PACKAGE, locale);
        final StringBuilder table = new StringBuilder();
        final ProjectBrokerModuleConfiguration moduleConfig = new ProjectBrokerModuleConfiguration(courseNode.getModuleConfiguration());

        // load project-list
        final CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
        final Long projectBrokerId = ProjectBrokerManagerFactory.getProjectBrokerManager().getProjectBrokerId(cpm, courseNode);
        if (projectBrokerId != null) {
            final List<Project> projects = ProjectBrokerManagerFactory.getProjectBrokerManager().getProjectListBy(projectBrokerId);
            // build table-header
            table.append(createHeaderLine(translator, moduleConfig));
            // loop over all projects
            for (final Project project : projects) {
                table.append(createProjectDataLine(translator, project, moduleConfig, translator));
            }
        } else {
            log.debug("projectBrokerId is null, courseNode=" + courseNode + " , course=" + course);
        }
        return table.toString();
    }

    private static String createProjectDataLine(final Translator t, final Project project, final ProjectBrokerModuleConfiguration moduleConfig,
            final Translator translator) {
        final StringBuilder line = new StringBuilder();

        line.append(project.getTitle());
        line.append(TABLE_DELIMITER);
        // loop over project leaders
        final StringBuilder projectLeader = new StringBuilder();
        boolean firstElement = true;
        for (final Identity identity : project.getProjectLeaders()) {
            if (!firstElement) {
                projectLeader.append(" , ");
            }
            projectLeader.append(getUserService().getFirstAndLastname(identity.getUser()));
            firstElement = false;
        }
        line.append(projectLeader.toString());
        line.append(TABLE_DELIMITER);

        line.append(t.translate(project.getState()));
        line.append(TABLE_DELIMITER);
        // loop over customfileds
        int customFieldIndex = 0;
        for (final CustomField customField : moduleConfig.getCustomFields()) {
            final String value = project.getCustomFieldValue(customFieldIndex++);
            line.append(value);
            line.append(TABLE_DELIMITER);
        }

        line.append(project.getSelectedPlaces());
        line.append(TABLE_DELIMITER);
        // loop over all events
        for (final Project.EventType eventType : Project.EventType.values()) {
            if (moduleConfig.isProjectEventEnabled(eventType)) {
                final ProjectEvent projectEvent = project.getProjectEvent(eventType);
                if (projectEvent.getStartDate() != null) {
                    line.append(translator.translate("export.event.start.prefix"));
                    line.append(" ");
                    line.append(projectEvent.getFormattedStartDate());
                    line.append(" ");
                }
                if (projectEvent.getEndDate() != null) {
                    line.append(translator.translate("export.event.end.prefix"));
                    line.append(" ");
                    line.append(projectEvent.getFormattedEndDate());
                }
                line.append(TABLE_DELIMITER);
            }
        }
        // loop over all paricipants

        final StringBuilder participants = new StringBuilder();
        boolean firstParticipants = true;
        for (final Identity identity : getBaseSecurity().getIdentitiesOfSecurityGroup(project.getProjectParticipantGroup())) {
            if (!firstParticipants) {
                participants.append(" , ");
            }
            participants.append(getUserService().getFirstAndLastname(identity.getUser()));
            firstParticipants = false;
        }
        line.append(participants.toString());
        line.append(TABLE_DELIMITER);
        line.append(project.getKey().toString());
        line.append(END_OF_LINE);
        return line.toString();
    }

    private static BaseSecurity getBaseSecurity() {
        return (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
    }

    private static String createHeaderLine(final Translator t, final ProjectBrokerModuleConfiguration moduleConfig) {
        final StringBuilder line = new StringBuilder();
        line.append(t.translate("export.header.title"));
        line.append(TABLE_DELIMITER);
        line.append(t.translate("export.header.projectleaders"));
        line.append(TABLE_DELIMITER);
        line.append(t.translate("export.header.projectstate"));
        line.append(TABLE_DELIMITER);
        // loop over enable customfileds
        for (final Iterator<CustomField> iterator = moduleConfig.getCustomFields().iterator(); iterator.hasNext();) {
            line.append(iterator.next().getName());
            line.append(TABLE_DELIMITER);
        }
        line.append(t.translate("export.header.selectedplaces"));
        line.append(TABLE_DELIMITER);
        // loop over events
        for (final Project.EventType eventType : Project.EventType.values()) {
            if (moduleConfig.isProjectEventEnabled(eventType)) {
                line.append(t.translate(eventType.getI18nKey()));
                line.append(TABLE_DELIMITER);
            }
        }
        line.append(t.translate("export.header.projectpaticipants"));
        line.append(TABLE_DELIMITER);
        line.append(t.translate("export.header.projectid"));
        line.append(END_OF_LINE);
        return line.toString();
    }

    private static UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
