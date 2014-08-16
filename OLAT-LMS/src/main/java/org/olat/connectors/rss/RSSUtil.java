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

package org.olat.connectors.rss;

import org.olat.data.basesecurity.Authentication;
import org.olat.data.basesecurity.Identity;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.system.commons.Settings;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<BR>
 * Constants and helper methods for the OLAT RSS feeds
 * <P>
 * Initial Date: Jan 12, 2005
 * 
 * @author gnaegi
 */
public class RSSUtil {

    /** Authentication provider name for RSS authentication **/
    public static final String RSS_AUTH_PROVIDER = "RSS-OLAT";
    /** Key under which the users rss token is beeing kept in the http session **/
    public static final String RSS_AUTH_TOKEN_KEY = "rsstoken";
    /** path prefix for personal rss feed **/
    public static final String RSS_PREFIX_PERSONAL = "/personal/";
    /** path prefix for public rss feed **/
    public static final String RSS_PREFIX_PUBLIC = "/public/";

    /** OLAT server URI **/
    public static final String URI_SERVER;
    /** Personal rss channel URI prefix **/
    public static final String URI_PERSONAL_CHANNEL;
    /** Public rss channel URI prefix **/
    public static final String URI_PUBLIC_CHANNEL;
    static {
        URI_SERVER = Settings.getServerContextPathURI() + "/";
        URI_PERSONAL_CHANNEL = URI_SERVER + "rss" + RSS_PREFIX_PERSONAL;
        URI_PUBLIC_CHANNEL = URI_SERVER + "rss" + RSS_PREFIX_PUBLIC;
    }

    /**
     * Puts the users rss token into the httpsession. If no token is available one is generated and peristed in the database
     * 
     * @param ureq
     * @return String the token
     */
    public static String putPersonalRssTokenInSession(final UserRequest ureq) {
        final Identity identity = ureq.getIdentity();

        Authentication auth = getBaseSecurityEBL().findOrCreateAuthenticationWithRandomCredential_2(identity, RSSUtil.RSS_AUTH_PROVIDER);
        String token = null;
        if (auth != null) {
            token = auth.getCredential();
        }
        ureq.getUserSession().putEntry(RSSUtil.RSS_AUTH_TOKEN_KEY, token);
        return token;
    }

    private static BaseSecurityEBL getBaseSecurityEBL() {
        return CoreSpringFactory.getBean(BaseSecurityEBL.class);
    }

    /**
     * Calculates the absolute URL to the users personal rss feed
     * 
     * @param ureq
     * @return String
     */
    public static String getPersonalRssLink(final UserRequest ureq) {
        final String token = (String) ureq.getUserSession().getEntry(RSSUtil.RSS_AUTH_TOKEN_KEY);
        return (getPersonalRssLink(ureq.getIdentity(), token));
    }

    /**
     * Calculates the absolute URL to the users personal rss feed
     * 
     * @param identity
     * @param token
     * @return String
     */
    public static String getPersonalRssLink(final Identity identity, final String token) {
        final String link = RSSUtil.URI_PERSONAL_CHANNEL + identity.getName() + "/" + token + "/" + "olat.rss";
        return link;
    }
}
