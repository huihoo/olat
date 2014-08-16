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

package org.olat.presentation.group.securitygroup;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.olat.data.basesecurity.Identity;
import org.olat.data.user.User;
import org.olat.lms.user.propertyhandler.UserPropertyHandler;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;

/**
 * Initial Date: Jul 29, 2003
 * 
 * @author Felix Jost, Florian Gnaegi
 */

public class IdentitiesOfGroupTableDataModel extends DefaultTableDataModel {
    private final List<UserPropertyHandler> userPropertyHandlers;

    /**
     * @param combo
     *            a List of Object[] with the array[0] = Identity, array[1] = addedToGroupTimestamp
     */
    public IdentitiesOfGroupTableDataModel(final List combo, final Locale locale, final List<UserPropertyHandler> userPropertyHandlers) {
        super(combo);
        setLocale(locale);
        this.userPropertyHandlers = userPropertyHandlers;
    }

    /**
	 */
    @Override
    public final Object getValueAt(final int row, final int col) {
        final Object[] co = (Object[]) getObject(row);
        final Identity identity = (Identity) co[0];
        final Date addedTo = (Date) co[1];
        final User user = identity.getUser();

        if (col == 0) {
            return identity.getName();

        } else if (col > 0 && col < userPropertyHandlers.size() + 1) {
            // get user property for this column
            final UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(col - 1);
            final String value = userPropertyHandler.getUserProperty(user, getLocale());
            return (value == null ? "n/a" : value);

        } else if (col == userPropertyHandlers.size() + 1) {
            return addedTo;

        } else {
            return "error";
        }
    }

    /**
	 */
    @Override
    public int getColumnCount() {
        return userPropertyHandlers.size() + 2;
    }

    /**
     * @param rowid
     * @return The identity at the given position in the dable
     */
    public Identity getIdentityAt(final int rowid) {
        final Object[] co = (Object[]) getObject(rowid);
        final Identity ident = (Identity) co[0];
        return ident;
    }

    /**
     * Return a list of identites for this bitset
     * 
     * @param objectMarkers
     * @return
     */
    public List<Identity> getIdentities(final BitSet objectMarkers) {
        final List<Identity> results = new ArrayList<Identity>();
        for (int i = objectMarkers.nextSetBit(0); i >= 0; i = objectMarkers.nextSetBit(i + 1)) {
            final Object[] elem = (Object[]) getObject(i);
            results.add((Identity) elem[0]);
        }
        return results;
    }

    /**
     * Remove identities from table-model.
     * 
     * @param toBeRemoved
     *            Remove this identities from table-model.
     */
    public void remove(final List<Identity> toBeRemoved) {
        for (final Identity identity : toBeRemoved) {
            remove(identity);
        }
    }

    /**
     * Remove an idenity from table-model.
     * 
     * @param ident
     */
    private void remove(final Identity ident) {
        for (final Iterator it_obj = getObjects().iterator(); it_obj.hasNext();) {
            final Object[] obj = (Object[]) it_obj.next();
            final Identity aIdent = (Identity) obj[0];
            if (aIdent == ident) {
                it_obj.remove();
                return;
            }
        }
    }

    /**
     * Add idenities to table-model.
     * 
     * @param addedIdentities
     *            Add thsi list of identities.
     */
    public void add(final List<Identity> addedIdentities) {
        for (final Identity identity : addedIdentities) {
            add(identity);
        }
    }

    /**
     * Add an idenity to table-model.
     * 
     * @param ident
     */
    private void add(final Identity identity) {
        getObjects().add(new Object[] { identity, new Date() });
    }

}
