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

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.callbacks.VFSSecurityCallback;
import org.olat.data.commons.vfs.olatimpl.OlatNamedContainerImpl;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 31.08.2011 <br>
 * 
 * @author guretzki
 */
@Component
public class Returnbox_EBL {
    public static final String RETURNBOX_DIR_NAME = "returnboxes";

    /**
     * Returnbox path relative to folder root.
     * 
     * @param courseEnv
     * @param cNode
     * @return Returnbox path relative to folder root.
     */
    public String getReturnboxRootFolder(final CourseEnvironment courseEnv, final CourseNode cNode) {
        return courseEnv.getCourseBaseContainer().getRelPath() + File.separator + Returnbox_EBL.RETURNBOX_DIR_NAME + File.separator + cNode.getIdent();
    }

    /**
     * @param courseEnvironment
     * @param node
     * @param assesseeIdentity
     * @return
     */
    public String getReturnboxFolderForIdentity(final CourseEnvironment courseEnvironment, final CourseNode node, Identity identity) {
        return getReturnboxRootFolder(courseEnvironment, node) + File.separator + identity.getName();
    }

    public VFSContainer createNamedReturnboxFolder(String returnboxFilePath, String returnboxRootFolderName, VFSSecurityCallback returnboxVfsSecurityCallback) {
        final OlatRootFolderImpl rootReturnbox = new OlatRootFolderImpl(returnboxFilePath, null);
        final OlatNamedContainerImpl namedReturnbox = new OlatNamedContainerImpl(returnboxRootFolderName, rootReturnbox);
        namedReturnbox.setLocalSecurityCallback(returnboxVfsSecurityCallback);
        return namedReturnbox;
    }

}
