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

import static org.olat.connectors.rest.security.RestSecurityHelper.isAuthorEditor;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.connectors.rest.security.RestSecurityHelper;
import org.olat.connectors.rest.support.vo.AssessableResultsVO;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.group.BusinessGroup;
import org.olat.data.qti.QTIResultSet;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.AssessableCourseNode;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.IQTESTCourseNode;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.lms.course.run.scoring.ScoreAccounting;
import org.olat.lms.course.run.scoring.ScoreEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.lms.ims.qti.IQManager;
import org.olat.lms.ims.qti.container.AssessmentContext;
import org.olat.lms.ims.qti.container.HttpItemInput;
import org.olat.lms.ims.qti.container.ItemContext;
import org.olat.lms.ims.qti.container.ItemInput;
import org.olat.lms.ims.qti.container.ItemsInput;
import org.olat.lms.ims.qti.container.SectionContext;
import org.olat.lms.ims.qti.navigator.Info;
import org.olat.lms.ims.qti.navigator.MenuItemNavigator;
import org.olat.lms.ims.qti.navigator.Navigator;
import org.olat.lms.ims.qti.process.AssessmentFactory;
import org.olat.lms.ims.qti.process.AssessmentInstance;
import org.olat.lms.security.IdentityEnvironment;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Retrieve and import course assessments
 * <P>
 * Initial Date: 7 apr. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
@Path("repo/courses/{courseId}/assessments")
public class CourseAssessmentWebService {

    private static final String VERSION = "1.0";

    private static final CacheControl cc = new CacheControl();
    static {
        cc.setMaxAge(-1);
    }

    /**
     * Retireves the version of the Course Assessment Web Service.
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
     * Returns the results of the course.
     * 
     * @response.representation.200.qname {http://www.example.com}assessableResultsVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc Array of results for the whole the course
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_ASSESSABLERESULTSVOes}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course not found
     * @param courseId
     *            The course resourceable's id
     * @param request
     *            The HTTP request
     * @return
     */
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getCourseResults(@PathParam("courseId") final Long courseId, @Context final HttpServletRequest request) {
        if (!RestSecurityHelper.isAuthor(request)) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }

        final ICourse course = CourseFactory.loadCourse(courseId);
        if (course == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }

        final List<Identity> courseUsers = loadUsers(course, course.getCourseEnvironment().getCourseGroupManager());
        int i = 0;
        final AssessableResultsVO[] results = new AssessableResultsVO[courseUsers.size()];
        for (final Identity courseUser : courseUsers) {
            results[i++] = getRootResult(courseUser, course);
        }
        return Response.ok(results).build();
    }

    /**
     * Returns the results of the course.
     * 
     * @response.representation.200.qname {http://www.example.com}assessableResultsVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The result of the course
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_ASSESSABLERESULTSVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The identity or the course not found
     * @param courseId
     *            The course resourceable's id
     * @param identityKey
     *            The id of the user
     * @param httpRequest
     *            The HTTP request
     * @param request
     *            The REST request
     * @return
     */
    @GET
    @Path("users/{identityKey}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getCourseResultsOf(@PathParam("courseId") final Long courseId, @PathParam("identityKey") final Long identityKey,
            @Context final HttpServletRequest httpRequest, @Context final Request request) {
        if (!RestSecurityHelper.isAuthor(httpRequest)) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }

        try {
            final Identity userIdentity = getBaseSecurity().loadIdentityByKey(identityKey, false);
            if (userIdentity == null) {
                return Response.serverError().status(Status.NOT_FOUND).build();
            }

            final Date lastModified = userIdentity.getLastLogin();
            Response.ResponseBuilder response = request.evaluatePreconditions(lastModified);
            if (response == null) {
                final ICourse course = CourseFactory.loadCourse(courseId);
                if (course == null) {
                    return Response.serverError().status(Status.NOT_FOUND).build();
                }

                final AssessableResultsVO results = getRootResult(userIdentity, course);
                response = Response.ok(results).lastModified(lastModified).cacheControl(cc);
            }
            return response.build();
        } catch (final Throwable e) {
            throw new WebApplicationException(e);
        }
    }

    private BaseSecurity getBaseSecurity() {
        return (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
    }

    /**
     * Exports results for an assessable course node for all students.
     * 
     * @response.representation.200.qname {http://www.example.com}assessableResultsVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc Export all results of all user of the course
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_ASSESSABLERESULTSVOes}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course not found
     * @param courseId
     *            The course resourceable's id
     * @param nodeId
     *            The id of the course building block
     * @param request
     *            The HTTP request
     * @return
     */
    @GET
    @Path("{nodeId}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getAssessableResults(@PathParam("courseId") final Long courseId, @PathParam("nodeId") final Long nodeId, @Context final HttpServletRequest request) {
        if (!RestSecurityHelper.isAuthor(request)) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }

        final ICourse course = CourseFactory.loadCourse(courseId);
        if (course == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        } else if (!isAuthorEditor(course, request)) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }

        final List<Identity> courseUsers = loadUsers(course, course.getCourseEnvironment().getCourseGroupManager());
        int i = 0;
        final AssessableResultsVO[] results = new AssessableResultsVO[courseUsers.size()];
        for (final Identity courseUser : courseUsers) {
            results[i++] = getNodeResult(courseUser, course, nodeId);
        }
        return Response.ok(results).build();
    }

    /**
     * Imports results for an assessable course node for the authenticated student.
     * 
     * @response.representation.qname {http://www.example.com}assessableResultsVO
     * @response.representation.mediaType application/xml, application/json
     * @response.representation.doc A result to import
     * @response.representation.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_ASSESSABLERESULTSVO}
     * @response.representation.200.doc Import successful
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The identity not found
     * @param courseId
     *            The resourceable id of the course
     * @param nodeId
     *            The id of the course building block
     * @param resultsVO
     *            The results
     * @param request
     *            The HTTP request
     * @return
     */
    @POST
    @Path("{nodeId}")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response postAssessableResults(@PathParam("courseId") final Long courseId, @PathParam("nodeId") final String nodeId, final AssessableResultsVO resultsVO,
            @Context final HttpServletRequest request) {
        if (!RestSecurityHelper.isAuthor(request)) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }

        final Identity identity = RestSecurityHelper.getUserRequest(request).getIdentity();
        if (identity == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }
        attachAssessableResults(courseId, nodeId, identity, resultsVO);
        return Response.ok().build();
    }

    private void attachAssessableResults(final Long courseResourceableId, final String nodeKey, final Identity requestIdentity, final AssessableResultsVO resultsVO) {
        try {
            final ICourse course = CourseFactory.openCourseEditSession(courseResourceableId);
            final CourseNode node = getParentNode(course, nodeKey);
            if (!(node instanceof AssessableCourseNode)) {
                throw new IllegalArgumentException("The supplied node key does not refer to an AssessableCourseNode");
            }
            final BaseSecurity securityManager = getBaseSecurity();
            final Identity userIdentity = securityManager.loadIdentityByKey(resultsVO.getIdentityKey());

            // create an identenv with no roles, no attributes, no locale
            final IdentityEnvironment ienv = new IdentityEnvironment();
            ienv.setIdentity(userIdentity);
            final UserCourseEnvironment userCourseEnvironment = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());

            // Fetch all score and passed and calculate score accounting for the
            // entire course
            userCourseEnvironment.getScoreAccounting().evaluateAll();

            if (node instanceof IQTESTCourseNode) {
                importTestItems(courseResourceableId, nodeKey, requestIdentity, resultsVO);
            } else {
                final AssessableCourseNode assessableNode = (AssessableCourseNode) node;
                final ScoreEvaluation scoreEval = new ScoreEvaluation(resultsVO.getScore(), Boolean.TRUE, new Long(nodeKey));// not directly pass this key
                assessableNode.updateUserScoreEvaluation(scoreEval, userCourseEnvironment, requestIdentity, true);
            }

            CourseFactory.saveCourseEditorTreeModel(course.getResourceableId());
            CourseFactory.closeCourseEditSession(course.getResourceableId(), true);
        } catch (final Throwable e) {
            throw new WebApplicationException(e);
        }
    }

    private void importTestItems(final Long courseResourceableId, final String nodeKey, final Identity identity, final AssessableResultsVO resultsVO) {
        try {

            final IQManager iqManager = IQManager.getInstance();

            // load the course and the course node
            final ICourse course = CourseFactory.loadCourse(courseResourceableId);
            final CourseNode courseNode = getParentNode(course, nodeKey);
            final ModuleConfiguration modConfig = courseNode.getModuleConfiguration();

            // check if the result set is already saved
            final QTIResultSet set = iqManager.getLastResultSet(identity, course.getResourceableId(), courseNode.getIdent());
            if (set == null) {
                final String resourcePathInfo = course.getResourceableId() + File.separator + courseNode.getIdent();

                // The use of these classes AssessmentInstance, AssessmentContext and
                // Navigator
                // allow the use of the persistence mechanism of OLAT without
                // duplicating the code.
                // The consequence is that we must loop on section and items and set the
                // navigator on
                // the right position before submitting the inputs.
                final AssessmentInstance ai = AssessmentFactory.createAssessmentInstance(identity, modConfig, false, resourcePathInfo);
                final Navigator navigator = ai.getNavigator();
                navigator.startAssessment();
                // The type of the navigator depends on the setting of the course node
                final boolean perItem = (navigator instanceof MenuItemNavigator);

                final Map<String, ItemInput> datas = convertToHttpItemInput(resultsVO.getResults());

                final AssessmentContext ac = ai.getAssessmentContext();
                final int sectioncnt = ac.getSectionContextCount();
                // loop on the sections
                for (int i = 0; i < sectioncnt; i++) {
                    final SectionContext sc = ac.getSectionContext(i);
                    navigator.goToSection(i);

                    ItemsInput iips = new ItemsInput();
                    final int itemcnt = sc.getItemContextCount();
                    // loop on the items
                    for (int j = 0; j < itemcnt; j++) {

                        final ItemContext it = sc.getItemContext(j);
                        if (datas.containsKey(it.getIdent())) {

                            if (perItem) {
                                // save the datas on a per item base
                                navigator.goToItem(i, j);

                                // the navigator can give informations on its current status
                                final Info info = navigator.getInfo();
                                if (info.containsError()) {
                                    // some items cannot processed twice
                                    System.out.println("Error");
                                } else {
                                    iips.addItemInput(datas.get(it.getIdent()));
                                    navigator.submitItems(iips);
                                    iips = new ItemsInput();
                                }
                            } else {
                                // put for a section
                                iips.addItemInput(datas.get(it.getIdent()));
                            }
                        }
                    }

                    if (!perItem) {
                        // save the inputs of the section. In a section based navigation,
                        // we must saved the inputs of the whole section at once
                        navigator.submitItems(iips);
                    }
                }

                navigator.submitAssessment();

                // persist the QTIResultSet (o_qtiresultset and o_qtiresult) on the
                // database
                // TODO iqManager.persistResults(ai, course.getResourceableId(),
                // courseNode.getIdent(), identity, "127.0.0.1");

                // write the reporting file on the file system
                // The path is <olatdata> / resreporting / <username> / Assessment /
                // <assessId>.xml
                // TODO Document docResReporting = iqManager.getResultsReporting(ai,
                // identity, Locale.getDefault());
                // TODO FilePersister.createResultsReporting(docResReporting, identity,
                // ai.getFormattedType(), ai.getAssessID());

                // prepare all instances needed to save the score at the course node
                // level
                final CourseEnvironment cenv = course.getCourseEnvironment();
                final IdentityEnvironment identEnv = new IdentityEnvironment();
                identEnv.setIdentity(identity);
                final UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(identEnv, cenv);

                // update scoring overview for the user in the current course
                final Float score = ac.getScore();
                final Boolean passed = ac.isPassed();
                final ScoreEvaluation sceval = new ScoreEvaluation(score, passed, new Long(nodeKey));// perhaps don't pass this key directly
                final AssessableCourseNode acn = (AssessableCourseNode) courseNode;
                // assessment nodes are assessable
                final boolean incrementUserAttempts = true;
                acn.updateUserScoreEvaluation(sceval, userCourseEnv, identity, incrementUserAttempts);
            } else {
                System.out.println("Result set already saved");
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, ItemInput> convertToHttpItemInput(final Map<Long, String> results) {
        final Map<String, ItemInput> datas = new HashMap<String, ItemInput>();
        for (final Long key : results.keySet()) {
            final HttpItemInput iip = new HttpItemInput(results.get(key));
            iip.putSingle(key.toString(), results.get(key));
            // TODO somehow obtain answer from value
            datas.put(iip.getIdent(), iip);
        }
        return datas;
    }

    private CourseNode getParentNode(final ICourse course, final String parentNodeId) {
        if (parentNodeId == null) {
            return course.getRunStructure().getRootNode();
        } else {
            return course.getEditorTreeModel().getCourseNode(parentNodeId);
        }
    }

    /**
     * Returns the results of a student at a specific assessable node
     * 
     * @response.representation.200.qname {http://www.example.com}assessableResultsVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The result of a user at a specific node
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_ASSESSABLERESULTSVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The identity or the course not found
     * @param courseId
     *            The course resourceable's id
     * @param nodeId
     *            The ident of the course building block
     * @param identityKey
     *            The id of the user
     * @param httpRequest
     *            The HTTP request
     * @param request
     *            The REST request
     * @return
     */
    @GET
    @Path("{nodeId}/users/{identityKey}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getCourseNodeResultsForNode(@PathParam("courseId") final Long courseId, @PathParam("nodeId") final Long nodeId,
            @PathParam("identityKey") final Long identityKey, @Context final HttpServletRequest httpRequest, @Context final Request request) {
        if (!RestSecurityHelper.isAuthor(httpRequest)) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }

        try {
            final Identity userIdentity = getBaseSecurity().loadIdentityByKey(identityKey, false);
            if (userIdentity == null) {
                return Response.serverError().status(Status.NOT_FOUND).build();
            }

            final Date lastModified = userIdentity.getLastLogin();
            Response.ResponseBuilder response = request.evaluatePreconditions(lastModified);
            if (response == null) {
                final ICourse course = CourseFactory.loadCourse(courseId);
                if (course == null) {
                    return Response.serverError().status(Status.NOT_FOUND).build();
                }

                final AssessableResultsVO results = getNodeResult(userIdentity, course, nodeId);
                response = Response.ok(results).lastModified(lastModified).cacheControl(cc);
            }
            return response.build();
        } catch (final Throwable e) {
            throw new WebApplicationException(e);
        }
    }

    private AssessableResultsVO getRootResult(final Identity identity, final ICourse course) {
        final CourseNode rootNode = course.getRunStructure().getRootNode();
        return getRootResult(identity, course, rootNode);
    }

    private AssessableResultsVO getNodeResult(final Identity identity, final ICourse course, final Long nodeId) {
        final CourseNode courseNode = course.getRunStructure().getNode(nodeId.toString());
        return getRootResult(identity, course, courseNode);
    }

    private AssessableResultsVO getRootResult(final Identity identity, final ICourse course, final CourseNode courseNode) {
        final AssessableResultsVO results = new AssessableResultsVO();
        results.setIdentityKey(identity.getKey());

        // create an identenv with no roles, no attributes, no locale
        final IdentityEnvironment ienv = new IdentityEnvironment();
        ienv.setIdentity(identity);
        final UserCourseEnvironment userCourseEnvironment = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());

        // Fetch all score and passed and calculate score accounting for the entire course
        final ScoreAccounting scoreAccounting = userCourseEnvironment.getScoreAccounting();
        scoreAccounting.evaluateAll();

        if (courseNode instanceof AssessableCourseNode) {
            final AssessableCourseNode assessableRootNode = (AssessableCourseNode) courseNode;
            final ScoreEvaluation scoreEval = scoreAccounting.evalCourseNode(assessableRootNode);
            results.setScore(scoreEval.getScore());
            results.setPassed(scoreEval.getPassed());
        }

        return results;
    }

    private List<Identity> loadUsers(OLATResourceable course, final CourseGroupManager gm) {
        final List<Identity> identites = new ArrayList<Identity>();
        final BaseSecurity securityManager = getBaseSecurity();
        final List<BusinessGroup> groups = gm.getAllLearningGroupsFromAllContexts(course);

        final Set<Long> check = new HashSet<Long>();
        for (final BusinessGroup group : groups) {
            final SecurityGroup participants = group.getPartipiciantGroup();
            final List<Identity> ids = securityManager.getIdentitiesOfSecurityGroup(participants);
            for (final Identity id : ids) {
                if (!check.contains(id.getKey())) {
                    identites.add(id);
                    check.add(id.getKey());
                }
            }
        }
        return identites;
    }
}
