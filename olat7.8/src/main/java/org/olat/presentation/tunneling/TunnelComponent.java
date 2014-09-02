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

package org.olat.presentation.tunneling;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.olat.connectors.httpclient.HttpClientFactory;
import org.olat.data.basesecurity.Identity;
import org.olat.data.user.User;
import org.olat.data.user.UserConstants;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.commons.SimpleHtmlParser;
import org.olat.lms.commons.mediaresource.AsyncMediaResponsible;
import org.olat.lms.commons.mediaresource.HttpRequestMediaResource;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.user.UserService;
import org.olat.presentation.course.nodes.tu.TUConfigForm;
import org.olat.presentation.course.nodes.tu.TURequest;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.ComponentRenderer;
import org.olat.presentation.framework.core.render.ValidationResult;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * @author Mike Stock Comment:
 */
public class TunnelComponent extends Component implements AsyncMediaResponsible {

    private static final Logger log = LoggerHelper.getLogger();

    private static final ComponentRenderer RENDERER = new TunnelRenderer();
    private static final String USERAGENT_NAME = "OLAT tunneling module 1.1";

    private final String proto;
    private final String host;
    private final Integer port;
    private final String user;
    private final String pass;
    private String query = null;
    private HttpClient httpClientInstance = null;
    private final ModuleConfiguration config;
    private String htmlHead = null;
    private String jsOnLoad = null;
    private String htmlContent = null;
    private boolean firstCall = true;
    private final Locale loc;

    private final String startUri;

    /**
     * @param name
     * @param config
     * @param ureq
     */
    public TunnelComponent(final String name, final ModuleConfiguration config, final UserRequest ureq) {
        super(name);
        this.config = config;
        final int configVersion = config.getConfigurationVersion();

        // since config version 1
        proto = (String) config.get(TUConfigForm.CONFIGKEY_PROTO);
        host = (String) config.get(TUConfigForm.CONFIGKEY_HOST);
        port = (Integer) config.get(TUConfigForm.CONFIGKEY_PORT);
        user = (String) config.get(TUConfigForm.CONFIGKEY_USER);
        pass = (String) config.get(TUConfigForm.CONFIGKEY_PASS);
        httpClientInstance = HttpClientFactory.getHttpClientInstance(host, port.intValue(), proto, user, pass);
        httpClientInstance.getParams().setParameter("http.useragent", USERAGENT_NAME);
        startUri = (String) config.get(TUConfigForm.CONFIGKEY_URI);
        if (configVersion == 2) {
            // query string is available since config version 2
            query = (String) config.get(TUConfigForm.CONFIGKEY_QUERY);
        }
        loc = ureq.getLocale();

        fetchFirstResource(ureq.getIdentity());
    }

    /**
     * @return String
     */
    public String getType() {
        return "hw";
    }

    private void fetchFirstResource(final Identity ident) {

        final TURequest tureq = new TURequest(); // config, ureq);
        tureq.setContentType(null); // not used
        tureq.setMethod("GET");
        tureq.setParameterMap(Collections.EMPTY_MAP);
        tureq.setQueryString(query);
        if (startUri != null) {
            if (startUri.startsWith("/")) {
                tureq.setUri(startUri);
            } else {
                tureq.setUri("/" + startUri);
            }
        }

        // if (allowedToSendPersonalHeaders) {
        final String userName = ident.getName();
        final User u = ident.getUser();
        final String lastName = getUserService().getUserProperty(u, UserConstants.LASTNAME, loc);
        final String firstName = getUserService().getUserProperty(u, UserConstants.FIRSTNAME, loc);
        final String email = getUserService().getUserProperty(u, UserConstants.EMAIL, loc);

        tureq.setEmail(email);
        tureq.setFirstName(firstName);
        tureq.setLastName(lastName);
        tureq.setUserName(userName);
        // }

        final HttpMethod meth = fetch(tureq, httpClientInstance);
        if (meth == null) {
            setFetchError();
        } else {

            final Header responseHeader = meth.getResponseHeader("Content-Type");
            String mimeType;
            if (responseHeader == null) {
                setFetchError();
                mimeType = null;
            } else {
                mimeType = responseHeader.getValue();
            }

            if (mimeType != null && mimeType.startsWith("text/html")) {
                // we have html content, let doDispatch handle it for
                // inline rendering, update hreq for next content request
                String body;
                try {
                    body = meth.getResponseBodyAsString();
                } catch (final IOException e) {
                    log.warn("Problems when tunneling URL::" + tureq.getUri(), e);
                    htmlContent = "Error: cannot display inline :" + tureq.getUri() + ": Unknown transfer problem '";
                    return;
                }
                final SimpleHtmlParser parser = new SimpleHtmlParser(body);
                if (!parser.isValidHtml()) { // this is not valid HTML, deliver
                    // asynchronuous
                }
                meth.releaseConnection();
                htmlHead = parser.getHtmlHead();
                jsOnLoad = parser.getJsOnLoad();
                htmlContent = parser.getHtmlContent();
            } else {
                htmlContent = "Error: cannot display inline :" + tureq.getUri() + ": mime type was '" + mimeType + "' but expected 'text/html'. Response header was '"
                        + responseHeader + "'.";
            }
        }
    }

    /**
	 */
    @Override
    public MediaResource getAsyncMediaResource(final UserRequest ureq) {

        final String moduleURI = ureq.getModuleURI();
        // FIXME:fj: can we distinguish between a ../ call an a click to another component?
        // now works for start uri's like /demo/tunneldemo.php but when in tunneldemo.php
        // a link is used like ../ this link does not work (moduleURI is null). if i use
        // ../index.php instead everything works as expected
        if (moduleURI == null) { // after a click on some other component e.g.
            if (!firstCall) {
                return null;
            }
            firstCall = false; // reset first call
        }

        final TURequest tureq = new TURequest(config, ureq);

        // if (allowedToSendPersonalHeaders) {
        final String userName = ureq.getIdentity().getName();
        final User u = ureq.getIdentity().getUser();
        final String lastName = getUserService().getUserProperty(u, UserConstants.LASTNAME, loc);
        final String firstName = getUserService().getUserProperty(u, UserConstants.FIRSTNAME, loc);
        final String email = getUserService().getUserProperty(u, UserConstants.EMAIL, loc);
        tureq.setEmail(email);
        tureq.setFirstName(firstName);
        tureq.setLastName(lastName);
        tureq.setUserName(userName);
        // }

        final HttpMethod meth = fetch(tureq, httpClientInstance);
        if (meth == null) {
            setFetchError();
            return null;
        }

        final Header responseHeader = meth.getResponseHeader("Content-Type");
        if (responseHeader == null) {
            setFetchError();
            return null;
        }

        final String mimeType = responseHeader.getValue();
        if (mimeType != null && mimeType.startsWith("text/html")) {
            // we have html content, let doDispatch handle it for
            // inline rendering, update hreq for next content request
            String body;
            try {
                body = meth.getResponseBodyAsString();
            } catch (final IOException e) {
                log.warn("Problems when tunneling URL::" + tureq.getUri(), e);
                return null;
            }
            final SimpleHtmlParser parser = new SimpleHtmlParser(body);
            if (!parser.isValidHtml()) { // this is not valid HTML, deliver
                // asynchronuous
                return new HttpRequestMediaResource(meth);
            }
            meth.releaseConnection();
            htmlHead = parser.getHtmlHead();
            jsOnLoad = parser.getJsOnLoad();
            htmlContent = parser.getHtmlContent();
            setDirty(true);
        } else {
            return new HttpRequestMediaResource(meth); // this is a async browser
        }
        // refetch
        return null;
    }

    private void setFetchError() {
        // some fetch error - reset to generic error message
        htmlHead = null;
        jsOnLoad = null;
        htmlContent = "Server error: No connection to tunneling server. Please check your configuration!";
    }

    /**
     * @param tuReq
     * @param client
     * @return HttpMethod
     */
    public HttpMethod fetch(final TURequest tuReq, final HttpClient client) {

        final String modulePath = tuReq.getUri();

        HttpMethod meth = null;
        final String method = tuReq.getMethod();
        if (method.equals("GET")) {
            final GetMethod cmeth = new GetMethod(modulePath);
            final String queryString = tuReq.getQueryString();
            if (queryString != null) {
                cmeth.setQueryString(queryString);
            }
            meth = cmeth;
            if (meth == null) {
                return null;
            }
            // if response is a redirect, follow it
            meth.setFollowRedirects(true);

        } else if (method.equals("POST")) {
            final String type = tuReq.getContentType();
            if (type == null || type.equals("application/x-www-form-urlencoded")) {
                // regular post, no file upload
            }

            final PostMethod pmeth = new PostMethod(modulePath);
            final Set postKeys = tuReq.getParameterMap().keySet();
            for (final Iterator iter = postKeys.iterator(); iter.hasNext();) {
                final String key = (String) iter.next();
                final String vals[] = (String[]) tuReq.getParameterMap().get(key);
                for (int i = 0; i < vals.length; i++) {
                    pmeth.addParameter(key, vals[i]);
                }
                meth = pmeth;
            }
            if (meth == null) {
                return null;
                // Redirects are not supported when using POST method!
                // See RFC 2616, section 10.3.3, page 62
            }
        }

        // Add olat specific headers to the request, can be used by external
        // applications to identify user and to get other params
        // test page e.g. http://cgi.algonet.se/htbin/cgiwrap/ug/test.py
        meth.addRequestHeader("X-OLAT-USERNAME", tuReq.getUserName());
        meth.addRequestHeader("X-OLAT-LASTNAME", tuReq.getLastName());
        meth.addRequestHeader("X-OLAT-FIRSTNAME", tuReq.getFirstName());
        meth.addRequestHeader("X-OLAT-EMAIL", tuReq.getEmail());

        try {
            client.executeMethod(meth);
            return meth;
        } catch (final Exception e) {
            meth.releaseConnection();
        }
        return null;
    }

    /**
	 */
    @Override
    protected void doDispatchRequest(final UserRequest ureq) {
        // never called
    }

    /**
	 */
    @Override
    public String getExtendedDebugInfo() {
        return proto + ", " + host + ", " + port;
    }

    /**
     * @return Returns the htmlContent.
     */
    public String getHtmlContent() {
        return htmlContent;
    }

    /**
     * @return Returns the htmlHead.
     */
    public String getHtmlHead() {
        return htmlHead;
    }

    /**
     * @return Returns the jsOnLoad.
     */
    public String getJsOnLoad() {
        return jsOnLoad;
    }

    /**
	 */
    @Override
    public void validate(final UserRequest ureq, final ValidationResult vr) {
        super.validate(ureq, vr);
        // browser uri: e.g null or
        final String browserURI = ureq.getModuleURI();
        boolean redirect = true;
        if (browserURI == null) { // click on a treenode -> return without redirect
            // only if the currentURI is null (blank content)
            // or it is a root file
            if (startUri == null || startUri.indexOf("/") == -1) {
                redirect = false;
            }
        } else if (!ureq.isValidDispatchURI()) { // link from external
            // direct-jump-url or such ->
            // redirect
            redirect = true;
        } else {
            // browser uri != null and normal framework url dispatch = click from
            // within a page; currentURI == browserURI since asyncmedia-call took
            // place before validating.
            // never needs to redirect since browser page calculates relative page and
            // is handled by asyncmediaresponsible
            // FIXME:fj:a let userrequest tell me when is
            if (browserURI.startsWith("olatcmd")) {
                redirect = true;
            } else {
                redirect = false;
            }
        }
        if (redirect) {
            String newUri = startUri;
            if (newUri.charAt(0) == '/') {
                newUri = newUri.substring(1);
            }
            vr.setNewModuleURI(newUri);
        }
        return;

    }

    @Override
    public ComponentRenderer getHTMLRendererSingleton() {
        return RENDERER;
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
