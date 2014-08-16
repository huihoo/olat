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
 * Switzerland.
 * <p>
 */
package org.olat.presentation.repository;

import java.util.Locale;

import org.olat.data.repository.RepositoryEntry;
import org.olat.presentation.framework.core.components.table.CustomRenderColumnDescriptor;

/**
 * Description:<br>
 * This repository entry type column descriptor displays a CSS icon to represent the resource type. The underlying data model must provide an object of type
 * RepositoryEntry
 * <P>
 * Initial Date: 16.04.2008 <br>
 * 
 * @author Florian Gnägi, http://www.frentix.com
 */
public class RepositoryEntryTypeColumnDescriptor extends CustomRenderColumnDescriptor {
    Locale locale;

    /**
     * Constructor for this repo entry type column descriptor.
     * 
     * @param headerKey
     * @param dataColumn
     * @param action
     * @param locale
     * @param aligment
     */
    public RepositoryEntryTypeColumnDescriptor(final String headerKey, final int dataColumn, final String action, final Locale locale, final int aligment) {
        super(headerKey, dataColumn, action, locale, aligment, new RepositoryEntryIconRenderer(locale));
    }

    /**
     * We override the compare method because we want to sort on the resourceable type name of the contained olat resource and not on the object. Alternatively we could
     * have implemented the Comparable interface on the Repository entry, however this would have been missleading because this compare does not compare the repository
     * entries itself but only the resource type names.
     * 
     */
    @Override
    public int compareTo(final int rowa, final int rowb) {
        final RepositoryEntry a = (RepositoryEntry) table.getTableDataModel().getValueAt(rowa, dataColumn);
        final RepositoryEntry b = (RepositoryEntry) table.getTableDataModel().getValueAt(rowb, dataColumn);
        // compare is based on repository entries resourceable type name
        if (a == null || b == null) {
            final boolean ba = (a == null);
            final boolean bb = (b == null);
            final int res = ba ? (bb ? 0 : -1) : (bb ? 1 : 0);
            return res;
        }
        final int com = collator.compare(a.getOlatResource().getResourceableTypeName(), b.getOlatResource().getResourceableTypeName());
        return com;
    }

}
