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

import java.io.ByteArrayInputStream;

import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.VFSManager;
import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.data.portfolio.artefact.ArtefactDaoImpl;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.RichTextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.wizard.StepFormBasicController;
import org.olat.presentation.framework.core.control.generic.wizard.StepsEvent;
import org.olat.presentation.framework.core.control.generic.wizard.StepsRunContext;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.portfolio.artefacts.run.EPArtefactViewController;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * first step for wizzard, when creating a text-artefact can also be used as a separate form to edit an artefact
 * <P>
 * Initial Date: 01.09.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPCreateTextArtefactStepForm00 extends StepFormBasicController {

    private final AbstractArtefact artefact;
    private RichTextElement content;
    private final VFSContainer vfsTemp;
    private EPFrontendManager ePFMgr;
    private final String artFulltextContent;

    // use this constructor to edit an already existing artefact
    public EPCreateTextArtefactStepForm00(final UserRequest ureq, final WindowControl wControl, final AbstractArtefact artefact) {
        super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
        final PackageTranslator pt = new PackageTranslator(EPArtefactViewController.class.getPackage().getName(), ureq.getLocale(), getTranslator());
        this.flc.setTranslator(pt);
        this.artefact = artefact;
        ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean(EPFrontendManager.class);
        this.artFulltextContent = ePFMgr.getArtefactFullTextContent(artefact);
        this.vfsTemp = ePFMgr.getArtefactContainer(artefact);
        initForm(this.flc, this, ureq);
    }

    public EPCreateTextArtefactStepForm00(final UserRequest ureq, final WindowControl wControl, final Form rootForm, final StepsRunContext runContext, final int layout,
            final String customLayoutPageName, final AbstractArtefact artefact, final VFSContainer vfsTemp) {
        super(ureq, wControl, rootForm, runContext, layout, customLayoutPageName);
        // set fallback translator to re-use given strings
        final PackageTranslator pt = new PackageTranslator(EPArtefactViewController.class.getPackage().getName(), ureq.getLocale(), getTranslator());
        this.flc.setTranslator(pt);
        this.artefact = artefact;
        this.artFulltextContent = artefact.getFulltextContent(); // during collection the fulltextcontent is not persisted and therefore might be longer than db-length
                                                                 // restriction
        this.vfsTemp = vfsTemp;
        initForm(this.flc, this, ureq);
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, @SuppressWarnings("unused") final Controller listener, final UserRequest ureq) {

        final VFSItem contFile = vfsTemp.resolve(ArtefactDaoImpl.ARTEFACT_CONTENT_FILENAME);
        if (contFile == null) {
            vfsTemp.createChildLeaf(ArtefactDaoImpl.ARTEFACT_CONTENT_FILENAME);
        }
        content = uifactory.addRichTextElementForFileData("content", "artefact.content", artFulltextContent, 15, -1, false, vfsTemp,
                ArtefactDaoImpl.ARTEFACT_CONTENT_FILENAME, null, formLayout, ureq.getUserSession(), getWindowControl());
        content.getEditorConfiguration().setFileBrowserUploadRelPath("media");
        content.setMandatory(true);
        content.setNotEmptyCheck("artefact.content.not.empty");
        content.setExtDelay(true);

        if (!isUsedInStepWizzard()) {
            // add form buttons
            uifactory.addFormSubmitButton("stepform.submit", formLayout);
        }

    }

    @Override
    protected void formOK(final UserRequest ureq) {

        // either save values to runContext or do persist them
        // directly, if form is used outside step-context
        if (isUsedInStepWizzard()) {
            // save fulltext to temp-file
            final String fulltext = content.getValue();
            final VFSLeaf contFile = (VFSLeaf) vfsTemp.resolve(ArtefactDaoImpl.ARTEFACT_CONTENT_FILENAME);
            VFSManager.copyContent(new ByteArrayInputStream(fulltext.getBytes()), contFile, true);

            addToRunContext("artefact", artefact);
            addToRunContext("tempArtFolder", vfsTemp);
            fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
        } else {
            final String fulltext = content.getValue();
            artefact.setFulltextContent(fulltext);
            ePFMgr.updateArtefact(artefact);

            // the content-file is not needed in this case!! remove it.
            final VFSLeaf contFile = (VFSLeaf) vfsTemp.resolve(ArtefactDaoImpl.ARTEFACT_CONTENT_FILENAME);
            if (contFile != null) {
                contFile.delete();
            }
            fireEvent(ureq, Event.DONE_EVENT);
        }
    }

    @Override
    protected void doDispose() {
        // nothing, temp-file is cleaned within calling controller!
    }

}
