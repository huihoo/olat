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

package org.olat.presentation.course.nodes.projectbroker;

import java.util.Locale;

import org.olat.data.course.nodes.projectbroker.ProjectEvent;
import org.olat.presentation.framework.core.components.table.CustomCellRenderer;
import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.system.exception.AssertException;

/**
 * This renderer is used by the ProjectListController to render the 'ProjectEvent' column. The renderer distinguish between render for table content (with HTML) and
 * render for export (no HTML code).
 * 
 * @author Christian Guretzki
 */
public class ProjectEventColumnRenderer implements CustomCellRenderer {

    /**
     * Renderer for project-broker event-column. For table-view render with HTML elements e.g. 'vom 10.04.2010 10:00<br>
     * bis 11.04.2010 10:00' . When the renderer is null, no HTML tags will be added e.g. 'vom 10.04.2010 10:00 bis 11.04.2010 10:00' .
     * 
     * @param val
     *            must be from type ProjectEvent java.util.Locale, int, java.lang.String)
     */
    @Override
    public void render(final StringOutput sb, final Renderer renderer, final Object val, final Locale locale, final int alignment, final String action) {
        ProjectEvent projectEvent;
        final PackageTranslator translator = new PackageTranslator(PackageUtil.getPackageName(this.getClass()), locale);
        if (val == null) {
            // don't render nulls
            return;
        }
        if (val instanceof ProjectEvent) {
            projectEvent = (ProjectEvent) val;
        } else {
            throw new AssertException("ProjectEventColumnRenderer: Wrong object type, could only render ProjectEvent");
        }
        if (renderer == null) {
            // if no renderer is set, then we assume it's a table export - in which case we don't want the htmls (<br/>)
            if (projectEvent.getStartDate() != null) {
                sb.append(translator.translate("table.event.start.label"));
                sb.append(projectEvent.getFormattedStartDate());
                sb.append(" ");
            }
            if (projectEvent.getEndDate() != null) {
                sb.append(translator.translate("table.event.end.label"));
                sb.append(projectEvent.getFormattedEndDate());
            }
        } else {
            // add <br> between the dates
            if (projectEvent.getStartDate() != null) {
                sb.append(translator.translate("table.event.start.label"));
                sb.append(projectEvent.getFormattedStartDate());
                if (projectEvent.getEndDate() != null) {
                    sb.append("<br>");
                }
            }
            if (projectEvent.getEndDate() != null) {
                sb.append(translator.translate("table.event.end.label"));
                sb.append(projectEvent.getFormattedEndDate());
            }
        }

    }

}
