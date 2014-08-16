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

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.PersistentObject;
import org.olat.system.exception.AssertException;

/**
 * Description: <br>
 * Implementation of the Note Interface
 * 
 * @author Alexander Schneider
 */
public class NoteImpl extends PersistentObject implements Note {
    private Identity owner;
    private String resourceTypeName;
    private Long resourceTypeId;
    private String subtype;
    private String noteTitle;
    private String noteText;
    private Date lastModified;
    private static final int RESOURCETYPENAME_MAXLENGTH = 50;
    private static final int SUBTYPE_MAXLENGTH = 50;

    /**
     * Default construcor
     */
    public NoteImpl() {
        // nothing to do
    }

    /**
     * @return Returns the noteText.
     */
    @Override
    public String getNoteText() {
        return noteText;
    }

    /**
     * @param noteText
     *            The noteText to set.
     */
    @Override
    public void setNoteText(final String noteText) {
        this.noteText = noteText;
    }

    /**
     * @return Returns the noteTitle.
     */
    @Override
    public String getNoteTitle() {
        return noteTitle;
    }

    /**
     * @param noteTitle
     *            The noteTitle to set.
     */
    @Override
    public void setNoteTitle(final String noteTitle) {
        this.noteTitle = noteTitle;
    }

    /**
     * @return Returns the owner.
     */
    @Override
    public Identity getOwner() {
        return owner;
    }

    /**
     * @param owner
     *            The owner to set.
     */
    @Override
    public void setOwner(final Identity owner) {
        this.owner = owner;
    }

    /**
     * @return Returns the resourceTypeId.
     */
    @Override
    public Long getResourceTypeId() {
        return resourceTypeId;
    }

    /**
     * @param resourceTypeId
     *            The resourceTypeId to set.
     */
    @Override
    public void setResourceTypeId(final Long resourceTypeId) {
        this.resourceTypeId = resourceTypeId;
    }

    /**
     * @return Returns the resourceTypeName.
     */
    @Override
    public String getResourceTypeName() {
        return resourceTypeName;
    }

    /**
     * @param resourceTypeName
     *            The resourceTypeName to set.
     */
    @Override
    public void setResourceTypeName(final String resourceTypeName) {
        if (resourceTypeName.length() > RESOURCETYPENAME_MAXLENGTH) {
            throw new AssertException("resourcetypename in o_note too long");
        }
        this.resourceTypeName = resourceTypeName;
    }

    /**
     * @return Returns the subtype.
     */
    @Override
    public String getSubtype() {
        return subtype;
    }

    /**
     * @param subtype
     *            The subtype to set.
     */
    @Override
    public void setSubtype(final String subtype) {
        if (subtype != null && subtype.length() > SUBTYPE_MAXLENGTH) {
            throw new AssertException("subtype of o_note too long");
        }
        this.subtype = subtype;
    }

    /**
	 */
    @Override
    public Date getLastModified() {
        return lastModified;
    }

    /**
	 */
    @Override
    public void setLastModified(final Date date) {
        this.lastModified = date;
    }
}
