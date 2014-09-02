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

package org.olat.data.basesecurity;

import java.util.Date;

import org.olat.data.commons.database.CreateInfo;
import org.olat.data.commons.database.Persistable;

/**
 * Description: <BR/>
 * 
 * @author Felix Jost
 */
public interface Authentication extends CreateInfo, Persistable {

    /**
     * @return
     */
    public Identity getIdentity();

    /**
     * @return
     */
    public String getProvider();

    /**
     * @return
     */
    public String getAuthusername();

    /**
     * The old credential still supported for now, but should be replaced by the new credential.
     * 
     * @return
     */
    @Deprecated
    public String getCredential();

    public String getNewCredential();

    public Date getLastModified();

    /**
     * @param identity
     */
    public void setIdentity(Identity identity);

    /**
     * @param provider
     */
    public void setProvider(String provider);

    /**
     * @param authusername
     */
    public void setAuthusername(String authusername);

    /**
     * @Deprecated for OLAT, WebDAV authentications, supported for the others (feed, etc.)
     * 
     * @param credential
     */
    @Deprecated
    public void setCredential(String credential);

    /**
     * This is intended only for the OLAT provider credential, the old credential is too weak.
     */
    public void setNewCredential(String credential);

    public void setLastModified(Date date);

}
