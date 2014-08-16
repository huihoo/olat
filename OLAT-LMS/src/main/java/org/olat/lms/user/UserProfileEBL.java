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
package org.olat.lms.user;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.user.UserConstants;
import org.olat.lms.security.BaseSecurityModule;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.SyncerExecutor;
import org.olat.system.event.MultiUserEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * class created during DB-Bad smell refactoring
 * 
 * <P>
 * Initial Date: 07.09.2011 <br>
 * 
 * @author Branislav Balaz
 */
@Component
@Scope("prototype")
public class UserProfileEBL {

    @Autowired
    UserService userService;
    @Autowired
    BaseSecurity baseSecurityManager;

    private Identity identityToModify;
    private boolean emailChanged;
    private boolean userUpdated;

    /**
     * @param ureq
     */
    public UserProfileDataEBL updateUserProfileData(final UserProfileDataEBL userProfileData) {

        emailChanged = userProfileData.isEmailChanged();
        identityToModify = userProfileData.getIdentity();

        CoordinatorManager.getInstance().getCoordinator().getSyncer()
                .doInSync(OresHelper.createOLATResourceableInstance(Identity.class, userProfileData.getIdentity().getKey()), new SyncerExecutor() {
                    @Override
                    @SuppressWarnings("synthetic-access")
                    public void execute() {

                        if (!userProfileData.getCurrentEmail().equals(userProfileData.getChangedEmail())) {
                            // allow an admin to change email without
                            // verification workflow. usermanager is
                            // only permitted to do so, if set by
                            // config.
                            if (!(userProfileData.isOlatAdmin() || (BaseSecurityModule.USERMANAGER_CAN_BYPASS_EMAILVERIFICATION && userProfileData.isOlatManager()))) {
                                emailChanged = true;
                                // change email address to old address
                                // until it is verified
                                userService.setUserProperty(identityToModify.getUser(), UserConstants.EMAIL, userProfileData.getCurrentEmail());
                            }
                        }
                        userUpdated = userService.updateUserFromIdentity(userProfileData.getIdentity());
                        if (!userUpdated) {
                            // reload user data from db
                            identityToModify = baseSecurityManager.loadIdentityByKey(userProfileData.getIdentity().getKey());
                        }
                        CoordinatorManager
                                .getInstance()
                                .getCoordinator()
                                .getEventBus()
                                .fireEventToListenersOf(new MultiUserEvent("changed"),
                                        OresHelper.createOLATResourceableInstance(Identity.class, identityToModify.getKey()));

                    }
                });

        return new UserProfileDataEBL(identityToModify, emailChanged, userUpdated, userProfileData.isOlatAdmin(), userProfileData.isOlatManager(),
                userProfileData.getCurrentEmail(), userProfileData.getChangedEmail());
    }

    /*
     * TODO: ORID-1007 has been extracted from presentation and question if that should be moved outside
     */
    public Identity loadIdentity(final Long key) {
        return baseSecurityManager.loadIdentityByKey(key);
    }

}
