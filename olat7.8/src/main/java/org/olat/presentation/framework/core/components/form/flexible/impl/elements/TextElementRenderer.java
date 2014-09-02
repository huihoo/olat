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
package org.olat.presentation.framework.core.components.form.flexible.impl.elements;

import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.ComponentRenderer;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormJSHelper;
import org.olat.presentation.framework.core.render.RenderResult;
import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.render.RenderingState;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.presentation.framework.core.render.URLBuilder;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.StringHelper;

/**
 * Description:<br>
 * TODO: patrickb Class Description for TextElementRenderer
 * <P>
 * Initial Date: 08.12.2006 <br>
 * 
 * @author patrickb
 */
class TextElementRenderer implements ComponentRenderer {

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.URLBuilder, org.olat.presentation.framework.translator.Translator,
     * org.olat.presentation.framework.render.RenderResult, java.lang.String[])
     */
    @Override
    public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
        //
        TextElementComponent teC = (TextElementComponent) source;
        TextElementImpl te = teC.getTextElementImpl();

        String id = teC.getFormDispatchId();
        //

        String value = te.getValue();
        if (value == null) {
            value = "";
        }
        StringBuilder htmlAttributeVal = new StringBuilder();
        htmlAttributeVal.append(StringHelper.escapeHtmlAttribute(value));
        if (!source.isEnabled()) {
            // read only view
            sb.append("<span id=\"");
            sb.append(id);
            sb.append("\" ");
            sb.append(FormJSHelper.getRawJSFor(te.getRootForm(), id, te.getAction()));
            sb.append("title=\"");
            sb.append(htmlAttributeVal); // the uncutted value in tooltip
            sb.append("\" ");
            sb.append(" >");
            // use the longer from display size or real value length
            int size = (te.displaySize > value.length() ? te.displaySize : value.length());
            sb.append("<input type=\"").append(te.getHtmlInputType());
            sb.append("\" disabled=\"disabled\" class=\"b_form_element_disabled\" size=\"");
            sb.append(size);
            sb.append("\" value=\"");
            sb.append(htmlAttributeVal);
            sb.append("\" />");
            sb.append("</span>");

        } else {
            // read write view
            sb.append("<input type=\"").append(te.getHtmlInputType()).append("\" id=\"");
            sb.append(id);
            sb.append("\" name=\"");
            sb.append(id);
            sb.append("\" size=\"");
            sb.append(te.displaySize);
            if (te.maxlength > -1) {
                sb.append("\" maxlength=\"");
                sb.append(te.maxlength);
            }
            sb.append("\" value=\"");
            sb.append(htmlAttributeVal);
            sb.append("\" ");
            sb.append(FormJSHelper.getRawJSFor(te.getRootForm(), id, te.getAction()));
            sb.append(" />");
        }

        if (source.isEnabled()) {
            // add set dirty form only if enabled
            sb.append(FormJSHelper.getJSStartWithVarDeclaration(teC.getFormDispatchId()));
            /*
             * deactivated due OLAT-3094 and OLAT-3040 if(te.hasFocus()){ sb.append(FormJSHelper.getFocusFor(teC.getFormDispatchId())); }
             */
            sb.append(FormJSHelper.getSetFlexiFormDirty(te.getRootForm(), teC.getFormDispatchId()));
            sb.append(FormJSHelper.getJSEnd());
        }

    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.RenderingState)
     */
    @Override
    public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
        // not implemented
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.URLBuilder, org.olat.presentation.framework.translator.Translator,
     * org.olat.presentation.framework.render.RenderingState)
     */
    @Override
    public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderingState rstate) {
        // not implemented
    }

}
