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

package org.olat.presentation.sharedfolder;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.activitylogging.LearningResourceLoggingAction;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.activitylogging.OlatResourceableType;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.commons.vfs.securitycallbacks.ReadOnlyCallback;
import org.olat.lms.repository.RepositoryService;
import org.olat.presentation.commons.DisposedRepoEntryRestartController;
import org.olat.presentation.filebrowser.FolderRunController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.translator.I18nPackage;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.presentation.repository.EntryChangedEvent;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.event.Event;
import org.olat.system.event.GenericEventListener;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Displays the SharedFolder likewise a webserver. If it exists a file like index.htm(l) or default.htm(l), this will be displayed, otherwise the directory listing.
 * <P>
 * Initial Date: Sept 2, 2005 <br>
 * 
 * @author Alexander Schneider
 */
public class SharedFolderDisplayController extends BasicController implements GenericEventListener {

    /**
     * Name of a file (1. priority) that prevents directory listing in the sharedfolder, if it exists
     */
    private static final String INDEXDOTHTML = "index.html";

    /**
     * Name of a file (2. priority) that prevents directory listing in the sharedfolder, if it exists
     */
    private static final String INDEXDOTHTM = "index.htm";

    /**
     * Name of a file (3. priority) that prevents directory listing in the sharedfolder, if it exists
     */
    private static final String DEFAULTDOTHTML = "default.html";

    /**
     * Name of a file (4. priority) that prevents directory listing in the sharedfolder, if it exists
     */
    private static final String DEFAULTDOTHTM = "default.htm";

    private final OLATResourceable resourceable;
    private final RepositoryEntry sharedFolderRepoEntry;

    private Controller controller;

    /**
     * @param res
     * @param ureq
     * @param wControl
     * @param previewBackground
     */
    public SharedFolderDisplayController(final UserRequest ureq, final WindowControl wControl, final VFSContainer sharedFolder, final OLATResourceable ores,
            final boolean previewBackground) {
        super(ureq, wControl);
        resourceable = ores;
        sharedFolderRepoEntry = CoreSpringFactory.getBean(RepositoryService.class).lookupRepositoryEntry(ores, true);
        final VelocityContainer vcDisplay = createVelocityContainer("display");

        addLoggingResourceable(LoggingResourceable.wrap(ores, OlatResourceableType.genRepoEntry));
        ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_OPEN, getClass());

        VFSItem item = null;
        item = sharedFolder.resolve(INDEXDOTHTML);
        if (item == null) {
            item = sharedFolder.resolve(INDEXDOTHTM);
        }
        if (item == null) {
            item = sharedFolder.resolve(DEFAULTDOTHTML);
        }
        if (item == null) {
            item = sharedFolder.resolve(DEFAULTDOTHTM);
        }

        if (item == null) {
            sharedFolder.setLocalSecurityCallback(new ReadOnlyCallback());
            controller = new FolderRunController(sharedFolder, true, true, ureq, getWindowControl());
            controller.addControllerListener(this);
        } else {
            controller = new WebsiteDisplayController(ureq, getWindowControl(), sharedFolder, item.getName());
        }
        vcDisplay.put("displayer", controller.getInitialComponent());

        // Add html header with css definitions to this velocity container
        if (previewBackground) {
            final JSAndCSSComponent jsAndCss = new JSAndCSSComponent("previewcss", this.getClass(), null, "olat-preview.css", true);
            vcDisplay.put("previewcss", jsAndCss);
        }

        putInitialPanel(vcDisplay);

        // disposed message controller
        final Panel empty = new Panel("empty");// empty panel set as "menu" and "tool"
        final Translator trans = PackageUtil.createPackageTranslator(I18nPackage.SHAREDFOLDER_, ureq.getLocale());
        final Controller disposedRestartController = new DisposedRepoEntryRestartController(ureq, wControl, sharedFolderRepoEntry, trans.translate("disposed.title"),
                trans.translate("disposed.message"), trans.translate("disposed.command.restart"), trans.translate("deleted.title"), trans.translate("deleted.text"));
        final Controller layoutController = new LayoutMain3ColsController(ureq, wControl, empty, empty, disposedRestartController.getInitialComponent(),
                "disposed shared folder" + sharedFolderRepoEntry.getResourceableId());
        setDisposedMsgController(layoutController);

        // add as listener to glossary so we are being notified about events:
        // - deletion (OLATResourceableJustBeforeDeletedEvent)
        // - modification (EntryChangedEvent)
        final Identity identity = ureq.getIdentity();
        CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, identity, ores);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // nothing to do
    }

    /**
	 */
    @Override
    protected void doDispose() {
        ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_CLOSE, getClass());
        if (controller != null) {
            controller.dispose();
            controller = null;
        }
    }

    @Override
    public void event(Event event) {
        if (event instanceof OLATResourceableJustBeforeDeletedEvent) {
            final OLATResourceableJustBeforeDeletedEvent ojde = (OLATResourceableJustBeforeDeletedEvent) event;
            if (ojde.targetEquals(resourceable, true)) {
                dispose();
            }
        } else if (event instanceof EntryChangedEvent) {
            final EntryChangedEvent repoEvent = (EntryChangedEvent) event;
            if (sharedFolderRepoEntry.getKey().equals(repoEvent.getChangedEntryKey()) && repoEvent.getChange() == EntryChangedEvent.MODIFIED) {
                dispose();
            }
        }
    }

    @Override
    public boolean isControllerAndNotDisposed() {
        return !isDisposed();
    }
}
