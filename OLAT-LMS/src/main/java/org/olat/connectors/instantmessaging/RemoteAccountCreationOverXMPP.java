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
package org.olat.connectors.instantmessaging;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.ProviderManager;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * implementation of the account creator over XMPP messaging protocoll
 * <P>
 * Initial Date: 31.07.2008 <br>
 * 
 * @author guido
 */
public class RemoteAccountCreationOverXMPP implements RemoteAccountCreation {

    private AdminUserConnection adminUser;
    private static final Logger log = LoggerHelper.getLogger();

    protected RemoteAccountCreationOverXMPP() {
        final ProviderManager providerMgr = ProviderManager.getInstance();
        // register iq handlers
        providerMgr.addIQProvider("query", UserCreate.NAMESPACE, new UserCreate.Provider());
        providerMgr.addIQProvider("query", UserDelete.NAMESPACE, new UserDelete.Provider());
        providerMgr.addIQProvider("query", UserCheck.NAMESPACE, new UserCheck.Provider());

    }

    /**
	 */
    @Override
    public boolean createAccount(final String username, final String password, final String name, final String email) {
        if (log.isDebugEnabled()) {
            log.debug("trying to create IM account on server for username:" + username);
        }
        return sendPacket(new UserCreate(username, password, email, name));
    }

    /**
	 */
    @Override
    public boolean deleteAccount(final String username) {
        if (log.isDebugEnabled()) {
            log.debug("trying to delete IM account on server for username:" + username);
        }
        return sendPacket(new UserDelete(username));
    }

    private boolean sendPacket(final IQ packet) {
        final XMPPConnection con = adminUser.getConnection();
        try {
            packet.setFrom(con.getUser());
            final PacketCollector collector = con.createPacketCollector(new PacketIDFilter(packet.getPacketID()));
            con.sendPacket(packet);
            final IQ response = (IQ) collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
            collector.cancel();

            if (response == null) {
                // OLAT-5384: error happens frequently, lowering to WARN
                log.warn("Error while trying to create/delete user at IM server. Response was null!");
                return false;
            }
            if (response.getError() != null) {
                if (response.getError().getCode() == 503) {
                    // 503 code means service not available, IM server plugin may not installed
                    log.error("Openfire and OLAT talk over an custom Openfire plugin. Please make sure you have it installed! "
                            + "Download it under http://www.olat.org/downloads/stable/olatUserAndGroupService.jar");
                } else if (response.getError().getCode() == 407 || response.getError().getCode() == 409) {
                    // 407 or 409 -> conflict / user already exists
                    return true;
                } else if (response.getError().getCode() == 404) {
                    // 404 -> user not found, ok when trying to delete
                    return true;
                }
                log.warn("Error while trying to create/delete user at IM server. Errorcode: " + response.getError().getCode());
                return false;
            } else if (response.getType() == IQ.Type.ERROR) {
                log.error("Error while trying to create/delete user at IM server. Response type error");
                return false;
            }
            if (response instanceof UserCheck) {
                final UserCheck check = (UserCheck) response;
                return check.hasAccount();
            }
            return true;
        } catch (final Exception e) {
            log.error("Error while trying to create/delete user at IM server", e);
            return false;
        }
    }

    public void setConnection(final AdminUserConnection adminUser) {
        this.adminUser = adminUser;
    }

    /**
	 */
    @Override
    public boolean hasAccount(final String username) {
        return sendPacket(new UserCheck(username));
    }

}
