package org.olat.lms.notifications;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.data.basesecurity.Identity;
import org.olat.data.group.BusinessGroup;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.mail.MailTemplate;
import org.olat.system.mail.MailerResult;
import org.olat.system.security.OLATPrincipal;

/**
 * wrap all static calls inside the notification service for testability.
 * @author patrick
 *
 */
public interface NotificationServiceStaticDependenciesWrapper {

	String getFormatedName(Identity ident);

	String getMailFooter(Identity recipient, Identity sender);

	MailerResult sendMail(OLATPrincipal recipientTO, List<? extends OLATPrincipal> recipientsCC, List<? extends OLATPrincipal> recipientsBCC, MailTemplate template, OLATPrincipal sender);

	void intermediateCommit();

	/**
	 * 
	 * @param language as string, null also allowed
	 * @return in any case a locale. Either the specified one, or the system default locale
	 */
	Locale getLocaleFor(String language);

	PropertyManager getPropertyManager();

	Map<String, Object> getNotificationHandlers();

}
