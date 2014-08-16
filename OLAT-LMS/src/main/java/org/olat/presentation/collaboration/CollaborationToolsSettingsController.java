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

package org.olat.presentation.collaboration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.olat.data.calendar.CalendarDao;
import org.olat.lms.group.BGConfigFlags;
import org.olat.presentation.admin.quota.QuotaControllerFactory;
import org.olat.presentation.calendar.events.CalendarModifiedEvent;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.SingleSelection;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.event.Event;

/**
 * Description: <BR>
 * Administrative controller which allows configuration of collaboration tools.
 * <P>
 * Initial Date: Aug 23, 2004
 * 
 * @author patrick
 */

public class CollaborationToolsSettingsController extends BasicController {

    private final VelocityContainer vc_collabtools;
    private final ChoiceOfToolsForm cots;
    private NewsFormController newsController;
    private CalendarForm calendarForm;

    boolean lastCalendarEnabledState;
    private Controller quotaCtr;
    private final OLATResourceable businessGroup;

    /**
     * @param ureq
     * @param tools
     */
    public CollaborationToolsSettingsController(final UserRequest ureq, final WindowControl wControl, final OLATResourceable businessGroup, final BGConfigFlags flags) {
        super(ureq, wControl);
        this.businessGroup = businessGroup;
        final CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup);

        vc_collabtools = createVelocityContainer("collaborationtools");

        cots = new ChoiceOfToolsForm(ureq, wControl, collabTools, flags);
        listenTo(cots);
        vc_collabtools.put("choiceOfTools", cots.getInitialComponent());

        if (collabTools.isToolEnabled(CollaborationTools.TOOL_NEWS)) {
            addNewsTool(ureq);
        } else {
            vc_collabtools.contextPut("newsToolEnabled", Boolean.FALSE);
        }

        if (ureq.getUserSession().getRoles().isOLATAdmin()) {
            vc_collabtools.contextPut("isOlatAdmin", Boolean.TRUE);
            quotaCtr = QuotaControllerFactory.getQuotaEditorInstance(ureq, getWindowControl(), collabTools.getFolderRelPath(), false);
            listenTo(quotaCtr);
        } else {
            vc_collabtools.contextPut("isOlatAdmin", Boolean.FALSE);
        }

        // update calendar form: only show when enabled
        if (collabTools.isToolEnabled(CollaborationTools.TOOL_CALENDAR)) {
            lastCalendarEnabledState = true;
            vc_collabtools.contextPut("calendarToolEnabled", Boolean.TRUE);
            int iCalendarAccess = CollaborationTools.CALENDAR_ACCESS_OWNERS;
            final Long lCalendarAccess = collabTools.lookupCalendarAccess();
            if (lCalendarAccess != null) {
                iCalendarAccess = lCalendarAccess.intValue();
            }
            calendarForm = new CalendarForm(ureq, getWindowControl(), iCalendarAccess);
            listenTo(calendarForm);

            vc_collabtools.put("calendarform", calendarForm.getInitialComponent());
        } else {
            lastCalendarEnabledState = false;
            vc_collabtools.contextPut("folderToolEnabled", Boolean.FALSE);
        }

        // update quota form: only show when enabled
        if (collabTools.isToolEnabled(CollaborationTools.TOOL_FOLDER) && ureq.getUserSession().getRoles().isOLATAdmin()) {
            vc_collabtools.contextPut("folderToolEnabled", Boolean.TRUE);
            vc_collabtools.put("quota", quotaCtr.getInitialComponent());
        } else {
            vc_collabtools.contextPut("folderToolEnabled", Boolean.FALSE);
        }

        putInitialPanel(vc_collabtools);
    }

    private void addNewsTool(final UserRequest ureq) {
        final CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup);
        final String newsValue = collabTools.lookupNews();

        if (newsController != null) {
            removeAsListenerAndDispose(newsController);
        }
        newsController = new NewsFormController(ureq, getWindowControl(), (newsValue == null ? "" : newsValue));
        listenTo(newsController);

        vc_collabtools.contextPut("newsToolEnabled", Boolean.TRUE);
        vc_collabtools.put("newsform", newsController.getInitialComponent());
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup);

        if (source == cots && event.getCommand().equals("ONCHANGE")) {

            final Set<String> set = cots.getSelected();
            for (int i = 0; i < CollaborationTools.TOOLS.length; i++) {
                // usually one should check which one changed but here
                // it is okay to set all of them because ctsm has a cache
                // and writes only when really necessary.
                collabTools.setToolEnabled(CollaborationTools.TOOLS[i], set.contains("" + i));
            }
            // reload tools after a change
            collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup);

            fireEvent(ureq, Event.CHANGED_EVENT);

            // update news form: only show when enabled
            if (collabTools.isToolEnabled(CollaborationTools.TOOL_NEWS)) {
                addNewsTool(ureq);
            } else {
                vc_collabtools.contextPut("newsToolEnabled", Boolean.FALSE);
            }

            // update calendar form: only show when enabled
            final boolean newCalendarEnabledState = collabTools.isToolEnabled(CollaborationTools.TOOL_CALENDAR);
            if (newCalendarEnabledState != lastCalendarEnabledState) {
                if (newCalendarEnabledState) {
                    vc_collabtools.contextPut("calendarToolEnabled", Boolean.TRUE);
                    int iCalendarAccess = CollaborationTools.CALENDAR_ACCESS_OWNERS;
                    final Long lCalendarAccess = collabTools.lookupCalendarAccess();
                    if (lCalendarAccess != null) {
                        iCalendarAccess = lCalendarAccess.intValue();
                    }
                    if (calendarForm != null) {
                        this.removeAsListenerAndDispose(calendarForm);
                    }
                    calendarForm = new CalendarForm(ureq, getWindowControl(), iCalendarAccess);
                    listenTo(calendarForm);
                    vc_collabtools.put("calendarform", calendarForm.getInitialComponent());

                } else {

                    vc_collabtools.contextPut("calendarToolEnabled", Boolean.FALSE);

                    // notify calendar components to refresh their calendars
                    CoordinatorManager.getInstance().getCoordinator().getEventBus()
                            .fireEventToListenersOf(new CalendarModifiedEvent(), OresHelper.lookupType(CalendarDao.class));
                }
                lastCalendarEnabledState = newCalendarEnabledState;
            }

            // update quota form: only show when enabled
            if (collabTools.isToolEnabled(CollaborationTools.TOOL_FOLDER)) {
                vc_collabtools.contextPut("folderToolEnabled", Boolean.TRUE);
                if (ureq.getUserSession().getRoles().isOLATAdmin()) {
                    vc_collabtools.put("quota", quotaCtr.getInitialComponent());
                }
            } else {
                vc_collabtools.contextPut("folderToolEnabled", Boolean.FALSE);
            }

        } else if (source == this.newsController) {
            if (event.equals(Event.DONE_EVENT)) {
                final String news = this.newsController.getNewsValue();
                collabTools.saveNews(news);
            }

        } else if (source == this.calendarForm) {
            collabTools.saveCalendarAccess(new Long(calendarForm.getCalendarAccess()));
            // notify calendar components to refresh their calendars
            CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new CalendarModifiedEvent(), OresHelper.lookupType(CalendarDao.class));
        }
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        //
    }

    @Override
    protected void doDispose() {
        //
    }
}

class ChoiceOfToolsForm extends FormBasicController {

    CollaborationTools cts;
    MultipleSelectionElement ms;

    List<String> theKeys = new ArrayList<String>();
    List<String> theValues = new ArrayList<String>();

    public ChoiceOfToolsForm(final UserRequest ureq, final WindowControl wControl, final CollaborationTools cts, final BGConfigFlags flags) {
        super(ureq, wControl);
        this.cts = cts;

        for (int i = 0, j = 0; i < CollaborationTools.TOOLS.length; i++) {
            final String k = CollaborationTools.TOOLS[i];
            if (k.equals(CollaborationTools.TOOL_CHAT) && !flags.isEnabled(BGConfigFlags.BUDDYLIST)) {
                continue;
            }
            theKeys.add("" + i);
            theValues.add(translate("collabtools.named." + CollaborationTools.TOOLS[i]));
        }

        initForm(ureq);
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        //
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        ms = uifactory.addCheckboxesVertical("selection", formLayout, theKeys.toArray(new String[theKeys.size()]), theValues.toArray(new String[theValues.size()]), null,
                1);
        for (int i = 0; i < CollaborationTools.TOOLS.length; i++) {
            ms.select("" + i, cts.isToolEnabled(CollaborationTools.TOOLS[i]));
        }
        ms.addActionListener(listener, FormEvent.ONCLICK);
    }

    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        if (source == ms && event.getCommand().equals("ONCLICK")) {
            fireEvent(ureq, new Event("ONCHANGE"));
        }
    }

    protected Set<String> getSelected() {
        return ms.getSelectedKeys();
    }

    @Override
    protected void doDispose() {
        //
    }
}

class CalendarForm extends FormBasicController {

    private SingleSelection access;
    private final int calendarAccess;

    /**
     * @param name
     * @param news
     */
    public CalendarForm(final UserRequest ureq, final WindowControl wControl, final int calendarAccess) {
        super(ureq, wControl);
        this.calendarAccess = calendarAccess;
        initForm(ureq);
    }

    /**
     * @return String
     */
    public int getCalendarAccess() {
        if (access.getSelectedKey().equals("all")) {
            return CollaborationTools.CALENDAR_ACCESS_ALL;
        } else {
            return CollaborationTools.CALENDAR_ACCESS_OWNERS;
        }
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        setFormTitle("calendar.access.title");

        final String[] keys = new String[] { "owner", "all" };
        final String values[] = new String[] { translate("calendar.access.owners"), translate("calendar.access.all") };

        access = uifactory.addRadiosVertical("access", "calendar.access", formLayout, keys, values);

        if (calendarAccess == CollaborationTools.CALENDAR_ACCESS_ALL) {
            access.select("all", true);
        } else {
            access.select("owner", true);
        }

        uifactory.addFormSubmitButton("submit", formLayout);
    }

    @Override
    protected void doDispose() {
        //
    }
}
