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

package org.olat.lms.course.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org._3pq.jgrapht.DirectedGraph;
import org._3pq.jgrapht.Edge;
import org._3pq.jgrapht.alg.CycleDetector;
import org._3pq.jgrapht.edge.EdgeFactories;
import org._3pq.jgrapht.edge.EdgeFactories.DirectedEdgeFactory;
import org._3pq.jgrapht.graph.DefaultDirectedGraph;
import org.olat.data.group.area.BGArea;
import org.olat.lms.commons.tree.INode;
import org.olat.lms.commons.tree.TreeVisitor;
import org.olat.lms.commons.tree.Visitor;
import org.olat.lms.course.assessment.AssessmentHelper;
import org.olat.lms.course.condition.interpreter.ConditionErrorMessage;
import org.olat.lms.course.condition.interpreter.ConditionExpression;
import org.olat.lms.course.condition.interpreter.ConditionInterpreter;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.ENCourseNode;
import org.olat.lms.course.tree.CourseEditorTreeModel;
import org.olat.lms.course.tree.CourseEditorTreeNode;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.lms.repository.RepositoryService;
import org.olat.presentation.course.editor.StatusDescription;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * TODO: guido Class Description for CourseEditorEnvImpl
 */
public class CourseEditorEnvImpl implements CourseEditorEnv {
    /**
     * the course editor tree model used in this editing session, exist only once per open course editor
     */
    private final CourseEditorTreeModel cetm;
    String currentCourseNodeId = null;
    /**
     * the course group manager is used for answering the existXXX questions concering, groups and areas
     */
    private final CourseGroupManager cgm;
    /**
     * the editor locale, it is used in the condition interpreter to provide localized error messages.
     */
    private final Locale editorLocale;
    /**
     * book keeping of (coursNodeId, {conditionexpression,conditionexpression,...}) TODO: do we really need the information splitted up by category and condition
     * expression?
     */
    Map softRefs = new HashMap();
    /**
     * book keeping of (courseNodeId, StatusDescription)
     */
    Map statusDescs = new HashMap();
    /**
     * current active condition expression, it is activated by a call to <code>validateConditionExpression(..)</code> the condition interpreter is then asked for
     * validating the expression. This validation parses the expression into the atomic functions etc, which in turn access the <code>CourseEditorEnvImpl</code> to
     * <code>pushError()</code> and <code>addSoftReference()</code>.
     */
    ConditionExpression currentConditionExpression = null;
    /**
     * different organized info as in softRefs: (nodeId,{nodeid,nodeId,...})
     */
    Map<String, Set<String>> nodeRefs = new HashMap<String, Set<String>>();
    /**
     * the condition interpreter for evaluating the condtion expressions.
     */
    ConditionInterpreter ci = null;
    /**
     * the olatresourceable from the course
     */
    private OLATResourceable ores;

    public CourseEditorEnvImpl(final OLATResourceable courseOres, final CourseEditorTreeModel cetm, final CourseGroupManager cgm, final Locale editorLocale) {
        this.cetm = cetm;
        this.cgm = cgm;
        this.editorLocale = editorLocale;
        this.ores = courseOres;
    }

    /**
     * @param ci
     */
    @Override
    public void setConditionInterpreter(final ConditionInterpreter ci) {
        this.ci = ci;
    }

    @Override
    public OLATResourceable getCourseOlatResourceable() {
        return this.ores;
    }

    /**
	 */
    @Override
    public boolean isEnrollmentNode(final String nodeId) {
        final CourseEditorTreeNode cen = cetm.getCourseEditorNodeById(nodeId);
        if (cen == null) {
            return false;
        }
        if (cen.isDeleted()) {
            return false;
        }
        // node exists and is not marked as deleted, check the associated course
        // node correct type
        return (cen.getCourseNode() instanceof ENCourseNode);
    }

    /**
	 */
    @Override
    public boolean isAssessable(final String nodeId) {
        final CourseEditorTreeNode cen = cetm.getCourseEditorNodeById(nodeId);
        if (cen == null) {
            return false;
        }
        if (cen.isDeleted()) {
            return false;
        }
        // node exists and is not marked as deleted, check the associated course
        // node for assessability.
        return AssessmentHelper.checkIfNodeIsAssessable(cen.getCourseNode());
    }

    /**
	 */
    @Override
    public boolean existsNode(final String nodeId) {
        final CourseEditorTreeNode cen = cetm.getCourseEditorNodeById(nodeId);
        final boolean retVal = cen != null && !cen.isDeleted();
        return retVal;
    }

    /**
	 */
    @Override
    public boolean existsGroup(final String groupname) {
        // FIXME:fg:b improve performance by adding a special query for the existence
        // check!
        final List cnt = cgm.getLearningGroupsFromAllContexts(groupname, this.ores);
        return (cnt != null && cnt.size() > 0);
    }

    @Override
    public boolean existsRightGroup(final String groupname) {
        final List cnt = cgm.getRightGroupsFromAllContexts(groupname, this.ores);
        return (cnt != null && cnt.size() > 0);
    }

    /**
	 */
    @Override
    public boolean existsArea(final String areaname) {
        // FIXME:fg:b improve performance by adding a special query for the existence
        // check!
        final List cnt = cgm.getAllAreasFromAllContexts(this.ores);
        for (final Iterator iter = cnt.iterator(); iter.hasNext();) {
            final BGArea element = (BGArea) iter.next();
            if (element.getName().equals(areaname)) {
                return true;
            }
        }
        return false;
    }

    /**
	 */
    @Override
    public String getCurrentCourseNodeId() {
        return currentCourseNodeId;
    }

    /**
	 */
    @Override
    public void setCurrentCourseNodeId(final String courseNodeId) {
        this.currentCourseNodeId = courseNodeId;
    }

    /**
	 */
    @Override
    public Locale getEditorEnvLocale() {
        return editorLocale;
    }

    /**
	 */
    @Override
    public ConditionErrorMessage[] validateConditionExpression(final ConditionExpression condExpr) {
        // first set the active condition expression, which will be accessed from
        // the conditions functions inserting soft references
        currentConditionExpression = condExpr;
        if (condExpr.getExptressionString() == null) {
            return null;
        }
        // evaluate expression
        final ConditionErrorMessage[] cems = ci.syntaxTestExpression(condExpr);
        if (softRefs.containsKey(this.currentCourseNodeId)) {
            final List condExprs = (ArrayList) softRefs.get(this.currentCourseNodeId);
            for (final Iterator iter = condExprs.iterator(); iter.hasNext();) {
                final ConditionExpression element = (ConditionExpression) iter.next();
                if (element.getId().equals(currentConditionExpression.getId())) {
                    condExprs.remove(element);
                    break;
                }
            }
            condExprs.add(currentConditionExpression);
        } else {
            final List condExprs = new ArrayList();
            condExprs.add(currentConditionExpression);
            softRefs.put(currentCourseNodeId, condExprs);
        }

        //
        return cems;
    }

    /**
	 */
    @Override
    public void addSoftReference(final String category, final String softReference) {
        currentConditionExpression.addSoftReference(category, softReference);

    }

    /**
	 */
    @Override
    public void pushError(final Exception e) {
        currentConditionExpression.pushError(e);
    }

    /**
	 */
    @Override
    public void validateCourse() {
        /*
         * collect all condition error messages and soft references collect all configuration errors.
         */
        final String currentNodeWas = currentCourseNodeId;
        // reset all
        softRefs = new HashMap();
        nodeRefs = new HashMap<String, Set<String>>();
        Visitor v = new CollectConditionExpressionsVisitor();
        (new TreeVisitor(v, cetm.getRootNode(), true)).visitAll();
        for (final Iterator iter = softRefs.keySet().iterator(); iter.hasNext();) {
            final String nodeId = (String) iter.next();
            final List conditionExprs = (List) softRefs.get(nodeId);
            for (int i = 0; i < conditionExprs.size(); i++) {
                final ConditionExpression ce = (ConditionExpression) conditionExprs.get(i);
                // DO NOT validateConditionExpression(ce) as this is already done in the
                // CollectConditionExpressionsVisitor
                final Set<String> refs = new HashSet<String>(ce.getSoftReferencesOf("courseNodeId"));
                if (refs != null && refs.size() > 0) {
                    final Set<String> oldOnes = nodeRefs.put(nodeId, null);
                    if (oldOnes != null) {
                        refs.addAll(oldOnes);
                    }
                    nodeRefs.put(nodeId, refs);
                }
            }

        }
        // refresh,create status descriptions of the course
        statusDescs = new HashMap();
        v = new CollectStatusDescriptionVisitor(this);
        (new TreeVisitor(v, cetm.getRootNode(), true)).visitAll();
        //
        currentCourseNodeId = currentNodeWas;
    }

    /**
	 */
    @Override
    public StatusDescription[] getCourseStatus() {
        String[] a = new String[statusDescs.keySet().size()];
        a = (String[]) statusDescs.keySet().toArray(a);
        Arrays.sort(a);
        final List all2gether = new ArrayList();
        for (int i = a.length - 1; i >= 0; i--) {
            all2gether.addAll((List) statusDescs.get(a[i]));
        }
        StatusDescription[] retVal = new StatusDescription[all2gether.size()];
        retVal = (StatusDescription[]) all2gether.toArray(retVal);
        return retVal;
    }

    @Override
    public List getReferencingNodeIdsFor(final String ident) {
        final List refNodes = new ArrayList();
        for (final Iterator iter = nodeRefs.keySet().iterator(); iter.hasNext();) {
            final String nodeId = (String) iter.next();
            if (!nodeId.equals(ident)) {
                // self references are catched during form entering
                final Set refs = nodeRefs.get(nodeId);
                if (refs.contains(ident)) {
                    // nodeId references ident
                    refNodes.add(nodeId);
                }
            }
        }
        return refNodes;
    }

    @Override
    public String toString() {
        String retVal = "";
        final Set keys = softRefs.keySet();
        for (final Iterator iter = keys.iterator(); iter.hasNext();) {
            final String nodId = (String) iter.next();
            retVal += "nodeId:" + nodId + "\n";
            final List conditionExprs = (List) softRefs.get(nodId);
            for (final Iterator iterator = conditionExprs.iterator(); iterator.hasNext();) {
                final ConditionExpression ce = (ConditionExpression) iterator.next();
                retVal += "\t" + ce.toString() + "\n";
            }
            retVal += "\n";
        }
        return retVal;
    }

    class CollectStatusDescriptionVisitor implements Visitor {
        private final CourseEditorEnv cev;

        public CollectStatusDescriptionVisitor(final CourseEditorEnv cev) {
            this.cev = cev;
        }

        /**
		 */
        @Override
        public void visit(final INode node) {
            /**
             * collect only status descriptions of not deleted nodes
             */
            final CourseEditorTreeNode tmp = (CourseEditorTreeNode) node;
            if (!tmp.isDeleted()) {
                final CourseNode cn = tmp.getCourseNode();
                final String key = cn.getIdent();
                final StatusDescription[] allSds = cn.isConfigValid(cev);
                if (allSds.length > 0) {
                    for (int i = 0; i < allSds.length; i++) {
                        final StatusDescription sd = allSds[i];
                        if (sd != StatusDescription.NOERROR) {
                            if (!statusDescs.containsKey(key)) {
                                statusDescs.put(key, new ArrayList());
                            }
                            final List sds = (List) statusDescs.get(key);
                            sds.add(sd);
                        }
                    }
                }
            }
        }

    }

    class CollectConditionExpressionsVisitor implements Visitor {
        /**
		 */
        @Override
        public void visit(final INode node) {
            /**
             * collect condition expressions only for not deleted nodes
             */
            final CourseEditorTreeNode tmp = (CourseEditorTreeNode) node;
            final CourseNode cn = tmp.getCourseNode();
            final String key = cn.getIdent();
            final List condExprs = cn.getConditionExpressions();
            if (condExprs.size() > 0 && !tmp.isDeleted()) {
                // evaluate each expression
                for (final Iterator iter = condExprs.iterator(); iter.hasNext();) {
                    final ConditionExpression ce = (ConditionExpression) iter.next();
                    currentCourseNodeId = key;
                    currentConditionExpression = ce;
                    ci.syntaxTestExpression(ce);
                }
                // add it to the cache.
                softRefs.put(key, condExprs);
            }
        }

    }

    class Convert2DGVisitor implements Visitor {
        private final DirectedEdgeFactory def;
        private final DirectedGraph dg;

        public Convert2DGVisitor(final DirectedGraph dg) {
            this.dg = dg;
            def = new EdgeFactories.DirectedEdgeFactory();
        }

        @Override
        public void visit(final INode node) {
            final CourseEditorTreeNode tmp = (CourseEditorTreeNode) node;
            final CourseNode cn = tmp.getCourseNode();
            final String key = cn.getIdent();
            dg.addVertex(key);
            /*
             * add edge from parent to child. This directed edge represents the visibility accessability inheritance direction.
             */
            final INode parent = tmp.getParent();
            if (parent != null) {
                dg.addVertex(parent.getIdent());
                final Edge toParent = def.createEdge(parent.getIdent(), key);
                dg.addEdge(toParent);
            }
        }

    }

    /**
	 */
    @Override
    public Set<String> listCycles() {
        /*
         * convert nodeRefs datastructure to a directed graph
         */
        final DirectedGraph dg = new DefaultDirectedGraph();
        final DirectedEdgeFactory def = new EdgeFactories.DirectedEdgeFactory();
        /*
         * add the course structure as directed graph, where
         */
        final Visitor v = new Convert2DGVisitor(dg);
        (new TreeVisitor(v, cetm.getRootNode(), true)).visitAll();
        /*
         * iterate over nodeRefs, add each not existing node id as vertex, for each key - child relation add an edge to the directed graph.
         */
        final Iterator<String> keys = nodeRefs.keySet().iterator();
        while (keys.hasNext()) {
            // a node
            final String key = keys.next();
            if (!dg.containsVertex(key)) {
                dg.addVertex(key);
            }
            // and its children
            final Set<String> children = nodeRefs.get(key);
            final Iterator<String> childrenIt = children.iterator();
            while (childrenIt.hasNext()) {
                final String child = childrenIt.next();
                if (!dg.containsVertex(child)) {
                    dg.addVertex(child);
                }
                // add edge, precondition: vertex key - child are already added to the graph
                final Edge de = def.createEdge(key, child);
                dg.addEdge(de);
            }
        }
        /*
         * find the id's participating in the cycle, and return the intersection with set of id's which actually produce references.
         */
        final CycleDetector cd = new CycleDetector(dg);
        final Set<String> cycleIds = cd.findCycles();
        cycleIds.retainAll(nodeRefs.keySet());
        return cycleIds;
    }

    /**
     * @return CourseGroupManager for this course environment
     */
    @Override
    public CourseGroupManager getCourseGroupManager() {
        return cgm;
    }

    @Override
    public Long getRepositoryEntryId() {
        return getRepositoryService().getRepositoryEntryIdFromResourceable(ores.getResourceableId(), "CourseModule");
    }

    private RepositoryService getRepositoryService() {
        return CoreSpringFactory.getBean(RepositoryService.class);
    }

}
