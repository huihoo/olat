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
package org.olat.connectors.rest.repository.course;

import static org.olat.connectors.rest.security.RestSecurityHelper.isAuthor;
import static org.olat.connectors.rest.security.RestSecurityHelper.isAuthorEditor;
import static org.olat.connectors.rest.support.ObjectFactory.get;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.connectors.rest.support.vo.GroupVO;
import org.olat.data.group.BusinessGroup;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.ENCourseNode;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.system.commons.StringHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * This handles the enrollment building block.
 * <P>
 * Initial Date: 10 mai 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
@Path("repo/courses/{courseId}/elements/enrollment")
public class ENWebService extends AbstractCourseNodeWebService {

    /**
     * This attaches an enrollment element onto a given course, the element will be inserted underneath the supplied parentNodeId
     * 
     * @response.representation.mediaType application/x-www-form-urlencoded
     * @response.representation.doc The course node metadatas
     * @response.representation.200.qname {http://www.example.com}courseNodeVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The course node metadatas
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or parentNode not found
     * @param courseId
     *            The course resourceable's id
     * @param parentNodeId
     *            The node's id which will be the parent of this structure
     * @param position
     *            The node's position relative to its sibling nodes (optional)
     * @param shortTitle
     *            The node short title
     * @param longTitle
     *            The node long title
     * @param objectives
     *            The node learning objectives
     * @param visibilityExpertRules
     *            The rules to view the node (optional)
     * @param accessExpertRules
     *            The rules to access the node (optional)
     * @param groups
     *            A list of learning groups (list of keys)
     * @param cancelEnabled
     *            cancel enrollment enabled or not
     * @param request
     *            The HTTP request
     * @return The persisted contact element (fully populated)
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response attachEnrolmment(@PathParam("courseId") final Long courseId, @QueryParam("parentNodeId") final String parentNodeId,
            @QueryParam("position") final Integer position, @QueryParam("shortTitle") @DefaultValue("undefined") final String shortTitle,
            @QueryParam("longTitle") @DefaultValue("undefined") final String longTitle, @QueryParam("objectives") @DefaultValue("undefined") final String objectives,
            @QueryParam("visibilityExpertRules") final String visibilityExpertRules, @QueryParam("accessExpertRules") final String accessExpertRules,
            @QueryParam("groups") final String groups, @QueryParam("cancelEnabled") @DefaultValue("false") final boolean cancelEnabled,
            @Context final HttpServletRequest request) {

        final EnrollmentConfigDelegate config = new EnrollmentConfigDelegate(groups, cancelEnabled);
        return attach(courseId, parentNodeId, "en", position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
    }

    /**
     * This attaches an enrollment element onto a given course, the element will be inserted underneath the supplied parentNodeId
     * 
     * @response.representation.mediaType application/x-www-form-urlencoded
     * @response.representation.doc The course node metadatas
     * @response.representation.200.qname {http://www.example.com}courseNodeVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The course node metadatas
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or parentNode not found
     * @param courseId
     *            The course resourceable's id
     * @param parentNodeId
     *            The node's id which will be the parent of this structure
     * @param position
     *            The node's position relative to its sibling nodes (optional)
     * @param shortTitle
     *            The node short title
     * @param longTitle
     *            The node long title
     * @param objectives
     *            The node learning objectives
     * @param visibilityExpertRules
     *            The rules to view the node (optional)
     * @param accessExpertRules
     *            The rules to access the node (optional)
     * @param groups
     *            send the message to the specified groups
     * @param cancelEnabled
     *            cancel enrollment enabled or not
     * @param request
     *            The HTTP request
     * @return The persisted contact element (fully populated)
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response attachEnrollmenetPost(@PathParam("courseId") final Long courseId, @FormParam("parentNodeId") final String parentNodeId,
            @FormParam("position") final Integer position, @FormParam("shortTitle") @DefaultValue("undefined") final String shortTitle,
            @FormParam("longTitle") @DefaultValue("undefined") final String longTitle, @FormParam("objectives") @DefaultValue("undefined") final String objectives,
            @FormParam("visibilityExpertRules") final String visibilityExpertRules, @FormParam("accessExpertRules") final String accessExpertRules,
            @FormParam("groups") final String groups, @FormParam("cancelEnabled") @DefaultValue("false") final boolean cancelEnabled,
            @Context final HttpServletRequest request) {
        final EnrollmentConfigDelegate config = new EnrollmentConfigDelegate(groups, cancelEnabled);
        return attach(courseId, parentNodeId, "en", position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
    }

    /**
     * Retrieves the groups where the enrollment happens
     * 
     * @response.representation.200.qname {http://www.example.com}groupVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The groups
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_GROUPVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or course node not found
     * @param nodeId
     *            The node's id
     * @param httpRequest
     *            The HTTP request
     * @return An array of groups
     */
    @GET
    @Path("{nodeId}/groups")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getGroups(@PathParam("courseId") final Long courseId, @PathParam("nodeId") final String nodeId, @Context final HttpServletRequest httpRequest) {

        if (!isAuthor(httpRequest)) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }
        final ICourse course = loadCourse(courseId);
        if (course == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        } else if (!isAuthorEditor(course, httpRequest)) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }

        final CourseNode node = getParentNode(course, nodeId);
        final ModuleConfiguration config = node.getModuleConfiguration();
        final String groupeNames = (String) config.get(ENCourseNode.CONFIG_GROUPNAME);
        if (!StringHelper.containsNonWhitespace(groupeNames)) {
            return Response.ok(new GroupVO[0]).build();
        }

        final List<GroupVO> voes = new ArrayList<GroupVO>();
        final String[] groupeNameArr = groupeNames.split(",");
        final CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
        for (final String groupeName : groupeNameArr) {
            final List<BusinessGroup> groups = cgm.getLearningGroupsFromAllContexts(groupeName, course);
            for (final BusinessGroup group : groups) {
                voes.add(get(group));
            }
        }

        final GroupVO[] voArr = new GroupVO[voes.size()];
        voes.toArray(voArr);
        return Response.ok(voArr).build();
    }

    private class EnrollmentConfigDelegate implements CustomConfigDelegate {
        private final boolean cancelEnabled;
        private final List<String> groups;

        public EnrollmentConfigDelegate(final String groups, final boolean cancelEnabled) {
            this.groups = getGroupNames(groups);
            this.cancelEnabled = cancelEnabled;
        }

        @Override
        public boolean isValid() {
            return groups != null && !groups.isEmpty();
        }

        @Override
        public void configure(final ICourse course, final CourseNode newNode, final ModuleConfiguration moduleConfig) {
            moduleConfig.set(ENCourseNode.CONFIG_GROUPNAME, groups);
            moduleConfig.set(ENCourseNode.CONF_CANCEL_ENROLL_ENABLED, cancelEnabled);
        }

        private List<String> getGroupNames(final String groupIds) {
            final List<String> groupNames = new ArrayList<String>();

            if (StringHelper.containsNonWhitespace(groupIds)) {
                final String[] groupIdArr = groupIds.split(";");
                BusinessGroupService businessGroupService = (BusinessGroupService) CoreSpringFactory.getBean(BusinessGroupService.class);
                for (final String groupId : groupIdArr) {
                    final Long groupKey = new Long(groupId);
                    final BusinessGroup bg = businessGroupService.loadBusinessGroup(groupKey, false);
                    groupNames.add(bg.getName());
                }
            }

            return groupNames;
        }
    }
}
