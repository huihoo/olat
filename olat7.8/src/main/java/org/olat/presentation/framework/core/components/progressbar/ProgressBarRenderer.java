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

package org.olat.presentation.framework.core.components.progressbar;

import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.ComponentRenderer;
import org.olat.presentation.framework.core.render.RenderResult;
import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.render.RenderingState;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.presentation.framework.core.render.URLBuilder;
import org.olat.presentation.framework.core.translator.Translator;

/**
 * Initial Date: Feb 2, 2004 A <b>ChoiceRenderer </b> is
 * 
 * @author Andreas Ch. Kapp
 */
public class ProgressBarRenderer implements ComponentRenderer {

    /**
     * This is a singleton. There must be an empty contructor for the Class.forName() call.
     */
    public ProgressBarRenderer() {
        super();
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.URLBuilder, org.olat.presentation.framework.translator.Translator,
     * org.olat.presentation.framework.render.RenderResult, java.lang.String[])
     */
    @Override
    public void render(Renderer renderer, StringOutput target, Component source, URLBuilder urlBuilder, Translator translator, RenderResult renderResult, String[] args) {
        // Get the model object
        ProgressBar ubar = (ProgressBar) source;
        boolean renderLabels = (args == null) ? true : false;
        float percent = 100;
        if (!ubar.getIsNoMax())
            percent = 100 * ubar.getActual() / ubar.getMax();
        if (percent < 0)
            percent = 0;
        if (percent > 100)
            percent = 100;
        target.append("<div class=\"b_progress\"><div class=\"b_progress_bar\" style=\"width:");
        target.append(ubar.getWidth());
        target.append("px;\"><div style=\"width:");
        target.append(Math.round(percent * ubar.getWidth() / 100));
        target.append("px\" title=\"");
        target.append(Math.round(percent * ubar.getWidth() / 100));
        target.append("%\"></div></div>");
        if (renderLabels) {
            target.append("<div class=\"b_progress_label\">");
            if (ubar.isPercentagesEnabled()) {
                target.append(Math.round(percent));
                target.append("% (");
            }
            target.append(Math.round(ubar.getActual()));
            target.append("/");
            if (ubar.getIsNoMax())
                target.append("-");
            else
                target.append(Math.round(ubar.getMax()));
            target.append(" ");
            target.append(ubar.getUnitLabel());
            if (ubar.isPercentagesEnabled()) {
                target.append(")");
            }
            target.append("</div>");
        }
        target.append("</div>");

    }

    /**
     * org.olat.presentation.framework.components.Component)
     */
    @Override
    public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
        //
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.URLBuilder, org.olat.presentation.framework.translator.Translator)
     */
    @Override
    public void renderHeaderIncludes(Renderer renderer, StringOutput target, Component source, URLBuilder url, Translator translator, RenderingState rstate) {
        //
    }

}
