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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.olat.connectors.rest.security.RestSecurityHelper;
import org.olat.connectors.rest.support.ObjectFactory;
import org.olat.connectors.rest.support.vo.AuthenticationVO;
import org.olat.data.basesecurity.Authentication;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * This web service handles functionalities related to authentication credentials of users.
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
@Path("users/{username}/auth")
public class UserAuthenticationWebService {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String VERSION = "1.0";

    /**
     * The version of the User Authentication Web Service
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
     * Returns all user authentications
     * 
     * @response.representation.200.qname {http://www.example.com}authenticationVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The list of all users in the OLAT system
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_AUTHVOes}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The identity not found
     * @param username
     *            The username of the user to retrieve authentication
     * @param request
     *            The HTTP request
     * @return
     */
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getAuthenticationTokenList(@PathParam("username") final String username, @Context final HttpServletRequest request) {
        if (!RestSecurityHelper.isUserManager(request)) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }

        final BaseSecurity baseSecurity = getBaseSecurity();
        final Identity identity = baseSecurity.findIdentityByName(username);
        if (identity == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }
        final List<Authentication> authentications = baseSecurity.getAuthentications(identity);
        final AuthenticationVO[] vos = new AuthenticationVO[authentications.size()];
        int count = 0;
        for (final Authentication authentication : authentications) {
            vos[count++] = ObjectFactory.get(authentication, false);
        }
        return Response.ok(vos).build();
    }

    private BaseSecurity getBaseSecurity() {
        return (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
    }

    /**
     * Creates and persists an authentication
     * 
     * @response.representation.qname {http://www.example.com}authenticationVO
     * @response.representation.mediaType application/xml, application/json
     * @response.representation.doc An authentication to save
     * @response.representation.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_AUTHVO}
     * @response.representation.200.qname {http://www.example.com}authenticationVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The saved authentication
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_AUTHVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The identity not found
     * @param username
     *            The username of the user
     * @param authenticationVO
     *            The authentication object to persist
     * @param request
     *            The HTTP request
     * @return the saved authentication
     */
    @PUT
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response create(@PathParam("username") final String username, final AuthenticationVO authenticationVO, @Context final HttpServletRequest request) {
        if (!RestSecurityHelper.isUserManager(request)) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }

        final BaseSecurity baseSecurity = getBaseSecurity();
        final Identity identity = baseSecurity.loadIdentityByKey(authenticationVO.getIdentityKey(), false);
        if (identity == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }
        if (!identity.getName().equals(username)) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }

        final String provider = authenticationVO.getProvider();
        final String authUsername = authenticationVO.getAuthUsername();
        final String credentials = authenticationVO.getCredential();
        final Authentication authentication = baseSecurity.createAndPersistAuthentication(identity, provider, authUsername, credentials);
        if (authentication == null) {
            return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
        }
        log.info("Audit:New authentication created for " + authUsername + " with provider " + provider);
        final AuthenticationVO savedAuth = ObjectFactory.get(authentication, true);
        return Response.ok(savedAuth).build();
    }

    /**
     * Fallback method for browsers
     * 
     * @response.representation.qname {http://www.example.com}authenticationVO
     * @response.representation.mediaType application/xml, application/json
     * @response.representation.doc An authentication to save
     * @response.representation.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_AUTHVO}
     * @response.representation.200.qname {http://www.example.com}authenticationVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The saved authentication
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_AUTHVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The identity not found
     * @param username
     *            The username of the user
     * @param authenticationVO
     *            The authentication object to persist
     * @param request
     *            The HTTP request
     * @return the saved authentication
     */
    @POST
    @Path("new")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response createPost(@PathParam("username") final String username, final AuthenticationVO authenticationVO, @Context final HttpServletRequest request) {
        return create(username, authenticationVO, request);
    }

    /**
     * Deletes an authentication from the system
     * 
     * @response.representation.200.doc The authentication successfully deleted
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The identity or the authentication not found
     * @param username
     *            The username of the user
     * @param authKey
     *            The authentication key identifier
     * @param request
     *            The HTTP request
     * @return <code>Response</code> object. The operation status (success or fail)
     */
    @DELETE
    @Path("{authKey}")
    public Response delete(@PathParam("username") final String username, @PathParam("authKey") final Long authKey, @Context final HttpServletRequest request) {
        if (!RestSecurityHelper.isUserManager(request)) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }
        final BaseSecurity baseSecurity = getBaseSecurity();
        final Identity identity = baseSecurity.findIdentityByName(username);
        if (identity == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }
        final List<Authentication> authentications = baseSecurity.getAuthentications(identity);
        for (final Authentication authentication : authentications) {
            if (authKey.equals(authentication.getKey())) {
                baseSecurity.deleteAuthentication(authentication);
                return Response.ok().build();
            }
        }
        return Response.serverError().status(Status.NOT_FOUND).build();
    }

    /**
     * Fallback method for browsers
     * 
     * @response.representation.200.doc The authentication successfully deleted
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The identity or the authentication not found
     * @param username
     *            The username of the user
     * @param authKey
     *            The authentication key identifier
     * @param request
     *            The HTTP request
     * @return <code>Response</code> object. The operation status (success or fail)
     */
    @POST
    @Path("{authKey}/delete")
    public Response deletePost(@PathParam("username") final String username, @PathParam("authKey") final Long authKey, @Context final HttpServletRequest request) {
        return delete(username, authKey, request);
    }
}
