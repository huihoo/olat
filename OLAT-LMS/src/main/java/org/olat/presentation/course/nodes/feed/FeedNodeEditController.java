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
package org.olat.presentation.course.nodes.feed;

import java.util.Locale;

import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.AssessmentHelper;
import org.olat.lms.course.condition.Condition;
import org.olat.lms.course.nodes.AbstractFeedCourseNode;
import org.olat.lms.course.nodes.CourseNodeFactory;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.course.tree.CourseEditorTreeModel;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.lms.webfeed.FeedPreviewSecurityCallback;
import org.olat.lms.webfeed.FeedSecurityCallback;
import org.olat.presentation.course.condition.ConditionEditController;
import org.olat.presentation.course.editor.NodeEditController;
import org.olat.presentation.framework.core.UserRequest;
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
import org.olat.presentation.framework.core.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.repository.ReferencableEntriesSearchController;
import org.olat.presentation.webfeed.FeedUIFactory;
import org.olat.system.event.Event;

/**
 * The abstract feed course node edit controller.
 * <P>
 * Initial Date: Mar 31, 2009 <br>
 * 
 * @author gwassmann
 */
public abstract class FeedNodeEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {
    // Constants
    public static final String PANE_TAB_FEED = "pane.tab.feed";
    private static final String PANE_TAB_ACCESS = "pane.tab.access";
    private static final String[] paneKeys = { PANE_TAB_ACCESS, PANE_TAB_FEED };
    // More constants, mainly keys.
    private static final String CHOSEN_FEED_NAME = "chosen_feed_name";
    private static final String NO_FEED_CHOSEN = "no.feed.chosen";
    private static final String SHOW_PREVIEW_LINK = "showPreviewLink";
    private static final String COMMAND_PREVIEW = "command.preview";
    private static final String ERROR_REPOSITORY_ENTRY_MISSING = "error.repository.entry.missing";
    private static final String BUTTON_CHOOSE_FEED = "button.choose.feed";
    private static final String BUTTON_CHANGE_FEED = "button.change.feed";
    private static final String BUTTON_CREATE_FEED = "button.create.feed";
    // Components
    private TabbedPane tabbedPane;
    private final Panel learningResource;
    private final Link chooseButton, changeButton;
    private Link previewLink;
    private Link editLink;
    private final VelocityContainer accessVC, contentVC;
    // Controllers
    private final ConditionEditController readerCtr, posterCtr, moderatroCtr;
    private ReferencableEntriesSearchController searchController;
    private CloseableModalController cmc, cmcFeedCtr;
    private Controller feedController;
    // The actual node and its configuration for easy access.
    private final ModuleConfiguration config;
    private final AbstractFeedCourseNode node;
    private final ICourse course;
    private final FeedUIFactory uiFactory;
    private final String resourceTypeName;

    /**
     * Constructor. The uiFactory is needed for preview controller and the resourceTypeName for the repository search.
     * 
     * @param courseNode
     * @param course
     * @param uce
     * @param uiFactory
     * @param resourceTypeName
     * @param ureq
     * @param control
     */
    public FeedNodeEditController(final AbstractFeedCourseNode courseNode, final ICourse course, final UserCourseEnvironment uce, final FeedUIFactory uiFactory,
            final String resourceTypeName, final UserRequest ureq, final WindowControl control) {
        super(ureq, control);
        this.course = course;
        this.node = courseNode;
        this.config = courseNode.getModuleConfiguration();
        this.uiFactory = uiFactory;
        this.resourceTypeName = resourceTypeName;
        setTranslatorAndFallback(ureq.getLocale());

        this.getClass().getSuperclass();
        // Accessibility tab
        accessVC = new VelocityContainer("accessVC", FeedNodeEditController.class, "access", getTranslator(), this);
        final CourseGroupManager groupMgr = course.getCourseEnvironment().getCourseGroupManager();
        final CourseEditorTreeModel editorModel = course.getEditorTreeModel();

        // Moderator precondition
        final Condition moderatorCondition = node.getPreConditionModerator();
        moderatroCtr = new ConditionEditController(ureq, getWindowControl(), groupMgr, moderatorCondition, "moderatorConditionForm", AssessmentHelper.getAssessableNodes(
                editorModel, node), uce);
        this.listenTo(moderatroCtr);
        accessVC.put("moderatorCondition", moderatroCtr.getInitialComponent());

        // Poster precondition
        final Condition posterCondition = node.getPreConditionPoster();
        posterCtr = new ConditionEditController(ureq, getWindowControl(), groupMgr, posterCondition, "posterConditionForm", AssessmentHelper.getAssessableNodes(
                editorModel, node), uce);
        this.listenTo(posterCtr);
        accessVC.put("posterCondition", posterCtr.getInitialComponent());

        // Reader precondition
        final Condition readerCondition = node.getPreConditionReader();
        readerCtr = new ConditionEditController(ureq, getWindowControl(), groupMgr, readerCondition, "readerConditionForm", AssessmentHelper.getAssessableNodes(
                editorModel, node), uce);
        this.listenTo(readerCtr);
        accessVC.put("readerCondition", readerCtr.getInitialComponent());

        // Podcast tab. Embed the actual podcast learning contentVC into the
        // building block
        learningResource = new Panel("learning_resource_panel");
        contentVC = new VelocityContainer("accessVC", FeedNodeEditController.class, "edit", getTranslator(), this);
        changeButton = LinkFactory.createButtonSmall(BUTTON_CHANGE_FEED, contentVC, this);
        chooseButton = LinkFactory.createButtonSmall(BUTTON_CREATE_FEED, contentVC, this);

        if (config.get(ModuleConfiguration.CONFIG_KEY_REPOSITORY_REF) != null) {
            // fetch repository entry to display the repository entry title of the
            // chosen cp
            final RepositoryEntry re = courseNode.getReferencedRepositoryEntry();
            if (re == null) {
                // we cannot display the entries name, because the repository entry has
                // been deleted since it was last embeded.
                this.showError(ERROR_REPOSITORY_ENTRY_MISSING);
                contentVC.contextPut(SHOW_PREVIEW_LINK, Boolean.FALSE);
                contentVC.contextPut(CHOSEN_FEED_NAME, translate(NO_FEED_CHOSEN));
            } else {
                // no securitycheck on feeds, editable by everybody
                editLink = LinkFactory.createButtonSmall("edit", contentVC, this);
                contentVC.contextPut(SHOW_PREVIEW_LINK, Boolean.TRUE);
                previewLink = LinkFactory.createCustomLink(COMMAND_PREVIEW, COMMAND_PREVIEW, re.getDisplayname(), Link.NONTRANSLATED, contentVC, this);
                previewLink.setCustomEnabledLinkCSS("b_preview");
                previewLink.setTitle(getTranslator().translate(COMMAND_PREVIEW));

            }
        } else {
            // no valid config yet
            contentVC.contextPut(SHOW_PREVIEW_LINK, Boolean.FALSE);
            contentVC.contextPut(CHOSEN_FEED_NAME, translate(NO_FEED_CHOSEN));
        }
        learningResource.setContent(contentVC);
    }

    private void setTranslatorAndFallback(final Locale locale) {
        // The implementing class
        final Class<? extends FeedNodeEditController> thisClass = this.getClass();
        final Translator fallback = PackageUtil.createPackageTranslator(thisClass.getSuperclass(), locale);
        final Translator translator = PackageUtil.createPackageTranslator(thisClass, locale, fallback);
        setTranslator(translator);
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
        return tabbedPane;
    }

    /**
	 */
    @Override
    protected void doDispose() {
        if (cmc != null) {
            cmc.dispose();
            cmc = null;
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == chooseButton || source == changeButton) {
            searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq, resourceTypeName, translate(BUTTON_CHOOSE_FEED));
            this.listenTo(searchController);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), searchController.getInitialComponent(), true, translate(BUTTON_CREATE_FEED));
            cmc.activate();
        } else if (source == previewLink) {
            // Preview as modal dialogue only if the config is valid
            final RepositoryEntry re = node.getReferencedRepositoryEntry();
            if (re == null) {
                // The repository entry has been deleted meanwhile.
                this.showError("error.repoentrymissing");
            } else {
                final FeedSecurityCallback callback = new FeedPreviewSecurityCallback();
                feedController = uiFactory.createMainController(re.getOlatResource(), ureq, getWindowControl(), callback, course.getResourceableId(), node.getIdent());
                cmcFeedCtr = new CloseableModalController(getWindowControl(), translate("command.close"), feedController.getInitialComponent());
                this.listenTo(cmcFeedCtr);
                // cmcFeedCtr.insertHeaderCss();
                cmcFeedCtr.activate();
            }

        } else if (source == editLink) {
            CourseNodeFactory.getInstance().launchReferencedRepoEntryEditor(ureq, node);
        }

    }

    /**
	 */
    @Override
    protected void event(final UserRequest urequest, final Controller source, final Event event) {
        if (source == moderatroCtr) {
            if (event == Event.CHANGED_EVENT) {
                final Condition cond = moderatroCtr.getCondition();
                node.setPreConditionModerator(cond);
                fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        } else if (source == posterCtr) {
            if (event == Event.CHANGED_EVENT) {
                final Condition cond = posterCtr.getCondition();
                node.setPreConditionPoster(cond);
                fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        } else if (source == readerCtr) {
            if (event == Event.CHANGED_EVENT) {
                final Condition cond = readerCtr.getCondition();
                node.setPreConditionReader(cond);
                fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        } else if (source == searchController) {
            cmc.deactivate();
            // repository search controller done
            if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
                final RepositoryEntry re = searchController.getSelectedEntry();
                if (re != null) {
                    config.set(ModuleConfiguration.CONFIG_KEY_REPOSITORY_REF, re.getSoftkey());

                    contentVC.contextPut("showPreviewLink", Boolean.TRUE);
                    previewLink = LinkFactory.createCustomLink("command.preview", "command.preview", re.getDisplayname(), Link.NONTRANSLATED, contentVC, this);
                    previewLink.setCustomEnabledLinkCSS("b_preview");
                    previewLink.setTitle(getTranslator().translate("command.preview"));
                    // no securitycheck on feeds, editable by everybody
                    editLink = LinkFactory.createButtonSmall("edit", contentVC, this);
                    // fire event so the updated config is saved by the
                    // editormaincontroller
                    fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
                }
            } // else cancelled repo search
        } else if (source == cmcFeedCtr) {
            if (event == CloseableModalController.CLOSE_MODAL_EVENT) {
                cmcFeedCtr.dispose();
                feedController.dispose();
            }
        }
    }

    /**
	 */
    @Override
    public void addTabs(final TabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
        tabbedPane.addTab(translate(PANE_TAB_ACCESS), accessVC);
        tabbedPane.addTab(translate(PANE_TAB_FEED), learningResource);
    }

}
