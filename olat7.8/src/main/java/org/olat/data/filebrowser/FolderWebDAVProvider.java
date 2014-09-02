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

package org.olat.data.filebrowser;

import org.olat.connectors.webdav.WebDAVProvider;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.vfs.MergeSource;
import org.olat.data.commons.vfs.Quota;
import org.olat.data.commons.vfs.QuotaManager;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.callbacks.VFSSecurityCallback;
import org.olat.data.commons.vfs.olatimpl.OlatNamedContainerImpl;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.system.commons.manager.BasicManager;

/**
 * 
 */
public class FolderWebDAVProvider extends BasicManager implements WebDAVProvider {

    private static final String MOUNTPOINT = "home";

    @Override
    public String getMountPoint() {
        return MOUNTPOINT;
    }

    /**
	 */
    @Override
    public VFSContainer getContainer(Identity identity) {
        // merge /public and /private
        MergeSource homeMergeSource = new MergeSource(null, identity.getName());

        // mount /public
        OlatRootFolderImpl vfsPublic = new OlatRootFolderImpl(getRootPathFor(identity) + "/public", homeMergeSource);
        vfsPublic.getBasefile().mkdirs(); // lazy initialize folders
        // we do a little trick here and wrap it again in a NamedContainerImpl so
        // it doesn't show up as a OlatRootFolderImpl to prevent it from editing its MetaData
        OlatNamedContainerImpl vfsNamedPublic = new OlatNamedContainerImpl("public", vfsPublic);

        // mount /private
        OlatRootFolderImpl vfsPrivate = new OlatRootFolderImpl(getRootPathFor(identity) + "/private", homeMergeSource);
        vfsPrivate.getBasefile().mkdirs(); // lazy initialize folders
        // we do a little trick here and wrap it again in a NamedContainerImpl so
        // it doesn't show up as a OlatRootFolderImpl to prevent it from editing its MetaData
        OlatNamedContainerImpl vfsNamedPrivate = new OlatNamedContainerImpl("private", vfsPrivate);

        // set quota for this merge source
        QuotaManager qm = QuotaManager.getInstance();
        Quota quota = qm.getCustomQuotaOrDefaultDependingOnRole(identity, getRootPathFor(identity));

        VFSSecurityCallback securityCallback = new FullAccessSecurityCallback();
        securityCallback.setQuota(quota);

        homeMergeSource.setLocalSecurityCallback(securityCallback);
        homeMergeSource.addContainer(vfsNamedPublic);
        homeMergeSource.addContainer(vfsNamedPrivate);

        return homeMergeSource;
    }

    protected String getRootPathFor(Identity identity) {
        return FolderConfig.getUserHomes() + "/" + identity.getName();
    }

    // local impl because of layering and having sec. callbacks in lms level. Review with security refactoring
    private class FullAccessSecurityCallback implements VFSSecurityCallback {

        private Quota quota;

        @Override
        public boolean canRead() {
            return true;
        }

        @Override
        public boolean canWrite() {
            return true;
        }

        @Override
        public boolean canDelete() {
            return true;
        }

        @Override
        public boolean canList() {
            return true;
        }

        @Override
        public boolean canCopy() {
            return true;
        }

        @Override
        public boolean canDeleteRevisionsPermanently() {
            return true;
        }

        @Override
        public Quota getQuota() {
            return quota;
        }

        @Override
        public void setQuota(Quota quota) {
            this.quota = quota;
        }

    }

}
