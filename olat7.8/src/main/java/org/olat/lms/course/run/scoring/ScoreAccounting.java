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

package org.olat.lms.course.run.scoring;

import java.util.HashMap;
import java.util.Map;

import org.olat.lms.commons.tree.INode;
import org.olat.lms.commons.tree.TreeVisitor;
import org.olat.lms.commons.tree.Visitor;
import org.olat.lms.course.nodes.AssessableCourseNode;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.system.exception.OLATRuntimeException;

/**
 * Description:<BR/>
 * The score accounting contains all score evaluations for a user
 * <P/>
 * Initial Date: Oct 12, 2004
 * 
 * @author Felix Jost
 */
public class ScoreAccounting implements Visitor {
    private final UserCourseEnvironment userCourseEnvironment;

    private boolean error;
    private boolean childNotFoundError;
    private boolean childNotOfAssessableTypeError;
    private CourseNode evaluatingCourseNode;
    private String wrongChildID;

    private final Map cachedScoreEvals = new HashMap();
    private int recursionCnt;

    /**
     * Constructor of the user score accounting object
     * 
     * @param userCourseEnvironment
     */
    public ScoreAccounting(final UserCourseEnvironment userCourseEnvironment) {
        this.userCourseEnvironment = userCourseEnvironment;
    }

    /**
     * Retrieve all the score evaluations for all course nodes
     */
    public void evaluateAll() {
        cachedScoreEvals.clear();
        recursionCnt = 0;
        // collect all assessable nodes and eval 'em
        final CourseNode root = userCourseEnvironment.getCourseEnvironment().getRunStructure().getRootNode();
        // breadth first traversal gives an easier order of evaluation for debugging
        // however, for live it is absolutely mandatory to use depth first since using breadth first
        // the score accoutings local cache hash map will never be used. this can slow down things like
        // crazy (course with 10 tests, 300 users and some crazy score and passed calculations will have
        // 10 time performance differences)
        final TreeVisitor tv = new TreeVisitor(this, root, true); // true=depth first
        tv.visitAll();
    }

    /**
     * FIXME:fj: cmp this method and evalCourseNode Get the score evaluation for a given course node
     * 
     * @param courseNode
     * @return The score evaluation
     */
    public ScoreEvaluation getScoreEvaluation(final CourseNode courseNode) {
        ScoreEvaluation se = null;
        if (courseNode instanceof AssessableCourseNode) {
            final AssessableCourseNode acn = (AssessableCourseNode) courseNode;
            se = acn.getUserScoreEvaluation(userCourseEnvironment);
        }
        return se;
    }

    /**
     * evals the coursenode or simply returns the evaluation from the cache
     * 
     * @param cn
     * @return ScoreEvaluation
     */
    public ScoreEvaluation evalCourseNode(final AssessableCourseNode cn) {
        // make sure we have no circular calculations
        recursionCnt++;
        if (recursionCnt > 15) {
            throw new OLATRuntimeException("scoreaccounting.stackoverflow", new String[] { cn.getIdent(), cn.getShortTitle() },
                    PackageUtil.getPackageName(ScoreAccounting.class), "stack overflow in scoreaccounting, probably circular logic: acn =" + cn.toString(), null);
        }

        ScoreEvaluation se = (ScoreEvaluation) cachedScoreEvals.get(cn);
        if (se == null) { // result of this node has not been calculated yet, do it
            se = cn.getUserScoreEvaluation(userCourseEnvironment);
            cachedScoreEvals.put(cn, se);
            // System.out.println("cn eval: "+cn+" = "+ se);
        }
        recursionCnt--;
        return se;
    }

    /**
     * ----- to called by getScoreFunction only -----
     * 
     * @param childId
     * @return Float
     */
    public Float evalScoreOfCourseNode(final String childId) {
        final CourseNode foundNode = findChildByID(childId);
        if (foundNode == null) {
            error = true;
            childNotFoundError = true;
            wrongChildID = childId;
            return new Float(-9999999.0f);
        }
        if (!(foundNode instanceof AssessableCourseNode)) {
            error = true;
            childNotOfAssessableTypeError = true;
            wrongChildID = childId;
            return new Float(-1111111.0f);
        }
        final AssessableCourseNode acn = (AssessableCourseNode) foundNode;
        final ScoreEvaluation se = evalCourseNode(acn);
        if (se == null) { // the node could not provide any sensible information on scoring. e.g. a STNode with no calculating rules
            final String msg = "could not evaluate node " + acn.getShortTitle() + " (" + acn.getIdent() + ")" + "; called by node "
                    + (evaluatingCourseNode == null ? "n/a" : evaluatingCourseNode.getShortTitle() + " (" + evaluatingCourseNode.getIdent() + ")");
            new OLATRuntimeException(ScoreAccounting.class, "scoreaccounting.evaluationerror.score", new String[] { acn.getIdent(), acn.getShortTitle() },
                    PackageUtil.getPackageName(ScoreAccounting.class), msg, null);
        }
        Float score = se.getScore();
        if (score == null) { // a child has no score yet
            score = new Float(0.0f); // default to 0.0, so that the condition can be evaluated (zero points makes also the most sense for "no results yet", if to be
                                     // expressed in a number)
        }
        return score;
    }

    /**
     * ----- to be called by getPassedFunction only -----
     * 
     * @param childId
     * @return Boolean
     */
    public Boolean evalPassedOfCourseNode(final String childId) {
        final CourseNode foundNode = findChildByID(childId);
        if (foundNode == null) {
            error = true;
            childNotFoundError = true;
            wrongChildID = childId;
            return Boolean.FALSE;
        }
        if (!(foundNode instanceof AssessableCourseNode)) {
            error = true;
            childNotOfAssessableTypeError = true;
            wrongChildID = childId;
            return Boolean.FALSE;
        }
        final AssessableCourseNode acn = (AssessableCourseNode) foundNode;
        final ScoreEvaluation se = evalCourseNode(acn);
        if (se == null) { // the node could not provide any sensible information on scoring. e.g. a STNode with no calculating rules
            final String msg = "could not evaluate node '" + acn.getShortTitle() + "' (" + acn.getClass().getName() + "," + childId + ")";
            new OLATRuntimeException(ScoreAccounting.class, "scoreaccounting.evaluationerror.score", new String[] { acn.getIdent(), acn.getShortTitle() },
                    PackageUtil.getPackageName(ScoreAccounting.class), msg, null);
        }
        Boolean passed = se.getPassed();
        if (passed == null) { // a child has no "Passed" yet
            passed = Boolean.FALSE;
        }
        return passed;
    }

    /**
     * Change the score information for the given course node
     * 
     * @param acn
     * @param se
     */
    public void scoreInfoChanged(final AssessableCourseNode acn, final ScoreEvaluation se) {

        // FIXME:fj:b use cache infos
        /*
         * // either add a new entry if this is the first score that is provided, // or overwrite the entry in the cache if a scoreeval existed cachedScoreEvals.put(cn,
         * se); // go up the ladder and force each parent to recalulate the score while ((cn = (CourseNode) cn.getParent()) != null) { ScoreEvaluation sceval =
         * cn.evalScore(userCourseEnvironment); cachedScoreEvals.put(cn, sceval); }
         */
        // System.out.println("scoreInfoChanged - calc anew:\n"+cachedScoreEvals.toString());
        evaluateAll();
    }

    private CourseNode findChildByID(final String id) {
        final CourseNode foundNode = userCourseEnvironment.getCourseEnvironment().getRunStructure().getNode(id);
        return foundNode;
    }

    /**
     * used for error msg and debugging. denotes the coursenode which started a calculation. when an error occurs, we know which coursenode contains a faulty formula
     * 
     * @param evaluatingCourseNode
     */
    public void setEvaluatingCourseNode(final CourseNode evaluatingCourseNode) {
        this.evaluatingCourseNode = evaluatingCourseNode;
    }

    /**
	 */
    @Override
    public void visit(final INode node) {
        final CourseNode cn = (CourseNode) node;
        if (cn instanceof AssessableCourseNode) {
            final AssessableCourseNode acn = (AssessableCourseNode) cn;
            evalCourseNode(acn);
            // evalCourseNode will cache all infos
        }
        // else: non assessable nodes are not interesting here
    }

    /**
     * @return true if an error occured
     */
    public boolean isError() {
        return error;
    }

    /**
     * @return CourseNode
     */
    public CourseNode getEvaluatingCourseNode() {
        return evaluatingCourseNode;
    }

    /**
     * @return int
     */
    public int getRecursionCnt() {
        return recursionCnt;
    }

    /**
     * @return String
     */
    public String getWrongChildID() {
        return wrongChildID;
    }
}
