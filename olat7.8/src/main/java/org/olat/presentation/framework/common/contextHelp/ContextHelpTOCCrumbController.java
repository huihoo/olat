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
package org.olat.presentation.framework.common.contextHelp;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.lms.framework.common.contexthelp.ContextHelpModule;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.breadcrumb.CrumbBasicController;
import org.olat.presentation.framework.core.control.generic.dtabs.Activateable;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * The table of contents crumb controller shows the list of available context help pages. The pages are rendered as links. If clicked, they will open up as a new crumb on
 * the bread crumb path
 * <p>
 * The controller can be activated with a page identifyer, e.g. with <code>"org.olat.presentation:my-helppage.html"</code>
 * <P>
 * Initial Date: 04.11.2008 <br>
 * 
 * @author gnaegi
 */
class ContextHelpTOCCrumbController extends CrumbBasicController implements Activateable {
    private VelocityContainer tocVC;
    private List<String> pageIdentifyers;
    private List<Link> pageLinks;

    /**
     * Constructor to create a new context help table of contents controller
     * 
     * @param ureq
     * @param control
     * @param displayLocale
     */
    protected ContextHelpTOCCrumbController(UserRequest ureq, WindowControl control, Locale displayLocale) {
        super(ureq, control);
        setLocale(displayLocale, true);
        tocVC = createVelocityContainer("contexthelptoc");

        pageIdentifyers = new ArrayList<String>();
        pageLinks = new ArrayList<Link>();
        pageIdentifyers.addAll(ContextHelpModule.getAllContextHelpPages());

        sortToc();

        tocVC.contextPut("pageIdentifyers", pageIdentifyers);

        putInitialPanel(tocVC);
    }

    /**
	 */
    @Override
    public String getCrumbLinkHooverText() {
        return translate("contexthelp.crumb.toc.hover");
    }

    /**
	 */
    @Override
    public String getCrumbLinkText() {
        return translate("contexthelp.crumb.toc");
    }

    /**
	 */
    @Override
    protected void doDispose() {
        tocVC = null;
        pageIdentifyers = null;
    }

    /**
	 */
    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        if (source instanceof Link) {
            Link link = (Link) source;
            // Get requested page from link
            String pageIdentifyer = (String) link.getUserObject();
            activate(ureq, pageIdentifyer);
        }
    }

    /**
     * Change the locale for this view
     * 
     * @param locale
     * @param ureq
     */
    public void setLocale(Locale locale, UserRequest ureq) {
        if (tocVC == null) {
            // already disposed
            return;
        }
        // Update locale for subsequent requests
        setLocale(locale, true);

        sortToc();

        // Update title in view
        tocVC.setDirty(true);
        // Update next crumb in chain
        ContextHelpPageCrumbController child = (ContextHelpPageCrumbController) getChildCrumbController();
        if (child != null) {
            child.setLocale(locale, ureq);
        }
    }

    private void sortToc() {

        pageLinks.clear();

        for (int i = 0; i < pageIdentifyers.size(); i++) {
            String pageIdentifyer = pageIdentifyers.get(i);
            int splitPos = pageIdentifyer.indexOf(":");
            String bundleName = pageIdentifyer.substring(0, splitPos);
            String page = pageIdentifyer.substring(splitPos + 1);
            PackageTranslator pageTans = new PackageTranslator(bundleName, getLocale());
            String titleKey = "chelp." + page.split("\\.")[0] + ".title";
            String pageTitle = pageTans.translate(titleKey);
            Link link = LinkFactory.createLink(i + "", tocVC, this);
            link.setCustomDisplayText(pageTitle);
            link.setUserObject(pageIdentifyer);
            pageLinks.add(link);
        }

        final Collator c = Collator.getInstance(getLocale());
        c.setStrength(Collator.TERTIARY);
        Collections.sort(pageLinks, new Comparator<Link>() {
            public int compare(Link firstLink, Link secondLink) {
                return c.compare(firstLink.getCustomDisplayText(), secondLink.getCustomDisplayText());
            }
        });

        for (int i = 0; i < pageLinks.size(); i++) {
            tocVC.put(i + "", pageLinks.get(i));
        }
    }

    /**
	 */
    @Override
    public void activate(UserRequest ureq, String pageIdentifyer) {
        int splitPos = pageIdentifyer.indexOf(":");
        String bundleName = pageIdentifyer.substring(0, splitPos);
        String page = pageIdentifyer.substring(splitPos + 1);
        // Add new crumb controller now. Old one is disposed automatically
        ContextHelpPageCrumbController pageCrumController = new ContextHelpPageCrumbController(ureq, getWindowControl(), bundleName, page, getLocale());
        activateAndListenToChildCrumbController(pageCrumController);
    }

}
