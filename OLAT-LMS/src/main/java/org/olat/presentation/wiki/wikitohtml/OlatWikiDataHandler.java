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
package org.olat.presentation.wiki.wikitohtml;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.log4j.Logger;
import org.jamwiki.DataHandler;
import org.jamwiki.model.Topic;
import org.jamwiki.model.WikiFile;
import org.jamwiki.utils.InterWikiHandler;
import org.jamwiki.utils.PseudoTopicHandler;
import org.olat.lms.wiki.Wiki;
import org.olat.lms.wiki.WikiManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.util.StringUtils;

/**
 * Implementation of the Datahandler Interface from the jamwiki engine. It provides methods of checking whether a wiki topic exists and for lookup of files like images
 * used in a wiki topic.
 * 
 * @author guido
 */
public class OlatWikiDataHandler implements DataHandler {

    private static final Logger log = LoggerHelper.getLogger();

    private final OLATResourceable ores;
    private final String imageUri;
    private final String IMAGE_NAMESPACE = "Image:";
    private final String MEDIA_NAMESPACE = "Media:";

    /**
     * @param ores
     * @param imageUri
     */
    protected OlatWikiDataHandler(final OLATResourceable ores, final String imageUri) {
        this.ores = ores;
        this.imageUri = imageUri;
    }

    /**
	 */
    @Override
    public Topic lookupTopic(final String virtualWiki, final String topicName, final boolean deleteOK, final Object transactionObject) throws Exception {
        String decodedName = null;

        final Wiki wiki = WikiManager.getInstance().getOrLoadWiki(ores);
        try {
            decodedName = URLDecoder.decode(topicName, "utf-8");
        } catch (final UnsupportedEncodingException e) {
            //
        }
        if (log.isDebugEnabled()) {
            log.debug("page name not normalized: " + topicName);
            log.debug("page name normalized: " + FilterUtil.normalizeWikiLink(topicName));
            try {
                log.debug("page name urldecoded name: " + URLDecoder.decode(topicName, "utf-8"));
                log.debug("page name urldecoded and normalized: " + FilterUtil.normalizeWikiLink(URLDecoder.decode(topicName, "utf-8")));
                log.debug("page name urldecoded normalized and transformed to id: " + wiki.generatePageId(FilterUtil.normalizeWikiLink(decodedName)));
            } catch (final UnsupportedEncodingException e) {
                //
            }
        }
        final Topic topic = new Topic();
        if (decodedName.startsWith(IMAGE_NAMESPACE)) {
            final String imageName = topicName.substring(IMAGE_NAMESPACE.length());
            if (!wiki.mediaFileExists(imageName)) {
                return null;
            }
            topic.setName(imageName);
            topic.setTopicType(Topic.TYPE_IMAGE);
            return topic;
        } else if (decodedName.startsWith(MEDIA_NAMESPACE)) {
            final String mediaName = topicName.substring(MEDIA_NAMESPACE.length(), topicName.length());
            if (!wiki.mediaFileExists(mediaName)) {
                return null;
            }
            topic.setName(mediaName);
            topic.setTopicType(Topic.TYPE_FILE);
            return topic;
        }
        if (wiki.pageExists(wiki.generatePageId(FilterUtil.normalizeWikiLink(decodedName)))) {
            topic.setName(topicName);
            return topic;
        }
        return null;
    }

    /**
	 */
    @Override
    public WikiFile lookupWikiFile(final String virtualWiki, String topicName) throws Exception {
        final WikiFile wikifile = new WikiFile();
        if (topicName.startsWith(IMAGE_NAMESPACE)) {
            topicName = topicName.substring(IMAGE_NAMESPACE.length());
        } else if (topicName.startsWith(MEDIA_NAMESPACE)) {
            topicName = topicName.substring(MEDIA_NAMESPACE.length(), topicName.length());
        }
        topicName = topicName.replace(" ", "_"); // topic name comes in with "_" replaced as normal but it the image case it does not make sense
        wikifile.setFileName(topicName);
        wikifile.setUrl(this.imageUri + topicName);
        wikifile.setAbsUrl(WikiManager.getInstance().getMediaFolder(ores).getBasefile().getAbsolutePath());
        return wikifile;
    }

    /**
	 */
    @Override
    public boolean exists(final String virtualWiki, final String topic) {
        if (!StringUtils.hasText(topic)) {
            return false;
        }
        if (PseudoTopicHandler.isPseudoTopic(topic)) {
            return true;
        }
        if (InterWikiHandler.isInterWiki(topic)) {
            return true;
        }

        // try {
        // Utilities.validateTopicName(topic);
        // } catch (WikiException e) {
        // throw new OLATRuntimeException(this.getClass(), "invalid topic name!", e);
        // }

        final Wiki wiki = WikiManager.getInstance().getOrLoadWiki(ores);
        if (topic.startsWith(IMAGE_NAMESPACE) || topic.startsWith(MEDIA_NAMESPACE)) {
            return wiki.pageExists(topic);
        }
        final String pageId = WikiManager.generatePageId(FilterUtil.normalizeWikiLink(topic));
        return wiki.pageExists(pageId);
    }

}
