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

package org.olat.presentation.portal.calendar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.olat.data.calendar.CalendarEntry;
import org.olat.lms.calendar.CalendarUtils;
import org.olat.presentation.calendar.CalendarWrapperCreator;
import org.olat.presentation.calendar.components.CalendarRenderWrapper;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.Windows;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;
import org.olat.presentation.framework.core.components.table.Table;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableEvent;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.util.ComponentUtil;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.dtabs.DTabs;
import org.olat.presentation.home.site.HomeSite;
import org.olat.system.commons.OutputEscapeType;
import org.olat.system.event.Event;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description: Displays a little calendar with links to the users personal calendar
 * 
 * @author gnaegi Initial Date: Jul 26, 2006
 */
public class CalendarPortletRunController extends BasicController {

    private static final String CMD_LAUNCH = "cmd.launch";
    private static final int MAX_EVENTS = 5;

    private final VelocityContainer calendarVC;
    private final TableController tableController;
    private boolean dirty = false;
    private final Link showAllLink;

    /**
     * Constructor
     * 
     * @param ureq
     * @param wControl
     */
    protected CalendarPortletRunController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);

        calendarVC = createVelocityContainer("calendarPortlet");
        showAllLink = LinkFactory.createLink("calendar.showAll", calendarVC, this);
        ComponentUtil.registerForValidateEvents(calendarVC, this);

        final Date date = new Date();
        final String today = DateFormat.getTimeInstance(DateFormat.MEDIUM, ureq.getLocale()).format(date);
        calendarVC.contextPut("today", today);

        final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
        tableConfig.setTableEmptyMessage(translate("calendar.noEvents"));
        tableConfig.setDisplayTableHeader(false);
        tableConfig.setCustomCssClass("b_portlet_table");
        tableConfig.setDisplayRowCount(false);
        tableConfig.setPageingEnabled(false);
        tableConfig.setDownloadOffered(false);
        tableController = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
        // dummy header key, won't be used since setDisplayTableHeader is set to
        // false
        tableController.addColumnDescriptor(new PortletDateColumnDescriptor("calendar.date", 0, getTranslator()));
        tableController.addColumnDescriptor(new DefaultColumnDescriptor("calendar.subject", 1, CMD_LAUNCH, ureq.getLocale(), OutputEscapeType.HTML));

        final List events = getMatchingEvents(ureq, wControl);
        tableController.setTableDataModel(new EventsModel(events));
        listenTo(tableController);

        calendarVC.put("table", tableController.getInitialComponent());

        putInitialPanel(this.calendarVC);
    }

    private List getMatchingEvents(final UserRequest ureq, final WindowControl wControl) {
        final Date startDate = new Date();
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) + 7);
        final Date endDate = cal.getTime();
        List events = new ArrayList();
        CalendarWrapperCreator calendarWrapperCreator = CoreSpringFactory.getBean(CalendarWrapperCreator.class);
        final List calendars = calendarWrapperCreator.getListOfCalendarWrappers(ureq, wControl);
        calendars.addAll(calendarWrapperCreator.getListOfImportedCalendarWrappers(ureq));
        for (final Iterator iter = calendars.iterator(); iter.hasNext();) {
            final CalendarRenderWrapper calendarWrapper = (CalendarRenderWrapper) iter.next();
            final boolean readOnly = (calendarWrapper.getAccess() == CalendarRenderWrapper.ACCESS_READ_ONLY) && !calendarWrapper.isImported();
            final List eventsWithinPeriod = CalendarUtils.listEventsForPeriod(calendarWrapper.getCalendar(), startDate, endDate);
            for (final Iterator iterator = eventsWithinPeriod.iterator(); iterator.hasNext();) {
                final CalendarEntry event = (CalendarEntry) iterator.next();
                // skip non-public events
                if (readOnly && event.getClassification() != CalendarEntry.CLASS_PUBLIC) {
                    continue;
                }
                events.add(event);
            }
        }
        // sort events
        Collections.sort(events, new Comparator() {
            @Override
            public int compare(final Object arg0, final Object arg1) {
                final Date begin0 = ((CalendarEntry) arg0).getBegin();
                final Date begin1 = ((CalendarEntry) arg1).getBegin();
                return begin0.compareTo(begin1);
            }
        });
        if (events.size() > MAX_EVENTS) {
            events = events.subList(0, MAX_EVENTS);
        }
        return events;
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == showAllLink) {
            final String activationCmd = "cal." + new SimpleDateFormat("yyyy.MM.dd").format(new Date());
            final DTabs dts = Windows.getWindows(ureq).getWindow(ureq).getDynamicTabs();
            // was brasato:: getWindowControl().getDTabs().activateStatic(ureq, HomeSite.class.getName(), activationCmd);
            dts.activateStatic(ureq, HomeSite.class.getName(), activationCmd);
        } else if (event == ComponentUtil.VALIDATE_EVENT && dirty) {
            tableController.setTableDataModel(new EventsModel(getMatchingEvents(ureq, getWindowControl())));
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == tableController) {
            if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
                final TableEvent te = (TableEvent) event;
                final String actionid = te.getActionId();
                if (actionid.equals(CMD_LAUNCH)) {
                    final int rowid = te.getRowId();
                    final CalendarEntry calendarEntry = (CalendarEntry) ((DefaultTableDataModel) tableController.getTableDataModel()).getObject(rowid);
                    final Date startDate = calendarEntry.getBegin();
                    final String activationCmd = "cal." + new SimpleDateFormat("yyyy.MM.dd").format(startDate);
                    final DTabs dts = Windows.getWindows(ureq).getWindow(ureq).getDynamicTabs();
                    // was brasato:: getWindowControl().getDTabs().activateStatic(ureq, HomeSite.class.getName(), activationCmd);
                    dts.activateStatic(ureq, HomeSite.class.getName(), activationCmd);
                }
            }
        }
    }

    @Override
    protected void doDispose() {
        //
    }

    public void event(final Event event) {
        dirty = true;
    }

}

class EventsModel extends DefaultTableDataModel {

    private static final int COLUMNS = 2;
    private final int MAX_SUBJECT_LENGTH = 30;

    public EventsModel(final List events) {
        super(events);
    }

    @Override
    public int getColumnCount() {
        return COLUMNS;
    }

    /**
     * The output escaping is delegated to the renderer not to the data model.
     */
    @Override
    public Object getValueAt(final int row, final int col) {
        final CalendarEntry event = (CalendarEntry) getObject(row);
        switch (col) {
        case 0:
            return event;
        case 1:
            String subj = event.getSubject();
            if (subj.length() > MAX_SUBJECT_LENGTH) {
                subj = subj.substring(0, MAX_SUBJECT_LENGTH) + "...";
            }
            return subj;
        }
        throw new OLATRuntimeException("Unreacheable code.", null);
    }
}
