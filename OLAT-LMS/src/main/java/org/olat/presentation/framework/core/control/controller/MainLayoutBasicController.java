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
 * Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.presentation.framework.core.control.controller;

import org.apache.log4j.Logger;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.htmlheader.jscss.CustomCSS;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.layout.MainLayoutController;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * The man layout basic controller implements the MainLayout interface and offers all convenient methods form the basic controller.
 * <P>
 * Initial Date: 09.10.2007 <br>
 * 
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public abstract class MainLayoutBasicController extends BasicController implements MainLayoutController {

    private static final Logger log = LoggerHelper.getLogger();
    private CustomCSS customCSS;

    /**
     * @param ureq
     * @param wControl
     */
    public MainLayoutBasicController(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);
    }

    /**
     * Constructor with fallback translator
     * 
     * @param ureq
     * @param wControl
     * @param fallbackTranslator
     */
    public MainLayoutBasicController(UserRequest ureq, WindowControl wControl, Translator fallbackTranslator) {
        super(ureq, wControl, fallbackTranslator);
    }

    /**
	 */
    @Override
    public CustomCSS getCustomCSS() {
        if (log.isDebugEnabled()) {
            if (customCSS == null)
                log.debug("No custom CSS set for this main layout", null);
            else
                log.debug("Custom CSS set for this main layout, pointing to URL::" + customCSS.getCSSURL(), null);
        }
        return customCSS;
    }

    /**
	 */
    @Override
    public void setCustomCSS(CustomCSS newCustomCSS) {
        if (log.isDebugEnabled()) {
            if (newCustomCSS == null)
                log.debug("Setting emtpy custom CSS for this main layout", null);
            else
                log.debug("Setting custom CSS for this main layout, pointing to URL::" + newCustomCSS.getCSSURL(), null);
        }
        // cleanup if one already exists
        if (customCSS != null && customCSS != newCustomCSS) {
            customCSS.dispose();
        }
        this.customCSS = newCustomCSS;
    }

    /**
	 */
    @Override
    public synchronized void dispose() {
        // first execute dispose from basic controller
        super.dispose();
        // now dispose the custom css
        if (customCSS != null) {
            customCSS.dispose();
            customCSS = null;
        }
    }

}
