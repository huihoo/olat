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

package org.olat.presentation.framework.dispatcher.legacy;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.olat.system.commons.WebappHelper;
import org.olat.system.exception.StartupException;

/**
 * @author Mike Stock Comment:
 */
public class FilePathHandler implements PathHandler {

    private String root = null;

    /**
     * Path handler delivering files.
     */
    public FilePathHandler() {
        super();
    }

    /**
	 */
    @Override
    public void init(final String config) {
        final String rootConf = config;
        if (rootConf == null) {
            throw new StartupException("child 'root' missing in config for filepathhandler");
        }
        final File f = new File(rootConf);
        if (f.isAbsolute()) {
            setRoot(rootConf);
        } else {
            setRoot(WebappHelper.getContextRoot() + "/" + rootConf);
        }
    }

    /**
	 */
    @Override
    public InputStream getInputStream(final HttpServletRequest request, final ResourceDescriptor rd) {
        final File f = new File(root + rd.getRelPath());
        try {
            return new BufferedInputStream(new FileInputStream(f));
        } catch (final Exception e) {
            return null;
        }
    }

    /**
	 */
    @Override
    public ResourceDescriptor getResourceDescriptor(final HttpServletRequest request, final String relPath) {
        if (root == null) {
            return null;
        }
        try {
            final File f = new File(root + relPath);
            if (!f.exists() || f.isDirectory()) {
                return null;
            }
            final ResourceDescriptor rd = new ResourceDescriptor(relPath);
            rd.setLastModified(f.lastModified());
            rd.setSize(f.length());
            String mimeType = WebappHelper.getMimeType(relPath);
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            if (mimeType.equals("text/html")) {
                mimeType = "text/html; charset=utf-8";
            }
            rd.setContentType(mimeType);
            return rd;
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * @return The canonical root of this file handler.
     */
    public String getRoot() {
        return root;
    }

    /**
     * Set the canonical root of this file handler.
     * 
     * @param newRoot
     */
    public void setRoot(final String newRoot) {
        root = newRoot;
    }

}
