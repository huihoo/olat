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

package org.olat.presentation.course.repository;

/* TODO: ORID-1007 'File' */
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.vfs.LocalFileImpl;
import org.olat.data.commons.vfs.QuotaManager;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.filters.VFSItemFileTypeFilter;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.admin.quota.QuotaConstants;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.CourseModule;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.Structure;
import org.olat.lms.course.config.CourseConfig;
import org.olat.lms.course.config.CourseConfigManagerImpl;
import org.olat.lms.course.imports.CourseExportEBL;
import org.olat.lms.course.imports.CourseRepository_EBL;
import org.olat.lms.course.tree.CourseEditorTreeNode;
import org.olat.lms.glossary.GlossaryManager;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.lms.repository.RepositoryEntryImportExport;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.lms.sharedfolder.SharedFolderManager;
import org.olat.presentation.commons.filechooser.FileChooserController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.repository.IAddController;
import org.olat.presentation.repository.RepositoryAddCallback;
import org.olat.system.commons.Formatter;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: 13.05.2005
 * 
 * @author Mike Stock
 */
public class ImportCourseController extends BasicController implements IAddController {

    private OLATResource newCourseResource;
    private ICourse course;// o_clusterOK: creation process
    private File fCourseImportZIP;
    private final RepositoryAddCallback callback;

    private FileChooserController cfc;
    private Controller activeImportController;
    private ImportSharedfolderReferencesController sharedFolderImportController;
    private ImportGlossaryReferencesController glossaryImportController;
    private final List nodeList = new ArrayList();
    private int nodeListPos = 0;
    private final Panel myPanel;
    private static final VFSItemFileTypeFilter zipTypeFilter = new VFSItemFileTypeFilter(new String[] { "zip" });
    private CourseRepository_EBL courseRepositoryEbl;

    /**
     * Import a course from a previous export.
     * 
     * @param callback
     * @param ureq
     * @param wControl
     */
    public ImportCourseController(final RepositoryAddCallback callback, final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        this.callback = callback;
        courseRepositoryEbl = CoreSpringFactory.getBean(CourseRepository_EBL.class);
        myPanel = new Panel("importPanel");
        myPanel.addListener(this);

        // prepare generic filechoser for add file
        removeAsListenerAndDispose(cfc);
        QuotaManager.getInstance().getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_REPO).getUlLimitKB();
        cfc = new FileChooserController(ureq, getWindowControl(), (int) FolderConfig.getLimitULKB(), false);
        listenTo(cfc);

        cfc.setSuffixFilter(zipTypeFilter);
        myPanel.setContent(cfc.getInitialComponent());
        this.putInitialPanel(myPanel);
    }

    /**
	 */
    @Override
    public Component getTransactionComponent() {
        return getInitialComponent();
    }

    /**
	 */
    @Override
    public boolean transactionFinishBeforeCreate() {
        // create group management
        final CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
        cgm.createCourseGroupmanagement(course.getResourceableId().toString(), course);
        // import groups
        cgm.importCourseLearningGroups(getCourseExportEBL().getExportDataDir(course), course);
        cgm.importCourseRightGroups(getCourseExportEBL().getExportDataDir(course), course);
        return true;
    }

    private CourseExportEBL getCourseExportEBL() {
        return CoreSpringFactory.getBean(CourseExportEBL.class);
    }

    @Override
    public void repositoryEntryCreated(final RepositoryEntry re) {
        getBaseSecurityEBL().createCourseAdminPolicy(re);
        course = CourseFactory.getCourseEditSession(re.getOlatResource().getResourceableId());
        courseRepositoryEbl.setShortAndLongTitle(re.getDisplayname(), course);
        CourseEditorTreeNode editorRootNode = ((CourseEditorTreeNode) course.getEditorTreeModel().getRootNode());
        markCourseStructureAsNewWithoutRootNode(editorRootNode);
        courseRepositoryEbl.saveCourseAndCloseEditSession(course);
    }

    private BaseSecurityEBL getBaseSecurityEBL() {
        return CoreSpringFactory.getBean(BaseSecurityEBL.class);
    }

    /**
     * @param editorRootNode
     */
    private void markCourseStructureAsNewWithoutRootNode(final CourseEditorTreeNode editorRootNode) {
        markDirtyNewRecursively(editorRootNode);
        // root has already been created during export. Unmark it.
        editorRootNode.setNewnode(false);
    }

    /**
	 */
    @Override
    public void transactionAborted() {
        courseRepositoryEbl.cleanupExportDataDir(course);
        if (course != null) {
            CourseFactory.deleteCourse(newCourseResource);
            course = null;
        }
    }

    /**
     * Mark whole tree (incl. root node) "dirty" and "new" recursively.
     * 
     * @param editorRootNode
     */
    private void markDirtyNewRecursively(final CourseEditorTreeNode editorRootNode) {
        editorRootNode.setDirty(true);
        editorRootNode.setNewnode(true);
        if (editorRootNode.getChildCount() > 0) {
            for (int i = 0; i < editorRootNode.getChildCount(); i++) {
                markDirtyNewRecursively((CourseEditorTreeNode) editorRootNode.getChildAt(i));
            }
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // nothing to do
        /*
         * if (source == finishedMessage) { getWindowControl().pop(); // save the editor tree model, to persist any changes made during import.
         * course.saveEditorTreeModel(); callback.finished(ureq); }
         */
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == cfc) {
            if (event.equals(Event.DONE_EVENT)) {
                // create new repository entry
                if (cfc.isFileFromFolder()) {
                    final VFSLeaf vfsLeaf = cfc.getFileSelection();
                    if (!(vfsLeaf instanceof LocalFileImpl)) {
                        callback.failed(ureq);
                        return;
                    }
                    fCourseImportZIP = ((LocalFileImpl) vfsLeaf).getBasefile();
                } else {
                    fCourseImportZIP = cfc.getUploadedFile();
                }
                newCourseResource = OLATResourceManager.getInstance().createOLATResourceInstance(CourseModule.class);
                course = CourseFactory.importCourseFromZip(newCourseResource, fCourseImportZIP);
                // cfc.release();
                if (course == null) {
                    callback.failed(ureq);
                    return;
                }
                // create empty run structure
                course = CourseFactory.openCourseEditSession(course.getResourceableId());
                final Structure runStructure = course.getRunStructure();
                runStructure.getRootNode().removeAllChildren();

                CourseFactory.saveCourse(course.getResourceableId());
                // CourseFactory.closeCourseEditSession(course.getResourceableId());

            } else if (event.equals(Event.CANCELLED_EVENT)) {
                callback.canceled(ureq);
                return;
            }
            callback.setResourceable(newCourseResource);
            // Set title of root node. do not call course.getTitle() at this point.
            callback.setDisplayName(course.getEditorTreeModel().getRootNode().getTitle());
            String trimedFCourseImportZIP = Formatter.truncate(fCourseImportZIP.getName(), RepositoryEntry.MAX_RESOURCENAME_LENGTH);
            callback.setResourceName(trimedFCourseImportZIP);
            // collect all nodes
            collectNodesAsList((CourseEditorTreeNode) course.getEditorTreeModel().getRootNode(), nodeList);
            nodeListPos = 0;
            final boolean finished = processNodeList(ureq);
            if (finished) {
                // no node wanted to provide a controller to import its stuff. We're finished processing the nodes.
                // now process any shared folder reference...
                final CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();
                if (courseConfig.hasCustomSharedFolder()) {
                    processSharedFolder(ureq);
                } else if (courseConfig.hasGlossary()) {
                    processGlossary(ureq);
                } else {
                    // only when no sharedFolder and no glossary
                    // getWindowControl().pushAsModalDialog(translator.translate("import.suc.title"), finishedMessage);
                    // save the editor tree model, to persist any changes made during import.
                    CourseFactory.saveCourseEditorTreeModel(course.getResourceableId());
                    callback.finished(ureq);
                }
            }
        } else if (source == activeImportController) {
            if (event == Event.DONE_EVENT) {
                // continues to search through the list of nodes
                final boolean finished = processNodeList(ureq);
                if (finished) {
                    final CourseConfig courseConfig = CourseConfigManagerImpl.getInstance().loadConfigFor(course);
                    if (courseConfig.hasCustomSharedFolder()) {
                        processSharedFolder(ureq);
                    } else if (courseConfig.hasGlossary()) {
                        processGlossary(ureq);
                    } else {
                        // getWindowControl().pushAsModalDialog(translator.translate("import.suc.title"), finishedMessage);
                        // save the editor tree model, to persist any changes made during import.
                        CourseFactory.saveCourseEditorTreeModel(course.getResourceableId());
                        callback.finished(ureq);
                    }
                }
            } else if (event == Event.CANCELLED_EVENT) {
                callback.canceled(ureq);
                return;
            } else if (event == Event.FAILED_EVENT) {
                callback.canceled(ureq);
                showError("add.failed");
                return;
            }
        } else if (source == sharedFolderImportController) {
            if (event == Event.DONE_EVENT) {
                final CourseConfig courseConfig = CourseConfigManagerImpl.getInstance().loadConfigFor(course);
                if (courseConfig.hasGlossary()) {
                    processGlossary(ureq);
                } else {
                    // getWindowControl().pushAsModalDialog(translator.translate("import.suc.title"), finishedMessage);
                    // save the editor tree model, to persist any changes made during import.
                    CourseFactory.saveCourseEditorTreeModel(course.getResourceableId());
                    callback.finished(ureq);
                }
            } else if (event == Event.CANCELLED_EVENT) {
                callback.canceled(ureq);
                // FIXME: this does not remove all data from the database, see repositoryManger
                if (course != null) {
                    CourseFactory.deleteCourse(newCourseResource);
                }
                return;
            } else if (event == Event.FAILED_EVENT) {
                callback.canceled(ureq);
                showError("add.failed");
                return;
            }
        } else if (source == glossaryImportController) {
            if (event == Event.DONE_EVENT) {
                // getWindowControl().pushAsModalDialog(translator.translate("import.suc.title"), finishedMessage);
                // save the editor tree model, to persist any changes made during import.
                CourseFactory.saveCourseEditorTreeModel(course.getResourceableId());
                callback.finished(ureq);
            } else if (event == Event.CANCELLED_EVENT) {
                callback.canceled(ureq);
                // FIXME: this does not remove all data from the database, see repositoryManger
                if (course != null) {
                    CourseFactory.deleteCourse(newCourseResource);
                }
                return;
            } else if (event == Event.FAILED_EVENT) {
                callback.canceled(ureq);
                showError("add.failed");
                return;
            }
        }
    }

    private void processSharedFolder(final UserRequest ureq) {
        // if shared folder controller exists we did already import this one.
        if (sharedFolderImportController == null) {
            final RepositoryEntryImportExport sfImportExport = SharedFolderManager.getInstance().getRepositoryImportExport(getCourseExportEBL().getExportDataDir(course));

            removeAsListenerAndDispose(sharedFolderImportController);
            sharedFolderImportController = new ImportSharedfolderReferencesController(sfImportExport, course, ureq, getWindowControl());
            listenTo(sharedFolderImportController);

            myPanel.setContent(sharedFolderImportController.getInitialComponent());
        }
    }

    private void processGlossary(final UserRequest ureq) {
        // if glossary controller exists we did already import this one.
        if (glossaryImportController == null) {
            final RepositoryEntryImportExport sfImportExport = GlossaryManager.getInstance().getRepositoryImportExport(getCourseExportEBL().getExportDataDir(course));

            removeAsListenerAndDispose(glossaryImportController);
            glossaryImportController = new ImportGlossaryReferencesController(sfImportExport, course, ureq, getWindowControl());
            listenTo(glossaryImportController);

            myPanel.setContent(glossaryImportController.getInitialComponent());
        }
    }

    /**
     * Collect all nodes as list.
     * 
     * @param rootNode
     * @param nl
     */
    private void collectNodesAsList(final CourseEditorTreeNode rootNode, final List nl) {
        nl.add(rootNode);
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            collectNodesAsList((CourseEditorTreeNode) rootNode.getChildAt(i), nl);
        }
    }

    /**
     * Process the list of nodes to import. Call importNode on each node, starting at currentPos in the list of nodes. If a node provides a Controller, set the
     * activeImportController to the Controller returned by the importNode(), active this controller and return false. The calling method should then just exit its
     * event() method and yield control to the activeImportController. When the activeImportController is finished, it sends a Event.DONE_EVENT and this controller
     * continues to process the nodes in the list.
     * 
     * @param ureq
     * @return True if the whole list is processed, false otherwise.
     */
    private boolean processNodeList(final UserRequest ureq) {
        while (nodeListPos < nodeList.size()) {
            final CourseEditorTreeNode nextNode = (CourseEditorTreeNode) nodeList.get(nodeListPos);
            nodeListPos++;
            final Controller ctrl = nextNode.getCourseNode().importNode(getCourseExportEBL().getExportDataDir(course), course, false, ureq, getWindowControl());
            if (ctrl != null) {
                // this node needs a controller to do its import job.
                removeAsListenerAndDispose(activeImportController);
                activeImportController = ctrl;
                listenTo(activeImportController);

                myPanel.setContent(activeImportController.getInitialComponent());
                return false;
            }
        }
        return true;
    }

    @Override
    protected void doDispose() {
        if (course != null) {
            CourseFactory.closeCourseEditSession(course.getResourceableId(), false);
        }
    }

}
