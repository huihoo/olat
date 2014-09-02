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

package org.olat.presentation.framework.core.media.fileresource;

import org.olat.lms.commons.fileresource.SurveyFileResource;
import org.olat.lms.commons.fileresource.TestFileResource;
import org.olat.lms.framework.core.media.fileresource.FileDetailsFormEBL;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: Apr 19, 2004
 * 
 * @author Mike Stock
 */
public class FileDetailsForm extends FormBasicController {

    private final OLATResourceable res;
    private final FileDetailsFormEBL fileDetailsFormEBL;

    /**
     * Create details form with values from resourceable res.
     * 
     * @param name
     * @param locale
     * @param res
     */
    public FileDetailsForm(final UserRequest ureq, final WindowControl wControl, final OLATResourceable res) {
        super(ureq, wControl);
        this.res = res;
        fileDetailsFormEBL = CoreSpringFactory.getBean(FileDetailsFormEBL.class, new Object[] { res });
        initForm(ureq);

    }

    @Override
    protected void formOK(final UserRequest ureq) {
        //
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        uifactory.addStaticTextElement("size", "fr.size", fileDetailsFormEBL.getFileSizeLabel(), formLayout);
        uifactory.addStaticTextElement("last", "fr.last", fileDetailsFormEBL.getFileLastModifiedLable(getLocale()), formLayout);

        final String resType = res.getResourceableTypeName();
        if (isTestOrSurveyFileResource(resType)) {

            fileDetailsFormEBL.buildDocument();

            if (fileDetailsFormEBL.isNotNullDocument()) {

                extractTextsFromDocument(formLayout);
            }
        }

        flc.setEnabled(false);
    }

    private void extractTextsFromDocument(final FormItemContainer formLayout) {
        uifactory.addStaticTextElement("title", "qti.title", fileDetailsFormEBL.extractTitleFromDocument(), formLayout);

        uifactory.addStaticTextElement("obj", "qti.objectives", fileDetailsFormEBL.extractObjectivesFromDocument(), formLayout);

        uifactory.addStaticTextElement("qti.questions", "qti.questions", fileDetailsFormEBL.extractNumberOfQuestionsFromDocument(), formLayout);

        uifactory.addStaticTextElement("qti.timelimit", "qti.timelimit", fileDetailsFormEBL.extractTimeLimitFromDocument(), formLayout);
    }

    private boolean isTestOrSurveyFileResource(final String resType) {
        return resType.equals(TestFileResource.TYPE_NAME) || resType.equals(SurveyFileResource.TYPE_NAME);
    }

    @Override
    protected void doDispose() {
        //
    }

}
