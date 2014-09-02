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
package org.olat.presentation.portfolio.filter;

import java.util.ArrayList;
import java.util.List;

import org.olat.lms.portfolio.PortfolioAbstractHandler;
import org.olat.lms.portfolio.artefacthandler.EPArtefactHandler;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * edit artefact type filter
 * <P>
 * Initial Date: 19.07.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPArtefactTypeSelectionController extends FormBasicController {

    private final PortfolioAbstractHandler portfolioModule;
    private List<String> selectedTypeList;
    private ArrayList<MultipleSelectionElement> typeCmpList;

    public EPArtefactTypeSelectionController(final UserRequest ureq, final WindowControl wControl, final List<String> selectedTypeList) {
        super(ureq, wControl);

        portfolioModule = (PortfolioAbstractHandler) CoreSpringFactory.getBean(PortfolioAbstractHandler.class);
        this.selectedTypeList = selectedTypeList;
        initForm(ureq);
    }

    @SuppressWarnings("unused")
    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        setFormDescription("filter.type.intro");

        final String[] keys = new String[] { "onoff" };
        final String[] values = new String[] { translate("filter.type.enabled") };

        final List<EPArtefactHandler<?>> handlers = portfolioModule.getAllAvailableArtefactHandlers();
        typeCmpList = new ArrayList<MultipleSelectionElement>();
        for (final EPArtefactHandler<?> handler : handlers) {
            final Translator handlerTrans = handler.getHandlerTranslator(getTranslator());
            this.flc.setTranslator(handlerTrans);
            final String handlerClass = PortfolioFilterController.HANDLER_PREFIX + handler.getClass().getSimpleName() + PortfolioFilterController.HANDLER_TITLE_SUFFIX;
            final MultipleSelectionElement chkBox = uifactory.addCheckboxesHorizontal(handlerClass, formLayout, keys, values, null);
            if (selectedTypeList != null && selectedTypeList.contains(handler.getType())) {
                chkBox.select(keys[0], true);
            }
            chkBox.addActionListener(this, FormEvent.ONCHANGE);
            chkBox.setUserObject(handler.getType());
            typeCmpList.add(chkBox);
        }
        uifactory.addFormSubmitButton("filter.type.submit", formLayout);
    }

    /**
     * org.olat.presentation.framework.components.form.flexible.FormItem, org.olat.presentation.framework.components.form.flexible.impl.FormEvent) fire change events on
     * every click in form and update gui
     */
    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        super.formInnerEvent(ureq, source, event);
        updateSelectedTypeList();
        fireEvent(ureq, Event.CHANGED_EVENT);
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    protected void updateSelectedTypeList() {
        if (selectedTypeList == null) {
            selectedTypeList = new ArrayList<String>();
        }
        for (final MultipleSelectionElement typeCmp : typeCmpList) {
            final String selType = (String) typeCmp.getUserObject();
            if (typeCmp.isSelected(0) && !selectedTypeList.contains(selType)) {
                selectedTypeList.add(selType);
            }
            if (!typeCmp.isSelected(0) && selectedTypeList.contains(selType)) {
                selectedTypeList.remove(selType);
            }
        }
        if (selectedTypeList.size() == 0) {
            selectedTypeList = null;
        }
    }

    @Override
    protected void doDispose() {
        // nothing
    }

}
