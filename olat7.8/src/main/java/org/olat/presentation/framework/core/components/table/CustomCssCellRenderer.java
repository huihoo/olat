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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.presentation.framework.core.components.table;

import java.util.Locale;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.system.commons.StringHelper;

/**
 * Description:<br>
 * Render a cell with an custom css class applied. The hover text is optional
 * <P>
 * Initial Date: Nov 13, 2006 <br>
 * 
 * @author guido
 */
public abstract class CustomCssCellRenderer implements CustomCellRenderer {

    @Override
    public void render(final StringOutput sb, final Renderer renderer, final Object val, final Locale locale, final int alignment, final String action) {
        String value = getCellValue(val);
        if (renderer == null) {
            // render for export
            if (!StringHelper.containsNonWhitespace(value)) {
                // try css class
                value = getCssClass(val);
                if (!StringHelper.containsNonWhitespace(value)) {
                    // try title
                    value = getHoverText(val);
                } else {
                    // remove helper css classes
                    if (value != null) {
                        value = value.replace("b_small_icon", "").trim();
                    }
                }
            }
            sb.append(value);
        } else {

            sb.append("<span class=\"");
            sb.append(getCssClass(val));
            String hoverText = getHoverText(val);
            if (StringHelper.containsNonWhitespace(hoverText)) {
                sb.append("\" title=\"");
                sb.append(StringEscapeUtils.escapeHtml(hoverText));
            }
            sb.append("\">");
            sb.append(value);// OLAT-1024: value should be escaped here, but apparently it is escaped in model
            sb.append("</span>");
        }

    }

    protected abstract String getCssClass(Object val);

    protected abstract String getCellValue(Object val);

    protected abstract String getHoverText(Object val);

}
