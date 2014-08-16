/**
 * 
 */
package org.olat.lms.notifications;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.properties.PropertyManager;
import org.olat.lms.commons.LmsSpringBeanTypes;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.lms.commons.mail.MailTemplateHelper;
import org.olat.system.mail.MailTemplate;
import org.olat.system.mail.MailerResult;
import org.olat.system.mail.MailerWithTemplate;
import org.olat.system.security.OLATPrincipal;
import org.olat.system.spring.CoreSpringFactory;
import org.springframework.stereotype.Component;

/**
 * This is the place where all the static class accesses (I18nManacher.getInstance(), PropertyManager.getInstance(), etc.) 
 * from the classes in the notification package is isolated.
 * The main reason is the testability of these classes and hence, i.e. make those getInstance calls mockable. 
 * 
 * @author patrick
 *
 */
@Component
class DefaultStaticDependenciesDelegator implements	NotificationServiceStaticDependenciesWrapper {

	@Override
	public String getFormatedName(Identity ident) {
		return NotificationHelper.getFormatedName(ident);
	}

	@Override
	public String getMailFooter(Identity recipient, Identity sender) {
		return MailTemplateHelper.getMailFooter(recipient, sender);
	}

	@Override
	public MailerResult sendMail(OLATPrincipal recipientTO, List<? extends OLATPrincipal> recipientsCC, List<? extends OLATPrincipal> recipientsBCC, MailTemplate template, OLATPrincipal sender) {
		return MailerWithTemplate.getInstance().sendMail(recipientTO, recipientsCC, recipientsBCC, template, sender);
	}

	@Override
	public void intermediateCommit() {
		DBFactory.getInstance().intermediateCommit();
	}

	@Override
	public Locale getLocaleFor(String language) {
		return I18nManager.getInstance().getLocaleOrDefault(language);
	}

	@Override
	public PropertyManager getPropertyManager() {
		return PropertyManager.getInstance();
	}

	@Override
	public Map<String, Object> getNotificationHandlers() {
		return CoreSpringFactory.getBeansOfType(LmsSpringBeanTypes.notificationsHandler);
	}


}
