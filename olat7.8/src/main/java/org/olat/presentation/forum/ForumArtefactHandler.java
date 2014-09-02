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

import java.util.List;

import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.forum.Message;
import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.data.portfolio.artefact.ForumArtefact;
import org.olat.lms.forum.ForumService;
import org.olat.lms.portfolio.artefacthandler.EPAbstractHandler;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.commons.resource.OLATResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * The ArtefactHandler for Forums
 * <P>
 * Initial Date: 11.06.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class ForumArtefactHandler extends EPAbstractHandler<ForumArtefact> {

    protected ForumArtefactHandler() {
    }

    @Autowired
    ForumService forumService;

    /**
	 */
    @Override
    public void prefillArtefactAccordingToSource(final AbstractArtefact artefact, final Object source) {
        super.prefillArtefactAccordingToSource(artefact, source);
        if (source instanceof OLATResourceable) {
            final OLATResourceable ores = (OLATResourceable) source;
            final Message fm = forumService.loadMessage(ores.getResourceableId());
            final String thread = fm.getThreadtop() != null ? fm.getThreadtop().getTitle() + " - " : "";
            artefact.setTitle(thread + fm.getTitle());

            final VFSContainer msgContainer = forumService.getMessageContainer(fm.getForum().getKey(), fm.getKey());
            if (msgContainer != null) {
                final List<VFSItem> foAttach = msgContainer.getItems();
                if (foAttach.size() != 0) {
                    artefact.setFileSourceContainer(msgContainer);
                }
            }

            artefact.setSignature(70);
            artefact.setFulltextContent(fm.getBody());
        }
    }

    @Override
    public ForumArtefact createArtefact() {
        final ForumArtefact artefact = new ForumArtefact();
        return artefact;
    }

    @Override
    public String getType() {
        return ForumArtefact.FORUM_ARTEFACT_TYPE;
    }

    @Override
    public Controller createDetailsController(final UserRequest ureq, final WindowControl wControl, final AbstractArtefact artefact, final boolean readOnlyMode) {
        return new ForumArtefactDetailsController(ureq, wControl, artefact);
    }

    @Override
    public String getIcon(final AbstractArtefact artefact) {
        return "o_fo_icon";
    }
}
