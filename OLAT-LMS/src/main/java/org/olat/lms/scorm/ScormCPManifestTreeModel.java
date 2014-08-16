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

package org.olat.lms.scorm;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.olat.data.commons.xml.XMLParser;
import org.olat.lms.ims.resources.IMSEntityResolver;
import org.olat.presentation.framework.core.components.tree.GenericTreeModel;
import org.olat.presentation.framework.core.components.tree.GenericTreeNode;
import org.olat.presentation.framework.core.components.tree.TreeNode;
import org.olat.system.exception.AssertException;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.spring.CoreSpringFactory;

/**
 * @author Felix Jost
 */
public class ScormCPManifestTreeModel extends GenericTreeModel {

    private Element rootElement;
    private final Map nsuris = new HashMap(2);
    private final Map hrefToTreeNode = new HashMap();
    private Map resources; // keys: resource att 'identifier'; values: resource att 'href'
    private int nodeId = 0;
    // number tree ascending in depth first traversal. Keys: GenericTreeNode | values: int
    private final Map nodeToId = new HashMap();
    private final Map scormIdToNode = new HashMap();
    private final Map itemStatus;

    /**
     * Constructor of the content packaging tree model
     * 
     * @param scormFolderPath
     *            the imsmanifest.xml file
     * @param itemStatus
     *            a Map containing the status of each item like "completed, not attempted, ..."
     */
    public ScormCPManifestTreeModel(final String scormFolderPath, final Map itemStatus) {
        ScormEBL scormEbl = CoreSpringFactory.getBean(ScormEBL.class);
        this.itemStatus = itemStatus;
        final Document doc = loadDocument(scormEbl.getManifestFile(scormFolderPath));
        // get all organization elements. need to set namespace
        rootElement = doc.getRootElement();
        final String nsuri = rootElement.getNamespace().getURI();
        nsuris.put("ns", nsuri);

        final XPath meta = rootElement.createXPath("//ns:organization");
        meta.setNamespaceURIs(nsuris);

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
        /*
         * Get all organizations
         */
        List organizations = new LinkedList();
        organizations = meta.selectNodes(rootElement);
        if (organizations.isEmpty()) {
            throw new AssertException("could not find element organization");
        }
        final GenericTreeNode gtn = buildTreeNodes(organizations);
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

    private GenericTreeNode buildTreeNodes(final List organizations) {
        final GenericTreeNode gtn = new GenericTreeNode();
        // 0 is a valid index since List is testet be be not empty above
        final String rootNode = ((Element) organizations.get(0)).getParent().elementText("default");
        // if only one organization avoid too much hierarchy levels...
        if (organizations.size() == 1) {
            return buildNode((Element) organizations.get(0));
        }
        // FIXME: localize "Content:"
        gtn.setTitle((rootNode == null) ? "Content:" : rootNode);
        gtn.setIconCssClass("o_scorm_org");
        gtn.setAccessible(false);

        for (int i = 0; i < organizations.size(); ++i) {
            final GenericTreeNode gtnchild = buildNode((Element) organizations.get(i));
            gtn.addChild(gtnchild);
        }
        return gtn;
    }

    private GenericTreeNode buildNode(final Element item) {
        final GenericTreeNode treeNode = new GenericTreeNode();

        // extract title
        String title = item.elementText("title");
        if (title == null) {
            title = item.attributeValue("identifier");
        }
        treeNode.setAltText(title);
        treeNode.setTitle(title);

        if (item.getName().equals("organization")) {
            treeNode.setIconCssClass("o_scorm_org");
            treeNode.setAccessible(false);
        } else if (item.getName().equals("item")) {
            scormIdToNode.put(new Integer(nodeId).toString(), treeNode);
            nodeToId.put(treeNode, new Integer(nodeId));

            // set node images according to scorm sco status
            final String itemStatusDesc = (String) itemStatus.get(Integer.toString(nodeId));
            treeNode.setIconCssClass("o_scorm_item");
            if (itemStatusDesc != null) {
                // add icon decorator for current status
                treeNode.setIconDecorator1CssClass("o_scorm_" + itemStatusDesc);
            }

            nodeId++;
            // set resolved file path directly
            final String identifierref = item.attributeValue("identifierref");
            final XPath meta = rootElement.createXPath("//ns:resource[@identifier='" + identifierref + "']");
            meta.setNamespaceURIs(nsuris);
            final String href = (String) resources.get(identifierref);
            if (href != null) {
                treeNode.setUserObject(href);
                // allow lookup of a treenode given a href so we can quickly adjust the menu if the user clicks on hyperlinks within the text
                hrefToTreeNode.put(href, treeNode);
            } else {
                treeNode.setAccessible(false);
            }
        }

        final List chds = item.elements("item");
        final int childcnt = chds.size();
        for (int i = 0; i < childcnt; i++) {
            final Element childitem = (Element) chds.get(i);
            final GenericTreeNode gtnchild = buildNode(childitem);
            treeNode.addChild(gtnchild);
        }
        return treeNode;
    }

    private Document loadDocument(final File documentF) {
        FileInputStream in = null;
        BufferedInputStream bis = null;
        Document doc = null;
        try {
            in = new FileInputStream(documentF);
            bis = new BufferedInputStream(in);
            final XMLParser xmlParser = new XMLParser(new IMSEntityResolver());
            doc = xmlParser.parse(bis, false);
        } catch (final IOException e) {
            throw new OLATRuntimeException(ScormCPManifestTreeModel.class, "could not read and parse from file " + documentF.getAbsolutePath(), e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (bis != null) {
                    bis.close();
                }
            } catch (final Exception e) {
                // we did our best to close the inputStream
            }
        }
        return doc;
    }

    /**
     * @param treeNode
     * @return int
     */
    public int lookupScormNodeId(final TreeNode treeNode) {
        final Integer nodeInteger = (Integer) nodeToId.get(treeNode);
        return nodeInteger.intValue();
    }

    /**
     * @param itemId
     * @return an uri that points to the ressource identified by a flat id
     */
    public TreeNode getNodeByScormItemId(final String itemId) {
        final TreeNode node = (TreeNode) scormIdToNode.get(itemId);
        return node;
    }

    /**
     * @return a map with key->ascending flat string number from traversing tree. value->TreeNode
     */
    public Map getScormIdToNodeRelation() {
        return scormIdToNode;
    }

}
