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
package org.olat.data.portfolio.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Constants;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.NamedGroupImpl;
import org.olat.data.basesecurity.PolicyImpl;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.basesecurity.SecurityGroupImpl;
import org.olat.data.basesecurity.SecurityGroupMembershipImpl;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBQuery;
import org.olat.data.commons.database.PersistentObject;
import org.olat.data.group.BusinessGroup;
import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.data.portfolio.restriction.CollectRestriction;
import org.olat.data.portfolio.restriction.RestrictionsConstants;
import org.olat.data.repository.RepositoryDao;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Description:<br>
 * Manager to operate ePortfolio maps, structure-elements, pages.
 * <P>
 * Initial Date: 11.06.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
@Repository("epStructureManager")
public class PortfolioStructureDaoImpl extends BasicManager implements PortfolioStructureDao {

    private static final Logger log = LoggerHelper.getLogger();

    public static final String STRUCTURE_ELEMENT_TYPE_NAME = "EPStructureElement";

    @Autowired
    DB dbInstance;
    @Autowired
    RepositoryDao repositoryDao;
    @Autowired
    OLATResourceManager resourceManager;
    @Autowired
    BaseSecurity securityManager;

    /**
     * spring
     */
    private PortfolioStructureDaoImpl() {
        //
    }

    /**
     * Return the list of artefacts glued to this structure element
     * 
     * @param structure
     * @return A list of artefacts
     */
    @Override
    public List<AbstractArtefact> getArtefacts(final PortfolioStructure structure) {
        return getArtefacts(structure, -1, -1);
    }

    @Override
    public List<PortfolioStructureMap> getOpenStructuredMapAfterDeadline() {
        final StringBuilder sb = new StringBuilder();
        sb.append("select map from ").append(EPStructuredMap.class.getName()).append(" as map");
        sb.append(" where (map.status is null or not(map.status = 'closed'))").append(" and map.deadLine<:currentDate");

        final DBQuery query = dbInstance.createQuery(sb.toString());
        query.setDate("currentDate", new Date());
        @SuppressWarnings("unchecked")
        final List<PortfolioStructureMap> maps = query.list();
        return maps;
    }

    @Override
    public List<PortfolioStructure> getStructureElements(final int firstResult, final int maxResults, final ElementType... types) {
        final StringBuilder sb = new StringBuilder();
        sb.append("select stEl from ").append(EPStructureElement.class.getName()).append(" stEl");
        sb.append(" where stEl.class in (");
        boolean first = true;
        for (final ElementType type : types) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append(getImplementation(type).getName());
        }
        sb.append(")");

        final DBQuery query = dbInstance.createQuery(sb.toString());
        if (firstResult > 0) {
            query.setFirstResult(firstResult);
        }
        if (maxResults > 0) {
            query.setMaxResults(maxResults);
        }
        @SuppressWarnings("unchecked")
        final List<PortfolioStructure> pStructs = query.list();
        return pStructs;
    }

    @Override
    public Long getStructureElementsCount(final ElementType... types) {
        final StringBuilder sb = new StringBuilder();
        sb.append("select count(*) from ").append(EPStructureElement.class.getName()).append(" stEl");
        sb.append(" where stEl.class in (");
        boolean first = true;
        for (final ElementType type : types) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append(getImplementation(type).getName());
        }
        sb.append(")");

        final DBQuery query = dbInstance.createQuery(sb.toString());
        return (Long) query.list().get(0);
    }

    @Override
    public List<PortfolioStructure> getStructureElementsForUser(final Identity ident, final ElementType... types) {
        final StringBuilder sb = new StringBuilder();
        sb.append("select stEl from ").append(EPStructureElement.class.getName()).append(" stEl");
        sb.append(" where ownerGroup in ( ").append("select sgi.key from").append(" org.olat.data.basesecurity.SecurityGroupImpl as sgi,")
                .append(" org.olat.data.basesecurity.SecurityGroupMembershipImpl as sgmsi ").append(" where sgmsi.securityGroup = sgi and sgmsi.identity =:ident")
                .append(" )");
        if (types != null && types.length > 0) {
            sb.append(" and stEl.class in (");
            boolean first = true;
            for (final ElementType type : types) {
                if (first) {
                    first = false;
                } else {
                    sb.append(",");
                }
                sb.append(getImplementation(type).getName());
            }
            sb.append(")");
        }

        final DBQuery query = dbInstance.createQuery(sb.toString());
        query.setEntity("ident", ident);
        @SuppressWarnings("unchecked")
        final List<PortfolioStructure> pStructs = query.list();
        return pStructs;
    }

    /**
     * Check if the identity is owner of the map
     * 
     * @param identity
     * @param ores
     * @return
     */
    @Override
    public boolean isMapOwner(final Identity identity, final OLATResourceable ores) {
        final StringBuilder sb = new StringBuilder();
        sb.append("select count(stEl) from ").append(EPStructureElement.class.getName()).append(" stEl ").append(" where stEl.olatResource.resId=:resourceableId")
                .append(" and stEl.olatResource.resName=:resourceableTypeName").append(" and stEl.ownerGroup in ( ").append("  select sgi.key from")
                .append("  org.olat.data.basesecurity.SecurityGroupImpl as sgi,").append("  org.olat.data.basesecurity.SecurityGroupMembershipImpl as sgmsi ")
                .append("  where sgmsi.securityGroup = sgi and sgmsi.identity =:owner").append(" )");

        final DBQuery query = dbInstance.createQuery(sb.toString());
        query.setEntity("owner", identity);
        query.setLong("resourceableId", ores.getResourceableId());
        query.setString("resourceableTypeName", ores.getResourceableTypeName());

        final Number count = (Number) query.uniqueResult();
        return count.intValue() == 1;
    }

    /**
     * Check if the identity is owner or is in a valid policy
     * 
     * @param identity
     * @param ores
     * @return
     */
    @Override
    public boolean isMapVisible(final Identity identity, final OLATResourceable ores) {
        final StringBuilder sb = new StringBuilder();
        sb.append("select count(stEl) from ").append(EPStructureElement.class.getName()).append(" stEl ").append(" inner join stEl.olatResource as oRes ")
                .append(" where oRes.resId=:resourceableId").append(" and oRes.resName=:resourceableTypeName").append(" and ( oRes in ( ")
                .append("  select policy.olatResource from").append("  ").append(PolicyImpl.class.getName()).append(" as policy, ").append("  ")
                .append(SecurityGroupImpl.class.getName())
                .append(" as sgi,")
                .append("  ")
                .append(SecurityGroupMembershipImpl.class.getName())
                .append(" as sgmsi ")
                .append("  where sgi = policy.securityGroup")
                // implicit inner join
                .append("  and (sgmsi.securityGroup = sgi and sgmsi.identity =:identity) ")
                // member of the security group
                .append("  and (policy.from is null or policy.from<=:date)").append("  and (policy.to is null or policy.to>=:date)").append(" )")
                .append(" or stEl.ownerGroup in ( ").append("select sgi.key from").append(" org.olat.data.basesecurity.SecurityGroupImpl as sgi,")
                .append(" org.olat.data.basesecurity.SecurityGroupMembershipImpl as sgmsi ").append(" where sgmsi.securityGroup = sgi and sgmsi.identity =:identity")
                .append(" ))");

        final DBQuery query = dbInstance.createQuery(sb.toString());
        query.setEntity("identity", identity);
        query.setLong("resourceableId", ores.getResourceableId());
        query.setString("resourceableTypeName", ores.getResourceableTypeName());
        query.setDate("date", new Date());

        final Number count = (Number) query.uniqueResult();
        return count.intValue() == 1;
    }

    @Override
    public List<PortfolioStructure> getStructureElementsFromOthers(final Identity ident, final Identity choosenOwner, final ElementType... types) {
        final StringBuilder sb = new StringBuilder();
        sb.append("select stEl from ").append(EPStructureElement.class.getName()).append(" stEl ");
        sb.append(" inner join stEl.olatResource as oRes ");
        sb.append(" where oRes in ( ").append("  select policy.olatResource from").append("  ").append(PolicyImpl.class.getName()).append(" as policy, ").append("  ")
                .append(SecurityGroupImpl.class.getName()).append(" as sgi,").append("  ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmsi ")
                .append("  where sgi = policy.securityGroup")// implicit inner
                // join
                .append("  and (sgmsi.securityGroup = sgi and sgmsi.identity =:ident) ")// member of the security group
                .append("  and (policy.from is null or policy.from<=:date)").append("  and (policy.to is null or policy.to>=:date)").append(" )");

        // remove owner
        sb.append(" and stEl.ownerGroup not in ( ").append("select sgi2.key from").append(" org.olat.data.basesecurity.SecurityGroupImpl as sgi2,")
                .append(" org.olat.data.basesecurity.SecurityGroupMembershipImpl as sgmsi2 ").append(" where sgmsi2.securityGroup = sgi2 and sgmsi2.identity =:ident")
                .append(" )");

        if (choosenOwner != null) {
            sb.append(" and stEl.ownerGroup in ( ").append("select sgi.key from").append(" org.olat.data.basesecurity.SecurityGroupImpl as sgi,")
                    .append(" org.olat.data.basesecurity.SecurityGroupMembershipImpl as sgmsi ").append(" where sgmsi.securityGroup = sgi and sgmsi.identity =:owner")
                    .append(" )");
        }
        if (types != null && types.length > 0) {
            sb.append(" and stEl.class in (");
            boolean first = true;
            for (final ElementType type : types) {
                if (first) {
                    first = false;
                } else {
                    sb.append(",");
                }
                sb.append(getImplementation(type).getName());
            }
            sb.append(")");
        }

        final DBQuery query = dbInstance.createQuery(sb.toString());
        query.setEntity("ident", ident);
        query.setDate("date", new Date());
        if (choosenOwner != null) {
            query.setEntity("owner", choosenOwner);
        }

        @SuppressWarnings("unchecked")
        final List<PortfolioStructure> pStructs = query.list();
        return pStructs;
    }

    @Override
    public List<PortfolioStructure> getStructureElementsFromOthersWithoutPublic(final Identity ident, final Identity choosenOwner, final ElementType... types) {
        final StringBuilder sb = new StringBuilder();
        sb.append("select stEl from ").append(EPStructureElement.class.getName()).append(" stEl ");
        sb.append(" inner join stEl.olatResource as oRes ");
        sb.append(" where oRes in ( ").append("  select policy.olatResource from").append("  ").append(PolicyImpl.class.getName()).append(" as policy, ").append("  ")
                .append(SecurityGroupImpl.class.getName()).append(" as sgi,").append("  ").append(SecurityGroupMembershipImpl.class.getName())
                .append(" as sgmsi ")
                .append("  where sgi = policy.securityGroup ")
                // implicit inner join
                .append("  and sgi = policy.securityGroup ")
                .append("  and (sgmsi.securityGroup = sgi and sgmsi.identity =:ident) ")
                // member of the security group
                .append("  and (policy.from is null or policy.from<=:date)").append("  and (policy.to is null or policy.to>=:date)").append("  and sgi not in (")
                .append("    select ngroup.securityGroup from ").append("    ").append(NamedGroupImpl.class.getName()).append(" as ngroup ")
                .append("    where ngroup.groupName =:usersGroup").append("   )").append(" )");

        // remove owner
        sb.append(" and stEl.ownerGroup not in ( ").append("	 select sgi2.key from").append("  org.olat.data.basesecurity.SecurityGroupImpl as sgi2,")
                .append("  org.olat.data.basesecurity.SecurityGroupMembershipImpl as sgmsi2 ").append("  where sgmsi2.securityGroup = sgi2 and sgmsi2.identity =:ident")
                .append(" )");

        if (choosenOwner != null) {
            sb.append(" and stEl.ownerGroup in ( ").append("select sgi.key from").append(" org.olat.data.basesecurity.SecurityGroupImpl as sgi,")
                    .append(" org.olat.data.basesecurity.SecurityGroupMembershipImpl as sgmsi ").append(" where sgmsi.securityGroup = sgi and sgmsi.identity =:owner")
                    .append(" )");
        }
        if (types != null && types.length > 0) {
            sb.append(" and stEl.class in (");
            boolean first = true;
            for (final ElementType type : types) {
                if (first) {
                    first = false;
                } else {
                    sb.append(",");
                }
                sb.append(getImplementation(type).getName());
            }
            sb.append(")");
        }

        final DBQuery query = dbInstance.createQuery(sb.toString());
        query.setEntity("ident", ident);
        query.setString("usersGroup", Constants.GROUP_OLATUSERS);
        query.setDate("date", new Date());
        if (choosenOwner != null) {
            query.setEntity("owner", choosenOwner);
        }

        @SuppressWarnings("unchecked")
        final List<PortfolioStructure> pStructs = query.list();
        return pStructs;
    }

    public List<PortfolioStructure> getStructureElementsFromOthersLimited(final Identity ident, final Identity choosenOwner, final int limitFrom, final int limitTo,
            final ElementType... types) {
        final StringBuilder sb = buildStructureElementsFromOthersLimitedQuery("stEl", ident, choosenOwner, types);
        final DBQuery query = dbInstance.createQuery(sb.toString());

        // limits
        if (limitTo > 0 && (limitFrom < limitTo)) {
            query.setFirstResult(limitFrom);
            query.setMaxResults(limitTo - limitFrom);
        }

        query.setEntity("ident", ident);
        query.setDate("date", new Date());
        if (choosenOwner != null) {
            query.setEntity("owner", choosenOwner);
        }

        @SuppressWarnings("unchecked")
        final List<PortfolioStructure> pStructs = query.list();
        return pStructs;
    }

    /**
     * 
     * @param select
     * @param ident
     * @param choosenOwner
     * @param limitFrom
     * @param limitTo
     * @param types
     * @return
     */
    private StringBuilder buildStructureElementsFromOthersLimitedQuery(String select, final Identity ident, final Identity choosenOwner, final ElementType... types) {
        StringBuilder sb = new StringBuilder();

        sb.append("select ").append(select).append(" from ");

        sb.append(EPStructureElement.class.getName()).append(" stEl ");
        sb.append(" inner join stEl.olatResource as oRes ");
        sb.append(" where oRes in ( ").append("  select policy.olatResource from").append("  ").append(PolicyImpl.class.getName()).append(" as policy, ").append("  ")
                .append(SecurityGroupImpl.class.getName()).append(" as sgi,").append("  ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmsi ")
                .append("  where sgi = policy.securityGroup")// implicit inner join
                .append("  and (sgmsi.securityGroup = sgi and sgmsi.identity =:ident) ")// member of the security group
                .append("  and (policy.from is null or policy.from<=:date)").append("  and (policy.to is null or policy.to>=:date)").append(" )");

        // remove owner
        sb.append(" and stEl.ownerGroup not in ( ").append("select sgi2.key from").append(" org.olat.data.basesecurity.SecurityGroupImpl as sgi2,")
                .append(" org.olat.data.basesecurity.SecurityGroupMembershipImpl as sgmsi2 ").append(" where sgmsi2.securityGroup = sgi2 and sgmsi2.identity =:ident")
                .append(" )");

        if (choosenOwner != null) {
            sb.append(" and stEl.ownerGroup in ( ").append("select sgi.key from").append(" org.olat.basesecurity.SecurityGroupImpl as sgi,")
                    .append(" org.olat.data.basesecurity.SecurityGroupMembershipImpl as sgmsi ").append(" where sgmsi.securityGroup = sgi and sgmsi.identity =:owner")
                    .append(" )");
        }
        if (types != null && types.length > 0) {
            sb.append(" and stEl.class in (");
            boolean first = true;
            for (final ElementType type : types) {
                if (first) {
                    first = false;
                } else {
                    sb.append(",");
                }
                sb.append(getImplementation(type).getName());
            }
            sb.append(")");
        }

        return sb;
    }

    private Class<?> getImplementation(final ElementType type) {
        switch (type) {
        case DEFAULT_MAP:
            return EPDefaultMap.class;
        case STRUCTURED_MAP:
            return EPStructuredMap.class;
        case TEMPLATE_MAP:
            return EPStructuredMapTemplate.class;
        default:
            return null;
        }
    }

    @Override
    public List<PortfolioStructure> getReferencedMapsForArtefact(final AbstractArtefact artefact) {
        final List<PortfolioStructure> pfList = getAllReferencesForArtefact(artefact);
        final List<PortfolioStructure> mapList = new ArrayList<PortfolioStructure>();
        for (final Iterator<?> iterator = pfList.iterator(); iterator.hasNext();) {
            final EPStructureElement portfolioStructure = (EPStructureElement) iterator.next();
            EPStructureElement actStruct = portfolioStructure;
            while (actStruct.getRoot() != null) {
                final EPStructureElement actRoot = actStruct.getRoot();
                if (actRoot != null) {
                    actStruct = actRoot;
                }
            }
            if (!mapList.contains(actStruct)) {
                mapList.add(actStruct);
            }
        }
        return mapList;
    }

    @Override
    public List<PortfolioStructure> getAllReferencesForArtefact(final AbstractArtefact artefact) {
        final StringBuilder sb = new StringBuilder();
        sb.append("select link.structureElement from ").append(EPStructureToArtefactLink.class.getName()).append(" link").append(" where link.artefact=:artefactEl ");
        final DBQuery query = dbInstance.createQuery(sb.toString());
        query.setEntity("artefactEl", artefact);
        @SuppressWarnings("unchecked")
        final List<PortfolioStructure> pfList = query.list();
        return pfList;
    }

    /**
     * Return the list of artefacts glued to this structure element
     * 
     * @param structure
     * @param firstResult
     * @param maxResults
     * @return
     */
    @Override
    public List<AbstractArtefact> getArtefacts(final PortfolioStructure structure, final int firstResult, final int maxResults) {
        final StringBuilder sb = new StringBuilder();
        sb.append("select link.artefact from ").append(EPStructureToArtefactLink.class.getName()).append(" link")
                .append(" where link.structureElement.key=:structureElKey order by link.order");

        final DBQuery query = dbInstance.createQuery(sb.toString());
        query.setLong("structureElKey", structure.getKey());
        if (firstResult > 0) {
            query.setFirstResult(firstResult);
        }
        if (maxResults > 0) {
            query.setMaxResults(maxResults);
        }

        @SuppressWarnings("unchecked")
        final List<AbstractArtefact> artefacts = query.list();
        return artefacts;
    }

    /**
     * Return the number of artefacts hold by a structure element
     * 
     * @param structure
     * @return
     */
    @Override
    public int countArtefacts(final PortfolioStructure structure) {
        final StringBuilder sb = new StringBuilder();
        sb.append("select count(link) from ").append(EPStructureToArtefactLink.class.getName()).append(" link").append(" where link.structureElement=:structureEl");

        final DBQuery query = dbInstance.createQuery(sb.toString());
        query.setEntity("structureEl", structure);
        final Number count = (Number) query.uniqueResult();
        return count.intValue();
    }

    /**
     * Count all artefacts (links) in a map
     */
    @Override
    public int countArtefactsRecursively(final PortfolioStructure structure) {
        // return countArtefactsRecursively(structure, 0);

        final StringBuilder sb = new StringBuilder();
        sb.append("select count(link) from ").append(EPStructureToArtefactLink.class.getName()).append(" link").append(" inner join link.structureElement structure ")
                .append(" inner join structure.rootMap root").append(" where root=:structureEl");

        final DBQuery query = dbInstance.createQuery(sb.toString());
        query.setEntity("structureEl", structure);
        final Number count = (Number) query.uniqueResult();
        return count.intValue();
    }

    @Override
    public int countArtefactsRecursively(final PortfolioStructure structure, int res) {
        final List<PortfolioStructure> childs = loadStructureChildren(structure);
        res = res + countArtefacts(structure);
        for (final PortfolioStructure portfolioStructure : childs) {
            res = countArtefactsRecursively(portfolioStructure, res);
        }
        return res;
    }

    @Override
    public boolean isArtefactInStructure(final AbstractArtefact artefact, final PortfolioStructure structure) {
        final StringBuilder sb = new StringBuilder();
        sb.append("select link.key from ").append(EPStructureToArtefactLink.class.getName()).append(" link")
                .append(" where link.structureElement=:structureEl and link.artefact=:artefact");

        final DBQuery query = dbInstance.createQuery(sb.toString());
        query.setEntity("structureEl", structure);
        query.setEntity("artefact", artefact);

        @SuppressWarnings("unchecked")
        final List<Long> key = query.list();
        return key.size() == 1 ? true : false;
    }

    /**
     * Number of children
     */
    @Override
    public int countStructureChildren(final PortfolioStructure structure) {
        if (structure == null) {
            throw new NullPointerException();
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("select count(link) from ").append(EPStructureToStructureLink.class.getName()).append(" link").append(" where link.parent=:structureEl");

        final DBQuery query = dbInstance.createQuery(sb.toString());
        query.setEntity("structureEl", structure);

        final Number count = (Number) query.uniqueResult();
        return count.intValue();
    }

    /**
     * Retrieve the children structures
     * 
     * @param structure
     * @return
     */
    @Override
    public List<PortfolioStructure> loadStructureChildren(final PortfolioStructure structure) {
        return loadStructureChildren(structure, -1, -1);
    }

    /**
     * @param structure
     * @param firstResult
     * @param maxResults
     * @return
     */
    @Override
    public List<PortfolioStructure> loadStructureChildren(final PortfolioStructure structure, final int firstResult, final int maxResults) {
        if (structure == null) {
            throw new NullPointerException();
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("select link.child from ").append(EPStructureToStructureLink.class.getName()).append(" link")
                .append(" where link.parent=:structureEl order by link.order");

        final DBQuery query = dbInstance.createQuery(sb.toString());
        if (firstResult > 0) {
            query.setFirstResult(firstResult);
        }
        if (maxResults > 0) {
            query.setMaxResults(maxResults);
        }
        query.setEntity("structureEl", structure);

        @SuppressWarnings("unchecked")
        final List<PortfolioStructure> resources = query.list();
        return resources;
    }

    /**
     * Retrieve the parent of the structure
     * 
     * @param structure
     * @return
     */
    @Override
    public PortfolioStructure loadStructureParent(final PortfolioStructure structure) {
        if (structure == null) {
            throw new NullPointerException();
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("select link.parent from ").append(EPStructureToStructureLink.class.getName()).append(" link").append(" where link.child=:structureEl");

        final DBQuery query = dbInstance.createQuery(sb.toString());
        query.setEntity("structureEl", structure);

        @SuppressWarnings("unchecked")
        final List<PortfolioStructure> resources = query.list();
        if (resources.isEmpty()) {
            return null;
        }
        if (resources.size() == 1) {
            return resources.get(0);
        }
        log.error("A structure child has more than one parent");
        return null;
    }

    /**
     * Add a link between a structure element and an artefact
     * 
     * @param author
     * @param artefact
     * @param structure
     * @return
     */
    @Override
    public boolean addArtefactToStructure(final Identity author, final AbstractArtefact artefact, final PortfolioStructure structure) {
        if (author == null || artefact == null || structure == null) {
            throw new NullPointerException();
        }
        if (structure instanceof EPStructureElement) {
            EPStructureElement structureEl = (EPStructureElement) structure;
            final boolean canAdd = canAddArtefact(structureEl, artefact);
            if (!canAdd) {
                return false;
            }
            // save eventual changes
            // TODO update the changes before dbInstance.updateObject(structureEl);
            // reconnect to the session
            structureEl = (EPStructureElement) dbInstance.loadObject(structureEl);

            final EPStructureToArtefactLink link = new EPStructureToArtefactLink();
            link.setArtefact(artefact);
            link.setStructureElement(structureEl);
            link.setAuthor(author);
            structureEl.getInternalArtefacts().add(link);
            dbInstance.updateObject(structureEl);
            return true;
        }
        return false;
    }

    protected boolean canAddArtefact(final EPStructureElement structureEl, final AbstractArtefact newArtefact) {

        final List<CollectRestriction> restrictions = structureEl.getCollectRestrictions();
        if (restrictions == null || restrictions.isEmpty()) {
            return true;
        }

        boolean allOk = true;
        final List<String> artefactTypeAllowed = new ArrayList<String>();
        final List<AbstractArtefact> artefacts = getArtefacts(structureEl);
        artefacts.add(newArtefact);

        for (final CollectRestriction restriction : restrictions) {
            final String type = restriction.getArtefactType();
            final int count = countRestrictionType(artefacts, restriction);
            artefactTypeAllowed.add(type);

            if (type.equals(newArtefact.getResourceableTypeName())) {
                if (RestrictionsConstants.MAX.equals(restriction.getRestriction())) {
                    allOk &= (restriction.getAmount() > 0 && count <= restriction.getAmount());
                } else if (RestrictionsConstants.EQUAL.equals(restriction.getRestriction())) {
                    allOk &= (restriction.getAmount() > 0 && count <= restriction.getAmount());
                }
            }
        }

        allOk &= artefactTypeAllowed.contains(newArtefact.getResourceableTypeName());
        return allOk;
    }

    @Override
    public boolean moveArtefactFromStructToStruct(final AbstractArtefact artefact, final PortfolioStructure oldParStruct, final PortfolioStructure newParStruct) {
        final EPStructureElement oldEPSt = (EPStructureElement) oldParStruct;
        final Identity author = oldEPSt.getInternalArtefacts().get(0).getAuthor();
        if (author == null) {
            return false; // old model without author, doesn't work!
        }

        removeArtefactFromStructure(artefact, oldParStruct);
        boolean allOk = false;
        allOk = addArtefactToStructure(author, artefact, newParStruct);
        return allOk;
    }

    /**
     * Check the collect restriction against the structure element
     * 
     * @param structure
     * @return
     */
    @Override
    public boolean checkCollectRestriction(final PortfolioStructure structure) {
        if (structure instanceof EPStructureElement) {
            final EPStructureElement structureEl = (EPStructureElement) structure;
            final List<CollectRestriction> restrictions = structureEl.getCollectRestrictions();
            if (restrictions == null || restrictions.isEmpty()) {
                return true;
            }

            boolean allOk = true;
            final List<String> artefactTypeAllowed = new ArrayList<String>();
            final List<AbstractArtefact> artefacts = getArtefacts(structureEl);
            for (final CollectRestriction restriction : restrictions) {
                final int count = countRestrictionType(artefacts, restriction);
                artefactTypeAllowed.add(restriction.getArtefactType());
                boolean ok = true;
                if (RestrictionsConstants.MAX.equals(restriction.getRestriction())) {
                    ok &= (restriction.getAmount() > 0 && count <= restriction.getAmount());
                } else if (RestrictionsConstants.MIN.equals(restriction.getRestriction())) {
                    ok &= (restriction.getAmount() > 0 && count >= restriction.getAmount());
                } else if (RestrictionsConstants.EQUAL.equals(restriction.getRestriction())) {
                    ok &= (restriction.getAmount() > 0 && count == restriction.getAmount());
                } else {
                    ok &= false;
                }
                allOk &= ok;
            }

            for (final AbstractArtefact artefact : artefacts) {
                allOk &= artefactTypeAllowed.contains(artefact.getResourceableTypeName());
            }
            return allOk;
        }
        return true;
    }

    private int countRestrictionType(final List<AbstractArtefact> artefacts, final CollectRestriction restriction) {
        int count = 0;
        if (StringHelper.containsNonWhitespace(restriction.getArtefactType())) {
            for (final AbstractArtefact artefact : artefacts) {
                if (restriction.getArtefactType().equals(artefact.getResourceableTypeName())) {
                    count++;
                }
            }
        }
        return count;
    }

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
    @Override
    public PortfolioStructure removeArtefactFromStructure(final AbstractArtefact artefact, final PortfolioStructure structure) {
        return removeArtefactFromStructure(artefact, structure, false);
    }

    private PortfolioStructure removeArtefactFromStructure(final AbstractArtefact artefact, final PortfolioStructure structure, final boolean updateFirst) {
        if (artefact == null || structure == null) {
            throw new NullPointerException();
        }
        if (artefact.getKey() == null) {
            return null;// not persisted
        }
        if (structure instanceof EPStructureElement) {
            // save eventual changes
            if (updateFirst) {
                dbInstance.updateObject(structure);
            }
            // reconnect to the session
            final EPStructureElement structureEl = (EPStructureElement) dbInstance.loadObject((EPStructureElement) structure);
            EPStructureToArtefactLink linkToDelete = null;
            for (final Iterator<EPStructureToArtefactLink> linkIt = structureEl.getInternalArtefacts().iterator(); linkIt.hasNext();) {
                final EPStructureToArtefactLink link = linkIt.next();
                if (link.getArtefact().getKey().equals(artefact.getKey())) {
                    linkIt.remove();
                    linkToDelete = link;
                    break;
                }
            }
            // I have not set the cascade all delete
            if (linkToDelete != null) {
                dbInstance.updateObject(structureEl);
                dbInstance.deleteObject(linkToDelete);
            }
            return structureEl;
        }
        return null;
    }

    /**
     * Move up an artefact in the list
     * 
     * @param structure
     * @param artefact
     */
    @Override
    public void moveUp(final PortfolioStructure structure, final AbstractArtefact artefact) {
        move(structure, artefact, true);
    }

    /**
     * Move down an artefact in the list
     * 
     * @param structure
     * @param artefact
     */
    @Override
    public void moveDown(final PortfolioStructure structure, final AbstractArtefact artefact) {
        move(structure, artefact, false);
    }

    private void move(final PortfolioStructure structure, final AbstractArtefact artefact, final boolean up) {
        if (artefact == null || structure == null) {
            throw new NullPointerException();
        }
        if (structure instanceof EPStructureElement) {
            // save eventual changes
            dbInstance.updateObject(structure);
            // reconnect to the session
            final EPStructureElement structureEl = (EPStructureElement) dbInstance.loadObject((EPStructureElement) structure);
            final List<EPStructureToArtefactLink> artefactLinks = structureEl.getInternalArtefacts();
            final int index = indexOf(artefactLinks, artefact);
            if (up && index > 0) {
                // swap the link with the previous link in the list
                Collections.swap(artefactLinks, index, index - 1);
                dbInstance.updateObject(structureEl);
            } else if (!up && (index >= 0 && index < (artefactLinks.size() - 1))) {
                // swap the link with the next link in the list
                Collections.swap(artefactLinks, index, index + 1);
                dbInstance.updateObject(structureEl);
            }
        }
    }

    private int indexOf(final List<EPStructureToArtefactLink> artefactLinks, final AbstractArtefact artefact) {
        int count = 0;
        for (final EPStructureToArtefactLink link : artefactLinks) {
            if (link.getArtefact().getKey().equals(artefact.getKey())) {
                return count;
            }
            count++;
        }
        return -1;
    }

    /**
     * Add a child structure to the parent structure.
     * 
     * @param parentStructure
     * @param childStructure
     */
    @Override
    public void addStructureToStructure(PortfolioStructure parentStructure, final PortfolioStructure childStructure) {
        if (parentStructure == null || childStructure == null) {
            throw new NullPointerException();
        }
        if (childStructure instanceof EPStructureElement) {
            // save eventual changes
            dbInstance.updateObject(parentStructure);
            // reconnect to the session
            parentStructure = (EPStructureElement) dbInstance.loadObject((EPStructureElement) parentStructure);
            final EPStructureToStructureLink link = new EPStructureToStructureLink();
            link.setParent(parentStructure);
            link.setChild(childStructure);

            // refresh internal link to its root element
            ((EPStructureElement) childStructure).setRoot((EPStructureElement) parentStructure);

            ((EPStructureElement) parentStructure).getInternalChildren().add(link);
        }
    }

    @Override
    public boolean moveStructureToNewParentStructure(final PortfolioStructure structToBeMvd, final PortfolioStructure oldParStruct, final PortfolioStructure newParStruct) {
        if (structToBeMvd == null || oldParStruct == null || newParStruct == null) {
            throw new NullPointerException();
        }
        try { // try catch, as used in d&d TOC-tree, should still continue on error
            removeStructure(oldParStruct, structToBeMvd);
            addStructureToStructure(newParStruct, structToBeMvd);
        } catch (final Exception e) {
            log.error("could not move structure " + structToBeMvd.getKey() + " from " + oldParStruct.getKey() + " to " + newParStruct.getKey(), e);
            return false;
        }
        return true;
    }

    @Override
    public void deleteRootStructure(final PortfolioStructure rootStructure) {
        if (rootStructure == null) {
            throw new NullPointerException();
        }
        if (rootStructure.getKey() == null) {
            return;
        }
        if (rootStructure instanceof EPStructureElement) {
            dbInstance.deleteObject(rootStructure);
        }

    }

    /**
     * Remove a child structure from its parent structure.
     * 
     * @param parentStructure
     * @param childStructure
     */

    // this has to be done recursively for pages, structs also!
    // also remove the artefacts from each!
    @Override
    public void removeStructure(final PortfolioStructure parentStructure, final PortfolioStructure childStructure) {
        if (childStructure == null) {
            throw new NullPointerException();
        }
        if (childStructure.getKey() == null) {
            return;// child not persisted
        }
        if (parentStructure == null) {
            return; // cannot remove with no parent!
        }
        if (childStructure instanceof EPStructureElement) {
            // save eventual changes
            dbInstance.updateObject(parentStructure);
            // reconnect to the session

            EPStructureToStructureLink linkToDelete = null;
            final EPStructureElement parentStructureEl = (EPStructureElement) dbInstance.loadObject((EPStructureElement) parentStructure);
            for (final Iterator<EPStructureToStructureLink> linkIt = parentStructureEl.getInternalChildren().iterator(); linkIt.hasNext();) {
                final EPStructureToStructureLink link = linkIt.next();

                // List<AbstractArtefact> thisStructsArtefacts = getArtefacts(link.getChild());
                // for (AbstractArtefact artefact : thisStructsArtefacts) {
                // removeArtefactFromStructure(artefact, link.getChild());
                // }

                if (link.getChild().getKey().equals(childStructure.getKey())) {
                    linkIt.remove();
                    linkToDelete = link;
                    break;
                }
            }

            // I have not set the cascade all delete
            if (linkToDelete != null) {
                dbInstance.updateObject(parentStructureEl);
                dbInstance.deleteObject(linkToDelete);
            }
        }
        if (parentStructure == childStructure) {
            deleteRootStructure(childStructure);
            return;
        }
    }

    /**
     * This method is only for templates.
     * 
     * @param res
     */
    @Override
    public void deletePortfolioMapTemplate(final OLATResourceable res) {
        final PortfolioStructure map = loadPortfolioStructure(res);
        if (map == null) {
            return;// nothing to delete
        }
        removeStructureRecursively(map);
        deletePortfolioMapTemplateRecursively((EPStructureElement) map);
        dbInstance.deleteObject(map);
    }

    private void deletePortfolioMapTemplateRecursively(final EPStructureElement element) {
        element.getInternalArtefacts().clear();
        element.setRoot(null);
        element.setRootMap(null);
        final List<EPStructureToStructureLink> links = element.getInternalChildren();
        for (final EPStructureToStructureLink subLink : links) {
            deletePortfolioMapTemplateRecursively((EPStructureElement) subLink.getChild());
        }
        links.clear();
    }

    @Override
    public void removeStructureRecursively(PortfolioStructure struct) {
        final List<PortfolioStructure> children = loadStructureChildren(struct);
        for (final PortfolioStructure childstruct : children) {
            removeStructureRecursively(childstruct);
        }
        // remove artefact-links
        final List<AbstractArtefact> thisStructsArtefacts = getArtefacts(struct);
        for (final AbstractArtefact artefact : thisStructsArtefacts) {
            removeArtefactFromStructure(artefact, struct, false);
        }

        // remove from parent
        final PortfolioStructure parent = loadStructureParent(struct);
        removeStructure(parent, struct);

        // remove structure itself
        struct = (EPStructureElement) dbInstance.loadObject((EPStructureElement) struct);
        dbInstance.deleteObject(struct);
    }

    /**
     * Move a structure element up in the list
     * 
     * @param parentStructure
     * @param childStructure
     */
    @Override
    public void moveUp(final PortfolioStructure parentStructure, final PortfolioStructure childStructure) {
        move(parentStructure, childStructure, true);
    }

    /**
     * Move a structure element down in the list and save the parent and the list
     * 
     * @param parentStructure
     * @param childStructure
     */
    @Override
    public void moveDown(final PortfolioStructure parentStructure, final PortfolioStructure childStructure) {
        move(parentStructure, childStructure, false);
    }

    private void move(final PortfolioStructure parentStructure, final PortfolioStructure childStructure, final boolean up) {
        if (childStructure == null || parentStructure == null) {
            throw new NullPointerException();
        }
        if (parentStructure instanceof EPStructureElement) {
            // save eventual changes
            dbInstance.updateObject(parentStructure);
            // reconnect to the session
            final EPStructureElement structureEl = (EPStructureElement) dbInstance.loadObject((EPStructureElement) parentStructure);
            final List<EPStructureToStructureLink> structureLinks = structureEl.getInternalChildren();
            final int index = indexOf(structureLinks, childStructure);
            if (up && index > 0) {
                // swap the link with the previous link in the list
                Collections.swap(structureLinks, index, index - 1);
                dbInstance.updateObject(structureEl);
            } else if (!up && (index >= 0 && index < (structureLinks.size() - 1))) {
                // swap the link with the next link in the list
                Collections.swap(structureLinks, index, index + 1);
                dbInstance.updateObject(structureEl);
            }
        }
    }

    private int indexOf(final List<EPStructureToStructureLink> artefactLinks, final PortfolioStructure structure) {
        int count = 0;
        for (final EPStructureToStructureLink link : artefactLinks) {
            if (link.getChild().getKey().equals(structure.getKey())) {
                return count;
            }
            count++;
        }
        return -1;
    }

    @Override
    public void copyStructureRecursively(final PortfolioStructure source, final PortfolioStructure target, final boolean withArtefacts) {
        // all changes are overwritten
        final EPStructureElement targetEl = (EPStructureElement) target;
        if (targetEl instanceof EPStructuredMap) {
            ((EPStructuredMap) targetEl).setCopyDate(new Date());
        }

        // update the source
        dbInstance.updateObject(source);
        // reconnect to the session
        final EPStructureElement sourceEl = (EPStructureElement) source;
        targetEl.setStyle(sourceEl.getStyle());
        copyEPStructureElementRecursively(sourceEl, targetEl, withArtefacts);
    }

    private void copyEPStructureElementRecursively(EPStructureElement sourceEl, final EPStructureElement targetEl, final boolean withArtefacts) {
        // needed if the sourceEl come from a link. Hibernate doesn't initialize the list properly
        sourceEl = (EPStructureElement) dbInstance.loadObject(sourceEl);
        if (withArtefacts) {
            final List<EPStructureToArtefactLink> artefactLinks = sourceEl.getInternalArtefacts();
            for (final EPStructureToArtefactLink artefactLink : artefactLinks) {
                final EPStructureToArtefactLink link = instantiateClone(artefactLink);
                link.setStructureElement(targetEl);// make the pseudo
                targetEl.getInternalArtefacts().add(link); // bidirectional relations
            }
        }

        // clone the links
        final List<EPStructureToStructureLink> childLinks = sourceEl.getInternalChildren();
        for (final EPStructureToStructureLink childLink : childLinks) {
            copy(childLink, targetEl, withArtefacts, false);
        }

        savePortfolioStructure(targetEl);
    }

    /**
     * Sync the tree structure recursively with or without artefacts
     * 
     * @param sourceEl
     * @param targetEl
     * @param withArtefacts
     */
    @Override
    public void syncStructureRecursively(final PortfolioStructure source, final PortfolioStructure target, final boolean withArtefacts) {
        // all changes are overwritten
        final EPStructureElement sourceEl = (EPStructureElement) source;

        // update the source
        dbInstance.updateObject(target);
        // reconnect to the session
        final EPStructureElement targetEl = (EPStructureElement) dbInstance.loadObject((EPStructureElement) target);
        syncEPStructureElementRecursively(sourceEl, targetEl, withArtefacts);
    }

    /**
     * This sync method only sync the structure of the tree and no content. TODO: epf: SR sync collect restriction, title, description, representation-mode
     * (table/miniview) also pay attention to this on copy and import!
     * 
     * @param sourceEl
     * @param targetEl
     * @param withArtefacts
     */
    private void syncEPStructureElementRecursively(final EPStructureElement sourceEl, final EPStructureElement targetEl, final boolean withArtefacts) {
        if (withArtefacts) {
            syncArtefacts(sourceEl, targetEl);
        }

        final List<EPStructureToStructureLink> sourceRefLinks = new ArrayList<EPStructureToStructureLink>(sourceEl.getInternalChildren());
        final List<EPStructureToStructureLink> targetRefLinks = new ArrayList<EPStructureToStructureLink>(targetEl.getInternalChildren());

        final Comparator<EPStructureToStructureLink> COMPARATOR = new KeyStructureToStructureLinkComparator();

        // remove deleted elements
        final List<EPStructureToStructureLink> linksToDelete = new ArrayList<EPStructureToStructureLink>();
        for (final Iterator<EPStructureToStructureLink> targetIt = targetEl.getInternalChildren().iterator(); targetIt.hasNext();) {
            final EPStructureToStructureLink targetLink = targetIt.next();
            final int index = indexOf(sourceRefLinks, targetLink, COMPARATOR);
            if (index < 0) {
                linksToDelete.add(targetLink);
                targetIt.remove();
            }
        }

        // add new element
        final Set<Long> newSourceRefLinkKeys = new HashSet<Long>();
        for (final EPStructureToStructureLink sourceRefLink : sourceRefLinks) {
            final int index = indexOf(targetRefLinks, sourceRefLink, COMPARATOR);
            if (index < 0) {
                // create a new structure element
                copy(sourceRefLink, targetEl, withArtefacts, false);
                newSourceRefLinkKeys.add(sourceRefLink.getKey());
            }
        }

        // at this point, we must have the same content in the two list
        // but with perhaps other ordering: reorder
        final List<EPStructureToStructureLink> targetLinks = targetEl.getInternalChildren();
        for (int i = 0; i < sourceRefLinks.size(); i++) {
            final EPStructureToStructureLink sourceRefLink = sourceRefLinks.get(i);
            final int index = indexOf(targetLinks, sourceRefLink, COMPARATOR);
            if (index == i) {
                // great, right at its position
            } else if (index > i) {
                Collections.swap(targetLinks, i, index);
            } else {
                // not possible
            }

            // sync recursively
            if (index >= 0) {
                final EPStructureElement subSourceEl = (EPStructureElement) sourceRefLink.getChild();
                final EPStructureElement subTargetEl = (EPStructureElement) targetLinks.get(i).getChild();
                syncEPStructureElementRecursively(subSourceEl, subTargetEl, withArtefacts);
            }
        }

        dbInstance.updateObject(targetEl);
    }

    private int indexOf(final List<EPStructureToStructureLink> refLinks, final EPStructureToStructureLink link, final Comparator<EPStructureToStructureLink> comparator) {
        int count = 0;
        for (final EPStructureToStructureLink refLink : refLinks) {
            if (comparator.compare(refLink, link) == 0) {
                return count;
            }
            count++;
        }
        return -1;
    }

    private void syncArtefacts(final EPStructureElement sourceEl, final EPStructureElement targetEl) {
        final List<EPStructureToArtefactLink> artefactLinks = sourceEl.getInternalArtefacts();
        for (final EPStructureToArtefactLink artefactLink : artefactLinks) {
            // TODO
        }
    }

    /**
     * Copy/Import structure elements recursively
     * 
     * @param refLink
     * @param targetEl
     * @param withArtefacts
     *            Copy the artefacts
     * @param importEl
     *            Don't load elements from the DB
     */
    private void copy(final EPStructureToStructureLink refLink, final EPStructureElement targetEl, final boolean withArtefacts, final boolean importEl) {
        final EPStructureElement childSourceEl = (EPStructureElement) refLink.getChild();
        final EPStructureElement clonedChildEl = instantiateClone(refLink.getChild());
        if (clonedChildEl == null) {
            log.warn("Attempt to clone an unsupported structure type: " + refLink.getChild(), null);
        } else {
            final OLATResource resource = resourceManager.createOLATResourceInstance(clonedChildEl.getClass());
            clonedChildEl.setOlatResource(resource);
            // set root
            if (targetEl.getRoot() == null) {
                // it's the root element
                clonedChildEl.setRoot(targetEl);
            } else {
                clonedChildEl.setRoot(targetEl.getRoot());
            }
            if (targetEl.getRootMap() == null && targetEl instanceof PortfolioStructureMap) {
                clonedChildEl.setRootMap((PortfolioStructureMap) targetEl);
            } else {
                clonedChildEl.setRootMap(targetEl.getRootMap());
            }
            clonedChildEl.setStructureElSource(childSourceEl.getKey());

            copyCollectRestriction(childSourceEl, clonedChildEl);
            if (importEl) {
                importEPStructureElementRecursively(childSourceEl, clonedChildEl);
            } else {
                copyEPStructureElementRecursively(childSourceEl, clonedChildEl, withArtefacts);
            }

            final EPStructureToStructureLink link = new EPStructureToStructureLink();
            link.setParent(targetEl);
            link.setChild(clonedChildEl);
            targetEl.getInternalChildren().add(link);
        }
    }

    private EPStructureToArtefactLink instantiateClone(final EPStructureToArtefactLink link) {
        final EPStructureToArtefactLink clone = new EPStructureToArtefactLink();
        clone.setArtefact(link.getArtefact());
        clone.setAuthor(link.getAuthor());
        clone.setCreationDate(new Date());
        clone.setReflexion(link.getReflexion());
        return clone;
    }

    private EPStructureElement instantiateClone(final PortfolioStructure source) {
        EPStructureElement targetEl = null;
        // don't forget the inheritence
        if (source instanceof EPPage) {
            targetEl = new EPPage();
            targetEl.setTitle(((EPPage) source).getTitle());
            targetEl.setDescription(((EPPage) source).getDescription());
        } else if (source instanceof EPStructureElement) {
            targetEl = new EPStructureElement();
            targetEl.setTitle(((EPStructureElement) source).getTitle());
            targetEl.setDescription(((EPStructureElement) source).getDescription());
        }
        return targetEl;
    }

    private void copyCollectRestriction(final PortfolioStructure source, final PortfolioStructure target) {
        if (source == null || target == null || source.getCollectRestrictions() == null || source.getCollectRestrictions().isEmpty()) {
            return;
        }

        final List<CollectRestriction> targetRestrictions = target.getCollectRestrictions();
        for (final CollectRestriction sourceRestriction : source.getCollectRestrictions()) {
            final CollectRestriction targetRestriction = new CollectRestriction();
            targetRestriction.setArtefactType(sourceRestriction.getArtefactType());
            targetRestriction.setAmount(sourceRestriction.getAmount());
            targetRestriction.setRestriction(sourceRestriction.getRestriction());
            targetRestrictions.add(targetRestriction);
        }
    }

    @Override
    public boolean isTemplateInUse(final PortfolioStructureMap template, final OLATResourceable targetOres, final String targetSubPath, final String targetBusinessPath) {
        final StringBuilder sb = new StringBuilder();
        sb.append("select count(map) from ").append(EPStructuredMap.class.getName()).append(" map").append(" where map.structuredMapSource=:template");
        if (targetOres != null) {
            sb.append(" and map.targetResource.resourceableId=:resourceId").append(" and map.targetResource.resourceableTypeName=:resourceType");
        }
        if (targetSubPath != null) {
            sb.append(" and map.targetResource.subPath=:subPath");
        }
        if (targetBusinessPath != null) {
            sb.append(" and map.targetResource.businessPath=:businessPath");
        }

        final DBQuery query = dbInstance.createQuery(sb.toString());
        query.setEntity("template", template);
        if (targetOres != null) {
            query.setLong("resourceId", targetOres.getResourceableId());
            query.setString("resourceType", targetOres.getResourceableTypeName());
        }
        if (targetSubPath != null) {
            query.setString("subPath", targetSubPath);
        }
        if (targetBusinessPath != null) {
            query.setString("businessPath", targetBusinessPath);
        }

        final Number count = (Number) query.uniqueResult();
        return count.intValue() > 0;
    }

    @Override
    public PortfolioStructureMap loadPortfolioStructuredMap(final Identity identity, final PortfolioStructureMap template, final OLATResourceable targetOres,
            final String targetSubPath, final String targetBusinessPath) {
        if (template == null) {
            throw new NullPointerException();
        }
        if (!(template instanceof EPStructuredMapTemplate)) {
            throw new AssertException("Only template are acceptable");
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("select map from ").append(EPStructuredMap.class.getName()).append(" map").append(" where map.structuredMapSource=:template");
        if (targetOres != null) {
            sb.append(" and map.targetResource.resourceableId=:resourceId").append(" and map.targetResource.resourceableTypeName=:resourceType");
        }
        if (targetSubPath != null) {
            sb.append(" and map.targetResource.subPath=:subPath");
        }
        if (targetBusinessPath != null) {
            sb.append(" and map.targetResource.businessPath=:businessPath");
        }
        sb.append(" and map.ownerGroup in ( ").append("select sgi.key from").append(" org.olat.data.basesecurity.SecurityGroupImpl as sgi,")
                .append(" org.olat.data.basesecurity.SecurityGroupMembershipImpl as sgmsi ").append(" where sgmsi.securityGroup = sgi and sgmsi.identity =:ident")
                .append(" )");

        final DBQuery query = dbInstance.createQuery(sb.toString());
        query.setEntity("template", template);
        query.setEntity("ident", identity);
        if (targetOres != null) {
            query.setLong("resourceId", targetOres.getResourceableId());
            query.setString("resourceType", targetOres.getResourceableTypeName());
        }
        if (targetSubPath != null) {
            query.setString("subPath", targetSubPath);
        }
        if (targetBusinessPath != null) {
            query.setString("businessPath", targetBusinessPath);
        }

        @SuppressWarnings("unchecked")
        final List<PortfolioStructureMap> maps = query.list();
        // if not found, it is an empty list
        if (maps.isEmpty()) {
            return null;
        }
        return maps.get(0);
    }

    /**
     * Load the repository entry of a template with the map key
     * 
     * @param key
     *            The template key
     * @return The repository entry
     */
    @Override
    public RepositoryEntry loadPortfolioRepositoryEntryByMapKey(final Long key) {
        if (key == null) {
            throw new NullPointerException();
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("select repo from ").append(RepositoryEntry.class.getName()).append(" repo").append(" where repo.olatResource in (select map.olatResource from ")
                .append(EPStructuredMapTemplate.class.getName()).append(" map where map.key=:key").append(")");

        final DBQuery query = dbInstance.createQuery(sb.toString());
        query.setLong("key", key);

        @SuppressWarnings("unchecked")
        final List<RepositoryEntry> entries = query.list();
        // if not found, it is an empty list
        if (entries.isEmpty()) {
            return null;
        }
        return entries.get(0);
    }

    /**
     * @param olatResourceable
     *            cannot be null
     * @return The structure element or null if not found
     */
    @Override
    public PortfolioStructure loadPortfolioStructure(final OLATResourceable olatResourceable) {
        if (olatResourceable == null) {
            throw new NullPointerException();
        }

        final OLATResource resource = resourceManager.findResourceable(olatResourceable);
        if (resource == null) {
            return null;
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("select element from ").append(EPStructureElement.class.getName()).append(" element").append(" where element.olatResource=:resource");

        final DBQuery query = dbInstance.createQuery(sb.toString());
        query.setEntity("resource", resource);

        @SuppressWarnings("unchecked")
        final List<PortfolioStructure> resources = query.list();
        // if not found, it is an empty list
        if (resources.isEmpty()) {
            return null;
        }
        return resources.get(0);
    }

    /**
     * Load a portfolio structure by its primary key
     * 
     * @param key
     *            cannot be null
     * @return The structure element or null if not found
     */
    // FIXME: epf: SR: error loading structures without olatresource!
    @Override
    public PortfolioStructure loadPortfolioStructureByKey(final Long key) {
        if (key == null) {
            throw new NullPointerException();
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("select element from ").append(EPStructureElement.class.getName()).append(" element").append(" where element.key=:key");

        final DBQuery query = dbInstance.createQuery(sb.toString());
        query.setLong("key", key);

        @SuppressWarnings("unchecked")
        final List<PortfolioStructure> resources = query.list();
        // if not found, it is an empty list
        if (resources.isEmpty()) {
            return null;
        }
        return resources.get(0);
    }

    /**
     * Create a basic structure element
     * 
     * @param title
     * @param description
     * @return The structure element
     */
    @Override
    public PortfolioStructure createPortfolioStructure(final PortfolioStructure root, final String title, final String description) {
        final EPStructureElement el = new EPStructureElement();
        el.setRoot((EPStructureElement) root);
        if (root != null && root.getRootMap() == null && root instanceof PortfolioStructureMap) {
            el.setRootMap((PortfolioStructureMap) root);
        } else if (root != null) {
            el.setRootMap(root.getRootMap());
        }
        return fillStructureElement(el, title, description);
    }

    /**
     * Create a page element
     * 
     * @param title
     * @param description
     * @return The structure element
     */
    @Override
    public PortfolioStructure createPortfolioPage(final PortfolioStructure root, final String title, final String description) {
        final EPPage el = new EPPage();
        el.setRoot((EPStructureElement) root);
        if (root != null && root.getRootMap() == null && root instanceof PortfolioStructureMap) {
            el.setRootMap((PortfolioStructureMap) root);
        } else if (root != null) {
            el.setRootMap(root.getRootMap());
        }
        return fillStructureElement(el, title, description);
    }

    @Override
    public PortfolioStructureMap createPortfolioStructuredMap(final PortfolioStructureMap template, final Identity identity, final String title,
            final String description, final OLATResourceable targetOres, final String targetSubPath, final String targetBusinessPath) {
        final EPStructuredMap el = new EPStructuredMap();
        el.setStructuredMapSource((EPStructuredMapTemplate) template);
        el.setStructureElSource(template.getKey());

        if (template != null) {
            copyCollectRestriction(template, el);
        }

        final EPTargetResource targetResource = el.getTargetResource();
        if (targetOres != null) {
            targetResource.setResourceableId(targetOres.getResourceableId());
            targetResource.setResourceableTypeName(targetOres.getResourceableTypeName());
        }
        if (StringHelper.containsNonWhitespace(targetSubPath)) {
            targetResource.setSubPath(targetSubPath);
        }
        if (StringHelper.containsNonWhitespace(targetBusinessPath)) {
            targetResource.setBusinessPath(targetBusinessPath);
        }

        fillStructureElement(el, title, description);

        // create security group
        final SecurityGroup ownerGroup = createSecurityGroup(el, identity);
        el.setOwnerGroup(ownerGroup);
        return el;
    }

    @Override
    public PortfolioStructureMap createPortfolioDefaultMap(final Identity identity, final String title, final String description) {
        final EPDefaultMap el = new EPDefaultMap();

        fillStructureElement(el, title, description);

        // create security group
        final SecurityGroup ownerGroup = createSecurityGroup(el, identity);
        el.setOwnerGroup(ownerGroup);

        return el;
    }

    @Override
    public PortfolioStructureMap createPortfolioDefaultMap(final BusinessGroup group, final String title, final String description) {
        final EPDefaultMap el = new EPDefaultMap();

        // don't create security group for map linked to a group
        // SecurityGroup ownerGroup = group.getOwnerGroup();
        // el.setOwnerGroup(ownerGroup);

        fillStructureElement(el, title, description);
        return el;
    }

    private EPStructureElement fillStructureElement(final EPStructureElement el, final String title, final String description) {
        el.setTitle(title);
        el.setDescription(description);
        final OLATResource resource = resourceManager.createOLATResourceInstance(el.getClass());
        el.setOlatResource(resource);
        dbInstance.saveObject(resource);
        return el;
    }

    /**
     * Create a map template, create an OLAT resource and a repository entry with a security group of type owner to the repository and add the identity has an owner.
     * 
     * @param identity
     * @param title
     * @param description
     * @return The structure element
     */
    @Override
    public PortfolioStructureMap createPortfolioMapTemplate(final Identity identity, final String title, final String description) {
        final EPStructuredMapTemplate el = new EPStructuredMapTemplate();

        fillStructureElement(el, title, description);

        // create security group
        final SecurityGroup ownerGroup = createSecurityGroup(el, identity);
        el.setOwnerGroup(ownerGroup);

        // create a repository entry with default security settings
        createRepositoryEntry(identity, ownerGroup, el.getOlatResource(), title);
        return el;
    }

    /**
     * Import the structure.
     * 
     * @param root
     * @param identity
     * @return
     */
    @Override
    public PortfolioStructureMap importPortfolioMapTemplate(final PortfolioStructure root, final Identity identity) {
        final EPStructuredMapTemplate el = new EPStructuredMapTemplate();

        fillStructureElement(el, root.getTitle(), root.getDescription());

        importEPStructureElementRecursively((EPStructureElement) root, el);

        // create an empty security group
        final SecurityGroup ownerGroup = securityManager.createAndPersistSecurityGroup();
        el.setOwnerGroup(ownerGroup);

        return el;
    }

    private void importEPStructureElementRecursively(final EPStructureElement sourceEl, final EPStructureElement targetEl) {

        // clone the links
        final List<EPStructureToStructureLink> childLinks = sourceEl.getInternalChildren();
        for (final EPStructureToStructureLink childLink : childLinks) {
            copy(childLink, targetEl, false, true);
        }

        savePortfolioStructure(targetEl);
    }

    /**
     * Create an OLAT Resource with the type of a template map.
     * 
     * @return
     */
    @Override
    public OLATResource createPortfolioMapTemplateResource() {
        final OLATResource resource = resourceManager.createOLATResourceInstance(EPStructuredMapTemplate.class);
        return resource;
    }

    /**
     * Create a template map with the given repsoitory entry and olat resource (in the repository entry). The repository entry must already be persisted.
     * 
     * @param identity
     * @param entry
     * @return
     */
    @Override
    public PortfolioStructureMap createAndPersistPortfolioMapTemplateFromEntry(final Identity identity, final RepositoryEntry entry) {
        EPStructuredMapTemplate el = (EPStructuredMapTemplate) loadPortfolioStructure(entry.getOlatResource());
        if (el == null) {
            el = new EPStructuredMapTemplate();
        }
        el.setTitle(entry.getDisplayname());
        el.setDescription(entry.getDescription());
        el.setOlatResource(entry.getOlatResource());

        // create security group
        SecurityGroup ownerGroup = entry.getOwnerGroup();
        if (ownerGroup == null) {
            ownerGroup = createSecurityGroup(el, identity);
        }
        el.setOwnerGroup(ownerGroup);

        dbInstance.saveObject(el);
        return el;
    }

    /**
     * Add an author to the repository entry linked to the map
     * 
     * @param map
     * @param author
     */
    @Override
    public void addAuthor(final PortfolioStructureMap map, final Identity author) {
        if (map instanceof EPStructuredMapTemplate) {
            final EPStructuredMapTemplate mapImpl = (EPStructuredMapTemplate) map;
            final RepositoryEntry re = repositoryDao.lookupRepositoryEntry(mapImpl.getOlatResource(), true);
            final SecurityGroup ownerGroup = re.getOwnerGroup();
            if (!securityManager.isIdentityInSecurityGroup(author, ownerGroup)) {
                securityManager.addIdentityToSecurityGroup(author, ownerGroup);
                dbInstance.updateObject(ownerGroup);
            }
        }
    }

    /**
     * Remove an author to repository entry linked to the map
     * 
     * @param map
     * @param author
     */
    @Override
    public void removeAuthor(final PortfolioStructureMap map, final Identity author) {
        if (map instanceof EPStructuredMapTemplate) {
            final EPStructuredMapTemplate mapImpl = (EPStructuredMapTemplate) map;
            final RepositoryEntry re = repositoryDao.lookupRepositoryEntry(mapImpl.getOlatResource(), true);
            final SecurityGroup ownerGroup = re.getOwnerGroup();
            if (securityManager.isIdentityInSecurityGroup(author, ownerGroup)) {
                securityManager.removeIdentityFromSecurityGroup(author, ownerGroup);
                dbInstance.updateObject(ownerGroup);
            }
        }
    }

    private void createRepositoryEntry(final Identity identity, SecurityGroup ownerGroup, final OLATResource oresable, final String title) {
        // create a repository entry
        final RepositoryEntry addedEntry = repositoryDao.createRepositoryEntryInstance(identity.getName());
        addedEntry.setCanDownload(false);
        addedEntry.setCanLaunch(true);
        addedEntry.setDisplayname(title);
        addedEntry.setResourcename("-");
        // Do set access for owner at the end, because unfinished course should be invisible
        addedEntry.setAccess(RepositoryEntry.ACC_OWNERS);

        if (ownerGroup == null) {
            // create security group
            ownerGroup = securityManager.createAndPersistSecurityGroup();
            // create olat resource for the security group
            final OLATResource ownerGroupResource = resourceManager.createOLATResourceInstance(ownerGroup);
            resourceManager.saveOLATResource(ownerGroupResource);
            // member of this group may modify member's membership
            securityManager.createAndPersistPolicy(ownerGroup, Constants.PERMISSION_ACCESS, ownerGroup);
            // members of this group are always authors also
            securityManager.createAndPersistPolicy(ownerGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_AUTHOR);
            securityManager.addIdentityToSecurityGroup(identity, ownerGroup);
        }
        addedEntry.setOwnerGroup(ownerGroup);

        // Set the resource on the repository entry and save the entry.
        // bind resource and repository entry
        addedEntry.setOlatResource(oresable);
        resourceManager.saveOLATResource(oresable);
        repositoryDao.saveRepositoryEntry(addedEntry);
    }

    private SecurityGroup createSecurityGroup(final EPAbstractMap map, final Identity author) {
        // create security group
        final SecurityGroup ownerGroup = securityManager.createAndPersistSecurityGroup();
        // create olat resource for the security group
        final OLATResource ownerGroupResource = resourceManager.createOLATResourceInstance(ownerGroup);
        resourceManager.saveOLATResource(ownerGroupResource);
        // member of this group may modify member's membership
        securityManager.createAndPersistPolicyWithResource(ownerGroup, Constants.PERMISSION_ACCESS, map.getOlatResource());
        securityManager.addIdentityToSecurityGroup(author, ownerGroup);
        return ownerGroup;
    }

    /**
     * Add or update a restriction to the collection of artefacts for a given structure element
     * 
     * @param structure
     * @param artefactType
     * @param restriction
     * @param amount
     */
    @Override
    public void addCollectRestriction(final PortfolioStructure structure, final String artefactType, final String restriction, final int amount) {
        if (structure == null) {
            throw new NullPointerException("Structure cannot be null");
        }

        final EPStructureElement structEl = (EPStructureElement) structure;
        final List<CollectRestriction> restrictions = structEl.getCollectRestrictions();

        final CollectRestriction cr = new CollectRestriction();
        cr.setArtefactType(artefactType);
        cr.setRestriction(restriction);
        cr.setAmount(amount);
        restrictions.add(cr);
    }

    @Override
    public void submitMap(final EPStructuredMap map) {
        map.setStatus(StructureStatusEnum.CLOSED);
        map.setReturnDate(new Date());
        dbInstance.updateObject(map);
    }

    @Override
    public void savePortfolioStructure(final PortfolioStructure portfolioStructure) {
        if (portfolioStructure instanceof PersistentObject) {
            final PersistentObject persistentStructure = (PersistentObject) portfolioStructure;
            if (persistentStructure.getKey() == null) {
                dbInstance.saveObject(portfolioStructure.getOlatResource());
                dbInstance.saveObject(portfolioStructure);
            } else {
                dbInstance.updateObject(portfolioStructure);
            }
        }
    }

    private static class KeyStructureToStructureLinkComparator implements Comparator<EPStructureToStructureLink>, Serializable {
        public KeyStructureToStructureLinkComparator() {
            //
        }

        @Override
        public int compare(final EPStructureToStructureLink o1, final EPStructureToStructureLink o2) {
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }

            final PortfolioStructure ps1 = o1.getChild();
            final PortfolioStructure ps2 = o2.getChild();
            if (ps1 instanceof EPStructureElement && ps2 instanceof EPStructureElement) {
                final EPStructureElement eps1 = (EPStructureElement) ps1;
                final EPStructureElement eps2 = (EPStructureElement) ps2;

                final Long t1 = eps1.getStructureElSource() == null ? eps1.getKey() : eps1.getStructureElSource();
                final Long t2 = eps2.getStructureElSource() == null ? eps2.getKey() : eps2.getStructureElSource();

                if (t1 == null) {
                    return -1;
                }
                if (t2 == null) {
                    return 1;
                }
                return t1.compareTo(t2);
            }
            return -1;
        }
    }

    @Override
    public boolean setReflexionForArtefactToStructureLink(final AbstractArtefact artefact, final PortfolioStructure structure, final String reflexion) {
        // EPStructureElement structureEl = (EPStructureElement) structure;
        final EPStructureElement structureEl = (EPStructureElement) dbInstance.loadObject((EPStructureElement) structure);
        final List<EPStructureToArtefactLink> links = structureEl.getInternalArtefacts();
        boolean changed = false;
        for (final EPStructureToArtefactLink epStructureToArtefactLink : links) {
            if (epStructureToArtefactLink.getArtefact().getKey().equals(artefact.getKey())) {
                epStructureToArtefactLink.setReflexion(reflexion);
                dbInstance.updateObject(epStructureToArtefactLink);
                changed = true;
                break;
            }
        }
        // savePortfolioStructure(structure);
        return changed;
    }

    @Override
    public String getReflexionForArtefactToStructureLink(final AbstractArtefact artefact, final PortfolioStructure structure) {
        if (structure == null) {
            return null;
        }
        final EPStructureElement structureEl = (EPStructureElement) dbInstance.loadObject((EPStructureElement) structure);
        final List<EPStructureToArtefactLink> links = structureEl.getInternalArtefacts();
        for (final EPStructureToArtefactLink epStructureToArtefactLink : links) {
            if (epStructureToArtefactLink.getArtefact().getKey().equals(artefact.getKey())) {
                return epStructureToArtefactLink.getReflexion();
            }
        }
        return null;
    }

    public void addStructureToStructure(PortfolioStructure parentStructure, final PortfolioStructure childStructure, int destinationPos) {
        if (parentStructure == null || childStructure == null) {
            throw new NullPointerException();
        }
        if (childStructure instanceof EPStructureElement) {
            // save eventual changes
            dbInstance.updateObject(parentStructure);
            // reconnect to the session
            parentStructure = (EPStructureElement) dbInstance.loadObject((EPStructureElement) parentStructure);
            final EPStructureToStructureLink link = new EPStructureToStructureLink();
            link.setParent(parentStructure);
            link.setChild(childStructure);

            // refresh internal link to its root element
            ((EPStructureElement) childStructure).setRoot((EPStructureElement) parentStructure);

            if (destinationPos == -1) {
                ((EPStructureElement) parentStructure).getInternalChildren().add(link);
            } else {
                ((EPStructureElement) parentStructure).getInternalChildren().add(destinationPos, link);
            }
        }
    }

    public Integer[] getRestrictionStatisticsOfMap(PortfolioStructure structureMap, int done, int todo) {
        final List<PortfolioStructure> children = loadStructureChildren(structureMap);
        for (final PortfolioStructure child : children) {
            Integer[] childStat = getRestrictionStatisticsOfMap(child, done, todo);
            done = childStat[0];
            todo = childStat[1];
        }
        // summarize
        Integer[] statsArr = getRestrictionStatistics(structureMap);
        if (statsArr != null) {
            done += statsArr[0];
            todo += statsArr[1];
        }

        return new Integer[] { done, todo };
    }

    protected Integer[] getRestrictionStatistics(PortfolioStructure structure) {
        if (structure instanceof EPStructureElement) {
            EPStructureElement structEl = (EPStructureElement) structure;
            structEl = (EPStructureElement) loadPortfolioStructureByKey(structEl.getKey());
            final List<CollectRestriction> restrictions = structEl.getCollectRestrictions();

            if (restrictions != null && !restrictions.isEmpty()) {
                int todo = 0;
                int done = 0;
                List<AbstractArtefact> artefacts = getArtefacts(structEl);
                for (CollectRestriction cR : restrictions) {
                    if (RestrictionsConstants.MIN.equals(cR.getRestriction()) || RestrictionsConstants.EQUAL.equals(cR.getRestriction())) {
                        todo += cR.getAmount();
                        int actualCRCount = countRestrictionType(artefacts, cR);
                        done += actualCRCount;
                    }
                }
                return new Integer[] { done, todo };
            }
        }
        return null;
    }

    public boolean reOrderStructures(PortfolioStructure parent, PortfolioStructure orderSubject, int orderDest) {
        EPStructureElement structureEl = (EPStructureElement) dbInstance.loadObject((EPStructureElement) parent);
        List<EPStructureToStructureLink> structureLinks = structureEl.getInternalChildren();

        int oldPos = indexOf(structureLinks, orderSubject);
        if (oldPos != orderDest && oldPos != -1) {
            EPStructureToStructureLink link = structureLinks.remove(oldPos);
            while (orderDest > structureLinks.size())
                orderDest--; // place at end
            structureLinks.add(orderDest, link);
            dbInstance.updateObject(structureEl);
            return true;
        }
        return false;
    }

}
