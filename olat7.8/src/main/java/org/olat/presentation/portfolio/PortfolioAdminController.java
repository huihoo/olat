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
package org.olat.presentation.portfolio;

import java.util.ArrayList;
import java.util.List;

import org.olat.lms.portfolio.PortfolioModule;
import org.olat.lms.portfolio.artefacthandler.EPArtefactHandler;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.portfolio.filter.PortfolioFilterController;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * allows to admin the ePortfolio-module
 * <P>
 * Initial Date: 21.07.2010 <br>
 * 
 * @author: srosse
 */
public class PortfolioAdminController extends FormBasicController {

    private final PortfolioModule portfolioModule;

    private MultipleSelectionElement portfolioEnabled;
    private final List<MultipleSelectionElement> handlersEnabled = new ArrayList<MultipleSelectionElement>();

    private static String[] enabledKeys = new String[] { "on" };
    private final String[] enabledValues;

    public PortfolioAdminController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl, "adminconfig");

        portfolioModule = (PortfolioModule) CoreSpringFactory.getBean("portfolioModule");
        enabledValues = new String[] { translate("enabled") };

        initForm(ureq);
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, @SuppressWarnings("unused") final UserRequest ureq) {
        if (formLayout instanceof FormLayoutContainer) {
            final FormLayoutContainer layoutContainer = (FormLayoutContainer) formLayout;

            // module configuration
            final FormLayoutContainer moduleFlc = FormLayoutContainer.createDefaultFormLayout("flc_module", getTranslator());
            layoutContainer.add(moduleFlc);

            portfolioEnabled = uifactory.addCheckboxesHorizontal("portfolio.module.enabled", moduleFlc, enabledKeys, enabledValues, null);
            portfolioEnabled.select(enabledKeys[0], portfolioModule.isEnabled());
            portfolioEnabled.addActionListener(listener, FormEvent.ONCHANGE);

            // handlers configuration
            final FormLayoutContainer handlersFlc = FormLayoutContainer.createDefaultFormLayout("flc_handlers", getTranslator());
            layoutContainer.add(handlersFlc);

            final List<EPArtefactHandler<?>> handlers = portfolioModule.getAllAvailableArtefactHandlers();
            for (final EPArtefactHandler<?> handler : handlers) {
                final Translator handlerTrans = handler.getHandlerTranslator(getTranslator());
                handlersFlc.setTranslator(handlerTrans);
                final String handlerClass = PortfolioFilterController.HANDLER_PREFIX + handler.getClass().getSimpleName()
                        + PortfolioFilterController.HANDLER_TITLE_SUFFIX;
                final MultipleSelectionElement handlerEnabled = uifactory.addCheckboxesHorizontal(handlerClass, handlersFlc, enabledKeys, enabledValues, null);
                handlerEnabled.select(enabledKeys[0], handler.isEnabled());
                handlerEnabled.setUserObject(handler);
                handlerEnabled.addActionListener(listener, FormEvent.ONCHANGE);
                handlersEnabled.add(handlerEnabled);
            }
        }
    }

    @Override
    protected void doDispose() {
        // auto-disposed
    }

    @SuppressWarnings("unused")
    @Override
    protected void formOK(final UserRequest ureq) {
        //
    }

    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        super.formInnerEvent(ureq, source, event);
        if (source == portfolioEnabled) {
            final boolean enabled = portfolioEnabled.isSelected(0);
            portfolioModule.setEnabled(enabled);
            showWarning("portfolio.module.change.warning");
        } else if (handlersEnabled.contains(source)) {
            final EPArtefactHandler<?> handler = (EPArtefactHandler<?>) source.getUserObject();
            final boolean enabled = ((MultipleSelectionElement) source).isSelected(0);
            portfolioModule.setEnableArtefactHandler(handler, enabled);
        }
    }
}
