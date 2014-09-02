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

package org.olat.presentation.calendar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.olat.data.calendar.CalendarEntry;
import org.olat.data.calendar.OlatCalendar;
import org.olat.data.commons.xml.XStreamHelper;
import org.olat.lms.calendar.CalendarService;
import org.olat.presentation.calendar.components.CalendarRenderWrapper;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.tree.GenericTreeModel;
import org.olat.presentation.framework.core.components.tree.GenericTreeNode;
import org.olat.presentation.framework.core.components.tree.SelectionTree;
import org.olat.presentation.framework.core.components.tree.TreeEvent;
import org.olat.presentation.framework.core.components.tree.TreeModel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

public class CopyEventToCalendarController extends BasicController {

    private final VelocityContainer mainVC;
    private final SelectionTree calendarSelectionTree;
    private final CalendarEntry calendarEntry;
    private final CalendarService calendarService;

    public CopyEventToCalendarController(final UserRequest ureq, final WindowControl wControl, final CalendarEntry calendarEntry, final Collection calendars,
            final Translator translator) {
        super(ureq, wControl);
        this.calendarEntry = calendarEntry;
        calendarService = CoreSpringFactory.getBean(CalendarService.class);

        mainVC = createVelocityContainer("calCopy");
        calendarSelectionTree = new SelectionTree("calSelection", translator);
        calendarSelectionTree.addListener(this);
        calendarSelectionTree.setMultiselect(true);
        calendarSelectionTree.setFormButtonKey("cal.copy.submit");
        calendarSelectionTree.setShowCancelButton(true);
        calendarSelectionTree.setTreeModel(new CalendarSelectionModel(calendars, calendarEntry.getCalendar(), translator));
        mainVC.put("tree", calendarSelectionTree);

        putInitialPanel(mainVC);
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == calendarSelectionTree) {
            final TreeEvent te = (TreeEvent) event;
            if (event.getCommand().equals(TreeEvent.COMMAND_TREENODES_SELECTED)) {
                // rebuild calendar entry links
                final List selectedNodesIDS = te.getNodeIds();
                final TreeModel model = calendarSelectionTree.getTreeModel();
                for (final Iterator iter = selectedNodesIDS.iterator(); iter.hasNext();) {
                    final String nodeId = (String) iter.next();
                    final GenericTreeNode node = (GenericTreeNode) model.getNodeById(nodeId);
                    final CalendarRenderWrapper calendarWrapper = (CalendarRenderWrapper) node.getUserObject();
                    final OlatCalendar cal = calendarWrapper.getCalendar();
                    final CalendarEntry clonedKalendarEvent = (CalendarEntry) XStreamHelper.xstreamClone(calendarEntry);
                    if (clonedKalendarEvent.getCalendarEntryLinks().size() != 0) {
                        clonedKalendarEvent.setCalendarEntryLinks(new ArrayList());
                    }
                    calendarService.addEntryTo(cal, clonedKalendarEvent);
                }
                fireEvent(ureq, Event.DONE_EVENT);
            } else {
                fireEvent(ureq, Event.CANCELLED_EVENT);
            }
        }
    }

    @Override
    protected void doDispose() {
        // nothing to do here
    }

}

class CalendarSelectionModel extends GenericTreeModel {

    public CalendarSelectionModel(final Collection calendars, final OlatCalendar excludeKalendar, final Translator translator) {
        final GenericTreeNode rootNode = new GenericTreeNode(translator.translate("cal.copy.rootnode"), null);
        for (final Iterator iter_calendars = calendars.iterator(); iter_calendars.hasNext();) {
            final CalendarRenderWrapper calendarWrapper = (CalendarRenderWrapper) iter_calendars.next();
            final GenericTreeNode node = new GenericTreeNode(calendarWrapper.getCalendarConfig().getDisplayName(), calendarWrapper);
            node.setIdent(calendarWrapper.getCalendar().getCalendarID());
            if (calendarWrapper.getCalendar().getCalendarID().equals(excludeKalendar.getCalendarID())) {
                // this is the calendar, the event comes from
                node.setSelected(true);
                node.setAccessible(false);
            } else {
                node.setAccessible(calendarWrapper.getAccess() == CalendarRenderWrapper.ACCESS_READ_WRITE);
            }
            rootNode.addChild(node);
        }
        setRootNode(rootNode);
    }

}
