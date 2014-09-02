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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.LocalFolderImpl;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.commons.fileresource.FileResourceManager;
import org.olat.lms.commons.fileresource.ImsCPFileResource;
import org.olat.lms.ims.cp.CPCore;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.layout.MainLayoutController;
import org.olat.presentation.ims.cp.CPEditMainController;
import org.olat.presentation.ims.cp.CPUIFactory;
import org.olat.presentation.ims.cp.CreateNewCPController;
import org.olat.presentation.repository.IAddController;
import org.olat.presentation.repository.RepositoryAddCallback;
import org.olat.presentation.repository.WizardCloseResourceController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.coordinate.LockResult;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.stereotype.Component;

/**
 * Initial Date: Apr 6, 2004
 * 
 * @author Mike Stock Comment:
 */
@Component
public class ImsCPRepositoryHandler extends FileRepositoryHandler implements RepositoryHandler {

    private static final Logger log = LoggerHelper.getLogger();

    public static final String PROCESS_CREATENEW = "new";
    public static final String PROCESS_IMPORT = "add";

    private static final boolean LAUNCHEABLE = true;
    private static final boolean DOWNLOADEABLE = true;
    private static final boolean EDITABLE = true;
    private static final boolean WIZARD_SUPPORT = false;
    private List supportedTypes;

    /**
	 * 
	 */
    protected ImsCPRepositoryHandler() {
        supportedTypes = new ArrayList(1);
        supportedTypes.add(ImsCPFileResource.TYPE_NAME);
    }

    /**
	 */
    @Override
    public List getSupportedTypes() {
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
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public MainLayoutController createLaunchController(final OLATResourceable res, final String initialViewIdentifier, final UserRequest ureq,
            final WindowControl wControl) {
        final File cpRoot = FileResourceManager.getInstance().unzipFileResource(res);
        final LocalFolderImpl vfsWrapper = new LocalFolderImpl(cpRoot);
        final Controller realController = null;

        // jump to either the forum or the folder if the business-launch-path says so.
        final BusinessControl bc = wControl.getBusinessControl();
        final ContextEntry ce = bc.popLauncherContextEntry();
        if (ce != null) { // a context path is left for me
            log.debug("businesscontrol (for further jumps) would be:" + bc);
            final OLATResourceable ores = ce.getOLATResourceable();
            log.debug("OLATResourceable=" + ores);
            final String typeName = ores.getResourceableTypeName();
            // typeName format: 'path=/test1/test2/readme.txt'
            // First remove prefix 'path='
            final String path = typeName.substring("path=".length());
            if (path.length() > 0 && !path.equals(CPCore.MANIFEST_NAME)) {
                log.debug("direct navigation to container-path=" + path);
                return CPUIFactory.getInstance().createMainLayoutResourceableListeningWrapperController(res, ureq, wControl, vfsWrapper, true, false, path);
            } else {
                return CPUIFactory.getInstance().createMainLayoutResourceableListeningWrapperController(res, ureq, wControl, vfsWrapper);
            }
        } else {
            return CPUIFactory.getInstance().createMainLayoutResourceableListeningWrapperController(res, ureq, wControl, vfsWrapper);
        }

    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public Controller createEditorController(final OLATResourceable res, final UserRequest ureq, final WindowControl wControl) {
        // only unzips, if not already unzipped
        final File cpRoot = FileResourceManager.getInstance().unzipFileResource(res);
        final LocalFolderImpl vfsWrapper = new LocalFolderImpl(cpRoot);
        return new CPEditMainController(ureq, wControl, vfsWrapper, res);

    }

    /**
     * org.olat.presentation.framework.UserRequest, org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public IAddController createAddController(final RepositoryAddCallback callback, final Object userObject, final UserRequest ureq, final WindowControl wControl) {
        if (userObject == null || userObject.equals(PROCESS_CREATENEW)) {
            return new CreateNewCPController(callback, ureq, wControl);
        } else {
            return super.createAddController(callback, userObject, ureq, wControl);
        }
    }

    @Override
    protected String getDeletedFilePrefix() {
        return "del_imscp_";
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
