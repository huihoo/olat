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
package org.olat.lms.course.nodes;

import java.io.File;
import java.util.List;

import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.commons.UsedByXstream;
import org.olat.lms.commons.fileresource.PodcastFileResource;
import org.olat.lms.commons.validation.ValidationStatus;
import org.olat.lms.core.notification.service.SubscriptionContext;
import org.olat.lms.course.CourseModule;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.editor.CourseEditorEnv;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.webfeed.FeedManager;
import org.olat.lms.webfeed.FeedSecurityCallback;
import org.olat.presentation.course.editor.NodeEditController;
import org.olat.presentation.course.editor.StatusDescription;
import org.olat.presentation.course.nodes.TitledWrapperHelper;
import org.olat.presentation.course.nodes.feed.FeedNodeEditController;
import org.olat.presentation.course.nodes.feed.FeedPeekviewController;
import org.olat.presentation.course.nodes.feed.podcast.PodcastNodeEditController;
import org.olat.presentation.course.run.navigation.NodeRunConstructionResult;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.tabbable.TabbableController;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.webfeed.FeedMainController;
import org.olat.presentation.webfeed.FeedUIFactory;
import org.olat.presentation.webfeed.podcast.PodcastUIFactory;

/**
 * The podcast course node.
 * <P>
 * Initial Date: Mar 30, 2009 <br>
 * 
 * @author gwassmann
 */
public class PodcastCourseNode extends AbstractFeedCourseNode implements UsedByXstream {
    public static final String TYPE = FeedManager.KIND_PODCAST;

    /**
     * @param type
     */
    public PodcastCourseNode() {
        super(TYPE);
        updateModuleConfigDefaults(true);
    }

    /**
     * org.olat.lms.course.ICourse, org.olat.lms.course.run.userview.UserCourseEnvironment)
     */
    @Override
    public TabbableController createEditController(final UserRequest ureq, final WindowControl wControl, final ICourse course, final UserCourseEnvironment euce) {
        final CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
        final TabbableController podcastChildController = new PodcastNodeEditController(this, course, euce, ureq, wControl);
        return new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, chosenNode, course.getCourseEnvironment().getCourseGroupManager(), euce,
                podcastChildController);
    }

    /**
     * org.olat.lms.course.run.userview.UserCourseEnvironment, org.olat.lms.course.run.userview.NodeEvaluation, java.lang.String)
     */
    @Override
    public NodeRunConstructionResult createNodeRunConstructionResult(final UserRequest ureq, final WindowControl control, final UserCourseEnvironment userCourseEnv,
            final NodeEvaluation ne, final String nodecmd) {
        final RepositoryEntry entry = getReferencedRepositoryEntry();
        // create business path courseID:nodeID
        // userCourseEnv.getCourseEnvironment().getCourseResourceableId();
        // getIdent();
        final Long courseId = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
        final String nodeId = this.getIdent();
        final boolean isAdmin = ureq.getUserSession().getRoles().isOLATAdmin();
        final boolean isGuest = ureq.getUserSession().getRoles().isGuestOnly();
        final SubscriptionContext subscriptionContext = CourseModule.createSubscriptionContext(userCourseEnv.getCourseEnvironment(), this);
        final FeedSecurityCallback callback = new FeedNodeSecurityCallback(ne, isAdmin, isGuest, subscriptionContext);
        ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrap(this));
        final FeedMainController podcastCtr = PodcastUIFactory.getInstance(ureq.getLocale()).createMainController(entry.getOlatResource(), ureq, control, callback,
                courseId, nodeId);
        podcastCtr.activate(ureq, nodecmd);
        final Controller wrapperCtrl = TitledWrapperHelper.getWrapper(ureq, control, podcastCtr, this, "o_podcast_icon");
        final NodeRunConstructionResult result = new NodeRunConstructionResult(wrapperCtrl);
        return result;
    }

    /**
     * org.olat.lms.course.run.userview.UserCourseEnvironment, org.olat.lms.course.run.userview.NodeEvaluation)
     */
    @Override
    public Controller createPeekViewRunController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv, final NodeEvaluation ne) {
        if (ne.isAtLeastOneAccessible()) {
            // Create a feed peekview controller that shows the latest two entries
            final RepositoryEntry entry = getReferencedRepositoryEntry();
            final Long courseId = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
            final String nodeId = this.getIdent();
            final boolean isAdmin = ureq.getUserSession().getRoles().isOLATAdmin();
            final boolean isGuest = ureq.getUserSession().getRoles().isGuestOnly();
            final SubscriptionContext subscriptionContext = CourseModule.createSubscriptionContext(userCourseEnv.getCourseEnvironment(), this);
            final FeedSecurityCallback callback = new FeedNodeSecurityCallback(ne, isAdmin, isGuest, subscriptionContext);
            final FeedUIFactory uiFactory = PodcastUIFactory.getInstance(ureq.getLocale());
            final Controller peekViewController = new FeedPeekviewController(entry.getOlatResource(), ureq, wControl, callback, courseId, nodeId, uiFactory, 2,
                    "o_podcast_peekview");
            return peekViewController;
        } else {
            // use standard peekview
            return super.createPeekViewRunController(ureq, wControl, userCourseEnv, ne);
        }
    }

    @Override
    protected String getDefaultTitleOption() {
        return CourseNode.DISPLAY_OPTS_CONTENT;
    }

    /**
	 */
    @Override
    public StatusDescription[] isConfigValid(final CourseEditorEnv cev) {
        oneClickStatusCache = null;
        final String translatorStr = PackageUtil.getPackageName(PodcastNodeEditController.class);
        final List<StatusDescription> sds = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
        oneClickStatusCache = StatusDescriptionHelper.sort(sds);
        return oneClickStatusCache;
    }

    /**
	 */
    @Override
    public StatusDescription isConfigValid() {
        if (oneClickStatusCache != null) {
            return oneClickStatusCache[0];
        }

        StatusDescription status = StatusDescription.NOERROR;
        final boolean invalid = config.get(ModuleConfiguration.CONFIG_KEY_REPOSITORY_REF) == null;
        if (invalid) {
            final String[] params = new String[] { this.getShortTitle() };
            final String shortKey = "error.no.reference.short";
            final String longKey = "error.no.reference.long";
            final String translationPackage = PackageUtil.getPackageName(PodcastNodeEditController.class);
            status = new StatusDescription(ValidationStatus.ERROR, shortKey, longKey, params, translationPackage);
            status.setDescriptionForUnit(getIdent());
            // Set which pane is affected by error
            status.setActivateableViewIdentifier(FeedNodeEditController.PANE_TAB_FEED);
        }
        return status;
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public Controller importNode(final File importDirectory, final ICourse course, final boolean unattendedImport, final UserRequest ureq, final WindowControl wControl) {
        return super.importNode(importDirectory, course, unattendedImport, ureq, wControl, getResourceType());
    }

    private String getResourceType() {
        return PodcastFileResource.TYPE_NAME;
    }
}
