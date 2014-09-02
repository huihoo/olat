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

package org.olat.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.After;
import org.junit.Before;
import org.olat.connectors.rest.RestModule;
import org.olat.connectors.rest.security.RestApiLoginFilter;
import org.olat.connectors.rest.security.RestSecurityHelper;
import org.olat.connectors.rest.support.OlatRestApplication;
import org.olat.connectors.rest.support.vo.ErrorVO;
import org.olat.connectors.rest.support.vo.LinkVO;
import org.olat.system.commons.StringHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;

import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.http.servlet.ServletAdapter;
import com.sun.jersey.test.framework.spi.container.TestContainer;

/**
 * Description:<br>
 * Abstract class which start and stop a grizzly server for every test
 * <P>
 * Initial Date: 14 apr. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
public abstract class OlatJerseyTestCase extends OlatTestCase {

    private static final Logger log = LoggerHelper.getLogger();

    protected static final JsonFactory jsonFactory = new JsonFactory();

    private final static String CONTEXT_PATH = "olat";

    private final static int PORT = 9998;
    private final static String HOST = "localhost";
    private final static String PROTOCOL = "http";

    private GrizzlyWebServer webServer;

    @Autowired
    private RestModule restModule;

    /**
     * @param arg0
     */
    public OlatJerseyTestCase() {
        super();
        instantiateGrizzlyWebServer();
    }

    /**
     * Instantiates the Grizzly Web Server
     */
    private void instantiateGrizzlyWebServer() {
        webServer = new GrizzlyWebServer(PORT);
        final ServletAdapter sa = new ServletAdapter();
        Servlet servletInstance = null;
        try {
            servletInstance = (HttpServlet) Class.forName("com.sun.jersey.spi.container.servlet.ServletContainer").newInstance();
        } catch (final Exception ex) {
            log.error("Cannot instantiate the Grizzly Servlet Container", ex);
        }
        sa.setServletInstance(servletInstance);
        sa.addFilter(new RestApiLoginFilter(), "jerseyfilter", null);
        sa.addInitParameter("javax.ws.rs.Application", OlatRestApplication.class.getName());
        sa.setContextPath("/" + CONTEXT_PATH);
        webServer.addGrizzlyAdapter(sa, null);
    }

    protected URI getBaseURI() {
        return UriBuilder.fromUri(PROTOCOL + "://" + HOST + "/").port(PORT).build();
    }

    protected URI getContextURI() {
        return UriBuilder.fromUri(getBaseURI()).path(CONTEXT_PATH).build();
    }

    @Before
    public void setUp() throws Exception {
        // always enabled the REST API for testing
        restModule.setEnabled(true);

        log.info("Starting the Grizzly Web Container...");
        try {
            webServer.start();
        } catch (final IOException ex) {
            log.error("Cannot start the Grizzly Web Container");
        }
    }

    /**
     * Tear down the test by invoking {@link TestContainer#stop() } on the test container obtained from the test container factory.
     * 
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        log.info("Stopping the Grizzly Web Container...");
        webServer.stop();
        webServer.getSelectorThread().stopEndpoint();
    }

    /**
     * @return bare bone HttpClient
     */
    public HttpClient getHttpClient() {
        final HttpClient c = new HttpClient();
        c.getHostConfiguration().setHost(HOST, PORT, PROTOCOL);
        return c;
    }

    /**
     * Return an authenticated HttpClient based on session cookie
     * 
     * @param username
     * @param password
     * @return
     * @throws HttpException
     * @throws IOException
     */
    public HttpClient getAuthenticatedCookieBasedClient(final String username, final String password) throws HttpException, IOException {
        final HttpClient c = getHttpClient();
        loginWithCookie(username, password, c);
        return c;
    }

    /**
     * Return an authenticated HttpClient based on session cookie
     * 
     * @param username
     * @param password
     * @return
     * @throws HttpException
     * @throws IOException
     */
    public String getAuthenticatedTokenBasedClient(final String username, final String password) throws HttpException, IOException {
        final HttpClient c = getHttpClient();
        return loginWithToken(username, password, c);
    }

    /**
     * Login the HttpClient based on the session cookie
     * 
     * @param username
     * @param password
     * @param c
     * @throws HttpException
     * @throws IOException
     */
    public HttpClient loginWithCookie(final String username, final String password) throws HttpException, IOException {
        final HttpClient c = getHttpClient();
        final URI uri = UriBuilder.fromUri(getContextURI()).path("auth").path(username).queryParam("password", password).build();
        final GetMethod method = new GetMethod(uri.toString());
        method.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
        final int response = c.executeMethod(method);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        assertTrue(response == 200);
        assertTrue(body != null && body.length() > "<hello></hello>".length());
        return c;
    }

    /**
     * Login the HttpClient based on the session cookie
     * 
     * @param username
     * @param password
     * @param c
     * @throws HttpException
     * @throws IOException
     */
    public void loginWithCookie(final String username, final String password, final HttpClient c) throws HttpException, IOException {
        final URI uri = UriBuilder.fromUri(getContextURI()).path("auth").path(username).queryParam("password", password).build();
        final GetMethod method = new GetMethod(uri.toString());
        method.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
        final int response = c.executeMethod(method);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        assertTrue(response == 200);
        assertTrue(body != null && body.length() > "<hello></hello>".length());
    }

    /**
     * Login the HttpClient based on the session cookie
     * 
     * @param username
     * @param password
     * @param c
     * @throws HttpException
     * @throws IOException
     */
    public String loginWithToken(final String username, final String password, final HttpClient c) throws HttpException, IOException {
        final URI uri = UriBuilder.fromUri(getContextURI()).path("auth").path(username).queryParam("password", password).build();
        final GetMethod method = new GetMethod(uri.toString());
        final int response = c.executeMethod(method);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        assertTrue(response == 200);
        assertTrue(body != null && body.length() > "<hello></hello>".length());
        final Header securityToken = method.getResponseHeader(RestSecurityHelper.SEC_TOKEN);
        assertTrue(securityToken != null && StringHelper.containsNonWhitespace(securityToken.getValue()));
        return securityToken == null ? null : securityToken.getValue();
    }

    public String getToken(final HttpMethod method) {
        final Header header = method.getResponseHeader(RestSecurityHelper.SEC_TOKEN);
        return header == null ? null : header.getValue();
    }

    public GetMethod createGet(final String requestStr, final String accept, final boolean cookie) {
        final URI requestURI = UriBuilder.fromUri(getContextURI()).path(requestStr).build();
        return createGet(requestURI, accept, cookie);
    }

    /**
     * Return a GetMethod
     * 
     * @param requestURI
     * @param accept
     *            accepted mime-type
     * @param cookie
     *            allow cookie or not
     * @return
     */
    public GetMethod createGet(final URI requestURI, final String accept, final boolean cookie) {
        final GetMethod method = new GetMethod(requestURI.toString());
        if (cookie) {
            method.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
        }
        method.addRequestHeader("Accept", accept);
        return method;
    }

    public PutMethod createPut(final String requestStr, final String accept, final boolean cookie) {
        final URI requestURI = UriBuilder.fromUri(getContextURI()).path(requestStr).build();
        return createPut(requestURI, accept, cookie);
    }

    public PutMethod createPut(final URI requestURI, final String accept, final boolean cookie) {
        final PutMethod method = new PutMethod(requestURI.toString());
        if (cookie) {
            method.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
        }
        if (StringHelper.containsNonWhitespace(accept)) {
            method.addRequestHeader("Accept", accept);
        }
        method.addRequestHeader("Accept-Language", "en");
        return method;
    }

    public PostMethod createPost(final String requestStr, final String accept, final boolean cookie) {
        final URI requestURI = UriBuilder.fromUri(getContextURI()).path(requestStr).build();
        return createPost(requestURI, accept, cookie);
    }

    public PostMethod createPost(final URI requestURI, final String accept, final boolean cookie) {
        final PostMethod method = new PostMethod(requestURI.toString());
        if (cookie) {
            method.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
        }
        if (StringHelper.containsNonWhitespace(accept)) {
            method.addRequestHeader("Accept", accept);
        }
        return method;
    }

    public DeleteMethod createDelete(final String requestStr, final String accept, final boolean cookie) {
        final URI requestURI = UriBuilder.fromUri(getContextURI()).path(requestStr).build();
        return createDelete(requestURI, accept, cookie);
    }

    public DeleteMethod createDelete(final URI requestURI, final String accept, final boolean cookie) {
        final DeleteMethod method = new DeleteMethod(requestURI.toString());
        if (cookie) {
            method.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
        }
        if (StringHelper.containsNonWhitespace(accept)) {
            method.addRequestHeader("Accept", accept);
        }
        return method;
    }

    protected <T> T parse(final String body, final Class<T> cl) {
        try {
            final ObjectMapper mapper = new ObjectMapper(jsonFactory);
            final T obj = mapper.readValue(body, cl);
            return obj;
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected String stringuified(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper(jsonFactory);
            final StringWriter w = new StringWriter();
            mapper.writeValue(w, obj);
            return w.toString();
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected List<ErrorVO> parseErrorArray(final String body) {
        try {
            final ObjectMapper mapper = new ObjectMapper(jsonFactory);
            return mapper.readValue(body, new TypeReference<List<ErrorVO>>() {/* */
            });
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected List<LinkVO> parseLinkArray(final String body) {
        try {
            final ObjectMapper mapper = new ObjectMapper(jsonFactory);
            return mapper.readValue(body, new TypeReference<List<LinkVO>>() {/* */
            });
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
