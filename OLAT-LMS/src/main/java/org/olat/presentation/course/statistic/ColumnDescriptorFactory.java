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
package org.olat.presentation.course.statistic;

import java.text.SimpleDateFormat;

import org.olat.lms.course.statistic.IStatisticManager;
import org.olat.presentation.course.statistic.types.ColumnDescriptorProvider;
import org.olat.presentation.course.statistic.types.DailyStatisticColumnDescriptorProvider;
import org.olat.presentation.course.statistic.types.DayOfWeekStatisticColumnDescriptorProvider;
import org.olat.presentation.course.statistic.types.GeneralWeeklyStatisticColumnDescriptorProvider;
import org.olat.presentation.course.statistic.types.HomeOrgStatisticColumnDescriptorProvider;
import org.olat.presentation.course.statistic.types.HourOfDayStatisticColumnDescriptorProvider;
import org.olat.presentation.course.statistic.types.OrgTypeStatisticColumnDescriptorProvider;
import org.olat.presentation.course.statistic.types.StudyBranch3StatisticColumnDescriptorProvider;
import org.olat.presentation.course.statistic.types.StudyLevelStatisticColumnDescriptorProvider;
import org.olat.presentation.course.statistic.types.WeeklyStatisticColumnDescriptorProvider;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.table.ColumnDescriptor;

/**
 * Creates ColumnDescriptor for each IStatisticManager.STATISTIC_TYPE.
 * 
 * <P>
 * Initial Date: 05.04.2011 <br>
 * 
 * @author lavinia
 */
public class ColumnDescriptorFactory {

    /**
     * the SimpleDateFormat with which the column headers will be created formatted by the database, so change this in coordination with any db changes if you really need
     * to
     **/
    private static final SimpleDateFormat columnHeaderFormat_ = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public static ColumnDescriptor getColumnDescriptor(final UserRequest ureq, IStatisticManager.STATISTIC_TYPE type, final int column, final String headerId) {
        ColumnDescriptorProvider provider = null;
        if (IStatisticManager.STATISTIC_TYPE.DAILY.equals(type)) {
            provider = new DailyStatisticColumnDescriptorProvider();
        } else if (IStatisticManager.STATISTIC_TYPE.DAY_OF_WEEK.equals(type)) {
            provider = new DayOfWeekStatisticColumnDescriptorProvider();
        } else if (IStatisticManager.STATISTIC_TYPE.WEEKLY.equals(type)) {
            provider = new WeeklyStatisticColumnDescriptorProvider();
        } else if (IStatisticManager.STATISTIC_TYPE.GENERAL_WEEKLY.equals(type)) {
            provider = new GeneralWeeklyStatisticColumnDescriptorProvider();
        } else if (IStatisticManager.STATISTIC_TYPE.HOUR_OF_DAY.equals(type)) {
            provider = new HourOfDayStatisticColumnDescriptorProvider();
        } else if (IStatisticManager.STATISTIC_TYPE.HOME_ORG.equals(type)) {
            provider = new HomeOrgStatisticColumnDescriptorProvider();
        } else if (IStatisticManager.STATISTIC_TYPE.ORG_TYPE.equals(type)) {
            provider = new OrgTypeStatisticColumnDescriptorProvider();
        } else if (IStatisticManager.STATISTIC_TYPE.STUDY_BRANCH.equals(type)) {
            provider = new StudyBranch3StatisticColumnDescriptorProvider();
        } else if (IStatisticManager.STATISTIC_TYPE.STUDY_LEVEL.equals(type)) {
            provider = new StudyLevelStatisticColumnDescriptorProvider();
        }
        return provider.createColumnDescriptor(ureq, column, headerId);
    }
}
