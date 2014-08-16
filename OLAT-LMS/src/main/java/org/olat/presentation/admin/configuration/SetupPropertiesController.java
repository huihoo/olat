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
package org.olat.presentation.admin.configuration;

import java.util.List;

import org.olat.data.commons.database.HsqldbDatabaseManagerGUI;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.commons.WebappHelper;
import org.olat.system.commons.configuration.OLATProperty;
import org.olat.system.commons.configuration.PropertyLocator;
import org.olat.system.commons.configuration.SystemPropertiesService;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * TODO: guido Class Description for SetupPropertiesController
 * <P>
 * Initial Date: 02.02.2010 <br>
 * 
 * @author guido
 */
public class SetupPropertiesController extends BasicController {

    VelocityContainer content = createVelocityContainer("setup");
    private Link showDBManager;

    /**
     * @param ureq
     * @param wControl
     */
    public SetupPropertiesController(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);
        Panel main = new Panel("setup");

        SystemPropertiesService propertyService = (SystemPropertiesService) CoreSpringFactory.getBean(SystemPropertiesService.class);

        List<OLATProperty> l = propertyService.getDefaultProperties();

        content.contextPut("defaultProperties", propertyService.getDefaultProperties());
        content.contextPut("overwriteProperties", propertyService.getOverwriteProperties());
        content.contextPut("olatdataOverwriteProperties", propertyService.getOlatdataOverwriteProperties());
        content.contextPut("mavenProperties", propertyService.getMavenProperties());

        content.contextPut("overwritePropertiesLocation", propertyService.getOverwritePropertiesURL());
        content.contextPut("olatdataOverwritePropertiesLocation", propertyService.getOlatdataOverwritePropertiesURL());
        content.contextPut("mavenPropertiesLocation", propertyService.getMavenPropertiesURL());

        content.contextPut("userDataRoot", WebappHelper.getUserDataRoot());

        // add button to start hsqldbmanager gui when hsqldb is in use
        if (propertyService.getStringProperty(PropertyLocator.DB_VENDOR).equals("hsqldb")) {
            showDBManager = LinkFactory.createButton("show.hsqldb", content, this);
            content.contextPut("hibernateConnectionUrl", "using embedded hsqldb");
        } else {
            String dbvendor = propertyService.getStringProperty(PropertyLocator.DB_VENDOR);
            String dbhost = propertyService.getStringProperty(PropertyLocator.DB_HOST);
            String dbport = propertyService.getStringProperty(PropertyLocator.DB_HOST_PORT);
            String dbname = propertyService.getStringProperty(PropertyLocator.DB_NAME);
            String dboptions = propertyService.getStringProperty(PropertyLocator.DB_URL_OPTIONS_MYSQL);
            content.contextPut("hibernateConnectionUrl", " jdbc:" + dbvendor + "://" + dbhost + ":" + dbport + "/" + dbname + dboptions);
        }

        main.setContent(content);
        putInitialPanel(main);

    }

    /**
	 */
    @Override
    @SuppressWarnings("unused")
    protected void event(UserRequest ureq, Component source, Event event) {
        if (source == showDBManager) {
            // accessing the bean will create it
            HsqldbDatabaseManagerGUI hsqldb = (HsqldbDatabaseManagerGUI) CoreSpringFactory.getBean(HsqldbDatabaseManagerGUI.class);
            content.contextPut("hibernateConnectionUrl", hsqldb.getDBUrl());
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // TODO Auto-generated method stub

    }
}
