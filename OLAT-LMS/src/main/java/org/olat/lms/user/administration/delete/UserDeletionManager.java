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

package org.olat.lms.user.administration.delete;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Authentication;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.lifecycle.LifeCycleManager;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.data.user.User;
import org.olat.data.user.UserConstants;
import org.olat.data.user.delete.UserDeletionDao;
import org.olat.lms.commons.LmsSpringBeanTypes;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.lms.course.assessment.EfficiencyStatementManager;
import org.olat.lms.repository.delete.DeletionModule;
import org.olat.lms.user.UserDataDeletable;
import org.olat.lms.user.UserService;
import org.olat.lms.user.propertyhandler.UserPropertyHandler;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.user.administration.delete.SelectionController;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.mail.MailTemplate;
import org.olat.system.mail.MailerResult;
import org.olat.system.mail.MailerWithTemplate;
import org.olat.system.spring.CoreSpringFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Manager for user-deletion.
 * 
 * @author Christian Guretzki
 */
public class UserDeletionManager extends BasicManager {

    private static final Logger log = LoggerHelper.getLogger();

    public static final String DELETED_USER_DELIMITER = "_bkp_";
    /** Default value for last-login duration in month. */
    private static final int DEFAULT_LAST_LOGIN_DURATION = 24;
    /** Default value for delete-email duration in days. */
    private static final int DEFAULT_DELETE_EMAIL_DURATION = 30;
    private static final String LAST_LOGIN_DURATION_PROPERTY_NAME = "LastLoginDuration";
    private static final String DELETE_EMAIL_DURATION_PROPERTY_NAME = "DeleteEmailDuration";
    private static final String PROPERTY_CATEGORY = "UserDeletion";

    private static UserDeletionManager INSTANCE;
    public static final String SEND_DELETE_EMAIL_ACTION = "sendDeleteEmail";
    private static final String USER_DELETED_ACTION = "userdeleted";
    private static boolean keepUserLoginAfterDeletion;
    private static boolean keepUserEmailAfterDeletion;

    // 07.07.2011/cg : we must use CoreSpringFactory because with @Autowired spring-startup error occurs
    // @Autowired
    // private Set<UserDataDeletable> userDataDeletableResources;

    @Autowired
    DeletionModule deletionModule;
    @Autowired
    UserService userService;
    @Autowired
    UserDeletionDao userDeletionDao;
    @Autowired
    BaseSecurity baseSecurity;

    /**
     * [used by spring]
     */
    private UserDeletionManager() {
        INSTANCE = this;
    }

    /**
     * @return Singleton.
     */
    public static UserDeletionManager getInstance() {
        return INSTANCE;
    }

    /**
     * Send 'delete'- emails to a list of identities. The delete email is an announcement for the user-deletion.
     * 
     * @param selectedIdentities
     * @return String with warning message (e.g. email-address not valid, could not send email). If there is no warning, the return String is empty ("").
     */
    public String sendUserDeleteEmailTo(final List<Identity> selectedIdentities, final MailTemplate template, final boolean isTemplateChanged,
            final String keyEmailSubject, final String keyEmailBody, final Identity sender, final Translator pT) {
        final StringBuilder buf = new StringBuilder();
        if (template != null) {
            final MailerWithTemplate mailer = MailerWithTemplate.getInstance();
            template.addToContext("responseTo", deletionModule.getEmailResponseTo());
            for (final Iterator iter = selectedIdentities.iterator(); iter.hasNext();) {
                final Identity identity = (Identity) iter.next();
                if (!isTemplateChanged) {
                    // Email template has NOT changed => take translated version of subject and body text
                    final Translator identityTranslator = PackageUtil.createPackageTranslator(SelectionController.class, I18nManager.getInstance().getLocaleOrDefault(
                            identity.getUser().getPreferences().getLanguage()));
                    template.setSubjectTemplate(identityTranslator.translate(keyEmailSubject));
                    template.setBodyTemplate(identityTranslator.translate(keyEmailBody));
                }
                template.putVariablesInMailContext(template.getContext(), identity);
                log.debug(" Try to send Delete-email to identity=" + identity.getName() + " with email="
                        + userService.getUserProperty(identity.getUser(), UserConstants.EMAIL));
                List<Identity> ccIdentities = new ArrayList<Identity>();
                if (template.getCpfrom()) {
                    ccIdentities.add(sender);
                } else {
                    ccIdentities = null;
                }
                final MailerResult mailerResult = mailer.sendMailUsingTemplateContext(identity, ccIdentities, null, template, sender);
                if (mailerResult.getReturnCode() != MailerResult.OK) {
                    buf.append(
                            pT.translate("email.error.send.failed", new String[] { userService.getUserProperty(identity.getUser(), UserConstants.EMAIL),
                                    identity.getName() })).append("\n");
                }
                log.info("Audit:User-Deletion: Delete-email send to identity=" + identity.getName() + " with email="
                        + userService.getUserProperty(identity.getUser(), UserConstants.EMAIL));
                userDeletionDao.markSendEmailEvent(identity);
            }
        } else {
            // no template => User decides to sending no delete-email, mark only in lifecycle table 'sendEmail'
            for (final Iterator iter = selectedIdentities.iterator(); iter.hasNext();) {
                final Identity identity = (Identity) iter.next();
                log.info("Audit:User-Deletion: Move in 'Email sent' section without sending email, identity=" + identity.getName());
                userDeletionDao.markSendEmailEvent(identity);
            }
        }
        return buf.toString();
    }

    /**
     * Return list of identities which have last-login older than 'lastLoginDuration' parameter. This user are ready to start with user-deletion process.
     * 
     * @param lastLoginDuration
     *            last-login duration in month
     * @return List of Identity objects
     */
    public List getDeletableIdentities(final int lastLoginDuration) {
        return userDeletionDao.getDeletableIdentities(lastLoginDuration);
    }

    /**
     * Return list of identities which are in user-deletion-process. user-deletion-process means delete-announcement.email send, duration of waiting for response is not
     * expired.
     * 
     * @param deleteEmailDuration
     *            Duration of user-deletion-process in days
     * @return List of Identity objects
     */
    public List getIdentitiesInDeletionProcess(final int deleteEmailDuration) {
        return userDeletionDao.getIdentitiesInDeletionProcess(deleteEmailDuration);
    }

    /**
     * Return list of identities which are ready-to-delete in user-deletion-process. (delete-announcement.email send, duration of waiting for response is expired).
     * 
     * @param deleteEmailDuration
     *            Duration of user-deletion-process in days
     * @return List of Identity objects
     */
    public List getIdentitiesReadyToDelete(final int deleteEmailDuration) {
        return userDeletionDao.getIdentitiesReadyToDelete(deleteEmailDuration);
    }

    /**
     * @return true when user can be deleted (non deletion-process is still running)
     */
    public boolean isReadyToDelete() {
        return UserFileDeletionManager.isReadyToDelete();
    }

    public void deleteIdentities(final List<Identity> toDeleteIdentities) {
        for (int i = 0; i < toDeleteIdentities.size(); i++) {
            deleteIdentity(toDeleteIdentities.get(i));
            DBFactory.getInstance().intermediateCommit();
        }
    }

    /**
     * Delete all user-data in registered deleteable resources.
     * 
     * @param identity
     * @return true
     */
    public void deleteIdentity(Identity identity) {

        log.info("Start deleteIdentity for identity=" + identity);

        final String newName = getBackupStringWithDate(identity.getName());

        log.info("Start EfficiencyStatementManager.archiveUserData for identity=" + identity);
        EfficiencyStatementManager.getInstance().archiveUserData(identity, getArchivFilePath(identity));

        log.info("Start Deleting user=" + identity);
        // 07.07.2011/cg : we must use CoreSpringFactory here because with @Autowired spring-startup error occurs
        Map<String, Object> map = CoreSpringFactory.getBeansOfType(LmsSpringBeanTypes.userDataDeletable);
        for (Iterator iterator = map.values().iterator(); iterator.hasNext();) {
            UserDataDeletable element = (UserDataDeletable) iterator.next();
            log.info("UserDataDeletable-Loop element=" + element);
            element.deleteUserData(identity, newName);
        }
        log.info("deleteUserProperties user=" + identity.getUser());
        userService.deleteUserProperties(identity.getUser());
        // Delete all authentications for certain identity
        final List authentications = baseSecurity.getAuthentications(identity);
        for (final Iterator iter = authentications.iterator(); iter.hasNext();) {
            final Authentication auth = (Authentication) iter.next();
            log.info("deleteAuthentication auth=" + auth);
            baseSecurity.deleteAuthentication(auth);
            log.debug("Delete auth=" + auth + "  of identity=" + identity);
        }

        // remove identity from its security groups
        final List<SecurityGroup> securityGroups = baseSecurity.getSecurityGroupsForIdentity(identity);
        for (final SecurityGroup secGroup : securityGroups) {
            baseSecurity.removeIdentityFromSecurityGroup(identity, secGroup);
            log.info("Removing user=" + identity + " from security group=" + secGroup.toString());
        }

        // can be used, if there is once the possibility to delete identities without db-constraints...
        // if neither email nor login should be kept, REALLY DELETE Identity
        /*
         * if (!keepUserEmailAfterDeletion & !keepUserLoginAfterDeletion){ identity = (Identity)DBFactory.getInstance().loadObject(identity);
         * DBFactory.getInstance().deleteObject(identity.getUser()); DBFactory.getInstance().deleteObject(identity); } else {
         */
        identity = (Identity) DBFactory.getInstance().loadObject(identity);
        // keep login-name only -> change email
        if (!keepUserEmailAfterDeletion) {
            final List<UserPropertyHandler> userPropertyHandlers = userService.getUserPropertyHandlersFor(
                    "org.olat.presentation.user.administration.UsermanagerUserSearchForm", true);
            final User persistedUser = identity.getUser();
            String actualProperty;
            for (final UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
                actualProperty = userPropertyHandler.getName();
                if (actualProperty.equals(UserConstants.EMAIL)) {
                    final String oldEmail = userPropertyHandler.getUserProperty(persistedUser, null);
                    String newEmail = "";
                    if (StringHelper.containsNonWhitespace(oldEmail)) {
                        newEmail = getBackupStringWithDate(oldEmail);
                    }
                    log.info("Update user-property user=" + persistedUser);
                    userPropertyHandler.setUserProperty(persistedUser, newEmail);
                }
            }
        }

        // keep email only -> change login-name
        if (!keepUserLoginAfterDeletion) {
            identity.setName(newName);
        }

        // keep everything, change identity.status to deleted
        log.info("Change stater identity=" + identity);
        identity.setStatus(Identity.STATUS_DELETED);
        DBFactory.getInstance().updateObject(identity);
        LifeCycleManager.createInstanceFor(identity).deleteTimestampFor(SEND_DELETE_EMAIL_ACTION);
        LifeCycleManager.createInstanceFor(identity).markTimestampFor(USER_DELETED_ACTION, createLifeCycleLogDataFor(identity));
        // }

        // TODO: chg: ev. log.audit at another place
        log.info("Audit:User-Deletion: Delete all userdata for identity=" + identity);
    }

    public String getBackupStringWithDate(final String original) {
        final DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
        final String dateStamp = dateFormat.format(new Date());
        return dateStamp + DELETED_USER_DELIMITER + original;
    }

    private String createLifeCycleLogDataFor(final Identity identity) {
        final StringBuilder buf = new StringBuilder();
        buf.append("<identity>");
        buf.append("<username>").append(identity.getName()).append("</username>");
        buf.append("<lastname>").append(identity.getName()).append("</lastname>");
        buf.append("<firstname>").append(identity.getName()).append("</firstname>");
        buf.append("<email>").append(identity.getName()).append("</email>");
        buf.append("</identity>");
        return buf.toString();
    }

    /**
     * Re-activate an identity, lastLogin = now, reset deleteemaildate = null.
     * 
     * @param identity
     */
    public void setIdentityAsActiv(final Identity anIdentity) {
        userDeletionDao.setIdentityAsActiv(anIdentity);
    }

    /**
     * @return Return duration in days for waiting for reaction on delete-email.
     */
    public int getDeleteEmailDuration() {
        return getPropertyByName(DELETE_EMAIL_DURATION_PROPERTY_NAME, DEFAULT_DELETE_EMAIL_DURATION);
    }

    /**
     * @return Return last-login duration in month for user on delete-selection list.
     */
    public int getLastLoginDuration() {
        return getPropertyByName(LAST_LOGIN_DURATION_PROPERTY_NAME, DEFAULT_LAST_LOGIN_DURATION);
    }

    private int getPropertyByName(final String name, final int defaultValue) {
        final List properties = PropertyManager.getInstance().findProperties(null, null, null, PROPERTY_CATEGORY, name);
        if (properties.size() == 0) {
            return defaultValue;
        } else {
            return ((PropertyImpl) properties.get(0)).getLongValue().intValue();
        }
    }

    public void setLastLoginDuration(final int lastLoginDuration) {
        setProperty(LAST_LOGIN_DURATION_PROPERTY_NAME, lastLoginDuration);
    }

    public void setDeleteEmailDuration(final int deleteEmailDuration) {
        setProperty(DELETE_EMAIL_DURATION_PROPERTY_NAME, deleteEmailDuration);
    }

    private void setProperty(final String propertyName, final int value) {
        final List properties = PropertyManager.getInstance().findProperties(null, null, null, PROPERTY_CATEGORY, propertyName);
        PropertyImpl property = null;
        if (properties.size() == 0) {
            property = PropertyManager.getInstance().createPropertyInstance(null, null, null, PROPERTY_CATEGORY, propertyName, null, new Long(value), null, null);
        } else {
            property = (PropertyImpl) properties.get(0);
            property.setLongValue(new Long(value));
        }
        PropertyManager.getInstance().saveProperty(property);
    }

    /**
     * Return in spring config defined administrator identity.
     * 
     * @return
     */
    public Identity getAdminIdentity() {
        return deletionModule.getAdminUserIdentity();
    }

    /**
     * Setter method used by spring
     * 
     * @param keepUserLoginAfterDeletion
     *            The keepUserLoginAfterDeletion to set.
     */
    public void setKeepUserLoginAfterDeletion(final boolean keepUserLoginAfterDeletion) {
        UserDeletionManager.keepUserLoginAfterDeletion = keepUserLoginAfterDeletion;
    }

    /**
     * Setter method used by spring
     * 
     * @param keepUserEmailAfterDeletion
     *            The keepUserEmailAfterDeletion to set.
     */
    public void setKeepUserEmailAfterDeletion(final boolean keepUserEmailAfterDeletion) {
        UserDeletionManager.keepUserEmailAfterDeletion = keepUserEmailAfterDeletion;
    }

    public static boolean isKeepUserLoginAfterDeletion() {
        return keepUserLoginAfterDeletion;
    }

    private File getArchivFilePath(final Identity identity) {
        final String archiveFilePath = deletionModule.getArchiveRootPath() + File.separator + UserDeletionDao.USER_ARCHIVE_DIR + File.separator
                + DeletionModule.getArchiveDatePath() + File.separator + "del_identity_" + identity.getName();
        final File archiveIdentityRootDir = new File(archiveFilePath);
        if (!archiveIdentityRootDir.exists()) {
            archiveIdentityRootDir.mkdirs();
        }
        return archiveIdentityRootDir;
    }

}
