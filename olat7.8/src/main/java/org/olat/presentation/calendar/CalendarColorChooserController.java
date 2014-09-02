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

package org.olat.presentation.calendar;

import java.util.HashMap;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;

public class CalendarColorChooserController extends BasicController {

    private static final String SELECTED_COLOR_CSS = "o_cal_colorchooser_selected";

    private final VelocityContainer colorVC;
    private String choosenColor;
    private final HashMap colorLinks;
    private final Link cancelButton;

    public CalendarColorChooserController(final UserRequest ureq, final WindowControl wControl, final String currentCssSelection) {
        super(ureq, wControl);

        colorVC = createVelocityContainer("calColor");

        cancelButton = LinkFactory.createButton("cancel", colorVC, this);

        colorLinks = new HashMap();
        final Link greenLink = LinkFactory.createCustomLink("greenLink", "selc", "", Link.NONTRANSLATED, colorVC, this);
        if (currentCssSelection.equals("o_cal_green")) {
            greenLink.setCustomEnabledLinkCSS(SELECTED_COLOR_CSS);
            greenLink.setCustomDisabledLinkCSS(SELECTED_COLOR_CSS);
        }
        final Link blueLink = LinkFactory.createCustomLink("blueLink", "selc", "", Link.NONTRANSLATED, colorVC, this);
        if (currentCssSelection.equals("o_cal_blue")) {
            blueLink.setCustomEnabledLinkCSS(SELECTED_COLOR_CSS);
            blueLink.setCustomDisabledLinkCSS(SELECTED_COLOR_CSS);
        }
        final Link orangeLink = LinkFactory.createCustomLink("orangeLink", "selc", "", Link.NONTRANSLATED, colorVC, this);
        if (currentCssSelection.equals("o_cal_orange")) {
            orangeLink.setCustomEnabledLinkCSS(SELECTED_COLOR_CSS);
            orangeLink.setCustomDisabledLinkCSS(SELECTED_COLOR_CSS);
        }
        final Link yellowLink = LinkFactory.createCustomLink("yellowLink", "selc", "", Link.NONTRANSLATED, colorVC, this);
        if (currentCssSelection.equals("o_cal_yellow")) {
            yellowLink.setCustomEnabledLinkCSS(SELECTED_COLOR_CSS);
            yellowLink.setCustomDisabledLinkCSS(SELECTED_COLOR_CSS);
        }
        final Link redLink = LinkFactory.createCustomLink("redLink", "selc", "", Link.NONTRANSLATED, colorVC, this);
        if (currentCssSelection.equals("o_cal_red")) {
            redLink.setCustomEnabledLinkCSS(SELECTED_COLOR_CSS);
            redLink.setCustomDisabledLinkCSS(SELECTED_COLOR_CSS);
        }
        final Link greyLink = LinkFactory.createCustomLink("greyLink", "selc", "", Link.NONTRANSLATED, colorVC, this);
        if (currentCssSelection.equals("o_cal_grey")) {
            greyLink.setCustomEnabledLinkCSS(SELECTED_COLOR_CSS);
            greyLink.setCustomDisabledLinkCSS(SELECTED_COLOR_CSS);
        }

        colorLinks.put(greenLink, "o_cal_green");
        colorLinks.put(blueLink, "o_cal_blue");
        colorLinks.put(orangeLink, "o_cal_orange");
        colorLinks.put(yellowLink, "o_cal_yellow");
        colorLinks.put(redLink, "o_cal_red");
        colorLinks.put(greyLink, "o_cal_grey");

        putInitialPanel(colorVC);
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == cancelButton) {
            fireEvent(ureq, Event.CANCELLED_EVENT);
        } else if (colorLinks.containsKey(source)) {
            choosenColor = (String) colorLinks.get(source);
            final Link colorLink = (Link) source;
            colorLink.setCustomEnabledLinkCSS(choosenColor);
            colorLink.setCustomDisabledLinkCSS(choosenColor);
            fireEvent(ureq, Event.DONE_EVENT);
        }
    }

    public String getChoosenColor() {
        return choosenColor;
    }

    @Override
    protected void doDispose() {
        // nothing to dispose
    }

}
