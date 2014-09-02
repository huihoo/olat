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

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.messages.MessageController;
import org.olat.presentation.framework.core.control.generic.messages.MessageUIFactory;
import org.olat.system.commons.Formatter;
import org.olat.system.event.Event;

/**
 * Description: <BR>
 * First implementation of a news controller
 * <P>
 * Initial Date: Aug 23, 2004
 * 
 * @author patrick
 */

public class SimpleNewsController extends BasicController {

    private MessageController mc;

    /**
     * Constructor for a news controller
     * 
     * @param ureq
     *            The user request
     * @param wControl
     *            The window control
     * @param htmlText
     *            a html-formatted text to be displayed
     */
    public SimpleNewsController(final UserRequest ureq, final WindowControl wControl, String htmlText) {
        super(ureq, wControl);
        htmlText = Formatter.formatLatexFormulas(htmlText);
        mc = MessageUIFactory.createInfoMessage(ureq, wControl, null, htmlText);
        putInitialPanel(mc.getInitialComponent());
    }

    /**
	 */
    @Override
    @SuppressWarnings("unused")
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        //
    }

    /**
	 */
    @Override
    @SuppressWarnings("unused")
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        //
    }

    /**
	 */
    @Override
    protected void doDispose() {
        if (mc != null) {
            mc.dispose();
            mc = null;
        }
    }

}
