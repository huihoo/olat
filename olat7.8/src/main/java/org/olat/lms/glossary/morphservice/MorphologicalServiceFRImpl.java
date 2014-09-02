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
 * Copyright (c) 1999-2010 at frentix GmbH, Switzerland, http://www.frentix.com
 * <p>
 */
package org.olat.lms.glossary.morphservice;

import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.olat.connectors.httpclient.HttpClientFactory;
import org.olat.data.commons.xml.XStreamHelper;
import org.olat.lms.commons.mediaresource.HttpRequestMediaResource;
import org.olat.system.logging.log4j.LoggerHelper;

import com.thoughtworks.xstream.XStream;

/**
 * Description:<br>
 * Connects to a morphological service and lets retrieve flexions for a word. This is a variant to be used with french glossaries and is only compatible with the defined
 * service.
 * <P>
 * Initial Date: 30.09.2010 <br>
 * 
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
public class MorphologicalServiceFRImpl implements MorphologicalService {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String MORPHOLOGICAL_SERVICE_ADRESS = "http://www.cl.uzh.ch/kitt/cgi-bin/olat/ms_fr.cgi";
    private static final String SERVICE_NAME = "Morphological Service FR - University Zurich";
    private static final String SERVICE_IDENTIFIER = "ms-fr-uzh-cli";
    private static final String GLOSS_TERM_PARAM = "word";

    private String replyStatus = "";

    /**
	 * 
	 */
    protected MorphologicalServiceFRImpl() {
        // TODO Auto-generated constructor stub
    }

    /**
	 */
    @Override
    public ArrayList<String> getFlexions(String word) {
        InputStream xmlReplyStream = retreiveXMLReply(word);
        XStream xstream = XStreamHelper.createXStreamInstance();
        xstream.alias("xml", FlexionReply.class);
        xstream.alias("wordform", String.class);
        ArrayList<String> stemWithWordforms;
        try {
            Object msReply = XStreamHelper.readObject(xstream, xmlReplyStream);
            FlexionReply flexionReply = (FlexionReply) msReply;
            // set reply status to remind it
            setReplyStatus(flexionReply.getStatus());
            stemWithWordforms = flexionReply.getStem();
        } catch (Exception e) {
            log.error("XML-Reply was not valid XML", e);
            stemWithWordforms = null;
            setReplyStatus(MorphologicalService.STATUS_ERROR);
        }
        return stemWithWordforms;
    }

    private InputStream retreiveXMLReply(String word) {
        HttpClient client = HttpClientFactory.getHttpClientInstance();
        HttpMethod method = new GetMethod(MORPHOLOGICAL_SERVICE_ADRESS);
        NameValuePair wordValues = new NameValuePair(GLOSS_TERM_PARAM, word);
        if (log.isDebugEnabled()) {
            String url = MORPHOLOGICAL_SERVICE_ADRESS + "?" + GLOSS_TERM_PARAM + "=" + word;
            log.debug("Send GET request to morph-service with URL: " + url);
        }
        method.setQueryString(new NameValuePair[] { wordValues });
        try {
            client.executeMethod(method);
            int status = method.getStatusCode();
            if (status == HttpStatus.SC_NOT_MODIFIED || status == HttpStatus.SC_OK) {
                if (log.isDebugEnabled()) {
                    log.debug("got a valid reply!");
                }
            } else if (method.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                log.error("Morphological Service unavailable (404)::" + method.getStatusLine().toString(), null);
            } else {
                log.error("Unexpected HTTP Status::" + method.getStatusLine().toString(), null);
            }
        } catch (Exception e) {
            log.error("Unexpected exception trying to get flexions!", e);
        }
        Header responseHeader = method.getResponseHeader("Content-Type");
        if (responseHeader == null) {
            // error
            log.error("URL not found!", null);
        }
        HttpRequestMediaResource mr = new HttpRequestMediaResource(method);
        InputStream inputStream = mr.getInputStream();

        return inputStream;
    }

    /**
	 */
    @Override
    public String getReplyStatus() {
        return replyStatus;
    }

    /**
     * sets the status found in the reply
     * 
     * @param replyStatus
     */
    private void setReplyStatus(String replyStatus) {
        this.replyStatus = replyStatus;
    }

    /**
	 */
    @Override
    public String getMorphServiceDescriptor() {
        return SERVICE_NAME;
    }

    /**
	 */
    @Override
    public String getMorphServiceIdentifier() {
        return SERVICE_IDENTIFIER;
    }

    @Override
    public ArrayList<String> getFlexions(String partOfSpeech, String word) {
        return getFlexions(word);
    }

    @Override
    public String assumePartOfSpeech(String glossTerm) {
        // not needed for french
        return null;
    }

}
