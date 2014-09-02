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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
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

import org.olat.connectors.rest.repository.course.config.CustomConfigFactory;
import org.olat.connectors.rest.support.ObjectFactory;
import org.olat.connectors.rest.support.vo.CourseNodeVO;
import org.olat.connectors.rest.support.vo.elements.SurveyConfigVO;
import org.olat.connectors.rest.support.vo.elements.TaskConfigVO;
import org.olat.connectors.rest.support.vo.elements.TestConfigVO;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.condition.interpreter.ConditionExpression;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.IQSURVCourseNode;
import org.olat.lms.course.nodes.IQTESTCourseNode;
import org.olat.lms.course.nodes.MSCourseNode;
import org.olat.lms.course.nodes.TACourseNode;
import org.olat.lms.ims.qti.process.AssessmentInstance;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.course.editor.NodeEditController;
import org.olat.presentation.course.nodes.iq.IQEditController;
import org.olat.presentation.course.nodes.sp.SPEditController;
import org.olat.presentation.course.nodes.ta.TaskController;
import org.olat.presentation.course.nodes.tu.TUConfigForm;
import org.olat.system.commons.StringHelper;

/**
 * This interface provides course building capabilities from our REST API.
 * <p>
 * Initial Date: Feb 8, 2010 Time: 3:45:50 PM<br>
 * 
 * @author cbuckley, srosse, stephane.rosse@frentix.com
 */
@Path("repo/courses/{courseId}/elements")
public class CourseElementWebService extends AbstractCourseNodeWebService {

    private static final String VERSION = "0.1";

    /**
     * The version of the Course Elements Web Service
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
     * Retrieves metadata of the course node
     * 
     * @response.representation.200.qname {http://www.example.com}courseNodeVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The course node metadatas
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or parentNode not found
     * @param courseId
     *            The course resourceable's id
     * @param nodeId
     *            The node's id
     * @param request
     *            The HTTP request
     * @return The persisted structure element (fully populated)
     */
    @GET
    @Path("{nodeId}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getCourseNode(@PathParam("courseId") final Long courseId, @PathParam("nodeId") final String nodeId, @Context final HttpServletRequest request) {
        if (!isAuthor(request)) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }

        final ICourse course = loadCourse(courseId);
        if (course == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }

        final CourseNode courseNode = getParentNode(course, nodeId);
        if (courseNode == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }

        final CourseNodeVO vo = ObjectFactory.get(courseNode);
        return Response.ok(vo).build();
    }

    /**
     * This attaches a Structure Element onto a given course. The element will be inserted underneath the supplied parentNodeId.
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
     * @param request
     *            The HTTP request
     * @return The persisted structure element (fully populated)
     */
    @POST
    @Path("structure")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response attachStructurePost(@PathParam("courseId") final Long courseId, @FormParam("parentNodeId") final String parentNodeId,
            @FormParam("position") final Integer position, @FormParam("shortTitle") @DefaultValue("undefined") final String shortTitle,
            @FormParam("longTitle") @DefaultValue("undefined") final String longTitle, @FormParam("objectives") @DefaultValue("undefined") final String objectives,
            @FormParam("visibilityExpertRules") final String visibilityExpertRules, @FormParam("accessExpertRules") final String accessExpertRules,
            @Context final HttpServletRequest request) {
        return attachStructure(courseId, parentNodeId, position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, request);
    }

    /**
     * This attaches a Structure Element onto a given course. The element will be inserted underneath the supplied parentNodeId.
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
     * @param request
     *            The HTTP request
     * @return The persisted structure element (fully populated)
     */
    @PUT
    @Path("structure")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response attachStructure(@PathParam("courseId") final Long courseId, @QueryParam("parentNodeId") final String parentNodeId,
            @QueryParam("position") final Integer position, @QueryParam("shortTitle") @DefaultValue("undefined") final String shortTitle,
            @QueryParam("longTitle") @DefaultValue("undefined") final String longTitle, @QueryParam("objectives") @DefaultValue("undefined") final String objectives,
            @QueryParam("visibilityExpertRules") final String visibilityExpertRules, @QueryParam("accessExpertRules") final String accessExpertRules,
            @Context final HttpServletRequest request) {
        return attach(courseId, parentNodeId, "st", position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, null, request);
    }

    /**
     * This attaches a Single Page Element onto a given course. The element will be inserted underneath the supplied parentNodeId.
     * 
     * @response.representation.mediaType multipart/form-data
     * @response.representation.doc The content of the single page
     * @response.representation.200.qname {http://www.example.com}courseNodeVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The course node metadatas
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or parentNode not found
     * @param courseId
     *            The course resourceable id
     * @param parentNodeId
     *            The node's id which will be the parent of this single page
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
     * @param filename
     *            The single page file name
     * @param file
     *            The file input stream
     * @param request
     *            The HTTP request
     * @return The persisted Single Page Element(fully populated)
     */
    @POST
    @Path("singlepage")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response attachSinglePagePost(@PathParam("courseId") final Long courseId, @FormParam("parentNodeId") final String parentNodeId,
            @FormParam("position") final Integer position, @FormParam("shortTitle") @DefaultValue("undefined") final String shortTitle,
            @FormParam("longTitle") @DefaultValue("undefined") final String longTitle, @FormParam("objectives") @DefaultValue("undefined") final String objectives,
            @FormParam("visibilityExpertRules") final String visibilityExpertRules, @FormParam("accessExpertRules") final String accessExpertRules,
            @FormParam("filename") @DefaultValue("attachment") final String filename, @FormParam("file") final InputStream file, @Context final HttpServletRequest request) {
        return attachSinglePage(courseId, parentNodeId, position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, filename, file, request);
    }

    /**
     * This attaches a Single Page Element onto a given course. The element will be inserted underneath the supplied parentNodeId.
     * 
     * @response.representation.mediaType multipart/form-data
     * @response.representation.doc The content of the single page
     * @response.representation.200.qname {http://www.example.com}courseNodeVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc the course node metadatas
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or parentNode not found
     * @param courseId
     *            The course resourceable's id
     * @param parentNodeId
     *            The node's id which will be the parent of this single page
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
     * @param filename
     *            The single page file name
     * @param file
     *            The file input stream
     * @param request
     *            The HTTP request
     * @return The persisted Single Page Element(fully populated)
     */
    @PUT
    @Path("singlepage")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response attachSinglePage(@PathParam("courseId") final Long courseId, @QueryParam("parentNodeId") final String parentNodeId,
            @QueryParam("position") final Integer position, @QueryParam("shortTitle") @DefaultValue("undefined") final String shortTitle,
            @QueryParam("longTitle") @DefaultValue("undefined") final String longTitle, @QueryParam("objectives") @DefaultValue("undefined") final String objectives,
            @QueryParam("visibilityExpertRules") final String visibilityExpertRules, @QueryParam("accessExpertRules") final String accessExpertRules,
            @FormParam("filename") @DefaultValue("attachment") final String filename, @FormParam("file") final InputStream file, @Context final HttpServletRequest request) {
        final SinglePageCustomConfig config = new SinglePageCustomConfig(file, filename);
        return attach(courseId, parentNodeId, "sp", position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
    }

    /**
     * This attaches a Single Page Element onto a given course. The element will be inserted underneath the supplied parentNodeId. The page is found in the resource
     * folder of the course.
     * 
     * @response.representation.mediaType application/x-www-form-urlencoded
     * @response.representation.doc The content of the single page
     * @response.representation.200.qname {http://www.example.com}courseNodeVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc the course node metadatas
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or parentNode not found
     * @param courseId
     *            The course resourceable's id
     * @param parentNodeId
     *            The node's id which will be the parent of this single page
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
     * @param filename
     *            The single page file name
     * @param path
     *            The path of the file
     * @param request
     *            The HTTP request
     * @return The persisted Single Page Element(fully populated)
     */
    @POST
    @Path("singlepage")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response attachSinglePagePost(@PathParam("courseId") final Long courseId, @FormParam("parentNodeId") final String parentNodeId,
            @FormParam("position") final Integer position, @FormParam("shortTitle") @DefaultValue("undefined") final String shortTitle,
            @FormParam("longTitle") @DefaultValue("undefined") final String longTitle, @FormParam("objectives") @DefaultValue("undefined") final String objectives,
            @FormParam("visibilityExpertRules") final String visibilityExpertRules, @FormParam("accessExpertRules") final String accessExpertRules,
            @FormParam("filename") final String filename, @FormParam("path") final String path, @Context final HttpServletRequest request) {
        return attachSinglePage(courseId, parentNodeId, position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, filename, path, request);
    }

    /**
     * This attaches a Single Page Element onto a given course. The element will be inserted underneath the supplied parentNodeId. The page is found in the resource
     * folder of the course.
     * 
     * @response.representation.mediaType application/x-www-form-urlencoded
     * @response.representation.doc The content of the single page
     * @response.representation.200.qname {http://www.example.com}courseNodeVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc the course node metadatas
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or parentNode not found
     * @param courseId
     *            The course resourceable's id
     * @param parentNodeId
     *            The node's id which will be the parent of this single page
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
     * @param filename
     *            The single page file name
     * @param path
     *            The path of the file
     * @param request
     *            The HTTP request
     * @return The persisted Single Page Element(fully populated)
     */
    @PUT
    @Path("singlepage")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response attachSinglePage(@PathParam("courseId") final Long courseId, @QueryParam("parentNodeId") final String parentNodeId,
            @QueryParam("position") final Integer position, @QueryParam("shortTitle") @DefaultValue("undefined") final String shortTitle,
            @QueryParam("longTitle") @DefaultValue("undefined") final String longTitle, @QueryParam("objectives") @DefaultValue("undefined") final String objectives,
            @QueryParam("visibilityExpertRules") final String visibilityExpertRules, @QueryParam("accessExpertRules") final String accessExpertRules,
            @QueryParam("filename") final String filename, @QueryParam("path") final String path, @Context final HttpServletRequest request) {
        final SinglePageCustomConfig config = new SinglePageCustomConfig(path, filename);
        return attach(courseId, parentNodeId, "sp", position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
    }

    /**
     * This attaches a Folder Element onto a given course. The element will be inserted underneath the supplied parentNodeId.
     * 
     * @response.representation.mediaType application/x-www-form-urlencoded
     * @response.representation.200.qname {http://www.example.com}courseNodeVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The folder node metadatas
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or parentNode not found
     * @param courseId
     *            The course resourceable's id
     * @param parentNodeId
     *            The node's id which will be the parent of this folder
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
     * @param request
     *            The HTTP request
     * @return The persisted folder element (fully populated)
     */
    @POST
    @Path("folder")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response attachFolderPost(@PathParam("courseId") final Long courseId, @FormParam("parentNodeId") final String parentNodeId,
            @FormParam("position") final Integer position, @FormParam("shortTitle") @DefaultValue("undefined") final String shortTitle,
            @FormParam("longTitle") @DefaultValue("undefined") final String longTitle, @FormParam("objectives") @DefaultValue("undefined") final String objectives,
            @FormParam("visibilityExpertRules") final String visibilityExpertRules, @FormParam("accessExpertRules") final String accessExpertRules,
            @Context final HttpServletRequest request) {
        return attachFolder(courseId, parentNodeId, position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, request);
    }

    /**
     * This attaches a Folder Element onto a given course. The element will be inserted underneath the supplied parentNodeId.
     * 
     * @response.representation.mediaType application/x-www-form-urlencoded
     * @response.representation.200.qname {http://www.example.com}courseNodeVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The folder node metadatas
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or parentNode not found
     * @param courseId
     *            The course resourceable id
     * @param parentNodeId
     *            The node's id which will be the parent of this folder
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
     * @param request
     *            The HTTP request
     * @return The persisted folder element (fully populated)
     */
    @PUT
    @Path("folder")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response attachFolder(@PathParam("courseId") final Long courseId, @QueryParam("parentNodeId") final String parentNodeId,
            @QueryParam("position") final Integer position, @QueryParam("shortTitle") @DefaultValue("undefined") final String shortTitle,
            @QueryParam("longTitle") @DefaultValue("undefined") final String longTitle, @QueryParam("objectives") @DefaultValue("undefined") final String objectives,
            @QueryParam("visibilityExpertRules") final String visibilityExpertRules, @QueryParam("accessExpertRules") final String accessExpertRules,
            @Context final HttpServletRequest request) {
        return attach(courseId, parentNodeId, "bc", position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, null, request);
    }

    /**
     * This attaches a Task Element onto a given course. The element will be inserted underneath the supplied parentNodeId.
     * 
     * @response.representation.mediaType application/x-www-form-urlencoded
     * @response.representation.doc The task node metadatas
     * @response.representation.200.qname {http://www.example.com}courseNodeVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The course node metadatas
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or parentNode not found
     * @param courseId
     *            The course resourceable id
     * @param parentNodeId
     *            The node's id which will be the parent of this task
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
     * @param text
     *            The task node text
     * @param points
     *            The task node's possible points
     * @param request
     *            The HTTP request
     * @return The persisted task element (fully populated)
     */
    @POST
    @Path("task")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response attachTaskPost(@PathParam("courseId") final Long courseId, @FormParam("parentNodeId") final String parentNodeId,
            @FormParam("position") final Integer position, @FormParam("shortTitle") @DefaultValue("undefined") final String shortTitle,
            @FormParam("longTitle") @DefaultValue("undefined") final String longTitle, @FormParam("objectives") @DefaultValue("undefined") final String objectives,
            @FormParam("visibilityExpertRules") final String visibilityExpertRules, @FormParam("accessExpertRules") final String accessExpertRules,
            @FormParam("text") final String text, @FormParam("points") final Float points, @Context final HttpServletRequest request) {
        return attachTask(courseId, parentNodeId, position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, text, points, request);
    }

    /**
     * This attaches a Task Element onto a given course. The element will be inserted underneath the supplied parentNodeId.
     * 
     * @response.representation.mediaType application/x-www-form-urlencoded
     * @response.representation.doc The task node metadatas
     * @response.representation.200.qname {http://www.example.com}courseNodeVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The course node metadatas
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or parentNode not found
     * @param courseId
     *            The course resourceable id
     * @param parentNodeId
     *            The node's id which will be the parent of this task
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
     * @param text
     *            The task node text
     * @param points
     *            The task node's possible points
     * @param request
     *            The HTTP request
     * @return The persisted task element (fully populated)
     */
    @PUT
    @Path("task")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response attachTask(@PathParam("courseId") final Long courseId, @QueryParam("parentNodeId") final String parentNodeId,
            @QueryParam("position") final Integer position, @QueryParam("shortTitle") @DefaultValue("undefined") final String shortTitle,
            @QueryParam("longTitle") @DefaultValue("undefined") final String longTitle, @QueryParam("objectives") @DefaultValue("undefined") final String objectives,
            @QueryParam("visibilityExpertRules") final String visibilityExpertRules, @QueryParam("accessExpertRules") final String accessExpertRules,
            @QueryParam("text") final String text, @QueryParam("points") final Float points, @Context final HttpServletRequest request) {
        final TaskCustomConfig config = new TaskCustomConfig(points, text);
        return attach(courseId, parentNodeId, "ta", position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
    }

    /**
     * This attaches a Test Element onto a given course. The element will be inserted underneath the supplied parentNodeId.
     * 
     * @response.representation.mediaType application/x-www-form-urlencoded
     * @response.representation.doc The course node metadatas
     * @response.representation.200.qname {http://www.example.com}courseNodeVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The test node metadatas
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course, parentNode or test not found
     * @param courseId
     *            The course resourceable id
     * @param parentNodeId
     *            The node's id which will be the parent of this test
     * @param testResourceableId
     *            The test node's id which is retorned in the response of the import test resource
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
     * @param request
     *            The HTTP request
     * @return The persisted test element (fully populated)
     */
    @POST
    @Path("test")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response attachTestPost(@PathParam("courseId") final Long courseId, @FormParam("parentNodeId") final String parentNodeId,
            @FormParam("shortTitle") @DefaultValue("undefined") final String shortTitle, @FormParam("position") final Integer position,
            @FormParam("longTitle") @DefaultValue("undefined") final String longTitle, @FormParam("objectives") @DefaultValue("undefined") final String objectives,
            @FormParam("visibilityExpertRules") final String visibilityExpertRules, @FormParam("accessExpertRules") final String accessExpertRules,
            @FormParam("testResourceableId") final Long testResourceableId, @Context final HttpServletRequest request) {
        return attachTest(courseId, parentNodeId, position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, testResourceableId, request);
    }

    /**
     * This attaches a Test Element onto a given course. The element will be inserted underneath the supplied parentNodeId.
     * 
     * @response.representation.mediaType application/x-www-form-urlencoded
     * @response.representation.doc the course node metadatas
     * @response.representation.200.qname {http://www.example.com}courseNodeVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc the test node metadatas
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc course, parentNode or test not found
     * @param courseId
     *            The course resourceable id
     * @param parentNodeId
     *            The node's id which will be the parent of this test
     * @param testResourceableId
     *            The test node's id which is retorned in the response of the import test resource
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
     * @param request
     *            The HTTP request
     * @return The persisted test element (fully populated)
     */
    @PUT
    @Path("test")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response attachTest(@PathParam("courseId") final Long courseId, @QueryParam("parentNodeId") final String parentNodeId,
            @QueryParam("position") final Integer position, @QueryParam("shortTitle") @DefaultValue("undefined") final String shortTitle,
            @QueryParam("longTitle") @DefaultValue("undefined") final String longTitle, @QueryParam("objectives") @DefaultValue("undefined") final String objectives,
            @QueryParam("visibilityExpertRules") final String visibilityExpertRules, @QueryParam("accessExpertRules") final String accessExpertRules,
            @QueryParam("testResourceableId") final Long testResourceableId, @Context final HttpServletRequest request) {
        final RepositoryService rm = RepositoryServiceImpl.getInstance();
        final RepositoryEntry testRepoEntry = rm.lookupRepositoryEntry(testResourceableId);
        if (testRepoEntry == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }

        final CustomConfigDelegate config = CustomConfigFactory.getTestCustomConfig(testRepoEntry);
        return attach(courseId, parentNodeId, "iqtest", position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
    }

    /**
     * Attaches an assessment building block.
     * 
     * @response.representation.mediaType application/x-www-form-urlencoded
     * @response.representation.doc The assessment node metadatas
     * @response.representation.200.qname {http://www.example.com}courseNodeVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The course node metadatas
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or parentNode not found
     * @param courseId
     *            The course resourceable's id
     * @param parentNodeId
     *            The node's id which will be the parent of this assessment
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
     * @param request
     *            The HTTP request
     * @return
     */
    @POST
    @Path("assessment")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response attachAssessmentPost(@PathParam("courseId") final Long courseId, @FormParam("parentNodeId") final String parentNodeId,
            @FormParam("position") final Integer position, @FormParam("shortTitle") @DefaultValue("undefined") final String shortTitle,
            @FormParam("longTitle") @DefaultValue("undefined") final String longTitle, @FormParam("objectives") @DefaultValue("undefined") final String objectives,
            @FormParam("visibilityExpertRules") final String visibilityExpertRules, @FormParam("accessExpertRules") final String accessExpertRules,
            @Context final HttpServletRequest request) {
        return attachAssessment(courseId, parentNodeId, position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, request);
    }

    /**
     * Attaches an assessment building block.
     * 
     * @response.representation.mediaType application/x-www-form-urlencoded
     * @response.representation.doc The assessment node metadatas
     * @response.representation.200.qname {http://www.example.com}courseNodeVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The course node metadatas
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or parentNode not found
     * @param courseId
     *            The course resourceable's id
     * @param parentNodeId
     *            The node's id which will be the parent of this assessment
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
     * @param request
     *            The HTTP request
     * @return
     */
    @PUT
    @Path("assessment")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response attachAssessment(@PathParam("courseId") final Long courseId, @QueryParam("parentNodeId") final String parentNodeId,
            @QueryParam("position") final Integer position, @QueryParam("shortTitle") @DefaultValue("undefined") final String shortTitle,
            @QueryParam("longTitle") @DefaultValue("undefined") final String longTitle, @QueryParam("objectives") @DefaultValue("undefined") final String objectives,
            @QueryParam("visibilityExpertRules") final String visibilityExpertRules, @QueryParam("accessExpertRules") final String accessExpertRules,
            @Context final HttpServletRequest request) {
        final AssessmentCustomConfig config = new AssessmentCustomConfig();
        return attach(courseId, parentNodeId, "ms", position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
    }

    /**
     * Attaches an wiki building block.
     * 
     * @response.representation.mediaType application/x-www-form-urlencoded
     * @response.representation.doc The assessment node metadatas
     * @response.representation.200.qname {http://www.example.com}courseNodeVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The course node metadatas
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or parentNode not found
     * @param courseId
     *            The course resourceable's id
     * @param parentNodeId
     *            The node's id which will be the parent of this assessment
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
     * @param request
     *            The HTTP request
     * @return
     */
    @POST
    @Path("wiki")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response attachWikiPost(@PathParam("courseId") final Long courseId, @QueryParam("parentNodeId") final String parentNodeId,
            @QueryParam("position") final Integer position, @QueryParam("shortTitle") @DefaultValue("undefined") final String shortTitle,
            @QueryParam("longTitle") @DefaultValue("undefined") final String longTitle, @QueryParam("objectives") @DefaultValue("undefined") final String objectives,
            @QueryParam("visibilityExpertRules") final String visibilityExpertRules, @QueryParam("accessExpertRules") final String accessExpertRules,
            @QueryParam("wikiResourceableId") final Long wikiResourceableId, @Context final HttpServletRequest request) {
        return attachWiki(courseId, parentNodeId, position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, wikiResourceableId, request);
    }

    /**
     * Attaches an wiki building block.
     * 
     * @response.representation.mediaType application/x-www-form-urlencoded
     * @response.representation.doc The assessment node metadatas
     * @response.representation.200.qname {http://www.example.com}courseNodeVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The course node metadatas
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or parentNode not found
     * @param courseId
     *            The course resourceable's id
     * @param parentNodeId
     *            The node's id which will be the parent of this assessment
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
     * @param request
     *            The HTTP request
     * @return
     */
    @PUT
    @Path("wiki")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response attachWiki(@PathParam("courseId") final Long courseId, @QueryParam("parentNodeId") final String parentNodeId,
            @QueryParam("position") final Integer position, @QueryParam("shortTitle") @DefaultValue("undefined") final String shortTitle,
            @QueryParam("longTitle") @DefaultValue("undefined") final String longTitle, @QueryParam("objectives") @DefaultValue("undefined") final String objectives,
            @QueryParam("visibilityExpertRules") final String visibilityExpertRules, @QueryParam("accessExpertRules") final String accessExpertRules,
            @QueryParam("wikiResourceableId") final Long wikiResourceableId, @Context final HttpServletRequest request) {
        final RepositoryService rm = RepositoryServiceImpl.getInstance();
        final RepositoryEntry wikiRepoEntry = rm.lookupRepositoryEntry(wikiResourceableId);
        if (wikiRepoEntry == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }
        final WikiCustomConfig config = new WikiCustomConfig(wikiRepoEntry);
        return attach(courseId, parentNodeId, "wiki", position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
    }

    /**
     * Attaches an blog building block.
     * 
     * @response.representation.mediaType application/x-www-form-urlencoded
     * @response.representation.doc The assessment node metadatas
     * @response.representation.200.qname {http://www.example.com}courseNodeVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The course node metadatas
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or parentNode not found
     * @param courseId
     *            The course resourceable's id
     * @param parentNodeId
     *            The node's id which will be the parent of this assessment
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
     * @param blogResourceableId
     *            The softkey of the blog resourceable (optional)
     * @param request
     *            The HTTP request
     * @return
     */
    @POST
    @Path("blog")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response attachBlogPost(@PathParam("courseId") final Long courseId, @QueryParam("parentNodeId") final String parentNodeId,
            @QueryParam("position") final Integer position, @QueryParam("shortTitle") @DefaultValue("undefined") final String shortTitle,
            @QueryParam("longTitle") @DefaultValue("undefined") final String longTitle, @QueryParam("objectives") @DefaultValue("undefined") final String objectives,
            @QueryParam("visibilityExpertRules") final String visibilityExpertRules, @QueryParam("accessExpertRules") final String accessExpertRules,
            @QueryParam("repoEntry") final Long blogResourceableId, @Context final HttpServletRequest request) {
        return attachBlog(courseId, parentNodeId, position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, blogResourceableId, request);
    }

    /**
     * Attaches an blog building block.
     * 
     * @response.representation.mediaType application/x-www-form-urlencoded
     * @response.representation.doc The assessment node metadatas
     * @response.representation.200.qname {http://www.example.com}courseNodeVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The course node metadatas
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or parentNode not found
     * @param courseId
     *            The course resourceable's id
     * @param parentNodeId
     *            The node's id which will be the parent of this assessment
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
     * @param blogResourceableId
     *            The softkey of the blog resourceable (optional)
     * @param request
     *            The HTTP request
     * @return
     */
    @PUT
    @Path("blog")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response attachBlog(@PathParam("courseId") final Long courseId, @QueryParam("parentNodeId") final String parentNodeId,
            @QueryParam("position") final Integer position, @QueryParam("shortTitle") @DefaultValue("undefined") final String shortTitle,
            @QueryParam("longTitle") @DefaultValue("undefined") final String longTitle, @QueryParam("objectives") @DefaultValue("undefined") final String objectives,
            @QueryParam("visibilityExpertRules") final String visibilityExpertRules, @QueryParam("accessExpertRules") final String accessExpertRules,
            @QueryParam("repoEntry") final Long blogResourceableId, @Context final HttpServletRequest request) {
        final RepositoryService rm = RepositoryServiceImpl.getInstance();
        final RepositoryEntry blogRepoEntry = rm.lookupRepositoryEntry(blogResourceableId);
        if (blogRepoEntry == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }
        final BlogCustomConfig config = new BlogCustomConfig(blogRepoEntry);

        return attach(courseId, parentNodeId, "blog", position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
    }

    /**
     * Attaches an survey building block.
     * 
     * @response.representation.mediaType application/x-www-form-urlencoded
     * @response.representation.doc The assessment node metadatas
     * @response.representation.200.qname {http://www.example.com}courseNodeVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The course node metadatas
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or parentNode not found
     * @param courseId
     *            The course resourceable's id
     * @param parentNodeId
     *            The node's id which will be the parent of this assessment
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
     * @param request
     *            The HTTP request
     * @return
     */
    @POST
    @Path("survey")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response attachSurveyPost(@PathParam("courseId") final Long courseId, @QueryParam("parentNodeId") final String parentNodeId,
            @QueryParam("position") final Integer position, @QueryParam("shortTitle") @DefaultValue("undefined") final String shortTitle,
            @QueryParam("longTitle") @DefaultValue("undefined") final String longTitle, @QueryParam("objectives") @DefaultValue("undefined") final String objectives,
            @QueryParam("visibilityExpertRules") final String visibilityExpertRules, @QueryParam("accessExpertRules") final String accessExpertRules,
            @QueryParam("surveyResourceableId") final Long surveyResourceableId, @Context final HttpServletRequest request) {
        return attachSurvey(courseId, parentNodeId, position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, surveyResourceableId, request);
    }

    /**
     * Attaches an survey building block.
     * 
     * @response.representation.mediaType application/x-www-form-urlencoded
     * @response.representation.doc The assessment node metadatas
     * @response.representation.200.qname {http://www.example.com}courseNodeVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The course node metadatas
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or parentNode not found
     * @param courseId
     *            The course resourceable's id
     * @param parentNodeId
     *            The node's id which will be the parent of this assessment
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
     * @param request
     *            The HTTP request
     * @return
     */
    @PUT
    @Path("survey")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response attachSurvey(@PathParam("courseId") final Long courseId, @QueryParam("parentNodeId") final String parentNodeId,
            @QueryParam("position") final Integer position, @QueryParam("shortTitle") @DefaultValue("undefined") final String shortTitle,
            @QueryParam("longTitle") @DefaultValue("undefined") final String longTitle, @QueryParam("objectives") @DefaultValue("undefined") final String objectives,
            @QueryParam("visibilityExpertRules") final String visibilityExpertRules, @QueryParam("accessExpertRules") final String accessExpertRules,
            @QueryParam("surveyResourceableId") final Long surveyResourceableId, @Context final HttpServletRequest request) {
        final RepositoryService rm = RepositoryServiceImpl.getInstance();
        final RepositoryEntry surveyRepoEntry = rm.lookupRepositoryEntry(surveyResourceableId);
        if (surveyRepoEntry == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }
        final CustomConfigDelegate config = CustomConfigFactory.getSurveyCustomConfig(surveyRepoEntry);

        return attach(courseId, parentNodeId, "iqsurv", position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
    }

    /**
     * Attaches an external page building block.
     * 
     * @response.representation.mediaType application/x-www-form-urlencoded
     * @response.representation.doc The external page node metadatas
     * @response.representation.200.qname {http://www.example.com}courseNodeVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The course node metadatas
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or parentNode not found
     * @param courseId
     *            The course resourceable's id
     * @param parentNodeId
     *            The node's id which will be the parent of this assessment
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
     * @param url
     *            The URL of the external page
     * @param request
     *            The HTTP request
     * @return
     */
    @POST
    @Path("externalpage")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response attachExternalPagePost(@PathParam("courseId") final Long courseId, @QueryParam("parentNodeId") final String parentNodeId,
            @QueryParam("position") final Integer position, @QueryParam("shortTitle") @DefaultValue("undefined") final String shortTitle,
            @QueryParam("longTitle") @DefaultValue("undefined") final String longTitle, @QueryParam("objectives") @DefaultValue("undefined") final String objectives,
            @QueryParam("visibilityExpertRules") final String visibilityExpertRules, @QueryParam("accessExpertRules") final String accessExpertRules,
            @QueryParam("url") final String url, @Context final HttpServletRequest request) {
        return attachExternalPage(courseId, parentNodeId, position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, url, request);
    }

    /**
     * Attaches an external page building block.
     * 
     * @response.representation.mediaType application/x-www-form-urlencoded
     * @response.representation.doc The external page node metadatas
     * @response.representation.200.qname {http://www.example.com}courseNodeVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The course node metadatas
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or parentNode not found
     * @response.representation.409.doc The given URL is not valid
     * @param courseId
     *            The course resourceable's id
     * @param parentNodeId
     *            The node's id which will be the parent of this assessment
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
     * @param url
     *            The URL of the external page
     * @param request
     *            The HTTP request
     * @return
     */
    @PUT
    @Path("externalpage")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response attachExternalPage(@PathParam("courseId") final Long courseId, @QueryParam("parentNodeId") final String parentNodeId,
            @QueryParam("position") final Integer position, @QueryParam("shortTitle") @DefaultValue("undefined") final String shortTitle,
            @QueryParam("longTitle") @DefaultValue("undefined") final String longTitle, @QueryParam("objectives") @DefaultValue("undefined") final String objectives,
            @QueryParam("visibilityExpertRules") final String visibilityExpertRules, @QueryParam("accessExpertRules") final String accessExpertRules,
            @QueryParam("url") final String url, @Context final HttpServletRequest request) {
        URL externalUrl = null;
        try {
            externalUrl = new URL(url);
        } catch (final MalformedURLException e) {
            return Response.serverError().status(Status.CONFLICT).build();
        }
        final ExternalPageCustomConfig config = new ExternalPageCustomConfig(externalUrl);

        return attach(courseId, parentNodeId, "tu", position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
    }

    /**
     * This attaches a Task file onto a given task element.
     * 
     * @response.representation.mediaType application/x-www-form-urlencoded
     * @response.representation.doc The task node metadatas
     * @response.representation.200.qname {http://www.example.com}courseNodeVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The course node metadatas
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or parentNode not found
     * @param courseId
     *            The course resourceable id
     * @param nodeId
     *            The node's id which will be the parent of this task file
     * @param request
     *            The HTTP request
     * @return The persisted task element (fully populated)
     */
    @POST
    @Path("task/{nodeId}/file")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response attachTaskFilePost(@PathParam("courseId") final Long courseId, @FormParam("nodeId") final String nodeId,
            @FormParam("filename") @DefaultValue("task") final String filename, @FormParam("file") final InputStream file, @Context final HttpServletRequest request) {
        return attachTaskFile(courseId, nodeId, filename, file, request);
    }

    /**
     * This attaches a Task file onto a given task element.
     * 
     * @response.representation.mediaType application/x-www-form-urlencoded
     * @response.representation.doc The task node metadatas
     * @response.representation.200.qname {http://www.example.com}courseNodeVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The course node metadatas
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or parentNode not found
     * @response.representation.406.doc The course node is not of type task
     * @param courseId
     *            The course resourceable id
     * @param nodeId
     *            The node's id which will be the parent of this task file
     * @param request
     *            The HTTP request
     * @return The persisted task element (fully populated)
     */
    @PUT
    @Path("task/{nodeId}/file")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response attachTaskFile(@PathParam("courseId") final Long courseId, @PathParam("nodeId") final String nodeId,
            @FormParam("filename") @DefaultValue("task") final String filename, @FormParam("file") final InputStream file, @Context final HttpServletRequest request) {
        final ICourse course = loadCourse(courseId);
        final CourseNode node = getParentNode(course, nodeId);
        if (course == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }
        if (node == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        } else if (!(node instanceof TACourseNode)) {
            return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
        }
        if (!isAuthorEditor(course, request)) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }
        final String taskFolderPath = TACourseNode.getTaskFolderPathRelToFolderRoot(course, node);
        final OlatRootFolderImpl taskFolder = new OlatRootFolderImpl(taskFolderPath, null);
        VFSLeaf singleFile = (VFSLeaf) taskFolder.resolve("/" + filename);
        if (singleFile == null) {
            singleFile = taskFolder.createChildLeaf("/" + filename);
        }
        if (file != null) {
            final OutputStream out = singleFile.getOutputStream(false);
            FileUtils.copy(file, out);
            FileUtils.closeSafely(out);
            FileUtils.closeSafely(file);
        } else {
            return Response.status(Status.NOT_ACCEPTABLE).build();
        }

        return Response.ok().build();
    }

    /**
     * This attaches the run-time configuration onto a given task element.
     * 
     * @response.representation.mediaType application/x-www-form-urlencoded
     * @response.representation.doc The task node configuration
     * @response.representation.200.qname {http://www.example.com}surveyConfigVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The task node configuration
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or task node not found
     * @response.representation.406.doc The call is not applicable to task course node
     * @response.representation.409.doc The configuration is not valid
     * @param courseId
     * @param nodeId
     * @param enableAssignment
     * @param taskAssignmentType
     * @param taskAssignmentText
     * @param enableTaskPreview
     * @param enableTaskDeselect
     * @param onlyOneUserPerTask
     * @param enableDropbox
     * @param enableDropboxConfirmationMail
     * @param dropboxConfirmationText
     * @param enableReturnbox
     * @param enableScoring
     * @param grantScoring
     * @param scoreMin
     * @param scoreMax
     * @param grantPassing
     * @param scorePassingThreshold
     * @param enableCommentField
     * @param commentForUser
     * @param commentForCoaches
     * @param enableSolution
     * @param accessExpertRuleTask
     * @param accessExpertRuleDropbox
     * @param accessExpertRuleReturnbox
     * @param accessExpertRuleScoring
     * @param accessExpertRuleSolution
     * @param request
     * @return
     */
    @POST
    @Path("task/{nodeId}/configuration")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response addTaskConfigurationPost(@PathParam("courseId") final Long courseId, @PathParam("nodeId") final String nodeId,
            @QueryParam("enableAssignment") final Boolean enableAssignment, @QueryParam("taskAssignmentType") final String taskAssignmentType,
            @QueryParam("taskAssignmentText") final String taskAssignmentText, @QueryParam("enableTaskPreview") final Boolean enableTaskPreview,
            @QueryParam("enableTaskDeselect") final Boolean enableTaskDeselect, @QueryParam("onlyOneUserPerTask") final Boolean onyOneUserPerTask,
            @QueryParam("enableDropbox") final Boolean enableDropbox, @QueryParam("enableDropboxConfirmationMail") final Boolean enableDropboxConfirmationMail,
            @QueryParam("dropboxConfirmationText") final String dropboxConfirmationText, @QueryParam("enableReturnbox") final Boolean enableReturnbox,
            @QueryParam("enableScoring") final Boolean enableScoring, @QueryParam("grantScoring") final Boolean grantScoring,
            @QueryParam("scoreMin") final Float scoreMin, @QueryParam("scoreMax") final Float scoreMax, @QueryParam("grantPassing") final Boolean grantPassing,
            @QueryParam("scorePassingThreshold") final Float scorePassingThreshold, @QueryParam("enableCommentField") final Boolean enableCommentField,
            @QueryParam("commentForUser") final String commentForUser, @QueryParam("commentForCoaches") final String commentForCoaches,
            @QueryParam("enableSolution") final Boolean enableSolution, @QueryParam("accessExpertRuleTask") final String accessExpertRuleTask,
            @QueryParam("accessExpertRuleDropbox") final String accessExpertRuleDropbox, @QueryParam("accessExpertRuleReturnbox") final String accessExpertRuleReturnbox,
            @QueryParam("accessExpertRuleScoring") final String accessExpertRuleScoring, @QueryParam("accessExpertRuleSolution") final String accessExpertRuleSolution,
            @Context final HttpServletRequest request) {

        return addTaskConfiguration(courseId, nodeId, enableAssignment, taskAssignmentType, taskAssignmentText, enableTaskPreview, enableTaskDeselect, onyOneUserPerTask,
                enableDropbox, enableDropboxConfirmationMail, dropboxConfirmationText, enableReturnbox, enableScoring, grantScoring, scoreMin, scoreMax, grantPassing,
                scorePassingThreshold, enableCommentField, commentForUser, commentForCoaches, enableSolution, accessExpertRuleTask, accessExpertRuleDropbox,
                accessExpertRuleReturnbox, accessExpertRuleScoring, accessExpertRuleSolution, request);
    }

    /**
     * This attaches the run-time configuration onto a given task element.
     * 
     * @response.representation.mediaType application/x-www-form-urlencoded
     * @response.representation.doc The task node configuration
     * @response.representation.200.qname {http://www.example.com}surveyConfigVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The task node configuration
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or task node not found
     * @response.representation.406.doc The call is not applicable to task course node
     * @response.representation.409.doc The configuration is not valid
     * @param courseId
     * @param nodeId
     * @param enableAssignment
     * @param taskAssignmentType
     * @param taskAssignmentText
     * @param enableTaskPreview
     * @param enableTaskDeselect
     * @param onlyOneUserPerTask
     * @param enableDropbox
     * @param enableDropboxConfirmationMail
     * @param dropboxConfirmationText
     * @param enableReturnbox
     * @param enableScoring
     * @param grantScoring
     * @param scoreMin
     * @param scoreMax
     * @param grantPassing
     * @param scorePassingThreshold
     * @param enableCommentField
     * @param commentForUser
     * @param commentForCoaches
     * @param enableSolution
     * @param accessExpertRuleTask
     * @param accessExpertRuleDropbox
     * @param accessExpertRuleReturnbox
     * @param accessExpertRuleScoring
     * @param accessExpertRuleSolution
     * @param request
     * @return
     */
    @PUT
    @Path("task/{nodeId}/configuration")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response addTaskConfiguration(@PathParam("courseId") final Long courseId, @PathParam("nodeId") final String nodeId,
            @QueryParam("enableAssignment") final Boolean enableAssignment, @QueryParam("taskAssignmentType") final String taskAssignmentType,
            @QueryParam("taskAssignmentText") final String taskAssignmentText, @QueryParam("enableTaskPreview") final Boolean enableTaskPreview,
            @QueryParam("enableTaskDeselect") final Boolean enableTaskDeselect, @QueryParam("onlyOneUserPerTask") final Boolean onlyOneUserPerTask,
            @QueryParam("enableDropbox") final Boolean enableDropbox, @QueryParam("enableDropboxConfirmationMail") final Boolean enableDropboxConfirmationMail,
            @QueryParam("dropboxConfirmationText") final String dropboxConfirmationText, @QueryParam("enableReturnbox") final Boolean enableReturnbox,
            @QueryParam("enableScoring") final Boolean enableScoring, @QueryParam("grantScoring") final Boolean grantScoring,
            @QueryParam("scoreMin") final Float scoreMin, @QueryParam("scoreMax") final Float scoreMax, @QueryParam("grantPassing") final Boolean grantPassing,
            @QueryParam("scorePassingThreshold") final Float scorePassingThreshold, @QueryParam("enableCommentField") final Boolean enableCommentField,
            @QueryParam("commentForUser") final String commentForUser, @QueryParam("commentForCoaches") final String commentForCoaches,
            @QueryParam("enableSolution") final Boolean enableSolution, @QueryParam("accessExpertRuleTask") final String accessExpertRuleTask,
            @QueryParam("accessExpertRuleDropbox") final String accessExpertRuleDropbox, @QueryParam("accessExpertRuleReturnbox") final String accessExpertRuleReturnbox,
            @QueryParam("accessExpertRuleScoring") final String accessExpertRuleScoring, @QueryParam("accessExpertRuleSolution") final String accessExpertRuleSolution,
            @Context final HttpServletRequest request) {

        final TaskFullConfig config = new TaskFullConfig(enableAssignment, taskAssignmentType, taskAssignmentText, enableTaskPreview, enableTaskDeselect,
                onlyOneUserPerTask, enableDropbox, enableDropboxConfirmationMail, dropboxConfirmationText, enableReturnbox, enableScoring, grantScoring, scoreMin,
                scoreMax, grantPassing, scorePassingThreshold, enableCommentField, commentForUser, commentForCoaches, enableSolution, accessExpertRuleTask,
                accessExpertRuleDropbox, accessExpertRuleReturnbox, accessExpertRuleScoring, accessExpertRuleSolution);

        return attachNodeConfig(courseId, nodeId, config, request);
    }

    /**
     * Retrieves configuration of the task course node
     * 
     * @response.representation.200.qname {http://www.example.com}surveyConfigVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The course node configuration
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or task node not found
     * @param courseId
     * @param nodeId
     * @return the task course node configuration
     */
    @GET
    @Path("task/{nodeId}/configuration")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getTaskConfiguration(@PathParam("courseId") final Long courseId, @PathParam("nodeId") final String nodeId) {

        final TaskConfigVO config = new TaskConfigVO();
        final ICourse course = loadCourse(courseId);
        final CourseNode courseNode = getParentNode(course, nodeId);
        final ModuleConfiguration moduleConfig = courseNode.getModuleConfiguration();
        // build configuration with fallback to default values
        final Boolean isAssignmentEnabled = (Boolean) moduleConfig.get(TACourseNode.CONF_TASK_ENABLED);
        config.setIsAssignmentEnabled(isAssignmentEnabled == null ? Boolean.TRUE : isAssignmentEnabled);
        final String taskAssignmentType = moduleConfig.getStringValue(TACourseNode.CONF_TASK_TYPE);
        config.setTaskAssignmentType(taskAssignmentType == null ? TaskController.TYPE_MANUAL : taskAssignmentType);
        final String taskAssignmentText = moduleConfig.getStringValue(TACourseNode.CONF_TASK_TEXT);
        config.setTaskAssignmentText(taskAssignmentText == null ? "" : taskAssignmentText);
        final Boolean isTaskPreviewEnabled = moduleConfig.get(TACourseNode.CONF_TASK_PREVIEW) == null ? Boolean.FALSE : moduleConfig
                .getBooleanEntry(TACourseNode.CONF_TASK_PREVIEW);
        config.setIsTaskPreviewEnabled(isTaskPreviewEnabled);
        final Boolean isTaskDeselectEnabled = moduleConfig.getBooleanEntry(TACourseNode.CONF_TASK_DESELECT);
        config.setIsTaskDeselectEnabled(isTaskDeselectEnabled == null ? Boolean.FALSE : isTaskDeselectEnabled);
        final Boolean onlyOneUserPerTask = (Boolean) moduleConfig.get(TACourseNode.CONF_TASK_SAMPLING_WITH_REPLACEMENT);
        config.setOnlyOneUserPerTask(onlyOneUserPerTask == null ? Boolean.TRUE : onlyOneUserPerTask);
        final Boolean isDropboxEnabled = (Boolean) moduleConfig.get(TACourseNode.CONF_DROPBOX_ENABLED);
        config.setIsDropboxEnabled(isDropboxEnabled == null ? Boolean.TRUE : isDropboxEnabled);
        final Boolean isDropboxConfirmationMailEnabled = (Boolean) moduleConfig.get(TACourseNode.CONF_DROPBOX_CONFIRMATION_REQUESTED);
        config.setIsDropboxConfirmationMailEnabled(isDropboxConfirmationMailEnabled == null ? Boolean.FALSE : isDropboxConfirmationMailEnabled);

        final Boolean isReturnboxEnabled = (Boolean) moduleConfig.get(TACourseNode.CONF_RETURNBOX_ENABLED);
        config.setIsReturnboxEnabled(isReturnboxEnabled == null ? Boolean.TRUE : isReturnboxEnabled);
        final Boolean isScoringEnabled = (Boolean) moduleConfig.get(TACourseNode.CONF_SCORING_ENABLED);
        config.setIsScoringEnabled(isScoringEnabled == null ? Boolean.TRUE : isScoringEnabled);
        final Boolean isScoringGranted = (Boolean) moduleConfig.get(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD);
        config.setIsScoringGranted(isScoringGranted == null ? Boolean.FALSE : isScoringGranted);
        final Float minScore = (Float) moduleConfig.get(MSCourseNode.CONFIG_KEY_SCORE_MIN);
        config.setMinScore(minScore);
        final Float maxScore = (Float) moduleConfig.get(MSCourseNode.CONFIG_KEY_SCORE_MAX);
        config.setMaxScore(maxScore);
        final Boolean isPassingGranted = (Boolean) moduleConfig.get(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD);
        config.setIsPassingGranted(isPassingGranted == null ? Boolean.FALSE : isPassingGranted);
        final Float passingScoreThreshold = (Float) moduleConfig.get(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
        config.setPassingScoreThreshold(passingScoreThreshold);
        final Boolean hasCommentField = (Boolean) moduleConfig.get(MSCourseNode.CONFIG_KEY_HAS_COMMENT_FIELD);
        config.setHasCommentField(hasCommentField == null ? Boolean.FALSE : hasCommentField);
        final String commentForUser = moduleConfig.getStringValue(MSCourseNode.CONFIG_KEY_INFOTEXT_USER);
        config.setCommentForUser(commentForUser == null ? "" : commentForUser);
        final String commentForCoaches = moduleConfig.getStringValue(MSCourseNode.CONFIG_KEY_INFOTEXT_COACH);
        config.setCommentForCoaches(commentForCoaches == null ? "" : commentForCoaches);
        final Boolean isSolutionEnabled = (Boolean) moduleConfig.get(TACourseNode.CONF_SOLUTION_ENABLED);
        config.setIsSolutionEnabled(isSolutionEnabled == null ? Boolean.TRUE : isSolutionEnabled);
        // get the conditions
        final List lstConditions = courseNode.getConditionExpressions();
        for (final Object obj : lstConditions) {
            final ConditionExpression cond = (ConditionExpression) obj;
            final String id = cond.getId();
            final String expression = cond.getExptressionString();
            if (id.equals(TACourseNode.ACCESS_TASK)) {
                config.setConditionTask(expression);
            } else if (id.equals("drop")) {
                config.setConditionDropbox(expression);
            } else if (id.equals(TACourseNode.ACCESS_RETURNBOX)) {
                config.setConditionReturnbox(expression);
            } else if (id.equals(TACourseNode.ACCESS_SCORING)) {
                config.setConditionScoring(expression);
            } else if (id.equals(TACourseNode.ACCESS_SOLUTION)) {
                config.setConditionSolution(expression);
            }
        }

        return Response.ok(config).build();
    }

    /**
     * This attaches the run-time configuration onto a given survey element.
     * 
     * @response.representation.mediaType application/x-www-form-urlencoded
     * @response.representation.doc The test node configuration
     * @response.representation.200.qname {http://www.example.com}surveyConfigVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The survey node configuration
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or survey node not found
     * @response.representation.406.doc The call is not applicable to survey course node
     * @response.representation.409.doc The configuration is not valid
     * @param courseId
     * @param nodeId
     * @param allowCancel
     * @param allowNavigation
     * @param allowSuspend
     * @param sequencePresentation
     * @param showNavigation
     * @param showQuestionTitle
     * @param showSectionsOnly
     * @param request
     * @return
     */
    @POST
    @Path("survey/{nodeId}/configuration")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response addSurveyConfigurationPost(@PathParam("courseId") final Long courseId, @PathParam("nodeId") final String nodeId,
            @QueryParam("allowCancel") @DefaultValue("false") final Boolean allowCancel,
            @QueryParam("allowNavigation") @DefaultValue("false") final Boolean allowNavigation,
            @QueryParam("allowSuspend") @DefaultValue("false") final Boolean allowSuspend,
            @QueryParam("sequencePresentation") @DefaultValue(AssessmentInstance.QMD_ENTRY_SEQUENCE_ITEM) final String sequencePresentation,
            @QueryParam("showNavigation") @DefaultValue("true") final Boolean showNavigation,
            @QueryParam("showQuestionTitle") @DefaultValue("true") final Boolean showQuestionTitle,
            @QueryParam("showSectionsOnly") @DefaultValue("false") final Boolean showSectionsOnly, @Context final HttpServletRequest request) {

        return addSurveyConfiguration(courseId, nodeId, allowCancel, allowNavigation, allowSuspend, sequencePresentation, showNavigation, showQuestionTitle,
                showSectionsOnly, request);
    }

    /**
     * This attaches the run-time configuration onto a given survey element.
     * 
     * @response.representation.mediaType application/x-www-form-urlencoded
     * @response.representation.doc The test node configuration
     * @response.representation.200.qname {http://www.example.com}surveyConfigVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The survey node configuration
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or survey node not found
     * @response.representation.406.doc The call is not applicable to survey course node
     * @response.representation.409.doc The configuration is not valid
     * @param courseId
     * @param nodeId
     * @param allowCancel
     * @param allowNavigation
     * @param allowSuspend
     * @param sequencePresentation
     * @param showNavigation
     * @param showQuestionTitle
     * @param showSectionsOnly
     * @param request
     * @return
     */
    @PUT
    @Path("survey/{nodeId}/configuration")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response addSurveyConfiguration(@PathParam("courseId") final Long courseId, @PathParam("nodeId") final String nodeId,
            @QueryParam("allowCancel") @DefaultValue("false") final Boolean allowCancel,
            @QueryParam("allowNavigation") @DefaultValue("false") final Boolean allowNavigation,
            @QueryParam("allowSuspend") @DefaultValue("false") final Boolean allowSuspend,
            @QueryParam("sequencePresentation") @DefaultValue(AssessmentInstance.QMD_ENTRY_SEQUENCE_ITEM) final String sequencePresentation,
            @QueryParam("showNavigation") @DefaultValue("true") final Boolean showNavigation,
            @QueryParam("showQuestionTitle") @DefaultValue("true") final Boolean showQuestionTitle,
            @QueryParam("showSectionsOnly") @DefaultValue("false") final Boolean showSectionsOnly, @Context final HttpServletRequest request) {

        final SurveyFullConfig config = new SurveyFullConfig(allowCancel, allowNavigation, allowSuspend, sequencePresentation, showNavigation, showQuestionTitle,
                showSectionsOnly);

        return attachNodeConfig(courseId, nodeId, config, request);
    }

    /**
     * Retrieves configuration of the survey course node
     * 
     * @response.representation.200.qname {http://www.example.com}surveyConfigVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The course node configuration
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or survey node not found
     * @param courseId
     * @param nodeId
     * @return survey course node configuration
     */
    @GET
    @Path("survey/{nodeId}/configuration")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getSurveyConfiguration(@PathParam("courseId") final Long courseId, @PathParam("nodeId") final String nodeId) {

        final SurveyConfigVO config = new SurveyConfigVO();
        final ICourse course = loadCourse(courseId);
        final CourseNode courseNode = getParentNode(course, nodeId);
        final ModuleConfiguration moduleConfig = courseNode.getModuleConfiguration();
        // build configuration with fallback to default values
        final Boolean allowCancel = (Boolean) moduleConfig.get(IQEditController.CONFIG_KEY_ENABLECANCEL);
        config.setAllowCancel(allowCancel == null ? false : allowCancel);
        final Boolean allowNavi = (Boolean) moduleConfig.get(IQEditController.CONFIG_KEY_ENABLEMENU);
        config.setAllowNavigation(allowNavi == null ? false : allowNavi);
        final Boolean allowSuspend = (Boolean) moduleConfig.get(IQEditController.CONFIG_KEY_ENABLESUSPEND);
        config.setAllowSuspend(allowSuspend == null ? false : allowSuspend);
        config.setSequencePresentation(moduleConfig.getStringValue(IQEditController.CONFIG_KEY_SEQUENCE, AssessmentInstance.QMD_ENTRY_SEQUENCE_ITEM));
        final Boolean showNavi = (Boolean) moduleConfig.get(IQEditController.CONFIG_KEY_DISPLAYMENU);
        config.setShowNavigation(showNavi == null ? true : showNavi);
        final Boolean showQuestionTitle = (Boolean) moduleConfig.get(IQEditController.CONFIG_KEY_QUESTIONTITLE);
        config.setShowQuestionTitle(showQuestionTitle == null ? true : showQuestionTitle);
        final Boolean showSectionsOnly = (Boolean) moduleConfig.get(IQEditController.CONFIG_KEY_RENDERMENUOPTION);
        config.setShowSectionsOnly(showSectionsOnly == null ? false : showSectionsOnly);

        return Response.ok(config).build();
    }

    /**
     * This attaches the run-time configuration onto a given test element.
     * 
     * @response.representation.mediaType application/x-www-form-urlencoded
     * @response.representation.doc The test node configuration
     * @response.representation.200.qname {http://www.example.com}testConfigVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The test node configuration
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or test node not found
     * @response.representation.406.doc The call is not applicable to test course node
     * @response.representation.409.doc The configuration is not valid
     * @param courseId
     * @param nodeId
     * @param allowCancel
     * @param allowNavigation
     * @param allowSuspend
     * @param numAttempts
     * @param sequencePresentation
     * @param showNavigation
     * @param showQuestionTitle
     * @param showResultsAfterFinish
     * @param showResultsDependendOnDate
     * @param showResultsOnHomepage
     * @param showScoreInfo
     * @param showQuestionProgress
     * @param showScoreProgress
     * @param showSectionsOnly
     * @param summaryPresentation
     * @param startDate
     * @param endDate
     * @param request
     * @return
     */
    @POST
    @Path("test/{nodeId}/configuration")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response addTestConfigurationPost(@PathParam("courseId") final Long courseId, @PathParam("nodeId") final String nodeId,
            @QueryParam("allowCancel") @DefaultValue("false") final Boolean allowCancel,
            @QueryParam("allowNavigation") @DefaultValue("false") final Boolean allowNavigation,
            @QueryParam("allowSuspend") @DefaultValue("false") final Boolean allowSuspend, @QueryParam("numAttempts") @DefaultValue("0") final int numAttempts,
            @QueryParam("sequencePresentation") @DefaultValue(AssessmentInstance.QMD_ENTRY_SEQUENCE_ITEM) final String sequencePresentation,
            @QueryParam("showNavigation") @DefaultValue("true") final Boolean showNavigation,
            @QueryParam("showQuestionTitle") @DefaultValue("true") final Boolean showQuestionTitle,
            @QueryParam("showResultsAfterFinish") @DefaultValue("true") final Boolean showResultsAfterFinish,
            @QueryParam("showResultsDependendOnDate") @DefaultValue("false") final Boolean showResultsDependendOnDate,
            @QueryParam("showResultsOnHomepage") @DefaultValue("false") final Boolean showResultsOnHomepage,
            @QueryParam("showScoreInfo") @DefaultValue("true") final Boolean showScoreInfo,
            @QueryParam("showQuestionProgress") @DefaultValue("true") final Boolean showQuestionProgress,
            @QueryParam("showScoreProgress") @DefaultValue("true") final Boolean showScoreProgress,
            @QueryParam("showSectionsOnly") @DefaultValue("false") final Boolean showSectionsOnly,
            @QueryParam("summaryPresentation") @DefaultValue(AssessmentInstance.QMD_ENTRY_SUMMARY_COMPACT) final String summaryPresentation,
            @QueryParam("startDate") final Long startDate, @QueryParam("endDate") final Long endDate, @Context final HttpServletRequest request) {

        return addTestConfiguration(courseId, nodeId, allowCancel, allowNavigation, allowSuspend, numAttempts, sequencePresentation, showNavigation, showQuestionTitle,
                showResultsAfterFinish, showResultsDependendOnDate, showResultsOnHomepage, showScoreInfo, showQuestionProgress, showScoreProgress, showSectionsOnly,
                summaryPresentation, startDate, endDate, request);
    }

    /**
     * This attaches the run-time configuration onto a given test element.
     * 
     * @response.representation.mediaType application/x-www-form-urlencoded
     * @response.representation.doc The test node configuration
     * @response.representation.200.qname {http://www.example.com}testConfigVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The test node configuration
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or test node not found
     * @response.representation.406.doc The call is not applicable to test course node
     * @response.representation.409.doc The configuration is not valid
     * @param courseId
     * @param nodeId
     * @param allowCancel
     * @param allowNavigation
     * @param allowSuspend
     * @param numAttempts
     * @param sequencePresentation
     * @param showNavigation
     * @param showQuestionTitle
     * @param showResultsAfterFinish
     * @param showResultsDependendOnDate
     * @param showResultsOnHomepage
     * @param showScoreInfo
     * @param showQuestionProgress
     * @param showScoreProgress
     * @param showSectionsOnly
     * @param summaryPresentation
     * @param startDate
     * @param endDate
     * @param request
     * @return
     */
    @PUT
    @Path("test/{nodeId}/configuration")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response addTestConfiguration(@PathParam("courseId") final Long courseId, @PathParam("nodeId") final String nodeId,
            @QueryParam("allowCancel") @DefaultValue("false") final Boolean allowCancel,
            @QueryParam("allowNavigation") @DefaultValue("false") final Boolean allowNavigation,
            @QueryParam("allowSuspend") @DefaultValue("false") final Boolean allowSuspend, @QueryParam("numAttempts") @DefaultValue("0") final int numAttempts,
            @QueryParam("sequencePresentation") @DefaultValue(AssessmentInstance.QMD_ENTRY_SEQUENCE_ITEM) final String sequencePresentation,
            @QueryParam("showNavigation") @DefaultValue("true") final Boolean showNavigation,
            @QueryParam("showQuestionTitle") @DefaultValue("true") final Boolean showQuestionTitle,
            @QueryParam("showResultsAfterFinish") @DefaultValue("true") final Boolean showResultsAfterFinish,
            @QueryParam("showResultsDependendOnDate") @DefaultValue("false") final Boolean showResultsDependendOnDate,
            @QueryParam("showResultsOnHomepage") @DefaultValue("false") final Boolean showResultsOnHomepage,
            @QueryParam("showScoreInfo") @DefaultValue("true") final Boolean showScoreInfo,
            @QueryParam("showQuestionProgress") @DefaultValue("true") final Boolean showQuestionProgress,
            @QueryParam("showScoreProgress") @DefaultValue("true") final Boolean showScoreProgress,
            @QueryParam("showSectionsOnly") @DefaultValue("false") final Boolean showSectionsOnly,
            @QueryParam("summaryPresentation") @DefaultValue(AssessmentInstance.QMD_ENTRY_SUMMARY_COMPACT) final String summaryPresentation,
            @QueryParam("startDate") final Long startDate, @QueryParam("endDate") final Long endDate, @Context final HttpServletRequest request) {

        final TestFullConfig config = new TestFullConfig(allowCancel, allowNavigation, allowSuspend, numAttempts, sequencePresentation, showNavigation,
                showQuestionTitle, showResultsAfterFinish, showResultsDependendOnDate, showResultsOnHomepage, showScoreInfo, showQuestionProgress, showScoreProgress,
                showSectionsOnly, summaryPresentation, startDate, endDate);

        return attachNodeConfig(courseId, nodeId, config, request);
    }

    /**
     * Retrieves configuration of the test course node
     * 
     * @response.representation.200.qname {http://www.example.com}testConfigVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The course node configuration
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_COURSENODEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or test node not found
     * @param courseId
     * @param nodeId
     * @return test course node configuration
     */
    @GET
    @Path("test/{nodeId}/configuration")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getTestConfiguration(@PathParam("courseId") final Long courseId, @PathParam("nodeId") final String nodeId) {

        final TestConfigVO config = new TestConfigVO();
        final ICourse course = loadCourse(courseId);
        final CourseNode courseNode = getParentNode(course, nodeId);
        // build configuration with fallback to default values
        final ModuleConfiguration moduleConfig = courseNode.getModuleConfiguration();
        final Boolean allowCancel = (Boolean) moduleConfig.get(IQEditController.CONFIG_KEY_ENABLECANCEL);
        config.setAllowCancel(allowCancel == null ? false : allowCancel);
        final Boolean allowNavi = (Boolean) moduleConfig.get(IQEditController.CONFIG_KEY_ENABLEMENU);
        config.setAllowNavigation(allowNavi == null ? false : allowNavi);
        final Boolean allowSuspend = (Boolean) moduleConfig.get(IQEditController.CONFIG_KEY_ENABLESUSPEND);
        config.setAllowSuspend(allowSuspend == null ? false : allowSuspend);
        config.setNumAttempts(moduleConfig.getIntegerSafe(IQEditController.CONFIG_KEY_ATTEMPTS, 0));
        config.setSequencePresentation(moduleConfig.getStringValue(IQEditController.CONFIG_KEY_SEQUENCE, AssessmentInstance.QMD_ENTRY_SEQUENCE_ITEM));
        final Boolean showNavi = (Boolean) moduleConfig.get(IQEditController.CONFIG_KEY_DISPLAYMENU);
        config.setShowNavigation(showNavi == null ? true : showNavi);
        final Boolean showQuestionTitle = (Boolean) moduleConfig.get(IQEditController.CONFIG_KEY_QUESTIONTITLE);
        config.setShowQuestionTitle(showQuestionTitle == null ? true : showQuestionTitle);
        final Boolean showResFinish = (Boolean) moduleConfig.get(IQEditController.CONFIG_KEY_RESULT_ON_FINISH);
        config.setShowResultsAfterFinish(showResFinish == null ? true : showResFinish);
        final Boolean showResDate = (Boolean) moduleConfig.get(IQEditController.CONFIG_KEY_DATE_DEPENDENT_RESULTS);
        config.setShowResultsDependendOnDate(showResDate == null ? false : showResDate);
        config.setShowResultsStartDate((Date) moduleConfig.get(IQEditController.CONFIG_KEY_RESULTS_START_DATE));
        config.setShowResultsEndDate((Date) moduleConfig.get(IQEditController.CONFIG_KEY_RESULTS_END_DATE));
        final Boolean showResHomepage = (Boolean) moduleConfig.get(IQEditController.CONFIG_KEY_RESULT_ON_HOME_PAGE);
        config.setShowResultsOnHomepage(showResHomepage == null ? false : showResHomepage);
        final Boolean showScoreInfo = (Boolean) moduleConfig.get(IQEditController.CONFIG_KEY_ENABLESCOREINFO);
        config.setShowScoreInfo(showScoreInfo == null ? true : showScoreInfo);
        final Boolean showQuestionProgress = (Boolean) moduleConfig.get(IQEditController.CONFIG_KEY_QUESTIONPROGRESS);
        config.setShowQuestionProgress(showQuestionProgress == null ? true : showQuestionProgress);
        final Boolean showScoreProgress = (Boolean) moduleConfig.get(IQEditController.CONFIG_KEY_SCOREPROGRESS);
        config.setShowScoreProgress(showScoreProgress == null ? true : showScoreProgress);
        final Boolean showSectionsOnly = (Boolean) moduleConfig.get(IQEditController.CONFIG_KEY_RENDERMENUOPTION);
        config.setShowSectionsOnly(showSectionsOnly == null ? false : showSectionsOnly);
        config.setSummeryPresentation(moduleConfig.getStringValue(IQEditController.CONFIG_KEY_SUMMARY, AssessmentInstance.QMD_ENTRY_SUMMARY_COMPACT));

        return Response.ok(config).build();
    }

    public class ExternalPageCustomConfig implements CustomConfigDelegate {
        private final URL url;

        public ExternalPageCustomConfig(final URL url) {
            this.url = url;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void configure(final ICourse course, final CourseNode newNode, final ModuleConfiguration moduleConfig) {
            moduleConfig.setBooleanEntry(NodeEditController.CONFIG_STARTPAGE, Boolean.FALSE.booleanValue());

            moduleConfig.setConfigurationVersion(2);
            moduleConfig.set(TUConfigForm.CONFIGKEY_PROTO, url.getProtocol());
            moduleConfig.set(TUConfigForm.CONFIGKEY_HOST, url.getHost());
            moduleConfig.set(TUConfigForm.CONFIGKEY_URI, url.getPath());
            moduleConfig.set(TUConfigForm.CONFIGKEY_QUERY, url.getQuery());
            final int port = url.getPort();
            moduleConfig.set(TUConfigForm.CONFIGKEY_PORT, new Integer(port != -1 ? port : url.getDefaultPort()));
            moduleConfig.setBooleanEntry(TUConfigForm.CONFIG_IFRAME, true);
        }

    }

    public class WikiCustomConfig implements CustomConfigDelegate {
        private final RepositoryEntry wikiRepoEntry;

        public WikiCustomConfig(final RepositoryEntry wikiRepoEntry) {
            this.wikiRepoEntry = wikiRepoEntry;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void configure(final ICourse course, final CourseNode newNode, final ModuleConfiguration moduleConfig) {
            if (wikiRepoEntry != null) {
                moduleConfig.set(ModuleConfiguration.CONFIG_KEY_REPOSITORY_REF, wikiRepoEntry.getSoftkey());
            }
            moduleConfig.setBooleanEntry(NodeEditController.CONFIG_STARTPAGE, false);
            moduleConfig.setConfigurationVersion(1);
        }
    }

    public class BlogCustomConfig implements CustomConfigDelegate {
        private final RepositoryEntry blogRepoEntry;

        public BlogCustomConfig(final RepositoryEntry blogRepoEntry) {
            this.blogRepoEntry = blogRepoEntry;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void configure(final ICourse course, final CourseNode newNode, final ModuleConfiguration moduleConfig) {
            if (blogRepoEntry != null) {
                moduleConfig.set(ModuleConfiguration.CONFIG_KEY_REPOSITORY_REF, blogRepoEntry.getSoftkey());
            }
        }
    }

    public class TaskFullConfig implements FullConfigDelegate {

        private final Boolean enableAssignment;
        private final String taskAssignmentText;
        private final String taskAssignmentType;
        private final Boolean enableTaskDeselect;
        private final Boolean enableTaskPreview;
        private final Boolean onlyOneUserPerTask;
        private final Boolean enableDropbox;
        private final String dropboxConfirmationText;
        private final Boolean enableDropboxConfirmationMail;
        private final Boolean enableReturnbox;
        private final Boolean grantPassing;
        private final Boolean grantScoring;
        private final Boolean enableScoring;
        private final Float scoreMin;
        private final Float scoreMax;
        private final Boolean enableCommentField;
        private final Float scorePassingThreshold;
        private final String commentForUser;
        private final String commentForCoaches;
        private final String accessExpertRuleTask;
        private final Boolean enableSolution;
        private final String accessExpertRuleSolution;
        private final String accessExpertRuleReturnbox;
        private final String accessExpertRuleDropbox;
        private final String accessExpertRuleScoring;

        public TaskFullConfig(final Boolean enableAssignment, final String taskAssignmentType, final String taskAssignmentText, final Boolean enableTaskPreview,
                final Boolean enableTaskDeselect, final Boolean onlyOneUserPerTask, final Boolean enableDropbox, final Boolean enableDropboxConfirmationMail,
                final String dropboxConfirmationText, final Boolean enableReturnbox, final Boolean enableScoring, final Boolean grantScoring, final Float scoreMin,
                final Float scoreMax, final Boolean grantPassing, final Float scorePassingThreshold, final Boolean enableCommentField, final String commentForUser,
                final String commentForCoaches, final Boolean enableSolution, final String accessExpertRuleTask, final String accessExpertRuleDropbox,
                final String accessExpertRuleReturnbox, final String accessExpertRuleScoring, final String accessExpertRuleSolution) {
            this.enableAssignment = enableAssignment;
            this.taskAssignmentType = taskAssignmentType;
            this.taskAssignmentText = taskAssignmentText;
            this.enableTaskPreview = enableTaskPreview;
            this.enableTaskDeselect = enableTaskDeselect;
            this.onlyOneUserPerTask = onlyOneUserPerTask;
            this.enableDropbox = enableDropbox;
            this.enableDropboxConfirmationMail = enableDropboxConfirmationMail;
            this.dropboxConfirmationText = dropboxConfirmationText;
            this.enableReturnbox = enableReturnbox;
            this.enableScoring = enableScoring;
            this.grantScoring = grantScoring;
            this.scoreMin = scoreMin;
            this.scoreMax = scoreMax;
            this.grantPassing = grantPassing;
            this.scorePassingThreshold = scorePassingThreshold;
            this.enableCommentField = enableCommentField;
            this.commentForUser = commentForUser;
            this.commentForCoaches = commentForCoaches;
            this.enableSolution = enableSolution;
            this.accessExpertRuleTask = accessExpertRuleTask;
            this.accessExpertRuleDropbox = accessExpertRuleDropbox;
            this.accessExpertRuleReturnbox = accessExpertRuleReturnbox;
            this.accessExpertRuleScoring = accessExpertRuleScoring;
            this.accessExpertRuleSolution = accessExpertRuleSolution;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public boolean isApplicable(final ICourse course, final CourseNode courseNode) {
            return courseNode instanceof TACourseNode;
        }

        @Override
        public void configure(final ICourse course, final CourseNode newNode, final ModuleConfiguration moduleConfig) {
            // module configuration
            if (enableAssignment != null) {
                moduleConfig.set(TACourseNode.CONF_TASK_ENABLED, enableAssignment);
            }
            if (taskAssignmentType != null) {
                moduleConfig.setStringValue(TACourseNode.CONF_TASK_TYPE, taskAssignmentType);
            }
            if (taskAssignmentText != null) {
                moduleConfig.setStringValue(TACourseNode.CONF_TASK_TEXT, taskAssignmentText);
            }
            if (enableTaskPreview != null) {
                moduleConfig.setBooleanEntry(TACourseNode.CONF_TASK_PREVIEW, enableTaskPreview);
            }
            if (enableTaskDeselect != null) {
                moduleConfig.setBooleanEntry(TACourseNode.CONF_TASK_DESELECT, enableTaskDeselect);
            }
            if (onlyOneUserPerTask != null) {
                moduleConfig.set(TACourseNode.CONF_TASK_SAMPLING_WITH_REPLACEMENT, onlyOneUserPerTask);
            }
            if (enableDropbox != null) {
                moduleConfig.set(TACourseNode.CONF_DROPBOX_ENABLED, enableDropbox);
            }
            if (enableDropboxConfirmationMail != null) {
                moduleConfig.set(TACourseNode.CONF_DROPBOX_CONFIRMATION_REQUESTED, enableDropboxConfirmationMail);
            }

            if (enableReturnbox != null) {
                moduleConfig.set(TACourseNode.CONF_RETURNBOX_ENABLED, enableReturnbox);
            }
            if (enableScoring != null) {
                moduleConfig.set(TACourseNode.CONF_SCORING_ENABLED, enableScoring);
            }
            if (grantScoring != null) {
                moduleConfig.set(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD, grantScoring);
            }
            if (scoreMin != null) {
                moduleConfig.set(MSCourseNode.CONFIG_KEY_SCORE_MIN, scoreMin);
            }
            if (scoreMax != null) {
                moduleConfig.set(MSCourseNode.CONFIG_KEY_SCORE_MAX, scoreMax);
            }
            if (grantPassing != null) {
                moduleConfig.set(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD, grantPassing);
            }
            if (scorePassingThreshold != null) {
                moduleConfig.set(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE, scorePassingThreshold);
            }
            if (enableCommentField != null) {
                moduleConfig.set(MSCourseNode.CONFIG_KEY_HAS_COMMENT_FIELD, enableCommentField);
            }
            if (commentForUser != null) {
                moduleConfig.setStringValue(MSCourseNode.CONFIG_KEY_INFOTEXT_USER, commentForUser);
            }
            if (commentForCoaches != null) {
                moduleConfig.setStringValue(MSCourseNode.CONFIG_KEY_INFOTEXT_COACH, commentForCoaches);
            }
            if (enableSolution != null) {
                moduleConfig.set(TACourseNode.CONF_SOLUTION_ENABLED, enableSolution);
            }
            // conditions
            final TACourseNode courseNode = (TACourseNode) newNode;
            createExpertCondition("drop", accessExpertRuleDropbox);
            if (accessExpertRuleTask != null) {
                courseNode.setConditionTask(createExpertCondition(TACourseNode.ACCESS_TASK, accessExpertRuleTask));
            }
            if (accessExpertRuleDropbox != null) {
                courseNode.setConditionDrop(createExpertCondition(TACourseNode.ACCESS_DROPBOX, accessExpertRuleDropbox));
            }
            if (accessExpertRuleReturnbox != null) {
                courseNode.setConditionReturnbox(createExpertCondition(TACourseNode.ACCESS_RETURNBOX, accessExpertRuleReturnbox));
            }
            if (accessExpertRuleScoring != null) {
                courseNode.setConditionScoring(createExpertCondition(TACourseNode.ACCESS_SCORING, accessExpertRuleScoring));
            }
            if (accessExpertRuleSolution != null) {
                courseNode.setConditionSolution(createExpertCondition(TACourseNode.ACCESS_SOLUTION, accessExpertRuleSolution));
            }
        }
    }

    public class SurveyFullConfig implements FullConfigDelegate {

        private final Boolean allowSuspend;
        private final Boolean allowNavigation;
        private final Boolean allowCancel;
        private final Boolean showSectionsOnly;
        private final Boolean showQuestionTitle;
        private final Boolean showNavigation;
        private final String sequencePresentation;

        public SurveyFullConfig(final Boolean allowCancel, final Boolean allowNavigation, final Boolean allowSuspend, final String sequencePresentation,
                final Boolean showNavigation, final Boolean showQuestionTitle, final Boolean showSectionsOnly) {
            this.allowCancel = allowCancel;
            this.allowNavigation = allowNavigation;
            this.allowSuspend = allowSuspend;
            this.sequencePresentation = sequencePresentation;
            this.showNavigation = showNavigation;
            this.showQuestionTitle = showQuestionTitle;
            this.showSectionsOnly = showSectionsOnly;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public boolean isApplicable(final ICourse course, final CourseNode courseNode) {
            return courseNode instanceof IQSURVCourseNode;
        }

        @Override
        public void configure(final ICourse course, final CourseNode newNode, final ModuleConfiguration moduleConfig) {
            if (allowCancel != null) {
                moduleConfig.set(IQEditController.CONFIG_KEY_ENABLECANCEL, allowCancel);
            }
            if (allowNavigation != null) {
                moduleConfig.set(IQEditController.CONFIG_KEY_ENABLEMENU, allowNavigation);
            }
            if (allowSuspend != null) {
                moduleConfig.set(IQEditController.CONFIG_KEY_ENABLESUSPEND, allowSuspend);
            }
            if (sequencePresentation != null) {
                moduleConfig.set(IQEditController.CONFIG_KEY_SEQUENCE, sequencePresentation);
            }
            if (showNavigation != null) {
                moduleConfig.set(IQEditController.CONFIG_KEY_DISPLAYMENU, showNavigation);
            }
            if (showQuestionTitle != null) {
                moduleConfig.set(IQEditController.CONFIG_KEY_QUESTIONTITLE, showQuestionTitle);
            }
            if (showSectionsOnly != null) {
                moduleConfig.set(IQEditController.CONFIG_KEY_RENDERMENUOPTION, showSectionsOnly);
            }
        }
    }

    public class TestFullConfig implements FullConfigDelegate {

        private final Boolean allowCancel;
        private final Boolean allowSuspend;
        private final Boolean allowNavigation;
        private final Boolean showResultsDependendOnDate;
        private final Boolean showNavigation;
        private final String sequencePresentation;
        private final Boolean showQuestionTitle;
        private final Integer numAttempts;
        private final Boolean showScoreInfo;
        private final Boolean showResultsOnHomepage;
        private final Boolean showResultsAfterFinish;
        private final Boolean showScoreProgress;
        private final Boolean showQuestionProgress;
        private final Boolean showSectionsOnly;
        private final Long startDate;
        private final String summaryPresentation;
        private final Long endDate;

        public TestFullConfig(final Boolean allowCancel, final Boolean allowNavigation, final Boolean allowSuspend, final int numAttempts,
                final String sequencePresentation, final Boolean showNavigation, final Boolean showQuestionTitle, final Boolean showResultsAfterFinish,
                final Boolean showResultsDependendOnDate, final Boolean showResultsOnHomepage, final Boolean showScoreInfo, final Boolean showQuestionProgress,
                final Boolean showScoreProgress, final Boolean showSectionsOnly, final String summaryPresentation, final Long startDate, final Long endDate) {
            this.allowCancel = allowCancel;
            this.allowNavigation = allowNavigation;
            this.allowSuspend = allowSuspend;
            this.numAttempts = numAttempts;
            this.sequencePresentation = sequencePresentation;
            this.showNavigation = showNavigation;
            this.showQuestionTitle = showQuestionTitle;
            this.showResultsAfterFinish = showResultsAfterFinish;
            this.showResultsDependendOnDate = showResultsDependendOnDate;
            this.showResultsOnHomepage = showResultsOnHomepage;
            this.showScoreInfo = showScoreInfo;
            this.showQuestionProgress = showQuestionProgress;
            this.showScoreProgress = showScoreProgress;
            this.showSectionsOnly = showSectionsOnly;
            this.summaryPresentation = summaryPresentation;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public boolean isApplicable(final ICourse course, final CourseNode courseNode) {
            return courseNode instanceof IQTESTCourseNode;
        }

        @Override
        public void configure(final ICourse course, final CourseNode newNode, final ModuleConfiguration moduleConfig) {
            if (allowCancel != null) {
                moduleConfig.set(IQEditController.CONFIG_KEY_ENABLECANCEL, allowCancel);
            }
            if (allowNavigation != null) {
                moduleConfig.set(IQEditController.CONFIG_KEY_ENABLEMENU, allowNavigation);
            }
            if (allowSuspend != null) {
                moduleConfig.set(IQEditController.CONFIG_KEY_ENABLESUSPEND, allowSuspend);
            }
            if (numAttempts != null) {
                moduleConfig.set(IQEditController.CONFIG_KEY_ATTEMPTS, numAttempts);
            }
            if (sequencePresentation != null) {
                moduleConfig.set(IQEditController.CONFIG_KEY_SEQUENCE, sequencePresentation);
            }
            if (showNavigation != null) {
                moduleConfig.set(IQEditController.CONFIG_KEY_DISPLAYMENU, showNavigation);
            }
            if (showQuestionTitle != null) {
                moduleConfig.set(IQEditController.CONFIG_KEY_QUESTIONTITLE, showQuestionTitle);
            }
            if (showResultsAfterFinish != null) {
                moduleConfig.set(IQEditController.CONFIG_KEY_RESULT_ON_FINISH, showResultsAfterFinish);
            }
            if (showResultsDependendOnDate != null) {
                moduleConfig.set(IQEditController.CONFIG_KEY_DATE_DEPENDENT_RESULTS, showResultsDependendOnDate);
            }
            if (startDate != null && endDate != null) {
                moduleConfig.set(IQEditController.CONFIG_KEY_RESULTS_START_DATE, new Date(startDate));
                moduleConfig.set(IQEditController.CONFIG_KEY_RESULTS_END_DATE, new Date(endDate));
            }
            if (showResultsOnHomepage != null) {
                moduleConfig.set(IQEditController.CONFIG_KEY_RESULT_ON_HOME_PAGE, showResultsOnHomepage);
            }
            if (showScoreInfo != null) {
                moduleConfig.set(IQEditController.CONFIG_KEY_ENABLESCOREINFO, showScoreInfo);
            }
            if (showQuestionProgress != null) {
                moduleConfig.set(IQEditController.CONFIG_KEY_QUESTIONPROGRESS, showQuestionProgress);
            }
            if (showScoreProgress != null) {
                moduleConfig.set(IQEditController.CONFIG_KEY_SCOREPROGRESS, showScoreProgress);
            }
            if (showSectionsOnly != null) {
                moduleConfig.set(IQEditController.CONFIG_KEY_RENDERMENUOPTION, showSectionsOnly);
            }
            if (summaryPresentation != null) {
                moduleConfig.set(IQEditController.CONFIG_KEY_SUMMARY, summaryPresentation);
            }
        }
    }

    public class SinglePageCustomConfig implements CustomConfigDelegate {
        private InputStream in;
        private final String filename;
        private String path;

        public SinglePageCustomConfig(final InputStream in, final String filename) {
            this.in = in;
            this.filename = filename;
        }

        public SinglePageCustomConfig(final String path, final String filename) {
            this.path = path;
            this.filename = filename;
        }

        @Override
        public boolean isValid() {
            return in != null || StringHelper.containsNonWhitespace(path);
        }

        @Override
        public void configure(final ICourse course, final CourseNode newNode, final ModuleConfiguration moduleConfig) {
            newNode.setDisplayOption(CourseNode.DISPLAY_OPTS_TITLE_DESCRIPTION_CONTENT);
            final VFSContainer rootContainer = course.getCourseFolderContainer();
            VFSLeaf singleFile = (VFSLeaf) rootContainer.resolve("/" + filename);
            if (singleFile == null) {
                singleFile = rootContainer.createChildLeaf("/" + filename);
            }

            if (in != null) {
                moduleConfig.set(SPEditController.CONFIG_KEY_FILE, "/" + filename);

                final OutputStream out = singleFile.getOutputStream(false);
                FileUtils.copy(in, out);
                FileUtils.closeSafely(out);
                FileUtils.closeSafely(in);
            } else {
                if (StringHelper.containsNonWhitespace(path)) {
                    if (!path.startsWith("/")) {
                        path = "/" + path;
                    }
                    if (!path.endsWith("/")) {
                        path += "/";
                    }
                } else {
                    path = "/";
                }
                moduleConfig.set(SPEditController.CONFIG_KEY_FILE, path + filename);
            }
            // saved node configuration
        }
    }

    public class TaskCustomConfig implements CustomConfigDelegate {
        private final Float points;
        private final String text;

        public TaskCustomConfig(final Float points, final String text) {
            this.points = points;
            this.text = text;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void configure(final ICourse course, final CourseNode newNode, final ModuleConfiguration moduleConfig) {
            newNode.updateModuleConfigDefaults(true);
            moduleConfig.set(TACourseNode.CONF_TASK_TEXT, text);
            if (points != null) {
                moduleConfig.set(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD, true);
                moduleConfig.set(MSCourseNode.CONFIG_KEY_SCORE_MIN, new Float(0));
                moduleConfig.set(MSCourseNode.CONFIG_KEY_SCORE_MAX, points);
            }
            final TACourseNode taskNode = (TACourseNode) newNode;
            taskNode.getConditionExpressions();
        }
    }

    public class AssessmentCustomConfig implements CustomConfigDelegate {

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void configure(final ICourse course, final CourseNode newNode, final ModuleConfiguration moduleConfig) {
            // Use default min and max scores and default cut value
            /*
             * //score granted (default is FALSE) Boolean scoreField = Boolean.FALSE; modConfig.set(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD, scoreField); //if score
             * granted == TRUE we can set these values if (scoreField) { modConfig.set(MSCourseNode.CONFIG_KEY_SCORE_MIN, 5.0f);
             * modConfig.set(MSCourseNode.CONFIG_KEY_SCORE_MAX, 10.0f); } //display passed / failed (note that TRUE means automatic and FALSE means manually)... //default
             * is TRUE Boolean displayPassed = Boolean.TRUE; modConfig.set(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD, displayPassed); //display set to false -> we can set
             * these values manually if (!displayPassed.booleanValue()) { //passed set to when score higher than cut value
             * modConfig.set(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE, 5.0f); }
             */

            // comment
            moduleConfig.set(MSCourseNode.CONFIG_KEY_HAS_COMMENT_FIELD, Boolean.TRUE);
            // info coach
            moduleConfig.set(MSCourseNode.CONFIG_KEY_INFOTEXT_COACH, "Info coach");
            // info user
            moduleConfig.set(MSCourseNode.CONFIG_KEY_INFOTEXT_USER, "Info user");
        }
    }

}
