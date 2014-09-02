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

package org.olat.presentation.framework.core.control.generic.closablewrapper;

import java.util.ArrayList;
import java.util.List;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.presentation.framework.core.control.WindowBackOffice;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.util.ZIndexWrapper;
import org.olat.presentation.framework.core.render.ValidationResult;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;

/**
 * @author Felix Jost<br>
 *         Comment: this controller takes a component in its contructor and wraps a velocity container around it with a single link/button (with a userdefined
 *         displayname) which closes the dialog. <br>
 *         Important: the method getMainComponent is overridden and throws an Exception, since there is a different method to be used: activate(WindowController
 *         wControl). This reason is the this controller is intended to be used only as "a popup"/modal dialog (since it offers the 'close' button) and after clicking
 *         that button, it should disappear by itself. Therefore you can only use it in conjunction with a WindowsController. </pre>
 * @deprecated don't use this anymore. Use BasicController methods or controllers from org.core.gui.control.modal package
 */
@Deprecated
public class CloseableModalController extends DefaultController {
    /**
     * Comment for <code>CLOSE_MODAL_EVENT</code>
     */
    public static final Event CLOSE_MODAL_EVENT = new Event("CLOSE_MODAL_EVENT");
    private static final String VELOCITY_ROOT = PackageUtil.getPackageVelocityRoot(CloseableModalController.class);

    private VelocityContainer myContent;
    private Link closeIcon;
    private boolean displayAsOverlay;
    private boolean activated;

    /**
     * @param wControl
     * @param closeButtonText
     * @param modalContent
     */
    public CloseableModalController(WindowControl wControl, String closeButtonText, Component modalContent) {
        this(wControl, closeButtonText, modalContent, true, null);
    }

    public CloseableModalController(WindowControl wControl, String closeButtonText, Component modalContent, boolean displayAsOverlay, String title) {
        this(wControl, closeButtonText, modalContent, displayAsOverlay, title, true);
    }

    public CloseableModalController(WindowControl wControl, String closeButtonText, Component modalContent, boolean showCloseIcon) {
        this(wControl, closeButtonText, modalContent, true, null, showCloseIcon);
    }

    /**
     * Additional constructor if display of content as overlay is not suitable.
     * 
     * @param wControl
     * @param closeButtonText
     * @param modalContent
     * @param showAsOverlay
     * @param showCloseIcon
     *            make visibility of close-button optional
     */
    public CloseableModalController(WindowControl wControl, String closeButtonText, Component modalContent, boolean displayAsOverlay, String title, boolean showCloseIcon) {
        super(wControl);
        final Panel guiMsgPlace = new Panel("guimessage_place");
        myContent = new VelocityContainer("closeablewrapper", VELOCITY_ROOT + "/index.html", null, this) {
            @Override
            public void validate(UserRequest ureq, ValidationResult vr) {
                super.validate(ureq, vr);
                // just before rendering, we need to tell the windowbackoffice that we are a favorite for accepting gui-messages.
                // the windowbackoffice doesn't know about guimessages, it is only a container that keeps them for one render cycle
                WindowBackOffice wbo = getWindowControl().getWindowBackOffice();
                List<ZIndexWrapper> zindexed = (List<ZIndexWrapper>) wbo.getData("guimessage");
                if (zindexed == null) {
                    zindexed = new ArrayList<ZIndexWrapper>(3);
                    wbo.putData("guimessage", zindexed);
                }
                zindexed.add(new ZIndexWrapper(guiMsgPlace, 20));
            }
        };
        myContent.put("guimessage", guiMsgPlace);

        closeIcon = LinkFactory.createIconClose(closeButtonText, myContent, this);
        if (!showCloseIcon) {
            closeIcon.setVisible(false);
        }

        if (title != null) {
            myContent.contextPut("title", StringHelper.escapeHtml(title));
        }
        myContent.put("modalContent", modalContent); // use our own name
        this.displayAsOverlay = displayAsOverlay;

        setInitialComponent(myContent);
    }

    /**
	 */
    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        if (source == closeIcon) {
            deactivate();
            fireEvent(ureq, CLOSE_MODAL_EVENT);
        }
    }

    /**
	 */
    @Override
    public Component getInitialComponent() {
        throw new RuntimeException("please use activate() instead");
    }

    /**
	 * 
	 */
    public void activate() {
        if (displayAsOverlay) {
            getWindowControl().pushAsModalDialog(myContent);
        } else {
            getWindowControl().pushToMainArea(myContent);
        }
        activated = true;
    }

    /**
     * deactivates the modal controller. please do use this method here instead of getWindowControl().pop() !
     */
    public void deactivate() {
        getWindowControl().pop();
        activated = false;
    }

    /**
     * insert css in HTML-header, wich overwrites default css
     */
    public void insertHeaderCss() {
        JSAndCSSComponent jac = new JSAndCSSComponent("cmc-css", this.getClass(), null, "olat-preview.css", true);
        myContent.put("cmc-css", jac);
    }

    @Override
    protected void doDispose() {
        if (activated) {
            deactivate();
        }
    }

}
