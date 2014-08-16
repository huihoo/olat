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

package org.olat.presentation.framework.core.components.table;

import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.system.exception.AssertException;

/**
 * Description:<br>
 * The static column descriptor displays the same value on every row. Usually this is used to execute an action like 'edit this record'
 * <P>
 * 
 * @author Felix Jost
 */
public class StaticColumnDescriptor implements ColumnDescriptor {
    private String headerKey;
    private int alignment;
    private String action;
    private String cellValue;
    private boolean popUpWindowAction;
    private String popUpWindowAttributes;
    private HrefGenerator hrefGenerator;
    private boolean translateHeaderKey = true;

    /**
     * Constructor for StaticColumnDescriptor. The default alignment ist left
     * 
     * @param action
     * @param headerKey
     * @param cellValue
     */
    public StaticColumnDescriptor(final String action, final String headerKey, final String cellValue) {
        this.action = action;
        this.headerKey = headerKey;
        this.cellValue = cellValue;
        this.alignment = ALIGNMENT_LEFT;
    }

    /**
	 */
    @Override
    public String getHeaderKey() {
        return headerKey;
    }

    /**
	 */
    @Override
    public boolean translateHeaderKey() {
        return translateHeaderKey;
    }

    /**
     * Option to set the flag to not translate the header key. In this case, the header key value is actually not a key but rather an already translated value
     * 
     * @param translateHeaderKey
     */
    public void setTranslateHeaderKey(final boolean translateHeaderKey) {
        this.translateHeaderKey = translateHeaderKey;
    }

    /**
	 */
    @Override
    public int getAlignment() {
        return alignment;
    }

    /**
	 */
    @Override
    public void renderValue(final StringOutput so, final int row, final Renderer renderer) {
        so.append(cellValue);
    }

    /**
	 */
    @Override
    public int compareTo(final int rowa, final int rowb) {
        throw new AssertException("staticcolumndescriptor was called to be sorted, but did not offer to be sorted");
    }

    /**
	 */
    @Override
    public void setTable(final Table arg0) {
        // not needed here, ignore
    }

    /**
	 */
    @Override
    public String getAction(final int row) {
        return action;
    }

    /**
     * Sets the alignment.
     * 
     * @param alignment
     *            The alignment to set
     */
    public void setAlignment(final int alignment) {
        this.alignment = alignment;
    }

    /**
	 */
    @Override
    public void modelChanged() {
        //
    }

    /**
	 */
    @Override
    public void sortingAboutToStart() {
        //
    }

    /**
	 */
    @Override
    public void otherColumnDescriptorSorted() {
        //
    }

    /**
	 */
    @Override
    public boolean isSortingAllowed() {
        return false;
    }

    /**
	 */
    @Override
    public boolean isPopUpWindowAction() {
        return popUpWindowAction;
    }

    /**
	 */
    @Override
    public String getPopUpWindowAttributes() {
        return popUpWindowAttributes;
    }

    /**
     * Optional action link configuration
     * 
     * @param popUpWindowAction
     *            true: action link will open in new window, false: action opens in current window
     * @param popUpWindowAttributes
     *            javascript window.open attributes or null if default values are used e.g. something like this:
     *            "height=600, width=600, location=no, menubar=no, resizable=yes, status=no, scrollbars=yes, toolbar=no"
     */
    public void setIsPopUpWindowAction(final boolean popUpWindowAction, final String popUpWindowAttributes) {
        this.popUpWindowAction = popUpWindowAction;
        this.popUpWindowAttributes = popUpWindowAttributes;
    }

    @Override
    public void setHrefGenerator(final HrefGenerator h) {
        this.hrefGenerator = h;
    }

    @Override
    public HrefGenerator getHrefGenerator() {
        return hrefGenerator;
    }

    @Override
    public String toString(final int rowid) {
        StringOutput sb = new StringOutput();
        renderValue(sb, rowid, null);
        return sb.toString();
    }

}
