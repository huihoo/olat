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

import org.olat.data.reference.Reference;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.fileresource.TestFileResource;
import org.olat.lms.ims.qti.IQManager;
import org.olat.lms.ims.qti.IQPreviewSecurityCallback;
import org.olat.lms.ims.qti.IQSecurityCallback;
import org.olat.lms.ims.qti.process.AssessmentInstance;
import org.olat.lms.ims.qti.process.ImsRepositoryResolver;
import org.olat.lms.ims.qti.process.Resolver;
import org.olat.lms.reference.ReferenceService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.presentation.ims.qti.editor.AddNewQTIDocumentController;
import org.olat.presentation.ims.qti.editor.QTIEditorMainController;
import org.olat.presentation.repository.AddFileResourceController;
import org.olat.presentation.repository.IAddController;
import org.olat.presentation.repository.RepositoryAddCallback;
import org.olat.presentation.repository.RepositoryAddController;
import org.olat.presentation.repository.WizardCloseResourceController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.exception.AssertException;
import org.olat.system.spring.CoreSpringFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial Date: Apr 6, 2004
 * 
 * @author Mike Stock Comment:
 */
@Component
public class QTITestRepositoryHandler extends QTIRepositoryHandler {
    private static final boolean LAUNCHEABLE = true;
    private static final boolean DOWNLOADEABLE = true;
    private static final boolean EDITABLE = true;
    private static final boolean WIZARD_SUPPORT = false;

    private static List<String> supportedTypes;

    @Autowired
    private ReferenceService referenceService;

    /**
     * Default constructor.
     */
    protected QTITestRepositoryHandler() {
        super();
    }

    /**
	 */
    @Override
    public List<String> getSupportedTypes() {
        return supportedTypes;
    }

    static { // initialize supported types
        supportedTypes = new ArrayList<String>(1);
        supportedTypes.add(TestFileResource.TYPE_NAME);
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
     * @param ureq
     * @param wControl
     * @return Controller
     */
    public Controller getLaunchController(final OLATResourceable res, final UserRequest ureq, final WindowControl wControl) {
        final Resolver resolver = new ImsRepositoryResolver(res);
        final IQSecurityCallback secCallback = new IQPreviewSecurityCallback();
        final Controller runController = IQManager.getInstance().createIQDisplayController(res, resolver, AssessmentInstance.QMD_ENTRY_TYPE_SELF, secCallback, ureq,
                wControl);
        // use on column layout
        final LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, wControl, null, null, runController.getInitialComponent(), null);
        layoutCtr.addDisposableChildController(runController); // dispose content on layout dispose
        return layoutCtr;
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public Controller createEditorController(final OLATResourceable res, final UserRequest ureq, final WindowControl wControl) {
        final TestFileResource fr = CoreSpringFactory.getBean(TestFileResource.class);
        fr.overrideResourceableId(res.getResourceableId());

        // check if we can edit in restricted mode -> only typos
        final List<Reference> referencees = referenceService.getReferencesTo(res);
        final QTIEditorMainController editor = new QTIEditorMainController(referencees, ureq, wControl, fr);
        if (editor.isLockedSuccessfully()) {
            return editor;
        }
        return null;
    }

    /**
     * org.olat.presentation.framework.UserRequest, org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public IAddController createAddController(final RepositoryAddCallback callback, final Object userObject, final UserRequest ureq, final WindowControl wControl) {
        if (userObject == null || userObject.equals(RepositoryAddController.PROCESS_ADD)) {
            return new AddFileResourceController(callback, supportedTypes, new String[] { "zip" }, ureq, wControl);
        } else {
            return new AddNewQTIDocumentController(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS, callback, ureq, wControl);
        }
    }

    @Override
    protected String getDeletedFilePrefix() {
        return "del_qtitest_";
    }

    @Override
    public WizardCloseResourceController createCloseResourceController(final UserRequest ureq, final WindowControl wControl, final RepositoryEntry repositoryEntry) {
        throw new AssertException("not implemented");
    }

}
