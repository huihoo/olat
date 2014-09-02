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
 * TODO: patrickb Class Description for SingleSelectionElementComponentRenderer
 * <P>
 * Initial Date: 31.12.2006 <br>
 * 
 * @author patrickb
 */
class RadioElementRenderer implements ComponentRenderer {

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.URLBuilder, org.olat.presentation.framework.translator.Translator,
     * org.olat.presentation.framework.render.RenderResult, java.lang.String[])
     */
    @Override
    public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {

        RadioElementComponent ssec = (RadioElementComponent) source;

        String subStrName = "name=\"" + ssec.getGroupingName() + "\"";

        String key = ssec.getKey();
        String value = ssec.getValue();
        value = StringHelper.escapeHtml(value);
        boolean selected = ssec.isSelected();
        // read write view
        sb.append("<input ");
        sb.append("id=\"");
        sb.append(ssec.getFormDispatchId());
        sb.append("\" ");
        sb.append("type=\"radio\" class=\"b_radio\" " + subStrName + " value=\"");
        sb.append(key);
        sb.append("\" ");
        if (selected) {
            sb.append(" checked=\"checked\" ");
        }
        if (!source.isEnabled()) {
            // mark as disabled and do not add javascript
            sb.append(" disabled=\"disabled\" ");
        } else {
            // use the selection elements formDispId instead of the one of this element.
            sb.append(FormJSHelper.getRawJSFor(ssec.getRootForm(), ssec.getSelectionElementFormDisId(), ssec.getAction()));
        }
        sb.append(" />");
        sb.append("<label for=\"").append(ssec.getFormDispatchId()).append("\">");
        sb.append(value);
        sb.append("</label>");

        if (source.isEnabled()) {
            // add set dirty form only if enabled
            sb.append(FormJSHelper.getJSStartWithVarDeclaration(ssec.getFormDispatchId()));
            sb.append(FormJSHelper.getSetFlexiFormDirtyForCheckbox(ssec.getRootForm(), ssec.getFormDispatchId()));
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
