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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.presentation.webfeed.navigation;

import java.util.ArrayList;
import java.util.List;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.event.Event;

/**
 * Displays a navigation tool for Dated objects sorted by month. Like this:
 * 
 * <pre>
 * &lt;&lt; 2009 &gt;&gt;
 * August (3)
 * July (12)
 * June (29)
 * ...
 * </pre>
 * 
 * Fires: NavigationEvent
 * <P>
 * Initial Date: Aug 12, 2009 <br>
 * 
 * @author gwassmann
 */
public class YearNavigationController extends BasicController {

    private YearNavigationModel model;
    private Link next, previous, yearLink;
    private VelocityContainer mainVC;
    private List<Link> monthLinks;
    private List<? extends Dated> allObjects;
    private boolean showAll = true;

    /**
     * Constructor based on a list of <code>Dated</code> objects.
     * 
     * @param ureq
     * @param control
     * @param fallBackTranslator
     * @param datedObjects
     */
    public YearNavigationController(UserRequest ureq, WindowControl control, Translator fallBackTranslator, List<? extends Dated> datedObjects) {
        super(ureq, control, fallBackTranslator);
        // Create the model
        model = new YearNavigationModel(datedObjects, ureq.getLocale());
        allObjects = datedObjects;
        showAll = true;
        //
        mainVC = createVelocityContainer("yearnavigation");
        next = LinkFactory.createCustomLink("navi.forward", "navi.forward", null, Link.NONTRANSLATED, mainVC, this);
        next.setCustomEnabledLinkCSS("b_small_icon b_forward_icon b_float_right");
        next.setCustomDisabledLinkCSS("b_small_icon b_forward_icon b_float_right"); // b_disabled added by link component
        next.setTooltip(translate("navi.forward"), false);
        previous = LinkFactory.createCustomLink("navi.backward", "navi.backward", null, Link.NONTRANSLATED, mainVC, this);
        previous.setCustomEnabledLinkCSS("b_small_icon b_backward_icon b_float_left");
        previous.setCustomDisabledLinkCSS("b_small_icon b_backward_icon b_float_left");
        previous.setTooltip(translate("navi.backward"), false);
        createLinks();
        this.putInitialPanel(mainVC);
    }

    /**
     * Creates the year and month links to be displayed
     */
    private void createLinks() {
        Year year = model.getCurrentYear();
        if (year != null) {
            yearLink = LinkFactory.createLink("yearLink", mainVC, this);
            yearLink.setCustomEnabledLinkCSS("b_year");
            yearLink.setCustomDisplayText(year.getName());
            yearLink.setUserObject(year);
            mainVC.contextPut("year", year);
            // Reestablish month links
            monthLinks = new ArrayList<Link>();
            for (Month month : year.getMonths()) {
                Link monthLink = LinkFactory.createLink("month_" + month.getName(), mainVC, this);
                monthLink.setCustomEnabledLinkCSS("b_month");
                monthLink.setCustomDisplayText(model.getMonthName(month));
                monthLink.setUserObject(month);
                monthLinks.add(monthLink);
            }
            // enable/disable the navigation links
            next.setEnabled(model.hasNext());
            previous.setEnabled(model.hasPrevious());
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing so far
    }

    /**
	 */
    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        if (source == next) {
            model.next();
            createLinks();
            Year year = (Year) yearLink.getUserObject();
            Event navEvent = new NavigationEvent(year.getItems());
            fireEvent(ureq, navEvent);
            yearLink.setCustomEnabledLinkCSS("b_year b_selected");

        } else if (source == previous) {
            model.previous();
            createLinks();
            Year year = (Year) yearLink.getUserObject();
            Event navEvent = new NavigationEvent(year.getItems());
            fireEvent(ureq, navEvent);
            yearLink.setCustomEnabledLinkCSS("b_year b_selected");

        } else if (source == yearLink) {
            // Click on year toggles between year filter and show all filter
            if (showAll) {
                Year year = (Year) yearLink.getUserObject();
                Event navEvent = new NavigationEvent(year.getItems());
                fireEvent(ureq, navEvent);
                // update GUI
                yearLink.setCustomEnabledLinkCSS("b_year b_selected");
                for (Link monthLink : monthLinks) {
                    monthLink.setCustomEnabledLinkCSS("b_month");
                }
                showAll = false;
            } else {
                Event navEvent = new NavigationEvent(allObjects);
                fireEvent(ureq, navEvent);
                // update GUI
                yearLink.setCustomEnabledLinkCSS("b_year");
                showAll = true;
            }

        } else if (monthLinks.contains(source)) {
            Link monthLink = (Link) source;
            Month month = (Month) monthLink.getUserObject();
            Event navEvent = new NavigationEvent(month.getItems());
            fireEvent(ureq, navEvent);
            // update GUI
            yearLink.setCustomEnabledLinkCSS("b_year");
            for (Link link : monthLinks) {
                link.setCustomEnabledLinkCSS("b_month");
            }
            monthLink.setCustomEnabledLinkCSS("b_month b_selected");
        }
    }

    public void setDatedObjects(List<? extends Dated> datedObjects) {
        model.setDatedObjects(datedObjects);
        createLinks();
    }

    /**
     * Adds the item to the model
     * 
     * @param item
     */
    public void add(Dated item) {
        model.add(item);
        createLinks();
    }

    /**
     * Removes the item from the model
     * 
     * @param item
     */
    public void remove(Dated item) {
        model.remove(item);
        mainVC.setDirty(true);
    }

}
