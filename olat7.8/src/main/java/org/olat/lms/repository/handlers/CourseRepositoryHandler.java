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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.fileutil.ZipUtil;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.commons.i18n.I18nModule;
import org.olat.lms.commons.mediaresource.CleanupAfterDeliveryFileMediaResource;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.coordinate.LockingService;
import org.olat.lms.core.course.campus.service.CampusCourseCoreService;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.CourseModule;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.wizard.create.CourseCreationConfiguration;
import org.olat.lms.course.wizard.create.CourseCreationHelper;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.reference.ReferenceService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.user.UserService;
import org.olat.presentation.course.repository.CreateNewCourseController;
import org.olat.presentation.course.repository.ImportCourseController;
import org.olat.presentation.course.wizard.close.WizardCloseCourseController;
import org.olat.presentation.course.wizard.create.CcStep00;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.layout.MainLayoutController;
import org.olat.presentation.framework.core.control.generic.wizard.Step;
import org.olat.presentation.framework.core.control.generic.wizard.StepRunnerCallback;
import org.olat.presentation.framework.core.control.generic.wizard.StepsMainRunController;
import org.olat.presentation.framework.core.control.generic.wizard.StepsRunContext;
import org.olat.presentation.framework.core.translator.I18nPackage;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.repository.IAddController;
import org.olat.presentation.repository.RepositoryAddCallback;
import org.olat.presentation.repository.WizardCloseResourceController;
import org.olat.system.commons.CodeHelper;
import org.olat.system.commons.Settings;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.WebappHelper;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.LockResult;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial Date: Apr 15, 2004
 * 
 * @author Comment: Mike Stock
 */
@Component
public class CourseRepositoryHandler implements RepositoryHandler {

    private static final Logger log = LoggerHelper.getLogger();
    /**
     * Command to add (i.e. import) a course.
     */
    public static final String PROCESS_IMPORT = "add";
    /**
     * Command to create a new course.
     */
    public static final String PROCESS_CREATENEW = "new";

    private static final boolean LAUNCHEABLE = true;
    private static final boolean DOWNLOADEABLE = true;
    private static final boolean EDITABLE = true;
    private static final boolean WIZARD_SUPPORT = true;
    private List<String> supportedTypes;
    @Autowired
    ReferenceService referenceService;
    @Autowired
    BusinessGroupService businessGroupService;
    @Autowired
    UserService userService;
    @Autowired
    LockingService lockingService;
    @Autowired
    CampusCourseCoreService campusCourseCoreService;

    /**
	 * 
	 */
    protected CourseRepositoryHandler() {
        supportedTypes = new ArrayList<String>(1);
        supportedTypes.add(CourseModule.getCourseTypeName());
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
    public boolean supportsDownload(final RepositoryEntry repoEntry) {
        return DOWNLOADEABLE;
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
    public MainLayoutController createLaunchController(final OLATResourceable res, final String initialViewIdentifier, final UserRequest ureq,
            final WindowControl wControl) {
        return CourseFactory.createLaunchController(ureq, wControl, res, initialViewIdentifier);
    }

    /**
	 */
    @Override
    public MediaResource getAsMediaResource(final OLATResourceable res) {
        final RepositoryEntry re = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(res, true);
        String exportFileName = re.getDisplayname();
        exportFileName = StringHelper.transformDisplayNameToFileSystemName(exportFileName);

        File fExportZIP = null;
        try {
            fExportZIP = File.createTempFile(exportFileName, ".zip", new File(System.getProperty("java.io.tmpdir")));
            fExportZIP.delete(); // just use the name of this file not the file itself
            CourseFactory.exportCourseToZIP(res, fExportZIP);
        } catch (IOException e) {
            log.error(e);
        }

        return new CleanupAfterDeliveryFileMediaResource(fExportZIP);
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public Controller createEditorController(final OLATResourceable res, final UserRequest ureq, final WindowControl wControl) {
        // throw new AssertException("a course is not directly editable!!! (reason: lock is never released), res-id:"+res.getResourceableId());
        return CourseFactory.createEditorController(ureq, wControl, res);
    }

    /**
     * org.olat.presentation.framework.UserRequest, org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public IAddController createAddController(final RepositoryAddCallback callback, final Object userObject, final UserRequest ureq, final WindowControl wControl) {
        if (userObject == null || userObject.equals(PROCESS_CREATENEW)) {
            return new CreateNewCourseController(callback, ureq, wControl);
        } else if (userObject.equals(PROCESS_IMPORT)) {
            return new ImportCourseController(callback, ureq, wControl);
        } else {
            throw new AssertException("Command " + userObject + " not supported by CourseHandler.");
        }
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public Controller createWizardController(final OLATResourceable res, final UserRequest ureq, final WindowControl wControl) {
        // load the course structure
        final RepositoryEntry repoEntry = (RepositoryEntry) res;
        final ICourse course = CourseFactory.loadCourse(repoEntry.getOlatResource());
        final Translator cceTranslator = PackageUtil.createPackageTranslator(CourseCreationHelper.class, ureq.getLocale());
        final CourseCreationConfiguration courseConfig = new CourseCreationConfiguration(course.getCourseTitle(), Settings.getServerContextPathURI()
                + "/url/RepositoryEntry/" + repoEntry.getKey());
        // wizard finish callback called after "finish" is called
        final CourseCreationHelper ccHelper = new CourseCreationHelper(ureq.getLocale(), repoEntry, courseConfig, course, businessGroupService);
        final StepRunnerCallback finishCallback = new StepRunnerCallback() {
            @Override
            public Step execute(final UserRequest ureq, final WindowControl control, final StepsRunContext runContext) {
                // here goes the code which reads out the wizards data from the runContext and then does some wizardry
                ccHelper.finalizeWorkflow(ureq);
                // control.setInfo(CourseCreationMailHelper.getSuccessMessageString(ureq));
                control.setInfo(cceTranslator.translate("coursecreation.success"));
                // send notification mail
                // final MailerResult mr = CourseCreationMailHelper.sentNotificationMail(ureq, ccHelper.getConfiguration());
                // MailTemplateHelper.printErrorsAndWarnings(mr, control, ureq.getLocale());
                return StepsMainRunController.DONE_MODIFIED;
            }
        };
        final Step start = new CcStep00(ureq, courseConfig, repoEntry);
        final StepsMainRunController ccSMRC = new StepsMainRunController(ureq, wControl, start, finishCallback, null, cceTranslator.translate("coursecreation.title"));
        return ccSMRC;
    }

    @Override
    public Controller createDetailsForm(final UserRequest ureq, final WindowControl wControl, final OLATResourceable res) {
        return CourseFactory.getDetailsForm(ureq, wControl, res);
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public boolean cleanupOnDelete(final OLATResourceable res) {
        // notify all current users of this resource (course) that it will be deleted now.
        CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new OLATResourceableJustBeforeDeletedEvent(res), res);
        // archiving is done within readyToDelete
        CourseFactory.deleteCourse(res);

        campusCourseCoreService.deleteResourceableIdReference(res);

        // delete resourceable
        final OLATResourceManager rm = OLATResourceManager.getInstance();
        final OLATResource ores = rm.findResourceable(res);
        rm.deleteOLATResource(ores);
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
        /*
         * make an archive of the course nodes with valuable data
         */
        final String charset = userService.getUserCharset(ureq.getIdentity());
        CourseFactory.archiveCourse(res, charset, ureq.getLocale(), ureq.getIdentity());
        /*
		 * 
		 */
        return true;
    }

    /**
	 */
    @Override
    public OLATResourceable createCopy(final OLATResourceable res, final Identity identity) {
        return CourseFactory.copyCourse(res, identity);
    }

    /**
     * Archive the hole course with runtime-data and course-structure-data.
     * 
     */
    @Override
    public String archive(final Identity archiveOnBehalfOf, final String archivFilePath, final RepositoryEntry entry) {
        final ICourse course = CourseFactory.loadCourse(entry.getOlatResource());
        // Archive course runtime data (like delete course, archive e.g. logfiles, node-data)
        final File tmpExportDir = new File(FolderConfig.getCanonicalTmpDir() + "/" + CodeHelper.getRAMUniqueID());
        tmpExportDir.mkdirs();
        CourseFactory.archiveCourse(archiveOnBehalfOf, course, WebappHelper.getDefaultCharset(), I18nModule.getDefaultLocale(), tmpExportDir, true);
        // Archive course run structure (like course export)
        final String courseExportFileName = "course_export.zip";
        final File courseExportZIP = new File(tmpExportDir, courseExportFileName);
        CourseFactory.exportCourseToZIP(entry.getOlatResource(), courseExportZIP);
        // Zip runtime data and course run structure data into one zip-file
        final String completeArchiveFileName = "del_course_" + entry.getOlatResource().getResourceableId() + ".zip";
        final String completeArchivePath = archivFilePath + File.separator + completeArchiveFileName;
        ZipUtil.zipAll(tmpExportDir, new File(completeArchivePath), false);
        FileUtils.deleteDirsAndFiles(tmpExportDir, true, true);
        return completeArchiveFileName;
    }

    /**
	 */
    @Override
    public LockResult acquireLock(final OLATResourceable ores, final Identity identity) {
        return lockingService.acquireLock(ores, identity, CourseFactory.COURSE_EDITOR_LOCK);
    }

    /**
	 */
    @Override
    public void releaseLock(final LockResult lockResult) {
        if (lockResult != null) {
            lockingService.releaseLock(lockResult);
        }
    }

    /**
	 */
    @Override
    public boolean isLocked(final OLATResourceable ores) {
        return lockingService.isLocked(ores, CourseFactory.COURSE_EDITOR_LOCK);
    }

    @Override
    public WizardCloseResourceController createCloseResourceController(final UserRequest ureq, final WindowControl wControl, final RepositoryEntry repositoryEntry) {
        return new WizardCloseCourseController(ureq, wControl, repositoryEntry);
    }

}
