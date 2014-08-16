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
 * Copyright (c) 2007 frentix GmbH, Switzerland<br>
 * <p>
 */
package org.olat.lms.user.propertyhandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.olat.lms.activitylogging.LogModule;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.configuration.Initializable;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * <h3>Description:</h3> This class implements the user properties configuration
 * <p>
 * Initial Date: 31.07.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class UserPropertiesConfigImpl implements UserPropertiesConfig, Initializable {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String USER_PROPERTY_LOG_CONFIGURATION = "userPropertyLogConfiguration";
    public static final String PACKAGE = UserPropertiesConfigImpl.class.getPackage().getName();

    private Map<String, UserPropertyHandler> userPropertyNameLookupMap;
    private final Map<String, List> userPropertyUsageContextsLookupMap = new HashMap();

    private List<UserPropertyHandler> userPropertyHandlers;
    private Map<String, UserPropertyUsageContext> userPropertyUsageContexts;

    protected UserPropertiesConfigImpl() {
    }

    @Override
    public void init() {
        final List<UserPropertyHandler> userPropertyHandlers = getUserPropertyHandlersFor(USER_PROPERTY_LOG_CONFIGURATION, false);
        final Set<String> userProperties = new LinkedHashSet<String>();
        for (final Iterator<UserPropertyHandler> iterator = userPropertyHandlers.iterator(); iterator.hasNext();) {
            userProperties.add(iterator.next().getName());
        }
        LogModule.setUserProperties(userProperties);
    }

    /**
     * Spring setter
     * 
     * @param userPropertyUsageContexts
     */
    public void setUserPropertyUsageContexts(final Map<String, UserPropertyUsageContext> userPropertyUsageContexts) {
        this.userPropertyUsageContexts = userPropertyUsageContexts;
    }

    /**
     * Spring setter
     * 
     * @param userPropertyHandlers
     */
    public void setUserPropertyHandlers(final List<UserPropertyHandler> userPropertyHandlers) {
        this.userPropertyHandlers = userPropertyHandlers;
        // populate name lookup map for faster lookup service
        userPropertyNameLookupMap = new HashMap<String, UserPropertyHandler>(userPropertyHandlers.size());
        for (final UserPropertyHandler propertyHandler : userPropertyHandlers) {
            final String name = propertyHandler.getName();
            userPropertyNameLookupMap.put(name, propertyHandler);
        }
    }

    /**
	 */
    @Override
    public UserPropertyHandler getPropertyHandler(final String handlerName) {
        final UserPropertyHandler handler = userPropertyNameLookupMap.get(handlerName);
        if (log.isDebugEnabled() && handler == null) {
            log.debug("UserPropertyHander for handlerName::" + handlerName + " not found, check your configuration.", null);
        }
        return handler;
    }

    /**
	 */
    @Override
    public Translator getTranslator(final Translator fallBack) {
        return new PackageTranslator(PACKAGE, fallBack.getLocale(), fallBack);
    }

    /**
	 */
    @Override
    public List<UserPropertyHandler> getAllUserPropertyHandlers() {
        return userPropertyHandlers;
    }

    /**
	 */
    @Override
    public List<UserPropertyHandler> getUserPropertyHandlersFor(final String usageIdentifyer, final boolean isAdministrativeUser) {
        List<UserPropertyHandler> currentUsageHandlers;
        final String key = usageIdentifyer + "_" + isAdministrativeUser;
        // synchronize access to lookup map in this VM. No need for clustering locks.
        synchronized (userPropertyUsageContextsLookupMap) {
            // use little hashmap as local cache makes no sense to perform this over
            // and over again
            currentUsageHandlers = userPropertyUsageContextsLookupMap.get(key);
            if (currentUsageHandlers != null) {
                return currentUsageHandlers;
            }
            // not found, build it and put it in cache
            currentUsageHandlers = new ArrayList<UserPropertyHandler>();
            final UserPropertyUsageContext currentUsageConfig = getCurrentUsageConfig(usageIdentifyer);
            // add all handlers that are accessable for this user
            for (final UserPropertyHandler propertyHandler : currentUsageConfig.getPropertyHandlers()) {
                // if configured for this class and if isAdministrativeUser
                if (currentUsageConfig.isForAdministrativeUserOnly(propertyHandler) && !isAdministrativeUser) {
                    // don't add this handler for this user
                    continue;
                }
                currentUsageHandlers.add(propertyHandler);
            }
            // now add list to cache
            userPropertyUsageContextsLookupMap.put(key, currentUsageHandlers);
            return currentUsageHandlers;
        }
    }

    /**
	 */
    @Override
    public boolean isMandatoryUserProperty(final String usageIdentifyer, final UserPropertyHandler propertyHandler) {
        final UserPropertyUsageContext currentUsageConfig = getCurrentUsageConfig(usageIdentifyer);
        return currentUsageConfig.isMandatoryUserProperty(propertyHandler);
    }

    /**
	 */
    @Override
    public boolean isUserViewReadOnly(final String usageIdentifyer, final UserPropertyHandler propertyHandler) {
        final UserPropertyUsageContext currentUsageConfig = getCurrentUsageConfig(usageIdentifyer);
        return currentUsageConfig.isUserViewReadOnly(propertyHandler);
    }

    /**
     * Internal helper to get the usage configuration for this identifyer
     * 
     * @param usageIdentifyer
     * @return
     */
    private UserPropertyUsageContext getCurrentUsageConfig(final String usageIdentifyer) {
        UserPropertyUsageContext currentUsageConfig = userPropertyUsageContexts.get(usageIdentifyer);
        if (currentUsageConfig == null) {
            currentUsageConfig = userPropertyUsageContexts.get("default");
            log.warn("Could not find user property usage configuration for usageIdentifyer::" + usageIdentifyer
                    + ", please check your userPropertiesContext.xml file. Using default configuration instead.");
            if (currentUsageConfig == null) {
                throw new OLATRuntimeException("Missing default user property usage configuratoin in userPropertiesContext.xml", null);
            }
        }
        return currentUsageConfig;
    }
}
