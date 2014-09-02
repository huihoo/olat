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
import java.io.Serializable;

import org.apache.log4j.Logger;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.vfs.MergeSource;
import org.olat.data.commons.vfs.NamedContainerImpl;
import org.olat.data.commons.vfs.Quota;
import org.olat.data.commons.vfs.QuotaManager;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.olatimpl.OlatNamedContainerImpl;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.data.commons.xml.XStreamHelper;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.admin.quota.QuotaConstants;
import org.olat.lms.commons.tree.INode;
import org.olat.lms.commons.tree.TreeVisitor;
import org.olat.lms.commons.tree.Visitor;
import org.olat.lms.commons.vfs.securitycallbacks.FullAccessWithQuotaCallback;
import org.olat.lms.commons.vfs.securitycallbacks.ReadOnlyCallback;
import org.olat.lms.course.assessment.AssessmentHelper;
import org.olat.lms.course.config.CourseConfig;
import org.olat.lms.course.config.CourseConfigManager;
import org.olat.lms.course.config.CourseConfigManagerImpl;
import org.olat.lms.course.nodes.BCCourseNode;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.lms.course.run.environment.CourseEnvironmentImpl;
import org.olat.lms.course.tree.CourseEditorTreeModel;
import org.olat.lms.course.tree.CourseEditorTreeNode;
import org.olat.lms.glossary.GlossaryManager;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.lms.repository.RepositoryEntryImportExport;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.sharedfolder.SharedFolderManager;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.exception.AssertException;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

import com.thoughtworks.xstream.XStream;

/**
 * Description:<br>
 * Implementation of the course data structure. The course is defined using a runStructure and the editorTreeModel. Additional things are available through the
 * courseEnvironment (e.g. access to managers (Factory methods) or the course configuration)<br/>
 * It is allowed to save a course only if the course is in readAndWrite.
 * <P>
 * Initial Date: 12.08.2005 <br>
 * 
 * @author Felix Jost
 */
public class PersistingCourseImpl implements ICourse, Serializable {

    public static String COURSE_ROOT_DIR_NAME = "course";

    private static final String EDITORTREEMODEL_XML = "editortreemodel.xml";
    private static final String RUNSTRUCTURE_XML = "runstructure.xml";
    private static final String ORES_TYPE_NAME = CourseModule.getCourseTypeName();
    private static final String COURSEFOLDER = "coursefolder";

    private final Long resourceableId;
    private Structure runStructure;
    private boolean hasAssessableNodes = false;
    private CourseEditorTreeModel editorTreeModel;
    private CourseConfig courseConfig;
    private final CourseEnvironment courseEnvironment;
    private OlatRootFolderImpl courseRootContainer;
    private String courseTitle = null;
    /** courseTitleSyncObj is a final Object only used for synchronizing the courseTitle getter - see OLAT-5654 */
    private final Object courseTitleSyncObj = new Object();
    private static final Logger log = LoggerHelper.getLogger();

    // an PersistingCourseImpl instance could be readOnly if readAndWrite == false, or readAndWrite
    private boolean readAndWrite = false; // default readOnly

    public boolean isReadAndWrite() {
        return readAndWrite;
    }

    public void setReadAndWrite(final boolean readAndWrite) {
        this.readAndWrite = readAndWrite;
    }

    /**
     * Creates a new Course instance and creates the course filesystem if it does not already exist. Editor and run structures are not yet set. Use load() to initialize
     * the editor and run structure from persisted XML structure.
     * 
     * @param resourceableId
     */
    PersistingCourseImpl(final Long resourceableId) {
        this.resourceableId = resourceableId;
        // prepare filesystem and set course base path and course folder paths
        prepareFilesystem();
        courseConfig = CourseConfigManagerImpl.getInstance().loadConfigFor(this); // load or init defaults
        courseEnvironment = new CourseEnvironmentImpl(this);
    }

    /**
     * do upgrades if needed
     */
    private void checkForVersionUpdateAndUpdate() {
        final CourseUpgrade cu = new CourseUpgrade();
        cu.migrateCourse(this);
    }

    /**
	 */
    @Override
    public Structure getRunStructure() {
        return runStructure;
    }

    /**
	 */
    @Override
    public CourseEditorTreeModel getEditorTreeModel() {
        return editorTreeModel;
    }

    /**
	 */
    @Override
    public OlatRootFolderImpl getCourseBaseContainer() {
        return courseRootContainer;
    }

    /**
	 */
    @Override
    public VFSContainer getCourseFolderContainer() {
        return getCourseFolderContainer(true);
    }

    @Override
    public VFSContainer getCourseFolderContainerWithoutCourseElementFolders() {
        return getCourseFolderContainer(false);
    }

    private VFSContainer getCourseFolderContainer(boolean includeCourseElementFolders) {
        // add local course folder's children as read/write source and any sharedfolder as subfolder
        final MergeSource courseFolderContainer = new MergeSource(null, getCourseTitle());
        courseFolderContainer.addContainersChildren(getIsolatedCourseFolder(), true);

        // grab any shared folder that is configured
        OlatRootFolderImpl sharedFolder = null;
        final String sfSoftkey = getCourseConfig().getSharedFolderSoftkey();
        if (sfSoftkey != null) {
            final RepositoryService rm = RepositoryServiceImpl.getInstance();
            final RepositoryEntry re = rm.lookupRepositoryEntryBySoftkey(sfSoftkey, false);
            if (re != null) {
                sharedFolder = SharedFolderManager.getInstance().getSharedFolder(re.getOlatResource());
                if (sharedFolder != null) {
                    sharedFolder.setLocalSecurityCallback(new ReadOnlyCallback());
                    // add local course folder's children as read/write source and any sharedfolder as subfolder
                    courseFolderContainer.addContainer(new NamedContainerImpl("_sharedfolder", sharedFolder));
                }
            }
        }

        // add all course building blocks of type BC to a virtual folder
        if (includeCourseElementFolders) {
            final MergeSource BCNodesContainer = new MergeSource(null, "_courseelementdata");
            addFolderBuildingBlocks(BCNodesContainer, getRunStructure().getRootNode());
            if (BCNodesContainer.getItems().size() > 0) {
                courseFolderContainer.addContainer(BCNodesContainer);
            }
        }

        return courseFolderContainer;
    }

    /**
     * internal method to recursively add all course building blocks of type BC to a given VFS container. This should only be used for an author view, it does not test
     * for security.
     * 
     * @param BCNodesContainer
     * @param courseNode
     */
    private void addFolderBuildingBlocks(final MergeSource BCNodesContainer, final CourseNode courseNode) {
        for (int i = 0; i < courseNode.getChildCount(); i++) {
            final CourseNode child = (CourseNode) courseNode.getChildAt(i);
            if (child instanceof BCCourseNode) {
                final BCCourseNode bcNode = (BCCourseNode) child;
                // add folder not to merge source. Use name and node id to have unique name
                final String path = BCCourseNode.getFoldernodePathRelToFolderBase(getCourseEnvironment(), bcNode);
                final OlatRootFolderImpl rootFolder = new OlatRootFolderImpl(path, null);
                final String folderName = bcNode.getShortTitle() + " (" + bcNode.getIdent() + ")";
                final OlatNamedContainerImpl BCFolder = new OlatNamedContainerImpl(folderName, rootFolder);
                BCNodesContainer.addContainer(BCFolder);
            }
            // recursion for all childrenØ
            addFolderBuildingBlocks(BCNodesContainer, child);
        }
    }

    /**
	 */
    @Override
    public CourseEnvironment getCourseEnvironment() {
        return courseEnvironment;
    }

    /**
	 */
    @Override
    public String getCourseTitle() {
        synchronized (courseTitleSyncObj) { // o_clusterOK by:ld/se
            if (courseTitle == null) {
                // load repository entry for this course and get title from it
                final RepositoryEntry re = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(
                        OresHelper.createOLATResourceableInstance(CourseModule.class, this.resourceableId), false);
                if (re == null) {
                    throw new AssertException("trying to get repoentry of a course to get the title, but no repoentry found although course is there, course resid = "
                            + resourceableId);
                }
                courseTitle = re.getDisplayname();
            }
        }
        return courseTitle;
    }

    /**
     * Prepares the filesystem for this course.
     */
    private void prepareFilesystem() {
        // generate course base path
        final String relPath = File.separator + COURSE_ROOT_DIR_NAME + File.separator + getResourceableId().longValue();
        courseRootContainer = new OlatRootFolderImpl(relPath, null);
        final File fBasePath = courseRootContainer.getBasefile();
        if (!fBasePath.exists() && !fBasePath.mkdirs()) {
            throw new OLATRuntimeException(this.getClass(), "Could not create course base path:" + courseRootContainer, null);
        }
    }

    protected OlatRootFolderImpl getIsolatedCourseFolder() {
        // create local course folder
        final OlatRootFolderImpl isolatedCourseFolder = new OlatRootFolderImpl(courseRootContainer.getRelPath() + File.separator + COURSEFOLDER, null);
        // generate course folder
        final File fCourseFolder = isolatedCourseFolder.getBasefile();
        if (!fCourseFolder.exists() && !fCourseFolder.mkdirs()) {
            throw new OLATRuntimeException(this.getClass(), "could not create course's coursefolder path:" + fCourseFolder.getAbsolutePath(), null);
        }

        final QuotaManager qm = QuotaManager.getInstance();
        Quota q = qm.getCustomQuota(isolatedCourseFolder.getRelPath());
        if (q == null) {
            final Quota defQuota = qm.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_COURSE);
            q = QuotaManager.getInstance().createQuota(isolatedCourseFolder.getRelPath(), defQuota.getQuotaKB(), defQuota.getUlLimitKB());
        }
        final FullAccessWithQuotaCallback secCallback = new FullAccessWithQuotaCallback(q);
        isolatedCourseFolder.setLocalSecurityCallback(secCallback);
        return isolatedCourseFolder;
    }

    /**
     * Save the run structure to disk, persist to the xml file
     */
    void saveRunStructure() {
        writeObject(RUNSTRUCTURE_XML, getRunStructure());
        log.debug("saveRunStructure");
    }

    /**
     * Save the editor tree model to disk, persist to the xml file
     */
    void saveEditorTreeModel() {
        writeObject(EDITORTREEMODEL_XML, getEditorTreeModel());
        log.debug("saveEditorTreeModel");
    }

    /**
     * See OLAT-5368: Course Export can take longer than say 2min.
     * <p>
     */
    @Override
    public void exportToFilesystem(final File exportDirectory) {
        final long s = System.currentTimeMillis();
        log.info("exportToFilesystem: exporting course " + this + " to " + exportDirectory + "...");
        final File fCourseBase = getCourseBaseContainer().getBasefile();

        FileUtils.copyFileToDir(new File(fCourseBase, CourseConfigManager.COURSECONFIG_XML), exportDirectory, "course export courseconfig");
        // export editor structure
        FileUtils.copyFileToDir(new File(fCourseBase, EDITORTREEMODEL_XML), exportDirectory, "course export exitortreemodel");
        // export run structure
        FileUtils.copyFileToDir(new File(fCourseBase, RUNSTRUCTURE_XML), exportDirectory, "course export runstructure");
        // export course folder
        FileUtils.copyDirToDir(getIsolatedCourseFolder().getBasefile(), exportDirectory, "course export folder");
        // export any node data
        final File fExportedDataDir = new File(exportDirectory, EXPORTED_DATA_FOLDERNAME);
        fExportedDataDir.mkdirs();
        log.info("exportToFilesystem: exporting course " + this + ": exporting all nodes...");
        final Visitor visitor = new NodeExportVisitor(fExportedDataDir, this);
        final TreeVisitor tv = new TreeVisitor(visitor, getEditorTreeModel().getRootNode(), true);
        tv.visitAll();
        log.info("exportToFilesystem: exporting course " + this + ": exporting all nodes...done.");

        // OLAT-5368: do intermediate commit to avoid transaction timeout
        // discussion intermediatecommit vs increased transaction timeout:
        // pro intermediatecommit: not much
        // pro increased transaction timeout: would fix OLAT-5368 but only move the problem
        // @TODO OLAT-2597: real solution is a long-running background-task concept...
        DBFactory.getInstance().intermediateCommit();

        // export shared folder
        final CourseConfig config = getCourseConfig();
        if (config.hasCustomSharedFolder()) {
            log.info("exportToFilesystem: exporting course " + this + ": shared folder...");
            if (!SharedFolderManager.getInstance().exportSharedFolder(config.getSharedFolderSoftkey(), fExportedDataDir)) {
                // export failed, delete reference to shared folder in the course config
                log.info("exportToFilesystem: exporting course " + this + ": export of shared folder failed.");
                config.setSharedFolderSoftkey(CourseConfig.VALUE_EMPTY_SHAREDFOLDER_SOFTKEY);
                CourseConfigManagerImpl.getInstance().saveConfigTo(this, config);
            }
            log.info("exportToFilesystem: exporting course " + this + ": shared folder...done.");
        }

        // OLAT-5368: do intermediate commit to avoid transaction timeout
        // discussion intermediatecommit vs increased transaction timeout:
        // pro intermediatecommit: not much
        // pro increased transaction timeout: would fix OLAT-5368 but only move the problem
        // @TODO OLAT-2597: real solution is a long-running background-task concept...
        DBFactory.getInstance().intermediateCommit();

        // export glossary
        if (config.hasGlossary()) {
            log.info("exportToFilesystem: exporting course " + this + ": glossary...");
            if (!GlossaryManager.getInstance().exportGlossary(config.getGlossarySoftKey(), fExportedDataDir)) {
                // export failed, delete reference to glossary in the course config
                log.info("exportToFilesystem: exporting course " + this + ": export of glossary failed.");
                config.setGlossarySoftKey(null);
                CourseConfigManagerImpl.getInstance().saveConfigTo(this, config);
            }
            log.info("exportToFilesystem: exporting course " + this + ": glossary...done.");
        }

        // OLAT-5368: do intermediate commit to avoid transaction timeout
        // discussion intermediatecommit vs increased transaction timeout:
        // pro intermediatecommit: not much
        // pro increased transaction timeout: would fix OLAT-5368 but only move the problem
        // @TODO OLAT-2597: real solution is a long-running background-task concept...
        DBFactory.getInstance().intermediateCommit();

        log.info("exportToFilesystem: exporting course " + this + ": configuration and repo data...");
        // export configuration file
        FileUtils.copyFileToDir(new File(fCourseBase, CourseConfigManager.COURSECONFIG_XML), exportDirectory, "course export configuration and repo info");
        // export learning groups
        CourseGroupManager courseGroupManager = (CourseGroupManager) CoreSpringFactory.getBean("persistingCourseGroupManager");
        courseGroupManager.exportCourseLeaningGroups(fExportedDataDir, this);
        // export right groups
        courseGroupManager.exportCourseRightGroups(fExportedDataDir, this);
        // export repo metadata
        final RepositoryService rm = RepositoryServiceImpl.getInstance();
        final RepositoryEntry myRE = rm.lookupRepositoryEntry(this, true);
        final RepositoryEntryImportExport importExport = new RepositoryEntryImportExport(myRE, fExportedDataDir);
        importExport.exportDoExportProperties();

        // OLAT-5368: do intermediate commit to avoid transaction timeout
        // discussion intermediatecommit vs increased transaction timeout:
        // pro intermediatecommit: not much
        // pro increased transaction timeout: would fix OLAT-5368 but only move the problem
        // @TODO OLAT-2597: real solution is a long-running background-task concept...
        DBFactory.getInstance().intermediateCommit();

        log.info("exportToFilesystem: exporting course " + this + " to " + exportDirectory + " done.");
        log.info("finished export course '" + getCourseTitle() + "' in t=" + Long.toString(System.currentTimeMillis() - s));
    }

    /**
     * Load the course from disk/database, load the run structure from xml file etc.
     */
    void load() {
        /*
         * remember that loading of the courseConfiguration is already done within the constructor !
         */
        Object obj;
        obj = readObject(RUNSTRUCTURE_XML);
        if (!(obj instanceof Structure)) {
            throw new AssertException("Error reading course run structure.");
        }
        runStructure = (Structure) obj;
        initHasAssessableNodes();

        obj = readObject(EDITORTREEMODEL_XML);
        if (!(obj instanceof CourseEditorTreeModel)) {
            throw new AssertException("Error reading course editor tree model.");
        }
        editorTreeModel = (CourseEditorTreeModel) obj;
        checkForVersionUpdateAndUpdate();
    }

    /**
     * Write a structure to an XML file in the course base path folder.
     * 
     * @param fileName
     * @param obj
     */
    private void writeObject(final String fileName, final Object obj) {
        VFSItem vfsItem = getCourseBaseContainer().resolve(fileName);
        if (vfsItem == null) {
            vfsItem = getCourseBaseContainer().createChildLeaf(fileName);
        }
        final XStream xStream = CourseXStreamAliases.getCourseXStream();
        XStreamHelper.writeObject(xStream, (VFSLeaf) vfsItem, obj);
    }

    /**
     * Read a structure from XML file within the course base path folder.
     * 
     * @param fileName
     * @return de-serialized object
     * @throws OLATRuntimeException
     *             if de-serialization fails.
     */
    private Object readObject(final String fileName) {
        final VFSItem vfsItem = getCourseBaseContainer().resolve(fileName);
        if (vfsItem == null || !(vfsItem instanceof VFSLeaf)) {
            throw new AssertException("Cannot resolve file: " + fileName + " course=" + this.toString());
        }

        final XStream xStream = CourseXStreamAliases.getCourseXStream();
        return XStreamHelper.readObject(xStream, ((VFSLeaf) vfsItem).getInputStream());
    }

    /**
	 */
    @Override
    public String getResourceableTypeName() {
        return ORES_TYPE_NAME;
    }

    /**
	 */
    @Override
    public Long getResourceableId() {
        return resourceableId;
    }

    /**
     * Package private. Only used by CourseFactory.
     * 
     * @param editorTreeModel
     */
    void setEditorTreeModel(final CourseEditorTreeModel editorTreeModel) {
        this.editorTreeModel = editorTreeModel;
    }

    /**
     * Package private. Only used by CourseFactory.
     * 
     * @param runStructure
     */
    void setRunStructure(final Structure runStructure) {
        this.runStructure = runStructure;
        initHasAssessableNodes();
    }

    /**
     * This should only be called via the CourseFactory, since it has to update the course cache.
     * <p>
     * Sets the course configuration.
     * 
     * @param courseConfig
     */
    protected void setCourseConfig(final CourseConfig courseConfig) {
        this.courseConfig = courseConfig;
        CourseConfigManagerImpl.getInstance().saveConfigTo(this, courseConfig);
    }

    /**
     * @return
     */
    public CourseConfig getCourseConfig() {
        return courseConfig;
    }

    /**
     * Sets information about there are assessable nodes or structure course nodes (subtype of assessable node), which 'hasPassedConfigured' or 'hasScoreConfigured' is
     * true in the structure.
     */
    void initHasAssessableNodes() {
        this.hasAssessableNodes = AssessmentHelper.checkForAssessableNodes(runStructure.getRootNode());
    }

    /**
	 */
    @Override
    public boolean hasAssessableNodes() {
        return hasAssessableNodes;
    }

    /**
	 */
    @Override
    public String toString() {
        return "Course:[" + getResourceableId() + "," + courseTitle + "], " + super.toString();
    }

}

class NodeExportVisitor implements Visitor {

    private final File exportDirectory;
    private final ICourse course;

    /**
     * Constructor of the node deletion visitor
     * 
     * @param exportDirectory
     * @param course
     */
    public NodeExportVisitor(final File exportDirectory, final ICourse course) {
        this.exportDirectory = exportDirectory;
        this.course = course;
    }

    /**
     * Visitor pattern to delete the course nodes
     * 
     */
    @Override
    public void visit(final INode node) {
        final CourseEditorTreeNode cNode = (CourseEditorTreeNode) node;
        cNode.getCourseNode().exportNode(exportDirectory, course);
        // OLAT-5368: do frequent intermediate commits to avoid transaction timeout
        // discussion intermediatecommit vs increased transaction timeout:
        // pro intermediatecommit: not much
        // pro increased transaction timeout: would fix OLAT-5368 but only move the problem
        // @TODO OLAT-2597: real solution is a long-running background-task concept...
        DBFactory.getInstance().intermediateCommit();
    }

}
