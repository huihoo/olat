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
package org.olat.lms.repository.handlers;

import java.util.ArrayList;
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.fileresource.FileResourceManager;
import org.olat.lms.commons.fileresource.PodcastFileResource;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.reference.ReferenceService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.webfeed.FeedManager;
import org.olat.lms.webfeed.FeedResourceSecurityCallback;
import org.olat.lms.webfeed.FeedSecurityCallback;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.layout.MainLayoutController;
import org.olat.presentation.framework.core.translator.I18nPackage;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.presentation.repository.AddFileResourceController;
import org.olat.presentation.repository.IAddController;
import org.olat.presentation.repository.RepositoryAddCallback;
import org.olat.presentation.repository.WizardCloseResourceController;
import org.olat.presentation.webfeed.podcast.PodcastUIFactory;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.LockResult;
import org.olat.system.exception.AssertException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Responsible class for handling any actions involving podcast resources.
 * <P>
 * Initial Date: Feb 25, 2009 <br>
 * 
 * @author Gregor Wassmann
 */
@Component
public class PodcastRepositoryHandler implements RepositoryHandler {
    public static final String PROCESS_CREATENEW = "create_new";
    public static final String PROCESS_UPLOAD = "upload";

    private static final boolean DOWNLOADABLE = true;
    private static final boolean EDITABLE = true;
    private static final boolean LAUNCHABLE = true;
    private static final boolean WIZARD_SUPPORT = false;
    private List<String> supportedTypes;
    @Autowired
    private ReferenceService referenceService;

    /**
	 * 
	 */
    protected PodcastRepositoryHandler() {
        supportedTypes = new ArrayList<String>(1);
        supportedTypes.add(PodcastFileResource.TYPE_NAME);
    }

    /**
	 */
    @Override
    public LockResult acquireLock(final OLATResourceable ores, final Identity identity) {
        return FeedManager.getInstance().acquireLock(ores, identity);
    }

    /**
	 */
    @Override
    public String archive(final Identity archiveOnBehalfOf, final String archivFilePath, final RepositoryEntry repoEntry) {
        // Apperantly, this method is used for backing up any user related content
        // (comments etc.) on deletion. Up to now, this doesn't exist in podcasts.
        return null;
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public boolean cleanupOnDelete(final OLATResourceable res) {
        CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new OLATResourceableJustBeforeDeletedEvent(res), res);
        // For now, notifications are not implemented since a podcast feed is meant
        // to be subscriped to anyway.
        // NotificationServiceProvider.getNotificationService().deletePublishersOf(res);
        FeedManager.getInstance().delete(res);
        return true;
    }

    /**
	 */
    @Override
    public OLATResourceable createCopy(final OLATResourceable res, final Identity identity) {
        final FeedManager manager = FeedManager.getInstance();
        return manager.copy(res);
    }

    /**
     * org.olat.presentation.framework.UserRequest, org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public IAddController createAddController(final RepositoryAddCallback callback, final Object userObject, final UserRequest ureq, final WindowControl wControl) {
        IAddController addCtr = null;
        if (userObject == null || userObject.equals(PROCESS_UPLOAD)) {
            addCtr = new AddFileResourceController(callback, supportedTypes, new String[] { "zip" }, ureq, wControl);
        } else {
            addCtr = PodcastUIFactory.getInstance(ureq.getLocale()).createAddController(callback, ureq, wControl);
        }
        return addCtr;
    }

    /**
	 */
    @Override
    public MediaResource getAsMediaResource(final OLATResourceable res) {
        final FeedManager manager = FeedManager.getInstance();
        return manager.getFeedArchiveMediaResource(res);
    }

    @Override
    public Controller createDetailsForm(final UserRequest ureq, final WindowControl wControl, final OLATResourceable res) {
        return FileResourceManager.getInstance().getDetailsForm(ureq, wControl, res);
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public Controller createEditorController(final OLATResourceable res, final UserRequest ureq, final WindowControl control) {
        // Return the launch controller. Owners and admins will be able to edit the
        // podcast 'inline'.
        return createLaunchController(res, null, ureq, control);
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public MainLayoutController createLaunchController(final OLATResourceable res, final String initialViewIdentifier, final UserRequest ureq,
            final WindowControl wControl) {
        final RepositoryEntry repoEntry = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(res, false);
        final boolean isAdmin = ureq.getUserSession().getRoles().isOLATAdmin();
        final boolean isOwner = RepositoryServiceImpl.getInstance().isOwnerOfRepositoryEntry(ureq.getIdentity(), repoEntry);
        final FeedSecurityCallback callback = new FeedResourceSecurityCallback(isAdmin, isOwner);
        final Controller podcastCtr = PodcastUIFactory.getInstance(ureq.getLocale()).createMainController(res, ureq, wControl, callback);
        final LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, wControl, null, null, podcastCtr.getInitialComponent(), null);
        layoutCtr.addDisposableChildController(podcastCtr);
        return layoutCtr;
    }

    /**
	 */
    @Override
    public List<String> getSupportedTypes() {
        return supportedTypes;
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
    public void releaseLock(final LockResult lockResult) {
        FeedManager.getInstance().releaseLock(lockResult);
    }

    /**
	 */
    @Override
    public boolean supportsDownload(final RepositoryEntry repoEntry) {
        return DOWNLOADABLE;
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
    public boolean supportsLaunch(final RepositoryEntry repoEntry) {
        return LAUNCHABLE;
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
     * org.olat.data.repository.RepositoryEntry)
     */
    @Override
    public WizardCloseResourceController createCloseResourceController(final UserRequest ureq, final WindowControl control, final RepositoryEntry repositoryEntry) {
        // This was copied from WikiHandler. No specific close wizard is
        // implemented.
        throw new AssertException("not implemented");
    }

    /**
	 */
    @Override
    public boolean isLocked(final OLATResourceable ores) {
        return FeedManager.getInstance().isLocked(ores);
    }

}
