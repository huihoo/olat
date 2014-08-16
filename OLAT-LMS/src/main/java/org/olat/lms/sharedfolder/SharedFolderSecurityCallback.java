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

package org.olat.lms.sharedfolder;

import org.olat.data.commons.vfs.Quota;
import org.olat.data.commons.vfs.QuotaManager;
import org.olat.data.commons.vfs.callbacks.VFSSecurityCallback;
import org.olat.lms.admin.quota.QuotaConstants;

/**
 * Initial Date: 29.06.2005
 * 
 * @author Alexander Schneider Comment:
 */

public class SharedFolderSecurityCallback implements VFSSecurityCallback {

    private Quota sharedFolderQuota;

    public SharedFolderSecurityCallback(final String relPath) {
        initSharedFolderQuota(relPath);
    }

    /**
     * @return boolean
     */
    @Override
    public boolean canRead() {
        return true;
    }

    /**
     * @return boolean
     */
    @Override
    public boolean canWrite() {
        return true;
    }

    /**
     * @return boolean
     */
    @Override
    public boolean canDelete() {
        return true;
    }

    /**
     * @return boolean
     */
    @Override
    public boolean canList() {
        return true;
    }

    /**
     * @return boolean
     */
    @Override
    public boolean canCopy() {
        return true;
    }

    /**
	 */
    @Override
    public boolean canDeleteRevisionsPermanently() {
        return false;
    }

    /**
     * @return boolean
     */
    @Override
    public Quota getQuota() {
        return sharedFolderQuota;
    }

    @Override
    public void setQuota(final Quota quota) {
        sharedFolderQuota = quota;
    }

    /**
     * Node folder quota has to be ionitialized only once for all of the paths.
     * 
     * @param path
     */
    private void initSharedFolderQuota(final String relPath) {
        final QuotaManager qm = QuotaManager.getInstance();
        sharedFolderQuota = qm.getCustomQuota(relPath);
        if (sharedFolderQuota == null) {
            final Quota defQuota = qm.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_COURSE);
            sharedFolderQuota = QuotaManager.getInstance().createQuota(relPath, defQuota.getQuotaKB(), defQuota.getUlLimitKB());
        }
    }

}
