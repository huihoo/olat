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

import org.olat.lms.activitylogging.ILoggingAction;
import org.olat.lms.activitylogging.LearningResourceLoggingAction;
import org.olat.lms.course.config.CourseConfig;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.SelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.ControllerEventListener;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * TODO: patrick Class Description for CourseEfficencyStatementController
 * <P>
 * Initial Date: Aug 12, 2005 <br>
 * 
 * @author patrick
 */
public class CourseCalendarConfigController extends BasicController implements ControllerEventListener {

    private final CourseCalendarConfigForm calConfigForm;
    private final VelocityContainer myContent;
    private final CourseConfig courseConfig;
    private ILoggingAction loggingAction;

    /**
     * @param course
     * @param ureq
     * @param wControl
     */
    public CourseCalendarConfigController(final UserRequest ureq, final WindowControl wControl, final CourseConfig courseConfig) {
        super(ureq, wControl);
        this.courseConfig = courseConfig;

        myContent = createVelocityContainer("CourseCalendar");
        calConfigForm = new CourseCalendarConfigForm(ureq, wControl, courseConfig.isCalendarEnabled());
        listenTo(calConfigForm);
        myContent.put("calendarForm", calConfigForm.getInitialComponent());
        //
        putInitialPanel(myContent);
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        //
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == calConfigForm) {
            if (event == Event.DONE_EVENT) {
                courseConfig.setCalendarEnabled(calConfigForm.isCalendarEnabled());
                if (calConfigForm.isCalendarEnabled()) {
                    loggingAction = LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_CALENDAR_ENABLED;
                } else {
                    loggingAction = LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_CALENDAR_DISABLED;
                }
                this.fireEvent(ureq, Event.CHANGED_EVENT);
            }
        }
    }

    @Override
    protected void doDispose() {
        //
    }

    /**
     * @return Return the log message if any, else null.
     */
    public ILoggingAction getLoggingAction() {
        return loggingAction;
    }

}

class CourseCalendarConfigForm extends FormBasicController {

    private SelectionElement isOn;
    private final boolean calendarEnabled;

    /**
     * @param name
     * @param chatEnabled
     */
    public CourseCalendarConfigForm(final UserRequest ureq, final WindowControl wControl, final boolean calendarEnabled) {
        super(ureq, wControl);
        this.calendarEnabled = calendarEnabled;
        initForm(ureq);
    }

    /**
     * @return if chat is enabled
     */
    public boolean isCalendarEnabled() {
        return isOn.isSelected(0);
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {

        isOn = uifactory.addCheckboxesVertical("isOn", "chkbx.calendar.onoff", formLayout, new String[] { "xx" }, new String[] { "" }, null, 1);
        isOn.select("xx", calendarEnabled);

        uifactory.addFormSubmitButton("save", "save", formLayout);

    }

    @Override
    protected void doDispose() {
        //
    }

}
