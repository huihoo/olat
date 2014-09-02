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

package org.olat.lms.course.nodes;

import org.olat.data.commons.vfs.Quota;
import org.olat.data.commons.vfs.QuotaManager;
import org.olat.data.commons.vfs.callbacks.VFSSecurityCallback;
import org.olat.lms.admin.quota.QuotaConstants;
import org.olat.lms.core.notification.service.Subscribed;
import org.olat.lms.core.notification.service.SubscriptionContext;
import org.olat.lms.course.run.userview.NodeEvaluation;

/**
 * Initial Date: 10.02.2005
 * 
 * @author Mike Stock
 */

public class FolderNodeCallback implements VFSSecurityCallback, Subscribed {

    private final NodeEvaluation ne;
    private Quota nodeFolderQuota;
    private final boolean isOlatAdmin;
    private final boolean isGuestOnly;
    private final SubscriptionContext nodefolderSubContext;

    /**
     * Folder node security callback constructor
     * 
     * @param ne
     *            the current node evaluation
     * @param isOlatAdmin
     *            true if the current user has admin rights, false otherwhise.
     * @param isGuestOnly
     *            true if the current user has guest rights, false otherwhise. admins will have full access, regardless of their node evaluation
     * @param nodefolderSubContext
     */
    public FolderNodeCallback(final String relPath, final NodeEvaluation ne, final boolean isOlatAdmin, final boolean isGuestOnly,
            final SubscriptionContext nodefolderSubContext) {
        this.ne = ne;
        this.isOlatAdmin = isOlatAdmin;
        this.isGuestOnly = isGuestOnly;
        this.nodefolderSubContext = nodefolderSubContext;
        final QuotaManager qm = QuotaManager.getInstance();
        nodeFolderQuota = qm.getCustomQuota(relPath);
        if (nodeFolderQuota == null) {
            final Quota defQuota = qm.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_NODES);
            nodeFolderQuota = QuotaManager.getInstance().createQuota(relPath, defQuota.getQuotaKB(), defQuota.getUlLimitKB());
        }
    }

    /**
	 */
    @Override
    public boolean canList() {
        return ne.isCapabilityAccessible("download") || ne.isCapabilityAccessible("upload") || isOlatAdmin;
    }

    /**
	 */
    @Override
    public boolean canRead() {
        return ne.isCapabilityAccessible("download") || ne.isCapabilityAccessible("upload") || isOlatAdmin;
    }

    /**
	 */
    @Override
    public boolean canWrite() {
        if (isGuestOnly) {
            return false;
        }
        return ne.isCapabilityAccessible("upload") || isOlatAdmin;
    }

    /**
	 */
    @Override
    public boolean canDelete() {
        if (isGuestOnly) {
            return false;
        }
        return ne.isCapabilityAccessible("upload") || isOlatAdmin;
    }

    /**
	 */
    @Override
    public boolean canCopy() {
        return canRead() && canWrite();
    }

    @Override
    public boolean canDeleteRevisionsPermanently() {
        return isOlatAdmin;
    }

    /**
	 */
    @Override
    public Quota getQuota() {
        return nodeFolderQuota;
    }

    /**
	 */
    @Override
    public void setQuota(final Quota quota) {
        nodeFolderQuota = quota;
    }

    /**
	 */
    @Override
    public SubscriptionContext getSubscriptionContext() {
        return nodefolderSubContext;
    }
}
