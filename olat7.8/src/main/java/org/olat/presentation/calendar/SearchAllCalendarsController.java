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

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.olat.data.calendar.CalendarDao;
import org.olat.data.calendar.CalendarEntry;
import org.olat.lms.calendar.CalendarUtils;
import org.olat.lms.calendar.GotoDateEvent;
import org.olat.presentation.calendar.components.CalendarRenderWrapper;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.tree.GenericTreeModel;
import org.olat.presentation.framework.core.components.tree.GenericTreeNode;
import org.olat.presentation.framework.core.components.tree.SelectionTree;
import org.olat.presentation.framework.core.components.tree.TreeEvent;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;

public class SearchAllCalendarsController extends BasicController {

    private final Collection calendars;

    private final VelocityContainer mainVC;
    private final Panel panel;
    private final SearchAllCalendarsForm searchForm;
    private SelectionTree eventSelectionTree;

    private final Link backLink;

    public SearchAllCalendarsController(final UserRequest ureq, final WindowControl wControl, final Collection calendars) {
        super(ureq, wControl);
        this.calendars = calendars;

        mainVC = createVelocityContainer("calSearchMain");
        backLink = LinkFactory.createLinkBack(mainVC, this);
        mainVC.contextPut("displayBackLink", Boolean.FALSE);
        panel = new Panel("panel");
        searchForm = new SearchAllCalendarsForm(ureq, wControl);
        listenTo(searchForm);

        panel.setContent(searchForm.getInitialComponent());
        mainVC.put("panel", panel);

        this.putInitialPanel(mainVC);
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == eventSelectionTree) {
            final TreeEvent te = (TreeEvent) event;
            if (event.getCommand().equals(TreeEvent.COMMAND_TREENODE_CLICKED)) {
                final Date gotoDate = (Date) eventSelectionTree.getSelectedNode().getUserObject();
                fireEvent(ureq, new GotoDateEvent(gotoDate));
            } else {
                fireEvent(ureq, Event.CANCELLED_EVENT);
            }
        } else if (source == backLink) {
            if (panel.getContent() == eventSelectionTree) {
                mainVC.contextPut("displayBackLink", Boolean.FALSE);
                panel.setContent(searchForm.getInitialComponent());
            }
        }
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == searchForm) {
            mainVC.contextPut("displayBackLink", Boolean.TRUE);
            if (event == Event.DONE_EVENT) {
                eventSelectionTree = new SelectionTree("eventSelection", getTranslator());
                eventSelectionTree.addListener(this);
                eventSelectionTree.setFormButtonKey("cal.goto.event");
                eventSelectionTree.setShowCancelButton(true);
                eventSelectionTree.setMultiselect(false);
                eventSelectionTree.setShowAltTextAsHoverOnTitle(true);
                eventSelectionTree.setAllowEmptySelection(false);
                eventSelectionTree.setEscapeHtml(true);
                final GenericTreeNode rootNode = new GenericTreeNode(translate("cal.search.results"), null);
                rootNode.setAccessible(false);
                final String subject = searchForm.getSubject();
                final String location = searchForm.getLocation();
                final Date beginPeriod = searchForm.getBeginPeriod();
                final Date endPeriod = searchForm.getEndPeriod();
                // search for events and build selection tree model
                for (final Iterator iter = calendars.iterator(); iter.hasNext();) {
                    final CalendarRenderWrapper calendarWrapper = (CalendarRenderWrapper) iter.next();
                    // for locally read-only calendars: search only public events, for imported calendar search in private and public events
                    final boolean searchPublicEventsOnly = (calendarWrapper.getAccess() == CalendarRenderWrapper.ACCESS_READ_ONLY) && !calendarWrapper.isImported();
                    final List matchingEvents = CalendarUtils
                            .findEvents(calendarWrapper.getCalendar(), subject, location, beginPeriod, endPeriod, searchPublicEventsOnly);
                    if (matchingEvents.size() == 0) {
                        continue;
                    }
                    final GenericTreeNode calendarNode = new GenericTreeNode(translate("cal.form.calendarname") + ": "
                            + calendarWrapper.getCalendarConfig().getDisplayName(), null);
                    calendarNode.setAccessible(false);
                    rootNode.addChild(calendarNode);
                    for (final Iterator Iter_matching = matchingEvents.iterator(); Iter_matching.hasNext();) {
                        final CalendarEntry matchingEvent = (CalendarEntry) Iter_matching.next();
                        final StringBuilder display = new StringBuilder();
                        String truncatedSubject = matchingEvent.getSubject();
                        if (truncatedSubject.length() > CalendarDao.MAX_SUBJECT_DISPLAY_LENGTH) {
                            truncatedSubject = truncatedSubject.substring(0, CalendarDao.MAX_SUBJECT_DISPLAY_LENGTH) + "...";
                        }
                        display.append(truncatedSubject);
                        display.append(" (");
                        display.append(CalendarUtils.getDateTimeAsString(matchingEvent.getBegin(), getLocale()));
                        display.append(" - ");
                        display.append(CalendarUtils.getDateTimeAsString(matchingEvent.getEnd(), getLocale()));
                        display.append(")");
                        final GenericTreeNode eventNode = new GenericTreeNode(display.toString(), matchingEvent);
                        eventNode.setAltText(layoutEventDetails(matchingEvent, calendarWrapper));
                        eventNode.setUserObject(matchingEvent.getBegin());
                        if (searchPublicEventsOnly) {
                            eventNode.setAccessible(false);
                        }
                        calendarNode.addChild(eventNode);
                    }
                }
                if (rootNode.getChildCount() != 0) {
                    // add matching events to tree
                    final GenericTreeModel treeModel = new GenericTreeModel();
                    treeModel.setRootNode(rootNode);
                    eventSelectionTree.setTreeModel(treeModel);
                    panel.setContent(eventSelectionTree);
                } else {
                    // no matching events
                    mainVC.contextPut("displayBackLink", Boolean.FALSE);
                    this.showInfo("cal.search.noMatches");
                }
            }
        }
    }

    private String layoutEventDetails(final CalendarEntry event, final CalendarRenderWrapper calendarWrapper) {
        final StringBuilder details = new StringBuilder();
        details.append(translate("cal.form.calendarname")).append(": ").append(calendarWrapper.getCalendarConfig().getDisplayName()).append(", ");
        details.append(translate("cal.form.begin")).append(": ").append(CalendarUtils.getDateTimeAsString(event.getBegin(), getLocale())).append(", ");
        details.append(translate("cal.form.end")).append(": ").append(CalendarUtils.getDateTimeAsString(event.getEnd(), getLocale())).append(", ");
        details.append(translate("cal.form.subject")).append(": ").append(event.getSubject()).append(", ");
        if (event.getLocation() != null) {
            details.append(translate("cal.form.location")).append(": ").append(event.getLocation());
        }
        return details.toString();
    }

    @Override
    protected void doDispose() {
        // nothing to do here
    }

}
