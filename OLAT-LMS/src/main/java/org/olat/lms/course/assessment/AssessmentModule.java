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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.CourseModule;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.Structure;
import org.olat.presentation.course.editor.PublishEvent;
import org.olat.system.commons.configuration.Destroyable;
import org.olat.system.commons.configuration.Initializable;
import org.olat.system.event.Event;
import org.olat.system.event.GenericEventListener;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * This is a PublishEvent listener, and triggers the update of the EfficiencyStatements for the published course. It only considers the events from the same JVM.
 * <P>
 * Initial Date: 11.08.2006 <br>
 * 
 * @author patrickb
 */
public class AssessmentModule implements Initializable, Destroyable, GenericEventListener {

    private static final Logger log = LoggerHelper.getLogger();

    private static int DEFAULT_POOLSIZE = 3;
    /*
     * worker pool for updating effciency statements
     */
    private ExecutorService updateESPool;
    private List upcomingWork;
    private final CourseModule courseModule;

    /**
     * [used by spring]
     */
    private AssessmentModule(final CourseModule courseModule) {
        this.courseModule = courseModule;
    }

    /**
	 */
    @Override
    public void init() {
        /*
         * init Worker pool
         */
        final ThreadFactory4UpdateEfficiencyWorker th4uew = new ThreadFactory4UpdateEfficiencyWorker();
        updateESPool = Executors.newFixedThreadPool(DEFAULT_POOLSIZE, th4uew);
        upcomingWork = new ArrayList();
        /*
         * always last step, register for course events
         */
        CourseModule.registerForCourseType(this, null);
        /*
         * no more code after here!
         */
    }

    /**
	 */
    @Override
    public void destroy() {
        /*
         * first step in destroy, deregister for course events
         */
        // no longer listen to changes
        CourseModule.deregisterForCourseType(this);
        /*
         * no other code before here!
         */
        // wait for all work being done
        updateESPool.shutdown();
        // check that working queue is empty
        if (upcomingWork.size() > 0) {
            // hanging work!!
            log.warn("still some Efficiency Statement recalculations open!!");
        }

        //
    }

    /**
     * Called at course publish.
     * 
     */
    @Override
    public void event(final Event event) {
        if (event instanceof PublishEvent) {
            final PublishEvent pe = (PublishEvent) event;
            // FIXME: LD: temporary introduced the (pe.getCommand() == PublishEvent.EVENT_IDENTIFIER) to filter the events from the same VM
            if (pe.getState() == PublishEvent.PRE_PUBLISH && pe.getEventIdentifier() == PublishEvent.EVENT_IDENTIFIER) {
                // PRE PUBLISH -> check node for changes
                addToUpcomingWork(pe);
                return;
            } else if (pe.getState() == PublishEvent.PUBLISH && pe.getEventIdentifier() == PublishEvent.EVENT_IDENTIFIER) {
                // a publish event, check if it matches a previous checked
                boolean recalc = false;
                final Long resId = pe.getPublishedCourseResId();
                synchronized (upcomingWork) { // o_clusterOK by:ld synchronized OK - only one cluster node must update the EfficiencyStatements (the course is locked for
                                              // editing) (same as e.g. file indexer)
                    recalc = upcomingWork.contains(resId);
                    if (recalc) {
                        upcomingWork.remove(resId);
                    }
                }
                if (recalc) {
                    final ICourse pubCourse = CourseFactory.loadCourse(pe.getPublishedCourseResId());
                    final UpdateEfficiencyStatementsWorker worker = new UpdateEfficiencyStatementsWorker(pubCourse);
                    updateESPool.execute(worker);
                }
            }
        }

    }

    /**
     * @param pe
     */
    private void addToUpcomingWork(final PublishEvent pe) {
        final ICourse course = CourseFactory.loadCourse(pe.getPublishedCourseResId());
        final boolean courseEfficiencyEnabled = course.getCourseEnvironment().getCourseConfig().isEfficencyStatementEnabled();
        if (!courseEfficiencyEnabled) {
            // no efficiency enabled, stop here.
            return;
        }
        // deleted + inserted + modified node ids -> changedNodeIds
        final Set changedNodeIds = pe.getDeletedCourseNodeIds();
        changedNodeIds.addAll(pe.getInsertedCourseNodeIds());
        changedNodeIds.addAll(pe.getModifiedCourseNodeIds());
        //
        boolean courseAssessmentChanged = false;
        final Structure courseRun = course.getRunStructure();
        for (final Iterator iter = changedNodeIds.iterator(); iter.hasNext();) {
            final String nodeId = (String) iter.next();
            final boolean wasNodeAsessable = AssessmentHelper.checkIfNodeIsAssessable(courseRun.getNode(nodeId));
            final boolean isNodeAssessable = AssessmentHelper.checkIfNodeIsAssessable(course.getEditorTreeModel().getCourseNode(nodeId));
            // if node was or became assessable
            if (wasNodeAsessable || isNodeAssessable) {
                courseAssessmentChanged = true;
                break;
            }
        }
        if (!courseAssessmentChanged) {
            // assessment changes detected, stop here
            return;
        }
        synchronized (upcomingWork) { // o_clusterOK by:ld synchronized OK - only one cluster node must update the EfficiencyStatements (the course is locked for editing)
            upcomingWork.add(course.getResourceableId());
        }
        return;
    }

    public class ThreadFactory4UpdateEfficiencyWorker implements ThreadFactory {

        /**
		 */
        @Override
        public Thread newThread(final Runnable r) {
            final Thread th = new Thread(r);
            th.setName("UpdateEfficiencyStatements");
            // kill this thread if OLAT is no longer active
            th.setDaemon(true);
            return th;
        }

    }

    /**
	 */
    @Override
    public boolean isControllerAndNotDisposed() {
        return false;
    }

}
