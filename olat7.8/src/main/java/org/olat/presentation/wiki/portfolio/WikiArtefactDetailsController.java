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
package org.olat.presentation.wiki.portfolio;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.jamwiki.DataHandler;
import org.jamwiki.model.Topic;
import org.jamwiki.model.WikiFile;
import org.jamwiki.parser.AbstractParser;
import org.jamwiki.parser.ParserDocument;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.jflex.JFlexParser;
import org.olat.data.commons.filter.FilterFactory;
import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.data.portfolio.artefact.WikiArtefact;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Show the specific part of the WikiArtefact
 * <P>
 * Initial Date: 11 oct. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class WikiArtefactDetailsController extends BasicController {

    private static final Logger log = LoggerHelper.getLogger();

    private final VelocityContainer vC;

    WikiArtefactDetailsController(final UserRequest ureq, final WindowControl wControl, final AbstractArtefact artefact) {
        super(ureq, wControl);
        final WikiArtefact fArtefact = (WikiArtefact) artefact;
        vC = createVelocityContainer("details");
        final EPFrontendManager ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean(EPFrontendManager.class);
        final String wikiText = getContent(ePFMgr.getArtefactFullTextContent(fArtefact));
        vC.contextPut("text", wikiText);
        putInitialPanel(vC);
    }

    @Override
    @SuppressWarnings("unused")
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        //
    }

    @Override
    protected void doDispose() {
        //
    }

    private static String getContent(final String content) {
        try {
            final ParserInput input = new ParserInput();
            input.setWikiUser(null);
            input.setAllowSectionEdit(false);
            input.setDepth(2);
            input.setContext("");
            input.setLocale(Locale.ENGLISH);
            input.setTopicName("dummy");
            input.setUserIpAddress("0.0.0.0");
            input.setDataHandler(new DummyDataHandler());
            input.setVirtualWiki("/olat");

            final AbstractParser parser = new JFlexParser(input);
            final ParserDocument parsedDoc = parser.parseHTML(content);
            final String parsedContent = parsedDoc.getContent();
            String filteredContent = FilterFactory.getHtmlTagAndDescapingFilter().filter(parsedContent);
            filteredContent = StringHelper.escapeHtml(filteredContent);
            return filteredContent;
        } catch (final Exception e) {
            e.printStackTrace();
            log.error("", e);
            return content;
        }
    }

    static class DummyDataHandler implements DataHandler {

        @Override
        @SuppressWarnings("unused")
        public boolean exists(final String virtualWiki, final String topic) {
            return true;
        }

        @Override
        @SuppressWarnings("unused")
        public Topic lookupTopic(final String virtualWiki, final String topicName, final boolean deleteOK, final Object transactionObject) throws Exception {
            return null;
        }

        @Override
        @SuppressWarnings("unused")
        public WikiFile lookupWikiFile(final String virtualWiki, final String topicName) throws Exception {
            return null;
        }
    }

}
