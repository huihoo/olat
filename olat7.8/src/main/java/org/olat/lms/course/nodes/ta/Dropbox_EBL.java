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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.vfs.Quota;
import org.olat.data.commons.vfs.QuotaManager;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.callbacks.VFSSecurityCallback;
import org.olat.data.commons.vfs.olatimpl.OlatNamedContainerImpl;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.data.filebrowser.metadata.MetaInfo;
import org.olat.data.filebrowser.metadata.tagged.MetaTagged;
import org.olat.lms.admin.quota.QuotaConstants;
import org.olat.lms.commons.vfs.securitycallbacks.FullAccessWithQuotaCallback;
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
public class Dropbox_EBL {
    public static final String DROPBOX_DIR_NAME = "dropboxes";

    @Autowired
    QuotaManager quotaManager;

    /**
     * Dropbox path relative to folder root.
     * 
     * @param courseEnv
     * @param cNode
     * @return Dropbox path relative to folder root.
     */
    public String getDropboxRootFolder(final CourseEnvironment courseEnv, final CourseNode cNode) {
        return courseEnv.getCourseBaseContainer().getRelPath() + File.separator + DROPBOX_DIR_NAME + File.separator + cNode.getIdent();
    }

    public String getDropboxFolderForIdentity(final CourseEnvironment courseEnv, final CourseNode cNode, final Identity identity) {
        return getDropboxRootFolder(courseEnv, cNode) + File.separator + identity.getName();
    }

    public int getNumberOfFilesInDropbbox(final String dropboxPath) {
        final VFSContainer fDropbox = getDropBox(dropboxPath);
        return fDropbox.getItems().size();
    }

    public boolean saveUploadedFileInDropbox(VFSLeaf fIn, String dropboxPath, Identity uploadIdentity) {
        final VFSContainer fDropbox = getDropBox(dropboxPath);
        VFSLeaf fOut;
        if (fDropbox.resolve(fIn.getName()) != null) {
            // FIXME ms: check if dropbox quota is exceeded -> with customers abklaeren
            fOut = fDropbox.createChildLeaf(getNewUniqueName(fIn.getName()));
        } else {
            fOut = fDropbox.createChildLeaf(fIn.getName());
        }

        final InputStream in = fIn.getInputStream();
        final OutputStream out = new BufferedOutputStream(fOut.getOutputStream(false));
        boolean success = FileUtils.copy(in, out);
        FileUtils.closeSafely(in);
        FileUtils.closeSafely(out);

        if (fOut instanceof MetaTagged) {
            final MetaInfo info = ((MetaTagged) fOut).getMetaInfo();
            if (info != null) {
                info.setAuthor(uploadIdentity.getName());
                info.write();
            }
        }
        return success;
    }

    private String getNewUniqueName(final String name) {
        String body = null;
        String ext = null;
        final int dot = name.lastIndexOf(".");
        if (dot != -1) {
            body = name.substring(0, dot);
            ext = name.substring(dot);
        } else {
            body = name;
            ext = "";
        }
        final String tStamp = new SimpleDateFormat("yyMMdd-HHmmss").format(new Date());
        return body + "." + tStamp + ext;
    }

    /**
     * Get the dropbox of an identity.
     * 
     * @param identity
     * @return Dropbox of an identity
     */
    private VFSContainer getDropBox(final String dropboxPath) {
        final OlatRootFolderImpl dropBox = new OlatRootFolderImpl(dropboxPath, null);
        if (!dropBox.getBasefile().exists()) {
            dropBox.getBasefile().mkdirs();
        }
        return dropBox;
    }

    /**
     * Get upload limit for dropbox of a certain user. The upload can be limited by available-folder space, max folder size or configurated upload-limit.
     * 
     * @param ureq
     * @param identity
     *            TODO
     * @param dropboxPath
     *            TODO
     * @return max upload limit in KB
     */
    public int getUploadLimit(final Identity identity, final String dropboxPath) {
        Quota dropboxQuota = quotaManager.getCustomQuota(dropboxPath);
        if (dropboxQuota == null) {
            dropboxQuota = quotaManager.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_NODES);
        }
        final OlatRootFolderImpl rootFolder = new OlatRootFolderImpl(dropboxPath, null);
        final VFSContainer dropboxContainer = new OlatNamedContainerImpl(identity.getName(), rootFolder);
        final FullAccessWithQuotaCallback secCallback = new FullAccessWithQuotaCallback(dropboxQuota);
        rootFolder.setLocalSecurityCallback(secCallback);
        final int ulLimit = quotaManager.getUploadLimitKB(dropboxQuota.getQuotaKB(), dropboxQuota.getUlLimitKB(), dropboxContainer);
        return ulLimit;
    }

    /**
     * @param dropboxFilePath
     * @param dropboxRootFolderName
     * @param dropboxVfsSecurityCallback
     * @return
     */
    public VFSContainer createNamedDropboxFolder(String dropboxFilePath, String dropboxRootFolderName, VFSSecurityCallback dropboxVfsSecurityCallback) {
        final OlatRootFolderImpl rootDropbox = new OlatRootFolderImpl(dropboxFilePath, null);
        rootDropbox.setLocalSecurityCallback(dropboxVfsSecurityCallback);
        final OlatNamedContainerImpl namedDropbox = new OlatNamedContainerImpl(dropboxRootFolderName, rootDropbox);
        namedDropbox.setLocalSecurityCallback(dropboxVfsSecurityCallback);
        return namedDropbox;
    }

}
