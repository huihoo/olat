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
package org.olat.data.note;

import java.util.List;

import org.olat.data.basesecurity.Identity;

/**
 * TODO: Class Description for NoteDao
 * 
 * <P>
 * Initial Date: 04.07.2011 <br>
 * 
 * @author lavinia
 */
public interface NoteDao {

    /**
     * @param owner
     * @param resourceTypeName
     * @param resourceTypeId
     * @return a note, either a new one in RAM, or the persisted if found using the params
     */
    public Note loadNoteOrCreateInRAM(final Identity owner, final String resourceTypeName, final Long resourceTypeId);

    /**
     * @param owner
     * @return a list of notes belonging to the owner
     */
    public List<Note> listUserNotes(final Identity owner);

    /**
     * Deletes a note on the database
     * 
     * @param n
     *            the note
     */
    public void deleteNote(Note n);

    /**
     * Save a note
     * 
     * @param n
     */
    public void saveNote(final Note n);

    /**
     * Update a note
     * 
     * @param n
     */
    public void updateNote(final Note n);

}
