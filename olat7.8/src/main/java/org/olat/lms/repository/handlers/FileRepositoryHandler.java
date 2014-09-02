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
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.fileutil.ZipUtil;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.fileresource.FileResourceManager;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.reference.ReferenceService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.I18nPackage;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.repository.AddFileResourceController;
import org.olat.presentation.repository.IAddController;
import org.olat.presentation.repository.RepositoryAddCallback;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.spring.CoreSpringFactory;
import org.springframework.stereotype.Component;

/**
 * Common super class for all file-based handlers.
 * 
 * @author Christian Guretzki
 */
@Component
public abstract class FileRepositoryHandler {

    /**
	 * 
	 */
    protected FileRepositoryHandler() {
    }

    /**
	 */
    public MediaResource getAsMediaResource(final OLATResourceable res) {
        return FileResourceManager.getInstance().getAsDownloadeableMediaResource(res);
    }

    /**
     * org.olat.presentation.framework.UserRequest, org.olat.presentation.framework.control.WindowControl)
     */
    public IAddController createAddController(final RepositoryAddCallback callback, final Object userObject, final UserRequest ureq, final WindowControl wControl) {
        return new AddFileResourceController(callback, getSupportedTypes(), new String[] { "zip" }, ureq, wControl);
    }

    public Controller createDetailsForm(final UserRequest ureq, final WindowControl wControl, final OLATResourceable res) {
        return FileResourceManager.getInstance().getDetailsForm(ureq, wControl, res);
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    public boolean cleanupOnDelete(final OLATResourceable res) {
        // notify all current users of this resource (content packaging file resource) that it will be deleted now.
        CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new OLATResourceableJustBeforeDeletedEvent(res), res);
        FileResourceManager.getInstance().deleteFileResource(res);
        return true;
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    public boolean readyToDelete(final OLATResourceable res, final UserRequest ureq, final WindowControl wControl) {
        ReferenceService referenceService = CoreSpringFactory.getBean(ReferenceService.class);
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
    public OLATResourceable createCopy(final OLATResourceable res, final Identity identity) {
        return FileResourceManager.getInstance().createCopy(res);
    }

    public String archive(final Identity archiveOnBehalfOf, final String archivFilePath, final RepositoryEntry repoEntry) {
        final String exportFileName = getDeletedFilePrefix() + repoEntry.getOlatResource().getResourceableId() + ".zip";
        final String fullFilePath = archivFilePath + File.separator + exportFileName;
        final File rootFile = FileResourceManager.getInstance().getFileResourceRoot(repoEntry.getOlatResource());
        ZipUtil.zipAll(rootFile, new File(fullFilePath), false);
        return exportFileName;
    }

    abstract protected String getDeletedFilePrefix();

    abstract protected List getSupportedTypes();

}
