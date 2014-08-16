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
package org.olat.lms.group.context;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Constants;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.group.BusinessGroup;
import org.olat.data.group.area.BGArea;
import org.olat.data.group.area.BGAreaDao;
import org.olat.data.group.context.BGContext;
import org.olat.data.group.context.BGContextDao;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.CourseModule;
import org.olat.lms.course.ICourse;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.repository.RepositoryService;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * TODO: Class Description for BusinessGroupAreaServiceImpl
 * 
 * <P>
 * Initial Date: 27.06.2011 <br>
 * 
 * @author guido
 */
@Service
public class BusinessGroupContextServiceImpl implements BusinessGroupContextService {

    private static final Logger log = LoggerHelper.getLogger();

    protected BusinessGroupContextServiceImpl() {
    }

    @Autowired
    BGAreaDao areaManager;
    @Autowired
    BusinessGroupService businessGroupService;
    @Autowired
    BGContextDao bgContextDao;
    @Autowired
    BGAreaDao bgAreaDao;
    @Autowired
    BaseSecurity baseSecurity;
    @Autowired
    RepositoryService repositoryService;

    /**
     * org.olat.data.group.context.BGContext)
     */
    @Override
    public BGContext copyAndAddBGContextToResource(final String contextName, final OLATResource resource, final BGContext originalBgContext) {
        if (!originalBgContext.isDefaultContext()) {
            throw new AssertException("Can only copy default contexts");
        }

        // 1. Copy context as default context. Owner group of original context will
        // not be
        // copied since this is a default context
        final BGContext targetContext = createAndAddBGContextToResource(contextName, resource, originalBgContext.getGroupType(), null, true);
        // 2. Copy areas
        final Map areas = areaManager.copyBGAreasOfBGContext(originalBgContext, targetContext);
        // 3. Copy Groups
        // only group configuration will be copied, no group members are copied
        final List origGroups = bgContextDao.getGroupsOfBGContext(originalBgContext);
        final Iterator iter = origGroups.iterator();
        while (iter.hasNext()) {
            final BusinessGroup origGroup = (BusinessGroup) iter.next();
            businessGroupService.copyBusinessGroup(origGroup, origGroup.getName(), origGroup.getDescription(), origGroup.getMinParticipants(),
                    origGroup.getMaxParticipants(), targetContext, areas, true, true, true, false, false, true, false);
        }
        return targetContext;
    }

    /**
     * org.olat.data.basesecurity.Identity, boolean)
     */
    @Override
    public BGContext createAndAddBGContextToResource(final String contextName, final OLATResource resource, final String groupType, final Identity initialOwner,
            final boolean defaultContext) {
        final BGContext context = createAndPersistBGContext(contextName, null, groupType, initialOwner, defaultContext);
        addBGContextToResource(context, resource);
        return context;
    }

    @Override
    public void addBGContextToResource(final BGContext bgContext, final OLATResource resource) {
        bgContextDao.getBGContext2ResourceAndSave(resource, bgContext);
        // update course context list in this course resource
        if (resource.getResourceableTypeName().equals(CourseModule.getCourseTypeName())) {
            final ICourse course = CourseFactory.loadCourse(resource);
        } else if (resource.getResourceableTypeName().equals("junitcourse")) {
            // do nothing when in junit test mode
        } else {
            throw new AssertException("Currently only course resources allowed in resource to context relations.");
        }
        log.info("Audit:Added Business Group Context to OLATResource " + resource.toString() + bgContext.toString());
    }

    public void updateBGContext(final BGContext bgContext) {
        // 1) update context
        bgContextDao.updateBGContext(bgContext);
        // 2) reload course contexts for all courses wher this context is used
        final List resources = bgContextDao.findOLATResourcesForBGContext(bgContext);
        for (final Iterator iter = resources.iterator(); iter.hasNext();) {
            final OLATResource resource = (OLATResource) iter.next();
            if (resource.getResourceableTypeName().equals(CourseModule.getCourseTypeName())) {
                final ICourse course = CourseFactory.loadCourse(resource);
            } else if (resource.getResourceableTypeName().equals("junitcourse")) {
                // do nothing when in junit test mode
            } else {
                throw new AssertException("Currently only course resources allowed in resource to context relations.");
            }
        }
    }

    @Override
    public void removeBGContextFromResource(final BGContext bgContext, final OLATResource resource) {
        // 1) delete references for this resource
        bgContextDao.removeBGContextFromResource(bgContext, resource);
        // 2) update course context list in this course resource
        if (resource.getResourceableTypeName().equals(CourseModule.getCourseTypeName())) {
            final ICourse course = CourseFactory.loadCourse(resource);
        } else if (resource.getResourceableTypeName().equals("junitcourse")) {
            // do nothing when in junit test mode
        } else {
            throw new AssertException("Currently only course resources allowed in resource to context relations.");
        }

        log.info("Audit:Removed Business Group Context from OLATResource " + resource.toString() + bgContext.toString());
    }

    /**
	 */
    @Override
    public void deleteCompleteBGContext(BGContext bgContext) {
        // 1) Delete all groups from group context
        final List groups = bgContextDao.getGroupsOfBGContext(bgContext);
        businessGroupService.deleteBusinessGroups(groups);
        // 2) Delete all group areas
        final List areas = bgAreaDao.findBGAreasOfBGContext(bgContext);
        for (final Iterator iter = areas.iterator(); iter.hasNext();) {
            final BGArea area = (BGArea) iter.next();
            bgAreaDao.deleteBGArea(area);
        }
        // 3) Delete group to resource relations
        final List referencingResources = bgContextDao.findOLATResourcesForBGContext(bgContext);
        for (final Iterator iter = referencingResources.iterator(); iter.hasNext();) {
            final OLATResource refRes = (OLATResource) iter.next();
            bgContextDao.removeBGContextFromResource(bgContext, refRes);
        }
        // 4) Delete group context
        bgContextDao.deleteBGContext(bgContext);
        // 5) Delete security group
        final SecurityGroup owners = bgContext.getOwnerGroup();
        if (owners != null) {
            baseSecurity.deleteSecurityGroup(owners);
        }
        log.info("Audit:Deleted Business Group Context" + bgContext.toString());
    }

    /**
	 */
    @Override
    public List findRepositoryEntriesForBGContext(final BGContext bgContext) {
        final List resources = bgContextDao.findOLATResourcesForBGContext(bgContext);
        final List entries = new ArrayList();
        for (final Iterator iter = resources.iterator(); iter.hasNext();) {
            final OLATResource resource = (OLATResource) iter.next();
            final RepositoryEntry entry = repositoryService.lookupRepositoryEntry(resource, false);
            if (entry == null) {
                throw new AssertException("No repository entry found for olat resource with TYPE::" + resource.getResourceableTypeName() + " ID::"
                        + resource.getResourceableId());
            } else {
                entries.add(entry);
            }
        }
        return entries;
    }

    /**
     * boolean)
     */
    @Override
    public BGContext createAndPersistBGContext(final String name, final String description, final String groupType, final Identity owner, final boolean defaultContext) {
        if (name == null) {
            throw new AssertException("Business group context name must not be null");
        }
        if (groupType == null) {
            throw new AssertException("Business group groupType name must not be null");
        }

        // 1) create administrative owner security group, add owner if available
        final SecurityGroup ownerGroup = baseSecurity.createAndPersistSecurityGroup();
        if (owner != null) {
            baseSecurity.addIdentityToSecurityGroup(owner, ownerGroup);
        }
        // 2) create new group context with this security group and save it
        final BGContext bgContext = bgContextDao.createAndPersistBGContext(name, description, ownerGroup, groupType, defaultContext);
        // 3) save context owner policy to this context and the owner group
        baseSecurity.createAndPersistPolicy(ownerGroup, Constants.PERMISSION_ACCESS, bgContext);
        // 4) save groupmanager policy on this group - all members are automatically
        // group managers
        baseSecurity.createAndPersistPolicy(ownerGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_GROUPMANAGER);
        log.info("Audit:Created Business Group Context" + bgContext.toString());
        return bgContext;
    }

}
