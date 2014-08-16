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

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.note.Note;
import org.olat.data.note.NoteDao;
import org.olat.lms.commons.change.ChangeManager;
import org.olat.presentation.note.NoteEvent;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation for NoteService
 * 
 * <P>
 * Initial Date: 04.05.2011 <br>
 * 
 * @author lavinia
 */
@Service("noteService")
public class NoteServiceImpl implements NoteService {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    private NoteDao noteDao;

    private NoteServiceImpl() {
        // [Spring]
    }

    /**
     * @see org.olat.lms.note.NoteService#setNote(org.olat.data.note.Note)
     */
    @Override
    public void setNote(Note note) {
        if (note.getKey() == null) {
            noteDao.saveNote(note);
            final Long newKey = note.getKey();
            final OLATResourceable ores = OresHelper.createOLATResourceableInstance(Note.class, newKey);
            ChangeManager.changed(ChangeManager.ACTION_CREATE, ores);
        } else {
            noteDao.updateNote(note);
        }
        fireBookmarkEvent(note.getOwner());
    }

    /**
     * @see org.olat.lms.note.NoteService#deleteNote(org.olat.data.note.Note)
     */
    @Override
    public void deleteNote(Note n) {
        noteDao.deleteNote(n);
        fireBookmarkEvent(n.getOwner());
    }

    /**
     * @see org.olat.lms.note.NoteService#getUserNotes(org.olat.data.basesecurity.Identity)
     */
    @Override
    public List<Note> getUserNotes(Identity owner) {
        return noteDao.listUserNotes(owner);
    }

    /**
     * Returns a note, either a new one in RAM, or the persisted if found using the params.
     * 
     * @param owner
     * @param resourceTypeName
     * @param resourceTypeId
     * @return
     */
    public Note getNote(final Identity owner, final String resourceTypeName, final Long resourceTypeId) {
        return noteDao.loadNoteOrCreateInRAM(owner, resourceTypeName, resourceTypeId);
    }

    /**
     * Delete all notes for certain identity.
     * 
     * @param identity
     *            Delete notes for this identity.
     */
    @Override
    @SuppressWarnings("unused")
    public void deleteUserData(final Identity identity, final String newDeletedUserName) {
        final List<Note> userNotes = getUserNotes(identity);
        for (final Iterator<Note> iter = userNotes.iterator(); iter.hasNext();) {
            this.deleteNote(iter.next());
        }
        if (log.isDebugEnabled()) {
            log.debug("All notes deleted for identity=" + identity, null);
        }
    }

    /**
     * Fire NoteEvent for a specific user after save/update/delete note.
     * 
     * @param identity
     */
    private void fireBookmarkEvent(final Identity identity) {
        // event this identity
        final NoteEvent noteEvent = new NoteEvent(identity.getName());
        final OLATResourceable eventBusOres = OresHelper.createOLATResourceableInstance(Identity.class, identity.getKey());
        // TODO: LD: use SingleUserEventCenter
        CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(noteEvent, eventBusOres);
    }

}
