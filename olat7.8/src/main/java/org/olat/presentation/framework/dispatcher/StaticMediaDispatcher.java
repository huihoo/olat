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
package org.olat.presentation.framework.dispatcher;

/* TODO: ORID-1007 'File' */
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.commons.mediaresource.NotFoundMediaResource;
import org.olat.lms.commons.mediaresource.ServletUtil;
import org.olat.lms.framework.dispatcher.DispatcherEBL;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.system.commons.Settings;
import org.olat.system.commons.WebappHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * <h3>Description:</h3> A dispatcher that delivers raw static files without any servlet intervention or security checks directly from the webapp/static directory.
 * <p>
 * The URL contains the web app version ID to make sure browsers always fetch the newest version after a new release to prevent browser caching issues.
 * <p>
 * This should only be used to deliver basic files from the body.html and some other static resource. When developing modules, put all your static files like js libraries
 * or other resource into the _static resources folder and include them using the JSAndCSSComponent.java or get the URL to those resources from the
 * ClassPathStaticDispatcher.java
 * <p>
 * Initial Date: 16.05.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class StaticMediaDispatcher implements Dispatcher {

    private static final Logger log = LoggerHelper.getLogger();

    private static String mapperPath;

    /**
     * Constructor
     * 
     * @param mapperPathFromConfig
     */
    protected StaticMediaDispatcher(String mapperPathFromConfig) {
        mapperPath = mapperPathFromConfig;
    }

    /**
	 */
    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response, @SuppressWarnings("unused") String uriPrefix) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            if (log.isDebugEnabled()) {
                log.debug("PathInfo is null for static request URI::" + request.getRequestURI(), null);
            }
            ServletUtil.serveResource(request, response, new NotFoundMediaResource("error"));
            return;
        }

        MediaResource resource = getDispatcherEBL().getStaticMediaResource(pathInfo, mapperPath, request.getRequestURI());
        ServletUtil.serveResource(request, response, resource);
    }

    private DispatcherEBL getDispatcherEBL() {
        return CoreSpringFactory.getBean(DispatcherEBL.class);
    }

    /**
     * Note: use only rarely - all non-generic js libs and css classes should be included using JsAndCssComponent, and all images should be referenced with the css
     * background-image capability. <br>
     * renders a uri which is mounted to the webapp/static/ directory of your web application.
     * <p>
     * This method will add a version ID to the path that guarantees that the browser fetches the file again when you release a new version of your application.
     * 
     * @param target
     * @param URI
     *            e.g. img/specialimagenotpossiblewithcss.jpg
     */
    public static void renderStaticURI(StringOutput target, String URI) {
        renderStaticURI(target, URI, true);
    }

    /**
     * Render a static URL to resource. This is only used in special cases, in most scenarios you should use the JSAndCssComponent
     * 
     * @param target
     *            The output target
     * @param URI
     *            e.g. img/specialimagenotpossiblewithcss.jpg
     * @param addVersionID
     *            true: the build version is added to the URL to force browser reload the resource when releasing a new version; false: don't add version (but allow
     *            browsers to cache even when resource has changed). Only use false when really needed
     */
    private static void renderStaticURI(StringOutput target, String URI, boolean addVersionID) {
        String root = WebappHelper.getServletContextPath();
        target.append(root); // e.g /olat
        target.append(mapperPath); // e.g. /raw/
        // Add version to make URL change after new release and force browser to
        // load new static files
        if (addVersionID) {
            target.append(Settings.getBuildIdentifier());
        } else {
            target.append(DispatcherEBL.NOVERSION);
        }
        target.append("/");
        if (URI != null)
            target.append(URI);
    }

    /**
     * Create a static URI for this relative URI. Helper method in case no String output is available.
     * <p>
     * This method will add a version ID to the path that guarantees that the browser fetches the file again when you release a new version of your application.
     * 
     * @param URI
     *            e.g. img/specialimagenotpossiblewithcss.jpg
     * @return
     */
    public static String createStaticURIFor(String URI) {
        return createStaticURIFor(URI, true);
    }

    /**
     * Create a static URI for this relative URI. Helper method in case no String output is available.
     * 
     * @param URI
     *            e.g. img/specialimagenotpossiblewithcss.jpg
     * @param addVersionID
     *            true: the build version is added to the URL to force browser reload the resource when releasing a new version; false: don't add version (but allow
     *            browsers to cache even when resource has changed). Only use false when really needed
     * @return
     */
    public static String createStaticURIFor(String URI, boolean addVersionID) {
        StringOutput so = new StringOutput();
        renderStaticURI(so, URI, addVersionID);
        return so.toString();
    }

    public static String getStaticMapperPath() {
        return mapperPath;
    }

}
