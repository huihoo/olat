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
 * Technische Universitaet Chemnitz Lehrstuhl Technische Informatik Author Marcel Karras (toka@freebits.de) Author Norbert Englisch
 * (norbert.englisch@informatik.tu-chemnitz.de) Author Sebastian Fritzsche (seb.fritzsche@googlemail.com)
 */

package org.olat.lms.course.wizard.create;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.olat.lms.commons.mail.MailTemplateHelper;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.mail.MailTemplate;
import org.olat.system.mail.MailerResult;
import org.olat.system.mail.MailerWithTemplate;
import org.olat.system.security.OLATPrincipal;

/**
 * Description:<br>
 * This helper class provides the functionality to send a notification mail after succesfully finalizing the course creation wizard.
 * <P>
 * 
 * @author Marcel Karras (toka@freebits.de)
 */
public class CourseCreationMailHelper {

    private static final Logger log = LoggerHelper.getLogger();

    /**
     * Get the success info message.
     * 
     * @param ureq
     *            user request
     * @return info message
     */
    public static final String getSuccessMessageString(final UserRequest ureq) {
        final Translator translator = PackageUtil.createPackageTranslator(CourseCreationMailHelper.class, ureq.getLocale());
        return translator.translate("coursecreation.success");
    }

    /**
     * Sent notification mail for signalling that course creation was successful.
     * 
     * @param ureq
     *            user request
     * @param config
     *            course configuration object
     * @return mailer result object
     */
    public static final MailerResult sentNotificationMail(final UserRequest ureq, final CourseCreationConfiguration config) {
        final Translator translator = PackageUtil.createPackageTranslator(CourseCreationMailHelper.class, ureq.getLocale());
        log.info("Course creation with wizard finished. [User: " + ureq.getIdentity().getName() + "] [Course name: " + config.getCourseTitle() + "]");
        final String subject = translator.translate("mail.subject", new String[] { config.getCourseTitle() });
        String body = translator.translate("mail.body.0", new String[] { config.getCourseTitle() });
        body += translator.translate("mail.body.1");
        body += translator.translate("mail.body.2", new String[] { config.getExtLink() });
        body += translator.translate("mail.body.3");
        body += translator.translate("mail.body.4");

        int counter = 1;
        if (config.isCreateSinglePage()) {
            body += translator.translate("mail.body.4.2", new String[] { Integer.toString(++counter) });
        }
        if (config.isCreateEnrollment()) {
            body += translator.translate("mail.body.4.3", new String[] { Integer.toString(++counter) });
        }
        if (config.isCreateDownloadFolder()) {
            body += translator.translate("mail.body.4.4", new String[] { Integer.toString(++counter) });
        }
        if (config.isCreateForum()) {
            body += translator.translate("mail.body.4.5", new String[] { Integer.toString(++counter) });
        }
        if (config.isCreateContactForm()) {
            body += translator.translate("mail.body.4.6", new String[] { Integer.toString(++counter) });
        }
        body += translator.translate("mail.body.5");
        body += translator.translate("mail.body.6");
        body += translator.translate("mail.body.greetings");

        final MailTemplate template = new MailTemplate(subject, body, MailTemplateHelper.getMailFooter(ureq.getIdentity(), null), null) {
            @Override
            @SuppressWarnings("unused")
            public void putVariablesInMailContext(final VelocityContext context, final OLATPrincipal identity) {
                // nothing to do
            }
        };
        return MailerWithTemplate.getInstance().sendMail(ureq.getIdentity(), null, null, template, null);
    }

}
