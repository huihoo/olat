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

package org.olat.presentation.course.nodes.basiclti;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.imsglobal.basiclti.BasicLTIUtil;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.user.User;
import org.olat.data.user.UserConstants;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.commons.mediaresource.StringMediaResource;
import org.olat.lms.course.nodes.BasicLTICourseNode;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.dispatcher.mapper.Mapper;
import org.olat.system.commons.Settings;
import org.olat.system.commons.WebappHelper;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * is the controller for displaying contents in an iframe served by Basic LTI
 * 
 * @author guido
 * @author Charles Severance
 */
public class LTIRunController extends BasicController {

    private static final Logger log = LoggerHelper.getLogger();
    private VelocityContainer run;
    private final BasicLTICourseNode courseNode;
    private final Panel main;
    private final ModuleConfiguration config;
    private final CourseEnvironment courseEnv;
    private String postData;
    private Mapper contentMapper;
    private Mapper talkbackMapper;

    /**
     * Constructor for tunneling run controller
     * 
     * @param wControl
     * @param config
     *            The module configuration
     * @param ureq
     *            The user request
     * @param ltCourseNode
     *            The current course node
     * @param cenv
     *            the course environment
     */
    public LTIRunController(final WindowControl wControl, final ModuleConfiguration config, final UserRequest ureq, final BasicLTICourseNode ltCourseNode,
            final CourseEnvironment cenv) {
        super(ureq, wControl);
        this.courseNode = ltCourseNode;
        this.config = config;
        this.courseEnv = cenv;

        main = new Panel("ltrunmain");
        doBasicLTI(ureq);
        this.putInitialPanel(main);
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // nothing to do
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        // nothing to do
    }

    private void doBasicLTI(final UserRequest ureq) {
        run = createVelocityContainer("run");

        // push title and learning objectives, only visible on intro page
        run.contextPut("menuTitle", courseNode.getShortTitle());
        run.contextPut("displayTitle", courseNode.getLongTitle());

        // put url in template to show content on extern page
        URL url = null;
        try {
            url = new URL((String) config.get(LTIConfigForm.CONFIGKEY_PROTO), (String) config.get(LTIConfigForm.CONFIGKEY_HOST),
                    ((Integer) config.get(LTIConfigForm.CONFIGKEY_PORT)).intValue(), (String) config.get(LTIConfigForm.CONFIGKEY_URI));
        } catch (final MalformedURLException e) {
            // this should not happen since the url was already validated in edit mode
            run.contextPut("url", "");
        }
        if (url != null) {
            final StringBuilder sb = new StringBuilder(128);
            sb.append(url.toString());
            // since the url only includes the path, but not the query (?...), append
            // it here, if any
            final String query = (String) config.get(LTIConfigForm.CONFIGKEY_QUERY);
            if (query != null) {
                sb.append("?");
                sb.append(query);
            }
            run.contextPut("url", sb.toString());

            final String key = (String) config.get(LTIConfigForm.CONFIGKEY_KEY);
            final String pass = (String) config.get(LTIConfigForm.CONFIGKEY_PASS);
            final String debug = (String) config.get(LTIConfigForm.CONFIG_KEY_DEBUG);

            talkbackMapper = new Mapper() {

                @Override
                public MediaResource handle(final String relPath, final HttpServletRequest request) {
                    /**
                     * this is the place for error handling coming from the LTI tool, depending on error state may present some information for the user or just add some
                     * information to the olat.log file
                     */
                    final StringMediaResource mediares = new StringMediaResource();
                    final StringBuilder sb = new StringBuilder();
                    sb.append("lti_msg: ").append(request.getParameter("lti_msg")).append("<br/>");
                    sb.append("lti_errormsg: ").append(request.getParameter("lti_errormsg")).append("<br/>");
                    sb.append("lti_log: ").append(request.getParameter("lti_log")).append("<br/>");
                    sb.append("lti_errorlog: ").append(request.getParameter("lti_errorlog")).append("<br/>");
                    mediares.setData("<html><body>" + sb.toString() + "</body></html>");
                    mediares.setContentType("text/html");
                    mediares.setEncoding("UTF-8");
                    return mediares;
                }
            };
            final String backMapperUrl = registerMapper(talkbackMapper);

            final String serverUri = ureq.getHttpReq().getScheme() + "://" + ureq.getHttpReq().getServerName() + ":" + ureq.getHttpReq().getServerPort();

            Properties props = LTIProperties(ureq);
            setProperty(props, "launch_presentation_return_url", serverUri + backMapperUrl + "/");
            props = BasicLTIUtil.signProperties(props, sb.toString(), "POST", key, pass, null, null, null);

            postData = BasicLTIUtil.postLaunchHTML(props, sb.toString(), "true".equals(debug));

            contentMapper = new Mapper() {
                @Override
                public MediaResource handle(final String relPath, final HttpServletRequest request) {
                    final StringMediaResource mediares = new StringMediaResource();
                    mediares.setData(postData);
                    mediares.setContentType("text/html");
                    mediares.setEncoding("UTF-8");
                    return mediares;
                }

            };
            log.debug("Basic LTI Post data: " + postData, null);

        }
        final String mapperUri = registerMapper(contentMapper);
        run.contextPut("mapperUri", mapperUri + "/");

        main.setContent(run);
    }

    private Properties LTIProperties(final UserRequest ureq) {
        final Identity ident = ureq.getIdentity();
        final Locale loc = ureq.getLocale();
        final User u = ident.getUser();
        final String lastName = getUserService().getUserProperty(u, UserConstants.LASTNAME, loc);
        final String firstName = getUserService().getUserProperty(u, UserConstants.FIRSTNAME, loc);
        final String email = getUserService().getUserProperty(u, UserConstants.EMAIL, loc);

        final String custom = (String) config.get(LTIConfigForm.CONFIG_KEY_CUSTOM);
        final boolean sendname = Boolean.valueOf((String) config.get(LTIConfigForm.CONFIG_KEY_SENDNAME));
        final boolean sendemail = Boolean.valueOf((String) config.get(LTIConfigForm.CONFIG_KEY_SENDEMAIL));

        final Properties props = new Properties();
        setProperty(props, "resource_link_id", courseNode.getIdent());
        setProperty(props, "resource_link_title", courseNode.getShortTitle());
        setProperty(props, "resource_link_description", courseNode.getLongTitle());
        setProperty(props, "user_id", u.getKey() + "");
        setProperty(props, "launch_presentation_locale", loc.toString());
        setProperty(props, "launch_presentation_document_target", "iframe");

        if (sendname) {
            setProperty(props, "lis_person_name_given", firstName);
            setProperty(props, "lis_person_name_family", lastName);
            setProperty(props, "lis_person_name_full", firstName + " " + lastName);
        }
        if (sendemail) {
            setProperty(props, "lis_person_contact_email_primary", email);
        }

        setProperty(props, "roles", setRoles(ureq.getUserSession().getRoles()));
        setProperty(props, "context_id", courseEnv.getCourseResourceableId().toString());
        setProperty(props, "context_label", courseEnv.getCourseTitle());
        setProperty(props, "context_title", courseEnv.getCourseTitle());
        setProperty(props, "context_type", "CourseSection");

        // Pull in and parse the custom parameters
        // Note to Chuck - move this into BasicLTI Util
        if (custom != null) {
            final String[] params = custom.split("[\n;]");
            for (int i = 0; i < params.length; i++) {
                final String param = params[i];
                if (param == null) {
                    continue;
                }
                if (param.length() < 1) {
                    continue;
                }
                final int pos = param.indexOf("=");
                if (pos < 1) {
                    continue;
                }
                if (pos + 1 > param.length()) {
                    continue;
                }
                final String key = BasicLTIUtil.mapKeyName(param.substring(0, pos));
                if (key == null) {
                    continue;
                }
                String value = param.substring(pos + 1);
                value = value.trim();
                if (value.length() < 1) {
                    continue;
                }
                if (value == null) {
                    continue;
                }
                setProperty(props, "custom_" + key, value);
            }
        }

        setProperty(props, "tool_consumer_instance_guid", Settings.getServerconfig("server_fqdn"));
        setProperty(props, "tool_consumer_instance_name", WebappHelper.getInstanceId());
        setProperty(props, "tool_consumer_instance_contact_email", WebappHelper.getMailConfig("mailSupport"));

        return props;
    }

    private void setProperty(final Properties props, final String key, final String value) {
        if (value == null) {
            return;
        }
        if (value.trim().length() < 1) {
            return;
        }
        props.setProperty(key, value);
    }

    /**
     * A comma-separated list of URN values for roles. If this list is non-empty, it should contain at least one role from the LIS System Role, LIS Institution Role, or
     * LIS Context Role vocabularies (See Appendix A of LTI_BasicLTI_Implementation_Guide_rev1.pdf).
     * 
     * @param roles
     * @return
     */
    private String setRoles(final Roles roles) {
        StringBuilder rolesStr = new StringBuilder("Learner");
        if (roles.isAuthor()) {
            rolesStr.append(",").append("Instructor");
        }
        if (roles.isOLATAdmin()) {
            rolesStr.append(",").append("Administrator");
        }
        if (roles.isGuestOnly()) {
            rolesStr = new StringBuilder();
            rolesStr.append("Guest");
        }
        return rolesStr.toString();
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
