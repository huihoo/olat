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
 * Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.lms.ims.cp;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.dom4j.tree.DefaultDocument;
import org.dom4j.tree.DefaultElement;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.fileutil.ZipUtil;
import org.olat.data.commons.vfs.LocalFolderImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.VFSManager;
import org.olat.data.commons.xml.XMLParser;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.fileresource.FileResourceManager;
import org.olat.lms.ims.cp.objects.CPOrganization;
import org.olat.lms.ims.cp.objects.CPResource;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.system.commons.CodeHelper;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.stereotype.Service;

/**
 * The CP manager implementation.
 * <p>
 * In many cases, method calls are delegated to the content package object.
 * <P>
 * Initial Date: 04.07.2008 <br>
 * 
 * @author Sergio Trentini
 */
@Service
public class CPManagerImpl extends CPManager {

    private static final Logger log = LoggerHelper.getLogger();

    /**
     * [spring]
     */
    private CPManagerImpl() {
        //
    }

    /**
	 */
    @Override
    public ContentPackage load(final VFSContainer directory, final OLATResourceable ores) {
        final XMLParser parser = new XMLParser();
        ContentPackage cp;

        final VFSLeaf file = (VFSLeaf) directory.resolve("imsmanifest.xml");

        if (file != null) {
            try {
                final DefaultDocument doc = (DefaultDocument) parser.parse(file.getInputStream(), false);
                cp = new ContentPackage(doc, directory, ores);
                // If a wiki is imported or a new cp created, set a unique orga
                // identifier.
                if (cp.getLastError() == null) {
                    if (cp.isOLATContentPackage() && CPCore.OLAT_ORGANIZATION_IDENTIFIER.equals(cp.getFirstOrganizationInManifest().getIdentifier())) {
                        setUniqueOrgaIdentifier(cp);
                    }
                }

            } catch (final OLATRuntimeException e) {
                cp = new ContentPackage(null, directory, ores);
                log.error("Reading imsmanifest failed. Dir: " + directory.getName() + ". Ores: " + ores.getResourceableId(), e);
                cp.setLastError("Exception reading XML for IMS CP: invalid xml-file ( " + directory.getName() + ")");
            }

        } else {
            cp = new ContentPackage(null, directory, ores);
            cp.setLastError("Exception reading XML for IMS CP: IMS-Manifest not found in " + directory.getName());
            log.error("IMS manifiest xml couldn't be found in dir " + directory.getName() + ". Ores: " + ores.getResourceableId(), null);
            throw new OLATRuntimeException(this.getClass(), "The imsmanifest.xml file was not found.", new IOException());
        }
        return cp;
    }

    /**
	 */
    @Override
    public ContentPackage createNewCP(final OLATResourceable ores, final String initalPageTitle) {
        // copy template cp to new repo-location
        if (copyTemplCP(ores)) {
            final File cpRoot = FileResourceManager.getInstance().unzipFileResource(ores);
            log.debug("createNewCP: cpRoot=" + cpRoot);
            log.debug("createNewCP: cpRoot.getAbsolutePath()=" + cpRoot.getAbsolutePath());
            System.out.println("createNewCP: cpRoot.getAbsolutePath()=" + cpRoot.getAbsolutePath());
            final LocalFolderImpl vfsWrapper = new LocalFolderImpl(cpRoot);
            final ContentPackage cp = load(vfsWrapper, ores);

            // Modify the copy of the template to get a unique identifier
            final CPOrganization orga = setUniqueOrgaIdentifier(cp);
            setOrgaTitleToRepoEntryTitle(ores, orga);
            // Also set the translated title of the inital page.
            orga.getItems().get(0).setTitle(initalPageTitle);

            writeToFile(cp);
            return cp;

        } else {
            log.error("CP couldn't be created. Error when copying template. Ores: " + ores.getResourceableId(), null);
            throw new OLATRuntimeException("ERROR while creating new empty cp. an error occured while trying to copy template CP", null);
        }
    }

    /**
     * Sets the organization title to the title of the repository entry.
     * 
     * @param ores
     * @param orga
     */
    private void setOrgaTitleToRepoEntryTitle(final OLATResourceable ores, final CPOrganization orga) {
        // Set the title of the organization to the title of the resource.
        final RepositoryService resMgr = RepositoryServiceImpl.getInstance();
        final RepositoryEntry cpEntry = resMgr.lookupRepositoryEntry(ores, false);
        if (cpEntry != null) {
            final String title = cpEntry.getDisplayname();
            orga.setTitle(title);
        }
    }

    /**
     * Assigns the organization a unique identifier in order to prevent any caching issues in the extjs menu tree later.
     * 
     * @param cp
     * @return The first organization of the content package.
     */
    private CPOrganization setUniqueOrgaIdentifier(final ContentPackage cp) {
        final CPOrganization orga = cp.getFirstOrganizationInManifest();
        final String newOrgaIdentifier = "olatcp-" + CodeHelper.getForeverUniqueID();
        orga.setIdentifier(newOrgaIdentifier);
        return orga;
    }

    @Override
    public boolean isSingleUsedResource(final CPResource res, final ContentPackage cp) {
        return cp.isSingleUsedResource(res);
    }

    @Override
    public String addBlankPage(final ContentPackage cp, final String title) {
        return cp.addBlankPage(title);
    }

    @Override
    public String addBlankPage(final ContentPackage cp, final String title, final String parentNodeID) {
        return cp.addBlankPage(parentNodeID, title);
    }

    @Override
    public void updatePage(final ContentPackage cp, final CPPage page) {
        cp.updatePage(page);
    }

    /**
	 */
    @Override
    public boolean addElement(final ContentPackage cp, final DefaultElement newElement) {
        return cp.addElement(newElement);

    }

    /**
	 */
    @Override
    public boolean addElement(final ContentPackage cp, final DefaultElement newElement, final String parentIdentifier, final int position) {
        return cp.addElement(newElement, parentIdentifier, position);
    }

    /**
	 */
    @Override
    public boolean addElementAfter(final ContentPackage cp, final DefaultElement newElement, final String identifier) {
        return cp.addElementAfter(newElement, identifier);
    }

    /**
	 */
    @Override
    public void removeElement(final ContentPackage cp, final String identifier, final boolean deleteResource) {
        cp.removeElement(identifier, deleteResource);
    }

    /**
	 */
    @Override
    public void moveElement(final ContentPackage cp, final String nodeID, final String newParentID, final int position) {
        cp.moveElement(nodeID, newParentID, position);
    }

    /**
	 */
    @Override
    public String copyElement(final ContentPackage cp, final String sourceID) {
        return cp.copyElement(sourceID, sourceID);
    }

    /**
	 */
    @Override
    public DefaultDocument getDocument(final ContentPackage cp) {
        return cp.getDocument();
    }

    @Override
    public String getItemTitle(final ContentPackage cp, final String itemID) {
        return cp.getItemTitle(itemID);
    }

    /**
	 */
    @Override
    public DefaultElement getElementByIdentifier(final ContentPackage cp, final String identifier) {
        return cp.getElementByIdentifier(identifier);
    }

    @Override
    public CPTreeDataModel getTreeDataModel(final ContentPackage cp) {
        return cp.buildTreeDataModel();
    }

    /**
	 */
    @Override
    public CPOrganization getFirstOrganizationInManifest(final ContentPackage cp) {
        return cp.getFirstOrganizationInManifest();
    }

    /**
	 */
    @Override
    public CPPage getFirstPageToDisplay(final ContentPackage cp) {
        return cp.getFirstPageToDisplay();
    }

    /**
	 */
    @Override
    public void writeToFile(final ContentPackage cp) {
        cp.writeToFile();
    }

    /**
	 */
    @Override
    public VFSLeaf writeToZip(final ContentPackage cp) {
        final OLATResourceable ores = cp.getResourcable();
        final VFSContainer cpRoot = cp.getRootDir();
        final VFSContainer oresRoot = FileResourceManager.getInstance().getFileResourceRootImpl(ores);
        final RepositoryEntry repoEntry = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(ores, false);
        String zipFileName = "imscp.zip";
        if (repoEntry != null) {
            final String zipName = repoEntry.getResourcename();
            if (zipName != null && zipName.endsWith(".zip")) {
                zipFileName = zipName;
            }
        }
        // delete old archive and create new one
        final VFSItem oldArchive = oresRoot.resolve(zipFileName);
        if (oldArchive != null) {
            oldArchive.delete();
        }
        ZipUtil.zip(cpRoot.getItems(), oresRoot.createChildLeaf(zipFileName), true);
        final VFSLeaf zip = (VFSLeaf) oresRoot.resolve(zipFileName);
        return zip;
    }

    /**
	 */
    @Override
    public String getPageByItemId(final ContentPackage cp, final String itemIdentifier) {
        return cp.getPageByItemId(itemIdentifier);
    }

    /**
     * copies the default,empty, cp template to the new ores-directory
     * 
     * @param ores
     * @return
     */
    private boolean copyTemplCP(final OLATResourceable ores) {
        final File root = FileResourceManager.getInstance().getFileResourceRoot(ores);

        final String packageName = ContentPackage.class.getCanonicalName();
        String path = packageName.replace('.', '/');
        path = path.replace("/ContentPackage", "/_resources/imscp.zip");

        path = VFSManager.sanitizePath(path);
        final URL url = this.getClass().getResource(path);
        try {
            final File f = new File(url.toURI());
            if (f.exists() && root.exists()) {
                FileUtils.copyFileToDir(f, root, "copy imscp template");
            } else {
                log.error("cp template was not copied. Source:  " + url + " Target: " + root.getAbsolutePath(), null);
            }
        } catch (final URISyntaxException e) {
            log.error("Bad url syntax when copying cp template. url: " + url + " Ores:" + ores.getResourceableId(), null);
            return false;
        }

        return true;
    }

}
