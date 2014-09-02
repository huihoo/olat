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
package org.olat.presentation.examples.guidemo;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.dev.controller.SourceViewController;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * TODO: guido Class Description for GuiDemoTooltipsController
 * <P>
 * Initial Date: 19.06.2007 <br>
 * 
 * @author guido
 */
public class GuiDemoTooltipsController extends BasicController {

    private final VelocityContainer content = createVelocityContainer("tooltips");
    private final VelocityContainer tooltipContent = createVelocityContainer("tooltipContent");
    private final Link link4;

    public GuiDemoTooltipsController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);

        final Link button = LinkFactory.createButton("button", content, this);
        button.setTooltip("tooltip.content", false);
        final Link linkT = LinkFactory.createLink("linkT", content, this);
        linkT.setTooltip("tooltip.content", true);
        final Link link1 = LinkFactory.createLink("link1", content, this);
        link1.setTooltip("tooltip.content", false);
        final Link link2 = LinkFactory.createLink("link2", content, this);
        link2.setTooltip("tooltip.content", true);

        // link with component content

        /**
         * works when passing a custom html id like id="blalala" and then fetching the content from this id with getElementById("id").innerHTML but with the component the
         * component is not invisible and gets renderer somewhere and somehow not
         */

        final Link link3 = LinkFactory.createLink("link3", content, this);
        link3.setTooltip(tooltipContent, true);
        content.put("tooltipContent", tooltipContent);
        link4 = LinkFactory.createLink("link4", tooltipContent, this);

        // add source view control
        final Controller sourceview = new SourceViewController(ureq, wControl, this.getClass(), content);
        content.put("sourceview", sourceview.getInitialComponent());

        putInitialPanel(content);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // TODO Auto-generated method stub

    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == link4) {
            getWindowControl().setInfo("You clicked a link rendered in a tooltip!");
            link4.setDirty(true);
        }

    }

}
