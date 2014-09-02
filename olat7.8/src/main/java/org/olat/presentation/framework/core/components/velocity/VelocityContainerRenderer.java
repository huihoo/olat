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

package org.olat.presentation.framework.core.components.velocity;

import java.util.Iterator;
import java.util.Map;

import org.apache.velocity.context.Context;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.ComponentRenderer;
import org.olat.presentation.framework.core.render.RenderResult;
import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.render.RenderingState;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.presentation.framework.core.render.URLBuilder;
import org.olat.presentation.framework.core.render.velocity.VelocityHelper;
import org.olat.presentation.framework.core.render.velocity.VelocityRenderDecorator;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Renderer for the VelocityContainer <br>
 * 
 * @author Felix Jost
 */
public class VelocityContainerRenderer implements ComponentRenderer {

    private final String theme;
    private VelocityHelper velocityHelper;

    /**
     * @param theme
     *            if null, then pages will be searched under the path defined by the page, e.g. /_theme/index.html instead of /index.html (an underscore is added)
     */
    public VelocityContainerRenderer(String theme) {
        super();
        this.theme = theme;
        velocityHelper = (VelocityHelper) CoreSpringFactory.getBean(VelocityHelper.class);
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.URLBuilder, org.olat.presentation.framework.translator.Translator,
     * org.olat.presentation.framework.render.RenderResult, java.lang.String[])
     */
    @Override
    public void render(Renderer renderer, StringOutput target, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
        VelocityContainer vc = (VelocityContainer) source;
        String pagePath = vc.getPage();
        Context ctx = vc.getContext();

        // the component id of the urlbuilder will be overwritten by the recursive render call for
        // subcomponents (see Renderer)
        Renderer fr = Renderer.getInstance(vc, translator, ubu, renderResult, renderer.getGlobalSettings());
        VelocityRenderDecorator vrdec = new VelocityRenderDecorator(fr, vc);
        ctx.put("r", vrdec);
        String mm = velocityHelper.mergeContent(pagePath, ctx, theme);

        // experimental!!!
        // surround with red border if recording mark indicates so.
        if (vc.markingCommandString != null) {
            target.append("<table style=\"border:3px solid red; background-color:#E0E0E0; padding:4px; margin:0px;\"><tr><td>").append(mm).append("</td></tr></table>");
        } else {
            target.append(mm);
        }
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.URLBuilder, org.olat.presentation.framework.translator.Translator)
     */
    @Override
    public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderingState rstate) {
        VelocityContainer vc = (VelocityContainer) source;
        // the velocity container itself needs no headerincludes, but ask the
        // children also
        Map comps = vc.getComponents();
        for (Iterator iter = comps.values().iterator(); iter.hasNext();) {
            Component child = (Component) iter.next();
            renderer.renderHeaderIncludes(sb, child, rstate);
        }
    }

    /**
     * org.olat.presentation.framework.components.Component)
     */
    @Override
    public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
        VelocityContainer vc = (VelocityContainer) source;
        // the velocity container itself needs no headerincludes, but ask the
        // children also
        Map comps = vc.getComponents();
        for (Iterator iter = comps.values().iterator(); iter.hasNext();) {
            Component child = (Component) iter.next();
            renderer.renderBodyOnLoadJSFunctionCall(sb, child, rstate);
        }
    }
}
