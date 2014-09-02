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

import org.apache.log4j.Logger;
import org.dom4j.tree.DefaultElement;
import org.olat.data.commons.vfs.LocalFileImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.lms.ims.cp.objects.CPItem;
import org.olat.lms.ims.cp.objects.CPMetadata;
import org.olat.lms.ims.cp.objects.CPOrganization;
import org.olat.lms.ims.cp.objects.CPResource;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Represents a CP-Page used for GUI
 * <P>
 * Initial Date: 26.08.2008 <br>
 * 
 * @author Sergio Trentini
 */
public class CPPage {

    private static final Logger log = LoggerHelper.getLogger();
    private String identifier;
    private String idRef;
    private String title;
    private VFSContainer rootDir;
    private VFSLeaf pageFile;
    private ContentPackage cp;
    private CPMetadata metadata;
    private boolean cpRoot; // if this page represents the <organization>
                            // element

    // of the manifest

    public CPPage(final String identifier, final String title, final ContentPackage cp) {
        this.identifier = identifier;
        this.title = title;
        this.cp = cp;
        this.rootDir = cp.getRootDir();
    }

    /**
     * @param identifier
     * @param cp
     */
    public CPPage(final String identifier, final ContentPackage cp) {
        this.identifier = identifier;
        final CPManager cpMgm = (CPManager) CoreSpringFactory.getBean(CPManager.class);
        final DefaultElement ele = cpMgm.getElementByIdentifier(cp, identifier);
        if (ele instanceof CPItem) {
            final CPItem pageItem = (CPItem) ele;
            this.cpRoot = false;
            this.idRef = pageItem.getIdentifierRef();
            this.title = pageItem.getTitle();
            this.rootDir = cp.getRootDir();
            this.metadata = pageItem.getMetadata();
            if (metadata != null) {
                metadata.setTitle(title);
            }
            this.cp = cp;
            final String filePath = cpMgm.getPageByItemId(cp, identifier);
            if (filePath != null && filePath != "") {
                final LocalFileImpl f = (LocalFileImpl) cp.getRootDir().resolve(filePath);
                this.pageFile = f;
            }
        } else if (ele instanceof CPOrganization) {
            final CPOrganization orga = (CPOrganization) ele;
            this.cpRoot = true;
            this.title = orga.getTitle();
        }
    }

    public void setIdentifier(final String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getIdRef() {
        return idRef;
    }

    public String getTitle() {
        return title;
    }

    public VFSContainer getRootDir() {
        return rootDir;
    }

    public boolean isOrgaPage() {
        return cpRoot;
    }

    /**
     * returns the html-file of this page. can return null.... check with isInfoPage()
     * 
     * @return
     */
    public VFSLeaf getPageFile() {
        return pageFile;
    }

    public String getFileName() {
        if (pageFile == null) {
            return "";
        }
        return pageFile.getName();
    }

    protected CPResource getResource() {
        CPResource resource = null;
        final CPManager mgr = (CPManager) CoreSpringFactory.getBean(CPManager.class);
        final DefaultElement resElement = mgr.getElementByIdentifier(cp, idRef);
        if (resElement instanceof CPResource) {
            resource = (CPResource) resElement;
        }
        return resource;
    }

    public CPMetadata getMetadata() {
        return metadata;
    }

    /**
     * returns true, if this page represents a chapter page (no linked html-page-resource)
     * 
     * @return
     */
    protected boolean isChapterPage() {
        if (pageFile != null) {
            return false;
        } else {
            return true;
        }
    }

    public void setFile(final VFSLeaf file) {
        this.pageFile = file;
    }

    protected void setRootDir(final VFSContainer rootDir) {
        this.rootDir = rootDir;
    }

    public void setMetadata(final CPMetadata meta) {
        log.info("set Metadata for CPPage: " + this.getTitle());
        this.metadata = meta;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

}
