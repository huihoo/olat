/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.orgrmform
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

package org.olat.presentation.forum;

/* TODO: ORID-1007 'File' */
import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.PersistenceHelper;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.filters.VFSItemExcludePrefixFilter;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.data.forum.Forum;
import org.olat.data.forum.Message;
import org.olat.data.marking.Mark;
import org.olat.data.marking.MarkDAO;
import org.olat.data.marking.MarkResourceStat;
import org.olat.data.user.User;
import org.olat.data.user.UserConstants;
import org.olat.lms.activitylogging.ILoggingAction;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.BusinessControlFactory;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.commons.mediaresource.VFSMediaResource;
import org.olat.lms.core.notification.service.PublishEventTO;
import org.olat.lms.core.notification.service.PublishEventTO.EventType;
import org.olat.lms.core.notification.service.PublisherData;
import org.olat.lms.core.notification.service.SubscriptionContext;
import org.olat.lms.forum.ForumCallback;
import org.olat.lms.forum.ForumHelper;
import org.olat.lms.forum.ForumLoggingAction;
import org.olat.lms.forum.ForumService;
import org.olat.lms.forum.Status;
import org.olat.lms.forum.archiver.ForumArchiveManager;
import org.olat.lms.forum.archiver.ForumRTFFormatter;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.user.UserService;
import org.olat.presentation.commons.ConsumableBoolean;
import org.olat.presentation.framework.common.ControllerFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.table.BooleanColumnDescriptor;
import org.olat.presentation.framework.core.components.table.ColumnDescriptor;
import org.olat.presentation.framework.core.components.table.CustomCellRenderer;
import org.olat.presentation.framework.core.components.table.CustomCssCellRenderer;
import org.olat.presentation.framework.core.components.table.CustomRenderColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;
import org.olat.presentation.framework.core.components.table.GenericObjectArrayTableDataModel;
import org.olat.presentation.framework.core.components.table.Table;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableEvent;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.presentation.marking.MarkingUIFactory;
import org.olat.presentation.notification.ContextualSubscriptionController;
import org.olat.presentation.portfolio.EPUIFactory;
import org.olat.presentation.search.SearchServiceUIFactory;
import org.olat.presentation.search.SearchServiceUIFactory.DisplayOption;
import org.olat.presentation.user.DisplayPortraitController;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.event.Event;
import org.olat.system.event.GenericEventListener;
import org.olat.system.exception.AssertException;
import org.olat.system.exception.OLATSecurityException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description: <br>
 * CREATE: - new thread (topmessage) -> ask ForumCallback 'mayOpenNewThread' - new message -> ask ForumCallback 'mayReplyMessage' <br>
 * READ: - everybody may read every message <br>
 * UPDATE: - who wrote a message may edit and save his message as long as it has no children. - if somebody want to edit a message of somebodyelse -> ask ForumCallback
 * 'mayEditMessageAsModerator' <br>
 * DELETE: - who wrote a message may delete his message as long as it has no children. - if somebody want to delete a message of somebodyelse -> ask ForumCallback
 * 'mayDeleteMessageAsModerator' <br>
 * <br>
 * Notifications: notified when: <br>
 * a new thread is opened <br>
 * a new reply is given <br>
 * a message has been edited <br>
 * but not when a message has been deleted <br>
 * 
 * @author Felix Jost
 * @author Refactorings: Roman Haag, roman.haag@frentix.com, frentix GmbH
 */
public class ForumController extends BasicController implements GenericEventListener {

    private static final Logger log = LoggerHelper.getLogger();

    protected static final String TINYMCE_EMPTYLINE_CODE = "<p>&nbsp;</p>"; // is pre/appended to quote message to allow writing inside.

    private static final String CMD_SHOWDETAIL = "showdetail";
    private static final String CMD_SHOWMARKED = "showmarked";
    private static final String CMD_SHOWNEW = "shownew";

    protected static final String GUI_PREFS_THREADVIEW_KEY = "forum.threadview.enabled";

    private final ForumCallback focallback;

    Message currentMsg; // current Msg (in detailview), so we know after
    private final Formatter f;

    private final Collator collator;

    private Panel forumPanel;

    private final VelocityContainer vcListTitles;
    private VelocityContainer vcEditMessage;
    private VelocityContainer vcThreadView;
    private final VelocityContainer vcFilterView;
    private final Link msgCreateButton;
    private final Link archiveForumButton;
    private Link archiveThreadButton;
    private Link backLinkListTitles;
    private Link backLinkSearchListTitles;
    private List<Map<String, Object>> currentMessagesMap;
    private Link closeThreadButton;
    private Link openThreadButton;
    private Link hideThreadButton;
    private Link showThreadButton;
    private final Link filterForUserButton;

    private final TableController allThreadTableCtr;
    private final TableController singleThreadTableCtr;

    private GenericObjectArrayTableDataModel attdmodel;
    private ForumMessagesTableDataModel sttdmodel;
    private final ForumService fm;
    private final Forum forum;
    private List<Message> msgs;
    private List<Message> threadMsgs;
    private final Set<Long> rms; // all keys from messages that the user already read
    private boolean forumChangedEventReceived = false;

    private DialogBoxController yesno, yesNoSplit, archiveFoDiaCtr, archiveThDiaCtr;
    private TableController moveMessageTableCtr;
    List<Message> threadList;
    private CloseableModalController cmcMoveMsg;

    private final SubscriptionContext subsContext;
    private ContextualSubscriptionController csc;

    private MessageEditController msgEditCtr;
    private ForumThreadViewModeController viewSwitchCtr;
    private Map<Long, Integer> msgDeepMap;

    private boolean searchMode = false;
    private FilterForUserController filterForUserCtr;

    private Controller searchController;

    private final OLATResourceable forumOres;
    private ForumNotificationTypeHandler forumNotificationTypeHandler;

    private boolean showHeader;

    /**
     * @param forum
     * @param focallback
     * @param ureq
     * @param wControl
     */
    ForumController(final Forum forum, final ForumCallback focallback, final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        this.forum = forum;
        this.focallback = focallback;
        addLoggingResourceable(LoggingResourceable.wrap(forum));

        showHeader = true;
        forumOres = OresHelper.createOLATResourceableInstance(Forum.class, forum.getKey());
        f = Formatter.getInstance(ureq.getLocale());
        fm = getForumService();

        msgs = fm.getMessagesByForum(forum);

        collator = Collator.getInstance(ureq.getLocale());
        collator.setStrength(Collator.PRIMARY);

        forumPanel = new Panel("forumPanel");
        forumPanel.addListener(this);

        // create page
        vcListTitles = createVelocityContainer("list_titles");
        vcListTitles.contextPut("showHeader", showHeader);

        msgCreateButton = LinkFactory.createButtonSmall("msg.create", vcListTitles, this);
        archiveForumButton = LinkFactory.createButtonSmall("archive.forum", vcListTitles, this);
        filterForUserButton = LinkFactory.createButtonSmall("filter", vcListTitles, this);

        if (!this.isGuestOnly(ureq)) {
            final SearchServiceUIFactory searchServiceUIFactory = (SearchServiceUIFactory) CoreSpringFactory.getBean(SearchServiceUIFactory.class);
            searchController = searchServiceUIFactory.createInputController(ureq, wControl, DisplayOption.STANDARD, null);
            listenTo(searchController);
            vcListTitles.put("search_input", searchController.getInitialComponent());
        }

        // a list of titles of all messages in all threads
        vcListTitles.contextPut("security", focallback);

        // --- subscription ---
        subsContext = focallback.getSubscriptionContext();
        forumNotificationTypeHandler = CoreSpringFactory.getBean(ForumNotificationTypeHandler.class);
        // if sc is null, then no subscription is desired
        if (subsContext != null) {
            final String data = String.valueOf(forum.getKey());
            final PublisherData pdata = new PublisherData(OresHelper.calculateTypeName(Forum.class), data);

            csc = new ContextualSubscriptionController(ureq, getWindowControl(), subsContext, pdata);
            listenTo(csc);

            vcListTitles.put("subscription", csc.getInitialComponent());
        }

        final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
        tableConfig.setCustomCssClass("o_forum");
        tableConfig.setSelectedRowUnselectable(true);
        tableConfig.setDownloadOffered(false);
        tableConfig.setTableEmptyMessage(translate("forum.emtpy"));

        allThreadTableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
        listenTo(allThreadTableCtr);
        allThreadTableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.typeimg", 0, null, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT,
                new MessageIconRenderer()));
        allThreadTableCtr.addColumnDescriptor(new StickyRenderColumnDescriptor("table.thread", 1, CMD_SHOWDETAIL, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT,
                new StickyThreadCellRenderer()));
        allThreadTableCtr.addColumnDescriptor(new StickyColumnDescriptor("table.userfriendlyname", 2, null, ureq.getLocale()));
        allThreadTableCtr.addColumnDescriptor(new StickyColumnDescriptor("table.lastModified", 3, null, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_CENTER));
        allThreadTableCtr.addColumnDescriptor(new StickyColumnDescriptor("table.marked", 4, CMD_SHOWMARKED, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_RIGHT));
        allThreadTableCtr.addColumnDescriptor(new StickyColumnDescriptor("table.unread", 5, CMD_SHOWNEW, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_RIGHT));
        allThreadTableCtr.addColumnDescriptor(new StickyColumnDescriptor("table.total", 6, null, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_RIGHT));

        singleThreadTableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
        listenTo(singleThreadTableCtr);
        singleThreadTableCtr.addColumnDescriptor(new ThreadColumnDescriptor("table.title", 0, CMD_SHOWDETAIL));
        singleThreadTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.userfriendlyname", 1, null, ureq.getLocale()));
        singleThreadTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.modified", 2, null, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_CENTER));
        singleThreadTableCtr.addColumnDescriptor(new BooleanColumnDescriptor("table.header.state", 3, "", translate("table.row.new")));

        rms = getReadSet(ureq.getIdentity()); // here we fetch which messages had

        // been read by the user
        threadList = prepareListTitles(msgs);

        // precalulate message deepness
        precalcMessageDeepness(msgs);

        // Default view
        forumPanel = putInitialPanel(vcListTitles);
        // jump to either the forum or the folder if the business-launch-path says so.
        final BusinessControl bc = getWindowControl().getBusinessControl();
        final ContextEntry ce = bc.popLauncherContextEntry();
        if (ce != null) { // a context path is left for me
            if (log.isDebugEnabled()) {
                log.debug("businesscontrol (for further jumps) would be: " + bc.toString());
            }
            final OLATResourceable ores = ce.getOLATResourceable();
            if (log.isDebugEnabled()) {
                log.debug("OLATResourceable= " + ores.toString());
            }
            final Long resId = ores.getResourceableId();
            if (resId.longValue() != 0) {
                if (log.isDebugEnabled()) {
                    log.debug("messageId=" + ores.getResourceableId());
                }
                currentMsg = fm.findMessage(ores.getResourceableId());
                if (currentMsg != null) {
                    showThreadView(ureq, currentMsg, null);
                    scrollToCurrentMessage();
                } else {
                    // message not found, do nothing. Load normal start screen
                    showError("deleteok");
                    log.debug("Invalid messageId=" + ores.getResourceableId());
                }
            } else {
                // FIXME:chg: Should not happen, occurs when course-node are called
                if (log.isDebugEnabled()) {
                    log.debug("Invalid messageId=" + ores.getResourceableId().toString());
                }
            }
        }

        // Register for forum events
        CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), forum);

        // filter for user
        vcFilterView = createVelocityContainer("filter_view");
    }

    /**
     * If event received, must update thread overview.
     */
    private boolean checkForumChangedEventReceived() {
        if (forumChangedEventReceived) {
            this.showThreadOverviewView();
            forumChangedEventReceived = false;
            return true;
        }
        return false;
    }

    /**
	 */
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (checkForumChangedEventReceived()) {
            return;
        }
        final String cmd = event.getCommand();
        if (source == msgCreateButton) {
            showNewThreadView(ureq);
        } else if (source == archiveForumButton) {
            archiveFoDiaCtr = activateYesNoDialog(ureq, null, translate("archive.forum.dialog"), archiveFoDiaCtr);
        } else if (source == filterForUserButton) {
            showFilterForUserView(ureq);
        } else if (source == backLinkListTitles) { // back link list titles
            if (searchMode && filterForUserCtr != null && filterForUserCtr.isShowResults()) {
                forumPanel.setContent(vcFilterView);
            } else {
                searchMode = false;
                showThreadOverviewView();
            }
        } else if (source == backLinkSearchListTitles) {
            if (filterForUserCtr != null && filterForUserCtr.isShowResults()) {
                filterForUserCtr.setShowSearch();
            } else {
                showThreadOverviewView();
            }
        } else if (source == archiveThreadButton) {
            archiveThDiaCtr = activateYesNoDialog(ureq, null, translate("archive.thread.dialog"), archiveThDiaCtr);
        } else if (source == closeThreadButton) {
            closeThread(ureq, currentMsg, true);
        } else if (source == openThreadButton) {
            closeThread(ureq, currentMsg, false);
        } else if (source == hideThreadButton) {
            hideThread(ureq, currentMsg, true);
        } else if (source == showThreadButton) {
            hideThread(ureq, currentMsg, false);
        } else if (source == vcThreadView) {
            if (cmd.startsWith("attachment_")) {
                final Map<String, Object> messageMap = getMessageMapFromCommand(ureq.getIdentity(), cmd);
                final Long messageId = (Long) messageMap.get("id");
                currentMsg = fm.loadMessage(messageId);
                doAttachmentDelivery(ureq, cmd, messageMap);
            }
        } else if (source instanceof Link) {
            // all other commands have the message value map id coded into the
            // the command name. get message from this id
            final Link link = (Link) source;
            final String command = link.getCommand();
            final Map<String, Object> messageMap = getMessageMapFromCommand(ureq.getIdentity(), command);
            final Long messageId = (Long) messageMap.get("id");

            final Message updatedMessage = fm.findMessage(messageId);
            if (updatedMessage != null) {
                currentMsg = updatedMessage;
                // now dispatch the commands
                if (command.startsWith("qt_")) {
                    showReplyView(ureq, true, currentMsg);
                } else if (command.startsWith("rp_")) {
                    showReplyView(ureq, false, currentMsg);
                } else if (command.startsWith("dl_")) {
                    showDeleteMessageView(ureq);
                } else if (command.startsWith("ed_")) {
                    showEditMessageView(ureq);
                } else if (command.startsWith("split_")) {
                    showSplitThreadView(ureq);
                } else if (command.startsWith("move_")) {
                    showMoveMessageView(ureq);
                }
            } else if (currentMsg != null) {
                showInfo("header.cannoteditmessage");
                showThreadOverviewView();
            }
        }
    }

    /**
	 */
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (checkForumChangedEventReceived()) {
            return;
        }
        if (source == yesno) {
            if (DialogBoxUIFactory.isYesEvent(event)) { // yes
                doDeleteMessage(ureq);
                if (currentMsg.getThreadtop() == null) {
                    showThreadOverviewView(); // was last message in thread
                } else {
                    showThreadView(ureq, currentMsg.getThreadtop(), null);
                }
            }
        } else if (source == archiveFoDiaCtr) {
            if (DialogBoxUIFactory.isYesEvent(event)) { // ok
                doArchiveForum(ureq);
                showInfo("archive.forum.successfully");
            }
        } else if (source == archiveThDiaCtr) {
            if (DialogBoxUIFactory.isYesEvent(event)) { // ok
                doArchiveThread(ureq, currentMsg);
                showInfo("archive.thread.successfully");
            }
        } else if (source == singleThreadTableCtr) {
            if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
                // user has selected a message title from singleThreadTable
                // -> display message details and below all messages with the same
                // topthread_id as the selected one
                final TableEvent te = (TableEvent) event;
                final String actionid = te.getActionId();
                if (actionid.equals(CMD_SHOWDETAIL)) {
                    final int rowid = te.getRowId();
                    final Message m = (Message) sttdmodel.getObjects().get(rowid);
                    showThreadView(ureq, m, null);
                    ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_MESSAGE_READ, getClass(), LoggingResourceable.wrap(currentMsg));
                }
            }
        } else if (source == allThreadTableCtr) {
            if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
                final TableEvent te = (TableEvent) event;
                final String actionid = te.getActionId();
                final int rowid = te.getRowId();
                final Object[] msgWrapper = (Object[]) attdmodel.getObjects().get(rowid);
                final int size = msgWrapper.length;
                final Message m = (Message) msgWrapper[size - 1];
                if (actionid.equals(CMD_SHOWDETAIL)) {
                    showThreadView(ureq, m, null);
                } else if (CMD_SHOWMARKED.equals(actionid)) {
                    showThreadView(ureq, m, ForumThreadViewModeController.VIEWMODE_MARKED);
                } else if (CMD_SHOWNEW.equals(actionid)) {
                    showThreadView(ureq, m, ForumThreadViewModeController.VIEWMODE_NEW);
                }
            }
        } else if (source == yesNoSplit) {
            // the dialogbox is already removed from the gui stack - do not use getWindowControl().pop(); to remove dialogbox
            if (DialogBoxUIFactory.isYesEvent(event)) {
                splitThread(ureq);
            }
        } else if (source == moveMessageTableCtr) {
            final TableEvent te = (TableEvent) event;
            final Message topMsg = threadList.get(te.getRowId());
            moveMessage(ureq, topMsg);
        }

        // events from messageEditor
        else if (source == msgEditCtr) {
            // persist changed or new message
            if (event == Event.DONE_EVENT) {
                if (msgEditCtr.getLastEditModus().equals(MessageEditController.EDITMODE_NEWTHREAD)) {
                    // creation done -> save
                    doNewThread(ureq);
                    msgEditCtr.persistTempUploadedFiles(currentMsg);
                } else if (msgEditCtr.getLastEditModus().equals(MessageEditController.EDITMODE_EDITMSG)) {
                    // edit done -> save
                    final Message updatedMessage = fm.findMessage(currentMsg.getKey());
                    if (updatedMessage != null) {
                        doEditMessage(ureq);
                        // file persisting is done already, as a msg-key was known during edit.
                    } else {
                        showInfo("header.cannoteditmessage");
                    }
                } else if (msgEditCtr.getLastEditModus().equals(MessageEditController.EDITMODE_REPLYMSG)) {
                    // reply done -> save
                    final Message updatedMessage = fm.findMessage(currentMsg.getKey());
                    if (updatedMessage != null) {
                        doReplyMessage(ureq);
                        msgEditCtr.persistTempUploadedFiles(currentMsg);
                    } else {
                        showInfo("header.cannotsavemessage");
                    }
                }
                // show thread view after all kind of operations
                showThreadView(ureq, currentMsg, null);

                // editor was canceled
            } else if (event == Event.CANCELLED_EVENT) {
                // back to 'list all titles' if canceled on new thread
                if (msgEditCtr.getLastEditModus().equals(MessageEditController.EDITMODE_NEWTHREAD)) {
                    forumPanel.setContent(vcListTitles);
                } else {
                    showThreadView(ureq, currentMsg, null);
                }
            }

        } else if (source == viewSwitchCtr) {
            if (event == Event.CHANGED_EVENT) {
                // viewmode has been switched, so change view:
                final String mode = viewSwitchCtr.getSelectedViewMode();
                showThreadView(ureq, currentMsg, mode);
            }
        } else if (source == filterForUserCtr) {
            if (event instanceof OpenMessageInThreadEvent) {
                final OpenMessageInThreadEvent openEvent = (OpenMessageInThreadEvent) event;
                final Message selectedMsg = openEvent.getMessage();
                showThreadView(ureq, selectedMsg, null);
                scrollToCurrentMessage();
            }
        }
    }

    /**
	 */
    public void event(final Event event) {
        if (event instanceof ForumChangedEvent) {
            forumChangedEventReceived = true;
        }
    }

    // //////////////////////////////////////
    // Application logic, do sth...
    // //////////////////////////////////////

    private void doEditMessage(final UserRequest ureq) {
        // after editing message
        final boolean userIsMsgCreator = ureq.getIdentity().getKey().equals(currentMsg.getCreator().getKey());
        final boolean children = fm.hasChildren(currentMsg);

        if (focallback.mayEditMessageAsModerator() || ((userIsMsgCreator) && (children == false))) {

            currentMsg = msgEditCtr.getMessageBackAfterEdit();
            currentMsg.setModifier(ureq.getIdentity());

            final boolean changeLastModifiedDate = true; // OLAT-6295
            PublishEventTO publishEventTO = forumNotificationTypeHandler.createPublishEventTO(subsContext, forum.getResourceableId(), ureq.getIdentity(), currentMsg,
                    EventType.CHANGED);
            publishEventTO.setSourceEntryId(String.valueOf(currentMsg.getKey()));
            fm.updateMessage(ureq.getIdentity(), currentMsg, changeLastModifiedDate, null, publishEventTO);

            // do logging
            ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_MESSAGE_EDIT, getClass(), LoggingResourceable.wrap(currentMsg));

        } else {
            showWarning("may.not.save.msg.as.author");
            forumPanel.setContent(vcEditMessage);
        }
    }

    private void doReplyMessage(final UserRequest ureq) {
        // after replying to a message
        Message m = fm.createMessage();
        m = msgEditCtr.getMessageBackAfterEdit();

        fm.replyToMessage(ureq.getIdentity(), currentMsg, m,
                forumNotificationTypeHandler.createPublishEventTO(subsContext, forum.getResourceableId(), ureq.getIdentity(), m, EventType.NEW));

        currentMsg = m;
        markRead(m, ureq.getIdentity());

        // do logging
        ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_REPLY_MESSAGE_CREATE, getClass(), LoggingResourceable.wrap(currentMsg));
    }

    private void doNewThread(final UserRequest ureq) {
        // after creating a thread
        Message m = fm.createMessage();
        m = msgEditCtr.getMessageBackAfterEdit();

        if (!focallback.mayOpenNewThread()) {
            throw new OLATSecurityException("not allowed to open new thread in forum " + forum.getKey());
        }
        // open a new thread
        fm.addTopMessage(forum, m, ureq.getIdentity(),
                forumNotificationTypeHandler.createPublishEventTO(subsContext, forum.getResourceableId(), ureq.getIdentity(), m, EventType.NEW));
        // null ?
        // EventType.NEW :
        // EventType.NO_PUBLISH));

        currentMsg = m;
        markRead(m, ureq.getIdentity());

        // do logging
        addLoggingResourceable(LoggingResourceable.wrap(m));
        ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_MESSAGE_CREATE, getClass());
    }

    private void doAttachmentDelivery(final UserRequest ureq, final String cmd, final Map<String, Object> messageMap) {
        // user selected one attachment from the attachment list
        final int pos = Integer.parseInt(cmd.substring(cmd.indexOf("_") + 1, cmd.lastIndexOf("_")));
        // velocity counter starts at 1
        final List<VFSItem> attachments = new ArrayList<VFSItem>();
        attachments.addAll((Collection<VFSItem>) messageMap.get("attachments"));
        final VFSItem vI = attachments.get(pos - 1);
        final VFSLeaf vl = (VFSLeaf) vI;
        ureq.getDispatchResult().setResultingMediaResource(new VFSMediaResource(vl));
    }

    private void doDeleteMessage(final UserRequest ureq) {
        final boolean children = fm.hasChildren(currentMsg);
        final boolean hasParent = currentMsg.getParent() != null;
        final boolean userIsMsgCreator = ureq.getIdentity().getKey().equals(currentMsg.getCreator().getKey());
        if (focallback.mayDeleteMessageAsModerator() || (userIsMsgCreator && children == false)) {
            fm.deleteMessageTree(forum.getKey(), currentMsg);
            showInfo("deleteok");
            // do logging
            if (hasParent) {
                ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_MESSAGE_DELETE, getClass(), LoggingResourceable.wrap(currentMsg));
            } else {
                ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_THREAD_DELETE, getClass(), LoggingResourceable.wrap(currentMsg));
            }
        } else {
            showWarning("may.not.delete.msg.as.author");
        }
    }

    private void doArchiveForum(final UserRequest ureq) {
        final ForumRTFFormatter rtff = new ForumRTFFormatter(getArchiveContainer(ureq), false);
        final ForumArchiveManager fam = ForumArchiveManager.getInstance();
        fam.applyFormatter(rtff, forum.getKey().longValue(), focallback);
    }

    private void doArchiveThread(final UserRequest ureq, final Message currMsg) {
        final Message m = currMsg.getThreadtop();
        final Long topMessageId = (m == null) ? currMsg.getKey() : m.getKey();

        final ForumRTFFormatter rtff = new ForumRTFFormatter(getArchiveContainer(ureq), true);
        final ForumArchiveManager fam = ForumArchiveManager.getInstance();
        fam.applyFormatterForOneThread(rtff, forum.getKey().longValue(), topMessageId.longValue());
    }

    // //////////////////////////////////////
    // Presentation
    // //////////////////////////////////////

    private void showFilterForUserView(final UserRequest ureq) {
        searchMode = true;
        backLinkSearchListTitles = LinkFactory.createCustomLink("backLinkLT", "back", "listalltitles", Link.LINK_BACK, vcFilterView, this);

        removeAsListenerAndDispose(filterForUserCtr);
        filterForUserCtr = new FilterForUserController(ureq, getWindowControl(), forum);
        listenTo(filterForUserCtr);

        vcFilterView.put("filterForUser", filterForUserCtr.getInitialComponent());
        forumPanel.setContent(vcFilterView);
    }

    private void showThreadOverviewView() {
        // user has clicked on button 'list all message titles'
        // -> display allThreadTable
        msgs = fm.getMessagesByForum(forum);
        prepareListTitles(msgs);
        forumPanel.setContent(vcListTitles);
        // do logging
        ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_MESSAGE_LIST, getClass());
    }

    private void showNewThreadView(final UserRequest ureq) {
        // user has clicked on button 'open new thread'.
        final Message m = fm.createMessage();

        removeAsListenerAndDispose(msgEditCtr);
        msgEditCtr = new MessageEditController(ureq, getWindowControl(), focallback, m, null);
        listenTo(msgEditCtr);

        forumPanel.setContent(msgEditCtr.getInitialComponent());
    }

    private void showEditMessageView(final UserRequest ureq) {
        // user has clicked on button 'edit'
        final boolean userIsMsgCreator = ureq.getIdentity().getKey().equals(currentMsg.getCreator().getKey());
        final boolean children = fm.hasChildren(currentMsg);
        if (focallback.mayEditMessageAsModerator() || ((userIsMsgCreator) && (children == false))) {
            // user is forum-moderator -> may edit every message on every level
            // or user is author of the current message and it has still no
            // children
            removeAsListenerAndDispose(msgEditCtr);
            msgEditCtr = new MessageEditController(ureq, getWindowControl(), focallback, currentMsg, null);
            listenTo(msgEditCtr);

            forumPanel.setContent(msgEditCtr.getInitialComponent());
        } else if ((userIsMsgCreator) && (children == true)) {
            // user is author of the current message but it has already at least
            // one child
            showWarning("may.not.save.msg.as.author");
        } else {
            // user isn't author of the current message
            showInfo("may.not.edit.msg");
        }
    }

    private void showDeleteMessageView(final UserRequest ureq) {
        // user has clicked on button 'delete'
        // -> display modal dialog 'Do you really want to delete this message?'
        // 'yes': back to allThreadTable, 'no' back to messageDetails
        final int numOfChildren = countNumOfChildren(currentMsg, threadMsgs);
        final boolean children = fm.hasChildren(currentMsg);
        final boolean userIsMsgCreator = ureq.getIdentity().getKey().equals(currentMsg.getCreator().getKey());
        String currentMsgTitle = StringHelper.escapeHtml(currentMsg.getTitle());

        if (focallback.mayDeleteMessageAsModerator()) {
            // user is forum-moderator -> may delete every message on every level
            if (numOfChildren == 0) {
                yesno = activateYesNoDialog(ureq, null, translate("reallydeleteleaf", currentMsgTitle), yesno);
            } else if (numOfChildren == 1) {
                yesno = activateYesNoDialog(ureq, null, translate("reallydeletenode1", currentMsgTitle), yesno);
            } else {
                yesno = activateYesNoDialog(ureq, null,
                        getTranslator().translate("reallydeletenodeN", new String[] { currentMsgTitle, Integer.toString(numOfChildren) }), yesno);
            }
        } else if ((userIsMsgCreator) && (children == false)) {
            // user may delete his own message if it has no children
            yesno = activateYesNoDialog(ureq, null, translate("reallydeleteleaf", currentMsgTitle), yesno);
        } else if ((userIsMsgCreator) && (children == true)) {
            // user may not delete his own message because it has at least one child
            showWarning("may.not.delete.msg.as.author");
        } else {
            // user isn't author of the current message
            showInfo("may.not.delete.msg");
        }
    }

    private void showReplyView(final UserRequest ureq, final boolean quote, final Message parent) {
        // user has clicked on button 'reply'
        if (focallback.mayReplyMessage()) {

            final Message quotedMessage = fm.createMessage();
            String reString = "";
            if (parent != null && parent.getThreadtop() == null) {
                // add reString only for the first answer
                reString = translate("msg.title.re");
            }
            quotedMessage.setTitle(reString + currentMsg.getTitle());
            if (quote) {
                // load message to form as quotation
                final StringBuilder quoteSB = new StringBuilder();
                quoteSB.append(TINYMCE_EMPTYLINE_CODE);
                quoteSB.append("<div class=\"b_quote_wrapper\"><div class=\"b_quote_author mceNonEditable\">");
                final String date = f.formatDateAndTime(currentMsg.getCreationDate());
                final User creator = currentMsg.getCreator().getUser();
                final String creatorName = getUserService().getFirstAndLastname(creator);
                quoteSB.append(getTranslator().translate("msg.quote.intro", new String[] { date, creatorName }));
                quoteSB.append("</div><blockquote class=\"b_quote\">");
                quoteSB.append(currentMsg.getBody());
                quoteSB.append("</blockquote></div>");
                quoteSB.append(TINYMCE_EMPTYLINE_CODE);
                quotedMessage.setBody(quoteSB.toString());
            }

            removeAsListenerAndDispose(msgEditCtr);
            msgEditCtr = new MessageEditController(ureq, getWindowControl(), focallback, currentMsg, quotedMessage);
            listenTo(msgEditCtr);

            forumPanel.setContent(msgEditCtr.getInitialComponent());
        } else {
            showInfo("may.not.reply.msg");
        }
    }

    private void showSplitThreadView(final UserRequest ureq) {
        if (focallback.mayEditMessageAsModerator()) {
            // user is forum-moderator -> may delete every message on every level
            final int numOfChildren = countNumOfChildren(currentMsg, threadMsgs);

            // provide yesNoSplit as argument, this ensures that dc is disposed before newly created
            yesNoSplit = activateYesNoDialog(ureq, null,
                    getTranslator().translate("reallysplitthread", new String[] { StringHelper.escapeHtml(currentMsg.getTitle()), Integer.toString(numOfChildren) }),
                    yesNoSplit);

            // activateYesNoDialog means that this controller listens to it, and dialog is shown on screen.
            // nothing further to do here!
            return;
        }
    }

    private void showMoveMessageView(final UserRequest ureq) {
        if (focallback.mayEditMessageAsModerator()) {
            // prepare the table data
            msgs = fm.getMessagesByForum(forum);
            threadList = prepareListTitles(msgs);
            final DefaultTableDataModel tdm = new DefaultTableDataModel(threadList) {

                @Override
                public Object getValueAt(final int row, final int col) {
                    final Message m = threadList.get(row);
                    final boolean isSource = m.equalsByPersistableKey(currentMsg.getThreadtop());
                    switch (col) {
                    case 0:
                        final String title = StringHelper.escapeHtml(m.getTitle()).toString();
                        return title;
                    case 1:
                        if (m.getCreator().getStatus().equals(Identity.STATUS_DELETED)) {
                            return m.getCreator().getName();
                        } else {
                            return getUserService().getFirstAndLastname(m.getCreator().getUser());
                        }
                    case 2:
                        final Date mod = m.getLastModified();
                        return mod;
                    case 3:
                        return !isSource;

                    default:
                        return "error";
                    }
                }

                @Override
                public int getColumnCount() {
                    return 4;
                }
            };

            // prepare the table config
            final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
            tableConfig.setCustomCssClass("o_forum");
            tableConfig.setSelectedRowUnselectable(true);
            tableConfig.setDownloadOffered(false);
            tableConfig.setTableEmptyMessage(translate("forum.emtpy"));

            // prepare the table controller
            removeAsListenerAndDispose(moveMessageTableCtr);
            moveMessageTableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
            listenTo(moveMessageTableCtr);

            moveMessageTableCtr.addColumnDescriptor(true, new DefaultColumnDescriptor("table.thread", 0, null, ureq.getLocale()));
            moveMessageTableCtr.addColumnDescriptor(true, new DefaultColumnDescriptor("table.userfriendlyname", 1, null, ureq.getLocale()));
            moveMessageTableCtr.addColumnDescriptor(true, new DefaultColumnDescriptor("table.lastModified", 2, null, ureq.getLocale()));
            moveMessageTableCtr.addColumnDescriptor(true, new BooleanColumnDescriptor("table.choose", 3, "move", translate("table.choose"), translate("table.source")));
            moveMessageTableCtr.setTableDataModel(tdm);

            // push the modal dialog with the table as content
            removeAsListenerAndDispose(cmcMoveMsg);
            cmcMoveMsg = new CloseableModalController(getWindowControl(), "close", moveMessageTableCtr.getInitialComponent());
            listenTo(cmcMoveMsg);

            cmcMoveMsg.activate();
        }
    }

    private void showThreadView(final UserRequest ureq, final Message m, String viewMode) {

        adjustBusinessControlPath(ureq, m);

        // remove old messages from velocity and dispose controllers
        disposeCurrentMessages();
        // now fetch current thread
        final Message threadTopM = m.getThreadtop();
        currentMsg = m; // in some cases already set, but set current message anyway
        threadMsgs = fm.getThread(threadTopM == null ? m.getKey() : threadTopM.getKey());
        precalcMessageDeepness(threadMsgs);
        // for simplicity no reuse of container, always create new one
        vcThreadView = createVelocityContainer("threadview");
        // to access the function renderFileIconCssClass(..) which is accessed in threadview.html using $myself.renderFileIconCssClass
        vcThreadView.contextPut("myself", this);

        backLinkListTitles = LinkFactory.createCustomLink("backLinkLT", "back", "listalltitles", Link.LINK_BACK, vcThreadView, this);
        archiveThreadButton = LinkFactory.createButtonSmall("archive.thread", vcThreadView, this);

        final boolean isClosed = Status.getStatus(m.getStatusCode()).isClosed();
        vcThreadView.contextPut("isClosed", isClosed);
        if (!isClosed) {
            closeThreadButton = LinkFactory.createButtonSmall("close.thread", vcThreadView, this);
        } else {
            openThreadButton = LinkFactory.createButtonSmall("open.thread", vcThreadView, this);
        }
        final boolean isHidden = Status.getStatus(m.getStatusCode()).isHidden();
        vcThreadView.contextPut("isHidden", isHidden);
        if (!isHidden) {
            hideThreadButton = LinkFactory.createButtonSmall("hide.thread", vcThreadView, this);
        } else {
            showThreadButton = LinkFactory.createButtonSmall("show.thread", vcThreadView, this);
        }

        // allow to set thread-viewmode prefs and get actual ones
        viewSwitchCtr = new ForumThreadViewModeController(ureq, getWindowControl(), viewMode);
        listenTo(viewSwitchCtr);
        vcThreadView.put("threadViewSwitch", viewSwitchCtr.getInitialComponent());

        vcThreadView.contextPut("showThreadTable", Boolean.FALSE);
        vcThreadView.contextPut("threadMode", Boolean.FALSE);
        vcThreadView.contextPut("msgDeepMap", msgDeepMap);

        // add all messages that are needed
        currentMessagesMap = new ArrayList<Map<String, Object>>(threadMsgs.size());

        final MarkDAO marking = (MarkDAO) CoreSpringFactory.getBean(MarkDAO.class);
        final List<String> markResSubPath = new ArrayList<String>();
        for (final Message threadMsg : threadMsgs) {
            markResSubPath.add(threadMsg.getKey().toString());
        }
        final List<Mark> markList = marking.getMarks(forumOres, ureq.getIdentity(), markResSubPath);
        final Map<String, Mark> marks = new HashMap<String, Mark>(markList.size() * 2 + 1);
        for (final Mark mark : markList) {
            marks.put(mark.getResSubPath(), mark);
        }
        final List<MarkResourceStat> statList = marking.getStats(forumOres, markResSubPath, ureq.getIdentity());
        final Map<String, MarkResourceStat> stats = new HashMap<String, MarkResourceStat>(statList.size() * 2 + 1);
        for (final MarkResourceStat stat : statList) {
            stats.put(stat.getSubPath(), stat);
        }

        if (viewMode == null) {
            viewMode = viewSwitchCtr.getThreadViewMode(ureq);
        }

        if (ForumThreadViewModeController.VIEWMODE_FLAT.equals(viewMode)) {
            // all messages in flat view
            List<Message> orderedMessages = new ArrayList<Message>();

            orderedMessages.addAll(threadMsgs);
            orderedMessages = threadMsgs;
            Collections.sort(orderedMessages);

            int msgNum = 0;
            final Iterator<Message> iter = orderedMessages.iterator();
            while (iter.hasNext()) {
                final Message msg = iter.next();
                // add message and mark as read
                addMessageToCurrentMessagesAndVC(ureq, msg, vcThreadView, currentMessagesMap, msgNum, marks, stats);
                msgNum++;
            }
            // do logging
            ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_THREAD_READ, getClass(), LoggingResourceable.wrap(currentMsg));
        } else if (ForumThreadViewModeController.VIEWMODE_MESSAGE.equals(viewMode)) {
            // single message in thread view, add message and mark as read
            addMessageToCurrentMessagesAndVC(ureq, m, vcThreadView, currentMessagesMap, 0, marks, stats);
            // init single thread list and append
            sttdmodel = new ForumMessagesTableDataModel(threadMsgs, rms);
            sttdmodel.setLocale(ureq.getLocale());
            singleThreadTableCtr.setTableDataModel(sttdmodel);
            final int position = PersistenceHelper.indexOf(threadMsgs, currentMsg);
            singleThreadTableCtr.setSelectedRowId(position);
            vcThreadView.contextPut("showThreadTable", Boolean.TRUE);
            vcThreadView.put("singleThreadTable", singleThreadTableCtr.getInitialComponent());
            // do logging
            ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_MESSAGE_READ, getClass(), LoggingResourceable.wrap(currentMsg));

        } else if (ForumThreadViewModeController.VIEWMODE_MARKED.equals(viewMode)) {
            // marked messages in flat view
            List<Message> orderedMessages = new ArrayList<Message>();

            orderedMessages.addAll(threadMsgs);
            orderedMessages = threadMsgs;
            Collections.sort(orderedMessages);

            int msgNum = 0;
            for (final Message msg : orderedMessages) {
                // add marked message
                if (marks.containsKey(msg.getKey().toString())) {
                    addMessageToCurrentMessagesAndVC(ureq, msg, vcThreadView, currentMessagesMap, msgNum, marks, stats);
                    msgNum++;
                }
            }
            // do logging
            ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_THREAD_READ, getClass(), LoggingResourceable.wrap(currentMsg));
        } else if (ForumThreadViewModeController.VIEWMODE_NEW.equals(viewMode)) {
            // new messages in flat view
            List<Message> orderedMessages = new ArrayList<Message>();

            orderedMessages.addAll(threadMsgs);
            orderedMessages = threadMsgs;
            Collections.sort(orderedMessages);

            int msgNum = 0;
            for (final Message msg : orderedMessages) {
                // add new message
                if (!rms.contains(msg.getKey())) {
                    addMessageToCurrentMessagesAndVC(ureq, msg, vcThreadView, currentMessagesMap, msgNum, marks, stats);
                    msgNum++;
                }
            }
            // do logging
            ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_THREAD_READ, getClass(), LoggingResourceable.wrap(currentMsg));
        } else {
            // real threaded view with indent
            vcThreadView.contextPut("threadMode", Boolean.TRUE);
            final List<Message> orderedMessages = new ArrayList<Message>();
            orderMessagesThreaded(threadMsgs, orderedMessages, (threadTopM == null ? m : threadTopM));
            // all messages in thread view
            // Iterator iter = threadMsgs.iterator();
            final Iterator<Message> iter = orderedMessages.iterator();

            int msgNum = 0;
            while (iter.hasNext()) {
                final Message msg = iter.next();
                // add message and mark as read
                addMessageToCurrentMessagesAndVC(ureq, msg, vcThreadView, currentMessagesMap, msgNum, marks, stats);
                msgNum++;
            }
            // do logging
            ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_THREAD_READ, getClass(), LoggingResourceable.wrap(m));
        }
        vcThreadView.contextPut("messages", currentMessagesMap);
        // add security callback
        vcThreadView.contextPut("security", focallback);
        vcThreadView.contextPut("mode", viewMode);
        forumPanel.setContent(vcThreadView);
    }

    private void scrollToCurrentMessage() {
        // Scroll to message, but only the first time the view is rendered
        if (currentMsg.getThreadtop() == null || currentMessagesMap.size() == 1) {
            vcThreadView.contextPut("goToMessage", Boolean.FALSE);
        } else {
            vcThreadView.contextPut("goToMessage", new ConsumableBoolean(true));
            vcThreadView.contextPut("goToMessageId", currentMsg.getKey());
        }
    }

    // //////////////////////////////////////
    // Helper Methods / Classes
    // //////////////////////////////////////

    private void precalcMessageDeepness(final List<Message> msgList) {
        msgDeepMap = new HashMap<Long, Integer>();
        for (final Message message : msgList) {
            final int deepness = messageDeepness(message, 0);
            msgDeepMap.put(message.getKey(), deepness);
        }
    }

    private int messageDeepness(final Message msg, final int deep) {
        if (deep > 20) {
            return 20;
        }
        if (msg.getParent() == null) {
            return deep;
        } else {
            final int newDeep = deep + 1;
            return messageDeepness(msg.getParent(), newDeep);
        }
    }

    private void addMessageToCurrentMessagesAndVC(final UserRequest ureq, final Message m, final VelocityContainer vcContainer, final List<Map<String, Object>> allList,
            final int msgCount, final Map<String, Mark> marks, final Map<String, MarkResourceStat> stats) {
        // all values belonging to a message are stored in this map
        // these values can be accessed in velocity. make sure you clean up
        // everything
        // you create here in disposeCurrentMessages()!
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", m.getKey());

        if (rms.contains(m.getKey())) {
            // already read
            map.put("newMessage", Boolean.FALSE);
        } else {
            // mark now as read
            markRead(m, ureq.getIdentity());
            map.put("newMessage", Boolean.TRUE);
        }
        // add some data now
        final Date creationDate = m.getCreationDate();
        final Identity modifier = m.getModifier();
        if (modifier != null) {
            map.put("isModified", Boolean.TRUE);
            map.put("modfname", getUserService().getUserProperty(modifier.getUser(), UserConstants.FIRSTNAME, ureq.getLocale()));
            map.put("modlname", getUserService().getUserProperty(modifier.getUser(), UserConstants.LASTNAME, ureq.getLocale()));
        } else {
            map.put("isModified", Boolean.FALSE);
        }
        map.put("title", m.getTitle());
        map.put("body", m.getBody());
        map.put("date", f.formatDateAndTime(creationDate));
        final Identity creator = m.getCreator();
        map.put("firstname", Formatter.truncate(getUserService().getUserProperty(creator.getUser(), UserConstants.FIRSTNAME, ureq.getLocale()), 18)); // keeps the first
                                                                                                                                                      // 15 chars

        map.put("lastname", Formatter.truncate(getUserService().getUserProperty(creator.getUser(), UserConstants.LASTNAME, ureq.getLocale()), 18));

        // map.put("username", Formatter.truncate(creator.getName(),18));

        map.put("modified", f.formatDateAndTime(m.getLastModified()));
        // message attachments
        final VFSContainer msgContainer = fm.getMessageContainer(forum.getKey(), m.getKey());
        map.put("messageContainer", msgContainer);
        final List<VFSItem> attachments = new ArrayList<VFSItem>(msgContainer.getItems(new VFSItemExcludePrefixFilter(MessageEditController.ATTACHMENT_EXCLUDE_PREFIXES)));
        // List attachments = msgContainer.getItems();
        map.put("attachments", attachments);
        if (attachments == null || attachments.size() == 0) {
            map.put("hasAttachments", Boolean.FALSE);
        } else {
            map.put("hasAttachments", Boolean.TRUE);
        }
        // number of children and modify/delete permissions
        int numOfChildren;
        numOfChildren = countNumOfChildren(m, threadMsgs);
        final Integer nOfCh = new Integer(numOfChildren);
        map.put("nOfCh", nOfCh);
        final boolean userIsMsgCreator = ureq.getIdentity().getKey().equals(creator.getKey());
        final Boolean uIsMsgC = new Boolean(userIsMsgCreator);
        map.put("uIsMsgC", uIsMsgC);
        final boolean isThreadtop = m.getThreadtop() == null;
        map.put("isThreadtop", Boolean.valueOf(isThreadtop));
        boolean isThreadClosed = Status.getStatus(m.getStatusCode()).isClosed();
        if (!isThreadtop) {
            isThreadClosed = Status.getStatus(m.getThreadtop().getStatusCode()).isClosed();
        }
        map.put("isThreadClosed", isThreadClosed);
        if (!isGuestOnly(ureq)) {
            // add portrait to map for later disposal and key for rendering in velocity
            final DisplayPortraitController portrait = new DisplayPortraitController(ureq, getWindowControl(), m.getCreator(), true, true);
            // add also to velocity
            map.put("portrait", portrait);
            final String portraitComponentVCName = m.getKey().toString();
            map.put("portraitComponentVCName", portraitComponentVCName);
            vcContainer.put(portraitComponentVCName, portrait.getInitialComponent());
        }
        allList.add(map);
        /*
         * those Link objects are used! see event method and the instanceof Link part! but reference won't be used!
         */
        LinkFactory.createCustomLink("dl_" + msgCount, "dl_" + msgCount, "msg.delete", Link.BUTTON_SMALL, vcThreadView, this);
        LinkFactory.createCustomLink("ed_" + msgCount, "ed_" + msgCount, "msg.update", Link.BUTTON_SMALL, vcThreadView, this);
        LinkFactory.createCustomLink("rm_" + msgCount, "rm_" + msgCount, "msg.remove", Link.BUTTON_SMALL, vcThreadView, this);
        LinkFactory.createCustomLink("up_" + msgCount, "up_" + msgCount, "msg.upload", Link.BUTTON_SMALL, vcThreadView, this);
        LinkFactory.createCustomLink("qt_" + msgCount, "qt_" + msgCount, "msg.quote", Link.BUTTON_SMALL, vcThreadView, this);
        LinkFactory.createCustomLink("rp_" + msgCount, "rp_" + msgCount, "msg.reply", Link.BUTTON_SMALL, vcThreadView, this);
        LinkFactory.createCustomLink("split_" + msgCount, "split_" + msgCount, "msg.split", Link.BUTTON_SMALL, vcThreadView, this);
        LinkFactory.createCustomLink("move_" + msgCount, "move_" + msgCount, "msg.move", Link.BUTTON_SMALL, vcThreadView, this);

        final String subPath = m.getKey().toString();
        final Mark currentMark = marks.get(subPath);
        final MarkResourceStat stat = stats.get(subPath);

        String businessPath = currentMark == null ? getWindowControl().getBusinessControl().getAsString() + "[Message:" + m.getKey() + "]" : currentMark
                .getBusinessPath();
        final Controller markCtrl = MarkingUIFactory.getMarkController(ureq, getWindowControl(), currentMark, stat, forumOres, subPath, businessPath);
        vcThreadView.put("mark_" + msgCount, markCtrl.getInitialComponent());

        businessPath = BusinessControlFactory.getInstance().getAsString(getWindowControl().getBusinessControl()) + "[Message:" + m.getKey() + "]";
        if (uIsMsgC) {
            final OLATResourceable messageOres = OresHelper.createOLATResourceableInstance("Forum", m.getKey());
            final Controller ePFCollCtrl = EPUIFactory.createArtefactCollectWizzardController(ureq, getWindowControl(), messageOres, businessPath);
            if (ePFCollCtrl != null) {
                final String ePFAddComponentName = "eportfolio_" + msgCount;
                map.put("ePFCollCtrl", ePFCollCtrl);
                map.put("ePFAddComponentName", ePFAddComponentName);
                vcThreadView.put(ePFAddComponentName, ePFCollCtrl.getInitialComponent());
            }
        }
    }

    private boolean isGuestOnly(final UserRequest ureq) {
        return ureq.getUserSession().getRoles().isGuestOnly();
    }

    private void disposeCurrentMessages() {
        if (currentMessagesMap != null) {
            final Iterator<Map<String, Object>> iter = currentMessagesMap.iterator();
            while (iter.hasNext()) {
                final Map<String, Object> messageMap = iter.next();
                // cleanup portrait controllers
                final Controller ctr = (Controller) messageMap.get("portrait");
                if (ctr != null) { // ctr could be null for a guest user
                    ctr.dispose();
                    vcThreadView.remove(ctr.getInitialComponent());
                }
                // cleanup mark controllers

                // cleanup ePortfolio controllers
                final Controller ePCtr = (Controller) messageMap.get("ePFCollCtrl");
                if (ePCtr != null) {
                    ePCtr.dispose();
                }
            }
        }
    }

    private List<Message> prepareListTitles(final List<Message> messages) {
        final List<Message> tmpThreadList = new ArrayList<Message>();
        // extract threads from all messages
        final List<Object[]> threads = new ArrayList<Object[]>();
        final int numTableCols = 8;
        final MarkDAO marking = (MarkDAO) CoreSpringFactory.getBean(MarkDAO.class);
        final List<MarkResourceStat> stats = marking.getStats(forumOres, null, getIdentity());

        final boolean isModerator = focallback.mayEditMessageAsModerator();
        for (final Iterator<Message> iter = messages.iterator(); iter.hasNext();) {
            final Message thread = iter.next();
            if (thread.getParent() == null) {
                // put all data in a generic object array
                final Object[] mesgWrapper = new Object[numTableCols];
                String title = StringHelper.escapeHtml(thread.getTitle()).toString();
                title = Formatter.truncate(title, 50);
                final Status messageStatus = Status.getStatus(thread.getStatusCode());
                final boolean isSticky = messageStatus.isSticky();
                final boolean isClosed = messageStatus.isClosed();
                final boolean isHidden = messageStatus.isHidden();
                if (isHidden && !isModerator) {
                    continue;
                }

                mesgWrapper[0] = "status_thread";
                if (isSticky && isClosed) {
                    mesgWrapper[0] = "status_sticky_closed";
                } else if (isSticky) {
                    mesgWrapper[0] = "status_sticky";
                } else if (isClosed) {
                    mesgWrapper[0] = "status_closed";
                }
                if (isHidden) {
                    title = translate("msg.hidden") + " " + title;
                }
                mesgWrapper[1] = new ForumHelper.MessageWrapper(title, isSticky, collator);
                final User creator = thread.getCreator().getUser();
                mesgWrapper[2] = new ForumHelper.MessageWrapper(getUserService().getFirstAndLastname(creator), isSticky, collator);
                // find latest date, and number of read messages for all children
                // init with thread values
                Date lastModified = thread.getLastModified();
                int readCounter = (rms.contains(thread.getKey()) ? 1 : 0);
                int childCounter = 1;
                int statCounter = 0;
                final String threadSubPath = thread.getKey().toString();
                for (final MarkResourceStat stat : stats) {
                    if (threadSubPath.equals(stat.getSubPath())) {
                        statCounter += stat.getCount();
                    }
                }

                for (final Iterator<Message> iter2 = messages.iterator(); iter2.hasNext();) {
                    final Message msg = iter2.next();
                    if (msg.getThreadtop() != null && msg.getThreadtop().getKey().equals(thread.getKey())) {
                        // a child is found, update values
                        childCounter++;
                        if (rms.contains(msg.getKey())) {
                            readCounter++;
                        }
                        if (msg.getLastModified().after(lastModified)) {
                            lastModified = msg.getLastModified();
                        }

                        final String subPath = msg.getKey().toString();
                        for (final MarkResourceStat stat : stats) {
                            if (subPath.equals(stat.getSubPath())) {
                                statCounter += stat.getCount();
                            }
                        }
                    }
                }
                mesgWrapper[3] = new ForumHelper.MessageWrapper(lastModified, isSticky, collator);
                // lastModified
                mesgWrapper[4] = new ForumHelper.MessageWrapper(new Integer(statCounter), isSticky, collator);
                // marked
                mesgWrapper[5] = new ForumHelper.MessageWrapper(new Integer((childCounter - readCounter)), isSticky, collator);
                // unread
                mesgWrapper[6] = new ForumHelper.MessageWrapper(new Integer(childCounter), isSticky, collator);
                // add message itself for later usage
                mesgWrapper[7] = thread;
                tmpThreadList.add(thread);
                threads.add(mesgWrapper);
            }
        }
        // build table model
        attdmodel = new GenericObjectArrayTableDataModel(threads, numTableCols);
        allThreadTableCtr.setTableDataModel(attdmodel);
        allThreadTableCtr.setSortColumn(3, false);

        vcListTitles.put("allThreadTable", allThreadTableCtr.getInitialComponent());
        vcListTitles.contextPut("hasThreads", (attdmodel.getRowCount() == 0) ? Boolean.FALSE : Boolean.TRUE);

        return tmpThreadList;
    }

    /**
     * @param m
     * @param messages
     * @return number of all children, grandchildren, grand-grandchildren etc. of a certain message
     */
    private int countNumOfChildren(final Message m, final List<Message> messages) {
        int counter = 0;
        counter = countChildrenRecursion(m, messages, counter);
        return counter;
    }

    private int countChildrenRecursion(final Message m, final List<Message> messages, int counter) {
        for (final Iterator<Message> iter = messages.iterator(); iter.hasNext();) {
            final Message element = iter.next();
            if (element.getParent() != null) {
                if (m.getKey().equals(element.getParent().getKey())) {
                    counter = countChildrenRecursion(element, messages, counter);
                    counter++;
                }
            }
        }
        return counter;
    }

    private VFSContainer getArchiveContainer(final UserRequest ureq) {
        VFSContainer container = new OlatRootFolderImpl(FolderConfig.getUserHomes() + File.separator + ureq.getIdentity().getName() + "/private/archive", null);
        // append export timestamp to avoid overwriting previous export
        final Date tmp = new Date(System.currentTimeMillis());
        final java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss");
        final String folder = "forum_" + forum.getKey().toString() + "_" + formatter.format(tmp);
        VFSItem vfsItem = container.resolve(folder);
        if (vfsItem == null || !(vfsItem instanceof VFSContainer)) {
            vfsItem = container.createChildContainer(folder);
        }
        container = (VFSContainer) vfsItem;
        return container;
    }

    private void adjustBusinessControlPath(final UserRequest ureq, final Message m) {
        ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrap(m));
        final OLATResourceable ores = OresHelper.createOLATResourceableInstance(Message.class, m.getKey());
        final ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(ores);

        final WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, getWindowControl());

        // Simple way to "register" the new ContextEntry although only a VelocityPage was flipped.
        Controller dummy = new BasicController(ureq, bwControl) {

            @Override
            protected void event(final UserRequest ureq, final Component source, final Event event) {
                // TODO Auto-generated method stub

            }

            @Override
            protected void doDispose() {
                // TODO Auto-generated method stub

            }

        };
        dummy.dispose();
        dummy = null;
    }

    private Set<Long> getReadSet(final Identity s) {
        // FIXME:fj:c put the whole readset of 1 user / 1 forum in one property
        // only: 234,45646,2343,23432 etc.
        // Problem now is that a lot of rows are generated: number of users x
        // visited messages of all forums = e.g. 5000 x 300 = 1.5 million etc.

        return getForumService().getReadSet(s, forum);
    }

    private ForumService getForumService() {
        return (ForumService) CoreSpringFactory.getBean("forumService");

    }

    private RepositoryService getRepositoryService() {
        return (RepositoryService) CoreSpringFactory.getBean(RepositoryServiceImpl.class);

    }

    private void markRead(final Message m, final Identity s) {
        if (!rms.contains(m.getKey())) {
            rms.add(m.getKey());
            getForumService().markAsRead(s, m);
        }
    }

    /**
     * [used by velocity in vcThreadView.contextPut("myself", this);]
     * 
     * @param filename
     * @return css class that has a background icon for the given filename
     */
    public String renderFileIconCssClass(final String filename) {
        final String filetype = filename.substring(filename.lastIndexOf(".") + 1);
        if (filetype == null) {
            return "b_filetype_file"; // default
        }
        return "b_filetype_" + filetype;
    }

    protected void doDispose() {
        disposeCurrentMessages();
        CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, forum);
    }

    /**
     * Get the message value map from a velocity command. The command must have the signature commandname_messagemapid
     * 
     * @param identity
     * @param command
     * @return Map the value map for the current message
     */
    private Map<String, Object> getMessageMapFromCommand(final Identity identity, final String command) {
        final String cmdId = command.substring(command.lastIndexOf("_") + 1);
        try {
            final Integer id = Integer.valueOf(cmdId);
            return currentMessagesMap.get(id.intValue());
        } catch (final NumberFormatException e) {
            throw new AssertException("Tried to parse forum message id from command::" + command
                    + " but message id was not a long. Could be a user who tries to hack the system. User::" + identity.getName(), e);
        }
    }

    /**
     * Orders the messages in the logical instead of chronological order.
     * 
     * @param messages
     * @param orderedList
     * @param startMessage
     */
    private void orderMessagesThreaded(List<Message> messages, final List<Message> orderedList, final Message startMessage) {
        if (messages == null || orderedList == null || startMessage == null) {
            return;
        }
        final Iterator<Message> iterMsg = messages.iterator();
        while (iterMsg.hasNext()) {
            final Message msg = iterMsg.next();
            if (msg.getParent() == null) {
                orderedList.add(msg);
                final ArrayList<Message> copiedMessages = new ArrayList<Message>();
                copiedMessages.addAll(messages);
                copiedMessages.remove(msg);
                messages = copiedMessages;

                continue;
            }
            if ((msg.getParent() != null) && (msg.getParent().getKey().equals(startMessage.getKey()))) {
                orderedList.add(msg);
                orderMessagesThreaded(messages, orderedList, msg);
            }
        }
    }

    /**
     * Calls splitThread on ForumService and shows the new thread view.
     * 
     * @param ureq
     */
    private void splitThread(final UserRequest ureq) {
        if (focallback.mayEditMessageAsModerator()) {
            final Message newTopMessage = fm.splitThread(ureq.getIdentity(), currentMsg);
            showThreadView(ureq, newTopMessage, null);
            // do logging
            ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_THREAD_SPLIT, getClass(), LoggingResourceable.wrap(currentMsg));

        } else {
            showWarning("may.not.split.thread");
        }
    }

    /**
     * Calls moveMessage on ForumService
     * 
     * @param ureq
     * @param topMessage
     */
    private void moveMessage(final UserRequest ureq, final Message topMsg) {
        if (focallback.mayEditMessageAsModerator()) {
            currentMsg = fm.moveMessage(ureq.getIdentity(), currentMsg, topMsg);
            cmcMoveMsg.deactivate();
            showThreadView(ureq, topMsg, null);
            ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_MESSAGE_MOVE, getClass(), LoggingResourceable.wrap(currentMsg));
        } else {
            showWarning("may.not.move.message");
        }
    }

    /**
     * Sets the closed status to the threadtop message.
     * 
     * @param ureq
     * @param msg
     * @param closed
     */
    private void closeThread(final UserRequest ureq, Message msg, final boolean closed) {
        // if the input message is not the Threadtop get the Threadtop message
        if (msg != null && msg.getThreadtop() != null) {
            msg = msg.getThreadtop();
        }
        if (msg != null && msg.getThreadtop() == null) {
            currentMsg = fm.loadMessage(msg.getKey());
            final Status status = Status.getStatus(currentMsg.getStatusCode());
            status.setClosed(closed);
            if (currentMsg.getParent() == null) {
                currentMsg.setStatusCode(Status.getStatusCode(status));
                final boolean changeLastModifiedDate = !closed; // OLAT-6295
                fm.updateMessage(ureq.getIdentity(), currentMsg, changeLastModifiedDate, new ForumChangedEvent("close"), PublishEventTO.getNoPublishInstance());
            }
            // do logging
            ILoggingAction loggingAction;
            if (closed) {
                loggingAction = ForumLoggingAction.FORUM_THREAD_CLOSE;
            } else {
                loggingAction = ForumLoggingAction.FORUM_THREAD_REOPEN;
            }

            ThreadLocalUserActivityLogger.log(loggingAction, getClass(), LoggingResourceable.wrap(currentMsg));
            showThreadOverviewView();
        }
    }

    /**
     * Sets the hidden status to the threadtop message.
     * 
     * @param ureq
     * @param msg
     * @param hidden
     */
    private void hideThread(final UserRequest ureq, Message msg, final boolean hidden) {
        // if the input message is not the Threadtop get the Threadtop message
        if (msg != null && msg.getThreadtop() != null) {
            msg = msg.getThreadtop();
        }
        if (msg != null && msg.getThreadtop() == null) {
            currentMsg = fm.loadMessage(msg.getKey());
            final Status status = Status.getStatus(currentMsg.getStatusCode());
            status.setHidden(hidden);
            if (currentMsg.getParent() == null) {
                currentMsg.setStatusCode(Status.getStatusCode(status));
                final boolean changeLastModifiedDate = !hidden; // OLAT-6295
                fm.updateMessage(ureq.getIdentity(), currentMsg, changeLastModifiedDate, new ForumChangedEvent("hide"), PublishEventTO.getNoPublishInstance());
            }
            // do logging
            ILoggingAction loggingAction;
            if (hidden) {
                loggingAction = ForumLoggingAction.FORUM_THREAD_HIDE;
            } else {
                loggingAction = ForumLoggingAction.FORUM_THREAD_SHOW;
            }

            ThreadLocalUserActivityLogger.log(loggingAction, getClass(), LoggingResourceable.wrap(currentMsg));
            showThreadOverviewView();
        }
    }

    // //////////////////////////////////////
    // Sticky things
    // //////////////////////////////////////

    /**
     * Description:<br>
     * Tree cell renderer for the sticky thread titles.
     * <P>
     * Initial Date: 09.07.2007 <br>
     * 
     * @author Lavinia Dumitrescu
     */
    class StickyThreadCellRenderer extends CustomCssCellRenderer {

        @Override
        protected String getCssClass(final Object val) {
            final ForumHelper.MessageWrapper messageWrapper = (ForumHelper.MessageWrapper) val;
            if (messageWrapper.isSticky()) {
                return "o_forum_thread_sticky";
            }
            return "";
        }

        @Override
        protected String getCellValue(final Object val) {
            final ForumHelper.MessageWrapper messageWrapper = (ForumHelper.MessageWrapper) val;
            return messageWrapper.toString();
        }

        @Override
        protected String getHoverText(final Object val) {
            return null;
        }
    }

    /**
     * Description:<br>
     * <code>ColumnDescriptor</code> with special <code>compareTo</code> method implementation. Allows a special column sorting for MessageWrappers considering the sticky
     * attribute.
     * <P>
     * Initial Date: 11.07.2007 <br>
     * 
     * @author Lavinia Dumitrescu
     */
    private class StickyColumnDescriptor extends DefaultColumnDescriptor {
        public StickyColumnDescriptor(final String headerKey, final int dataColumn, final String action, final Locale locale) {
            super(headerKey, dataColumn, action, locale, ColumnDescriptor.ALIGNMENT_LEFT);
        }

        /**
         * Sole constructor.
         * 
         * @param headerKey
         * @param dataColumn
         * @param action
         * @param locale
         *            used ONLY for method getRenderValue in case the Object is of type Date to provide locale-sensitive Date formatting
         * @param alignment
         *            left, middle or right; constants in ColumnDescriptor
         */
        public StickyColumnDescriptor(final String headerKey, final int dataColumn, final String action, final Locale locale, final int alignment) {
            super(headerKey, dataColumn, action, locale, alignment);
        }

        /**
         * Delegates comparison to the <code>ForumHelper.compare</code>. In case the <code>ForumHelper.compare</code> returns <code>ForumHelper.NOT_MY_JOB</code>, the
         * comparison is executed by the superclass.
         * 
         */
        @Override
        public int compareTo(final int rowa, final int rowb) {
            final ForumHelper.MessageWrapper a = (ForumHelper.MessageWrapper) getTable().getTableDataModel().getValueAt(rowa, getDataColumn());
            final ForumHelper.MessageWrapper b = (ForumHelper.MessageWrapper) getTable().getTableDataModel().getValueAt(rowb, getDataColumn());
            final boolean sortAscending = getTable().isSortAscending();

            int comparisonOutcome = ForumHelper.compare(a, b, sortAscending);
            if (comparisonOutcome == ForumHelper.NOT_MY_JOB) {
                comparisonOutcome = super.compareTo(rowa, rowb);
            }
            return comparisonOutcome;
        }
    }

    /**
     * Description:<br>
     * <code>ColumnDescriptor</code> with special <code>compareTo</code> method implementation for a <code>CustomCellRenderer</code>. Allows a special column sorting for
     * MessageWrappers considering the sticky attribute.
     * <P>
     * Initial Date: 11.07.2007 <br>
     * 
     * @author Lavinia Dumitrescu
     */
    private class StickyRenderColumnDescriptor extends CustomRenderColumnDescriptor {

        public StickyRenderColumnDescriptor(final String headerKey, final int dataColumn, final String action, final Locale locale, final int alignment,
                final CustomCellRenderer customCellRenderer) {
            super(headerKey, dataColumn, action, locale, alignment, customCellRenderer);
        }

        /**
         * Delegates comparison to the <code>ForumHelper.compare</code>. In case the <code>ForumHelper.compare</code> returns <code>ForumHelper.NOT_MY_JOB</code>, the
         * comparison is executed by the superclass.
         * 
         */
        public int compareTo(final int rowa, final int rowb) {
            final ForumHelper.MessageWrapper a = (ForumHelper.MessageWrapper) getTable().getTableDataModel().getValueAt(rowa, getDataColumn());
            final ForumHelper.MessageWrapper b = (ForumHelper.MessageWrapper) getTable().getTableDataModel().getValueAt(rowb, getDataColumn());
            final boolean sortAscending = getTable().isSortAscending();

            int comparisonOutcome = ForumHelper.compare(a, b, sortAscending);
            if (comparisonOutcome == ForumHelper.NOT_MY_JOB) {
                comparisonOutcome = super.compareTo(rowa, rowb);
            }
            return comparisonOutcome;
        }
    }

    class MessageIconRenderer extends CustomCssCellRenderer {

        protected String getHoverText(final Object val) {
            return ControllerFactory.translateResourceableTypeName((String) val, getLocale());
        }

        protected String getCellValue(final Object val) {
            return "";
        }

        protected String getCssClass(final Object val) {
            // val.toString()
            // use small icon and create icon class for resource: o_FileResource-SHAREDFOLDER_icon
            return "b_small_icon " + "o_forum_" + ((String) val) + "_icon";
        }
    }

    /**
	 */
    @Override
    public boolean isControllerAndNotDisposed() {
        return !isDisposed();
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

    public void setShowHeader(boolean show) {
        showHeader = show;
        vcListTitles.contextPut("showHeader", showHeader);
    }

}
