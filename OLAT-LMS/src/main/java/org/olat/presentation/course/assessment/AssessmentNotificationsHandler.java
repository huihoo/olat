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

package org.olat.presentation.course.assessment;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.PersistenceHelper;
import org.olat.data.notifications.Publisher;
import org.olat.data.notifications.Subscriber;
import org.olat.data.properties.AssessmentPropertyDao;
import org.olat.data.properties.PropertyImpl;
import org.olat.lms.commons.tree.INode;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.CourseGroupsEBL;
import org.olat.lms.course.CourseModule;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.Structure;
import org.olat.lms.course.assessment.AssessmentManager;
import org.olat.lms.course.nodes.AssessableCourseNode;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.CourseNodeFactory;
import org.olat.lms.course.nodes.STCourseNode;
import org.olat.lms.course.properties.CoursePropertyManager;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.lms.group.learn.CourseRights;
import org.olat.lms.notifications.NotificationHelper;
import org.olat.lms.notifications.NotificationService;
import org.olat.lms.notifications.NotificationsHandler;
import org.olat.lms.notifications.NotificationsUpgradeHelper;
import org.olat.lms.notifications.PublisherData;
import org.olat.lms.notifications.SubscriptionContext;
import org.olat.lms.notifications.SubscriptionInfo;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.notifications.SubscriptionListItem;
import org.olat.presentation.notifications.TitleItem;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Description:<br>
 * Calculates it the user has any assessment news for the notification system. Currently this checks for new tests
 * <P>
 * Initial Date: 21-giu-2005 <br>
 * 
 * @author Roberto Bagnoli
 */
@Component
public class AssessmentNotificationsHandler implements NotificationsHandler {
    private static final Logger log = LoggerHelper.getLogger();

    private static final String CSS_CLASS_USER_ICON = "o_icon_user";
    // private static final String CSS_CLASSS_IQTEST_ICON = "o_iqtest_icon";
    private final Map<Long, SubscriptionContext> subsContexts = new HashMap<Long, SubscriptionContext>();

    @Autowired
    NotificationService notificationService;
    @Autowired
    CourseGroupsEBL courseGroupEBL;

    /**
     * spring
     */
    private AssessmentNotificationsHandler() {
        //
    }

    /**
     * Returns the <code>SubscriptionContext</code> to use for assessment notification about specified <code>ICourse</code>.<br>
     * <br>
     * <b>PRE CONDITIONS</b>
     * <ul>
     * <li> <code>course != null</code>
     * </ul>
     * If <code>ident == null</code>, the subscription context is (created and) returned without authorization control
     * 
     * @param ident
     *            the identity, if null, no subscription check will be made
     * @param course
     * @return the subscription context to use or <code>null</code> if the identity associated to the request is not allowed to be notified
     */
    public SubscriptionContext getAssessmentSubscriptionContext(final Identity ident, final ICourse course) {
        SubscriptionContext sctx = null;

        if (ident == null || canSubscribeForAssessmentNotification(ident, course)) {
            // Creates a new SubscriptionContext only if not found into cache
            final Long courseId = course.getResourceableId();
            synchronized (subsContexts) { // o_clusterOK by:ld - no problem to have independent subsContexts caches for each cluster node
                sctx = subsContexts.get(courseId);
                if (sctx == null) {
                    // a subscription context showing to the root node (the course's root
                    // node is started when clicking such a notification)
                    final CourseNode cn = course.getRunStructure().getRootNode();
                    final CourseEnvironment ce = course.getCourseEnvironment();
                    // FIXME:fg:b little problem is that the assessment tool and the course are not "the same" anymore, that is you can open the same course twice in the
                    // dynamic tabs by a) klicking e.g. via repo, and b via notifications link to the assementtool
                    sctx = new SubscriptionContext(CourseModule.ORES_COURSE_ASSESSMENT, ce.getCourseResourceableId(), cn.getIdent());
                    subsContexts.put(courseId, sctx);
                }
            }
        }

        return sctx;
    }

    /**
     * Shortcut for <code>getAssessmentSubscriptionContext((Identity) null, course)</code>
     * 
     * @param course
     * @return the AssessmentSubscriptionContext
     */
    private SubscriptionContext getAssessmentSubscriptionContext(final ICourse course) {
        return getAssessmentSubscriptionContext((Identity) null, course);
    }

    /**
     * Return <code>PublisherData</code> instance to use for assessment notification.<br>
     * <br>
     * <b>PRE CONDITIONS</b>
     * <ul>
     * <li> <code>course != null</code>
     * </ul>
     * 
     * @param course
     * @param the
     *            business path
     * @return the publisherdata
     */
    public PublisherData getAssessmentPublisherData(final ICourse course, final String businessPath) {
        return new PublisherData(CourseModule.ORES_COURSE_ASSESSMENT, String.valueOf(course.getCourseEnvironment().getCourseResourceableId()), businessPath);
    }

    /**
     * Signal the <code>NotificationsManagerImpl</code> about assessment news available for a course.<br>
     * <br>
     * <b>PRE CONDITIONS</b>
     * <ul>
     * <li> <code>courseId != null</code>
     * </ul>
     * 
     * @param courseId
     *            the resourceable id of the course to signal news about
     * @param ident
     *            the identity to ignore news for
     */
    public void markPublisherNews(final Identity ident, final Long courseId) {
        final ICourse course = loadCourseFromId(courseId);
        if (course == null) {
            throw new AssertException("course with id " + courseId + " not found!");
        }
        markPublisherNews(ident, course);
    }

    /**
     * Signal the <code>NotificationsManagerImpl</code> about assessment news available on a course.<br>
     * <br>
     * <b>PRE CONDITIONS</b>
     * <ul>
     * <li> <code>course != null</code>
     * </ul>
     * 
     * @param course
     *            the course to signal news about
     * @param ident
     *            the identity to ignore news for
     */
    private void markPublisherNews(final Identity ident, final ICourse course) {
        final SubscriptionContext subsContext = getAssessmentSubscriptionContext(course);
        if (subsContext != null) {
            notificationService.markPublisherNews(subsContext, ident);
        }
    }

    /**
     * Assessment notification rights check.<br>
     * Tests if an <code>Identity</code> can subscribe for assessment notification for the specified <code>ICourse</code>.<br>
     * <br>
     * <b>PRE CONDITIONS</b>
     * <ul>
     * <li> <code>course != null</code>
     * </ul>
     * 
     * @param ident
     *            the identity to check rights for. Can be <code>null</code>
     * @param course
     *            the course to check rights against
     * @return if <code>ident == null</code> this method always returns false; otherwise subscriptions rights are met only by course administrators and course coaches
     */
    private boolean canSubscribeForAssessmentNotification(final Identity ident, final ICourse course) {
        if (ident == null) {
            return false;
        }

        return courseGroupEBL.isSuperUser(ident, course);
    }

    /**
     * Utility method.<br>
     * Load an instance of <code>ICourse</code> given its numeric resourceable id
     */
    private ICourse loadCourseFromId(final Long courseId) {
        return CourseFactory.loadCourse(courseId);
    }

    /**
     * Utility method.<br>
     * Build (recursively) the list of all test nodes belonging to the specified <code>ICourse</code>.<br>
     * The returned <code>List</code> is empty if course has no AssessableCourseNode. Structure course node are excluded from the list.<br>
     * <br>
     * <b>PRE CONDITIONS</b>
     * <ul>
     * <li> <code>course != null</code>
     * </ul>
     * <br>
     * <b>POST CONDITIONS</b>
     * <ul>
     * <li>The returned list, if not empty, contains ONLY instances of type <code>AssessableCourseNode</code>
     * </ul>
     */
    private List<AssessableCourseNode> getCourseTestNodes(final ICourse course) {
        final List<AssessableCourseNode> assessableNodes = new ArrayList<AssessableCourseNode>();

        final Structure courseStruct = course.getRunStructure();
        final CourseNode rootNode = courseStruct.getRootNode();

        getCourseTestNodes(rootNode, assessableNodes);

        return assessableNodes;
    }

    /**
     * Recursive step used by <code>getCourseAssessableNodes(ICourse)</code>.<br>
     * <br>
     * <b>PRE CONDITIONS</b>
     * <ul>
     * <li> <code>course != null</code>
     * <li> <code>result != null</code>
     * </ul>
     * 
     */
    private void getCourseTestNodes(final INode node, final List<AssessableCourseNode> result) {
        if (node != null) {
            if (node instanceof AssessableCourseNode && !(node instanceof STCourseNode)) {
                result.add((AssessableCourseNode) node);
            }

            for (int i = 0; i < node.getChildCount(); i++) {
                getCourseTestNodes(node.getChildAt(i), result);
            }
        }
    }

    /**
	 */
    @Override
    public SubscriptionInfo createSubscriptionInfo(final Subscriber subscriber, final Locale locale, final Date compareDate) {
        SubscriptionInfo si = null;
        final Publisher p = subscriber.getPublisher();
        if (!NotificationsUpgradeHelper.isCourseRepositoryEntryFound(p)) {
            // course don't exist anymore
            notificationService.deactivate(p);
            return notificationService.getNoSubscriptionInfo();
        }

        try {
            final Date latestNews = p.getLatestNewsDate();
            final Identity identity = subscriber.getIdentity();

            // do not try to create a subscription info if state is deleted - results in
            // exceptions, course
            // can't be loaded when already deleted
            if (notificationService.isPublisherValid(p) && compareDate.before(latestNews)) {
                final Long courseId = new Long(p.getData());
                final ICourse course = loadCourseFromId(courseId);
                if (course != null) {
                    // course admins or users with the course right to have full access to
                    // the assessment tool will have full access to user tests
                    final boolean hasFullAccess = courseGroupEBL.hasAllRights(identity, course);
                    List<Identity> coachedUsers = new ArrayList<Identity>();
                    if (!hasFullAccess) {
                        coachedUsers = courseGroupEBL.getCoachedUsers(identity, course);
                    }
                    final List<AssessableCourseNode> testNodes = getCourseTestNodes(course);

                    for (final AssessableCourseNode test : testNodes) {
                        final CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();

                        final List<PropertyImpl> scoreProperties = cpm.listCourseNodeProperties(test, null, null, AssessmentPropertyDao.SCORE);
                        final List<PropertyImpl> attemptProperties = cpm.listCourseNodeProperties(test, null, null, AssessmentPropertyDao.ATTEMPTS);

                        for (final PropertyImpl attemptProperty : attemptProperties) {
                            final Date modDate = attemptProperty.getLastModified();
                            final Identity assessedIdentity = attemptProperty.getIdentity();
                            if (modDate.after(compareDate) && (hasFullAccess || PersistenceHelper.listContainsObjectByKey(coachedUsers, assessedIdentity))) {
                                String score = null;
                                for (final PropertyImpl scoreProperty : scoreProperties) {
                                    if (scoreProperty.getIdentity().equalsByPersistableKey(assessedIdentity)
                                            || scoreProperty.getCategory().equals(attemptProperty.getCategory())) {
                                        score = scoreProperty.getFloatValue().toString();
                                        break;
                                    }
                                }

                                final Translator translator = PackageUtil.createPackageTranslator(AssessmentNotificationsHandler.class, locale);
                                final SubscriptionListItem subListItem = createSubscriptionListItem(p, test, modDate, assessedIdentity, score, translator);
                                if (si == null) {
                                    final String title = translator.translate("notifications.header", new String[] { course.getCourseTitle() });
                                    final String css = CourseNodeFactory.getInstance().getCourseNodeConfiguration(test.getType()).getIconCSSClass();
                                    si = new SubscriptionInfo(new TitleItem(title, css), null);
                                }
                                si.addSubscriptionListItem(subListItem);
                            }
                        }
                    }
                }
            }
            if (si == null) {
                si = notificationService.getNoSubscriptionInfo();
            }
            return si;
        } catch (final Exception e) {
            log.error("Error while creating assessment notifications", e);
            checkPublisher(p);
            return notificationService.getNoSubscriptionInfo();
        }
    }

    /**
     * @param p
     * @param test
     * @param modDate
     * @param assessedIdentity
     * @param score
     * @param translator
     * @return
     */
    private SubscriptionListItem createSubscriptionListItem(final Publisher p, final AssessableCourseNode test, final Date modDate, final Identity assessedIdentity,
            String score, final Translator translator) {
        String desc;
        final String type = translator.translate("notifications.entry." + test.getType());
        if (score == null) {
            desc = translator.translate("notifications.entry.attempt", new String[] { test.getShortTitle(), NotificationHelper.getFormatedName(assessedIdentity), type });
        } else {
            desc = translator.translate("notifications.entry", new String[] { test.getShortTitle(), NotificationHelper.getFormatedName(assessedIdentity), score, type });
        }

        String urlToSend = null;
        if (p.getBusinessPath() != null) {
            final String businessPath = p.getBusinessPath() + "[assessmentTool:0][Identity:" + assessedIdentity.getKey() + "][CourseNode:" + test.getIdent() + "]";
            urlToSend = NotificationHelper.getURLFromBusinessPathString(p, businessPath);
        }

        final SubscriptionListItem subListItem = new SubscriptionListItem(desc, urlToSend, modDate, CSS_CLASS_USER_ICON);
        return subListItem;
    }

    private void checkPublisher(final Publisher p) {
        try {
            if (!NotificationsUpgradeHelper.isCourseRepositoryEntryFound(p)) {
                log.info("deactivating publisher with key; " + p.getKey(), null);
                notificationService.deactivate(p);
            }
        } catch (final Exception e) {
            log.error("", e);
        }
    }

    @Override
    public String createTitleInfo(final Subscriber subscriber, final Locale locale) {
        try {
            final Long resId = subscriber.getPublisher().getResId();
            final String displayName = getRepositoryService().lookupDisplayNameByOLATResourceableId(resId);
            final Translator trans = PackageUtil.createPackageTranslator(AssessmentNotificationsHandler.class, locale);
            final String title = trans.translate("notifications.title", new String[] { displayName });
            return title;
        } catch (final Exception e) {
            log.error("Error while creating assessment notifications for subscriber: " + subscriber.getKey(), e);
            checkPublisher(subscriber.getPublisher());
            return "-";
        }
    }

    /**
     * @return
     */
    private RepositoryService getRepositoryService() {
        return CoreSpringFactory.getBean(RepositoryServiceImpl.class);
    }

    @Override
    public String getType() {
        return "AssessmentManager";
    }

}
