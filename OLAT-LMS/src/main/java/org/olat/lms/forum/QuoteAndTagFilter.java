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
 * frentix GmbH, Switzerland, http://www.frentix.com
 * <p>
 */
package org.olat.lms.forum;

import java.io.IOException;
import java.io.StringReader;

import org.apache.log4j.Logger;
import org.cyberneko.html.parsers.DOMParser;
import org.olat.data.commons.filter.Filter;
import org.olat.system.commons.StringHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class QuoteAndTagFilter implements Filter {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String QUOTE_WRAPPER = "b_quote_wrapper";

    /**
	 */
    @Override
    public String filter(final String original) {
        try {
            final DOMParser parser = new DOMParser();
            parser.parse(new InputSource(new StringReader(original)));
            final Document document = parser.getDocument();
            final StringBuilder sb = new StringBuilder();
            scanNode(document, sb);
            return sb.toString();
        } catch (final SAXException e) {
            log.error("", e);
            return null;
        } catch (final IOException e) {
            log.error("", e);
            return null;
        }
    }

    private void scanNode(final Node node, final StringBuilder sb) {
        for (Node child = node; child != null; child = child.getNextSibling()) {
            if (child.hasAttributes()) {
                final Node nodeclass = child.getAttributes().getNamedItem("class");
                if (nodeclass != null && QUOTE_WRAPPER.equals(nodeclass.getNodeValue())) {
                    continue;
                }
            }
            if (child.hasChildNodes()) {
                scanNode(child.getFirstChild(), sb);
            }
            if (child.getNodeType() == Node.TEXT_NODE) {
                final String text = child.getNodeValue();
                if (StringHelper.containsNonWhitespace(text)) {
                    if (sb.length() > 0) {
                        sb.append(' ');
                    }
                    sb.append(text);
                }
            }
        }
    }
}
