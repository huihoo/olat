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
 * Copyright (c) 2005-2006 by JGS goodsolutions GmbH, Switzerland<br>
 * http://www.goodsolutions.ch All rights reserved.
 * <p>
 */
package org.olat.presentation.framework.common;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.BusinessControlFactory;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.commons.context.ContextEntryControllerCreator;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.Windows;
import org.olat.presentation.framework.core.components.Window;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.dtabs.DTab;
import org.olat.presentation.framework.core.control.generic.dtabs.DTabs;
import org.olat.presentation.repository.DynamicTabHelper;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.exception.OLATSecurityException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * input: e.g. [repoentry:123] or [repoentry:123][CourseNode:456] or ...
 * <P>
 * Initial Date: 16.06.2006 <br>
 * 
 * @author Felix Jost
 */
public class NewControllerFactory {

    private static final Logger log = LoggerHelper.getLogger();

    private static NewControllerFactory INSTANCE = new NewControllerFactory();
    // map of controller creators, setted by Spring configuration
    private final Map<String, ContextEntryControllerCreator> contextEntryControllerCreators = new HashMap<String, ContextEntryControllerCreator>();

    /**
     * Get an instance of the new controller factory
     * 
     * @return
     */
    public static NewControllerFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Singleton constructor
     */
    private NewControllerFactory() {
        //
    }

    /**
     * Add a context entry controller creator for a specific key. This is used to add new creators at runtime, e.g. from a self contained module. It is allowed to
     * overwrite existing ContextEntryControllerCreator. Use the canLaunch() method to check if for a certain key something is already defined.
     * 
     * @param key
     * @param controllerCreator
     */
    public synchronized void addContextEntryControllerCreator(final String key, final ContextEntryControllerCreator controllerCreator) {
        final ContextEntryControllerCreator oldCreator = contextEntryControllerCreators.get(key);
        contextEntryControllerCreators.put(key, controllerCreator);
        // Add config logging to console
        log.info("Adding context entry controller creator for key::" + key + " and value::" + controllerCreator.getClass().getCanonicalName()
                + (oldCreator == null ? "" : " replaceing existing controller creator ::" + oldCreator.getClass().getCanonicalName()), null);
    }

    /**
     * Check if a context entry controller creator is available for the given key
     * 
     * @param key
     * @return true: key is known; false: key can not be used
     */
    public boolean canLaunch(final String key) {
        return contextEntryControllerCreators.containsKey(key);
    }

    /**
     * Check first context entry can be launched a further check is mostly not possible, as it gets validated through the BC-stack while building the controller-chain
     * 
     * return true: if this will be launchable at least for the first step.
     */
    public boolean validateCEWithContextControllerCreator(final UserRequest ureq, final WindowControl wControl, ContextEntry ce) {
        String firstType = ce.getOLATResourceable().getResourceableTypeName();
        if (canLaunch(firstType)) {
            return contextEntryControllerCreators.get(firstType).validateContextEntryAndShowError(ce, ureq, wControl);
        }
        return false;
    }

    /**
     * Launch a controller in a tab or site in the given window from a user request url
     * 
     * @param ureq
     * @param wControl
     */
    public void launch(final UserRequest ureq, final WindowControl wControl) {
        final BusinessControl bc = wControl.getBusinessControl();
        final ContextEntry mainCe = bc.popLauncherContextEntry();
        OLATResourceable ores = mainCe.getOLATResourceable();

        // Check for RepositoryEntry resource
        boolean ceConsumed = false;
        if (ores.getResourceableTypeName().equals(OresHelper.calculateTypeName(RepositoryEntry.class))) {
            // It is a repository-entry => get OLATResourceable from RepositoryEntry
            final RepositoryService repom = RepositoryServiceImpl.getInstance();
            final RepositoryEntry re = repom.lookupRepositoryEntry(ores.getResourceableId());
            if (re != null) {
                ores = re.getOlatResource();
                ceConsumed = true;
            }
        }

        // was brasato:: DTabs dts = wControl.getDTabs();
        Window window = Windows.getWindows(ureq.getUserSession()).getWindow(ureq);

        if (window == null) {
            log.debug("Found no window for jumpin => take WindowBackOffice", null);
            window = wControl.getWindowBackOffice().getWindow();
        }
        final DTabs dts = window.getDynamicTabs();
        DTab dt = dts.getDTab(ores);
        if (dt != null) {
            // tab already open => close it
            dts.removeDTab(dt);// disposes also dt and controllers
        }

        final String firstType = mainCe.getOLATResourceable().getResourceableTypeName();
        // String firstTypeId = ClassToId.getInstance().lookup() BusinessGroup
        final ContextEntryControllerCreator typeHandler = contextEntryControllerCreators.get(firstType);
        if (typeHandler == null) {
            log.warn("Cannot found an handler for context entry: " + mainCe, null);
            return;// simply return and don't throw a red screen
        }
        if (!typeHandler.validateContextEntryAndShowError(mainCe, ureq, wControl)) {
            // simply return and don't throw a red screen
            return;
        }

        final String siteClassName = typeHandler.getSiteClassName(mainCe);
        // open in existing site
        if (siteClassName != null) {
            // use special activation key to trigger the activate method
            String viewIdentifyer = null;
            if (bc.hasContextEntry()) {
                final ContextEntry subContext = bc.popLauncherContextEntry();
                if (subContext != null) {
                    final OLATResourceable subResource = subContext.getOLATResourceable();
                    if (subResource != null) {
                        viewIdentifyer = subResource.getResourceableTypeName();
                        if (subResource.getResourceableId() != null) {
                            // add resource instance id if available. The ':' is a common
                            // separator in the activatable interface
                            viewIdentifyer = viewIdentifyer + ":" + subResource.getResourceableId();
                        }
                    }
                }
            } else if (!ceConsumed) {
                // the olatresourceable is not in a dynamic tab but in a fix one
                if (ores != null) {
                    viewIdentifyer = ores.getResourceableTypeName();
                    if (ores.getResourceableId() != null) {
                        // add resource instance id if available. The ':' is a common
                        // separator in the activatable interface
                        viewIdentifyer = viewIdentifyer + ":" + ores.getResourceableId();
                    }
                }
            }
            dts.activateStatic(ureq, siteClassName, viewIdentifyer);
        } else {
            // or create new tab
            final WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, dts.getWindowControl());
            try {
                final Controller launchC = typeHandler.createController(mainCe, ureq, bwControl);
                final String tabName = typeHandler.getTabName(mainCe);
                DynamicTabHelper.openResourceTab(ores, ureq, launchC, tabName, null);
            } catch (OLATSecurityException ex) {
                // possibly not authenticated
                log.warn(ex.getMessage());
            }
        }
    }
}
