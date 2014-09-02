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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.velocity.context.Context;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.lms.commons.i18n.I18nModule;
import org.olat.lms.search.SearchResourceContext;
import org.olat.presentation.framework.common.contextHelp.ContextHelpDispatcher;
import org.olat.presentation.framework.core.render.velocity.VelocityHelper;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * The context help document indexes a context sensitive help page
 * <P>
 * Initial Date: 05.11.2008 <br>
 * 
 * @author gnaegi
 */
public class ContextHelpDocument extends OlatDocument {

    // Must correspond with org/olat/presentation/search/_i18n/LocalStrings_xx.properties
    // Do not use '_' because Lucene has problems with it
    public static final String TYPE = "type.contexthelp";

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("</?[a-zA-Z0-9]+\\b[^>]*>");

    private static final Pattern HTML_SPACE_PATTERN = Pattern.compile("&nbsp;");

    private static final Logger log = LoggerHelper.getLogger();

    /**
     * Factory method to create a search document for a context sensitive help page
     * 
     * @param searchResourceContext
     * @param bundleName
     * @param page
     * @param pageTranslator
     * @param ctx
     * @param pagePath
     * @return
     */
    public static Document createDocument(final SearchResourceContext searchResourceContext, final String bundleName, final String page, final Translator pageTranslator,
            final Context ctx, final String pagePath) {
        final ContextHelpDocument contextHelpDocument = new ContextHelpDocument();
        final I18nManager i18nMgr = I18nManager.getInstance();

        // Set all know attributes
        searchResourceContext.setFilePath(ContextHelpDispatcher.createContextHelpURI(pageTranslator.getLocale(), bundleName, page));
        contextHelpDocument.setResourceUrl(searchResourceContext.getResourceUrl());// to adhere to the [path=...] convention
        contextHelpDocument.setLastChange(new Date(i18nMgr.getLastModifiedDate(pageTranslator.getLocale(), bundleName)));
        final String lang = I18nManager.getInstance().getLanguageTranslated(pageTranslator.getLocale().toString(), I18nModule.isOverlayEnabled());
        contextHelpDocument.setDocumentType(TYPE);
        contextHelpDocument.setCssIcon("b_contexthelp_icon");
        contextHelpDocument.setTitle(pageTranslator.translate("chelp." + page.split("\\.")[0] + ".title") + " (" + lang + ")");

        final VelocityHelper vh = (VelocityHelper) CoreSpringFactory.getBean(VelocityHelper.class);
        String mergedContent = vh.mergeContent(pagePath, ctx, null);
        // Remove any HTML stuff from page
        Matcher m = HTML_TAG_PATTERN.matcher(mergedContent);
        mergedContent = m.replaceAll(" ");
        // Remove all &nbsp
        m = HTML_SPACE_PATTERN.matcher(mergedContent);
        mergedContent = m.replaceAll(" ");
        // Finally set content
        contextHelpDocument.setContent(mergedContent);

        if (log.isDebugEnabled()) {
            log.debug(contextHelpDocument.toString());
        }
        return contextHelpDocument.getLuceneDocument();
    }

}
