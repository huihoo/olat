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

package org.olat.presentation.repository;

import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.course.CourseModule;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.config.CourseConfig;
import org.olat.lms.instantmessaging.InstantMessagingModule;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.SelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.SingleSelection;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;

/**
 *
 */
public class DisplayCourseInfoForm extends FormBasicController {

    private SingleSelection layout;
    private SingleSelection sfolder;
    private SelectionElement chatIsOn;
    private SelectionElement efficencyStatement;
    private SelectionElement calendar;
    private SingleSelection glossary;

    private static final String KEY_NO = "0";
    private static final String KEY_YES = "1";

    private final CourseConfig cc;

    public DisplayCourseInfoForm(final UserRequest ureq, final WindowControl wControl, final ICourse course) {
        super(ureq, wControl);
        cc = course.getCourseEnvironment().getCourseConfig();
        initForm(ureq);
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        //
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        setFormTitle("details.summaryprop");
        setFormContextHelp("org.olat.presentation.repository", "rep-meta-infoCourse.html", "help.hover.rep.detail");

        chatIsOn = uifactory.addCheckboxesVertical("chatIsOn", "chkbx.chat.onoff", formLayout, new String[] { "xx" }, new String[] { null }, null, 1);
        chatIsOn.select("xx", cc.isChatEnabled());
        chatIsOn.setVisible(InstantMessagingModule.isEnabled() && CourseModule.isCourseChatEnabled());

        uifactory.addStaticTextElement("layout", "form.layout.cssfile", cc.hasCustomCourseCSS() ? cc.getCssLayoutRef() : translate("form.layout.setsystemcss"),
                formLayout);

        String name;
        final String softkey = cc.getSharedFolderSoftkey();
        RepositoryEntry re = RepositoryServiceImpl.getInstance().lookupRepositoryEntryBySoftkey(softkey, false);
        if (re == null) {
            cc.setSharedFolderSoftkey(CourseConfig.VALUE_EMPTY_SHAREDFOLDER_SOFTKEY);
            name = translate("sf.notconfigured");
        } else {
            name = re.getDisplayname();
        }

        uifactory.addStaticTextElement("sfolder", "sf.resourcetitle", cc.hasCustomSharedFolder() ? name : translate("sf.notconfigured"), formLayout);

        efficencyStatement = uifactory.addCheckboxesVertical("efficencyStatement", "chkbx.efficency.onoff", formLayout, new String[] { "xx" }, new String[] { null },
                null, 1);
        efficencyStatement.select("xx", cc.isEfficencyStatementEnabled());

        calendar = uifactory.addCheckboxesVertical("calendar", "chkbx.calendar.onoff", formLayout, new String[] { "xx" }, new String[] { null }, null, 1);
        calendar.select("xx", cc.isCalendarEnabled());

        String glossName;
        final String glossSoftKey = cc.getGlossarySoftKey();
        re = RepositoryServiceImpl.getInstance().lookupRepositoryEntryBySoftkey(glossSoftKey, false);
        if (re == null) {
            glossName = translate("glossary.no.glossary");
        } else {
            glossName = re.getDisplayname();
        }

        uifactory.addStaticTextElement("glossary", "glossary.isconfigured", cc.hasGlossary() ? glossName : translate("glossary.no.glossary"), formLayout);

        flc.setEnabled(false);
    }

    @Override
    protected void doDispose() {
        //
    }
}
