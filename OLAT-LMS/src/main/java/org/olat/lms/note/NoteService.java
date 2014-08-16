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
package org.olat.lms.note;

import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.note.Note;
import org.olat.lms.user.UserDataDeletable;

/**
 * TODO: Class Description for NoteService
 * 
 * <P>
 * Initial Date: 03.05.2011 <br>
 * 
 * @author lavinia
 */
public interface NoteService extends UserDataDeletable {

    /**
     * Finds or creates note.
     * 
     * @param owner
     * @param resourceTypeName
     * @param resourceTypeId
     * @return
     */
    public Note getNote(final Identity owner, final String resourceTypeName, final Long resourceTypeId);

    /**
     * Stores a note.
     * 
     * @param note
     */
    public void setNote(Note note);

    /**
     * Deletes this note.
     * 
     * @param n
     */
    public void deleteNote(Note n);

    /**
     * Get all notes for the given identity.
     * 
     * @param owner
     * @return
     */
    public List<Note> getUserNotes(final Identity owner);

    /**
     * Deletes all notes of this identity.
     * 
     * @see org.olat.lms.user.UserDataDeletable#deleteUserData(org.olat.data.basesecurity.Identity, java.lang.String)
     */
    public void deleteUserData(final Identity identity, final String newDeletedUserName);

}
