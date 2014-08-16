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

package org.olat.presentation.framework.core.components.tabbedpane;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Locale;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.ComponentRenderer;
import org.olat.presentation.framework.core.components.Container;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.exception.AssertException;

/**
 * enclosing_type Description: <br>
 * 
 * @author Felix Jost
 */
public class TabbedPane extends Container {
    private static final ComponentRenderer RENDERER = new TabbedPaneRenderer();

    /**
     * Comment for <code>PARAM_PANE_ID</code>
     */
    protected static final String PARAM_PANE_ID = "taid";

    private List tabbedPanes = new ArrayList(4);
    private List displayNames = new ArrayList(4);
    private BitSet disabledPanes = new BitSet(4);
    private int selectedPane = -1;
    private Boolean showTabTitle = Boolean.TRUE;
    private Translator compTrans;

    /**
     * @param name
     */
    public TabbedPane(String name, Locale locale) {
        super(name);
        compTrans = PackageUtil.createPackageTranslator(this.getClass(), locale);
    }

    /**
	 */
    @Override
    protected void doDispatchRequest(UserRequest ureq) {
        // the taid indicates which tab the user clicked
        String s_taid = ureq.getParameter(PARAM_PANE_ID);
        int newTaid = Integer.parseInt(s_taid);

        dispatchRequest(ureq, newTaid);
    }

    /**
     * @param ureq
     * @param newTaid
     */
    public void dispatchRequest(UserRequest ureq, int newTaid) {
        if (!isEnabled(newTaid))
            throw new AssertException("tab with id " + newTaid + " is not enabled, but was dispatched");
        Component oldSelComp = getTabAt(selectedPane);
        setSelectedPane(newTaid);
        Component newSelComp = getTabAt(selectedPane);
        fireEvent(ureq, new TabbedPaneChangedEvent(oldSelComp, newSelComp));
    }

    /**
     * Sets the selectedPane.
     * 
     * @param selectedPane
     *            The selectedPane to set
     */
    public void setSelectedPane(int selectedPane) {
        // get old selected component and remove it from render tree
        Component oldSelComp = getTabAt(this.selectedPane);
        remove(oldSelComp);

        // activate new
        this.selectedPane = selectedPane;
        Component newSelComp = getTabAt(selectedPane);
        super.put("atp", newSelComp);
        // setDirty(true); not needed since: line above marks this container automatically dirty
    }

    public void dissableTabTitle() {
        showTabTitle = Boolean.FALSE;
    }

    public Boolean isTabTitleEnabled() {
        return showTabTitle;
    }

    /**
     * @param displayName
     * @param component
     * @return
     */
    public int addTab(String displayName, Component component) {
        displayNames.add(displayName);
        tabbedPanes.add(component);
        if (selectedPane == -1) {
            selectedPane = 0; // if no pane has been selected, select the first one
            super.put("atp", component);
        }
        return tabbedPanes.size() - 1;
    }

    public void removeAll() {
        if (this.selectedPane != -1) {
            Component oldSelComp = getTabAt(this.selectedPane);
            remove(oldSelComp);
        }
        tabbedPanes.clear();
        displayNames.clear();
        disabledPanes.clear();
        selectedPane = -1;
        setDirty(true);
    }

    /**
     * @param position
     * @return
     */
    protected Component getTabAt(int position) {
        return (Component) tabbedPanes.get(position);
    }

    /**
     * @param position
     * @return
     */
    protected String getDisplayNameAt(int position) {
        return (String) displayNames.get(position);
    }

    /**
	 */
    @Override
    public void put(Component component) {
        throw new RuntimeException("please don't use put() in a TabbedPane, but addTab(...)");
    }

    /**
     * @return
     */
    protected int getTabCount() {
        return (tabbedPanes == null ? 0 : tabbedPanes.size());
    }

    /**
     * Returns the selectedPane.
     * 
     * @return int
     */
    protected int getSelectedPane() {
        return selectedPane;
    }

    /**
     * @deprecated
     * @param displayName
     */
    @Deprecated
    public void setSelectedPane(String displayName) {
        if (displayName == null)
            return;
        int pos = displayNames.indexOf(displayName);
        if (pos > -1) {
            setSelectedPane(pos);
        }
    }

    public int getPaneIdForComponent(Component velocityTemplate) {
        return tabbedPanes.indexOf(velocityTemplate);
    }

    /**
	 */
    @Override
    public String getExtendedDebugInfo() {
        return "selectedPane:" + selectedPane;
    }

    /**
     * @param pane
     * @param enabled
     */
    public void setEnabled(int pane, boolean enabled) {
        boolean wasEnabled = isEnabled();
        if (wasEnabled ^ enabled) {
            setDirty(true);
        }
        disabledPanes.set(pane, !enabled);
    }

    /**
     * @param pane
     * @return
     */
    protected boolean isEnabled(int pane) {
        return !disabledPanes.get(pane);
    }

    @Override
    public ComponentRenderer getHTMLRendererSingleton() {
        return RENDERER;
    }

    protected Translator getCompTrans() {
        return compTrans;
    }

}
