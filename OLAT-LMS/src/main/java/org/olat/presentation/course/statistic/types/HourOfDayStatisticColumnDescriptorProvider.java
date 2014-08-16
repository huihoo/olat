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

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import org.olat.presentation.course.statistic.StatisticDisplayController;
import org.olat.presentation.course.statistic.TotalAwareColumnDescriptor;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.table.ColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;

/**
 * TODO: Class Description for HourOfDayStatisticColumnDescriptor
 * 
 * <P>
 * Initial Date: 05.04.2011 <br>
 * 
 * @author lavinia
 */
public class HourOfDayStatisticColumnDescriptorProvider implements ColumnDescriptorProvider {

    @Override
    public ColumnDescriptor createColumnDescriptor(final UserRequest ureq, final int column, final String headerId) {
        if (column == 0) {
            return new DefaultColumnDescriptor("stat.table.header.node", 0, null, ureq.getLocale());
        }
        String hourOfDayLocaled = headerId;

        try {
            final Calendar c = Calendar.getInstance(ureq.getLocale());
            c.setTime(new Date());
            c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(headerId));
            c.set(Calendar.MINUTE, 0);
            final DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT, ureq.getLocale());
            hourOfDayLocaled = df.format(c.getTime());
        } catch (final RuntimeException re) {
            re.printStackTrace(System.out);
        }

        final TotalAwareColumnDescriptor cd = new TotalAwareColumnDescriptor(hourOfDayLocaled, column, StatisticDisplayController.CLICK_TOTAL_ACTION + column,
                ureq.getLocale(), ColumnDescriptor.ALIGNMENT_RIGHT);
        cd.setTranslateHeaderKey(false);
        return cd;
    }
}
