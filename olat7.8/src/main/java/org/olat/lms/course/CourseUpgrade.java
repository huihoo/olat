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
 * Copyright (c) since 2007 at frentix GmbH, Switzerland, http://www.frentix.com
 * <p>
 */
package org.olat.lms.course;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.commons.tree.INode;
import org.olat.lms.commons.tree.TreeVisitor;
import org.olat.lms.commons.tree.Visitor;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.MSCourseNode;
import org.olat.lms.course.nodes.TACourseNode;
import org.olat.lms.course.tree.CourseEditorTreeModel;
import org.olat.presentation.framework.core.components.tree.TreeNode;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.StringHelper;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * Upgrade-Code for course will check for up-to-date editorTreeModel or runStructure first.
 * <P>
 * Initial Date: 17.07.2009 <br>
 * 
 * @author Roman Haag, www.frentix.com, roman.haag@frentix.com,
 */
public class CourseUpgrade {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String MS_TYPE = "ms";

    public CourseUpgrade() {
        //
    }

    public void migrateCourse(final ICourse course) {
        final PersistingCourseImpl ccourse = (PersistingCourseImpl) course;
        // only upgrade from version 1 => 2
        // this will migrate wiki-syntax to html
        final int migrateTargetVersion = 2;
        final CourseEditorTreeModel editorTreeModel = course.getEditorTreeModel();
        if (!editorTreeModel.isVersionUpToDate() && editorTreeModel.getVersion() != migrateTargetVersion) {
            upgradeEditorTreeModel(ccourse);
            editorTreeModel.setVersion(migrateTargetVersion);
            ccourse.setEditorTreeModel(editorTreeModel);
            ccourse.saveEditorTreeModel();
        }
        final Structure runStructure = course.getRunStructure();
        if (!runStructure.isVersionUpToDate() && runStructure.getVersion() != migrateTargetVersion) {
            upgradeRunStructure(ccourse);
            ccourse.getRunStructure().setVersion(migrateTargetVersion);
            ccourse.setRunStructure(runStructure);
            ccourse.saveRunStructure();
        }
    }

    private void upgradeRunStructure(final ICourse course) {
        final Structure cR = course.getRunStructure();
        final CourseNode rsRootNode = cR.getRootNode();
        final Set<String> allSubTreeids = new HashSet<String>();
        final TreeVisitor tv = new TreeVisitor(new Visitor() {
            @Override
            public void visit(final INode node) {
                allSubTreeids.add(node.getIdent());
            }
        }, rsRootNode, true);
        tv.visitAll();
        final Structure runStructure = course.getRunStructure();

        int nodeCounter = 0;
        for (final Iterator<String> iterator2 = allSubTreeids.iterator(); iterator2.hasNext();) {
            final String nodeId = iterator2.next();
            final CourseNode rsn = runStructure.getNode(nodeId);

            // migrate if this node is a Task
            if (rsn.getType().equals(TACourseNode.CONF_TASK_TYPE)) {
                migrateSingleTask(rsn);
            }
            // migrate no access text for every node:
            migrateSingleAccessDeniedExplanation(rsn);

            // migrate assessment nodes
            if (rsn.getType().equals(MS_TYPE)) {
                migrateSingleAssessment(rsn);
            }

            // migrate course node description
            migrateSingleNodeDesc(rsn);

            nodeCounter++;
        }

        log.info("Audit:**** Lazy migration finished for runStructure of course " + course.getResourceableId() + " with a total of " + nodeCounter
                + " course node descriptions. ****", null);

    }

    private void upgradeEditorTreeModel(final ICourse course) {
        // EDITOR: get all course nodes
        final CourseEditorTreeModel cT = course.getEditorTreeModel();
        final TreeNode rootNode = cT.getRootNode();
        final Set<String> allSubTreeids = new HashSet<String>();
        final TreeVisitor tv = new TreeVisitor(new Visitor() {
            @Override
            public void visit(final INode node) {
                allSubTreeids.add(node.getIdent());
            }
        }, rootNode, true);
        tv.visitAll();

        final CourseEditorTreeModel editorTreeModel = course.getEditorTreeModel();
        // EDITOR: loop all course nodes
        int nodeCounter = 0;
        for (final Iterator<String> iterator2 = allSubTreeids.iterator(); iterator2.hasNext();) {
            final String nodeId = iterator2.next();
            final CourseNode cetn = editorTreeModel.getCourseNode(nodeId);
            // migrate if this node is a Task
            if (cetn.getType().equals(TACourseNode.CONF_TASK_TYPE)) {
                migrateSingleTask(cetn);
            }
            // migrate no access text for every node:
            migrateSingleAccessDeniedExplanation(cetn);

            // migrate assessment nodes
            if (cetn.getType().equals(MS_TYPE)) {
                migrateSingleAssessment(cetn);
            }

            // migrate course node description
            migrateSingleNodeDesc(cetn);

            nodeCounter++;

        } // for

        log.info("Audit:**** Lazy migration finished for editorTreeModel of course " + course.getResourceableId() + " with a total of " + nodeCounter
                + " course node descriptions. ****", null);
    }

    private void migrateSingleTask(final CourseNode node) {
        final ModuleConfiguration taskConf = node.getModuleConfiguration();
        final String[] allKeys = new String[] { TACourseNode.CONF_TASK_TEXT, MSCourseNode.CONFIG_KEY_INFOTEXT_USER, MSCourseNode.CONFIG_KEY_INFOTEXT_COACH };
        for (int i = 0; i < allKeys.length; i++) {
            final String thisKey = allKeys[i];
            final String oldDesc = (String) taskConf.get(thisKey);
            if (StringHelper.containsNonWhitespace(oldDesc)) {
                final String newDesc = Formatter.formatWikiMarkup(oldDesc);
                taskConf.set(thisKey, newDesc);
            }
        }
    }

    private void migrateSingleAccessDeniedExplanation(final CourseNode node) {
        final String oldDesc = node.getNoAccessExplanation();
        if (StringHelper.containsNonWhitespace(oldDesc)) {
            final String newDesc = Formatter.formatWikiMarkup(oldDesc);
            node.setNoAccessExplanation(newDesc);
        }
    }

    private void migrateSingleAssessment(final CourseNode node) {
        final ModuleConfiguration modConfig = node.getModuleConfiguration();
        final String infoUser = (String) modConfig.get(MSCourseNode.CONFIG_KEY_INFOTEXT_USER);
        final String newInfoUser = Formatter.formatWikiMarkup(infoUser);
        modConfig.set(MSCourseNode.CONFIG_KEY_INFOTEXT_USER, newInfoUser);
        final String infoCoach = (String) modConfig.get(MSCourseNode.CONFIG_KEY_INFOTEXT_COACH);
        final String newInfoCoach = Formatter.formatWikiMarkup(infoCoach);
        modConfig.set(MSCourseNode.CONFIG_KEY_INFOTEXT_COACH, newInfoCoach);
    }

    private void migrateSingleNodeDesc(final CourseNode node) {
        final String oldDesc = node.getLearningObjectives();
        if (StringHelper.containsNonWhitespace(oldDesc)) {
            final String newDesc = Formatter.formatWikiMarkup(oldDesc);
            node.setLearningObjectives(newDesc);
        }
    }
}
