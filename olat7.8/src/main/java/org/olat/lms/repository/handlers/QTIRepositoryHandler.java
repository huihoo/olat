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

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.fileutil.ZipUtil;
import org.olat.lms.commons.fileresource.FileResource;
import org.olat.lms.commons.fileresource.FileResourceManager;
import org.olat.lms.commons.fileresource.SurveyFileResource;
import org.olat.lms.coordinate.LockingService;
import org.olat.lms.ims.qti.IQManager;
import org.olat.lms.ims.qti.IQPreviewSecurityCallback;
import org.olat.lms.ims.qti.IQSecurityCallback;
import org.olat.lms.ims.qti.process.AssessmentInstance;
import org.olat.lms.ims.qti.process.ImsRepositoryResolver;
import org.olat.lms.ims.qti.process.Resolver;
import org.olat.lms.reference.ReferenceService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.layout.MainLayoutController;
import org.olat.presentation.framework.core.translator.I18nPackage;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.coordinate.LockResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial Date: Apr 6, 2004
 * 
 * @author Mike Stock Comment:
 */
@Component
public abstract class QTIRepositoryHandler extends FileRepositoryHandler implements RepositoryHandler {

    @Autowired
    private ReferenceService referenceService;
    @Autowired
    LockingService lockingService;

    /**
     * Default constructor.
     */
    protected QTIRepositoryHandler() {
        // Implemented by specific sub-class
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public MainLayoutController createLaunchController(final OLATResourceable res, final String initialViewIdentifier, final UserRequest ureq,
            final WindowControl wControl) {
        final Resolver resolver = new ImsRepositoryResolver(res);
        final IQSecurityCallback secCallback = new IQPreviewSecurityCallback();
        final MainLayoutController runController = res.getResourceableTypeName().equals(SurveyFileResource.TYPE_NAME) ? IQManager.getInstance()
                .createIQDisplayController(res, resolver, AssessmentInstance.QMD_ENTRY_TYPE_SURVEY, secCallback, ureq, wControl) : IQManager.getInstance()
                .createIQDisplayController(res, resolver, AssessmentInstance.QMD_ENTRY_TYPE_SELF, secCallback, ureq, wControl);
        // use on column layout
        final LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, wControl, null, null, runController.getInitialComponent(), null);
        layoutCtr.addDisposableChildController(runController); // dispose content on layout dispose
        return layoutCtr;
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
        if (lockingService.isLocked(res, null)) {
            final Translator translator = PackageUtil.createPackageTranslator(I18nPackage.REPOSITORY_, ureq.getLocale());
            wControl.setError(translator.translate("details.delete.error.editor"));
            return false;
        }
        return true;
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

    /**
	 */
    @Override
    public OLATResourceable createCopy(final OLATResourceable res, final Identity identity) {
        final OLATResourceable oLATResourceable = super.createCopy(res, identity);

        final File sourceFile = FileResourceManager.getInstance().getFileResource(oLATResourceable);
        // unzip sourceFile in a temp dir, delete changelog if any, zip back, delete temp dir
        final FileResource tempFr = new FileResource();
        // move file to its new place
        final File fResourceFileroot = FileResourceManager.getInstance().getFileResourceRoot(tempFr);
        if (!FileUtils.copyFileToDir(sourceFile, fResourceFileroot, "create qti copy")) {
            return null;
        }
        final File fUnzippedDir = FileResourceManager.getInstance().unzipFileResource(tempFr);
        final File changeLogDir = new File(fUnzippedDir, "changelog");
        if (changeLogDir.exists()) {
            final boolean changeLogDeleted = FileUtils.deleteDirsAndFiles(changeLogDir, true, true);
        }
        final File targetZipFile = sourceFile;
        FileUtils.deleteDirsAndFiles(targetZipFile.getParentFile(), true, false);
        ZipUtil.zipAll(fUnzippedDir, targetZipFile, false);
        FileResourceManager.getInstance().deleteFileResource(tempFr);

        return oLATResourceable;
    }

}
