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

package org.olat.presentation.course.statistic;

import org.olat.lms.course.ICourse;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.layout.GenericMainController;
import org.olat.presentation.framework.core.control.generic.tool.ToolController;
import org.olat.presentation.framework.core.control.generic.tool.ToolFactory;
import org.olat.presentation.framework.extensions.action.ActionExtension;
import org.olat.system.event.Event;

/**
 * Initial Date: 03.12.2009 <br>
 * 
 * @author bja
 */
public class StatisticMainController extends GenericMainController {

    private final ToolController toolC;
    private final ICourse course;

    public StatisticMainController(final UserRequest ureq, final WindowControl windowControl, final ICourse course) {
        super(ureq, windowControl);
        this.course = course;

        // Tool and action box
        toolC = ToolFactory.createToolController(getWindowControl());
        listenTo(toolC);
        toolC.addHeader(translate("tool.name"));
        toolC.addLink("cmd.close", translate("command.closestatistic"), null, "b_toolbox_close");
        setToolController(toolC);

        init(ureq);
    }

    @Override
    protected void doDispose() {
        // controllers disposed by BasicController:
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == toolC) {
            if (event.getCommand().equals("cmd.close")) {
                doDispose();
                fireEvent(ureq, Event.DONE_EVENT);
            }
        }
    }

    @Override
    protected Controller createController(final ActionExtension ae, final UserRequest ureq) {
        if (ae instanceof StatisticActionExtension) {
            final StatisticActionExtension sae = (StatisticActionExtension) ae;
            return sae.createController(ureq, getWindowControl(), course);
        } else {
            return super.createController(ae, ureq);
        }
    }

    @Override
    protected Controller handleOwnMenuTreeEvent(final Object uobject, final UserRequest ureq) {
        // no own tree events to handle
        return null;
    }

}
