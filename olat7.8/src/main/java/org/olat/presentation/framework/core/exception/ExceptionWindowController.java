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

package org.olat.presentation.framework.core.exception;

import java.util.Date;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.exception.StaleObjectRuntimeException;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.Windows;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.Window;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.DefaultChiefController;
import org.olat.presentation.framework.core.control.WindowBackOffice;
import org.olat.presentation.framework.core.control.info.WindowControlInfo;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.Settings;
import org.olat.system.commons.WebappHelper;
import org.olat.system.event.Event;
import org.olat.system.exception.KnownIssueException;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.logging.Tracing;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class ExceptionWindowController extends DefaultChiefController {

    private static final Logger log = LoggerHelper.getLogger();
    private static final String PACKAGE = PackageUtil.getPackageName(ExceptionWindowController.class);
    private static final String VELOCITY_ROOT = PackageUtil.getPackageVelocityRoot(ExceptionWindowController.class);

    private VelocityContainer msg;

    /**
     * @param ureq
     * @param th
     */
    public ExceptionWindowController(UserRequest ureq, Throwable th, boolean allowBackButton) {
        // TODO remove the below warn again once OLAT-5715 has been resolved - or it turns out that below warn is too verbose
        // the idea is that at this stage the throwable still contains a stacktrace but passed into the OLATRuntimeException
        // below as the cause it somehow gets lost. If this does not turn out to be true then the line below can be removed.
        // in any case, it is just a log.warn
        log.warn("ExceptionWindowController<init>: Throwable occurred, logging the full stacktrace:", th);

        // Disable inline translation mode whenever an exception occurs
        I18nManager i18nMgr = I18nManager.getInstance();
        if (i18nMgr.isCurrentThreadMarkLocalizedStringsEnabled()) {
            // Don't show back button when previously in inline translation mode, will not work
            allowBackButton = false;
            i18nMgr.setMarkLocalizedStringsEnabled(ureq.getUserSession(), false);
        }
        //
        Translator trans = new PackageTranslator(PACKAGE, ureq.getLocale());
        Formatter formatter = Formatter.getInstance(ureq.getLocale());
        msg = new VelocityContainer("olatmain", VELOCITY_ROOT + "/exception_page.html", trans, this);
        // Disallow wrapping of divs around the panel and the main velocity page
        // (since it contains the "<html><head... intro of the html page,
        // and thus has better to be replaced as a whole (new page load) instead of
        // a dom replacement)
        msg.setDomReplaceable(false);

        msg.contextPut("buildversion", Settings.getVersion());

        OLATRuntimeException o3e;

        if (th == null) {
            o3e = new OLATRuntimeException("Error Screen with a Throwable == null", null);
        } else if (!(th instanceof OLATRuntimeException)) {
            o3e = new OLATRuntimeException(th.getMessage(), th);
        } else {
            o3e = (OLATRuntimeException) th;
        }

        String detailedmessage = null;
        // translate user message if available
        if (o3e.getUsrMsgKey() != null && o3e.getUsrMsgPackage() != null) {
            PackageTranslator usrMsgTrans = new PackageTranslator(o3e.getUsrMsgPackage(), ureq.getLocale());
            if (o3e.getUsrMsgArgs() == null) {
                detailedmessage = usrMsgTrans.translate(o3e.getUsrMsgKey());
            } else {
                detailedmessage = usrMsgTrans.translate(o3e.getUsrMsgKey(), o3e.getUsrMsgArgs());
            }
        }
        // fix detailed message
        if (detailedmessage == null) {
            if (o3e instanceof StaleObjectRuntimeException) {
                StaleObjectRuntimeException soe = (StaleObjectRuntimeException) o3e;
                detailedmessage = trans.translate("error.staleobjectexception") + "<br />(" + soe.getKey() + " : " + soe.getPersClassName() + ")";
            } else
                detailedmessage = "-";
        }

        // fetch more info
        // get the latest window which caused this exception
        String componentListenerInfo = "";
        Windows ws = Windows.getWindows(ureq);

        Window window = ws.getWindow(ureq);
        if (window != null) {
            Component target = window.getAndClearLatestDispatchedComponent();
            if (target != null) {
                // there was a component id given, and a matching target could be found
                componentListenerInfo = "<dispatchinfo>\n\t<componentinfo>\n\t\t<compname>" + target.getComponentName() + "</compname>\n\t\t<compclass>"
                        + target.getClass().getName() + "</compclass>\n\t\t<extendedinfo>" + target.getExtendedDebugInfo() + "</extendedinfo>\n\t\t<event>";
                Event latestEv = target.getAndClearLatestFiredEvent();
                if (latestEv != null) {
                    componentListenerInfo += "\n\t\t\t<class>" + latestEv.getClass().getName() + "</class>\n\t\t\t<command>" + latestEv.getCommand()
                            + "</command>\n\t\t\t<tostring>" + latestEv + "</tostring>";
                }
                componentListenerInfo += "\n\t\t</event>\n\t</componentinfo>\n\t<controllerinfo>";
                Controller c = target.getLatestDispatchedController();
                if (c != null) {
                    // can be null if the error occured in the component itself
                    // componentListenerInfo += c.toString();
                    // WindowControl control = c.getWindowControl();
                    // sorry, getting windowcontrol on a controller which does not have one (all should have one, legacy) throws an exception
                    try {

                        WindowControlInfo wci = c.getWindowControlForDebug().getWindowControlInfo();
                        while (wci != null) {
                            String cName = wci.getControllerClassName();
                            componentListenerInfo += "\n\t\t<controllername>" + cName + "</controllername>";
                            wci = wci.getParentWindowControlInfo();
                        }
                    } catch (Exception e) {
                        componentListenerInfo += "no info, probably no windowcontrol set: " + e.getClass().getName() + ", " + e.getMessage();
                    }
                }
                componentListenerInfo += "\n\t</controllerinfo>\n</dispatchinfo>";
            }
        }

        if (o3e instanceof KnownIssueException) {
            KnownIssueException kie = (KnownIssueException) o3e;
            msg.contextPut("knownissuelink", kie.getJiraLink());
        }

        // TODO: DB.getInstance().hasTransaction() TODO: log db transaction id if in
        // transaction
        long nextErrorNum = Tracing.getNextErrorNumber();

        String sessionId = "n/a";
        if (ureq != null && ureq.getHttpReq() != null && ureq.getHttpReq().getSession(false) != null) {
            sessionId = ureq.getHttpReq().getSession(false).getId();
        }

        log.error("**RedScreen** " + o3e.getLogMsg() + " ::_::" + componentListenerInfo + " ::_:: sessionid=" + sessionId, o3e);
        // only if debug
        if (Settings.isDebuging()) {
            msg.contextPut("debug", Boolean.TRUE);
        } else {
            msg.contextPut("debug", Boolean.FALSE);
        }
        msg.contextPut("listenerInfo", Formatter.escWithBR(componentListenerInfo).toString());
        msg.contextPut("stacktrace", OLATRuntimeException.throwableToHtml(th));

        Identity curIdent = ureq.getIdentity();
        msg.contextPut("username", curIdent == null ? "n/a" : curIdent.getName());
        msg.contextPut("allowBackButton", Boolean.valueOf(allowBackButton));
        msg.contextPut("detailedmessage", detailedmessage);
        // Cluster NodeId + E-Nr
        msg.contextPut("errnum", Settings.getNodeInfo() + "-E" + nextErrorNum);
        msg.contextPut("supportaddress", WebappHelper.getMailConfig("mailSupport"));
        msg.contextPut("time", formatter.formatDateAndTime(new Date()));

        WindowBackOffice wbo = ws.getWindowManager().createWindowBackOffice("errormessagewindow", this);
        Window w = wbo.getWindow();

        msg.put("jsCssRawHtmlHeader", w.getJsCssRawHtmlHeader());
        // the current GUI theme and the global settings that contains the
        // font-size. both are pushed as objects so that window.dirty always reads
        // out the correct value
        msg.contextPut("theme", w.getGuiTheme());
        msg.contextPut("globalSettings", ws.getWindowManager().getGlobalSettings());

        w.setContentPane(msg);
        setWindow(w);
    }

    /**
	 */
    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        //
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to do here
    }

}
