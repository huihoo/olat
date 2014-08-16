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

package org.olat.presentation.framework.core.components.htmlsite;

import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.ComponentRenderer;
import org.olat.presentation.framework.core.render.RenderResult;
import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.render.RenderingState;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.presentation.framework.core.render.URLBuilder;
import org.olat.presentation.framework.core.translator.Translator;

/**
 * @author Felix Jost
 */
public class HtmlStaticPageComponentRenderer implements ComponentRenderer {

    /**
     * Constructor for TableRenderer. Singleton and must be reentrant There must be an empty contructor for the Class.forName() call
     */
    public HtmlStaticPageComponentRenderer() {
        super();
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.URLBuilder, org.olat.presentation.framework.translator.Translator,
     * org.olat.presentation.framework.render.RenderResult, java.lang.String[])
     */
    @Override
    public void render(Renderer renderer, StringOutput target, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
        HtmlStaticPageComponent cpc = (HtmlStaticPageComponent) source;
        // Is called for the current inline html
        String htmlContent = cpc.getHtmlContent();
        // Add wrapper css style if defined (Used by scaling feature)
        String wrapperCssStyle = cpc.getWrapperCssStyle();
        if (htmlContent != null && wrapperCssStyle != null) {
            htmlContent = "<div style=\"" + wrapperCssStyle + "\">" + htmlContent + "</div>";
        }
        // Indicate to framework that the page probably will load some media files
        renderResult.setAsyncMediaResponsible(cpc); // indicate browser fetch of e.g. images includes in the rendered page should be directed to the cpcomponent so this
                                                    // component can deliver the e.g. images
        if (htmlContent != null)
            target.append(htmlContent);
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.URLBuilder, org.olat.presentation.framework.translator.Translator)
     */
    @Override
    public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderingState rstate) {
        HtmlStaticPageComponent cpc = (HtmlStaticPageComponent) source;
        // is called for the current inline html
        String mm = cpc.getHtmlHead();
        if (mm != null)
            sb.append(mm);
    }

    /**
     * org.olat.presentation.framework.components.Component)
     */
    @Override
    public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
        HtmlStaticPageComponent cpc = (HtmlStaticPageComponent) source;
        // is called for the current inline html
        String mm = cpc.getJsOnLoad();
        if (mm != null)
            sb.append(mm);
    }
}
