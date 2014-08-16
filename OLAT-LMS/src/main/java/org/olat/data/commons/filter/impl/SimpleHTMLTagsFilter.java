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
package org.olat.data.commons.filter.impl;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.olat.data.commons.filter.Filter;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * The html tags filter takes a string and filters all HTML tags. The filter does not remove the code within the tags, only the tag itself. Example: '&lt;font
 * color="red"&gt;hello&lt;/font&gt;world' will become 'hello world'
 * <p>
 * The filter might not be perfect, its a simple version. All tag attributes will be removed as well.
 * <p>
 * Use the SimpleHTMLTagsFilterTest to add new testcases that must work with this filter.
 * <P>
 * Initial Date: 15.07.2009 <br>
 * 
 * @author gnaegi
 */
public class SimpleHTMLTagsFilter implements Filter {
    private static final Logger log = LoggerHelper.getLogger();

    // match <p> <p/> <br> <br/>
    private static final Pattern brAndPTagsPattern = Pattern.compile("<((br)|p|(BR)|P)( )*(/)?>");
    // match </h1>..
    private static final Pattern titleTagsPattern = Pattern.compile("</[hH][123456]>");
    // match everything <....>
    private static final Pattern stripHTMLTagsPattern = Pattern.compile("<(!|/)?\\w+((\\s+[\\w-]+(\\s*(=\\s*)?(?:\".*?\"|'.*?'|[^'\">\\s]+))?)+\\s*|\\s*)/?>");
    // match entities
    private static final Pattern htmlSpacePattern = Pattern.compile("&nbsp;");

    /**
	 */
    @Override
    public String filter(String original) {
        try {
            if (original == null)
                return null;
            // some strange chars let to infinite loop in the regexp and need to be replaced
            String modified = original.replaceAll("\u00a0", " ");
            modified = brAndPTagsPattern.matcher(modified).replaceAll(" ");
            modified = titleTagsPattern.matcher(modified).replaceAll(" ");
            if (log.isDebugEnabled())
                log.debug("trying to remove all html tags from: " + modified);
            modified = stripHTMLTagsPattern.matcher(modified).replaceAll("");
            modified = htmlSpacePattern.matcher(modified).replaceAll(" ");
            return modified;
        } catch (Throwable e) {
            log.error("Could not filter HTML tags. Using unfiltered string! Original string was::" + original, e);
            return original;
        }
    }
}
