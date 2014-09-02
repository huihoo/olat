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

package org.olat.lms.ims.qti.render;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.dom.DOMDocument;
import org.dom4j.io.DocumentSource;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.lms.ims.resources.IMSEntityResolver;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.framework.dispatcher.StaticMediaDispatcher;
import org.olat.presentation.ims.qti.QTIResultDetailsController;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author Mike Stock Comment: Initial Date: 04.06.2003
 */
public class LocalizedXSLTransformer {
    private static ConcurrentHashMap<String, LocalizedXSLTransformer> instanceHash = new ConcurrentHashMap<String, LocalizedXSLTransformer>(5);
    private static final Logger log = LoggerHelper.getLogger();

    private static EntityResolver er = new IMSEntityResolver();
    private static VelocityEngine velocityEngine;

    static {
        // init velocity engine
        Properties p = null;
        try {
            velocityEngine = new VelocityEngine();
            p = new Properties();
            p.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
            p.setProperty("runtime.log.logsystem.log4j.category", "syslog");
            velocityEngine.init(p);
        } catch (final Exception e) {
            throw new OLATRuntimeException("config error with velocity properties::" + p.toString(), e);
        }
    }

    private final Translator pT;
    private Transformer transformer;
    /**
     * <code>RESULTS2HTML</code>
     */
    private static final String XSLFILENAME = "results2html_generic.xsl";

    /**
     * Private constructor, use getInstance to get an instance of the LocalizedXSLTransformer
     * 
     * @param trans
     */
    private LocalizedXSLTransformer(final Translator trans) {
        pT = trans;
        initTransformer();
    }

    /**
     * Get a localized transformer instance.
     * 
     * @param locale
     *            The locale for this transformer instance
     * @return A localized transformer
     */
    // cluster_ok only in VM
    public synchronized static LocalizedXSLTransformer getInstance(final Locale locale) {
        LocalizedXSLTransformer instance = instanceHash.get(I18nManager.getInstance().getLocaleKey(locale));
        if (instance == null) {
            final Translator trans = PackageUtil.createPackageTranslator(QTIResultDetailsController.class, locale);
            final LocalizedXSLTransformer newInstance = new LocalizedXSLTransformer(trans);
            instance = instanceHash.putIfAbsent(I18nManager.getInstance().getLocaleKey(locale), newInstance); // see javadoc of ConcurrentHashMap
            if (instance == null) { // newInstance was put into the map
                instance = newInstance;
            }
        }
        return instance;
    }

    /**
     * Render with a localized stylesheet. The localized stylesheet is addressed by its name with appended locale. E.g. mystyle.xsl in DE locale is addressed by
     * mystyle_de.xsl
     * 
     * @param node
     *            The node to render
     * @param styleSheetName
     *            The stylesheet to use.
     * @return Results of XSL transformation
     */
    private StringBuilder render(final Element node) {
        try {
            Document doc = node.getDocument();
            if (doc == null) {
                doc = new DOMDocument();
                doc.add(node);
            }
            final DocumentSource xmlsource = new DocumentSource(node);

            // ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final StringWriter sw = new StringWriter();
            final StreamResult result = new StreamResult(sw);
            synchronized (transformer) {// o_clusterOK by:fj transformer is per vm
                transformer.transform(xmlsource, result);
            }
            final String res = sw.toString();
            return new StringBuilder(res); // .append(result.getOutputStream());
        } catch (final Exception e) {
            throw new OLATRuntimeException(LocalizedXSLTransformer.class, "Error transforming XML.", e);
        }
    }

    /**
     * Render results processing document
     * 
     * @param doc
     *            The <results/>document
     * @return transformation results
     */
    public StringBuilder renderResults(final Document doc) {
        return render(doc.getRootElement());
    }

    /**
     * Helper to create XSLT transformer for this instance
     */
    private void initTransformer() {
        // build new transformer
        final InputStream xslin = getClass().getResourceAsStream("/org/olat/lms/ims/resources/xsl/" + XSLFILENAME);
        // translate xsl with velocity
        final Context vcContext = new VelocityContext();
        vcContext.put("t", pT);
        vcContext.put("staticPath", StaticMediaDispatcher.createStaticURIFor(""));
        String xslAsString = "";
        try {
            xslAsString = slurp(xslin);
        } catch (final IOException e) {
            log.error("Could not convert xsl to string!", e);
        }
        final String replacedOutput = evaluateValue(xslAsString, vcContext);
        final TransformerFactory tfactory = TransformerFactory.newInstance();
        XMLReader reader;
        try {
            reader = XMLReaderFactory.createXMLReader();
            reader.setEntityResolver(er);
            final Source xsltsource = new SAXSource(reader, new InputSource(new StringReader(replacedOutput)));
            this.transformer = tfactory.newTransformer(xsltsource);
        } catch (final SAXException e) {
            throw new OLATRuntimeException("Could not initialize transformer!", e);
        } catch (final TransformerConfigurationException e) {
            throw new OLATRuntimeException("Could not initialize transformer (wrong config)!", e);
        }
    }

    /**
     * Takes String with template and fills values from Translator in Context
     * 
     * @param valToEval
     *            String with variables to replace
     * @param vcContext
     *            velocity context containing a translator in this case
     * @return input String where values from context were replaced
     */
    private String evaluateValue(final String valToEval, final Context vcContext) {
        final StringWriter evaluatedValue = new StringWriter();
        // evaluate inputFieldValue to get a concatenated string
        try {
            velocityEngine.evaluate(vcContext, evaluatedValue, "vcUservalue", valToEval);
        } catch (final ParseErrorException e) {
            log.error("parsing of values in xsl-file of LocalizedXSLTransformer not possible!", e);
            return "ERROR";
        } catch (final MethodInvocationException e) {
            log.error("evaluating of values in xsl-file of LocalizedXSLTransformer not possible!", e);
            return "ERROR";
        } catch (final ResourceNotFoundException e) {
            log.error("xsl-file of LocalizedXSLTransformer not found!", e);
            return "ERROR";
        } catch (final IOException e) {
            log.error("could not read xsl-file of LocalizedXSLTransformer!", e);
            return "ERROR";
        }
        return evaluatedValue.toString();
    }

    /**
     * convert xsl InputStream to String
     * 
     * @param in
     * @return xsl as String
     * @throws IOException
     */
    private static String slurp(final InputStream in) throws IOException {
        final StringBuffer out = new StringBuffer();
        final byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1;) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }

}
