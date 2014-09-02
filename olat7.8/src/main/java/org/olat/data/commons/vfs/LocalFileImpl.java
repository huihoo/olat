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

package org.olat.data.commons.vfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.olat.data.commons.vfs.version.Versionable;
import org.olat.data.commons.vfs.version.Versions;
import org.olat.data.commons.vfs.version.VersionsManager;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * VFSLeaf implementation that is based on a java.io.File from a local filesystem
 * <P>
 * Initial Date: 23.06.2005 <br>
 * 
 * @author Felix Jost
 */
public class LocalFileImpl extends LocalImpl implements VFSLeaf, Versionable {
    private static final Logger log = LoggerHelper.getLogger();

    private Versions versions;

    private LocalFileImpl() {
        super(null, null);
        throw new AssertException("Cannot instantiate LocalFileImpl().");
    }

    /**
     * Constructor
     * 
     * @param file
     *            The real file wrapped by this VFSLeaf
     */
    public LocalFileImpl(File file) {
        this(file, null);
    }

    /**
     * @param file
     */
    protected LocalFileImpl(File file, VFSContainer parentContainer) {
        super(file, parentContainer);
    }

    /**
	 */
    @Override
    public InputStream getInputStream() {
        try {
            return new FileInputStream(getBasefile());
        } catch (FileNotFoundException e) {
            log.warn("Could not create input stream for file::" + getBasefile().getAbsolutePath(), e);
            return null;
        }
    }

    /**
	 */
    @Override
    public long getSize() {
        return getBasefile().length();
    }

    /**
	 */
    @Override
    public OutputStream getOutputStream(boolean append) {
        OutputStream os = null;
        try {
            os = new FileOutputStream(getBasefile(), append);
        } catch (FileNotFoundException e) {
            log.warn("Could not create output stream for file::" + getBasefile().getAbsolutePath(), e);
        }
        return os;
    }

    @Override
    public Versions getVersions() {
        if (versions == null) {
            versions = VersionsManager.getInstance().createVersionsFor(this);
        }
        return versions;
    }

    /**
	 */
    @Override
    public VFSStatus rename(String newname) {
        File f = getBasefile();
        if (!f.exists()) {
            return VFSConstants.NO;
        }

        File par = f.getParentFile();
        File nf = new File(par, newname);
        if (getVersions().isVersioned()) {
            // rename the versions;
            VersionsManager.getInstance().rename(this, newname);
        }
        boolean ren = f.renameTo(nf);
        if (ren) {
            // f.renameTo() does NOT modify the path contained in the object f!!
            // The guys at sun consider this a feature and not a bug...
            // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4094022
            // We need to manually reload the new basefile
            super.setBasefile(new File(nf.getAbsolutePath()));
            return VFSConstants.YES;
        } else {
            return VFSConstants.NO;
        }
    }

    /**
	 */
    @Override
    public VFSStatus delete() {
        if (getVersions().isVersioned()) {
            VersionsManager.getInstance().delete(this, false);
        }
        return getBasefile().delete() ? VFSConstants.YES : VFSConstants.NO;
    }

    /**
	 */
    @Override
    public VFSItem resolve(String path) {
        path = VFSManager.sanitizePath(path);
        if (path.equals("/"))
            return this;
        String name = VFSManager.extractChild(path);
        if (path.equals(name)) {
            return this;
        } else {
            return null;
        }
    }

    /**
	 */
    @Override
    public String toString() {
        return "LFile [file=" + getBasefile() + "] ";
    }

}
