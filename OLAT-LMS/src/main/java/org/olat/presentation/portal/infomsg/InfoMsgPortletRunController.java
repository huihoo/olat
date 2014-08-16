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

package org.olat.presentation.portal.infomsg;

import org.olat.lms.admin.sysinfo.MaintenanceMsgManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Displays the infomessage in a portlet.
 * <P>
 * 
 * @author Lars Eberle (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class InfoMsgPortletRunController extends DefaultController {

    private static final String VELOCITY_ROOT = PackageUtil.getPackageVelocityRoot(InfoMsgPortletRunController.class);
    private final Translator trans;
    private final VelocityContainer infoVC;

    /**
     * Constructor
     * 
     * @param ureq
     * @param wControl
     */
    protected InfoMsgPortletRunController(final UserRequest ureq, final WindowControl wControl) {
        super(wControl);
        this.trans = new PackageTranslator(PackageUtil.getPackageName(InfoMsgPortletRunController.class), ureq.getLocale());
        this.infoVC = new VelocityContainer("infoVC", VELOCITY_ROOT + "/portlet.html", trans, this);
        final MaintenanceMsgManager mrg = (MaintenanceMsgManager) CoreSpringFactory.getBean(MaintenanceMsgManager.class);
        final String infoMsg = mrg.getMaintenanceMessage();
        if (StringHelper.containsNonWhitespace(infoMsg)) {
            infoVC.contextPut("content", infoMsg);
        } else {
            infoVC.contextPut("content", trans.translate("nothing"));
        }
        setInitialComponent(this.infoVC);
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // doesn't do anything
    }

    @Override
    protected void doDispose() {
        // nothing to dispose
    }

}
