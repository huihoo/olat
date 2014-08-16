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
package org.olat.lms.course.imports;

import java.io.File;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Constants;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.portfolio.structure.EPStructuredMapTemplate;
import org.olat.data.portfolio.structure.PortfolioStructure;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryEntryImportExport;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.repository.handlers.PortfolioRepositoryHandler;
import org.olat.lms.repository.handlers.RepositoryHandler;
import org.olat.lms.repository.handlers.RepositoryHandlerFactory;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;
import org.springframework.stereotype.Component;

/**
 * TODO: Class Description for ImportPortfolioEBL
 * 
 * <P>
 * Initial Date: 02.09.2011 <br>
 * 
 * @author lavinia
 */
@Component
public class ImportPortfolioEBL {

    private static final Logger log = LoggerHelper.getLogger();

    private ImportPortfolioEBL() {

    }

    /**
     * Import a referenced repository entry.
     * 
     * @param importExport
     * @param node
     * @param importMode
     *            Type of import.
     * @param keepSoftkey
     *            If true, no new softkey will be generated.
     * @param owner
     * @return
     */
    // TODO: ORID-1007, Code Duplication (see RepositoryEBL, ImportReferencesEBL)
    public RepositoryEntry doImport(final RepositoryEntryImportExport importExport, final CourseNode node, final boolean keepSoftkey, final Identity owner) {
        final File fExportedFile = importExport.getExportedFile();
        final PortfolioStructure structure = PortfolioRepositoryHandler.getAsObject(fExportedFile);
        if (structure == null) {
            log.warn("Error adding portfolio map resource during repository reference import: " + importExport.getDisplayName());
            return null;
        }

        final EPFrontendManager ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean(EPFrontendManager.class);
        final PortfolioStructure map = ePFMgr.importPortfolioMapTemplate(structure, owner);

        // create repository entry
        final RepositoryService rm = RepositoryServiceImpl.getInstance();
        final RepositoryEntry importedRepositoryEntry = rm.createRepositoryEntryInstance(owner.getName());
        importedRepositoryEntry.setDisplayname(importExport.getDisplayName());
        importedRepositoryEntry.setResourcename(importExport.getResourceName());
        importedRepositoryEntry.setDescription(importExport.getDescription());
        if (keepSoftkey) {
            final String theSoftKey = importExport.getSoftkey();
            if (rm.lookupRepositoryEntryBySoftkey(theSoftKey, false) != null) {
                /*
                 * keepSoftKey == true -> is used for importing in unattended mode. "Importing and keeping the soft key" only works if there is not an already existing
                 * soft key. In the case both if's are taken the respective IMS resource is not imported. It is important to be aware that the resource which triggered
                 * the import process still keeps the soft key reference, and thus points to the already existing resource.
                 */
                return null;
            }
            importedRepositoryEntry.setSoftkey(importExport.getSoftkey());
        }

        // Set the resource on the repository entry.
        final OLATResource ores = OLATResourceManager.getInstance().findOrPersistResourceable(map.getOlatResource());
        importedRepositoryEntry.setOlatResource(ores);
        final RepositoryHandler rh = RepositoryHandlerFactory.getInstance().getRepositoryHandler(importedRepositoryEntry);
        importedRepositoryEntry.setCanLaunch(rh.supportsLaunch(importedRepositoryEntry));

        SecurityGroup newGroup = createOrGetOwnerGroupWithIdentity(owner, map);
        importedRepositoryEntry.setOwnerGroup(newGroup);
        rm.saveRepositoryEntry(importedRepositoryEntry);

        if (!keepSoftkey) {
            node.setRepositoryReference(importedRepositoryEntry);
        }

        return importedRepositoryEntry;
    }

    /**
     * @param owner
     * @param map
     * @return
     */
    private SecurityGroup createOrGetOwnerGroupWithIdentity(final Identity owner, final PortfolioStructure map) {
        // create security group
        final BaseSecurity securityManager = (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
        SecurityGroup newGroup;
        if (map instanceof EPStructuredMapTemplate) {
            newGroup = ((EPStructuredMapTemplate) map).getOwnerGroup();
        } else {
            newGroup = securityManager.createAndPersistSecurityGroup();
        }

        // member of this group may modify member's membership
        securityManager.createAndPersistPolicy(newGroup, Constants.PERMISSION_ACCESS, newGroup);
        // members of this group are always authors also
        securityManager.createAndPersistPolicy(newGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_AUTHOR);
        securityManager.addIdentityToSecurityGroup(owner, newGroup);
        return newGroup;
    }

}
