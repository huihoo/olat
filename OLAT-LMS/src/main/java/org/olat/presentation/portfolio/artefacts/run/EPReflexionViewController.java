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
package org.olat.presentation.portfolio.artefacts.run;

import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.data.portfolio.structure.PortfolioStructure;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.RichTextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.commons.StringHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * a view only controller for reflexion on artefact and on link
 * <P>
 * Initial Date: 8 nov. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EPReflexionViewController extends FormBasicController {

    private RichTextElement artRef;
    private String artefactReflexion = "";
    private final EPFrontendManager ePFMgr;
    private String linkReflexion = "";
    private final boolean canSeeArtefactReflexion;
    private RichTextElement linkRef;

    public EPReflexionViewController(final UserRequest ureq, final WindowControl wControl, final AbstractArtefact artefact, final PortfolioStructure structure) {
        super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
        ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean(EPFrontendManager.class);
        this.linkReflexion = ePFMgr.getReflexionForArtefactToStructureLink(artefact, structure);
        this.canSeeArtefactReflexion = artefact.getAuthor().getKey().equals(ureq.getIdentity().getKey());
        if (canSeeArtefactReflexion) {
            this.artefactReflexion = artefact.getReflexion();
        }

        initForm(ureq);
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    @SuppressWarnings("unused")
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        setFormDescription("artefact.reflexion.view.descr");

        if (canSeeArtefactReflexion) {
            artRef = uifactory.addRichTextElementForStringDataMinimalistic("reflexion.artefact", "reflexion.artefact", artefactReflexion, 12, -1, false, formLayout,
                    ureq.getUserSession(), getWindowControl());
            artRef.setExtDelay(true);
            artRef.setEnabled(false);
        }

        if (StringHelper.containsNonWhitespace(linkReflexion)) {
            linkRef = uifactory.addRichTextElementForStringDataMinimalistic("reflexion.link", "reflexion.link", linkReflexion, 12, -1, false, formLayout,
                    ureq.getUserSession(), getWindowControl());
            linkRef.setExtDelay(true);
            linkRef.setEnabled(false);
        }

    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing
    }

    /**
	 */
    @Override
    @SuppressWarnings("unused")
    protected void formOK(final UserRequest ureq) {
        // do nothing
    }
}
