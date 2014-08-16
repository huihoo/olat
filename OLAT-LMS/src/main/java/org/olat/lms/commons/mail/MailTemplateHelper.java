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
package org.olat.lms.commons.mail;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.olat.data.basesecurity.Identity;
import org.olat.data.user.User;
import org.olat.data.user.UserConstants;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.lms.commons.i18n.I18nModule;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.Settings;
import org.olat.system.mail.MailTemplate;
import org.olat.system.mail.MailerResult;
import org.olat.system.security.OLATPrincipal;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Provides methods for building MailTemplate instances using translation keys available in this package.
 * 
 * <P>
 * Initial Date: 18.04.2011 <br>
 * 
 * @author lavinia
 */
public class MailTemplateHelper {

    private static Map<String, Translator> translators;

    static {
        translators = new HashMap<String, Translator>();
    }

    /**
     * Creates a MailTemplate with a footer using this package translator.
     * 
     * @param locale
     * @return
     */
    public static MailTemplate getMailTemplateWithFooterNoUserData(Locale locale) {
        // initialize the mail footer with info about this OLAt installation
        Translator trans = getTranslator(locale);
        return getMailTemplate(trans.translate("footer.no.userdata", new String[] { Settings.getServerContextPathURI() }));
    }

    /**
     * Creates a MailTemplate with a footer containing user data, using this package translator.
     * 
     * @param mailFromIdentity
     * @return
     */
    public static MailTemplate getMailTemplateWithFooterWithUserData(Identity mailFromIdentity) {
        User user = mailFromIdentity.getUser();
        Translator trans = getTranslator(new Locale(user.getPreferences().getLanguage()));
        String institution = getUserService().getUserProperty(user, UserConstants.INSTITUTIONALNAME);
        if (institution == null)
            institution = "";
        return getMailTemplate(trans.translate("footer.with.userdata", new String[] { getUserService().getUserProperty(user, UserConstants.FIRSTNAME),
                getUserService().getUserProperty(user, UserConstants.LASTNAME), mailFromIdentity.getName(), institution, Settings.getServerContextPathURI() }));
    }

    /**
     * Creates a new Stub implementation for MailTemplate.
     * 
     * @param footer
     * @return
     */
    private static MailTemplate getMailTemplate(String footer) {
        // create a mail template which all these data
        final MailTemplate mailTempl = new MailTemplate(footer) {
            @Override
            public void putVariablesInMailContext(final VelocityContext context, final OLATPrincipal identity) {
                // nothing to do
            }
        };
        return mailTempl;
    }

    /**
     * takes a List containing email Strings and converts them to a String containing the Email Strings separated by a <b>, </b>. The returned String can be fed directly
     * to the e-mailer helper as the e-mail to field. <br>
     * <ul>
     * <li>Entries in the parameter emailRecipients are expected to be not null and of Type String.</li>
     * </ul>
     * 
     * @param emailRecipients
     * @param delimiter
     * @return "email1, email2, email3," or null if emailRecipientIdentites was null
     */
    public static String formatIdentitesAsEmailToString(final List emailRecipients, String delimiter) {
        int elCnt = emailRecipients.size();
        // 2..n recipients
        StringBuilder tmpDET = new StringBuilder();
        for (int i = 0; i < elCnt; i++) {
            tmpDET.append((String) emailRecipients.get(i));
            if (i < elCnt - 1) {
                tmpDET.append(delimiter);
            }
        }
        return tmpDET.toString();
    }

    /**
     * takes an array of Identies and converts them to a String containing the Identity-Emails separated by a <b>, </b>. The returned String can be fed directly to the
     * e-mailer helper as the e-mail to field. <br>
     * <ul>
     * <li>Entries in the parameter emailRecipientIdentites are expected to be not null.</li>
     * </ul>
     * 
     * @param emailRecipientIdentities
     * @return "email1, email2, email3," or null if emailRecipientIdentites was null
     */
    /*
     * private static String formatIdentitesAsEmailToString(final Identity[] emailRecipientIdentities) { int elCnt = emailRecipientIdentities.length; // 2..n recipients
     * StringBuilder tmpDET = new StringBuilder(); for (int i = 0; i < elCnt; i++) { tmpDET.append(emailRecipientIdentities[i].getUser().getProperty(UserConstants.EMAIL,
     * null)); if (i < elCnt - 1) { tmpDET.append(", "); } } return tmpDET.toString(); }
     */

    /**
     * Create a mail footer for the given locale and sender.
     * 
     * @param locale
     *            Defines language of footer text. If null, the systems default locale is used
     * @param sender
     *            Details about sender embedded in mail footer. If null no such details are attached to the footer
     * @return The mail footer as string
     */
    public static String getMailFooter(Identity recipient, Identity sender) {
		Locale recipientLocale = Locale.getDefault();
        if (recipient != null) {
			recipientLocale = I18nManager.getInstance().getLocaleOrDefault(recipient.getUser().getPreferences().getLanguage());
		} /*else if (recipientsCC != null && recipientsCC.size() > 0) {
			recLocale = I18nManager.getInstance().getLocaleOrDefault(recipientsCC.get(0).getUser().getPreferences().getLanguage());
		}*/
		if (recipientLocale == null) {
			recipientLocale = I18nModule.getDefaultLocale();
        }
		Translator trans = getTranslator(recipientLocale);
        if (sender == null) {
            // mail sent by platform configured sender address
            return trans.translate("footer.no.userdata", new String[] { Settings.getServerContextPathURI() });
        }
        // mail sent by a system user
        User user = sender.getUser();
        String institution = getUserService().getUserProperty(user, UserConstants.INSTITUTIONALNAME);
		if (institution == null) institution = "";
		return trans.translate("footer.with.userdata", new String[] { getUserService().getUserProperty(user, UserConstants.FIRSTNAME), getUserService().getUserProperty(user, UserConstants.LASTNAME),
                        sender.getName(), institution, Settings.getServerContextPathURI() });

    }

    public static String getTitleForFailedUsersError(Locale locale) {
        return getTranslator(locale).translate("mailhelper.error.failedusers.title");
    }

    public static String getMessageForFailedUsersError(Locale locale, List<? extends OLATPrincipal> disabledIdentities) {
        String message = getTranslator(locale).translate("mailhelper.error.failedusers");
        message += "\n<ul>\n";
        for (OLATPrincipal principal : disabledIdentities) {
            message += "<li>\n";
            message += principal.getAttributes().getFirstName();
            message += " ";
            message += principal.getAttributes().getLastName();
            message += "\n</li>\n";
        }
        message += "</ul>\n";
        return message;
    }

    /**
     * Method to evaluate the mailer result and disply general error and warning messages. If you want to display other messages instead you have to evaluate the mailer
     * result yourself and print messages accordingly.
     * 
	 * @param mailerResult The mailer result to be evaluated
	 * @param wControl The current window controller
	 * @param locale The users local
     */
    public static void printErrorsAndWarnings(MailerResult mailerResult, WindowControl wControl, Locale locale) {
        if (mailerResult == null) {
            return;
        }
        StringBuilder errors = new StringBuilder();
        StringBuilder warnings = new StringBuilder();
        appendErrorsAndWarnings(mailerResult, errors, warnings, locale);
        // now print a warning to the users screen
        if (errors.length() > 0) {
            wControl.setError(errors.toString());
        }
        if (warnings.length() > 0) {
            wControl.setWarning(warnings.toString());
        }
    }

    /**
     * Method to evaluate the mailer result. The errors and warnings will be attached to the given string buffers. If you want to display other messages instead you have
     * to evaluate the mailer result yourself and print messages accordingly.
     * 
	 * @param mailerResult The mailer result to be evaluated
	 * @param errors StringBuilder for the error messages
	 * @param warnings StringBuilder for the warnings
	 * @param locale The users local
     */
    public static void appendErrorsAndWarnings(MailerResult mailerResult, StringBuilder errors, StringBuilder warnings, Locale locale) {
        Translator trans = PackageUtil.createPackageTranslator(MailTemplateHelper.class, locale);
        int returnCode = mailerResult.getReturnCode();
        List<OLATPrincipal> failedIdentites = mailerResult.getFailedIdentites();

        // first the severe errors
        if (returnCode == MailerResult.SEND_GENERAL_ERROR) {
            errors.append("<p>").append(trans.translate("mailhelper.error.send.general")).append("</p>");
        } else if (returnCode == MailerResult.SENDER_ADDRESS_ERROR) {
            errors.append("<p>").append(trans.translate("mailhelper.error.sender.address")).append("</p>");
        } else if (returnCode == MailerResult.RECIPIENT_ADDRESS_ERROR) {
            errors.append("<p>").append(trans.translate("mailhelper.error.recipient.address")).append("</p>");
        } else if (returnCode == MailerResult.TEMPLATE_GENERAL_ERROR) {
            errors.append("<p>").append(trans.translate("mailhelper.error.template.general")).append("</p>");
        } else if (returnCode == MailerResult.TEMPLATE_PARSE_ERROR) {
            errors.append("<p>").append(trans.translate("mailhelper.error.template.parse")).append("</p>");
        } else if (returnCode == MailerResult.ATTACHMENT_INVALID) {
            errors.append("<p>").append(trans.translate("mailhelper.error.attachment")).append("</p>");
        } else {
            // mail could be send, but maybe not to all the users (e.g. invalid mail
            // adresses or a temporary problem)
            if (failedIdentites != null && failedIdentites.size() > 0) {
                warnings.append("<p>").append(trans.translate("mailhelper.error.failedusers"));
                warnings.append("<ul>");
                for (OLATPrincipal principal : failedIdentites) {
                    warnings.append("<li>");
					warnings.append(trans.translate(
							"mailhelper.error.failedusers.user",
							new String[] { principal.getAttributes().getFirstName(), principal.getAttributes().getLastName(),
									principal.getAttributes().getEmail(), principal.getName() }));
                    warnings.append("</li>");
                }
                warnings.append("</ul></p>");
            }
        }
    }

    /**
     * Helper method to reuse translators. It makes no sense to build a new translator over and over again. We keep one for each language and reuse this one during the
     * whole lifetime
     * 
     * @param locale
     * @return a translator for the given locale
     */
    private static Translator getTranslator(Locale locale) {
        String ident = locale.toString();
        synchronized (translators) { // o_cluster_ok
            Translator trans = translators.get(ident);
            if (trans == null) {
                trans = PackageUtil.createPackageTranslator(MailTemplateHelper.class, locale);
                translators.put(ident, trans);
            }
            return trans;
        }
    }

    private static UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
