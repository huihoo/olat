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

import org.apache.log4j.Logger;
import org.dom4j.tree.DefaultElement;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.lms.ims.cp.CPCore;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * This class represents a resources-element of a IMS-manifest-file
 * <P>
 * Initial Date: 26.06.2008 <br>
 * 
 * @author sergio
 */
public class CPResources extends DefaultElement implements CPNode {

    private static final Logger log = LoggerHelper.getLogger();
    private final Vector<CPResource> resources;
    private CPManifest parent;
    private final Vector<String> errors;

    /**
     * this constructor i used when building up the cp (parsing XML manifest)
     * 
     * @param me
     */
    public CPResources(final DefaultElement me) {
        super(me.getName());
        resources = new Vector<CPResource>();
        errors = new Vector<String>();

        setAttributes(me.attributes());
        setContent(me.content());
    }

    /**
     * this constructor is used when creating a new CP
     */
    public CPResources() {
        super(CPCore.RESOURCES);
        resources = new Vector<CPResource>();
        errors = new Vector<String>();
    }

    /**
	 */
    @Override
    public void buildChildren() {
        final Iterator<DefaultElement> children = this.elementIterator();
        // iterate through children
        while (children.hasNext()) {
            final DefaultElement child = children.next();
            if (child.getName().equals(CPCore.RESOURCE)) {
                final CPResource res = new CPResource(child);
                res.setParentElement(this);
                res.buildChildren();
                resources.add(res);
            } else {
                errors.add("Invalid IMS-Manifest (only \"resource\"-elements allowed under <resources> )");
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

    @Override
    public void buildDocument(final DefaultElement parent) {
        final DefaultElement resourceElement = new DefaultElement(CPCore.RESOURCES);

        for (final Iterator<CPResource> itResources = resources.iterator(); itResources.hasNext();) {
            final CPResource res = itResources.next();
            res.buildDocument(resourceElement);
        }
        parent.add(resourceElement);
    }

    // *** CP manipulation ***

    /**
     * Adds a new Resouce - element to the end of the resources-vector
     */
    public void addResource(final CPResource newResource) {
        newResource.setParentElement(this);
        resources.add(newResource);
    }

    /**
     * removes a child-resource from this elements resource-collection
     * 
     * @param id
     *            the identifier of the <resource>-element to remove
     */
    public void removeChild(final String id) {
        try {
            final CPResource res = (CPResource) getElementByIdentifier(id);
            resources.remove(res);
        } catch (final Exception e) {
            log.error("child " + id + " was not removed.", e);
            throw new OLATRuntimeException(CPOrganizations.class, "error while removing child: child-element (<resource>) with identifier \"" + id + "\" not found!",
                    new Exception());
        }
    }

    // ***GETTERS ***

    public Vector<CPResource> getResources() {
        return resources;
    }

    public Iterator<CPResource> getResourceIterator() {
        return resources.iterator();
    }

    /**
     * Returns the Resource with the specified identifier Returns null if Resource is not found
     * 
     * @param identifier
     * @return
     */
    public CPResource getResourceByID(final String identifier) {
        final Iterator<CPResource> it = resources.iterator();
        CPResource res;
        while (it.hasNext()) {
            res = it.next();
            if (res.getIdentifier().equals(identifier)) {
                return res;
            }
        }
        // TODO: should it throw an exception, if no element with the given
        // identifier is found ???
        return null;
    }

    /**
	 */
    @Override
    public DefaultElement getElementByIdentifier(final String id) {
        DefaultElement e;
        for (final Iterator<CPResource> itResources = resources.iterator(); itResources.hasNext();) {
            final CPResource res = itResources.next();
            e = res.getElementByIdentifier(id);
            if (e != null) {
                return e;
            }
        }
        return null;
    }

    @Override
    public int getPosition() {
        // there is only one <resources> element
        return 0;
    }

    public CPManifest getParentElement() {
        return parent;
    }

    public VFSContainer getRootDir() {
        return parent.getRootDir();
    }

    /**
     * returns all dependencies (CPDependency) in a vector
     * 
     * @return
     */
    public Vector<CPDependency> getAllDependencies() {
        final Vector<CPDependency> deps = new Vector<CPDependency>();
        for (final CPResource res : getResources()) {
            deps.addAll(res.getDependencies());
        }
        return deps;
    }

    /**
     * returns all files (CPFile) in a vector
     * 
     * @return
     */
    public Vector<CPFile> getAllFiles() {
        final Vector<CPFile> files = new Vector<CPFile>();
        for (final CPResource res : getResources()) {
            files.addAll(res.getFiles());
        }
        return files;
    }

    // *** SETTERS ***

    @Override
    public void setPosition(final int pos) {
        // there is only one <resources> element
    }

    public void setParentElement(final CPManifest parent) {
        this.parent = parent;
    }
}
