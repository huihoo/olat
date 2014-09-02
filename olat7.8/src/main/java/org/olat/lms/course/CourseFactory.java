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

package org.olat.lms.course;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Constants;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.commons.fileutil.ExportUtil;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.fileutil.ZipUtil;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.xml.XStreamHelper;
import org.olat.data.reference.Reference;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.calendar.CalendarService;
import org.olat.lms.commons.context.BusinessControlFactory;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.commons.tree.INode;
import org.olat.lms.commons.tree.TreeVisitor;
import org.olat.lms.commons.tree.Visitor;
import org.olat.lms.commons.util.ObjectCloner;
import org.olat.lms.course.archiver.ScoreAccountingHelper;
import org.olat.lms.course.config.CourseConfig;
import org.olat.lms.course.config.CourseConfigManagerImpl;
import org.olat.lms.course.imports.CourseExportEBL;
import org.olat.lms.course.imports.ImportGlossaryEBL;
import org.olat.lms.course.imports.ImportSharedFolderEBL;
import org.olat.lms.course.nodes.BCCourseNode;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.STCourseNode;
import org.olat.lms.course.nodes.TACourseNode;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.lms.course.statistic.AsyncExportManager;
import org.olat.lms.course.tree.CourseEditorTreeModel;
import org.olat.lms.course.tree.CourseEditorTreeNode;
import org.olat.lms.glossary.GlossaryManager;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.lms.learn.notification.service.NotificationLearnService;
import org.olat.lms.reference.ReferenceService;
import org.olat.lms.repository.RepositoryEntryImportExport;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.sharedfolder.SharedFolderManager;
import org.olat.presentation.commons.session.UserSession;
import org.olat.presentation.course.editor.EditorMainController;
import org.olat.presentation.course.run.RunMainController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.htmlheader.jscss.CustomCSS;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.layout.MainLayoutController;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.system.commons.CodeHelper;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.LockResult;
import org.olat.system.coordinate.SyncerCallback;
import org.olat.system.coordinate.SyncerExecutor;
import org.olat.system.coordinate.cache.CacheWrapper;
import org.olat.system.event.MultiUserEvent;
import org.olat.system.exception.AssertException;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;
import org.olat.testutils.codepoints.server.Codepoint;

/**
 * Description: <BR>
 * Use the course factory to create course run and edit controllers or to load a course from disk Initial Date: Oct 12, 2004
 * 
 * @author Felix Jost
 * @author guido
 */
public class CourseFactory extends BasicManager {

    /**
     * TODO: gs, spring does not support autowiring of static fields, so lets get rid of that static bad smell stuff
     */
    private static CacheWrapper loadedCourses;
    private static ConcurrentHashMap<Long, ModifyCourseEvent> modifyCourseEvents = new ConcurrentHashMap<Long, ModifyCourseEvent>();

    public static final String COURSE_EDITOR_LOCK = "courseEditLock";
    // this is the lock that must be aquired at course editing, copy course, export course, configure course.
    private static LockResult lockEntry;
    private static Map<Long, PersistingCourseImpl> courseEditSessionMap = new HashMap<Long, PersistingCourseImpl>();
    private static final Logger log = LoggerHelper.getLogger();

    private static RepositoryService repositoryManager;
    private static OLATResourceManager olatResourceManager;
    private static BaseSecurity securityManager;
    private static ReferenceService referenceService;
    private static GlossaryManager glossaryManager;
    private static CourseGroupManager courseGroupManager;

    /**
     * [used by spring] autowiring not possible because of static access
     */
    private CourseFactory(final CoordinatorManager coordinatorManager, final RepositoryService repositoryManager, final OLATResourceManager olatResourceManager,
            final BaseSecurity securityManager, ReferenceService referenceService, GlossaryManager glossaryManager, final CourseGroupManager courseGroupManager) {
        loadedCourses = coordinatorManager.getCoordinator().getCacher().getOrCreateCache(CourseFactory.class, "courses");
        CourseFactory.repositoryManager = repositoryManager;
        CourseFactory.olatResourceManager = olatResourceManager;
        CourseFactory.securityManager = securityManager;
        CourseFactory.referenceService = referenceService;
        CourseFactory.glossaryManager = glossaryManager;
        CourseFactory.courseGroupManager = courseGroupManager;
    }

    /**
     * Create a run controller for the given course resourceable
     * 
     * @param ureq
     * @param wControl
     * @param olatResource
     * @param initialViewIdentifier
     *            if null the default view will be started, otherwise a controllerfactory type dependant view will be activated (subscription subtype)
     * @return run controller for the given course resourceable
     */
    public static MainLayoutController createLaunchController(final UserRequest ureq, final WindowControl wControl, final OLATResourceable olatResource,
            final String initialViewIdentifier) {
        final ICourse course = loadCourse(olatResource);
        long startT = 0;
        if (log.isDebugEnabled()) {
            startT = System.currentTimeMillis();
        }
        final MainLayoutController launchC = new RunMainController(ureq, wControl, course, initialViewIdentifier, true, true);
        if (log.isDebugEnabled()) {
            log.debug("Runview for [[" + course.getCourseTitle() + "]] took [ms]" + (System.currentTimeMillis() - startT));
        }

        return launchC;
    }

    /**
     * Create an editor controller for the given course resourceable
     * 
     * @param ureq
     * @param wControl
     * @param olatResource
     * @return editor controller for the given course resourceable; if the editor is already locked, it returns a controller with a lock message
     */
    public static Controller createEditorController(final UserRequest ureq, final WindowControl wControl, final OLATResourceable olatResource) {
        final ICourse course = loadCourse(olatResource);
        final EditorMainController emc = new EditorMainController(ureq, wControl, course);
        if (!emc.getLockEntry().isSuccess()) {
            // get i18n from the course runmaincontroller to say that this editor is
            // already locked by another person

            final Translator translator = PackageUtil.createPackageTranslator(RunMainController.class, ureq.getLocale());
            wControl.setWarning(translator.translate("error.editoralreadylocked", new String[] { emc.getLockEntry().getOwner().getName() }));
            return null;
            // return new MonologController(ureq.getLocale(), translator.translate("error.editoralreadylocked", new String[] { emc.getLockEntry()
            // .getOwner().getName() }), null, true);
        }
        // set the logger if editor is started
        // since 5.2 groups / areas can be created from the editor -> should be logged.
        emc.addLoggingResourceable(LoggingResourceable.wrap(course));
        return emc;
    }

    /**
     * Creates an empty course with a single root node. The course is linked to the resourceable ores.
     * 
     * @param ores
     * @param shortTitle
     *            Short title of root node
     * @param longTitle
     *            Long title of root node
     * @param learningObjectives
     *            Learning objectives of root node
     * @return an empty course with a single root node.
     */
    public static ICourse createEmptyCourse(final OLATResourceable ores, final String shortTitle, final String longTitle, final String learningObjectives) {
        final PersistingCourseImpl newCourse = new PersistingCourseImpl(ores.getResourceableId());
        // Put new course in course cache
        putCourseInCache(newCourse.getResourceableId(), newCourse);

        final Structure initialStructure = new Structure();
        final CourseNode runRootNode = new STCourseNode();
        runRootNode.setShortTitle(shortTitle);
        runRootNode.setLongTitle(longTitle);
        runRootNode.setLearningObjectives(learningObjectives);
        initialStructure.setRootNode(runRootNode);
        newCourse.setRunStructure(initialStructure);
        newCourse.saveRunStructure();

        final CourseEditorTreeModel editorTreeModel = new CourseEditorTreeModel();
        final CourseEditorTreeNode editorRootNode = new CourseEditorTreeNode((CourseNode) ObjectCloner.deepCopy(runRootNode));
        editorTreeModel.setRootNode(editorRootNode);
        newCourse.setEditorTreeModel(editorTreeModel);
        newCourse.saveEditorTreeModel();

        return newCourse;
    }

    /**
     * Gets the course from cache if already there, or loads the course and puts it into cache. To be called for the "CourseRun" model.
     * 
     * @param resourceableId
     * @return the course with the given id (the type is always CourseModule.class.toString())
     */
    public static ICourse loadCourse(final Long resourceableId) {
        if (resourceableId == null) {
            throw new AssertException("No resourceable ID found.");
        }
        PersistingCourseImpl course = getCourseFromCache(resourceableId);
        if (course == null) {
            // o_clusterOK by:ld - load and put in cache in doInSync block to ensure
            // that no invalidate cache event was missed
            if (log.isDebugEnabled()) {
                log.debug("try to load course with resourceableId=" + resourceableId);
            }
            final OLATResourceable courseResourceable = OresHelper.createOLATResourceableInstance(PersistingCourseImpl.class, resourceableId);
            course = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(courseResourceable, new SyncerCallback<PersistingCourseImpl>() {
                @Override
                public PersistingCourseImpl execute() {
                    PersistingCourseImpl theCourse = null;
                    theCourse = getCourseFromCache(resourceableId);
                    if (theCourse == null) {
                        long startTime = 0;
                        long endTime = 0;
                        if (log.isDebugEnabled()) {
                            startTime = System.currentTimeMillis();
                        }
                        theCourse = new PersistingCourseImpl(resourceableId);
                        theCourse.load();
                        if (log.isDebugEnabled()) {
                            endTime = System.currentTimeMillis();
                        }
                        putCourseInCache(resourceableId, theCourse);
                        long diff = 0;
                        if (log.isDebugEnabled()) {
                            diff = Long.valueOf(endTime - startTime);
                        }
                        if (log.isDebugEnabled()) {
                            log.debug("[[" + resourceableId + "[[" + diff + "[[" + theCourse.getCourseTitle());
                        }
                    }
                    return theCourse;
                }
            });
        }

        return course;
    }

    /**
     * Load the course for the given course resourceable
     * 
     * @param olatResource
     * @return the course for the given course resourceable
     */
    public static ICourse loadCourse(final OLATResourceable olatResource) {
        final Long resourceableId = olatResource.getResourceableId();
        return loadCourse(resourceableId);
    }

    /**
     * @param resourceableId
     * @return the PersistingCourseImpl instance for the input key.
     */
    static PersistingCourseImpl getCourseFromCache(final Long resourceableId) { // o_clusterOK by:ld
        return (PersistingCourseImpl) loadedCourses.get(String.valueOf(resourceableId.longValue()));
    }

    /**
     * Puts silent.
     * 
     * @param resourceableId
     * @param course
     */
    static void putCourseInCache(final Long resourceableId, final PersistingCourseImpl course) { // o_clusterOK by:ld
        loadedCourses.put(String.valueOf(resourceableId.longValue()), course);
        log.debug("putCourseInCache ");
    }

    /**
     * @param resourceableId
     */
    private static void removeFromCache(final Long resourceableId) { // o_clusterOK by: ld
        loadedCourses.remove(String.valueOf(resourceableId.longValue()));
        log.debug("removeFromCache ");
    }

    /**
     * Puts the current course in the local cache and removes it from other caches (other cluster nodes).
     * 
     * @param resourceableId
     * @param course
     */
    private static void updateCourseInCache(final Long resourceableId, final PersistingCourseImpl course) { // o_clusterOK by:ld
        loadedCourses.update(String.valueOf(resourceableId.longValue()), course);
        log.debug("updateCourseInCache ");
    }

    /**
     * Delete a course including its course folder and all references to resources this course holds.
     * 
     * @param res
     */
    public static void deleteCourse(final OLATResourceable res) {
        final CalendarService calendarService = CoreSpringFactory.getBean(CalendarService.class);
        final long start = System.currentTimeMillis();
        log.info("deleteCourse: starting to delete course. res=" + res);
        final PersistingCourseImpl course = (PersistingCourseImpl) loadCourse(res);

        // find all references to course
        final List refs = referenceService.getReferences(course);
        for (final Iterator iter = refs.iterator(); iter.hasNext();) {
            final Reference ref = (Reference) iter.next();
            referenceService.delete(ref);
        }
        // call cleanupOnDelet for nodes
        final Visitor visitor = new NodeDeletionVisitor(course);
        final TreeVisitor tv = new TreeVisitor(visitor, course.getRunStructure().getRootNode(), true);
        tv.visitAll();

        // delete course configuration
        CourseConfigManagerImpl.getInstance().deleteConfigOf(course);
        // delete course group- and rightmanagement
        course.getCourseEnvironment().getCourseGroupManager().deleteCourseGroupmanagement(course);
        // delete all remaining course properties
        course.getCourseEnvironment().getCoursePropertyManager().deleteAllCourseProperties();
        // delete course calendar
        calendarService.deleteCourseCalendar(course);
        // cleanup cache
        removeFromCache(res.getResourceableId());
        // TODO: ld: broadcast event: DeleteCourseEvent

        // Everything is deleted, so we could get rid of course logging
        // with the change in user audit logging - which now all goes into a DB
        // we no longer do this though!

        // delete course directory
        final File fCourseBasePath = course.getCourseBaseContainer().getBasefile();
        final boolean deletionSuccessful = FileUtils.deleteDirsAndFiles(fCourseBasePath, true, true);
        log.info("deleteCourse: finished deletion. res=" + res + ", deletion successful: " + deletionSuccessful + ", duration: " + (System.currentTimeMillis() - start)
                + " ms.");
    }

    private static NotificationLearnService getNotificationLearnService() {
        return CoreSpringFactory.getBean(NotificationLearnService.class);
    }

    private static CourseExportEBL getCourseExportEBL() {
        return CoreSpringFactory.getBean(CourseExportEBL.class);
    }

    /**
     * Copies a course. More specifically, the run and editor structures and the course folder will be copied to create a new course.
     * 
     * @param sourceRes
     * @param ureq
     * @return copy of the course.
     */
    public static OLATResourceable copyCourse(final OLATResourceable sourceRes, final Identity identity) {

        final PersistingCourseImpl sourceCourse = (PersistingCourseImpl) loadCourse(sourceRes);

        final OLATResourceable targetRes = OLATResourceManager.getInstance().createOLATResourceInstance(CourseModule.class);
        final PersistingCourseImpl targetCourse = new PersistingCourseImpl(targetRes.getResourceableId());
        // Put new course in course cache
        putCourseInCache(targetRes.getResourceableId(), targetCourse);

        final File fTargetCourseBasePath = targetCourse.getCourseBaseContainer().getBasefile();

        synchronized (sourceCourse) { // o_clusterNOK - cannot be solved with doInSync since could take too long (leads to error: "Lock wait timeout exceeded")
            // copy configuration
            final CourseConfig courseConf = CourseConfigManagerImpl.getInstance().copyConfigOf(sourceCourse);
            targetCourse.setCourseConfig(courseConf);
            // save structures
            targetCourse.setRunStructure((Structure) XStreamHelper.xstreamClone(sourceCourse.getRunStructure()));
            targetCourse.saveRunStructure();
            targetCourse.setEditorTreeModel((CourseEditorTreeModel) XStreamHelper.xstreamClone(sourceCourse.getEditorTreeModel()));
            targetCourse.saveEditorTreeModel();

            Codepoint.codepoint(CourseFactory.class, "copyCourseAfterSaveTreeModel");

            // copy course folder
            final File fSourceCourseFolder = sourceCourse.getIsolatedCourseFolder().getBasefile();
            if (fSourceCourseFolder.exists()) {
                FileUtils.copyDirToDir(fSourceCourseFolder, fTargetCourseBasePath, false, "copy course folder");
            }

            // copy folder nodes directories
            final File fSourceFoldernodesFolder = new File(FolderConfig.getCanonicalRoot()
                    + BCCourseNode.getFoldernodesPathRelToFolderBase(sourceCourse.getCourseEnvironment()));
            if (fSourceFoldernodesFolder.exists()) {
                FileUtils.copyDirToDir(fSourceFoldernodesFolder, fTargetCourseBasePath, false, "copy folder nodes directories");
            }

            // copy task folder directories
            final File fSourceTaskfoldernodesFolder = new File(FolderConfig.getCanonicalRoot()
                    + TACourseNode.getTaskFoldersPathRelToFolderRoot(sourceCourse.getCourseEnvironment()));
            if (fSourceTaskfoldernodesFolder.exists()) {
                FileUtils.copyDirToDir(fSourceTaskfoldernodesFolder, fTargetCourseBasePath, false, "copy task folder directories");
            }

            // update references
            final List refs = referenceService.getReferences(sourceCourse);
            for (final Iterator iter = refs.iterator(); iter.hasNext();) {
                final Reference ref = (Reference) iter.next();
                referenceService.addReference(targetCourse, ref.getTarget(), ref.getUserdata());
            }
            courseGroupManager.createCourseGroupmanagementAsCopy(sourceCourse, sourceCourse.getCourseTitle(), targetCourse);
        }
        return targetRes;
    }

    /**
     * Exports an entire course to a zip file.
     * 
     * @param sourceRes
     * @param fTargetZIP
     * @return true if successfully exported, false otherwise.
     */
    public static void exportCourseToZIP(final OLATResourceable sourceRes, final File fTargetZIP) {
        final PersistingCourseImpl sourceCourse = (PersistingCourseImpl) loadCourse(sourceRes);

        // add files to ZIP
        final File fExportDir = new File(System.getProperty("java.io.tmpdir") + File.separator + CodeHelper.getRAMUniqueID());
        fExportDir.mkdirs();
        synchronized (sourceCourse) { // o_clusterNOK - cannot be solved with doInSync since could take too long (leads to error: "Lock wait timeout exceeded")
            sourceCourse.exportToFilesystem(fExportDir);
            Codepoint.codepoint(CourseFactory.class, "longExportCourseToZIP");
            final Set<String> fileSet = new HashSet<String>();
            final String[] files = fExportDir.list();
            for (int i = 0; i < files.length; i++) {
                fileSet.add(files[i]);
            }
            ZipUtil.zip(fileSet, fExportDir, fTargetZIP, false);
            FileUtils.deleteDirsAndFiles(fExportDir, true, true);
        }
    }

    /**
     * Import a course from a ZIP file.
     * 
     * @param ores
     * @param zipFile
     * @return New Course.
     */
    public static ICourse importCourseFromZip(final OLATResourceable ores, final File zipFile) {
        // Generate course with filesystem
        final PersistingCourseImpl newCourse = new PersistingCourseImpl(ores.getResourceableId());
        CourseConfigManagerImpl.getInstance().deleteConfigOf(newCourse);

        // Unzip course strucure in new course
        final File fCanonicalCourseBasePath = newCourse.getCourseBaseContainer().getBasefile();
        if (ZipUtil.unzip(zipFile, fCanonicalCourseBasePath)) {
            // Load course strucure now
            try {
                newCourse.load();
                final CourseConfig cc = CourseConfigManagerImpl.getInstance().loadConfigFor(newCourse);
                // newCourse is not in cache yet, so we cannot call setCourseConfig()
                newCourse.setCourseConfig(cc);
                putCourseInCache(newCourse.getResourceableId(), newCourse);
                return newCourse;
            } catch (final AssertException ae) {
                // ok failed, cleanup below
                // better logging to search error
                log.error("rollback importCourseFromZip", ae);
            }
        }
        // cleanup if not successfull
        FileUtils.deleteDirsAndFiles(fCanonicalCourseBasePath, true, true);
        return null;
    }

    /**
     * Deploys a course from an exported course ZIP file. This process is unattended and therefore relies on some default assumptions on how to setup the entry and add
     * any referenced resources to the repository.
     * 
     * @param exportedCourseZIPFile
     */
    public static RepositoryEntry deployCourseFromZIP(final File exportedCourseZIPFile, final int access) {
        // create the course instance
        OLATResource newCourseResource = olatResourceManager.createOLATResourceInstance(CourseModule.class);
        ICourse course = CourseFactory.importCourseFromZip(newCourseResource, exportedCourseZIPFile);
        // course is now also in course cache!
        if (course == null) {
            log.error("Error deploying course from ZIP: " + exportedCourseZIPFile.getAbsolutePath());
            return null;
        }
        final File courseExportData = getCourseExportEBL().getExportDataDir(course);
        // get the export data directory
        // create the repository entry
        final RepositoryEntry re = repositoryManager.createRepositoryEntryInstance("administrator");
        final RepositoryEntryImportExport importExport = new RepositoryEntryImportExport(courseExportData);
        final String softKey = importExport.getSoftkey();
        final RepositoryEntry existingEntry = repositoryManager.lookupRepositoryEntryBySoftkey(softKey, false);
        if (existingEntry != null) {
            log.info("RepositoryEntry with softkey " + softKey + " already exists. Course will not be deployed.");
            CourseFactory.deleteCourse(newCourseResource);
            return existingEntry;
        }
        // ok, continue import
        newCourseResource = olatResourceManager.findOrPersistResourceable(newCourseResource);
        re.setOlatResource(newCourseResource);
        re.setSoftkey(softKey);
        re.setInitialAuthor(importExport.getInitialAuthor());
        re.setDisplayname(importExport.getDisplayName());
        re.setResourcename(importExport.getResourceName());
        re.setDescription(importExport.getDescription());
        re.setCanLaunch(true);

        // set access configuration
        re.setAccess(access);

        course = openCourseEditSession(course.getResourceableId());
        // create group management
        courseGroupManager.createCourseGroupmanagement(course.getResourceableId().toString(), newCourseResource);
        // import groups
        courseGroupManager.importCourseLearningGroups(courseExportData, newCourseResource);
        courseGroupManager.importCourseRightGroups(courseExportData, newCourseResource);

        // create security group
        final SecurityGroup ownerGroup = securityManager.createAndPersistSecurityGroup();
        // member of this group may modify member's membership
        securityManager.createAndPersistPolicy(ownerGroup, Constants.PERMISSION_ACCESS, ownerGroup);
        // members of this group are always authors also
        securityManager.createAndPersistPolicy(ownerGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_AUTHOR);
        securityManager.addIdentityToSecurityGroup(securityManager.findIdentityByName("administrator"), ownerGroup);
        re.setOwnerGroup(ownerGroup);
        // save the repository entry
        repositoryManager.saveRepositoryEntry(re);
        // Create course admin policy for owner group of repository entry
        // -> All owners of repository entries are course admins
        securityManager.createAndPersistPolicy(re.getOwnerGroup(), Constants.PERMISSION_ADMIN, re.getOlatResource());

        // deploy any referenced repository entries of the editor structure. This will also
        // include any references in the run structure, since any node in the runstructure is also
        // present in the editor structure.
        deployReferencedRepositoryEntries(courseExportData, course, (CourseEditorTreeNode) course.getEditorTreeModel().getRootNode());
        // register any references in the run structure. The referenced entries have been
        // previousely deplyed (as part of the editor structure deployment process - see above method call)
        registerReferences(course, course.getRunStructure().getRootNode());
        // import shared folder references
        deployReferencedSharedFolders(courseExportData, course);
        // import glossary references
        deployReferencedGlossary(courseExportData, course);
        closeCourseEditSession(course.getResourceableId(), true);
        // cleanup export data
        FileUtils.deleteDirsAndFiles(courseExportData, true, true);
        log.info("Successfully deployed course " + re.getDisplayname() + " from ZIP: " + exportedCourseZIPFile.getAbsolutePath());
        return re;
    }

    /**
     * Unattended deploy any referenced repository entries.
     * 
     * @param importDirectory
     * @param course
     * @param currentNode
     */
    private static void deployReferencedRepositoryEntries(final File importDirectory, final ICourse course, final CourseEditorTreeNode currentNode) {
        for (int i = 0; i < currentNode.getChildCount(); i++) {
            final CourseEditorTreeNode childNode = (CourseEditorTreeNode) currentNode.getChildAt(i);
            childNode.getCourseNode().importNode(importDirectory, course, true, null, null);
            deployReferencedRepositoryEntries(importDirectory, course, childNode);
        }
    }

    /**
     * Register any referenced repository entries.
     * 
     * @param course
     * @param currentNode
     */
    private static void registerReferences(final ICourse course, final CourseNode currentNode) {
        for (int i = 0; i < currentNode.getChildCount(); i++) {
            final CourseNode childNode = (CourseNode) currentNode.getChildAt(i);
            if (childNode.needsReferenceToARepositoryEntry()) {
                referenceService.addReference(course, childNode.getReferencedRepositoryEntry().getOlatResource(), childNode.getIdent());
            }
            registerReferences(course, childNode);
        }
    }

    private static void deployReferencedSharedFolders(final File importDirectory, final ICourse course) {
        final CourseConfig cc = course.getCourseEnvironment().getCourseConfig();
        if (!cc.hasCustomSharedFolder()) {
            return;
        }
        final RepositoryEntryImportExport importExport = SharedFolderManager.getInstance().getRepositoryImportExport(importDirectory);
        final Identity owner = getBaseSecurity().findIdentityByName("administrator");
        getImportSharedFolderEBL().doImport(importExport, course, false, owner);
    }

    private static ImportSharedFolderEBL getImportSharedFolderEBL() {
        return CoreSpringFactory.getBean(ImportSharedFolderEBL.class);
    }

    private static BaseSecurity getBaseSecurity() {
        return (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
    }

    /**
     * Deploy referenced glossaries using the administrator account as owner
     * 
     * @param importDirectory
     * @param course
     */
    private static void deployReferencedGlossary(final File importDirectory, final ICourse course) {
        final CourseConfig cc = course.getCourseEnvironment().getCourseConfig();
        if (!cc.hasGlossary()) {
            return;
        }
        final RepositoryEntryImportExport importExport = glossaryManager.getRepositoryImportExport(importDirectory);
        final Identity owner = securityManager.findIdentityByName("administrator");
        getImportGlossaryEBL().doImport(importExport, course, false, owner);
    }

    private static ImportGlossaryEBL getImportGlossaryEBL() {
        return CoreSpringFactory.getBean(ImportGlossaryEBL.class);
    }

    /**
     * Get a details form for a given course resourceable
     * 
     * @param res
     * @param ureq
     * @return details component displaying details of the course.
     */
    public static Controller getDetailsForm(final UserRequest ureq, final WindowControl wControl, final OLATResourceable res) {
        // course does not provide a details component, this is somehow hardcoded in the
        // RepositoryDetailsController
        return null;
    }

    /**
     * Create a user locale dependent help-course run controller
     * 
     * @param ureq
     *            The user request
     * @param wControl
     *            The current window controller
     * @return The help-course run controller
     */
    public static Controller createHelpCourseLaunchController(final UserRequest ureq, final WindowControl wControl) {
        // Find repository entry for this course
        final String helpCourseSoftKey = CourseModule.getHelpCourseSoftKey();
        final RepositoryService rm = RepositoryServiceImpl.getInstance();
        RepositoryEntry entry = null;
        if (helpCourseSoftKey != null) {
            entry = rm.lookupRepositoryEntryBySoftkey(helpCourseSoftKey, false);
        }
        if (entry == null) {
            final Translator translator = PackageUtil.createPackageTranslator(CourseFactory.class, ureq.getLocale());
            wControl.setError(translator.translate("error.helpcourse.not.configured"));
            // create empty main controller
            final LayoutMain3ColsController emptyCtr = new LayoutMain3ColsController(ureq, wControl, null, null, null, null);
            return emptyCtr;
        } else {
            // Increment launch counter
            rm.incrementLaunchCounter(entry);
            final OLATResource ores = entry.getOlatResource();
            final ICourse course = loadCourse(ores);

            final ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(entry);
            final WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, wControl);

            final RunMainController launchC = new RunMainController(ureq, bwControl, course, null, false, false);
            return launchC;
        }
    }

    /**
     * visit all nodes in the specified course and make them archiving any data into the identity's export directory.
     * 
     * @param res
     * @param charset
     * @param locale
     * @param identity
     */
    public static void archiveCourse(final OLATResourceable res, final String charset, final Locale locale, final Identity identity) {
        final PersistingCourseImpl course = (PersistingCourseImpl) loadCourse(res);
        final File exportDirectory = CourseFactory.getOrCreateDataExportDirectory(identity, course.getCourseTitle());
        final boolean isOLATAdmin = getBaseSecurity().isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_ADMIN);
        final boolean isOresOwner = RepositoryServiceImpl.getInstance().isOwnerOfRepositoryEntry(identity,
                RepositoryServiceImpl.getInstance().lookupRepositoryEntry(res, false));
        final boolean isOresInstitutionalManager = RepositoryServiceImpl.getInstance().isInstitutionalRessourceManagerFor(
                RepositoryServiceImpl.getInstance().lookupRepositoryEntry(res, false), identity);
        archiveCourse(identity, course, charset, locale, exportDirectory, isOLATAdmin, isOresOwner, isOresInstitutionalManager);
    }

    /**
     * visit all nodes in the specified course and make them archiving any data into the identity's export directory.
     * 
     * @param res
     * @param charset
     * @param locale
     * @param identity
     */
    public static void archiveCourse(final Identity archiveOnBehalfOf, final ICourse course, final String charset, final Locale locale, final File exportDirectory,
            final boolean isOLATAdmin, final boolean... oresRights) {
        // archive course results overview
        final List users = ScoreAccountingHelper.loadUsers(course.getCourseEnvironment());
        final List nodes = ScoreAccountingHelper.loadAssessableNodes(course.getCourseEnvironment());

        final String result = ScoreAccountingHelper.createCourseResultsOverviewTable(users, nodes, course, locale);
        final String fileName = ExportUtil.createFileNameWithTimeStamp(course.getCourseTitle(), "xls");
        ExportUtil.writeContentToFile(fileName, result, exportDirectory, charset);

        // archive all nodes content
        final Visitor archiveV = new NodeArchiveVisitor(locale, course, exportDirectory, charset);
        final TreeVisitor tv = new TreeVisitor(archiveV, course.getRunStructure().getRootNode(), true);
        tv.visitAll();
        // archive all course log files
        // OLATadmin gets all logfiles independent of the visibility configuration
        final boolean isOresOwner = (oresRights.length > 0) ? oresRights[0] : false;
        final boolean isOresInstitutionalManager = (oresRights.length > 1) ? oresRights[1] : false;

        final boolean aLogV = isOresOwner || isOresInstitutionalManager || isOLATAdmin;
        final boolean uLogV = isOLATAdmin;
        final boolean sLogV = isOresOwner || isOresInstitutionalManager || isOLATAdmin;

        // make an intermediate commit here to make sure long running course log export doesn't
        // cause db connection timeout to be triggered
        // @TODO transactions/backgroundjob:
        // rework when backgroundjob infrastructure exists
        DBFactory.getInstance(false).intermediateCommit();
        AsyncExportManager.getInstance().asyncArchiveCourseLogFiles(archiveOnBehalfOf, new Runnable() {
            @Override
            public void run() {
                // that's fine, I dont need to do anything here
            };
        }, course.getResourceableId(), exportDirectory.getPath(), null, null, aLogV, uLogV, sLogV, charset, null, null);

        CourseGroupManager courseGroupManager = (CourseGroupManager) CoreSpringFactory.getBean("persistingCourseGroupManager");
        courseGroupManager.archiveCourseGroups(exportDirectory, course);
    }

    /**
     * Returns the data export directory. If the directory does not yet exist the directory will be created
     * 
     * @param ureq
     *            The user request
     * @param courseName
     *            The course name or title. Will be used as directory name
     * @return The file representing the dat export directory
     */
    public static File getOrCreateDataExportDirectory(final Identity identity, final String courseName) {
        final File exportFolder = new File( // folder where exported user data should be
                // put
                FolderConfig.getCanonicalRoot() + FolderConfig.getUserHomes() + "/" + identity.getName() + "/private/archive/"
                        + Formatter.makeStringFilesystemSave(courseName));
        if (exportFolder.exists()) {
            if (!exportFolder.isDirectory()) {
                throw new OLATRuntimeException(ExportUtil.class, "File " + exportFolder.getAbsolutePath() + " already exists but it is not a folder!", null);
            }
        } else {
            exportFolder.mkdirs();
        }
        return exportFolder;
    }

    /**
     * Returns the data export directory.
     * 
     * @param ureq
     *            The user request
     * @param courseName
     *            The course name or title. Will be used as directory name
     * @return The file representing the dat export directory
     */
    public static File getDataExportDirectory(final Identity identity, final String courseName) {
        final File exportFolder = new File( // folder where exported user data should be
                // put
                FolderConfig.getCanonicalRoot() + FolderConfig.getUserHomes() + "/" + identity.getName() + "/private/archive/"
                        + Formatter.makeStringFilesystemSave(courseName));
        exportFolder.mkdirs();
        return exportFolder;
    }

    /**
     * Returns the personal folder of the given identity.
     * <p>
     * The idea of this method is to match the first part of what getOrCreateDataExportDirectory() returns.
     * <p>
     * 
     * @param identity
     * @return
     */
    public static File getPersonalDirectory(final Identity identity) {
        if (identity == null) {
            return null;
        }
        return new File(FolderConfig.getCanonicalRoot() + FolderConfig.getUserHomes() + "/" + identity.getName());
    }

    /**
     * Returns the data export directory. If the directory does not yet exist the directory will be created
     * 
     * @param ureq
     *            The user request
     * @param courseName
     *            The course name or title. Will be used as directory name
     * @return The file representing the dat export directory
     */
    public static File getOrCreateStatisticDirectory(final Identity identity, final String courseName) {
        final File exportFolder = new File( // folder where exported user data should be
                // put
                FolderConfig.getCanonicalRoot() + FolderConfig.getUserHomes() + "/" + identity.getName() + "/private/statistics/"
                        + Formatter.makeStringFilesystemSave(courseName));
        if (exportFolder.exists()) {
            if (!exportFolder.isDirectory()) {
                throw new OLATRuntimeException(ExportUtil.class, "File " + exportFolder.getAbsolutePath() + " already exists but it is not a folder!", null);
            }
        } else {
            exportFolder.mkdirs();
        }
        return exportFolder;
    }

    /**
     * Stores the editor tree model AND the run structure (both xml files). Called at publish.
     * 
     * @param resourceableId
     */
    public static void saveCourse(final Long resourceableId) {
        if (resourceableId == null) {
            throw new AssertException("No resourceable ID found.");
        }

        final PersistingCourseImpl theCourse = getCourseEditSession(resourceableId);
        if (theCourse != null) {
            // o_clusterOK by: ld (although the course is locked for editing, we still have to insure that load course is synchronized)
            CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(theCourse, new SyncerExecutor() {
                @Override
                public void execute() {
                    final PersistingCourseImpl course = getCourseEditSession(resourceableId);
                    if (course != null && course.isReadAndWrite()) {
                        course.initHasAssessableNodes();
                        course.saveRunStructure();
                        course.saveEditorTreeModel();

                        // clear modifyCourseEvents at publish, since the updateCourseInCache is called anyway
                        modifyCourseEvents.remove(resourceableId);
                        updateCourseInCache(resourceableId, course);
                    } else if (!course.isReadAndWrite()) {
                        throw new AssertException("Cannot saveCourse because theCourse is readOnly! You have to open an courseEditSession first!");
                    }
                }
            });
        } else {
            throw new AssertException("Cannot saveCourse because theCourse is null! Have you opened a courseEditSession yet?");
        }
    }

    /**
     * Stores ONLY the editor tree model (e.g. at course tree editing - add/remove/move course nodes).
     * 
     * @param resourceableId
     */
    public static void saveCourseEditorTreeModel(final Long resourceableId) {
        if (resourceableId == null) {
            throw new AssertException("No resourceable ID found.");
        }

        final PersistingCourseImpl course = getCourseEditSession(resourceableId);
        if (course != null && course.isReadAndWrite()) {
            synchronized (loadedCourses) { // o_clusterOK by: ld (clusterOK since the course is locked for editing)
                course.saveEditorTreeModel();

                modifyCourseEvents.putIfAbsent(resourceableId, new ModifyCourseEvent(resourceableId));
            }
        } else if (course == null) {
            throw new AssertException("Cannot saveCourseEditorTreeModel because course is null! Have you opened a courseEditSession yet?");
        } else if (!course.isReadAndWrite()) {
            throw new AssertException("Cannot saveCourse because theCourse is readOnly! You have to open an courseEditSession first!");
        }
    }

    /**
     * Updates the course cache forcing other cluster nodes to reload this course. <br/>
     * This is triggered after the course editor is closed. <br/>
     * It also removes the courseEditSession for this course.
     * 
     * @param resourceableId
     */
    public static void fireModifyCourseEvent(final Long resourceableId) {
        ModifyCourseEvent modifyCourseEvent = modifyCourseEvents.get(resourceableId);
        if (modifyCourseEvent != null) {
            synchronized (modifyCourseEvents) { // o_clusterOK by: ld
                modifyCourseEvent = modifyCourseEvents.remove(resourceableId);
                if (modifyCourseEvent != null) {
                    final PersistingCourseImpl course = getCourseEditSession(resourceableId);
                    if (course != null) {
                        updateCourseInCache(resourceableId, course);
                    }
                }
            }
        }
        // close courseEditSession if not already closed
        closeCourseEditSession(resourceableId, false);
    }

    /**
     * Create a custom css object for the course layout. This can then be set on a MainLayoutController to activate the course layout
     * 
     * @param usess
     *            The user session
     * @param courseEnvironment
     *            the course environment
     * @return The custom course css or NULL if no course css is available
     */
    public static CustomCSS getCustomCourseCss(final UserSession usess, final CourseEnvironment courseEnvironment) {
        CustomCSS customCSS = null;
        final CourseConfig courseConfig = courseEnvironment.getCourseConfig();
        if (courseConfig.hasCustomCourseCSS()) {
            // Notify the current tab that it should load a custom CSS
            final VFSContainer courseContainer = courseEnvironment.getCourseFolderContainer();
            customCSS = new CustomCSS(courseContainer, courseConfig.getCssLayoutRef(), usess);
        }
        return customCSS;
    }

    /**
     * Save courseConfig and update cache.
     * 
     * @param resourceableId
     * @param cc
     */
    public static void setCourseConfig(final Long resourceableId, final CourseConfig cc) {
        if (resourceableId == null) {
            throw new AssertException("No resourceable ID found.");
        }

        final PersistingCourseImpl theCourse = getCourseEditSession(resourceableId);
        if (theCourse != null) {
            // o_clusterOK by: ld (although the course is locked for editing, we still have to insure that load course is synchronized)
            CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(theCourse, new SyncerExecutor() {
                @Override
                public void execute() {
                    final PersistingCourseImpl course = getCourseEditSession(resourceableId);
                    if (course != null) {
                        course.setCourseConfig(cc);

                        updateCourseInCache(resourceableId, course);
                    }
                }
            });
        } else {
            throw new AssertException("Cannot setCourseConfig because theCourse is null! Have you opened a courseEditSession yet?");
        }
    }

    /**
     * Loads the course or gets it from cache, and adds it to the courseEditSessionMap. <br/>
     * It guarantees that the returned value is never null. <br/>
     * The courseEditSession object should live between acquire course lock and release course lock. TODO: remove course from courseEditSessionMap at close course editor
     * 
     * @param resourceableId
     * @return
     */
    public static PersistingCourseImpl openCourseEditSession(final Long resourceableId) {
        PersistingCourseImpl course = courseEditSessionMap.get(resourceableId);
        if (course != null) {
            // TODO LD: check this out! - here we might have a valid session if the course was just created/imported/copied
            // throw new AssertException("There is already an edit session open for this course: " + resourceableId);
            log.error("There is already an edit session open for this course: " + resourceableId);
        } else if (course == null) {
            course = (PersistingCourseImpl) loadCourse(resourceableId);
            course.setReadAndWrite(true);
            courseEditSessionMap.put(resourceableId, course);
            log.debug("getCourseEditSession - put course in courseEditSessionMap: " + resourceableId);
            // System.out.println("put course in courseEditSessionMap: " + resourceableId);
        }
        return course;
    }

    /**
     * Provides the currently edited course object with this id. <br/>
     * It guarantees that the returned value is never null if the openCourseEditSession was called first. <br/>
     * The CourseEditSession object should live between acquire course lock and release course lock. TODO: remove course from courseEditSessionMap at close course editor
     * 
     * @param resourceableId
     * @return
     */
    public static PersistingCourseImpl getCourseEditSession(final Long resourceableId) {
        // TODO: this must be called after the course lock is acquired, else must by synchronized (doInSync)
        final PersistingCourseImpl course = courseEditSessionMap.get(resourceableId);
        if (course == null) {
            throw new AssertException("No edit session open for this course: " + resourceableId + " - Open a session first!");
        }
        return course;
    }

    /**
     * TODO: remove course from courseEditSessionMap at releaseLock
     * 
     * @param resourceableId
     */
    public static void closeCourseEditSession(final Long resourceableId, final boolean checkIfAnyAvailable) {
        final PersistingCourseImpl course = courseEditSessionMap.get(resourceableId);
        if (course == null && checkIfAnyAvailable) {
            throw new AssertException("No edit session open for this course: " + resourceableId + " - There is nothing to be closed!");
        } else if (course != null) {
            course.setReadAndWrite(false);
            courseEditSessionMap.remove(resourceableId);
            log.debug("removeCourseEditSession for course: " + resourceableId);
            // System.out.println("removeCourseEditSession for course: " + resourceableId);
        }
    }

}

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

class NodeArchiveVisitor implements Visitor {
    private final File exportPath;
    private final Locale locale;
    private final ICourse course;
    private final String charset;

    /**
     * @param locale
     * @param course
     * @param exportPath
     * @param charset
     */
    public NodeArchiveVisitor(final Locale locale, final ICourse course, final File exportPath, final String charset) {
        this.locale = locale;
        this.exportPath = exportPath;
        // o_clusterOk by guido: save to hold reference to course inside editor
        this.course = course;
        this.charset = charset;
    }

    /**
	 */
    @Override
    public void visit(final INode node) {
        final CourseNode cn = (CourseNode) node;
        cn.archiveNodeData(locale, course, exportPath, charset);
    }
}

class NodeDeletionVisitor implements Visitor {

    private final ICourse course;

    /**
     * Constructor of the node deletion visitor
     * 
     * @param course
     */
    public NodeDeletionVisitor(final ICourse course) {
        this.course = course;
    }

    /**
     * Visitor pattern to delete the course nodes
     * 
     */
    @Override
    public void visit(final INode node) {
        final CourseNode cNode = (CourseNode) node;
        cNode.cleanupOnDelete(course);
    }

}

/**
 * Description:<br>
 * Event triggered if a course was edited - namely the course tree model have changed (e.g. nodes added, deleted)
 * <P>
 * Initial Date: 22.07.2008 <br>
 * 
 * @author Lavinia Dumitrescu
 */
class ModifyCourseEvent extends MultiUserEvent {

    private final Long courseId;

    /**
     * @param command
     */
    public ModifyCourseEvent(final Long resourceableId) {
        super("modify_course");
        courseId = resourceableId;
    }

    public Long getCourseId() {
        return courseId;
    }

}
