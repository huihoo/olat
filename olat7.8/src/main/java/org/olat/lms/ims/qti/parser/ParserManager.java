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

package org.olat.lms.ims.qti.parser;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * @author rkulow
 */
public class ParserManager implements IParser {

    private static final String PROPERTIES_FILENAME = "/org/olat/lms/ims/qti/parser/qtiparser.properties";
    private HashMap<String, String> parserMap;
    private static final String PARSER_DEFAULT = "defaultparser";
    private static final Logger log = LoggerHelper.getLogger();

    /**
	 * 
	 */
    public ParserManager() {
        this.init();
    }

    private void init() {
        this.parserMap = new HashMap<String, String>();

        try {
            final Properties prop = new Properties();
            Resource res = new ClassPathResource(PROPERTIES_FILENAME);
            prop.load(res.getInputStream());

            final Enumeration<Object> enumeration = prop.keys();
            while (enumeration.hasMoreElements()) {
                final String key = (String) enumeration.nextElement();
                final String value = prop.getProperty(key);
                this.parserMap.put(key, value);
            }
        } catch (final Exception e) {
            log.error("Error while loading qti parsers from parser properties file", e);
        }
        if (parserMap.size() == 0) {
            throw new OLATRuntimeException(this.getClass(), "Could not read item parsers list from:" + PROPERTIES_FILENAME, null);
        }

    }

    /**
     * @param doc
     * @return
     */
    public Object parse(final Document doc) {
        final Element rootElement = doc.getRootElement();
        return this.parse(rootElement);
    }

    /**
	 */
    @Override
    public Object parse(final Element element) {
        try {
            if (element == null) {
                return null;
            }
            final String name = element.getName();
            String parserClassName = null;
            final Object tmpName = this.parserMap.get(name);
            if (tmpName == null) {
                parserClassName = (String) this.parserMap.get(PARSER_DEFAULT);
            } else {
                parserClassName = (String) tmpName;
            }
            if (log.isDebugEnabled()) {
                log.debug("ELEMENTNAME:" + name + "PARSERNAME" + parserClassName);
            }
            final Class parserClass = this.getClass().getClassLoader().loadClass(parserClassName);
            final IParser parser = (IParser) parserClass.newInstance();
            return parser.parse(element);
        } catch (final ClassNotFoundException e) {
            throw new OLATRuntimeException(this.getClass(), "Class not found in QTI editor", e);
        } catch (final InstantiationException e) {
            throw new OLATRuntimeException(this.getClass(), "Instatiation problem in QTI editor", e);
        } catch (final IllegalAccessException e) {
            throw new OLATRuntimeException(this.getClass(), "Illegal Access in QTI editor", e);
        }
    }

}
