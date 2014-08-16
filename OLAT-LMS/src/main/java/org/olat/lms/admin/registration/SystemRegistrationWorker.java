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
package org.olat.lms.admin.registration;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;
import org.olat.connectors.httpclient.HttpClientFactory;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.configuration.PropertyLocator;
import org.olat.system.commons.configuration.SystemPropertiesService;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * OLAT-6437
 * 
 * <P>
 * Initial Date: 18.07.2011 <br>
 * 
 * @author guido
 */
@Component("systemRegistrationWorkerenabled")
public class SystemRegistrationWorker implements Enregister {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    SystemPropertiesService propertyService;

    /**
     * [spring]
     */
    protected SystemRegistrationWorker() {
        //
    }

    @Override
    @Async
    public void register(String registrationData, String url, String version) {
        try {
            // delay post that not all send at the same time
            Thread.sleep(delayPost());
            doTheWork(registrationData, url, version);
        } catch (InterruptedException e) {
            //
        }

    }

    protected boolean doTheWork(String registrationData, String url, String version) {
        String registrationKey = propertyService.getStringProperty(PropertyLocator.SYSTEM_REG_SECRET_KEY);
        boolean regStatus = false;
        if (StringHelper.containsNonWhitespace(registrationData)) {
            // only send when there is something to send
            final HttpClient client = HttpClientFactory.getHttpClientInstance();
            client.getParams().setParameter("http.useragent", "OLAT Registration Agent ; " + version);

            log.info("URL:" + url, null);
            final PutMethod method = new PutMethod(url);
            if (registrationKey != null) {
                // updating
                method.setRequestHeader("Authorization", registrationKey);
                if (log.isDebugEnabled()) {
                    log.debug("Authorization: " + registrationKey, null);
                } else {
                    log.debug("Authorization: EXISTS", null);
                }
            } else {
                log.info("Authorization: NONE", null);
            }
            method.setRequestHeader("Content-Type", "application/xml; charset=utf-8");
            try {
                method.setRequestEntity(new StringRequestEntity(registrationData, "application/xml", "UTF8"));
                client.executeMethod(method);
                final int status = method.getStatusCode();
                if (status == HttpStatus.SC_NOT_MODIFIED || status == HttpStatus.SC_OK) {
                    log.info("Successfully registered OLAT installation on olat.org server, thank you for your support!", null);
                    registrationKey = method.getResponseBodyAsString();
                    propertyService.setProperty(PropertyLocator.SYSTEM_REG_SECRET_KEY, registrationKey);
                    regStatus = true;
                } else if (method.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                    log.error("File could be created not on registration server::" + method.getStatusLine().toString(), null);
                    regStatus = false;
                } else if (method.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                    log.info(method.getResponseBodyAsString() + method.getStatusText());
                    regStatus = false;
                } else {
                    log.error("Unexpected HTTP Status::" + method.getStatusLine().toString() + " during registration call", null);
                    regStatus = false;
                }
            } catch (final Exception e) {
                log.error("Unexpected exception during registration call", e);
                regStatus = false;
            }
        } else {
            log.warn(
                    "****************************************************************************************************************************************************************************",
                    null);
            log.warn(
                    "* This OLAT installation is not registered. Please, help us with your statistical data and register your installation under Adminisration - Systemregistration. THANK YOU! *",
                    null);
            log.warn(
                    "****************************************************************************************************************************************************************************",
                    null);
        }
        return regStatus;
    }

    /**
     * delay request from > 1s up to < 30s
     * 
     * @return
     */
    private int delayPost() {
        return (int) (Math.random() * 10000 * 3);
    }

}
