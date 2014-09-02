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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.system.mail;

import java.util.List;

import org.apache.log4j.Logger;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.WebappHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.security.OLATPrincipal;

/**
 * Description:<br>
 * Some mail helpers
 * <P>
 * Initial Date: 21.11.2006 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH<br>
 *         http://www.frentix.com
 */
public class MailHelper {

    private static final Logger log = LoggerHelper.getLogger();

    private static String mailhost;
    private static String mailhostTimeout;

    private static int maxSizeOfAttachments = 5;

    static {
        mailhost = WebappHelper.getMailConfig("mailhost");
        mailhostTimeout = WebappHelper.getMailConfig("mailTimeout");

        String smtpUser = null, smtpPwd = null;
        if (WebappHelper.isMailHostAuthenticationEnabled()) {
            smtpUser = WebappHelper.getMailConfig("smtpUser");
            smtpPwd = WebappHelper.getMailConfig("smtpPwd");
        }

        if (log.isDebugEnabled()) {
            log.debug("using smtp host::" + mailhost + " with timeout::" + mailhostTimeout + " , smtpUser::" + smtpUser + " and smtpPwd::" + smtpPwd);
        }

        String maxSizeStr = WebappHelper.getMailConfig("mailAttachmentMaxSize");
        if (StringHelper.containsNonWhitespace(maxSizeStr)) {
            maxSizeOfAttachments = Integer.parseInt(maxSizeStr);
        }

    }

    /**
     * @return the maximum size allowed for attachements in MB (default 5MB)
     */
    public static int getMaxSizeForAttachement() {
        return maxSizeOfAttachments;
    }

    /**
     * Checks if the given mail address is potentially a valid email address that can be used to send emails. It does NOT check if the mail address exists, it checks only
     * for syntactical validity.
     * 
     * @param mailAddress
     * @return
     */
    public static boolean isValidEmailAddress(String mailAddress) {
        return EmailAddressValidator.isValidEmailAddress(mailAddress);
    }

    /**
     * check for disabled mail address
     * 
     * @param recipients
     * @param result
     * @return
     */
    public static MailerResult removeDisabledMailAddress(List<? extends OLATPrincipal> identities, MailerResult result) {
        boolean value = false;
        if (identities != null) {
            for (OLATPrincipal principal : identities) {
                value = principal.getAttributes().isEmailDisabled();
                if (value) {
                    result.addFailedIdentites(principal);
                    if (result.getReturnCode() != MailerResult.RECIPIENT_ADDRESS_ERROR) {
                        result.setReturnCode(MailerResult.RECIPIENT_ADDRESS_ERROR);
                    }
                }
            }
        }
        return result;
    }
}
