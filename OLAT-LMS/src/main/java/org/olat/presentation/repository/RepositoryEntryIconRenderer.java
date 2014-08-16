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
 * Copyright (c) 2008 frentix GmbH,<br>
 * http://www.frentix.com,<br>
 * Switzerland.*
 * <p>
 */
package org.olat.presentation.repository;

import java.util.Locale;

import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.i18n.I18nModule;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.framework.common.ControllerFactory;
import org.olat.presentation.framework.core.components.table.CustomCssCellRenderer;

/**
 * Description:<br>
 * This cell renderer displays the repository entry type as a CSS icon. The underlying data model must provide an object of type RepositoryEntry
 * <P>
 * Initial Date: 16.04.2008 <br>
 * 
 * @author Florian Gnägi, http://www.frentix.com
 */
public class RepositoryEntryIconRenderer extends CustomCssCellRenderer {
    private final Locale locale;

    /**
     * Constructor
     * 
     * @param locale
     */
    public RepositoryEntryIconRenderer(final Locale locale) {
        this.locale = locale;
    }

    /**
     * Constructor
     */
    public RepositoryEntryIconRenderer() {
        this.locale = I18nModule.getDefaultLocale();
    }

    /**
	 */
    @Override
    protected String getCellValue(final Object val) {
        return "";
    }

    /**
	 */
    @Override
    protected String getCssClass(final Object val) {
        // use small icon and create icon class for resource:
        // o_FileResource-SHAREDFOLDER_icon
        final RepositoryEntry re = (RepositoryEntry) val;
        if (re == null) {
            return "";
        }
        return "b_small_icon " + getIconCssClass(re);
    }

    public String getIconCssClass(final RepositoryEntry re) {
        String iconCSSClass = "o_" + re.getOlatResource().getResourceableTypeName().replace(".", "-");
        if (re != null && RepositoryServiceImpl.getInstance().createRepositoryEntryStatus(re.getStatusCode()).isClosed()) {
            iconCSSClass = iconCSSClass.concat("_icon_closed");
        } else {
            iconCSSClass = iconCSSClass.concat("_icon");
        }
        return iconCSSClass;
    }

    /**
	 */
    @Override
    protected String getHoverText(final Object val) {
        final RepositoryEntry re = (RepositoryEntry) val;
        if (re == null) {
            return "n/a";
        }
        final String typeName = re.getOlatResource().getResourceableTypeName();
        return ControllerFactory.translateResourceableTypeName(typeName, locale);
    }

}
