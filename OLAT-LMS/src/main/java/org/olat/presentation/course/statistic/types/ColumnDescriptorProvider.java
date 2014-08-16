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
package org.olat.presentation.course.statistic.types;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.table.ColumnDescriptor;

/**
 * TODO: Class Description for ColumnDescriptorProvider
 * 
 * <P>
 * Initial Date: 05.04.2011 <br>
 * 
 * @author lavinia
 */
public interface ColumnDescriptorProvider {

    /**
     * Create a ColumnDescriptor for the given column (0 represents the course node, 1 and onward meaning columns number)
     * 
     * @param ureq
     *            the userrequest - used to get the Locale from
     * @param column
     *            the column - 0 represents the course node, 1 and onward is the column number
     * @param headerId
     *            more information about the column if contained in the StatisticResult - some implementors of IStatisticManager might choose to use this information,
     *            some might not.
     * @return a ColumnDescriptor which is then used to create the table
     */
    public ColumnDescriptor createColumnDescriptor(final UserRequest ureq, final int column, final String headerId);
}
