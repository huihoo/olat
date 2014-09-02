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

package org.olat.presentation.user.administration.delete;

import java.util.ArrayList;
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.user.administration.delete.UserDeletionManager;
import org.olat.presentation.events.MultiIdentityChosenEvent;
import org.olat.presentation.events.SingleIdentityChosenEvent;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;

/**
 * Controller for 'Direct User Deletion' tab.
 * 
 * @author guretzki
 */
public class DirectDeleteController extends BasicController {

    private final VelocityContainer myContent;

    private DeletableUserSearchController usc;
    private DialogBoxController deleteConfirmController;
    private List<Identity> toDelete;
    private UserListForm userListForm;
    private BulkDeleteController bdc;
    private CloseableModalController cmc;

    public DirectDeleteController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        myContent = createVelocityContainer("directdelete");

        initializeUserSearchController(ureq);
        initializeUserListForm(ureq);

        putInitialPanel(myContent);

    }

    /**
     * This dispatches component events...
     * 
     */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        //
    }

    /**
     * This dispatches controller events...
     * 
     */
    @Override
    protected void event(final UserRequest ureq, final Controller sourceController, final Event event) {
        if (sourceController == usc) {
            if (event == Event.CANCELLED_EVENT) {
                removeAsListenerAndDispose(usc);

            } else if (event instanceof MultiIdentityChosenEvent) {
                final MultiIdentityChosenEvent multiEvent = (MultiIdentityChosenEvent) event;
                toDelete = multiEvent.getChosenIdentities();
                if (toDelete.size() == 0) {
                    showError("msg.selectionempty");
                    return;
                }
                if (!UserDeletionManager.getInstance().isReadyToDelete()) {
                    showInfo("info.is.not.ready.to.delete");
                    return;
                }
                deleteConfirmController = activateOkCancelDialog(ureq, null, translate("readyToDelete.delete.confirm", buildUserNameList(toDelete)),
                        deleteConfirmController);
                return;
            } else if (event instanceof SingleIdentityChosenEvent) {
                // single choose event may come from autocompleter user search
                if (!UserDeletionManager.getInstance().isReadyToDelete()) {
                    showInfo("info.is.not.ready.to.delete");
                    return;
                }
                final SingleIdentityChosenEvent uce = (SingleIdentityChosenEvent) event;
                toDelete = new ArrayList<Identity>();
                toDelete.add(uce.getChosenIdentity());

                deleteConfirmController = activateOkCancelDialog(ureq, null, translate("readyToDelete.delete.confirm", uce.getChosenIdentity().getName()),
                        deleteConfirmController);
                return;
            } else {
                throw new AssertException("unknown event ::" + event.getCommand());
            }
        } else if (sourceController == deleteConfirmController) {
            if (DialogBoxUIFactory.isOkEvent(event)) {
                UserDeletionManager.getInstance().deleteIdentities(toDelete);

                initializeUserSearchController(ureq);
                initializeUserListForm(ureq);

                showInfo("deleted.users.msg");
            }
        } else if (sourceController == bdc) {
            toDelete = bdc.getToDelete();
            cmc.deactivate();

            deleteConfirmController = activateOkCancelDialog(ureq, null, translate("readyToDelete.delete.confirm", buildUserNameList(toDelete)), deleteConfirmController);

        } else if (sourceController == cmc) {
            if (event == Event.CANCELLED_EVENT) {
                cmc.deactivate();
            }
        } else if (sourceController == userListForm) {

            removeAsListenerAndDispose(bdc);
            bdc = new BulkDeleteController(ureq, getWindowControl(), userListForm.getLogins(), userListForm.getReason());
            listenTo(bdc);

            removeAsListenerAndDispose(cmc);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), bdc.getInitialComponent());
            listenTo(cmc);

            cmc.activate();
        }
    }

    /**
     * Build comma seperated list of usernames.
     * 
     * @param toDelete
     * @return
     */
    private String buildUserNameList(final List<Identity> toDeleteIdentities) {
        final StringBuilder buf = new StringBuilder();
        for (final Identity identity : toDeleteIdentities) {
            if (buf.length() > 0) {
                buf.append(",");
            }
            buf.append(identity.getName());
        }
        return buf.toString();
    }

    private void initializeUserSearchController(final UserRequest ureq) {
        removeAsListenerAndDispose(usc);
        usc = new DeletableUserSearchController(ureq, getWindowControl());
        listenTo(usc);
        myContent.put("usersearch", usc.getInitialComponent());
        myContent.contextPut("deletedusers", new ArrayList());
    }

    private void initializeUserListForm(final UserRequest ureq) {
        myContent.contextRemove("userlist");
        removeAsListenerAndDispose(userListForm);
        userListForm = new UserListForm(ureq, getWindowControl());
        listenTo(userListForm);
        myContent.put("userlist", userListForm.getInitialComponent());
    }

    @Override
    protected void doDispose() {
        //
    }

}
