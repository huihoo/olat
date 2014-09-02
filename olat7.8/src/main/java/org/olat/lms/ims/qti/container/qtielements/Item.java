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

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.olat.system.commons.StringHelper;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Initial Date: 25.11.2004
 * 
 * @author Mike Stock
 */
public class Item extends GenericQTIElement {

    private static final Logger log = LoggerHelper.getLogger();

    private final String title;

    /**
     * @param el_item
     */
    public Item(final Element el_item) {
        super(el_item);
        title = el_item.attributeValue("title");
    }

    /**
	 */
    @Override
    public void render(final StringBuilder buffer, final RenderInstructions ri) {
        buffer.append("<div class=\"o_qti_item\">");
        if (((Boolean) ri.get(RenderInstructions.KEY_RENDER_TITLE)).booleanValue()) {
            buffer.append("<h3>").append(StringHelper.escapeHtml(title)).append("</h3>");
        }
        // append dummy iteminput to recognise empty statements
        buffer.append("<input type=\"hidden\" value=\"\" name=\"dummy§").append(getQTIIdent()).append("§xx§xx\" />");
        ri.put(RenderInstructions.KEY_ITEM_IDENT, getQTIIdent());
        GenericQTIElement itemObjectives = null;
        GenericQTIElement itemPresentation = null;
        for (int i = 0; i < getChildCount(); i++) {
            final GenericQTIElement next = (GenericQTIElement) getChildAt(i);
            if (next instanceof Objectives) {
                itemObjectives = next;
            } else if (next instanceof Presentation) {
                itemPresentation = next;
            }
        }
        if (itemObjectives != null) {
            itemObjectives.render(buffer, ri);
        }
        if (itemPresentation != null) {
            itemPresentation.render(buffer, ri);
        }
        buffer.append("</div>");
    }

}
