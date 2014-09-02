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
 * Copyright (c) 2008 frentix GmbH, Switzerland<br>
 * <p>
 */
package org.olat.presentation.admin.campus;

/* TODO: ORID-1007 'File' */
import org.apache.log4j.Logger;
import org.olat.lms.core.course.campus.CampusConfiguration;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.FormLink;
import org.olat.presentation.framework.core.components.form.flexible.elements.SingleSelection;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * 
 * 
 * @author Christian Guretzki
 */
public class CampusAdminController extends FormBasicController {

    private static final Logger log = LoggerHelper.getLogger();
    private TextElement resourceableIdTextElement;
    private SingleSelection languagesSelection;
    private static String[] languages = { "DE", "EN", "FR", "IT" };
    private static String DEFAULT_LANGUAGE = "DE";
    private FormLink saveButton;
    private CampusConfiguration campusConfiguration;
    private Long templateCourseResourcableId;

    public CampusAdminController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        campusConfiguration = CoreSpringFactory.getBean(CampusConfiguration.class);
        templateCourseResourcableId = campusConfiguration.getTemplateCourseResourcableId(DEFAULT_LANGUAGE);
        initForm(this.flc, this, ureq);
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        setFormTitle("header.campus.admin");

        languagesSelection = uifactory.addDropdownSingleselect("languagesSelection", "campus.admin.form.template.language", formLayout, languages, languages, null);
        languagesSelection.select(DEFAULT_LANGUAGE, true);
        languagesSelection.addActionListener(listener, FormEvent.ONCHANGE);

        resourceableIdTextElement = uifactory.addTextElement("resourceableId", "campus.admin.form.resourceableid", 60, "", formLayout);
        resourceableIdTextElement.setValue(templateCourseResourcableId.toString());

        saveButton = uifactory.addFormLink("save", formLayout, Link.BUTTON);
        saveButton.addActionListener(this, FormEvent.ONCLICK);
    }

    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        if (source == languagesSelection) {
            Long templateCourseResourcableId = campusConfiguration.getTemplateCourseResourcableId(languagesSelection.getSelectedKey());
            if (templateCourseResourcableId != null) {
                resourceableIdTextElement.setValue(templateCourseResourcableId.toString());
            }
        }
        if (source == saveButton) {
            source.getRootForm().submit(ureq);
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to clean up
    }

    @Override
    protected boolean validateFormLogic(final UserRequest ureq) {
        if (!isNumber(resourceableIdTextElement.getValue())) {
            resourceableIdTextElement.setErrorKey("error.campus.admin.form.resourceableid", null);
            return false;
        }
        return true;
    }

    private boolean isNumber(String stringValue) {
        try {
            Long.parseLong(stringValue);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        log.info("formOk: value=" + resourceableIdTextElement.getValue());
        try {
            Long templateCourseResourcableIdAsLong = Long.parseLong(resourceableIdTextElement.getValue());
            campusConfiguration.saveTemplateCourseResourcableId(templateCourseResourcableIdAsLong, languagesSelection.getSelectedKey());
        } catch (NumberFormatException ex) {
            this.showWarning("campus.admin.form.could.not.save");
        }

    }
}
