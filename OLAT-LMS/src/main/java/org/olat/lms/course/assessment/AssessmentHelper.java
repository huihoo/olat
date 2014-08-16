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

package org.olat.lms.course.assessment;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.commons.tree.INode;
import org.olat.lms.commons.tree.TreeVisitor;
import org.olat.lms.commons.tree.Visitor;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.AssessableCourseNode;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.ProjectBrokerCourseNode;
import org.olat.lms.course.nodes.STCourseNode;
import org.olat.lms.course.nodes.ScormCourseNode;
import org.olat.lms.course.run.scoring.ScoreEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.lms.course.tree.CourseEditorTreeModel;
import org.olat.lms.course.tree.CourseEditorTreeNode;
import org.olat.lms.security.IdentityEnvironment;
import org.olat.presentation.course.nodes.iq.IQEditController;
import org.olat.system.commons.Formatter;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * Helper methods for the course assessment system
 * <P>
 * Initial Date: Oct 28, 2004<br>
 * 
 * @author gnaegi
 */
public class AssessmentHelper {

    private static final Logger log = LoggerHelper.getLogger();

    /**
     * String to symbolize 'not available' or 'not assigned' in assessments details *
     */
    public static final String DETAILS_NA_VALUE = "n/a";

    /** Highes score value supported by OLAT * */
    public static final float MAX_SCORE_SUPPORTED = 10000f;
    /** Lowest score value supported by OLAT * */
    public static final float MIN_SCORE_SUPPORTED = -10000f;

    /**
     * Wraps an identity and it's score evaluation / attempts in a wrapper object for a given course node
     * 
     * @param identity
     * @param localUserCourseEnvironmentCache
     * @param course
     *            the course
     * @param courseNode
     *            an assessable course node or null if no details and attempts must be fetched
     * @return a wrapped identity
     */
    public static AssessedIdentityWrapper wrapIdentity(final Identity identity, final Map<Long, UserCourseEnvironment> localUserCourseEnvironmentCache,
            final ICourse course, final AssessableCourseNode courseNode) {
        // Try to get user course environment from local hash map cache. If not
        // successful
        // create the environment and add it to the map for later performance
        // optimization
        // synchronized (localUserCourseEnvironmentCache) { //o_clusterOK by:ld - no need to synchronized - only local variables
        UserCourseEnvironment uce = localUserCourseEnvironmentCache.get(identity.getKey());
        if (uce == null) {
            uce = createAndInitUserCourseEnvironment(identity, course);
            // add to cache for later usage
            localUserCourseEnvironmentCache.put(identity.getKey(), uce);
            if (log.isDebugEnabled()) {
                log.debug("localUserCourseEnvironmentCache hit failed , adding course environment for user::" + identity.getName());
            }
        }
        return wrapIdentity(uce, courseNode);
        // }
    }

    /**
     * Wraps an identity and it's score evaluation / attempts in a wrapper object for a given course node
     * 
     * @param uce
     *            The users course environment. Must be initialized (uce.getScoreAccounting().evaluateAll() must be called previously)
     * @param courseNode
     *            an assessable course node or null if no details and attempts must be fetched
     * @return a wrapped identity
     */
    public static AssessedIdentityWrapper wrapIdentity(final UserCourseEnvironment uce, final AssessableCourseNode courseNode) {
        // Fetch attempts and details for this node if available
        Integer attempts = null;
        String details = null;
        if (courseNode != null) {
            if (courseNode.hasAttemptsConfigured()) {
                attempts = courseNode.getUserAttempts(uce);
            }
            if (courseNode.hasDetails()) {
                details = courseNode.getDetailsListView(uce);
                if (details == null) {
                    details = DETAILS_NA_VALUE;
                }
            }
        }
        final AssessedIdentityWrapper aiw = new AssessedIdentityWrapper(uce, attempts, details);
        return aiw;
    }

    /**
     * Create a user course environment for the given user and course. After creation, the users score accounting will be initialized.
     * 
     * @param identity
     * @param course
     * @return Initialized user course environment
     */
    public static UserCourseEnvironment createAndInitUserCourseEnvironment(final Identity identity, final ICourse course) {
        // create an identenv with no roles, no attributes, no locale
        final IdentityEnvironment ienv = new IdentityEnvironment();
        ienv.setIdentity(identity);
        final UserCourseEnvironment uce = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());
        // Fetch all score and passed and calculate score accounting for the entire
        // course
        uce.getScoreAccounting().evaluateAll();
        return uce;
    }

    /**
     * check the given node for assessability.
     * 
     * @param node
     * @return
     */
    public static boolean checkIfNodeIsAssessable(final CourseNode node) {
        if (node instanceof AssessableCourseNode) {
            if (node instanceof STCourseNode) {
                final STCourseNode scn = (STCourseNode) node;
                if (scn.hasPassedConfigured() || scn.hasScoreConfigured()) {
                    return true;
                }
            } else if (node instanceof ScormCourseNode) {
                final ScormCourseNode scormn = (ScormCourseNode) node;
                if (scormn.hasScoreConfigured()) {
                    return true;
                }
            } else if (node instanceof ProjectBrokerCourseNode) {
                return false;// TODO:cg 28.01.2010 ProjectBroker : no assessment-tool in V1.0 return always false
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks recursivley a course structure or a part of it for assessable nodes or for structure course nodes (subtype of assessable node), which 'hasPassedConfigured'
     * or 'hasScoreConfigured' is true. If founds the first node that meets the criterias, it returns true.
     * 
     * @param node
     * @return boolean
     */
    public static boolean checkForAssessableNodes(final CourseNode node) {
        if (checkIfNodeIsAssessable(node)) {
            return true;
        }
        // check children now
        final int count = node.getChildCount();
        for (int i = 0; i < count; i++) {
            final CourseNode cn = (CourseNode) node.getChildAt(i);
            if (checkForAssessableNodes(cn)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get all assessable nodes including the root node (if assessable)
     * 
     * @param editorModel
     * @param excludeNode
     *            Node that should be excluded in the list, e.g. the current node or null if all assessable nodes should be used
     * @return List of assessable course nodes
     */
    public static List<CourseNode> getAssessableNodes(final CourseEditorTreeModel editorModel, final CourseNode excludeNode) {
        final CourseEditorTreeNode rootNode = (CourseEditorTreeNode) editorModel.getRootNode();
        final List<CourseNode> nodes = new ArrayList<CourseNode>();
        // visitor class: takes all assessable nodes if not the exclude node and
        // puts
        // them into the nodes list
        final Visitor visitor = new Visitor() {
            @Override
            public void visit(final INode node) {
                final CourseEditorTreeNode editorNode = (CourseEditorTreeNode) node;
                final CourseNode courseNode = editorModel.getCourseNode(node.getIdent());
                if (!editorNode.isDeleted() && (courseNode != excludeNode)) {
                    if (checkIfNodeIsAssessable(courseNode)) {
                        nodes.add(courseNode);
                    }
                }
            }
        };
        // not visit beginning at the root node
        final TreeVisitor tv = new TreeVisitor(visitor, rootNode, false);
        tv.visitAll();

        return nodes;
    }

    /**
     * @param score
     *            The score to be rounded
     * @return The rounded score for GUI presentation
     */
    public static String getRoundedScore(final Float score) {
        if (score == null) {
            return null;
        }
        return Formatter.roundToString(score.floatValue(), 3);
    }

    public static final String KEY_TYPE = "type";
    public static final String KEY_IDENTIFYER = "identifyer";
    public static final String KEY_INDENT = "indent";

    public static final String KEY_TITLE_SHORT = "short.title";
    public static final String KEY_TITLE_LONG = "long.title";
    public static final String KEY_PASSED = "passed";
    public static final String KEY_SCORE = "score";
    public static final String KEY_ATTEMPTS = "attempts";
    public static final String KEY_DETAILS = "details";
    public static final String KEY_SELECTABLE = "selectable";

    /**
     * Add all assessable nodes and the scoring data to a list. Each item in the list is an object array that has the following data:
     * 
     * @param recursionLevel
     * @param courseNode
     * @param userCourseEnv
     * @param discardEmptyNodes
     * @param discardComments
     * @return list of object arrays or null if empty
     */
    public static List<Map<String, Object>> addAssessableNodeAndDataToList(final int recursionLevel, final CourseNode courseNode,
            final UserCourseEnvironment userCourseEnv, final boolean discardEmptyNodes, final boolean discardComments) {
        // 1) Get list of children data using recursion of this method
        final List<Map<String, Object>> childrenData = new ArrayList<Map<String, Object>>(50);
        for (int i = 0; i < courseNode.getChildCount(); i++) {
            final CourseNode child = (CourseNode) courseNode.getChildAt(i);
            final List<Map<String, Object>> childData = addAssessableNodeAndDataToList((recursionLevel + 1), child, userCourseEnv, discardEmptyNodes, discardComments);
            if (childData != null) {
                childrenData.addAll(childData);
            }
        }

        // 2) Get data of this node only if
        // - it has any wrapped children or
        // - it is of an assessable course node type
        boolean hasDisplayableValuesConfigured = false;
        boolean hasDisplayableUserValues = false;
        if ((childrenData.size() > 0 || courseNode instanceof AssessableCourseNode) && !(courseNode instanceof ProjectBrokerCourseNode)) {
            // TODO:cg 04.11.2010 ProjectBroker : no assessment-tool in V1.0 , remove projectbroker completely form assessment-tool gui
            // Store node and user data in object array. This object array serves as data model for
            // the user assessment overview table
            final Map<String, Object> nodeData = new HashMap<String, Object>();
            // indent
            nodeData.put(KEY_INDENT, new Integer(recursionLevel));
            // course node data
            nodeData.put(KEY_TYPE, courseNode.getType());
            nodeData.put(KEY_TITLE_SHORT, courseNode.getShortTitle());
            nodeData.put(KEY_TITLE_LONG, courseNode.getLongTitle());
            nodeData.put(KEY_IDENTIFYER, courseNode.getIdent());

            if (courseNode instanceof AssessableCourseNode) {
                final AssessableCourseNode assessableCourseNode = (AssessableCourseNode) courseNode;
                final ScoreEvaluation scoreEvaluation = userCourseEnv.getScoreAccounting().getScoreEvaluation(courseNode);
                // details
                if (assessableCourseNode.hasDetails()) {
                    hasDisplayableValuesConfigured = true;
                    final String detailValue = assessableCourseNode.getDetailsListView(userCourseEnv);
                    if (detailValue == null) {
                        // ignore unset details in discardEmptyNodes mode
                        nodeData.put(KEY_DETAILS, AssessmentHelper.DETAILS_NA_VALUE);
                    } else {
                        nodeData.put(KEY_DETAILS, detailValue);
                        hasDisplayableUserValues = true;
                    }
                }
                // attempts
                if (assessableCourseNode.hasAttemptsConfigured()) {
                    hasDisplayableValuesConfigured = true;
                    final Integer attemptsValue = assessableCourseNode.getUserAttempts(userCourseEnv);
                    if (attemptsValue != null) {
                        nodeData.put(KEY_ATTEMPTS, attemptsValue);
                        if (attemptsValue.intValue() > 0) {
                            // ignore attempts = 0 in discardEmptyNodes mode
                            hasDisplayableUserValues = true;
                        }
                    }
                }
                // score
                if (assessableCourseNode.hasScoreConfigured()) {
                    hasDisplayableValuesConfigured = true;
                    final Float score = scoreEvaluation.getScore();
                    if (score != null) {
                        nodeData.put(KEY_SCORE, AssessmentHelper.getRoundedScore(score));
                        hasDisplayableUserValues = true;
                    }
                }
                // passed
                if (assessableCourseNode.hasPassedConfigured()) {
                    hasDisplayableValuesConfigured = true;
                    final Boolean passed = scoreEvaluation.getPassed();
                    if (passed != null) {
                        nodeData.put(KEY_PASSED, passed);
                        hasDisplayableUserValues = true;
                    }
                }
                // selection command available
                final AssessableCourseNode acn = (AssessableCourseNode) courseNode;
                if (acn.isEditableConfigured()) {
                    // Assessable course nodes are selectable
                    nodeData.put(KEY_SELECTABLE, Boolean.TRUE);
                } else {
                    // assessable nodes that do not have score or passed are not selectable
                    // (e.g. a st node with no defined rule
                    nodeData.put(KEY_SELECTABLE, Boolean.FALSE);
                }
                if (!hasDisplayableUserValues && assessableCourseNode.hasCommentConfigured() && !discardComments) {
                    // comments are invisible in the table but if configured the node must be in the list
                    // for the efficiency statement this can be ignored, this is the case when discardComments is true
                    hasDisplayableValuesConfigured = true;
                    if (assessableCourseNode.getUserUserComment(userCourseEnv) != null) {
                        hasDisplayableUserValues = true;
                    }
                }
            } else {
                // Not assessable nodes are not selectable. (e.g. a node that
                // has an assessable child node but is itself not assessable)
                nodeData.put(KEY_SELECTABLE, Boolean.FALSE);
            }
            // 3) Add data of this node to mast list if node assessable or children list has any data.
            // Do only add nodes when they have any assessable element, otherwhise discard (e.g. empty course,
            // structure nodes without scoring rules)! When the discardEmptyNodes flag is set then only
            // add this node when there is user data found for this node.
            if (childrenData.size() > 0 || (discardEmptyNodes && hasDisplayableValuesConfigured && hasDisplayableUserValues)
                    || (!discardEmptyNodes && hasDisplayableValuesConfigured)) {
                final List<Map<String, Object>> nodeAndChildren = new ArrayList<Map<String, Object>>();
                nodeAndChildren.add(nodeData);
                // 4) Add children data list to master list
                nodeAndChildren.addAll(childrenData);
                return nodeAndChildren;
            }
        }
        return null;
    }

    /**
     * Evaluates if the results are visble or not in respect of the configured CONFIG_KEY_DATE_DEPENDENT_RESULTS parameter. <br>
     * The results are always visible if no date dependent, or if date dependent only in the period: startDate-endDate. EndDate could be null, that is there is no
     * restriction for the end date.
     * 
     * @return true if is visible.
     */
    public static boolean isResultVisible(final ModuleConfiguration modConfig) {
        boolean isVisible = false;
        final Boolean showResultsActive = (Boolean) modConfig.get(IQEditController.CONFIG_KEY_DATE_DEPENDENT_RESULTS);
        if (showResultsActive != null && showResultsActive.booleanValue()) {
            final Date startDate = (Date) modConfig.get(IQEditController.CONFIG_KEY_RESULTS_START_DATE);
            final Date endDate = (Date) modConfig.get(IQEditController.CONFIG_KEY_RESULTS_END_DATE);
            final Date currentDate = new Date();
            if (currentDate.after(startDate) && (endDate == null || currentDate.before(endDate))) {
                isVisible = true;
            }
        } else {
            isVisible = true;
        }
        return isVisible;
    }

}
