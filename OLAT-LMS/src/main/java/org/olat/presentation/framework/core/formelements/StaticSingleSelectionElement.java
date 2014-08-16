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

package org.olat.presentation.framework.core.formelements;

import org.olat.lms.commons.i18n.I18nManager;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.system.exception.AssertException;
import org.olat.system.exception.OLATRuntimeException;

/**
 * @author Felix Jost
 */
public class StaticSingleSelectionElement extends AbstractFormElement implements SingleSelectionElement {
    // use the default i18n package for "IE post no postdata when ssl timeout - message"
    private static final String PACKAGE = PackageUtil.getPackageName(I18nManager.class);

    private String[] values;
    private String[] keys;
    private int selected = -1;
    private int original = -1;

    /**
     * @param labelKey
     * @param keys
     * @param values
     */
    public StaticSingleSelectionElement(String labelKey, String[] keys, String[] values) {
        this.keys = keys;
        this.values = values;
        setLabelKey(labelKey);
    }

    /**
	 */
    @Override
    public String getKey(int which) {
        return keys[which];
    }

    /**
	 */
    @Override
    public String getValue(int which) {
        return values[which];
    }

    /**
	 */
    @Override
    public int getSize() {
        return keys.length;
    }

    /**
	 */
    @Override
    public boolean isSelected(int which) {
        return which == selected;
    }

    /**
	 */
    @Override
    public void setValues(String[] values) {
        if (values == null) { // no selection made (possible for radioboxes, but not
            // for dropdown list) -> selected = -1
            // selected = -1;
            throw new OLATRuntimeException(StaticSingleSelectionElement.class, "error.noformpostdata", null, PACKAGE,
                    "no value submitted!, name of element:" + getName(), null);
        }
        if (values.length != 1)
            throw new AssertException("got multiple values in singleselectionelement:" + getName());
        String key = values[0];
        selected = findPosByKey(key);
    }

    /**
	 */
    @Override
    public String getSelectedKey() {
        if (selected == -1)
            throw new AssertException("no key selected");
        return getKey(selected);
    }

    /**
	 */
    @Override
    public boolean isOneSelected() {
        return selected != -1;
    }

    /**
     * Returns the selected.
     * 
     * @return int
     */
    @Override
    public int getSelected() {
        return selected;
    }

    /**
	 */
    @Override
    public void select(String key, boolean select) {
        if (select)
            selected = findPosByKey(key);
        else
            selected = -1;
        // Remember original selection for dirty evaluation
        if (original == -1)
            original = selected;
    }

    private int findPosByKey(String key) {
        int iSelected = -1;
        boolean found = false;
        for (int i = 0; !found && i < keys.length; i++) {
            if (key.equals(keys[i])) {
                found = true;
                iSelected = i;
            }
        }
        if (!found)
            throw new RuntimeException("could not find key " + key);
        return iSelected;
    }

    /**
	 * 
	 *
	 */
    public void SingleSelectionElement() {
        //
    }

    /**
	 */
    @Override
    public boolean isDirty() {
        return (original == selected) ? false : true;
    }

}
