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

import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.dom4j.tree.DefaultDocument;
import org.dom4j.tree.DefaultElement;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.lms.ims.cp.objects.CPDependency;
import org.olat.lms.ims.cp.objects.CPFile;
import org.olat.lms.ims.cp.objects.CPItem;
import org.olat.lms.ims.cp.objects.CPManifest;
import org.olat.lms.ims.cp.objects.CPMetadata;
import org.olat.lms.ims.cp.objects.CPOrganization;
import org.olat.lms.ims.cp.objects.CPOrganizations;
import org.olat.lms.ims.cp.objects.CPResource;
import org.olat.lms.ims.cp.objects.CPResources;
import org.olat.lms.wiki.WikiToCPExport;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * This class provides basic functionality for a IMS Content Package
 * <P>
 * Initial Date: 27.06.2008 <br>
 * 
 * @author Sergio Trentini
 */
public class CPCore {

    private static final Logger log = LoggerHelper.getLogger();

    /**
     * The CP Manifest name
     */
    public static final String MANIFEST_NAME = "imsmanifest.xml";

    // Element and Attribute Names
    public static final String MANIFEST = "manifest";
    public static final String ORGANIZATIONS = "organizations";
    public static final String RESOURCES = "resources";
    public static final String DEFAULT = "default";
    public static final String ORGANIZATION = "organization";
    public static final String ITEM = "item";
    public static final String PARAMETERS = "parameters";
    public static final String RESOURCE = "resource";
    public static final String BASE = "base";
    public static final String FILE = "file";
    public static final String TYPE = "type";
    public static final String HREF = "href";
    public static final String METADATA = "metadata";
    public static final String IDENTIFIER = "identifier";
    public static final String IDENTIFIERREF = "identifierref";
    public static final String STRUCTURE = "structure";
    public static final String TITLE = "title";
    public static final String DEPENDENCY = "dependency";
    public static final String VERSION = "version";
    public static final String SCHEMA = "schema";
    public static final String SCHEMALOCATION = "schemaLocation";
    public static final String SCHEMAVERSION = "schemaversion";
    public static final String ISVISIBLE = "isvisible";
    public static final String OLAT_MANIFEST_IDENTIFIER = "olat_ims_cp_editor_v1";
    public static final String OLAT_ORGANIZATION_IDENTIFIER = "TOC";

    private final DefaultDocument doc;
    private final VFSContainer rootDir;
    private CPManifest rootNode;

    private final Vector<String> errors;

    public CPCore(final DefaultDocument doc, final VFSContainer rootDir) {
        this.doc = doc;
        this.rootDir = rootDir;
        errors = new Vector<String>();
        buildTree();
    }

    /**
     * parses the document, builds manifest-datamodel-tree-structure
     */
    public void buildTree() {
        if (doc != null) {
            rootNode = new CPManifest(this, (DefaultElement) doc.getRootElement());
            rootNode.buildChildren();
        }
    }

    /**
     * this is case-sensitive!
     * 
     * @param identifier
     * @return an Element by its IDENTIFIER attribute starting at the manifests root element This will do a deep recursive search
     */
    public DefaultElement getElementByIdentifier(final String identifier) {
        return rootNode.getElementByIdentifier(identifier);
    }

    // *** CP manipulation ***

    /**
     * adds an element as a child to the element with id parentId if the element with parentId is not found, it returns false if adding was successful, it returns true
     */
    public boolean addElement(final DefaultElement newElement, final String parentId, final int position) {

        final DefaultElement parentElement = rootNode.getElementByIdentifier(parentId);
        if (parentElement == null) {
            throw new OLATRuntimeException(CPOrganizations.class, "Parent-element with identifier:\"" + parentId + "\" not found!", new Exception());
        }

        if (parentElement instanceof CPItem) {
            // parent is a <item>
            if (newElement instanceof CPItem) {
                // only CPItems can be added to CPItems
                final CPItem item = (CPItem) parentElement;
                item.addItemAt((CPItem) newElement, position);
                return true;
            } else {
                throw new OLATRuntimeException(CPOrganizations.class, "you can only add <item>  elements to an <item>-element", new Exception());
            }

        } else if (parentElement instanceof CPOrganization) {
            // parent is a <organization>
            if (newElement instanceof CPItem) {
                // add a new item to organization element
                final CPOrganization org = (CPOrganization) parentElement;
                org.addItemAt((CPItem) newElement, position);
                return true;
            } else {
                throw new OLATRuntimeException(CPOrganizations.class, "you can only add <item>  elements to an <organization>-element", new Exception());
            }

        } else if (parentElement instanceof CPResource) {
            // parent is a <resource>
            final CPResource resource = (CPResource) parentElement;
            if (newElement instanceof CPFile) {
                resource.addFile((CPFile) newElement);
            } else if (newElement instanceof CPDependency) {
                resource.addDependency((CPDependency) newElement);
            } else {
                throw new OLATRuntimeException(CPOrganizations.class, "you can only add <dependency> or <file> elements to a Resource", new Exception());
            }
            return true;
        } else if (parentElement instanceof CPResources) {
            // parent is <resources> !!see the "s" at the end ;)
            if (newElement instanceof CPResource) {
                final CPResources resources = (CPResources) parentElement;
                resources.addResource((CPResource) newElement);
                return true;
            } else {
                throw new OLATRuntimeException(CPOrganizations.class, "you can only add <resource>elements to the <resources> element", new Exception());
            }
        }

        return false;
    }

    /**
     * adds an element to the CP. Only accepts <resource> and <organization> elements
     * 
     * @param newElement
     * @return
     */
    public void addElement(final DefaultElement newElement) {

        if (newElement instanceof CPResource) {
            rootNode.getResources().addResource((CPResource) newElement);
        } else if (newElement instanceof CPOrganization) {
            rootNode.getOrganizations().addOrganization((CPOrganization) newElement);
        } else if (newElement instanceof CPItem) {
            if (rootNode.getOrganizations().getOrganizations().size() > 0) {
                rootNode.getOrganizations().getOrganizations().elementAt(0).addItem((CPItem) newElement);
            }
        } else {
            throw new OLATRuntimeException(CPOrganizations.class, "invalid newElement for adding to manifest", new Exception());
        }

    }

    /**
     * adds an element to the cp. Adds it after the item with identifier "id"
     * 
     * @param newElement
     * @param id
     * @return
     */
    public boolean addElementAfter(final DefaultElement newElement, final String id) {
        final DefaultElement beforeElement = rootNode.getElementByIdentifier(id);

        if (beforeElement == null) {
            return false;
        }

        if (beforeElement instanceof CPItem) {
            // beforeElement is a <item>
            // ==> newElement has to be an <item>
            final CPItem beforeItem = (CPItem) beforeElement;
            final DefaultElement parent = beforeItem.getParentElement();
            if (!(newElement instanceof CPItem)) {
                throw new OLATRuntimeException(CPOrganizations.class, "only <item> element allowed", new Exception());
            }
            if (parent instanceof CPItem) {
                final CPItem p = (CPItem) parent;
                p.addItemAt((CPItem) newElement, beforeItem.getPosition() + 1);
            } else if (parent instanceof CPOrganization) {
                final CPOrganization o = (CPOrganization) parent;
                o.addItemAt((CPItem) newElement, beforeItem.getPosition() + 1);
            } else {
                throw new OLATRuntimeException(CPOrganizations.class, "you cannot add an <item> element to a " + parent.getName() + " element", new Exception());
            }

        }

        return true;
    }

    /**
     * removes an element with identifier "identifier" from the manifest
     * 
     * @param identifier
     *            the identifier if the element to remove
     * @param booleanFlag
     *            indicates whether to remove linked resources as well...! (needed for moving elements)
     */
    public void removeElement(final String identifier, final boolean resourceFlag) {

        final DefaultElement el = rootNode.getElementByIdentifier(identifier);
        if (el != null) {
            if (el instanceof CPItem) {
                // element is CPItem
                final CPItem item = (CPItem) el;

                // first remove resources
                if (resourceFlag) {
                    // Delete children (depth first search)
                    removeChildElements(item, resourceFlag);

                    // remove referenced resource
                    final CPResource res = (CPResource) rootNode.getElementByIdentifier(item.getIdentifierRef());
                    if (res != null && referencesCount(res) == 1) {
                        res.removeFromManifest();
                    }
                }
                // then remove item
                item.removeFromManifest();

            } else if (el instanceof CPOrganization) {
                // element is organization
                final CPOrganization org = (CPOrganization) el;
                org.removeFromManifest(resourceFlag);
            } else if (el instanceof CPMetadata) {
                // element is <metadata>
                final CPMetadata md = (CPMetadata) el;
                md.removeFromManifest();
            }
        } else {
            throw new OLATRuntimeException(CPOrganizations.class, "couldn't remove element with id \"" + identifier + "\"! Element not found in manifest ",
                    new Exception());

        }
    }

    /**
     * Deletes all children of the element specified by the identifier
     * 
     * @param identifier
     * @param deleteResource
     */
    void removeChildElements(final CPItem item, final boolean deleteResource) {
        if (item != null) {
            for (final String childIdentifier : item.getItemIdentifiers()) {
                removeElement(childIdentifier, deleteResource);
            }
        }
    }

    /**
     * Checks how many item-elements link to the given resource element.
     * 
     * @param resource
     * @return
     */
    protected int referencesCount(final CPResource resource) {

        int linkCount = 0;
        final Vector<CPItem> items = new Vector<CPItem>();
        for (final Iterator<CPOrganization> it = rootNode.getOrganizations().getOrganizationIterator(); it.hasNext();) {
            final CPOrganization org = it.next();
            items.addAll(org.getAllItems());
        }

        for (final CPItem item : items) {
            if (item.getIdentifierRef().equals(resource.getIdentifier())) {
                linkCount++;
            }
        }

        final Vector<CPDependency> dependencies = rootNode.getResources().getAllDependencies();
        for (final CPDependency dependency : dependencies) {
            if (dependency.getIdentifierRef().equals(resource.getIdentifier())) {
                linkCount++;
            }
        }

        return linkCount;
    }

    public void moveElement(final String nodeID, final String newParentID, final int position) {
        final DefaultElement elementToMove = rootNode.getElementByIdentifier(nodeID);
        if (elementToMove != null) {
            if (elementToMove instanceof CPItem) {
                removeElement(nodeID, false);
                addElement(elementToMove, newParentID, position);
            } else if (elementToMove instanceof CPOrganization) {
                // not yet supported
            } else {
                throw new OLATRuntimeException(CPOrganizations.class, "Only <item>-elements are moveable...!", new Exception());
            }
        }
    }

    /**
     * duplicates an element and inserts it after targetID
     * 
     * @param sourceID
     * @param targetID
     */
    public String copyElement(final String sourceID, final String targetID) {
        final DefaultElement elementToCopy = rootNode.getElementByIdentifier(sourceID);
        if (elementToCopy == null) {
            throw new OLATRuntimeException(CPOrganizations.class, "element with identifier \"" + sourceID + "\" not found..!", new Exception());
        }

        if (elementToCopy instanceof CPItem) {
            final CPItem newItem = (CPItem) elementToCopy.clone();
            cloneResourceOfItemAndSubitems(newItem);
            addElementAfter(newItem, targetID);
            return newItem.getIdentifier();
        } else {
            // if (elementToCopy.getClass().equals(CPOrganization.class)) {
            // not yet supported
            throw new OLATRuntimeException(CPOrganizations.class, "You can only copy <item>-elements...!", new Exception());
        }

    }

    /**
     * Clones all editable resources of the item and its subitems.
     * 
     * @param item
     */
    private void cloneResourceOfItemAndSubitems(final CPItem item) {
        cloneResourceOfItem(item);
        for (final CPItem child : item.getItems()) {
            cloneResourceOfItemAndSubitems(child);
        }
    }

    /**
     * Clones the resource of an item. If the resource is not editable, i.e. it is not an html, Word or Excel file, there's no need to clone it and nothing will be done.
     * Editable resources are cloned and the single referenced file is copied.
     * 
     * @param item
     */
    private void cloneResourceOfItem(final CPItem item) {
        final DefaultElement ref = getElementByIdentifier(item.getIdentifierRef());
        if (ref != null && ref instanceof CPResource) {
            final CPResource resource = (CPResource) ref;
            // Clone the resource if the linked file is editable (i.e. it is an html,
            // Word or Excel file)
            final String href = resource.getFullHref();
            if (href != null) {
                final String extension = href.substring(href.lastIndexOf(".") + 1);
                if ("htm".equals(extension) || "html".equals(extension) || "doc".equals(extension) || "xls".equals(extension)) {
                    final CPResource clonedResource = (CPResource) resource.clone();
                    addElement(clonedResource);
                    item.setIdentifierRef(clonedResource.getIdentifier());
                }
            }
        }
    }

    /**
     * searches for the next, not already-used "(copy n)" string as identifier
     * 
     * @param identifier
     * @return the new identifier as String
     * @deprecated use auto-generated id CodeHelper.getGlobalForeverUniqueID()
     */
    @Deprecated
    private String _getNextCopyID(final String identifier) {
        // FIXME: i18n
        int n = 1;
        DefaultElement e = rootNode.getElementByIdentifier(identifier + " (Copy " + n + ")");
        while (e != null) {
            n++;
            e = rootNode.getElementByIdentifier(identifier + " (Copy " + n + ")");
        }
        return identifier + " (Copy " + n + ")";
    }

    /**
     * Searches for <item>-elements or <dependency>-elements which references to the resource with id "resourceIdentifier" if an element is found, search is aborted and
     * the found element is returned
     * 
     * @param resourceIdentifier
     * @return the found element or null
     */
    public DefaultElement findReferencesToResource(final String resourceIdentifier) {

        // search for <item identifierref="resourceIdentifier" >
        for (final Iterator<CPOrganization> it = rootNode.getOrganizations().getOrganizationIterator(); it.hasNext();) {
            final CPOrganization org = it.next();
            for (final Iterator<CPItem> itO = org.getItems().iterator(); itO.hasNext();) {
                final CPItem item = itO.next();
                final CPItem found = _findReferencesToRes(item, resourceIdentifier);
                if (found != null) {
                    return found;
                }
            }
        }

        // search for <dependency identifierref="resourceIdentifier" >
        for (final Iterator<CPResource> itRes = rootNode.getResources().getResourceIterator(); itRes.hasNext();) {
            final CPResource res = itRes.next();
            for (final Iterator<CPDependency> itDep = res.getDependencyIterator(); itDep.hasNext();) {
                final CPDependency dep = itDep.next();
                if (dep.getIdentifierRef().equals(resourceIdentifier)) {
                    return dep;
                }
            }
        }

        return null;
    }

    /**
     * searches recursively for <item>-elements with identifierRef "id" in the children-collection of the item "item"
     * 
     * @param item
     * @param id
     * @return
     */
    private CPItem _findReferencesToRes(final CPItem item, final String id) {
        if (item.getIdentifierRef().equals(id)) {
            return item;
        }
        for (final Iterator<CPItem> itO = item.getItems().iterator(); itO.hasNext();) {
            final CPItem it = itO.next();
            final CPItem found = _findReferencesToRes(it, id);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    // *** getters ***

    /**
     * Returns the rootNode of the manifest
     * 
     * @return CPManifest
     */
    public CPManifest getRootNode() {
        return rootNode;
    }

    public VFSContainer getRootDir() {
        return rootDir;
    }

    /**
     * Returns the DefaultDocument of this CP
     * 
     * @return the xml Document of this CP
     */
    public DefaultDocument buildDocument() {
        // if (doc != null) return doc;
        final DefaultDocument newDoc = new DefaultDocument();
        rootNode.buildDocument(newDoc);
        return newDoc;
    }

    /**
     * returns the first <organization> element of this manifest Note: IMS standard allows multiple <organization>-elements
     * 
     * @return
     */
    public CPOrganization getFirstOrganizationInManifest() {
        final Vector<CPOrganization> orgas = rootNode.getOrganizations().getOrganizations();
        // integrity check already done, there is at least one <organization> at
        // this moment
        return orgas.firstElement();
    }

    /**
     * Gets the linked page for the <item> element with given id if no resource (page) is referenced, null is returned
     * 
     * @param id
     * @return
     */
    public String getPageByItemID(final String id) {
        final DefaultElement ele = getElementByIdentifier(id);
        if (ele instanceof CPItem) {
            final CPItem item = (CPItem) ele;
            if (item.getIdentifierRef() == null || item.getIdentifierRef().equals("")) {
                return null;
            }
            final DefaultElement resElement = getElementByIdentifier(item.getIdentifierRef());
            if (resElement instanceof CPResource) {
                final CPResource res = (CPResource) resElement;
                return res.getFullHref();
            } else {
                log.info("method: getPageByItemID(" + id + ") :  invalid manifest.. identifierred of <item> must point to a <resource>-element");
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Returns the first page within the given organization returns null if no page found (empty organization)
     * 
     * @return
     */
    public CPItem getFirstPageToDisplay() {
        final CPOrganization orga = getFirstOrganizationInManifest();
        return orga.getFirstItem();
    }

    /**
     * returns the item of an <item> element (with given identifier) in the manifest
     * 
     * @param itemID
     *            the identifier of the item
     * @return returns the title. returns null if element is not found, or element is not an <item>
     */
    public String getItemTitle(final String itemID) {
        final DefaultElement ele = getElementByIdentifier(itemID);
        if (ele == null) {
            return null;
        }
        if (ele instanceof CPItem) {
            final CPItem item = (CPItem) ele;
            return item.getTitle();
        } else {
            return null;
        }

    }

    /**
     * Returns the last error of this ContentPackage (after building it.. ) returns null, if no error occurred..
     * 
     * @return
     */
    String getLastError() {
        if (errors.size() == 0) {
            return rootNode.getLastError();
        }
        return errors.firstElement();
    }

    protected void setLastError(final String err) {
        errors.add(err);
    }

    /**
     * @return Returns a true if the CP was created with the OLAT CP editor or exported from an OLAT wiki. False otherwise.
     */
    public boolean isOLATContentPackage() {
        boolean isOLATCP = false;
        final String identifier = rootNode.getIdentifier();
        isOLATCP = OLAT_MANIFEST_IDENTIFIER.equals(identifier);
        isOLATCP |= WikiToCPExport.WIKI_MANIFEST_IDENTIFIER.equals(identifier);
        return isOLATCP;
    }

}
