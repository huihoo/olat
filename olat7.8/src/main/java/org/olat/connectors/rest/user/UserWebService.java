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

import static org.olat.connectors.rest.security.RestSecurityHelper.getLocale;
import static org.olat.connectors.rest.security.RestSecurityHelper.getUserRequest;
import static org.olat.connectors.rest.security.RestSecurityHelper.isUserManager;
import static org.olat.connectors.rest.user.UserVOFactory.formatDbUserProperty;
import static org.olat.connectors.rest.user.UserVOFactory.get;
import static org.olat.connectors.rest.user.UserVOFactory.link;
import static org.olat.connectors.rest.user.UserVOFactory.parseUserProperty;
import static org.olat.connectors.rest.user.UserVOFactory.post;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.olat.connectors.rest.support.ObjectFactory;
import org.olat.connectors.rest.support.vo.ErrorVO;
import org.olat.connectors.rest.support.vo.GroupVO;
import org.olat.data.basesecurity.Authentication;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.fileutil.ImageHelper;
import org.olat.data.group.BusinessGroup;
import org.olat.data.user.User;
import org.olat.data.user.UserConstants;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.user.DisplayPortraitManager;
import org.olat.lms.user.UserService;
import org.olat.lms.user.administration.delete.UserDeletionManager;
import org.olat.lms.user.propertyhandler.UserPropertyHandler;
import org.olat.presentation.framework.core.components.form.ValidationError;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.CodeHelper;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.WebappHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * This web service handles functionalities related to <code>User</code>.
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
@Path("users")
public class UserWebService {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String VERSION = "1.0";
    public static final String PROPERTY_HANDLER_IDENTIFIER = UserWebService.class.getName();
    private BusinessGroupService businessGroupService;
    public static CacheControl cc = new CacheControl();

    /**
	 * 
	 */
    public UserWebService() {
        cc.setMaxAge(-1);
    }

    /**
     * The version of the User Web Service
     * 
     * @response.representation.200.mediaType text/plain
     * @response.representation.200.doc The version of this specific Web Service
     * @response.representation.200.example 1.0
     * @return The version number
     */
    @GET
    @Path("version")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getVersion() {
        return Response.ok(VERSION).build();
    }

    /**
     * Search users and return them in a simple form (without user properties). User properties can be added two the query parameters. If the authUsername and the
     * authProvider are set, the search is made only with these two parameters because they are sufficient to return a single user.
     * 
     * @response.representation.200.qname {http://www.example.com}userVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The list of all users in the OLAT system
     * @response.representation.200.example {@link org.olat.connectors.rest.user.Examples#SAMPLE_USERVOes}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @param login
     *            The login (search with like)
     * @param authProvider
     *            An authentication provider (optional)
     * @param authUsername
     *            An specific username from the authentication provider
     * @param uriInfo
     *            The URI infos
     * @param httpRequest
     *            The HTTP request
     * @return An array of users
     */
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getUserListQuery(@QueryParam("login") final String login, @QueryParam("authProvider") final String authProvider,
            @QueryParam("authUsername") final String authUsername, @Context final UriInfo uriInfo, @Context final HttpServletRequest httpRequest) {
        final MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
        return getUserList(login, authProvider, authUsername, params, uriInfo, httpRequest);
    }

    private Response getUserList(final String login, final String authProvider, final String authUsername, final Map<String, List<String>> params, final UriInfo uriInfo,
            final HttpServletRequest httpRequest) {
        if (!isUserManager(httpRequest)) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }

        List<Identity> identities;
        // make only a search by authUsername
        if (StringHelper.containsNonWhitespace(authProvider) && StringHelper.containsNonWhitespace(authUsername)) {
            final Authentication auth = getBaseSecurity().findAuthenticationByAuthusername(authUsername, authProvider);
            if (auth == null) {
                identities = Collections.emptyList();
            } else {
                identities = Collections.singletonList(auth.getIdentity());
            }
        } else {
            String[] authProviders = null;
            if (StringHelper.containsNonWhitespace(authProvider)) {
                authProviders = new String[] { authProvider };
            }

            // retrieve and convert the parameters value
            final Map<String, String> userProps = new HashMap<String, String>();
            if (!params.isEmpty()) {
                final Locale locale = getLocale(httpRequest);
                final List<UserPropertyHandler> propertyHandlers = getUserService().getUserPropertyHandlersFor(PROPERTY_HANDLER_IDENTIFIER, false);
                for (final UserPropertyHandler handler : propertyHandlers) {
                    if (!params.containsKey(handler.getName())) {
                        continue;
                    }

                    final List<String> values = params.get(handler.getName());
                    if (values.isEmpty()) {
                        continue;
                    }

                    final String value = formatDbUserProperty(values.get(0), handler, locale);
                    userProps.put(handler.getName(), value);
                }
            }

            identities = getBaseSecurity().getIdentitiesByPowerSearch(login, userProps, true, null, null, authProviders, null, null, null, null,
                    Identity.STATUS_VISIBLE_LIMIT);
        }

        int count = 0;
        final UserVO[] userVOs = new UserVO[identities.size()];
        for (final Identity identity : identities) {
            userVOs[count++] = link(get(identity), uriInfo);
        }
        return Response.ok(userVOs).build();
    }

    private BaseSecurity getBaseSecurity() {
        return (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
    }

    /**
     * Creates and persists a new user entity
     * 
     * @response.representation.qname {http://www.example.com}userVO
     * @response.representation.mediaType application/xml, application/json
     * @response.representation.doc The user to persist
     * @response.representation.example {@link org.olat.connectors.rest.user.Examples#SAMPLE_USERVO}
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The persisted user
     * @response.representation.200.example {@link org.olat.connectors.rest.user.Examples#SAMPLE_USERVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.406.mediaType application/xml, application/json
     * @response.representation.406.doc The list of errors
     * @response.representation.406.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_ERRORVOes}
     * @param user
     *            The user to persist
     * @param request
     *            The HTTP request
     * @return the new persisted <code>User</code>
     */
    @PUT
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response create(final UserVO user, @Context final HttpServletRequest request) {
        if (!isUserManager(request)) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }

        // Check if login is still available
        final Identity identity = getBaseSecurity().findIdentityByName(user.getLogin());
        if (identity != null) {
            final Locale locale = getLocale(request);
            final Translator translator = new PackageTranslator("org.olat.presentation.user.administration", locale);
            final String translation = translator.translate("new.error.loginname.choosen");
            final ErrorVO[] errorVos = new ErrorVO[] { new ErrorVO("org.olat.presentation.user.administration", "new.error.loginname.choosen", translation) };
            return Response.ok(errorVos).status(Status.NOT_ACCEPTABLE).build();
        }

        final List<ErrorVO> errors = validateUser(user, request);
        if (errors.isEmpty()) {
            final User newUser = getUserService().createUser(user.getFirstName(), user.getLastName(), user.getEmail());
            final Identity id = getBaseSecurity().createAndPersistIdentityAndUserWithUserGroup(user.getLogin(), user.getPassword(), newUser);
            post(newUser, user, getLocale(request));
            getUserService().updateUser(newUser);
            return Response.ok(get(id)).build();
        }

        // content not ok
        final ErrorVO[] errorVos = new ErrorVO[errors.size()];
        errors.toArray(errorVos);
        return Response.ok(errorVos).status(Status.NOT_ACCEPTABLE).build();
    }

    /**
     * Fallback method for browser
     * 
     * @response.representation.qname {http://www.example.com}userVO
     * @response.representation.mediaType application/xml, application/json
     * @response.representation.doc The user to persist
     * @response.representation.example {@link org.olat.connectors.rest.user.Examples#SAMPLE_USERVO}
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The persisted user
     * @response.representation.200.example {@link org.olat.connectors.rest.user.Examples#SAMPLE_USERVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.406.mediaType application/xml, application/json
     * @response.representation.406.doc The list of errors
     * @response.representation.406.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_ERRORVOes}
     * @param user
     *            The user to persist
     * @param request
     *            The HTTP request
     * @return the new persisted <code>User</code>
     */
    @POST
    @Path("new")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response createPost(final UserVO user, @Context final HttpServletRequest request) {
        return create(user, request);
    }

    /**
     * Retrieves an user given its unique key identifier
     * 
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The user
     * @response.representation.200.example {@link org.olat.connectors.rest.user.Examples#SAMPLE_USERVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The identity not found
     * @param identityKey
     *            The user key identifier of the user being searched
     * @param uriInfo
     *            The URI infos
     * @param httpRequest
     *            The HTTP request
     * @param request
     *            The REST request
     * @return an xml or json representation of a the user being search. The xml correspond to a <code>UserVO</code>. <code>UserVO</code> is a simplified representation
     *         of the <code>User</code> and <code>Identity</code>
     */
    @GET
    @Path("{identityKey}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response findById(@PathParam("identityKey") final Long identityKey, @Context final UriInfo uriInfo, @Context final HttpServletRequest httpRequest,
            @Context final Request request) {
        try {
            final Identity identity = getBaseSecurity().loadIdentityByKey(identityKey, false);
            if (identity == null) {
                return Response.serverError().status(Status.NOT_FOUND).build();
            }

            final boolean isUserManager = isUserManager(httpRequest);
            final UserVO userVO = link(get(identity, true, isUserManager), uriInfo);
            return Response.ok(userVO).build();
        } catch (final Throwable e) {
            throw new WebApplicationException(e);
        }
    }

    /**
     * Retrieves the portrait of an user
     * 
     * @response.representation.200.mediaType application/octet-stream
     * @response.representation.200.doc The portrait as image
     * @response.representation.404.doc The identity or the portrait not found
     * @param identityKey
     *            The identity key of the user being searched
     * @param request
     *            The REST request
     * @return The image
     */
    @GET
    @Path("{identityKey}/portrait")
    @Produces({ "image/jpeg", "image/jpg", MediaType.APPLICATION_OCTET_STREAM })
    public Response getPortrait(@PathParam("identityKey") final Long identityKey, @Context final Request request) {
        try {
            final Identity identity = getBaseSecurity().loadIdentityByKey(identityKey, false);
            if (identity == null) {
                return Response.serverError().status(Status.NOT_FOUND).build();
            }

            final File portraitDir = DisplayPortraitManager.getInstance().getPortraitDir(identity);
            final File portrait = new File(portraitDir, DisplayPortraitManager.PORTRAIT_BIG_FILENAME);
            if (!portrait.exists()) {
                return Response.serverError().status(Status.NOT_FOUND).build();
            }

            final Date lastModified = new Date(portrait.lastModified());
            Response.ResponseBuilder response = request.evaluatePreconditions(lastModified);
            if (response == null) {
                response = Response.ok(portrait).lastModified(lastModified).cacheControl(cc);
            }
            return response.build();
        } catch (final Throwable e) {
            throw new WebApplicationException(e);
        }
    }

    /**
     * Upload the portrait of an user
     * 
     * @response.representation.200.mediaType application/octet-stream
     * @response.representation.200.doc The portrait as image
     * @response.representation.401.doc Not authorized
     * @response.representation.404.doc The identity or the portrait not found
     * @param identityKey
     *            The user key identifier of the user being searched
     * @param fileName
     *            The name of the image (mandatory)
     * @param file
     *            The image
     * @param request
     *            The REST request
     * @return The image
     */
    @POST
    @Path("{identityKey}/portrait")
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    public Response postPortrait(@PathParam("identityKey") final Long identityKey, @FormParam("filename") final String filename,
            @FormParam("file") final InputStream file, @Context final HttpServletRequest request) {
        try {
            final Identity identity = getBaseSecurity().loadIdentityByKey(identityKey, false);
            if (identity == null) {
                return Response.serverError().status(Status.NOT_FOUND).build();
            }

            final Identity authIdentity = getUserRequest(request).getIdentity();
            if (!isUserManager(request) && !identity.equalsByPersistableKey(authIdentity)) {
                return Response.serverError().status(Status.UNAUTHORIZED).build();
            }

            final File tmpFile = getTmpFile(filename);
            FileUtils.save(file, tmpFile);

            final DisplayPortraitManager dps = DisplayPortraitManager.getInstance();
            final File uploadDir = dps.getPortraitDir(identity);
            final File pBigFile = new File(uploadDir, DisplayPortraitManager.PORTRAIT_BIG_FILENAME);
            final File pSmallFile = new File(uploadDir, DisplayPortraitManager.PORTRAIT_SMALL_FILENAME);
            boolean ok = ImageHelper.scaleImage(tmpFile, pBigFile, DisplayPortraitManager.WIDTH_PORTRAIT_BIG);
            if (ok) {
                ok = ImageHelper.scaleImage(tmpFile, pSmallFile, DisplayPortraitManager.WIDTH_PORTRAIT_SMALL);
            }
            tmpFile.delete();
            return Response.ok().build();
        } catch (final Throwable e) {
            throw new WebApplicationException(e);
        }
    }

    /**
     * Deletes the portrait of an user
     * 
     * @response.representation.200.doc The portrait deleted
     * @response.representation.401.doc Not authorized
     * @param identityKey
     *            The identity key identifier of the user being searched
     * @param request
     *            The REST request
     * @return The image
     */
    @DELETE
    @Path("{identityKey}/portrait")
    public Response deletePortrait(@PathParam("identityKey") final Long identityKey, @Context final HttpServletRequest request) {
        try {
            final Identity authIdentity = getUserRequest(request).getIdentity();
            final Identity identity = getBaseSecurity().loadIdentityByKey(identityKey, false);
            if (identity == null) {
                return Response.serverError().status(Status.NOT_FOUND).build();
            } else if (!isUserManager(request) && !identity.equalsByPersistableKey(authIdentity)) {
                return Response.serverError().status(Status.UNAUTHORIZED).build();
            }

            final DisplayPortraitManager dps = DisplayPortraitManager.getInstance();
            final File uploadDir = dps.getPortraitDir(identity);
            final File pBigFile = new File(uploadDir, DisplayPortraitManager.PORTRAIT_BIG_FILENAME);
            if (pBigFile.exists()) {
                pBigFile.delete();
            }
            final File pSmallFile = new File(uploadDir, DisplayPortraitManager.PORTRAIT_SMALL_FILENAME);
            if (pSmallFile.exists()) {
                pSmallFile.delete();
            }
            return Response.ok().build();
        } catch (final Throwable e) {
            throw new WebApplicationException(e);
        }
    }

    /**
     * Return all groups of a user
     * 
     * @response.representation.200.qname {http://www.example.com}groupVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The groups of the user
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_GROUPVOes}
     * @response.representation.404.doc The identity not found
     * @param identityKey
     *            The key of the user
     * @return
     */
    @GET
    @Path("{identityKey}/groups")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getUserGroupList(@PathParam("identityKey") final Long identityKey) {
        final Identity retrievedUser = getBaseSecurity().loadIdentityByKey(identityKey, false);
        if (retrievedUser == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }

        final List<BusinessGroup> groups = new ArrayList<BusinessGroup>();
        final List<String> bgTypes = new ArrayList<String>();
        bgTypes.add(BusinessGroup.TYPE_BUDDYGROUP);
        bgTypes.add(BusinessGroup.TYPE_LEARNINGROUP);

        final Set<Long> groupIds = new HashSet<Long>();
        for (final String bgType : bgTypes) {
            final List<BusinessGroup> attendedGroups = businessGroupService.findBusinessGroupsAttendedBy(bgType, retrievedUser, null);
            for (final BusinessGroup group : attendedGroups) {
                if (!groupIds.contains(group.getKey())) {
                    groups.add(group);
                    groupIds.add(group.getKey());
                }
            }

            final List<BusinessGroup> ownedGroups = businessGroupService.findBusinessGroupsOwnedBy(bgType, retrievedUser, null);
            for (final BusinessGroup group : ownedGroups) {
                if (!groupIds.contains(group.getKey())) {
                    groups.add(group);
                    groupIds.add(group.getKey());
                }
            }
        }

        int count = 0;
        final GroupVO[] groupVOs = new GroupVO[groups.size()];
        for (final BusinessGroup group : groups) {
            groupVOs[count++] = ObjectFactory.get(group);
        }
        return Response.ok(groupVOs).build();
    }

    /**
     * Update an user
     * 
     * @response.representation.qname {http://www.example.com}userVO
     * @response.representation.mediaType application/xml, application/json
     * @response.representation.doc The user
     * @response.representation.example {@link org.olat.connectors.rest.user.Examples#SAMPLE_USERVO}
     * @response.representation.200.qname {http://www.example.com}userVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The user
     * @response.representation.200.example {@link org.olat.connectors.rest.user.Examples#SAMPLE_USERVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The identity not found
     * @response.representation.406.qname {http://www.example.com}errorVO
     * @response.representation.406.mediaType application/xml, application/json
     * @response.representation.406.doc The list of validation errors
     * @response.representation.406.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_ERRORVOes}
     * @param identityKey
     *            The user key identifier
     * @param user
     *            The user datas
     * @param uriInfo
     *            The URI infos
     * @param request
     *            The HTTP request
     * @return <code>User</code> object. The operation status (success or fail)
     */
    @POST
    @Path("{identityKey}")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response update(@PathParam("identityKey") final Long identityKey, final UserVO user, @Context final UriInfo uriInfo, @Context final HttpServletRequest request) {
        try {
            if (user == null) {
                return Response.serverError().status(Status.NO_CONTENT).build();
            }
            if (!isUserManager(request)) {
                return Response.serverError().status(Status.UNAUTHORIZED).build();
            }

            final BaseSecurity baseSecurity = getBaseSecurity();
            final Identity retrievedIdentity = baseSecurity.loadIdentityByKey(identityKey, false);
            if (retrievedIdentity == null) {
                return Response.serverError().status(Status.NOT_FOUND).build();
            }

            final User retrievedUser = retrievedIdentity.getUser();
            final List<ErrorVO> errors = validateUser(user, request);
            if (errors.isEmpty()) {
                post(retrievedUser, user, getLocale(request));
                getUserService().updateUser(retrievedUser);
                return Response.ok(link(get(retrievedIdentity, true, true), uriInfo)).build();
            }

            // content not ok
            final ErrorVO[] errorVos = new ErrorVO[errors.size()];
            errors.toArray(errorVos);
            return Response.ok(errorVos).status(Status.NOT_ACCEPTABLE).build();
        } catch (final Exception e) {
            log.error("Error updating an user", e);
            return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private List<ErrorVO> validateUser(final UserVO user, final HttpServletRequest request) {

        final Locale locale = getLocale(request);
        final List<ErrorVO> errors = new ArrayList<ErrorVO>();
        final List<UserPropertyHandler> propertyHandlers = getUserService().getUserPropertyHandlersFor(PROPERTY_HANDLER_IDENTIFIER, false);
        validateProperty(UserConstants.FIRSTNAME, user.getFirstName(), propertyHandlers, errors, getUserService(), locale);
        validateProperty(UserConstants.LASTNAME, user.getLastName(), propertyHandlers, errors, getUserService(), locale);
        validateProperty(UserConstants.EMAIL, user.getEmail(), propertyHandlers, errors, getUserService(), locale);
        for (final UserPropertyHandler propertyHandler : propertyHandlers) {
            if (!UserConstants.FIRSTNAME.equals(propertyHandler.getName()) && !UserConstants.LASTNAME.equals(propertyHandler.getName())
                    && !UserConstants.EMAIL.equals(propertyHandler.getName())) {
                validateProperty(user, propertyHandler, errors, getUserService(), locale);
            }
        }
        return errors;
    }

    private boolean validateProperty(final String name, final String value, final List<UserPropertyHandler> handlers, final List<ErrorVO> errors,
            final UserService userService, final Locale locale) {
        for (final UserPropertyHandler handler : handlers) {
            if (handler.getName().equals(name)) {
                return validateProperty(value, handler, errors, userService, locale);
            }
        }
        return true;
    }

    private boolean validateProperty(final UserVO user, final UserPropertyHandler userPropertyHandler, final List<ErrorVO> errors, final UserService userService,
            final Locale locale) {
        final String value = user.getProperty(userPropertyHandler.getName());
        return validateProperty(value, userPropertyHandler, errors, userService, locale);
    }

    private boolean validateProperty(String value, final UserPropertyHandler userPropertyHandler, final List<ErrorVO> errors, final UserService userService,
            final Locale locale) {
        final ValidationError error = new ValidationError();
        if (!StringHelper.containsNonWhitespace(value) && getUserService().isMandatoryUserProperty(PROPERTY_HANDLER_IDENTIFIER, userPropertyHandler)) {
            final Translator translator = new PackageTranslator("org.olat.presentation", locale);
            final String translation = translator.translate("new.form.mandatory");
            errors.add(new ErrorVO("org.olat.presentation", "new.form.mandatory", translation));
            return false;
        }

        value = parseUserProperty(value, userPropertyHandler, locale);

        if (!userPropertyHandler.isValidValue(value, error, locale)) {
            final String pack = userPropertyHandler.getClass().getPackage().getName();
            final Translator translator = new PackageTranslator(pack, locale);
            final String translation = translator.translate(error.getErrorKey());
            errors.add(new ErrorVO(pack, error.getErrorKey(), translation));
            return false;
        }

        return true;
    }

    /**
     * Delete an user from the system
     * 
     * @response.representation.200.doc The user is removed from the group
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The identity not found
     * @param identityKey
     *            The user key identifier
     * @param request
     *            The HTTP request
     * @return <code>Response</code> object. The operation status (success or fail)
     */
    @DELETE
    @Path("{identityKey}")
    public Response delete(@PathParam("identityKey") final Long identityKey, @Context final HttpServletRequest request) {
        if (!isUserManager(request)) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }

        final Identity identity = getBaseSecurity().loadIdentityByKey(identityKey, false);
        if (identity == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }
        UserDeletionManager.getInstance().deleteIdentity(identity);
        return Response.ok().build();
    }

    /**
     * Fallback method for browsers
     * 
     * @response.representation.200.doc The user is removed from the group
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The identity not found
     * @param identityKey
     *            The user key identifier
     * @param request
     *            The HTTP request
     * @return
     */
    @POST
    @Path("{identityKey}/delete")
    @Produces(MediaType.APPLICATION_XML)
    public Response deletePost(@PathParam("identityKey") final Long identityKey, @Context final HttpServletRequest request) {
        return delete(identityKey, request);
    }

    private File getTmpFile(String suffix) {
        suffix = (suffix == null ? "" : suffix);
        final File tmpFile = new File(WebappHelper.getUserDataRoot() + "/tmp/", CodeHelper.getGlobalForeverUniqueID() + "_" + suffix);
        FileUtils.createEmptyFile(tmpFile);
        return tmpFile;
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
