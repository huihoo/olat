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

package org.olat.data.commons.vfs.olatimpl;

import java.io.File;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.vfs.LocalFileImpl;
import org.olat.data.commons.vfs.OlatRelPathImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.filebrowser.metadata.MetaInfo;
import org.olat.data.filebrowser.metadata.MetaInfoFileImpl;
import org.olat.data.filebrowser.metadata.tagged.MetaTagged;
import org.olat.data.filebrowser.thumbnail.ThumbnailService;
import org.olat.system.commons.StringHelper;
import org.olat.system.spring.CoreSpringFactory;

public class OlatRootFileImpl extends LocalFileImpl implements OlatRelPathImpl, MetaTagged {

    private String fileRelPath;

    /**
     * @param Path
     *            to the file, relative to <code>bcroot</code>
     * @param parentContainer
     *            Optional VFS parent container
     */
    public OlatRootFileImpl(String fileRelPath, VFSContainer parentContainer) {
        super(new File(FolderConfig.getCanonicalRoot() + fileRelPath), parentContainer);
        this.fileRelPath = fileRelPath;
    }

    /**
	 */
    @Override
    public String getRelPath() {
        return fileRelPath;
    }

    /**
	 */
    @Override
    public MetaInfo getMetaInfo() {
        BaseSecurity baseSecurity = CoreSpringFactory.getBean(BaseSecurity.class);
        ThumbnailService thumbnailService = CoreSpringFactory.getBean(ThumbnailService.class);
        return new MetaInfoFileImpl(thumbnailService, baseSecurity, this);
    }

    @Override
    public String toString() {
        if (getMetaInfo() != null && StringHelper.containsNonWhitespace(getMetaInfo().getTitle())) {
            return getMetaInfo().getTitle();
        } else {
            return getName();
        }
    }
}
