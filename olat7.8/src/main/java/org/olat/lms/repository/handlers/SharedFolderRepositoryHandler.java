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

package org.olat.lms.repository.handlers;

import java.util.ArrayList;
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.fileresource.FileResourceManager;
import org.olat.lms.commons.fileresource.SharedFolderFileResource;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.reference.ReferenceService;
import org.olat.lms.sharedfolder.SharedFolderManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.layout.MainLayoutController;
import org.olat.presentation.framework.core.translator.I18nPackage;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.presentation.repository.IAddController;
import org.olat.presentation.repository.RepositoryAddCallback;
import org.olat.presentation.repository.WizardCloseResourceController;
import org.olat.presentation.sharedfolder.CreateNewSharedFolderController;
import org.olat.presentation.sharedfolder.SharedFolderDisplayController;
import org.olat.presentation.sharedfolder.SharedFolderEditorController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.LockResult;
import org.olat.system.exception.AssertException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Description:<br>
 * TODO: as Class Description for SharedFolderHandler
 * <P>
 * Initial Date: Apr 6 <br>
 * 
 * @author gnaegi
 */
@Component
public class SharedFolderRepositoryHandler implements RepositoryHandler {

    private static final boolean LAUNCHEABLE = true;
    private static final boolean DOWNLOADEABLE = false;
    private static final boolean EDITABLE = true;
    private static final boolean WIZARD_SUPPORT = false;
    private List<String> supportedTypes;
    @Autowired
    private ReferenceService referenceService;

    /**
     * Comment for <code>PROCESS_CREATENEW</code>
     */
    public static final String PROCESS_CREATENEW = "cn";

    /**
     * Default constructor.
     */
    protected SharedFolderRepositoryHandler() {
        supportedTypes = new ArrayList<String>(1);
        supportedTypes.add(SharedFolderFileResource.TYPE_NAME);
    }

    /**
	 */
    @Override
    public List<String> getSupportedTypes() {
        return supportedTypes;
    }

    /**
	 */
    @Override
    public boolean supportsLaunch(final RepositoryEntry repoEntry) {
        return LAUNCHEABLE;
    }

    /**
	 */
    @Override
    public boolean supportsDownload(final RepositoryEntry repoEntry) {
        return DOWNLOADEABLE;
    }

    /**
	 */
    @Override
    public boolean supportsEdit(final RepositoryEntry repoEntry) {
        return EDITABLE;
    }

    /**
	 */
    @Override
    public boolean supportsWizard(final RepositoryEntry repoEntry) {
        return WIZARD_SUPPORT;
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public Controller createWizardController(final OLATResourceable res, final UserRequest ureq, final WindowControl wControl) {
        throw new AssertException("Trying to get wizard where no creation wizard is provided for this type.");
    }

    /**
     * @param res
     * @param initialViewIdentifier
     * @param ureq
     * @param wControl
     * @return Controller
     */
    @Override
    public MainLayoutController createLaunchController(final OLATResourceable res, final String initialViewIdentifier, final UserRequest ureq,
            final WindowControl wControl) {
        final VFSContainer sfContainer = SharedFolderManager.getInstance().getSharedFolder(res);
        final SharedFolderDisplayController sfdCtr = new SharedFolderDisplayController(ureq, wControl, sfContainer, res, false);
        // use on column layout
        final LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, wControl, null, null, sfdCtr.getInitialComponent(), null);
        layoutCtr.addDisposableChildController(sfdCtr); // dispose content on layout dispose
        return layoutCtr;
    }

    /**
	 */
    @Override
    public MediaResource getAsMediaResource(final OLATResourceable res) {
        return SharedFolderManager.getInstance().getAsMediaResource(res);
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public Controller createEditorController(final OLATResourceable res, final UserRequest ureq, final WindowControl wControl) {
        final Controller sharedFolderCtr = new SharedFolderEditorController(res, ureq, wControl);
        // use on column layout
        final LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, wControl, null, null, sharedFolderCtr.getInitialComponent(), null);
        layoutCtr.addDisposableChildController(sharedFolderCtr); // dispose content on layout dispose
        return layoutCtr;
    }

    /**
     * org.olat.presentation.framework.UserRequest, org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public IAddController createAddController(final RepositoryAddCallback callback, final Object userObject, final UserRequest ureq, final WindowControl wControl) {
        return new CreateNewSharedFolderController(callback, ureq, wControl);
    }

    @Override
    public Controller createDetailsForm(final UserRequest ureq, final WindowControl wControl, final OLATResourceable res) {
        return null;
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public boolean cleanupOnDelete(final OLATResourceable res) {
        // do not need to notify all current users of this resource, since the only
        // way to access this resource
        // FIXME:fj:c to be perfect, still need to notify
        // repositorydetailscontroller and searchresultcontroller....
        CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new OLATResourceableJustBeforeDeletedEvent(res), res);
        SharedFolderManager.getInstance().deleteSharedFolder(res);
        return true;
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public boolean readyToDelete(final OLATResourceable res, final UserRequest ureq, final WindowControl wControl) {
        final String referencesSummary = referenceService.getReferencesToSummary(res, ureq.getLocale());
        if (referencesSummary != null) {
            final Translator translator = PackageUtil.createPackageTranslator(I18nPackage.REPOSITORY_, ureq.getLocale());
            wControl.setError(translator.translate("details.delete.error.references", new String[] { referencesSummary }));
            return false;
        }
        return true;
    }

    /**
	 */
    @Override
    public OLATResourceable createCopy(final OLATResourceable res, final Identity identity) {
        return FileResourceManager.getInstance().createCopy(res);
    }

    @Override
    public String archive(final Identity archiveOnBehalfOf, final String archivFilePath, final RepositoryEntry repoEntry) {
        return SharedFolderManager.getInstance().archive(archivFilePath, repoEntry);
    }

    /**
	 */
    @Override
    public LockResult acquireLock(final OLATResourceable ores, final Identity identity) {
        // nothing to do
        return null;
    }

    /**
	 */
    @Override
    public void releaseLock(final LockResult lockResult) {
        // nothing to do since nothing locked
    }

    /**
	 */
    @Override
    public boolean isLocked(final OLATResourceable ores) {
        return false;
    }

    @Override
    public WizardCloseResourceController createCloseResourceController(final UserRequest ureq, final WindowControl wControl, final RepositoryEntry repositoryEntry) {
        throw new AssertException("not implemented");
    }
}
