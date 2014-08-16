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

import org.jivesoftware.smack.ConnectionListener;
import org.olat.lms.instantmessaging.InstantMessagingClient;
import org.olat.lms.instantmessaging.InstantMessagingModule;

/**
 * Initial Date: 21.02.2005 Helper Class for connection monitoring of instant messaging connection
 * 
 * @author guido
 */
public class XMPPConnListener implements ConnectionListener {
    private InstantMessagingClient imc = null;

    public XMPPConnListener(final InstantMessagingClient imc) {
        this.imc = imc;
    }

    /**
     * called on normal disconnect
     * 
     */
    @Override
    public void connectionClosed() {
        // if connection.close() is called by OLAT the imc is already
        // disconnected when getting this event
        if (imc.isConnected()) {
            imc.setIsConnected(false);
            InstantMessagingModule.getAdapter().getClientManager().destroyInstantMessagingClient(imc.getUsername());
        }
    }

    /**
     * called automatically when server crashes
     * 
     */
    @Override
    public void connectionClosedOnError(final Exception e) {
        // if connection.close() is called by OLAT the imc is already
        // disconnected when getting this event
        if (imc.isConnected()) {
            imc.setIsConnected(false);
            InstantMessagingModule.getAdapter().getClientManager().destroyInstantMessagingClient(imc.getUsername());
        }
    }

    @Override
    public void reconnectingIn(final int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void reconnectionFailed(final Exception arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void reconnectionSuccessful() {
        // TODO Auto-generated method stub

    }

}
