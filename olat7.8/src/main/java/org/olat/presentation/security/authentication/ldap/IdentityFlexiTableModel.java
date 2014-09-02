package org.olat.presentation.security.authentication.ldap;

import java.util.List;

import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;

public class IdentityFlexiTableModel extends DefaultTableDataModel {
    private int columnCount = 0;

    public IdentityFlexiTableModel(final List objects, final int columnCount) {
        super(objects);
        this.columnCount = columnCount;
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public Object getValueAt(final int row, final int col) {
        final List entry = (List) objects.get(row);
        final Object value = entry.get(col);
        return (value == null ? "n/a" : value);
    }
}
