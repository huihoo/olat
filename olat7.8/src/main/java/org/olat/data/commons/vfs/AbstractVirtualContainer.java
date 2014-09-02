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

import org.olat.data.commons.vfs.filters.VFSItemFilter;

/**
 * Description:<br>
 * TODO: Felix Jost Class Description for VirtualContainer
 * <P>
 * Initial Date: 23.06.2005 <br>
 * 
 * @author Felix Jost
 */
public abstract class AbstractVirtualContainer implements VFSContainer {

    private final String name;
    protected VFSItemFilter defaultFilter = null;

    /**
     * @param name
     */
    public AbstractVirtualContainer(String name) {
        this.name = name;
    }

    /**
     * constructor for anynomous types
     */
    public AbstractVirtualContainer() {
        this.name = null;
    }

    /**
	 */
    @Override
    public VFSStatus canDelete() {
        return VFSConstants.NO;
    }

    /**
	 */
    @Override
    public VFSStatus canRename() {
        return VFSConstants.NO;
    }

    /**
	 */
    @Override
    public VFSStatus canCopy() {
        return VFSConstants.NO;
    }

    /**
	 */
    @Override
    public VFSStatus copyFrom(VFSItem vfsItem) {
        return VFSConstants.ERROR_FAILED;
    }

    /**
	 */
    @Override
    public String getName() {
        return name;
    }

    /**
	 */
    @Override
    public long getLastModified() {
        return VFSConstants.UNDEFINED;
    }

    /**
	 */
    @Override
    public VFSStatus rename(String newname) {
        throw new RuntimeException("unsupported");
    }

    /**
	 */
    @Override
    public VFSStatus delete() {
        throw new RuntimeException("unsupported");
    }

    /**
	 */
    @Override
    public VFSContainer createChildContainer(String name) {
        return null;
    }

    /**
	 */
    @Override
    public VFSLeaf createChildLeaf(String name) {
        return null;
    }

    /**
	 */
    @Override
    public void setDefaultItemFilter(VFSItemFilter defaultFilter) {
        this.defaultFilter = defaultFilter;
    }

    /**
	 */
    @Override
    public VFSItemFilter getDefaultItemFilter() {
        return this.defaultFilter;
    }

    @Override
    public boolean exists() {
        throw new UnsupportedOperationException("unsupported");
    }

    @Override
    public VFSStatus deleteItems() {
        throw new UnsupportedOperationException("unsupported");
    }

}
