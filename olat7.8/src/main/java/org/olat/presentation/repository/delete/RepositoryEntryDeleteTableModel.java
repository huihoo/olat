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

package org.olat.presentation.repository.delete;

import java.util.Date;
import java.util.List;

import org.olat.data.lifecycle.LifeCycleManager;
import org.olat.data.repository.RepositoryDeletionDao;
import org.olat.data.repository.RepositoryEntry;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;

/**
 * The repository-entry table data model for repository deletion.
 * 
 * @author Christian Guretzki
 */
public class RepositoryEntryDeleteTableModel extends DefaultTableDataModel {

    /**
     * @param objects
     */
    public RepositoryEntryDeleteTableModel(final List objects) {
        super(objects);
    }

    /**
	 */
    @Override
    public final Object getValueAt(final int row, final int col) {
        final RepositoryEntry repositoryEntry = (RepositoryEntry) getObject(row);
        switch (col) {
        case 0:
            // TODO:cg check data garbage in db, each repositoryEntry should have a ores => cleanup-code to remove this entries
            return repositoryEntry;
        case 1:
            final String titel = repositoryEntry.getDisplayname();
            return (titel == null ? "n/a" : titel);
        case 2:
            final String author = repositoryEntry.getInitialAuthor();
            return (author == null ? "n/a" : author);
        case 3:
            final Date lastUsage = repositoryEntry.getLastUsage();
            return (lastUsage == null ? "n/a" : lastUsage);
        case 4:
            final Date deleteEmail = LifeCycleManager.createInstanceFor(repositoryEntry).lookupLifeCycleEntry(RepositoryDeletionDao.SEND_DELETE_EMAIL_ACTION)
                    .getLcTimestamp();
            return (deleteEmail == null ? "n/a" : deleteEmail);
        default:
            return "error";
        }
    }

    /**
	 */
    @Override
    public int getColumnCount() {
        return 5;
    }
}
