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
package org.olat.presentation.notification;

import java.text.Collator;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.olat.lms.learn.notification.service.SubscriptionTO;
import org.olat.presentation.framework.core.components.table.ColumnDescriptor;
import org.olat.presentation.framework.core.components.table.CustomCellRenderer;
import org.olat.presentation.framework.core.components.table.CustomRenderColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.StringHelper;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Initial Date: 15.03.2012 <br>
 * 
 * @author Branislav Balaz
 */
public class NotificationSettingsTableDataModel extends DefaultTableDataModel {
    private static final Logger log = LoggerHelper.getLogger();

    private Translator translator;
    private Collator collator;

    NotificationSettingsTableDataModel(final Translator translator) {
        super(null);
        this.translator = translator;
        this.setLocale(translator.getLocale());
        collator = Collator.getInstance(translator.getLocale());
    }

    /**
     * Add the column descriptors to the given table controller that matches with this data model
     * 
     * @param notificationSettingsTableController
     */
    public void addTableColumns(final TableController notificationSettingsTableController) {
        // column 0 is the multiselect column
        notificationSettingsTableController.addColumnDescriptor(new DefaultColumnDescriptor("settings.table.column.name.type", 1, null, getLocale()));
        notificationSettingsTableController.addColumnDescriptor(new CustomRenderColumnDescriptor("settings.table.column.name.typename", 2, null, getLocale(),
                ColumnDescriptor.ALIGNMENT_LEFT, new PublisherSourceCellRenderer()));
        notificationSettingsTableController.addColumnDescriptor(new CustomRenderColumnDescriptor("settings.table.column.name.course", 3, null, getLocale(),
                ColumnDescriptor.ALIGNMENT_LEFT, new CourseCellRenderer()));
        // notificationSettingsTableController.addColumnDescriptor(new DefaultColumnDescriptor("settings.table.column.name.course", 3, null, getLocale()));
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public Object getValueAt(int row, int col) {
        final SubscriptionTO subscriptionTO = getObject(row);
        switch (col) {
        case 0:
            return subscriptionTO;
        case 1:
            // TODO: convert to icon
            return getSourceTypeTranslation(subscriptionTO.getSourceType());
        case 2:
            return new SubscriptionTOComparableAfterCourseNodeTitle(subscriptionTO);
        case 3:
            return new SubscriptionTOComparableAfterContextTitle(subscriptionTO);// return subscriptionTO.getCourseTitle(); //
        default:
            return "ERROR";
        }
    }

    private Object getSourceTypeTranslation(String sourceType) {
        return translator.translate("settings.table.subscription.type." + sourceType);
    }

    @Override
    public SubscriptionTO getObject(int row) {
        return (SubscriptionTO) super.getObject(row);
    }

    @Override
    public NotificationSettingsTableDataModel createCopyWithEmptyList() {
        return new NotificationSettingsTableDataModel(translator);
    }

    private void appendHtmlLink(StringOutput sb, final String title, final String url) {
        sb.append("<a href=\"").append(url).append("\"");
        sb.append(">").append(title).append("</a>");
    }

    class PublisherSourceCellRenderer implements CustomCellRenderer {

        @Override
        public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
            SubscriptionTOComparableAfterCourseNodeTitle comparable = (SubscriptionTOComparableAfterCourseNodeTitle) val;
            SubscriptionTO subscriptionTO = comparable.subscriptionTO;
            // title
            String title = subscriptionTO.getCourseNodeTitle();
            title = StringHelper.escapeHtml(title);
            // link
            final String infoTitle = Formatter.truncate(title, 30);
            final String urlString = subscriptionTO.getPublisherSourceUrl();
            appendHtmlLink(sb, infoTitle, urlString);
        }

    }

    class CourseCellRenderer implements CustomCellRenderer {

        @Override
        public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
            SubscriptionTOComparableAfterContextTitle comparable = (SubscriptionTOComparableAfterContextTitle) val;
            SubscriptionTO subscriptionTO = comparable.subscriptionTO;
            // title
            String title = subscriptionTO.getCourseTitle();
            title = StringHelper.escapeHtml(title);
            // link
            final String infoTitle = Formatter.truncate(title, 30);
            final String urlString = subscriptionTO.getContextUrl();
            appendHtmlLink(sb, infoTitle, urlString);
        }
    }

    class SubscriptionTOComparableAfterCourseNodeTitle implements Comparable<SubscriptionTOComparableAfterCourseNodeTitle> {
        SubscriptionTO subscriptionTO;

        SubscriptionTOComparableAfterCourseNodeTitle(SubscriptionTO subscriptionTO_) {
            subscriptionTO = subscriptionTO_;
        }

        /**
         * Sorting after the CourseNodeTitle.
         */
        @Override
        public int compareTo(SubscriptionTOComparableAfterCourseNodeTitle theOther) {
            String courseNodeTitle1 = this.subscriptionTO.getCourseNodeTitle();
            String courseNodeTitle2 = theOther.subscriptionTO.getCourseNodeTitle();
            // delegate to collator
            return collator.compare(courseNodeTitle1, courseNodeTitle2);
        }
    }

    class SubscriptionTOComparableAfterContextTitle implements Comparable<SubscriptionTOComparableAfterContextTitle> {
        SubscriptionTO subscriptionTO;

        SubscriptionTOComparableAfterContextTitle(SubscriptionTO subscriptionTO_) {
            subscriptionTO = subscriptionTO_;
        }

        @Override
        public int compareTo(SubscriptionTOComparableAfterContextTitle theOther) {
            String courseTitle1 = this.subscriptionTO.getCourseTitle();
            String courseTitle2 = theOther.subscriptionTO.getCourseTitle(); // delegate to collator
            return collator.compare(courseTitle1, courseTitle2);
        }
    }

}
