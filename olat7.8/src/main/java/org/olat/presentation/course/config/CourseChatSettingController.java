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

import org.olat.lms.course.config.CourseConfig;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.ControllerEventListener;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;

/**
 * Description: <br>
 * Initial Date: Jun 16, 2005 <br>
 * 
 * @author patrick
 */
public class CourseChatSettingController extends BasicController implements ControllerEventListener {

    private final CourseChatSettingsForm chatForm;
    private final VelocityContainer myContent;
    private final CourseConfig courseConfig;

    /**
     * @param course
     * @param ureq
     * @param wControl
     */
    public CourseChatSettingController(final UserRequest ureq, final WindowControl wControl, final CourseConfig courseConfig) {
        super(ureq, wControl);
        this.courseConfig = courseConfig;

        myContent = createVelocityContainer("CourseChat");
        chatForm = new CourseChatSettingsForm(ureq, wControl, courseConfig.isChatEnabled());
        listenTo(chatForm);
        myContent.put("chatForm", chatForm.getInitialComponent());
        //
        putInitialPanel(myContent);
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        //
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == chatForm) {
            if (event == Event.DONE_EVENT) {
                courseConfig.setChatIsEnabled(chatForm.chatIsEnabled());
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
