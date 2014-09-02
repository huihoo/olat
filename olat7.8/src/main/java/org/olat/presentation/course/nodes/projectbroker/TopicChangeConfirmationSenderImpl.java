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
package org.olat.presentation.course.nodes.projectbroker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.course.nodes.projectbroker.Project;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.lms.learn.notification.service.ConfirmationLearnService;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: Nov 28, 2012 <br>
 * 
 * @author Branislav Balaz
 */
public class TopicChangeConfirmationSenderImpl implements TopicChangeConfirmationSender {

    private final Identity identity;
    private final Project project;
    private final CourseEnvironment courseEnvironment;
    private final CourseNode courseNode;

    public TopicChangeConfirmationSenderImpl(Identity identity, Project project, CourseEnvironment courseEnvironment, CourseNode courseNode) {
        this.identity = identity;
        this.project = project;
        this.courseEnvironment = courseEnvironment;
        this.courseNode = courseNode;
    }

    @Override
    public void sendTopicEditConfirmation() {
        List<Identity> recipients = getRecipients();
        getConfirmationLearnService().sendTopicEditConfirmation(recipients, identity, courseEnvironment.getCourseTitle(), courseEnvironment.getRepositoryEntryId(),
                Long.valueOf(courseNode.getIdent()), project.getKey(), project.getTitle());
    }

    private List<Identity> getRecipients() {
        Set<Identity> participants = new HashSet<Identity>(getBaseSecurity().getIdentitiesOfSecurityGroup(project.getProjectParticipantGroup()));
        Set<Identity> candidates = new HashSet<Identity>(getBaseSecurity().getIdentitiesOfSecurityGroup(project.getCandidateGroup()));
        List<Identity> recipients = new ArrayList<Identity>();
        recipients.addAll(participants);
        recipients.addAll(candidates);
        return recipients;
    }

    @Override
    public void sendTopicDeleteConfirmation() {
        List<Identity> recipients = getRecipients();
        getConfirmationLearnService().sendTopicDeleteConfirmation(recipients, identity, courseEnvironment.getCourseTitle(), courseEnvironment.getRepositoryEntryId(),
                Long.valueOf(courseNode.getIdent()), project.getKey(), project.getTitle());
    }

    private ConfirmationLearnService getConfirmationLearnService() {
        return CoreSpringFactory.getBean(ConfirmationLearnService.class);
    }

    private BaseSecurity getBaseSecurity() {
        return CoreSpringFactory.getBean(BaseSecurity.class);
    }

}
