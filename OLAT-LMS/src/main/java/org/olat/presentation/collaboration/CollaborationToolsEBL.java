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
package org.olat.presentation.collaboration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.fileutil.ZipUtil;
import org.olat.data.commons.vfs.LocalFolderImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.data.properties.PropertyImpl;
import org.olat.lms.forum.archiver.ForumArchiveManager;
import org.olat.lms.forum.archiver.ForumFormatter;
import org.olat.lms.forum.archiver.ForumRTFFormatter;
import org.olat.lms.properties.PropertyManagerEBL;
import org.olat.lms.properties.PropertyParameterObject;
import org.olat.lms.wiki.WikiManager;
import org.olat.lms.wiki.WikiToZipUtils;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 28.09.2011 <br>
 * 
 * @author Branislav Balaz
 */
@Component
public class CollaborationToolsEBL {

    @Autowired
    private PropertyManagerEBL propertyManagerEBL;
    private static final Logger log = LoggerHelper.getLogger();

    public void deleteCollaborationToolsFolder(final String folderPath) {

        final File fFolderRoot = getFolder(folderPath);
        if (fFolderRoot.exists()) {
            FileUtils.deleteDirsAndFiles(fFolderRoot, true, true);
        }

    }

    private File getFolder(final String folderPath) {
        final OlatRootFolderImpl vfsContainer = new OlatRootFolderImpl(folderPath, null);
        final File fFolderRoot = vfsContainer.getBasefile();
        return fFolderRoot;
    }

    public void archiveForum(final OLATResourceable ores, final String archivFilePath) {
        PropertyParameterObject propertyParameterObject = new PropertyParameterObject.Builder().resourceable(ores).category(PropertyManagerEBL.PROP_CAT_BG_COLLABTOOLS)
                .name(PropertyManagerEBL.KEY_FORUM).build();
        final PropertyImpl forumKeyProperty = propertyManagerEBL.findProperty(propertyParameterObject);
        if (forumKeyProperty != null) {
            final VFSContainer archiveContainer = new LocalFolderImpl(new File(archivFilePath));
            final String archiveForumName = "del_forum_" + forumKeyProperty.getLongValue();
            final VFSContainer archiveForumContainer = archiveContainer.createChildContainer(archiveForumName);
            final ForumFormatter ff = new ForumRTFFormatter(archiveForumContainer, false);
            ForumArchiveManager.getInstance().applyFormatter(ff, forumKeyProperty.getLongValue(), null);
        }
    }

    public void archiveWiki(final OLATResourceable ores, final String archivFilePath) {
        final VFSContainer wikiContainer = WikiManager.getInstance().getWikiRootContainer(ores);
        final VFSLeaf wikiZip = WikiToZipUtils.getWikiAsZip(wikiContainer);
        final String exportFileName = "del_wiki_" + ores.getResourceableId() + ".zip";
        final File archiveDir = new File(archivFilePath);
        if (!archiveDir.exists()) {
            archiveDir.mkdir();
        }
        final String fullFilePath = archivFilePath + File.separator + exportFileName;

        try {
            FileUtils.bcopy(wikiZip.getInputStream(), new File(fullFilePath), "archive wiki");
        } catch (final FileNotFoundException e) {
            log.warn("Can not archive wiki repoEntry=" + ores.getResourceableId());
        } catch (final IOException ioe) {
            log.warn("Can not archive wiki repoEntry=" + ores.getResourceableId());
        }
    }

    public void archiveCollaborationToolsFolder(final OLATResourceable ores, final String archiveFilePath, final String folderPath) {
        final File fFolderRoot = getFolder(folderPath);
        if (fFolderRoot.exists()) {
            final String zipFileName = "del_folder_" + ores.getResourceableId() + ".zip";
            final String fullZipFilePath = archiveFilePath + File.separator + zipFileName;
            ZipUtil.zipAll(fFolderRoot, new File(fullZipFilePath), true);
        }
    }

}
