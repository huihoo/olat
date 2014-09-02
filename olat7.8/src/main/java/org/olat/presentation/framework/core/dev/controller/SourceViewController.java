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
package org.olat.presentation.framework.core.dev.controller;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;

import org.olat.lms.commons.mediaresource.StringMediaResource;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.system.commons.configuration.PropertyLocator;
import org.olat.system.commons.configuration.SystemPropertiesService;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

import de.java2html.converter.JavaSource2HTMLConverter;
import de.java2html.javasource.JavaSource;
import de.java2html.javasource.JavaSourceParser;
import de.java2html.options.JavaSourceConversionOptions;

/**
 * Description:<br>
 * Displays java and/or velocity source in an new browserwindow
 * <P>
 * Initial Date: 03.09.2009 <br>
 * 
 * @author guido
 */
public class SourceViewController extends BasicController {

    private static final String TEXT_HTML_CHARSET_UTF_8 = "text/html; charset=utf-8";
    private VelocityContainer content;
    private Link viewJSource, viewVeloctiySource;
    private Class clazz;
    private VelocityContainer vc, sourceview;
    private CloseableModalController view;
    private final static String HTML_START = "<html><body>";
    private final static String HTML_STOP = "</body></html>";

    public SourceViewController(UserRequest ureq, WindowControl control, Class clazz, VelocityContainer vc) {
        super(ureq, control);
        this.clazz = clazz;
        this.vc = vc;
        sourceview = createVelocityContainer("sourceview");
        content = createVelocityContainer("sourcecontrols");

        viewJSource = LinkFactory.createLink("jsource", content, this);
        viewJSource.setTarget("_blank");

        viewVeloctiySource = LinkFactory.createLink("vsource", content, this);

        putInitialPanel(content);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // TODO Auto-generated method stub

    }

    /**
	 */
    @Override
    protected void event(UserRequest ureq, Component source, Event event) {

        SystemPropertiesService props = CoreSpringFactory.getBean(SystemPropertiesService.class);

        String hg = props.getStringProperty(PropertyLocator.SOURCE_VIEW_HG_REPO);

        if (source == viewVeloctiySource) {
            String uri = hg + "/src/main/java/" + vc.getPage();
            try {
                URL url = new URL(uri);
                URLConnection uc = url.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = in.readLine()) != null)
                    sb.append(line);
                sourceview.contextPut("content", sb.toString());
                sourceview.contextPut("vcname", vc.getPage());
                removeAsListenerAndDispose(view);
                view = new CloseableModalController(getWindowControl(), "close...", sourceview);
                listenTo(view);
                view.activate();
            } catch (IOException e) {
                showInfo("Trouble reading from " + uri);
            }

        } else if (source == viewJSource) {
            // Parse the raw text to a JavaSource object
            JavaSource jsource = null;
            String className = clazz.getCanonicalName().replace('.', '/');
            String uri = hg + "/src/main/java/" + className + ".java";
            try {
                URL url = new URL(uri);
                URLConnection uc = url.openConnection();
                final InputStream is = uc.getInputStream();
                jsource = new JavaSourceParser().parse(new BufferedInputStream(is));
            } catch (IOException e) {
                showInfo("Trouble reading from " + uri);
            }

            // Create a converter and write the JavaSource object as Html
            JavaSource2HTMLConverter converter = new JavaSource2HTMLConverter();
            StringWriter writer = new StringWriter();
            writer.append(HTML_START);
            try {
                JavaSourceConversionOptions options = JavaSourceConversionOptions.getDefault();
                options.setShowLineNumbers(true);
                converter.convert(jsource, options, writer);
            } catch (IOException e) {
                //
            }
            StringMediaResource mr = new StringMediaResource();
            mr.setContentType(TEXT_HTML_CHARSET_UTF_8);
            writer.append(HTML_STOP);
            mr.setData(writer.toString());
            ureq.getDispatchResult().setResultingMediaResource(mr);
        }
    }

    @Override
    protected void event(UserRequest ureq, Controller source, Event event) {
        if (source == view) {

        }
    }

}
