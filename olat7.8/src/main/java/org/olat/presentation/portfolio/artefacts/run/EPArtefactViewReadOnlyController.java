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
package org.olat.presentation.portfolio.artefacts.run;

import java.util.ArrayList;
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.filter.FilterFactory;
import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.data.portfolio.structure.PortfolioStructure;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.lms.portfolio.security.EPSecurityCallback;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalWindowWrapperController;
import org.olat.presentation.portfolio.EPUIFactory;
import org.olat.presentation.portfolio.structel.EPStructureChangeEvent;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * simple artefact read-only controller
 * <P>
 * Initial Date: 17.11.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPArtefactViewReadOnlyController extends BasicController {

    private final VelocityContainer vC;
    private final EPFrontendManager ePFMgr;
    private Link detailsLink;
    private final AbstractArtefact artefact;
    private PortfolioStructure struct;
    private final EPSecurityCallback secCallback;
    private Link unlinkLink;

    protected EPArtefactViewReadOnlyController(final UserRequest ureq, final WindowControl wControl, final AbstractArtefact artefact,
            final EPSecurityCallback secCallback, final PortfolioStructure struct) {
        super(ureq, wControl);
        this.artefact = artefact;
        this.struct = struct;
        this.secCallback = secCallback;
        vC = createVelocityContainer("smallSingleArtefact");
        vC.contextPut("artefact", artefact);
        final Identity artIdent = artefact.getAuthor();
        final String fullName = getUserService().getFirstAndLastname(artIdent.getUser());

        String description = FilterFactory.getHtmlTagAndDescapingFilter().filter(artefact.getDescription());
        description = FilterFactory.filterXSS(description);
        description = Formatter.truncate(description, 50);
        vC.contextPut("description", description);
        vC.contextPut("authorName", StringHelper.escapeHtml(fullName));
        if (secCallback.canView()) {
            detailsLink = LinkFactory.createCustomLink("small.details.link", "open", "small.details.link", Link.LINK, vC, this);
        }
        if (secCallback.canRemoveArtefactFromStruct()) {
            unlinkLink = LinkFactory.createCustomLink("unlink.link", "remove", "table.header.unlink", Link.LINK, vC, this);
        }

        ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean(EPFrontendManager.class);
        final List<String> tags = ePFMgr.getArtefactTags(artefact);

        List<String> escapedTags = new ArrayList<String>(tags.size());
        for (String tag : tags) {
            escapedTags.add(StringHelper.escapeHtml(tag));
        }
        vC.contextPut("tags", StringHelper.formatAsCSVString(escapedTags));

        putInitialPanel(vC);
    }

    /**
	 */
    @SuppressWarnings("unused")
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == detailsLink && secCallback.canView()) {
            final String title = translate("view.artefact.header");
            final CloseableModalWindowWrapperController artDetails = EPUIFactory.getAndActivatePopupArtefactController(artefact, ureq, getWindowControl(), title);
            listenTo(artDetails);
        } else if (source == unlinkLink) {
            struct = ePFMgr.removeArtefactFromStructure(artefact, struct);
            fireEvent(ureq, new EPStructureChangeEvent(EPStructureChangeEvent.ADDED, struct)); // refresh ui
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
