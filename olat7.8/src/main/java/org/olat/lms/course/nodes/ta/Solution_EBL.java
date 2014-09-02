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
package org.olat.lms.course.nodes.ta;

import java.io.File;

import org.olat.data.commons.vfs.Quota;
import org.olat.data.commons.vfs.QuotaManager;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.callbacks.VFSSecurityCallback;
import org.olat.data.commons.vfs.olatimpl.OlatNamedContainerImpl;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.lms.admin.quota.QuotaConstants;
import org.olat.lms.commons.vfs.securitycallbacks.FullAccessWithQuotaCallback;
import org.olat.lms.commons.vfs.securitycallbacks.ReadOnlyCallback;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 31.08.2011 <br>
 * 
 * @author guretzki
 */
@Component
public class Solution_EBL {
    /** Solution folder-name in the file-system. */
    public static final String SOLUTION_FOLDER_NAME = "solutions";

    @Autowired
    QuotaManager quotaManager;

    /**
     * Solution-folder path relative to folder root.
     * 
     * @param courseEnv
     * @param cNode
     * @return Returnbox path relative to folder root.
     */
    public String getSolutionRootFolder(final CourseEnvironment courseEnv, final CourseNode cNode) {
        return courseEnv.getCourseBaseContainer().getRelPath() + File.separator + SOLUTION_FOLDER_NAME + File.separator + cNode.getIdent();
    }

    /**
     * @param courseEnv
     * @return the relative folder base path for folder nodes
     */
    public static String getSolutionFolderBasePath(final CourseEnvironment courseEnv) {
        return courseEnv.getCourseBaseContainer().getRelPath() + File.separator + SOLUTION_FOLDER_NAME;
    }

    public OlatNamedContainerImpl getNodeFolderContainer(final CourseEnvironment courseEnvironment, final CourseNode node) {
        final String path = getSolutionRootFolder(courseEnvironment, node);
        final OlatRootFolderImpl rootFolder = new OlatRootFolderImpl(path, null);
        final OlatNamedContainerImpl namedFolder = new OlatNamedContainerImpl(SOLUTION_FOLDER_NAME, rootFolder);
        Quota quota = quotaManager.getCustomQuota(namedFolder.getRelPath());
        if (quota == null) {
            final Quota defQuota = quotaManager.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_NODES);
            quota = quotaManager.createQuota(namedFolder.getRelPath(), defQuota.getQuotaKB(), defQuota.getUlLimitKB());
        }
        final VFSSecurityCallback secCallback = new FullAccessWithQuotaCallback(quota);
        namedFolder.setLocalSecurityCallback(secCallback);
        return namedFolder;
    }

    /**
     * @param courseEnvironment
     * @param node
     * @return
     */
    public VFSContainer getReadonlyFolderContainer(CourseEnvironment courseEnvironment, CourseNode node) {
        final OlatRootFolderImpl rootFolder = new OlatRootFolderImpl(getSolutionRootFolder(courseEnvironment, node), null);
        final OlatNamedContainerImpl namedContainer = new OlatNamedContainerImpl("solutions", rootFolder);
        namedContainer.setLocalSecurityCallback(new ReadOnlyCallback());
        return namedContainer;
    }

}
