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
package org.olat.lms.activitylogging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.olat.data.activitylogging.ActivityLoggingDao;
import org.olat.data.activitylogging.LoggingObject;
import org.olat.data.basesecurity.Identity;
import org.olat.data.user.Preferences;
import org.olat.data.user.User;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.presentation.commons.session.SessionInfo;
import org.olat.presentation.commons.session.UserSession;
import org.olat.system.commons.ReturnValue;

/**
 * Test for ActivityLogger, method 'log'.
 * 
 * @author Christian Guretzki
 */
public class UserActivityLoggerImplTest {

    private ActivityLoggingDao activityLoggingDaoMock;
    private UserSession userSessionMock;
    private ILoggingAction defaultLoggingActionMock;
    private Set<String> userProperties;
    private I18nManager i18nManagerMock;
    private String LocaleKey = "de";
    private String testUserName = "test-user";

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        userSessionMock = createUserSessionMock(testUserName);
        activityLoggingDaoMock = mock(ActivityLoggingDao.class);

        defaultLoggingActionMock = createLoggingActionMock(ActionType.tracking);
        userProperties = new HashSet<String>();
        i18nManagerMock = mock(I18nManager.class);
        when(i18nManagerMock.getLocaleOrDefault(LocaleKey)).thenReturn(new Locale(LocaleKey));
    }

    /**
     * Test method 'log' when user-session is null. Input : Result: no logging is done
     */
    @Test
    public void testLog_noLoggingWithoutUserSession() {
        // Create service without user-session
        IUserActivityLogger userActivityLogger = createUserActivityLogger(null, false); // UserSession is null
        userActivityLogger.setStickyActionType(null);
        ReturnValue<LoggingObject> returnValue = userActivityLogger.log(defaultLoggingActionMock, this.getClass(), null);
        verify(activityLoggingDaoMock, never()).saveLogObject(returnValue.getValue());
    }

    /**
     * Test method 'log' when user-session is null. Input : Result: no logging is done
     */
    @Test
    public void testLog_noLoggingWithAnonymusModeActionTypeTracking() {
        IUserActivityLogger userActivityLogger = createUserActivityLogger(userSessionMock, true); // isLogAnonymous = true
        ReturnValue<LoggingObject> returnValue = userActivityLogger.log(defaultLoggingActionMock, this.getClass(), null);
        verify(activityLoggingDaoMock, never()).saveLogObject(returnValue.getValue());
    }

    /**
     * Test method 'log' when UserSession.getSessionInfo() is null. Input : Result: no logging is done
     */
    @Test
    public void testLog_noLoggingWhenSessionInfoIsNull() {
        when(userSessionMock.getSessionInfo()).thenReturn(null);
        IUserActivityLogger userActivityLogger = createUserActivityLogger(userSessionMock, false);
        ReturnValue<LoggingObject> returnValue = userActivityLogger.log(defaultLoggingActionMock, this.getClass(), null);
        verify(activityLoggingDaoMock, never()).saveLogObject(returnValue.getValue());
        assertNull("should not log when 'SessionInfo is null' ", returnValue.getValue());
    }

    /**
     * Test method 'log' when Usession_.getSessionInfo().getSession() is null. Input : Result: no logging is done
     */
    @Test
    public void testLog_noLoggingWhenSessionInfoSessionIsNull() {
        // Mock SessionInfo to have SessionInfo.getSession() is null
        SessionInfo sessionInfo = mock(SessionInfo.class);
        when(sessionInfo.getSession()).thenReturn(null);
        when(userSessionMock.getSessionInfo()).thenReturn(sessionInfo);

        IUserActivityLogger userActivityLogger = createUserActivityLogger(userSessionMock, false);
        ReturnValue<LoggingObject> returnValue = userActivityLogger.log(defaultLoggingActionMock, this.getClass(), null);
        verify(activityLoggingDaoMock, never()).saveLogObject(returnValue.getValue());
        assertNull("should not log when 'SessionInfo.getSession() is null' ", returnValue.getValue());
    }

    /**
     * Test method 'log' when Usession_.getSessionInfo().getSession().getId() is null. Input : Result: no logging is done
     */
    @Test
    public void testLog_noLoggingWhenSessionInfoSessionIdIsNull() {
        // Mock SessionInfo to have SessionInfo.getSession().getId is null
        HttpSession session = mock(HttpSession.class);
        when(session.getId()).thenReturn(null);
        SessionInfo sessionInfo = mock(SessionInfo.class);
        when(sessionInfo.getSession()).thenReturn(session);
        when(userSessionMock.getSessionInfo()).thenReturn(sessionInfo);

        IUserActivityLogger userActivityLogger = createUserActivityLogger(userSessionMock, false);
        ReturnValue<LoggingObject> returnValue = userActivityLogger.log(defaultLoggingActionMock, this.getClass(), null);
        verify(activityLoggingDaoMock, never()).saveLogObject(returnValue.getValue());
        assertNull("should not log when 'SessionInfo.getSession().getId() is null' ", returnValue.getValue());
    }

    /**
     * Test method 'log' when session_.getSessionInfo().getSession().getId().length() is 0. Input : Result: no logging is done
     */
    @Test
    public void testLog_noLoggingWhenSessionInfoSessionIdhasLengthZero() {
        // Mock SessionInfo to have SessionInfo.getSession().getId has length 0
        HttpSession session = mock(HttpSession.class);
        when(session.getId()).thenReturn("");
        SessionInfo sessionInfo = mock(SessionInfo.class);
        when(sessionInfo.getSession()).thenReturn(session);
        when(userSessionMock.getSessionInfo()).thenReturn(sessionInfo);

        IUserActivityLogger userActivityLogger = createUserActivityLogger(userSessionMock, false);
        ReturnValue<LoggingObject> returnValue = userActivityLogger.log(defaultLoggingActionMock, this.getClass(), null);
        verify(activityLoggingDaoMock, never()).saveLogObject(returnValue.getValue());
        assertNull("should not log when 'SessionInfo.getSession().getId().length = 0' ", returnValue.getValue());
    }

    /**
     * Test method 'log' when session.getIdentity() is null. Input : Result: no logging is done
     */
    @Test
    public void testLog_noLoggingWhenSessionIdentityIsNull() {
        // Mock SessionInfo to have SessionInfo.getSession().getId has length 0
        HttpSession session = mock(HttpSession.class);
        when(session.getId()).thenReturn("session_id");
        SessionInfo sessionInfo = mock(SessionInfo.class);
        when(sessionInfo.getSession()).thenReturn(session);
        when(userSessionMock.getSessionInfo()).thenReturn(sessionInfo);
        when(userSessionMock.getIdentity()).thenReturn(null);

        IUserActivityLogger userActivityLogger = createUserActivityLogger(userSessionMock, false);
        ReturnValue<LoggingObject> returnValue = userActivityLogger.log(defaultLoggingActionMock, this.getClass(), null);
        verify(activityLoggingDaoMock, never()).saveLogObject(returnValue.getValue());
        assertNull("should not log when 'SessionInfo.getIdentity() is null' ", returnValue.getValue());
    }

    /**
     * Test method 'log' when session_.getSessionInfo().getSession().getId().length() is 0. Input : Result: no logging is done
     */
    @Test
    public void testLog_AnonymousLoggingHasNoUsernameForActionTypeStatistic() {
        IUserActivityLogger userActivityLogger = createUserActivityLogger(userSessionMock, true);
        ILoggingAction loginAction = createLoggingActionMock(ActionType.statistic);
        when(loginAction.getActionObject()).thenReturn("test");

        ReturnValue<LoggingObject> returnValue = userActivityLogger.log(loginAction, this.getClass(), null);
        verify(activityLoggingDaoMock).saveLogObject(returnValue.getValue());
        assertNotNull("Missing logging, no log-object created", returnValue.getValue());
        assertEquals("Logging must have empty username '' because it is anonymus and ActionType=statistic", "", returnValue.getValue().getUserName());
    }

    /**
     * Test method 'log' when session_.getSessionInfo().getSession().getId().length() is 0. Input : Result: no logging is done
     */
    @Test
    public void testLog_NoneAnonymousLoggingHasUsernameForActionTypeStatistic() {
        IUserActivityLogger userActivityLogger = createUserActivityLogger(userSessionMock, false);
        ILoggingAction loginAction = createLoggingActionMock(ActionType.statistic);
        when(loginAction.getActionObject()).thenReturn("test");

        ReturnValue<LoggingObject> returnValue = userActivityLogger.log(loginAction, this.getClass(), null);
        verify(activityLoggingDaoMock).saveLogObject(returnValue.getValue());
        assertNotNull("Missing logging, no log-object created ", returnValue.getValue());
        assertEquals("Logging must use username '" + testUserName + "' because it is non-anonymus and ActionType=statistic", testUserName, returnValue.getValue()
                .getUserName());
    }

    /**
     * Test method 'log' when session_.getSessionInfo().getSession().getId().length() is 0. Input : Result: no logging is done
     */
    @Test
    public void testLog_NoAnonymousLoggingForActionTypeAdmin() {
        IUserActivityLogger userActivityLogger = createUserActivityLogger(userSessionMock, true);
        ILoggingAction loginAction = createLoggingActionMock(ActionType.admin);
        when(loginAction.getActionObject()).thenReturn("test");

        ReturnValue<LoggingObject> returnValue = userActivityLogger.log(loginAction, this.getClass(), null);
        verify(activityLoggingDaoMock).saveLogObject(returnValue.getValue());
        assertNotNull("Missing logging, no log-object created", returnValue.getValue());
        assertEquals("Logging must have username '" + testUserName + "' because it is anonymus but ActionType=admin", testUserName, returnValue.getValue().getUserName());
    }

    @Test
    public void testLog_update() {
        IUserActivityLogger userActivityLogger = createUserActivityLogger(userSessionMock, true);
        ILoggingAction loginAction = createLoggingActionMock(ActionType.admin);
        when(loginAction.getActionObject()).thenReturn("test");

        // first call to create log object
        ReturnValue<LoggingObject> returnValue = userActivityLogger.log(loginAction, this.getClass());
        verify(activityLoggingDaoMock).saveLogObject(returnValue.getValue());
        when(userSessionMock.getEntry(UserActivityLoggerImpl.USESS_KEY_USER_ACTIVITY_LOGGING_LAST_LOG)).thenReturn(returnValue.getValue());

        // second call for duration
        ReturnValue<LoggingObject> lastReturnValue = userActivityLogger.log(loginAction, this.getClass());
        // TODO ticket I-130413-0018
        // we don't update duration as long as last logging action is not persisted (key != null)
        // verify(activityLoggingDaoMock).updateDuration(eq(returnValue.getValue()), anyLong());
        verify(activityLoggingDaoMock).saveLogObject(lastReturnValue.getValue());
    }

    // ////////////////////////
    // Helper to create Mock's
    // ////////////////////////
    /**
     * @param actionType
     *            Use this action type e.g. ActionType.tracking
     * @return LogingAction with CrudAction.create, ActionVerb.add add passed parameter actionType
     */
    private ILoggingAction createLoggingActionMock(ActionType actionType) {
        ILoggingAction loginAction = mock(ILoggingAction.class);
        when(loginAction.getCrudAction()).thenReturn(CrudAction.create);
        when(loginAction.getActionVerb()).thenReturn(ActionVerb.add);
        when(loginAction.getResourceActionType()).thenReturn(actionType);
        return loginAction;
    }

    /**
     * 
     * @param userName
     *            Use this username
     * @return UserSession with session_id='session_id' , identity with username='test-user', localKey='de'
     */
    private UserSession createUserSessionMock(String userName) {
        // Mock SessionInfo to have SessionInfo.getSession().getId has length 0
        UserSession userSessionMock = mock(UserSession.class);
        HttpSession session = mock(HttpSession.class);
        when(session.getId()).thenReturn("session_id");
        SessionInfo sessionInfo = mock(SessionInfo.class);
        when(sessionInfo.getSession()).thenReturn(session);
        when(userSessionMock.getSessionInfo()).thenReturn(sessionInfo);
        Identity testIdentity = mock(Identity.class);
        when(testIdentity.getName()).thenReturn(testUserName);
        when(userSessionMock.getIdentity()).thenReturn(testIdentity);

        // identity.getUser().getPreferences().getLanguage()
        User userMock = mock(User.class);
        Preferences preferencesMock = mock(Preferences.class);
        when(preferencesMock.getLanguage()).thenReturn(LocaleKey);
        when(userMock.getPreferences()).thenReturn(preferencesMock);
        when(testIdentity.getUser()).thenReturn(userMock);
        return userSessionMock;
    }

    private IUserActivityLogger createUserActivityLogger(UserSession userSession, boolean isLogAnonymous) {
        return new UserActivityLoggerImpl(userSession, activityLoggingDaoMock, isLogAnonymous, userProperties, i18nManagerMock);
    }

}
