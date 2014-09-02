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

import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.SingleSelection;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * switch between artefact-preview and the table view mode, sends an event on changes, persist setting in user-properties
 * <P>
 * Initial Date: 16.11.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPViewModeController extends FormBasicController {

    public static final String VIEWMODE_TABLE = "table";
    public static final String VIEWMODE_DETAILS = "details";
    public static final String VIEWMODE_CONTEXT_ARTEFACTPOOL = "artefact";
    public static final String VIEWMODE_CONTEXT_MAP = "map";
    public static final String VIEWMODE_CHANGED_EVENT_CMD = "viewModeChangedEventCommand";
    private SingleSelection viewRadio;
    private final EPFrontendManager ePFMgr;
    private String userPrefsMode;
    private final String context;

    public EPViewModeController(final UserRequest ureq, final WindowControl wControl, final Form rootForm, final String context) {
        super(ureq, wControl, FormBasicController.LAYOUT_DEFAULT, null, rootForm);
        this.context = context;
        ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean(EPFrontendManager.class);
        userPrefsMode = ePFMgr.getUsersPreferedArtefactViewMode(getIdentity(), context);

        initForm(ureq);
    }

    public EPViewModeController(final UserRequest ureq, final WindowControl wControl, final String context) {
        super(ureq, wControl);
        this.context = context;

        ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean(EPFrontendManager.class);
        userPrefsMode = ePFMgr.getUsersPreferedArtefactViewMode(getIdentity(), context);

        initForm(ureq);
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @SuppressWarnings("unused")
    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        final String[] theKeys = new String[] { VIEWMODE_TABLE, VIEWMODE_DETAILS };
        final String[] theValues = new String[] { translate("view.mode.table"), translate("view.mode.details") };

        viewRadio = uifactory.addRadiosHorizontal("view.mode", formLayout, theKeys, theValues);
        viewRadio.addActionListener(this, FormEvent.ONCLICK);
        if (userPrefsMode != null) {
            viewRadio.select(userPrefsMode, true);
        } else {
            viewRadio.select(VIEWMODE_DETAILS, true);
        }
    }

    /**
     * org.olat.presentation.framework.components.form.flexible.FormItem, org.olat.presentation.framework.components.form.flexible.impl.FormEvent)
     */
    @SuppressWarnings("unused")
    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        if (source == viewRadio) {
            if (!viewRadio.getSelectedKey().equals(userPrefsMode)) {
                final String newUserPrefsMode = viewRadio.getSelectedKey();
                ePFMgr.setUsersPreferedArtefactViewMode(getIdentity(), newUserPrefsMode, context);
                userPrefsMode = newUserPrefsMode;
                fireEvent(ureq, new Event(VIEWMODE_CHANGED_EVENT_CMD));
            }
        }
    }

    /**
	 */
    @SuppressWarnings("unused")
    @Override
    protected void formOK(final UserRequest ureq) {
        // nothing to persist, see formInnerEvent
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing
    }

}
