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

package org.olat.presentation.framework.core.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.olat.data.commons.database.DBFactory;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.mediaresource.AsyncMediaResponsible;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.commons.mediaresource.RedirectMediaResource;
import org.olat.lms.commons.mediaresource.ServletUtil;
import org.olat.presentation.framework.core.GUIInterna;
import org.olat.presentation.framework.core.GlobalSettings;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.Windows;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.control.ChiefController;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.DispatchResult;
import org.olat.presentation.framework.core.control.JSAndCSSAdder;
import org.olat.presentation.framework.core.control.JSAndCSSAdderImpl;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.dtabs.DTabs;
import org.olat.presentation.framework.core.control.history.HistoryEntry;
import org.olat.presentation.framework.core.control.info.WindowControlInfo;
import org.olat.presentation.framework.core.control.winmgr.Command;
import org.olat.presentation.framework.core.control.winmgr.CommandFactory;
import org.olat.presentation.framework.core.control.winmgr.MediaResourceMapper;
import org.olat.presentation.framework.core.control.winmgr.WindowBackOfficeImpl;
import org.olat.presentation.framework.core.exception.MsgFactory;
import org.olat.presentation.framework.core.render.RenderResult;
import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.presentation.framework.core.render.URLBuilder;
import org.olat.presentation.framework.core.render.ValidationResult;
import org.olat.presentation.framework.core.render.intercept.InterceptHandler;
import org.olat.presentation.framework.core.render.intercept.InterceptHandlerInstance;
import org.olat.presentation.framework.core.themes.Theme;
import org.olat.presentation.framework.core.util.ReusableURLHelper;
import org.olat.presentation.framework.core.util.component.ComponentTraverser;
import org.olat.presentation.framework.core.util.component.ComponentVisitor;
import org.olat.presentation.framework.dispatcher.mapper.MapperRegistry;
import org.olat.system.commons.Settings;
import org.olat.system.commons.configuration.PropertyLocator;
import org.olat.system.commons.configuration.SystemPropertiesService;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;
import org.olat.testutils.codepoints.server.Codepoint;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class Window extends Container {

    private static final Logger log = LoggerHelper.getLogger();

    private static final int MAX_HISTORY_ENTRIES = 100;

    private static final String LOG_SEPARATOR = "^$^";
    /**
     * old time stamp call, but no asyncmediaresponsible <code>OLDTIMESTAMPCALL</code>
     */
    public static final Event OLDTIMESTAMPCALL = new Event("ots");
    /**
     * while dispatching: component with id not found <code>COMPONENTNOTFOUND</code>
     */
    public static final Event COMPONENTNOTFOUND = new Event("cnf");
    /**
     * fired when the dispatch cycle (dispatch to a component) is finished
     */
    public static final Event END_OF_DISPATCH_CYCLE = new Event("eodc");

    /**
     * fired before inline (text/html computed response) takes place
     */
    public static final Event BEFORE_INLINE_RENDERING = new Event("before_inline_rendering");

    /**
     * fired after the response has been rendered into a string (but not delivered to the client yet)
     */
    public static final Event AFTER_INLINE_RENDERING = new Event("after_inline_rendering");

    public static final Event AFTER_VALIDATING = new Event("before_validate");

    /**
     * fired just before the targetcomponent.dispatch takes places
     */
    public static final Event ABOUT_TO_DISPATCH = new Event("about_to_dispatch");

    private String uriPrefix;
    private Container contentPane;
    private String latestTimestamp;
    private AsyncMediaResponsible asyncMediaResponsible;
    private String instanceId;
    private int timestamp = 1; // used to find out when a user has called
    // back/forward/reload in the browser and to detect
    // asyncmedia resources

    private Theme guiTheme;

    private boolean validatingCausedRerendering = false;

    // for debugging and errortracing reasons
    private Component latestDispatchedComp;
    private String latestDispatchComponentInfo = null;

    // wbackoffice reference
    private WindowBackOfficeImpl wbackofficeImpl;
    // mutex for rendering
    private final Object render_mutex = new Object();
    // delegate for css and js includes
    private final JSAndCSSAdderImpl jsAndCssAdder;

    private List<HistoryEntry> history = new ArrayList<HistoryEntry>();
    private int historyPos = -1;

    private Map<String, Object> attributes = new HashMap<String, Object>();

    /**
     * @param name
     * @param chiefController
     */
    public Window(String name, WindowBackOfficeImpl wbackoffice) {
        super(name);
        this.wbackofficeImpl = wbackoffice;
        this.jsAndCssAdder = wbackoffice.createJSAndCSSAdder();
        // set default GUI css theme
        SystemPropertiesService properties = (SystemPropertiesService) CoreSpringFactory.getBean(SystemPropertiesService.class);
        this.guiTheme = new Theme(properties.getStringProperty(PropertyLocator.LAYOUT_THEME));
        setGuiTheme(guiTheme);
    }

    public Component getJsCssRawHtmlHeader() {
        return jsAndCssAdder.getJsCssRawHtmlHeader();
    }

    /**
     * @param guiThemeBaseUri
     *            the URI of the base folder of the current Gui theme, r.g. 'http://www.myserver.com/olat/raw/themes/default/'
     */
    public void setGuiTheme(Theme guiTheme) {
        this.guiTheme = guiTheme;
    }

    /**
     * @return the current GUI theme
     */
    public Theme getGuiTheme() {
        return this.guiTheme;
    }

    /**
     * @return Container
     */
    public Container getContentPane() {
        return contentPane;
    }

    /**
     * Sets the contentPane.
     * 
     * @param contentPane
     *            The contentPane to set
     */
    public void setContentPane(Container contentPane) {
        this.contentPane = contentPane;
    }

    /**
	 */
    public Component getComponent(@SuppressWarnings("unused") String name) {
        throw new AssertException("please use getContentPane()");
    }

    /**
	 */
    protected void doDispatchRequest(UserRequest ureq) {
        dispatchRequest(ureq, false);
    }

    /**
     * @param ureq
     * @param renderOnly
     */
    public void dispatchRequest(UserRequest ureq, boolean renderOnly) {
        HttpServletRequest request = ureq.getHttpReq();
        HttpServletResponse response = ureq.getHttpResp();
        String timestampID = ureq.getTimestampID();
        String componentID = ureq.getComponentID();
        // int browserT = timestampID == null? 0 : Integer.parseInt(timestampID);

        // case windowId timestamp componentId
        // --------------------------------------------
        //
        // 1 null null null -> if (!renderOnly)-> error else doRenderOnly<br>
        // 2 invalid n/a n/a -> not handled here, but in Windows
        // 3 valid valid valid -> dispatch and further handling (check for new
        // window and res. media resource
        // 4 valid valid invalid -> no dispatch (silently) -> rerender (no res.
        // media res and no new window check needed)
        // 5 valid invalid n/a -> asyncRes == null? "doNotUseReload" :
        // getAsyncRes==null? renderInline : serve resource
        // 6 valid null n/a -> no timestamp -> just rerender inline

        // defs:
        // rerender: component validation and inline rendering

        // case 1:
        // simply rerender, no dispatching
        // case 3:
        // dispatch, check for new Window
        // case 5:
        // check newWindow, serve resource/renderInline

        // order:
        // 1. check for timestamp: valid: case 3,4 ; invalid -> case 5, or -1 ->
        // indicator of just rendering and no revalidating needed
        // 2. dispatch to component, unless flag renderOnly in method sig., or
        // inlineRerender set (timestamps indicates)

        boolean inline = false;
        boolean validate = false;
        boolean checkNewWindow = false;
        boolean dispatch = false;

        // increase the timestamp, but not if we are in loadperformancemode: then all url's have
        // to work independant of previous ones -> when no increase: timestamp is always the same here
        boolean incTimestamp = !GUIInterna.isLoadPerformanceMode();

        MediaResource mr = null;
        StringBuilder debugMsg = null;
        long debug_start = 0;
        if (log.isDebugEnabled()) {
            debug_start = System.currentTimeMillis();
            debugMsg = new StringBuilder("::winst:");
        }
        synchronized (this) { // o_clusterOK by:fj
            // sync dispatching per window to avoid rendering problems
            // when user repeateadly presses reload, and also to distribute bandwidth more
            // evenly.
            // postcondition: each controller's events are called by one gui-thread at a time only.

            GlobalSettings gsettings = wbackofficeImpl.getGlobalSettings();
            boolean bgEnab = gsettings.getAjaxFlags().isIframePostEnabled();
            // System.out.println("in window:");
            // -------------------------
            // ----- ajax mode ---------
            // -------------------------
            if (bgEnab && (ureq.getMode() & 1) == 1) {
                // first check on "ajax-command-was-not-in-hidden-iframe hint" -> if so, rerender the current window
                if (ureq.getParameter("o_win_jsontop") != null) {
                    renderOnly = true;
                } else {
                    try {

                        // if target in background (m = mode , 0.bit set)
                        // 1.) do dispatch to component if component timestamp ok

                        // REVIEW:PB: this will be the code allowing back forward navigation
                        // --> boolean inlineAfterBackForward = false;
                        // FIXME:fj:b avoid double traversal to find component again below
                        String s_compID = ureq.getComponentID();
                        if (s_compID == null)
                            throw new AssertException("no component id found in req:" + ureq.toString());
                        // throws NumberFormatException if not a number
                        long compID = Long.parseLong(s_compID);
                        List foundPath = new ArrayList(10);
                        Component target = ComponentHelper.findDescendantOrSelfByID(getContentPane(), compID, foundPath);
                        boolean validForDispatching;
                        if (target != null) { // the target was found
                            int tst = target.getTimestamp();
                            String cTimest = Integer.toString(tst, 10);
                            String urlCTimest = ureq.getComponentTimestamp();
                            validForDispatching = cTimest.equals(urlCTimest);
                            if (!validForDispatching && log.isDebugEnabled()) {
                                log.debug("Invalid timestamp: ureq.compid:" + ureq.getComponentID() + " ureq.win-ts:" + ureq.getTimestampID() + " ureq.comp-ts:"
                                        + ureq.getComponentTimestamp() + " target.timestamp:" + cTimest + " target=" + target);
                            }
                        } else {
                            // the component was not found in the rendertree anymore.
                            // this can happen e.g. on quick double-clicks, so that the dom-replacement-command never reaches the client.
                            if (log.isDebugEnabled())
                                log.debug("no ajax dispatch: component not found (target=null)");
                            validForDispatching = false;
                        }

                        /*
                         * REVIEW:PB: this will be the code allowing back forward navigation so far it is disabled window fires OLDTIMESTAMPCALL event ---> if
                         * (!validForDispatching) { // user clicked back or forward after an ajax request which causes the history of the hidden iframe to go back one and
                         * post // an old request which has an old timestamp. int diff = findInHistory(ureq); // if not found in history: probably the case that a link
                         * got opened in a new window by the user by using the right-mouse-button. // in this case, we later send a full page reload
                         * //System.out.println("ajax back: diff "+diff); if (diff != 0) { wbackofficeImpl.browserBackOrForward(ureq, diff); inline = true;
                         * inlineAfterBackForward = true; } } <--------
                         */

                        // 2.) collect dirty components (top-down, return from sub-path when first dirty node met)
                        // 3.) return to sender...
                        boolean didDispatch = false;
                        if (validForDispatching) {
                            didDispatch = doDispatchToComponent(ureq, null); // FIXME:fj:c enable time stats for ajax-mode
                            if (log.isDebugEnabled()) {
                                long durationAfterDoDispatchToComponent = System.currentTimeMillis() - debug_start;
                                log.debug("Perf-Test: Window durationAfterDoDispatchToComponent=" + durationAfterDoDispatchToComponent);
                            }
                        }

                        MediaResource mmr = null;
                        // REVIEW:PB: this will be the code allowing back forward navigation
                        // -----> if (didDispatch || inlineAfterBackForward) {
                        if (didDispatch || !validForDispatching) {
                            if (validForDispatching) {
                                Window ww = ureq.getDispatchResult().getResultingWindow();
                                if (ww != null) {
                                    // a link which causes a new window to be openend should always
                                    // a) have the normal mode set (not the ajax mode)
                                    // b) have the target="_blank" attribute
                                    // reason: in non-ajax-mode, a link has to know beforehand whether it opens in a new window or not.
                                    // FIXME:fj:c think about bodyOnLoad -> win.open(new window url)
                                    throw new AssertException("a link in ajax mode should never result in a new window");
                                }
                                mmr = ureq.getDispatchResult().getResultingMediaResource();
                                if (mmr == null) {
                                    inline = true;
                                } else {
                                    inline = false;
                                }
                            }

                            // REVIEW:PB: this will be the code allowing back forward navigation
                            // -----> if (inline) {
                            if (inline || !validForDispatching) {
                                Container top = getContentPane();
                                // always validate here, since we are never in the case of just rerendering (we are in the bg iframe)
                                ValidatingVisitor vv = new ValidatingVisitor(gsettings, jsAndCssAdder);
                                ComponentTraverser ct = new ComponentTraverser(vv, top, false);
                                if (log.isDebugEnabled()) {
                                    long durationBeforeVisitAll = System.currentTimeMillis() - debug_start;
                                    log.debug("Perf-Test: Window durationBeforeVisitAll=" + durationBeforeVisitAll);
                                }
                                ct.visitAll(ureq);
                                if (log.isDebugEnabled()) {
                                    long durationAfterVisitAll = System.currentTimeMillis() - debug_start;
                                    log.debug("Perf-Test: Window durationAfterVisitAll=" + durationAfterVisitAll);
                                }
                                wbackofficeImpl.fireCycleEvent(Window.AFTER_VALIDATING);

                                ValidationResult vr = vv.getValidationResult();

                                boolean newJsCssAdded = vr.getJsAndCSSAdder().finishAndCheckChange();
                                String newModUri = vr.getNewModuleURI();
                                // !validForDispatching ||
                                if (newJsCssAdded || newModUri != null) {
                                    // send 302 redirect so the ajax-iframe's parent window gets reloaded to either include new js/css or to prepare the address bar
                                    // url for asynchronous requests when delivering inline-contentpackaging.
                                    // set window id to cur id, timestamp to current timestamp,
                                    // component id to -1 -> indicates rerender
                                    String uri = buildURIForRedirect(newModUri); // newModUri == null in case "just" new css or js libs have been added
                                    // set this only for the first request (the .html request), but clear it afterwards for asyncmedia
                                    validatingCausedRerendering = true;
                                    Command rmrcom = CommandFactory.createParentRedirectTo(uri);
                                    wbackofficeImpl.sendCommandTo(rmrcom);
                                    // OLAT-4563: so the timestamp is not incremented, we do only a redirect
                                    setDirty(false);
                                } else {
                                    // inline rendering by selectively replacing the dirty components in the dom tree of the browser
                                    wbackofficeImpl.fireCycleEvent(Window.BEFORE_INLINE_RENDERING);

                                    // Start by preparing the client: must be called prior to the
                                    // other commands to not overwrite the form o2c dirty flag
                                    // wich might be set by later commands
                                    if (!this.isDirty()) {
                                        wbackofficeImpl.sendCommandTo(CommandFactory.createPrepareClientCommand(wbackofficeImpl.getBusinessControlPath()));
                                    }

                                    // Add the js and css files and related pre init commands
                                    Command jscsscom = jsAndCssAdder.extractJSCSSCommand();
                                    wbackofficeImpl.sendCommandTo(jscsscom);

                                    // Add the DOM replacement commands. Must be called after the
                                    // js and css commands. Inline JS scripts might have
                                    // dependencies to previously loaded js libs
                                    if (this.isDirty()) {
                                        // special case: when the window itself is dirty we require
                                        // a full page refresh in any case
                                        String reRenderUri = buildURIFor(this, timestampID, null);
                                        Command rmrcom = CommandFactory.createParentRedirectTo(reRenderUri);
                                        wbackofficeImpl.sendCommandTo(rmrcom);
                                        this.setDirty(false);
                                    } else {
                                        // check for dirty child components in the component tree
                                        if (log.isDebugEnabled()) {
                                            long durationBeforeHandleDirties = System.currentTimeMillis() - debug_start;
                                            log.debug("Perf-Test: Window durationBeforeHandleDirties=" + durationBeforeHandleDirties);
                                        }
                                        Command co = handleDirties();
                                        if (log.isDebugEnabled()) {
                                            long durationAfterHandleDirties = System.currentTimeMillis() - debug_start;
                                            log.debug("Perf-Test: Window durationAfterHandleDirties=" + durationAfterHandleDirties);
                                        }
                                        // DUMP FOR EACH CLICK THE CURRENT JumpInPath -> for later usage and debugging.
                                        // System.err.println("V^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^V");
                                        WindowControl current = (WindowControl) wbackofficeImpl.getWindow().getAttribute("BUSPATH");
                                        // System.err.println(current != null ? JumpInManager.getRestJumpInUri(current.getBusinessControl()) : "NONE");
                                        // System.err.println("T^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^T");
                                        wbackofficeImpl.fireCycleEvent(AFTER_INLINE_RENDERING);
                                        if (co != null) { // see method handleDirties for the rare case of co == null even if there are dirty components;
                                            wbackofficeImpl.sendCommandTo(co);
                                        }
                                    }
                                }
                            } else { // not inline
                                if (!validForDispatching) {
                                    // not valid: fire oldtimestamp event
                                    fireEvent(ureq, OLDTIMESTAMPCALL);
                                    throw new AssertException("unreachable code reached");
                                }
                                if (log.isDebugEnabled()) {
                                    long durationBeforeCreateMediaResourceMapper = System.currentTimeMillis() - debug_start;
                                    log.debug("Perf-Test: Window durationBeforeCreateMediaResourceMapper=" + durationBeforeCreateMediaResourceMapper);
                                }
                                // not inline, new mediaresource
                                // send it to the parent window (e.g. an excel download, but could also be a 302 redirect)
                                // if the browser has e.g. pdf configured to be displayed inline, we want it to fill the whole area (self window), not the hidden iframe.
                                // the same for 302.
                                // -> send a command which offers a new location for the main window.
                                // create a mapper which maps this mediaresource, and serves it once only
                                MediaResourceMapper extMRM = new MediaResourceMapper();
                                extMRM.setMediaResource(mmr);
                                // FIXME:fj:b deregister old mapper, or reuse current one
                                String res = MapperRegistry.getInstanceFor(ureq.getUserSession()).register(extMRM) + "/";
                                // e.g. res = /olat/m/10001/
                                Command rmrcom = CommandFactory.createParentRedirectForExternalResource(res);
                                wbackofficeImpl.sendCommandTo(rmrcom);
                                if (log.isDebugEnabled()) {
                                    long durationAfterCreateMediaResourceMapper = System.currentTimeMillis() - debug_start;
                                    log.debug("Perf-Test: Window durationAfterCreateMediaResourceMapper=" + durationAfterCreateMediaResourceMapper);
                                }
                            }
                        } else { // not dispatched
                            if (log.isDebugEnabled()) {
                                long durationBeforeBuildURIFor = System.currentTimeMillis() - debug_start;
                                log.debug("Perf-Test: Window durationBeforeBuildURIFor=" + durationBeforeBuildURIFor);
                            }
                            log.debug("Found a valid timestamp but could not dispatch to component: ureq.compid:" + ureq.getComponentID() + " ureq.win-ts:"
                                    + ureq.getTimestampID() + " ureq.comp-ts:" + ureq.getComponentTimestamp() + " target.timestamp:" + target.getTimestamp() + " target="
                                    + target);
                            String reRenderUri = buildURIFor(this, timestampID, null);
                            Command rmrcom = CommandFactory.createParentRedirectTo(reRenderUri);
                            wbackofficeImpl.sendCommandTo(rmrcom);
                        }
                        if (log.isDebugEnabled()) {
                            long durationBeforeServeResource = System.currentTimeMillis() - debug_start;
                            log.debug("Perf-Test: Window durationBeforeServeResource=" + durationBeforeServeResource);
                        }
                        MediaResource jsonmr = wbackofficeImpl.extractCommands(true);
                        ServletUtil.serveResource(request, response, jsonmr);
                    } catch (Throwable th) {
                        // in any case, try to inform the user appropriately.
                        // a) error while dispatching (e.g. db problem, npe, ...)
                        // b) for inline: error while validating or json-rendering dirty components.

                        // since an error has occured for a request which is targeted in the background iframe, we need to redirect to the error window.
                        // create the error window
                        try {
                            log.debug("Error in Window , rollback");
                            DBFactory.getInstance().rollback();

                            ChiefController msgcc = MsgFactory.createMessageChiefController(ureq, th);
                            Window errWindow = msgcc.getWindow();
                            // register window
                            Windows.getWindows(ureq).registerWindow(errWindow);
                            // redirect to the error window
                            String newWinUri = buildRenderOnlyURIFor(errWindow);
                            Command rmrcom = CommandFactory.createParentRedirectTo(newWinUri);
                            wbackofficeImpl.sendCommandTo(rmrcom);
                            MediaResource jsonmr = wbackofficeImpl.extractCommands(true);
                            ServletUtil.serveResource(request, response, jsonmr);
                        } catch (Throwable anotherTh) {
                            log.error("Exception while handling exception!!!!", anotherTh);
                        }
                    }
                    if (log.isDebugEnabled()) {
                        long durationDispatchRequest = System.currentTimeMillis() - debug_start;
                        log.debug("Perf-Test: Window return from 1 durationDispatchRequest=" + durationDispatchRequest);
                    }
                    return;
                }
            }

            // -------------------------
            // ----- standard mode -----
            // -------------------------
            if (renderOnly || timestampID == null) {
                inline = true;
                validate = true;
            } else if (validatingCausedRerendering && timestampID.equals("-1")) {
                // the first request after the 302 redirect cause by a component validation
                // -> just rerender, but clear the flag for further async media requests
                validatingCausedRerendering = false;
                inline = true;
                validate = false; // no need to revalidate right now
                checkNewWindow = false;
                dispatch = false;
            } else {
                // [POST: !renderOnly && timestampID != null]
                // if we had a inline rendering at least once (latestTimestamp is
                // set), then check for an old timestamp
                // System.out.println("dispatch normal: compid:"+ureq.getComponentID()+" win-ts:"+ureq.getTimestampID()+" comp-ts:"+ureq.getComponentTimestamp());
                if (latestTimestamp != null && !timestampID.equals(latestTimestamp)) {
                    // this is not a link from the latest rendering, but from a previous
                    // one, since it has a wrong timestamp parameter -> check for
                    // asynchronous media
                    if (asyncMediaResponsible == null) { // no async resp.
                        // assume it to be a link from an old window (using browser back or
                        // "open in new
                        // window/tab" in the browser).
                        if ((componentID != null && componentID.equals("-1")) || (ureq.getParameter("o_winrndo") != null)) {
                            // just rerender
                        } else {
                            // not a valid timestamp -> most likely a browser back or forward event (or a copy/paste of a url) ->

                            // fire event to listening chiefcontroller
                            fireEvent(ureq, OLDTIMESTAMPCALL);
                            /*
                             * REVIEW:PB: this will be the code allowing back forward navigation ----> // look at the timestamps more thoroughly. if
                             * (!timestampID.equals("-1")) { int diff = findInHistory(ureq); // diff == 0 -> reload (->ignore, so it will cause a simple rerendering) //
                             * diff < 0 -> browser-back // diff > 0 -> browser-forward //System.out.println("!!!!(normal) back: diff "+diff);
                             * wbackofficeImpl.browserBackOrForward(ureq, diff); } // else a 302 redirect of the main window -> simply rerender if (ureq.getComponentID()
                             * != null) {
                             * //System.out.println("normal: compid:"+ureq.getComponentID()+" win-ts:"+ureq.getTimestampID()+" comp-ts:"+ureq.getComponentTimestamp()); }
                             * else {
                             * //System.out.println("special url - no component part (e.g. 302 redirect because of new req. js / css) compid:"+ureq.getComponentID()
                             * +" win-ts:"+ureq.getTimestampID()+" comp-ts:"+ureq.getComponentTimestamp()); } validate = true; <-------------
                             */
                        }
                        // just rerender current window
                        inline = true;
                        // do not increment timestamp so that e.g. url in a iframe remain valid
                        incTimestamp = false;
                    } else {
                        // some component will take care of it for the moment, so be it
                        mr = asyncMediaResponsible.getAsyncMediaResource(ureq);
                        if (mr == null) { // indicates inline rendering
                            inline = true;
                            checkNewWindow = true; // an inline rendered async link should be
                            // able to produce a new window
                            validate = true;
                        } else { // serve the resource.
                            // all flags remain at their default value
                        }
                    }
                } else {
                    // latestTimestamp == null || timestampID.equals(latestTimestamp)

                    dispatch = true;
                    checkNewWindow = true;
                    validate = true;
                }
            }
            // end of simple flagging.
            long dstart = 0;
            if (log.isDebugEnabled()) {
                dstart = System.currentTimeMillis();
                long syncIntroDiff = dstart - debug_start;
                debugMsg.append("sync_bdisp:").append(syncIntroDiff).append(LOG_SEPARATOR);
            }

            if (dispatch) {
                boolean didDispatch = doDispatchToComponent(ureq, debugMsg);
                if (log.isDebugEnabled()) {
                    long dstop = System.currentTimeMillis();
                    long diff = dstop - dstart;
                    debugMsg.append("disp_comp:").append(diff).append(LOG_SEPARATOR);
                    // log.debug("componentdispatchtime: " + (dstop - dstart));
                }
                if (didDispatch) { // the component with the given id was found
                    mr = ureq.getDispatchResult().getResultingMediaResource();
                    if (mr == null) {
                        inline = true;
                    } else {
                        inline = false;
                    }
                } else {
                    // component with id was not found -> probably asynchronous thread changed flow ->
                    // just rerender
                    inline = true;
                    dispatch = false;
                    checkNewWindow = false;
                    validate = true;
                }

            }

            if (checkNewWindow) {
                Window resWindow = ureq.getDispatchResult().getResultingWindow();
                if (resWindow != null) {
                    // register it first, if not done before
                    Windows ws = Windows.getWindows(ureq);
                    if (!ws.isRegistered(resWindow)) {
                        resWindow.setUriPrefix(uriPrefix);
                        ws.registerWindow(resWindow);
                    }
                    // render initial state of new window by redirecting (302) to the new
                    // window id. needed for asyncronous data like images loaded

                    // todo maybe better delegate window registry to the windowbackoffice?
                    URLBuilder ubu = new URLBuilder(uriPrefix, resWindow.getInstanceId(), String.valueOf(resWindow.timestamp), resWindow.wbackofficeImpl);
                    StringOutput sout = new StringOutput(30);
                    ubu.buildURI(sout, null, null);
                    mr = new RedirectMediaResource(sout.toString());
                    ServletUtil.serveResource(request, response, mr);
                    if (log.isDebugEnabled()) {
                        long diff = System.currentTimeMillis() - debug_start;
                        debugMsg.append("rdirnw:").append(diff).append(LOG_SEPARATOR);
                        log.debug(debugMsg.toString());
                        long durationDispatchRequest = System.currentTimeMillis() - debug_start;
                        log.debug("Perf-Test: Window return from 2 durationDispatchRequest=" + durationDispatchRequest);
                    }
                    return;
                }
            }

            if (inline) {
                // do inline rendering.

                Container top = getContentPane();
                // validate prior to rendering, but only if the timestamp was not null
                // /
                // the component just got dispatched
                if (validate) { // do not validate if a previous validate lead to a
                    // redirect; validating makes no sense here
                    // long t1 = System.currentTimeMillis();
                    ValidatingVisitor vv = new ValidatingVisitor(gsettings, jsAndCssAdder);
                    ComponentTraverser ct = new ComponentTraverser(vv, top, false);
                    ct.visitAll(ureq);
                    wbackofficeImpl.fireCycleEvent(Window.AFTER_VALIDATING);
                    ValidationResult vr = vv.getValidationResult();
                    String newModUri = vr.getNewModuleURI();

                    vr.getJsAndCSSAdder().finishAndCheckChange(); // ignore the return value since we are just about rendering anyway

                    if (newModUri != null) {
                        // send 302 redirect without dispatching, but just rerender
                        // inline.
                        // set window id to cur id, timestamp to current timestamp,
                        // component id to -1 -> indicates rerender
                        String uri = buildURIForRedirect(newModUri);
                        MediaResource mrr = new RedirectMediaResource(uri);
                        // set this only for the first request (the .html request), but clear it afterwards for asyncmedia
                        validatingCausedRerendering = true;
                        ServletUtil.serveResource(request, response, mrr);
                        if (log.isDebugEnabled()) {
                            long diff = System.currentTimeMillis() - debug_start;
                            debugMsg.append("rdirva:").append(diff).append(LOG_SEPARATOR);
                            log.debug(debugMsg.toString());
                            long durationDispatchRequest = System.currentTimeMillis() - debug_start;
                            log.debug("Perf-Test: Window return form 3 durationDispatchRequest=" + durationDispatchRequest);
                        }
                        return;
                    }
                }

                wbackofficeImpl.fireCycleEvent(BEFORE_INLINE_RENDERING);
                String result;
                synchronized (render_mutex) { // o_clusterOK by:fj
                    // render now
                    if (incTimestamp)
                        timestamp++;
                    String newTimestamp = String.valueOf(timestamp);
                    // add the businesscontrol path for bookmarking:
                    // each url has a part in it (the so called business path), which, in case of an invalid url or invalidated
                    // session, can be used as a bookmark. that is, urls from our framework are bookmarkable, but require some little
                    // coding effort: setting an appropriate business path and launching for each controller.
                    // note: the businesspath may also be used as a easy (but of course not perfect) back-button-solution:
                    // if the timestamp of a request is outdated, simply jump to its bookmarked business control path.
                    URLBuilder ubu = new URLBuilder(uriPrefix, getInstanceId(), newTimestamp, wbackofficeImpl);
                    RenderResult renderResult = new RenderResult();

                    // if we have an around-component-interception
                    // set the handler for this render cycle
                    InterceptHandler interceptHandler = wbackofficeImpl.getInterceptHandler();
                    if (interceptHandler != null) {
                        InterceptHandlerInstance dhri = interceptHandler.createInterceptHandlerInstance();
                        renderResult.setInterceptHandlerRenderInstance(dhri);
                    }

                    Renderer fr = Renderer.getInstance(top, top.getTranslator(), ubu, renderResult, gsettings);
                    long rstart = 0;
                    if (log.isDebugEnabled()) {
                        rstart = System.currentTimeMillis();
                    }
                    result = fr.render(top).toString();
                    if (log.isDebugEnabled()) {
                        long rstop = System.currentTimeMillis();
                        long diff = rstop - rstart;
                        debugMsg.append("render:").append(diff).append(LOG_SEPARATOR);
                    }
                    if (renderResult.getRenderException() != null)
                        throw new OLATRuntimeException(Window.class, renderResult.getLogMsg(), renderResult.getRenderException());

                    // after rendering we know if some component awaits further async
                    // calls
                    // like images, so get a handler
                    AsyncMediaResponsible amr = renderResult.getAsyncMediaResponsible();
                    setAsyncMediaResponsible(amr); // if amr == null -> we are not
                    // excepting
                    // any async calls in the near future...
                    latestTimestamp = newTimestamp;
                }
                if (log.isDebugEnabled()) {
                    long diff = System.currentTimeMillis() - debug_start;
                    debugMsg.append("inl_comp:").append(diff).append(LOG_SEPARATOR);
                }

                // DUMP FOR EACH CLICK THE CURRENT JumpInPath -> for later usage and debugging.
                // System.err.println("VV^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^VV");
                WindowControl current = (WindowControl) wbackofficeImpl.getWindow().getAttribute("BUSPATH");
                // System.err.println(current != null ? JumpInManager.getRestJumpInUri(current.getBusinessControl()) : "NONE");
                // System.err.println("TT^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^TT");
                wbackofficeImpl.fireCycleEvent(AFTER_INLINE_RENDERING);
                ServletUtil.serveStringResource(request, response, result);
                if (log.isDebugEnabled()) {
                    long diff = System.currentTimeMillis() - debug_start;
                    debugMsg.append("inl_serve:").append(diff).append(LOG_SEPARATOR);
                }
            }
            // else serve mediaresource, but postpone serving to when lock has been released,
            // otherwise e.g. a large download blocks the window, so that the user cannot click until the download is finished
        } // end of synchronized(this)

        if (!inline) {
            // it can be an async media resource, or a resulting mediaresource (image, an excel download, a 302 redirect, and so on.)
            if (log.isDebugEnabled()) {
                long diff = System.currentTimeMillis() - debug_start;
                debugMsg.append("mr_comp:").append(diff).append(LOG_SEPARATOR);
            }
            ServletUtil.serveResource(request, response, mr);
            if (log.isDebugEnabled()) {
                long diff = System.currentTimeMillis() - debug_start;
                debugMsg.append("mr_serve:").append(diff).append(LOG_SEPARATOR);
            }
        }

        if (log.isDebugEnabled()) {
            // log the collected data now
            log.debug(debugMsg.toString());
            long durationDispatchRequest = System.currentTimeMillis() - debug_start;
            log.debug("Perf-Test: Window durationDispatchRequest=" + durationDispatchRequest);
        }

    }

    /**
     * @param componentID
     * @param timestampID
     * @param componentTimestamp
     * @return -1 for back, 1 for forward, 0 for nothing/reload
     */
    private int findInHistory(UserRequest ureq) {
        for (int i = historyPos - 1; i >= 0; i--) {
            HistoryEntry he = history.get(i);
            if (he.isSame(ureq)) {
                // found "left" to the current position = left in time = back
                historyPos = i; // make ready for future comparisons
                return -1;
            }
        }
        // not found -> check forward
        int size = history.size();
        for (int i = historyPos + 1; i < size; i++) {
            HistoryEntry he = history.get(i);
            if (he.isSame(ureq)) {
                // found "right" to the current position = right in time = forward
                historyPos = i; // make ready for future comparisons
                return 1;
            }
        }
        // not found, either reload, fake, error, or current entry -> return reload
        return 0;
    }

    /**
     * @param entry
     */
    private void addToHistory(HistoryEntry entry) {
        // clear old forward history
        int size = history.size();
        if (historyPos < size - 1) { // we are not rightmost,
            // that is we have traversed back in history
            // -> clear forward history
            history.subList(historyPos + 1, size).clear();
        }
        // now add the new entry
        history.add(entry);
        historyPos++;

        // restrict to 100 entries per user should be more than enough
        int nsize = history.size();
        if (nsize > MAX_HISTORY_ENTRIES) {
            // clear leftmost entry and adjust history position
            history.remove(0);
            historyPos--;
        }

    }

    /**
     * Set a window-scope variable
     * 
     * @param key
     *            the identifier, must not be NULL
     * @param value
     *            the value, must not be NULL. Use removeAttribute() to remove a key
     */
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    /**
     * Get a window-scope variable
     * 
     * @param key
     *            the identifier, must not be NULL
     * @return The object or NULL if no object exists for this key
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * Remove a windo-scope variable
     * 
     * @param key
     *            the identifier, must not be NULL
     * @param the
     *            previously attribute that was set for this key or NULL when the key was not set at all
     */
    public Object removeAttribute(String key) {
        return attributes.remove(key);
    }

    /**
     * Convenience method to get DTabs.
     */
    public DTabs getDynamicTabs() {
        return (DTabs) attributes.get("DTabs");
    }

    /**
     * to be called by Window.java or the AjaxController only! this method is synchronized on the Window instance
     * 
     * @return a updateUI-Command or null if there are no dirty components (normally not the case for sync (user-click) request, but often the case for pull request,
     *         since nothing has changed yet on the screen.
     */
    public Command handleDirties() {
        // need to sync to window, since the dispatching must be finished so that the render tree is stable before we collect the dirties.
        // more accurately, the synchronized is needed when other classes than window call this method.
        synchronized (this) {
            Command com = null;
            StringBuilder debugMsg = null;
            long start = 0;
            if (log.isDebugEnabled()) {
                log.debug("Perf-Test: Window.handleDirties started...");
                start = System.currentTimeMillis();
            }

            final List<Component> dirties = new ArrayList<Component>();
            ComponentVisitor dirtyV = new ComponentVisitor() {
                public boolean visit(Component comp, UserRequest ureq) {
                    boolean visitChildren = false;
                    if (!comp.isVisible()) {
                        // a component just made -visible- still needs to be collected (detected by checking dirty flag)
                        if (comp.isDirty()) {
                            dirties.add(comp);
                            comp.setDirty(false); // clear manually here since this component will not be rendered
                        }
                    } else if (comp.isDirty()) {
                        dirties.add(comp);
                    } else {
                        // visible and not dirty -> visit children
                        visitChildren = true;
                    }
                    return visitChildren;
                }
            };
            ComponentTraverser ct = new ComponentTraverser(dirtyV, getContentPane(), false);
            ct.visitAll(null);
            if (log.isDebugEnabled()) {
                long durationVisitAll = System.currentTimeMillis() - start;
                log.debug("Perf-Test: Window.handleDirties after ct.visitAll durationVisitAll=" + durationVisitAll);
            }
            int dCnt = dirties.size();
            log.debug("Perf-Test: Window.handleDirties dirties.size()=" + dirties.size());
            if (dCnt > 0) { // collect the redraw dirties command
                try {
                    JSONObject root = new JSONObject();
                    root.put("cc", dirties.size());
                    root.put("wts", timestamp);
                    JSONArray ja = new JSONArray();
                    root.put("cps", ja);

                    GlobalSettings gsettings = wbackofficeImpl.getGlobalSettings();

                    synchronized (render_mutex) { // o_clusterOK by:fj
                        // we let all dirty components render themselves.
                        // not offered (since not usability-useful) is the include of new js-libraries and css-libraries here, since this may invoke a screen reload
                        // which disturbes the user and lets him/her loose the focus and the cursor.
                        AsyncMediaResponsible amr = null;

                        long rstart = 0;
                        if (log.isDebugEnabled()) {
                            rstart = System.currentTimeMillis();
                            debugMsg = new StringBuilder("update:").append(String.valueOf(dCnt)).append(";");
                        }

                        for (int i = 0; i < dCnt; i++) {
                            long startLoop = System.currentTimeMillis();
                            Component toRender = dirties.get(i);
                            log.debug("Perf-Test: Window.handleDirties toRender.getComponentName()=" + toRender.getComponentName());
                            log.debug("Perf-Test: Window.handleDirties toRender=" + toRender);
                            boolean wasDomR = toRender.isDomReplaceable();
                            if (!wasDomR) {
                                throw new AssertException("cannot replace as dom fragment:" + toRender.getComponentName() + " (" + toRender.getClass().getName() + "),"
                                        + toRender.getExtendedDebugInfo());
                            }

                            Panel wrapper = new Panel("renderpanel");
                            wrapper.setDomReplaceable(false); // to omit <div> around the render helper panel
                            RenderResult renderResult = null;
                            StringOutput jsol = new StringOutput();
                            StringOutput hdr = new StringOutput();
                            String result = null;
                            try {
                                toRender.setDomReplaceable(false);
                                wrapper.setContent(toRender);
                                String newTimestamp = String.valueOf(timestamp);
                                URLBuilder ubu = new URLBuilder(uriPrefix, getInstanceId(), newTimestamp, wbackofficeImpl);

                                renderResult = new RenderResult();

                                // if we have an around-component-interception
                                // set the handler for this render cycle
                                InterceptHandler interceptHandler = wbackofficeImpl.getInterceptHandler();
                                if (interceptHandler != null) {
                                    InterceptHandlerInstance dhri = interceptHandler.createInterceptHandlerInstance();
                                    renderResult.setInterceptHandlerRenderInstance(dhri);
                                }

                                Renderer fr = Renderer.getInstance(wrapper, null, ubu, renderResult, gsettings);

                                jsol = new StringOutput();
                                fr.renderBodyOnLoadJSFunctionCall(jsol, toRender);

                                hdr = new StringOutput();
                                fr.renderHeaderIncludes(hdr, toRender);

                                long pstart = 0;
                                if (log.isDebugEnabled()) {
                                    pstart = System.currentTimeMillis();
                                }
                                result = fr.render(toRender).toString();
                                if (log.isDebugEnabled()) {
                                    long pstop = System.currentTimeMillis();
                                    debugMsg.append(toRender.getComponentName()).append(":").append((pstop - pstart));
                                    if (i < dCnt - 1)
                                        debugMsg.append(",");
                                }
                            } catch (Exception e) {
                                throw new OLATRuntimeException(Window.class, renderResult.getLogMsg(), renderResult.getRenderException());
                            } finally {
                                toRender.setDomReplaceable(true);
                            }
                            if (renderResult.getRenderException() != null)
                                throw new OLATRuntimeException(Window.class, renderResult.getLogMsg(), renderResult.getRenderException());

                            AsyncMediaResponsible curAmr = renderResult.getAsyncMediaResponsible();
                            if (curAmr != null) {
                                if (amr != null) {
                                    throw new AssertException("can set amr only once in a screen!");
                                } else {
                                    amr = curAmr;
                                }
                            }

                            JSONObject jo = new JSONObject();
                            long cid = toRender.getDispatchID();
                            if (Settings.isDebuging()) {
                                // for debugging only
                                jo.put("cname", toRender.getComponentName());
                                jo.put("clisteners", toRender.getListenerInfo());
                                jo.put("hfragsize", result.length());
                            }

                            jo.put("cid", cid);
                            jo.put("cidvis", toRender.isVisible());
                            jo.put("hfrag", result);
                            jo.put("jsol", jsol);
                            jo.put("hdr", hdr);
                            ja.put(jo);
                            if (log.isDebugEnabled()) {
                                long durationLoop = System.currentTimeMillis() - startLoop;
                                log.debug("Perf-Test: Window.handleDirties loop i=" + i + " durationLoop=" + durationLoop);
                            }
                        }
                        // polling case should never set the asyncMediaResp.
                        // to null otherwise it possible that e.g. pdf served as following click within a CP component
                        if (amr != null)
                            setAsyncMediaResponsible(amr);

                        if (log.isDebugEnabled()) {
                            long rstop = System.currentTimeMillis();
                            debugMsg.append(";inl_part_render:").append((rstop - rstart));
                            log.debug(debugMsg.toString());
                        }

                    }
                    com = CommandFactory.createDirtyComponentsCommand();
                    com.setSubJSON(root);
                    if (log.isDebugEnabled()) {
                        long durationHandleDirties = System.currentTimeMillis() - start;
                        log.debug("Perf-Test: Window.handleDirties finished 1  durationHandleDirties=" + durationHandleDirties);
                    }
                    return com;

                } catch (JSONException e) {
                    throw new AssertException("wrong data put into json object", e);
                }
            }
            if (log.isDebugEnabled()) {
                long durationHandleDirties = System.currentTimeMillis() - start;
                log.debug("Perf-Test: Window.handleDirties finished 2  durationHandleDirties=" + durationHandleDirties);
            }
            return com;
        }
    }

    /**
     * builds a url for this window
     * 
     * @param win
     *            the window id the new url
     * @param timestampId
     * @param componentId
     * @param moduleUri
     * @param bc
     *            the businesscontrolpath
     * @return the new (relative) url as a string
     */
    private String buildURIFor(Window win, String timestampId, String moduleUri) {
        URLBuilder ubu = new URLBuilder(uriPrefix, win.getInstanceId(), timestampId, wbackofficeImpl);
        StringOutput so = new StringOutput();
        ubu.buildURI(so, null, null, moduleUri, 0);
        String uri = so.toString();
        return uri;
    }

    private String buildURIForRedirect(String moduleUri) {
        return buildURIFor(this, "-1", moduleUri);
    }

    private String buildRenderOnlyURIFor(Window win) {
        return buildURIFor(win, null, null);
    }

    /**
     * @param ureq
     * @return true if the event was indeed dispatched to the component, false otherwise (reasons: no component found with given id, or component disabled)
     */
    private boolean doDispatchToComponent(UserRequest ureq, StringBuilder debugMsg) {
        String s_compID = ureq.getComponentID();
        if (s_compID == null)
            return false; // throw new AssertException("no component id found in req:" + ureq.toString());

        Component target;
        List<Component> foundPath = new ArrayList<Component>(10);

        // OLAT-1973
        if (GUIInterna.isLoadPerformanceMode()) {
            String compPath = ureq.getParameter("e");
            Component cur = getContentPane();
            String[] res = compPath.split("!");
            boolean correctFullPath = true;
            for (int i = res.length - 1; i >= 0 && correctFullPath; i--) {
                String cname = res[i];
                Container co = (Container) cur; // we did not record the leaf, so we know it's a container
                Component c = co.getComponent(cname);
                if (c == null) {
                    correctFullPath = false;
                    // throw new AssertException("cannot find: "+compPath);
                } else {
                    foundPath.add(c);
                    cur = c;
                }
            }
            // if we could not find our component following the full path, also search the component in the full component tree.
            // the reason is that simply adding a panel or such around a component should not break existing (jmeter)-functional-tests.
            //
            // As long as we find only one component with a child with the name we search, we are ok and assume this new component to be the
            // same from a gui-side meaning, even if it is in a different parent hierarchy.
            // If more than one match is found (which should occur very rarely when developers choose meaningful names for components
            // to be put into containers), we cannot determine which component was meant and must throw an exception - the functional test
            // will break and needs to be improved/adjusted to the new layout.
            // if no match is found, we could not find the component at all and must raise an exception also.
            //
            if (correctFullPath) {
                // cur is now the component to dispatch
                target = cur;
            } else {
                String childName = res[0]; // Pre: all paths have at least one entry
                List<Component> founds = ReusableURLHelper.findComponentsWithChildName(childName, getContentPane());
                int foundsCnt = founds.size();
                if (foundsCnt == 1) {
                    // unique -> high probability that the recorded link is still the same
                    target = founds.get(0);
                } else if (foundsCnt == 0) {
                    throw new AssertException("cannot find: " + compPath);
                } else { // >1 -> ambiguous, two possible targets
                    throw new AssertException("cannot find: " + compPath);
                }
            }
        } else {
            long compID = Long.parseLong(s_compID); // throws NumberFormatException if
            // not a number
            target = ComponentHelper.findDescendantOrSelfByID(getContentPane(), compID, foundPath);
        }

        if (target == null) {
            // there was a component id given, but no matching target could be found
            fireEvent(ureq, COMPONENTNOTFOUND);
            return false;
            // do not dispatch; which means just rerender later; good if
            // the
            // gui tree was changed by another thread in the meantime.
            // do not throw an exception here, because this can happen if the gui
            // tree was changed by another thread in the meantime
        }
        if (!target.isVisible()) {
            throw new OLATRuntimeException(Window.class, "target with name: '" + target.getComponentName() + "', was invisible, but called to dispatch", null);
        }
        boolean toDispatch = true; // TODO:fj:c is foundpath needed for something else than the enabled-check. if no -> one boolean is enough
        for (Iterator iter = foundPath.iterator(); iter.hasNext();) {
            Component curComp = (Component) iter.next();
            if (!curComp.isEnabled()) {
                toDispatch = false;
                break;
            }
        }
        if (toDispatch) {
            latestDispatchComponentInfo = target.getComponentName() + " :" + target.getExtendedDebugInfo();
            latestDispatchedComp = target;
            Codepoint.setThreadLocalLogDetails(latestDispatchComponentInfo);

            // dispatch
            wbackofficeImpl.fireCycleEvent(Window.ABOUT_TO_DISPATCH);
            target.dispatchRequest(ureq);

            // after dispatching, commit (docu)
            DBFactory.getInstance().commit();

            // add the new URL to the browser history, but not if the klick resulted in a new browser window (a href .. target=...) or in a download (excel or such)
            DispatchResult dr = ureq.getDispatchResult();
            MediaResource tMr = dr.getResultingMediaResource();
            Window tWin = dr.getResultingWindow();
            if (tMr == null && tWin == null) {
                addToHistory(new HistoryEntry(ureq));
            }
            wbackofficeImpl.fireCycleEvent(END_OF_DISPATCH_CYCLE);

            // if loglevel is set accordingly, collect anonymous controller usage statistics.
            if (debugMsg != null) {
                Controller c = target.getLatestDispatchedController();
                if (c != null) {
                    WindowControl wCo = null;
                    try {
                        wCo = c.getWindowControlForDebug();
                    } catch (Exception e) {
                        // getWindowControl throw an Assertion if wControl = null
                    }
                    if (wCo != null) {
                        String coInfo = "";
                        WindowControlInfo wci = wCo.getWindowControlInfo();
                        while (wci != null) {
                            String cName = wci.getControllerClassName();
                            coInfo = cName + ":" + coInfo;
                            wci = wci.getParentWindowControlInfo();
                        }

                        BusinessControl bc = wCo.getBusinessControl();
                        String businessPath = bc == null ? "n/a" : bc.getAsString();
                        String compName = target.getComponentName();
                        String msg = "wci:" + coInfo + "%%" + compName + "%%" + businessPath + "%%";
                        // allowed for debugging, dispatching is already over
                        Event ev = target.getAndClearLatestFiredEvent();
                        if (ev != null) {
                            msg += ev.getClass().getName() + ":" + ev.getCommand() + "%%";
                        }
                        String targetInfo = target.getExtendedDebugInfo();
                        msg += targetInfo + "%%";
                        debugMsg.append(msg).append(LOG_SEPARATOR);
                        // log.debug(msg);
                    } else {
                        // no windowcontrol -> ignore
                    }
                } // else: a component with -no- controller as listener, makes no sense in 99.99% of the cases; ignore in those rare cases
            } else {
                // no debug level, consume the left over event (for minor memory reasons)
                target.getAndClearLatestFiredEvent();
            }

            // we do not want to keep a reference which could be old.
            // in case we do not reach the next line because of an exception in dispatch(), we clear the value in the exceptionwindowcontroller's error handling
            latestDispatchedComp = null;
        }
        return toDispatch;
    }

    /**
     * Sets the asyncMediaResponsible.
     * 
     * @param asyncMediaResponsible
     *            The asyncMediaResponsible to set
     */
    private void setAsyncMediaResponsible(AsyncMediaResponsible asyncMediaResponsible) {
        this.asyncMediaResponsible = asyncMediaResponsible;
    }

    /**
     * @return String
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * Sets the instanceId.
     * 
     * @param instanceId
     *            The instanceId to set
     */
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * Sets the uriPrefix.
     * 
     * @param uriPrefix
     *            The uriPrefix to set
     */
    public void setUriPrefix(String uriPrefix) {
        this.uriPrefix = uriPrefix;
    }

    /**
     * @return LatestDispatchComponentInfo
     */
    public String getLatestDispatchComponentInfo() {
        return latestDispatchComponentInfo;
    }

    /**
     * @return the chiefcontroller that owns this window
     */
    /*
     * public ChiefController getChiefController() { return chiefController; }
     */

    public ComponentRenderer getHTMLRendererSingleton() {
        throw new AssertException("a window should never be rendered, but its contentpane");
    }

    /**
     * to be used for exception reporting only!
     * 
     * @return
     */
    public Component getAndClearLatestDispatchedComponent() {
        Component tmp = latestDispatchedComp;
        latestDispatchedComp = null;
        return tmp;
    }

}

class ValidatingVisitor implements ComponentVisitor {
    private ValidationResult validationResult;

    /**
	 * 
	 */
    public ValidatingVisitor(GlobalSettings globalSetting, JSAndCSSAdder jsAndCSSAdder) {
        validationResult = new ValidationResult(globalSetting, jsAndCSSAdder);
    }

    /**
	 */
    public boolean visit(Component comp, UserRequest ureq) {
        // validate only visble components
        if (comp.isVisible()) {
            comp.validate(ureq, validationResult);
            return true;
        }
        return false;
    }

    /**
     * @return the validationresult
     */
    ValidationResult getValidationResult() {
        return validationResult;
    }
}
