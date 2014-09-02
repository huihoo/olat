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
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.olat.lms.core.notification.service.PublishEventTO;
import org.olat.lms.core.notification.service.UserNotificationEventTO;
import org.olat.lms.folder.FolderNotificationTypeHandler;
import org.olat.presentation.dialogelements.DialogElementsNotificationTypeHandler;
import org.olat.presentation.forum.ForumNotificationTypeHandler;
import org.olat.presentation.framework.core.components.table.ColumnDescriptor;
import org.olat.presentation.framework.core.components.table.CustomCellRenderer;
import org.olat.presentation.framework.core.components.table.CustomRenderColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.webfeed.blog.BlogNotificationTypeHandler;
import org.olat.presentation.webfeed.podcast.PodcastNotificationTypeHandler;
import org.olat.presentation.wiki.WikiNotificationTypeHandler;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.StringHelper;

import com.ibm.icu.text.SimpleDateFormat;

/**
 * Initial Date: 15.03.2012 <br>
 * 
 * @author Branislav Balaz
 */
public class NotificationNewsTableDataModel extends DefaultTableDataModel<UserNotificationEventTO> {

    private Translator translator;
    private Collator collator;

    private static final int COLUMN_COUNT = 7;

    NotificationNewsTableDataModel(final Translator translator) {
        super(new ArrayList<UserNotificationEventTO>());
        this.translator = translator;
        this.setLocale(translator.getLocale());
        collator = Collator.getInstance(translator.getLocale());
    }

    /**
     * Add the column descriptors to the given table controller that matches with this data model
     * 
     * @param notificationNewsTableController
     */
    public void addTableColumns(final TableController notificationNewsTableController) {
        notificationNewsTableController.addColumnDescriptor(new DefaultColumnDescriptor("notification.news.creator.table.column.name", 0, null, getLocale()));
        notificationNewsTableController.addColumnDescriptor(new DefaultColumnDescriptor("notification.news.action.table.column.name", 1, null, getLocale()));
        notificationNewsTableController.addColumnDescriptor(new CustomRenderColumnDescriptor("notification.news.entry.table.column.name", 2, null, getLocale(),
                ColumnDescriptor.ALIGNMENT_LEFT, new EventSourceEntryCellRenderer()));
        notificationNewsTableController.addColumnDescriptor(new CustomRenderColumnDescriptor("notification.news.time.table.column.name", 3, null, getLocale(),
                ColumnDescriptor.ALIGNMENT_LEFT, new EventDateCellRenderer()));
        notificationNewsTableController.addColumnDescriptor(new DefaultColumnDescriptor("notification.news.type.table.column.name", 4, null, getLocale()));
        notificationNewsTableController.addColumnDescriptor(new CustomRenderColumnDescriptor("notification.news.typename.table.column.name", 5, null, getLocale(),
                ColumnDescriptor.ALIGNMENT_LEFT, new EventSourceCellRenderer()));
        notificationNewsTableController.addColumnDescriptor(new CustomRenderColumnDescriptor("notification.news.course.table.column.name", 6, null, getLocale(),
                ColumnDescriptor.ALIGNMENT_LEFT, new ContextLinkCellRenderer()));
        notificationNewsTableController.setSortColumn(3, false);
    }

    @Override
    public int getColumnCount() {
        return COLUMN_COUNT;
    }

    @Override
    public Object getValueAt(int row, int col) {
        final UserNotificationEventTO userNotificationEventTO = getObject(row);
        switch (col) {
        case 0:
            return userNotificationEventTO.getCreatorFirstLastName();
        case 1:
            return getAction(userNotificationEventTO.getEventType());
        case 2:
            return new UserNotificationEventTOComparableAfterSourceEntryTitle(userNotificationEventTO);
        case 3:
            return userNotificationEventTO.getCreationDate();
        case 4:
            return getType(userNotificationEventTO.getSourceType());
        case 5:
            return new UserNotificationEventTOComparableAfterSourceTitle(userNotificationEventTO);
        case 6:
            return new UserNotificationEventTOComparableAfterContextTitle(userNotificationEventTO);
        default:
            return "ERROR";
        }
    }

    private String getAction(PublishEventTO.EventType eventType) {
        String action = "";
        if (PublishEventTO.EventType.NEW.equals(eventType)) {
            action = translator.translate("notification.news.action.created.table.colum.value");
        } else if (PublishEventTO.EventType.CHANGED.equals(eventType)) {
            action = translator.translate("notification.news.action.changed.table.colum.value");
        }
        return action;
    }

    private String getType(String sourceType) {
        String typeTranslation = "";
        if (ForumNotificationTypeHandler.FORUM_SOURCE_TYPE.equals(sourceType)) {
            typeTranslation = translator.translate("notification.news.type.title_fo");
        } else if (WikiNotificationTypeHandler.WIKI_SOURCE_TYPE.equals(sourceType)) {
            typeTranslation = translator.translate("notification.news.type.title_wiki");
        } else if (DialogElementsNotificationTypeHandler.DIALOGELEMENTS_SOURCE_TYPE.equals(sourceType)) {
            typeTranslation = translator.translate("notification.news.type.title_dialog");
        } else if (BlogNotificationTypeHandler.BLOG_SOURCE_TYPE.equals(sourceType)) {
            typeTranslation = translator.translate("notification.news.type.title_blog");
        } else if (FolderNotificationTypeHandler.FOLDER_SOURCE_TYPE.equals(sourceType)) {
            typeTranslation = translator.translate("notification.news.type.title_bc");
        } else if (PodcastNotificationTypeHandler.PODCAST_SOURCE_TYPE.equals(sourceType)) {
            typeTranslation = translator.translate("notification.news.type.title_podcast");
        }
        return typeTranslation;
    }

    @Override
    public NotificationNewsTableDataModel createCopyWithEmptyList() {
        return new NotificationNewsTableDataModel(translator);
    }

    /**
     * Render the context/course title as a link.
     * 
     */
    class ContextLinkCellRenderer implements CustomCellRenderer {

        @Override
        public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
            UserNotificationEventTOComparableAfterContextTitle comparable = (UserNotificationEventTOComparableAfterContextTitle) val;
            UserNotificationEventTO notificationEvent = comparable.userNotificationEventTO;
            // title
            String title = notificationEvent.getContextTitle();
            title = StringHelper.escapeHtml(title);
            // link
            final String infoTitle = Formatter.truncate(title, 30);
            final String urlString = notificationEvent.getContextUrl();
            sb.append("<a href=\"").append(urlString).append("\"");
            sb.append(">").append(infoTitle).append("</a>");
        }
    }

    /**
     * Render the source title as a link.
     * 
     */
    class EventSourceCellRenderer implements CustomCellRenderer {

        @Override
        public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
            UserNotificationEventTOComparableAfterSourceTitle comparable = (UserNotificationEventTOComparableAfterSourceTitle) val;
            UserNotificationEventTO notificationEvent = comparable.userNotificationEventTO;
            // title
            String title = notificationEvent.getSourceTitle();
            title = StringHelper.escapeHtml(title);
            // link
            final String infoTitle = Formatter.truncate(title, 30);
            final String urlString = notificationEvent.getEventSourceUrl();
            sb.append("<a href=\"").append(urlString).append("\"");
            sb.append(">").append(infoTitle).append("</a>");
        }
    }

    /**
     * 
     * Render the source entry title as a link.
     */
    class EventSourceEntryCellRenderer implements CustomCellRenderer {

        @Override
        public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
            UserNotificationEventTOComparableAfterSourceEntryTitle comparable = (UserNotificationEventTOComparableAfterSourceEntryTitle) val;
            UserNotificationEventTO notificationEvent = comparable.userNotificationEventTO;
            // title
            String title = notificationEvent.getSourceEntryTitle();
            title = StringHelper.escapeHtml(title);
            // link
            final String infoTitle = Formatter.truncate(title, 30);
            final String urlString = notificationEvent.getEventSourceEntryUrl();
            sb.append("<a href=\"").append(urlString).append("\"");
            sb.append(">").append(infoTitle).append("</a>");
        }
    }

    /**
     * It needs a CustomCellRenderer since date sorting is delegated to tableModel.
     * 
     */
    class EventDateCellRenderer implements CustomCellRenderer {

        @Override
        public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
            Date creationDate = (Date) val;
            SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy", translator.getLocale());
            SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm", translator.getLocale());
            // title
            String dateInfo = formatDate.format(creationDate) + " " + formatTime.format(creationDate);
            sb.append(dateInfo);
        }
    }

    /**
     * Comparable used for sorting objects from column with index==2. This is a wrapper for UserNotificationEventTO objects.
     * 
     */
    class UserNotificationEventTOComparableAfterContextTitle implements Comparable<UserNotificationEventTOComparableAfterContextTitle> {
        UserNotificationEventTO userNotificationEventTO;

        UserNotificationEventTOComparableAfterContextTitle(UserNotificationEventTO userNotificationEventTO_) {
            userNotificationEventTO = userNotificationEventTO_;
        }

        /**
         * Sorting after the SourceEntryTitle.
         */
        @Override
        public int compareTo(UserNotificationEventTOComparableAfterContextTitle theOther) {
            String contextTitle1 = this.userNotificationEventTO.getContextTitle();
            String contextTitle2 = theOther.userNotificationEventTO.getContextTitle();
            // delegate to collator
            return collator.compare(contextTitle1, contextTitle2);
        }
    }

    /**
     * Comparable used for sorting objects from column with index==2. This is a wrapper for UserNotificationEventTO objects.
     * 
     */
    class UserNotificationEventTOComparableAfterSourceEntryTitle implements Comparable<UserNotificationEventTOComparableAfterSourceEntryTitle> {
        UserNotificationEventTO userNotificationEventTO;

        UserNotificationEventTOComparableAfterSourceEntryTitle(UserNotificationEventTO userNotificationEventTO_) {
            userNotificationEventTO = userNotificationEventTO_;
        }

        /**
         * Sorting after the SourceEntryTitle.
         */
        @Override
        public int compareTo(UserNotificationEventTOComparableAfterSourceEntryTitle theOther) {
            String sourceEntryTitle1 = this.userNotificationEventTO.getSourceEntryTitle();
            String sourceEntryTitle2 = theOther.userNotificationEventTO.getSourceEntryTitle();
            // delegate to collator
            return collator.compare(sourceEntryTitle1, sourceEntryTitle2);
        }
    }

    /**
     * Comparable used for sorting objects from column with index==5. This is a wrapper for UserNotificationEventTO objects.
     * 
     */
    class UserNotificationEventTOComparableAfterSourceTitle implements Comparable<UserNotificationEventTOComparableAfterSourceTitle> {
        UserNotificationEventTO userNotificationEventTO;

        UserNotificationEventTOComparableAfterSourceTitle(UserNotificationEventTO userNotificationEventTO_) {
            userNotificationEventTO = userNotificationEventTO_;
        }

        /**
         * Sorting after the SourceEntryTitle.
         */
        @Override
        public int compareTo(UserNotificationEventTOComparableAfterSourceTitle theOther) {
            String sourceEntryTitle1 = this.userNotificationEventTO.getSourceTitle();
            String sourceEntryTitle2 = theOther.userNotificationEventTO.getSourceTitle();
            // delegate to collator
            return collator.compare(sourceEntryTitle1, sourceEntryTitle2);
        }
    }

}
