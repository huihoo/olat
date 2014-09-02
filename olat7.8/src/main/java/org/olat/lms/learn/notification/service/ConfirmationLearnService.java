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
package org.olat.lms.learn.notification.service;

import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.core.notification.service.AssessmentConfirmationInfo;
import org.olat.lms.core.notification.service.ConfirmationInfo;
import org.olat.lms.core.notification.service.RecipientInfo;
import org.olat.lms.core.notification.service.TestConfirmation;
import org.olat.lms.learn.LearnService;

/**
 * A Confirmation is a message sent automatically upon completion of an user action. (e.g. a student submits a test) <br>
 * The method names schema is: send<Context><Action><Recipient>Confirmation, any of Context, Action or Recipient are optional.
 * 
 * <p>
 * Initial Date: 02.08.2012 <br>
 * 
 * @author lavinia
 */
public interface ConfirmationLearnService extends LearnService {

    /**
     * "Drop" means: student uploads a file into the drop box (in task course node).
     */
    boolean sendTaskDropStudentConfirmation(Identity student, String courseTitle, Long courseRepositoryEntryId, Long courseNodeId, String fileName);

    /**
     * "Drop" means: student uploads a file into the drop box (in topic course node).
     */
    boolean sendTopicDropStudentConfirmation(Identity student, String courseTitle, Long courseRepositoryEntryId, Long courseNodeId, String fileName, Long projectId);

    boolean sendTaskDropTutorConfirmation(List<Identity> recipientIdentities, Identity originatorIdentity, String courseTitle, Long courseRepositoryEntryId,
            Long courseNodeId, String fileName);

    boolean sendTopicDropTutorConfirmation(List<Identity> recipientIdentities, Identity originatorIdentity, String courseTitle, Long courseRepositoryEntryId,
            Long courseNodeId, String fileName, Long projectId);

    /**
     * "Return" means: Tutor uploads a file into the return box (in task course node).
     */
    boolean sendTaskReturnStudentConfirmation(Identity student, Identity tutor, String courseTitle, Long courseRepositoryEntryId, Long courseNodeId, String fileName);

    boolean sendTopicReturnStudentConfirmation(Identity student, Identity tutor, String courseTitle, Long courseRepositoryEntryId, Long courseNodeId, String fileName,
            Long projectId);

    /**
     * Student confirmation at submit test.
     */
    boolean sendTestSubmitStudentConfirmation(Identity student, String courseTitle, Long courseRepositoryEntryId, Long courseNodeId, String testTitle);

    boolean sendTestSubmitTutorConfirmation(List<Identity> recipientIdentities, Identity originatorIdentity, String courseTitle, Long courseRepositoryEntryId,
            Long courseNodeId, String testTitle);

    boolean sendTestReplacedConfirmation(List<Identity> recipientIdentities, Identity originatorIdentity, String courseTitle, Long courseRepositoryEntryId,
            Long courseNodeId, String testTitle, TestConfirmation.TYPE type);

    /**
     * sends confirmation when an user is added/removed/moved to/from a group.
     */
    boolean sendGroupConfirmation(ConfirmationInfo groupConfirmationInfo);

    List<RecipientInfo> createRecipientInfos(List<Identity> recipientIdentities);

    /**
     * sends confirmation when an already assigned task is deleted.
     */
    boolean sendTaskDeleteConfirmation(List<Identity> recipientIdentities, Identity originatorIdentity, String courseTitle, Long courseRepositoryEntryId,
            Long courseNodeId, String courseNodeTitle, String deletedTask);

    boolean sendTopicEnrollConfirmation(List<Identity> recipientIdentities, Identity originatorIdentity, String courseTitle, Long courseRepositoryEntryId,
            Long courseNodeId, Long topicId, String topicName);

    boolean sendTopicCancelEnrollConfirmation(List<Identity> recipientIdentities, Identity originatorIdentity, String courseTitle, Long courseRepositoryEntryId,
            Long courseNodeId, Long topicId, String topicName);

    boolean sendAssessmentConfirmation(AssessmentConfirmationInfo assessmentConfirmationInfo);

    boolean sendTopicEditConfirmation(List<Identity> recipientIdentities, Identity originatorIdentity, String courseTitle, Long courseRepositoryEntryId,
            Long courseNodeId, Long topicId, String topicName);

    boolean sendTopicDeleteConfirmation(List<Identity> recipientIdentities, Identity originatorIdentity, String courseTitle, Long courseRepositoryEntryId,
            Long courseNodeId, Long topicId, String topicName);

}
