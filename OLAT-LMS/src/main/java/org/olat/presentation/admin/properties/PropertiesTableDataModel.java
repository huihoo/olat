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

package org.olat.presentation.admin.properties;

import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.properties.PropertyImpl;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;

/**
 * Description:<br>
 * 
 * @author Alexander Schneider
 */
public class PropertiesTableDataModel extends DefaultTableDataModel {

    /**
     * Default constructor.
     */
    public PropertiesTableDataModel() {
        super(null);
    }

    /**
     * Initialize table model with objects.
     * 
     * @param objects
     */
    public PropertiesTableDataModel(final List objects) {
        super(objects);
    }

    /**
	 */
    @Override
    public int getColumnCount() {
        // resourceTypeName, resourceTypeId, category, name, floatValue, stringValue, textValue
        return 11;
    }

    /**
	 */
    @Override
    public final Object getValueAt(final int row, final int col) {
        final PropertyImpl p = (PropertyImpl) objects.get(row);
        switch (col) {
        case 0:
            final Identity id = p.getIdentity();
            return ((id != null) ? (p.getIdentity().getName()) : (null));
        case 1:
            return p.getResourceTypeName();
        case 2:
            return p.getResourceTypeId(); // may be null; in this case, the table renders nothing for this cell;
        case 3:
            return p.getCategory();
        case 4:
            return p.getName();
        case 5:
            return p.getFloatValue();
        case 6:
            return p.getStringValue();
        case 7:
            return p.getTextValue();
        case 8:
            return p.getCreationDate().toString();
        case 9:
            return p.getLastModified().toString();
        case 10:
            return p.getLongValue();
        default:
            return "error";
        }
    }

}
