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

package org.olat.lms.ims.cp.objects;

import java.util.Iterator;
import java.util.Vector;

import org.dom4j.tree.DefaultElement;
import org.olat.lms.ims.cp.CPCore;
import org.olat.system.exception.OLATRuntimeException;

/**
 * Description:<br>
 * This class represents a organization-element of a IMS-manifest-file
 * <P>
 * Initial Date: 26.06.2008 <br>
 * 
 * @author Sergio Trentini
 */
public class CPOrganization extends DefaultElement implements CPNode {

    private String title;
    private String identifier;
    private final String structure;
    private CPMetadata metadata;
    private int position;
    private CPOrganizations parent;
    private final Vector<CPItem> items;
    private final Vector<String> errors;

    /**
     * this constructor is used when the cp is built (parsing XML manifest file)
     * 
     * @param me
     */
    CPOrganization(final DefaultElement me) {
        super(me.getName());
        items = new Vector<CPItem>();
        errors = new Vector<String>();
        setAttributes(me.attributes());
        setContent(me.content());

        this.identifier = me.attributeValue(CPCore.IDENTIFIER);
        this.structure = me.attributeValue(CPCore.STRUCTURE, "hierarchical");
        // this.title = me.

    }

    /**
	 */
    @Override
    public void buildChildren() {
        final Iterator<DefaultElement> children = this.elementIterator();
        // iterate through children
        while (children.hasNext()) {
            final DefaultElement child = children.next();
            if (child.getName().equals(CPCore.ITEM)) {
                final CPItem item = new CPItem(child, this);
                item.setParentElement(this);
                item.buildChildren();
                item.setPosition(items.size());
                items.add(item);
            } else if (child.getName().equals(CPCore.TITLE)) {
                title = child.getText();
            } else if (child.getName().equals(CPCore.METADATA)) {
                metadata = new CPMetadata(child);
                metadata.setParentElement(this);
            } else {
                errors.add("Invalid IMS-Manifest ( unallowed element under <organization> )");
            }
        }

        this.clearContent();
        validateElement();
    }

    /**
	 */
    @Override
    public boolean validateElement() {
        // nothing to validate
        return true;
    }

    /**
	 */
    @Override
    public void buildDocument(final DefaultElement parent) {

        final DefaultElement orgaElement = new DefaultElement(CPCore.ORGANIZATION);

        orgaElement.addAttribute(CPCore.IDENTIFIER, identifier);
        orgaElement.addAttribute(CPCore.STRUCTURE, structure);

        final DefaultElement titleElement = new DefaultElement(CPCore.TITLE);
        titleElement.setText(title);
        orgaElement.add(titleElement);

        if (metadata != null) {
            metadata.buildDocument(orgaElement);
        }
        for (final Iterator<CPItem> itItem = items.iterator(); itItem.hasNext();) {
            final CPItem item = itItem.next();
            item.buildDocument(orgaElement);
        }
        parent.add(orgaElement);

    }

    // *** CP manipulation ***

    /**
     * adds a new CPItem to the end of the items-vector
     */
    public void addItem(final CPItem newItem) {
        newItem.setParentElement(this);
        items.add(newItem);
    }

    public void addItemAt(final CPItem newItem, final int index) {
        newItem.setParentElement(this);
        if (index > -1 && index <= items.size()) {
            items.add(index, newItem);
            newItem.setPosition(index);
        } else {
            items.add(newItem);
        }
    }

    /**
     * removes this <organization> element from the manifest
     * 
     * @param resourceFlag
     *            indicates whether linked resources should be deleted as well (true-->delete resources too)
     */
    public void removeFromManifest(final boolean resourceFlag) {
        for (final Iterator<CPItem> itItem = items.iterator(); itItem.hasNext();) {
            final CPItem item = itItem.next();
            parent.getParentElement().getCP().removeElement(item.getIdentifier(), resourceFlag);
        }
    }

    /**
     * removes a child <item> from this elements children-collection
     * 
     * @param id
     *            the identifier of the child to remove
     */
    public void removeChild(final String id) {
        boolean removed = false;
        for (final Iterator<CPItem> itItem = items.iterator(); itItem.hasNext();) {
            final CPItem item = itItem.next();
            if (item.getIdentifier().equals(id)) {
                items.remove(item);
                removed = true;
                break;
            }
        }
        if (!removed) {
            throw new OLATRuntimeException(CPOrganizations.class, "error while removing child: child-element with identifier \"" + id + "\" not found!", new Exception());
        }

    }

    // *** getters ***

    /**
     * Returns the Item with the specified identifier Returns null if Item is not found
     * 
     * @param identifier
     *            id
     * @return CPItem or null
     */
    public CPItem getItemByID(final String id) {
        final Iterator<CPItem> it = items.iterator();
        CPItem item;
        while (it.hasNext()) {
            item = it.next();
            if (item.getIdentifier().equals(id)) {
                return item;
            }
        }
        // TODO: should it throw an exception, if no element with the given
        // identifier is found ???
        return null;
    }

    public Vector<CPItem> getItems() {
        return items;
    }

    public Iterator<CPItem> getItemIterator() {
        return items.iterator();
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(final String identifier) {
        this.identifier = identifier;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    String getStructure() {
        return structure;
    }

    /**
	 */
    @Override
    public DefaultElement getElementByIdentifier(final String id) {
        if (identifier.equals(id)) {
            return this;
        }

        DefaultElement e;
        for (final Iterator<CPItem> it = items.iterator(); it.hasNext();) {
            final CPItem item = it.next();
            e = item.getElementByIdentifier(id);
            if (e != null) {
                return e;
            }
        }
        return null;
    }

    @Override
    public int getPosition() {
        return position;
    }

    /**
     * returns a vector that holds all items in this organization-element (recursively, all ! )
     * 
     * @return vector
     */
    public Vector<CPItem> getAllItems() {
        final Vector<CPItem> allItems = new Vector<CPItem>();
        for (final Iterator<CPItem> itItem = items.iterator(); itItem.hasNext();) {
            final CPItem item = itItem.next();
            allItems.add(item);
            allItems.addAll(item.getAllItems());
        }
        // System.out.println("item count: "+allItems.size());
        return allItems;
    }

    /**
     * Returns the first Item. If this organization has no child-items, null is returned.
     * 
     * @return
     */
    public CPItem getFirstItem() {
        if (items.size() > 0) {
            return items.firstElement();
        }
        return null;
    }

    /**
     * returns the parent element
     * 
     * @return
     */
    CPOrganizations getParentElement() {
        return parent;
    }

    /**
     * @return
     */
    String getLastError() {
        for (final CPItem item : getAllItems()) {
            final String err = item.getLastError();
            if (err != null) {
                return err;
            }
        }
        return null;
    }

    // *** SETTERS ***

    @Override
    public void setPosition(final int pos) {
        position = pos;
    }

    public void setParentElement(final CPOrganizations parent) {
        this.parent = parent;
    }

    public void setMetadata(final CPMetadata md) {
        metadata = md;
    }

}
