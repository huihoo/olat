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

package org.olat.lms.ims.qti.container.qtielements;

import org.dom4j.Element;
import org.olat.system.exception.AssertException;

/**
 * Initial Date: 24.11.2004
 * 
 * @author Mike Stock
 */
public class Matimage extends GenericQTIElement {

    /**
     * Comment for <code>xmlClass</code>
     */
    public static final String xmlClass = "matimage";

    String imagetype;
    String uri;
    int width = -1;
    int height = -1;

    /**
     * @param el_matimage
     */
    public Matimage(final Element el_matimage) {
        super(el_matimage);
        imagetype = el_matimage.attributeValue("imagetype");
        uri = el_matimage.attributeValue("uri");
        final String sWidth = el_matimage.attributeValue("width");
        if (sWidth != null && sWidth.length() > 0) {
            try {
                width = Integer.parseInt(sWidth);
            } catch (final NumberFormatException nfe) {
                throw new AssertException("Non-integer value for width.");
            }
        }

        final String sHeight = el_matimage.attributeValue("height");
        if (sHeight != null && sHeight.length() > 0) {
            try {
                height = Integer.parseInt(sHeight);
            } catch (final NumberFormatException nfe) {
                throw new AssertException("Non-integer value for height.");
            }
        }
    }

    /**
	 */
    @Override
    public void render(final StringBuilder buffer, final RenderInstructions ri) {
        buffer.append("<img class=\"o_qti_item_matimage\" src=\"");
        if (!(uri.startsWith("http://") || uri.startsWith("https://"))) {
            buffer.append((String) ri.get(RenderInstructions.KEY_STATICS_PATH));
        }
        buffer.append(uri).append("\"");
        if (width != -1) {
            buffer.append(" width=\"").append(width).append("\"");
        }
        if (height != -1) {
            buffer.append(" height=\"").append(height).append("\"");
        }
        buffer.append(" />");
    }

}
