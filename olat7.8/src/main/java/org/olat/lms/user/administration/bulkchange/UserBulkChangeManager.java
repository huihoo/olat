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
 * Copyright (c) since 2004 at frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.lms.user.administration.bulkchange;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Constants;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.user.Preferences;
import org.olat.data.user.User;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.lms.security.authentication.OLATAuthManager;
import org.olat.lms.user.UserService;
import org.olat.lms.user.propertyhandler.UserPropertyHandler;
import org.olat.presentation.framework.core.components.form.ValidationError;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.user.administration.bulkchange.UserBulkChangeStep00;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * this is a helper class which can be used in bulkChange-Steps and also the UsermanagerUserSearchController
 * <P>
 * Initial Date: 07.03.2008 <br>
 * 
 * @author rhaag
 */
public class UserBulkChangeManager extends BasicManager {
    private static VelocityEngine velocityEngine;
    private static final Logger log = LoggerHelper.getLogger();

    private static UserBulkChangeManager INSTANCE = new UserBulkChangeManager();

    public static final String PWD_IDENTIFYER = "password";
    public static final String LANG_IDENTIFYER = "language";

    public UserBulkChangeManager() {
        // init velocity engine
        Properties p = null;
        try {
            velocityEngine = new VelocityEngine();
            p = new Properties();
            p.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
            p.setProperty("runtime.log.logsystem.log4j.category", "syslog");
            velocityEngine.init(p);
        } catch (final Exception e) {
            throw new RuntimeException("config error " + p.toString());
        }
    }

    public static UserBulkChangeManager getInstance() {
        return INSTANCE;
    }

    public void changeSelectedIdentities(final List<Identity> selIdentities, final HashMap<String, String> attributeChangeMap,
            final HashMap<String, String> roleChangeMap, final ArrayList<String> notUpdatedIdentities, final boolean isAdministrativeUser, final Translator trans) {

        final Translator transWithFallback = getUserService().getUserPropertiesConfig().getTranslator(trans);
        final String usageIdentifyer = UserBulkChangeStep00.class.getCanonicalName();

        notUpdatedIdentities.clear();
        final List<Identity> changedIdentities = new ArrayList<Identity>();
        final List<UserPropertyHandler> userPropertyHandlers = getUserService().getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
        final String[] securityGroups = { Constants.GROUP_USERMANAGERS, Constants.GROUP_GROUPMANAGERS, Constants.GROUP_AUTHORS, Constants.GROUP_ADMIN };
        final BaseSecurity secMgr = (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);

        // loop over users to be edited:
        for (Identity identity : selIdentities) {
            final DB db = DBFactory.getInstance();
            // reload identity from cache, to prevent stale object
            identity = (Identity) db.loadObject(identity);
            final User user = identity.getUser();
            String errorDesc = "";
            boolean updateError = false;
            // change pwd
            if (attributeChangeMap.containsKey(PWD_IDENTIFYER)) {
                String newPwd = attributeChangeMap.get(PWD_IDENTIFYER);
                if (StringHelper.containsNonWhitespace(newPwd)) {
                    if (!getUserService().verifyPasswordStrength("", newPwd, "")) {
                        errorDesc = transWithFallback.translate("error.password");
                        updateError = true;
                    }
                } else {
                    newPwd = null;
                }
                OLATAuthManager.changePasswordAsAdmin(identity, newPwd);
            }

            // set language
            final String userLanguage = user.getPreferences().getLanguage();
            if (attributeChangeMap.containsKey(LANG_IDENTIFYER)) {
                final String inputLanguage = attributeChangeMap.get(LANG_IDENTIFYER);
                if (!userLanguage.equals(inputLanguage)) {
                    final Preferences preferences = user.getPreferences();
                    preferences.setLanguage(I18nManager.getInstance().getLocaleOrDefault(inputLanguage));
                    user.setPreferences(preferences);
                }
            }

            final Context vcContext = new VelocityContext();
            // set all properties as context
            setUserContext(identity, vcContext, isAdministrativeUser);
            // loop for each property configured in UserBulkChangeStep00
            for (int k = 0; k < userPropertyHandlers.size(); k++) {
                final UserPropertyHandler propHandler = userPropertyHandlers.get(k);
                final String propertyName = propHandler.getName();
                final String userValue = getUserService().getUserProperty(identity.getUser(), propertyName);
                String inputFieldValue = "";
                if (attributeChangeMap.containsKey(propertyName)) {
                    inputFieldValue = attributeChangeMap.get(propertyName);
                    inputFieldValue = inputFieldValue.replace("$", "$!");
                    final String evaluatedInputFieldValue = evaluateValueWithUserContext(inputFieldValue, vcContext);

                    // validate evaluated property-value
                    final ValidationError validationError = new ValidationError();
                    // do validation checks with users current locale!
                    final Locale locale = transWithFallback.getLocale();
                    if (!propHandler.isValidValue(evaluatedInputFieldValue, validationError, locale)) {
                        errorDesc = transWithFallback.translate(validationError.getErrorKey()) + " (" + evaluatedInputFieldValue + ")";
                        updateError = true;
                        break;
                    }

                    if (!evaluatedInputFieldValue.equals(userValue)) {
                        final String stringValue = propHandler.getStringValue(evaluatedInputFieldValue, locale);
                        propHandler.setUserProperty(user, stringValue);
                    }
                }

            } // for (propertyHandlers)

            // set roles for identity
            // loop over securityGroups defined above
            for (final String securityGroup : securityGroups) {
                final SecurityGroup secGroup = secMgr.findSecurityGroupByName(securityGroup);
                final Boolean isInGroup = secMgr.isIdentityInSecurityGroup(identity, secGroup);
                String thisRoleAction = "";
                if (roleChangeMap.containsKey(securityGroup)) {
                    thisRoleAction = roleChangeMap.get(securityGroup);
                    // user not anymore in security group, remove him
                    if (isInGroup && thisRoleAction.equals("remove")) {
                        secMgr.removeIdentityFromSecurityGroup(identity, secGroup);
                    }
                    // user not yet in security group, add him
                    if (!isInGroup && thisRoleAction.equals("add")) {
                        secMgr.addIdentityToSecurityGroup(identity, secGroup);
                    }
                }
            }

            // set status
            if (roleChangeMap.containsKey("Status")) {
                final Integer status = Integer.parseInt(roleChangeMap.get("Status"));
                secMgr.saveIdentityStatus(identity, status);
                identity = (Identity) db.loadObject(identity);
            }

            // persist changes:
            if (updateError) {
                final String errorOutput = identity.getName() + ": " + errorDesc;
                log.debug("error during bulkChange of users, following user could not be updated: " + errorOutput);
                notUpdatedIdentities.add(errorOutput);
            } else {
                getUserService().updateUserFromIdentity(identity);
                changedIdentities.add(identity);
                log.info("Audit:user successfully changed during bulk-change: " + identity.getName());
            }

            // commit changes for this user
            db.intermediateCommit();
        }

    }

    public String evaluateValueWithUserContext(final String valToEval, final Context vcContext) {
        final StringWriter evaluatedUserValue = new StringWriter();
        // evaluate inputFieldValue to get a concatenated string
        try {
            velocityEngine.evaluate(vcContext, evaluatedUserValue, "vcUservalue", valToEval);
        } catch (final ParseErrorException e) {
            log.error("parsing of values in BulkChange Field not possible!");
            e.printStackTrace();
            return "ERROR";
        } catch (final MethodInvocationException e) {
            log.error("evaluating of values in BulkChange Field not possible!");
            e.printStackTrace();
            return "ERROR";
        } catch (final ResourceNotFoundException e) {
            log.error("evaluating of values in BulkChange Field not possible!");
            e.printStackTrace();
            return "ERROR";
        } catch (final IOException e) {
            log.error("evaluating of values in BulkChange Field not possible!");
            e.printStackTrace();
            return "ERROR";
        }
        return evaluatedUserValue.toString();
    }

    /**
     * @param identity
     * @param vcContext
     * @param isAdministrativeUser
     */
    public void setUserContext(final Identity identity, final Context vcContext, final boolean isAdministrativeUser) {
        List<UserPropertyHandler> userPropertyHandlers2;
        userPropertyHandlers2 = getUserService().getAllUserPropertyHandlers();
        for (final UserPropertyHandler userPropertyHandler : userPropertyHandlers2) {
            final String propertyName = userPropertyHandler.getName();
            final String userValue = getUserService().getUserProperty(identity.getUser(), propertyName);
            vcContext.put(propertyName, userValue);
        }
    }

    public Context getDemoContext(final Locale locale, final boolean isAdministrativeUser) {
        final Translator propertyTrans = PackageUtil.createPackageTranslator(UserPropertyHandler.class, locale);
        return getDemoContext(propertyTrans, isAdministrativeUser);
    }

    public Context getDemoContext(final Translator propertyTrans, final boolean isAdministrativeUser) {
        final Context vcContext = new VelocityContext();
        List<UserPropertyHandler> userPropertyHandlers2;
        userPropertyHandlers2 = getUserService().getAllUserPropertyHandlers();
        for (final UserPropertyHandler userPropertyHandler : userPropertyHandlers2) {
            final String propertyName = userPropertyHandler.getName();
            final String userValue = propertyTrans.translate("import.example." + userPropertyHandler.getName());
            vcContext.put(propertyName, userValue);
        }
        return vcContext;
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
