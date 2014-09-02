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
package org.olat.data.notification;

import java.util.Locale;

import javax.annotation.PostConstruct;

import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.IdentityImpl;
import org.olat.data.basesecurity.TestIdentityFactory;
import org.olat.data.commons.dao.GenericDao;
import org.olat.data.notification.Publisher.ContextType;
import org.olat.data.user.Preferences;
import org.olat.data.user.TestUserFactory;
import org.olat.data.user.UserImpl;
import org.olat.lms.core.notification.impl.UserServiceNotificationMock;
import org.olat.lms.core.notification.service.NotificationService;
import org.olat.lms.core.notification.service.NotificationSubscriptionContext;
import org.olat.lms.core.notification.service.PublishEventTO;
import org.olat.presentation.forum.ForumNotificationTypeHandler;
import org.olat.system.security.PrincipalAttributes;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date: 02.12.2011 <br>
 * 
 * @author lavinia
 */
public class DaoObjectMother {

    public static final String TEST_MAIL_SUFFIX = "@TEST.tst";
    public static final String USER_CLARA = "clara";
    public static final String USER_BERTA = "berta";
    public static final String USER_ALF = "alf";

    private Identity alf;
    private Identity clara;

    NotificationSubscriptionContext alfNotificationSubscriptionContext;

    private static Long contextId_1 = new Long(123);
    private static Long contextId_2 = new Long(223);
    private static Long contextId_3 = new Long(323);

    private static Long sourceId_1 = new Long(456);
    private static Long sourceId_2 = new Long(556);
    private static Long sourceId_3 = new Long(656);

    private Long sourceId_11 = new Long(458);
    private Long sourceId_12 = new Long(459);

    private static Long subcontextId_1 = new Long(111);
    private static Long subcontextId_2 = new Long(222);

    private static Long messageId_1 = new Long(1);
    private static Long messageId_2 = new Long(2);
    private static Long messageId_3 = new Long(2);

    @Autowired
    NotificationService notificationService;
    @Autowired
    UserServiceNotificationMock userServiceNotificationMock;
    @Autowired
    GenericDao<IdentityImpl> identityDao;
    @Autowired
    GenericDao<UserImpl> userDao;

    @PostConstruct
    void initType() {
        identityDao.setType(IdentityImpl.class);
        userDao.setType(UserImpl.class);
    }

    /**
     * Creates new transient Publisher for a Forum, and fills in the attributes - doesn't save. <br/>
     * Use createForumOnePublisher and createForumTwoPublisher instead.
     * 
     */
    @Deprecated
    public static Publisher createForumPublisher(Long contextId, Long subcontextId, Long sourceId) {
        Publisher publisher = new Publisher();
        publisher.setContextId(contextId);
        publisher.setContextType(Publisher.ContextType.COURSE);
        publisher.setSourceType(ForumNotificationTypeHandler.FORUM_SOURCE_TYPE);
        publisher.setSourceId(sourceId);
        publisher.setSubcontextId(subcontextId);
        return publisher;
    }

    /**
     * In this context.
     */
    public Publisher createForumOnePublisher() {
        return createForumPublisher(contextId_1, subcontextId_1, sourceId_1);
    }

    /**
     * In this context.
     */
    public Publisher createForumTwoPublisher() {
        return createForumPublisher(contextId_1, subcontextId_2, sourceId_2);
    }

    public Long getContextId() {
        return contextId_1;
    }

    public Long getOtherContextId() {
        return new Long(129);
    }

    public Long getSourceIdOne() {
        return sourceId_1;
    }

    public Long getSourceIdTwo() {
        return sourceId_2;
    }

    public Long getSubcontextIdOne() {
        return subcontextId_1;
    }

    public Identity createAndSaveIdentity(String username) { // TODO: alternative to the other method with the same name
        // UserImpl user = (UserImpl) userServiceNotificationMock.createUser(username + "_FIRST", username + "_LAST", username + TEST_MAIL_SUFFIX);
        UserImpl user = createUser(username + "_FIRST", username + "_LAST", username + TEST_MAIL_SUFFIX);
        userDao.save(user);
        IdentityImpl identity = TestIdentityFactory.createTestIdentityForJunit(username, user);
        identityDao.save(identity);
        return identity;
    }

    public UserImpl createUser(String firstName, String lastName, String eMail) {
        UserImpl newUser = TestUserFactory.createTestUserForJunit(firstName, lastName, eMail);
        // TODO: extract the following code in UserServiceImpl and re-use it
        Preferences prefs = newUser.getPreferences();

        Locale loc = Locale.GERMAN;
        prefs.setLanguage(loc.toString());
        prefs.setFontsize("normal");
        prefs.setPresenceMessagesPublic(false);
        prefs.setInformSessionTimeout(false);
        return newUser;
    }

    public IdentityImpl createAndSaveIdentity_(String username) {
        IdentityImpl identity = identityDao.create();
        identity.setName(username);
        identity = identityDao.save(identity);
        return identity;
    }

    /**
     * Creates 3 identities: alf, berta, clara, <br/>
     * each one subscribes to the same publisher, <br/>
     * clara creates one event, <br/>
     * and publish this event. <br/>
     * Returns the number of published events: 2 (for alf and berta).
     */
    public int createThreeIdentities_subscribe_publishOneEvent(NotificationService notificationService) {
        clara = createThreeIdentities_subscribe(notificationService);

        PublishEventTO publishEventTO = createFirstPublishEventTO(clara);
        int eventCounter = notificationService.publishEvent(publishEventTO);
        return eventCounter;
    }

    public PublishEventTO createFirstPublishEventTO(Identity creatorIdentity) {
        PublishEventTO publishEventTO = PublishEventTO.getValidInstance(ContextType.COURSE, contextId_1, "AAA_course", subcontextId_1,
                ForumNotificationTypeHandler.FORUM_SOURCE_TYPE, sourceId_1, "a_Forum", "a_message", creatorIdentity, PublishEventTO.EventType.CHANGED);
        publishEventTO.setSourceEntryId(messageId_1.toString());
        return publishEventTO;
    }

    /**
     * Assumes that clara already published more events for this publisher.
     */
    public int publishEventAsClara(NotificationService notificationService) {
        PublishEventTO publishEventTO = PublishEventTO.getValidInstance(ContextType.COURSE, contextId_1, "AAA_course", subcontextId_1,
                ForumNotificationTypeHandler.FORUM_SOURCE_TYPE, sourceId_1, "a_Forum", "a_message", clara, PublishEventTO.EventType.CHANGED);
        publishEventTO.setSourceEntryId(messageId_1.toString());
        return notificationService.publishEvent(publishEventTO);
    }

    /**
     * creates 3 identities: alf, berta, clara and subscribes all of them.
     */
    private Identity createThreeIdentities_subscribe(NotificationService notificationService) {
        alf = createAndSaveIdentity(USER_ALF);
        Identity berta = createAndSaveIdentity(USER_BERTA);
        clara = createAndSaveIdentity(USER_CLARA);

        alfNotificationSubscriptionContext = subscribeToPublisher(alf, contextId_1, sourceId_1);
        subscribeToPublisher(berta, contextId_1, sourceId_1);
        subscribeToPublisher(clara, contextId_1, sourceId_1);
        return clara;
    }

    public NotificationSubscriptionContext subscribeToPublisher(Identity alf, Long contextId, Long sourceId) {
        NotificationSubscriptionContext alfNotificationSubscriptionContext = new NotificationSubscriptionContext(alf, ForumNotificationTypeHandler.FORUM_SOURCE_TYPE,
                sourceId, Publisher.ContextType.COURSE, contextId, subcontextId_1);

        notificationService.subscribe(alfNotificationSubscriptionContext);
        return alfNotificationSubscriptionContext;
    }

    /**
     * creates 3 identities, subscribe each one to 3 publishers, and return the creator identity. <br/>
     * We choose clara as creator, this means she triggers the events, but she won't get notified.
     * 
     */
    private Identity createThreeIdentities_subscribeToThreePublishers() {
        alf = createAndSaveIdentity(USER_ALF);
        Identity berta = createAndSaveIdentity(USER_BERTA);
        clara = createAndSaveIdentity(USER_CLARA);

        // subscribe to publisher_1
        subscribeToPublisher(alf, contextId_1, sourceId_1);
        subscribeToPublisher(berta, contextId_1, sourceId_1);
        subscribeToPublisher(clara, contextId_1, sourceId_1);
        // subscribe to publisher_2
        subscribeToPublisher(alf, contextId_2, sourceId_2);
        subscribeToPublisher(berta, contextId_2, sourceId_2);
        subscribeToPublisher(clara, contextId_2, sourceId_2);
        // subscribe to publisher_3
        subscribeToPublisher(alf, contextId_3, sourceId_3);
        subscribeToPublisher(berta, contextId_3, sourceId_3);
        subscribeToPublisher(clara, contextId_3, sourceId_3);

        return clara;
    }

    private Identity createThreeIdentities_subscribeToFivePublishersInThreeCourses() {
        alf = createAndSaveIdentity(USER_ALF);
        Identity berta = createAndSaveIdentity(USER_BERTA);
        clara = createAndSaveIdentity(USER_CLARA);

        // subscribe to publisher_1
        subscribeToPublisher(alf, contextId_1, sourceId_1);
        subscribeToPublisher(berta, contextId_1, sourceId_1);
        subscribeToPublisher(clara, contextId_1, sourceId_1);

        // subscribe to publisher_2 in the same course
        subscribeToPublisher(alf, contextId_1, sourceId_11);
        subscribeToPublisher(berta, contextId_1, sourceId_11);
        subscribeToPublisher(clara, contextId_1, sourceId_11);

        // subscribe to publisher_2 in the same course
        subscribeToPublisher(alf, contextId_1, sourceId_12);
        subscribeToPublisher(berta, contextId_1, sourceId_12);
        subscribeToPublisher(clara, contextId_1, sourceId_12);

        // subscribe to publisher_2
        subscribeToPublisher(alf, contextId_2, sourceId_2);
        subscribeToPublisher(berta, contextId_2, sourceId_2);
        subscribeToPublisher(clara, contextId_2, sourceId_2);
        // subscribe to publisher_3
        subscribeToPublisher(alf, contextId_3, sourceId_3);
        subscribeToPublisher(berta, contextId_3, sourceId_3);
        subscribeToPublisher(clara, contextId_3, sourceId_3);

        return clara;
    }

    /**
     * Publishes 6 events (2 subscribers for 3 publishers, that is 6 subscriptions, one event per subscription - the creator of the event doesn't get notified)
     */
    public int createThreeIdentities_subscribeToThreePublishers_publishOneEventForEachPublisher() throws Exception {
        clara = createThreeIdentities_subscribeToThreePublishers();

        int eventCounter = publishMessageEvent(clara, contextId_1, "AAA_course", sourceId_1, "a_Forum", messageId_1, "a_message", PublishEventTO.EventType.CHANGED);

        Thread.sleep(3600);
        eventCounter += publishMessageEvent(clara, contextId_3, "CAA_course", sourceId_3, "c_Forum", messageId_3, "c_message", PublishEventTO.EventType.NEW);

        Thread.sleep(3600);
        eventCounter += publishMessageEvent(clara, contextId_2, "BAA_course", sourceId_2, "b_Forum", messageId_2, "b_message", PublishEventTO.EventType.NEW);

        return eventCounter;
    }

    /**
     * Publishes 13 events.
     */
    public int createThreeIdentities_subscribeToFivePublishers_publishEventsForEachPublisher() throws Exception {
        clara = createThreeIdentities_subscribeToFivePublishersInThreeCourses();

        int eventCounter = publishMessageEvent(clara, contextId_1, "AAA_course", sourceId_1, "a_Forum", messageId_1, "a_message", PublishEventTO.EventType.CHANGED);

        Thread.sleep(3600);
        eventCounter += publishMessageEvent(clara, contextId_3, "CAA_course", sourceId_3, "c_Forum", messageId_3, "c_message", PublishEventTO.EventType.NEW);

        Thread.sleep(3600);
        eventCounter += publishMessageEvent(clara, contextId_2, "BAA_course", sourceId_2, "b_Forum", messageId_2, "b_message", PublishEventTO.EventType.NEW);

        Thread.sleep(3600);
        eventCounter += publishMessageEvent(clara, contextId_3, "CAA_course", sourceId_3, "c_Forum", messageId_1, "a_message", PublishEventTO.EventType.NEW);

        Thread.sleep(3600);
        eventCounter += publishMessageEvent(clara, contextId_3, "CAA_course", sourceId_3, "c_Forum", messageId_2, "b_message", PublishEventTO.EventType.NEW);

        Thread.sleep(3600);
        eventCounter += publishMessageEvent(clara, contextId_2, "BAA_course", sourceId_2, "b_Forum", messageId_1, "a_message", PublishEventTO.EventType.NEW);

        Thread.sleep(3600);
        eventCounter += publishMessageEvent(clara, contextId_2, "BAA_course", sourceId_2, "b_Forum", messageId_3, "c_message", PublishEventTO.EventType.NEW);

        Thread.sleep(3600);
        eventCounter += publishMessageEvent(clara, contextId_1, "AAA_course", sourceId_1, "a_Forum", messageId_3, "c_message", PublishEventTO.EventType.CHANGED);

        Thread.sleep(3600);
        eventCounter += publishMessageEvent(clara, contextId_1, "AAA_course", sourceId_1, "a_Forum", messageId_2, "b_message", PublishEventTO.EventType.CHANGED);

        Thread.sleep(3600);
        eventCounter += publishMessageEvent(clara, contextId_1, "AAA_course", sourceId_11, "ba_Forum", messageId_3, "ba_message", PublishEventTO.EventType.NEW);

        Thread.sleep(3600);
        eventCounter += publishMessageEvent(clara, contextId_1, "AAA_course", sourceId_11, "ba_Forum", messageId_3, "ba_message", PublishEventTO.EventType.CHANGED);

        Thread.sleep(3600);
        eventCounter += publishMessageEvent(clara, contextId_1, "AAA_course", sourceId_11, "ba_Forum", messageId_3, "ba_message", PublishEventTO.EventType.CHANGED);

        Thread.sleep(3600);
        eventCounter += publishMessageEvent(clara, contextId_1, "AAA_course", sourceId_12, "ca_Forum", messageId_3, "ca_message", PublishEventTO.EventType.CHANGED);

        return eventCounter;
    }

    private int publishMessageEvent(Identity clara, Long contextId, String contextTitle, Long sourceId, String sourceTitle, Long messageId, String sourceEntryTitle,
            PublishEventTO.EventType type) {
        PublishEventTO publishEventTO_1 = PublishEventTO.getValidInstance(ContextType.COURSE, contextId, contextTitle, subcontextId_1,
                ForumNotificationTypeHandler.FORUM_SOURCE_TYPE, sourceId, sourceTitle, sourceEntryTitle, clara, type);
        publishEventTO_1.setSourceEntryId(messageId.toString());
        int eventCounter = notificationService.publishEvent(publishEventTO_1);
        return eventCounter;
    }

    public String getAlfRecipientEmail() {
        return USER_ALF + TEST_MAIL_SUFFIX;
    }

    public Identity getSubscriberIdentityAlf() {
        return alf;
    }

    public Identity getEventCreatorIdentity() {

        return new IdentityMock(clara, "Clara", "Seseman", "clara@gmail.com");
    }

    public String getEventCreatorUsername() {
        return USER_CLARA;
    }

    public NotificationSubscriptionContext getAlfNotificationSubscriptionContext() {
        return alfNotificationSubscriptionContext;
    }

    class IdentityMock extends IdentityImpl {
        Identity identity;
        PrincipalAttributes principalAttributes;

        IdentityMock(Identity identityImpl, String firstName, String lastName, String email) {
            identity = identityImpl;
            principalAttributes = new PrincipalAttributesMock(firstName, lastName, email);
        }

        public PrincipalAttributes getAttributes() {
            return principalAttributes;
        }
    }

    class PrincipalAttributesMock implements PrincipalAttributes {

        String firstName, lastName, email;

        PrincipalAttributesMock(String firstName_, String lastName_, String email_) {
            firstName = firstName_;
            lastName = lastName_;
            email = email_;
        }

        @Override
        public String getEmail() {

            return email;
        }

        @Override
        public String getInstitutionalEmail() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isEmailDisabled() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public String getFirstName() {

            return firstName;
        }

        @Override
        public String getLastName() {

            return lastName;
        }

    }

}
