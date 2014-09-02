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

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.CreateInfo;
import org.olat.data.commons.database.ModifiedInfo;
import org.olat.data.commons.database.Persistable;

/**
 * Description:<br>
 * This is the interface to the note.
 * 
 * @author Alexander Schneider
 */
public interface Note extends CreateInfo, ModifiedInfo, Persistable {
    public abstract Identity getOwner();

    public abstract String getResourceTypeName();

    public abstract Long getResourceTypeId();

    public abstract String getSubtype();

    public abstract String getNoteTitle();

    public abstract String getNoteText();

    public abstract void setOwner(Identity identity);

    public abstract void setResourceTypeName(String resourceTypeName);

    public abstract void setResourceTypeId(Long resourceTypeId);

    public abstract void setSubtype(String subtype);

    public abstract void setNoteTitle(String nodeTitle);

    public abstract void setNoteText(String noteText);

}
