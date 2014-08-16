/**
 * 
 */
package org.olat.lms.notifications;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.olat.data.basesecurity.Identity;
import org.olat.data.notifications.Subscriber;
import org.olat.system.mail.MailTemplate;
import org.olat.system.mail.MailerResult;

/**
 * The following tests is checking several notification service methods and ensure that english, german, italian and french translation exists for sending notification
 * mails.
 * 
 * The tests makes use of the Junit Theory feature, which allows to run the same testmethod with different input values. In this case this are the different locale for
 * en,de,it and fr. The locale tn is used to verify the verification method (check for existence of translation.key instead of translated text).
 * 
 * @author patrick
 * 
 */
@RunWith(Theories.class)
public class NotificationServiceTemplateTranslationTest {

    private ObjectMother notificationWorld;
    private NotificationServiceImpl notificationService;
    private ArrayList<SubscriptionItem> subscriptionItems;

    // not atBefore because it is used in conjunction with atTheory and atDataPoints
    public void setupWorldWith(Locale locale, Date lastEmailedDate) {
        notificationWorld = new ObjectMother(locale, lastEmailedDate);
        notificationService = notificationWorld.getNotificationService();

        subscriptionItems = new ArrayList<SubscriptionItem>();
        SubscriptionItem exampleSubscription = new SubscriptionItem("Nothing to mock here", "http://www.olat.org", "The long journey of templating.");
        subscriptionItems.add(exampleSubscription);
    }

    @Test
    @Ignore
    public void testTranslationFailsWithKeyWhenUsingUnsupportedLanguageForEmailSending() {
        // Setup
        Locale locale = new Locale("tn");
        setupWorldWith(locale, new Date(1970));

        // Exercise
        successfullSendMailToIdentityAndUpdateSubscriber();

        // verify
        // the template used is not replaced as the language does not exist.
        // This works because the translator is configured to use the provided
        // language as fallback and default language.
        MailTemplate generateMailTemplate = notificationWorld.captureMailTemplateForVerification(1);
        String subjectTemplate = generateMailTemplate.getSubjectTemplate();
        boolean translationKeyFound = subjectTemplate.indexOf(NotificationServiceImpl.KEY_RSS_TITLE) == 0;
        assertTrue("the translation key is found instead of the translated subject", translationKeyFound);
    }

    @DataPoints
    public static Locale[] workingLocales = new Locale[] { Locale.ENGLISH, Locale.GERMAN, Locale.FRENCH, Locale.ITALIAN };

    @Theory
    public void testTranslationForEmailSending(Locale locale) {
        // Setup
        setupWorldWith(locale, new Date(1970));

        // exercise
        successfullSendMailToIdentityAndUpdateSubscriber();

        // verify
        // the template used is replaced with actual text
        MailTemplate generateMailTemplate = notificationWorld.captureMailTemplateForVerification(1);
        String subjectTemplate = generateMailTemplate.getSubjectTemplate();
        verifySubjectIsTranslated(subjectTemplate);
    }

    @Theory
    public void testNotifyAllSubscriberByEmail(Locale locale) {
        setupWorldWith(locale, new Date(1970));
        // setup
        notificationWorld.configureValidSubscribers(notificationWorld.getAnySubscriber(), notificationWorld.getTheSubscriber());

        Identity anyIdentity = notificationWorld.getAnyIdentity();
        Identity theIdentity = notificationWorld.getTheIdendity();

        notificationWorld.configureLatesEmailSentFor(theIdentity, aMonthAgo());
        notificationWorld.configureLatesEmailSentFor(anyIdentity, aMonthAgo());

        notificationWorld.configureSendMailInNotificationDependencies(theIdentity, MailerResult.OK);
        notificationWorld.configureSendMailInNotificationDependencies(anyIdentity, MailerResult.OK);

        // exercise
        /** TODO: set correct title */
        notificationService.notifyAllSubscribersByEmail("");

        // verify
        // the capturing allows only the last execution to be captured, in this case this is
        // theIdentity aka Peter Bichsel.
        MailTemplate captureMailTemplateForVerification = notificationWorld.captureMailTemplateForVerification(2);
        String subjectTemplate = captureMailTemplateForVerification.getSubjectTemplate();
        verifySubjectIsTranslated(subjectTemplate);

        boolean theIdentityNameFoundInSubject = subjectTemplate.indexOf(ObjectMother.THE_IDENTITY_FIRSTNAME) > -1;
        theIdentityNameFoundInSubject = theIdentityNameFoundInSubject && subjectTemplate.indexOf(ObjectMother.THE_IDENTITY_LASTNAME) > -1;
        assertTrue("The name of theIdentity is found in the subject", theIdentityNameFoundInSubject);

    }

    private Calendar aMonthAgo() {
        Calendar lastSentNotificationMailAMonthAgo = GregorianCalendar.getInstance();
        lastSentNotificationMailAMonthAgo.add(GregorianCalendar.MONTH, -1);
        return lastSentNotificationMailAMonthAgo;
    }

    private void verifySubjectIsTranslated(String subjectTemplate) {
        boolean subjectIsTranslated = subjectTemplate.indexOf(NotificationServiceImpl.KEY_RSS_TITLE) < 0;
        assertTrue("The subject is translated and contains not the key", subjectIsTranslated);
    }

    /*
     * configure mocks etc to send a mail with a given locale
     */
    private void successfullSendMailToIdentityAndUpdateSubscriber() {

        Identity theIdentity = notificationWorld.getTheIdendity();
        notificationWorld.configureIdentityInNotificationDependencies(theIdentity);
        Identity anyIdentity = notificationWorld.getAnyIdentity();

        notificationWorld.configureSendMailInNotificationDependencies(theIdentity, MailerResult.OK);
        notificationWorld.configureSendMailInNotificationDependencies(anyIdentity, MailerResult.OK);
        List<Subscriber> subscribers = notificationWorld.getSubcribers();

        // execute
        /** TODO: set correct title */
        boolean sendMailToUserAndUpdateSubscriber = notificationService.sendMailToUserAndUpdateSubscriber(theIdentity, subscriptionItems, "", subscribers);

        // verify
        assertTrue(sendMailToUserAndUpdateSubscriber);
    }

}
