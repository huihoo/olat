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
package org.olat.data.portfolio.structure;

import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.group.BusinessGroup;
import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * Interface extracted from EPStructureManager.
 * 
 * <P>
 * Initial Date: 28.06.2011 <br>
 * 
 * @author lavinia
 */
public interface PortfolioStructureDao {

    /**
     * Return the list of artefacts glued to this structure element
     * 
     * @param structure
     * @return A list of artefacts
     */
    public List<AbstractArtefact> getArtefacts(final PortfolioStructure structure);

    public List<PortfolioStructureMap> getOpenStructuredMapAfterDeadline();

    public List<PortfolioStructure> getStructureElements(final int firstResult, final int maxResults, final ElementType... types);

    public List<PortfolioStructure> getStructureElementsForUser(final Identity ident, final ElementType... types);

    /**
     * Check if the identity is owner of the map
     * 
     * @param identity
     * @param ores
     * @return
     */
    public boolean isMapOwner(final Identity identity, final OLATResourceable ores);

    /**
     * Check if the identity is owner or is in a valid policy
     * 
     * @param identity
     * @param ores
     * @return
     */
    public boolean isMapVisible(final Identity identity, final OLATResourceable ores);

    public List<PortfolioStructure> getStructureElementsFromOthers(final Identity ident, final Identity choosenOwner, final ElementType... types);

    public List<PortfolioStructure> getStructureElementsFromOthersWithoutPublic(final Identity ident, final Identity choosenOwner, final ElementType... types);

	public List<PortfolioStructure> getStructureElementsFromOthersLimited(final Identity ident, final Identity choosenOwner ,final int limitFrom, final int limitTo,final ElementType... types);

    public List<PortfolioStructure> getReferencedMapsForArtefact(final AbstractArtefact artefact);

    public List<PortfolioStructure> getAllReferencesForArtefact(final AbstractArtefact artefact);

    /**
     * Return the list of artefacts glued to this structure element
     * 
     * @param structure
     * @param firstResult
     * @param maxResults
     * @return
     */
    public List<AbstractArtefact> getArtefacts(final PortfolioStructure structure, final int firstResult, final int maxResults);

    /**
     * Return the number of artefacts hold by a structure element
     * 
     * @param structure
     * @return
     */
    public int countArtefacts(final PortfolioStructure structure);

    /**
     * Count all artefacts (links) in a map
     */
    public int countArtefactsRecursively(final PortfolioStructure structure);

    public int countArtefactsRecursively(final PortfolioStructure structure, int res);

    public boolean isArtefactInStructure(final AbstractArtefact artefact, final PortfolioStructure structure);

    /**
     * Number of children
     */
    public int countStructureChildren(final PortfolioStructure structure);

    /**
     * Retrieve the children structures
     * 
     * @param structure
     * @return
     */
    public List<PortfolioStructure> loadStructureChildren(final PortfolioStructure structure);

    /**
     * @param structure
     * @param firstResult
     * @param maxResults
     * @return
     */
    public List<PortfolioStructure> loadStructureChildren(final PortfolioStructure structure, final int firstResult, final int maxResults);

    /**
     * Retrieve the parent of the structure
     * 
     * @param structure
     * @return
     */
    public PortfolioStructure loadStructureParent(final PortfolioStructure structure);

    /**
     * Add a link between a structure element and an artefact
     * 
     * @param author
     * @param artefact
     * @param structure
     * @return
     */
    public boolean addArtefactToStructure(final Identity author, final AbstractArtefact artefact, final PortfolioStructure structure);

    public boolean moveArtefactFromStructToStruct(final AbstractArtefact artefact, final PortfolioStructure oldParStruct, final PortfolioStructure newParStruct);

    /**
     * Check the collect restriction against the structure element
     * 
     * @param structure
     * @return
     */
    public boolean checkCollectRestriction(final PortfolioStructure structure);

    /**
     * Remove a link between a structure element and an artefact.
     * 
     * @param author
     *            The author of the link
     * @param artefact
     *            The artefact to link
     * @param structure
     *            The structure element
     * @return The link
     */
    public PortfolioStructure removeArtefactFromStructure(final AbstractArtefact artefact, final PortfolioStructure structure);

    /**
     * Move up an artefact in the list
     * 
     * @param structure
     * @param artefact
     */
    public void moveUp(final PortfolioStructure structure, final AbstractArtefact artefact);

    /**
     * Move down an artefact in the list
     * 
     * @param structure
     * @param artefact
     */
    public void moveDown(final PortfolioStructure structure, final AbstractArtefact artefact);

    /**
     * Add a child structure to the parent structure.
     * 
     * @param parentStructure
     * @param childStructure
     */
    public void addStructureToStructure(PortfolioStructure parentStructure, final PortfolioStructure childStructure);
	public void addStructureToStructure(PortfolioStructure parentStructure, final PortfolioStructure childStructure, int pos);

	public boolean moveStructureToNewParentStructure(final PortfolioStructure structToBeMvd, final PortfolioStructure oldParStruct,
			final PortfolioStructure newParStruct);

    public void deleteRootStructure(final PortfolioStructure rootStructure);

    /**
     * Remove a child structure from its parent structure.
     * 
     * @param parentStructure
     * @param childStructure
     */

    // this has to be done recursively for pages, structs also!
    // also remove the artefacts from each!
    public void removeStructure(final PortfolioStructure parentStructure, final PortfolioStructure childStructure);

    /**
     * This method is only for templates.
     * 
     * @param res
     */
    public void deletePortfolioMapTemplate(final OLATResourceable res);

    public void removeStructureRecursively(PortfolioStructure struct);

    /**
     * Move a structure element up in the list
     * 
     * @param parentStructure
     * @param childStructure
     */
    public void moveUp(final PortfolioStructure parentStructure, final PortfolioStructure childStructure);

    /**
     * Move a structure element down in the list and save the parent and the list
     * 
     * @param parentStructure
     * @param childStructure
     */
    public void moveDown(final PortfolioStructure parentStructure, final PortfolioStructure childStructure);

    public void copyStructureRecursively(final PortfolioStructure source, final PortfolioStructure target, final boolean withArtefacts);

    /**
     * Sync the tree structure recursively with or without artefacts
     * 
     * @param sourceEl
     * @param targetEl
     * @param withArtefacts
     */
    public void syncStructureRecursively(final PortfolioStructure source, final PortfolioStructure target, final boolean withArtefacts);

    public boolean isTemplateInUse(final PortfolioStructureMap template, final OLATResourceable targetOres, final String targetSubPath, final String targetBusinessPath);

    public PortfolioStructureMap loadPortfolioStructuredMap(final Identity identity, final PortfolioStructureMap template, final OLATResourceable targetOres,
            final String targetSubPath, final String targetBusinessPath);

    /**
     * Load the repository entry of a template with the map key
     * 
     * @param key
     *            The template key
     * @return The repository entry
     */
    public RepositoryEntry loadPortfolioRepositoryEntryByMapKey(final Long key);

    /**
     * @param olatResourceable
     *            cannot be null
     * @return The structure element or null if not found
     */
    public PortfolioStructure loadPortfolioStructure(final OLATResourceable olatResourceable);

    /**
     * Load a portfolio structure by its primary key
     * 
     * @param key
     *            cannot be null
     * @return The structure element or null if not found
     */
    // FIXME: epf: SR: error loading structures without olatresource!
    public PortfolioStructure loadPortfolioStructureByKey(final Long key);

    /**
     * Create a basic structure element
     * 
     * @param title
     * @param description
     * @return The structure element
     */
    public PortfolioStructure createPortfolioStructure(final PortfolioStructure root, final String title, final String description);

    /**
     * Create a page element
     * 
     * @param title
     * @param description
     * @return The structure element
     */
    public PortfolioStructure createPortfolioPage(final PortfolioStructure root, final String title, final String description);

    public PortfolioStructureMap createPortfolioStructuredMap(final PortfolioStructureMap template, final Identity identity, final String title,
            final String description, final OLATResourceable targetOres, final String targetSubPath, final String targetBusinessPath);

    public PortfolioStructureMap createPortfolioDefaultMap(final Identity identity, final String title, final String description);

    public PortfolioStructureMap createPortfolioDefaultMap(final BusinessGroup group, final String title, final String description);

    /**
     * Create a map template, create an OLAT resource and a repository entry with a security group of type owner to the repository and add the identity has an owner.
     * 
     * @param identity
     * @param title
     * @param description
     * @return The structure element
     */
    public PortfolioStructureMap createPortfolioMapTemplate(final Identity identity, final String title, final String description);

    /**
     * Import the structure.
     * 
     * @param root
     * @param identity
     * @return
     */
    public PortfolioStructureMap importPortfolioMapTemplate(final PortfolioStructure root, final Identity identity);

    /**
     * Create an OLAT Resource with the type of a template map.
     * 
     * @return
     */
    public OLATResource createPortfolioMapTemplateResource();

    /**
     * Create a template map with the given repsoitory entry and olat resource (in the repository entry). The repository entry must already be persisted.
     * 
     * @param identity
     * @param entry
     * @return
     */
    public PortfolioStructureMap createAndPersistPortfolioMapTemplateFromEntry(final Identity identity, final RepositoryEntry entry);

    /**
     * Add an author to the repository entry linked to the map
     * 
     * @param map
     * @param author
     */
    public void addAuthor(final PortfolioStructureMap map, final Identity author);

    /**
     * Remove an author to repository entry linked to the map
     * 
     * @param map
     * @param author
     */
    public void removeAuthor(final PortfolioStructureMap map, final Identity author);

    /**
     * Add or update a restriction to the collection of artefacts for a given structure element
     * 
     * @param structure
     * @param artefactType
     * @param restriction
     * @param amount
     */
    public void addCollectRestriction(final PortfolioStructure structure, final String artefactType, final String restriction, final int amount);

    public void submitMap(final EPStructuredMap map);

    public void savePortfolioStructure(final PortfolioStructure portfolioStructure);

    public boolean setReflexionForArtefactToStructureLink(final AbstractArtefact artefact, final PortfolioStructure structure, final String reflexion);

    public String getReflexionForArtefactToStructureLink(final AbstractArtefact artefact, final PortfolioStructure structure);
    
    public Integer[] getRestrictionStatisticsOfMap(PortfolioStructure structureMap, int done, int todo);

public boolean reOrderStructures(PortfolioStructure parent, PortfolioStructure orderSubject, int orderDest);


}