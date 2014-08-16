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

package org.olat.presentation.wiki;

import java.util.LinkedList;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * Keeps track of a list of visited elements like links. It's realized as a queue where the recently visited element will be on top. It fires an event when a user clicks
 * on an element of the queue. The event is forwarded to the listening controller.
 * <P>
 * Initial Date: Jul 4, 2006 <br>
 * 
 * @author guido
 */
public class BreadCrumbController extends BasicController {

    private static final Object ACTION_GO = "go";
    private static final int VISIBLE_ELEMENTS = 5;
    private final VelocityContainer content = createVelocityContainer("breadcrump");
    private LinkedList<Crumb> queue = new LinkedList<Crumb>();

    public BreadCrumbController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        content.contextPut("queue", queue);
        putInitialPanel(content);
    }

    public void addLink(final String name, final String desc) {
        final Crumb crumb = queue.peek();
        if (crumb == null) {
            queue.addFirst(new Crumb(name, desc));
        } else {
            if (!crumb.getName().equals(name)) {
                queue.addFirst(new Crumb(name, desc));
            }
        }
        final int size = queue.size();
        content.contextPut("queue", queue.subList(0, size < VISIBLE_ELEMENTS ? size : VISIBLE_ELEMENTS));

    }

    /**
     * should be called if a link should no longer be part of the queue
     * 
     * @param name
     *            of the link
     */
    public void removeLink(final String name) {
        if (name != null) {
            // create copy frist, to prevent concurrent mod. exception
            final LinkedList<Crumb> queueCopy = new LinkedList<Crumb>(queue);
            for (final Crumb crumb : queueCopy) {
                if (crumb.getName().equals(name)) {
                    queue.remove(crumb);
                }
            }

            final int size = queue.size();
            content.contextPut("queue", queue.subList(0, size < VISIBLE_ELEMENTS ? size : VISIBLE_ELEMENTS));
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        final String command = event.getCommand();
        if (source == content) {
            if (command.equals(ACTION_GO)) {
                fireEvent(ureq, new Event(ureq.getModuleURI()));
            }
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // clean up
        queue.clear();
        queue = null;
    }

}
