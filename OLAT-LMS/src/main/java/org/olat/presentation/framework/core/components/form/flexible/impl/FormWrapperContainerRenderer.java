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
package org.olat.presentation.framework.core.components.form.flexible.impl;

import java.util.HashSet;
import java.util.Set;

import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.ComponentRenderer;
import org.olat.presentation.framework.core.components.Container;
import org.olat.presentation.framework.core.control.winmgr.AJAXFlags;
import org.olat.presentation.framework.core.render.RenderResult;
import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.render.RenderingState;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.presentation.framework.core.render.URLBuilder;
import org.olat.presentation.framework.core.translator.Translator;

/**
 * Description:<br>
 * TODO: patrickb Class Description for FormRenderer
 * <P>
 * Initial Date: 27.11.2006 <br>
 * 
 * @author patrickb
 */
class FormWrapperContainerRenderer implements ComponentRenderer {

    private static final Set<String> acceptedInstructions = new HashSet<String>();
    static {
        acceptedInstructions.add("class");
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.URLBuilder, org.olat.presentation.framework.translator.Translator,
     * org.olat.presentation.framework.render.RenderResult, java.lang.String[])
     */
    @Override
    @SuppressWarnings("unused")
    public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
        FormWrapperContainer formC = (FormWrapperContainer) source;
        Container toRender = formC.getFormLayout();

        /*
         * extract check for render instruction to the form wrapper
         */
        boolean hasRenderInstr = (args != null && args.length > 0);

        if (toRender != null) {
            AJAXFlags flags = renderer.getGlobalSettings().getAjaxFlags();
            boolean iframePostEnabled = flags.isIframePostEnabled();
            /*
             * FORM HEADER
             */
            sb.append("<form ");
            if (hasRenderInstr) {
                // append render instructions if available
                // flexi form supports only class
                FormJSHelper.appendRenderInstructions(sb, args[0], acceptedInstructions);
            }

            sb.append(" method=\"post\"");
            // Set encoding to multipart only if multipart data is available to reduce
            // transfer and parameter extracing overhead
            if (formC.isMultipartEnabled()) {
                sb.append(" enctype=\"multipart/form-data\"");
            }
            sb.append(" name=\"");
            sb.append(formC.getFormName());
            sb.append("\" action=\"");
            ubu.buildURI(sb, new String[] { Form.FORMID }, new String[] { Form.FORMCMD }, iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
            sb.append("\" ");
            // check if ready to accept a new request
            sb.append(" onsubmit=\"if(o_info.linkbusy) return false; else o_beforeserver(); return true;\" ");
            if (iframePostEnabled) {
                ubu.appendTarget(sb);
            }
            sb.append(">");
            // hidden input field for dispatch uri
            sb.append("<input type=\"hidden\" id=\"");
            sb.append(formC.getDispatchFieldId());
            sb.append("\" name=\"dispatchuri\" value=\"").append(Form.FORM_UNDEFINED).append("\" />");
            sb.append("<input type=\"hidden\" id=\"");
            sb.append(formC.getEventFieldId());
            sb.append("\" name=\"dispatchevent\" value=\"").append(Form.FORM_UNDEFINED).append("\" />");
            /*
             * FORM CONTAINER
             */
            renderer.render(sb, toRender, args);
            /*
             * FORM FOOTER
             */
            sb.append("</form>");
            /*
             * FORM SUBMIT on keypress enter
             */
            sb.append(FormJSHelper.submitOnKeypressEnter(formC.getFormName()));
        }
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.RenderingState)
     */
    @Override
    public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
        FormWrapperContainer formC = (FormWrapperContainer) source;
        Container toRender = formC.getFormLayout();
        if (toRender != null) {
            renderer.renderBodyOnLoadJSFunctionCall(sb, toRender, rstate);
        }
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.URLBuilder, org.olat.presentation.framework.translator.Translator,
     * org.olat.presentation.framework.render.RenderingState)
     */
    @Override
    @SuppressWarnings("unused")
    public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderingState rstate) {
        FormWrapperContainer formC = (FormWrapperContainer) source;
        Container toRender = formC.getFormLayout();
        if (toRender != null) {
            renderer.renderHeaderIncludes(sb, toRender, rstate);
        }

    }

}
