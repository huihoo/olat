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
package org.olat.presentation.portfolio.structel.edit;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.data.portfolio.restriction.CollectRestriction;
import org.olat.data.portfolio.structure.PortfolioStructure;
import org.olat.lms.portfolio.PortfolioAbstractHandler;
import org.olat.lms.portfolio.artefacthandler.EPArtefactHandler;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.spacesaver.ToggleBoxController;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.portfolio.filter.PortfolioFilterController;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Small controller with show the error message for the collect restriction.
 * <P>
 * Initial Date: 13 oct. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EPCollectRestrictionResultController extends BasicController {

    private final VelocityContainer vc;
    private final VelocityContainer mainVc;
    private ToggleBoxController errorBoxController;
    private final PortfolioStructure structureEl;

    public EPCollectRestrictionResultController(final UserRequest ureq, final WindowControl wControl, final PortfolioStructure structureEl) {
        super(ureq, wControl);

        this.structureEl = structureEl;

        vc = createVelocityContainer("restrictions_msg");
        mainVc = createVelocityContainer("restrictions_msg_wrapper");
        errorBoxController = new ToggleBoxController(ureq, getWindowControl(), "ep-restrictions-" + structureEl.getKey(), "  ", " ", vc);
        listenTo(errorBoxController);
        mainVc.put("description", errorBoxController.getInitialComponent());
        putInitialPanel(mainVc);
    }

    public void setMessage(final UserRequest ureq, final List<CollectRestriction> restrictions, final boolean passed) {
        final List<String> errors = new ArrayList<String>();
        for (final CollectRestriction restriction : restrictions) {
            final String error = getMessage(restriction, getTranslator(), null);
            errors.add(error);
        }

        final Boolean passedObj = new Boolean(passed);
        final Object currentStatus = vc.getContext().get("restrictionsPassed");
        if (Boolean.TRUE.equals(currentStatus) && Boolean.FALSE.equals(passedObj)) {
            removeAsListenerAndDispose(errorBoxController);
            errorBoxController = new ToggleBoxController(ureq, getWindowControl(), "ep-restrictions-" + structureEl.getKey(), "  ", " ", vc);
            listenTo(errorBoxController);
        }
        vc.contextPut("messages", errors);
        vc.contextPut("restrictionsPassed", passedObj);
        mainVc.contextPut("restrictionsPassed", passedObj);
        mainVc.setDirty(true);
    }

    private String getMessage(final CollectRestriction restriction, Translator translator, final Locale locale) {
        if (translator == null) {
            translator = PackageUtil.createPackageTranslator(EPCollectRestrictionResultController.class, locale);
        }
        final String[] args = getMessageArgs(restriction, translator);
        return translator.translate("restriction.error", args);
    }

    private String[] getMessageArgs(final CollectRestriction restriction, final Translator translator) {
        final String[] args = new String[3];
        args[0] = translator.translate("restriction." + restriction.getRestriction());
        final PortfolioAbstractHandler portfolioModule = (PortfolioAbstractHandler) CoreSpringFactory.getBean(PortfolioAbstractHandler.class);
        final EPArtefactHandler<?> handler = portfolioModule.getArtefactHandler(restriction.getArtefactType());
        if (handler != null) {
            final String handlerClass = PortfolioFilterController.HANDLER_PREFIX + handler.getClass().getSimpleName() + PortfolioFilterController.HANDLER_TITLE_SUFFIX;
            args[1] = handler.getHandlerTranslator(translator).translate(handlerClass);
        } else {
            args[1] = translator.translate("restriction.handler.unknown");
        }
        args[2] = Integer.toString(restriction.getAmount());
        return args;
    }

    @Override
    protected void doDispose() {
        //
    }

    @Override
    @SuppressWarnings("unused")
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        //
    }

}
