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
import java.util.ArrayList;
import java.util.List;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.vfs.LocalFileImpl;
import org.olat.data.commons.vfs.LocalFolderImpl;
import org.olat.data.commons.vfs.OlatRelPathImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.filters.VFSItemFilter;
import org.olat.data.filebrowser.metadata.MetaInfo;
import org.olat.data.filebrowser.metadata.MetaInfoFileImpl;
import org.olat.data.filebrowser.metadata.tagged.MetaTagged;
import org.olat.data.filebrowser.thumbnail.ThumbnailService;
import org.olat.system.spring.CoreSpringFactory;

public class OlatRootFolderImpl extends LocalFolderImpl implements OlatRelPathImpl, MetaTagged {

    private String folderRelPath;

    public OlatRootFolderImpl(String folderRelPath, VFSContainer parent) {
        super(new File(FolderConfig.getCanonicalRoot() + folderRelPath), parent);
        this.folderRelPath = folderRelPath;
    }

    /**
     * Wrapp all LocalImpls to OlatRootImpls
     * 
     */
    @Override
    public VFSContainer createChildContainer(String name) {
        VFSItem result = super.createChildContainer(name);
        if (result == null)
            return null;
        return new OlatRootFolderImpl(folderRelPath + "/" + name, this);
    }

    /**
     * Wrapp all LocalImpls to OlatRootImpls
     * 
     */
    @Override
    public VFSLeaf createChildLeaf(String name) {
        VFSItem result = super.createChildLeaf(name);
        if (result == null)
            return null;
        return new OlatRootFileImpl(folderRelPath + "/" + name, this);
    }

    /**
     * Wrapp all LocalImpls to OlatRootImpls
     * 
     */
    @Override
    public List<VFSItem> getItems() {
        List<VFSItem> items = super.getItems();
        items = wrapItems(items);
        return items;
    }

    /**
	 */
    @Override
    public List<VFSItem> getItems(VFSItemFilter filter) {
        List<VFSItem> items = super.getItems(filter);
        items = wrapItems(items);
        return items;
    }

    /**
     * @param children
     * @return
     */
    private List<VFSItem> wrapItems(List<VFSItem> items) {
        List<VFSItem> wrappedItems = new ArrayList<VFSItem>(items.size());
        // now wrapp all LocalImpls to OlatRootImpls...
        for (VFSItem item : items) {
            if (item instanceof LocalFolderImpl) {
                wrappedItems.add(new OlatRootFolderImpl(folderRelPath + "/" + item.getName(), this));
            } else if (item instanceof LocalFileImpl) {
                wrappedItems.add(new OlatRootFileImpl(folderRelPath + "/" + item.getName(), this));
            }
        }
        return wrappedItems;
    }

    /**
	 */
    @Override
    public String getRelPath() {
        return folderRelPath;
    }

    /**
	 */
    @Override
    public MetaInfo getMetaInfo() {
        BaseSecurity baseSecurity = CoreSpringFactory.getBean(BaseSecurity.class);
        ThumbnailService thumbnailService = CoreSpringFactory.getBean(ThumbnailService.class);
        return new MetaInfoFileImpl(thumbnailService, baseSecurity, this);
    }

}
