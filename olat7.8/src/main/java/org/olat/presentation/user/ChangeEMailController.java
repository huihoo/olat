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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.olat.data.basesecurity.Identity;
import org.olat.data.registration.TemporaryKeyImpl;
import org.olat.data.user.User;
import org.olat.data.user.UserConstants;
import org.olat.lms.registration.RegistrationService;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.framework.dispatcher.DispatcherAction;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

import com.thoughtworks.xstream.XStream;

/**
 * This controller do change the email of a user after he has clicked the appropriate activation-link.
 * <P>
 * Initial Date: 27.04.2009 <br>
 * 
 * @author bja
 */
public class ChangeEMailController extends DefaultController {

    protected static final String PACKAGE = ProfileAndHomePageEditController.class.getPackage().getName();
    protected static final String CHANGE_EMAIL_ENTRY = "change.email.login";

    public static final int TIME_OUT = 3;

    protected Translator pT;
    protected String emKey;
    protected TemporaryKeyImpl tempKey;

    protected UserRequest userRequest;

    /**
     * executed after click the link in email
     * 
     * @param ureq
     * @param wControl
     */
    public ChangeEMailController(final UserRequest ureq, final WindowControl wControl) {
        super(wControl);
        this.userRequest = ureq;
        pT = new PackageTranslator(PACKAGE, userRequest.getLocale());
        pT = getUserService().getUserPropertiesConfig().getTranslator(pT);
        emKey = userRequest.getHttpReq().getParameter("key");
        if ((emKey == null) && (userRequest.getUserSession().getEntry(CHANGE_EMAIL_ENTRY) != null)) {
            emKey = getUserService().getUserProperty(userRequest.getIdentity().getUser(), UserConstants.EMAILCHANGE);
        }
        if (emKey != null) {
            // key exist
            // we check if given key is a valid temporary key
            tempKey = getRegistrationService().loadTemporaryKeyByRegistrationKey(emKey);
        }
        if (emKey != null) {
            // if key is not valid we redirect to first page
            if (tempKey == null) {
                // registration key not available
                userRequest.getUserSession().putEntryInNonClearedStore("error.change.email", pT.translate("error.change.email"));
                DispatcherAction.redirectToDefaultDispatcher(userRequest.getHttpResp());
                return;
            } else {
                if (!isLinkTimeUp()) {
                    try {
                        if ((userRequest.getUserSession().getEntry(CHANGE_EMAIL_ENTRY) == null)
                                || (!userRequest.getUserSession().getEntry(CHANGE_EMAIL_ENTRY).equals(CHANGE_EMAIL_ENTRY))) {
                            userRequest.getUserSession().putEntryInNonClearedStore(CHANGE_EMAIL_ENTRY, CHANGE_EMAIL_ENTRY);
                            DispatcherAction.redirectToDefaultDispatcher(userRequest.getHttpResp());
                            return;
                        } else {
                            if (userRequest.getIdentity() == null) {
                                DispatcherAction.redirectToDefaultDispatcher(userRequest.getHttpResp());
                                return;
                            }
                        }
                    } catch (final ClassCastException e) {
                        DispatcherAction.redirectToDefaultDispatcher(userRequest.getHttpResp());
                        return;
                    }
                } else {
                    // link time is up
                    userRequest.getUserSession().putEntryInNonClearedStore("error.change.email.time", pT.translate("error.change.email.time"));
                    final XStream xml = new XStream();
                    final HashMap<String, String> mails = (HashMap<String, String>) xml.fromXML(tempKey.getEmailAddress());
                    final Identity ident = getUserService().findIdentityByEmail(mails.get("currentEMail"));
                    if (ident != null) {
                        // remove keys
                        getUserService().setUserProperty(ident.getUser(), UserConstants.EMAILCHANGE, null);
                    }
                    // delete registration key
                    getRegistrationService().deleteTemporaryKeyWithId(tempKey.getRegistrationKey());
                    DispatcherAction.redirectToDefaultDispatcher(userRequest.getHttpResp());
                    return;
                }
            }
        }
    }

    /**
     * check if the link time up
     * 
     * @return
     */
    public boolean isLinkTimeUp() {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_WEEK, TIME_OUT * -1);

        if (tempKey == null) {
            // the database entry was deleted
            return true;
        }

        if (!tempKey.getCreationDate().after(cal.getTime())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * delete registration key, 'change.email.login' entry and set the userproperty emchangeKey to null
     */
    public void deleteRegistrationKey() {
        final User user = userRequest.getIdentity().getUser();
        // remove keys
        getUserService().setUserProperty(user, UserConstants.EMAILCHANGE, null);
        userRequest.getUserSession().removeEntryFromNonClearedStore(CHANGE_EMAIL_ENTRY);
        userRequest.getUserSession().removeEntryFromNonClearedStore("error.change.email.time");
        // delete registration key
        if (tempKey != null) {
            getRegistrationService().deleteTemporaryKeyWithId(tempKey.getRegistrationKey());
        }
    }

    @Override
    protected void doDispose() {
        // nothing to do
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // nothing to do
    }

    protected UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

    protected RegistrationService getRegistrationService() {
        return CoreSpringFactory.getBean(RegistrationService.class);
    }

}
