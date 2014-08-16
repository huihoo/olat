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
package org.olat.presentation.forum;

import java.util.ArrayList;
import java.util.List;

import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.filters.VFSItemExcludePrefixFilter;
import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.data.portfolio.artefact.ForumArtefact;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.download.DownloadComponent;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.util.CSSHelper;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Show the specific part of the ForumArtefact
 * <P>
 * Initial Date: 11 oct. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ForumArtefactDetailsController extends BasicController {

    private final VelocityContainer vC;
    protected static final String[] ATTACHMENT_EXCLUDE_PREFIXES = new String[] { ".nfs", ".CVS", ".DS_Store" }; // see: MessageEditController.ATTACHMENT_EXCLUDE_PREFIXES

    ForumArtefactDetailsController(final UserRequest ureq, final WindowControl wControl, final AbstractArtefact artefact) {
        super(ureq, wControl);
        final ForumArtefact fArtefact = (ForumArtefact) artefact;
        vC = createVelocityContainer("messageDetails");
        final EPFrontendManager ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean(EPFrontendManager.class);
        vC.contextPut("text", ePFMgr.getArtefactFullTextContent(fArtefact));
        final VFSContainer artContainer = ePFMgr.getArtefactContainer(artefact);
        if (artContainer != null && artContainer.getItems().size() != 0) {
            final List<VFSItem> attachments = new ArrayList<VFSItem>(artContainer.getItems(new VFSItemExcludePrefixFilter(ATTACHMENT_EXCLUDE_PREFIXES)));
            int i = 1; // vc-shift!
            for (final VFSItem vfsItem : attachments) {
                final VFSLeaf file = (VFSLeaf) vfsItem;
                // DownloadComponent downlC = new DownloadComponent("download"+i, file);
                final DownloadComponent downlC = new DownloadComponent("download" + i, file, file.getName() + " (" + String.valueOf(file.getSize() / 1024) + " KB)",
                        null, CSSHelper.createFiletypeIconCssClassFor(file.getName()));
                vC.put("download" + i, downlC);
                i++;
            }
            vC.contextPut("attachments", attachments);
            vC.contextPut("hasAttachments", true);
        }

        putInitialPanel(vC);
    }

    @Override
    @SuppressWarnings("unused")
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        //
    }

    @Override
    protected void doDispose() {
        //
    }
}
