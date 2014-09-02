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

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.olat.connectors.httpclient.HttpClientFactory;
import org.olat.data.basesecurity.Identity;
import org.olat.data.user.User;
import org.olat.data.user.UserConstants;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.commons.mediaresource.HttpRequestMediaResource;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.commons.mediaresource.NotFoundMediaResource;
import org.olat.lms.user.UserService;
import org.olat.presentation.course.nodes.tu.TUConfigForm;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.clone.CloneableController;
import org.olat.presentation.framework.core.control.generic.iframe.IFrameDisplayController;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.dispatcher.mapper.Mapper;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: 09.01.2006
 * 
 * @author Felix Jost<br>
 *         Description: this controller serves an external content/web page in a iframe. the content is all redirected through olat in order to be able to supply the
 *         olat-specific parameters and/or password for basic authentication to the request </pre>
 */
public class IframeTunnelController extends BasicController implements CloneableController {

    private VelocityContainer myContent;

    HttpClient httpClientInstance = null; // package local for performance only
    private ModuleConfiguration config;

    /**
     * Constructor for a tunnel component wrapper controller
     * 
     * @param ureq
     *            the userrequest
     * @param wControl
     *            the windowcontrol
     * @param config
     *            the module configuration
     */
    public IframeTunnelController(final UserRequest ureq, final WindowControl wControl, final ModuleConfiguration config) {
        super(ureq, wControl);
        // use iframe translator for generic iframe title text
        setTranslator(PackageUtil.createPackageTranslator(IFrameDisplayController.class, ureq.getLocale()));
        this.config = config;

        // configuration....
        final int configVersion = config.getConfigurationVersion();
        // since config version 1
        final String proto = (String) config.get(TUConfigForm.CONFIGKEY_PROTO);
        final String host = (String) config.get(TUConfigForm.CONFIGKEY_HOST);
        final Integer port = (Integer) config.get(TUConfigForm.CONFIGKEY_PORT);
        final String user = (String) config.get(TUConfigForm.CONFIGKEY_USER);
        final String startUri = (String) config.get(TUConfigForm.CONFIGKEY_URI);
        final String pass = (String) config.get(TUConfigForm.CONFIGKEY_PASS);
        String firstQueryString = null;
        if (configVersion == 2) {
            // query string is available since config version 2
            firstQueryString = (String) config.get(TUConfigForm.CONFIGKEY_QUERY);
        }

        final boolean usetunnel = config.getBooleanSafe(TUConfigForm.CONFIG_TUNNEL);
        myContent = createVelocityContainer("iframe_index");
        if (!usetunnel) { // display content directly
            final String rawurl = TUConfigForm.getFullURL(proto, host, port, startUri, firstQueryString).toString();
            myContent.contextPut("url", rawurl);
        } else { // tunnel
            final Identity ident = ureq.getIdentity();

            if (user != null && user.length() > 0) {
                httpClientInstance = HttpClientFactory.getHttpClientInstance(host, port.intValue(), proto, user, pass);
            } else {
                httpClientInstance = HttpClientFactory.getHttpClientInstance(host, port.intValue(), proto, null, null);
            }

            final Locale loc = ureq.getLocale();
            final Mapper mapper = new Mapper() {
                @Override
                public MediaResource handle(final String relPath, final HttpServletRequest hreq) {
                    MediaResource mr = null;
                    final String method = hreq.getMethod();
                    String uri = relPath;
                    HttpMethod meth = null;

                    if (uri == null) {
                        uri = (startUri == null) ? "" : startUri;
                    }
                    if (uri.length() > 0 && uri.charAt(0) != '/') {
                        uri = "/" + uri;
                    }

                    // String contentType = hreq.getContentType();

                    // if (allowedToSendPersonalHeaders) {
                    final String userName = ident.getName();
                    final User u = ident.getUser();
                    final String lastName = getUserService().getUserProperty(u, UserConstants.LASTNAME, loc);
                    final String firstName = getUserService().getUserProperty(u, UserConstants.FIRSTNAME, loc);
                    final String email = getUserService().getUserProperty(u, UserConstants.EMAIL, loc);

                    if (method.equals("GET")) {
                        final GetMethod cmeth = new GetMethod(uri);
                        final String queryString = hreq.getQueryString();
                        if (queryString != null) {
                            cmeth.setQueryString(queryString);
                        }
                        meth = cmeth;
                        // if response is a redirect, follow it
                        if (meth == null) {
                            return null;
                        }
                        meth.setFollowRedirects(true);

                    } else if (method.equals("POST")) {
                        // if (contentType == null || contentType.equals("application/x-www-form-urlencoded")) {
                        // regular post, no file upload
                        // }
                        final Map params = hreq.getParameterMap();
                        final PostMethod pmeth = new PostMethod(uri);
                        final Set postKeys = params.keySet();
                        for (final Iterator iter = postKeys.iterator(); iter.hasNext();) {
                            final String key = (String) iter.next();
                            final String vals[] = (String[]) params.get(key);
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
                    meth.addRequestHeader("X-OLAT-USERNAME", userName);
                    meth.addRequestHeader("X-OLAT-LASTNAME", lastName);
                    meth.addRequestHeader("X-OLAT-FIRSTNAME", firstName);
                    meth.addRequestHeader("X-OLAT-EMAIL", email);

                    boolean ok = false;
                    try {
                        httpClientInstance.executeMethod(meth);
                        ok = true;
                    } catch (final Exception e) {
                        // handle error later
                    }

                    if (!ok) {
                        // error
                        meth.releaseConnection();
                        return new NotFoundMediaResource(relPath);
                    }

                    // get or post successfully
                    final Header responseHeader = meth.getResponseHeader("Content-Type");
                    if (responseHeader == null) {
                        // error
                        return new NotFoundMediaResource(relPath);
                    }
                    mr = new HttpRequestMediaResource(meth);
                    return mr;
                }
            };

            final String amapPath = registerMapper(mapper);
            String alluri = amapPath + startUri;
            if (firstQueryString != null) {
                alluri += "?" + firstQueryString;
            }
            myContent.contextPut("url", alluri);
        }

        final String frameId = "ifdc" + hashCode(); // for e.g. js use
        myContent.contextPut("frameId", frameId);

        putInitialPanel(myContent);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // nothing to do
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // mapper autodisposed by basic controller
    }

    /**
	 */
    @Override
    public Controller cloneController(final UserRequest ureq, final WindowControl control) {
        return new IframeTunnelController(ureq, control, config);
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
