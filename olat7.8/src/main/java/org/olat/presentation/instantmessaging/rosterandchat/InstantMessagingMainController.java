/**
 * OLAT - Online Learning and Training<br />
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br />
 * you may not use this file except in compliance with the License.<br />
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br />
 * software distributed under the License is distributed on an "AS IS" BASIS, <br />
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br />
 * See the License for the specific language governing permissions and <br />
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br />
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.presentation.instantmessaging.rosterandchat;

/* TODO: ORID-1007 'File' */
//import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.olat.lms.ims.qti.process.AssessmentInstance;
import org.olat.lms.instantmessaging.Chat_EBL;
import org.olat.lms.instantmessaging.ClientHelper;
import org.olat.lms.instantmessaging.ClientManager;
import org.olat.lms.instantmessaging.ConncectedUsersHelper;
import org.olat.lms.instantmessaging.InstantMessagingEvent;
import org.olat.lms.instantmessaging.InstantMessagingModule;
import org.olat.presentation.course.nodes.iq.AssessmentEvent;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.creator.ControllerCreator;
import org.olat.presentation.framework.core.control.floatingresizabledialog.FloatingResizableDialogController;
import org.olat.presentation.framework.core.control.generic.popup.PopupBrowserWindow;
import org.olat.presentation.framework.core.themes.Theme;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.presentation.framework.layout.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.presentation.instantmessaging.ConnectedClientsListController;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.WebappHelper;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.event.Event;
import org.olat.system.event.EventBus;
import org.olat.system.event.GenericEventListener;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br />
 * Main controller which initates the connection and provides status change/roster and chat possibilities
 * <P>
 * Initial Date: 26.04.2007 <br />
 * 
 * @author guido
 */
public class InstantMessagingMainController extends BasicController implements GenericEventListener {

    private static final Logger log = LoggerHelper.getLogger();
    public static final String ACTION_CHAT = "cmd.chat";
    public static final String ACTION_MSG = "cmd.msg";
    private final VelocityContainer main = createVelocityContainer("index");
    private final VelocityContainer chatContent = createVelocityContainer("chat");
    private final VelocityContainer chatMsgFieldContent = createVelocityContainer("chatMsgField");
    private final VelocityContainer statusChangerContent = createVelocityContainer("statusChangerContent");
    private final VelocityContainer statusChangerLink = createVelocityContainer("statusChangerLink");
    private final VelocityContainer newMsgIcon = createVelocityContainer("newMsgIcon");
    private final VelocityContainer buddiesSummary = createVelocityContainer("buddiesSummary");
    private final VelocityContainer buddiesList = createVelocityContainer("buddiesList");
    private final VelocityContainer buddiesListContent = createVelocityContainer("buddiesListContent");
    private final Panel notifieNewMsgPanel;
    private final Panel buddiesSummaryPanel, statusPanel;
    private final Panel statusChangerPanel, rosterPanel;
    private final Link available, unavailable, dnd, xa, away;
    private final Link chat, onlineOfflineCount;
    private final Link toggleOffline, toggleGroups, showOtherUsers, statusChanger;
    private int toggleOfflineMode;
    private int toggleGroupsMode;
    private final ClientHelper clientHelper;
    private final Panel buddiesListContentPanel;
    private final ClientManager clientManager;
    private final String username;
    private FloatingResizableDialogController statusChangerPanelCtr;
    private FloatingResizableDialogController rosterPanelCtr;
    private FloatingResizableDialogController chatPanelCtr;
    private final JSAndCSSComponent jsc;
    private final ChatManagerController chatMgrCtrl;
    private final Map<String, NewMessageIconInfo> showNewMessageHolder = new HashMap<String, NewMessageIconInfo>(2);

    private final EventBus singleUserEventCenter;
    private final OLATResourceable ass;

    public InstantMessagingMainController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        username = getIdentity().getName();
        clientHelper = new ClientHelper(username, this, buddiesListContent, getTranslator());
        this.clientManager = InstantMessagingModule.getAdapter().getClientManager();
        clientManager.registerEventListener(username, this, true);

        final boolean ajaxOn = getWindowControl().getWindowBackOffice().getWindowManager().isAjaxEnabled();
        chatContent.contextPut("isAjaxMode", Boolean.valueOf(ajaxOn));

        // checks with the given intervall if dirty components are available to
        // rerender
        jsc = new JSAndCSSComponent("intervall", this.getClass(), null, null, false, null, InstantMessagingModule.getIDLE_POLLTIME());
        main.put("updatecontrol", jsc);

        // configure new message sound
        newMsgIcon.contextPut("iconsHolder", showNewMessageHolder);

        String newMessageSoundURL = getNewMessageSoundUrl();

        newMsgIcon.contextPut("newMessageSoundURL", newMessageSoundURL);

        notifieNewMsgPanel = new Panel("newMsgPanel");
        notifieNewMsgPanel.setContent(newMsgIcon);

        statusChanger = LinkFactory.createCustomLink("statusChanger", "cmd.status", "", Link.NONTRANSLATED, statusChangerLink, this);
        statusChanger.setCustomEnabledLinkCSS("b_small_icon o_instantmessaging_" + clientManager.getInstantMessagingClient(username).getDefaultRosterStatus() + "_icon");
        statusChanger.registerForMousePositionEvent(true);
        statusChanger.setTooltip(getTranslator().translate("im.status.change.long"), false);
        statusChangerPanel = new Panel("statusChangerPanel");
        statusChangerPanel.setContent(statusChanger);

        statusPanel = new Panel("statusPanel");

        // set defaults
        buddiesSummaryPanel = new Panel("buddiesSummaryPanel");
        buddiesSummaryPanel.setContent(buddiesSummary);

        rosterPanel = new Panel("rosterPanel");

        onlineOfflineCount = LinkFactory.createCustomLink("onlineOfflineCount", "cmd.roster", "", Link.NONTRANSLATED, buddiesSummary, this);
        onlineOfflineCount.setCustomDisplayText("(?/?)");
        onlineOfflineCount.setTooltip(getTranslator().translate("im.roster.intro"), false);
        onlineOfflineCount.registerForMousePositionEvent(true);

        /**
         * status changer links
         */
        available = LinkFactory.createLink("presence.available", statusChangerContent, this);
        available.setCustomEnabledLinkCSS("o_instantmessaging_available_icon");

        chat = LinkFactory.createLink("presence.chat", statusChangerContent, this);
        chat.setCustomEnabledLinkCSS("o_instantmessaging_chat_icon");

        away = LinkFactory.createLink("presence.away", statusChangerContent, this);
        away.setCustomEnabledLinkCSS("o_instantmessaging_away_icon");

        xa = LinkFactory.createLink("presence.xa", statusChangerContent, this);
        xa.setCustomEnabledLinkCSS("o_instantmessaging_xa_icon");

        dnd = LinkFactory.createLink("presence.dnd", statusChangerContent, this);
        dnd.setCustomEnabledLinkCSS("o_instantmessaging_dnd_icon");

        unavailable = LinkFactory.createLink("presence.unavailable", statusChangerContent, this);
        unavailable.setCustomEnabledLinkCSS("o_instantmessaging_unavailable_icon");

        statusChangerContent.contextPut("contextpath", WebappHelper.getServletContextPath());
        statusChangerContent.contextPut("lang", ureq.getLocale().toString());

        /**
         * buddies list links
         */

        toggleOffline = LinkFactory.createCustomLink("toggleOffline", "cmd.offline", "", Link.NONTRANSLATED, buddiesList, this);
        toggleOffline.setCustomDisplayText(getTranslator().translate("im.show.offline.buddies"));
        toggleOffline.setCustomEnabledLinkCSS("o_instantmessaging_showofflineswitch");

        toggleGroups = LinkFactory.createCustomLink("toggleGroups", "cmd.groups", "", Link.NONTRANSLATED, buddiesList, this);
        toggleGroups.setCustomDisplayText(getTranslator().translate("im.show.groups"));
        toggleGroups.setCustomEnabledLinkCSS("o_instantmessaging_showgroupswitch");

        showOtherUsers = LinkFactory.createLink("im.others.connected", buddiesList, this);
        showOtherUsers.setCustomEnabledLinkCSS("o_instantmessaging_footerlinks");
        showOtherUsers.setAjaxEnabled(false); // opens new window -> disable
                                              // background post!
        showOtherUsers.setTarget("_blank");

        buddiesList.contextPut("contextpath", WebappHelper.getServletContextPath());
        buddiesList.contextPut("lang", ureq.getLocale().toString());
        buddiesList.contextPut("othersConnected", new ConncectedUsersHelper());

        buddiesListContent.contextPut("imclient", clientHelper);
        buddiesListContentPanel = new Panel("buddiesListContent");
        buddiesListContentPanel.setContent(buddiesListContent);
        buddiesList.put("buddiesListContent", buddiesListContentPanel);

        main.put("newMsgPanel", notifieNewMsgPanel);
        main.put("statusChangerPanel", statusChangerPanel);
        main.put("buddiesSummaryPanel", buddiesSummaryPanel);
        main.put("rosterPanel", rosterPanel);
        main.put("statusPanel", statusPanel);

        // creates and manages the p2p chats
        chatMgrCtrl = new ChatManagerController(ureq, wControl);
        listenTo(chatMgrCtrl);
        newMsgIcon.put("chats", chatMgrCtrl.getInitialComponent());

        ass = OresHelper.createOLATResourceableType(AssessmentEvent.class);
        singleUserEventCenter = ureq.getUserSession().getSingleUserEventCenter();
        singleUserEventCenter.registerFor(this, getIdentity(), ass);

        putInitialPanel(main);
    }

    /**
     * @return
     */
    private String getNewMessageSoundUrl() {
        final Theme guiTheme = getWindowControl().getWindowBackOffice().getWindow().getGuiTheme();
        Chat_EBL chatEbl = CoreSpringFactory.getBean(Chat_EBL.class);
        String newMessageSoundURL = chatEbl.getNewMessageSoundURL(guiTheme.getIdentifyer(), guiTheme.getBaseURI());
        return newMessageSoundURL;
    }

    /**
	 */
    @Override
    protected void doDispose() {
        InstantMessagingModule.getAdapter().getClientManager().deregisterControllerListener(username, this);
        InstantMessagingModule.getAdapter().getClientManager().destroyInstantMessagingClient(username);
        singleUserEventCenter.deregisterFor(this, ass);
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.system.event.control.Event)
     */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (clientHelper.isConnected()) {
            final boolean ajaxOn = getWindowControl().getWindowBackOffice().getWindowManager().isAjaxEnabled();
            chatContent.contextPut("isAjaxMode", Boolean.valueOf(ajaxOn));

            if (source == statusChanger) {
                removeAsListenerAndDispose(statusChangerPanelCtr);
                statusChangerPanelCtr = new FloatingResizableDialogController(ureq, getWindowControl(), statusChangerContent, getTranslator().translate(
                        "im.status.change"), 210, 200, statusChanger.getOffsetX() - 130, statusChanger.getOffsetY() + 25, null, null, false, false, true, "im_status");
                listenTo(statusChangerPanelCtr);
                statusPanel.setContent(statusChangerPanelCtr.getInitialComponent());
                statusChanger.setDirty(false);

            } else if (source == onlineOfflineCount) {
                if (!clientHelper.isChatDisabled()) { // only open panel when
                                                      // chat is not disabled
                    removeAsListenerAndDispose(rosterPanelCtr);
                    rosterPanelCtr = new FloatingResizableDialogController(ureq, getWindowControl(), buddiesList, getTranslator().translate("im.buddies"), 300, 500,
                            onlineOfflineCount.getOffsetX() - 80, onlineOfflineCount.getOffsetY() + 25, null, null, true, true, true, "im_roster");
                    listenTo(rosterPanelCtr);
                    rosterPanel.setContent(rosterPanelCtr.getInitialComponent());
                    onlineOfflineCount.setDirty(false);
                }

            } else if (source == available) {
                clientHelper.sendPresenceAvailable(Presence.Mode.available);
                changeCSS();
            } else if (source == chat) {
                clientHelper.sendPresenceAvailable(Presence.Mode.chat);
                changeCSS();
            } else if (source == away) {
                clientHelper.sendPresenceAvailable(Presence.Mode.away);
                changeCSS();
            } else if (source == xa) {
                clientHelper.sendPresenceAvailable(Presence.Mode.xa);
                changeCSS();
            } else if (source == dnd) {
                clientHelper.sendPresenceAvailable(Presence.Mode.dnd);
                changeCSS();
            } else if (source == unavailable) {
                clientHelper.sendPresenceUnavailable();
                changeCSS();
            }
            // buddies list
            else if (source == toggleOffline) {
                if (toggleOfflineMode == 0) {
                    toggleOffline.setCustomDisplayText(getTranslator().translate("im.hide.offline.buddies"));
                    toggleOfflineMode = 1;
                    clientHelper.setShowOfflineBuddies(true);
                } else {
                    toggleOffline.setCustomDisplayText(getTranslator().translate("im.show.offline.buddies"));
                    toggleOfflineMode = 0;
                    clientHelper.setShowOfflineBuddies(false);
                }
                buddiesListContent.setDirty(true);

            } else if (source == toggleGroups) {
                if (toggleGroupsMode == 0) {
                    toggleGroups.setCustomDisplayText(getTranslator().translate("im.hide.groups"));
                    toggleGroupsMode = 1;
                    clientHelper.setShowGroupsInRoster(true);
                } else {
                    toggleGroups.setCustomDisplayText(getTranslator().translate("im.show.groups"));
                    toggleGroupsMode = 0;
                    clientHelper.setShowGroupsInRoster(false);
                }
                buddiesListContent.setDirty(true);

            } else if (source == showOtherUsers) {
                // open new window with a list of online users
                final ControllerCreator ctrlCreator = new ControllerCreator() {
                    @Override
                    public Controller createController(final UserRequest lureq, final WindowControl lwControl) {
                        final ConnectedClientsListController clientsListCtr = new ConnectedClientsListController(lureq, lwControl);
                        final LayoutMain3ColsController mainLayoutCtr = new LayoutMain3ColsController(lureq, lwControl, null, null, clientsListCtr.getInitialComponent(),
                                null);
                        mainLayoutCtr.addDisposableChildController(clientsListCtr);
                        return mainLayoutCtr;
                    }
                };
                // wrap the content controller into a full header layout
                final ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
                // open in new browser window
                final PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, layoutCtrlr);
                pbw.open(ureq);
                //
            }

            if (source instanceof Link) {
                final Link link = (Link) source;

                // chat gets created by click on buddy list
                if (link.getCommand().equals(ACTION_CHAT)) {
                    chatMgrCtrl.createChat(ureq, getWindowControl(), (String) link.getUserObject(), link.getOffsetX() - 470, link.getOffsetY(), true, null);
                } else if (link.getCommand().equals(ACTION_MSG)) {// chats gets
                                                                  // created
                                                                  // by click
                                                                  // on new
                                                                  // message
                                                                  // icon
                    final NewMessageIconInfo info = (NewMessageIconInfo) link.getUserObject();
                    final Link msgLink = info.getNewMessageLink();
                    chatMgrCtrl.createChat(ureq, getWindowControl(), info.getJabberId(), msgLink.getOffsetX() - 470, msgLink.getOffsetY(), true,
                            info.getInitialMessages());
                    showNewMessageHolder.remove(info.getJabberId());
                    newMsgIcon.setDirty(true);
                }
            }

        } else {
            // connection is broken, set "service not available" to the GUI
            onlineOfflineCount.setCustomDisplayText("n/a");
            onlineOfflineCount.setTitle(getTranslator().translate("im.error.connection"));
            statusChanger.setCustomEnabledLinkCSS("b_small_icon o_instantmessaging_error_icon");
        }

    }

    private void changeCSS() {
        statusChanger.setCustomEnabledLinkCSS("b_small_icon o_instantmessaging_" + clientHelper.getStatus() + "_icon");
        getWindowControl().setInfo(getTranslator().translate("new.status") + " " + getTranslator().translate("presence." + clientHelper.getStatus()));
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == statusChangerPanelCtr) {
            // closing the floating panel event
            statusChangerPanel.setContent(statusChangerLink);
            removeAsListenerAndDispose(statusChangerPanelCtr);
            statusPanel.setContent(null);
            statusChanger.setDirty(false);
        } else if (source == rosterPanelCtr) {
            // closing the floating panel event
            buddiesSummaryPanel.setContent(onlineOfflineCount);
            onlineOfflineCount.setCustomDisplayText(clientHelper.buddyCountOnline());
            removeAsListenerAndDispose(rosterPanelCtr);
            rosterPanel.setContent(null);

        } else if (source == chatPanelCtr) {
            // closing the floating panel event
            notifieNewMsgPanel.setContent(newMsgIcon);
            chatMsgFieldContent.contextPut("chatMessages", "");
            jsc.setRefreshIntervall(InstantMessagingModule.getIDLE_POLLTIME());
        } else if (source == chatMgrCtrl) {
            // closing events from chat manager controller
            notifieNewMsgPanel.setContent(newMsgIcon);
        }
    }

    /**
     * gets called if either a new message or a presence change from one of the buddies happens or an Assessment starts or ends
     * 
     */
    @Override
    public void event(final Event event) {

        if (event instanceof AssessmentEvent) {
            if (((AssessmentEvent) event).getEventType().equals(AssessmentEvent.TYPE.STARTED)) {
                main.contextPut("inAssessment", true);
                return;
            }
            if (((AssessmentEvent) event).getEventType().equals(AssessmentEvent.TYPE.STOPPED)) {
                final OLATResourceable a = OresHelper.createOLATResourceableType(AssessmentInstance.class);
                if (singleUserEventCenter.getListeningIdentityCntFor(a) < 1) {
                    main.contextPut("inAssessment", false);
                }
                return;
            }
        }

        final InstantMessagingEvent imEvent = (InstantMessagingEvent) event;
        if (imEvent.getCommand().equals("presence")) {
            final Presence presence = (Presence) imEvent.getPacket();
            log.debug("incoming presence for user: " + presence.getFrom() + " type: " + presence, null);
            getWindowControl().getWindowBackOffice().invokeLater(new Runnable() {
                @Override
                public void run() {
                    // smack does not immediately update it's presence
                    // information, so when testing only with one person
                    // the OnlineOffline count value can be wrong
                    // in a productive environment there are normally
                    // lot's of presence packages
                    onlineOfflineCount.setCustomDisplayText(clientHelper.buddyCountOnline());
                    onlineOfflineCount.setDirty(true);
                    // adjust also presence icon in gui it may be auto
                    // changed by cronjob
                    statusChanger.setCustomEnabledLinkCSS("b_small_icon o_instantmessaging_" + clientHelper.getStatus() + "_icon");
                    statusChanger.setDirty(true);
                }
            });

        } else if (imEvent.getCommand().equals("message")) {
            // user receives messages from an other user
            final Message initialMessage = (Message) imEvent.getPacket();
            final String jid = extractJid(initialMessage.getFrom());

            if (((initialMessage.getType() == Message.Type.chat || initialMessage.getType() == Message.Type.normal)) && initialMessage.getBody() != null) {
                if (!chatMgrCtrl.hasRunningChat(jid)) {// only show icon if no
                                                       // chat running or msg
                                                       // from other user
                    // add follow up message to info holder
                    if (showNewMessageHolder.get(jid) != null) {
                        final NewMessageIconInfo info = showNewMessageHolder.get(extractJid(initialMessage.getFrom()));
                        info.addInitialMessage(initialMessage);
                    } else {
                        final NewMessageIconInfo newMessageInfo = new NewMessageIconInfo(initialMessage.getFrom(), initialMessage);
                        showNewMessageHolder.put(jid, newMessageInfo);
                        newMessageInfo.setNewMessageLink(createShowNewMessageLink(initialMessage.getFrom(), newMessageInfo));
                    }
                }
            }
        }

    }

    /**
     * creates an new message icon link
     * 
     * @param jabberId
     */
    private Link createShowNewMessageLink(final String jabberId, final NewMessageIconInfo newMessageInfo) {
        final Link link = LinkFactory.createCustomLink(extractJid(jabberId), ACTION_MSG, "", Link.NONTRANSLATED, newMsgIcon, this);
        link.registerForMousePositionEvent(true);
        link.setCustomEnabledLinkCSS("b_small_icon o_instantmessaging_new_msg_icon");
        link.setTooltip(getTranslator().translate("im.new.message", new String[] { StringHelper.escapeHtml(jabberId) }), false);
        link.setUserObject(newMessageInfo);
        return link;
    }

    /**
     * extract the jabber id without the resource appendix
     * 
     * @param jabberId
     * @return
     */
    private String extractJid(final String jabberId) {
        final int pos = jabberId.lastIndexOf("/");
        if (pos > 0) {
            return jabberId.substring(0, jabberId.lastIndexOf("/"));
        }
        return jabberId;
    }

    /**
	 */
    @Override
    public boolean isControllerAndNotDisposed() {
        return !isDisposed();
    }

}
