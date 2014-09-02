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

import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.data.portfolio.artefact.FileArtefact;
import org.olat.lms.portfolio.PortfolioAbstractHandler;
import org.olat.lms.portfolio.artefacthandler.EPArtefactHandler;
import org.olat.presentation.filebrowser.commands.CmdAddToEPortfolio;
import org.olat.presentation.filebrowser.commands.FolderCommandHelper;
import org.olat.presentation.filebrowser.commands.FolderCommandStatus;
import org.olat.presentation.filebrowser.components.FolderComponent;
import org.olat.presentation.filebrowser.components.ListRenderer;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * wrapper for the old folder-architecture to handle clicks on ePortfolio-add in folder
 * <P>
 * Initial Date: 03.09.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class CmdAddToEPortfolioImpl extends BasicController implements CmdAddToEPortfolio {

    private int status;
    private VFSItem currentItem;
    private final PortfolioAbstractHandler portfolioModule;
    private Controller collectStepsCtrl;

    public CmdAddToEPortfolioImpl(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        portfolioModule = (PortfolioAbstractHandler) CoreSpringFactory.getBean(PortfolioAbstractHandler.class);
    }

    /**
     * might return NULL!, if item clicked was removed meanwhile or if portfolio is disabled or if only the folder-artefact-handler is disabled.
     * 
     * org.olat.presentation.framework.control.WindowControl, org.olat.presentation.framework.translator.Translator)
     */
    @Override
    public Controller execute(final FolderComponent folderComponent, final UserRequest ureq, final WindowControl wControl, final Translator translator) {
        final String pos = ureq.getParameter(ListRenderer.PARAM_EPORT);
        if (!StringHelper.containsNonWhitespace(pos)) {
            // somehow parameter did not make it to us
            status = FolderCommandStatus.STATUS_FAILED;
            getWindowControl().setError(translator.translate("failed"));
            return null;
        }

        status = FolderCommandHelper.sanityCheck(wControl, folderComponent);
        if (status == FolderCommandStatus.STATUS_SUCCESS) {
            currentItem = folderComponent.getCurrentContainerChildren().get(Integer.parseInt(pos));
            status = FolderCommandHelper.sanityCheck2(wControl, folderComponent, ureq, currentItem);
        }
        if (status == FolderCommandStatus.STATUS_FAILED) {
            return null;
        }

        final EPArtefactHandler<?> artHandler = portfolioModule.getArtefactHandler(FileArtefact.FILE_ARTEFACT_TYPE);
        final AbstractArtefact artefact = artHandler.createArtefact();
        artHandler.prefillArtefactAccordingToSource(artefact, currentItem);
        artefact.setAuthor(getIdentity());

        collectStepsCtrl = new ArtefactWizzardStepsController(ureq, wControl, artefact, currentItem.getParentContainer());

        return collectStepsCtrl;
    }

    /**
	 */
    @Override
    public int getStatus() {
        return status;
    }

    /**
	 */
    @Override
    public boolean runsModal() {
        return true;
    }

    /**
	 */
    @SuppressWarnings("unused")
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // none
    }

    /**
	 */
    @Override
    protected void doDispose() {
        if (collectStepsCtrl != null) {
            collectStepsCtrl.dispose();
            collectStepsCtrl = null;
        }
    }

}
