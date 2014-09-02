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
package org.olat.presentation.group;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.olat.data.group.area.BGArea;
import org.olat.data.group.area.BGAreaDao;
import org.olat.data.group.area.BGAreaDaoImpl;
import org.olat.data.group.context.BGContext;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.group.GroupLoggingAction;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.group.area.BGAreaFormController;
import org.olat.system.event.Event;
import org.olat.testutils.codepoints.server.Codepoint;

/**
 * Description:<br>
 * Shows the group form until a a group could be created or the form is cancelled. A group can not be created if its name already exists in the given context. This will
 * show an error on the form.<br>
 * Sends {@link Event#DONE_EVENT} in the case of successfully group creation and {@link Event#CANCELLED_EVENT} if the user no longer wishes to create a group.
 * <P>
 * Initial Date: 28.06.2007 <br>
 * 
 * @author patrickb
 */
public class NewAreaController extends BasicController {

    private final BGContext bgContext;
    private final VelocityContainer contentVC;
    private final BGAreaFormController areaCreateController;
    private boolean bulkMode = false;
    private Set<BGArea> newAreas;
    private HashSet<String> newAreaNames;
    private final BGAreaDao areaManager;

    /**
     * @param ureq
     * @param wControl
     * @param minMaxEnabled
     * @param bgContext
     * @param bulkMode
     * @param csvGroupNames
     */
    NewAreaController(final UserRequest ureq, final WindowControl wControl, final BGContext bgContext, final boolean bulkMode, final String csvAreaNames) {
        super(ureq, wControl);
        this.bgContext = bgContext;
        this.bulkMode = bulkMode;
        //
        this.areaManager = BGAreaDaoImpl.getInstance();
        this.contentVC = this.createVelocityContainer("areaform");
        this.contentVC.contextPut("bulkMode", bulkMode ? Boolean.TRUE : Boolean.FALSE);
        //
        this.areaCreateController = new BGAreaFormController(ureq, wControl, null, bulkMode);
        listenTo(this.areaCreateController);
        this.contentVC.put("areaForm", this.areaCreateController.getInitialComponent());

        if (csvAreaNames != null) {
            this.areaCreateController.setAreaName(csvAreaNames);
        }
        this.putInitialPanel(this.contentVC);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // Don't dispose anything

    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // Don't do anything.
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == this.areaCreateController) {
            if (event == Event.DONE_EVENT) {
                final String areaDesc = this.areaCreateController.getAreaDescription();

                Set<String> allNames = new HashSet<String>();
                if (this.bulkMode) {
                    allNames = this.areaCreateController.getGroupNames();
                } else {
                    allNames.add(this.areaCreateController.getAreaName());
                }

                if (areaManager.checkIfOneOrMoreNameExistsInContext(allNames, bgContext)) {
                    // set error of non existing name
                    this.areaCreateController.setAreaNameExistsError(null);
                } else {
                    Codepoint.codepoint(this.getClass(), "createArea");
                    // create bulkgroups only if there is no name which already exists.
                    newAreas = new HashSet<BGArea>();
                    newAreaNames = new HashSet<String>();
                    for (final Iterator<String> iter = allNames.iterator(); iter.hasNext();) {
                        final String areaName = iter.next();
                        final BGArea newArea = areaManager.createAndPersistBGAreaIfNotExists(areaName, areaDesc, bgContext);
                        newAreas.add(newArea);
                        newAreaNames.add(areaName);
                    }
                    // do loggin if ual given
                    for (final Iterator<BGArea> iter = newAreas.iterator(); iter.hasNext();) {
                        final BGArea a = iter.next();
                        ThreadLocalUserActivityLogger.log(GroupLoggingAction.AREA_CREATED, getClass(), LoggingResourceable.wrap(a));
                    }
                    // workflow successfully finished
                    // so far no events on the systembus to inform about new groups in BGContext
                    fireEvent(ureq, Event.DONE_EVENT);
                }
            } else if (event == Event.CANCELLED_EVENT) {
                // workflow cancelled
                fireEvent(ureq, Event.CANCELLED_EVENT);
            }
        }
    }

    /**
     * name of created area
     * 
     * @return
     */
    public String getCreatedAreaName() {
        return newAreas.iterator().next().getName();
    }

    /**
     * if Event.DONE_EVENT received the return value is always NOT NULL. If Event_FORM_CANCELLED ist received this will be null.
     * 
     * @return
     */
    public BGArea getCreatedArea() {
        return newAreas.iterator().next();
    }

    /**
     * in bulkmode the created ares
     * 
     * @return
     */
    public Set<BGArea> getCreatedAreas() {
        return newAreas;
    }

    /**
     * in bulkmode the validated area names
     * 
     * @return
     */
    public Set<String> getCreatedAreaNames() {
        return newAreaNames;
    }

    /**
     * if bulkmode is on or not
     * 
     * @return
     */
    public boolean isBulkMode() {
        return bulkMode;
    }

}
