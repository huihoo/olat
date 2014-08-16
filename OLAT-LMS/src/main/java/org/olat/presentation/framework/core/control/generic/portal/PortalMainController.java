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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.presentation.framework.core.control.generic.portal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.olat.lms.commons.util.ObjectCloner;
import org.olat.lms.preferences.Preferences;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.ControllerEventListener;
import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * The portal implementation has the ability to display a portal page as defined in the WEB-INF/olat_extensions.xml. Use the PortalFactory to create a new portal
 * instance.
 * <P>
 * Initial Date: 08.07.2005 <br>
 * 
 * @author gnaegi
 */
public class PortalMainController extends DefaultController implements ControllerEventListener {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String VELOCITY_ROOT = PackageUtil.getPackageVelocityRoot(PortalMainController.class);
    private static String MODE_EDIT = "editMode";
    private VelocityContainer portalVC;
    private Translator trans;
    private Map<String, PortletContainer> portletContainers; // map of all portlet containers (defined in portal columns + inactive portlets)
    private List<String> inactivePortlets; // list containing the names of inactive portlets
    private PortletFactory portletFactory;
    private Portal portal;
    private Link portalBackButton;
    private Link portalEditButton;
    private List<List<String>> portalColums;

    /**
     * Do use PortalFactory for create new Portals!
     * 
     * @param portalName
     *            identifyer of the portal
     * @param ureq
     * @param wControl
     * @param portalColumns
     *            List containing the default columns and rows
     * @param portletsConfigurations
     *            Map containing the portlet configurations
     */
    public PortalMainController(UserRequest ureq, WindowControl wControl, Portal portal) {
        super(wControl);
        this.portal = portal;
        portletFactory = CoreSpringFactory.getBean(PortletFactory.class);

        // user users personal configuration
        List userColumns = getUserPortalColumns(ureq);
        // clone default configuration for this user if user has no own configuration
        if (userColumns == null) {
            userColumns = (List) ObjectCloner.deepCopy(portal.getPortalColumns());
        }

        // check if users portal columns contain only defined portals. remove all non existing portals
        // to make it possible to change the portlets in a next release or to remove a portlet
        List<List<String>> cleanedUserColumns = new ArrayList<List<String>>();
        Set availablePortlets = portletFactory.getPortlets().keySet();
        Iterator colIter = userColumns.iterator();
        while (colIter.hasNext()) {
            // add this row as new cleaned row to columns
            List<String> cleanedRow = new ArrayList<String>();
            cleanedUserColumns.add(cleanedRow);
            // check all portlets in old row and copy to cleaned row if it exists
            List row = (List) colIter.next();
            Iterator rowIter = row.iterator();
            while (rowIter.hasNext()) {
                String portletName = (String) rowIter.next();
                if (availablePortlets.contains(portletName)) {
                    cleanedRow.add(portletName);
                }
                // discard invalid portlet names
            }
        }
        portalColums = cleanedUserColumns;

        this.portletContainers = new HashMap<String, PortletContainer>();
        this.inactivePortlets = new ArrayList<String>();

        this.trans = new PackageTranslator(PackageUtil.getPackageName(PortalMainController.class), ureq.getLocale());
        this.portalVC = new VelocityContainer("portalVC", VELOCITY_ROOT + "/portal.html", trans, this); // initialize arrays

        portalBackButton = LinkFactory.createButtonXSmall("command.portal.back", portalVC, this);
        portalEditButton = LinkFactory.createButtonXSmall("command.portal.edit", portalVC, this);

        // calculate the column css classes based on YAML schema
        int cols = portal.getPortalColumns().size();
        List<String> columnCssClassWrapper = new ArrayList(cols);
        columnCssClassWrapper.add(0, ""); // empty, in velocity things start with 1...
        List<String> columnCssClassInner = new ArrayList(cols);
        columnCssClassInner.add(0, ""); // empty, in velocity things start with 1...
        switch (cols) {
        case 0:
            // do nothing
            break;
        case 1:
            // empty css class
            columnCssClassWrapper.add(1, "");
            columnCssClassInner.add(1, "");
            break;
        case 2:
            // 50% each
            columnCssClassWrapper.add(1, "b_c50l");
            columnCssClassInner.add(1, "b_subcl");
            columnCssClassWrapper.add(2, "b_c50r");
            columnCssClassInner.add(2, "b_subcr");
            break;
        case 3:
            // 33% each
            columnCssClassWrapper.add(1, "b_c33l");
            columnCssClassInner.add(1, "b_subcl");
            columnCssClassWrapper.add(2, "b_c33l");
            columnCssClassInner.add(2, "b_subcl");
            columnCssClassWrapper.add(3, "b_c33r");
            columnCssClassInner.add(3, "b_subcr");
            break;
        case 4:
            // 25% each
            columnCssClassWrapper.add(1, "b_c25l");
            columnCssClassInner.add(1, "b_subcl");
            columnCssClassWrapper.add(2, "b_c25l");
            columnCssClassInner.add(2, "b_subcl");
            columnCssClassWrapper.add(3, "b_c25l");
            columnCssClassInner.add(3, "b_subcl");
            columnCssClassWrapper.add(4, "b_c25r");
            columnCssClassInner.add(4, "b_subcr");
            break;
        default:
            // do log as error but don't make redscreen for user.
            log.error("only up to 4 portal columns supported but " + cols + " columns available in portal::" + portal.getName());
            break;
        }
        portalVC.contextPut("columnCssClassWrapper", columnCssClassWrapper);
        portalVC.contextPut("columnCssClassInner", columnCssClassInner);

        // init all portlets enabled in the portal columns
        initPortlets(ureq);
        // push the columns to velocity
        this.portalVC.contextPut("portalColumns", cleanedUserColumns);
        // push list of inactive portlets to velocity
        this.portalVC.contextPut("inactivePortlets", inactivePortlets);
        // push all portlets to velocity
        this.portalVC.contextPut("portletContainers", portletContainers);
        this.portalVC.contextPut("locale", ureq.getLocale());
        // in run mode
        this.portalVC.contextPut(MODE_EDIT, Boolean.FALSE);
        setInitialComponent(portalVC);
    }

    private List getUserPortalColumns(UserRequest ureq) {
        Preferences gp = ureq.getUserSession().getGuiPreferences();
        return (List) gp.get(PortalMainController.class, "userPortalColumns" + portal.getName());
    }

    private void saveUserPortalColumnsConfiguration(UserRequest ureq, List userColumns) {
        Preferences gp = ureq.getUserSession().getGuiPreferences();
        gp.putAndSave(PortalMainController.class, "userPortalColumns" + portal.getName(), userColumns);
    }

    /**
     * Initialize all portles found in the configuration
     * 
     * @param ureq
     */
    private void initPortlets(UserRequest ureq) {
        // load all possible portlets, portlets run controller is only loaded when really used
        Iterator<Portlet> portletsIter = portletFactory.getPortlets().values().iterator();
        while (portletsIter.hasNext()) {
            Portlet portlet = portletsIter.next();
            log.debug("initPortlets portletName=" + portlet.getName());
            if (portlet.isEnabled()) {
                PortletContainer pc = PortletFactory.getPortletContainerFor(portlet, getWindowControl(), ureq);
                pc.addControllerListener(this);
                // remember this portlet container
                this.portletContainers.put(portlet.getName(), pc);
                String addLinkName = "command.add." + portlet.getName();
                Link tmp = LinkFactory.createCustomLink(addLinkName, addLinkName, "add", Link.BUTTON_XSMALL, portalVC, this);
                tmp.setUserObject(portlet.getName());
                // and add to velocity
                this.portalVC.put(portlet.getName(), pc.getInitialComponent());

                // check if portlet is active for this user
                Iterator colIter = this.portalColums.iterator();
                boolean isActive = false;
                while (colIter.hasNext()) {
                    List row = (List) colIter.next();
                    Iterator rowIter = row.iterator();
                    while (rowIter.hasNext()) {
                        String activePortletName = (String) rowIter.next();
                        if (portlet.getName().equals(activePortletName))
                            isActive = true;
                    }
                }
                if (isActive) {
                    // initialize portlet container for active portlets only
                    pc.initializeRunComponent(ureq);
                    log.debug("initPortlets: add to inacitve portlets portletName=" + portlet.getName());
                } else {
                    // add it to inacitve portlets list if not active
                    inactivePortlets.add(portlet.getName());
                    log.debug("initPortlets: add to inacitve portlets portletName=" + portlet.getName());
                }
            } else {
                log.debug("Portlet disabled portletName=" + portlet.getName());
            }
        }
        // update links on visible portlets
        updatePositionChangeLinks();
    }

    /**
	 */
    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        if (source == portalBackButton) {
            updatePorletContainerEditMode(ureq, false);
            this.portalVC.contextPut(MODE_EDIT, false);
        } else if (source == portalEditButton) {
            updatePorletContainerEditMode(ureq, true);
            this.portalVC.contextPut(MODE_EDIT, true);
        } else if (source instanceof Link && portalVC.getComponents().containsValue(source)) {
            Link tmp = (Link) source;
            String portletName = (String) tmp.getUserObject();
            List<String> firstColumn = this.portalColums.get(0);
            PortletContainer pc = this.portletContainers.get(portletName);
            if (pc == null)
                throw new AssertException("trying to add portlet with name::" + portletName
                        + " to portal, but portlet container did not exist. Could be a user modifying the URL...");
            // add to users portlet list
            firstColumn.add(portletName);
            // remove from inactive portlets list
            this.inactivePortlets.remove(portletName);
            // initialize portlet run component
            pc.initializeRunComponent(ureq);
            // save user config in db
            saveUserPortalColumnsConfiguration(ureq, portalColums);
            // update possible links in gui
            updatePositionChangeLinks();
            portalVC.setDirty(true);
        }
    }

    /**
     * Updates all portles using the given mode
     * 
     * @param editMode
     *            true: edit mode activated, false: deactivated
     */
    private void updatePorletContainerEditMode(UserRequest ureq, Boolean editMode) {
        Iterator<String> portletsIter = portletFactory.getPortlets().keySet().iterator();
        while (portletsIter.hasNext()) {
            String portletName = portletsIter.next();
            PortletContainer pc = this.portletContainers.get(portletName);
            if (pc != null)
                pc.setIsEditMode(ureq, editMode);
        }
    }

    /**
	 */
    @Override
    protected void event(UserRequest ureq, Controller source, Event event) {
        log.debug("PortalImpl event=" + event);
        if (source instanceof PortletContainer) {
            PortletContainer pc = (PortletContainer) source;
            String cmd = event.getCommand();
            boolean found = false;
            for (int column = 0; column < portalColums.size(); column++) {
                List rows = portalColums.get(column);
                for (int row = 0; row < rows.size(); row++) {
                    String portletName = (String) rows.get(row);
                    if (portletName.equals(pc.getPortlet().getName())) {
                        if (cmd.equals("move.up")) {
                            Collections.swap(rows, row, row - 1);
                            found = true;
                            break;
                        } else if (cmd.equals("move.down")) {
                            Collections.swap(rows, row, row + 1);
                            found = true;
                            break;
                        } else if (cmd.equals("move.right")) {
                            rows.remove(row);
                            List<String> newCol = portalColums.get(column + 1);
                            newCol.add(portletName);
                            found = true;
                            break;
                        } else if (cmd.equals("move.left")) {
                            rows.remove(row);
                            List<String> newCol = portalColums.get(column - 1);
                            newCol.add(portletName);
                            found = true;
                            break;
                        } else if (cmd.equals("close")) {
                            pc.deactivateRunComponent();
                            rows.remove(row);
                            this.inactivePortlets.add(portletName);
                            found = true;
                            break;
                        }
                    }
                }
                if (found)
                    break;
            }
            // save user config in db
            saveUserPortalColumnsConfiguration(ureq, portalColums);
            // update possible links in gui
            updatePositionChangeLinks();
            portalVC.setDirty(true);
        }
    }

    /**
     * Updates the velocity containers of all portlet containers to display the move links correctly
     */
    private void updatePositionChangeLinks() {
        Iterator colIter = portalColums.iterator();
        int colcount = 0;
        while (colIter.hasNext()) {
            List rows = (List) colIter.next();
            Iterator rowIter = rows.iterator();
            int rowcount = 0;
            while (rowIter.hasNext()) {
                String portletName = (String) rowIter.next();
                PortletContainer pc = this.portletContainers.get(portletName);
                if (pc == null) {
                    continue;
                }
                // up command
                if (rowcount == 0)
                    pc.setCanMoveUp(false);
                else
                    pc.setCanMoveUp(true);
                // down command
                if (rowIter.hasNext())
                    pc.setCanMoveDown(true);
                else
                    pc.setCanMoveDown(false);
                // left command
                if (colcount == 0)
                    pc.setCanMoveLeft(false);
                else
                    pc.setCanMoveLeft(true);
                // right command
                if (colIter.hasNext())
                    pc.setCanMoveRight(true);
                else
                    pc.setCanMoveRight(false);

                rowcount++;
            }
            colcount++;
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // cleanup all portlet containers
        Iterator iter = portletContainers.values().iterator();
        while (iter.hasNext()) {
            PortletContainer element = (PortletContainer) iter.next();
            element.dispose();
        }
        portletContainers = null;
    }

}
