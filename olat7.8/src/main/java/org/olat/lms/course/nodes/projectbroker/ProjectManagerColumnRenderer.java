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
package org.olat.lms.course.nodes.projectbroker;

import java.util.ArrayList;
import java.util.Locale;

import org.olat.data.basesecurity.Identity;
import org.olat.data.user.UserConstants;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.components.table.CustomCellRenderer;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * wrapper for multiple identities in one cell. needed to code the row-id into the cell for later callout-opening.
 * 
 * <P>
 * Initial Date: 01.04.2011 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class ProjectManagerColumnRenderer implements CustomCellRenderer {

    public static final String PROJECTMANAGER_COLUMN_ROW_IDENT = "pmrow";

    @Override
    public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
        if (val instanceof ArrayList) {
            @SuppressWarnings("unchecked")
            ArrayList<Identity> allIdents = (ArrayList<Identity>) val;
            Integer row = null;
            try {
                row = Integer.parseInt(action);
            } catch (Exception e) {
                // do nothing with that
            }

            StringBuilder buf = new StringBuilder();

            if (renderer != null && row != null) {
                // if no renderer is set, then we assume it's a table export - in which case we don't want the htmls (<b>)
                // no row might occur during table-search
                sb.append("<span class=\"projmgrrowcontent\"");
                sb.append("id=\"" + PROJECTMANAGER_COLUMN_ROW_IDENT + row + "\">");
            }

            for (Identity identity : allIdents) {
                String last = getUserService().getUserProperty(identity.getUser(), UserConstants.LASTNAME, locale);
                String first = getUserService().getUserProperty(identity.getUser(), UserConstants.FIRSTNAME, locale);
                buf.append(last).append(" ").append(first).append(", ");
            }
            sb.append(buf.substring(0, buf.length() - 2));

            if (renderer != null && row != null) {
                sb.append("</span>");
            }
        }
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }
}
