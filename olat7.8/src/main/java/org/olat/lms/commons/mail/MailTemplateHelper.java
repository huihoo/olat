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

import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.security.OLATPrincipal;

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

    public static String getTitleForFailedUsersError(Locale locale) {
        return getTranslator(locale).translate("mailhelper.error.failedusers.title");
    }

    public static String getMessageForFailedUsersError(Locale locale, List<? extends OLATPrincipal> disabledIdentities) {
        StringBuffer message = new StringBuffer();
        message.append(getTranslator(locale).translate("mailhelper.error.failedusers"));
        message.append("\n<ul>\n");
        for (OLATPrincipal principal : disabledIdentities) {
            message.append("<li>\n");
            message.append(principal.getAttributes().getFirstName());
            message.append(" ");
            message.append(principal.getAttributes().getLastName());
            message.append("\n</li>\n");
        }
        message.append("</ul>\n");
        return message.toString();
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

}
