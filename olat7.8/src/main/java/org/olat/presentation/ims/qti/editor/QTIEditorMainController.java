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

package org.olat.presentation.ims.qti.editor;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.commons.xml.XStreamHelper;
import org.olat.data.qti.QTIResult;
import org.olat.data.reference.Reference;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.user.User;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.commons.fileresource.FileResource;
import org.olat.lms.commons.mail.ContactMessage;
import org.olat.lms.commons.tree.INode;
import org.olat.lms.commons.tree.TreePosition;
import org.olat.lms.commons.tree.TreeVisitor;
import org.olat.lms.commons.tree.Visitor;
import org.olat.lms.coordinate.LockingService;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.ims.qti.IQManager;
import org.olat.lms.ims.qti.IQPreviewSecurityCallback;
import org.olat.lms.ims.qti.QTIChangeLogMessage;
import org.olat.lms.ims.qti.editor.QTIEditHelperEBL;
import org.olat.lms.ims.qti.editor.QTIEditorPackageEBL;
import org.olat.lms.ims.qti.editor.localstrings.QtiEditorLocalStrings;
import org.olat.lms.ims.qti.objects.ChoiceQuestion;
import org.olat.lms.ims.qti.objects.Item;
import org.olat.lms.ims.qti.objects.QTIDocument;
import org.olat.lms.ims.qti.objects.QTIObject;
import org.olat.lms.ims.qti.objects.Question;
import org.olat.lms.ims.qti.objects.Response;
import org.olat.lms.ims.qti.objects.Section;
import org.olat.lms.ims.qti.parser.ItemParser;
import org.olat.lms.ims.qti.process.AssessmentInstance;
import org.olat.lms.ims.qti.process.QTIEditorResolver;
import org.olat.lms.qti.QTIResultService;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.user.UserService;
import org.olat.presentation.commons.memento.Memento;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPane;
import org.olat.presentation.framework.core.components.tree.MenuTree;
import org.olat.presentation.framework.core.components.tree.SelectionTree;
import org.olat.presentation.framework.core.components.tree.TreeEvent;
import org.olat.presentation.framework.core.components.tree.TreeNode;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.VetoableCloseController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.MainLayoutBasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.control.generic.dialog.DialogController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.presentation.framework.core.control.generic.tool.ToolController;
import org.olat.presentation.framework.core.control.generic.tool.ToolFactory;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.presentation.ims.qti.editor.localstrings.EssayItemLocalStringsFactory;
import org.olat.presentation.ims.qti.editor.localstrings.FibItemLocalStringsFactory;
import org.olat.presentation.ims.qti.editor.localstrings.KprimItemLocalStringsFactory;
import org.olat.presentation.ims.qti.editor.localstrings.McItemLocalStringsFactory;
import org.olat.presentation.ims.qti.editor.localstrings.QtiEditorLocalStringsAbstractFactory;
import org.olat.presentation.ims.qti.editor.localstrings.ScItemLocalStringsFactory;
import org.olat.presentation.ims.qti.editor.localstrings.SectionLocalStringsFactory;
import org.olat.presentation.ims.qti.editor.tree.AssessmentNode;
import org.olat.presentation.ims.qti.editor.tree.GenericQtiNode;
import org.olat.presentation.ims.qti.editor.tree.InsertItemTreeModel;
import org.olat.presentation.ims.qti.editor.tree.ItemNode;
import org.olat.presentation.ims.qti.editor.tree.QTIEditorTreeModel;
import org.olat.presentation.ims.qti.editor.tree.SectionNode;
import org.olat.presentation.ims.qti.run.IQDisplayController;
import org.olat.system.commons.CodeHelper;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.StringHelper;
import org.olat.system.coordinate.LockResult;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.mail.ContactList;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description: <br>
 * QTIEditorMainController is started from within the repository. A persistent lock is set to prevent more than one user working on the same document, even if the current
 * working author has no active session. If the document is already linked to a node in a course, it is opened for corrections only. This restricted editing function
 * prohibits structural changes which would interfere with already existing results.
 * <p>
 * Furthermore, if a document is loaded into the editor, it is not available for linking in a course. Therefore, a document in the editor can always be saved back safely
 * to the repository. But it must be locked that users starting the document from an already referencing building block wait until the edited document is committed
 * completly to the repository.
 * <P>
 * Initial Date: Oct 21, 2004 <br>
 * 
 * @author mike
 */
public class QTIEditorMainController extends MainLayoutBasicController implements VetoableCloseController {

    private static final String QTI_PKG_OWNERS = "qtiPkgOwners";

    private static final Logger log = LoggerHelper.getLogger();

    /*
     * Toolbox Commands
     */
    private static final String CMD_TOOLS_CLOSE_EDITOR = "cmd.close";
    private static final String CMD_TOOLS_PREVIEW = "cmd.preview";
    private static final String CMD_TOOLS_CHANGE_MOVE = "cmd.move";
    private static final String CMD_TOOLS_CHANGE_COPY = "cmd.copy";
    private static final String CMD_TOOLS_CHANGE_DELETE = "cmd.delete";
    private static final String CMD_TOOLS_ADD_PREFIX = "cmd.add.";
    private static final String CMD_TOOLS_ADD_FREETEXT = CMD_TOOLS_ADD_PREFIX + "essay";
    private static final String CMD_TOOLS_ADD_FIB = CMD_TOOLS_ADD_PREFIX + "fib";
    private static final String CMD_TOOLS_ADD_MULTIPLECHOICE = CMD_TOOLS_ADD_PREFIX + "mc";
    private static final String CMD_TOOLS_ADD_SINGLECHOICE = CMD_TOOLS_ADD_PREFIX + "sc";
    private static final String CMD_TOOLS_ADD_KPRIM = CMD_TOOLS_ADD_PREFIX + "kprim";
    private static final String CMD_TOOLS_ADD_SECTION = CMD_TOOLS_ADD_PREFIX + "section";

    private static final String CMD_EXIT_SAVE = "exit.save";
    private static final String CMD_EXIT_DISCARD = "exit.discard";
    private static final String CMD_EXIT_CANCEL = "exit.cancel";

    // REVIEW:2008-11-20: patrickb, scalability project issue -> read/write lock in distributed system
    //
    // Problem:
    // - Editor Session holds a copy to work, in case the work copy is "committed" e.g. saved - the qti file(s)
    // are copied and replaced -> this may lead to "uncommitted" reads of users starting the qti test, during the
    // very same moment the files are written.
    // - Because qti tests may hold media files and the like the copying and replacing can last surprisingly long.
    //
    // This means saving a test must be an exclusive operation. Reads for test sessions should be concurrent.
    //
    // History of solutions:
    // 1) An OLAT wide lock (object) was used -> possible congestion, delay if many qti tests are started or edited.
    // 2) Read/Write Lock used to grant non-congestion on reading, only OLAT wide write lock - still not optimal
    // 2a) Optimal solution in non-distributed system (singleVM) - ReadWriteLock on specific resource, instead OLAT wide.
    // 3) Scalability Project: how often does it happen compared to how often a test is started only?
    // => pragmatic solution to protect but not slow down starting of many concurrent readers.
    // An open and active editor session for a specific test -> possible writes!! => no reads
    // An open but not active editor session for a specific test -> no possible writes to expect. => allow reads
    // No open
    // ----|start editor session=>copy files for working copy | work on copy | close browser | restart work on copy | commit work|---
    // ----|~~~~~~~~~~~~~~~~~~~~~~~~~~~~active session ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~| Sessiontimeout |~~~~ active sesson ~~~~~~~~~~~~~~~~~|
    // qti file locked ,no more active qti file locked
    // session, qti file
    // free for read
    // Case 1: Starting editor session (acquiring lock) at the same time as starting some qti test session(s) (polling lock only)
    // => only a problem if user exits and saves as fast as possible and some big qti file. (unlikely)
    // Case 2: Active editor session (acquired lock) and trying to start test
    // => annoying for users in the course, not best service of the LMS for its authors, but pragmatic. (rare case)
    // Case 3: Closing editor session (releasing lock) while somebody tries to start the test (polling lock only)
    // => annoying for user, as he just missed it for some milliseconds to be able to start. (very rare case)
    // Case 4: No qti editor session or no active editor session
    // => benefit of fast starting qti tests, best service for the LMS clients (98.512% of cases)
    //
    // This leads to the solution as follows:
    // - (as it was already the case) A persistent lock for started qti sessions, used to prevent multiple authors "branching" test versions and overwriting changes of
    // the others.
    // - a non persistent GUI lock to signal an active editor session, this can be polled before starting a qti test.
    // - lock out qti readers in the case of an active editor session
    //
    // public static final ReentrantReadWriteLock IS_SAVING_RWL = new ReentrantReadWriteLock();

    private final QTIEditorPackageEBL qtiPackage;
    final QtiEditorLocalStringsAbstractFactory sectionLocalStringsFactory;
    final QtiEditorLocalStringsAbstractFactory scItemLocalStringsFactory;
    final QtiEditorLocalStringsAbstractFactory mcItemLocalStringsFactory;
    final QtiEditorLocalStringsAbstractFactory kprimItemLocalStringsFactory;
    final QtiEditorLocalStringsAbstractFactory fibItemLocalStringsFactory;
    final QtiEditorLocalStringsAbstractFactory essayItemLocalStringsFactory;

    private VelocityContainer main, exitVC, restrictedEditWarningVC;
    private ToolController mainToolC;
    private MenuTree menuTree;
    private Panel mainPanel, wrapperPanel;
    private LayoutMain3ColsController columnLayoutCtr;

    private QTIDocument qtiDoc;
    private QTIEditorTreeModel menuTreeModel;
    private DialogBoxController deleteDialog;
    private DialogBoxController deleteMediaDialog;
    private IQDisplayController previewController;
    private SelectionTree moveTree, copyTree, insertTree;
    private InsertItemTreeModel insertTreeModel;
    private GenericQtiNode insertObject;
    private final LockResult lockEntry;
    private Controller failedMonolog, lockMonolog;
    private boolean restrictedEdit;
    private Map history = null;
    private String startedWithTitle;
    private final List referencees;
    private DialogController proceedRestricedEditDialog;
    private ContactMessage changeEmail;
    private String changeLog = null;
    private CloseableModalController cmc, cmcPrieview, cmcExit;
    private Panel exitPanel;
    private boolean notEditable;
    private LockResult activeSessionLock;
    private Link notEditableButton;
    private Set<String> deletableMediaFiles;

    public QTIEditorMainController(final List referencees, final UserRequest ureq, final WindowControl wControl, final FileResource fileResource) {
        super(ureq, wControl);

        for (final Iterator iter = referencees.iterator(); iter.hasNext();) {
            final Reference ref = (Reference) iter.next();
            if ("CourseModule".equals(ref.getSource().getResourceableTypeName())) {
                final ICourse course = CourseFactory.loadCourse(ref.getSource().getResourceableId());
                final CourseNode courseNode = course.getEditorTreeModel().getCourseNode(ref.getUserdata());
                final String repositorySoftKey = (String) courseNode.getModuleConfiguration().get(ModuleConfiguration.CONFIG_KEY_REPOSITORY_SOFTKEY);
                final Long repKey = RepositoryServiceImpl.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftKey, true).getKey();
                final QTIResultService qtiResultService = (QTIResultService) CoreSpringFactory.getBean(QTIResultService.class);
                final List<QTIResult> results = qtiResultService.selectResults(course.getResourceableId(), courseNode.getIdent(), repKey, 1);
                this.restrictedEdit = ((getLockingService().isLocked(course, null)) || (results != null && results.size() > 0)) ? true : false;
            }
            if (restrictedEdit) {
                break;
            }
        }
        if (getLockingService().isLocked(fileResource, null)) {
            this.restrictedEdit = true;
        }
        this.referencees = referencees;

        qtiPackage = CoreSpringFactory.getBean(QTIEditorPackageEBL.class, new Object[] { ureq.getIdentity(), fileResource });

        sectionLocalStringsFactory = CoreSpringFactory.getBean(SectionLocalStringsFactory.class, getTranslator());
        scItemLocalStringsFactory = CoreSpringFactory.getBean(ScItemLocalStringsFactory.class, getTranslator());
        mcItemLocalStringsFactory = CoreSpringFactory.getBean(McItemLocalStringsFactory.class, getTranslator());
        kprimItemLocalStringsFactory = CoreSpringFactory.getBean(KprimItemLocalStringsFactory.class, getTranslator());
        fibItemLocalStringsFactory = CoreSpringFactory.getBean(FibItemLocalStringsFactory.class, getTranslator());
        essayItemLocalStringsFactory = CoreSpringFactory.getBean(EssayItemLocalStringsFactory.class, getTranslator());

        // try to get lock which lives longer then the browser session in case of a closing browser window
        lockEntry = getLockingService().aquirePersistentLock(qtiPackage.getRepresentingResourceable(), ureq.getIdentity(), null);
        if (lockEntry.isSuccess()) {
            // acquired a lock for the duration of the session only
            // fileResource has the RepositoryEntre.getOlatResource within, which is used in qtiPackage
            activeSessionLock = getLockingService().acquireLock(qtiPackage.getRepresentingResourceable(), ureq.getIdentity(), null);
            final Long resourceableId = fileResource.getResourceableId();
            //
            qtiDoc = qtiPackage.getQTIDocument();
            if (qtiDoc == null) {
                notEditable = true;
            } else if (qtiPackage.isResumed()) {
                showInfo("info.resumed");
            }
            //
            init(ureq); // initialize the gui
        } else {
            wControl.setWarning(getTranslator().translate("error.lock",
                    new String[] { lockEntry.getOwner().getName(), Formatter.formatDatetime(new Date(lockEntry.getLockAquiredTime())) }));
        }
    }

    private LockingService getLockingService() {
        return CoreSpringFactory.getBean(LockingService.class);
    }

    /**
     * This constructor may only be used for new or non-referenced QTI files!
     * 
     * @param ureq
     * @param wControl
     * @param fileResource
     */
    public QTIEditorMainController(final UserRequest ureq, final WindowControl wControl, final FileResource fileResource) {
        // super(wControl) is called in referenced constructor
        // null as value for the List referencees sets restrictedEdit := false;
        this(null, ureq, wControl, fileResource);
    }

    private void init(final UserRequest ureq) {
        main = createVelocityContainer("index");
        JSAndCSSComponent jsAndCss;
        // Add html header js
        jsAndCss = new JSAndCSSComponent("qitjsandcss", this.getClass(), new String[] { "qti.js" }, null, true);
        main.put("qitjsandcss", jsAndCss);

        //
        mainPanel = new Panel("p_qti_editor");
        mainPanel.setContent(main);
        //
        if (notEditable) {
            // test not editable
            final VelocityContainer notEditable = createVelocityContainer("notEditable");
            notEditableButton = LinkFactory.createButton("ok", notEditable, this);
            final Panel panel = new Panel("notEditable");
            panel.setContent(notEditable);
            columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), null, null, panel, null);
            wrapperPanel = putInitialPanel(columnLayoutCtr.getInitialComponent());
            return;
        }

        mainToolC = populateToolC(); // qtiPackage must be loaded previousely
        listenTo(mainToolC);

        // initialize the history
        if (qtiPackage.isResumed() && qtiPackage.hasSerializedChangelog()) {
            // there were already changes made -> reload!
            history = qtiPackage.loadChangelog();
        } else {
            // start with a fresh history. Editor is resumed but no changes were made
            // so far.
            history = new HashMap();
        }

        if (restrictedEdit) {
            mainToolC.setEnabled(CMD_TOOLS_ADD_SECTION, false);
            mainToolC.setEnabled(CMD_TOOLS_ADD_SINGLECHOICE, false);
            mainToolC.setEnabled(CMD_TOOLS_ADD_MULTIPLECHOICE, false);

            mainToolC.setEnabled(CMD_TOOLS_ADD_FIB, false);
            if (!qtiPackage.getQTIDocument().isSurvey()) {
                mainToolC.setEnabled(CMD_TOOLS_ADD_KPRIM, false);
            }
            if (qtiPackage.getQTIDocument().isSurvey()) {
                mainToolC.setEnabled(CMD_TOOLS_ADD_FREETEXT, false);
            }
        }
        mainToolC.setEnabled(CMD_TOOLS_CHANGE_DELETE, false);
        mainToolC.setEnabled(CMD_TOOLS_CHANGE_MOVE, false);
        mainToolC.setEnabled(CMD_TOOLS_CHANGE_COPY, false);

        // The menu tree model represents the structure of the qti document.
        // All insert/move operations on the model are propagated to the structure
        // by the node
        menuTreeModel = new QTIEditorTreeModel(qtiPackage);
        menuTree = new MenuTree("QTIDocumentTree");
        menuTree.setTreeModel(menuTreeModel);
        menuTree.setSelectedNodeId(menuTree.getTreeModel().getRootNode().getIdent());
        menuTree.addListener(this);// listen to the tree
        // remember the qtidoc title when we started this editor, to correctly name
        // the history report
        this.startedWithTitle = menuTree.getSelectedNode().getAltText();
        //
        main.put("tabbedPane", menuTreeModel.getQtiRootNode().createEditTabbedPane(ureq, getWindowControl(), getTranslator(), this));
        main.contextPut("qtititle", menuTreeModel.getQtiRootNode().getAltText());
        main.contextPut("isRestrictedEdit", restrictedEdit ? Boolean.TRUE : Boolean.FALSE);
        //
        columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), menuTree, mainToolC.getInitialComponent(), mainPanel, "qtieditor"
                + qtiPackage.getRepresentingResourceable());
        listenTo(columnLayoutCtr);
        // Add css background
        if (restrictedEdit) {
            columnLayoutCtr.addCssClassToMain("o_editor_qti_correct");
        } else {
            columnLayoutCtr.addCssClassToMain("o_editor_qti");
        }
        wrapperPanel = putInitialPanel(columnLayoutCtr.getInitialComponent());

        if (restrictedEdit) {
            restrictedEditWarningVC = createVelocityContainer("restrictedEditDialog");
            proceedRestricedEditDialog = new DialogController(ureq.getLocale(), translate("yes"), translate("no"), translate("qti.restricted.edit.warning")
                    + "<br/><br/>" + createReferenceesMsg(ureq), null, true, null);
            listenTo(proceedRestricedEditDialog);
            restrictedEditWarningVC.put("dialog", proceedRestricedEditDialog.getInitialComponent());
            // we would like to us a modal dialog here, but this does not work! we
            // can't push to stack because the outher workflows pushes us after the
            // controller to the stack. Thus, if we used a modal dialog here the
            // dialog would never show up.
            columnLayoutCtr.setCol3(restrictedEditWarningVC);
            columnLayoutCtr.hideCol1(true);
            columnLayoutCtr.hideCol2(true);
        }
    }

    /**
	 */
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        //
        if (source == menuTree) { // catch menu tree clicks
            if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
                GenericQtiNode clickedNode;
                clickedNode = menuTreeModel.getQtiNode(menuTree.getSelectedNodeId());
                final TabbedPane tabbedPane = clickedNode.createEditTabbedPane(ureq, getWindowControl(), getTranslator(), this);
                if (tabbedPane != null) {
                    main.put("tabbedPane", tabbedPane);
                } else {
                    final VelocityContainer itemNotEditable = createVelocityContainer("tab_itemAlien");
                    main.put("tabbedPane", itemNotEditable);
                    return;
                }

                // enable/disable delete and move
                // if (!restrictedEdit) {
                // only available in full edit mode
                if (clickedNode instanceof AssessmentNode) {
                    mainToolC.setEnabled(CMD_TOOLS_CHANGE_DELETE, false);
                    mainToolC.setEnabled(CMD_TOOLS_CHANGE_MOVE, false);
                    mainToolC.setEnabled(CMD_TOOLS_CHANGE_COPY, false);
                } else {
                    mainToolC.setEnabled(CMD_TOOLS_CHANGE_DELETE, true && !restrictedEdit);
                    mainToolC.setEnabled(CMD_TOOLS_CHANGE_MOVE, true && !restrictedEdit);
                    if (clickedNode instanceof ItemNode) {
                        mainToolC.setEnabled(CMD_TOOLS_CHANGE_COPY, true && !restrictedEdit);
                    } else {
                        mainToolC.setEnabled(CMD_TOOLS_CHANGE_COPY, false);
                    }
                }
                // }
            }
        } else if (source == moveTree) { // catch move operations
            cmc.deactivate();
            removeAsListenerAndDispose(cmc);
            cmc = null;

            final TreeEvent te = (TreeEvent) event;
            if (te.getCommand().equals(TreeEvent.COMMAND_TREENODE_CLICKED)) {
                // user chose a position to insert a new node
                final String nodeId = te.getNodeId();
                final TreePosition tp = insertTreeModel.getTreePosition(nodeId);
                final GenericQtiNode parentTargetNode = (GenericQtiNode) tp.getParentTreeNode();
                int targetPos = tp.getChildpos();
                final GenericQtiNode selectedNode = (GenericQtiNode) menuTree.getSelectedNode();
                final int selectedPos = selectedNode.getPosition();
                final GenericQtiNode parentSelectedNode = (GenericQtiNode) selectedNode.getParent();
                if (parentTargetNode == parentSelectedNode) {
                    // if we're on the same subnode
                    if (targetPos > selectedNode.getPosition()) {
                        // if we're moving after our current position
                        targetPos--;
                        // decrease insert pos since we're going to be removed from the
                        // parent before re-insert
                    }
                }
                // insert into menutree (insert on GenericNode do a remove from parent)
                parentTargetNode.insert(selectedNode, targetPos);
                // insert into model (remove from parent needed prior to insert)
                final QTIObject subject = parentSelectedNode.removeQTIObjectAt(selectedPos);
                parentTargetNode.insertQTIObjectAt(subject, targetPos);
                qtiPackage.serializeQTIDocument();
                menuTree.setDirty(true); // force rerendering for ajax mode
            }
        } else if (source == copyTree) { // catch copy operations
            cmc.deactivate();
            removeAsListenerAndDispose(cmc);
            cmc = null;

            final TreeEvent te = (TreeEvent) event;
            if (te.getCommand().equals(TreeEvent.COMMAND_TREENODE_CLICKED)) {
                // user chose a position to insert the node to be copied
                final String nodeId = te.getNodeId();
                final TreePosition tp = insertTreeModel.getTreePosition(nodeId);
                final int targetPos = tp.getChildpos();
                final ItemNode selectedNode = (ItemNode) menuTree.getSelectedNode();
                // only items are moveable
                // use XStream instead of ObjectCloner
                // Item qtiItem =
                // (Item)xstream.fromXML(xstream.toXML(selectedNode.getUnderlyingQTIObject()));
                final Item toClone = (Item) selectedNode.getUnderlyingQTIObject();
                final Item qtiItem = (Item) XStreamHelper.xstreamClone(toClone);
                // copy flow label class too, olat-2791
                final Question orgQuestion = toClone.getQuestion();
                if (orgQuestion instanceof ChoiceQuestion) {
                    final String flowLabelClass = ((ChoiceQuestion) orgQuestion).getFlowLabelClass();
                    final Question copyQuestion = qtiItem.getQuestion();
                    if (copyQuestion instanceof ChoiceQuestion) {
                        ((ChoiceQuestion) copyQuestion).setFlowLabelClass(flowLabelClass);
                    } else {
                        throw new AssertException("Could not copy flow-label-class, wrong type of copy question , must be 'ChoiceQuestion' but is " + copyQuestion);
                    }
                }
                String editorIdentPrefix = "";
                if (qtiItem.getIdent().startsWith(ItemParser.ITEM_PREFIX_SCQ)) {
                    editorIdentPrefix = ItemParser.ITEM_PREFIX_SCQ;
                } else if (qtiItem.getIdent().startsWith(ItemParser.ITEM_PREFIX_MCQ)) {
                    editorIdentPrefix = ItemParser.ITEM_PREFIX_MCQ;
                } else if (qtiItem.getIdent().startsWith(ItemParser.ITEM_PREFIX_KPRIM)) {
                    editorIdentPrefix = ItemParser.ITEM_PREFIX_KPRIM;
                } else if (qtiItem.getIdent().startsWith(ItemParser.ITEM_PREFIX_FIB)) {
                    editorIdentPrefix = ItemParser.ITEM_PREFIX_FIB;
                } else if (qtiItem.getIdent().startsWith(ItemParser.ITEM_PREFIX_ESSAY)) {
                    editorIdentPrefix = ItemParser.ITEM_PREFIX_ESSAY;
                }
                // set new ident... this is all it needs for our engine to recognise it
                // as a new item.
                qtiItem.setIdent(editorIdentPrefix + CodeHelper.getForeverUniqueID());
                // insert into menutree (insert on GenericNode do a remove from parent)
                final GenericQtiNode parentTargetNode = (GenericQtiNode) tp.getParentTreeNode();
                final GenericQtiNode newNode = new ItemNode(qtiItem, qtiPackage);
                parentTargetNode.insert(newNode, targetPos);
                // insert into model
                parentTargetNode.insertQTIObjectAt(qtiItem, targetPos);
                // activate copied node
                menuTree.setSelectedNodeId(newNode.getIdent());
                event(ureq, menuTree, new Event(MenuTree.COMMAND_TREENODE_CLICKED));
                qtiPackage.serializeQTIDocument();
            }
        } else if (source == insertTree) { // catch insert operations
            cmc.deactivate();
            removeAsListenerAndDispose(cmc);
            cmc = null;

            final TreeEvent te = (TreeEvent) event;
            if (te.getCommand().equals(TreeEvent.COMMAND_TREENODE_CLICKED)) { // insert
                // new
                // node
                final String nodeId = te.getNodeId();
                final TreePosition tp = insertTreeModel.getTreePosition(nodeId);
                final GenericQtiNode parentTargetNode = (GenericQtiNode) tp.getParentTreeNode();
                // insert into menu tree
                parentTargetNode.insert(insertObject, tp.getChildpos());
                // insert into model
                parentTargetNode.insertQTIObjectAt(insertObject.getUnderlyingQTIObject(), tp.getChildpos());
                // activate inserted node
                menuTree.setSelectedNodeId(insertObject.getIdent());
                event(ureq, menuTree, new Event(MenuTree.COMMAND_TREENODE_CLICKED));
                qtiPackage.serializeQTIDocument();
            }
        } else if (source == exitVC) {
            if (event.getCommand().equals(CMD_EXIT_SAVE)) {
                if (isRestrictedEdit() && history.size() > 0) {
                    changeLog = getChangeMessageAtEditTest(ureq);
                    final QTIChangeLogMessage clm = new QTIChangeLogMessage(changeLog, true);
                    qtiPackage.commitChangelog(clm);
                    final StringBuilder traceMsg = new StringBuilder(true ? "Visible for ALL \n" : "Visible for GROUP only \n");
                    log.info(traceMsg.append(changeLog).toString());

                    // ISSUE-7077: added this cleanup, but question why this has not been there also in previous version 7.6 and despite that there working correctly
                    cmcExit.deactivate();
                    removeAsListenerAndDispose(cmcExit);
                    cmcExit = null;

                    // save, remove locks and tmp files
                    saveAndExit(ureq);

                    return;
                } else {
                    // remove modal dialog and proceed with exit process
                    cmcExit.deactivate();
                    removeAsListenerAndDispose(cmcExit);
                    cmcExit = null;
                    // remove lock, clean tmp dir, fire done event to close editor
                    saveAndExit(ureq);
                }
            } else if (event.getCommand().equals(CMD_EXIT_DISCARD)) {
                // remove modal dialog and proceed with exit process
                cmcExit.deactivate();
                removeAsListenerAndDispose(cmcExit);
                cmcExit = null;
                // cleanup, so package does not get resumed
                qtiPackage.cleanupTmpPackageDir();
                // remove lock
                removeLocksAndExit(ureq);

            } else if (event.getCommand().equals(CMD_EXIT_CANCEL)) {
                // remove modal dialog and go back to edit mode
                cmcExit.deactivate();
                removeAsListenerAndDispose(cmcExit);
                cmcExit = null;
            }

        } else if (source == notEditableButton) {
            fireEvent(ureq, Event.DONE_EVENT); // close editor
        }
    }

    /**
     * This is what get logged if the test was changed after someone already used it.
     */
    private String getChangeMessageAtEditTest(final UserRequest ureq) {
        final String userN = ureq.getIdentity().getName();
        String changeMsg = "Changed by: " + getUserService().getFirstAndLastname(ureq.getIdentity().getUser()) + " [" + userN + "]\n";
        changeMsg += createChangeMessage();
        return changeMsg;
    }

    private void removeLocksAndExit(final UserRequest ureq) {
        // remove lock
        if (lockEntry.isSuccess()) {
            getLockingService().releaseLock(activeSessionLock);
            getLockingService().releasePersistentLock(lockEntry);
        }
        fireEvent(ureq, Event.DONE_EVENT); // close editor
    }

    private void saveAndExit(final UserRequest ureq) {
        boolean saveOk = false;
        //
        // acquire write lock
        // IS_SAVING_RWL.writeLock().lock();
        // synchronized(IS_SAVING){
        // try {
        try {
            saveOk = qtiPackage.savePackageToRepository();
        } catch (AssertException ex) {
            // this is thrown by consistency checks
            throw new AssertException("Consisteny problems while saving QTI element [" + qtiPackage.getRepresentingResourceable().getResourceableId() + "].", ex);
        }
        // } finally {
        // IS_SAVING_RWL.writeLock().unlock();
        // }
        // }// release write lock
        if (!saveOk) {
            getWindowControl().setError(translate("error.save"));
            return;
        }
        // cleanup, so package does not get resumed
        qtiPackage.cleanupTmpPackageDir();
        removeLocksAndExit(ureq);
    }

    /**
	 */
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == mainToolC) {
            final String cmd = event.getCommand();
            if (cmd.equals(CMD_TOOLS_CLOSE_EDITOR)) { // exitVC hook:
                // save package back to repository
                exitVC = createVelocityContainer("exitDialog");
                exitPanel = new Panel("exitPanel");
                exitPanel.setContent(exitVC);
                cmcExit = new CloseableModalController(getWindowControl(), translate("editor.preview.close"), exitPanel);
                cmcExit.activate();
                listenTo(cmcExit);
                return;

            } else if (cmd.equals(CMD_TOOLS_PREVIEW)) { // preview
                previewController = IQManager.getInstance().createIQDisplayController(new QTIEditorResolver(qtiPackage),
                        qtiPackage.getQTIDocument().isSurvey() ? AssessmentInstance.QMD_ENTRY_TYPE_SURVEY : AssessmentInstance.QMD_ENTRY_TYPE_SELF,
                        new IQPreviewSecurityCallback(), ureq, getWindowControl());
                if (previewController.isReady()) {
                    // in case previewController was unable to initialize, a message was
                    // set by displayController
                    // this is the case if no more attempts or security check was
                    // unsuccessfull
                    previewController.addControllerListener(this);
                    cmcPrieview = new CloseableModalController(getWindowControl(), translate("editor.preview.close"), previewController.getInitialComponent());
                    cmcPrieview.insertHeaderCss();
                    cmcPrieview.activate();
                    listenTo(cmcPrieview);

                } else {
                    getWindowControl().setWarning(translate("error.preview"));
                }
            } else if (cmd.equals(CMD_TOOLS_CHANGE_DELETE)) { // prepare delete

                final GenericQtiNode clickedNode = menuTreeModel.getQtiNode(menuTree.getSelectedNodeId());
                String msg = "";
                if (clickedNode instanceof SectionNode) {
                    if (QTIEditHelperEBL.countSections(qtiPackage.getQTIDocument().getAssessment()) == 1) {
                        // At least one section
                        getWindowControl().setError(translate("error.atleastonesection"));
                        return;
                    }
                    msg = translate("delete.section", StringHelper.escapeHtml(clickedNode.getTitle()));
                } else if (clickedNode instanceof ItemNode) {
                    if (((SectionNode) clickedNode.getParent()).getChildCount() == 1) {
                        // At least one item
                        getWindowControl().setError(translate("error.atleastoneitem"));
                        return;
                    }
                    msg = translate("delete.item", StringHelper.escapeHtml(clickedNode.getTitle()));
                }
                deleteDialog = activateYesNoDialog(ureq, null, msg, deleteDialog);
                deleteDialog.setUserObject(clickedNode);
                return;
            } else if (cmd.equals(CMD_TOOLS_CHANGE_MOVE)) {
                // cannot move the last item
                final GenericQtiNode clickedNode = menuTreeModel.getQtiNode(menuTree.getSelectedNodeId());
                if (clickedNode instanceof ItemNode && ((SectionNode) clickedNode.getParent()).getChildCount() == 1) {
                    getWindowControl().setError(translate("error.move.atleastoneitem"));
                    return;
                }
                final TreeNode selectedNode = menuTree.getSelectedNode();
                moveTree = new SelectionTree("moveTree", getTranslator());
                moveTree.setFormButtonKey("submit");
                insertTreeModel = new InsertItemTreeModel(menuTreeModel, (selectedNode instanceof SectionNode) ? InsertItemTreeModel.INSTANCE_ASSESSMENT
                        : InsertItemTreeModel.INSTANCE_SECTION);
                moveTree.setTreeModel(insertTreeModel);
                moveTree.addListener(this);
                cmc = new CloseableModalController(getWindowControl(), translate("close"), moveTree, true, translate("title.move"));
                cmc.activate();
                listenTo(cmc);

            } else if (cmd.equals(CMD_TOOLS_CHANGE_COPY)) {
                copyTree = new SelectionTree("copyTree", getTranslator());
                copyTree.setFormButtonKey("submit");
                insertTreeModel = new InsertItemTreeModel(menuTreeModel, InsertItemTreeModel.INSTANCE_SECTION);
                copyTree.setTreeModel(insertTreeModel);
                copyTree.addListener(this);
                cmc = new CloseableModalController(getWindowControl(), translate("close"), copyTree, true, translate("title.copy"));
                cmc.activate();
                listenTo(cmc);

            } else if (cmd.startsWith(CMD_TOOLS_ADD_PREFIX)) {
                if (cmd.equals(CMD_TOOLS_ADD_SECTION)) {
                    addSection();
                } else if (cmd.equals(CMD_TOOLS_ADD_SINGLECHOICE)) {
                    addSingleChoice();
                } else if (cmd.equals(CMD_TOOLS_ADD_MULTIPLECHOICE)) {
                    addMultipleChoice();
                } else if (cmd.equals(CMD_TOOLS_ADD_KPRIM)) {
                    addKprim();
                } else if (cmd.equals(CMD_TOOLS_ADD_FIB)) {
                    addFib();
                } else if (cmd.equals(CMD_TOOLS_ADD_FREETEXT)) {
                    addFreeText();
                }

                // prepare insert tree
                insertTree = new SelectionTree("insertTree", getTranslator());
                insertTree.setFormButtonKey("submit");
                if (cmd.equals(CMD_TOOLS_ADD_SECTION)) {
                    insertTreeModel = new InsertItemTreeModel(menuTreeModel, InsertItemTreeModel.INSTANCE_ASSESSMENT);
                } else {
                    insertTreeModel = new InsertItemTreeModel(menuTreeModel, InsertItemTreeModel.INSTANCE_SECTION);
                }
                insertTree.setTreeModel(insertTreeModel);
                insertTree.addListener(this);
                cmc = new CloseableModalController(getWindowControl(), translate("close"), insertTree, true, translate("title.add"));
                cmc.activate();
                listenTo(cmc);
            }
        } else if (source == deleteDialog) { // event from delete dialog
            if (DialogBoxUIFactory.isYesEvent(event)) { // yes, delete
                final GenericQtiNode clickedNode = (GenericQtiNode) deleteDialog.getUserObject();
                // check if any media to delete as well
                if (clickedNode.getUnderlyingQTIObject() instanceof Item) {
                    final Item selectedItem = (Item) clickedNode.getUnderlyingQTIObject();
                    deletableMediaFiles = QTIEditHelperEBL.getDeletableMedia(qtiPackage.getQTIDocument(), selectedItem);
                }

                // remove from underlying model
                ((GenericQtiNode) clickedNode.getParent()).removeQTIObjectAt(clickedNode.getPosition());

                // remove from tree model
                clickedNode.removeFromParent();
                qtiPackage.serializeQTIDocument();
                menuTree.setSelectedNodeId(clickedNode.getParent().getIdent());
                event(ureq, menuTree, new Event(MenuTree.COMMAND_TREENODE_CLICKED));
                // ask user to confirm referenced media removal
                if (deletableMediaFiles != null && deletableMediaFiles.size() > 0) {
                    final String msg = translate("delete.item.media", deletableMediaFiles.toString());
                    deleteMediaDialog = activateYesNoDialog(ureq, null, msg, deleteMediaDialog);
                }
            }
            // cleanup controller
            removeAsListenerAndDispose(deleteDialog);
            deleteDialog = null;

        } else if (source == deleteMediaDialog) { // event from deleteMediaDialog
            if (DialogBoxUIFactory.isYesEvent(event)) { // yes, delete
                qtiPackage.removeMediaFiles(deletableMediaFiles);
                deleteMediaDialog = null;
                deletableMediaFiles = null;
            }
        } else if (event instanceof NodeBeforeChangeEvent) {
            final NodeBeforeChangeEvent nce = (NodeBeforeChangeEvent) event;
            // active node changed some data
            final String activeQtiNodeId = menuTree.getSelectedNodeId();
            final GenericQtiNode activeQtiNode = menuTreeModel.getQtiNode(activeQtiNodeId);
            menuTree.setDirty(true); // force rerendering for ajax mode
            /*
             * mementos are only created in restricted mode
             */
            if (isRestrictedEdit()) {
                final String key = nce.getSectionIdent() + "/" + nce.getItemIdent() + "/" + nce.getQuestionIdent() + "/" + nce.getResponseIdent();
                if (!history.containsKey(key)) {
                    final Memento memento = activeQtiNode.createMemento();
                    history.put(key, memento);
                    qtiPackage.serializeChangelog(history);
                }
            }

            /*
             * generate a Memento, store it for further use
             */
            if (nce.hasNewTitle) {
                // update the treemodel to reflect the change of the underlying qti node
                activeQtiNode.setMenuTitleAndAlt(nce.getNewTitle());
                main.contextPut("qtititle", menuTreeModel.getQtiRootNode().getAltText());
            }
        } else if (source == proceedRestricedEditDialog) {
            // restricted edit warning
            if (event == DialogController.EVENT_FIRSTBUTTON) {
                // remove dialog and continue with real content
                columnLayoutCtr.setCol3(mainPanel);
                columnLayoutCtr.hideCol1(false);
                columnLayoutCtr.hideCol2(false);
                removeAsListenerAndDispose(proceedRestricedEditDialog);
                proceedRestricedEditDialog = null;
            } else {
                // remove lock as user is not interested in restricted edit
                // and quick editor
                removeLocksAndExit(ureq);
            }

        }
    }

    private void addFreeText() {
        QtiEditorLocalStrings qtiEditorLocalStrings = essayItemLocalStringsFactory.createLocalStrings();
        insertObject = new ItemNode(QTIEditHelperEBL.createEssayItem(qtiEditorLocalStrings), qtiPackage);
    }

    private void addFib() {
        QtiEditorLocalStrings qtiEditorLocalStrings = fibItemLocalStringsFactory.createLocalStrings();
        insertObject = new ItemNode(QTIEditHelperEBL.createFIBItem(qtiEditorLocalStrings), qtiPackage);
    }

    private void addKprim() {
        QtiEditorLocalStrings qtiEditorLocalStrings = kprimItemLocalStringsFactory.createLocalStrings();
        insertObject = new ItemNode(QTIEditHelperEBL.createKPRIMItem(qtiEditorLocalStrings), qtiPackage);
    }

    private void addMultipleChoice() {
        QtiEditorLocalStrings qtiEditorLocalStrings = mcItemLocalStringsFactory.createLocalStrings();
        insertObject = new ItemNode(QTIEditHelperEBL.createMCItem(qtiEditorLocalStrings), qtiPackage);
    }

    private void addSingleChoice() {
        QtiEditorLocalStrings qtiEditorLocalStrings = scItemLocalStringsFactory.createLocalStrings();
        insertObject = new ItemNode(QTIEditHelperEBL.createSCItem(qtiEditorLocalStrings), qtiPackage);
    }

    private void addSection() {
        QtiEditorLocalStrings qtiEditorLocalStrings = sectionLocalStringsFactory.createLocalStrings();
        final Section newSection = QTIEditHelperEBL.createSection(qtiEditorLocalStrings);
        qtiEditorLocalStrings = scItemLocalStringsFactory.createLocalStrings();
        final Item newItem = QTIEditHelperEBL.createSCItem(qtiEditorLocalStrings);
        newSection.getItems().add(newItem);
        final SectionNode scNode = new SectionNode(newSection, qtiPackage);
        final ItemNode itemNode = new ItemNode(newItem, qtiPackage);
        scNode.addChild(itemNode);
        insertObject = scNode;
    }

    /**
	 */
    protected void doDispose() {
        // controlers disposed by BasicController:
        // release activeSessionLock upon dispose
        if (activeSessionLock != null && activeSessionLock.isSuccess()) {
            getLockingService().releaseLock(activeSessionLock);
        }
    }

    private ToolController populateToolC() {
        final ToolController tc = ToolFactory.createToolController(getWindowControl());
        // tools
        tc.addHeader(translate("tools.tools.header"));
        tc.addLink(CMD_TOOLS_PREVIEW, translate("tools.tools.preview"), CMD_TOOLS_PREVIEW, "b_toolbox_preview");
        tc.addLink(CMD_TOOLS_CLOSE_EDITOR, translate("tools.tools.closeeditor"), null, "b_toolbox_close");
        // if (!restrictedEdit) {
        tc.addHeader(translate("tools.add.header"));
        // adds within the qti document level
        tc.addLink(CMD_TOOLS_ADD_SECTION, translate("tools.add.section"), CMD_TOOLS_ADD_SECTION, "o_mi_qtisection");
        // adds within a section
        tc.addLink(CMD_TOOLS_ADD_SINGLECHOICE, translate("tools.add.singlechoice"), CMD_TOOLS_ADD_SINGLECHOICE, "o_mi_qtisc");
        tc.addLink(CMD_TOOLS_ADD_MULTIPLECHOICE, translate("tools.add.multiplechoice"), CMD_TOOLS_ADD_MULTIPLECHOICE, "o_mi_qtimc");
        if (!qtiPackage.getQTIDocument().isSurvey()) {
            tc.addLink(CMD_TOOLS_ADD_KPRIM, translate("tools.add.kprim"), CMD_TOOLS_ADD_KPRIM, "o_mi_qtikprim");
        }
        tc.addLink(CMD_TOOLS_ADD_FIB, translate("tools.add.cloze"), CMD_TOOLS_ADD_FIB, "o_mi_qtifib");
        if (qtiPackage.getQTIDocument().isSurvey()) {
            tc.addLink(CMD_TOOLS_ADD_FREETEXT, translate("tools.add.freetext"), CMD_TOOLS_ADD_FREETEXT, "o_mi_qtiessay");
        }
        // change
        tc.addHeader(translate("tools.change.header"));
        // change actions
        tc.addLink(CMD_TOOLS_CHANGE_DELETE, translate("tools.change.delete"), CMD_TOOLS_CHANGE_DELETE, "b_toolbox_delete");
        tc.addLink(CMD_TOOLS_CHANGE_MOVE, translate("tools.change.move"), CMD_TOOLS_CHANGE_MOVE, "b_toolbox_move");
        tc.addLink(CMD_TOOLS_CHANGE_COPY, translate("tools.change.copy"), CMD_TOOLS_CHANGE_COPY, "b_toolbox_copy");
        // }

        return tc;
    }

    /**
	 */
    public boolean requestForClose() {
        // enter save/discard dialog if not already in it
        if (cmcExit == null) {
            exitVC = createVelocityContainer("exitDialog");
            exitPanel = new Panel("exitPanel");
            exitPanel.setContent(exitVC);
            cmcExit = new CloseableModalController(getWindowControl(), translate("editor.preview.close"), exitPanel);
            cmcExit.activate();
            listenTo(cmcExit);
        }
        return false;
    }

    /**
     * helper method to create the message about qti resource stakeholders and from where the qti resource is referenced.
     * 
     * @return
     */
    private String createReferenceesMsg(final UserRequest ureq) {
        /*
         * problems: A tries to reference this test, after test editor has been started
         */
        changeEmail = new ContactMessage(ureq.getIdentity());

        // the owners of this qtiPkg
        final RepositoryEntry myEntry = getRepositoryService().lookupRepositoryEntry(qtiPackage.getRepresentingResourceable(), false);

        // add qti resource owners as group
        addContactListToContactMessage(myEntry, QTI_PKG_OWNERS, changeEmail);

        final StringBuilder result = new StringBuilder();
        result.append(translate("qti.restricted.leading"));
        for (final Iterator iter = referencees.iterator(); iter.hasNext();) {
            final Reference element = (Reference) iter.next();
            // FIXME:discuss:possible performance/cache problem
            if ("CourseModule".equals(element.getSource().getResourceableTypeName())) {
                final ICourse course = CourseFactory.loadCourse(element.getSource().getResourceableId());
                // the course owners
                final RepositoryEntry entry = getRepositoryService().lookupRepositoryEntry(course, false);
                final String courseTitle = course.getCourseTitle();
                final List stakeHolderIdentities = addContactListToContactMessage(entry, courseTitle, changeEmail);

                final StringBuilder stakeHolders = new StringBuilder();
                User user = ((Identity) stakeHolderIdentities.get(0)).getUser();
                final Locale loc = ureq.getLocale();
                stakeHolders.append(getUserService().getFirstAndLastname(user));
                for (int i = 1; i < stakeHolderIdentities.size(); i++) {
                    user = ((Identity) stakeHolderIdentities.get(i)).getUser();
                    stakeHolders.append(", ").append(getUserService().getFirstAndLastname(user));
                }

                final CourseNode cn = course.getEditorTreeModel().getCourseNode(element.getUserdata());
                final String courseNodeTitle = cn.getShortTitle();
                result.append(translate("qti.restricted.course", courseTitle));
                result.append(translate("qti.restricted.node", courseNodeTitle));
                result.append(translate("qti.restricted.owners", stakeHolders.toString()));
            }
        }
        return result.toString();
    }

    private RepositoryService getRepositoryService() {
        return CoreSpringFactory.getBean(RepositoryServiceImpl.class);
    }

    private List<Identity> addContactListToContactMessage(final RepositoryEntry myEntry, final String title, final ContactMessage contactMessage) {
        final SecurityGroup ownersGroup = myEntry.getOwnerGroup();
        ContactList cl = new ContactList(title);
        List<Identity> stakeHolderIdentities = getBaseSecurity().getIdentitiesOfSecurityGroup(ownersGroup);
        cl.addAllIdentites(stakeHolderIdentities);
        contactMessage.addEmailTo(cl);
        return stakeHolderIdentities;
    }

    private BaseSecurity getBaseSecurity() {
        return CoreSpringFactory.getBean(BaseSecurity.class);
    }

    /**
     * helper method to create the change log message
     * 
     * @return
     */
    private String createChangeMessage() {

        // FIXME:pb:break down into smaller pieces
        final StringBuilder result = new StringBuilder();
        if (isRestrictedEdit()) {
            final Set keys = history.keySet();
            /*
			 * 
			 */
            final Visitor v = new Visitor() {
                /*
                 * a history key is built as follows sectionkey+"/"+itemkey+"/"+questionkey+"/"+responsekey
                 */
                String sectionKey = null;
                String itemkey = null;
                int pos = 0;
                Map itemMap = new HashMap();

                public void visit(final INode node) {
                    if (node instanceof AssessmentNode) {
                        final AssessmentNode an = (AssessmentNode) node;
                        final String key = "null/null/null/null";
                        if (history.containsKey(key)) {
                            // some assessment top level data changed
                            final Memento mem = (Memento) history.get(key);
                            result.append("---+ Changes in test " + formatVariable(startedWithTitle) + ":");
                            result.append(an.createChangeMessage(mem));
                        }
                    } else if (node instanceof SectionNode) {
                        final SectionNode sn = (SectionNode) node;
                        final String tmpKey = ((Section) sn.getUnderlyingQTIObject()).getIdent();
                        final String key = tmpKey + "/null/null/null";
                        if (history.containsKey(key)) {
                            // some section only data changed
                            final Memento mem = (Memento) history.get(key);
                            result.append("\n---++ Section " + formatVariable(sn.getAltText()) + " changes:");
                            result.append(sn.createChangeMessage(mem));
                        }
                    } else if (node instanceof ItemNode) {
                        final ItemNode in = (ItemNode) node;
                        final SectionNode sn = (SectionNode) in.getParent();
                        final String parentSectkey = ((Section) ((SectionNode) in.getParent()).getUnderlyingQTIObject()).getIdent();
                        final Item item = (Item) in.getUnderlyingQTIObject();
                        final Question question = item.getQuestion();
                        final String itemKey = item.getIdent();
                        String prefixKey = "null/" + itemKey;
                        final String questionIdent = question != null ? question.getQuestion().getId() : "null";
                        final String key = prefixKey + "/" + questionIdent + "/null";
                        final StringBuilder changeMessage = new StringBuilder();
                        boolean hasChanges = false;

                        if (!itemMap.containsKey(itemKey)) {
                            Memento questMem = null;
                            Memento respMem = null;
                            if (history.containsKey(key)) {
                                // question changed!
                                questMem = (Memento) history.get(key);
                                hasChanges = true;
                            }
                            // if(!hasChanges){
                            // check if a response changed
                            // new prefix for responses
                            prefixKey += "/null/";
                            // list contains org.olat.presentation.ims.qti.editor.beecom.objects.Response
                            final List responses = question != null ? question.getResponses() : null;
                            if (responses != null && responses.size() > 0) {
                                // check for changes in each response
                                for (final Iterator iter = responses.iterator(); iter.hasNext();) {
                                    final Response resp = (Response) iter.next();
                                    if (history.containsKey(prefixKey + resp.getIdent())) {
                                        // this response changed!
                                        final Memento tmpMem = (Memento) history.get(prefixKey + resp.getIdent());
                                        if (respMem != null) {
                                            respMem = respMem.getTimestamp() > tmpMem.getTimestamp() ? tmpMem : respMem;
                                        } else {
                                            hasChanges = true;
                                            respMem = tmpMem;
                                        }
                                    }
                                }
                            }
                            // }
                            // output message
                            if (hasChanges) {
                                Memento mem = null;
                                if (questMem != null && respMem != null) {
                                    // use the earlier memento
                                    mem = questMem.getTimestamp() > respMem.getTimestamp() ? respMem : questMem;
                                } else if (questMem != null) {
                                    mem = questMem;
                                } else if (respMem != null) {
                                    mem = respMem;
                                }
                                changeMessage.append(in.createChangeMessage(mem));
                                itemMap.put(itemKey, itemKey);
                                if (!parentSectkey.equals(sectionKey)) {
                                    // either this item belongs to a new section or no section
                                    // is active
                                    result.append("\n---++ Section " + formatVariable(sn.getAltText()) + " changes:");
                                    result.append("\n").append(changeMessage);
                                    sectionKey = parentSectkey;
                                } else {
                                    result.append("\n").append(changeMessage);
                                }
                            }

                        }
                    }
                }

                private String formatVariable(final String var) {
                    if (StringHelper.containsNonWhitespace(var)) {
                        return var;
                    }
                    return "[no entry]";
                }
            };
            final TreeVisitor tv = new TreeVisitor(v, menuTreeModel.getRootNode(), false);
            tv.visitAll();
        }
        /*
		 * 
		 */
        return result.toString();
    }

    /**
     * whether the editor runs in restricted mode or not.
     * 
     * @return
     */
    public boolean isRestrictedEdit() {
        return restrictedEdit;
    }

    public boolean isLockedSuccessfully() {
        return lockEntry.isSuccess();
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
