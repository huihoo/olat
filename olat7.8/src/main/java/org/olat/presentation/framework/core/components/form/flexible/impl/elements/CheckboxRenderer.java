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
 * TODO: patrickb Class Description for CheckboxRenderer
 * <P>
 * Initial Date: 04.01.2007 <br>
 * 
 * @author patrickb
 */
class CheckboxRenderer implements ComponentRenderer {

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.URLBuilder, org.olat.presentation.framework.translator.Translator,
     * org.olat.presentation.framework.render.RenderResult, java.lang.String[])
     */
    @Override
    public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {

        // default should allow <b> </b> coming from localstring properties (see also http://bugs.olat.org/jira/browse/OLAT-4208)
        // OLAT-1024: CheckboxRenderer will escape all strings, independent on their source (user inputed or localstring properties)
        /*
         * boolean escapeHTML = true; if (args != null && args.length > 0) { for (int i = 0; i < args.length; i++) { if
         * (CheckboxElementComponent.RENDERARG_ESCAPEHTML.equals(args[i])) { escapeHTML = true;// so far used from SelectionTreeComponent, e.q. make the publish render
         * tree safe against special chars in node titles } } }
         */

        CheckboxElementComponent cec = (CheckboxElementComponent) source;

        String subStrName = "name=\"" + cec.getGroupingName() + "\"";

        String key = cec.getKey();
        String value = cec.getValue();
        // key must always be escaped since it becomes a value for an attribute
        key = StringHelper.escapeHtmlAttribute(key);
        if (!cec.isTrustedText()) {
            value = StringHelper.escapeHtml(value);
        }

        boolean selected = cec.isSelected();

        // read write view
        String cssClass = cec.getCssClass(); // optional CSS class
        sb.append("<input type=\"checkbox\" ");
        sb.append("id=\"");
        sb.append(cec.getFormDispatchId());
        sb.append("\" ");
        sb.append("class=\"b_checkbox\" ");
        sb.append(subStrName);
        sb.append(" value=\"");
        sb.append(key);
        sb.append("\"");
        if (selected)
            sb.append(" checked=\"checked\" ");
        if (!source.isEnabled()) {
            sb.append(" disabled=\"disabled\" ");
        } else {
            // use the selection form dispatch id and not the one of the element!
            sb.append(FormJSHelper.getRawJSFor(cec.getRootForm(), cec.getSelectionElementFormDisId(), cec.getAction()));
        }
        sb.append(" />");
        if (cssClass != null)
            sb.append("<span class=\"").append(cssClass).append("\">");
        if (StringHelper.containsNonWhitespace(value)) {
            sb.append("<label class=\"b_checkbox_label\" for=\"").append(cec.getFormDispatchId()).append("\">");
            sb.append(value);
            sb.append("</label>");
        }
        if (cssClass != null)
            sb.append("</span>");

        if (source.isEnabled()) {
            // add set dirty form only if enabled
            sb.append(FormJSHelper.getJSStartWithVarDeclaration(cec.getFormDispatchId()));
            sb.append(FormJSHelper.getSetFlexiFormDirtyForCheckbox(cec.getRootForm(), cec.getFormDispatchId()));
            sb.append(FormJSHelper.getJSEnd());
        }

    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.RenderingState)
     */
    @Override
    public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
        // TODO Auto-generated method stub

    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.URLBuilder, org.olat.presentation.framework.translator.Translator,
     * org.olat.presentation.framework.render.RenderingState)
     */
    @Override
    public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderingState rstate) {
        // TODO Auto-generated method stub

    }

}
