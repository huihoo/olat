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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.mail.ContactList;

/**
 * Initial Date: Oct 5, 2011 <br>
 * 
 * @author patrick
 */
public class CourseContactMessageUIModel {

    static final String KEY_RECIPIENTS = "recipients";
    static final String KEY_FORM_MESSAGE_CHCKBX_PARTIPS = "form.message.chckbx.partips";
    static final String KEY_FORM_MESSAGE_CHCKBX_COACHES = "form.message.chckbx.coaches";

    private Translator translator;
    private Stack<ContactList> contactLists;
    private String mSubject;
    private String mBody;

    /**
     * @param mSubject
     * @param mBody
     * @param emailListConfig
     * @param grpList
     * @param areaList
     * @param courseOLATResourceable
     * @param cgm
     * @param partipsConfigured
     * @param coachesConfigured
     * @param translator
     */
    public CourseContactMessageUIModel(String mSubject, String mBody, List<String> emailListConfig, List<String> grpList, List<String> areaList,
            OLATResourceable courseOLATResourceable, CourseGroupManager courseGroupManager, Boolean partipsConfigured, Boolean coachesConfigured, Translator translator) {

        this.translator = translator;

        this.mSubject = mSubject;
        this.mBody = mBody;
        this.contactLists = new Stack<ContactList>();

        for (int i = 0; i < grpList.size(); i++) {
            if (coachesConfigured) {
                ContactList cl = retrieveCoachesFromGroup(grpList.get(i), courseGroupManager, courseOLATResourceable);
                contactLists.push(cl);
            }
            if (partipsConfigured) {
                ContactList cl = retrieveParticipantsFromGroup(grpList.get(i), courseGroupManager, courseOLATResourceable);
                contactLists.push(cl);
            }
        }

        for (int i = 0; i < areaList.size(); i++) {
            if (coachesConfigured) {
                ContactList cl = retrieveCoachesFromArea(areaList.get(i), courseGroupManager, courseOLATResourceable);
                contactLists.push(cl);
            }
            if (partipsConfigured) {
                ContactList cl = retrieveParticipantsFromArea(areaList.get(i), courseGroupManager, courseOLATResourceable);
                contactLists.push(cl);
            }
        }

        if (emailListConfig != null && emailListConfig.size() > 0) { /* added size check during writing Unittests */
            ContactList emailList = new ContactList(translate(KEY_RECIPIENTS));
            for (Iterator<String> iter = emailListConfig.iterator(); iter.hasNext();) {
                String email = iter.next();
                emailList.add(email);
            }
            contactLists.push(emailList);
        }

    }

    public Stack<ContactList> getContactLists() {
        return contactLists;
    }

    private String translate(String key) {
        return translator.translate(key);
    }

    private ContactList retrieveCoachesFromGroup(String grpName, CourseGroupManager cgm, OLATResourceable courseResourceable) {
        List<Identity> coaches = cgm.getCoachesFromLearningGroup(grpName, courseResourceable);
        Set<Identity> coachesWithoutDuplicates = new HashSet<Identity>(coaches);
        coaches = new ArrayList<Identity>(coachesWithoutDuplicates);
        ContactList cl = new ContactList(translate(KEY_FORM_MESSAGE_CHCKBX_COACHES));
        cl.addAllIdentites(coaches);
        return cl;
    }

    private ContactList retrieveCoachesFromArea(String areaName, CourseGroupManager cgm, OLATResourceable courseOLATResourceable) {
        List<Identity> coaches = cgm.getCoachesFromArea(areaName, courseOLATResourceable);
        Set<Identity> coachesWithoutDuplicates = new HashSet<Identity>(coaches);
        coaches = new ArrayList<Identity>(coachesWithoutDuplicates);
        ContactList cl = new ContactList(translate(KEY_FORM_MESSAGE_CHCKBX_COACHES));
        cl.addAllIdentites(coaches);
        return cl;
    }

    private ContactList retrieveParticipantsFromGroup(String grpName, CourseGroupManager cgm, OLATResourceable courseOLATResourceable) {
        List<Identity> participiants = cgm.getParticipantsFromLearningGroup(grpName, courseOLATResourceable);
        ContactList cl = new ContactList(translate(KEY_FORM_MESSAGE_CHCKBX_PARTIPS));
        cl.addAllIdentites(participiants);
        return cl;
    }

    private ContactList retrieveParticipantsFromArea(String areaName, CourseGroupManager cgm, OLATResourceable courseOLATResourceable) {
        List<Identity> participiants = cgm.getParticipantsFromArea(areaName, courseOLATResourceable);
        ContactList cl = new ContactList(translate(KEY_FORM_MESSAGE_CHCKBX_PARTIPS));
        cl.addAllIdentites(participiants);
        return cl;
    }

    public String getmSubject() {
        return mSubject;
    }

    public String getmBody() {
        return mBody;
    }

}
