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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.presentation.framework.dispatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.olat.lms.commons.mediaresource.ClasspathMediaResource;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.commons.mediaresource.NotFoundMediaResource;
import org.olat.lms.commons.mediaresource.ServletUtil;
import org.olat.lms.framework.dispatcher.DispatcherEBL;
import org.olat.system.commons.Settings;
import org.olat.system.commons.WebappHelper;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * Allows to register static mappers. Here you can create urls which are valid for all users and can be cached by browsers. The delivered resources must be static files
 * in the _static directory of your code.
 * <p>
 * In mod-jk mode this static files can be delivered directly from apache or even from another file server
 * <p>
 * If you need global mappers that provide dynamic content, then you mus use the GlobalMapperRegistry.java
 * <p>
 * If you need urls that are only accessible for one user, use the MapperRegistry.java
 * <P>
 * Initial Date: 6.10.2008 <br>
 * 
 * @author Florian Gnaegi
 */
public class ClassPathStaticDispatcher implements Dispatcher {

    private static final Logger log = LoggerHelper.getLogger();

    private static String pathClasspathStatic;
    private static boolean copyStaticFiles;
    private static ClassPathStaticDispatcher INSTANCE;
    private DispatcherEBL dispatcherEBL;

    /**
     * Constructor, only used by spring. Use getInstance instead!
     * 
     * @param copyStaticFilesConfig
     *            true: copy static files and deliver via StaticMediaDispatcher; false: don't copy files and deliver from classpath
     * @param dispatcherPath
     *            : path where to dispatch files ( e.g. '/classpath/')
     */
    protected ClassPathStaticDispatcher(boolean copyStaticFilesConfig, String dispatcherPath) {
        pathClasspathStatic = dispatcherPath;
        INSTANCE = this;
        copyStaticFiles = copyStaticFilesConfig;
        if (copyStaticFiles) {
            getDispatcherEBL().copyStaticClassPathFiles(pathClasspathStatic);
        }
    }

    private DispatcherEBL getDispatcherEBL() {
        // return CoreSpringFactory.getBean(DispatcherEBL.class);
        // workaround for getting DispatcherEBL via CoreSpringFactory, the servletContext is not initialized yet
        if (dispatcherEBL == null) {
            dispatcherEBL = new DispatcherEBL();
        }
        return dispatcherEBL;
    }

    /**
     * @return MapperRegistry
     */
    public static ClassPathStaticDispatcher getInstance() {
        return INSTANCE;
    }

    /**
     * Create a path for the _static directory for the given class. Resources that are in this directory can be addressed relatively to the generated mapper path. The
     * resources can also be stored in a jar file.
     * 
     * @param globalNameClass
     *            class for the name of the mapper. the name of the mapper is the name of the package name for this class
     * @return the path under which this mapper will be called, without / at the end, e.g. /olat/classpath/612/org.olat.presentation.demo.tabledemo (the 612 here is the
     *         versionId to guarantee the uniqueness across releases to trick out buggy css browser caches)
     */
    public String getMapperBasePath(Class clazz) {
        return getMapperBasePath(clazz, true);
    }

    /**
     * Create a path for the _static directory for the given class. Resources that are in this directory can be addressed relatively to the generated mapper path. The
     * resources can also be stored in a jar file.
     * 
     * @param clazz
     *            The package name of this class is used for the path
     * @param addVersionID
     *            true: the build version is added to the URL to force browser reload the resource when releasing a new version; false: don't add version (but allow
     *            browsers to cache even when resource has changed). Only use false when really needed
     * @return the path under which this mapper will be called, without / at the end, e.g. /olat/classpath/612/org.olat.presentation.demo.tabledemo (the 612 here is the
     *         versionId to guarantee the uniqueness across releases to trick out buggy css browser caches)
     */
    private String getMapperBasePath(Class clazz, boolean addVersionID) {
        StringBuffer sb = new StringBuffer();
        sb.append(WebappHelper.getServletContextPath());
        // When mod jk is enabled, the files are deliverd by apache. During
        // startup in the classpath that live in a _static directory are copied to
        // the webapp/static/classpath/ directory.
        // Thus, when mod_jk is enabled, the path is different then when delivered
        // by tomcat.
        // Examples:
        // mod_jk disabled: /olat/classpath/61x/org.olat.presentation/myfile.css
        // -> dispatched by DispatcherAction, delivered by ClassPathStaticDispatcher
        // mod_jk enabled: /olat/raw/61x/classpath/org.olat.presentation/myfile.css
        // -> /olat/raw is unmounted in apache config, files delivered by apache
        if (copyStaticFiles) {
            sb.append(StaticMediaDispatcher.getStaticMapperPath());
            if (addVersionID) {
                // version before classpath, compatible with StaticMediaDiapatcher
                sb.append(Settings.getBuildIdentifier());
            } else {
                sb.append(DispatcherEBL.NOVERSION);
            }
            sb.append(pathClasspathStatic.substring(0, pathClasspathStatic.length()));
        } else {
            sb.append(pathClasspathStatic);
            if (addVersionID) {
                // version after classpath, compatible with ClassPathStaticDispatcher
                sb.append(Settings.getBuildIdentifier());
            } else {
                sb.append(DispatcherEBL.NOVERSION);
            }
            sb.append("/");
        }
        String className = clazz.getName();
        int ls = className.lastIndexOf('.');
        // post: ls != -1, since we don't use the java default package
        String pkg = className.substring(0, ls);
        // using baseClass.getPackage() would add unneeded inefficient and synchronized code
        if (pkg != null)
            sb.append(pkg);
        return sb.toString();
    }

    /**
     * @param hreq
     * @param hres
     */
    @Override
    public void execute(HttpServletRequest hreq, HttpServletResponse hres, String pathInfo) {
        String path = hreq.getPathInfo();
        // e.g. /olat/classpath/612/org.olat.presentation.demo.tabledemo/blu.html
        // or /olat/classpath/_noversion_/org.olat.presentation.demo.tabledemo/blu.html
        String prefix;
        if (path.indexOf(pathClasspathStatic + Settings.getBuildIdentifier()) != -1) {
            prefix = pathClasspathStatic + Settings.getBuildIdentifier() + "/";
        } else if (path.indexOf(pathClasspathStatic + DispatcherEBL.NOVERSION) != -1) {
            prefix = pathClasspathStatic + DispatcherEBL.NOVERSION + "/";
        } else {
            log.warn("Invalid static path::" + path + " - sent 404", null);
            DispatcherAction.sendNotFound(hreq.getRequestURI(), hres);
            return;
        }
        String subInfo = path.substring(prefix.length());
        int slashPos = subInfo.indexOf('/');
        if (slashPos == -1) {
            log.warn("Invalid static path::" + path + " - sent 404", null);
            DispatcherAction.sendNotFound(hreq.getRequestURI(), hres);
            return;
        }
        // packageName e.g. org.olat.presentation
        String packageName = subInfo.substring(0, slashPos);
        String relPath = subInfo.substring(slashPos);
        // brasato:: can this happen at all, or does tomcat filter out - till now never reached - needs some little cpu cycles
        if (relPath.indexOf("..") != -1) {
            log.warn("ClassPathStatic resource path contained '..': relpath::" + relPath + " - sent 403", null);
            DispatcherAction.sendForbidden(hreq.getRequestURI(), hres);
        }
        // /bla/blu.html
        Package pakkage;
        MediaResource mr;
        pakkage = Package.getPackage(packageName);

        if (pakkage == null) {
            mr = new NotFoundMediaResource(path);
            log.warn("404 Not Found! Cound not serve classpath static resource with path: " + path);
        } else {
            mr = createClassPathStaticFileMediaResourceFor(pakkage, relPath);
        }
        ServletUtil.serveResource(hreq, hres, mr);
    }

    /**
     * Create a static class path media resource form a given base class
     * 
     * @param baseClass
     * @param relPath
     * @return
     */
    public MediaResource createClassPathStaticFileMediaResourceFor(Class baseClass, String relPath) {
        return new ClasspathMediaResource(baseClass, DispatcherEBL.CLASSPATH_STATIC_DIR_NAME + relPath);
    }

    /**
     * Create a static class path media resource form a given package
     * 
     * @param baseClass
     * @param relPath
     * @return
     */
    private MediaResource createClassPathStaticFileMediaResourceFor(Package pakkage, String relPath) {
        return new ClasspathMediaResource(pakkage, DispatcherEBL.CLASSPATH_STATIC_DIR_NAME + relPath);
    }

}
