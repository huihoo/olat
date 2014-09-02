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

package org.olat.presentation.admin.instantMessaging;

import org.olat.lms.instantmessaging.InstantMessagingModule;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.FormLink;
import org.olat.presentation.framework.core.components.form.flexible.elements.IntegerElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.FormSubmit;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;

/**
 * @author gnaegi <www.goodsolutions.ch>
 * @author guido Initial Date: Aug 2, 2006 Description: Instant messaging server administration task within olat
 */
public class InstantMessagingAdminController extends FormBasicController {

    private FormLink doSyncButton;
    private IntegerElement idlePollTime;
    private IntegerElement chatPollTime;
    private FormSubmit submit;
    private FormLinkImpl checkPlugin;
    private FormLinkImpl reconnectAdminUser;

    public InstantMessagingAdminController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl, "index");
        //
        initForm(this.flc, this, ureq);
    }

    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        if (source == doSyncButton) {
            final boolean hasError = InstantMessagingModule.getAdapter().synchronizeAllBuddyGroupsWithIMServer();
            InstantMessagingModule.getAdapter().synchronizeLearningGroupsWithIMServer();
            if (!hasError) {
                showInfo("imadmin.sync.failed");
            }
        } else if (source == checkPlugin) {
            final String ok = InstantMessagingModule.getAdapter().checkServerPlugin();
            showInfo("imadmin.plugin.version", ok);
        } else if (source == reconnectAdminUser) {
            InstantMessagingModule.getAdapter().resetAdminConnection();
            showInfo("imadmin.plugin.admin.connection.done");
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to dispose
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        // TODO Auto-generated method stub
        InstantMessagingModule.setCHAT_POLLTIME(chatPollTime.getIntValue());
        InstantMessagingModule.setIDLE_POLLTIME(idlePollTime.getIntValue());
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        final FormLayoutContainer mainLayout = FormLayoutContainer.createDefaultFormLayout("mainLayout", getTranslator());
        formLayout.add(mainLayout);

        final String imServerName = InstantMessagingModule.getAdapter().getConfig().getServername();
        final String imAdminUsername = InstantMessagingModule.getAdapter().getConfig().getAdminUsername();
        final String imAdminPw = InstantMessagingModule.getAdapter().getConfig().getAdminPassword();

        flc.contextPut("IMServerAdminGUI", imServerName);
        flc.contextPut("IMServerAdminUsername", imAdminUsername);
        flc.contextPut("IMServerAdminPw", imAdminPw);

        checkPlugin = new FormLinkImpl("imadmin.plugin.check");
        checkPlugin.setCustomEnabledLinkCSS("b_button");
        formLayout.add(checkPlugin);

        reconnectAdminUser = new FormLinkImpl("imadmin.plugin.admin.reconnect");
        reconnectAdminUser.setCustomEnabledLinkCSS("b_button");
        formLayout.add(reconnectAdminUser);

        // doSyncButton = LinkFactory.createButton("imadmin.sync.cmd.dosync", imAdminVC, this);
        doSyncButton = new FormLinkImpl("imadmin.sync.cmd.dosync");
        doSyncButton.setCustomEnabledLinkCSS("b_button");
        doSyncButton.setCustomDisabledLinkCSS("b_button b_button_disabled");
        formLayout.add(doSyncButton);

        idlePollTime = uifactory.addIntegerElement("idlepolltime", "imadming.idlepolltime", InstantMessagingModule.getIDLE_POLLTIME(), mainLayout);
        idlePollTime.setExampleKey("imadming.idlepolltime.default", new String[] { "" + InstantMessagingModule.getAdapter().getConfig().getIdlePolltime() });
        idlePollTime.showExample(true);

        chatPollTime = uifactory.addIntegerElement("chatpolltime", "imadming.chatpolltime", InstantMessagingModule.getCHAT_POLLTIME(), mainLayout);
        chatPollTime.setExampleKey("imadming.chatpolltime.default", new String[] { "" + InstantMessagingModule.getAdapter().getConfig().getChatPolltime() });
        chatPollTime.showExample(true);

        submit = new FormSubmit("subm", "submit");

        mainLayout.add(submit);

    }

}
