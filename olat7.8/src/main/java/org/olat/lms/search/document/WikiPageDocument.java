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

package org.olat.lms.search.document;

import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.jamwiki.DataHandler;
import org.jamwiki.model.Topic;
import org.jamwiki.model.WikiFile;
import org.jamwiki.parser.AbstractParser;
import org.jamwiki.parser.ParserDocument;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.jflex.JFlexParser;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.filter.FilterFactory;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.wiki.WikiPage;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Lucene document mapper.
 * 
 * @author Christian Guretzki
 */
public class WikiPageDocument extends OlatDocument {

    private static final Logger log = LoggerHelper.getLogger();

    private static final DummyDataHandler DUMMY_DATA_HANDLER = new DummyDataHandler();

    private static BaseSecurity identityManager;

    public WikiPageDocument() {
        super();
        identityManager = (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
    }

    public static Document createDocument(final SearchResourceContext searchResourceContext, final WikiPage wikiPage) {
        final WikiPageDocument wikiPageDocument = new WikiPageDocument();

        final long userId = wikiPage.getInitalAuthor();
        if (userId != 0) {
            final Identity identity = identityManager.loadIdentityByKey(Long.valueOf(userId));
            wikiPageDocument.setAuthor(identity.getName());
        }
        wikiPageDocument.setTitle(wikiPage.getPageName());
        wikiPageDocument.setContent(getContent(wikiPage));
        wikiPageDocument.setCreatedDate(new Date(wikiPage.getCreationTime()));
        wikiPageDocument.setLastChange(new Date(wikiPage.getModificationTime()));
        wikiPageDocument.setResourceUrl(searchResourceContext.getResourceUrl());
        wikiPageDocument.setDocumentType(searchResourceContext.getDocumentType());
        wikiPageDocument.setCssIcon("o_wiki_icon");
        wikiPageDocument.setParentContextType(searchResourceContext.getParentContextType());
        wikiPageDocument.setParentContextName(searchResourceContext.getParentContextName());

        if (log.isDebugEnabled()) {
            log.debug(wikiPageDocument.toString());
        }
        return wikiPageDocument.getLuceneDocument();
    }

    private static String getContent(final WikiPage wikiPage) {
        try {
            final ParserInput input = new ParserInput();
            input.setWikiUser(null);
            input.setAllowSectionEdit(false);
            input.setDepth(2);
            input.setContext("");
            input.setLocale(Locale.ENGLISH);
            input.setTopicName("dummy");
            input.setUserIpAddress("0.0.0.0");
            input.setDataHandler(DUMMY_DATA_HANDLER);
            input.setVirtualWiki("/olat");

            final AbstractParser parser = new JFlexParser(input);
            final ParserDocument parsedDoc = parser.parseHTML(wikiPage.getContent());
            final String parsedContent = parsedDoc.getContent();
            final String filteredContent = FilterFactory.getHtmlTagAndDescapingFilter().filter(parsedContent);
            return filteredContent;
        } catch (final Exception e) {
            e.printStackTrace();
            log.error("", e);
            return wikiPage.getContent();
        }
    }

    private static class DummyDataHandler implements DataHandler {

        @Override
        public boolean exists(final String virtualWiki, final String topic) {
            return true;
        }

        @Override
        public Topic lookupTopic(final String virtualWiki, final String topicName, final boolean deleteOK, final Object transactionObject) throws Exception {
            return null;
        }

        @Override
        public WikiFile lookupWikiFile(final String virtualWiki, final String topicName) throws Exception {
            return null;
        }
    }
}
