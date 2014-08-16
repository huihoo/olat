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
 * Description:<br>
 * The MailTemplate holds a mail subject/body template and the according methods to populate the velocity contexts with the user values
 * <P>
 * Usage:<br>
 * Helper to create various mail templates used in the groupmanagement when adding and removing users.
 * <p>
 * Initial Date: 23.11.2006 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH<br>
 *         http://www.frentix.com
 */

package org.olat.presentation.group.mail;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.filter.FilterFactory;
import org.olat.data.group.BusinessGroup;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.user.UserConstants;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.lms.commons.mail.MailTemplateHelper;
import org.olat.lms.group.context.BusinessGroupContextService;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.group.BGControllerFactory;
import org.olat.presentation.group.BGTranslatorFactory;
import org.olat.system.commons.Settings;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.mail.MailTemplate;
import org.olat.system.security.OLATPrincipal;
import org.olat.system.spring.CoreSpringFactory;

public class BGMailHelper {
    private static final Logger log = LoggerHelper.getLogger();

    /**
     * The mail templated when adding users to a group. The method chooses automatically the right translator for the given group type to customize the template text
     * 
     * @param group
     * @param actor
     * @return the generated MailTemplate
     */
    public static MailTemplate createAddParticipantMailTemplate(final BusinessGroup group, final Identity actor) {
        final String subjectKey = "notification.mail.added.subject";
        final String bodyKey = "notification.mail.added.body";
        return createMailTemplate(group, actor, subjectKey, bodyKey);
    }

    /**
     * The mail templated when removing users from a group. The method chooses automatically the right translator for the given group type to customize the template text
     * 
     * @param group
     * @param actor
     * @return the generated MailTemplate
     */
    public static MailTemplate createRemoveParticipantMailTemplate(final BusinessGroup group, final Identity actor) {
        final String subjectKey = "notification.mail.removed.subject";
        final String bodyKey = "notification.mail.removed.body";
        return createMailTemplate(group, actor, subjectKey, bodyKey);
    }

    /**
     * The mail templated when deleting a whole group. The method chooses automatically the right translator for the given group type to customize the template text
     * 
     * @param group
     * @param actor
     * @return the generated MailTemplate
     */
    public static MailTemplate createDeleteGroupMailTemplate(final BusinessGroup group, final Identity actor) {
        final String subjectKey = "notification.mail.deleted.subject";
        final String bodyKey = "notification.mail.deleted.body";
        return createMailTemplate(group, actor, subjectKey, bodyKey);
    }

    /**
     * The mail templated when a user added himself to a group. The method chooses automatically the right translator for the given group type to customize the template
     * text
     * 
     * @param group
     * @param actor
     * @return the generated MailTemplate
     */
    public static MailTemplate createAddMyselfMailTemplate(final BusinessGroup group, final Identity actor) {
        final String subjectKey = "notification.mail.added.self.subject";
        final String bodyKey = "notification.mail.added.self.body";
        return createMailTemplate(group, actor, subjectKey, bodyKey);
    }

    /**
     * The mail templated when a user removed himself from a group. The method chooses automatically the right translator for the given group type to customize the
     * template text
     * 
     * @param group
     * @param actor
     * @return the generated MailTemplate
     */
    public static MailTemplate createRemoveMyselfMailTemplate(final BusinessGroup group, final Identity actor) {
        final String subjectKey = "notification.mail.removed.self.subject";
        final String bodyKey = "notification.mail.removed.self.body";
        return createMailTemplate(group, actor, subjectKey, bodyKey);
    }

    /**
     * The mail templated when adding users to a waitinglist. The method chooses automatically the right translator for the given group type to customize the template
     * text
     * 
     * @param group
     * @param actor
     * @return the generated MailTemplate
     */
    public static MailTemplate createAddWaitinglistMailTemplate(final BusinessGroup group, final Identity actor) {
        final String subjectKey = "notification.mail.waitingList.added.subject";
        final String bodyKey = "notification.mail.waitingList.added.body";
        return createMailTemplate(group, actor, subjectKey, bodyKey);
    }

    /**
     * The mail templated when removing users from a waiting list. The method chooses automatically the right translator for the given group type to customize the
     * template text
     * 
     * @param group
     * @param actor
     * @return the generated MailTemplate
     */
    public static MailTemplate createRemoveWaitinglistMailTemplate(final BusinessGroup group, final Identity actor) {
        final String subjectKey = "notification.mail.waitingList.removed.subject";
        final String bodyKey = "notification.mail.waitingList.removed.body";
        return createMailTemplate(group, actor, subjectKey, bodyKey);
    }

    /**
     * The mail templated when automatically transferring users from the waitinglist to the participants list adding users to a waitinglist. The method chooses
     * automatically the right translator for the given group type to customize the template text
     * 
     * @param group
     * @param actor
     * @return the generated MailTemplate
     */
    public static MailTemplate createWaitinglistTransferMailTemplate(final BusinessGroup group, final Identity actor) {
        final String subjectKey = "notification.mail.waitingList.transfer.subject";
        final String bodyKey = "notification.mail.waitingList.transfer.body";
        return createMailTemplate(group, actor, subjectKey, bodyKey);
    }

    /**
     * Internal helper - does all the magic
     * 
     * @param group
     * @param actor
     * @param subjectKey
     * @param bodyKey
     * @return
     */
    private static MailTemplate createMailTemplate(final BusinessGroup group, final Identity actor, final String subjectKey, final String bodyKey) {
        // build learning resources as list of url as string
        final StringBuilder learningResources = new StringBuilder();
        if (group.getGroupContext() != null) {
            final List repoEntries = getBgContextService().findRepositoryEntriesForBGContext(group.getGroupContext());
            final Iterator iter = repoEntries.iterator();
            while (iter.hasNext()) {
                final RepositoryEntry entry = (RepositoryEntry) iter.next();
                final String title = entry.getDisplayname();
                learningResources.append(title);
                learningResources.append(" (");
                learningResources.append(getURL(entry));
                learningResources.append(")\n");
            }
        }
        final String courselist = learningResources.toString();
        // get group name and description
        final String groupname = group.getName();
        final String groupdescription = FilterFactory.getHtmlTagAndDescapingFilter().filter(group.getDescription());
        // get some data about the actor and fetch the translated subject / body via
        // i18n module
        final String[] bodyArgs = new String[] { getUserService().getUserProperty(actor.getUser(), UserConstants.FIRSTNAME),
                getUserService().getUserProperty(actor.getUser(), UserConstants.LASTNAME), getUserService().getUserProperty(actor.getUser(), UserConstants.EMAIL),
                actor.getName() };
        final Locale locale = I18nManager.getInstance().getLocaleOrDefault(actor.getUser().getPreferences().getLanguage());
        final Translator trans = BGTranslatorFactory.createBGPackageTranslator(PackageUtil.getPackageName(BGControllerFactory.class), group.getType(), locale);
        String subject = trans.translate(subjectKey);
        String body = trans.translate(bodyKey, bodyArgs);

        subject = replaceInText(subject, "\\$groupname", groupname);
        body = replaceInText(body, "\\$groupname", groupname);
        body = replaceInText(body, "\\$groupdescription", groupdescription);
        body = replaceInText(body, "\\$courselist", courselist);

        // create a mail template which all these data
		final MailTemplate mailTempl = new MailTemplate(subject, body, MailTemplateHelper.getMailFooter( actor, null), null) {
            @Override
            public void putVariablesInMailContext(final VelocityContext context, final OLATPrincipal principal) {
                // Put user variables into velocity context
                context.put("firstname", principal.getAttributes().getFirstName());
                context.put("lastname", principal.getAttributes().getLastName());
                context.put("login", principal.getName());
                // Put variables from greater context
                context.put("groupname", groupname);
                context.put("groupdescription", groupdescription);
                context.put("courselist", courselist);
            }
        };
        return mailTempl;
    }

    /**
     * @return
     */
    private static BusinessGroupContextService getBgContextService() {
        return CoreSpringFactory.getBean(BusinessGroupContextService.class);
    }

    private static UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

    private static String getURL(RepositoryEntry entry) {
        return Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + entry.getKey();
    }

    private static String replaceInText(String text, String query, String replacement) {
        try {
            return text.replaceAll(query, replacement == null ? "" : replacement);
        } catch (Exception e) {
            log.warn("replacting in text threw exception: ", e);
            return text;
        }
    }

}
