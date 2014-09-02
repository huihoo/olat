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

package org.olat.lms.group;

import java.util.ArrayList;
import java.util.List;

import org.olat.connectors.webdav.WebDAVProvider;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.MergeSource;
import org.olat.data.commons.vfs.NamedContainerImpl;
import org.olat.data.commons.vfs.Quota;
import org.olat.data.commons.vfs.QuotaManager;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.data.group.BusinessGroup;
import org.olat.lms.admin.quota.QuotaConstants;
import org.olat.lms.commons.vfs.securitycallbacks.FullAccessWithQuotaCallback;
import org.olat.lms.core.notification.service.SubscriptionContext;
import org.olat.presentation.collaboration.CollaborationTools;
import org.olat.presentation.collaboration.CollaborationToolsFactory;
import org.olat.system.commons.Formatter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 */
public class GroupfoldersWebDAVProvider implements WebDAVProvider {

    private static final String MOUNTPOINT = "groupfolders";
    @Autowired
    private BusinessGroupService businessGroupService;

    @Override
    public String getMountPoint() {
        return MOUNTPOINT;
    }

    // TODO OLAT-6874 => see comment in CoursefolderWebDAVProvider
    @Override
    public VFSContainer getContainer(final Identity identity) {
        final MergeSource cfRoot = new MergeSource(null, null);
        // collect buddy groups
        final QuotaManager qm = QuotaManager.getInstance();
        final List<BusinessGroup> groups = businessGroupService.findBusinessGroupsAttendedBy(null, identity, null);
        groups.addAll(businessGroupService.findBusinessGroupsOwnedBy(null, identity, null));

        final List<Long> addedGroupKeys = new ArrayList<Long>();
        final List<String> addedGroupNames = new ArrayList<String>();
        for (final BusinessGroup group : groups) {
            if (addedGroupKeys.contains(group.getKey())) {
                continue; // check for duplicate groups
            }
            final CollaborationTools tools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(group);
            if (tools.isToolEnabled(CollaborationTools.TOOL_FOLDER)) {
                String name = group.getName();
                if (addedGroupNames.contains(name)) {
                    // attach a serial to the group name to avoid duplicate mount points...
                    int serial = 1;
                    final int serialMax = 100;
                    while (addedGroupNames.contains(name + serial) && serial < serialMax) {
                        serial++;
                    }
                    if (serial == serialMax) {
                        continue; // continue without adding mount point
                    }
                    name = name + serial;
                }

                // create container and set quota
                final OlatRootFolderImpl localImpl = new OlatRootFolderImpl(tools.getFolderRelPath(), cfRoot);
                localImpl.getBasefile().mkdirs(); // lazy initialize dirs
                final NamedContainerImpl grpContainer = new NamedContainerImpl(Formatter.makeStringFilesystemSave(name), localImpl);
                Quota q = qm.getCustomQuota(tools.getFolderRelPath());
                if (q == null) {
                    final Quota defQuota = qm.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_GROUPS);
                    q = QuotaManager.getInstance().createQuota(tools.getFolderRelPath(), defQuota.getQuotaKB(), defQuota.getUlLimitKB());
                }

                final SubscriptionContext sc = new SubscriptionContext(group, "toolfolder");
                final FullAccessWithQuotaCallback secCallback = new FullAccessWithQuotaCallback(q, sc);
                grpContainer.setLocalSecurityCallback(secCallback);

                // add container
                cfRoot.addContainer(grpContainer);
                addedGroupKeys.add(group.getKey());
                addedGroupNames.add(name);
            }
        }
        return cfRoot;
    }

}
