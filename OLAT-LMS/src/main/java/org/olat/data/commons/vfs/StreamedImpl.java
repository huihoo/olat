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

import java.io.InputStream;
import java.io.OutputStream;

import org.olat.data.commons.vfs.callbacks.VFSSecurityCallback;

/**
 * Description:<br>
 * TODO: Felix Jost Class Description for StreamedImpl
 * <P>
 * Initial Date: 24.06.2005 <br>
 * 
 * @author Felix Jost
 */
public class StreamedImpl implements VFSLeaf {

    private final String name;
    private final InputStream inStream;
    private VFSContainer parentContainer;
    private VFSSecurityCallback securityCallback;

    /**
     * @param name
     * @param inStream
     */
    public StreamedImpl(String name, VFSContainer parentContainer, InputStream inStream) {
        this.name = name;
        this.parentContainer = parentContainer;
        this.inStream = inStream;
    }

    @Override
    public VFSContainer getParentContainer() {
        return parentContainer;
    }

    @Override
    public void setParentContainer(VFSContainer parentContainer) {
        this.parentContainer = parentContainer;
    }

    /**
	 */
    @Override
    public InputStream getInputStream() {
        return inStream;
    }

    /**
	 */
    @Override
    public long getLastModified() {
        // not known
        return VFSConstants.UNDEFINED;
    }

    /**
	 */
    @Override
    public long getSize() {
        // not known
        return VFSConstants.UNDEFINED;
    }

    /**
	 */
    @Override
    public OutputStream getOutputStream(boolean append) {
        throw new RuntimeException("cannot write to an inputstream - vfsleaf");
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
    public VFSStatus canDelete() {
        return VFSConstants.NO;
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
    public VFSStatus canCopy() {
        return VFSConstants.YES;
    }

    /**
	 */
    @Override
    public VFSStatus canWrite() {
        return VFSConstants.NO;
    }

    /**
	 */
    @Override
    public VFSItem resolve(String path) {
        // not found
        return null;
    }

    /**
	 */
    @Override
    public VFSSecurityCallback getLocalSecurityCallback() {
        return securityCallback;
    }

    /**
     * @param secCallback
     */
    @Override
    public void setLocalSecurityCallback(VFSSecurityCallback secCallback) {
        this.securityCallback = secCallback;
    }

    /**
	 */
    @Override
    public boolean isSame(VFSItem vfsItem) {
        if (!(vfsItem instanceof StreamedImpl))
            return false;
        return inStream.equals(((StreamedImpl) vfsItem).inStream);
    }

    @Override
    public boolean exists() {
        throw new UnsupportedOperationException("unsupported");
    }
}
