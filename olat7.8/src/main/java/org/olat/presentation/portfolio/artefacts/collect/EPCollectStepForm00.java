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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.presentation.portfolio.artefacts.collect;

import java.util.Date;
import java.util.List;

import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.lms.commons.context.BusinessControlFactory;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.RichTextElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.StaticTextElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.wizard.StepFormBasicController;
import org.olat.presentation.framework.core.control.generic.wizard.StepsEvent;
import org.olat.presentation.framework.core.control.generic.wizard.StepsRunContext;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.portfolio.artefacts.run.EPArtefactViewController;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * first collection step, collecting title and description of an artefact
 * <P>
 * Initial Date: 01.11.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPCollectStepForm00 extends StepFormBasicController {

    private TextElement title;
    private RichTextElement descript;
    private final AbstractArtefact artefact;
    private boolean simpleMode = false;

    public EPCollectStepForm00(final UserRequest ureq, final WindowControl wControl, final Form rootForm, final StepsRunContext runContext, final int layout,
            final String customLayoutPageName, final AbstractArtefact artefact) {
        super(ureq, wControl, rootForm, runContext, layout, customLayoutPageName);
        // set fallback translator to re-use given strings
        final PackageTranslator pt = new PackageTranslator(EPArtefactViewController.class.getPackage().getName(), ureq.getLocale(), getTranslator());
        this.flc.setTranslator(pt);
        this.artefact = artefact;
        initForm(this.flc, this, ureq);
    }

    // this constructor is used when editing an artefact, therefore the form doesn't show all fields!
    public EPCollectStepForm00(final UserRequest ureq, final WindowControl wControl, final AbstractArtefact artefact) {
        super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
        // set fallback translator to re-use given strings
        final PackageTranslator pt = new PackageTranslator(EPArtefactViewController.class.getPackage().getName(), ureq.getLocale(), getTranslator());
        this.flc.setTranslator(pt);
        this.artefact = artefact;
        this.simpleMode = true;
        initForm(ureq);
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, @SuppressWarnings("unused") final Controller listener, final UserRequest ureq) {
        title = uifactory.addTextElement("title", "artefact.title", 500, artefact.getTitle(), formLayout);
        title.setMandatory(true);
        title.setNotEmptyCheck("artefact.title.not.empty");
        title.setNotLongerThanCheck(512, "artefact.title.too.long");
        title.setVisible(!simpleMode);
        descript = uifactory.addRichTextElementForStringDataMinimalistic("description", "artefact.description", artefact.getDescription(), 7, -1, false, formLayout,
                ureq.getUserSession(), getWindowControl());
        descript.setExtDelay(true);
        descript.setMaxLength(4000);
        descript.setNotLongerThanCheck(4000, "artefact.description.too.long");

        final String artSource = artefact.getSource();
        if (StringHelper.containsNonWhitespace(artSource) && !simpleMode) {
            uifactory.addStaticTextElement("artefact.source", artSource, formLayout);
        }
        Date artDate = artefact.getCreationDate();
        if (artDate == null) {
            artDate = new Date();
        }
        final StaticTextElement date = uifactory.addStaticTextElement("artefact.date", Formatter.getInstance(getLocale()).formatDateAndTime(artDate), formLayout);
        date.setVisible(!simpleMode);

        final String busPath = artefact.getBusinessPath();
        if (StringHelper.containsNonWhitespace(busPath) && !simpleMode) {
            final BusinessControlFactory bCF = BusinessControlFactory.getInstance();
            final List<ContextEntry> ceList = bCF.createCEListFromString(busPath);
            final String busLink = bCF.getAsURIString(ceList, true);
            if (StringHelper.containsNonWhitespace(busLink)) {
                final String finalPath = "<a href=\"" + busLink + "\">" + busLink + "</a>";
                uifactory.addStaticTextElementWithTrustedText("artefact.link", finalPath, formLayout);
            }
        }

        if (!isUsedInStepWizzard()) {
            // add form buttons
            uifactory.addFormSubmitButton("stepform.submit", formLayout);
        }
    }

    @Override
    protected void doDispose() {
        // nothing
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        artefact.setTitle(title.getValue());
        artefact.setDescription(descript.getValue());

        // either save values to runContext or do persist them
        // directly, if form is used outside step-context
        if (isUsedInStepWizzard()) {
            addToRunContext("artefact", artefact);
            if (artefact.getFileSourceContainer() != null) {
                addToRunContext("tempArtFolder", artefact.getFileSourceContainer());
            }

            fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
        } else {
            fireEvent(ureq, Event.DONE_EVENT);
        }
    }

}
