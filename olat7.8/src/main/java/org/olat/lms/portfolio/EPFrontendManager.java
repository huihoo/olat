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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.lms.portfolio;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Constants;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Policy;
import org.olat.data.basesecurity.Roles;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.group.BusinessGroup;
import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.data.portfolio.artefact.ArtefactDao;
import org.olat.data.portfolio.artefact.EPFilterSettings;
import org.olat.data.portfolio.structure.EPPage;
import org.olat.data.portfolio.structure.EPStructureElement;
import org.olat.data.portfolio.structure.EPStructuredMap;
import org.olat.data.portfolio.structure.EPTargetResource;
import org.olat.data.portfolio.structure.ElementType;
import org.olat.data.portfolio.structure.PortfolioStructure;
import org.olat.data.portfolio.structure.PortfolioStructureDao;
import org.olat.data.portfolio.structure.PortfolioStructureMap;
import org.olat.data.resource.OLATResource;
import org.olat.data.tagging.Tag;
import org.olat.data.tagging.TaggingDao;
import org.olat.lms.commons.fileresource.BlogFileResource;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.AssessmentManager;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.lms.portfolio.artefacthandler.EPArtefactHandler;
import org.olat.lms.search.SearchResults;
import org.olat.lms.search.document.AbstractOlatDocument;
import org.olat.lms.search.document.ResultDocument;
import org.olat.lms.search.indexer.identity.PortfolioArtefactIndexer;
import org.olat.lms.search.searcher.SearchClient;
import org.olat.lms.security.IdentityEnvironment;
import org.olat.presentation.webfeed.blog.portfolio.LiveBlogArtefactHandler;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.Coordinator;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.SyncerCallback;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Manager for common used tasks for ePortfolio. Should be used for all calls from controllers. will itself use all other managers to manipulate artefacts or
 * structureElements and policies.
 * <P>
 * Initial Date: 11.06.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPFrontendManager extends BasicManager {

    private static final Logger log = LoggerHelper.getLogger();

    static final OLATResourceable ORES_MAPOWNER = OresHelper.createOLATResourceableType("EPStructureManager", "EPOwner");

    private final Coordinator coordinator;
    @Autowired
    private BaseSecurity securityManager;
    @Autowired
    private ArtefactDao artefactManager;
    @Autowired
    private PortfolioStructureDao structureManager;
    @Autowired
    private TaggingDao taggingManager;
    @Autowired
    private DB dbInstance;

    private SearchClient searchClient;
    private final EPSettingsManager settingsManager;
    @Autowired
    private EPPolicyManager policyManager;
    @Autowired
    private PortfolioModule portfolioModule;

    /**
     * [for Spring]
     */
    private EPFrontendManager(final EPSettingsManager settingsManager, final CoordinatorManager coordinatorManager) {
        this.coordinator = coordinatorManager.getCoordinator();
        this.settingsManager = settingsManager;
    }

    /**
     * [used by Spring]
     * 
     * @param searchClient
     */
    public void setSearchClient(final SearchClient searchClient) {
        this.searchClient = searchClient;
    }

    /**
     * Create and persist an artefact of the given type
     * 
     * @param type
     * @return The persisted artefact
     */
    public AbstractArtefact createAndPersistArtefact(final Identity identity, final String type) {
        final EPArtefactHandler<?> handler = portfolioModule.getArtefactHandler(type);
        if (handler != null && handler.isEnabled()) {
            final AbstractArtefact artefact = handler.createArtefact();
            artefact.setAuthor(identity);

            return artefactManager.saveArtefact(artefact);
        } else {
            return null;
        }
    }

    /**
     * Persists the artefact and returns the new version
     * 
     * @param artefact
     * @return The last version of the artefact
     */
    public AbstractArtefact updateArtefact(final AbstractArtefact artefact) {
        return artefactManager.updateArtefact(artefact);
    }

    /**
     * delete an artefact and also its vfs-artefactContainer
     * 
     * @param artefact
     */
    public void deleteArtefact(AbstractArtefact artefact) {
        final List<PortfolioStructure> linksToArtefact = structureManager.getAllReferencesForArtefact(artefact);
        for (final PortfolioStructure portfolioStructure : linksToArtefact) {
            structureManager.removeArtefactFromStructure(artefact, portfolioStructure);
        }
        // load again as session might be closed between
        artefact = artefactManager.loadArtefactByKey(artefact.getKey());

        // wrap concrete artefact as abstract-artefact to get the correct resName for the tag
        final OLATResourceable artefactOres = OresHelper.createOLATResourceableInstance(AbstractArtefact.class, artefact.getKey());
        taggingManager.deleteTags(artefactOres, null, null);
        artefactManager.deleteArtefact(artefact);
    }

    public boolean isArtefactClosed(final AbstractArtefact artefact) {
        return artefactManager.isArtefactClosed(artefact);
    }

    public PortfolioStructure removeArtefactFromStructure(final AbstractArtefact artefact, final PortfolioStructure structure) {
        return structureManager.removeArtefactFromStructure(artefact, structure);
    }

    /**
     * Create and persist a link between a structure element and an artefact.
     * 
     * @param author
     *            The author of the link
     * @param artefact
     *            The artefact to link
     * @param structure
     *            The structure element
     * @return The link
     */
    public boolean addArtefactToStructure(final Identity author, final AbstractArtefact artefact, final PortfolioStructure structure) {
        return structureManager.addArtefactToStructure(author, artefact, structure);
    }

    /**
     * move artefact from old to new structure do so by removing and re-adding to new target
     * 
     * @param artefact
     * @param oldParStruct
     * @param newParStruct
     * @return true if adding was successful
     */
    public boolean moveArtefactFromStructToStruct(final AbstractArtefact artefact, final PortfolioStructure oldParStruct, final PortfolioStructure newParStruct) {
        return structureManager.moveArtefactFromStructToStruct(artefact, oldParStruct, newParStruct);
    }

    /**
     * move a structure to a new parent-structure and removes old link
     * 
     * @param structToBeMvd
     * @param oldParStruct
     * @param newParStruct
     * @return true if no exception occured
     */
    public boolean moveStructureToNewParentStructure(final PortfolioStructure structToBeMvd, final PortfolioStructure oldParStruct, final PortfolioStructure newParStruct) {
        return structureManager.moveStructureToNewParentStructure(structToBeMvd, oldParStruct, newParStruct);
    }

    public boolean moveStructureToNewParentStructure(final PortfolioStructure structToBeMvd, final PortfolioStructure oldParStruct,
            final PortfolioStructure newParStruct, int destinationPos) {
        if (structToBeMvd == null || oldParStruct == null || newParStruct == null) {
            throw new NullPointerException();
        }
        try { // try catch, as used in d&d TOC-tree, should still continue on
              // error
            structureManager.removeStructure(oldParStruct, structToBeMvd);
            structureManager.addStructureToStructure(newParStruct, structToBeMvd, destinationPos);
        } catch (final Exception e) {
            log.error("could not move structure " + structToBeMvd.getKey() + " from " + oldParStruct.getKey() + " to " + newParStruct.getKey(), e);
            return false;
        }
        return true;
    }

    /**
     * set the reflexion for the link structureElement <-> artefact this can be a different reflexion than the one of the artefact. Reflects why the artefact was added to
     * this structure!
     * 
     * @param artefact
     * @param structure
     * @param reflexion
     * @return
     */
    public boolean setReflexionForArtefactToStructureLink(final AbstractArtefact artefact, final PortfolioStructure structure, final String reflexion) {
        return structureManager.setReflexionForArtefactToStructureLink(artefact, structure, reflexion);
    }

    /**
     * get the reflexion set on the link structureElement <-> artefact this can be a different reflexion than the one of the artefact. Reflects why the artefact was added
     * to this structure!
     * 
     * @param artefact
     * @param structure
     * @return String reflexion
     */
    public String getReflexionForArtefactToStructureLink(final AbstractArtefact artefact, final PortfolioStructure structure) {
        return structureManager.getReflexionForArtefactToStructureLink(artefact, structure);
    }

    /**
     * counts amount of artefact in all structures and every child element
     * 
     * @param structure
     * @return
     */
    public int countArtefactsRecursively(final PortfolioStructure structure) {
        return structureManager.countArtefactsRecursively(structure, 0);
    }

    public int countArtefactsInMap(final PortfolioStructureMap map) {
        return structureManager.countArtefactsRecursively(map);
    }

    /**
     * looks if the given artefact exists in the PortfolioStructure
     * 
     * @param artefact
     * @param structure
     * @return
     */
    public boolean isArtefactInStructure(final AbstractArtefact artefact, final PortfolioStructure structure) {
        return structureManager.isArtefactInStructure(artefact, structure);
    }

    /**
     * @see org.olat.data.portfolio.artefact.ArtefactDao#loadArtefactsByBusinessPath(java.lang.String)
     */
    public List<AbstractArtefact> loadArtefactsByBusinessPath(final String businessPath) {
        return artefactManager.loadArtefactsByBusinessPath(businessPath);
    }

    /**
     * load all artefacts with given businesspath from given identity this mostly is just to lookup for existance of already collected artefacts from same source
     * 
     * @param businessPath
     * @param author
     * @return
     */
    public List<AbstractArtefact> loadArtefactsByBusinessPath(final String businessPath, final Identity author) {
        return artefactManager.loadArtefactsByBusinessPath(businessPath, author);
    }

    /**
     * List artefacts for indexing
     * 
     * @param author
     *            (optional)
     * @param firstResult
     *            (optional)
     * @param maxResults
     *            (optional)
     * @return
     */
    public List<AbstractArtefact> getArtefacts(final Identity author, final int firstResult, final int maxResults) {
        return artefactManager.getArtefacts(author, null, firstResult, maxResults);
    }

    /**
     * Load the artefact by its primary key
     * 
     * @param key
     *            The primary key
     * @return The artefact or null if nothing found
     */
    public AbstractArtefact loadArtefactByKey(final Long key) {
        return artefactManager.loadArtefactByKey(key);
    }

    /**
     * get the users choice of attributes or a default
     * 
     * @return
     */
    public Map<String, Boolean> getArtefactAttributeConfig(final Identity ident) {
        return settingsManager.getArtefactAttributeConfig(ident);
    }

    /**
     * persist the users chosen attributes to show as a property
     * 
     * @param ident
     * @param artAttribConfig
     */
    public void setArtefactAttributeConfig(final Identity ident, final Map<String, Boolean> artAttribConfig) {
        settingsManager.setArtefactAttributeConfig(ident, artAttribConfig);
    }

    /**
     * get all persisted filters from a given user
     * 
     * @param ident
     * @return filtersettings or list with an empty filter, if none were found
     */
    public List<EPFilterSettings> getSavedFilterSettings(final Identity ident) {
        return settingsManager.getSavedFilterSettings(ident);
    }

    /**
     * persist users filter settings as property, only save such with a name
     * 
     * @param ident
     * @param filterList
     */
    public void setSavedFilterSettings(final Identity ident, final List<EPFilterSettings> filterList) {
        settingsManager.setSavedFilterSettings(ident, filterList);
    }

    /**
     * remove a given filter from users list
     * 
     * @param ident
     * @param filterName
     */
    public void deleteFilterFromUsersList(final Identity ident, final String filterID) {
        settingsManager.deleteFilterFromUsersList(ident, filterID);
    }

    /**
     * get the last selected PortfolioStructure of this user
     * 
     * @param ident
     *            Identity
     * @return the loaded PortfolioStructure
     */
    public PortfolioStructure getUsersLastUsedPortfolioStructure(final Identity ident) {
        final Long structKey = settingsManager.getUsersLastUsedPortfolioStructureKey(ident);
        if (structKey != null) {
            final PortfolioStructure struct = structureManager.loadPortfolioStructureByKey(structKey);
            return struct;
        }
        return null;
    }

    /**
     * get the users prefered viewing mode for artefacts (either table / preview)
     * 
     * @param ident
     * @return
     */
    public String getUsersPreferedArtefactViewMode(final Identity ident, final String context) {
        return settingsManager.getUsersPreferedArtefactViewMode(ident, context);
    }

    /**
     * persist the users prefered viewing mode for artefacts (either table / preview)
     * 
     * @param ident
     * @param preferedMode
     */
    public void setUsersPreferedArtefactViewMode(final Identity ident, final String preferedMode, final String context) {
        settingsManager.setUsersPreferedArtefactViewMode(ident, preferedMode, context);
    }

    /**
     * persist the last uses PortfolioStructure to use it later on
     * 
     * @param ident
     *            Identity
     * @param struct
     */
    public void setUsersLastUsedPortfolioStructure(final Identity ident, final PortfolioStructure struct) {
        settingsManager.setUsersLastUsedPortfolioStructure(ident, struct);
    }

    /**
     * returns an array of tags for given artefact
     * 
     * @param artefact
     * @return null if none are found
     */
    public List<String> getArtefactTags(final AbstractArtefact artefact) {
        // wrap concrete artefact as abstract-artefact to get the correct resName for the tag
        if (artefact.getKey() == null) {
            return null;
        }
        final OLATResourceable artefactOres = OresHelper.createOLATResourceableInstance(AbstractArtefact.class, artefact.getKey());
        final List<String> tags = taggingManager.getTagsAsString(null, artefactOres, null, null);
        return tags;
    }

    /**
     * add a tag to an artefact (will save a tag pointing to this artefact)
     * 
     * @param identity
     * @param artefact
     * @param tag
     */
    public void setArtefactTag(final Identity identity, final AbstractArtefact artefact, final String tag) {
        // wrap concrete artefact as abstract-artefact to get the correct resName for the tag
        final OLATResourceable artefactOres = OresHelper.createOLATResourceableInstance(AbstractArtefact.class, artefact.getKey());
        taggingManager.createAndPersistTag(identity, tag, artefactOres, null, null);
    }

    /**
     * add a List of tags to an artefact
     * 
     * @param identity
     * @param artefact
     * @param tags
     */
    public void setArtefactTags(final Identity identity, final AbstractArtefact artefact, final List<String> tags) {
        if (tags == null) {
            return;
        }
        // wrap concrete artefact as abstract-artefact to get the correct resName for the tag
        final OLATResourceable artefactOres = OresHelper.createOLATResourceableInstance(AbstractArtefact.class, artefact.getKey());
        final List<Tag> oldTags = taggingManager.loadTagsForResource(artefactOres, null, null);
        final List<String> oldTagStrings = new ArrayList<String>();
        final List<String> tagsToAdd = new ArrayList<String>(tags.size());
        tagsToAdd.addAll(tags);
        if (oldTags != null) { // there might be no tags yet
            for (final Tag oTag : oldTags) {
                if (tags.contains(oTag.getTag())) {
                    // still existing, nothing to do
                    oldTagStrings.add(oTag.getTag());
                    tagsToAdd.remove(oTag.getTag());
                } else {
                    // tag was deleted, remove it
                    taggingManager.deleteTag(oTag);
                }
            }
        }
        // look for all given tags, add the ones yet missing
        for (final String tag : tagsToAdd) {
            if (StringHelper.containsNonWhitespace(tag)) {
                taggingManager.createAndPersistTag(identity, tag, artefactOres, null, null);
            }
        }
    }

    /**
     * get all maps wherein (or in sub-structures) the given artefact is linked.
     * 
     * @param artefact
     * @return
     */
    public List<PortfolioStructure> getReferencedMapsForArtefact(final AbstractArtefact artefact) {
        return structureManager.getReferencedMapsForArtefact(artefact);
    }

    /**
     * get all artefacts for the given identity this represents the artefact pool
     * 
     * @param ident
     * @return
     */
    public List<AbstractArtefact> getArtefactPoolForUser(final Identity ident) {
        return artefactManager.getArtefactPoolForUser(ident);
    }

    /**
     * This is an optimized method to filter a list of artefact by tags and return the tags of this list of artefacts. This prevent to search two times or more the list
     * of tags of an artefact.
     * 
     * @param identity
     * @param tags
     * @return the filtered artefacts and their tags
     */
    public EPArtefactTagCloud getArtefactsAndTagCloud(final Identity identity, final List<String> tags) {
        final List<AbstractArtefact> artefacts = getArtefactPoolForUser(identity);
        final EPFilterSettings filterSettings = new EPFilterSettings();
        filterSettings.setTagFilter(tags);

        final Set<String> newTags = new HashSet<String>();
        filterArtefactsByTags(artefacts, filterSettings, newTags);

        return new EPArtefactTagCloud(artefacts, newTags);
    }

    /**
     * filter the provided list of artefacts with different filters
     * 
     * @param allArtefacts
     *            the list to manipulate on
     * @param filterSettings
     *            Settings for the filter to work on
     * @return
     */
    public List<AbstractArtefact> filterArtefactsByFilterSettings(final EPFilterSettings filterSettings, final Identity identity, final Roles roles) {
        final List<Long> artefactKeys = fulltextSearchAfterArtefacts(filterSettings, identity, roles);
        if (artefactKeys == null || artefactKeys.isEmpty()) {
            final List<AbstractArtefact> allArtefacts = artefactManager.getArtefactPoolForUser(identity);
            return filterArtefactsByFilterSettings(allArtefacts, filterSettings);
        }

        final List<AbstractArtefact> artefacts = artefactManager.getArtefacts(identity, artefactKeys, 0, 500);
        // remove the text-filter when the lucene-search got some results before
        final EPFilterSettings settings = filterSettings.cloneAfterFullText();
        return filterArtefactsByFilterSettings(artefacts, settings);
    }

    private List<Long> fulltextSearchAfterArtefacts(final EPFilterSettings filterSettings, final Identity identity, final Roles roles) {
        final String query = filterSettings.getTextFilter();
        if (StringHelper.containsNonWhitespace(query)) {
            try {
                final List<String> queries = new ArrayList<String>();
                appendAnd(queries, AbstractOlatDocument.RESERVED_TO, ":\"", identity.getKey().toString(), "\"");
                appendAnd(queries, "(", AbstractOlatDocument.DOCUMENTTYPE_FIELD_NAME, ":(", PortfolioArtefactIndexer.TYPE, "*))");
                final SearchResults searchResults = searchClient.doSearch(query, queries, identity, roles, 0, 1000, false);

                final List<Long> keys = new ArrayList<Long>();
                if (searchResults != null) {
                    final String marker = AbstractArtefact.class.getSimpleName();
                    for (final ResultDocument doc : searchResults.getList()) {
                        final String businessPath = doc.getResourceUrl();
                        int start = businessPath.indexOf(marker);
                        if (start > 0) {
                            start += marker.length() + 1;
                            final int stop = businessPath.indexOf(']', start);
                            if (stop < businessPath.length()) {
                                final String keyStr = businessPath.substring(start, stop);
                                try {
                                    keys.add(Long.parseLong(keyStr));
                                } catch (final Exception e) {
                                    log.error("Not a primary key: " + keyStr, e);
                                }
                            }
                        }
                    }
                }
                return keys;
            } catch (final Exception e) {
                log.error("", e);
                return Collections.emptyList();
            }
        } else {
            return Collections.emptyList();
        }
    }

    private void appendAnd(final List<String> queries, final String... strings) {
        final StringBuilder query = new StringBuilder();
        for (final String string : strings) {
            query.append(string);
        }

        if (query.length() > 0) {
            queries.add(query.toString());
        }
    }

    public List<AbstractArtefact> filterArtefactsByFilterSettings(final List<AbstractArtefact> allArtefacts, final EPFilterSettings filterSettings) {
        final long start = System.currentTimeMillis();
        if (allArtefacts == null) {
            return null;
        }
        final List<AbstractArtefact> filteredArtefactList = new ArrayList<AbstractArtefact>(allArtefacts.size());
        filteredArtefactList.addAll(allArtefacts);
        if (filterSettings != null && !filterSettings.isFilterEmpty()) {
            if (filteredArtefactList.size() != 0) {
                filterArtefactsByTags(filteredArtefactList, filterSettings, null);
            }
            if (filteredArtefactList.size() != 0) {
                filterArtefactsByType(filteredArtefactList, filterSettings.getTypeFilter());
            }
            if (filteredArtefactList.size() != 0) {
                filterArtefactsByString(filteredArtefactList, filterSettings.getTextFilter());
            }
            if (filteredArtefactList.size() != 0) {
                filterArtefactsByDate(filteredArtefactList, filterSettings.getDateFilter());
            }
        }
        final long duration = System.currentTimeMillis() - start;
        if (log.isDebugEnabled()) {
            log.debug("filtering took " + duration + "ms");
        }
        return filteredArtefactList;
    }

    /**
     * @param allArtefacts
     * @param filterSettings
     *            (containing tags to filter for or boolean if filter should keep only artefacts without a tag)
     * @param collect
     *            the tags found in the filtered artefacts
     * @return filtered artefact list
     */
    private void filterArtefactsByTags(final List<AbstractArtefact> artefacts, final EPFilterSettings filterSettings, final Set<String> cloud) {
        final List<String> tags = filterSettings.getTagFilter();
        // either search for artefacts with given tags, or such with no one!
        final List<AbstractArtefact> toRemove = new ArrayList<AbstractArtefact>();
        if (tags != null && tags.size() != 0) {
            // TODO: epf: RH: fix needed, as long as tags with uppercase initial are
            // allowed!
            for (final AbstractArtefact artefact : artefacts) {
                final List<String> artefactTags = getArtefactTags(artefact);
                if (!artefactTags.containsAll(tags)) {
                    toRemove.add(artefact);
                } else if (cloud != null) {
                    cloud.addAll(artefactTags);
                }
            }
            artefacts.removeAll(toRemove);
        } else if (filterSettings.isNoTagFilterSet()) {
            for (final AbstractArtefact artefact : artefacts) {
                if (!getArtefactTags(artefact).isEmpty()) {
                    toRemove.add(artefact);
                }
            }
            artefacts.removeAll(toRemove);
        }
    }

    private void filterArtefactsByType(final List<AbstractArtefact> artefacts, final List<String> type) {
        if (type != null && type.size() != 0) {
            final List<AbstractArtefact> toRemove = new ArrayList<AbstractArtefact>();
            for (final AbstractArtefact artefact : artefacts) {
                if (!type.contains(artefact.getResourceableTypeName())) {
                    toRemove.add(artefact);
                }
            }
            artefacts.removeAll(toRemove);
        }
    }

    /**
     * date comparison will first set startDate to 00:00:00 and set endDate to 23:59:59 else there might be no results if start = end date. dateList must be set according
     * to: dateList(0) = startDate dateList(1) = endDate
     */
    private void filterArtefactsByDate(final List<AbstractArtefact> artefacts, final List<Date> dateList) {
        if (dateList != null && dateList.size() != 0) {
            if (dateList.size() == 2) {
                Date startDate = dateList.get(0);
                Date endDate = dateList.get(1);
                final Calendar cal = Calendar.getInstance();
                if (startDate == null) {
                    cal.set(1970, 1, 1);
                } else {
                    cal.setTime(startDate);
                }
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                startDate = cal.getTime();
                cal.setTime(endDate);
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                endDate = cal.getTime();
                final List<AbstractArtefact> toRemove = new ArrayList<AbstractArtefact>();
                for (final AbstractArtefact artefact : artefacts) {
                    final Date creationDate = artefact.getCreationDate();
                    if (!(creationDate.before(endDate) && creationDate.after(startDate))) {
                        toRemove.add(artefact);
                    }
                }
                artefacts.removeAll(toRemove);
            } else {
                throw new AssertException("provided DateList must contain exactly two Date-objects");
            }
        }
    }

    private void filterArtefactsByString(final List<AbstractArtefact> artefacts, final String textFilter) {
        if (StringHelper.containsNonWhitespace(textFilter)) {
            final List<AbstractArtefact> toRemove = new ArrayList<AbstractArtefact>();
            for (final AbstractArtefact artefact : artefacts) {
                final String textCompare = artefact.getTitle() + artefact.getDescription() + artefact.getFulltextContent();
                if (!textCompare.toLowerCase().contains(textFilter.toLowerCase())) {
                    toRemove.add(artefact);
                }
            }
            artefacts.removeAll(toRemove);
        }
    }

    /**
     * returns defined amount of users mostly used tags, sorted by occurrence of tag
     * 
     * @param ident
     * @param amount
     *            nr of tags to return, if 0: the default (5) will be returned, if -1: you will get all
     * @return a combined map with tags including occurrence and tag format: "house (7), house"
     */
    public Map<String, String> getUsersMostUsedTags(final Identity ident, Integer amount) {
        amount = (amount == 0) ? 5 : amount;
        final List<String> outp = new ArrayList<String>();

        final Map<String, String> res = new HashMap<String, String>();
        final List<Map<String, Integer>> bla = taggingManager.getUserTagsWithFrequency(ident);
        for (final Map<String, Integer> map : bla) {
            final String caption = map.get("tag") + " (" + map.get("nr") + ")";
            outp.add(caption);
            res.put(caption, String.valueOf(map.get("tag")));
            if (amount == res.size()) {
                break;
            }
        }

        return res;
    }

    /**
     * get all tags a user owns, ordered and without duplicates
     * 
     * @param ident
     * @return
     */
    public List<String> getUsersTags(final Identity ident) {
        return taggingManager.getUserTagsAsString(ident);
    }

    /**
     * get all tags restricted to Artefacts a user owns, ordered and without duplicates
     * 
     * @param ident
     * @return
     */
    public List<String> getUsersTagsOfArtefactType(final Identity ident) {
        return taggingManager.getUserTagsOfTypeAsString(ident, AbstractArtefact.class.getSimpleName());
    }

    /**
     * lookup resources for a given tags
     * 
     * @param tagList
     * @return
     */
    public Set<OLATResourceable> getResourcesByTags(final List<Tag> tagList) {
        return taggingManager.getResourcesByTags(tagList);
    }

    /**
     * get all tags for a given resource
     * 
     * @param ores
     * @return
     */
    public List<Tag> loadTagsForResource(final OLATResourceable ores) {
        return taggingManager.loadTagsForResource(ores, null, null);
    }

    /**
     * sync map with its former source (template)
     */
    public boolean synchronizeStructuredMapToUserCopy(final PortfolioStructureMap map) {
        final EPStructuredMap userMap = (EPStructuredMap) map;
        final PortfolioStructureDao structMgr = structureManager; // only remove
        // synthetic access
        // warnings

        final Boolean synched = coordinator.getSyncer().doInSync(map.getOlatResource(), new SyncerCallback<Boolean>() {
            @Override
            public Boolean execute() {
                if (userMap.getStructuredMapSource() == null) {
                    return Boolean.FALSE;
                }
                // need to reload it, I don't know why
                final Long templateKey = userMap.getStructuredMapSource().getKey();
                userMap.setLastSynchedDate(new Date());
                final PortfolioStructure template = structMgr.loadPortfolioStructureByKey(templateKey);
                structMgr.syncStructureRecursively(template, userMap, true);
                return Boolean.TRUE;
            }
        });

        return synched.booleanValue();
    }

    /**
     * Assign a structure map to user. In other words, make a copy of the template and set the user as an author.
     * 
     * @param identity
     * @param portfolioStructureStructuredMapTemplate
     */
    // TODO: when implementing transactions, pay attention to this
    public PortfolioStructureMap assignStructuredMapToUser(final Identity identity, final PortfolioStructureMap mapTemplate, final OLATResourceable targetOres,
            final String targetSubPath, final String targetBusinessPath, final Date deadline) {
        // doInSync is here to check for nested doInSync exception in first place
        final Identity author = identity;
        // only remove synthetic access warnings
        final PortfolioStructureDao structMgr = structureManager;
        final PortfolioStructureMap template = mapTemplate;
        final OLATResourceable ores = targetOres;
        final String subPath = targetSubPath;

        final PortfolioStructureMap map = coordinator.getSyncer().doInSync(template.getOlatResource(), new SyncerCallback<PortfolioStructureMap>() {
            @Override
            public PortfolioStructureMap execute() {
                final String title = template.getTitle();
                final String description = template.getDescription();
                final PortfolioStructureMap copy = structMgr.createPortfolioStructuredMap(template, author, title, description, ores, subPath, targetBusinessPath);
                if (copy instanceof EPStructuredMap) {
                    ((EPStructuredMap) copy).setDeadLine(deadline);
                }
                structMgr.copyStructureRecursively(template, copy, true);
                return copy;
            }
        });
        return map;
    }

    /**
     * Low level function to copy the structure of elements, with or without the artefacts
     * 
     * @param source
     * @param target
     * @param withArtefacts
     */
    public void copyStructureRecursively(final PortfolioStructure source, final PortfolioStructure target, final boolean withArtefacts) {
        structureManager.copyStructureRecursively(source, target, withArtefacts);
    }

    /**
     * Return the structure elements of the given type without permission control. Need this for indexing.
     * 
     * @param firstResult
     * @param maxResults
     * @param type
     * @return
     */
    public List<PortfolioStructure> getStructureElements(final int firstResult, final int maxResults, final ElementType... type) {
        return structureManager.getStructureElements(firstResult, maxResults, type);
    }

    /**
     * Return the number of structure elements of the given type without permission control. Need this for indexing.
     * 
     * @param type
     * @return
     */
    public Long getStructureElementsCount(final ElementType... type) {
        return structureManager.getStructureElementsCount(type);
    }

    /**
     * get all Structure-Elements linked to identity over a security group (owner)
     * 
     * @param ident
     * @return
     */
    public List<PortfolioStructure> getStructureElementsForUser(final Identity identity, final ElementType... type) {
        return structureManager.getStructureElementsForUser(identity, type);
    }

    /**
     * Get all Structure-Elements linked which the identity can see over a policy,
     * 
     * @param ident
     *            The identity which what see maps
     * @param chosenOwner
     *            Limit maps from this identity
     * @param type
     *            Limit maps to this or these types
     * @return
     */
    public List<PortfolioStructure> getStructureElementsFromOthers(final Identity ident, final Identity chosenOwner, final ElementType... type) {
        return structureManager.getStructureElementsFromOthers(ident, chosenOwner, type);
    }

    /**
     * Get part of the Structure-Elements linked which the identity can see over a policy. The range of elements returned is specified by limitFrom and limitTo (used for
     * paging)
     * 
     * @param ident
     *            The identity which what see maps
     * @param chosenOwner
     *            Limit maps from this identity
     * @param limitFrom
     *            Limit maps
     * @param limitTo
     *            Limit maps
     * @param type
     *            Limit maps to this or these types
     * @return
     */
    public List<PortfolioStructure> getStructureElementsFromOthers(final Identity ident, final Identity chosenOwner, int limitFrom, int limitTo,
            final ElementType... type) {
        return structureManager.getStructureElementsFromOthersLimited(ident, chosenOwner, limitFrom, limitTo, type);
    }

    /**
     * Get all Structure-Elements linked which the identity can see over a policy, WITHOUT those that are public to all OLAT users ( GROUP_OLATUSERS ) !! this should be
     * used, to save performance when there are a lot of public shared maps!!
     * 
     * @param ident
     *            The identity which what see maps
     * @param chosenOwner
     *            Limit maps from this identity
     * @param type
     *            Limit maps to this or these types
     * @return
     */
    public List<PortfolioStructure> getStructureElementsFromOthersWithoutPublic(final Identity ident, final Identity choosenOwner, final ElementType... types) {
        return structureManager.getStructureElementsFromOthersWithoutPublic(ident, choosenOwner, types);
    }

    /**
     * Return the list of artefacts glued to this structure element
     * 
     * @param structure
     * @return A list of artefacts
     */
    public List<AbstractArtefact> getArtefacts(final PortfolioStructure structure) {
        return structureManager.getArtefacts(structure);
    }

    /**
     * Check the collect restriction against the structure element
     * 
     * @param structure
     * @return
     */
    public boolean checkCollectRestriction(final PortfolioStructure structure) {
        return structureManager.checkCollectRestriction(structure);
    }

    public boolean checkCollectRestrictionOfMap(final PortfolioStructureMap structure) {
        return checkAllCollectRestrictionRec(structure);
    }

    protected boolean checkAllCollectRestrictionRec(final PortfolioStructure structure) {
        boolean allOk = structureManager.checkCollectRestriction(structure);
        final List<PortfolioStructure> children = structureManager.loadStructureChildren(structure);
        for (final PortfolioStructure child : children) {
            allOk &= checkAllCollectRestrictionRec(child);
        }
        return allOk;
    }

    /**
     * Create a map for a user
     * 
     * @param root
     * @param identity
     * @param title
     * @param description
     * @return
     */
    public PortfolioStructureMap createAndPersistPortfolioDefaultMap(final Identity identity, final String title, final String description) {
        final PortfolioStructureMap map = structureManager.createPortfolioDefaultMap(identity, title, description);
        structureManager.savePortfolioStructure(map);
        return map;
    }

    /**
     * Create a map for a group
     * 
     * @param root
     * @param group
     * @param title
     * @param description
     * @return
     */
    public PortfolioStructureMap createAndPersistPortfolioDefaultMap(final BusinessGroup group, final String title, final String description) {
        final PortfolioStructureMap map = structureManager.createPortfolioDefaultMap(group, title, description);
        structureManager.savePortfolioStructure(map);
        return map;
    }

    /**
     * Create a structured map, based on template.
     * 
     * @param identity
     *            The author/owner of the map
     * @param title
     * @param description
     * @return The structure element
     */
    public PortfolioStructureMap createAndPersistPortfolioStructuredMap(final PortfolioStructureMap template, final Identity identity, final String title,
            final String description, final OLATResourceable targetOres, final String targetSubPath, final String targetBusinessPath) {
        final PortfolioStructureMap map = structureManager.createPortfolioStructuredMap(template, identity, title, description, targetOres, targetSubPath,
                targetBusinessPath);
        structureManager.savePortfolioStructure(map);
        return map;
    }

    /**
     * create a structure-element
     * 
     * @param root
     * @param title
     * @param description
     * @return
     */
    public PortfolioStructure createAndPersistPortfolioStructureElement(final PortfolioStructure root, final String title, final String description) {
        final EPStructureElement newStruct = (EPStructureElement) structureManager.createPortfolioStructure(root, title, description);
        if (root != null) {
            structureManager.addStructureToStructure(root, newStruct);
        }
        structureManager.savePortfolioStructure(newStruct);
        return newStruct;
    }

    /**
     * create a page
     * 
     * @param root
     * @param title
     * @param description
     * @return
     */
    public PortfolioStructure createAndPersistPortfolioPage(final PortfolioStructure root, final String title, final String description) {
        final EPPage newPage = (EPPage) structureManager.createPortfolioPage(root, title, description);
        if (root != null) {
            structureManager.addStructureToStructure(root, newPage);
        }
        structureManager.savePortfolioStructure(newPage);
        return newPage;
    }

    /**
     * This method is reserved to the repository. It removes the template completely
     * 
     * @param pStruct
     */
    public void deletePortfolioMapTemplate(final OLATResourceable res) {
        structureManager.deletePortfolioMapTemplate(res);
    }

    /**
     * delete a portfolio structure recursively with its childs
     * 
     * @param pStruct
     */
    public void deletePortfolioStructure(final PortfolioStructure pStruct) {
        structureManager.removeStructureRecursively(pStruct);
    }

    /**
     * save or update a structure
     * 
     * @param pStruct
     */
    public void savePortfolioStructure(final PortfolioStructure pStruct) {
        structureManager.savePortfolioStructure(pStruct);
    }

    /**
     * Number of children
     */
    public int countStructureChildren(final PortfolioStructure structure) {
        return structureManager.countStructureChildren(structure);
    }

    /**
     * Load a portfolio structure by its resource
     * 
     * @param ores
     * @return
     */
    public PortfolioStructure loadPortfolioStructure(final OLATResourceable ores) {
        return structureManager.loadPortfolioStructure(ores);
    }

    /**
     * Load a portfolio structure by its primary key
     * 
     * @param key
     *            cannot be null
     * @return The structure element or null if not found
     */
    public PortfolioStructure loadPortfolioStructureByKey(final Long key) {
        return structureManager.loadPortfolioStructureByKey(key);
    }

    /**
     * Retrieve the parent of the structure
     * 
     * @param structure
     * @return
     */
    public PortfolioStructure loadStructureParent(final PortfolioStructure structure) {
        return structureManager.loadStructureParent(structure);
    }

    /**
     * Retrieve the children structures
     * 
     * @param structure
     * @return
     */
    public List<PortfolioStructure> loadStructureChildren(final PortfolioStructure structure) {
        return structureManager.loadStructureChildren(structure);
    }

    /**
     * @param structure
     * @param firstResult
     * @param maxResults
     * @return
     */
    public List<PortfolioStructure> loadStructureChildren(final PortfolioStructure structure, final int firstResult, final int maxResults) {
        return structureManager.loadStructureChildren(structure, firstResult, maxResults);
    }

    public PortfolioStructureMap loadPortfolioStructureMap(final Identity identity, final PortfolioStructureMap template, final OLATResourceable targetOres,
            final String targetSubPath, final String targetBusinessPath) {
        // sync the map with the template on opening it in gui, not on loading!
        return structureManager.loadPortfolioStructuredMap(identity, template, targetOres, targetSubPath, targetBusinessPath);
    }

    /**
     * get the "already in use" state of a structuredMapTemplate
     * 
     * @param template
     * @param targetOres
     * @param targetSubPath
     * @param targetBusinessPath
     * @return
     */
    public boolean isTemplateInUse(final PortfolioStructureMap template, final OLATResourceable targetOres, final String targetSubPath, final String targetBusinessPath) {
        return structureManager.isTemplateInUse(template, targetOres, targetSubPath, targetBusinessPath);
    }

    /**
     * get root vfs-container where artefact file-system data is persisted
     * 
     * @return
     */
    public VFSContainer getArtefactsRoot() {
        return artefactManager.getArtefactsRoot();
    }

    /**
     * get vfs-container of a specific artefact
     * 
     * @param artefact
     * @return
     */
    public VFSContainer getArtefactContainer(final AbstractArtefact artefact) {
        return artefactManager.getArtefactContainer(artefact);
    }

    /**
     * get a temporary folder to store files while in wizzard
     * 
     * @param ident
     * @return
     */
    public VFSContainer getArtefactsTempContainer(final Identity ident) {
        return artefactManager.getArtefactsTempContainer(ident);
    }

    /**
     * as large fulltext-content of an artefact is persisted on filesystem, use this method to get fulltext
     * 
     * @param artefact
     * @return
     */
    public String getArtefactFullTextContent(final AbstractArtefact artefact) {
        return artefactManager.getArtefactFullTextContent(artefact);
    }

    /**
     * Check if the identity is the owner of this portfolio resource.
     * 
     * @param identity
     * @param ores
     * @return
     */
    public boolean isMapOwner(final Identity identity, final OLATResourceable ores) {
        return structureManager.isMapOwner(identity, ores);
    }

    /**
     * Check if the identity is owner of the portfolio resource or in a valid policy.
     * 
     * @param identity
     * @param ores
     * @return
     */
    public boolean isMapVisible(final Identity identity, final OLATResourceable ores) {
        return structureManager.isMapVisible(identity, ores);
    }

    public boolean isMapShared(final PortfolioStructureMap map) {
        final OLATResource resource = map.getOlatResource();
        final List<Policy> policies = securityManager.getPoliciesOfResource(resource, null);
        for (final Policy policy : policies) {
            if (policy.getPermission().contains(Constants.PERMISSION_READ)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return a list of wrapper containing the read policies of the map
     * 
     * @param map
     */
    public List<EPMapPolicy> getMapPolicies(final PortfolioStructureMap map) {
        return policyManager.getMapPolicies(map);
    }

    /**
     * Update the map policies of a map. The missing policies are deleted!
     * 
     * @param map
     * @param policyWrappers
     */
    public void updateMapPolicies(final PortfolioStructureMap map, final List<EPMapPolicy> policyWrappers) {
        policyManager.updateMapPolicies(map, policyWrappers);
    }

    /**
     * submit and close a structured map from a portfolio task
     * 
     * @param map
     */
    public void submitMap(final PortfolioStructureMap map) {
        submitMap(map, true);
    }

    private void submitMap(final PortfolioStructureMap map, final boolean logActivity) {
        if (!(map instanceof EPStructuredMap)) {
            return;// add an exception
        }

        final EPStructuredMap submittedMap = (EPStructuredMap) map;
        structureManager.submitMap(submittedMap);

        final EPTargetResource resource = submittedMap.getTargetResource();
        final OLATResourceable courseOres = resource.getOLATResourceable();
        final ICourse course = CourseFactory.loadCourse(courseOres);
        final AssessmentManager am = course.getCourseEnvironment().getAssessmentManager();
        final CourseNode courseNode = course.getRunStructure().getNode(resource.getSubPath());

        final List<Identity> owners = securityManager.getIdentitiesOfSecurityGroup(submittedMap.getOwnerGroup());
        for (final Identity owner : owners) {
            final IdentityEnvironment ienv = new IdentityEnvironment();
            ienv.setIdentity(owner);
            final UserCourseEnvironment uce = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());
            if (logActivity) {
                am.incrementNodeAttempts(courseNode, owner, uce);
            } else {
                am.incrementNodeAttemptsInBackground(courseNode, owner, uce);
            }

            log.info("Audit:Map " + map + " from " + owner.getName() + " has been submitted.");
        }
    }

    /**
     * Close all maps after the deadline if there is a deadline. It can be a long running process if a lot of maps are involved.
     */
    public void closeMapAfterDeadline() {
        final List<PortfolioStructureMap> mapsToClose = structureManager.getOpenStructuredMapAfterDeadline();
        int count = 0;
        for (final PortfolioStructureMap mapToClose : mapsToClose) {
            submitMap(mapToClose, false);
            count++;
            if (count % 5 == 0) {
                // this possibly takes longer than connection timeout, so do intermediatecommits.
                dbInstance.intermediateCommit();
            }
        }
    }

    /**
     * get a valid name of style for a given PortfolioStructure if style is not enabled anymore, the default will be used.
     * 
     * @param struct
     * @return the set style or the default from config if nothing is set.
     */
    public String getValidStyleName(final PortfolioStructure struct) {
        // first style in list is the default, can be named default.
        final List<String> allStyles = portfolioModule.getAvailableMapStyles();
        if (allStyles == null || allStyles.size() == 0) {
            throw new AssertException("at least one style (that also exists in brasato.css must be configured for maps.");
        }
        final String styleName = ((EPStructureElement) struct).getStyle();
        if (StringHelper.containsNonWhitespace(styleName) && allStyles.contains(styleName)) {
            return styleName;
        }
        return allStyles.get(0);
    }

    /**
     * The structure will be without any check on the DB copied. All the children structures MUST be loaded. This method is to use with the output of XStream at examples.
     * 
     * @param root
     * @param identity
     * @return The persisted structure
     */
    public PortfolioStructureMap importPortfolioMapTemplate(final PortfolioStructure root, final Identity identity) {
        return structureManager.importPortfolioMapTemplate(root, identity);
    }

    // not yet available
    public void archivePortfolio() {
    }

    // not yet available
    public void exportPortfolio() {
    }

    // not yet available
    public void importPortfolio() {
    }

    /**
     * same as getRestrictionStatistics(PortfolioStructure structure) but recursively for a map. get statistics about how much of the required (min, equal)
     * collect-restrictions have been fulfilled.
     * 
     * @param structure
     * @return array with "done" at 0 and "to be done" at 1, or "null" if no restrictions apply
     */
    public String[] getRestrictionStatisticsOfMap(final PortfolioStructureMap structure) {

        Integer[] stats = structureManager.getRestrictionStatisticsOfMap(structure, 0, 0);
        return new String[] { stats[0].toString(), stats[1].toString() };
    }

    /**
     * move a structures order within the same parent, allows manual sorting.
     * 
     * @param structToBeMvd
     * @param destinationPos
     *            where it should be placed
     * @return true if it went ok, false otherwise
     */
    public boolean moveStructureToPosition(PortfolioStructure structToBeMvd, int destinationPos) {
        return structureManager.reOrderStructures(loadStructureParent(structToBeMvd), structToBeMvd, destinationPos);
    }

    /**
     * Both a blog course node and a "Learning journal"/"Lerntagebuch" (technically 'LiveBlog') is referenced by a BlogFileResource. For the latter case we check here if
     * a corresponding "Learning journal" exists as an e-portfolio artefact and the given identity has the right to access it.
     * 
     * @param blogFileResource
     * @param identity
     * @return true if identity has access
     */
    public boolean hasAccessToLiveBlogFeedMedia(OLATResourceable blogFileResource, Identity identity) {
        if (!blogFileResource.getResourceableTypeName().equals(BlogFileResource.TYPE_NAME)) {
            throw new AssertException("Expected OLAT resource of type: '" + BlogFileResource.TYPE_NAME + "'");
        }

        final List<AbstractArtefact> liveBlogArtefacts = loadArtefactsByBusinessPath(LiveBlogArtefactHandler.LIVEBLOG + blogFileResource.getResourceableId() + "]");
        if (liveBlogArtefacts == null || liveBlogArtefacts.isEmpty()) {
            return false;
        }
        if (liveBlogArtefacts.size() > 1) {
            throw new AssertException("Only one LiveBlogArtefact expected for OLATResource: " + blogFileResource.getResourceableId());
        }

        // if requesting identity is author of LiveBlog allow access even if not attached to map
        final AbstractArtefact liveBlogArtefact = liveBlogArtefacts.get(0);
        if (liveBlogArtefact.getAuthor().equals(identity)) {
            return true;
        }

        // if LiveBlog attached to map(s) check access rights on map level
        final List<PortfolioStructure> referencedMaps = getReferencedMapsForArtefact(liveBlogArtefact);
        for (PortfolioStructure map : referencedMaps) {
            if (isMapVisible(identity, map)) {
                return true;
            } else {
                // allow template owner access to map
                if (map instanceof EPStructuredMap) {
                    PortfolioStructureMap source = ((EPStructuredMap) map).getStructuredMapSource();
                    if (source != null) {
                        if (isMapVisible(identity, source)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

}
