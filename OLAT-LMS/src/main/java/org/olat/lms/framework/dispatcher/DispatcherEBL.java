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
package org.olat.lms.framework.dispatcher;

import java.io.File;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.lms.commons.mediaresource.FileMediaResource;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.commons.mediaresource.NotFoundMediaResource;
import org.olat.system.commons.Settings;
import org.olat.system.commons.WebappHelper;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 21.10.2011 <br>
 * 
 * @author Branislav Balaz
 */
@Component
public class DispatcherEBL {

    private static final Logger log = LoggerHelper.getLogger();
    public static final String NOVERSION = "_noversion_";
    public static final String STATIC_DIR_NAME = "/static";
    public static final String CLASSPATH_STATIC_DIR_NAME = "_static";

    public MediaResource getStaticMediaResource(String pathInfo, String mapperPath, String requestUri /* TODO ORID-1007: this parameter is used only for logging */) {

        String staticRelativePath = getStaticRelativePath(pathInfo, mapperPath);

        // remove any .. in the path
        String normalizedRelPath = normalizePath(staticRelativePath);
        if (normalizedRelPath == null) {
            if (log.isDebugEnabled()) {
                log.debug("Path is null after noralizing for static request pathInfo::" + requestUri, null);
            }
            return new NotFoundMediaResource("error");
        }
        File staticFile = getStaticFileFromAbsolutePath(normalizedRelPath);
        // only serve if file exists
        if (!staticFile.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("File does not exist for URI::" + requestUri, null);
            }
            staticFile = getFallbackStaticFile(pathInfo, mapperPath);
            if (!staticFile.exists()) {
                return new NotFoundMediaResource("error");
            }
            // log as error, file exists but wrongly mapped
            log.warn("File exists but not mapped using version - use StaticMediaDispatch methods to create URL of static files! invalid URI::" + requestUri, null);
        }

        if (log.isDebugEnabled()) {
            log.debug("Serving resource URI::" + requestUri, null);
        }
        // Everything is ok, serve resource
        return new FileMediaResource(staticFile);

    }

    private File getStaticFileFromAbsolutePath(String normalizedRelPath) {
        // create the file from the path
        String staticAbsolutePath = WebappHelper.getContextRoot() + STATIC_DIR_NAME + normalizedRelPath;
        File staticFile = new File(staticAbsolutePath);
        return staticFile;
    }

    private File getFallbackStaticFile(String pathInfo, String mapperPath) {
        String staticRelativePath;
        String normalizedRelPath;
        String staticAbsolutePath;
        File staticFile;
        // try fallback without version ID
        staticRelativePath = pathInfo.substring(mapperPath.length(), pathInfo.length());
        normalizedRelPath = normalizePath(staticRelativePath);
        staticAbsolutePath = WebappHelper.getContextRoot() + STATIC_DIR_NAME + normalizedRelPath;
        staticFile = new File(staticAbsolutePath);
        return staticFile;
    }

    private String getStaticRelativePath(String pathInfo, String mapperPath) {
        // remove uri prefix and version from request if available
        String staticRelPath = null;
        if (pathInfo.indexOf(NOVERSION) != -1) {
            // no version provided - only remove mapper
            staticRelPath = pathInfo.substring(mapperPath.length() + 1 + NOVERSION.length(), pathInfo.length());
        } else {
            // version provided - remove it
            String version = Settings.getBuildIdentifier();
            staticRelPath = pathInfo.substring(mapperPath.length() + 1 + version.length(), pathInfo.length());
        }
        return staticRelPath;
    }

    /**
     * Return a context-relative path, beginning with a "/", that represents the canonical version of the specified path
     * <p>
     * ".." and "." elements are resolved out. If the specified path attempts to go outside the boundaries of the current context (i.e. too many ".." path elements are
     * present), return <code>null</code> instead.
     * <p>
     * 
     * @author Mike Stock
     * @param path
     *            Path to be normalized
     * @return the normalized path
     */
    public static String normalizePath(String path) {
        if (path == null)
            return null;

        // Create a place for the normalized path
        String normalized = path;

        try { // we need to decode potential UTF-8 characters in the URL
            normalized = new String(normalized.getBytes(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertException("utf-8 encoding must be supported on all java platforms...");
        }

        if (normalized.equals("/."))
            return "/";

        // Normalize the slashes and add leading slash if necessary
        if (normalized.indexOf('\\') >= 0)
            normalized = normalized.replace('\\', '/');
        if (!normalized.startsWith("/"))
            normalized = "/" + normalized;

        // Resolve occurrences of "//" in the normalized path
        while (true) {
            int index = normalized.indexOf("//");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index) + normalized.substring(index + 1);
        }

        // Resolve occurrences of "/./" in the normalized path
        while (true) {
            int index = normalized.indexOf("/./");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index) + normalized.substring(index + 2);
        }

        // Resolve occurrences of "/../" in the normalized path
        while (true) {
            int index = normalized.indexOf("/../");
            if (index < 0)
                break;
            if (index == 0)
                return (null); // Trying to go outside our context
            int index2 = normalized.lastIndexOf('/', index - 1);
            normalized = normalized.substring(0, index2) + normalized.substring(index + 3);
        }

        // Return the normalized path that we have completed
        return (normalized);
    }

    /**
     * Helper method to copy all class path static files to the webapp/static/classpath/ directory for direct delivery via apache.
     * <p>
     * This method should only be called once at startup. To speed up things it checks on the last modified date of the files and copies only new files.
     */
    public void copyStaticClassPathFiles(String pathClasspathStatic) {
        StringBuffer path = new StringBuffer();
        path.append(WebappHelper.getContextRoot());
        path.append(DispatcherEBL.STATIC_DIR_NAME);
        path.append(pathClasspathStatic.substring(0, pathClasspathStatic.length()));
        File classPathStaticDir = new File(path.toString());

        // 1) copy files from compiled web app classpath
        String srcPath = WebappHelper.getContextRoot() + "/WEB-INF/classes";
        log.info("Copying static file from webapp source path::" + srcPath + " - be patient, this can take a while the first time when you hava jsmath files installed",
                null);
        ClassPathStaticDirectoriesVisitor srcVisitor = new ClassPathStaticDirectoriesVisitor(srcPath, classPathStaticDir);
        FileUtils.visitRecursively(new File(srcPath), srcVisitor);
        // 3) Search in libs directory
        String libDirPath = WebappHelper.getContextRoot() + "/WEB-INF/lib";
        log.info("Copying static file from jar files from dir::" + libDirPath, null);
        ClassPathStaticDirectoriesVisitor libVisitor = new ClassPathStaticDirectoriesVisitor(libDirPath, classPathStaticDir);
        FileUtils.visitRecursively(new File(libDirPath), libVisitor);
    }

}
