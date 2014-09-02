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

package org.olat.lms.course.nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.repository.handlers.RepositoryHandler;
import org.olat.lms.repository.handlers.RepositoryHandlerFactory;
import org.olat.presentation.commons.PresentationSpringBeanTypes;
import org.olat.presentation.course.nodes.CourseNodeConfiguration;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.Windows;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.generic.dtabs.DTabs;
import org.olat.presentation.repository.DynamicTabHelper;
import org.olat.presentation.repository.RepositoryDetailsController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: 28.11.2003
 * 
 * @author Mike Stock
 * @author guido
 */
public class CourseNodeFactory {

    private static final Logger log = LoggerHelper.getLogger();

    private static CourseNodeFactory INSTANCE;
    private static List<String> courseNodeConfigurationsAliases;
    private static Map<String, CourseNodeConfiguration> courseNodeConfigurations;
    private final Object lockObject = new Object();
    private HashMap<String, CourseNodeConfiguration> allCourseNodeConfigurations;

    /**
     * [used by spring]
     */
    private CourseNodeFactory() {
        INSTANCE = this;
    }

    /**
     * @return an instance of the course node factory.
     */
    public static CourseNodeFactory getInstance() {
        return INSTANCE;
    }

    public List<String> getRegisteredCourseNodeAliases() {
        if (courseNodeConfigurationsAliases == null) {
            initCourseNodeConfigurationList();
        }
        return courseNodeConfigurationsAliases;
    }

    private void initCourseNodeConfigurationList() {
        courseNodeConfigurationsAliases = new ArrayList<String>();
        courseNodeConfigurations = new HashMap<String, CourseNodeConfiguration>();
        allCourseNodeConfigurations = new HashMap<String, CourseNodeConfiguration>();
        final Map sortedMap = new TreeMap();
        final Map<String, Object> courseNodeConfigurationMap = CoreSpringFactory.getBeansOfType(PresentationSpringBeanTypes.courseNodeConfiguration);
        final Collection<Object> courseNodeConfigurationValues = courseNodeConfigurationMap.values();
        for (final Object object : courseNodeConfigurationValues) {
            final CourseNodeConfiguration courseNodeConfiguration = (CourseNodeConfiguration) object;
            if (courseNodeConfiguration.isEnabled()) {
                int key = courseNodeConfiguration.getOrder();
                while (sortedMap.containsKey(key)) {
                    // a key with this value already exist => add 1000 because offset must be outside of other values.
                    key += 1000;
                }
                if (key != courseNodeConfiguration.getOrder()) {
                    log.warn("CourseNodeConfiguration Problem: Dublicate order-value for node=" + courseNodeConfiguration.getAlias() + ", append course node at the end");
                }
                sortedMap.put(key, courseNodeConfiguration);
            } else {
                log.debug("Disabled courseNodeConfiguration=" + courseNodeConfiguration);
            }
            allCourseNodeConfigurations.put(courseNodeConfiguration.getAlias(), courseNodeConfiguration);
        }

        for (final Object key : sortedMap.keySet()) {
            final CourseNodeConfiguration courseNodeConfiguration = (CourseNodeConfiguration) sortedMap.get(key);
            courseNodeConfigurationsAliases.add(courseNodeConfiguration.getAlias());
            courseNodeConfigurations.put(courseNodeConfiguration.getAlias(), courseNodeConfiguration);
        }
    }

    /**
     * @param type
     *            The node type
     * @return a new instance of the desired type of node
     */
    public CourseNodeConfiguration getCourseNodeConfiguration(final String alias) {
        if (courseNodeConfigurations == null) {
            synchronized (lockObject) {
                if (courseNodeConfigurations == null) { // check again in synchronized-block, only one may create list
                    initCourseNodeConfigurationList();
                }
            }
        }
        return courseNodeConfigurations.get(alias);
    }

    public CourseNodeConfiguration getCourseNodeConfigurationEvenForDisabledBB(String alias) {
        if (allCourseNodeConfigurations == null) {
            synchronized (lockObject) {
                if (allCourseNodeConfigurations == null) { // check again in synchronized-block, only one may create list
                    initCourseNodeConfigurationList();
                }
            }
        }
        return allCourseNodeConfigurations.get(alias);
    }

    /**
     * Launch an editor for the repository entry which is referenced in the given course node. The editor is launched in a new tab.
     * 
     * @param ureq
     * @param node
     */
    public void launchReferencedRepoEntryEditor(final UserRequest ureq, final CourseNode node) {
        final RepositoryEntry repositoryEntry = node.getReferencedRepositoryEntry();
        if (repositoryEntry == null) {
            // do nothing
            return;
        }
        final RepositoryHandler typeToEdit = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry);
        if (!typeToEdit.supportsEdit(repositoryEntry)) {
            throw new AssertException("Trying to edit repository entry which has no assoiciated editor: " + typeToEdit);
        }

        // Open editor in new tab
        final OLATResourceable ores = repositoryEntry.getOlatResource();
        final DTabs dts = Windows.getWindows(ureq).getWindow(ureq).getDynamicTabs();
        final Controller editorController = typeToEdit.createEditorController(ores, ureq, dts.getWindowControl());
        // issue OLAT-7082 - added similar logic as for already implemented doEdit method in RepositoryDetailsController
        if (editorController == null) {
            // editor could not be created -> warning is shown
            return;
        }
        DynamicTabHelper.openResourceTab(ores, ureq, editorController, repositoryEntry.getDisplayname(), RepositoryDetailsController.ACTIVATE_EDITOR);
    }

}
