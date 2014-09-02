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

package org.olat.lms.course.archiver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.group.BusinessGroup;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.AssessmentHelper;
import org.olat.lms.course.assessment.AssessmentManager;
import org.olat.lms.course.nodes.AssessableCourseNode;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.lms.course.run.scoring.ScoreEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.lms.security.IdentityEnvironment;
import org.olat.lms.user.UserService;
import org.olat.lms.user.propertyhandler.UserPropertyHandler;
import org.olat.presentation.course.archiver.ScoreAccountingArchiveController;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.StringHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * @author schneider Comment: Provides functionality to get a course results overview.
 */
public class ScoreAccountingHelper {
    private static final String PACKAGE = PackageUtil.getPackageName(ScoreAccountingArchiveController.class);

    /**
     * The results from assessable nodes are written to one row per user into an excel-sheet. An assessable node will only appear if it is producing at least one of the
     * following variables: score, passed, attempts, comments.
     * 
     * @param identities
     * @param myNodes
     * @param course
     * @param locale
     * @return String
     */
    public static String createCourseResultsOverviewTable(final List identities, final List myNodes, final ICourse course, final Locale locale) {
        Translator t = new PackageTranslator(PACKAGE, locale);
        final StringBuilder tableHeader1 = new StringBuilder();
        final StringBuilder tableHeader2 = new StringBuilder();
        final StringBuilder tableContent = new StringBuilder();
        final StringBuilder table = new StringBuilder();

        final String sequentialNumber = t.translate("column.header.seqnum");
        final String login = t.translate("column.header.login");
        // user properties are dynamic
        final String sc = t.translate("column.header.score");
        final String pa = t.translate("column.header.passed");
        final String co = t.translate("column.header.comment");
        final String cco = t.translate("column.header.coachcomment");
        final String at = t.translate("column.header.attempts");
        final String na = t.translate("column.field.notavailable");
        final String mi = t.translate("column.field.missing");
        final String yes = t.translate("column.field.yes");
        final String no = t.translate("column.field.no");

        tableHeader1.append(sequentialNumber);
        tableHeader1.append("\t");
        tableHeader2.append("\t");

        tableHeader1.append(login);
        tableHeader1.append("\t");
        tableHeader2.append("\t");
        // get user property handlers for this export, translate using the fallback
        // translator configured in the property handler

        final List<UserPropertyHandler> userPropertyHandlers = getUserService().getUserPropertyHandlersFor(ScoreAccountingHelper.class.getCanonicalName(), true);
        t = getUserService().getUserPropertiesConfig().getTranslator(t);
        for (final UserPropertyHandler propertyHandler : userPropertyHandlers) {
            tableHeader1.append(t.translate(propertyHandler.i18nColumnDescriptorLabelKey()));
            tableHeader1.append("\t");
            tableHeader2.append("\t");
        }

        // preload user properties cache
        course.getCourseEnvironment().getAssessmentManager().preloadCache();

        boolean firstIteration = true;
        int rowNumber = 1;
        final Iterator iterIdentities = identities.iterator();
        while (iterIdentities.hasNext()) {
            final Identity identity = (Identity) iterIdentities.next();
            final String uname = identity.getName();

            tableContent.append(rowNumber);
            tableContent.append("\t");
            tableContent.append(uname);
            tableContent.append("\t");
            // add dynamic user properties
            for (final UserPropertyHandler propertyHandler : userPropertyHandlers) {
                final String value = propertyHandler.getUserProperty(identity.getUser(), t.getLocale());
                tableContent.append((StringHelper.containsNonWhitespace(value) ? value : na));
                tableContent.append("\t");
            }

            // create a identenv with no roles, no attributes, no locale
            final IdentityEnvironment ienv = new IdentityEnvironment();
            ienv.setIdentity(identity);
            final UserCourseEnvironment uce = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());
            uce.getScoreAccounting().evaluateAll();
            final AssessmentManager am = course.getCourseEnvironment().getAssessmentManager();

            final Iterator iterNodes = myNodes.iterator();
            while (iterNodes.hasNext()) {
                final AssessableCourseNode acnode = (AssessableCourseNode) iterNodes.next();
                final boolean scoreOk = acnode.hasScoreConfigured();
                final boolean passedOk = acnode.hasPassedConfigured();
                final boolean attemptsOk = acnode.hasAttemptsConfigured();
                final boolean commentOk = acnode.hasCommentConfigured();

                if (scoreOk || passedOk || commentOk || attemptsOk) {
                    final ScoreEvaluation se = uce.getScoreAccounting().getScoreEvaluation(acnode);
                    boolean nodeColumnOk = false;
                    final StringBuilder tabs = new StringBuilder();

                    if (scoreOk) {
                        final Float score = se.getScore();
                        nodeColumnOk = true;
                        tabs.append("\t"); // tabulators for header1 after node title

                        if (firstIteration) {
                            tableHeader2.append(sc);
                            tableHeader2.append("\t");
                        }

                        if (score != null) {
                            tableContent.append(AssessmentHelper.getRoundedScore(score));
                            tableContent.append("\t");
                        } else { // score == null
                            tableContent.append(mi);
                            tableContent.append("\t");
                        }
                    }

                    if (passedOk) {
                        final Boolean passed = se.getPassed();
                        nodeColumnOk = true;
                        tabs.append("\t"); // tabulators for header1 after node title

                        if (firstIteration) {
                            tableHeader2.append(pa);
                            tableHeader2.append("\t");
                        }

                        if (passed != null) {
                            String yesno;
                            if (passed.booleanValue()) {
                                yesno = yes;
                            } else {
                                yesno = no;
                            }
                            tableContent.append(yesno);
                            tableContent.append("\t");
                        } else { // passed == null
                            tableContent.append(mi);
                            tableContent.append("\t");
                        }
                    }

                    if (attemptsOk) {
                        final Integer attempts = am.getNodeAttempts(acnode, identity);
                        final int a = attempts.intValue();
                        nodeColumnOk = true;
                        tabs.append("\t"); // tabulators for header1 after node title

                        if (firstIteration) {
                            tableHeader2.append(at);
                            tableHeader2.append("\t");
                        }

                        tableContent.append(a);
                        tableContent.append("\t");
                    }

                    if (commentOk) {
                        // Comments for user
                        final String comment = am.getNodeComment(acnode, identity);
                        nodeColumnOk = true;
                        tabs.append("\t"); // tabulators for header1 after node title

                        if (firstIteration) {
                            tableHeader2.append(co);
                            tableHeader2.append("\t");
                        }

                        if (comment != null) {
                            // put comment between double quote in order to prevent that
                            // '\t','\r' or '\n' destroy the excel table
                            tableContent.append("\"");
                            tableContent.append(comment);
                            tableContent.append("\"\t");
                        } else {
                            tableContent.append(mi);
                            tableContent.append("\t");
                        }

                        // Comments for tutors
                        final String coachComment = am.getNodeCoachComment(acnode, identity);
                        tabs.append("\t"); // tabulators for header1 after node title

                        if (firstIteration) {
                            tableHeader2.append(cco);
                            tableHeader2.append("\t");
                        }

                        if (coachComment != null) {
                            // put coachComment between double quote in order to prevent that
                            // '\t','\r' or '\n' destroy the excel table
                            tableContent.append("\"");
                            tableContent.append(coachComment);
                            tableContent.append("\"\t");
                        } else {
                            tableContent.append(mi);
                            tableContent.append("\t");
                        }

                    }

                    if (firstIteration && nodeColumnOk) {
                        final String shortTitle = acnode.getShortTitle();

                        tableHeader1.append(shortTitle);
                        tableHeader1.append(tabs.toString());
                    }

                }
            }
            if (firstIteration) {
                tableHeader1.append("\t\n");
                tableHeader2.append("\t\n");
            }
            tableContent.append("\t\n");
            firstIteration = false;
            rowNumber++;
        }

        table.append(tableHeader1);
        table.append(tableHeader2);
        table.append(tableContent);
        final String tab = table.toString();

        return tab;
    }

    /**
     * Load all users from all known learning groups into a list
     * 
     * @param courseEnv
     * @return The list of identities from this course
     */
    public static List<Identity> loadUsers(final CourseEnvironment courseEnv) {
        final List<Identity> identites = new ArrayList<Identity>();
        final CourseGroupManager gm = courseEnv.getCourseGroupManager();
        final BaseSecurity securityManager = (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
        final List groups = gm.getAllLearningGroupsFromAllContexts(courseEnv.getCourseOLATResourceable());

        final Iterator iter = groups.iterator();
        while (iter.hasNext()) {
            final BusinessGroup group = (BusinessGroup) iter.next();
            final SecurityGroup participants = group.getPartipiciantGroup();
            final List<Identity> ids = securityManager.getIdentitiesOfSecurityGroup(participants);
            identites.addAll(ids);
        }
        return identites;
    }

    /**
     * Load all nodes which are assessable
     * 
     * @param courseEnv
     * @return The list of assessable nodes from this course
     */
    public static List<CourseNode> loadAssessableNodes(final CourseEnvironment courseEnv) {
        final CourseNode rootNode = courseEnv.getRunStructure().getRootNode();
        final List<CourseNode> nodeList = new ArrayList<CourseNode>();
        collectAssessableCourseNodes(rootNode, nodeList);

        return nodeList;
    }

    /**
     * Collects recursively all assessable course nodes
     * 
     * @param node
     * @param nodeList
     */
    private static void collectAssessableCourseNodes(final CourseNode node, final List<CourseNode> nodeList) {
        if (node instanceof AssessableCourseNode) {
            nodeList.add(node);
        }
        final int count = node.getChildCount();
        for (int i = 0; i < count; i++) {
            final CourseNode cn = (CourseNode) node.getChildAt(i);
            collectAssessableCourseNodes(cn, nodeList);
        }
    }

    private static UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
