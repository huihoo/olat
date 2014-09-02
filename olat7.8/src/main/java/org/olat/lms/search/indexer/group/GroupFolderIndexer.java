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

package org.olat.lms.search.indexer.group;

import java.io.IOException;

import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.data.group.BusinessGroup;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.document.GroupDocument;
import org.olat.lms.search.indexer.FolderIndexer;
import org.olat.lms.search.indexer.FolderIndexerAccess;
import org.olat.lms.search.indexer.FolderIndexerTimeoutException;
import org.olat.lms.search.indexer.OlatFullIndexer;
import org.olat.lms.search.indexer.SubLevelIndexer;
import org.olat.presentation.collaboration.CollaborationTools;
import org.olat.presentation.collaboration.CollaborationToolsFactory;
import org.olat.presentation.group.run.BusinessGroupMainRunController;

/**
 * Index all group folders.
 * 
 * @author Christian Guretzki
 */
public class GroupFolderIndexer extends SubLevelIndexer<BusinessGroup> {

    // Must correspond with org/olat/presentation/search/_i18n/LocalStrings_xx.properties
    // Do not use '_' because Lucene has problems with it
    private static final String TYPE = "type.group.folder";

    @Override
    public void doIndex(final SearchResourceContext parentResourceContext, final BusinessGroup businessGroup, final OlatFullIndexer indexWriter) throws IOException,
            FolderIndexerTimeoutException {

        final VFSContainer rootContainer = new OlatRootFolderImpl(getFolderRelPath(businessGroup), null);

        final SearchResourceContext forumSearchResourceContext = new SearchResourceContext(parentResourceContext);
        forumSearchResourceContext.setBusinessControlFor(BusinessGroupMainRunController.ORES_TOOLFOLDER);
        forumSearchResourceContext.setDocumentType(TYPE);
        forumSearchResourceContext.setParentContextType(GroupDocument.TYPE);
        forumSearchResourceContext.setParentContextName(businessGroup.getName());
        FolderIndexer.indexVFSContainer(forumSearchResourceContext, rootContainer, indexWriter, FolderIndexerAccess.FULL_ACCESS);
    }

    private String getFolderRelPath(final BusinessGroup businessGroup) {
        return CollaborationTools.getFolderRelPath(businessGroup);
    }

    @Override
    public boolean checkAccess(final ContextEntry groupContextEntry, final BusinessControl businessControl, final Identity identity, final Roles roles) {
        // check if tool is enabled
        return CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(groupContextEntry.getOLATResourceable())
                .isToolEnabled(CollaborationTools.TOOL_FOLDER);
    }

    @Override
    public String getSupportedTypeName() {
        return BusinessGroupMainRunController.ORES_TOOLFOLDER.getResourceableTypeName();
    }

}
