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
package org.olat.presentation.wiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olat.data.commons.filter.FilterFactory;

/**
 * Initial Date: Jun 20, 2014 <br>
 * 
 * @author Branislav Balaz
 * 
 *         This class has been created as solution for WIKI XSS problem (issue OLAT-6974). To be sure that all wiki content is processed it is used in classes:
 * 
 *         WikiEditArticleForm - method getWikiContent()
 * 
 *         WikiPage - method getContent()
 * 
 * 
 */
public class WikiXSSProcessor {

    // used to find link content in wiki issue OLAT-6974
    private static final Pattern pattern = Pattern.compile("(?<=\\[\\[)[^]]+(?=\\]\\])");

    public static String getFilteredWikiContent(String wikiContent) {
        Matcher m = pattern.matcher(wikiContent);
        StringBuffer sb = new StringBuffer(wikiContent.length());
        while (m.find()) {
            String linkContent = m.group(0);
            String filteredLinkContent = FilterFactory.filterXSS(linkContent);
            m.appendReplacement(sb, Matcher.quoteReplacement(filteredLinkContent));
        }
        m.appendTail(sb);
        return sb.toString();
    }

}
