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
package org.olat.lms.upgrade.upgrades;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.olat.system.logging.log4j.LoggerHelper;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * GUIPreferencesParser
 * 
 * <P>
 * Initial Date: 26.07.2011 <br>
 * 
 * @author guido
 */
public class GUIPreferencesParser {
    private static final Logger log = LoggerHelper.getLogger();

    private DocumentBuilderFactory domFactory;
    private DocumentBuilder builder;
    protected final String queryInfo = "org.olat.commons.info.notification.InfoSubscription::subs";
    protected final String queryInfoNot = "org.olat.commons.info.notification.InfoSubscription::notdesired";
    protected final String queryCal = "org.olat.course.run.calendar.CourseCalendarSubscription::subs";
    protected final String queryCalNot = "org.olat.course.run.calendar.CourseCalendarSubscription::notdesired";

    /**
	 * 
	 */
    public GUIPreferencesParser() {
        domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(false);
        try {
            builder = domFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            log.error("error while parsing gui prefs: ", e);
        }

    }

    public Document createDocument(String xmlContent) {
        StringReader reader = new StringReader(xmlContent);
        try {
            return builder.parse(new InputSource(reader));
        } catch (SAXException e) {
            log.error("error while parsing gui prefs: ", e);
        } catch (IOException e) {
            log.error("error while parsing gui prefs: ", e);
        }
        return null;
    }

    public Document createDocument(File xmlFile) {
        try {
            return builder.parse(xmlFile);
        } catch (SAXException e) {
            log.error("error while parsing gui prefs: ", e);
        } catch (IOException e) {
            log.error("error while parsing gui prefs: ", e);
        }
        return null;
    }

    public List<String> parseDataForInputQuery(Document doc, String query) {
        List<String> result = new ArrayList();
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            // XPath Query for showing all nodes value
            XPathExpression expr = xpath.compile("//org.olat.preferences.DbPrefs/prefstore/entry[string='" + query + "']/list/string/text()");

            Object results = expr.evaluate(doc, XPathConstants.NODESET);
            NodeList nodes = (NodeList) results;

            for (int i = 0; i < nodes.getLength(); i++) {
                result.add(nodes.item(i).getNodeValue());
            }
        } catch (XPathExpressionException e) {
            log.error("error while parsing gui prefs: ", e);
            return result;
        } catch (DOMException e) {
            log.error("error while parsing gui prefs: ", e);
            return result;
        }
        return result;

    }

}
