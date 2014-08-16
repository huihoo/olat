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

import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.group.context.BGContext;
import org.olat.data.resource.OLATResource;

/**
 * TODO: Class Description for BusinessGroupAreaService
 * 
 * <P>
 * Initial Date: 27.06.2011 <br>
 * 
 * @author guido
 */
public interface BusinessGroupContextService {

    /**
     * Creates and persists a business group context as a copy of an existing business group context. The new created context will then be associated to the given
     * OLATResource. The copy process will copy all business group areas and all business groups. The groups will be configured identically as the original groups but
     * will not contain any users.
     * 
     * @param contextName
     *            The new context name
     * @param resource
     *            The OALTResource that the new context should be associated with using the group context management tool
     * @param originalBgContext
     *            The original business group context that is uses for the copy process
     * @return The new copied business group context
     */
    public BGContext copyAndAddBGContextToResource(String contextName, OLATResource resource, BGContext originalBgContext);

    /**
     * Creates a relation from a business group context to an OLATResource (e.g. course)
     * 
     * @param contextName
     *            The new context name
     * @param resource
     *            The OALTResource that the new context should be associated with
     * @param initialOwner
     *            The initial owner. the users who can manage the business group context using the group context management tool
     * @param groupType
     *            The group type the context should be used for
     * @param defaultContext
     *            true: create as a default context, false: create as a regular context
     * @return The new created business group context
     */
    public BGContext createAndAddBGContextToResource(String contextName, OLATResource resource, String groupType, Identity initialOwner, boolean defaultContext);

    /**
     * Add a business group context to an OLATResource
     * 
     * @param bgContext
     * @param resource
     */
    void addBGContextToResource(BGContext bgContext, OLATResource resource);

    /**
     * 
     * @param bgContext
     */
    public void updateBGContext(final BGContext bgContext);

    /**
     * @param bgContext
     * @param resource
     */
    public void removeBGContextFromResource(BGContext bgContext, OLATResource resource);

    /**
     * Delete complete Business-group context with
     * 
     * @param bgContext
     */
    public void deleteCompleteBGContext(BGContext bgContext);

    /**
     * Find all repository entries of the OLAT resources that have a relation to this group context. (see findOlatResourcesForBGContext)
     * 
     * @param bgContext
     * @return List of repository entries
     */
    public List findRepositoryEntriesForBGContext(BGContext bgContext);

    /**
     * Creates a busines group context object and persists the object in the database
     * 
     * @param name
     *            Display name of the group context
     * @param description
     * @param groupType
     *            Business group type that this business group context can contain
     * @param owner
     *            The initial owner, the users who can manage the business group context using the group context management tool
     * @param defaultContext
     *            true: create as a default context, false: create as a regular context
     * @return The persisted business group context
     */
    public BGContext createAndPersistBGContext(String name, String description, String groupType, Identity owner, boolean defaultContext);

}
