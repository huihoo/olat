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
package org.olat.connectors.rest.group;

import static org.olat.connectors.rest.security.RestSecurityHelper.isGroupManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.olat.connectors.rest.forum.ForumWebService;
import org.olat.connectors.rest.security.RestSecurityHelper;
import org.olat.connectors.rest.support.ObjectFactory;
import org.olat.connectors.rest.support.vo.GroupInfoVO;
import org.olat.connectors.rest.support.vo.GroupVO;
import org.olat.connectors.rest.user.UserVO;
import org.olat.connectors.rest.user.UserVOFactory;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.forum.Forum;
import org.olat.data.group.BusinessGroup;
import org.olat.data.properties.PropertyImpl;
import org.olat.lms.forum.ForumService;
import org.olat.lms.group.BGConfigFlags;
import org.olat.lms.group.BusinessGroupPropertyManager;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.properties.NarrowedPropertyManager;
import org.olat.lms.properties.PropertyManagerEBL;
import org.olat.presentation.collaboration.CollaborationTools;
import org.olat.presentation.collaboration.CollaborationToolsFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.system.commons.StringHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.SyncerCallback;
import org.olat.system.coordinate.SyncerExecutor;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * This handles the learning groups.
 * <P>
 * Initial Date: 23 mar. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
@Path("groups")
public class LearningGroupWebService {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String VERSION = "1.0";
    private BusinessGroupService businessGroupService;
    private CacheControl cc = new CacheControl();

    /**
	 * 
	 */
    public LearningGroupWebService() {
        businessGroupService = (BusinessGroupService) CoreSpringFactory.getBean(BusinessGroupService.class);
        cc.setMaxAge(-1);
    }

    /**
     * Retrieves the version of the Group Web Service.
     * 
     * @response.representation.200.mediaType text/plain
     * @response.representation.200.doc The version of this specific Web Service
     * @response.representation.200.example 1.0
     * @return
     */
    @GET
    @Path("version")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getVersion() {
        return Response.ok(VERSION).build();
    }

    /**
     * Return the list of all groups if you have group manager permission, or all learning group that you particip with or owne.
     * 
     * @response.representation.200.qname {http://www.example.com}groupVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc This is the list of all groups in OLAT system
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_GROUPVOes}
     * @param request
     *            The HTTP Request
     * @return
     */
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getGroupList(@Context final HttpServletRequest request) {
        List<BusinessGroup> bgs;
        if (isGroupManager(request)) {
            bgs = businessGroupService.getAllBusinessGroups();
        } else {
            bgs = new ArrayList<BusinessGroup>();
            final Identity identity = RestSecurityHelper.getIdentity(request);
            final List<BusinessGroup> ownedGroups = businessGroupService.findBusinessGroupsOwnedBy(null, identity, null);
            for (final BusinessGroup group : ownedGroups) {
                bgs.add(group);
            }
            final List<BusinessGroup> partGroups = businessGroupService.findBusinessGroupsAttendedBy(null, identity, null);
            for (final BusinessGroup group : partGroups) {
                bgs.add(group);
            }
        }

        int count = 0;
        final GroupVO[] groupVOs = new GroupVO[bgs.size()];
        for (final BusinessGroup bg : bgs) {
            groupVOs[count++] = ObjectFactory.get(bg);
        }
        return Response.ok(groupVOs).build();
    }

    /**
     * Return the group specified by the key of the group.
     * 
     * @response.representation.200.qname {http://www.example.com}groupVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc A business group in the OLAT system
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_GROUPVO}
     * @param groupKey
     *            The key of the group
     * @param request
     *            The REST request
     * @param httpRequest
     *            The HTTP request
     * @return
     */
    @GET
    @Path("{groupKey}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response findById(@PathParam("groupKey") final Long groupKey, @Context final Request request, @Context final HttpServletRequest httpRequest) {
        final BusinessGroup bg = businessGroupService.loadBusinessGroup(groupKey, false);
        if (bg == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }
        final Identity identity = RestSecurityHelper.getIdentity(httpRequest);
        if (!isGroupManager(httpRequest) && !businessGroupService.isIdentityInBusinessGroup(identity, bg)) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }

        final Date lastModified = bg.getLastModified();
        Response.ResponseBuilder response = request.evaluatePreconditions(lastModified);
        if (response == null) {
            final GroupVO vo = ObjectFactory.get(bg);
            response = Response.ok(vo);
        }
        return response.build();
    }

    /**
     * Updates a group.
     * 
     * @response.representation.qname {http://www.example.com}groupVO
     * @response.representation.mediaType application/xml, application/json
     * @response.representation.doc A business group in the OLAT system
     * @response.representation.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_GROUPVO}
     * @response.representation.200.qname {http://www.example.com}groupVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The saved business group
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_GROUPVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The business group cannot be found
     * @param groupKey
     *            The key of the group
     * @param group
     *            The group
     * @param request
     *            The HTTP request
     * @return
     */
    @POST
    @Path("{groupKey}")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response postGroup(@PathParam("groupKey") final Long groupKey, final GroupVO group, @Context final HttpServletRequest request) {
        if (!isGroupManager(request)) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }

        final BusinessGroup bg = businessGroupService.loadBusinessGroup(groupKey, false);
        if (bg == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }

        if (!StringHelper.containsNonWhitespace(group.getName())) {
            return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
        }

        final BusinessGroup savedBg = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(bg, new SyncerCallback<BusinessGroup>() {
            @Override
            public BusinessGroup execute() {
                final BusinessGroup reloadedBG = businessGroupService.loadBusinessGroup(bg);
                reloadedBG.setName(group.getName());
                reloadedBG.setDescription(group.getDescription());
                reloadedBG.setMinParticipants(normalize(group.getMinParticipants()));
                reloadedBG.setMaxParticipants(normalize(group.getMaxParticipants()));
                businessGroupService.updateBusinessGroup(reloadedBG);
                return reloadedBG;
            }
        });

        // save the updated group
        final GroupVO savedVO = ObjectFactory.get(savedBg);
        return Response.ok(savedVO).build();
    }

    /**
     * Deletes the business group specified by the groupKey.
     * 
     * @response.representation.200.doc The business group is deleted
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The business group cannot be found
     * @param groupKey
     *            The key of the group
     * @param request
     *            The HTTP request
     * @return
     */
    @DELETE
    @Path("{groupKey}")
    public Response deleteGroup(@PathParam("groupKey") final Long groupKey, @Context final HttpServletRequest request) {
        if (!isGroupManager(request)) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }

        final BusinessGroup bg = businessGroupService.loadBusinessGroup(groupKey, false);
        if (bg == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }
        businessGroupService.deleteBusinessGroup(bg);
        return Response.ok().build();
    }

    /**
     * Returns the informations of the group specified by the groupKey.
     * 
     * @response.representation.200.qname {http://www.example.com}groupInfoVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc Participants of the business group
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_GROUPINFOVO}
     * @response.representation.404.doc The business group cannot be found
     * @param groupKey
     *            The key of the group
     * @param request
     *            The HTTP Request
     * @return
     */
    @GET
    @Path("{groupKey}/infos")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getInformations(@PathParam("groupKey") final Long groupKey, @Context final HttpServletRequest request) {
        final BusinessGroup bg = businessGroupService.loadBusinessGroup(groupKey, false);
        if (bg == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }

        if (!isGroupManager(request)) {
            final Identity identity = RestSecurityHelper.getIdentity(request);
            if (!businessGroupService.isIdentityInBusinessGroup(identity, bg)) {
                return Response.serverError().status(Status.UNAUTHORIZED).build();
            }
        }

        final GroupInfoVO info = new GroupInfoVO();

        // forum
        final CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(bg);
        if (collabTools.isToolEnabled(CollaborationTools.TOOL_FORUM)) {
            info.setForumKey(getForumKey(bg));
        }

        final String news = collabTools.lookupNews();
        info.setNews(news);

        if (collabTools.isToolEnabled(CollaborationTools.TOOL_WIKI)) {
            info.setHasWiki(Boolean.TRUE);
        } else {
            info.setHasWiki(Boolean.FALSE);
        }

        if (collabTools.isToolEnabled(CollaborationTools.TOOL_FOLDER)) {
            info.setHasFolder(Boolean.TRUE);
        } else {
            info.setHasFolder(Boolean.FALSE);
        }

        return Response.ok(info).build();
    }

    private Long getForumKey(final BusinessGroup ores) {
        final ForumService fom = getForumService();
        final NarrowedPropertyManager npm = NarrowedPropertyManager.getInstance(ores);

        final Long forumKey = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ores, new SyncerCallback<Long>() {
            @Override
            public Long execute() {

                Long key;
                PropertyImpl forumKeyProperty = npm.findProperty(null, null, PropertyManagerEBL.PROP_CAT_BG_COLLABTOOLS, PropertyManagerEBL.KEY_FORUM);
                if (forumKeyProperty == null) {
                    // First call of forum, create new forum and save
                    final Forum forum = fom.addAForum();
                    key = forum.getKey();
                    if (log.isDebugEnabled()) {
                        log.debug("created new forum in collab tools: foid::" + key.longValue() + " for ores::" + ores.getResourceableTypeName() + "/"
                                + ores.getResourceableId());
                    }
                    forumKeyProperty = npm.createPropertyInstance(null, null, PropertyManagerEBL.PROP_CAT_BG_COLLABTOOLS, PropertyManagerEBL.KEY_FORUM, null, key, null,
                            null);
                    npm.saveProperty(forumKeyProperty);
                } else {
                    // Forum does already exist, load forum with key from properties
                    key = forumKeyProperty.getLongValue();
                    if (log.isDebugEnabled()) {
                        log.debug("loading forum in collab tools from properties: foid::" + key.longValue() + " for ores::" + ores.getResourceableTypeName() + "/"
                                + ores.getResourceableId());
                    }
                }

                return key;
            }
        });
        return forumKey;
    }

    private ForumService getForumService() {
        return (ForumService) CoreSpringFactory.getBean("forumService");

    }

    /**
     * Return the Forum web service
     * 
     * @param groupKey
     *            The key of the group
     * @param request
     *            The HTTP Request
     * @return
     */
    @Path("{groupKey}/forum")
    public ForumWebService getForum(@PathParam("groupKey") final Long groupKey, @Context final HttpServletRequest request) {
        final BusinessGroup bg = businessGroupService.loadBusinessGroup(groupKey, false);
        if (bg == null) {
            return null;
        }

        if (!isGroupManager(request)) {
            final Identity identity = RestSecurityHelper.getIdentity(request);
            if (!businessGroupService.isIdentityInBusinessGroup(identity, bg)) {
                return null;
            }
        }

        final Long forumKey = getForumKey(bg);
        final ForumService fom = getForumService();
        final Forum forum = fom.loadForum(forumKey);
        return new ForumWebService(forum);
    }

    /**
     * Returns the list of owners of the group specified by the groupKey.
     * 
     * @response.representation.200.qname {http://www.example.com}userVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc Owners of the business group
     * @response.representation.200.example {@link org.olat.connectors.rest.user.Examples#SAMPLE_USERVOes}
     * @response.representation.404.doc The business group cannot be found
     * @param groupKey
     *            The key of the group
     * @param request
     *            The HTTP Request
     * @return
     */
    @GET
    @Path("{groupKey}/owners")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getTutors(@PathParam("groupKey") final Long groupKey, @Context final HttpServletRequest request) {
        final BusinessGroup bg = businessGroupService.loadBusinessGroup(groupKey, false);
        if (bg == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }

        if (!isGroupManager(request)) {
            final Identity identity = RestSecurityHelper.getIdentity(request);
            if (!businessGroupService.isIdentityInBusinessGroup(identity, bg)) {
                return Response.serverError().status(Status.UNAUTHORIZED).build();
            }
            final BusinessGroupPropertyManager bgpm = new BusinessGroupPropertyManager(bg);
            if (!bgpm.showOwners()) {
                return Response.serverError().status(Status.UNAUTHORIZED).build();
            }
        }

        return getIdentityInGroup(bg.getOwnerGroup());
    }

    /**
     * Returns the list of participants of the group specified by the groupKey.
     * 
     * @response.representation.200.qname {http://www.example.com}userVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc Participants of the business group
     * @response.representation.200.example {@link org.olat.connectors.rest.user.Examples#SAMPLE_USERVOes}
     * @response.representation.404.doc The business group cannot be found
     * @param groupKey
     *            The key of the group
     * @param request
     *            The HTTP Request
     * @return
     */
    @GET
    @Path("{groupKey}/participants")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getParticipants(@PathParam("groupKey") final Long groupKey, @Context final HttpServletRequest request) {
        final BusinessGroup bg = businessGroupService.loadBusinessGroup(groupKey, false);
        if (bg == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }

        if (!isGroupManager(request)) {
            final Identity identity = RestSecurityHelper.getIdentity(request);
            if (!businessGroupService.isIdentityInBusinessGroup(identity, bg)) {
                return Response.serverError().status(Status.UNAUTHORIZED).build();
            }
            final BusinessGroupPropertyManager bgpm = new BusinessGroupPropertyManager(bg);
            if (!bgpm.showPartips()) {
                return Response.serverError().status(Status.UNAUTHORIZED).build();
            }
        }

        return getIdentityInGroup(bg.getPartipiciantGroup());
    }

    private Response getIdentityInGroup(final SecurityGroup sg) {
        final BaseSecurity securityManager = getBaseSecurity();
        final List<Object[]> owners = securityManager.getIdentitiesAndDateOfSecurityGroup(sg);

        int count = 0;
        final UserVO[] ownerVOs = new UserVO[owners.size()];
        for (int i = 0; i < owners.size(); i++) {
            final Identity identity = (Identity) (owners.get(i))[0];
            ownerVOs[count++] = UserVOFactory.get(identity);
        }
        return Response.ok(ownerVOs).build();
    }

    private BaseSecurity getBaseSecurity() {
        return (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
    }

    /**
     * Adds an owner to the group.
     * 
     * @response.representation.200.doc The user is added as owner of the group
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The business group or the user cannot be found
     * @param groupKey
     *            The key of the group
     * @param identityKey
     *            The user's id
     * @param request
     *            The HTTP request
     * @return
     */
    @PUT
    @Path("{groupKey}/owners/{identityKey}")
    public Response addTutor(@PathParam("groupKey") final Long groupKey, @PathParam("identityKey") final Long identityKey, @Context final HttpServletRequest request) {
        try {
            if (!isGroupManager(request)) {
                return Response.serverError().status(Status.UNAUTHORIZED).build();
            }

            final UserRequest ureq = RestSecurityHelper.getUserRequest(request);

            final BusinessGroup group = businessGroupService.loadBusinessGroup(groupKey, false);
            final Identity identity = getBaseSecurity().loadIdentityByKey(identityKey, false);
            if (identity == null || group == null) {
                return Response.serverError().status(Status.NOT_FOUND).build();
            }

            CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(group, new SyncerCallback<Boolean>() {
                @Override
                public Boolean execute() {
                    final BGConfigFlags flags = BGConfigFlags.createLearningGroupDefaultFlags();
                    businessGroupService.addOwnerAndFireEvent(ureq.getIdentity(), identity, group, flags, false);
                    return Boolean.TRUE;
                }
            });// end of doInSync

            return Response.ok().build();
        } catch (final Exception e) {
            log.error("Trying to add an owner to a group", e);
            return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Fallback method for browser.
     * 
     * @response.representation.200.doc The user is added as owner of the group
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The business group or the user cannot be found
     * @param groupKey
     *            The key of the group
     * @param identityKey
     *            The user's id
     * @param request
     *            The HTTP request
     * @return
     */
    @POST
    @Path("{groupKey}/owners/{identityKey}/new")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response addTutorPost(@PathParam("groupKey") final Long groupKey, @PathParam("identityKey") final Long identityKey, @Context final HttpServletRequest request) {
        return addTutor(groupKey, identityKey, request);
    }

    /**
     * Removes the owner from the group.
     * 
     * @response.representation.200.doc The user is removed as owner from the group
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The business group or the user cannot be found
     * @param groupKey
     *            The key of the group
     * @param identityKey
     *            The user's id
     * @param request
     *            The HTTP request
     * @return
     */
    @DELETE
    @Path("{groupKey}/owners/{identityKey}")
    public Response removeTutor(@PathParam("groupKey") final Long groupKey, @PathParam("identityKey") final Long identityKey, @Context final HttpServletRequest request) {
        try {
            if (!isGroupManager(request)) {
                return Response.serverError().status(Status.UNAUTHORIZED).build();
            }

            final UserRequest ureq = RestSecurityHelper.getUserRequest(request);

            final BusinessGroup group = businessGroupService.loadBusinessGroup(groupKey, false);
            final Identity identity = getBaseSecurity().loadIdentityByKey(identityKey, false);
            if (identity == null || group == null) {
                return Response.serverError().status(Status.NOT_FOUND).build();
            }

            CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(group, new SyncerCallback<Boolean>() {
                @Override
                public Boolean execute() {
                    final BGConfigFlags flags = BGConfigFlags.createLearningGroupDefaultFlags();
                    businessGroupService.removeOwnerAndFireEvent(ureq.getIdentity(), identity, group, flags, false);
                    return Boolean.TRUE;
                }
            });// end of doInSync

            return Response.ok().build();
        } catch (final Exception e) {
            log.error("Trying to remove an owner to a group", e);
            return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Fallback method for browser.
     * 
     * @response.representation.200.doc The user is removed as owner from the group
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The business group or the user cannot be found
     * @param groupKey
     *            The key of the group
     * @param identityKey
     *            The user's id
     * @param request
     *            The HTTP request
     * @return
     */
    @POST
    @Path("{groupKey}/owners/{identityKey}/delete")
    public Response removeTutorPost(@PathParam("groupKey") final Long groupKey, @PathParam("identityKey") final Long identityKey,
            @Context final HttpServletRequest request) {
        return removeTutor(groupKey, identityKey, request);
    }

    /**
     * Adds a participant to the group.
     * 
     * @response.representation.200.doc The user is added as participant of the group
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The business group or the user cannot be found
     * @param groupKey
     *            The key of the group
     * @param identityKey
     *            The user's id
     * @param request
     *            The HTTP request
     * @return
     */
    @PUT
    @Path("{groupKey}/participants/{identityKey}")
    public Response addParticipant(@PathParam("groupKey") final Long groupKey, @PathParam("identityKey") final Long identityKey, @Context final HttpServletRequest request) {
        try {
            if (!isGroupManager(request)) {
                return Response.serverError().status(Status.UNAUTHORIZED).build();
            }

            final UserRequest ureq = RestSecurityHelper.getUserRequest(request);

            final BusinessGroup group = businessGroupService.loadBusinessGroup(groupKey, false);
            final Identity identity = getBaseSecurity().loadIdentityByKey(identityKey, false);
            if (identity == null || group == null) {
                return Response.serverError().status(Status.NOT_FOUND).build();
            }

            CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(group, new SyncerCallback<Boolean>() {
                @Override
                public Boolean execute() {
                    final BGConfigFlags flags = BGConfigFlags.createLearningGroupDefaultFlags();
                    businessGroupService.addParticipantAndFireEvent(ureq.getIdentity(), identity, group, flags, false);
                    return Boolean.TRUE;
                }
            });// end of doInSync

            return Response.ok().build();
        } catch (final Exception e) {
            log.error("Trying to add a participant to a group", e);
            return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Fallback method for browser.
     * 
     * @response.representation.200.doc The user is added as participant of the group
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The business group or the user cannot be found
     * @param groupKey
     *            The id of the group
     * @param identityKey
     *            The user's id
     * @param request
     *            The HTTP request
     * @return
     */
    @POST
    @Path("{groupKey}/participants/{identityKey}/new")
    public Response addParticipantPost(@PathParam("groupKey") final Long groupKey, @PathParam("identityKey") final Long identityKey,
            @Context final HttpServletRequest request) {
        return addParticipant(groupKey, identityKey, request);
    }

    /**
     * Removes a participant from the group.
     * 
     * @response.representation.200.doc The user is remove from the group as participant
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The business group or the user cannot be found
     * @param groupKey
     *            The key of the group
     * @param identityKey
     *            The id of the user
     * @param request
     *            The HTTP request
     * @return
     */
    @DELETE
    @Path("{groupKey}/participants/{identityKey}")
    public Response removeParticipant(@PathParam("groupKey") final Long groupKey, @PathParam("identityKey") final Long identityKey,
            @Context final HttpServletRequest request) {
        try {
            if (!isGroupManager(request)) {
                return Response.serverError().status(Status.UNAUTHORIZED).build();
            }

            final UserRequest ureq = RestSecurityHelper.getUserRequest(request);

            final BusinessGroup group = businessGroupService.loadBusinessGroup(groupKey, false);
            final Identity identity = getBaseSecurity().loadIdentityByKey(identityKey, false);
            if (identity == null || group == null) {
                return Response.serverError().status(Status.NOT_FOUND).build();
            }

            CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(group, new SyncerExecutor() {
                @Override
                public void execute() {
                    final BGConfigFlags flags = BGConfigFlags.createLearningGroupDefaultFlags();
                    businessGroupService.removeParticipantAndFireEvent(ureq.getIdentity(), identity, group, flags, false);
                }
            });

            return Response.ok().build();
        } catch (final Exception e) {
            log.error("Trying to remove a participant to a group", e);
            return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Fallback method for browser.
     * 
     * @response.representation.200.doc The user is remove from the group as participant
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The business group or the user cannot be found
     * @param groupKey
     *            The key of the group
     * @param identityKey
     *            The id of the user
     * @param request
     *            The HTTP request
     * @return
     */
    @POST
    @Path("{groupKey}/participants/{identityKey}/delete")
    public Response removeParticipantPost(@PathParam("groupKey") final Long groupKey, @PathParam("identityKey") final Long identityKey,
            @Context final HttpServletRequest request) {
        return removeParticipant(groupKey, identityKey, request);
    }

    /**
     * @param integer
     * @return value bigger or equal than 0
     */
    private static final Integer normalize(final Integer integer) {
        if (integer == null) {
            return new Integer(0);
        }
        if (integer.intValue() < 0) {
            return new Integer(0);
        }
        return integer;
    }
}
