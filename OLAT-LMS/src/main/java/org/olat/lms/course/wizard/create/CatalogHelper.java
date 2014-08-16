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
 * Technische Universitaet Chemnitz Lehrstuhl Technische Informatik Author Marcel Karras (toka@freebits.de) Author Norbert Englisch
 * (norbert.englisch@informatik.tu-chemnitz.de) Author Sebastian Fritzsche (seb.fritzsche@googlemail.com)
 */
package org.olat.lms.course.wizard.create;

import java.util.List;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.catalog.CatalogEntry;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.catalog.CatalogService;
import org.olat.lms.course.ICourse;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.framework.core.components.tree.GenericTreeModel;
import org.olat.presentation.framework.core.components.tree.GenericTreeNode;
import org.olat.presentation.framework.core.components.tree.TreeModel;
import org.olat.presentation.framework.core.components.tree.TreeNode;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Helper class for common catalog operations that are not existent in the {@link org.olat.data.catalog.CatalogDaoImpl} yet.
 * <P>
 * Initial Date: 12.12.2008 <br>
 * 
 * @author Marcel Karras (toka@freebits.de)
 */
public class CatalogHelper {

    /**
     * Add a persisted course to the given catalog entry.
     * 
     * @param course
     *            course object
     * @param catEntry
     *            catalog entry
     */
    public static final void addCourseToCatalogEntry(final ICourse course, final CatalogEntry catEntry) {
        final OLATResource ores = OLATResourceManager.getInstance().findResourceable(course.getResourceableId(), course.getResourceableTypeName());
        final RepositoryService rm = RepositoryServiceImpl.getInstance();
        final RepositoryEntry re = rm.lookupRepositoryEntry(ores, true);
        final CatalogService catalogService = CoreSpringFactory.getBean(CatalogService.class);
        final CatalogEntry newLinkNotPersistedYet = catalogService.createCatalogEntry();
        newLinkNotPersistedYet.setName(re.getDisplayname());
        newLinkNotPersistedYet.setDescription(re.getDescription());
        newLinkNotPersistedYet.setRepositoryEntry(re);
        newLinkNotPersistedYet.setType(CatalogEntry.TYPE_LEAF);
        newLinkNotPersistedYet.setOwnerGroup(getBaseSecurity().createAndPersistSecurityGroup());
        catalogService.addCatalogEntry(catEntry, newLinkNotPersistedYet);
    }

    private static BaseSecurity getBaseSecurity() {
        return (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
    }

    @SuppressWarnings("unchecked")
    protected static final TreeNode buildCatalogNode(final CatalogEntry rootEntry) {
        final CatalogService catalogService = CoreSpringFactory.getBean(CatalogService.class);
        final List<CatalogEntry> children = catalogService.getChildrenOf(rootEntry);

        final GenericTreeNode ctn = new GenericTreeNode(rootEntry.getName(), rootEntry);
        ctn.setAccessible(true);

        for (int i = 0; i < children.size(); i++) {
            // add child itself
            final CatalogEntry cchild = children.get(i);
            if (cchild.getType() == CatalogEntry.TYPE_NODE) {
                final TreeNode ctchild = buildCatalogNode(cchild);
                ((GenericTreeNode) ctchild).setAccessible(true);
                ctn.addChild(ctchild);
            }
        }

        return ctn;

    }

    /**
     * Map the OLAT catalog structure to a new tree model.
     * 
     * @param rootEntry
     *            root catalog entry
     * @return tree model with catalog structure
     */
    public static final TreeModel createCatalogTree(final CatalogEntry rootEntry) {
        final GenericTreeModel tm = new GenericTreeModel();
        tm.setRootNode(buildCatalogNode(rootEntry));
        return tm;
    }

    /**
     * Create a path like "/19234817/19234819" from a specific catalog entry.
     * 
     * @param catalogEntry
     * @return
     */
    public static final String getPath(final CatalogEntry catalogEntry) {
        final StringBuffer path = new StringBuffer();
        CatalogEntry gce = catalogEntry;
        while (gce.getParent() != null) {
            path.insert(0, "/" + gce.getKey().toString());
            gce = gce.getParent();
        }
        path.insert(0, "/" + gce.getKey().toString());
        return path.toString();
    }
}
