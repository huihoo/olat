/**

 * 
 * 
 * 
 * 
 * 
 * 
 * 
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
package org.olat.presentation.commons;

import org.apache.log4j.Logger;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResourceManager;
import org.olat.presentation.framework.common.ControllerFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.dtabs.DTab;
import org.olat.presentation.framework.core.control.generic.dtabs.DTabs;
import org.olat.presentation.framework.core.control.generic.messages.MessageController;
import org.olat.presentation.framework.core.control.generic.messages.MessageUIFactory;
import org.olat.presentation.repository.DynamicTabHelper;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * Generic controller for handling disposal of repository entries (modification or deletion).
 * <P>
 * Initial Date: 19.04.2008 <br>
 * Reworked and made generic: oliver.buehler@agility-informatik.ch
 * 
 * @author patrickb
 */
public class DisposedRepoEntryRestartController extends BasicController {

    private static final Logger log = LoggerHelper.getLogger();

    private final RepositoryEntry repositoryEntry;
    private final String deletedTitleTranslation;
    private final String deletedTextTranslation;
    private final VelocityContainer initialContent;
    private final Link restartLink;
    private final Panel panel;

    public DisposedRepoEntryRestartController(final UserRequest ureq, final WindowControl wControl, final RepositoryEntry repositoryEntry,
            final String disposedTitleTranslation, final String disposedTextTranslation, final String disposedRestartTranslation, final String deletedTitleTranslation,
            final String deletedTextTranslation) {
        super(ureq, wControl);
        this.repositoryEntry = repositoryEntry;
        this.deletedTitleTranslation = deletedTitleTranslation;
        this.deletedTextTranslation = deletedTextTranslation;

        initialContent = createVelocityContainer("disposedcourserestart");
        initialContent.contextPut("disposedTitle", disposedTitleTranslation);
        initialContent.contextPut("disposedMessage", disposedTextTranslation);
        restartLink = LinkFactory.createCustomLink("disposed.command.restart", "disposed.command.restart", disposedRestartTranslation, Link.BUTTON + Link.NONTRANSLATED,
                initialContent, this);
        panel = putInitialPanel(initialContent);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // no action required
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == restartLink) {
            final DTabs dts = getWindowControl().getWindowBackOffice().getWindow().getDynamicTabs();
            final OLATResourceable ores = OLATResourceManager.getInstance().findResourceable(repositoryEntry.getOlatResource().getResourceableId(),
                    repositoryEntry.getOlatResource().getResourceableTypeName());
            if (ores == null) {
                // repository entry was deleted!
                final MessageController msgController = MessageUIFactory
                        .createInfoMessage(ureq, this.getWindowControl(), deletedTitleTranslation, deletedTextTranslation);
                panel.setContent(msgController.getInitialComponent());
                return;
            }

            // remove and dispose "old course run"
            DTab dt = dts.getDTab(ores);
            dts.removeDTab(dt);// disposes also dt and controllers

            if (dt == null) {
                log.warn("I-130715-0001: No open tab found for OLAT resource '" + ores + "' despite restart link has been clicked.");
            }

            final Controller launchController = ControllerFactory.createLaunchController(ores, null, ureq, dt.getWindowControl(), true);
            DynamicTabHelper.openRepoEntryTab(repositoryEntry, ureq, launchController, repositoryEntry.getDisplayname(), null);

            // last but not least dispose myself - to clean up.
            dispose();
        }
    }
}
