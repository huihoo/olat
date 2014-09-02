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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.cyberneko.html.parsers.SAXParser;
import org.olat.data.commons.filter.Filter;
import org.olat.system.logging.log4j.LoggerHelper;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Description:<br>
 * Filter the HTML code using Neko SAX parser (http://nekohtml.sourceforge.net) and extract the content. Neko parses the HTML entities too and delivers cleaned text.
 * <P>
 * Initial Date: 2 dec. 2009 <br>
 * 
 * @author srosse
 */
public class NekoHTMLFilter implements Filter {

    private static final Logger log = LoggerHelper.getLogger();

    public static final Set<String> blockTags = new HashSet<String>();
    static {
        blockTags.addAll(Arrays.asList("address", "blockquote", "br", "dir", "div", "dl", "fieldset", "form", "h1", "h2", "h3", "h4", "h5", "h6", "hr", "noframes",
                "noscript", "ol", "p", "pre", "table", "ul", "li"));
    }

    @Override
    public String filter(String original) {
        if (original == null)
            return null;
        try {
            SAXParser parser = new SAXParser();
            HTMLHandler contentHandler = new HTMLHandler((int) (original.length() * 0.66f));
            parser.setContentHandler(contentHandler);
            parser.parse(new InputSource(new StringReader(original)));
            return contentHandler.toString();
        } catch (SAXException e) {
            log.error("", e);
            return null;
        } catch (IOException e) {
            log.error("", e);
            return null;
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
    }

    public String filter(InputStream in) {
        if (in == null)
            return null;
        try {
            SAXParser parser = new SAXParser();
            HTMLHandler contentHandler = new HTMLHandler((int) (1000 * 0.66f));
            parser.setContentHandler(contentHandler);
            parser.parse(new InputSource(in));
            return contentHandler.toString();
        } catch (SAXException e) {
            log.error("", e);
            return null;
        } catch (IOException e) {
            log.error("", e);
            return null;
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
    }

    private class HTMLHandler extends DefaultHandler {
        private boolean collect = true;
        private boolean consumeBlanck = false;
        private final StringBuilder sb;

        public HTMLHandler(int size) {
            sb = new StringBuilder(size);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            String elem = localName.toLowerCase();
            if ("script".equals(elem)) {
                collect = false;
                // add a single whitespace before each block element but only if not there is not already a whitespace there
            } else if (blockTags.contains(elem) && sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
                consumeBlanck = true;
            }
        }

        @Override
        public void characters(char[] chars, int offset, int length) {
            if (collect) {
                if (consumeBlanck) {
                    if (sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ' && length > 0 && chars[offset] != ' ') {
                        sb.append(' ');
                    }
                    consumeBlanck = false;
                }
                sb.append(chars, offset, length);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            String elem = localName.toLowerCase();
            if ("script".equals(elem)) {
                collect = true;
            } else if (blockTags.contains(elem) && sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
                consumeBlanck = true;
            }
        }

        @Override
        public String toString() {
            return sb.toString();
        }
    }
}
