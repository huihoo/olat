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
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.presentation.tunneling;

import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.ComponentRenderer;
import org.olat.presentation.framework.core.render.RenderResult;
import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.render.RenderingState;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.presentation.framework.core.render.URLBuilder;
import org.olat.presentation.framework.core.translator.Translator;

/**
 * enclosing_type Description: <br>
 * 
 * @author Felix Jost
 */
public class TunnelRenderer implements ComponentRenderer {

    /**
     * Constructor for TableRenderer. Singleton and must be reentrant There must be an empty contructor for the Class.forName() call
     */
    public TunnelRenderer() {
        super();
    }

    /**
     * @param renderer
     * @param target
     * @param source
     * @param ubu
     * @param translator
     * @param renderResult
     * @param args
     */
    @Override
    public void render(final Renderer renderer, final StringOutput target, final Component source, final URLBuilder ubu, final Translator translator,
            final RenderResult renderResult, final String[] args) {
        final TunnelComponent tuc = (TunnelComponent) source;
        // is called for the current inline html
        renderResult.setAsyncMediaResponsible(tuc);
        final String htmlContent = tuc.getHtmlContent();
        if (htmlContent != null) {
            target.append(htmlContent);
        }
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.URLBuilder, org.olat.presentation.framework.translator.Translator)
     */
    @Override
    public void renderHeaderIncludes(final Renderer renderer, final StringOutput sb, final Component source, final URLBuilder ubu, final Translator translator,
            final RenderingState rstate) {
        final TunnelComponent tuc = (TunnelComponent) source;
        final String htmlHead = tuc.getHtmlHead();
        if (htmlHead != null) {
            sb.append(htmlHead);
        }
    }

    /**
     * org.olat.presentation.framework.components.Component)
     */
    @Override
    public void renderBodyOnLoadJSFunctionCall(final Renderer renderer, final StringOutput sb, final Component source, final RenderingState rstate) {
        final TunnelComponent tuc = (TunnelComponent) source;
        final String jsBodyOnLoad = tuc.getJsOnLoad();
        if (jsBodyOnLoad != null) {
            sb.append(jsBodyOnLoad);
        }
    }
}
