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

package org.olat.presentation.examples.guidemo.cssjs;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.dev.controller.SourceViewController;
import org.olat.system.event.Event;

public class GUIDemoCSSJSController extends BasicController {

    private final VelocityContainer mainVc;
    private final VelocityContainer modalVc1, jsVc1;
    private final VelocityContainer modalVc2, jsVc2;

    private final Link jscssremove;
    private final Link jscss, linkjs1, linkjs2;

    private final Panel jsTestP;

    public GUIDemoCSSJSController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        mainVc = createVelocityContainer("cssjsdemo");

        // the first demo has a css (which must be removed if not used) and a js lib
        modalVc1 = createVelocityContainer("modal1");
        final JSAndCSSComponent jscss1 = new JSAndCSSComponent("jsAndCssForDemo", this.getClass(), new String[] { "js1.js", "js1b.js" }, "style1.css", true);
        modalVc1.put("jsAndCssForDemo", jscss1); // we attach it to the modalComponent, so that it is activated when this component shows up on screen.

        // the second demo has a css (which is not removed even if not used anymore) and a js lib
        modalVc2 = createVelocityContainer("modal2");
        final JSAndCSSComponent jscss2 = new JSAndCSSComponent("jsAndCssForDemo", this.getClass(), new String[] { "js2.js", "js2b.js" }, "style2.css", false);
        modalVc2.put("jsAndCssForDemo", jscss2); // we attach it to the modalComponent, so that it is activated when this component shows up on screen.

        // js functions override test
        linkjs1 = LinkFactory.createButtonSmall("link.js1", mainVc, this);
        jsVc1 = createVelocityContainer("jstest1");
        final JSAndCSSComponent jstest1 = new JSAndCSSComponent("jstest1includes", this.getClass(), new String[] { "jsfuncdef1.js" }, null, false);
        jsVc1.put("jstest1includes", jstest1);

        linkjs2 = LinkFactory.createButtonSmall("link.js2", mainVc, this);
        jsVc2 = createVelocityContainer("jstest2");
        final JSAndCSSComponent jstest2 = new JSAndCSSComponent("jstest1includes", this.getClass(), new String[] { "jsfuncdef2.js" }, null, false);
        jsVc2.put("jstest2includes", jstest2);

        jsTestP = new Panel("jstestP");
        mainVc.put("jstestpanel", jsTestP);

        jscssremove = LinkFactory.createButtonXSmall("link.jscssremove", mainVc, this);
        jscss = LinkFactory.createButtonXSmall("link.jscss", mainVc, this);

        // add source view control
        final Controller sourceview = new SourceViewController(ureq, wControl, this.getClass(), mainVc);
        mainVc.put("sourceview", sourceview.getInitialComponent());

        // let the scripts (.js files) and css files be included when this controller's main component is rendered
        putInitialPanel(mainVc);
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == jscssremove) {
            final CloseableModalController cmc = new CloseableModalController(getWindowControl(),
                    "closing removes the css again, js -libs- are never removed by design, but you can include &lt;script&gt; tags in your velocity pages", modalVc1);
            cmc.activate();
        } else if (source == jscss) {
            final CloseableModalController cmc2 = new CloseableModalController(getWindowControl(),
                    "closing does not remove the css again, js -libs- are never removed by design, but you can include &lt;script&gt; tags in your velocity pages",
                    modalVc2);
            cmc2.activate();
        } else if (source == linkjs1) {
            jsTestP.setContent(jsVc1);
        } else if (source == linkjs2) {
            jsTestP.setContent(jsVc2);
        }
    }

    @Override
    protected void doDispose() {
        //
    }

}
