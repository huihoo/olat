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
package org.olat.connectors.rest.security;

import static org.olat.system.security.AuthenticationConstants.AUTHENTICATION_PROVIDER_OLAT;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.security.authentication.AuthenticationService;
import org.olat.lms.security.authentication.AuthenticationService.Provider;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.security.authentication.AuthHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Authenticate against OLAT Provider
 * <P>
 * Initial Date: 7 apr. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
@Path("auth")
public class Authentication {

    private static final String VERSION = "1.0";

    /**
     * Retrieves the version of the User Authentication Web Service
     * 
     * @response.representation.200.mediaType text/plain
     * @response.representation.200.doc The version of this specific Web Service
     * @response.representation.200.example 1.0
     * @return
     */
    @GET
    @Path("version")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getVersion() {
        return Response.ok(VERSION).build();
    }

    /**
     * Authenticates against OLAT Provider and provides a security token if authentication is successful. The security token is returned as a header named X-OLAT-TOKEN.
     * Given that the password is sent in clear text and not encrypted, it is not advisable to use this service over a none secure connection (https).
     * 
     * @response.representation.200.mediaType text/plain, application/xml
     * @response.representation.200.doc Say hello to the authenticated user, and give it a security token
     * @response.representation.200.example &lt;hello&gt;Hello john&lt;/hello&gt;
     * @response.representation.401.doc The authentication has failed
     * @response.representation.404.doc The identity not found
     * @param username
     *            The username
     * @param password
     *            The password (the password is in clear text, not encrypted)
     * @param httpRequest
     *            The HTTP request
     * @return
     */
    @GET
    @Path("{username}")
    @Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML })
    public Response login(@PathParam("username") final String username, @QueryParam("password") final String password, @Context final HttpServletRequest httpRequest) {

        final UserRequest ureq = RestSecurityHelper.getUserRequest(httpRequest);
        AuthenticationService authenticationService = (AuthenticationService) CoreSpringFactory.getBean(AuthenticationService.class);
        final Identity identity = authenticationService.authenticate(username, password, Provider.OLAT);
        if (identity == null) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }

        final int loginStatus = AuthHelper.doHeadlessLogin(identity, AUTHENTICATION_PROVIDER_OLAT, ureq);
        if (loginStatus == AuthHelper.LOGIN_OK) {
            // Forge a new security token
            final RestSecurityBean securityBean = (RestSecurityBean) CoreSpringFactory.getBean(RestSecurityBean.class);
            final String token = securityBean.generateToken(identity);
            return Response.ok("<hello identityKey=\"" + identity.getKey() + "\">Hello " + username + "</hello>", MediaType.APPLICATION_XML)
                    .header(RestSecurityHelper.SEC_TOKEN, token).build();
        }
        return Response.serverError().status(Status.UNAUTHORIZED).build();
    }
}
