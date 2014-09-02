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

import java.util.List;

import org.olat.data.commons.vfs.callbacks.VFSSecurityCallback;
import org.olat.data.commons.vfs.filters.VFSItemFilter;

/**
 * Description:<br>
 * TODO: Felix Jost Class Description for VirtualContainerImpl
 * <P>
 * Initial Date: 23.06.2005 <br>
 * 
 * @author Felix Jost
 */
public class NamedContainerImpl extends AbstractVirtualContainer {

    final VFSContainer delegate;

    /**
     * @param name
     * @param delegate
     */
    public NamedContainerImpl(String name, VFSContainer delegate) {
        super(name);
        this.delegate = delegate;
    }

    public VFSContainer getDelegate() {
        return delegate;
    }

    /**
	 */
    @Override
    public VFSContainer getParentContainer() {
        return delegate.getParentContainer();
    }

    /**
	 */
    @Override
    public void setParentContainer(VFSContainer parentContainer) {
        delegate.setParentContainer(parentContainer);
    }

    /**
	 */
    @Override
    public List getItems() {
        // FIXME:fj:b add as listener to "change ownergroup" event, so that the access may be denied, if ownergroup of repoitem has changed.
        return delegate.getItems();
    }

    /**
	 */
    @Override
    public List getItems(VFSItemFilter filter) {
        return delegate.getItems(filter);
    }

    /**
	 */
    @Override
    public VFSStatus copyFrom(VFSItem source) {
        return delegate.copyFrom(source);
    }

    /**
	 */
    @Override
    public VFSStatus canWrite() {
        return delegate.canWrite();
    }

    /**
	 */
    @Override
    public VFSStatus canCopy() {
        return delegate.canCopy();
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
        return delegate.delete();
    }

    /**
	 */
    @Override
    public long getLastModified() {
        return delegate.getLastModified();
    }

    /**
     * Be aware that this method can return tricky values:
     * <ul>
     * <li>If the path is '/', the named container itself is returned</li>
     * <li>for child elements, the item of the delegate object is returned</li>
     * </ul>
     * In the second case, the returned item does not know anymore that it was embedded in a named container. Thus, the isSame() method on the root element of the
     * resolved item is not the same as this object.
     */
    @Override
    public VFSItem resolve(String path) {
        path = VFSManager.sanitizePath(path);
        if (path.equals("/"))
            return this;
        return delegate.resolve(path);
    }

    /**
	 */
    @Override
    public VFSContainer createChildContainer(String name) {
        return delegate.createChildContainer(name);
    }

    /**
	 */
    @Override
    public VFSLeaf createChildLeaf(String name) {
        return delegate.createChildLeaf(name);
    }

    /**
	 */
    @Override
    public String toString() {
        return "NamedContainer " + getName() + "-> " + delegate.toString();
    }

    /**
	 */
    @Override
    public VFSSecurityCallback getLocalSecurityCallback() {
        return delegate.getLocalSecurityCallback();
    }

    /**
	 */
    @Override
    public void setLocalSecurityCallback(VFSSecurityCallback secCallback) {
        delegate.setLocalSecurityCallback(secCallback);
    }

    /**
	 */
    @Override
    public boolean isSame(VFSItem vfsItem) {
        return delegate.isSame(vfsItem);
    }

    /**
	 */
    @Override
    public void setDefaultItemFilter(VFSItemFilter defaultFilter) {
        delegate.setDefaultItemFilter(defaultFilter);
    }

    /**
	 */
    @Override
    public VFSItemFilter getDefaultItemFilter() {
        return delegate.getDefaultItemFilter();
    }

    @Override
    public String getPath() {
        return delegate.getPath();
    }

}
