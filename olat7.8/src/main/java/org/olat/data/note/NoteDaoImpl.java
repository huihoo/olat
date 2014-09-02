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

import java.util.Date;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.type.Type;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBQuery;
import org.olat.system.commons.manager.BasicManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Description:
 * 
 * @author Alexander Schneider
 */
@Repository
public class NoteDaoImpl extends BasicManager implements NoteDao {

    @Autowired
    private DB db;

    /**
     * [spring]
     * 
     */
    private NoteDaoImpl() {
        //
    }

    /**
     * @param owner
     * @param resourceTypeName
     * @param resourceTypeId
     * @return a note, either a new one in RAM, or the persisted if found using the params
     */
    @Override
    public Note loadNoteOrCreateInRAM(final Identity owner, final String resourceTypeName, final Long resourceTypeId) {
        Note note = findNote(owner, resourceTypeName, resourceTypeId);
        if (note == null) {
            note = createNote(owner, resourceTypeName, resourceTypeId);
        }
        return note;
    }

    /**
     * @param owner
     * @param resourceTypeName
     * @param resourceTypeId
     * @return the note
     */
    private Note createNote(final Identity owner, final String resourceTypeName, final Long resourceTypeId) {
        final Note n = new NoteImpl();
        n.setOwner(owner);
        n.setResourceTypeName(resourceTypeName);
        n.setResourceTypeId(resourceTypeId);
        return n;
    }

    /**
     * @param owner
     * @param resourceTypeName
     * @param resourceTypeId
     * @return the note
     */
    private Note findNote(final Identity owner, final String resourceTypeName, final Long resourceTypeId) {

        final String query = "from org.olat.data.note.NoteImpl as n where n.owner = ? and n.resourceTypeName = ? and n.resourceTypeId = ?";
        final List notes = db.find(query, new Object[] { owner.getKey(), resourceTypeName, resourceTypeId }, new Type[] { Hibernate.LONG, Hibernate.STRING,
                Hibernate.LONG });

        if (notes == null || notes.size() != 1) {
            return null;
        } else {
            return (Note) notes.get(0);
        }
    }

    /**
     * @param owner
     * @return a list of notes belonging to the owner
     */
    @Override
    public List<Note> listUserNotes(final Identity owner) {
        final String query = "from org.olat.data.note.NoteImpl as n inner join fetch n.owner as noteowner where noteowner = :noteowner";
        final DBQuery dbQuery = db.createQuery(query.toString());
        dbQuery.setEntity("noteowner", owner);
        final List<Note> notes = dbQuery.list();
        return notes;
    }

    /**
     * Deletes a note on the database
     * 
     * @param n
     *            the note
     */
    @Override
    public void deleteNote(Note n) {
        n = (Note) db.loadObject(n);
        db.deleteObject(n);
    }

    /**
     * Save a note
     * 
     * @param n
     */
    @Override
    public void saveNote(final Note n) {
        n.setLastModified(new Date());
        db.saveObject(n);

    }

    /**
     * Update a note
     * 
     * @param n
     */
    @Override
    public void updateNote(final Note n) {
        n.setLastModified(new Date());
        db.updateObject(n);

    }

}
