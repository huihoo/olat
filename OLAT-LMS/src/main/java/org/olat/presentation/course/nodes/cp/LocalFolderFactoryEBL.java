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
package org.olat.presentation.course.nodes.cp;

import java.io.File;

import org.olat.data.commons.vfs.LocalFolderImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.resource.OLATResource;
import org.olat.lms.commons.fileresource.FileResourceManager;
import org.olat.system.exception.AssertException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * TODO: Class Description for LocalFolderFactoryEBL
 * 
 * <P>
 * Initial Date: 20.09.2011 <br>
 * 
 * @author Branislav Balaz
 */
@Component
public class LocalFolderFactoryEBL {

    @Autowired
    FileResourceManager fileresourceManager;

    public VFSContainer getLocalFolderImplForOlatResource(final OLATResource olatResource) {
        final File cpRoot = fileresourceManager.unzipFileResource(olatResource);
        // should always exist because references cannot be deleted as long as
        // nodes reference them
        /* TODO: ORID-1007 check if that should be done for all cases */
        if (cpRoot == null) {
            throw new AssertException("file of repository entry " + olatResource.getKey() + " was missing");
        }
        return new LocalFolderImpl(cpRoot);
    }

}
