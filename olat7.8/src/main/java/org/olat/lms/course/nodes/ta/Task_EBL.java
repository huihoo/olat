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
package org.olat.lms.course.nodes.ta;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.data.properties.PropertyImpl;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.TACourseNode;
import org.olat.lms.course.properties.CoursePropertyManager;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.system.exception.AssertException;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 31.08.2011 <br>
 * 
 * @author guretzki
 */
@Component
public class Task_EBL {
    /** Property key for "assigned" property. */
    public static final String PROP_ASSIGNED = "ass";
    public static final String PROP_SAMPLED = "smpl";

    public String getAssignedTask(final Identity identity, final CourseEnvironment courseEnv, final CourseNode node) {
        final List samples = courseEnv.getCoursePropertyManager().findCourseNodeProperties(node, identity, null, PROP_ASSIGNED);
        if (samples.size() == 0) {
            return null; // no sample assigned yet
        }
        return ((PropertyImpl) samples.get(0)).getStringValue();
    }

    public void setAssignedTask(final Identity identity, final CourseEnvironment courseEnv, final CourseNode node, final String task) {
        final CoursePropertyManager cpm = courseEnv.getCoursePropertyManager();
        final PropertyImpl p = cpm.createCourseNodePropertyInstance(node, identity, null, PROP_ASSIGNED, null, null, task, null);
        cpm.saveProperty(p);
    }

    /**
     * Auto-assign a task to an identity and mark it as sampled if necessary.
     * 
     * @param identity
     * @return name of the assigned task or null if no more tasks are available.
     */
    // TODO: ORID-1007 CleanCode : Refactor without boolean parameter
    public String autoAssignTask(final Identity identity, CourseEnvironment courseEnv, CourseNode node, boolean samplingWithReplacement) {
        List availableTasks = compileAvailableTasks(courseEnv, node);
        if (availableTasks.size() == 0 && samplingWithReplacement) {
            unmarkAllSampledTasks(courseEnv, node); // unmark all tasks if samplingWithReplacement and no more tasks available
            availableTasks = compileAvailableTasks(courseEnv, node); // refetch tasks
        }
        if (availableTasks.size() == 0) {
            return null; // no more task available
        }

        final String task = (String) availableTasks.get((new Random()).nextInt(availableTasks.size()));
        setAssignedTask(identity, courseEnv, node, task); // assignes the file to this identity
        if (!samplingWithReplacement) {
            markTaskAsSampled(task, courseEnv, node); // remove the file from available files
        }
        return task;
    }

    /**
     * Cancel the task assignment.
     * 
     * @param identity
     * @param task
     */
    public void removeAssignedTask(final Identity identity, final String task, CourseEnvironment courseEnv, CourseNode node) {
        final CoursePropertyManager cpm = courseEnv.getCoursePropertyManager();
        // remove assigned
        List properties = cpm.findCourseNodeProperties(node, identity, null, Task_EBL.PROP_ASSIGNED);
        if (properties != null && properties.size() > 0) {
            final PropertyImpl propety = (PropertyImpl) properties.get(0);
            cpm.deleteProperty(propety);
        }
        // removed sampled
        properties = courseEnv.getCoursePropertyManager().findCourseNodeProperties(node, null, null, PROP_SAMPLED);
        if (properties != null && properties.size() > 0) {
            final PropertyImpl propety = (PropertyImpl) properties.get(0);
            cpm.deleteProperty(propety);
        }
    }

    public void markTaskAsSampled(final String task, CourseEnvironment courseEnv, CourseNode node) {
        final CoursePropertyManager cpm = courseEnv.getCoursePropertyManager();
        final PropertyImpl p = cpm.createCourseNodePropertyInstance(node, null, null, PROP_SAMPLED, null, null, task, null);
        cpm.saveProperty(p);
    }

    public void unmarkAllSampledTasks(CourseEnvironment courseEnv, CourseNode node) {
        courseEnv.getCoursePropertyManager().deleteNodeProperties(node, PROP_SAMPLED);
    }

    /**
     * Compiles a list of tasks based on the available files in the task folder, which have not been sampled so far.
     * 
     * @return List of available tasks.
     */
    public List compileAvailableTasks(CourseEnvironment courseEnv, CourseNode node) {
        final File[] taskSources = getTaskFolder(courseEnv, node).listFiles();
        final List tasks = new ArrayList(taskSources.length);
        final List sampledTasks = compileSampledTasks(courseEnv, node);
        for (int i = 0; i < taskSources.length; i++) {
            final File nextTask = taskSources[i];
            if (nextTask.isFile() && !sampledTasks.contains(nextTask.getName())) {
                tasks.add(nextTask.getName());
            }
        }
        return tasks;
    }

    /**
     * @return
     */
    public File getTaskFolder(CourseEnvironment courseEnv, CourseNode node) {
        final String taskFolderPath = FolderConfig.getCanonicalRoot() + TACourseNode.getTaskFolderPathRelToFolderRoot(courseEnv, node);
        File taskFolder = new File(taskFolderPath);
        if (!taskFolder.exists() && !taskFolder.mkdirs()) {
            throw new AssertException("Task folder " + taskFolderPath + " does not exist.");
        }
        return taskFolder;
    }

    /**
     * Compile a list of tasks marked as sampled.
     * 
     * @return List of sampled tasks.
     */
    private List compileSampledTasks(CourseEnvironment courseEnv, CourseNode node) {
        final List sampledTasks = new ArrayList();
        final List samples = courseEnv.getCoursePropertyManager().findCourseNodeProperties(node, null, null, PROP_SAMPLED);
        for (final Iterator iter = samples.iterator(); iter.hasNext();) {
            final PropertyImpl sample = (PropertyImpl) iter.next();
            sampledTasks.add(sample.getStringValue());
        }
        return sampledTasks;
    }

    public VFSItem getTaskFile(String taskFileName, CourseEnvironment courseEnv, CourseNode node) {
        OlatRootFolderImpl forumContainer = new OlatRootFolderImpl(TACourseNode.getTaskFolderPathRelToFolderRoot(courseEnv, node), null);
        return forumContainer.resolve(taskFileName);
    }

    /**
     * Cancel the task assignment.
     * 
     * @param identity
     * @param task
     */
    public void removeAssignedTask(final UserCourseEnvironment courseEnv, CourseNode node, final Identity identity, final String task) {
        final CoursePropertyManager cpm = courseEnv.getCourseEnvironment().getCoursePropertyManager();
        List properties = cpm.findCourseNodeProperties(node, identity, null, Task_EBL.PROP_ASSIGNED);
        if (properties != null && properties.size() > 0) {
            final PropertyImpl propety = (PropertyImpl) properties.get(0);
            cpm.deleteProperty(propety);
        }
        // removed sampled
        properties = cpm.findCourseNodeProperties(node, null, null, Task_EBL.PROP_SAMPLED);
        if (properties != null && properties.size() > 0) {
            final PropertyImpl propety = (PropertyImpl) properties.get(0);
            cpm.deleteProperty(propety);
        }
    }

}
