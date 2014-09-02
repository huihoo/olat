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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.presentation.framework.core.components.download;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.ComponentRenderer;
import org.olat.presentation.framework.core.control.winmgr.AJAXFlags;
import org.olat.presentation.framework.core.render.RenderResult;
import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.render.RenderingState;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.presentation.framework.core.render.URLBuilder;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.StringHelper;

/**
 * Description:<br>
 * This is the renderer for the DownloadComponent. The first optional render argument is interpreted as a CSS class that will be added in addition to the icon css class
 * of the component (if set).
 * <P>
 * Initial Date: 09.12.2009 <br>
 * 
 * @author gnaegi
 */
public class DownloadComponentRenderer implements ComponentRenderer {

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.URLBuilder, org.olat.presentation.framework.translator.Translator,
     * org.olat.presentation.framework.render.RenderResult, java.lang.String[])
     */
    @Override
    public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
        DownloadComponent comp = (DownloadComponent) source;
        if (comp.getDownloadMediaResoruce() == null)
            return;

        sb.append("<a href=\"");
        ubu.buildURI(sb, null, null, AJAXFlags.MODE_NORMAL); // rendered in new window anyway
        sb.append("\"");
        // Icon css class
        String iconCssClass = comp.getLinkCssIconClass();
        String cssArg = (args != null && args.length > 1 ? args[0] : null); // optional render argument
        if (iconCssClass != null || cssArg != null) {
            sb.append(" class=\"");
            if (iconCssClass != null) {
                sb.append("b_with_small_icon_left ");
                sb.append(iconCssClass);
                sb.append(" ");
            }
            if (cssArg != null) {
                sb.append(cssArg);
            }
            sb.append("\"");
        }
        // Tooltip
        String tip = comp.getLinkToolTip();
        if (tip != null) {
            sb.append(" ext:qtip=\"");
            sb.append(StringEscapeUtils.escapeHtml(tip));
            sb.append("\"");
        }
        sb.append(" target=\"_blank\">");
        // Link Text
        String text = comp.getLinkText();
        if (text != null) {
            sb.append(StringHelper.escapeHtml(text));
        }
        sb.append("</a>");

    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.RenderingState)
     */
    @Override
    public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
        // nothing to render
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.URLBuilder, org.olat.presentation.framework.translator.Translator,
     * org.olat.presentation.framework.render.RenderingState)
     */
    @Override
    public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderingState rstate) {
        // nothing to render
    }

}
