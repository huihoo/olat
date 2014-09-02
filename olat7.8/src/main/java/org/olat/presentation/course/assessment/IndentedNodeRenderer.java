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

package org.olat.presentation.course.assessment;

import java.util.Locale;
import java.util.Map;

import org.olat.lms.course.assessment.AssessmentHelper;
import org.olat.lms.course.nodes.CourseNodeFactory;
import org.olat.presentation.framework.core.components.table.CustomCellRenderer;
import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.system.commons.StringHelper;

/**
 * Description:<BR/>
 * Renders a node in a table using the node icon and indent. Required object is a map that contains the values using the keys defined in AssessmentHelper
 * <P/>
 * Initial Date: Nov 23, 2004
 * 
 * @author gnaegi
 */
public class IndentedNodeRenderer implements CustomCellRenderer {

    private static final String INDENT = "&nbsp;&nbsp;";

    /**
	 * 
	 */
    public IndentedNodeRenderer() {
        super();
    }

    /**
     * java.util.Locale, int, java.lang.String)
     */
    @Override
    public void render(final StringOutput sb, final Renderer renderer, final Object val, final Locale locale, final int alignment, final String action) {
        final Map nodeData = (Map) val;
        final Integer indent = (Integer) nodeData.get(AssessmentHelper.KEY_INDENT);
        final String type = (String) nodeData.get(AssessmentHelper.KEY_TYPE);

        final String cssClass = CourseNodeFactory.getInstance().getCourseNodeConfigurationEvenForDisabledBB(type).getIconCSSClass();
        final String title = (String) nodeData.get(AssessmentHelper.KEY_TITLE_SHORT);
        final String altText = (String) nodeData.get(AssessmentHelper.KEY_TITLE_LONG);

        appendIndent(sb, indent);
        sb.append("<span class=\"b_with_small_icon_left ").append(cssClass);
        if (altText != null) {
            sb.append("\" title= \"").append(StringHelper.escapeHtml(altText));
        }
        sb.append("\">");
        sb.append(StringHelper.escapeHtml(title));
        sb.append("</span>");
    }

    private void appendIndent(final StringOutput sb, final Integer indent) {
        for (int i = 0; i < indent.intValue(); i++) {
            sb.append(INDENT);
        }
    }

}
