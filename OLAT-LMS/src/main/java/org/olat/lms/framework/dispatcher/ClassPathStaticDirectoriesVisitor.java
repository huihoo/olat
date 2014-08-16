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
package org.olat.lms.framework.dispatcher;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.log4j.Logger;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.fileutil.FileVisitor;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * This visitor copies all *_static* directories to the webapp/static/classpath/
 * <p>
 * When the visitor visits a jar file, the jar file is opened and all files with the same naming scheme are copied from the jar to the webapp/static/classpath/ as well.
 * <p>
 * The copied files have the same timestamp as the original file. Files from within jar files will have the timestamp of the jar file itself.
 * <P>
 * Initial Date: 29.08.2008 <br>
 * 
 * @author gnaegi
 */
class ClassPathStaticDirectoriesVisitor implements FileVisitor {

    private static final Logger log = LoggerHelper.getLogger();

    private String basePath;
    private File classPathStaticDir;

    /**
     * Package scope constructor
     * 
     * @param classPathStaticDir
     */
    ClassPathStaticDirectoriesVisitor(String basePathConfig, File classPathStaticDir) {
        this.classPathStaticDir = classPathStaticDir;
        this.basePath = basePathConfig;
    }

    /**
	 */
    @Override
    public void visit(File sourceFile) {
        // skip directory entries
        if (sourceFile.isDirectory())
            return;
        // skip files within a CVS directory
        if (sourceFile.getParentFile().getName().equals("CVS"))
            return;
        // continue with files: either a normal file or a jar
        try {
            int staticDirPos = sourceFile.getAbsolutePath().indexOf(DispatcherEBL.CLASSPATH_STATIC_DIR_NAME);
            if (staticDirPos != -1) {
                // Copy this file to static place
                // extract class name without trailing slashes
                String packageName = sourceFile.getAbsolutePath().substring(basePath.length() + 1, staticDirPos - 1);
                packageName = packageName.replace(File.separator, ".");
                String fileName = sourceFile.getAbsolutePath().substring(staticDirPos + DispatcherEBL.CLASSPATH_STATIC_DIR_NAME.length());
                File targetFile = new File(classPathStaticDir, packageName + fileName);
                // Only do this if it does not already exist
                if (targetFile.exists() && targetFile.lastModified() >= sourceFile.lastModified()) {
                    if (log.isDebugEnabled())
                        log.debug("Skipping static file from filename::" + sourceFile + " - does already exist", null);
                } else {
                    targetFile.getParentFile().mkdirs();
                    BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(sourceFile));
                    BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetFile));
                    FileUtils.copy(inputStream, outputStream, sourceFile.length());
                    inputStream.close();
                    outputStream.close();
                    targetFile.setLastModified(sourceFile.lastModified());
                    if (log.isDebugEnabled())
                        log.debug("Copying static file from filename::" + sourceFile, null);
                }

            } else if (sourceFile.getName().endsWith(".jar")) {
                JarFile jar;
                jar = new JarFile(sourceFile);
                Enumeration<JarEntry> jarEntries = jar.entries();
                // Check in all jars - maybe we have static resources in custom jars
                // as well (not only in core)
                while (jarEntries.hasMoreElements()) {
                    JarEntry jarEntry = jarEntries.nextElement();
                    String jarEntryName = jarEntry.getName();
                    if (jarEntryName.indexOf(File.separator + DispatcherEBL.CLASSPATH_STATIC_DIR_NAME + File.separator) != -1) {
                        if (!jarEntry.isDirectory()) {
                            // Copy file from jar to static place
                            // extract class name without trailing slashes
                            staticDirPos = jarEntryName.indexOf(DispatcherEBL.CLASSPATH_STATIC_DIR_NAME);
                            String packageName = jarEntryName.substring(0, staticDirPos - 1);
                            packageName = packageName.replace(File.separator, ".");
                            String fileName = jarEntryName.substring(staticDirPos + DispatcherEBL.CLASSPATH_STATIC_DIR_NAME.length());
                            File targetFile = new File(classPathStaticDir, packageName + fileName);
                            // Only do this if it does not already exist.
                            // Use jar file last modified instead of jarEntry.getTime, seems to be unpredictable
                            if (targetFile.exists() && targetFile.lastModified() >= sourceFile.lastModified()) {
                                if (log.isDebugEnabled())
                                    log.debug("Skipping static file from jar, filename::" + jarEntryName + " in jar::" + jar.getName() + " - does already exist", null);
                            } else {
                                targetFile.getParentFile().mkdirs();
                                BufferedInputStream inputStream = new BufferedInputStream(jar.getInputStream(jarEntry));
                                BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetFile));
                                FileUtils.copy(inputStream, outputStream, jarEntry.getSize());
                                inputStream.close();
                                outputStream.close();
                                targetFile.setLastModified(sourceFile.lastModified());
                                if (log.isDebugEnabled())
                                    log.debug("Copying static file from jar, filename::" + jarEntryName + " in jar::" + jar.getName(), null);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new OLATRuntimeException("Error while copying files from jar::" + sourceFile.getAbsolutePath() + " - Delete all copied static resources in "
                    + classPathStaticDir.getAbsolutePath() + "and restart tomcat", e);
        }
    }

}
