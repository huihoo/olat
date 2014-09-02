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
package org.olat.presentation.course.assessment;

import java.util.Date;
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.core.notification.service.AssessmentConfirmationInfo;
import org.olat.lms.core.notification.service.RecipientInfo;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.AssessableCourseNode;
import org.olat.lms.course.nodes.IQTESTCourseNode;
import org.olat.lms.course.nodes.MSCourseNode;
import org.olat.lms.course.nodes.PortfolioCourseNode;
import org.olat.lms.course.nodes.TACourseNode;
import org.olat.lms.learn.notification.service.ConfirmationLearnService;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: Nov 7, 2012 <br>
 * 
 * @author Branislav Balaz
 */
abstract public class AssessmentConfirmationSender {

    protected final AssessableCourseNode courseNode;
    protected final Identity originator;
    protected final ICourse course;

    protected AssessmentConfirmationSender(Identity originator, AssessableCourseNode courseNode, ICourse course) {
        this.courseNode = courseNode;
        this.course = course;
        this.originator = originator;
    }

    abstract void sendAssessmentConfirmation();

    protected AssessmentConfirmationInfo.TYPE getConfirmationType() {
        AssessmentConfirmationInfo.TYPE type = null;
        if (MSCourseNode.TYPE.equals(courseNode.getType())) {
            type = AssessmentConfirmationInfo.TYPE.ASSESSMENT;
        } else if (IQTESTCourseNode.TYPE.equals(courseNode.getType())) {
            type = AssessmentConfirmationInfo.TYPE.TEST;
        } else if (PortfolioCourseNode.TYPE.equals(courseNode.getType())) {
            type = AssessmentConfirmationInfo.TYPE.PORTFOLIO;
        } else if (TACourseNode.TYPE.equals(courseNode.getType())) {
            type = AssessmentConfirmationInfo.TYPE.TASK;
        }
        return type;
    }

    private ConfirmationLearnService getConfirmationLearnService() {
        return CoreSpringFactory.getBean(ConfirmationLearnService.class);
    }

    protected void sendConfirmation(List<Identity> recipientIdentities) {
        List<RecipientInfo> recipients = getConfirmationLearnService().createRecipientInfos(recipientIdentities);
        AssessmentConfirmationInfo.TYPE type = getConfirmationType();
        AssessmentConfirmationInfo assessmentConfirmationInfo = AssessmentConfirmationInfo.createAssessmentConfirmationInfo(type, recipients, originator,
                course.getCourseTitle(), course.getCourseEnvironment().getRepositoryEntryId(), new Date(), Long.valueOf(courseNode.getIdent()),
                courseNode.getShortTitle());
        getConfirmationLearnService().sendAssessmentConfirmation(assessmentConfirmationInfo);
    }

}
