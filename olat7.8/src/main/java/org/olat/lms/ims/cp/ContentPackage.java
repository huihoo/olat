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

import java.io.IOException;

import org.apache.log4j.Logger;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultDocument;
import org.dom4j.tree.DefaultElement;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.lms.ims.cp.objects.CPFile;
import org.olat.lms.ims.cp.objects.CPItem;
import org.olat.lms.ims.cp.objects.CPManifest;
import org.olat.lms.ims.cp.objects.CPOrganization;
import org.olat.lms.ims.cp.objects.CPOrganizations;
import org.olat.lms.ims.cp.objects.CPResource;
import org.olat.system.commons.encoder.Encoder;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * This class represents an IMS Content-Package. Most of the functionality is delegated to cpcore (CPCore)
 * <P>
 * Initial Date: 27.06.2008 <br>
 * 
 * @author Sergio Trentini
 */
public class ContentPackage {

    // Delegate
    private final CPCore cpcore;
    private final OLATResourceable ores;
    private static final Logger log = LoggerHelper.getLogger();

    // *** constructors ***

    ContentPackage(final DefaultDocument doc, final VFSContainer parent, final OLATResourceable ores) {
        this.cpcore = new CPCore(doc, parent);
        this.ores = ores;
    }

    // *** CP manipulation ***

    String addBlankPage(final String title) {
        final CPItem newPage = new CPItem();
        newPage.setTitle(title);
        cpcore.addElement(newPage);
        return newPage.getIdentifier();
    }

    String addBlankPage(final String parentID, final String title) {
        final CPItem newPage = new CPItem();
        newPage.setTitle(title);
        cpcore.addElement(newPage, parentID, 0);
        return newPage.getIdentifier();
    }

    protected void updatePage(final CPPage page) {
        final DefaultElement ele = cpcore.getElementByIdentifier(page.getIdentifier());
        if (ele instanceof CPItem) {
            final CPItem item = (CPItem) ele;
            item.setTitle(page.getTitle());
            item.setMetadata(page.getMetadata());
            final String itemIdentifierRef = item.getIdentifierRef();
            if (itemIdentifierRef == null || itemIdentifierRef.equals("")) {
                // This item has no linked resource yet. Add one if there is a page file
                // attached.
                final VFSLeaf pageFile = page.getPageFile();
                if (pageFile != null) {
                    final CPResource res = new CPResource();
                    final CPFile file = new CPFile(pageFile);
                    res.addFile(file);
                    // TODO:GW Set type according to file
                    res.setType("text/html");
                    res.setHref(file.getHref());
                    item.setIdentifierRef(res.getIdentifier());
                    cpcore.getRootNode().getResources().addResource(res);
                }
            } else {// this item has already a linked resource
                // this is not supported, we don't change linked resources...
            }

        } else if (ele instanceof CPOrganization) {
            final CPOrganization organization = (CPOrganization) ele;
            organization.setTitle(page.getTitle());

        } else {
            // ERROR: this shouldn't be
            throw new OLATRuntimeException("Error while updating manifest with new Page-Data. Invalid identifier " + page.getIdentifier(), null);
        }
    }

    boolean addElement(final DefaultElement newElement, final String parentId, final int position) {
        return cpcore.addElement(newElement, parentId, position);
    }

    boolean addElement(final DefaultElement newElement) {
        cpcore.addElement(newElement);
        return true;
    }

    boolean addElementAfter(final DefaultElement newElement, final String id) {
        return cpcore.addElementAfter(newElement, id);
    }

    void removeElement(final String identifier, final boolean deleteResource) {
        // at the moment, we remove always with resources (if they are not linked
        // in another item)
        cpcore.removeElement(identifier, deleteResource);
    }

    void moveElement(final String nodeID, final String newParentID, final int position) {
        if (nodeID.equals(newParentID)) {
            throw new OLATRuntimeException(CPOrganizations.class, "error while moving element: source and destination are identical...", new Exception());
        }
        cpcore.moveElement(nodeID, newParentID, position);
    }

    String copyElement(final String sourceID, final String targetID) {
        return cpcore.copyElement(sourceID, targetID);
    }

    /**
     * writes the manifest.xml
     */
    void writeToFile() {
        final String filename = "imsmanifest.xml";
        final OutputFormat format = OutputFormat.createPrettyPrint();

        try {
            VFSLeaf outFile;
            // file may exist
            outFile = (VFSLeaf) cpcore.getRootDir().resolve("/" + filename);
            if (outFile == null) {
                // if not, create it
                outFile = cpcore.getRootDir().createChildLeaf("/" + filename);
            }
            final DefaultDocument manifestDocument = cpcore.buildDocument();
            final XMLWriter writer = new XMLWriter(outFile.getOutputStream(false), format);
            writer.write(manifestDocument);
        } catch (final Exception e) {
            log.error("imsmanifest for ores " + ores.getResourceableId() + "couldn't be written to file.", e);
            throw new OLATRuntimeException(CPOrganizations.class, "Error writing imsmanifest-file", new IOException());
        }
    }

    // *** getters ***

    protected boolean isSingleUsedResource(final CPResource res) {
        final int linkCount = cpcore.referencesCount(res);
        return (linkCount < 2);
    }

    public VFSContainer getRootDir() {
        return cpcore.getRootDir();
    }

    protected DefaultDocument getDocument() {
        return cpcore.buildDocument();
    }

    protected CPManifest getRootNode() {
        return cpcore.getRootNode();
    }

    protected DefaultElement getElementByIdentifier(final String identifier) {
        return cpcore.getElementByIdentifier(identifier);
    }

    public CPOrganization getFirstOrganizationInManifest() {
        return cpcore.getFirstOrganizationInManifest();
    }

    /**
     * Return the treeDataModel used for GUI-tree
     * 
     * @return
     */
    protected CPTreeDataModel buildTreeDataModel() {
        final String orgaIdentifier = getFirstOrganizationInManifest().getIdentifier();
        // For the root node id of the ext-js tree we use an md5 hash. This is to
        // make sure that no unwanted characters are handed over to JS.
        final String rootNodeId = Encoder.encrypt(orgaIdentifier);
        final CPTreeDataModel treeData = new CPTreeDataModel(orgaIdentifier, rootNodeId);
        treeData.setContentPackage(this);
        return treeData;
    }

    protected CPPage getFirstPageToDisplay() {
        final CPItem it = cpcore.getFirstPageToDisplay();
        if (it == null) {
            // in case the manifest has no item at all -> use organisation identifyer
            // instead
            return new CPPage(cpcore.getFirstOrganizationInManifest().getIdentifier(), this);
        } else {
            // display the found item
            return new CPPage(it.getIdentifier(), this);
        }
    }

    protected String getPageByItemId(final String itemID) {
        return cpcore.getPageByItemID(itemID);
    }

    protected String getItemTitle(final String itemID) {
        return cpcore.getItemTitle(itemID);
    }

    public String getLastError() {
        return cpcore.getLastError();
    }

    // *** SETTERS ***

    public void setLastError(final String error) {
        cpcore.setLastError(error);
    }

    /**
     * @return Returns a boolean value indicating whether the CP was created with the OLAT CP editor or not.
     */
    public boolean isOLATContentPackage() {
        return cpcore.isOLATContentPackage();
    }

    /**
     * @return Returns the ores.
     */
    public OLATResourceable getResourcable() {
        return ores;
    }

}
