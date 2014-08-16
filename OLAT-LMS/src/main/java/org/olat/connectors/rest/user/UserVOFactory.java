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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.connectors.rest.user;

import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.olat.connectors.rest.support.vo.LinkVO;
import org.olat.data.basesecurity.Identity;
import org.olat.data.user.User;
import org.olat.data.user.UserConstants;
import org.olat.lms.commons.i18n.I18nModule;
import org.olat.lms.user.HomePageConfig;
import org.olat.lms.user.HomePageConfigManagerImpl;
import org.olat.lms.user.UserService;
import org.olat.lms.user.propertyhandler.DatePropertyHandler;
import org.olat.lms.user.propertyhandler.GenderPropertyHandler;
import org.olat.lms.user.propertyhandler.UserPropertyHandler;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.StringHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Factory for object needed by the REST Api
 * <P>
 * Initial Date: 7 apr. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
public class UserVOFactory {

    // TODO give a direct access
    public static final String[] keys = new String[] { "male", "female", "-" };

    /*
     * EntityTag for Idenetiy is complex to compute. Every UserProperty is saved separately. public static EntityTag computeEtag(Identity identity) { int version =
     * ((IdentityImpl)identity).getVersion(); Long key = identity.getKey(); return new EntityTag("Identity-" + key + "-" + version); }
     */

    public static UserVO get(final Identity identity) {
        return get(identity, I18nModule.getDefaultLocale(), false, false);
    }

    public static UserVO get(final Identity identity, final Locale locale) {
        return get(identity, locale, false, false);
    }

    public static UserVO get(final Identity identity, final boolean allProperties, final boolean isAdmin) {
        return get(identity, I18nModule.getDefaultLocale(), allProperties, isAdmin);
    }

    public static UserVO get(final Identity identity, final Locale locale, final boolean allProperties, final boolean isAdmin) {
        final UserVO userVO = new UserVO();
        final User user = identity.getUser();
        userVO.setKey(identity.getKey());
        if (identity != null) {
            userVO.setLogin(identity.getName());
        }
        userVO.setFirstName(getUserService().getUserProperty(user, UserConstants.FIRSTNAME));
        userVO.setLastName(getUserService().getUserProperty(user, UserConstants.LASTNAME));
        userVO.setEmail(getUserService().getUserProperty(user, UserConstants.EMAIL));

        final HomePageConfig hpc = isAdmin ? null : HomePageConfigManagerImpl.getInstance().loadConfigFor(identity.getName());

        if (allProperties) {
            final List<UserPropertyHandler> propertyHandlers = getUserService().getUserPropertyHandlersFor(UserWebService.PROPERTY_HANDLER_IDENTIFIER, false);
            for (final UserPropertyHandler propertyHandler : propertyHandlers) {
                final String propName = propertyHandler.getName();
                if (hpc != null && !hpc.isEnabled(propName)) {
                    continue;
                }

                if (!UserConstants.FIRSTNAME.equals(propName) && !UserConstants.LASTNAME.equals(propName) && !UserConstants.EMAIL.equals(propName)) {

                    final String value = propertyHandler.getUserProperty(user, locale);
                    userVO.putProperty(propName, value);
                }
            }
        }
        return userVO;
    }

    public static void post(final User dbUser, final UserVO user, final Locale locale) {
        final List<UserPropertyHandler> propertyHandlers = getUserService().getUserPropertyHandlersFor(UserWebService.PROPERTY_HANDLER_IDENTIFIER, false);

        getUserService().setUserProperty(dbUser, UserConstants.FIRSTNAME, user.getFirstName());
        getUserService().setUserProperty(dbUser, UserConstants.LASTNAME, user.getLastName());
        getUserService().setUserProperty(dbUser, UserConstants.EMAIL, user.getEmail());
        for (final UserPropertyVO entry : user.getProperties()) {
            for (final UserPropertyHandler propertyHandler : propertyHandlers) {
                if (entry.getName().equals(propertyHandler.getName())) {
                    final String value = parseUserProperty(entry.getValue(), propertyHandler, locale);
                    String parsedValue;
                    if (propertyHandler instanceof DatePropertyHandler) {
                        parsedValue = formatDbDate(value, locale);
                    } else if (propertyHandler instanceof GenderPropertyHandler) {
                        parsedValue = parseGender(value, (GenderPropertyHandler) propertyHandler, locale);
                    } else {
                        parsedValue = propertyHandler.getStringValue(value, locale);
                    }
                    getUserService().setUserProperty(dbUser, entry.getName(), parsedValue);
                    break;
                }
            }
        }
    }

    public static String parseUserProperty(final String value, final UserPropertyHandler propertyHandler, final Locale locale) {
        String parsedValue;
        if (propertyHandler instanceof DatePropertyHandler) {
            parsedValue = parseDate(value, locale);
        } else if (propertyHandler instanceof GenderPropertyHandler) {
            parsedValue = parseGender(value, (GenderPropertyHandler) propertyHandler, locale);
        } else {
            parsedValue = propertyHandler.getStringValue(value, locale);
        }
        return parsedValue;
    }

    public static String formatDbUserProperty(final String value, final UserPropertyHandler propertyHandler, final Locale locale) {
        String formatedValue;
        if (propertyHandler instanceof DatePropertyHandler) {
            formatedValue = formatDbDate(value, locale);
        } else if (propertyHandler instanceof GenderPropertyHandler) {
            formatedValue = parseGender(value, (GenderPropertyHandler) propertyHandler, locale);
        } else {
            formatedValue = propertyHandler.getStringValue(value, locale);
        }
        return formatedValue;
    }

    public static UserVO link(final UserVO userVO, final UriInfo uriInfo) {
        if (uriInfo != null) {
            final UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();
            final URI getUri = baseUriBuilder.path("users").path(userVO.getKey().toString()).build();
            userVO.getLink().add(new LinkVO("self", getUri.toString(), ""));
            userVO.getLink().add(new LinkVO("edit", getUri.toString(), ""));
            userVO.getLink().add(new LinkVO("delete", getUri.toString(), ""));

            final URI groupUri = baseUriBuilder.path("users").path(userVO.getKey().toString()).path("groups").build();
            userVO.getLink().add(new LinkVO("self", groupUri.toString(), "Groups"));

            final URI portraitUri = baseUriBuilder.path("users").path(userVO.getKey().toString()).path("portrait").build();
            userVO.getLink().add(new LinkVO("self", portraitUri.toString(), "Portrait"));
        }
        return userVO;
    }

    /**
     * Allow the date to be in the raw form (yyyyMMdd) or translated to be translated
     * 
     * @param value
     * @param handler
     * @param locale
     * @return
     */
    public static final String parseDate(String value, final Locale locale) {
        if (!StringHelper.containsNonWhitespace(value)) {
            return value;
        }

        boolean raw = true;
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                raw = false;
                break;
            }
        }

        if (raw) {
            try {
                final DateFormat formater = new SimpleDateFormat("yyyyMMdd", Locale.GERMAN);
                final Date date = formater.parse(value);
                value = Formatter.getInstance(locale).formatDate(date);
            } catch (final ParseException e) {
                /* silently failed */
            }
        }
        return value;
    }

    /**
     * Allow the date to be in the localized form or not
     * 
     * @param value
     * @param handler
     * @param locale
     * @return
     */
    public static final String formatDbDate(String value, final Locale locale) {
        if (!StringHelper.containsNonWhitespace(value)) {
            return value;
        }

        boolean raw = true;
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                raw = false;
                break;
            }
        }

        if (raw) {
            return value;
        }
        try {
            final DateFormat outFormater = new SimpleDateFormat("yyyyMMdd", Locale.GERMAN);
            final DateFormat inFormater = DateFormat.getDateInstance(DateFormat.SHORT, locale);
            final Date date = inFormater.parse(value);
            value = outFormater.format(date);
        } catch (final ParseException e) {
            /* silently failed */
        }
        return value;
    }

    /**
     * Allow the value of gender to be in the raw form (male, female key word) or to be translated
     * 
     * @param value
     * @param handler
     * @param locale
     * @return
     */
    public static final String parseGender(String value, final GenderPropertyHandler handler, final Locale locale) {
        if (!StringHelper.containsNonWhitespace(value)) {
            value = "-";
        }

        final int index = Arrays.binarySearch(UserVOFactory.keys, value);
        if (index < 0) {
            // try to translate them
            boolean found = false;
            Translator trans = PackageUtil.createPackageTranslator(GenderPropertyHandler.class, locale);
            for (final String key : keys) {
                final String translation = trans.translate(handler.i18nFormElementLabelKey() + "." + key);
                if (translation.equals(value)) {
                    value = key;
                    found = true;
                    break;
                }
            }

            if (!found && !locale.equals(I18nModule.getDefaultLocale())) {
                // very last chance, try with the default locale
                trans = PackageUtil.createPackageTranslator(GenderPropertyHandler.class, I18nModule.getDefaultLocale());
                for (final String key : keys) {
                    final String translation = trans.translate(handler.i18nFormElementLabelKey() + "." + key);
                    if (translation.equals(value)) {
                        value = key;
                        found = true;
                        break;
                    }
                }
            }
        }
        return value;
    }

    private static UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
