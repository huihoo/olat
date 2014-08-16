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

package org.olat.connectors.rest.forum;

import static org.olat.connectors.rest.security.RestSecurityHelper.isAuthor;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
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

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.forum.Forum;
import org.olat.data.forum.Message;
import org.olat.lms.core.notification.service.PublishEventTO;
import org.olat.lms.forum.ForumService;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Web service to manage forum element. This implementation is only for import.
 * <P>
 * Initial Date: 26 aug. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Path("repo/forums/{forumKey}")
public class ForumImportWebService {

    private static final String VERSION = "1.0";

    /**
     * The version of the Forum Web Service
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
     * Creates a new thread in the forum of the course node
     * 
     * @response.representation.mediaType application/x-www-form-urlencoded
     * @response.representation.200.qname {http://www.example.com}messageVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The root message of the thread
     * @response.representation.200.example {@link org.olat.connectors.rest.forum.Examples#SAMPLE_MESSAGEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The author, forum or message not found
     * @param forumKey
     *            The id of the forum
     * @param title
     *            The title for the first post in the thread
     * @param body
     *            The body for the first post in the thread
     * @param authorKey
     *            The author key
     * @param request
     *            The HTTP request
     * @return The new thread
     */
    @POST
    @Path("threads")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response newThreadToForumPost(@PathParam("forumKey") final Long forumKey, @FormParam("title") final String title, @FormParam("body") final String body,
            @FormParam("authorKey") final Long authorKey, @Context final HttpServletRequest request) {
        return newThreadToForum(forumKey, title, body, authorKey, request);
    }

    /**
     * Creates a new thread in the forum of the course node
     * 
     * @response.representation.200.qname {http://www.example.com}messageVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The root message of the thread
     * @response.representation.200.example {@link org.olat.connectors.rest.forum.Examples#SAMPLE_MESSAGEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The author, forum or message not found
     * @param forumKey
     *            The id of the forum
     * @param title
     *            The title for the first post in the thread
     * @param body
     *            The body for the first post in the thread
     * @param authorKey
     *            The author user key
     * @param request
     *            The HTTP request
     * @return The new thread
     */
    @PUT
    @Path("threads")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response newThreadToForum(@PathParam("forumKey") final Long forumKey, @QueryParam("title") final String title, @QueryParam("body") final String body,
            @QueryParam("authorKey") final Long authorKey, @Context final HttpServletRequest request) {

        if (!isAuthor(request)) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }

        final BaseSecurity securityManager = getBaseSecurity();
        final Identity identity = securityManager.loadIdentityByKey(authorKey, false);
        if (identity == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }

        // load forum
        final ForumService fom = getForumService();
        final Forum forum = fom.loadForum(forumKey);
        if (forum == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }

        // creating the thread (a message without a parent message)
        final Message newThread = fom.createMessage();
        newThread.setTitle(title);
        newThread.setBody(body);
        // open a new thread
        fom.addTopMessage(forum, newThread, identity, createPublishEventTO());

        final MessageVO vo = new MessageVO(newThread);
        return Response.ok(vo).build();
    }

    private BaseSecurity getBaseSecurity() {
        return (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
    }

    private ForumService getForumService() {
        return (ForumService) CoreSpringFactory.getBean("forumService");

    }

    /**
     * Creates a new reply in the forum of the course node
     * 
     * @response.representation.mediaType application/x-www-form-urlencoded
     * @response.representation.200.qname {http://www.example.com}messageVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The root message of the thread
     * @response.representation.200.example {@link org.olat.connectors.rest.forum.Examples#SAMPLE_MESSAGEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The author or message not found
     * @param messageKey
     *            The id of the reply message
     * @param title
     *            The title for the first post in the thread
     * @param body
     *            The body for the first post in the thread
     * @param authorKey
     *            The author key
     * @param request
     *            The HTTP request
     * @return The new message
     */
    @POST
    @Path("posts/{messageKey}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response replyToPostPost(@PathParam("messageKey") final Long messageKey, @FormParam("title") final String title, @FormParam("body") final String body,
            @FormParam("authorKey") final Long authorKey, @Context final HttpServletRequest request) {
        return replyToPost(messageKey, title, body, authorKey, request);
    }

    /**
     * Creates a new reply in the forum of the course node
     * 
     * @response.representation.200.qname {http://www.example.com}messageVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The root message of the thread
     * @response.representation.200.example {@link org.olat.connectors.rest.forum.Examples#SAMPLE_MESSAGEVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The author or message not found
     * @param messageKey
     *            The id of the reply message
     * @param title
     *            The title for the first post in the thread
     * @param body
     *            The body for the first post in the thread
     * @param authorKey
     *            The author user key
     * @param request
     *            The HTTP request
     * @return The new Message
     */
    @PUT
    @Path("posts/{messageKey}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response replyToPost(@PathParam("messageKey") final Long messageKey, @QueryParam("title") final String title, @QueryParam("body") final String body,
            @QueryParam("authorKey") final Long authorKey, @Context final HttpServletRequest request) {

        if (!isAuthor(request)) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }

        final BaseSecurity securityManager = getBaseSecurity();
        final Identity identity = securityManager.loadIdentityByKey(authorKey, false);
        if (identity == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }

        // load forum
        final ForumService fom = getForumService();
        final Message mess = fom.loadMessage(messageKey);
        if (mess == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }

        // creating the thread (a message without a parent message)
        final Message newMessage = fom.createMessage();
        newMessage.setTitle(title);
        newMessage.setBody(body);
        fom.replyToMessage(identity, mess, newMessage, createPublishEventTO());
        final MessageVO vo = new MessageVO(newMessage);
        return Response.ok(vo).build();
    }

    private PublishEventTO createPublishEventTO() {
        return PublishEventTO.getNoPublishInstance();
    }
}
