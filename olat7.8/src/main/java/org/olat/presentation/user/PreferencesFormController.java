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
 * s
 * <p>
 */
package org.olat.presentation.user;

import java.nio.charset.Charset;
import java.util.Map;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.user.Preferences;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.lms.commons.i18n.I18nModule;
import org.olat.lms.commons.util.collection.ArrayHelper;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.SingleSelection;
import org.olat.presentation.framework.core.components.form.flexible.elements.StaticTextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowBackOffice;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * This form controller provides an interface to change the user's system preferences, like language and font size.
 * <p>
 * Events fired by this event:
 * <ul>
 * <li>Event.DONE_EVENT when something has been changed</li>
 * <li>Event.DONE_CANELLED when user cancelled action</li>
 * </ul>
 * <P>
 * Initial Date: Dec 11, 2009 <br>
 * 
 * @author gwassmann
 */
public class PreferencesFormController extends FormBasicController {
    private static final String[] cssFontsizeKeys = new String[] { "80", "90", "100", "110", "120", "140" };
    private Identity tobeChangedIdentity;
    private SingleSelection language, fontsize, charset;

    /**
     * Constructor for the user preferences form
     * 
     * @param ureq
     * @param wControl
     * @param tobeChangedIdentity
     *            the Identity which preferences are displayed and edited. Not necessarily the same as ureq.getIdentity()
     */
    public PreferencesFormController(final UserRequest ureq, final WindowControl wControl, final Identity tobeChangedIdentity) {
        super(ureq, wControl);
        this.tobeChangedIdentity = tobeChangedIdentity;
        initForm(ureq);
    }

    /**
	 */
    @Override
    protected void formOK(final UserRequest ureq) {
        final BaseSecurity secMgr = (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
        // Refresh user from DB to prevent stale object issues
        tobeChangedIdentity = secMgr.loadIdentityByKey(tobeChangedIdentity.getKey());
        final Preferences prefs = tobeChangedIdentity.getUser().getPreferences();
        prefs.setLanguage(I18nManager.getInstance().getLocaleOrDefault(language.getSelectedKey()));
        prefs.setFontsize(fontsize.getSelectedKey());

        // Maybe the user changed the font size
        if (ureq.getIdentity().equalsByPersistableKey(tobeChangedIdentity)) {
            final int fontSize = Integer.parseInt(fontsize.getSelectedKey());
            final WindowBackOffice wbo = getWindowControl().getWindowBackOffice();
            wbo.getWindowManager().setFontSize(fontSize);
            // set window dirty to force full page refresh
            wbo.getWindow().setDirty(true);
        }

        if (getUserService().updateUserFromIdentity(tobeChangedIdentity)) {
            // Language change needs logout / login
            showInfo("preferences.successful");
        } else {
            showInfo("preferences.unsuccessful");
        }

        getUserService().setUserCharset(tobeChangedIdentity, charset.getSelectedKey());
        fireEvent(ureq, Event.DONE_EVENT);
    }

    /**
	 */
    @Override
    protected void formCancelled(final UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        setFormTitle("title.prefs");
        setFormContextHelp(this.getClass().getPackage().getName(), "home-prefs.html", "help.hover.prefs");

        // load preferences
        final Preferences prefs = tobeChangedIdentity.getUser().getPreferences();

        // Username
        final StaticTextElement username = uifactory.addStaticTextElement("form.username", tobeChangedIdentity.getName(), formLayout);
        username.setEnabled(false);

        // Language
        final Map<String, String> languages = I18nManager.getInstance().getEnabledLanguagesTranslated();
        final String[] langKeys = StringHelper.getMapKeysAsStringArray(languages);
        final String[] langValues = StringHelper.getMapValuesAsStringArray(languages);
        ArrayHelper.sort(langKeys, langValues, false, true, false);
        language = uifactory.addDropdownSingleselect("form.language", formLayout, langKeys, langValues, null);
        final String langKey = prefs.getLanguage();
        // Preselect the users language if available. Maye not anymore enabled on
        // this server
        if (prefs.getLanguage() != null && I18nModule.getEnabledLanguageKeys().contains(langKey)) {
            language.select(prefs.getLanguage(), true);
        } else {
            language.select(I18nModule.getDefaultLocale().toString(), true);
        }

        // Font size
        final String[] cssFontsizeValues = new String[] { translate("form.fontsize.xsmall"), translate("form.fontsize.small"), translate("form.fontsize.normal"),
                translate("form.fontsize.large"), translate("form.fontsize.xlarge"), translate("form.fontsize.presentation") };
        fontsize = uifactory.addDropdownSingleselect("form.fontsize", formLayout, cssFontsizeKeys, cssFontsizeValues, null);
        fontsize.select(prefs.getFontsize(), true);
        fontsize.addActionListener(this, FormEvent.ONCHANGE);

        // Text encoding
        final Map<String, Charset> charsets = Charset.availableCharsets();
        final String currentCharset = getUserService().getUserCharset(tobeChangedIdentity);
        final String[] csKeys = StringHelper.getMapKeysAsStringArray(charsets);
        charset = uifactory.addDropdownSingleselect("form.charset", formLayout, csKeys, csKeys, null);
        charset.select(currentCharset, true);

        // Submit and cancel buttons
        final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
        formLayout.add(buttonLayout);
        uifactory.addFormSubmitButton("submit", buttonLayout);
        uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
    }

    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        if (source == fontsize && ureq.getIdentity().equalsByPersistableKey(tobeChangedIdentity)) {
            final int fontSize = Integer.parseInt(fontsize.getSelectedKey());
            final WindowBackOffice wbo = getWindowControl().getWindowBackOffice();
            wbo.getWindowManager().setFontSize(fontSize);
            wbo.getWindow().setDirty(true);
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to do
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
