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
package org.olat.presentation.framework.core.components.text;

import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.ComponentRenderer;
import org.olat.presentation.framework.core.render.RenderResult;
import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.render.RenderingState;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.presentation.framework.core.render.URLBuilder;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.StringHelper;

/**
 * Description:<br>
 * This renderer renders a simple text component either as span or div tag. Optionally a CSS class is added.
 * <p>
 * When the text component returns a NULL value, nothing will be rendered at all. An empty string will render an empty span or div tag.
 * <P>
 * Initial Date: 10.11.2009 <br>
 * 
 * @author gnaegi
 */
class TextComponentRenderer implements ComponentRenderer {

    /**
     * Default constructor
     */
    TextComponentRenderer() {
        // Constructor is only package scope
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.URLBuilder, org.olat.presentation.framework.translator.Translator,
     * org.olat.presentation.framework.render.RenderResult, java.lang.String[])
     */
    @Override
    public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
        TextComponent comp = (TextComponent) source;
        String text = comp.getDisplayText();
        if (text != null) {
            // Add a wrapper with a CSS class if necessary
            String cssClass = comp.getCssClass();
            String tag = comp.getSpanAsDomReplaceable() ? "span" : "div";
            // In any case render a span or div. If in ajax mode, another span/div
            // will be wrapped around this to identify the component.
            sb.append("<");
            sb.append(tag);
            // Add optional css class
            if (cssClass != null) {
                sb.append(" class=\"");
                sb.append(cssClass);
                sb.append("\"");
            }
            sb.append(">");
            sb.append(StringHelper.escapeHtml(text));
            sb.append("</");
            sb.append(tag);
            sb.append(">");
        }
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
