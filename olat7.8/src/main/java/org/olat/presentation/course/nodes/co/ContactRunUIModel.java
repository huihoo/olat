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
package org.olat.presentation.course.nodes.co;

import java.util.Stack;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.commons.mail.ContactMessage;
import org.olat.system.mail.ContactList;

/**
 * Initial Date: Oct 5, 2011 <br>
 * 
 * @author patrick
 */
public class ContactRunUIModel {

    private String shortTitle;
    private String longTitle;
    private String learningObjectives;
    private CourseContactMessageUIModel courseContactMessageUIModel;
    private Identity identity;

    /**
     * @param shortTitle
     * @param longTitle
     * @param learningObjectives
     * @param identity
     * @param courseContactMessageUIModel2
     */
    public ContactRunUIModel(String shortTitle, String longTitle, String learningObjectives, Identity identity, CourseContactMessageUIModel courseContactMessageUIModel) {
        this.shortTitle = shortTitle;
        this.longTitle = longTitle;
        this.learningObjectives = learningObjectives;
        this.identity = identity;
        this.courseContactMessageUIModel = courseContactMessageUIModel;
    }

    /**
     * @return
     */
    public String getShortTitle() {
        return shortTitle;
    }

    /**
     * @return
     */
    public String getLongTitle() {
        return longTitle;
    }

    /**
     * @return
     */
    public String getLearningObjectives() {
        return learningObjectives;
    }

    /**
     * @return
     */
    public ContactMessage getCourseContactMessage() {
        ContactMessage contactMessage = new ContactMessage(identity);
        Stack<ContactList> contactLists = courseContactMessageUIModel.getContactLists();

        while (!contactLists.empty()) {
            ContactList cl = contactLists.pop();
            contactMessage.addEmailTo(cl);
        }

        contactMessage.setBodyText(courseContactMessageUIModel.getmBody());
        contactMessage.setSubject(courseContactMessageUIModel.getmSubject());

        return contactMessage;
    }

    /**
     * @return
     */
    public boolean hasRecipients() {
        return courseContactMessageUIModel.getContactLists().size() > 0;
    }

}
