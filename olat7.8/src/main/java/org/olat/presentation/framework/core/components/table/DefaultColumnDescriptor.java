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

import java.sql.Timestamp;
import java.text.Collator;
import java.util.Date;
import java.util.Locale;

import org.olat.presentation.framework.common.StringRenderer;
import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.OutputEscapeType;

/**
 * Description:<br>
 * 
 * @author Felix Jost
 */
public class DefaultColumnDescriptor implements ColumnDescriptor {

    public final static String DEFAULT_POPUP_ATTRIBUTES = "height=600, width=600, location=no, menubar=no, resizable=yes, status=no, scrollbars=yes, toolbar=no";
    private Formatter formatter;
    private String action;
    private String headerKey;
    private int alignment;
    private boolean popUpWindowAction;
    private String popUpWindowAttributes;
    private HrefGenerator hrefGenerator;
    // protected to allow overriding of compare method
    protected Locale locale;
    protected Collator collator;
    protected Table table;
    protected int dataColumn;
    private boolean translateHeaderKey = true;

    private OutputEscapeType outputEscapeType;

    /**
     * Constructor for table default column descriptor
     * 
     * @param headerKey
     *            translation key for column header
     * @param dataColumn
     *            position of column
     * @param action
     *            name of event that should be fired when rows column is clicken. null allowed for no action
     * @param locale
     *            the users locale
     */
    public DefaultColumnDescriptor(final String headerKey, final int dataColumn, final String action, final Locale locale) {
        this(headerKey, dataColumn, action, locale, ColumnDescriptor.ALIGNMENT_LEFT);
    }

    /**
     * This constructor allows to choose the outputEscapeType at rendering value.
     * 
     */
    public DefaultColumnDescriptor(final String headerKey, final int dataColumn, final String action, final Locale locale, final OutputEscapeType outputEscape) {
        this(headerKey, dataColumn, action, locale, ColumnDescriptor.ALIGNMENT_LEFT);
        this.outputEscapeType = outputEscape;
    }

    /**
     * @param headerKey
     * @param dataColumn
     * @param action
     * @param locale
     *            used ONLY for method getRenderValue in case the Object is of type Date to provide locale-sensitive Date formatting
     * @param alignment
     *            left, middle or right; constants in ColumnDescriptor
     */
    public DefaultColumnDescriptor(final String headerKey, final int dataColumn, final String action, final Locale locale, final int alignment) {
        this.dataColumn = dataColumn;
        this.headerKey = headerKey;
        this.action = action;
        this.locale = locale;
        this.alignment = alignment;
        if (locale != null) {
            formatter = Formatter.getInstance(locale);
            collator = Collator.getInstance(locale);
        }
    }

    /**
	 */
    @Override
    public String getHeaderKey() {
        return headerKey;
    }

    @Override
    public boolean translateHeaderKey() {
        return translateHeaderKey;
    }

    public void setTranslateHeaderKey(final boolean translateHeaderKey) {
        this.translateHeaderKey = translateHeaderKey;
    }

    /**
     * @param row
     *            the row in the table
     * @return the object to be rendered given the row
     */
    protected Object getModelData(final int row) {
        return table.getTableDataModel().getValueAt(table.getSortedRow(row), dataColumn);
    }

    /**
     * Renders value depending on the outputEscapeType.
     */
    @Override
    public void renderValue(final StringOutput sb, final int row, final Renderer renderer) {
        Object val = getModelData(row);
        String res;
        if (val == null) {
            return;
        }
        if (val instanceof Date) {
            res = formatter.formatDateAndTime((Date) val);
            sb.append(res);
        } else {
            StringRenderer.render(val.toString(), sb, outputEscapeType);
        }
    }

    /**
	 */
    @Override
    public int getAlignment() {
        return alignment;
    }

    /**
     * is called repeatedly caused by Collections.sort(...);
     * 
     */
    @Override
    public int compareTo(final int rowa, final int rowb) {
        Object a = table.getTableDataModel().getValueAt(rowa, dataColumn);
        Object b = table.getTableDataModel().getValueAt(rowb, dataColumn);
        // depending on the class of the Objects, we compare
        // FIXME:fj:c Use CollationKeys for Performance to compare Strings

        if (a == null || b == null) {
            return compareNullObjects(a, b);
        }
        if (a instanceof String && b instanceof String) {
            return collator.compare(a, b);
        } else if (a instanceof Comparable && b instanceof Comparable) {
            return compareComparablesAndTimestamps(a, b);
        } else if (a instanceof Boolean && b instanceof Boolean) { // faster than string compare
            return compareBooleansHandlingNulls(a, b);
        } else { // don't know how to compare, use the String value
            return a.toString().compareTo(b.toString());
        }
    }

    private int compareBooleansHandlingNulls(final Object a, final Object b) {
        boolean ba = ((Boolean) a).booleanValue();
        boolean bb = ((Boolean) b).booleanValue();
        return ba ? (bb ? 0 : -1) : (bb ? 1 : 0);
    }

    private int compareComparablesAndTimestamps(final Object a, final Object b) {
        // grmpf, we need to check on timestamp since Timestamp cannot compare dates (ClassCastException)
        // See also http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5103041 for the java 1.4/1.5 code bug
        if (a instanceof Timestamp) { // a timestamp (a) cannot compare a date (b), but vice versa is ok.
            Timestamp ta = (Timestamp) a;
            Date aAsDate = new Date(ta.getTime()); // nanos get lost here, but milis should be enough in most cases, TODO:fj:c better solution here
            return ((Comparable) aAsDate).compareTo(b);
            // TODO:fj:a see also the todo in AuditInterceptor.java!
        } else {
            return ((Comparable) a).compareTo(b);
        }
    }

    private int compareNullObjects(final Object a, final Object b) {
        boolean ba = (a == null);
        boolean bb = (b == null);
        return ba ? (bb ? 0 : -1) : (bb ? 1 : 0);
    }

    /**
	 */
    @Override
    public void setTable(final Table table) {
        this.table = table;
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
     * @return Table
     */
    protected Table getTable() {
        return table;
    }

    /**
	 */
    @Override
    public void modelChanged() {
        // empty
    }

    /**
	 */
    @Override
    public void sortingAboutToStart() {
        // empty
    }

    /**
     * @return int
     */
    protected int getDataColumn() {
        return dataColumn;
    }

    /**
	 */
    @Override
    public void otherColumnDescriptorSorted() {
        // empty
    }

    /**
	 */
    @Override
    public boolean isSortingAllowed() {
        return true;
    }

    /**
     * @return Locale
     */
    public Locale getLocale() {
        return locale;
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
     * TODO:fj:b replace with PopupObject which is easily configurable Optional action link configuration
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

    /**
     * @return Returns the hrefGenerator.
     */
    @Override
    public HrefGenerator getHrefGenerator() {
        return hrefGenerator;
    }

    /**
     * @param hrefGenerator
     *            The hrefGenerator to set.
     */
    @Override
    public void setHrefGenerator(final HrefGenerator hrefGenerator) {
        this.hrefGenerator = hrefGenerator;
    }

    @Override
    public String toString(final int rowid) {
        Object obj = getModelData(rowid);
        if (obj instanceof Date) {
            return "[date]";
        }
        StringOutput sb = new StringOutput();
        renderValue(sb, rowid, null);
        return sb.toString();
    }

    public void setOutputEscape(OutputEscapeType outputEscape) {
        this.outputEscapeType = outputEscape;
    }

}
