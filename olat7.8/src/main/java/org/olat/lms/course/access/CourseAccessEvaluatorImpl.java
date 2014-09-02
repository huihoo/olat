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
package org.olat.lms.course.access;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.WikiCourseNode;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.course.run.userview.TreeEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.security.IdentityEnvironment;
import org.olat.presentation.course.run.navigation.NavigationHandler;
import org.olat.presentation.framework.core.components.tree.TreeNode;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 26.03.2012 <br>
 * 
 * @author Branislav Balaz
 */
@Component
public class CourseAccessEvaluatorImpl implements CourseAccessEvaluator {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    BaseSecurity baseSecurity;
    @Autowired
    RepositoryService repositoryService;

    /**
     * Is identity allowed to launch this course, and the root is visible and accessible.
     */
    @Override
    public boolean isCourseAccessibleForIdentity(Identity identity, RepositoryEntry courseRepositoryEntry, ICourse course, boolean evaluateExpertRules) {
        Roles identityRoles = baseSecurity.getRoles(identity);
        log.debug("isCourseAccesibleForIdentity");

        boolean isAllowedToLaunch = repositoryService.isAllowedToLaunch(identity, identityRoles, courseRepositoryEntry);
        commitDBImplTransaction();
        if (!isAllowedToLaunch) {
            return false;
        }

        final UserCourseEnvironment userCourseEnvironment = getUserCourseEnvironment(identity, course);
        NodeEvaluation courseRootNode = getNodeEvaluationIfNodeVisible(userCourseEnvironment, course, course.getRunStructure().getRootNode().getIdent(),
                evaluateExpertRules);
        commitDBImplTransaction();
        if (courseRootNode != null) {
            isAllowedToLaunch = courseRootNode.isVisible() && isCourseNodeAccessible(courseRootNode);
        } else {
            isAllowedToLaunch = false;
        }

        log.debug("isCourseAccesibleForIdentity - isAllowedToLaunch: " + isAllowedToLaunch);
        return isAllowedToLaunch;
    }

    @Override
    public boolean isCourseNodeVisibleForIdentity(Identity identity, ICourse course, String courseNodeId, boolean evaluateExpertRules) {
        final NodeEvaluation nodeEvaluation = getCourseNodeEvaluation(identity, course, courseNodeId, evaluateExpertRules);
        commitDBImplTransaction();
        if (nodeEvaluation != null) {
            return nodeEvaluation.isVisible();
        }
        return false;
    }

    /**
     * Is course node visible and accessible for this identity.
     */
    @Override
    public boolean isCourseNodeAccessibleForIdentity(Identity identity, ICourse course, CourseNode node, boolean evaluateExpertRules) {
        NodeEvaluation nodeEvaluation = getCourseNodeEvaluation(identity, course, node.getIdent(), evaluateExpertRules);
        commitDBImplTransaction();
        if (nodeEvaluation != null) {
            boolean isAccessible = isNodeAndAncestorsVisible(nodeEvaluation) && isCourseNodeCapabilityAccessible(nodeEvaluation)
                    && isCourseNodeAccessible(nodeEvaluation);
            log.debug("isCourseNodeAccesibleForIdentity - isAccessible: " + isAccessible);
            return isAccessible;
        }
        return false;
    }

    private NodeEvaluation getCourseNodeEvaluation(Identity identity, ICourse course, String courseNodeId, boolean evaluateExpertRules) {
        final UserCourseEnvironment userCourseEnvironment = getUserCourseEnvironment(identity, course);
        return getNodeEvaluationIfNodeVisible(userCourseEnvironment, course, courseNodeId, evaluateExpertRules);
    }

    private UserCourseEnvironment getUserCourseEnvironment(Identity identity, ICourse course) {
        IdentityEnvironment identityEnvironment = getIdentityEnvironment(identity);
        UserCourseEnvironment userCourseEnvironment = new UserCourseEnvironmentImpl(identityEnvironment, course.getCourseEnvironment());
        return userCourseEnvironment;
    }

    private IdentityEnvironment getIdentityEnvironment(Identity identity) {
        IdentityEnvironment identityEnvironment = new IdentityEnvironment();
        identityEnvironment.setIdentity(identity);
        Roles roles = baseSecurity.getRoles(identity);
        identityEnvironment.setRoles(roles);
        return identityEnvironment;
    }

    /**
     * Returns the nodeEvaluation if the treeEvaluation.getCorrespondingTreeNode(node) returns a not null treeNode.
     */
    private NodeEvaluation getNodeEvaluationIfNodeVisible(UserCourseEnvironment userCourseEnvironment, ICourse course, String courseNodeId, boolean evaluateExpertRules) {
        TreeEvaluation treeEvaluation = new TreeEvaluation();
        // OLAT-6756: in order to get a result from treeEvaluation.getCorrespondingTreeNode(node)
        // both rootNode and courseNode must be taken from the same structure
        CourseNode rootNode = course.getRunStructure().getRootNode();
        CourseNode courseNode = course.getRunStructure().getNode(courseNodeId);

        // this must be called before treeEvaluation.getCorrespondingTreeNode
        rootNode.eval(userCourseEnvironment.getConditionInterpreter(), treeEvaluation, evaluateExpertRules);

        // this returns a not null TreeNode, only if node and all its ancestors are visible
        final TreeNode newCalledTreeNode = treeEvaluation.getCorrespondingTreeNode(courseNode);
        if (newCalledTreeNode != null) {
            NodeEvaluation nodeEvaluation = (NodeEvaluation) newCalledTreeNode.getUserObject();
            return nodeEvaluation;
        }
        return null;
    }

    /**
     * This commits a transaction opened by DBImpl.beginTransaction(). <br/>
     * This was introduced as fix for frequent "Overdue resource check-out stack trace" error.
     */
    private void commitDBImplTransaction() {
        DBFactory.getInstance(false).intermediateCommit();
    }

    /**
     * TODO: fix the implementation, this only evaluates the current node visibility but not the ancestors
     */
    private boolean isNodeAndAncestorsVisible(NodeEvaluation nodeEvaluation) {
        return nodeEvaluation.isVisible();
    }

    private boolean isCourseNodeAccessible(NodeEvaluation nodeEvaluation) {
        return NavigationHandler.mayAccessWholeTreeUp(nodeEvaluation);
    }

    private boolean isCourseNodeCapabilityAccessible(NodeEvaluation nodeEvaluation) {
        // check wiki rules
        if (WikiCourseNode.TYPE.equals(nodeEvaluation.getCourseNode().getType())) {
            return nodeEvaluation.isCapabilityAccessible("access");
        }
        // TODO: check Forum,Blog,Podcast,File Dialog,Folder rules. At the moment for these elements is enough to check isCourseNodeAccessible (mayAccessWholeTreeUp) to
        // achieve behaviour like on GUI (not accessible).
        else {
            return true;
        }

    }
}
