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
package org.olat.presentation.portfolio.structel;

import java.util.ArrayList;
import java.util.List;

import org.olat.data.portfolio.structure.EPPage;
import org.olat.data.portfolio.structure.PortfolioStructure;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.lms.portfolio.security.EPSecurityCallback;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.portfolio.structel.run.EPTOCReadOnlyController;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * shows multiple pages in a map and handles the paging of them
 * <P>
 * Initial Date: 23.08.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPMultiplePageController extends BasicController {

    private final List<PortfolioStructure> pageList;
    private final List<Long> pageListByKeys;
    private Controller pageCtrl;
    private int previousPage;
    private final VelocityContainer vC;
    private final EPSecurityCallback secCallback;
    private final EPFrontendManager ePFMgr;

    public EPMultiplePageController(final UserRequest ureq, final WindowControl wControl, final List<PortfolioStructure> pageList, final EPSecurityCallback secCallback) {
        super(ureq, wControl);
        this.pageList = pageList;
        this.pageListByKeys = new ArrayList<Long>(pageList.size());
        this.secCallback = secCallback;
        ePFMgr = CoreSpringFactory.getBean(EPFrontendManager.class);

        vC = createVelocityContainer("multiPages");

        init(ureq);

        putInitialPanel(vC);
    }

    public EPPage getSelectedPage() {
        if (pageCtrl instanceof EPPageViewController) {
            return ((EPPageViewController) pageCtrl).getPage();
        }
        return null;
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    protected void init(final UserRequest ureq) {
        if (pageList != null && pageList.size() != 0) {

            // create toc link
            final Link tocLink = LinkFactory.createLink("toc", vC, this);
            tocLink.setUserObject(-1);

            int i = 1;
            final ArrayList<Link> pageLinkList = new ArrayList<Link>();
            for (final PortfolioStructure page : pageList) {
                pageListByKeys.add(page.getKey());
                final String pageTitle = StringHelper.escapeHtml(((EPPage) page).getTitle());
                final String shortPageTitle = Formatter.truncate(pageTitle, 20);
                final Link pageLink = LinkFactory.createCustomLink("pageLink" + i, "pageLink" + i, shortPageTitle, Link.LINK + Link.NONTRANSLATED, vC, this);
                pageLink.setUserObject(i - 1);
                pageLink.setTooltip(pageTitle, false);
                pageLinkList.add(pageLink);
                i++;
            }
            vC.contextPut("pageLinkList", pageLinkList);
            setAndInitActualPage(ureq, -1, false);
        }
    }

    protected void selectPage(final UserRequest ureq, final PortfolioStructure page) {
        int count = 0;
        for (final PortfolioStructure structure : pageList) {
            if (structure.getKey().equals(page.getKey())) {
                setAndInitActualPage(ureq, count, false);
                break;
            }
            count++;
        }
    }

    private void setAndInitActualPage(final UserRequest ureq, final int pageNum, final boolean withComments) {
        removeAsListenerAndDispose(pageCtrl);
        if (pageNum == -1) {
            // this is the toc
            final EPPage page = (EPPage) pageList.get(0);
            final PortfolioStructure map = ePFMgr.loadStructureParent(page);
            pageCtrl = new EPTOCReadOnlyController(ureq, getWindowControl(), map, secCallback);
            // disable toc-link
            enDisableTOC(false);
        } else {
            final EPPage page = (EPPage) pageList.get(pageNum);
            final PortfolioStructure map = ePFMgr.loadStructureParent(page);
            pageCtrl = new EPPageViewController(ureq, getWindowControl(), map, page, withComments, secCallback);
            // enable toc-link
            enDisableTOC(true);
        }

        vC.put("pageCtrl", pageCtrl.getInitialComponent());
        vC.contextPut("actualPage", pageNum + 1);
        listenTo(pageCtrl);
        // disable actual page itself
        final Link actLink = (Link) vC.getComponent("pageLink" + String.valueOf((pageNum + 1)));
        if (actLink != null) {
            actLink.setEnabled(false);
        }
        // enable previous page
        final Link prevLink = (Link) vC.getComponent("pageLink" + String.valueOf((previousPage)));
        if (prevLink != null) {
            prevLink.setEnabled(true);
        }
        previousPage = pageNum + 1;
    }

    private void enDisableTOC(final boolean enabled) {
        final Link tocLink = (Link) vC.getComponent("toc");
        if (tocLink != null) {
            tocLink.setEnabled(enabled);
        }
        vC.contextPut("tocactive", !enabled);
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, @SuppressWarnings("unused") final Event event) {
        if (source instanceof Link) {
            final Link link = (Link) source;
            final int pageNum = (Integer) link.getUserObject();
            setAndInitActualPage(ureq, pageNum, false);
        }

    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        super.event(ureq, source, event);
        if (source == pageCtrl) {
            if (source instanceof EPTOCReadOnlyController) {
                // activate selected structure from toc!
                if (event instanceof EPStructureEvent) {
                    final EPStructureEvent epEv = (EPStructureEvent) event;
                    final PortfolioStructure selStruct = epEv.getStructure();
                    if (event.getCommand().equals(EPStructureEvent.SELECT)) {
                        findAndActivatePage(ureq, selStruct, false);
                    } else if (event.getCommand().equals(EPStructureEvent.SELECT_WITH_COMMENTS)) {
                        findAndActivatePage(ureq, selStruct, true);
                    }
                }
            }
        }
    }

    private void findAndActivatePage(final UserRequest ureq, final PortfolioStructure selStruct, final boolean withComments) {
        if (pageListByKeys.contains(selStruct.getKey())) {
            final int pos = pageListByKeys.indexOf(selStruct.getKey());
            if (pos != -1) {
                setAndInitActualPage(ureq, pos, withComments);
            }
        } else {
            // lookup parents, as this could be an artefact or a structureElement
            final PortfolioStructure parentStruct = ePFMgr.loadStructureParent(selStruct);
            if (parentStruct != null) {
                findAndActivatePage(ureq, parentStruct, withComments);
            }
        }

    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing
    }

}
