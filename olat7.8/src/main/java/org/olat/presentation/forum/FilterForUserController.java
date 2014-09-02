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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.presentation.forum;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.filters.VFSItemExcludePrefixFilter;
import org.olat.data.forum.Forum;
import org.olat.data.forum.Message;
import org.olat.data.user.UserConstants;
import org.olat.lms.core.notification.service.SubscriptionContext;
import org.olat.lms.forum.ForumCallback;
import org.olat.lms.forum.ForumService;
import org.olat.lms.forum.Status;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.table.BaseTableDataModelWithoutFilter;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.StaticColumnDescriptor;
import org.olat.presentation.framework.core.components.table.Table;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableDataModel;
import org.olat.presentation.framework.core.components.table.TableEvent;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.creator.ControllerCreator;
import org.olat.presentation.framework.core.control.generic.ajax.autocompletion.AutoCompleterController;
import org.olat.presentation.framework.core.control.generic.ajax.autocompletion.EntriesChosenEvent;
import org.olat.presentation.framework.core.control.generic.ajax.autocompletion.ListProvider;
import org.olat.presentation.framework.core.control.generic.ajax.autocompletion.ListReceiver;
import org.olat.presentation.framework.core.control.generic.popup.PopupBrowserWindow;
import org.olat.presentation.framework.core.util.CSSHelper;
import org.olat.presentation.framework.layout.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.presentation.user.DisplayPortraitController;
import org.olat.presentation.user.UserInfoMainController;
import org.olat.system.commons.Formatter;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * This controller is implementing a search by user. There is two ways to search: a text field with autocomplete to select directly the user and the list of users which
 * have posted some messages in the forum. <BR>
 * Events:
 * <ul>
 * <li>OpenMessageInThreadEvent</li>
 * <ul>
 * <P>
 * Initial Date: 18 sept. 2009 <br>
 * 
 * @author srosse
 */
public class FilterForUserController extends BasicController {

    private static final String CMD_SHOW = "show";
    private static final String CMD_HOMEPAGE = "homepage";

    private final Forum forum;
    private List<Message> msgs;
    private List<Message> threadMsgs;
    private List<Map<String, Object>> currentMessagesMap;
    private UsersTableModel usersTableModel;
    private final UserListProvider userListProvider;
    private boolean forumChangedEventReceived = false;

    private final VelocityContainer mainVC;
    private VelocityContainer vcThreadView;
    private final TableController userListCtr;
    private final AutoCompleterController userAutoCompleterCtr;
    private final DateFormat dateFormat;
    private final Panel searchPanel;

    public FilterForUserController(final UserRequest ureq, final WindowControl wControl, final Forum forum) {
        super(ureq, wControl);
        this.forum = forum;

        msgs = getForumService().getMessagesByForum(forum);

        mainVC = createVelocityContainer("filter_for_user");

        final List<UserInfo> userInfoList = getUserInfoList();
        userListProvider = new UserListProvider(userInfoList);

        final boolean ajax = wControl.getWindowBackOffice().getWindowManager().getGlobalSettings().getAjaxFlags().isIframePostEnabled();
        mainVC.contextPut("ajax", new Boolean(ajax));

        // show key in result list, users that can see this filter have administrative rights
        userAutoCompleterCtr = new AutoCompleterController(ureq, wControl, userListProvider, null, true, 60, 3, null);
        listenTo(userAutoCompleterCtr);

        dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, ureq.getLocale());

        final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
        tableConfig.setColumnMovingOffered(true);
        tableConfig.setSortingEnabled(true);

        userListCtr = new TableController(tableConfig, ureq, wControl, getTranslator());
        final DefaultColumnDescriptor lastNameDesc = new DefaultColumnDescriptor("table.user.lastname", 0, CMD_HOMEPAGE, ureq.getLocale());
        lastNameDesc.setIsPopUpWindowAction(true, "height=600, width=900, location=no, menubar=no, resizable=yes, status=no, scrollbars=yes, toolbar=no");
        userListCtr.addColumnDescriptor(lastNameDesc);
        final DefaultColumnDescriptor firstNameDesc = new DefaultColumnDescriptor("table.user.firstname", 1, CMD_HOMEPAGE, ureq.getLocale());
        firstNameDesc.setIsPopUpWindowAction(true, "height=600, width=900, location=no, menubar=no, resizable=yes, status=no, scrollbars=yes, toolbar=no");
        userListCtr.addColumnDescriptor(firstNameDesc);
        userListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.user.replies", 2, null, ureq.getLocale()));
        userListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.user.threads", 3, null, ureq.getLocale()));
        userListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.lastModified", 4, null, ureq.getLocale()));
        userListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.numOfCharacters", 5, null, ureq.getLocale()));
        userListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.numOfWords", 6, null, ureq.getLocale()));
        userListCtr.addColumnDescriptor(new StaticColumnDescriptor(CMD_SHOW, "viewswitch.title", getTranslator().translate("viewswitch.title")));

        usersTableModel = new UsersTableModel(userInfoList);
        userListCtr.setTableDataModel(usersTableModel);
        listenTo(userListCtr);

        mainVC.put("userAutoCompleter", userAutoCompleterCtr.getInitialComponent());
        mainVC.put("userList", userListCtr.getInitialComponent());

        // results
        vcThreadView = createVelocityContainer("threadview");

        searchPanel = putInitialPanel(mainVC);
    }

    private ForumService getForumService() {
        return (ForumService) CoreSpringFactory.getBean("forumService");

    }

    /**
     * Is the controller displaying the results
     * 
     * @return
     */
    public boolean isShowResults() {
        return searchPanel.getContent() == vcThreadView;
    }

    /**
     * set the controller to its initial status, the search form
     */
    public void setShowSearch() {
        searchPanel.setContent(mainVC);
    }

    public void forumChanged() {
        forumChangedEventReceived = true;
    }

    private boolean checkForumChangedEventReceived() {
        if (forumChangedEventReceived) {
            loadMessages();
            forumChangedEventReceived = false;
            return true;
        }
        return false;
    }

    private void loadMessages() {

        msgs = getForumService().getMessagesByForum(forum);

        final List<UserInfo> userInfoList = getUserInfoList();
        userListProvider.setUserInfos(userInfoList);
        usersTableModel = new UsersTableModel(userInfoList);
        userListCtr.setTableDataModel(usersTableModel);
    }

    protected List<UserInfo> getUserInfoList() {
        final Map<Identity, UserInfo> infoMap = new HashMap<Identity, UserInfo>();

        for (final Message msg : msgs) {
            final Identity creator = msg.getCreator();
            if (creator == null) {
                continue;
            }
            UserInfo stats = infoMap.get(creator);
            if (stats == null) {
                stats = new UserInfo(creator);
                stats.setLastModified(msg.getLastModified());
                infoMap.put(creator, stats);
            } else {

                final Date lastModified = msg.getLastModified();
                if (stats.getLastModified().compareTo(lastModified) > 0) {
                    stats.setLastModified(lastModified);
                }
            }

            if (msg.getParent() == null) {
                stats.incThreads();
            } else {
                stats.incReplies();
            }
            stats.addNumOfCharacters(msg.getNumOfCharacters());
            stats.addNumOfWords(msg.getNumOfWords());
        }

        final List<UserInfo> infoList = new ArrayList<UserInfo>(infoMap.values());
        Collections.sort(infoList);
        return infoList;
    }

    @Override
    protected void doDispose() {
        //
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        checkForumChangedEventReceived();

        if (source instanceof Link) {
            // all other commands have the message value map id coded into the
            // the command name. get message from this id
            final Link link = (Link) source;
            final String command = link.getCommand();
            final Map<String, Object> messageMap = getMessageMapFromCommand(ureq.getIdentity(), command);
            final Long messageId = (Long) messageMap.get("id");

            final Message selectedMessage = getForumService().findMessage(messageId);
            if (selectedMessage != null) {
                if (command.startsWith("open_in_thread_")) {
                    fireEvent(ureq, new OpenMessageInThreadEvent(selectedMessage));
                }
            }
        }
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

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        checkForumChangedEventReceived();

        if (source == userListCtr) {
            if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
                final TableEvent te = (TableEvent) event;

                final int rowid = te.getRowId();
                final UserInfo selectedInfo = usersTableModel.getUserInfo(rowid);
                if (te.getActionId().equals(CMD_SHOW)) {
                    showMessages(ureq, selectedInfo);
                } else if (te.getActionId().equals(CMD_HOMEPAGE)) {
                    final ControllerCreator ctrlCreator = new ControllerCreator() {
                        @Override
                        public Controller createController(final UserRequest lureq, final WindowControl lwControl) {
                            return new UserInfoMainController(lureq, lwControl, selectedInfo.getIdentity());
                        }
                    };
                    // wrap the content controller into a full header layout
                    final ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
                    // open in new browser window
                    final PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, layoutCtrlr);
                    pbw.open(ureq);
                }
            }
        } else if (source == userAutoCompleterCtr) {
            if (event instanceof EntriesChosenEvent) {
                final List<String> selectedUsernames = ((EntriesChosenEvent) event).getEntries();
                if (selectedUsernames.size() == 1) {
                    final String username = selectedUsernames.get(0);
                    final UserInfo selectedInfo = usersTableModel.getUserInfo(username);
                    if (selectedInfo != null) {
                        showMessages(ureq, selectedInfo);
                    }
                }
            }
        }
    }

    protected void showMessages(final UserRequest ureq, final UserInfo selectedInfo) {
        // for simplicity no reuse of container, always create new one
        vcThreadView = createVelocityContainer("threadview");
        // to access the function renderFileIconCssClass(..) which is accessed in threadview.html using $myself.renderFileIconCssClass
        vcThreadView.contextPut("myself", this);
        vcThreadView.contextPut("filteredForFirstName", selectedInfo.getFirstName());
        vcThreadView.contextPut("filteredForLastName", selectedInfo.getLastName());

        vcThreadView.contextPut("isClosed", Boolean.FALSE);
        vcThreadView.contextPut("isHidden", Boolean.FALSE);

        vcThreadView.contextPut("showThreadTable", Boolean.FALSE);
        vcThreadView.contextPut("threadMode", Boolean.FALSE);
        vcThreadView.contextPut("msgDeepMap", null);

        threadMsgs = getMessages(selectedInfo.getIdentity());

        // add all messages that are needed
        currentMessagesMap = new ArrayList<Map<String, Object>>(threadMsgs.size());

        // all messages in flat view
        List<Message> orderedMessages = new ArrayList<Message>();

        orderedMessages.addAll(threadMsgs);
        orderedMessages = threadMsgs;
        Collections.sort(orderedMessages, new MessageComparatorByDate());

        int msgNum = 0;
        for (final Message msg : orderedMessages) {
            addMessageToCurrentMessagesAndVC(ureq, msg, vcThreadView, currentMessagesMap, msgNum++);
        }

        vcThreadView.contextPut("messages", currentMessagesMap);
        // add security callback
        vcThreadView.contextPut("security", new SearchForumCallback());
        searchPanel.setContent(vcThreadView);
    }

    // TODO this method is very similar to the same in ForumController
    private void addMessageToCurrentMessagesAndVC(final UserRequest ureq, final Message m, final VelocityContainer vcContainer, final List<Map<String, Object>> allList,
            final int msgCount) {
        // all values belonging to a message are stored in this map
        // these values can be accessed in velocity. make sure you clean up
        // everything
        // you create here in disposeCurrentMessages()!
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", m.getKey());

        map.put("newMessage", Boolean.FALSE);

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
        map.put("date", dateFormat.format(creationDate));
        final Identity creator = m.getCreator();
        map.put("firstname", Formatter.truncate(getUserService().getUserProperty(creator.getUser(), UserConstants.FIRSTNAME, ureq.getLocale()), 18)); // keeps the first
                                                                                                                                                      // 15 chars
        map.put("lastname", Formatter.truncate(getUserService().getUserProperty(creator.getUser(), UserConstants.LASTNAME, ureq.getLocale()), 18));

        // map.put("username", Formatter.truncate(creator.getName(),18));

        map.put("modified", dateFormat.format(m.getLastModified()));
        // message attachments
        final ForumService fm = getForumService();
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
        map.put("nOfCh", new Integer(1));
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
        // add portrait to map for later disposal and key for rendering in velocity
        final DisplayPortraitController portrait = new DisplayPortraitController(ureq, getWindowControl(), m.getCreator(), true, true);
        // add also to velocity
        map.put("portrait", portrait);
        final String portraitComponentVCName = m.getKey().toString();
        map.put("portraitComponentVCName", portraitComponentVCName);
        vcContainer.put(portraitComponentVCName, portrait.getInitialComponent());
        allList.add(map);

        LinkFactory.createCustomLink("open_in_thread_" + msgCount, "open_in_thread_" + msgCount, "msg.open_in_thread", Link.BUTTON_SMALL, vcThreadView, this);
    }

    private List<Message> getMessages(final Identity identity) {
        final List<Message> usersMessages = new ArrayList<Message>();
        for (final Message msg : msgs) {
            if (identity.equals(msg.getCreator())) {
                usersMessages.add(msg);
            }
        }
        return usersMessages;
    }

    /**
     * Provider for the autocomplete
     */
    public class UserListProvider implements ListProvider {
        private List<UserInfo> stats;

        public UserListProvider(final List<UserInfo> stats) {
            this.stats = stats;
        }

        public List<UserInfo> getUserInfos() {
            return stats;
        }

        public void setUserInfos(final List<UserInfo> stats) {
            this.stats = stats;
        }

        @Override
        public void getResult(String searchValue, final ListReceiver receiver) {
            searchValue = searchValue == null ? null : searchValue.toLowerCase();
            for (final UserInfo info : stats) {
                final Identity identity = info.getIdentity();
                final String name = identity.getName();

                if (identity.getName().startsWith(searchValue)) {
                    receiver.addEntry(name, name, info.getLastName() + " " + info.getFirstName(), CSSHelper.CSS_CLASS_USER);
                } else if (info.getFirstNameLowerCase().startsWith(searchValue)) {
                    receiver.addEntry(name, name, info.getLastName() + " " + info.getFirstName(), CSSHelper.CSS_CLASS_USER);
                } else if (info.getLastNameLowerCase().startsWith(searchValue)) {
                    receiver.addEntry(name, name, info.getLastName() + " " + info.getFirstName(), CSSHelper.CSS_CLASS_USER);
                }
            }
        }
    }

    /**
     * Read-only security callback
     */
    public class SearchForumCallback implements ForumCallback {

        @Override
        public SubscriptionContext getSubscriptionContext() {
            return null;
        }

        @Override
        public boolean mayArchiveForum() {
            return false;
        }

        @Override
        public boolean mayDeleteMessageAsModerator() {
            return false;
        }

        @Override
        public boolean mayEditMessageAsModerator() {
            return false;
        }

        @Override
        public boolean mayFilterForUser() {
            return true;
        }

        @Override
        public boolean mayOpenNewThread() {
            return false;
        }

        @Override
        public boolean mayReplyMessage() {
            return false;
        }
    }

    /**
     * TableDataModel for the overview of all users in the forum
     */
    public class UsersTableModel extends BaseTableDataModelWithoutFilter implements TableDataModel {
        private final List<UserInfo> infos;

        public UsersTableModel(final List<UserInfo> infos) {
            this.infos = infos;
        }

        public UserInfo getUserInfo(final String username) {
            for (final UserInfo info : infos) {
                final Identity id = info.getIdentity();
                if (username.equals(id.getName())) {
                    return info;
                }
            }
            return null;
        }

        public UserInfo getUserInfoByKey(final Long key) {
            for (final UserInfo info : infos) {
                final Identity id = info.getIdentity();
                if (key.equals(id.getKey())) {
                    return info;
                }
            }
            return null;
        }

        public UserInfo getUserInfo(final int rowid) {
            if (rowid >= 0 && rowid < infos.size()) {
                return infos.get(rowid);
            }
            return null;
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public int getRowCount() {
            return infos.size();
        }

        @Override
        public Object getValueAt(final int row, final int col) {
            final UserInfo userStats = infos.get(row);
            switch (col) {
            case 0:
                return userStats.getLastName();
            case 1:
                return userStats.getFirstName();
            case 2:
                return Integer.toString(userStats.getReplies());
            case 3:
                return Integer.toString(userStats.getThreads());
            case 4: {
                if (userStats.getLastModified() == null) {
                    return "";
                }
                return dateFormat.format(userStats.getLastModified());
            }
            case 5:
                return userStats.getNumOfCharacters();
            case 6:
                return userStats.getNumOfWords();
            case 7:
                return userStats;
            default:
                return "";
            }
        }

    }

    /**
     * Wrapper for all the statistics needed by the overview of all users and the autocomplete
     */
    public class UserInfo implements Comparable<UserInfo> {
        private int replies = 0;
        private int threads = 0;
        private int numOfCharacters = 0;
        private int numOfWords = 0;
        private Date lastModified;
        private final Identity identity;

        private final String firstName;
        private final String lastName;
        private final String firstNameLowerCase;
        private final String lastNameLowerCase;

        public UserInfo(final Identity identity) {
            this.identity = identity;

            firstName = getUserService().getUserProperty(identity.getUser(), UserConstants.FIRSTNAME, getLocale());
            lastName = getUserService().getUserProperty(identity.getUser(), UserConstants.LASTNAME, getLocale());

            firstNameLowerCase = firstName.toLowerCase();
            lastNameLowerCase = lastName.toLowerCase();
        }

        public Identity getIdentity() {
            return identity;
        }

        public int getReplies() {
            return replies;
        }

        public int getThreads() {
            return threads;
        }

        public int getNumOfCharacters() {
            return numOfCharacters;
        }

        public int getNumOfWords() {
            return numOfWords;
        }

        public Date getLastModified() {
            return lastModified;
        }

        public void setLastModified(final Date lastModified) {
            this.lastModified = lastModified;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getFirstNameLowerCase() {
            return firstNameLowerCase;
        }

        public String getLastNameLowerCase() {
            return lastNameLowerCase;
        }

        public void incReplies() {
            replies++;
        }

        public void incThreads() {
            threads++;
        }

        public void addNumOfCharacters(final Integer characters) {
            if (characters != null) {
                numOfCharacters += characters.intValue();
            }
        }

        public void addNumOfWords(final Integer words) {
            if (words != null) {
                numOfWords += words.intValue();
            }
        }

        @Override
        public int compareTo(final UserInfo o) {
            if (o == null) {
                return -1;
            }
            final String l1 = getLastNameLowerCase();
            final String l2 = o.getLastNameLowerCase();
            if (l1 == null) {
                return -1;
            }
            if (l2 == null) {
                return 1;
            }
            return l1.compareTo(l2);
        }
    }

    /**
     * Sort the message by date, ascending
     */
    private class MessageComparatorByDate implements Comparator<Message> {
        @Override
        public int compare(final Message o1, final Message o2) {
            final Date d1 = o1.getLastModified();
            final Date d2 = o2.getLastModified();

            if (d1 == null) {
                return -1;
            } else if (d2 == null) {
                return 1;
            }
            return d1.compareTo(d2);
        }
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
