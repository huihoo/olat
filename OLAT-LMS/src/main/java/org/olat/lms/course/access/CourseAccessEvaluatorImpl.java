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

    @Override
    public boolean isCourseNodeVisibleForIdentity(Identity identity, ICourse course, CourseNode node) {
        NodeEvaluation nodeEvaluation = getCourseNodeEvaluation(identity, course, node);
        commitDBImplTransaction();
        return nodeEvaluation.isVisible();
    }

    private NodeEvaluation getCourseNodeEvaluation(Identity identity, ICourse course, CourseNode node) {
        UserCourseEnvironment userCourseEnvironment = getUserCourseEnvironment(identity, course);
        return getNodeEvaulation(node, userCourseEnvironment);
    }

    private UserCourseEnvironment getUserCourseEnvironment(Identity identity, ICourse course) {
        IdentityEnvironment identityEnvironment = getIdentityEnvironment(identity);
        UserCourseEnvironment userCourseEnvironment = new UserCourseEnvironmentImpl(identityEnvironment, course.getCourseEnvironment());
        return userCourseEnvironment;
    }

    private NodeEvaluation getRootCourseNodeEvaluation(Identity identity, ICourse course) {
        UserCourseEnvironment userCourseEnvironment = getUserCourseEnvironment(identity, course);
        return getNodeEvaulation(userCourseEnvironment.getCourseEnvironment().getRunStructure().getRootNode(), userCourseEnvironment);
    }

    private NodeEvaluation getNodeEvaulation(CourseNode node, UserCourseEnvironment userCourseEnvironment) {
        TreeEvaluation treeEvaluation = new TreeEvaluation();
        NodeEvaluation nodeEvaluation = node.eval(userCourseEnvironment.getConditionInterpreter(), treeEvaluation);
        return nodeEvaluation;
    }

    private IdentityEnvironment getIdentityEnvironment(Identity identity) {
        IdentityEnvironment identityEnvironment = new IdentityEnvironment();
        identityEnvironment.setIdentity(identity);
        Roles roles = baseSecurity.getRoles(identity);
        identityEnvironment.setRoles(roles);
        return identityEnvironment;
    }

    /**
     * Is identity allowed to launch this course, and the root is visible and accessible.
     */
    @Override
    public boolean isCourseAccesibleForIdentity(Identity identity, RepositoryEntry courseRepositoryEntry, ICourse course) {
        Roles identityRoles = baseSecurity.getRoles(identity);
        NodeEvaluation courseRootNode = getRootCourseNodeEvaluation(identity, course);
        boolean isAllowedToLaunch = repositoryService.isAllowedToLaunch(identity, identityRoles, courseRepositoryEntry) && courseRootNode.isVisible()
                && isCourseNodeAccessible(courseRootNode);
        commitDBImplTransaction();
        log.debug("isCourseAccesibleForIdentity - isAllowedToLaunch: " + isAllowedToLaunch);
        return isAllowedToLaunch;
    }

    /**
     * This commits a transaction opened by DBImpl.beginTransaction(). <br/>
     * This was introduced as fix for frequent "Overdue resource check-out stack trace" error.
     */
    private void commitDBImplTransaction() {
        DBFactory.getInstance(false).commit();
    }

    /**
     * Is course node visible and accessible for this identity.
     */
    @Override
    public boolean isCourseNodeAccesibleForIdentity(Identity identity, ICourse course, CourseNode node) {
        NodeEvaluation nodeEvaluation = getCourseNodeEvaluation(identity, course, node);
        commitDBImplTransaction();
        boolean isAccessible = nodeEvaluation.isVisible() && isCourseNodeCapabilityAccessible(nodeEvaluation) && isCourseNodeAccessible(nodeEvaluation);
        log.debug("isCourseNodeAccesibleForIdentity - isAccessible: " + isAccessible);
        return isAccessible;
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
