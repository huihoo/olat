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

package org.olat.presentation.framework.core.render.velocity;

import java.io.StringWriter;
import java.util.HashSet;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.olat.system.commons.configuration.Initializable;
import org.olat.system.exception.AssertException;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Initial Date: 01.12.2003
 * 
 * @author Mike Stock
 */
public class VelocityHelper implements Initializable {

    private static final Logger log = LoggerHelper.getLogger();

    private VelocityEngine ve;
    private HashSet<String> resourcesNotFound = new HashSet<String>(128);

    private static final String DEFAULT_ENCODING = "UTF-8";
    private static String inputEncoding = DEFAULT_ENCODING;
    private static String outputEncoding = DEFAULT_ENCODING;
    private boolean isDebugging;
    private String sourcePath;

    /**
     * [spring]
     */
    private VelocityHelper() {
        //
    }

    /**
     * 
     * @param isDebugging
     */
    public void setDebugging(boolean isDebugging) {
        this.isDebugging = isDebugging;
    }

    public void setSourcePath(String path) {
        this.sourcePath = path;
    }

    public void init() {
        Properties p = null;
        try {
            ve = new VelocityEngine();
            p = new Properties();
            p.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
            // p.setProperty(RuntimeConstants.RUNTIME_LOG, OLATContext.getUserdataRoot() + "logs/velocity.log.txt");
            p.setProperty("runtime.log.logsystem.log4j.category", "syslog");

            p.setProperty(RuntimeConstants.RUNTIME_LOG_ERROR_STACKTRACE, "true");
            p.setProperty(RuntimeConstants.RUNTIME_LOG_INFO_STACKTRACE, "true");

            p.setProperty(RuntimeConstants.INPUT_ENCODING, getInputEncoding());
            p.setProperty(RuntimeConstants.OUTPUT_ENCODING, getOutputEncoding());

            if (isDebugging) {
                p.setProperty(RuntimeConstants.RESOURCE_LOADER, "file, classpath");
                // config for file lookup from webapp classpath
                p.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
                // uncomment to compress html template files (not gzip, but rather leave away unneeded tabs, spaces, etc. v0.5)
                // p.setProperty("file.resource.loader.class", "org.olat.presentation.framework.render.velocity.CompressingFileResourceLoader");
                p.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, sourcePath);
                p.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_CACHE, "false");
                p.setProperty("file.resource.loader.modificationCheckInterval", "3");
            } else {
                p.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
            }

            // for jars: use the classpathloader
            p.setProperty("classpath.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            // caching is on normally
            p.setProperty("classpath.resource.loader.cache", Boolean.toString(isDebugging));
            // cache default size is set to have no limits, see
            // http://velocity.apache.org/engine/releases/velocity-1.5/apidocs/org/apache/velocity/runtime/resource/ResourceCacheImpl.html#ResourceCacheImpl()
            // p.setProperty("resource.manager.cache.size", -1 + "");

            p.setProperty(RuntimeConstants.RESOURCE_MANAGER_LOGWHENFOUND, "false");
            p.setProperty(RuntimeConstants.VM_LIBRARY, "velocity/olat_velocimacros.vm");

            p.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_CACHE, Boolean.toString(isDebugging));
            p.setProperty(RuntimeConstants.VM_LIBRARY_AUTORELOAD, Boolean.toString(isDebugging));
            ve.init(p);
        } catch (Exception e) {
            throw new RuntimeException("config error " + p.toString(), e);
        }
    }

    /**
     * @param template
     *            e.g. org/olat/demo/_content/index.html
     * @param c
     *            the context
     * @param theme
     *            the theme e.g. "accessibility" or "printing". may be null if the default theme ("") should be taken
     * @return String the rendered template
     */
    private String merge(String template, Context c, String theme) {
        StringWriter wOut = new StringWriter(10000);

        try {
            Template vtemplate = null;
            if (log.isDebugEnabled())
                log.debug("Merging template::" + template + " for theme::" + theme, null);

            if (theme != null) {
                // try the theme first, if resource not found exception, fallback to normal resource.
                // e.g. try /_accessibility/index.html first, if not found, try /index.html.
                // this allows for themes to only provide the delta to the default templates

                // todo we could avoid those string operations, if the performance gain is measureable
                int latestSlash = template.lastIndexOf('/');
                StringBuilder sb = new StringBuilder(template.substring(0, latestSlash));
                sb.append("/_").append(theme).append("/").append(template.substring(latestSlash + 1));
                String themedTemplatePath = sb.toString();

                // check cache
                boolean notFound;
                synchronized (resourcesNotFound) { // o_clusterOK by:fj
                    notFound = resourcesNotFound.contains(themedTemplatePath);
                }

                if (!notFound) {
                    // never tried before -> try to load it
                    if (!ve.templateExists(themedTemplatePath)) {
                        // remember not found (since velocity doesn't) then try fallback.
                        // this will happen once for each theme when a resource does not exist in its themed variant but only in the default theme.
                        if (!isDebugging) {
                            synchronized (resourcesNotFound) { // o_clusterOK by:fj
                                resourcesNotFound.add(themedTemplatePath);
                            }
                        } // for debugging, allow introduction of themed files without restarting the application
                    } else {
                        // template exists -> load it
                        vtemplate = ve.getTemplate(themedTemplatePath, getInputEncoding());
                    }
                }
                // if not found, fallback to standard
                if (vtemplate == null) {
                    vtemplate = ve.getTemplate(template, getInputEncoding());
                }
            } else {
                // no theme, load the standard template
                vtemplate = ve.getTemplate(template, getInputEncoding());
            }

            vtemplate.merge(c, wOut);
        } catch (MethodInvocationException me) {
            throw new OLATRuntimeException(VelocityHelper.class, "MethodInvocationException occured while merging template: methName:" + me.getMethodName()
                    + ", refName:" + me.getReferenceName(), me.getWrappedThrowable());
        } catch (Exception e) {
            throw new OLATRuntimeException(VelocityHelper.class, "exception occured while merging template: " + e.getMessage(), e);
        }
        return wOut.toString();
    }

    /**
     * @param path
     * @param c
     * @return String
     */
    public String mergeContent(String path, Context c, String theme) {
        if (path == null)
            throw new AssertException("velocity path was null");
        return merge(path, c, theme);
    }

    /**
     * @param vtlInput
     * @param c
     * @return String
     */
    public String evaluateVTL(String vtlInput, Context c) {
        StringWriter wOut = new StringWriter(10000);

        try {
            ve.evaluate(c, wOut, "internalEvaluator", vtlInput);
        } catch (MethodInvocationException me) {
            throw new OLATRuntimeException(VelocityHelper.class, "MethodInvocationException occured while merging template: methName:" + me.getMethodName()
                    + ", refName:" + me.getReferenceName(), me);
        } catch (Exception e) {
            throw new OLATRuntimeException(VelocityHelper.class, "exception occured while merging template: " + e.getMessage(), e);
        }
        return wOut.toString();
    }

    /**
     * @return Returns the inputEncoding.
     */
    public static String getInputEncoding() {
        return inputEncoding;
    }

    /**
     * @return Returns the outputEncoding.
     */
    public static String getOutputEncoding() {
        return outputEncoding;
    }

}
