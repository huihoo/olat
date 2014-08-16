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
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * TODO: patrick Class Description for CourseEfficencyStatementController
 * <P>
 * Initial Date: Aug 12, 2005 <br>
 * 
 * @author patrick
 */
public class CourseEfficencyStatementController extends BasicController {

    private final CourseEfficencyStatementForm efficencyForm;
    private final VelocityContainer myContent;

    private DialogBoxController disableEfficiencyDC, enableEfficiencyDC;
    private boolean previousValue;

    private final CourseConfig courseConfig;
    private ILoggingAction loggingAction;

    /**
     * @param course
     * @param ureq
     * @param wControl
     */
    public CourseEfficencyStatementController(final UserRequest ureq, final WindowControl wControl, final CourseConfig courseConfig) {
        super(ureq, wControl);
        this.courseConfig = courseConfig;
        //
        myContent = createVelocityContainer("CourseEfficencyStatement");
        efficencyForm = new CourseEfficencyStatementForm(ureq, wControl, courseConfig.isEfficencyStatementEnabled());
        previousValue = courseConfig.isEfficencyStatementEnabled();
        listenTo(efficencyForm);
        myContent.put("efficencyForm", efficencyForm.getInitialComponent());

        putInitialPanel(myContent);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == disableEfficiencyDC) {
            if (DialogBoxUIFactory.isOkEvent(event)) {
                // yes disable!
                courseConfig.setEfficencyStatementIsEnabled(efficencyForm.isEnabledEfficencyStatement());
                previousValue = efficencyForm.isEnabledEfficencyStatement();
                loggingAction = LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_EFFICIENCY_STATEMENT_DISABLED;
                this.fireEvent(ureq, Event.CHANGED_EVENT);

            } else {
                // roll back in form
                efficencyForm.setEnabledEfficencyStatement(true);
            }
        } else if (source == enableEfficiencyDC) {
            if (DialogBoxUIFactory.isOkEvent(event)) {
                // yes enable!
                courseConfig.setEfficencyStatementIsEnabled(efficencyForm.isEnabledEfficencyStatement());
                previousValue = efficencyForm.isEnabledEfficencyStatement();
                loggingAction = LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_EFFICIENCY_STATEMENT_ENABLED;
                this.fireEvent(ureq, Event.CHANGED_EVENT);

            } else {
                // roll back in form
                efficencyForm.setEnabledEfficencyStatement(false);
            }
        } else if (source == efficencyForm) {
            if ((event == Event.DONE_EVENT) && (previousValue != efficencyForm.isEnabledEfficencyStatement())) {
                // only real changes trigger
                if (previousValue) {
                    // a change from enabled Efficiency to disabled
                    disableEfficiencyDC = activateYesNoDialog(ureq, null, translate("warning.change.todisabled"), disableEfficiencyDC);
                } else {
                    // a change from disabled Efficiency
                    enableEfficiencyDC = activateYesNoDialog(ureq, null, translate("warning.change.toenable"), enableEfficiencyDC);
                }
            }
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

    /**
     * @return Returns LOG_EFFICIENCY_STATEMENT_ENABLED or LOG_EFFICIENCY_STATEMENT_DISABLED or null if nothing changed.
     */
    public ILoggingAction getLoggingAction() {
        return loggingAction;
    }

}
