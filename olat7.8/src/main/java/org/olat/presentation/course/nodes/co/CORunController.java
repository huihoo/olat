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

package org.olat.presentation.course.nodes.co;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;

/**
 * Description:<BR/>
 * Run controller for the contact form building block
 * <P/>
 * Initial Date: Oct 13, 2004
 * 
 * @author gnaegi
 */
public class CORunController extends BasicController {

    public CORunController(final ContactRunView contactRunView, final ContactRunUIModel contactRunUIModel) {
        super(contactRunView.getUreq(), contactRunView.getWindowControl());

        if (contactRunUIModel.hasRecipients()) {
            // bind view and model
            contactRunView.setShortTitle(contactRunUIModel.getShortTitle());
            contactRunView.setLongTitle(contactRunUIModel.getLongTitle());
            contactRunView.setLearninObjectives(contactRunUIModel.getLearningObjectives());
            contactRunView.setCourseContactMessage(contactRunUIModel.getCourseContactMessage());
        }
        putInitialPanel(contactRunView.getInitialComponent(this));

    }

    /**
	 */
    @Override
    @SuppressWarnings("unused")
    public void event(final UserRequest ureq, final Component source, final Event event) {
        // special case here, that there are no components to listen to
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }

}
