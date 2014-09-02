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

package org.olat.presentation.course.nodes.wiki;

import org.olat.data.basesecurity.Identity;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.commons.fileresource.WikiResource;
import org.olat.lms.core.notification.service.SubscriptionContext;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.AssessmentHelper;
import org.olat.lms.course.condition.Condition;
import org.olat.lms.course.nodes.WikiCourseNode;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.course.tree.CourseEditorTreeModel;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.repository.handlers.RepositoryHandler;
import org.olat.lms.repository.handlers.RepositoryHandlerFactory;
import org.olat.lms.wiki.WikiManager;
import org.olat.lms.wiki.WikiSecurityCallback;
import org.olat.lms.wiki.WikiSecurityCallbackImpl;
import org.olat.presentation.course.condition.ConditionEditController;
import org.olat.presentation.course.editor.NodeEditController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.Windows;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPane;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.ControllerEventListener;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.control.generic.dtabs.DTabs;
import org.olat.presentation.framework.core.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.presentation.repository.DynamicTabHelper;
import org.olat.presentation.repository.ReferencableEntriesSearchController;
import org.olat.presentation.repository.RepositoryDetailsController;
import org.olat.presentation.wiki.WikiUIFactory;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.event.Event;

/**
 * Description: <BR/>
 * Edit controller for single page course nodes
 * <P/>
 * Initial Date: Oct 12, 2004
 * 
 * @author Felix Jost
 */
public class WikiEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {
    public static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";
    public static final String PANE_TAB_WIKICONFIG = "pane.tab.wikiconfig";
    public static final String PANE_TAB_WIKIDISPLAYCONFIG = "pane.tab.wikidisplayconfig";

    private static final String[] paneKeys = { PANE_TAB_WIKICONFIG, PANE_TAB_ACCESSIBILITY };

    private static final String CHOSEN_ENTRY = "chosen_entry";

    private final WikiCourseNode wikiCourseNode;
    private final ConditionEditController accessCondContr;
    private TabbedPane tabs;
    private final Panel main;
    private final VelocityContainer content;
    private ReferencableEntriesSearchController searchController;
    private Controller wikiCtr;
    private CloseableModalController cmcWikiCtr;
    private CloseableModalController cmcSearchController;
    private Link previewLink;
    private final Link chooseButton;
    private final Link changeButton;
    private Link editLink;
    private final VelocityContainer editAccessVc;
    private final ConditionEditController editCondContr;
    private final ICourse course;

    /**
     * Constructor for wiki page editor controller
     * 
     * @param config
     *            The node module configuration
     * @param ureq
     *            The user request
     * @param wikiCourseNode
     *            The current wiki page course node
     * @param course
     */
    public WikiEditController(final ModuleConfiguration config, final UserRequest ureq, final WindowControl wControl, final WikiCourseNode wikiCourseNode,
            final ICourse course, final UserCourseEnvironment euce) {
        super(ureq, wControl);
        this.wikiCourseNode = wikiCourseNode;
        // o_clusterOk by guido: save to hold reference to course inside editor
        this.course = course;

        main = new Panel("wikimain");

        content = this.createVelocityContainer("edit");
        /* previewButton = LinkFactory.createButtonSmall("command.preview", content, this); */
        chooseButton = LinkFactory.createButtonSmall("command.create", content, this);
        changeButton = LinkFactory.createButtonSmall("command.change", content, this);

        editAccessVc = this.createVelocityContainer("edit_access");
        final CourseGroupManager groupMgr = course.getCourseEnvironment().getCourseGroupManager();
        final CourseEditorTreeModel editorModel = course.getEditorTreeModel();
        // Accessibility precondition
        final Condition accessCondition = wikiCourseNode.getPreConditionAccess();
        accessCondContr = new ConditionEditController(ureq, getWindowControl(), groupMgr, accessCondition, "accessConditionForm", AssessmentHelper.getAssessableNodes(
                editorModel, wikiCourseNode), euce);
        this.listenTo(accessCondContr);
        editAccessVc.put("readerCondition", accessCondContr.getInitialComponent());

        // wiki read / write preconditions
        final Condition editCondition = wikiCourseNode.getPreConditionEdit();
        editCondContr = new ConditionEditController(ureq, getWindowControl(), groupMgr, editCondition, "editConditionForm", AssessmentHelper.getAssessableNodes(
                editorModel, wikiCourseNode), euce);
        this.listenTo(editCondContr);
        editAccessVc.put("editCondition", editCondContr.getInitialComponent());

        if (config.get(ModuleConfiguration.CONFIG_KEY_REPOSITORY_REF) != null) {
            // fetch repository entry to display the repository entry title of the
            // chosen wiki
            final RepositoryEntry re = wikiCourseNode.getReferencedRepositoryEntry(false);
            if (re == null) { // we cannot display the entrie's name, because the
                // repository entry had been deleted between the time
                // when it was chosen here, and now
                this.showError("error.repoentrymissing");
                content.contextPut("showPreviewLink", Boolean.FALSE);
                content.contextPut(CHOSEN_ENTRY, translate("no.entry.chosen"));
            } else {
                // no securitycheck on wiki, editable by everybody
                editLink = LinkFactory.createButtonSmall("edit", content, this);
                content.contextPut("showPreviewLink", Boolean.TRUE);
                previewLink = LinkFactory.createCustomLink("command.preview", "command.preview", re.getDisplayname(), Link.NONTRANSLATED, content, this);
                previewLink.setCustomEnabledLinkCSS("b_preview");
                previewLink.setTitle(getTranslator().translate("command.preview"));
            }
        } else {
            // no valid config yet
            content.contextPut("showPreviewLink", Boolean.FALSE);
            content.contextPut(CHOSEN_ENTRY, translate("no.entry.chosen"));
        }

        main.setContent(content);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == previewLink) {
            // Preview as modal dialogue only if the config is valid
            final RepositoryEntry re = this.wikiCourseNode.getReferencedRepositoryEntry(false);
            if (re == null) { // we cannot preview it, because the repository entry
                // had been deleted between the time when it was
                // chosen here, and now
                this.showError("error.repoentrymissing");
            } else {
                // File cpRoot =
                // FileResourceManager.getInstance().unzipFileResource(re.getOlatResource());
                final Identity ident = ureq.getIdentity();
                final boolean isOlatAdmin = ureq.getUserSession().getRoles().isOLATAdmin();
                final boolean isResourceOwner = RepositoryServiceImpl.getInstance().isOwnerOfRepositoryEntry(ident, re);
                final CourseEnvironment cenv = course.getCourseEnvironment();
                final SubscriptionContext subsContext = WikiManager.createTechnicalSubscriptionContextForCourse(cenv, wikiCourseNode);
                final WikiSecurityCallback callback = new WikiSecurityCallbackImpl(null, isOlatAdmin, false, false, isResourceOwner, subsContext);
                wikiCtr = WikiUIFactory.getInstance().createWikiMainController(ureq, getWindowControl(), re.getOlatResource(), callback, null);
                cmcWikiCtr = new CloseableModalController(getWindowControl(), translate("command.close"), wikiCtr.getInitialComponent());
                this.listenTo(cmcWikiCtr);
                cmcWikiCtr.insertHeaderCss();
                cmcWikiCtr.activate();
            }
        } else if (source == chooseButton || source == changeButton) {
            searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq, WikiResource.TYPE_NAME, translate("command.choose"));
            this.listenTo(searchController);
            cmcSearchController = new CloseableModalController(getWindowControl(), translate("close"), searchController.getInitialComponent(), true,
                    translate("command.create"));
            cmcSearchController.activate();
        } else if (source == editLink) {
            final RepositoryEntry repositoryEntry = wikiCourseNode.getReferencedRepositoryEntry();
            if (repositoryEntry == null) {
                // do nothing
                return;
            }
            final RepositoryHandler typeToEdit = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry);
            // Open editor in new tab
            final OLATResourceable ores = repositoryEntry.getOlatResource();

            final DTabs dts = Windows.getWindows(ureq).getWindow(ureq).getDynamicTabs();
            final Controller editorController = typeToEdit.createLaunchController(ores, null, ureq, dts.getWindowControl());
            DynamicTabHelper.openRepoEntryTab(repositoryEntry, ureq, editorController, repositoryEntry.getDisplayname(), RepositoryDetailsController.ACTIVATE_EDITOR);
        }

    }

    /**
	 */
    @Override
    protected void event(final UserRequest urequest, final Controller source, final Event event) {
        if (source == searchController) {
            cmcSearchController.deactivate();
            // repository search controller done
            if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
                final RepositoryEntry re = searchController.getSelectedEntry();
                if (re != null) {
                    wikiCourseNode.setRepositoryReference(re);
                    content.contextPut("showPreviewLink", Boolean.TRUE);
                    previewLink = LinkFactory.createCustomLink("command.preview", "command.preview", re.getDisplayname(), Link.NONTRANSLATED, content, this);
                    previewLink.setCustomEnabledLinkCSS("b_preview");
                    previewLink.setTitle(getTranslator().translate("command.preview"));
                    // no securitycheck on wiki, editable by everybody
                    editLink = LinkFactory.createButtonSmall("edit", content, this);
                    // fire event so the updated config is saved by the
                    // editormaincontroller
                    fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
                }
            } // else cancelled repo search
        } else if (source == accessCondContr) {
            if (event == Event.CHANGED_EVENT) {
                final Condition cond = accessCondContr.getCondition();
                wikiCourseNode.setPreConditionAccess(cond);
                fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        } else if (source == editCondContr) {
            if (event == Event.CHANGED_EVENT) {
                final Condition cond = editCondContr.getCondition();
                wikiCourseNode.setPreConditionEdit(cond);
                fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        } else if (source == cmcWikiCtr) {
            if (event == CloseableModalController.CLOSE_MODAL_EVENT) {
                cmcWikiCtr.dispose();
                wikiCtr.dispose();
            }
        }
    }

    /**
	 */
    @Override
    public void addTabs(final TabbedPane tabbedPane) {
        tabs = tabbedPane;
        tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), editAccessVc);
        tabbedPane.addTab(translate(PANE_TAB_WIKICONFIG), main);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // child controllers registered with listenTo() get disposed in BasicController
        if (wikiCtr != null) {
            wikiCtr.dispose();
            wikiCtr = null;
        }
    }

    /**
	 */
    @Override
    public String[] getPaneKeys() {
        return paneKeys;
    }

    /**
	 */
    @Override
    public TabbedPane getTabbedPane() {
        return tabs;
    }

}
