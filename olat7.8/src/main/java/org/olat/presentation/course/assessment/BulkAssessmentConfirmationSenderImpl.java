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

import java.util.ArrayList;
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.AssessableCourseNode;

/**
 * Initial Date: Nov 13, 2012 <br>
 * 
 * @author Branislav Balaz
 */
public class BulkAssessmentConfirmationSenderImpl extends AssessmentConfirmationSender {

    private final List<Identity> recipients;

    public BulkAssessmentConfirmationSenderImpl(Identity originator, AssessableCourseNode courseNode, ICourse course, List<Object[]> results) {
        super(originator, courseNode, course);
        recipients = getRecipients(results);
    }

    @Override
    public void sendAssessmentConfirmation() {
        sendConfirmation(recipients);
    }

    private List<Identity> getRecipients(List<Object[]> results) {
        List<Identity> recipients = new ArrayList<Identity>();
        for (Object[] result : results) {
            if (isCorrectRecipient(result)) {
                recipients.add((Identity) result[1]);
            }
        }
        return recipients;
    }

    private boolean isCorrectRecipient(Object[] result) {
        return (result[0] instanceof Boolean && ((Boolean) result[0]).equals(Boolean.TRUE)) && result[1] instanceof Identity;
    }

}
