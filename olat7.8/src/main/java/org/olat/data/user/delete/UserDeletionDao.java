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
package org.olat.data.user.delete;

import java.util.List;

import org.olat.data.basesecurity.Identity;

/**
 * TODO: Class Description for UserDeletionDao
 * 
 * <P>
 * Initial Date: 04.07.2011 <br>
 * 
 * @author guretzki
 */
public interface UserDeletionDao {

    /** Default value for last-login duration in month. */
    public static final String SEND_DELETE_EMAIL_ACTION = "sendDeleteEmail";

    public static final String USER_ARCHIVE_DIR = "archive_deleted_users";

    public abstract void markSendEmailEvent(Identity identity);

    /**
     * Return list of identities which have last-login older than 'lastLoginDuration' parameter. This user are ready to start with user-deletion process.
     * 
     * @param lastLoginDuration
     *            last-login duration in month
     * @return List of Identity objects
     */
    public abstract List getDeletableIdentities(final int lastLoginDuration);

    /**
     * Return list of identities which are in user-deletion-process. user-deletion-process means delete-announcement.email send, duration of waiting for response is not
     * expired.
     * 
     * @param deleteEmailDuration
     *            Duration of user-deletion-process in days
     * @return List of Identity objects
     */
    public abstract List getIdentitiesInDeletionProcess(final int deleteEmailDuration);

    /**
     * Return list of identities which are ready-to-delete in user-deletion-process. (delete-announcement.email send, duration of waiting for response is expired).
     * 
     * @param deleteEmailDuration
     *            Duration of user-deletion-process in days
     * @return List of Identity objects
     */
    public abstract List getIdentitiesReadyToDelete(final int deleteEmailDuration);

    /**
     * Re-activate an identity, lastLogin = now, reset deleteemaildate = null.
     * 
     * @param identity
     */
    public abstract void setIdentityAsActiv(final Identity anIdentity);

}
