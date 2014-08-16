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

import static org.olat.presentation.course.nodes.co.COEditController.CONFIG_KEY_EMAILTOADRESSES;
import static org.olat.presentation.course.nodes.co.COEditController.CONFIG_KEY_EMAILTOAREAS;
import static org.olat.presentation.course.nodes.co.COEditController.CONFIG_KEY_EMAILTOCOACHES;
import static org.olat.presentation.course.nodes.co.COEditController.CONFIG_KEY_EMAILTOGROUPS;
import static org.olat.presentation.course.nodes.co.COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS;
import static org.olat.presentation.course.nodes.co.COEditController.CONFIG_KEY_MBODY_DEFAULT;
import static org.olat.presentation.course.nodes.co.COEditController.CONFIG_KEY_MSUBJECT_DEFAULT;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.olat.data.group.BusinessGroup;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.group.BusinessGroupService;
import org.olat.system.commons.StringHelper;
import org.olat.system.mail.MailHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * This handles the contact building block.
 * <P>
 * Initial Date: 10 mai 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
@Path("repo/courses/{courseId}/elements/contact")
public class COWebService extends AbstractCourseNodeWebService {

    /**
     * This attaches a contact element onto a given course, the element will be inserted underneath the supplied parentNodeId
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
     * @param coaches
     *            Send to coaches (true/false)
     * @param participants
     *            Send to participants (true/false)
     * @param groups
     *            A list of learning groups (list of keys)
     * @param areas
     *            A list of learning areas (list of keys)
     * @param to
     *            The list of e-mail address
     * @param defaultSubject
     *            The default subject
     * @param defaultBody
     *            The default body text
     * @param request
     *            The HTTP request
     * @return The persisted contact element (fully populated)
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response attachContact(@PathParam("courseId") final Long courseId, @QueryParam("parentNodeId") final String parentNodeId,
            @QueryParam("position") final Integer position, @QueryParam("shortTitle") @DefaultValue("undefined") final String shortTitle,
            @QueryParam("longTitle") @DefaultValue("undefined") final String longTitle, @QueryParam("objectives") @DefaultValue("undefined") final String objectives,
            @QueryParam("visibilityExpertRules") final String visibilityExpertRules, @QueryParam("accessExpertRules") final String accessExpertRules,
            @QueryParam("coaches") @DefaultValue("false") final boolean coaches, @QueryParam("participants") @DefaultValue("false") final boolean participants,
            @QueryParam("groups") final String groups, @QueryParam("areas") final String areas, @QueryParam("to") final String to,
            @QueryParam("defaultSubject") final String defaultSubject, @QueryParam("defaultBody") final String defaultBody, @Context final HttpServletRequest request) {

        final ContactConfigDelegate config = new ContactConfigDelegate(coaches, participants, groups, areas, to, defaultSubject, defaultBody);
        return attach(courseId, parentNodeId, "co", position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
    }

    /**
     * This attaches a contact element onto a given course, the element will be inserted underneath the supplied parentNodeId
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
     * @param coaches
     *            send the message to coaches
     * @param participants
     *            send the message to participants
     * @param groups
     *            send the message to the specified groups
     * @param areas
     *            send the message to the specified learning areas
     * @param to
     *            send the message to the e-mail address
     * @param defaultSubject
     *            default subject of the message
     * @param defaultBody
     *            default body text of the message
     * @param request
     *            The HTTP request
     * @return The persisted contact element (fully populated)
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response attachContactPost(@PathParam("courseId") final Long courseId, @FormParam("parentNodeId") final String parentNodeId,
            @FormParam("position") final Integer position, @FormParam("shortTitle") @DefaultValue("undefined") final String shortTitle,
            @FormParam("longTitle") @DefaultValue("undefined") final String longTitle, @FormParam("objectives") @DefaultValue("undefined") final String objectives,
            @FormParam("visibilityExpertRules") final String visibilityExpertRules, @FormParam("accessExpertRules") final String accessExpertRules,
            @FormParam("coaches") @DefaultValue("false") final boolean coaches, @FormParam("participants") @DefaultValue("false") final boolean participants,
            @FormParam("groups") final String groups, @FormParam("areas") final String areas, @FormParam("to") final String to,
            @FormParam("defaultSubject") final String defaultSubject, @FormParam("defaultBody") final String defaultBody, @Context final HttpServletRequest request) {
        final ContactConfigDelegate config = new ContactConfigDelegate(coaches, participants, groups, areas, to, defaultSubject, defaultBody);
        return attach(courseId, parentNodeId, "co", position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
    }

    private class ContactConfigDelegate implements CustomConfigDelegate {
        private final Boolean coaches;
        private final Boolean participants;
        private final List<String> groups;
        private final List<String> areas;
        private final List<String> tos;
        private final String defaultSubject;
        private final String defaultBody;

        public ContactConfigDelegate(final Boolean coaches, final Boolean participants, final String groups, final String areas, final String to,
                final String defaultSubject, final String defaultBody) {
            this.coaches = coaches;
            this.participants = participants;
            this.groups = getGroupNames(groups);
            this.areas = getGroupNames(areas);
            this.tos = getEmails(to);
            this.defaultSubject = defaultSubject;
            this.defaultBody = defaultBody;
        }

        @Override
        public boolean isValid() {
            boolean ok = false;
            ok = ok || coaches;
            ok = ok || participants;
            ok = ok || (areas != null && !areas.isEmpty());
            ok = ok || (groups != null && !groups.isEmpty());
            ok = ok || (tos != null && !tos.isEmpty());

            /*
             * check validity of manually provided e-mails
             */
            if (tos != null && !tos.isEmpty()) {
                for (final String eAd : tos) {
                    ok = ok && MailHelper.isValidEmailAddress(eAd);
                }
            }
            return true;
        }

        @Override
        public void configure(final ICourse course, final CourseNode newNode, final ModuleConfiguration moduleConfig) {
            moduleConfig.set(CONFIG_KEY_EMAILTOGROUPS, groups);
            moduleConfig.set(CONFIG_KEY_EMAILTOAREAS, areas);
            moduleConfig.setBooleanEntry(CONFIG_KEY_EMAILTOCOACHES, coaches == null ? false : coaches.booleanValue());
            moduleConfig.setBooleanEntry(CONFIG_KEY_EMAILTOPARTICIPANTS, participants == null ? false : participants.booleanValue());
            moduleConfig.set(CONFIG_KEY_EMAILTOADRESSES, tos);
            moduleConfig.set(CONFIG_KEY_MSUBJECT_DEFAULT, defaultSubject);
            moduleConfig.set(CONFIG_KEY_MBODY_DEFAULT, defaultBody);
        }

        private List<String> getEmails(final String to) {
            final List<String> eList = new ArrayList<String>();
            if (StringHelper.containsNonWhitespace(to)) {
                final String[] emailAdress = to.split(";");
                if ((emailAdress != null) && (emailAdress.length > 0) && (!"".equals(emailAdress[0]))) {
                    for (String eAd : emailAdress) {
                        eAd = eAd.trim();
                        if (MailHelper.isValidEmailAddress(eAd)) {
                            eList.add(eAd);
                        }
                    }
                }
            }
            return eList;
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
