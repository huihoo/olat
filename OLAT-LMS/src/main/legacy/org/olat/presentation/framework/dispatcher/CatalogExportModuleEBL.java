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
package org.olat.lms.framework.dispatcher;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.lms.commons.mediaresource.FileMediaResource;
import org.olat.system.commons.WebappHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

/**
 * Initial Date: 20.10.2011 <br>
 * 
 * @author Branislav Balaz
 */
@Component
@Scope("prototype")
public class CatalogExportModuleEBL {

    private static final String OLD_FILE_SUFFIX = ".old";
    private static final String ENCODING_PROPERTY_VALUE = "utf-8";
    private static final String VERSION_PROPERTY_VALUE = "1.0";
    private static final String METHOD_PROPERTY_VALUE = "xml";
    private static final String YES_PROPERTY_VALUE = "yes";
    private static final Logger log = LoggerHelper.getLogger();
    private static final String XML_FILE = "catalog.xml";
    private static final String SYSTEM_DIR = "system";
    private boolean inAccess = false;

    public void transformCatalogXml(Document document) throws FileNotFoundException {

        try {
            final File systemDir = new File(WebappHelper.getUserDataRoot(), SYSTEM_DIR); // destination is .../olatdata/system/catalog.xml
            final File xmlFile = new File(systemDir, XML_FILE);
            final File oldXmlFile = new File(systemDir, XML_FILE + OLD_FILE_SUFFIX);
            final Result destination = getDestination(xmlFile, oldXmlFile);
            final Source source = new DOMSource(document);
            Transformer transformer = getTransformer();
            transformXml(source, destination, transformer);
            log.debug("                                ...done");
        } catch (final Exception e) {
            log.error("Error writing catalog export file.", e);
        }

    }

    private Result getDestination(final File file, final File oldFile) throws FileNotFoundException {
        OutputStream outputStream = getOutputStream(file, oldFile);
        final Result destination = new StreamResult(outputStream);
        return destination;
    }

    private OutputStream getOutputStream(final File file, final File oldFile) throws FileNotFoundException {
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(oldFile));
        try {
            final InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            FileUtils.copy(inputStream, outputStream); // copy old version for access in the meantime
        } catch (final Exception e) {
            log.warn("initial call of this method or fs error: catalog.xml not found, so don't copy it, that's ok");
        }
        outputStream = new BufferedOutputStream(new FileOutputStream(file));
        return outputStream;
    }

    private void transformXml(final Source source, final Result destination, Transformer transformer) throws TransformerException {
        inAccess = true;
        transformer.transform(source, destination); // and that's it
        inAccess = false;
    }

    private Transformer getTransformer() throws TransformerFactoryConfigurationError, TransformerConfigurationException {
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, YES_PROPERTY_VALUE); // insert newlines
        transformer.setOutputProperty(OutputKeys.METHOD, METHOD_PROPERTY_VALUE);
        transformer.setOutputProperty(OutputKeys.VERSION, VERSION_PROPERTY_VALUE);
        transformer.setOutputProperty(OutputKeys.ENCODING, ENCODING_PROPERTY_VALUE);
        transformer.setOutputProperty(OutputKeys.STANDALONE, YES_PROPERTY_VALUE);
        return transformer;
    }

    public FileMediaResource getCatalogXmlFileMediaResource() throws FileNotFoundException {
        File xmlFile;
        if (inAccess) {
            xmlFile = new File(new File(WebappHelper.getUserDataRoot(), SYSTEM_DIR), XML_FILE + OLD_FILE_SUFFIX);
        } else {
            xmlFile = new File(new File(WebappHelper.getUserDataRoot(), SYSTEM_DIR), XML_FILE);
        }
        if (isFileNotOk(xmlFile)) {
            throw new FileNotFoundException("Catalog export file not found!");
        }
        return new FileMediaResource(xmlFile, true);
    }

    private boolean isFileNotOk(File xmlFile) {
        return !xmlFile.exists() || !xmlFile.canRead();
    }

}
