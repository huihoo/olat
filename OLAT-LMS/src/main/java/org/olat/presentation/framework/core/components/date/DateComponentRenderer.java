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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.presentation.framework.core.components.date;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.ComponentRenderer;
import org.olat.presentation.framework.core.render.RenderResult;
import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.render.RenderingState;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.presentation.framework.core.render.URLBuilder;
import org.olat.presentation.framework.core.translator.Translator;

/**
 * Description:<br>
 * Renderer for the date component. An optional render argument can be used. This is interpreted as a CSS class name.
 * <P>
 * Initial Date: 01.12.2009 <br>
 * 
 * @author gnaegi
 */
public class DateComponentRenderer implements ComponentRenderer {

    /**
     * Package scope constuctro
     */
    DateComponentRenderer() {
        // Nothing to do
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.URLBuilder, org.olat.presentation.framework.translator.Translator,
     * org.olat.presentation.framework.render.RenderResult, java.lang.String[])
     */
    @Override
    public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
        DateComponent dateC = (DateComponent) source;
        Date date = dateC.getDate();
        Locale locale = translator.getLocale();

        sb.append("<div class=\"b_datecomp ");
        // Optional css class as render arg
        if (args != null && args.length == 1) {
            sb.append(args[0]);
        }
        sb.append("\">");
        // Add year if configured
        if (dateC.isShowYear()) {
            SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", locale);
            String year = yearFormat.format(date);
            sb.append("<div class=\"b_year b_year_").append(year);
            sb.append("\"><span>");
            sb.append(year);
            sb.append("</span>");
            sb.append("</div>");
        }
        // Add month.
        SimpleDateFormat monthNumberFormat = new SimpleDateFormat("MM", locale);
        sb.append("<div class=\"b_month b_month_").append(monthNumberFormat.format(date));
        sb.append("\"><span>");
        SimpleDateFormat monthDisplayFormat = new SimpleDateFormat("MMM", locale);
        sb.append(monthDisplayFormat.format(date).toUpperCase());
        sb.append("</span>");
        sb.append("</div>");
        // Add day
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd", locale);
        String day = dayFormat.format(date);
        sb.append("<div class=\"b_day b_day_").append(day);
        sb.append("\"><span>");
        sb.append(day);
        sb.append("</span>");
        sb.append("</div>");
        //
        sb.append("</div>");
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.RenderingState)
     */
    @Override
    public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
        // nothing to do
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.URLBuilder, org.olat.presentation.framework.translator.Translator,
     * org.olat.presentation.framework.render.RenderingState)
     */
    @Override
    public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderingState rstate) {
        // nothing to do
    }

}
