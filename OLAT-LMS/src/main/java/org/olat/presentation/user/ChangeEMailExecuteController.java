/**
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * Copyright (c) 2005-2008 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 * All rights reserved.
 */
package org.olat.presentation.user;

import java.util.HashMap;

import org.olat.data.basesecurity.Identity;
import org.olat.data.user.UserConstants;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.Translator;

import com.thoughtworks.xstream.XStream;

/**
 * Description:<br>
 * This controller do change the email from a user after he has clicked a link in email.
 * <P>
 * Initial Date: 19.05.2009 <br>
 * 
 * @author bja
 */
public class ChangeEMailExecuteController extends ChangeEMailController {

    public ChangeEMailExecuteController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        this.userRequest = ureq;
        pT = new PackageTranslator(PACKAGE, userRequest.getLocale());
        pT = getUserService().getUserPropertiesConfig().getTranslator(pT);
        emKey = userRequest.getHttpReq().getParameter("key");
        if (emKey == null) {
            emKey = getUserService().getUserProperty(userRequest.getIdentity().getUser(), UserConstants.EMAILCHANGE);
        }
        if (emKey != null) {
            // key exist
            // we check if given key is a valid temporary key
            tempKey = getRegistrationService().loadTemporaryKeyByRegistrationKey(emKey);
        }
    }

    /**
     * change email
     * 
     * @param wControl
     * @return
     */
    public boolean changeEMail(final WindowControl wControl) {
        final XStream xml = new XStream();
        final HashMap<String, String> mails = (HashMap<String, String>) xml.fromXML(tempKey.getEmailAddress());
        final Identity ident = getUserService().findIdentityByEmail(mails.get("currentEMail"));
        if (ident != null) {
            // change mail address
            getUserService().setUserProperty(ident.getUser(), UserConstants.EMAIL, mails.get("changedEMail"));
            // if old mail address closed then set the new mail address
            // unclosed
            final String value = getUserService().getUserProperty(ident.getUser(), UserConstants.EMAILDISABLED);
            if (value != null && value.equals("true")) {
                getUserService().setUserProperty(ident.getUser(), UserConstants.EMAILDISABLED, "false");
            }
            // success info message
            wControl.setInfo(pT.translate("success.change.email", new String[] { mails.get("currentEMail"), mails.get("changedEMail") }));
            // remove keys
            getUserService().setUserProperty(ident.getUser(), UserConstants.EMAILCHANGE, null);
            userRequest.getUserSession().removeEntryFromNonClearedStore(ChangeEMailController.CHANGE_EMAIL_ENTRY);
        }
        // delete registration key
        getRegistrationService().deleteTemporaryKeyWithId(tempKey.getRegistrationKey());

        return true;
    }

    public boolean isLinkClicked() {
        final Object entry = userRequest.getUserSession().getEntry(ChangeEMailController.CHANGE_EMAIL_ENTRY);
        return (entry != null);
    }

    public Translator getPackageTranslator() {
        return pT;
    }

}
