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

package org.olat.presentation.course.assessment;

import java.util.Date;
import java.util.List;

import org.olat.data.basesecurity.Roles;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.data.portfolio.artefact.EfficiencyStatementArtefact;
import org.olat.lms.course.assessment.EfficiencyStatement;
import org.olat.lms.course.assessment.EfficiencyStatementManager;
import org.olat.lms.portfolio.PortfolioModule;
import org.olat.lms.portfolio.artefacthandler.EPArtefactHandler;
import org.olat.lms.user.UserService;
import org.olat.lms.user.propertyhandler.UserPropertyHandler;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.MainLayoutBasicController;
import org.olat.presentation.framework.core.control.generic.messages.MessageUIFactory;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.presentation.portfolio.artefacts.collect.ArtefactWizzardStepsController;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Displays the users efficiency statement
 * <P>
 * Initial Date: 11.08.2005 <br>
 * 
 * @author gnaegi
 */
public class EfficiencyStatementController extends MainLayoutBasicController {

    private VelocityContainer userDataVC;
    private static final String usageIdentifyer = EfficiencyStatementController.class.getCanonicalName();

    private EfficiencyStatement efficiencyStatement;

    // to collect the eff.Statement as artefact
    private org.olat.presentation.framework.core.components.link.Link collectArtefactLink;
    private PortfolioModule portfolioModule;
    // the collect-artefact-wizard
    private Controller ePFCollCtrl;

    /**
     * Constructor
     * 
     * @param wControl
     * @param ureq
     * @param courseId
     */
    public EfficiencyStatementController(final WindowControl wControl, final UserRequest ureq, final Long courseRepoEntryKey) {
        this(wControl, ureq, EfficiencyStatementManager.getInstance().getUserEfficiencyStatement(courseRepoEntryKey, ureq.getIdentity()));
    }

    public EfficiencyStatementController(final WindowControl wControl, final UserRequest ureq, final EfficiencyStatement efficiencyStatement) {
        super(ureq, wControl);

        // either the efficiency statement or the error message, that no data is available goes to the content area
        Component content = null;

        if (efficiencyStatement != null) {
            this.efficiencyStatement = efficiencyStatement;

            // extract efficiency statement data
            // fallback translation for user properties
            setTranslator(getUserService().getUserPropertiesConfig().getTranslator(getTranslator()));
            userDataVC = createVelocityContainer("efficiencystatement");
            userDataVC.contextPut("courseTitle", StringHelper.escapeHtml(efficiencyStatement.getCourseTitle()) + " ("
                    + efficiencyStatement.getCourseRepoEntryKey().toString() + ")");
            userDataVC.contextPut("user", ureq.getIdentity().getUser());
            userDataVC.contextPut("username", ureq.getIdentity().getName());
            userDataVC.contextPut("date", StringHelper.formatLocaleDateTime(efficiencyStatement.getLastUpdated(), ureq.getLocale()));

            final Roles roles = ureq.getUserSession().getRoles();
            final boolean isAdministrativeUser = roles.isAdministrativeUser();
            final List<UserPropertyHandler> userPropertyHandlers = getUserService().getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
            userDataVC.contextPut("userPropertyHandlers", userPropertyHandlers);

            final Controller identityAssessmentCtr = new IdentityAssessmentOverviewController(ureq, wControl, efficiencyStatement.getAssessmentNodes());
            listenTo(identityAssessmentCtr);// dispose it when this one is disposed
            userDataVC.put("assessmentOverviewTable", identityAssessmentCtr.getInitialComponent());

            // add link to collect efficiencyStatement as artefact
            portfolioModule = (PortfolioModule) CoreSpringFactory.getBean("portfolioModule");
            EPArtefactHandler<?> artHandler = portfolioModule.getArtefactHandler(EfficiencyStatementArtefact.ARTEFACT_TYPE);
            if (portfolioModule.isEnabled() && artHandler != null && artHandler.isEnabled()) {
                collectArtefactLink = org.olat.presentation.framework.core.components.link.LinkFactory.createCustomLink("collectArtefactLink", "collectartefact", "",
                        org.olat.presentation.framework.core.components.link.Link.NONTRANSLATED, userDataVC, this);
                collectArtefactLink.setCustomEnabledLinkCSS("b_eportfolio_add_again");
            }

            content = userDataVC;
        } else {
            // message, that no data is available. This may happen in the case the "open efficiency" link is available, while in the meantime an author
            // disabled the efficiency statement.
            final String text = translate("efficiencystatement.nodata");
            final Controller messageCtr = MessageUIFactory.createErrorMessage(ureq, wControl, null, text);
            listenTo(messageCtr);// gets disposed as this controller gets disposed.
            content = messageCtr.getInitialComponent();
        }
        // Content goes to a 3 cols layout without left and right column
        final LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), null, null, content, null);
        listenTo(layoutCtr);
        putInitialPanel(layoutCtr.getInitialComponent());

    }

    /**
	 */
    @Override
    @SuppressWarnings("unused")
    public void event(final UserRequest ureq, final Component source, final Event event) {
        if (source.equals(collectArtefactLink)) {
            popupArtefactCollector(ureq);
        }
    }

    /**
     * opens the collect-artefact wizard
     * 
     * @param ureq
     */
    private void popupArtefactCollector(UserRequest ureq) {
        EPArtefactHandler<?> artHandler = portfolioModule.getArtefactHandler(EfficiencyStatementArtefact.ARTEFACT_TYPE);
        if (artHandler != null && artHandler.isEnabled()) {
            AbstractArtefact artefact = artHandler.createArtefact();
            artefact.setAuthor(getIdentity());// only author can create artefact
            // no business path becouse we cannot launch an efficiency statement
            artefact.setCollectionDate(new Date());
            artefact.setTitle(translate("artefact.title", new String[] { efficiencyStatement.getCourseTitle() }));
            artHandler.prefillArtefactAccordingToSource(artefact, efficiencyStatement);
            ePFCollCtrl = new ArtefactWizzardStepsController(ureq, getWindowControl(), artefact, (VFSContainer) null);
            listenTo(ePFCollCtrl);

            // set flag for js-window-resizing (see velocity)
            userDataVC.contextPut("collectwizard", true);
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
