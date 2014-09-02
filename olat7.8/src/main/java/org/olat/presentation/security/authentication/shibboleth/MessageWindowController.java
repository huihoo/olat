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

package org.olat.presentation.security.authentication.shibboleth;

import org.apache.log4j.Logger;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.Windows;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.Window;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.DefaultChiefController;
import org.olat.presentation.framework.core.control.WindowBackOffice;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.Settings;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Displays a simple message to the user TODO: Lavinia Dumitrescu Class Description for MessageWindowController
 * <P>
 * Initial Date: 05.11.2007 <br>
 * 
 * @author Lavinia Dumitrescu
 */
public class MessageWindowController extends DefaultChiefController {

    private static final Logger log = LoggerHelper.getLogger();
    private static final String PACKAGE = PackageUtil.getPackageName(MessageWindowController.class);
    private static final String VELOCITY_ROOT = PackageUtil.getPackageVelocityRoot(MessageWindowController.class);

    private final VelocityContainer msg;

    /**
     * @param ureq
     * @param th
     * @param detailedmessage
     * @param supportEmail
     */
    public MessageWindowController(final UserRequest ureq, final Throwable th, final String detailedmessage, final String supportEmail) {
        final Translator trans = new PackageTranslator(PACKAGE, ureq.getLocale());
        // Formatter formatter = Formatter.getInstance(ureq.getLocale());
        msg = new VelocityContainer("olatmain", VELOCITY_ROOT + "/message.html", trans, this);

        log.warn(th.getMessage() + " *** User info: " + detailedmessage);

        msg.contextPut("buildversion", Settings.getVersion());
        msg.contextPut("detailedmessage", detailedmessage);
        if (supportEmail != null) {
            msg.contextPut("supportEmail", supportEmail);
        }

        // Window w = new Window("messagewindow", this, jsadder);

        final Windows ws = Windows.getWindows(ureq);
        final WindowBackOffice wbo = ws.getWindowManager().createWindowBackOffice("messagewindow", this);
        final Window w = wbo.getWindow();

        msg.put("jsAndCssC", w.getJsCssRawHtmlHeader());
        msg.contextPut("theme", w.getGuiTheme());

        w.setContentPane(msg);
        setWindow(w);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        //
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }

}
