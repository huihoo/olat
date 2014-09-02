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

import org.olat.data.commons.vfs.callbacks.VFSSecurityCallback;

/**
 * Description:<br>
 * TODO: Felix Jost Class Description for LocalImpl
 * <P>
 * Initial Date: 23.06.2005 <br>
 * 
 * @author Felix Jost
 */
public abstract class LocalImpl implements VFSItem {

    private File basefile;
    private VFSContainer parentContainer;
    private VFSSecurityCallback securityCallback;

    /**
     * @param basefile
     */
    protected LocalImpl(File basefile, VFSContainer parent) {
        this.basefile = basefile;
        this.parentContainer = parent;
    }

    /**
	 */
    @Override
    public VFSContainer getParentContainer() {
        return parentContainer;
    }

    /**
	 */
    @Override
    public void setParentContainer(VFSContainer parentContainer) {
        this.parentContainer = parentContainer;
    }

    /**
	 */
    @Override
    public VFSStatus canDelete() {
        VFSContainer inheritingContainer = VFSManager.findInheritingSecurityCallbackContainer(this);
        if (inheritingContainer != null && !inheritingContainer.getLocalSecurityCallback().canDelete())
            return VFSConstants.NO_SECURITY_DENIED;
        return (basefile.canWrite() ? VFSConstants.YES : VFSConstants.NO);
    }

    /**
	 */
    @Override
    public VFSStatus canCopy() {
        VFSContainer inheritingContainer = VFSManager.findInheritingSecurityCallbackContainer(this);
        if (inheritingContainer != null && !inheritingContainer.getLocalSecurityCallback().canCopy())
            return VFSConstants.NO_SECURITY_DENIED;
        return VFSConstants.YES;
    }

    /**
	 */
    @Override
    public VFSStatus canRename() {
        VFSContainer inheritingContainer = VFSManager.findInheritingSecurityCallbackContainer(this);
        if (inheritingContainer != null && !inheritingContainer.getLocalSecurityCallback().canWrite())
            return VFSConstants.NO_SECURITY_DENIED;
        return VFSConstants.YES;
    }

    /**
	 */
    @Override
    public VFSStatus canWrite() {
        return VFSConstants.NO;
    }

    /**
	 */
    @Override
    public String getName() {
        return basefile.getName();
    }

    /**
     * Be aware that the returned base file reference might change, do not hold a local reference to it in your code! Due to a bug in Java after renaming a LocalImpl file
     * the base file will be a new object with a new reference!
     * 
     * @return the current base file
     */
    public File getBasefile() {
        return basefile;
    }

    /**
     * Used only to overcome the java rename bug http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4094022
     * 
     * @param newBasefile
     */
    protected void setBasefile(File newBasefile) {
        basefile = newBasefile;
    }

    /**
     * @return lmd
     */
    @Override
    public long getLastModified() {
        long lm = basefile.lastModified();
        // file returns zero -> we return -1 (see interface docu)
        return lm == 0L ? VFSConstants.UNDEFINED : lm;
    }

    /**
	 */
    @Override
    public abstract VFSStatus rename(String newname);

    /**
	 */
    @Override
    public abstract VFSStatus delete();

    /**
	 */
    @Override
    public void setLocalSecurityCallback(VFSSecurityCallback securityCallback) {
        this.securityCallback = securityCallback;
    }

    /**
	 */
    @Override
    public VFSSecurityCallback getLocalSecurityCallback() {
        return securityCallback;
    }

    /**
	 */
    @Override
    public boolean isSame(VFSItem vfsItem) {
        if (!(vfsItem instanceof LocalImpl))
            return false;
        return getBasefile().equals(((LocalImpl) vfsItem).getBasefile());
    }

    @Override
    public boolean exists() {
        return basefile.exists();
    }

    @Override
    public String getPath() {
        return basefile.getAbsolutePath();
    }

}
