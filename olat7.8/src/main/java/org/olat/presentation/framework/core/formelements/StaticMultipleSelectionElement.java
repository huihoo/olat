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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.olat.system.exception.AssertException;
import org.olat.system.exception.OLATRuntimeException;

/**
 * @author Felix Jost
 */
public class StaticMultipleSelectionElement extends AbstractFormElement implements MultipleSelectionElement {
    private String[] values;
    private String[] keys;
    private Set selected;
    private boolean enableCheckAll;

    /**
     * @param labelKey
     * @param keys
     * @param values
     * @param enableCheckAll
     */
    public StaticMultipleSelectionElement(String labelKey, String[] keys, String[] values, boolean enableCheckAll) {
        this.keys = keys;
        this.values = values;
        this.enableCheckAll = enableCheckAll;
        setLabelKey(labelKey);
        selected = new HashSet();
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
        String key = getKey(which);
        return selected.contains(key);
    }

    /**
     * input: keys of selected checkboxes
     * 
     */
    @Override
    public void setValues(String[] values) {
        selected = new HashSet(3);
        if (values == null)
            return; // no selection made (no checkbox activated) ->
        // selection is empty
        // H: values != null
        for (int i = 0; i < values.length; i++) {
            String key = values[i];
            // prevent introducing fake keys
            int ksi = keys.length;
            boolean foundKey = false;
            int j = 0;
            while (!foundKey && j < ksi) {
                String eKey = keys[j];
                if (eKey.equals(key))
                    foundKey = true;
                j++;
            }
            if (!foundKey)
                throw new AssertException("submitted key '" + key + "' was not found in the keys of formelement named " + this.getName() + " , keys="
                        + Arrays.asList(keys));
            selected.add(key);
        }
    }

    /**
	 */
    @Override
    public Set getSelectedKeys() {
        return selected;
    }

    /**
	 */
    @Override
    public boolean isAtLeastSelected(int howmany, String errorKey) {
        boolean ok = selected.size() >= howmany;
        if (!ok)
            setErrorKey(errorKey);
        return ok;
    }

    /**
	 */
    @Override
    public void select(String key, boolean select) {
        if (select) {
            selected.add(key);
        } else {
            selected.remove(key);
        }
    }

    /**
	 */
    @Override
    public boolean isDirty() {
        throw new OLATRuntimeException(StaticMultipleSelectionElement.class, "isDirty not implemented for StaticMultipleSelectionElement", null);
    }

    /**
	 */
    @Override
    public boolean enableCheckAll() {
        return enableCheckAll;
    }

}
