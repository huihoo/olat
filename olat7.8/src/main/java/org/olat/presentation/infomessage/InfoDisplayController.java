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

package org.olat.presentation.infomessage;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.infomessage.InfoMessage;
import org.olat.data.user.User;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.coordinate.LockingService;
import org.olat.lms.infomessage.InfoMessageFrontendManager;
import org.olat.lms.infomessage.InfoSecurityCallback;
import org.olat.lms.user.UserService;
import org.olat.presentation.course.nodes.info.InfoCourseNodeConfiguration;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.date.DateComponentFactory;
import org.olat.presentation.framework.core.components.date.DateElement;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.LockResult;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

import com.ibm.icu.util.Calendar;

/**
 * Description:<br>
 * Controller which displays the info messages from an OLATResourceable
 * <P>
 * Initial Date: 26 jul. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InfoDisplayController extends BasicController {

    private Link newInfoLink;
    private Link oldMsgsLink;
    private Link newMsgsLink;
    private final List<Link> editLinks = new ArrayList<Link>();
    private final List<Link> deleteLinks = new ArrayList<Link>();
    private DialogBoxController confirmDelete;
    private InfoEditController editController;
    private InfoEditFormController newMessageFormController;

    private final List<Long> previousDisplayKeys = new ArrayList<Long>();
    private final InfoSecurityCallback secCallback;
    private final OLATResourceable ores;
    private final String resSubPath;
    private final String businessPath;

    private int maxResults = 0;
    private int maxResultsConfig = 0;
    private int duration = -1;
    private Date after = null;
    private Date afterConfig = null;

    private final InfoMessageFrontendManager infoMessageManager;

    private LockResult lockEntry;

    private VelocityContainer messagesVC;
    private final Panel messagePanel;

    public InfoDisplayController(final UserRequest ureq, final WindowControl wControl, final ModuleConfiguration config, final InfoSecurityCallback secCallback,
            final OLATResourceable ores, final String resSubPath, final String businessPath) {
        super(ureq, wControl);
        this.secCallback = secCallback;
        this.ores = ores;
        this.resSubPath = resSubPath;
        this.businessPath = businessPath;

        infoMessageManager = CoreSpringFactory.getBean(InfoMessageFrontendManager.class);
        maxResults = maxResultsConfig = getConfigValue(config, InfoCourseNodeConfiguration.CONFIG_LENGTH, 10);
        duration = getConfigValue(config, InfoCourseNodeConfiguration.CONFIG_DURATION, 90);

        if (duration > 0) {
            final Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DATE, -duration);
            after = afterConfig = cal.getTime();
        }

        messagePanel = new Panel("messagePanel");
        messagePanel.addListener(this);
        messagesVC = createVelocityContainer("display");
        if (secCallback.canAdd()) {
            newInfoLink = LinkFactory.createButton("new_message", "new_message", messagesVC, this);
        }
        oldMsgsLink = LinkFactory.createButton("display.old_messages", "display.old_messages", messagesVC, this);
        newMsgsLink = LinkFactory.createButton("display.new_messages", "display.new_messages", messagesVC, this);
        messagePanel.setContent(messagesVC);
        putInitialPanel(messagePanel);

        // OLAT-6302 when a specific message is shown display the page that
        // contains the message. Jump in e.g. from portlet
        ContextEntry ce = wControl.getBusinessControl().popLauncherContextEntry();
        if (ce != null) { // a context path is left for me
            OLATResourceable businessPathResource = ce.getOLATResourceable();
            String typeName = businessPathResource.getResourceableTypeName();
            if ("InfoMessage".equals(typeName)) {
                Long messageId = businessPathResource.getResourceableId();
                if (messageId != null && messageId.longValue() > 0) {
                    // currently no pageing is implemented, just page with all entries
                    maxResults = -1;
                    after = null;
                }
            }
        }

        // now load with configuration
        loadMessages();
    }

    private int getConfigValue(final ModuleConfiguration config, final String key, final int def) {
        final String durationStr = (String) config.get(key);
        if ("\u221E".equals(durationStr)) {
            return -1;
        } else if (StringHelper.containsNonWhitespace(durationStr)) {
            try {
                return Integer.parseInt(durationStr);
            } catch (final NumberFormatException e) { /* fallback to default */
            }
        }
        return def;
    }

    /**
     * This is the main method which push the messages in the layout container, and clean-up old links.
     */
    protected void loadMessages() {
        // first clear the current message if any
        for (final Long key : previousDisplayKeys) {
            messagesVC.contextRemove("info.date." + key);
            if (messagesVC.getComponent("info.delete." + key) != null) {
                messagesVC.remove(messagesVC.getComponent("info.delete." + key));
            }
            if (messagesVC.getComponent("info.edit." + key) != null) {
                messagesVC.remove(messagesVC.getComponent("info.edit." + key));
            }
        }
        previousDisplayKeys.clear();
        deleteLinks.clear();

        final List<InfoMessage> msgs = infoMessageManager.loadInfoMessageByResource(ores, resSubPath, businessPath, after, null, 0, maxResults);
        final List<InfoMessageForDisplay> infoDisplays = new ArrayList<InfoMessageForDisplay>();
        for (final InfoMessage info : msgs) {
            previousDisplayKeys.add(info.getKey());
            infoDisplays.add(createInfoMessageForDisplay(info));

            final String dateCmpName = "info.date." + info.getKey();
            final DateElement dateEl = DateComponentFactory.createDateElementWithYear(dateCmpName, info.getCreationDate());
            messagesVC.put(dateCmpName, dateEl.getComponent());

            if (secCallback.canEdit()) {
                final String editName = "info.edit." + info.getKey();
                final Link link = LinkFactory.createButton(editName, "edit", messagesVC, this);
                link.setUserObject(info);
                editLinks.add(link);
            }
            if (secCallback.canDelete()) {
                final String editName = "info.delete." + info.getKey();
                final Link link = LinkFactory.createButton(editName, "delete", messagesVC, this);
                link.setUserObject(info);
                deleteLinks.add(link);
            }
        }
        messagesVC.contextPut("infos", infoDisplays);

        final int numOfInfos = infoMessageManager.countInfoMessageByResource(ores, resSubPath, businessPath, null, null);
        oldMsgsLink.setVisible((msgs.size() < numOfInfos));
        newMsgsLink.setVisible((msgs.size() == numOfInfos) && (numOfInfos > maxResultsConfig) && (maxResultsConfig > 0));
    }

    private InfoMessageForDisplay createInfoMessageForDisplay(final InfoMessage info) {
        String title = "";
        if (StringHelper.containsNonWhitespace(info.getTitle())) {
            title = Formatter.escWithBR(info.getTitle()).toString();
        }
        String message = "";
        if (StringHelper.containsNonWhitespace(info.getMessage())) {
            message = Formatter.escWithBR(info.getMessage()).toString();
        }

        final DateFormat formatter = DateFormat.getDateInstance(DateFormat.MEDIUM, getLocale());

        String modifier = null;
        if (info.getModifier() != null) {
            final User user = info.getModifier().getUser();
            final String formattedName = getUserService().getFirstAndLastname(user);
            final String creationDate = formatter.format(info.getModificationDate());
            modifier = translate("display.modifier", new String[] { formattedName, creationDate });
        }

        final String authorName = getUserService().getFirstAndLastname(info.getAuthor().getUser());

        final String creationDate = formatter.format(info.getCreationDate());
        final String infos = translate("display.info", new String[] { authorName, creationDate });

        return new InfoMessageForDisplay(info.getKey(), title, message, infos, modifier);
    }

    @Override
    protected void doDispose() {
        if (lockEntry != null) {
            getLockingService().releaseLock(lockEntry);
            lockEntry = null;
        }
    }

    private LockingService getLockingService() {
        return CoreSpringFactory.getBean(LockingService.class);
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == newMessageFormController) {
            if (event == Event.DONE_EVENT) {
                final InfoMessage msg = infoMessageManager.createInfoMessage(ores, resSubPath, businessPath, ureq.getIdentity());
                msg.setTitle(newMessageFormController.getTitle());
                msg.setMessage(newMessageFormController.getMessage());
                infoMessageManager.saveInfoMessage(msg);
            }
            messagesVC.remove(newMessageFormController.getInitialComponent());
            newMessageFormController = null;
            messagesVC.setDirty(true);
            loadMessages();

        } else if (source == confirmDelete) {
            if (DialogBoxUIFactory.isYesEvent(event)) {
                final InfoMessage msgToDelete = (InfoMessage) confirmDelete.getUserObject();
                infoMessageManager.deleteInfoMessage(msgToDelete);
                loadMessages();
            }
            confirmDelete.setUserObject(null);
            getLockingService().releaseLock(lockEntry);
            lockEntry = null;
        } else if (source == editController) {
            messagesVC.remove(editController.getInitialComponent());
            editController = null;
            loadMessages();
            getLockingService().releaseLock(lockEntry);
            lockEntry = null;
        } else {
            super.event(ureq, source, event);
        }
    }

    protected void popupDelete(final UserRequest ureq, InfoMessage msg) {
        final OLATResourceable mres = OresHelper.createOLATResourceableInstance(InfoMessage.class, msg.getKey());
        lockEntry = getLockingService().acquireLock(mres, ureq.getIdentity(), "");
        if (lockEntry.isSuccess()) {
            // locked -> reload the message
            msg = infoMessageManager.loadInfoMessage(msg.getKey());
            if (msg == null) {
                showWarning("already.deleted");
                getLockingService().releaseLock(lockEntry);
                lockEntry = null;
                loadMessages();
            } else {
                final String confirmDeleteText = translate("edit.confirm_delete", new String[] { StringHelper.escapeHtml(msg.getTitle()) });
                confirmDelete = activateYesNoDialog(ureq, null, confirmDeleteText, confirmDelete);
                confirmDelete.setUserObject(msg);
            }
        } else {
            final String name = getLockOwnersFirstAndLastName(lockEntry);
            showWarning("already.edited", name);
        }
    }

    /**
     * @return
     */
    private String getLockOwnersFirstAndLastName(final LockResult lockResult) {
        // final User user = lockEntry.getOwner().getUser();
        final User user = getBaseSecurity().findIdentityByName(lockResult.getOwner().getName()).getUser();
        final String name = getUserService().getFirstAndLastname(user);
        return name;
    }

    private BaseSecurity getBaseSecurity() {
        return CoreSpringFactory.getBean(BaseSecurity.class);
    }

    protected void popupEdit(final UserRequest ureq, InfoMessage msg) {
        final OLATResourceable mres = OresHelper.createOLATResourceableInstance(InfoMessage.class, msg.getKey());
        lockEntry = getLockingService().acquireLock(mres, ureq.getIdentity(), "");
        if (lockEntry.isSuccess()) {
            msg = infoMessageManager.loadInfoMessage(msg.getKey());
            if (msg == null) {
                showWarning("already.deleted");
                getLockingService().releaseLock(lockEntry);
                lockEntry = null;
                loadMessages();
            } else {
                removeAsListenerAndDispose(editController);
                editController = new InfoEditController(ureq, getWindowControl(), msg);
                listenTo(editController);
                messagesVC.put("edit_message_form", editController.getInitialComponent());
                messagesVC.contextPut("msg_key", msg.getKey());
            }
        } else {
            final String name = getLockOwnersFirstAndLastName(lockEntry);
            showWarning("already.edited", name);
        }
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        if (source == newInfoLink) {
            /** TODO: REVIEW GUI MITTEILUNGEN: bb/18.07.2012 **/
            removeAsListenerAndDispose(newMessageFormController);
            newMessageFormController = new InfoEditFormController(ureq, getWindowControl());
            listenTo(newMessageFormController);
            messagesVC.put("new_message_form", newMessageFormController.getInitialComponent());
            if (editController != null) {
                messagesVC.remove(editController.getInitialComponent());
                removeAsListenerAndDispose(editController);
                editController = null;
            }
        } else if (deleteLinks.contains(source)) {
            final InfoMessage msg = (InfoMessage) ((Link) source).getUserObject();
            popupDelete(ureq, msg);
        } else if (editLinks.contains(source)) {
            if (newMessageFormController != null) {
                messagesVC.remove(newMessageFormController.getInitialComponent());
                removeAsListenerAndDispose(newMessageFormController);
                newMessageFormController = null;
            }
            final InfoMessage msg = (InfoMessage) ((Link) source).getUserObject();
            popupEdit(ureq, msg);
        } else if (source == oldMsgsLink) {
            maxResults = -1;
            after = null;
            loadMessages();
        } else if (source == newMsgsLink) {
            maxResults = maxResultsConfig;
            after = afterConfig;
            loadMessages();
        }

    }

}
