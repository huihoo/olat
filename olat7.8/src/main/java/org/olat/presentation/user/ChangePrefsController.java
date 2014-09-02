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

package org.olat.presentation.user;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.preferences.Preferences;
import org.olat.lms.preferences.PreferencesService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.WindowManager;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: Apr 27, 2004
 * 
 * @author gnaegi Comment: This controller allows the user to edit the preferences of any subject. Make sure you check for security when creating this controller since
 *         this controller does not have any security checks whatsoever.
 */

public class ChangePrefsController extends BasicController {

    private final VelocityContainer myContent;
    private final Controller generalPrefsCtr;
    private final Controller specialPrefsCtr;

    /**
     * Constructor for the change user preferences controller
     * 
     * @param ureq
     *            The user request
     * @param wControl
     *            The current window controller
     * @param changeableIdentity
     *            The subject whose profile should be changed
     */
    public ChangePrefsController(final UserRequest ureq, final WindowControl wControl, final Identity changeableIdentity) {
        super(ureq, wControl);

        myContent = createVelocityContainer("prefs");

        generalPrefsCtr = new PreferencesFormController(ureq, wControl, changeableIdentity);
        listenTo(generalPrefsCtr);

        specialPrefsCtr = new SpecialPrefsForm(ureq, wControl, changeableIdentity);
        listenTo(specialPrefsCtr);

        myContent.put("general", generalPrefsCtr.getInitialComponent());
        myContent.put("special", specialPrefsCtr.getInitialComponent());

        putInitialPanel(myContent);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == myContent) {
            if (event.getCommand().equals("exeBack")) {
                fireEvent(ureq, Event.CANCELLED_EVENT);
            }
        }
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == generalPrefsCtr) {
            if (event == Event.DONE_EVENT) {
                fireEvent(ureq, Event.DONE_EVENT);
            } else if (event == Event.CANCELLED_EVENT) {
                fireEvent(ureq, Event.CANCELLED_EVENT);
            }
        }

    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }
}

/**
 * Description:<br>
 * The special prefs form is used to configure the delivery mode. Users can choose between web 1.0, web 2.0 and web 2.a. The configuration for web 1.0 and web 2.0 is only
 * available for user managers. Normal users can only enable/disable the web 2.a mode
 * <P>
 * Initial Date: 09.11.2010 <br>
 * 
 * @author gnaegi
 */
class SpecialPrefsForm extends FormBasicController {

    private final Identity tobeChangedIdentity;
    private final Preferences prefs;
    private MultipleSelectionElement prefsElement;
    private String[] keys, values;
    private boolean useAjaxCheckbox = false;

    public SpecialPrefsForm(final UserRequest ureq, final WindowControl wControl, final Identity changeableIdentity) {
        super(ureq, wControl);
        tobeChangedIdentity = changeableIdentity;
        PreferencesService prefstorage = (PreferencesService) CoreSpringFactory.getBean(PreferencesService.class);
        if (ureq.getUserSession().getGuiPreferences() != null) {
            prefs = ureq.getUserSession().getGuiPreferences();
        } else {
            prefs = prefstorage.getPreferencesFor(tobeChangedIdentity, false);
        }
        // The ajax configuration is only for user manager (technical stuff)
        useAjaxCheckbox = ureq.getUserSession().getRoles().isUserManager();
        // initialize checkbox keys depending on useAjaxCheckbox flag
        if (useAjaxCheckbox) {
            keys = new String[] { "ajax", "web2a" };
            values = new String[] { translate("ajaxon.label"), translate("accessibility.web2aMode.label") };
        } else {
            keys = new String[] { "web2a" };
            values = new String[] { translate("accessibility.web2aMode.label") };
        }

        initForm(ureq);
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        if (useAjaxCheckbox) {
            prefs.putAndSave(WindowManager.class, "ajax-beta-on", prefsElement.getSelectedKeys().contains("ajax"));
        }
        prefs.putAndSave(WindowManager.class, "web2a-beta-on", prefsElement.getSelectedKeys().contains("web2a"));
        if (ureq.getIdentity().equalsByPersistableKey(tobeChangedIdentity)) {
            showInfo("preferences.successful");
        }
    }

    @Override
    protected void formCancelled(final UserRequest ureq) {
        update();
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {

        setFormTitle("title.prefs.special");
        setFormContextHelp(this.getClass().getPackage().getName(), "home-prefs-special.html", "help.hover.home.prefs.special");

        prefsElement = uifactory.addCheckboxesVertical("prefs", "title.prefs.accessibility", formLayout, keys, values, null, 1);

        update();

        final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
        formLayout.add(buttonLayout);
        uifactory.addFormSubmitButton("submit", buttonLayout);
        uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
    }

    private void update() {
        final Boolean web2a = (Boolean) prefs.get(WindowManager.class, "web2a-beta-on");
        final Boolean ajax = (Boolean) prefs.get(WindowManager.class, "ajax-beta-on");
        if (useAjaxCheckbox) {
            prefsElement.select("ajax", ajax == null ? true : ajax.booleanValue());
        }
        prefsElement.select("web2a", web2a == null ? false : web2a.booleanValue());
    }

    @Override
    protected void doDispose() {
        //
    }

}
