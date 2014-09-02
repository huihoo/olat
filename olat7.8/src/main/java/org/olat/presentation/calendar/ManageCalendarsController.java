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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.presentation.calendar;

import java.util.List;
import java.util.Locale;

import org.olat.lms.calendar.ImportCalendarManager;
import org.olat.lms.commons.LearnServices;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;

/**
 * Description:<BR>
 * Manager for: 1. export settings of OLAT calendars. 2. import of external calendars 3. settings of imported calendars
 * <P>
 * Initial Date: June 28, 2008
 * 
 * @author Udit Sajjanhar
 */
public class ManageCalendarsController extends BasicController {

    private final VelocityContainer manageVC;
    private final ImportedCalendarConfigurationController importedCalendarConfig;
    private final CalendarFileUploadController calFileUpload;
    private final CalendarImportByUrlController calImportByUrl;
    private final Link importTypeFileButton;
    private final Link importTypeUrlButton;
    private CalendarImportNameForm nameForm;
    private final Panel panel;
    private String importUrl;
    private ImportCalendarManager importCalendarManager;

    ManageCalendarsController(final UserRequest ureq, final Locale locale, final WindowControl wControl, final List importedCalendarWrappers) {
        super(ureq, wControl);
        importCalendarManager = getService(LearnServices.importCalendarManager);
        manageVC = createVelocityContainer("manageCalendars");

        // Import calendar functionalities
        importedCalendarConfig = new ImportedCalendarConfigurationController(importedCalendarWrappers, ureq, getWindowControl(), true);
        importedCalendarConfig.addControllerListener(this);
        manageVC.put("importedCalendarConfig", importedCalendarConfig.getInitialComponent());
        manageVC.contextPut("importedCalendarWrappers", importedCalendarWrappers);

        calFileUpload = new CalendarFileUploadController(ureq, locale, wControl);
        listenTo(calFileUpload);

        calImportByUrl = new CalendarImportByUrlController(ureq, wControl);
        listenTo(calImportByUrl);

        panel = new Panel("panel");
        manageVC.put("fileupload", panel);
        manageVC.contextPut("choose", 1);

        importTypeFileButton = LinkFactory.createButton("cal.import.type.file", manageVC, this);
        importTypeUrlButton = LinkFactory.createButton("cal.import.type.url", manageVC, this);
        putInitialPanel(manageVC);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == importTypeFileButton) {
            manageVC.contextPut("choose", 0);
            panel.setContent(calFileUpload.getInitialComponent());
        } else if (source == importTypeUrlButton) {
            manageVC.contextPut("choose", 0);
            panel.setContent(calImportByUrl.getInitialComponent());
        } else {
            fireEvent(ureq, Event.DONE_EVENT);
        }
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == nameForm) {
            if (event == Event.DONE_EVENT) {
                importCalendarManager.persistCalendar(nameForm.getCalendarName(), ureq, importUrl);
                importUrl = null; // reset importUrl
                // inform the user of successful import
                showInfo("cal.import.success");

                // reset the panel to import an another calendar
                manageVC.contextPut("choose", 1);

                // update the imported calendar list
                importedCalendarConfig.setCalendars(importCalendarManager.getImportedCalendarsForIdentity(ureq));

                manageVC.contextPut("importedCalendarWrappers", importCalendarManager.getImportedCalendarsForIdentity(ureq));
            } else if (event == Event.CANCELLED_EVENT) {
                // reset the panel to import an another calendar
                manageVC.contextPut("choose", 1);
                panel.setContent(calFileUpload.getInitialComponent());
            }
        } else if (source == calFileUpload || source == calImportByUrl) {
            if (event == Event.DONE_EVENT) {
                // correct file has been uploaded. ask user the name of the calendar
                removeAsListenerAndDispose(nameForm);
                nameForm = new CalendarImportNameForm(ureq, getWindowControl());
                listenTo(nameForm);
                panel.setContent(nameForm.getInitialComponent());
                if (source == calImportByUrl) {
                    // store import url for persistCalendar call
                    importUrl = calImportByUrl.getImportUrl();
                }
            } else if (event == Event.CANCELLED_EVENT) {
                // reset the panel to import an another calendar
                manageVC.contextPut("choose", 1);
                if (source == calFileUpload) {
                    panel.setContent(calFileUpload.getInitialComponent());
                } else {
                    panel.setContent(calImportByUrl.getInitialComponent());
                }
            }
        } else if (source == importedCalendarConfig) {
            if (event == Event.CHANGED_EVENT) {
                manageVC.contextPut("importedCalendarWrappers", importCalendarManager.getImportedCalendarsForIdentity(ureq));
                manageVC.setDirty(true);
                fireEvent(ureq, Event.CHANGED_EVENT);
            }
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }

}
