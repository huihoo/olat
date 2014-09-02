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
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.presentation.dialogelements;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.vfs.Quota;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.filters.VFSLeafFilter;
import org.olat.data.forum.Forum;
import org.olat.lms.activitylogging.CourseLoggingAction;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.commons.mediaresource.VFSMediaResource;
import org.olat.lms.core.notification.impl.NotificationSubscriptionContextFactory;
import org.olat.lms.core.notification.service.PublishEventTO;
import org.olat.lms.core.notification.service.PublishEventTO.EventType;
import org.olat.lms.core.notification.service.PublisherData;
import org.olat.lms.core.notification.service.SubscriptionContext;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.CourseModule;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.DialogCourseNode;
import org.olat.lms.course.nodes.DialogNodeForumCallback;
import org.olat.lms.course.properties.CoursePropertyManager;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.dialogelements.DialogElement;
import org.olat.lms.dialogelements.DialogElementsPropertyManager;
import org.olat.lms.dialogelements.DialogPropertyElements;
import org.olat.lms.forum.ForumService;
import org.olat.presentation.course.nodes.dialog.DialogConfigForm;
import org.olat.presentation.filebrowser.FileUploadController;
import org.olat.presentation.filebrowser.FolderEvent;
import org.olat.presentation.forum.ForumUIFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableEvent;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.presentation.framework.core.control.generic.popup.PopupBrowserWindow;
import org.olat.presentation.framework.core.control.generic.title.TitleInfo;
import org.olat.presentation.notification.ContextualSubscriptionController;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.event.Event;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * TODO: guido Class Description for DialogController
 * <P>
 * Initial Date: 03.11.2005 <br>
 * 
 * @author guido
 */
public class DialogElementsController extends BasicController {

    protected static final String ACTION_START_FORUM = "startforum";
    protected static final String ACTION_SHOW_FILE = "showfile";
    protected static final String ACTION_DELETE_ELEMENT = "delete";
    private static final int TABLE_RESULTS_PER_PAGE = 5;

    private final DialogElementsPropertyManager dialogElmsMgr;
    private final VelocityContainer content;
    private TableController tableCtr;
    private final CourseNode courseNode;
    private FileUploadController fileUplCtr;
    private final Panel dialogPanel;
    private final ForumService forumService;
    private DialogElement recentDialogElement, selectedElement;
    private DialogElementsTableModel tableModel;
    private DialogBoxController confirmDeletionCtr;
    private ContextualSubscriptionController csCtr;
    private DialogNodeForumCallback forumCallback;
    private SubscriptionContext subsContext;
    private final CoursePropertyManager coursePropMgr;
    private final boolean isOlatAdmin;
    private final boolean isGuestOnly;
    private final NodeEvaluation nodeEvaluation;
    private final UserCourseEnvironment userCourseEnv;
    private final TableGuiConfiguration tableConf;
    private final Link uploadButton;
    private Controller forumCtr;
    private DialogElementsNotificationTypeHandler dialogElementsNotificationTypeHandler;

    public DialogElementsController(final UserRequest ureq, final WindowControl wControl, final CourseNode courseNode, final UserCourseEnvironment userCourseEnv,
            final NodeEvaluation nodeEvaluation) {
        super(ureq, wControl);
        this.nodeEvaluation = nodeEvaluation;
        this.userCourseEnv = userCourseEnv;
        this.coursePropMgr = userCourseEnv.getCourseEnvironment().getCoursePropertyManager();
        this.courseNode = courseNode;
        forumService = getForumService();
        dialogElmsMgr = DialogElementsPropertyManager.getInstance();
        dialogElementsNotificationTypeHandler = CoreSpringFactory.getBean(DialogElementsNotificationTypeHandler.class);

        content = createVelocityContainer("dialog");
        uploadButton = LinkFactory.createButtonSmall("dialog.upload.file", content, this);
        // uploadButton.setAlt("upload");
        isOlatAdmin = ureq.getUserSession().getRoles().isOLATAdmin();
        isGuestOnly = ureq.getUserSession().getRoles().isGuestOnly();

        forumCallback = new DialogNodeForumCallback(nodeEvaluation, isOlatAdmin, isGuestOnly, subsContext);
        content.contextPut("security", forumCallback);

        if (isGuestOnly) {
            // guests cannot subscribe (OLAT-2019)
            subsContext = null;
        } else {
            subsContext = CourseModule.createSubscriptionContext(userCourseEnv.getCourseEnvironment(), courseNode);
        }

        // if sc is null, then no subscription is desired
        if (subsContext != null) {
            // new: use as PublisherData.data the subsContext.getSubidentifier(), used to be "0"
            final PublisherData pdata = new PublisherData(OresHelper.calculateTypeName(DialogElement.class), String.valueOf(getNotificationSubscriptionContextFactory()
                    .getSubContextIdFrom(subsContext.getSubidentifier())));
            csCtr = new ContextualSubscriptionController(ureq, getWindowControl(), subsContext, pdata);
            listenTo(csCtr);
            content.put("subscription", csCtr.getInitialComponent());
        }
        // configure and display table
        tableConf = new TableGuiConfiguration();
        tableConf.setResultsPerPage(TABLE_RESULTS_PER_PAGE);
        tableConf.setPreferencesOffered(true, "FileDialogElementsTable");
        tableConf.setDownloadOffered(true);
        dialogPanel = putInitialPanel(content);
        showOverviewTable(ureq, forumCallback);
    }

    private ForumService getForumService() {
        return CoreSpringFactory.getBean(ForumService.class);

    }

    private NotificationSubscriptionContextFactory getNotificationSubscriptionContextFactory() {
        return CoreSpringFactory.getBean(NotificationSubscriptionContextFactory.class);
    }

    private void showOverviewTable(final UserRequest ureq, final DialogNodeForumCallback callback) {
        removeAsListenerAndDispose(tableCtr);
        tableCtr = new TableController(tableConf, ureq, getWindowControl(), getTranslator());
        final DialogPropertyElements elements = dialogElmsMgr.findDialogElements(coursePropMgr, courseNode);
        List list = new ArrayList();
        tableModel = new DialogElementsTableModel(getTranslator(), callback, courseNode.getModuleConfiguration());
        if (elements != null) {
            list = elements.getDialogPropertyElements();
        }
        for (final Iterator iter = list.iterator(); iter.hasNext();) {
            final DialogElement element = (DialogElement) iter.next();
            final Integer msgCount = forumService.countMessagesByForumID(element.getForumKey());
            element.setMessagesCount(msgCount);
            element.setNewMessages(new Integer(msgCount.intValue() - forumService.countReadMessagesByUserAndForum(ureq.getIdentity(), element.getForumKey())));
        }
        tableModel.setEntries(list);
        tableModel.addColumnDescriptors(tableCtr);
        tableCtr.setTableDataModel(tableModel);
        tableCtr.modelChanged();
        tableCtr.setSortColumn(3, false);
        listenTo(tableCtr);
        content.put("dialogElementsTable", tableCtr.getInitialComponent());
        dialogPanel.setContent(content);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        DialogElement entry = null;
        // process table events
        if (source == tableCtr) {
            final TableEvent te = (TableEvent) event;
            final String command = te.getActionId();
            final int row = te.getRowId();
            entry = tableModel.getEntryAt(row);
            if (command.equals(ACTION_START_FORUM)) {
                selectedElement = dialogElmsMgr.findDialogElement(coursePropMgr, courseNode, entry.getForumKey());
                if (selectedElement == null) {
                    showInfo("element.already.deleted");
                    return;
                }
                Forum forum = null;
                forum = forumService.loadForum(entry.getForumKey());
                content.contextPut("hasSelectedElement", Boolean.TRUE);
                content.contextPut("selectedElement", selectedElement);

                // display forum either inline or as popup
                final String integration = (String) courseNode.getModuleConfiguration().get(DialogConfigForm.DIALOG_CONFIG_INTEGRATION);

                subsContext = CourseModule.createSubscriptionContext(userCourseEnv.getCourseEnvironment(), courseNode, forum.getKey().toString());
                forumCallback = new DialogNodeForumCallback(nodeEvaluation, isOlatAdmin, isGuestOnly, subsContext);
                content.contextPut("security", forumCallback);

                if (integration.equals(DialogConfigForm.CONFIG_INTEGRATION_VALUE_INLINE)) {
                    removeAsListenerAndDispose(forumCtr);
                    forumCtr = ForumUIFactory.getStandardForumControllerWithoutHeader(ureq, getWindowControl(), forum, forumCallback);
                    listenTo(forumCtr);
                    content.contextPut("hasInlineForum", Boolean.TRUE);
                    content.put("forum", forumCtr.getInitialComponent());
                } else {
                    content.contextPut("hasInlineForum", Boolean.FALSE);
                    final TitleInfo titleInfo = new TitleInfo(translate("dialog.selected.element"), selectedElement.getFilename());
                    final PopupBrowserWindow pbw = ForumUIFactory.getPopupableForumController(ureq, getWindowControl(), forum, forumCallback, titleInfo);
                    pbw.open(ureq);
                }

            } else if (command.equals(ACTION_SHOW_FILE)) {
                doFileDelivery(ureq, entry.getForumKey());
            } else if (command.equals(ACTION_DELETE_ELEMENT)) {
                selectedElement = entry;
                String text = translate("element.delete", StringHelper.escapeHtml(entry.getFilename()));
                confirmDeletionCtr = activateYesNoDialog(ureq, null, text, confirmDeletionCtr);
                return;
            }
            // process file upload events
        } else if (source == fileUplCtr) {
            // event.
            if (event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT) {
                // reset recent element
                recentDialogElement = null;
                showOverviewTable(ureq, forumCallback);
            } else if (event.getCommand().equals(FolderEvent.UPLOAD_EVENT)) {
                String filename = null;
                try {
                    // get size of file
                    final VFSContainer forumContainer = forumService.getForumContainer(recentDialogElement.getForumKey());
                    final VFSLeaf vl = (VFSLeaf) forumContainer.getItems().get(0);
                    final String fileSize = StringHelper.formatMemory(vl.getSize());

                    // new dialog element
                    filename = ((FolderEvent) event).getFilename();
                    final DialogElement element = new DialogElement();
                    element.setAuthor(recentDialogElement.getAuthor());
                    element.setDate(new Date());
                    element.setFilename(filename);
                    element.setForumKey(recentDialogElement.getForumKey());
                    element.setFileSize(fileSize);

                    // do logging
                    // ThreadLocalUserActivityLogger.log(CourseLoggingAction.DIALOG_ELEMENT_FILE_UPLOADED, getClass(), LoggingResourceable.wrapUploadFile(filename));

                    // everything went well so save the property
                    PublishEventTO publishEventTO = dialogElementsNotificationTypeHandler.createPublishEventTO(subsContext, getNotificationSubscriptionContextFactory()
                            .getSubContextIdFrom(subsContext.getSubidentifier()), ureq.getIdentity(), element, EventType.NEW);
                    dialogElmsMgr.addDialogElementAndNotify(coursePropMgr, courseNode, element, publishEventTO);
                } catch (final Exception e) {
                    //
                    throw new OLATRuntimeException(DialogElementsController.class, "Error while adding new 'file discussion' element with filename: " + filename, e);
                }
            }
        } else if (source == confirmDeletionCtr) {
            if (DialogBoxUIFactory.isYesEvent(event)) {
                final DialogCourseNode node = (DialogCourseNode) courseNode;
                // archive data to personal folder
                node.doArchiveElement(selectedElement, CourseFactory.getOrCreateDataExportDirectory(ureq.getIdentity(), node.getShortTitle()));
                // delete element
                dialogElmsMgr.deleteDialogElement(coursePropMgr, courseNode, selectedElement.getForumKey());
                forumService.deleteForum(selectedElement.getForumKey());
                showOverviewTable(ureq, forumCallback);
                content.contextPut("hasSelectedElement", Boolean.FALSE);
                // do logging
                ThreadLocalUserActivityLogger.log(CourseLoggingAction.DIALOG_ELEMENT_FILE_DELETED, getClass(),
                        LoggingResourceable.wrapUploadFile(selectedElement.getFilename()));
            }
        }
    }

    /**
     * deliver the selected file and show in a popup
     * 
     * @param ureq
     * @param command
     */
    private void doFileDelivery(final UserRequest ureq, final Long forumKey) {
        final VFSContainer forumContainer = forumService.getForumContainer(forumKey);
        final VFSLeaf vl = (VFSLeaf) forumContainer.getItems(new VFSLeafFilter()).get(0);

        // ureq.getDispatchResult().setResultingMediaResource(new FileDialogMediaResource(vl));
        ureq.getDispatchResult().setResultingMediaResource(new VFSMediaResource(vl));
        // do logging
        ThreadLocalUserActivityLogger.log(CourseLoggingAction.DIALOG_ELEMENT_FILE_DOWNLOADED, getClass(), LoggingResourceable.wrapBCFile(vl.getName()));
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // process my content events
        if (source == content) {
            final String command = event.getCommand();
            if (command.equals(ACTION_SHOW_FILE)) {
                doFileDelivery(ureq, selectedElement.getForumKey());
            }
        } else if (source == uploadButton) {
            final Forum forum = forumService.addAForum();
            final VFSContainer forumContainer = forumService.getForumContainer(forum.getKey());

            removeAsListenerAndDispose(fileUplCtr);
            fileUplCtr = new FileUploadController(getWindowControl(), forumContainer, ureq, (int) FolderConfig.getLimitULKB(), Quota.UNLIMITED, null, false);
            listenTo(fileUplCtr);

            recentDialogElement = new DialogElement();
            recentDialogElement.setForumKey(forum.getKey());
            recentDialogElement.setAuthor(ureq.getIdentity().getName());
            dialogPanel.setContent(fileUplCtr.getInitialComponent());
        }

    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }

}
