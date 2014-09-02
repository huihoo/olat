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
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.fileresource.AnimationFileResource;
import org.olat.lms.commons.fileresource.DocFileResource;
import org.olat.lms.commons.fileresource.FileResource;
import org.olat.lms.commons.fileresource.ImageFileResource;
import org.olat.lms.commons.fileresource.MovieFileResource;
import org.olat.lms.commons.fileresource.PdfFileResource;
import org.olat.lms.commons.fileresource.PowerpointFileResource;
import org.olat.lms.commons.fileresource.SoundFileResource;
import org.olat.lms.commons.fileresource.XlsFileResource;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.layout.MainLayoutController;
import org.olat.presentation.repository.AddFileResourceController;
import org.olat.presentation.repository.IAddController;
import org.olat.presentation.repository.RepositoryAddCallback;
import org.olat.presentation.repository.WizardCloseResourceController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.coordinate.LockResult;
import org.olat.system.exception.AssertException;
import org.springframework.stereotype.Component;

/**
 * Initial Date: Apr 6, 2004
 * 
 * @author Mike Stock Comment:
 */
@Component
public class WebDocumentRepositoryHandler extends FileRepositoryHandler implements RepositoryHandler {

    private static final boolean LAUNCHEABLE = false;
    private static final boolean DOWNLOADEABLE = true;
    private static final boolean EDITABLE = false;
    private static final boolean WIZARD_SUPPORT = false;
    private List supportedTypes;

    /**
     * Default constructor.
     */
    protected WebDocumentRepositoryHandler() {
        supportedTypes = new ArrayList(5);
        supportedTypes.add(FileResource.GENERIC_TYPE_NAME);
        supportedTypes.add(DocFileResource.TYPE_NAME);
        supportedTypes.add(XlsFileResource.TYPE_NAME);
        supportedTypes.add(PowerpointFileResource.TYPE_NAME);
        supportedTypes.add(PdfFileResource.TYPE_NAME);
        supportedTypes.add(SoundFileResource.TYPE_NAME);
        supportedTypes.add(MovieFileResource.TYPE_NAME);
        supportedTypes.add(AnimationFileResource.TYPE_NAME);
        supportedTypes.add(ImageFileResource.TYPE_NAME);
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
        /*
         * For the time beeing, disable launching web ressources... FileResourceManager frm = FileResourceManager.getInstance();
         * ureq.getDispatchResult().setResultingMediaResource(frm.getAsInlineMediaResource(res));
         */
        return null;
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public Controller createEditorController(final OLATResourceable res, final UserRequest ureq, final WindowControl wControl) {
        throw new AssertException("a web document is not editable!!! res-id:" + res.getResourceableId());
    }

    /**
     * org.olat.presentation.framework.UserRequest, org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public IAddController createAddController(final RepositoryAddCallback callback, final Object userObject, final UserRequest ureq, final WindowControl wControl) {
        return new AddFileResourceController(callback, supportedTypes, ureq, wControl);
    }

    @Override
    protected String getDeletedFilePrefix() {
        return "del_webdoc_";
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
