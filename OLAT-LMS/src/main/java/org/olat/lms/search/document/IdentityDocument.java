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
package org.olat.lms.search.document;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.filter.FilterFactory;
import org.olat.data.user.User;
import org.olat.lms.commons.i18n.I18nModule;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.user.HomePageConfig;
import org.olat.lms.user.HomePageConfigManager;
import org.olat.lms.user.HomePageConfigManagerImpl;
import org.olat.lms.user.UserService;
import org.olat.lms.user.propertyhandler.UserPropertyHandler;
import org.olat.presentation.framework.core.util.CSSHelper;
import org.olat.system.commons.StringHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * <h3>Description:</h3>
 * <p>
 * The IdentityDocument creates a search engine view for a certain identity
 * <p>
 * Initial Date: 21.08.2009 <br>
 * 
 * @author gnaegi, gnaegi@frentix.com, www.frentix.com
 */
public class IdentityDocument extends OlatDocument {
    private static final Logger log = LoggerHelper.getLogger();

    /**
     * Factory method to create a new IdentityDocument
     * 
     * @param searchResourceContext
     * @param wikiPage
     * @return
     */
    public static Document createDocument(final SearchResourceContext searchResourceContext, final Identity identity) {

        final User user = identity.getUser();

        final HomePageConfigManager homepageMgr = HomePageConfigManagerImpl.getInstance();
        final HomePageConfig publishConfig = homepageMgr.loadConfigFor(identity.getName());

        final IdentityDocument identityDocument = new IdentityDocument();
        identityDocument.setTitle(identity.getName());
        identityDocument.setCreatedDate(user.getCreationDate());

        // loop through all user properties and collect the content string and the last modified
        final List<UserPropertyHandler> userPropertyHanders = getUserService().getUserPropertyHandlersFor(IdentityDocument.class.getName(), false);
        final StringBuilder content = new StringBuilder();
        for (final UserPropertyHandler userPropertyHandler : userPropertyHanders) {
            final String propertyName = userPropertyHandler.getName();
            // only index fields the user has published!
            if (publishConfig.isEnabled(propertyName)) {
                final String value = getUserService().getUserProperty(user, propertyName, I18nModule.getDefaultLocale());
                if (value != null) {
                    content.append(value).append(" ");
                }
            }
        }
        // user text
        String text = publishConfig.getTextAboutMe();
        if (StringHelper.containsNonWhitespace(text)) {
            text = FilterFactory.getHtmlTagsFilter().filter(text);
            content.append(text).append(' ');
        }
        // finally use the properties as the content for this identity
        if (content.length() > 0) {
            identityDocument.setContent(content.toString());
        }

        identityDocument.setResourceUrl(searchResourceContext.getResourceUrl());
        identityDocument.setDocumentType(searchResourceContext.getParentContextType());
        identityDocument.setCssIcon(CSSHelper.CSS_CLASS_USER);

        if (log.isDebugEnabled()) {
            log.debug(identityDocument.toString());
        }
        return identityDocument.getLuceneDocument();
    }

    private static UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
