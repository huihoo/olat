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

package org.olat.presentation.framework.core.control.winmgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.presentation.commons.AjaxSettings;
import org.olat.presentation.framework.core.GlobalSettings;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.WindowManager;
import org.olat.presentation.framework.core.chiefcontrollers.BaseChiefController;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.ComponentRenderer;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.components.velocity.VelocityContainerRenderer;
import org.olat.presentation.framework.core.control.ChiefController;
import org.olat.presentation.framework.core.control.ContentableChiefController;
import org.olat.presentation.framework.core.control.WindowBackOffice;
import org.olat.presentation.framework.core.control.creator.ControllerCreator;
import org.olat.presentation.framework.core.control.generic.popup.PopupBrowserWindow;
import org.olat.presentation.framework.core.control.generic.popup.PopupBrowserWindowController;
import org.olat.presentation.framework.core.control.generic.popup.PopupBrowserWindowControllerCreator;
import org.olat.presentation.framework.core.render.intercept.InterceptHandler;
import org.olat.presentation.framework.core.render.intercept.InterceptHandlerInstance;
import org.olat.presentation.framework.dispatcher.ClassPathStaticDispatcher;
import org.olat.system.commons.Settings;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: 23.03.2006 <br>
 * 
 * @author Felix Jost
 */
public class WindowManagerImpl extends BasicManager implements WindowManager {

    private List<WindowBackOfficeImpl> wbos = new ArrayList<WindowBackOfficeImpl>();

    // experimental!

    private GlobalSettings globalSettings;
    private boolean ajaxEnabled = false;

    private boolean forScreenReader = false;
    private boolean showDebugInfo = false;
    private boolean idDivsForced = false;

    private int fontSize = 100; // default width

    private int wboId = 0;

    private InterceptHandler screenreader_interceptHandler = null;

    private Map<Class, ComponentRenderer> screenReaderRenderers = new HashMap<Class, ComponentRenderer>();

    private PopupBrowserWindowControllerCreator pbwcc;

    // global urls for mapped path e.g. for css, js and so on; for all users!
    private static Map<String, String> mappedPaths = new HashMap<String, String>();

    public WindowManagerImpl() {

        this.pbwcc = CoreSpringFactory.getBean(PopupBrowserWindowControllerCreator.class);

        final AJAXFlags aflags = new AJAXFlags(this);
        globalSettings = new GlobalSettings() {

            @Override
            public int getFontSize() {
                return WindowManagerImpl.this.getFontSize();
            }

            @Override
            public AJAXFlags getAjaxFlags() {
                return aflags;
            }

            @Override
            public ComponentRenderer getComponentRendererFor(Component source) {
                return WindowManagerImpl.this.getComponentRendererFor(source);
            }

            @Override
            public boolean isIdDivsForced() {
                return WindowManagerImpl.this.isIdDivsForced();
            }
        };

        // add special classes for screenreader rendering
        // FIXME:FG: add support for multiple renderers (screenreader / iphone)
        // 1) move to a config file
        // 2) don't hardcode the theme (allow also iphone theme)
        // 3) check which special renderer are really needed
        // screenReaderRenderers.put(MenuTree.class, new MenuTreeScreenreaderRenderer());
        screenReaderRenderers.put(VelocityContainer.class, new VelocityContainerRenderer("screenreader"));
        // screenReaderRenderers.put(TabbedPane.class, new TabbedPaneScreenreaderRenderer());
    }

    @Override
    public void setForScreenReader(boolean forScreenReader) {
        this.forScreenReader = forScreenReader;
        if (forScreenReader) {
            screenreader_interceptHandler = new InterceptHandler() {

                @Override
                public InterceptHandlerInstance createInterceptHandlerInstance() {
                    return new ScreenReaderHandlerInstance();
                }
            };
        } else {
            screenreader_interceptHandler = null;
        }
    }

    /**
     * @param source
     * @return
     */
    protected ComponentRenderer getComponentRendererFor(Component source) {
        ComponentRenderer compRenderer;
        // to do: let "source - renderer pairs" be configured via spring for each mode like
        // default, accessibility, printing
        if (isForScreenReader()) {
            ComponentRenderer cr = screenReaderRenderers.get(source.getClass());
            if (cr != null) {
                compRenderer = cr;
            } else {
                compRenderer = source.getHTMLRendererSingleton();
            }
        } else {
            compRenderer = source.getHTMLRendererSingleton();
        }
        return compRenderer;
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public GlobalSettings getGlobalSettings() {
        return globalSettings;
    }

    @Override
    public void setAjaxWanted(UserRequest ureq, boolean enabled) {
        boolean globalOk = Settings.isAjaxGloballyOn();
        boolean browserOk = !AjaxSettings.isBrowserAjaxBlacklisted(ureq);
        boolean all = globalOk && browserOk && enabled;
        setAjaxEnabled(all);
    }

    /**
     * @return Returns the ajaxEnabled.
     */
    @Override
    public boolean isAjaxEnabled() {
        return ajaxEnabled;
    }

    /**
	 */
    @Override
    public String getMapPathFor(final Class baseClass) {
        return ClassPathStaticDispatcher.getInstance().getMapperBasePath(baseClass);
    }

    /**
	 */
    @Override
    public MediaResource createMediaResourceFor(final Class baseClass, String relPath) {
        return ClassPathStaticDispatcher.getInstance().createClassPathStaticFileMediaResourceFor(baseClass, relPath);
    }

    /**
     * <b>Only use for debug mode!!!<b><br>
     * use setAjaxWanted(ureq) instead sets the ajax on/off flag, -ignoring the browser-
     * 
     * @param enabled
     *            if true, ajax is on, renderers can render their links to post to the background frame and so on
     */
    @Override
    public void setAjaxEnabled(boolean enabled) {
        this.ajaxEnabled = enabled;
        for (WindowBackOfficeImpl wboImpl : wbos) {
            wboImpl.setAjaxEnabled(enabled);
        }
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public void setHighLightingEnabled(boolean enabled) {
        for (WindowBackOfficeImpl wboImpl : wbos) {
            wboImpl.setHighLightingEnabled(enabled);
        }
    }

    /*
     * (non-Javadoc)
     */
    public void setShowJSON(boolean enabled) {
        for (WindowBackOfficeImpl wboImpl : wbos) {
            wboImpl.setShowJSON(enabled);
        }
    }

    public void setShowDebugInfo(boolean showDebugInfo) {
        this.showDebugInfo = showDebugInfo;
        for (WindowBackOfficeImpl wboImpl : wbos) {
            wboImpl.setShowDebugInfo(showDebugInfo);
        }
    }

    public int getFontSize() {
        return fontSize;
    }

    @Override
    public void setFontSize(int fontSize) {

        this.fontSize = fontSize;
    }

    @Override
    public boolean isForScreenReader() {
        return forScreenReader;
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public WindowBackOffice createWindowBackOffice(String windowName, ChiefController owner) {
        WindowBackOfficeImpl wbo = new WindowBackOfficeImpl(this, windowName, owner, wboId++);
        wbos.add(wbo);
        return wbo;
    }

    /**
	 * 
	 */
    @Override
    public void dispose() {
        for (WindowBackOfficeImpl wboImpl : wbos) {
            wboImpl.dispose();
        }
    }

    protected InterceptHandler getScreenreader_interceptHandler() {
        return screenreader_interceptHandler;
    }

    protected boolean isShowDebugInfo() {
        return showDebugInfo;
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public ContentableChiefController createContentableChiefController(UserRequest ureq) {
        return new BaseChiefController(ureq);
    }

    /**
	 */
    @Override
    public PopupBrowserWindow createNewPopupBrowserWindowFor(UserRequest ureq, ControllerCreator contentControllerCreator) {
        BaseChiefController cc = new BaseChiefController(ureq);
        // supports the open(ureq) method
        PopupBrowserWindowController sbasec = pbwcc.createNewPopupBrowserController(ureq, cc.getWindowControl(), contentControllerCreator);
        // the content controller for the popupwindow is generated and set
        // at the moment the open method is called!!
        cc.setContentController(true, sbasec);
        return sbasec;
    }

    /**
     * needed only by guidebugdispatchercontroller for the gui debug mode!
     * 
     * @param idDivsForced
     */
    public void setIdDivsForced(boolean idDivsForced) {
        this.idDivsForced = idDivsForced;
    }

    /**
     * @return
     */
    public boolean isIdDivsForced() {
        return idDivsForced;
    }

}
