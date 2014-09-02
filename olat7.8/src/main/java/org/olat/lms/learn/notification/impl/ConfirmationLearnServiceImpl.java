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
package org.olat.lms.learn.notification.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.core.notification.service.AssessmentConfirmationInfo;
import org.olat.lms.core.notification.service.ConfirmationInfo;
import org.olat.lms.core.notification.service.ConfirmationService;
import org.olat.lms.core.notification.service.RecipientInfo;
import org.olat.lms.core.notification.service.TaskConfirmation;
import org.olat.lms.core.notification.service.TaskConfirmation.TYPE;
import org.olat.lms.core.notification.service.TestConfirmation;
import org.olat.lms.core.notification.service.TopicChangeConfirmationInfo;
import org.olat.lms.core.notification.service.TopicConfirmation;
import org.olat.lms.core.notification.service.TopicEnrollmentConfirmationInfo;
import org.olat.lms.learn.LearnBaseService;
import org.olat.lms.learn.notification.service.ConfirmationLearnService;
import org.olat.system.commons.service.ServiceContext;
import org.olat.system.commons.service.ServiceMetric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Initial Date: 02.08.2012 <br>
 * 
 * @author lavinia
 */
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class ConfirmationLearnServiceImpl extends LearnBaseService<ServiceMetric<ServiceContext>, ServiceContext> implements ConfirmationLearnService {

    @Autowired
    ConfirmationService confirmationService;

    @Override
    public boolean sendTaskDropStudentConfirmation(Identity student, String courseTitle, Long courseRepositoryEntryId, Long courseNodeId, String fileName) {
        List<Identity> identities = new ArrayList<Identity>();
        identities.add(student);
        String irrelevantCourseNodeTitle = "";
        return sendTaskConfirmation(identities, student, courseTitle, courseRepositoryEntryId, courseNodeId, irrelevantCourseNodeTitle, fileName,
                TaskConfirmation.TYPE.TASK_DROP_STUDENT);
    }

    private boolean sendTaskConfirmation(List<Identity> recipientIdentities, Identity originatorIdentity, String courseTitle, Long courseRepositoryEntryId,
            Long courseNodeId, String courseNodeTitle, String fileName, TYPE confirmationType) {
        TaskConfirmation confirmationInfo = createTaskConfirmation(recipientIdentities, originatorIdentity, courseTitle, courseRepositoryEntryId, courseNodeId,
                courseNodeTitle, fileName, confirmationType);
        return confirmationService.sendConfirmation(confirmationInfo);
    }

    private TaskConfirmation createTaskConfirmation(List<Identity> recipientIdentities, Identity originatorIdentity, String courseTitle, Long courseRepositoryEntryId,
            Long courseNodeId, String courseNodeTitle, String fileName, TYPE type) {
        List<RecipientInfo> recipientInfos = createRecipientInfos(recipientIdentities);

        TaskConfirmation confirmationInfo = TaskConfirmation.createTaskConfirmation(recipientInfos, originatorIdentity, courseTitle, courseRepositoryEntryId,
                courseNodeId, courseNodeTitle, fileName, type, new Date());
        return confirmationInfo;
    }

    @Override
    public boolean sendTopicDropStudentConfirmation(Identity student, String courseTitle, Long courseRepositoryEntryId, Long courseNodeId, String fileName, Long projectId) {
        List<Identity> recipientIdentities = new ArrayList<Identity>();
        recipientIdentities.add(student);
        return sendTopicConfirmation(recipientIdentities, student, courseTitle, courseRepositoryEntryId, courseNodeId, projectId, fileName,
                TopicConfirmation.TYPE.TOPIC_DROP_STUDENT);
    }

    private boolean sendTopicConfirmation(List<Identity> recipientIdentities, Identity originatorIdentity, String courseTitle, Long courseRepositoryEntryId,
            Long courseNodeId, Long projectId, String fileName, TopicConfirmation.TYPE confirmationType) {
        TopicConfirmation confirmationInfo = createTopicConfirmation(recipientIdentities, originatorIdentity, courseTitle, courseRepositoryEntryId, courseNodeId,
                projectId, fileName, confirmationType);
        return confirmationService.sendConfirmation(confirmationInfo);
    }

    private TopicConfirmation createTopicConfirmation(List<Identity> recipientIdentities, Identity originatorIdentity, String courseTitle, Long courseRepositoryEntryId,
            Long courseNodeId, Long projectId, String fileName, TopicConfirmation.TYPE type) {
        List<RecipientInfo> recipientInfos = createRecipientInfos(recipientIdentities);

        TopicConfirmation confirmationInfo = TopicConfirmation.createTaskConfirmation(recipientInfos, originatorIdentity, courseTitle, courseRepositoryEntryId,
                courseNodeId, projectId, fileName, type, new Date());
        return confirmationInfo;
    }

    @Override
    public boolean sendTaskDropTutorConfirmation(List<Identity> recipientIdentities, Identity originatorIdentity, String courseTitle, Long courseRepositoryEntryId,
            Long courseNodeId, String fileName) {
        String irrelevantCourseNodeTitle = "";
        return sendTaskConfirmation(recipientIdentities, originatorIdentity, courseTitle, courseRepositoryEntryId, courseNodeId, irrelevantCourseNodeTitle, fileName,
                TaskConfirmation.TYPE.TASK_DROP_TUTOR);
    }

    @Override
    public boolean sendTopicDropTutorConfirmation(List<Identity> recipientIdentities, Identity originatorIdentity, String courseTitle, Long courseRepositoryEntryId,
            Long courseNodeId, String fileName, Long projectId) {
        return sendTopicConfirmation(recipientIdentities, originatorIdentity, courseTitle, courseRepositoryEntryId, courseNodeId, projectId, fileName,
                TopicConfirmation.TYPE.TOPIC_DROP_TUTOR);
    }

    @Override
    public boolean sendTaskReturnStudentConfirmation(Identity student, Identity tutor, String courseTitle, Long courseRepositoryEntryId, Long courseNodeId,
            String fileName) {
        List<Identity> recipientIdentities = new ArrayList<Identity>();
        recipientIdentities.add(student);
        String irrelevantCourseNodeTitle = "";
        return sendTaskConfirmation(recipientIdentities, tutor, courseTitle, courseRepositoryEntryId, courseNodeId, irrelevantCourseNodeTitle, fileName,
                TaskConfirmation.TYPE.TASK_RETURN_STUDENT);
    }

    @Override
    public boolean sendTopicReturnStudentConfirmation(Identity student, Identity tutor, String courseTitle, Long courseRepositoryEntryId, Long courseNodeId,
            String fileName, Long projectId) {
        List<Identity> recipientIdentities = new ArrayList<Identity>();
        recipientIdentities.add(student);
        return sendTopicConfirmation(recipientIdentities, tutor, courseTitle, courseRepositoryEntryId, courseNodeId, projectId, fileName,
                TopicConfirmation.TYPE.TOPIC_RETURN_STUDENT);
    }

    @Override
    public boolean sendTestSubmitStudentConfirmation(Identity student, String courseTitle, Long courseRepositoryEntryId, Long courseNodeId, String testTitle) {
        List<Identity> identities = new ArrayList<Identity>();
        identities.add(student);
        ConfirmationInfo confirmationInfo = createTestConfirmation(identities, student, courseTitle, courseRepositoryEntryId, courseNodeId, testTitle,
                TestConfirmation.TYPE.TEST_SUBMIT_STUDENT);
        return confirmationService.sendConfirmation(confirmationInfo);
    }

    private TestConfirmation createTestConfirmation(List<Identity> recipientIdentities, Identity originatorIdentity, String courseTitle, Long courseRepositoryEntryId,
            Long courseNodeId, String courseNodeTitle, TestConfirmation.TYPE type) {
        List<RecipientInfo> recipientInfos = createRecipientInfos(recipientIdentities);

        TestConfirmation confirmationInfo = TestConfirmation.createTestConfirmation(recipientInfos, originatorIdentity, courseTitle, courseRepositoryEntryId,
                courseNodeId, courseNodeTitle, new Date(), type);
        return confirmationInfo;
    }

    @Override
    public List<RecipientInfo> createRecipientInfos(List<Identity> recipientIdentities) {
        List<RecipientInfo> recipientInfos = new ArrayList<RecipientInfo>();
        for (Identity identity : recipientIdentities) {
            Locale recipientLocale = new Locale(identity.getUser().getPreferences().getLanguage());
            String recipientEmail = identity.getAttributes().getEmail();
            recipientInfos.add(new RecipientInfo(recipientEmail, recipientLocale));
        }
        return recipientInfos;
    }

    @Override
    public boolean sendTestSubmitTutorConfirmation(List<Identity> recipientIdentities, Identity originatorIdentity, String courseTitle, Long courseRepositoryEntryId,
            Long courseNodeId, String testTitle) {
        ConfirmationInfo confirmationInfo = createTestConfirmation(recipientIdentities, originatorIdentity, courseTitle, courseRepositoryEntryId, courseNodeId,
                testTitle, TestConfirmation.TYPE.TEST_SUBMIT_TUTOR);
        return confirmationService.sendConfirmation(confirmationInfo);
    }

    @Override
    public boolean sendTestReplacedConfirmation(List<Identity> recipientIdentities, Identity originatorIdentity, String courseTitle, Long courseRepositoryEntryId,
            Long courseNodeId, String testTitle, TestConfirmation.TYPE type) {
        ConfirmationInfo confirmationInfo = createTestConfirmation(recipientIdentities, originatorIdentity, courseTitle, courseRepositoryEntryId, courseNodeId,
                testTitle, type);
        return confirmationService.sendConfirmation(confirmationInfo);
    }

    @Override
    public boolean sendGroupConfirmation(ConfirmationInfo groupConfirmationInfo) {
        return confirmationService.sendConfirmation(groupConfirmationInfo);
    }

    @Override
    public boolean sendTaskDeleteConfirmation(List<Identity> recipientIdentities, Identity originatorIdentity, String courseTitle, Long courseRepositoryEntryId,
            Long courseNodeId, String courseNodeTitle, String deletedTask) {
        return sendTaskConfirmation(recipientIdentities, originatorIdentity, courseTitle, courseRepositoryEntryId, courseNodeId, courseNodeTitle, deletedTask,
                TaskConfirmation.TYPE.TASK_DELETE_STUDENT);
    }

    @Override
    protected void setMetrics(List<ServiceMetric<ServiceContext>> metrics) {
        // we are not interested to set any metrics for this learn service, not yet
    }

    @Override
    public boolean sendTopicEnrollConfirmation(List<Identity> recipientIdentities, Identity originatorIdentity, String courseTitle, Long courseRepositoryEntryId,
            Long courseNodeId, Long topicId, String topicName) {
        return sendTopicEnrollmentConfirmation(TopicEnrollmentConfirmationInfo.TYPE.ENROLL, recipientIdentities, originatorIdentity, courseTitle,
                courseRepositoryEntryId, courseNodeId, topicId, topicName);
    }

    @Override
    public boolean sendTopicCancelEnrollConfirmation(List<Identity> recipientIdentities, Identity originatorIdentity, String courseTitle, Long courseRepositoryEntryId,
            Long courseNodeId, Long topicId, String topicName) {
        return sendTopicEnrollmentConfirmation(TopicEnrollmentConfirmationInfo.TYPE.CANCEL_ENROLL, recipientIdentities, originatorIdentity, courseTitle,
                courseRepositoryEntryId, courseNodeId, topicId, topicName);
    }

    private boolean sendTopicEnrollmentConfirmation(TopicEnrollmentConfirmationInfo.TYPE type, List<Identity> recipientIdentities, Identity originatorIdentity,
            String courseTitle, Long courseRepositoryEntryId, Long courseNodeId, Long topicId, String topicName) {
        List<RecipientInfo> recipients = createRecipientInfos(recipientIdentities);
        ConfirmationInfo confirmationInfo = TopicEnrollmentConfirmationInfo.createTopicEnrollmentConfirmationInfo(type, recipients, originatorIdentity, courseTitle,
                courseRepositoryEntryId, new Date(), courseNodeId, topicId, topicName);
        return confirmationService.sendConfirmation(confirmationInfo);
    }

    @Override
    public boolean sendAssessmentConfirmation(AssessmentConfirmationInfo assessmentConfirmationInfo) {
        return confirmationService.sendConfirmation(assessmentConfirmationInfo);
    }

    @Override
    public boolean sendTopicEditConfirmation(List<Identity> recipientIdentities, Identity originatorIdentity, String courseTitle, Long courseRepositoryEntryId,
            Long courseNodeId, Long topicId, String topicName) {
        return sendTopicChangeConfirmation(TopicChangeConfirmationInfo.TYPE.EDIT, recipientIdentities, originatorIdentity, courseTitle, courseRepositoryEntryId,
                courseNodeId, topicId, topicName);
    }

    @Override
    public boolean sendTopicDeleteConfirmation(List<Identity> recipientIdentities, Identity originatorIdentity, String courseTitle, Long courseRepositoryEntryId,
            Long courseNodeId, Long topicId, String topicName) {
        return sendTopicChangeConfirmation(TopicChangeConfirmationInfo.TYPE.DELETE, recipientIdentities, originatorIdentity, courseTitle, courseRepositoryEntryId,
                courseNodeId, topicId, topicName);
    }

    private boolean sendTopicChangeConfirmation(TopicChangeConfirmationInfo.TYPE type, List<Identity> recipientIdentities, Identity originatorIdentity,
            String courseTitle, Long courseRepositoryEntryId, Long courseNodeId, Long topicId, String topicName) {
        List<RecipientInfo> recipients = createRecipientInfos(recipientIdentities);
        ConfirmationInfo confirmationInfo = TopicChangeConfirmationInfo.createTopicChangeConfirmationInfo(type, recipients, originatorIdentity, courseTitle,
                courseRepositoryEntryId, new Date(), courseNodeId, topicId, topicName);
        return confirmationService.sendConfirmation(confirmationInfo);
    }

}
