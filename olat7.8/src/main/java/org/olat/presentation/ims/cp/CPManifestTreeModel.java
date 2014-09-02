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

package org.olat.presentation.ims.cp;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.xml.XMLParser;
import org.olat.lms.ims.resources.IMSEntityResolver;
import org.olat.presentation.framework.core.components.tree.GenericTreeModel;
import org.olat.presentation.framework.core.components.tree.GenericTreeNode;
import org.olat.presentation.framework.core.components.tree.TreeNode;
import org.olat.system.exception.AssertException;
import org.olat.system.exception.OLATRuntimeException;

/**
 * Description:<br>
 * 
 * @author Felix Jost
 */
public class CPManifestTreeModel extends GenericTreeModel {

    private Element rootElement;
    private final Map nsuris = new HashMap(2);
    private final Map hrefToTreeNode = new HashMap();
    private Map resources; // keys: resource att 'identifier'; values: resource att 'href'

    /**
     * Constructor of the content packaging tree model
     * 
     * @param manifest
     *            the imsmanifest.xml file
     */
    public CPManifestTreeModel(final VFSLeaf manifest) {
        final Document doc = loadDocument(manifest);
        // get all organization elements. need to set namespace
        rootElement = doc.getRootElement();
        final String nsuri = rootElement.getNamespace().getURI();
        nsuris.put("ns", nsuri);

        final XPath meta = rootElement.createXPath("//ns:organization");
        meta.setNamespaceURIs(nsuris);
        final Element orgaEl = (Element) meta.selectSingleNode(rootElement); // TODO: accept several organizations?
        if (orgaEl == null) {
            throw new AssertException("could not find element organization");
        }

        final XPath metares = rootElement.createXPath("//ns:resources");
        metares.setNamespaceURIs(nsuris);
        final Element elResources = (Element) metares.selectSingleNode(rootElement);
        if (elResources == null) {
            throw new AssertException("could not find element resources");
        }

        final List resourcesList = elResources.elements("resource");
        resources = new HashMap(resourcesList.size());
        for (final Iterator iter = resourcesList.iterator(); iter.hasNext();) {
            final Element elRes = (Element) iter.next();
            final String identVal = elRes.attributeValue("identifier");
            String hrefVal = elRes.attributeValue("href");
            if (hrefVal != null) { // href is optional element for resource element
                try {
                    hrefVal = URLDecoder.decode(hrefVal, "UTF-8");
                } catch (final UnsupportedEncodingException e) {
                    // each JVM must implement UTF-8
                }
            }
            resources.put(identVal, hrefVal);
        }
        final GenericTreeNode gtn = buildNode(orgaEl);
        setRootNode(gtn);
        rootElement = null; // help gc
        resources = null;
    }

    /**
     * @param href
     * @return TreeNode representing this href
     */
    public TreeNode lookupTreeNodeByHref(final String href) {
        return (TreeNode) hrefToTreeNode.get(href);
    }

    private GenericTreeNode buildNode(final Element item) {
        final GenericTreeNode gtn = new GenericTreeNode();

        // extract title
        String title = item.elementText("title");
        if (title == null) {
            title = item.attributeValue("identifier");
        }
        gtn.setAltText(title);
        gtn.setTitle(title);

        if (item.getName().equals("organization")) {
            gtn.setIconCssClass("o_cp_org");
            gtn.setAccessible(false);
        } else if (item.getName().equals("item")) {
            gtn.setIconCssClass("o_cp_item");
            // set resolved file path directly
            final String identifierref = item.attributeValue("identifierref");
            final XPath meta = rootElement.createXPath("//ns:resource[@identifier='" + identifierref + "']");
            meta.setNamespaceURIs(nsuris);
            final String href = (String) resources.get(identifierref);
            if (href != null) {
                gtn.setUserObject(href);
                // allow lookup of a treenode given a href so we can quickly adjust the menu if the user clicks on hyperlinks within the text
                hrefToTreeNode.put(href, gtn);
            } else {
                gtn.setAccessible(false);
            }
        }

        final List chds = item.elements("item");
        final int childcnt = chds.size();
        for (int i = 0; i < childcnt; i++) {
            final Element childitem = (Element) chds.get(i);
            final GenericTreeNode gtnchild = buildNode(childitem);
            gtn.addChild(gtnchild);
        }
        return gtn;
    }

    private Document loadDocument(final VFSLeaf documentF) {
        InputStream in = null;
        Document doc = null;
        try {
            in = documentF.getInputStream();
            final XMLParser xmlParser = new XMLParser(new IMSEntityResolver());
            doc = xmlParser.parse(in, false);
            in.close();
        } catch (final IOException e) {
            throw new OLATRuntimeException(CPManifestTreeModel.class, "could not read and parse from file " + documentF, e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (final Exception e) {
                // we did our best to close the inputStream
            }
        }
        return doc;
    }

}
