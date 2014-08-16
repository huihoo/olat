package org.olat.presentation.framework.core.components.form.flexible.impl.elements.table;

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
 * Copyright (c) 2008 frentix GmbH, Switzerland<br>
 * <p>
 */

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.render.RenderResult;
import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.render.RenderingState;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.presentation.framework.core.render.URLBuilder;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.StringHelper;

/**
 * <h3>Description:</h3> Render a cell with an custom css class applied. The hover text is optional
 * <p>
 * Initial Date: 21.03.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public abstract class CSSIconFlexiCellRenderer implements FlexiCellRenderer {
    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.URLBuilder, org.olat.presentation.framework.translator.Translator,
     * org.olat.presentation.framework.render.RenderResult, java.lang.String[])
     */
    @Override
    @SuppressWarnings("unused")
    public void render(Renderer renderer, StringOutput target, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {

    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.RenderingState)
     */
    @Override
    @SuppressWarnings("unused")
    public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
        // no body on load to render
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.URLBuilder, org.olat.presentation.framework.translator.Translator,
     * org.olat.presentation.framework.render.RenderingState)
     */
    @Override
    @SuppressWarnings("unused")
    public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderingState rstate) {
        // no header needed
    }

    /**
     * Render Date type with Formatter depending on locale. Render all other types with toString.
     * 
     * @param target
     * @param cellValue
     * @param translator
     */
    @Override
    public void render(StringOutput target, Object cellValue, Translator translator) {
        target.append("<span class=\"b_small_icon ");
        target.append(getCssClass(cellValue));
        String hoverText = getHoverText(cellValue, translator);
        if (StringHelper.containsNonWhitespace(hoverText)) {
            target.append("\" title=\"");
            target.append(StringEscapeUtils.escapeHtml(hoverText));
        }
        target.append("\">");
        target.append(getCellValue(cellValue));
        target.append("</span>");
    }

    protected abstract String getCssClass(Object val);

    protected abstract String getCellValue(Object val);

    protected abstract String getHoverText(Object val, Translator translator);
}
