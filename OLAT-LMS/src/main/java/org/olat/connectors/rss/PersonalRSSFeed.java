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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.connectors.rss;

import java.util.ArrayList;
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.bookmark.Bookmark;
import org.olat.data.notifications.Subscriber;
import org.olat.lms.bookmark.BookmarkService;
import org.olat.lms.notifications.NotificationHelper;
import org.olat.lms.notifications.NotificationService;
import org.olat.lms.notifications.SubscriptionInfo;
import org.olat.lms.notifications.SubscriptionItem;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.framework.dispatcher.StaticMediaDispatcher;
import org.olat.system.commons.Settings;
import org.olat.system.spring.CoreSpringFactory;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.feed.synd.SyndImage;
import com.sun.syndication.feed.synd.SyndImageImpl;

/**
 * Description:<BR>
 * RSS document that contains the users notificatons.
 * <P>
 * Initial Date: Jan 11, 2005 2004
 * 
 * @author Florian Gnägi
 */

public class PersonalRSSFeed extends SyndFeedImpl {

    /**
     * Constructor for a RSS document that contains all the users personal notifications
     * 
     * @param identity
     *            The users identity
     * @param token
     *            The users RSS-authentication token
     */
    public PersonalRSSFeed(final Identity identity, final String token) {
        super();
        setFeedType("rss_2.0");
        setEncoding(RSSServlet.DEFAULT_ENCODING);

        final NotificationService man = getNotificationService();
        final Translator translator = man.getNotificationsTranslator(identity);
        final List<Bookmark> bookmarks = getBookmarkService().findBookmarksByIdentity(identity);

        setTitle(translator.translate("rss.title", new String[] { identity.getName() }));
        setLink(RSSUtil.URI_SERVER);
        setDescription(translator.translate("rss.description", new String[] { identity.getName() }));

        // create and add an image to the feed
        final SyndImage image = new SyndImageImpl();
        image.setUrl(Settings.createServerURI() + StaticMediaDispatcher.createStaticURIFor("images/olat/olatlogo32x32.png"));
        image.setTitle("OLAT - Online Learning And Training");
        image.setLink(getLink());
        setImage(image);

        final List<SyndEntry> entries = new ArrayList<SyndEntry>();
        final SyndEntry entry = new SyndEntryImpl();
        entry.setTitle(translator.translate("rss.olat.title", new String[] { NotificationHelper.getFormatedName(identity) }));
        entry.setLink(getLink());
        final SyndContent description = new SyndContentImpl();
        description.setType("text/plain");
        description.setValue(translator.translate("rss.olat.description"));
        entry.setDescription(description);
        entries.add(entry);

        // bookmark news
        for (final Bookmark bookmark : bookmarks) {
            final SyndEntry item = new SyndEntryImpl();
            item.setTitle(translator.translate("rss.bookmark.title", new String[] { bookmark.getTitle() }));
            final String itemLink = getBookmarkService().createJumpInURL(bookmark);
            item.setLink(itemLink);
            final SyndContent itemDescription = new SyndContentImpl();
            itemDescription.setType("text/plain");
            itemDescription.setValue(bookmark.getDescription());
            item.setDescription(itemDescription);
            entries.add(item);
        }

        // notification news
        // we are only interested in subscribers which listen to a valid publisher
        final List<Subscriber> subs = man.getValidSubscribers(identity);
        for (final Subscriber subscriber : subs) {
            final SubscriptionItem si = man.createSubscriptionItem(subscriber, translator.getLocale(), SubscriptionInfo.MIME_PLAIN, SubscriptionInfo.MIME_HTML);
            if (si != null) {
                final SyndEntry item = new SyndEntryImpl();
                item.setTitle(si.getTitle());
                item.setLink(si.getLink());
                final SyndContent itemDescription = new SyndContentImpl();
                itemDescription.setType(SubscriptionInfo.MIME_HTML);
                itemDescription.setValue(si.getDescription());
                item.setDescription(itemDescription);
                entries.add(item);
            }
        }
        setEntries(entries);
    }

    private NotificationService getNotificationService() {
        return (NotificationService) CoreSpringFactory.getBean(NotificationService.class);
    }

    private BookmarkService getBookmarkService() {
        return (BookmarkService) CoreSpringFactory.getBean(BookmarkService.class);
    }

}
