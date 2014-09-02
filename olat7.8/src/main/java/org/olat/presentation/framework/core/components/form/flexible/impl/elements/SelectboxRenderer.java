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

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.presentation.framework.core.GUIInterna;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.ComponentRenderer;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormJSHelper;
import org.olat.presentation.framework.core.render.RenderResult;
import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.render.RenderingState;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.presentation.framework.core.render.URLBuilder;
import org.olat.presentation.framework.core.translator.Translator;

/**
 * Description:<br>
 * TODO: patrickb Class Description for SingleSelectionSelectboxRenderer
 * <P>
 * Initial Date: 02.01.2007 <br>
 * 
 * @author patrickb
 */
class SelectboxRenderer implements ComponentRenderer {

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.URLBuilder, org.olat.presentation.framework.translator.Translator,
     * org.olat.presentation.framework.render.RenderResult, java.lang.String[])
     */
    @Override
    @SuppressWarnings("unused")
    public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {

        SelectboxComponent ssec = (SelectboxComponent) source;

        if (GUIInterna.isLoadPerformanceMode()) {
            // Just make sure the replayID mapping is known
            ssec.getRootForm().getReplayableDispatchID(source);
        }

        String subStrName = "name=\"" + ssec.getGroupingName() + "\"";
        String[] options = ssec.getOptions();
        String[] values = ssec.getValues();
        String[] cssClasses = ssec.getCssClasses();

        // read write

        /*
         * opening <select ... >
         */
        sb.append("<select ");
        if (!ssec.isEnabled()) {
            sb.append(" disabled=\"disabled\" ");
        }
        sb.append("id=\"");
        sb.append(ssec.getFormDispatchId());
        sb.append("\" ");
        sb.append(subStrName);// the name
        if (ssec.isMultiSelect()) {
            sb.append(" multiple ");
            sb.append(" size=\"3\" ");
        }
        // add ONCHANGE Action to select
        if (ssec.getAction() == FormEvent.ONCHANGE) {
            sb.append(FormJSHelper.getRawJSFor(ssec.getRootForm(), ssec.getSelectionElementFormDisId(), ssec.getAction()));
        }

        sb.append(">");
        /*
         * the options <option ...>value</option>
         */
        int cnt = options.length;
        for (int i = 0; i < cnt; i++) {
            boolean selected = ssec.isSelected(i);
            sb.append("<option value=\"");
            sb.append(StringEscapeUtils.escapeHtml(options[i]));
            sb.append("\" ");
            if (selected)
                sb.append("selected=\"selected\" ");
            if (ssec.getAction() != FormEvent.ONCHANGE) {
                // all other events go to the option
                sb.append(FormJSHelper.getRawJSFor(ssec.getRootForm(), ssec.getSelectionElementFormDisId(), ssec.getAction()));
            }
            if (cssClasses != null) {
                sb.append(" class=\"");
                sb.append(cssClasses[i]);
                sb.append("\"");
            }
            sb.append(">");
            sb.append(StringEscapeUtils.escapeHtml(values[i]));
            sb.append("</option>");
        }
        /*
         * closing </select>
         */
        sb.append("</select>");

        if (source.isEnabled()) {
            // add set dirty form only if enabled
            sb.append(FormJSHelper.getJSStartWithVarDeclaration(ssec.getFormDispatchId()));
            sb.append(FormJSHelper.getSetFlexiFormDirty(ssec.getRootForm(), ssec.getFormDispatchId()));
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
