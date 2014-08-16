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

package org.olat.lms.course.nodes.projectbroker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.course.nodes.projectbroker.Project;
import org.olat.data.group.BusinessGroup;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.user.UserConstants;
import org.olat.lms.commons.mail.MailTemplateHelper;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.ProjectBrokerCourseNode;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.group.BusinessGroupServiceImpl;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.Formatter;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.mail.MailTemplate;
import org.olat.system.mail.MailerResult;
import org.olat.system.mail.MailerWithTemplate;
import org.olat.system.security.OLATPrincipal;
import org.olat.system.spring.CoreSpringFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author guretzki
 */

@Component
public class ProjectBrokerMailerImpl implements ProjectBrokerMailer {
    private static final String KEY_ENROLLED_EMAIL_TO_PARTICIPANT_SUBJECT = "mail.enrolled.to.participant.subject";
    private static final String KEY_ENROLLED_EMAIL_TO_PARTICIPANT_BODY = "mail.enrolled.to.participant.body";

    private static final String KEY_ENROLLED_EMAIL_TO_MANAGER_SUBJECT = "mail.enrolled.to.manager.subject";
    private static final String KEY_ENROLLED_EMAIL_TO_MANAGER_BODY = "mail.enrolled.to.manager.body";

    private static final String KEY_CANCEL_ENROLLMENT_EMAIL_TO_PARTICIPANT_SUBJECT = "mail.cancel.enrollment.to.participant.subject";
    private static final String KEY_CANCEL_ENROLLMENT_EMAIL_TO_PARTICIPANT_BODY = "mail.cancel.enrollment.to.participant.body";

    private static final String KEY_CANCEL_ENROLLMENT_EMAIL_TO_MANAGER_SUBJECT = "mail.cancel.enrollment.to.manager.subject";
    private static final String KEY_CANCEL_ENROLLMENT_EMAIL_TO_MANAGER_BODY = "mail.cancel.enrollment.to.manager.body";

    private static final String KEY_PROJECT_CHANGED_EMAIL_TO_PARTICIPANT_SUBJECT = "mail.project.changed.to.participant.subject";
    private static final String KEY_PROJECT_CHANGED_EMAIL_TO_PARTICIPANT_BODY = "mail.project.changed.to.participant.body";

    private static final String KEY_PROJECT_DELETED_EMAIL_TO_PARTICIPANT_SUBJECT = "mail.project.deleted.to.participant.subject";
    private static final String KEY_PROJECT_DELETED_EMAIL_TO_PARTICIPANT_BODY = "mail.project.deleted.to.participant.body";

    private static final String KEY_REMOVE_CANDIDATE_EMAIL_SUBJECT = "mail.remove.candidate.subject";
    private static final String KEY_REMOVE_CANDIDATE_EMAIL_BODY = "mail.remove.candidate.body";
    private static final String KEY_ACCEPT_CANDIDATE_EMAIL_SUBJECT = "mail.accept.candidate.subject";
    private static final String KEY_ACCEPT_CANDIDATE_EMAIL_BODY = "mail.accept.candidate.body";
    private static final String KEY_ADD_CANDIDATE_EMAIL_SUBJECT = "mail.add.candidate.subject";
    private static final String KEY_ADD_CANDIDATE_EMAIL_BODY = "mail.add.candidate.body";
    private static final String KEY_ADD_PARTICIPANT_EMAIL_SUBJECT = "mail.add.participant.subject";
    private static final String KEY_ADD_PARTICIPANT_EMAIL_BODY = "mail.add.participant.body";
    private static final String KEY_REMOVE_PARTICIPANT_EMAIL_SUBJECT = "mail.remove.participant.subject";
    private static final String KEY_REMOVE_PARTICIPANT_EMAIL_BODY = "mail.remove.participant.body";

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    private UserService userService;
    @Autowired
    private BaseSecurity baseSecurity;

    /**
	 * 
	 */
    private ProjectBrokerMailerImpl() {
        //
    }

    // For Enrollment
    @Override
    public MailerResult sendEnrolledEmailToParticipant(final Identity enrolledIdentity, final Project project, final Translator pT) {
        return sendEmail(enrolledIdentity, project, pT.translate(KEY_ENROLLED_EMAIL_TO_PARTICIPANT_SUBJECT), pT.translate(KEY_ENROLLED_EMAIL_TO_PARTICIPANT_BODY),
                pT.getLocale());
    }

    @Override
    public MailerResult sendEnrolledEmailToManager(final Identity enrolledIdentity, final Project project, final Translator pT) {
        return sendEmailToGroup(project.getProjectLeaderGroup(), enrolledIdentity, project, pT.translate(KEY_ENROLLED_EMAIL_TO_MANAGER_SUBJECT),
                pT.translate(KEY_ENROLLED_EMAIL_TO_MANAGER_BODY), pT.getLocale());
    }

    // For cancel enrollment
    @Override
    public MailerResult sendCancelEnrollmentEmailToParticipant(final Identity enrolledIdentity, final Project project, final Translator pT) {
        return sendEmail(enrolledIdentity, project, pT.translate(KEY_CANCEL_ENROLLMENT_EMAIL_TO_PARTICIPANT_SUBJECT),
                pT.translate(KEY_CANCEL_ENROLLMENT_EMAIL_TO_PARTICIPANT_BODY), pT.getLocale());
    }

    @Override
    public MailerResult sendCancelEnrollmentEmailToManager(final Identity enrolledIdentity, final Project project, final Translator pT) {
        return sendEmailToGroup(project.getProjectLeaderGroup(), enrolledIdentity, project, pT.translate(KEY_CANCEL_ENROLLMENT_EMAIL_TO_MANAGER_SUBJECT),
                pT.translate(KEY_CANCEL_ENROLLMENT_EMAIL_TO_MANAGER_BODY), pT.getLocale());
    }

    // Project change
    @Override
    public MailerResult sendProjectChangedEmailToParticipants(final Identity changer, final Project project, final Translator pT) {
        return sendEmailProjectChanged(project.getProjectParticipantGroup(), changer, project, pT.translate(KEY_PROJECT_CHANGED_EMAIL_TO_PARTICIPANT_SUBJECT),
                pT.translate(KEY_PROJECT_CHANGED_EMAIL_TO_PARTICIPANT_BODY), pT.getLocale());
    }

    @Override
    public MailerResult sendProjectDeletedEmailToParticipants(final Identity changer, final Project project, final Translator pT) {
        return sendEmailProjectChanged(project.getProjectParticipantGroup(), changer, project, pT.translate(KEY_PROJECT_DELETED_EMAIL_TO_PARTICIPANT_SUBJECT),
                pT.translate(KEY_PROJECT_DELETED_EMAIL_TO_PARTICIPANT_BODY), pT.getLocale());
    }

	public MailerResult sendProjectDeletedEmailToManager(Identity changer, Project project, Translator pT) {
		return sendEmailProjectChanged(project.getProjectLeaderGroup(), changer, project, 
        pT.translate(KEY_PROJECT_DELETED_EMAIL_TO_PARTICIPANT_SUBJECT), 
        pT.translate(KEY_PROJECT_DELETED_EMAIL_TO_PARTICIPANT_BODY), pT.getLocale());
	}

	public MailerResult sendProjectDeletedEmailToAccountManagers(Identity changer, Project project, CourseEnvironment courseEnv, CourseNode node, Translator pT){
		Long groupKey = null;
		PropertyImpl accountManagerGroupProperty = courseEnv.getCoursePropertyManager().findCourseNodeProperty(node, null, null, ProjectBrokerCourseNode.CONF_ACCOUNTMANAGER_GROUP_KEY);
		// Check if account-manager-group-key-property already exist
		if (accountManagerGroupProperty != null) {
			groupKey = accountManagerGroupProperty.getLongValue();
		} 
		if (groupKey != null) {
			BusinessGroup accountManagerGroup = getBusinessGroupService().loadBusinessGroup(groupKey, false);
			if (groupKey != null) {
				// Group could have been deleted in GUI accidentally
				// Send mail only to the owners as the participants are already covered by the sendProjectDeletedEmailToManager() method (OLAT-6416)
				return sendEmailProjectChanged(accountManagerGroup.getOwnerGroup(), changer, project, 
		        pT.translate(KEY_PROJECT_DELETED_EMAIL_TO_PARTICIPANT_SUBJECT), 
		        pT.translate(KEY_PROJECT_DELETED_EMAIL_TO_PARTICIPANT_BODY), pT.getLocale());
			}
		}
	  
		return null;
	}
	
	private BusinessGroupService getBusinessGroupService() {
		return CoreSpringFactory.getBean(BusinessGroupServiceImpl.class);
	}

    @Override
    public MailTemplate createRemoveAsCandiadateMailTemplate(final Project project, final Identity projectManager, final Translator pT) {
        return createProjectChangeMailTemplate(project, projectManager, pT.translate(KEY_REMOVE_CANDIDATE_EMAIL_SUBJECT), pT.translate(KEY_REMOVE_CANDIDATE_EMAIL_BODY),
                pT.getLocale());
    }

    @Override
    public MailTemplate createAcceptCandiadateMailTemplate(final Project project, final Identity projectManager, final Translator pT) {
        return createProjectChangeMailTemplate(project, projectManager, pT.translate(KEY_ACCEPT_CANDIDATE_EMAIL_SUBJECT), pT.translate(KEY_ACCEPT_CANDIDATE_EMAIL_BODY),
                pT.getLocale());
    }

    @Override
    public MailTemplate createAddCandidateMailTemplate(final Project project, final Identity projectManager, final Translator pT) {
        return createProjectChangeMailTemplate(project, projectManager, pT.translate(KEY_ADD_CANDIDATE_EMAIL_SUBJECT), pT.translate(KEY_ADD_CANDIDATE_EMAIL_BODY),
                pT.getLocale());
    }

    @Override
    public MailTemplate createAddParticipantMailTemplate(final Project project, final Identity projectManager, final Translator pT) {
        return createProjectChangeMailTemplate(project, projectManager, pT.translate(KEY_ADD_PARTICIPANT_EMAIL_SUBJECT), pT.translate(KEY_ADD_PARTICIPANT_EMAIL_BODY),
                pT.getLocale());
    }

    @Override
    public MailTemplate createRemoveParticipantMailTemplate(final Project project, final Identity projectManager, final Translator pT) {
        return createProjectChangeMailTemplate(project, projectManager, pT.translate(KEY_REMOVE_PARTICIPANT_EMAIL_SUBJECT),
                pT.translate(KEY_REMOVE_PARTICIPANT_EMAIL_BODY), pT.getLocale());
    }

    // ////////////////
    // Private Methods
    // ////////////////
    private MailerResult sendEmail(final Identity enrolledIdentity, final Project project, final String subject, final String body, final Locale locale) {
        final MailTemplate enrolledMailTemplate = this.createMailTemplate(project, enrolledIdentity, subject, body, locale);
        // TODO: cg/12.01.2010 in der Methode sendMailUsingTemplateContext wurden die Variablen nicht ersetzt (Fehler oder falsch angewendet?)
        // als Workaround wurde die Methode sendMailAsSeparateMails verwendet
        final List<Identity> enrolledIdentityList = new ArrayList<Identity>();
        enrolledIdentityList.add(enrolledIdentity);
        final MailerResult mailerResult = MailerWithTemplate.getInstance().sendMailAsSeparateMails(enrolledIdentityList, null, null, enrolledMailTemplate, null);
        log.info("Audit:ProjectBroker: sendEmail to identity.name=" + enrolledIdentity.getName() + " , mailerResult.returnCode=" + mailerResult.getReturnCode());
        return mailerResult;
    }

    private MailerResult sendEmailToGroup(final SecurityGroup group, final Identity enrolledIdentity, final Project project, final String subject, final String body,
            final Locale locale) {
        final MailTemplate enrolledMailTemplate = this.createMailTemplate(project, enrolledIdentity, subject, body, locale);
        // loop over all project manger
        final List<Identity> projectManagerList = baseSecurity.getIdentitiesOfSecurityGroup(group);
        final StringBuilder identityNames = new StringBuilder();
        for (final Identity identity : projectManagerList) {
            if (identityNames.length() > 0) {
                identityNames.append(",");
            }
            identityNames.append(identity.getName());
        }
        final MailerResult mailerResult = MailerWithTemplate.getInstance().sendMailAsSeparateMails(projectManagerList, null, null, enrolledMailTemplate, null);
        log.info("Audit:ProjectBroker: sendEmailToGroup: identities=" + identityNames.toString() + " , mailerResult.returnCode=" + mailerResult.getReturnCode());
        return mailerResult;
    }

    private MailerResult sendEmailProjectChanged(final SecurityGroup group, final Identity changer, final Project project, final String subject, final String body,
            final Locale locale) {
        final MailTemplate enrolledMailTemplate = this.createProjectChangeMailTemplate(project, changer, subject, body, locale);
        // loop over all project manger
        final List<Identity> projectManagerList = baseSecurity.getIdentitiesOfSecurityGroup(group);
        final StringBuilder identityNames = new StringBuilder();
        for (final Identity identity : projectManagerList) {
            if (identityNames.length() > 0) {
                identityNames.append(",");
            }
            identityNames.append(identity.getName());
        }
        final MailerResult mailerResult = MailerWithTemplate.getInstance().sendMailAsSeparateMails(projectManagerList, null, null, enrolledMailTemplate, null);
        log.info("Audit:ProjectBroker: sendEmailToGroup: identities=" + identityNames.toString() + " , mailerResult.returnCode=" + mailerResult.getReturnCode());
        return mailerResult;
    }

    /**
     * Create default template which fill in context 'firstname' , 'lastname' and 'username'.
     * 
     * @param subject
     * @param body
     * @return
     */
    private MailTemplate createMailTemplate(final Project project, final Identity enrolledIdentity, final String subject, final String body, final Locale locale) {
        final String projectTitle = project.getTitle();
        final String currentDate = Formatter.getInstance(locale).formatDateAndTime(new Date());
        final String firstNameEnrolledIdentity = userService.getUserProperty(enrolledIdentity.getUser(), UserConstants.FIRSTNAME);
        final String lastnameEnrolledIdentity = userService.getUserProperty(enrolledIdentity.getUser(), UserConstants.LASTNAME);
        final String usernameEnrolledIdentity = enrolledIdentity.getName();

        return new MailTemplate(subject, body, MailTemplateHelper.getMailFooter(enrolledIdentity, null), null) {
            @Override
            public void putVariablesInMailContext(final VelocityContext context, final OLATPrincipal identity) {
                context.put("enrolled_identity_firstname", firstNameEnrolledIdentity);
                context.put("enrolled_identity_lastname", lastnameEnrolledIdentity);
                context.put("enrolled_identity_username", usernameEnrolledIdentity);
                // Put variables from greater context
                context.put("projectTitle", projectTitle);
                context.put("currentDate", currentDate);
            }
        };
    }

    /**
     * Create default template which fill in context 'firstname' , 'lastname' and 'username'.
     * 
     * @param subject
     * @param body
     * @return
     */
    private MailTemplate createProjectChangeMailTemplate(final Project project, final Identity changer, final String subject, final String body, final Locale locale) {
        final String projectTitle = project.getTitle();
        final String currentDate = Formatter.getInstance(locale).formatDateAndTime(new Date());
        final String firstnameProjectManager = userService.getUserProperty(changer.getUser(), UserConstants.FIRSTNAME);
        final String lastnameProjectManager = userService.getUserProperty(changer.getUser(), UserConstants.LASTNAME);
        final String usernameProjectManager = changer.getName();

        return new MailTemplate(subject, body, MailTemplateHelper.getMailFooter(changer, null), null) {
            @Override
            public void putVariablesInMailContext(final VelocityContext context, final OLATPrincipal identity) {
                // Put variables from greater context
                context.put("projectTitle", projectTitle);
                context.put("currentDate", currentDate);
                context.put("firstnameProjectManager", firstnameProjectManager);
                context.put("lastnameProjectManager", lastnameProjectManager);
                context.put("usernameProjectManager", usernameProjectManager);
            }
        };
    }

}
