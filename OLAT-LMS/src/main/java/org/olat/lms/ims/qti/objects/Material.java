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

package org.olat.lms.ims.qti.objects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import org.olat.data.commons.filter.Filter;
import org.olat.data.commons.filter.FilterFactory;
import org.olat.lms.commons.UsedByXstream;
import org.olat.system.commons.CodeHelper;

/**
 * @author rkulow
 */
public class Material implements QTIObject, MatElement, UsedByXstream {

    private String id;
    private String lable = null;
    private List<QTIObject> elements = new ArrayList<QTIObject>();

    public Material() {
        id = "" + CodeHelper.getRAMUniqueID();
    }

    /**
	 */
    @Override
    public void addToElement(final Element root) {
        if (elements.size() == 0) {
            return;
        }
        final Element material = root.addElement("material");
        if (this.lable != null) {
            material.addAttribute("label", this.lable);
        }

        for (final QTIObject tmp : elements) {
            tmp.addToElement(material);
        }
    }

    @Override
    public String renderAsHtml(final String mediaBaseURL) {
        final StringBuilder sb = new StringBuilder();
        for (final Iterator<QTIObject> iter = elements.iterator(); iter.hasNext();) {
            final Object obj = iter.next();
            if (obj instanceof MatElement) {
                sb.append(((MatElement) obj).renderAsHtml(mediaBaseURL));
            }
        }
        if (!mediaBaseURL.equals("")) {
            final Filter urlFilter = FilterFactory.getBaseURLToMediaRelativeURLFilter(mediaBaseURL);
            return urlFilter.filter(sb.toString());
        }
        return sb.toString();
    }

    public String renderAsHtmlForEditor() {
        final String htmlWithToken = renderAsHtml("");
        return htmlWithToken;
    }

    /**
	 */
    @Override
    public String renderAsText() {
        final StringBuilder sb = new StringBuilder();
        for (final Iterator<QTIObject> iter = elements.iterator(); iter.hasNext();) {
            final Object obj = iter.next();
            if (obj instanceof MatElement) {
                sb.append(((MatElement) obj).renderAsText());
            }
        }
        return sb.toString();
    }

    public void add(final QTIObject obj) {
        elements.add(obj);
    }

    public void removeById(final String sId) {
        final Object obj = findById(sId);
        if (obj != null) {
            elements.remove(obj);
        }
    }

    public MatElement findById(final String sId) {
        for (final Iterator<QTIObject> iter = elements.iterator(); iter.hasNext();) {
            final Object obj = iter.next();
            if (!(obj instanceof MatElement)) {
                continue;
            }
            if (((MatElement) obj).getId().equals(sId)) {
                return (MatElement) obj;
            }
        }
        return null;
    }

    /**
     * Returns the elements.
     * 
     * @return List
     */
    public List<QTIObject> getElements() {
        return elements;
    }

    /**
     * Returns the lable.
     * 
     * @return String
     */
    public String getLable() {
        return lable;
    }

    /**
     * Sets the elements.
     * 
     * @param elements
     *            The elements to set
     */
    public void setElements(final List<QTIObject> elements) {
        this.elements = elements;
    }

    /**
     * Sets the lable.
     * 
     * @param lable
     *            The lable to set
     */
    public void setLable(final String lable) {
        this.lable = lable;
    }

    /**
     * @return
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * @param string
     */
    public void setId(final String string) {
        id = string;
    }

}
