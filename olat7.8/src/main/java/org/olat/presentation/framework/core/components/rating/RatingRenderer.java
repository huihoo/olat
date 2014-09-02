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
package org.olat.presentation.framework.core.components.rating;

import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.ComponentRenderer;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.winmgr.AJAXFlags;
import org.olat.presentation.framework.core.render.RenderResult;
import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.render.RenderingState;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.presentation.framework.core.render.URLBuilder;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.Formatter;

/**
 * Description:<br>
 * This renders the rating component
 * <P>
 * Initial Date: 31.10.2008 <br>
 * 
 * @author gnaegi
 */
public class RatingRenderer implements ComponentRenderer {

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.URLBuilder, org.olat.presentation.framework.translator.Translator,
     * org.olat.presentation.framework.render.RenderResult, java.lang.String[])
     */
    @Override
    public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
            @SuppressWarnings("unused") RenderResult renderResult, @SuppressWarnings("unused") String[] args) {
        RatingComponent rating = (RatingComponent) source;
        sb.append("<div class='b_rating ");
        // Add custom css class
        if (rating.getCssClass() != null)
            sb.append(rating.getCssClass());
        sb.append("'>");
        // Add Title
        String title = rating.getTitle();
        if (title != null) {
            sb.append("<div class='b_rating_title'>");
            if (rating.isTranslateTitle()) {
                title = translator.translate(title);
            }
            sb.append(title);
            sb.append("</div>"); // b_rating_title
        }
        // Add ratings and labels
        List<String> labels = rating.getRatingLabel();
        sb.append("<div class='b_rating_items");
        if (rating.isAllowUserInput()) {
            sb.append(" b_enabled");
        }
        sb.append("'>");

        boolean ajaxModeEnabled = renderer.getGlobalSettings().getAjaxFlags().isIframePostEnabled();
        for (int i = 0; i < labels.size(); i++) {
            // Add css class
            sb.append("<a class='");
            if (rating.getCurrentRating() >= i + 1) {
                sb.append("b_rating_item_on");
            } else {
                sb.append("b_rating_item_off");
            }
            sb.append("'");
            // Add action
            if (rating.isAllowUserInput()) {
                // Add link
                sb.append("href=\"");
                ubu.buildURI(sb, new String[] { VelocityContainer.COMMAND_ID }, new String[] { (i + 1) + "" }, ajaxModeEnabled ? AJAXFlags.MODE_TOBGIFRAME
                        : AJAXFlags.MODE_NORMAL);
                sb.append("\"");
                // add link target
                if (ajaxModeEnabled) {
                    ubu.appendTarget(sb);
                }
                // add check for olat busy
                sb.append(" onclick=\"return o2cl()\"  onkeypress=\"return o2cl()\"");

            } else {
                // Disabled link
                sb.append(" href='#' onclick='return false;'");
            }
            // Add item label
            String label = rating.getRatingLabel(i);
            if (label != null) {
                if (rating.isTranslateRatingLabels()) {
                    label = translator.translate(label);
                }
                StringBuilder escapedLabel = new StringBuilder();
                escapedLabel.append(StringEscapeUtils.escapeHtml(label));
                sb.append(" title=\"").append(escapedLabel).append("\"");
            }
            sb.append("></a>");
        }
        // Add text output
        if (rating.isShowRatingAsText()) {
            sb.append(Formatter.roundToString(rating.getCurrentRating(), 1));
            sb.append(" / ");
            sb.append(labels.size());
        }
        sb.append("</div>"); // b_rating_items
        // Add explanation
        String expl = rating.getExplanation();
        if (expl != null) {
            sb.append("<div class='b_rating_explanation'>");
            if (rating.isTranslateExplanation()) {
                expl = translator.translate(expl);
            }
            sb.append(expl);
            sb.append("</div>"); // b_rating_explanation
        }
        sb.append("</div>");// b_rating
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.RenderingState)
     */
    @Override
    public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
        // no body onload to execute
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.presentation.framework.render.URLBuilder, org.olat.presentation.framework.translator.Translator,
     * org.olat.presentation.framework.render.RenderingState)
     */
    @Override
    public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderingState rstate) {
        // no headers to load
    }

}
