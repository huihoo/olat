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
package org.olat.presentation.portfolio.filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.data.portfolio.artefact.EPFilterSettings;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.FormLink;
import org.olat.presentation.framework.core.components.form.flexible.elements.SingleSelection;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * shows available filters and let user select from it
 * <P>
 * Initial Date: 12.11.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPFilterSelectController extends FormBasicController {

    private final EPFrontendManager ePFMgr;
    private FormLink adaptBtn;
    private SingleSelection filterSel;
    private ArrayList<EPFilterSettings> nonEmptyFilters;
    private final String presetFilterID;

    public EPFilterSelectController(final UserRequest ureq, final WindowControl wControl, final String presetFilterID) {
        super(ureq, wControl);
        this.presetFilterID = presetFilterID;
        ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean(EPFrontendManager.class);

        initForm(ureq);
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @SuppressWarnings("unused")
    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        final List<EPFilterSettings> existingFilters = ePFMgr.getSavedFilterSettings(getIdentity());
        for (final Iterator<EPFilterSettings> existingFilterIt = existingFilters.iterator(); existingFilterIt.hasNext();) {
            if (existingFilterIt.next().isFilterEmpty()) {
                existingFilterIt.remove();
            }
        }

        final int amount = existingFilters.size() + 1;
        nonEmptyFilters = new ArrayList<EPFilterSettings>(amount);
        final String[] theKeys = new String[amount];
        final String[] theValues = new String[amount];
        theKeys[0] = String.valueOf(0);
        theValues[0] = translate("filter.all");
        int i = 1;
        String presetFilterIndex = "0";
        for (final EPFilterSettings epFilterSettings : existingFilters) {
            theKeys[i] = epFilterSettings.getFilterId();
            theValues[i] = epFilterSettings.getFilterName();
            if (presetFilterID != null && presetFilterID.equals(epFilterSettings.getFilterId())) {
                presetFilterIndex = epFilterSettings.getFilterId();
            }
            nonEmptyFilters.add(epFilterSettings);
            i++;
        }
        // don't show anything if no filter exists
        if (!nonEmptyFilters.isEmpty()) {
            final String page = this.velocity_root + "/filter_select.html";
            final FormLayoutContainer selection = FormLayoutContainer.createCustomFormLayout("filter_selection", getTranslator(), page);
            selection.setRootForm(mainForm);
            selection.setLabel("filter.select", null);
            formLayout.add(selection);

            filterSel = uifactory.addDropdownSingleselect("filter.select", selection, theKeys, theValues, null);
            filterSel.addActionListener(this, FormEvent.ONCHANGE);
            filterSel.select(presetFilterIndex, true);
            adaptBtn = uifactory.addFormLink("filter.adapt", selection);
            adaptBtn.setVisible(!presetFilterIndex.equals("0"));
        }
    }

    /**
     * org.olat.presentation.framework.components.form.flexible.FormItem, org.olat.presentation.framework.components.form.flexible.impl.FormEvent)
     */
    @SuppressWarnings("unused")
    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        if (source == filterSel) {
            final int selFilter = filterSel.getSelected();
            EPFilterSettings selFilterSet;
            if (selFilter != 0) {
                selFilterSet = nonEmptyFilters.get(selFilter - 1);
            } else {
                // all was selected, fire an empty filter
                selFilterSet = new EPFilterSettings();
            }
            fireEvent(ureq, new PortfolioFilterChangeEvent(selFilterSet));
        } else if (source == adaptBtn) {
            // launch search view
            final int selFilter = filterSel.getSelected();
            if (selFilter > 0) {
                final EPFilterSettings selFilterSet = nonEmptyFilters.get(selFilter - 1);
                fireEvent(ureq, new PortfolioFilterEditEvent(selFilterSet));
            }
        }
    }

    /**
	 */
    @SuppressWarnings("unused")
    @Override
    protected void formOK(final UserRequest ureq) {
        // nothing to persist
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing
    }

}
