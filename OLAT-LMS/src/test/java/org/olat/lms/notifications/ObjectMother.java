/**
 * 
 */
package org.olat.lms.notifications;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.mockito.ArgumentCaptor;
import org.olat.data.basesecurity.Identity;
import org.olat.data.notifications.NotificationsDao;
import org.olat.data.notifications.Publisher;
import org.olat.data.notifications.Subscriber;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.data.user.Preferences;
import org.olat.data.user.User;
import org.olat.data.user.UserConstants;
import org.olat.lms.commons.i18n.I18nManagerStaticDependenciesWrapper;
import org.olat.lms.commons.i18n.I18nModule;
import org.olat.lms.commons.i18n.TestI18nManagerInitializer;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.StringHelper;
import org.olat.system.mail.MailTemplate;
import org.olat.system.mail.MailerResult;
import org.olat.system.security.OLATPrincipal;
import org.olat.system.security.PrincipalAttributes;

/**
 * @author patrick
 *
 */
public class ObjectMother {

	static final String THE_IDENTITY_LASTNAME = "Bichsel";
	static final String THE_IDENTITY_FIRSTNAME = "Peter";
	static final String THE_IDENTITY_NAME = "pbichsel";
	static final String ANY_IDENTITY_LASTNAME = "Birkenstock";
	static final String ANY_IDENTITY_FIRSTNAME = "Heidi";
	static final String ANY_IDENTITY_NAME = "hbirkenstock";
	private NotificationsDao notificationsDaoMock;
	private Publisher anyPublisher;
	private Publisher thePublisher;
	private NotificationsHandler notificationsHandler;
	private NotificationServiceStaticDependenciesWrapper staticDependencyMock;
	private SubscriptionContext subscriptionContext;
	private Subscriber anySubscriber;
	private Subscriber theSubscriber;
	private SubscriptionInfo theSubscriberSubscriptionInfo;
	private Identity theIdentity;
	private Preferences theIdentityPreferences;
	private ArrayList<Subscriber> subscribers;
	private HashMap<String, Object> notificationHandlersMap;
	final static Long RES_ID = Long.valueOf(123);
	final static String SUBIDENTIFIER = "SUBIDENTIFIER";
	final static String PUBLISHER_TYPE = "InfoMessage";
	final static String RES_NAME = "RES_NAME";;
	private final Locale objectMotherLocale;
	private final Date objectMotherLatestMailedDate;
	private TestI18nManagerInitializer testI18nManagerInitializer;
	private NotificationServiceImpl notificationService;
	private Identity anyIdentity;
	private Preferences anyIdentityPreferences;
	private PropertyManager propertyManagerMock;

	public ObjectMother(){
		this(Locale.ENGLISH, new Date(1970));
	}	
	
	public ObjectMother(Locale locale, Date latestEmailDate) {
		objectMotherLocale = locale;
		objectMotherLatestMailedDate = latestEmailDate;
		
		testI18nManagerInitializer = org.olat.lms.commons.i18n.ObjectMother.setupI18nManagerForFallbackLocale(locale);
		
		//the following order of method calls is important
		initAnyPublisher();
		initThePublisher();
		initPeterBichselIdentity();
		initHeidiBirkenstockIdentity();
		initSimpleNotificationWorld(locale);
	}

	public ObjectMother createNotificationsWorld() {
		return this;
	}

	private void initSimpleNotificationWorld(Locale locale) {
		notificationsDaoMock = mock(NotificationsDao.class);		
		when(notificationsDaoMock.isPublisherValid(anyPublisher)).thenReturn(true);
		
		staticDependencyMock = mock(NotificationServiceStaticDependenciesWrapper.class);
		when(staticDependencyMock.getLocaleFor(locale.getLanguage())).thenReturn(locale);
		
		propertyManagerMock = mock(PropertyManager.class);
		when(staticDependencyMock.getPropertyManager()).thenReturn(propertyManagerMock);
		
		subscriptionContext = new SubscriptionContext(RES_NAME, RES_ID, SUBIDENTIFIER);
		when(notificationsDaoMock.isPublisherValid(thePublisher)).thenReturn(true);
		
		anySubscriber = mock(Subscriber.class);
		when(anySubscriber.getPublisher()).thenReturn(anyPublisher);
		when(anySubscriber.getIdentity()).thenReturn(anyIdentity);
		
		theSubscriber = mock(Subscriber.class);
		when(theSubscriber.getPublisher()).thenReturn(thePublisher);
		when(theSubscriber.getIdentity()).thenReturn(theIdentity);
		
		
		theSubscriberSubscriptionInfo = mock(SubscriptionInfo.class);
		when(theSubscriberSubscriptionInfo.hasNews()).thenReturn(true);

		
		notificationsHandler = mock(NotificationsHandler.class);
		when(notificationsHandler.getType()).thenReturn(PUBLISHER_TYPE);
		when(notificationsHandler.createSubscriptionInfo(any(Subscriber.class), any(Locale.class), any(Date.class))).thenReturn(theSubscriberSubscriptionInfo);
		
		notificationHandlersMap = new HashMap<String, Object>();
		notificationHandlersMap.put(PUBLISHER_TYPE, notificationsHandler);
		when(staticDependencyMock.getNotificationHandlers()).thenReturn(notificationHandlersMap);
				
		subscribers = new ArrayList<Subscriber>();
		subscribers.add(theSubscriber);		
		when(notificationsDaoMock.getSubscriberList(theIdentity, PUBLISHER_TYPE)).thenReturn(subscribers);
		
		notificationService = new NotificationServiceImpl(notificationsDaoMock,notificationHandlersMap, staticDependencyMock);
		
		Map<String, Boolean> intervals = new HashMap<String, Boolean>();
		intervals.put("weekly", Boolean.TRUE);
		intervals.put("daily", Boolean.TRUE);
		notificationService.setNotificationIntervals(intervals);
		notificationService.setDefaultNotificationInterval("daily");
		
	}

	private void initPeterBichselIdentity() {
		theIdentity = mock(Identity.class);
		User user = mock(User.class);
		when(user.getRawUserProperty(UserConstants.FIRSTNAME)).thenReturn(THE_IDENTITY_FIRSTNAME);
		when(user.getRawUserProperty(UserConstants.LASTNAME)).thenReturn(THE_IDENTITY_LASTNAME);

		theIdentityPreferences = mock(Preferences.class);
		when(theIdentity.getUser()).thenReturn(user);
		PrincipalAttributes principalAttributes = mock(PrincipalAttributes.class);
		when(principalAttributes.getFirstName()).thenReturn(THE_IDENTITY_FIRSTNAME);
		when(principalAttributes.getLastName()).thenReturn(THE_IDENTITY_LASTNAME);
		
		when(theIdentity.getAttributes()).thenReturn(principalAttributes);
		when(user.getPreferences()).thenReturn(theIdentityPreferences);
		when(theIdentityPreferences.getLanguage()).thenReturn(objectMotherLocale.getLanguage());
		
		when(theIdentity.getName()).thenReturn(THE_IDENTITY_NAME);
	}
	
	private void initHeidiBirkenstockIdentity() {
		anyIdentity = mock(Identity.class);
		User user = mock(User.class);
		when(user.getRawUserProperty(UserConstants.FIRSTNAME)).thenReturn(ANY_IDENTITY_FIRSTNAME);
		when(user.getRawUserProperty(UserConstants.LASTNAME)).thenReturn(ANY_IDENTITY_LASTNAME);

		anyIdentityPreferences = mock(Preferences.class);
		when(anyIdentity.getUser()).thenReturn(user);
		PrincipalAttributes principalAttributes = mock(PrincipalAttributes.class);
		when(principalAttributes.getFirstName()).thenReturn(ANY_IDENTITY_FIRSTNAME);
		when(principalAttributes.getLastName()).thenReturn(ANY_IDENTITY_LASTNAME);
		
		when(anyIdentity.getAttributes()).thenReturn(principalAttributes);
		when(user.getPreferences()).thenReturn(anyIdentityPreferences);
		when(anyIdentityPreferences.getLanguage()).thenReturn(objectMotherLocale.getLanguage());
		
		when(anyIdentity.getName()).thenReturn(ANY_IDENTITY_NAME);
	}

	private void initThePublisher() {
		thePublisher = mock(Publisher.class);
		when(thePublisher.getResName()).thenReturn(RES_NAME);
		when(thePublisher.getResId()).thenReturn(RES_ID);
		when(thePublisher.getSubidentifier()).thenReturn(SUBIDENTIFIER);
		when(thePublisher.getType()).thenReturn(PUBLISHER_TYPE);
	}

	private void initAnyPublisher() {
		anyPublisher = mock(Publisher.class);	
		when(anyPublisher.getResName()).thenReturn("any_res");
		when(anyPublisher.getResId()).thenReturn(Long.valueOf(456));
		when(anyPublisher.getSubidentifier()).thenReturn("any_subidentifier");
		when(anyPublisher.getType()).thenReturn(PUBLISHER_TYPE);
	}

	public Map<String, Object> getNotificationHandlers() {
		return notificationHandlersMap;
	}

	public NotificationsDao getNotificationDao() {
		return notificationsDaoMock;
	}

	public NotificationServiceStaticDependenciesWrapper getNotificationStaticDependencyWrapper() {
		return staticDependencyMock;
	}

	public Publisher getAnyPublisher() {
		return anyPublisher;
	}

	public Publisher getThePublisher() {
		return thePublisher;
	}

	public SubscriptionContext getSubscriptionContext() {
		return subscriptionContext;
	}

	public Date getLatestEmailDate() {
		return objectMotherLatestMailedDate;
	}

	public Subscriber getAnySubscriber() {
		return anySubscriber;
	}

	public Subscriber getTheSubscriber() {
		return theSubscriber;
	}

	public SubscriptionInfo getSubscriptionInfo() {
		return theSubscriberSubscriptionInfo;
	}

	public Identity getTheIdendity() {
		return theIdentity;
	}
	
	public Identity getAnyIdentity() {
		return anyIdentity;
	}

	public List<Subscriber> getSubcribers() {
		return subscribers;
	}
	
	public Translator getTranslatorForNotifications() {
		return PackageUtil.createPackageTranslator(NotificationServiceImpl.CLASS_SERVING_FOR_PACKAGETRANSLATOR_CREATION, objectMotherLocale);
	}

	public NotificationServiceStaticDependenciesWrapper configureIdentityInNotificationDependencies(Identity identity){
		PrincipalAttributes attributes = identity.getAttributes();
		boolean isNotValidIdentityToMock = attributes == null;
		if(isNotValidIdentityToMock){
			throw new IllegalStateException("Provided Idenity has no PrincipalAttributes configured !");
		}
		String firstName = attributes.getFirstName();
		String lastName = attributes.getLastName();
		isNotValidIdentityToMock = !StringHelper.containsNonWhitespace(firstName) || !StringHelper.containsNonWhitespace(lastName);

		if(isNotValidIdentityToMock){
			throw new IllegalStateException("Provided Idenity has either Lastname or Firstname not configured (or both)!");
		}
		
		when(staticDependencyMock.getFormatedName(identity)).thenReturn(firstName+" "+lastName);
		when(staticDependencyMock.getMailFooter(identity, null)).thenReturn("*** Footer from Testing ***");
		
		return staticDependencyMock;
	}

	public void configureSendMailInNotificationDependencies(Identity recipient, int mailStatus) {
		MailerResult mailerResultMock = mock(MailerResult.class);
		checkForValidMailStatus(mailStatus);
		when(mailerResultMock.getReturnCode()).thenReturn(mailStatus);
		when(staticDependencyMock.sendMail(eq(recipient), anyListOf(OLATPrincipal.class), anyListOf(OLATPrincipal.class), any(MailTemplate.class), any(OLATPrincipal.class))).thenReturn(mailerResultMock );
	}
	
	private void checkForValidMailStatus(int mailStatus) {
		switch (mailStatus) {
		case MailerResult.ATTACHMENT_INVALID:
			break;
		case MailerResult.MAILHOST_UNDEFINED:
			break;
		case MailerResult.OK:
			break;
		case MailerResult.RECIPIENT_ADDRESS_ERROR:
			break;
		case MailerResult.SEND_GENERAL_ERROR:
			break;
		case MailerResult.SENDER_ADDRESS_ERROR:
			break;
		case MailerResult.TEMPLATE_GENERAL_ERROR:
			break;
		case MailerResult.TEMPLATE_PARSE_ERROR:
				break;
		default:
			throw new IllegalStateException("Your MailStatus is not valid, see MailerResult for valid stati");
		}
		
	}

	public NotificationServiceImpl getNotificationService() {
		return notificationService;
	}

	public List<Subscriber> configureValidSubscribers(Subscriber anySubscriber, Subscriber theSubscriber) {
		List<Subscriber> allValidSubscribes = new ArrayList<Subscriber>();
		allValidSubscribes.add(anySubscriber);
		allValidSubscribes.add(theSubscriber);
		when(notificationsDaoMock.getAllValidSubscribers()).thenReturn(allValidSubscribes);
		when(staticDependencyMock.getFormatedName(anyIdentity)).thenReturn(ObjectMother.ANY_IDENTITY_FIRSTNAME+" "+ObjectMother.ANY_IDENTITY_LASTNAME);
		when(staticDependencyMock.getFormatedName(theIdentity)).thenReturn(ObjectMother.THE_IDENTITY_FIRSTNAME+" "+ObjectMother.THE_IDENTITY_LASTNAME);
		return allValidSubscribes;
	}

	public PropertyImpl configureLatesEmailSentFor(Identity identity,Calendar lastRecordedMailDate) {
		//TODO:2011-10-13:PB: not nice to mock a value object! should be possible to instantiate
		PropertyImpl commonPropertyImplValue = mock(PropertyImpl.class);
		when(commonPropertyImplValue.getLongValue()).thenReturn(lastRecordedMailDate.getTimeInMillis());
		when(propertyManagerMock.findProperty(identity, null, null, null,
				NotificationServiceImpl.LATEST_EMAIL_USER_PROP)).thenReturn(commonPropertyImplValue);
		return commonPropertyImplValue;
	}


	public MailTemplate captureMailTemplateForVerification(int expectedTimes) {
		ArgumentCaptor<OLATPrincipal> senderCaptor = ArgumentCaptor.forClass(OLATPrincipal.class);
		ArgumentCaptor<OLATPrincipal> fromCaptor = ArgumentCaptor.forClass(OLATPrincipal.class);
		ArgumentCaptor<List<? extends OLATPrincipal>> recipientsCCCaptor = new ArgumentCaptor<List<? extends OLATPrincipal>>();
		ArgumentCaptor<List<? extends OLATPrincipal>> recipientsBCCCaptor = new ArgumentCaptor<List<? extends OLATPrincipal>>();
		ArgumentCaptor<MailTemplate> templateCaptor = ArgumentCaptor.forClass(MailTemplate.class);
		
		
		verify(staticDependencyMock, times(expectedTimes)).sendMail(
				fromCaptor.capture(),
				recipientsCCCaptor.capture(),
				recipientsBCCCaptor.capture(),
				templateCaptor.capture(),
				senderCaptor.capture());
		
		MailTemplate mailTemplate = templateCaptor.getValue();
		return mailTemplate;
	}
	
}
