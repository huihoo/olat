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

package org.olat.presentation.course.config;

import org.apache.log4j.Logger;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.activitylogging.ILoggingAction;
import org.olat.lms.activitylogging.LearningResourceLoggingAction;
import org.olat.lms.commons.fileresource.GlossaryResource;
import org.olat.lms.course.config.CourseConfig;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.ControllerEventListener;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.repository.ReferencableEntriesSearchController;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description: <br>
 * Course config controller to modify the course glossary. The controller allows the user to enable / disable the course glossary by setting a
 * <p>
 * 
 * @author Florian Gnägi, frentix GmbH, http://www.frentix.com
 */
public class CourseConfigGlossaryController extends BasicController implements ControllerEventListener {

    private static final Logger log = LoggerHelper.getLogger();

    public static final String VALUE_EMPTY_GLOSSARY_FILEREF = "gf.notconfigured";
    private static final String COMMAND_REMOVE = "command.glossary.remove";
    private static final String COMMAND_ADD = "command.glossary.add";

    private final VelocityContainer myContent;
    private ReferencableEntriesSearchController repoSearchCtr;
    private Link addCommand, removeCommand;

    private CloseableModalController cmc;
    private final CourseConfig courseConfig;
    private final Long courseResourceableId;
    private ILoggingAction loggingAction;

    /**
     * Constructor
     * 
     * @param ureq
     * @param wControl
     * @param course
     */
    public CourseConfigGlossaryController(final UserRequest ureq, final WindowControl wControl, final CourseConfig courseConfig, final Long courseResourceableId) {
        super(ureq, wControl);
        this.courseConfig = courseConfig;
        this.courseResourceableId = courseResourceableId;

        myContent = createVelocityContainer("CourseGlossary");

        if (courseConfig.hasGlossary()) {
            final RepositoryEntry repoEntry = RepositoryServiceImpl.getInstance().lookupRepositoryEntryBySoftkey(courseConfig.getGlossarySoftKey(), false);
            if (repoEntry == null) {
                // Something is wrong here, maybe the glossary has been deleted. Try to
                // remove glossary from configuration
                doRemoveGlossary(ureq);
                log.warn("Course with ID::" + courseResourceableId + " had a config for a glossary softkey::" + courseConfig.getGlossarySoftKey()
                        + " but no such glossary was found");
            } else {
                removeCommand = LinkFactory.createButton(COMMAND_REMOVE, myContent, this);
                myContent.contextPut("repoEntry", repoEntry);
            }
        } else {
            addCommand = LinkFactory.createButton(COMMAND_ADD, myContent, this);
        }
        putInitialPanel(myContent);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == addCommand) {
            repoSearchCtr = new ReferencableEntriesSearchController(getWindowControl(), ureq, GlossaryResource.TYPE_NAME, translate("select"));
            listenTo(repoSearchCtr);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), repoSearchCtr.getInitialComponent());
            cmc.activate();
        } else if (source == removeCommand) {
            doRemoveGlossary(ureq);
            fireEvent(ureq, Event.CHANGED_EVENT);// FIXME:pb:send event to agency
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == repoSearchCtr) {
            cmc.deactivate();
            if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
                final RepositoryEntry repoEntry = repoSearchCtr.getSelectedEntry();
                doSelectGlossary(repoEntry, ureq);
                fireEvent(ureq, Event.CHANGED_EVENT);// FIXME:pb:send event to agency
            }
        }
    }

    /**
     * Updates config with selected glossary
     * 
     * @param repoEntry
     * @param ureq
     */
    private void doSelectGlossary(final RepositoryEntry repoEntry, final UserRequest ureq) {

        final String softkey = repoEntry.getSoftkey();
        courseConfig.setGlossarySoftKey(softkey);
        loggingAction = LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_GLOSSARY_ENABLED;

        // update view
        myContent.contextPut("repoEntry", repoEntry);
        myContent.remove(addCommand);
        removeCommand = LinkFactory.createButton(COMMAND_REMOVE, myContent, this);
        if (log.isDebugEnabled()) {
            log.debug("Set new glossary softkey::" + courseConfig.getGlossarySoftKey() + " for course with ID::" + courseResourceableId);
        }
        this.fireEvent(ureq, Event.CHANGED_EVENT);
    }

    /**
     * Removes the current glossary from the configuration
     * 
     * @param ureq
     */
    private void doRemoveGlossary(final UserRequest ureq) {

        courseConfig.setGlossarySoftKey(null);
        loggingAction = LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_GLOSSARY_DISABLED;

        // update view
        myContent.contextRemove("repoEntry");
        myContent.remove(removeCommand);
        addCommand = LinkFactory.createButton(COMMAND_ADD, myContent, this);

        if (log.isDebugEnabled()) {
            log.debug("Removed glossary softkey for course with ID::" + courseResourceableId);
        }
        this.fireEvent(ureq, Event.CHANGED_EVENT);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // auto dispose by basic controller
    }

    public ILoggingAction getLoggingAction() {
        return loggingAction;
    }
}
