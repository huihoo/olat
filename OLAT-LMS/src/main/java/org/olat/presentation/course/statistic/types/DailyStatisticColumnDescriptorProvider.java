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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.olat.presentation.course.statistic.StatisticDisplayController;
import org.olat.presentation.course.statistic.TotalAwareColumnDescriptor;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.table.ColumnDescriptor;

/**
 * TODO: Class Description for DailyStatisticColumnDescriptor
 * 
 * <P>
 * Initial Date: 05.04.2011 <br>
 * 
 * @author lavinia
 */
public class DailyStatisticColumnDescriptorProvider implements ColumnDescriptorProvider {

    /**
     * the SimpleDateFormat with which the column headers will be created formatted by the database, so change this in coordination with any db changes if you really need
     * to
     **/
    private final SimpleDateFormat columnHeaderFormat_ = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    /**
	 */
    @Override
    public ColumnDescriptor createColumnDescriptor(UserRequest ureq, int column, String headerId) {
        if (column == 0) {
            throw new IllegalStateException("column must never be 0 here");
        }

        String header = headerId;
        try {
            final Date d = columnHeaderFormat_.parse(headerId);

            final Calendar c = Calendar.getInstance(ureq.getLocale());
            c.setTime(d);
            final DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, ureq.getLocale());
            header = df.format(c.getTime());
        } catch (final ParseException pe) {
            // log.warn("createColumnDescriptor: ParseException while parsing " + headerId + ".", pe);
        }
        final TotalAwareColumnDescriptor cd = new TotalAwareColumnDescriptor(header, column, StatisticDisplayController.CLICK_TOTAL_ACTION + column, ureq.getLocale(),
                ColumnDescriptor.ALIGNMENT_RIGHT);
        cd.setTranslateHeaderKey(false);
        return cd;
    }

}
