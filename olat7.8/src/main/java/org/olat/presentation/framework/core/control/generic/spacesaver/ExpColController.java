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

package org.olat.presentation.framework.core.control.generic.spacesaver;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;

/**
 * Description: An expand / collapse controller which shows/hides a component upon click on a given link
 * 
 * @author Felix Jost
 */
public class ExpColController extends BasicController {
    private boolean expanded;
    private Component content;

    private VelocityContainer myContent;
    private Link providedLink;

    /**
     * @param ureq
     * @param initiallyExpanded
     * @param content
     * @param the
     *            link to click (e.g "Details"). may -not- have a listener yet
     */
    public ExpColController(UserRequest ureq, WindowControl wControl, boolean initiallyExpanded, Component content, Link link) {
        super(ureq, wControl);
        this.expanded = initiallyExpanded;
        this.providedLink = link;
        this.content = content;

        myContent = createVelocityContainer("index");
        myContent.put("link", link);
        myContent.put("content", content);

        content.setVisible(initiallyExpanded);
        link.addListener(this);

        putInitialPanel(myContent);
    }

    /**
	 */
    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        if (source == providedLink) {
            toggleUi();
        }
    }

    /**
     * Toggle the UI depending on the current state
     */
    public void toggleUi() {
        expanded = !expanded;
        updateUI();
    }

    private void updateUI() {
        // myContent.contextPut("islarge", isLarge? Boolean.TRUE : Boolean.FALSE);
        content.setVisible(expanded);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to do yet
    }

}
