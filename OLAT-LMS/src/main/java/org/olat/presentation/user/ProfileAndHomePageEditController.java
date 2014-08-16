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

package org.olat.presentation.user;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.internet.AddressException;

import org.olat.data.basesecurity.Identity;
import org.olat.data.registration.TemporaryKey;
import org.olat.data.user.User;
import org.olat.data.user.UserConstants;
import org.olat.lms.commons.mail.MailTemplateHelper;
import org.olat.lms.registration.RegistrationService;
import org.olat.lms.user.HomePageConfig;
import org.olat.lms.user.HomePageConfigManager;
import org.olat.lms.user.HomePageConfigManagerImpl;
import org.olat.lms.user.UserProfileDataEBL;
import org.olat.lms.user.UserProfileEBL;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.security.authentication.SupportsAfterLoginInterceptor;
import org.olat.system.commons.Settings;
import org.olat.system.event.Event;
import org.olat.system.mail.Emailer;
import org.olat.system.spring.CoreSpringFactory;

import com.thoughtworks.xstream.XStream;

/**
 * Initial Date: Jul 14, 2005
 * 
 * @author Alexander Schneider Comment:
 */
public class ProfileAndHomePageEditController extends BasicController implements SupportsAfterLoginInterceptor {

    private static final String VELOCITY_ROOT = PackageUtil.getPackageVelocityRoot(ProfileAndHomePageEditController.class);
    private static final long UPLOAD_LIMIT_KB = 500;

    private final VelocityContainer myContent;
    private final Link previewButton;

    private Translator translator;

    protected ProfileFormController profileFormController;
    private Component profileFormControllerComponent;
    private final HomePageConfigManager hpcm = HomePageConfigManagerImpl.getInstance();
    private PortraitUploadController portraitUploadController;
    private Controller hpDispC;
    private CloseableModalController clc;
    protected Identity identityToModify;
    protected HomePageConfig homePageConfig;
    private DialogBoxController dialogCtr;
    private static String SEPARATOR = "\n____________________________________________________________________\n";

    private boolean emailChanged = false;
    protected String changedEmail;
    protected String currentEmail;
    private final boolean isAdministrativeUser;

    public ProfileAndHomePageEditController(final UserRequest ureq, final WindowControl wControl) {
        this(ureq, wControl, ureq.getIdentity(), ureq.getUserSession().getRoles().isOLATAdmin());
    }

    /**
     * @param ureq
     * @param wControl
     * @param identity
     *            the identity to be changed. Can be different than current user (usermanager that edits another users profile)
     * @param isAdministrativeUser
     */
    public ProfileAndHomePageEditController(final UserRequest ureq, final WindowControl wControl, final Identity identityToModify, final boolean isAdministrativeUser) {
        super(ureq, wControl);
        this.identityToModify = identityToModify;

        this.isAdministrativeUser = isAdministrativeUser;
        this.translator = PackageUtil.createPackageTranslator(ProfileAndHomePageEditController.class, ureq.getLocale());
        this.translator = getUserService().getUserPropertiesConfig().getTranslator(this.translator);

        this.myContent = new VelocityContainer("homepage", VELOCITY_ROOT + "/homepage.html", this.translator, this);
        this.previewButton = LinkFactory.createButtonSmall("command.preview", this.myContent, this);
        this.homePageConfig = this.hpcm.loadConfigFor(this.identityToModify.getName());

        this.profileFormController = new ProfileFormController(ureq, wControl, this.homePageConfig, this.identityToModify, isAdministrativeUser);
        listenTo(this.profileFormController);
        this.profileFormControllerComponent = this.profileFormController.getInitialComponent();
        this.myContent.put("homepageform", this.profileFormControllerComponent);

        this.portraitUploadController = new PortraitUploadController(ureq, getWindowControl(), this.identityToModify, UPLOAD_LIMIT_KB);
        listenTo(this.portraitUploadController);

        final Component c = this.portraitUploadController.getInitialComponent();
        this.myContent.put("c", c);
        putInitialPanel(this.myContent);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == this.previewButton) {
            if (this.hpDispC != null) {
                removeAsListenerAndDispose(this.hpDispC);
            }
            this.hpDispC = new HomePageDisplayController(ureq, getWindowControl(), homePageConfig);
            listenTo(hpDispC);
            if (this.clc != null) {
                removeAsListenerAndDispose(clc);
            }
            this.clc = new CloseableModalController(getWindowControl(), this.translator.translate("command.closehp"), this.hpDispC.getInitialComponent());
            listenTo(clc);
            this.clc.insertHeaderCss();
            this.clc.activate();
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        super.event(ureq, source, event);
        if (source == this.portraitUploadController) {
            if (event.equals(Event.DONE_EVENT) || event.getCommand().equals(PortraitUploadController.PORTRAIT_DELETED_EVENT.getCommand())) {
                // should not fire event, as only needed to update identity if useradmin changed it. portrait doesnt change identity and is not shown in table.
                // see UserAdminController
                // fireEvent(ureq, Event.DONE_EVENT);
            }
        } else if (source == this.profileFormController) {
            if (event == Event.DONE_EVENT) {
                // get the new values from the form
                this.profileFormController.updateFromFormData(this.homePageConfig, this.identityToModify);

                // update the home page configuration
                this.hpcm.saveConfigTo(this.identityToModify.getName(), this.homePageConfig);

                // update the portrait upload (gender specific image)
                if (portraitUploadController != null) {
                    removeAsListenerAndDispose(portraitUploadController);
                }
                portraitUploadController = new PortraitUploadController(ureq, getWindowControl(), this.identityToModify, UPLOAD_LIMIT_KB);
                listenTo(this.portraitUploadController);
                this.myContent.put("c", this.portraitUploadController.getInitialComponent());

                // fire the appropriate event
                fireEvent(ureq, Event.DONE_EVENT);

                UserProfileEBL userProfileEBL = getUserProfileEBL();

                ProfileAndHomePageEditController.this.identityToModify = userProfileEBL.loadIdentity(ProfileAndHomePageEditController.this.identityToModify.getKey());
                currentEmail = getUserService().getUserProperty(ProfileAndHomePageEditController.this.identityToModify.getUser(), UserConstants.EMAIL);

                ProfileAndHomePageEditController.this.identityToModify = profileFormController
                        .updateIdentityFromFormData(ProfileAndHomePageEditController.this.identityToModify);
                changedEmail = getUserService().getUserProperty(ProfileAndHomePageEditController.this.identityToModify.getUser(), UserConstants.EMAIL);

                UserProfileDataEBL userProfileDataEBL = new UserProfileDataEBL(ProfileAndHomePageEditController.this.identityToModify, false, false, ureq
                        .getUserSession().getRoles().isOLATAdmin(), ureq.getUserSession().getRoles().isUserManager(), currentEmail, changedEmail);
                userProfileDataEBL = userProfileEBL.updateUserProfileData(userProfileDataEBL);

                if (!userProfileDataEBL.isUserUpdated()) {
                    getWindowControl().setInfo(translate("profile.unsuccessful"));
                }

                if (userProfileDataEBL.isEmailChanged()) {
                    if (dialogCtr != null) {
                        dialogCtr.dispose();
                    }
                    if (dialogCtr != null) {
                        dialogCtr.dispose();
                    }
                    final String changerEMail = getUserService().getUserProperty(ureq.getIdentity().getUser(), UserConstants.EMAIL, ureq.getLocale());
                    String dialogText = "";
                    if (changerEMail != null && changerEMail.length() > 0 && changerEMail.equals(currentEmail)) {
                        dialogText = translate("email.change.dialog.text");
                    } else {
                        dialogText = translate("email.change.dialog.text.usermanager");
                    }
                    dialogCtr = DialogBoxUIFactory.createYesNoDialog(ureq, getWindowControl(), translate("email.change.dialog.title"), dialogText);
                    dialogCtr.addControllerListener(this);
                    dialogCtr.activate();
                }
            }
        } else if (source == dialogCtr) {
            dialogCtr.dispose();
            dialogCtr = null;
            if (DialogBoxUIFactory.isYesEvent(event)) {
                if (changedEmail != null) {
                    createChangeEmailWorkflow(ureq);
                }
            }
            resetForm(ureq, getWindowControl());
        }
    }

    private void createChangeEmailWorkflow(final UserRequest ureq) {
        // send email
        changedEmail = changedEmail.trim();
        String body = null;
        String subject = null;
        // get remote address
        final String ip = ureq.getHttpReq().getRemoteAddr();
        final String today = DateFormat.getDateInstance(DateFormat.LONG, ureq.getLocale()).format(new Date());
        // mailer configuration
        final String serverpath = Settings.getServerContextPathURI();
        final String servername = ureq.getHttpReq().getServerName();

        final Emailer mailer = new Emailer(MailTemplateHelper.getMailTemplateWithFooterNoUserData(ureq.getLocale()));

        // load or create temporary key
        final HashMap<String, String> mailMap = new HashMap<String, String>();
        mailMap.put("currentEMail", currentEmail);
        mailMap.put("changedEMail", changedEmail);

        final XStream xml = new XStream();
        final String serMailMap = xml.toXML(mailMap);

        TemporaryKey tk = loadCleanTemporaryKey(serMailMap);
        if (tk == null) {
            tk = getRegistrationService().createTemporaryKeyByEmail(serMailMap, ip, RegistrationService.EMAIL_CHANGE);
        } else {
            getRegistrationService().deleteTemporaryKeyWithId(tk.getRegistrationKey());
            tk = getRegistrationService().createTemporaryKeyByEmail(serMailMap, ip, RegistrationService.EMAIL_CHANGE);
        }

        // create date, time string
        final Calendar cal = Calendar.getInstance();
        cal.setTime(tk.getCreationDate());
        cal.add(Calendar.DAY_OF_WEEK, ChangeEMailController.TIME_OUT);
        final String time = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, ureq.getLocale()).format(cal.getTime());
        // create body and subject for email
        body = this.translator.translate("email.change.body", new String[] {
                serverpath + "/dmz/emchange/index.html?key=" + tk.getRegistrationKey() + "&lang=" + ureq.getLocale().getLanguage(), time })
                + SEPARATOR + this.translator.translate("email.change.wherefrom", new String[] { serverpath, today, ip });
        subject = translate("email.change.subject");
        // send email
        try {
            final boolean isMailSent = mailer.sendEmail(changedEmail, subject, body);
            if (isMailSent) {
                tk.setMailSent(true);
                // set key
                final User user = this.identityToModify.getUser();
                getUserService().setUserProperty(user, UserConstants.EMAILCHANGE, tk.getRegistrationKey());
                getUserService().updateUser(user);
                getWindowControl().setInfo(this.translator.translate("email.sent"));
            } else {
                tk.setMailSent(false);
                getRegistrationService().deleteTemporaryKeyWithId(tk.getRegistrationKey());
                getWindowControl().setError(this.translator.translate("email.notsent"));
            }
        } catch (final AddressException e) {
            getRegistrationService().deleteTemporaryKeyWithId(tk.getRegistrationKey());
            getWindowControl().setError(this.translator.translate("email.notsent"));
        } catch (final SendFailedException e) {
            getRegistrationService().deleteTemporaryKeyWithId(tk.getRegistrationKey());
            getWindowControl().setError(this.translator.translate("email.notsent"));
        } catch (final MessagingException e) {
            getRegistrationService().deleteTemporaryKeyWithId(tk.getRegistrationKey());
            getWindowControl().setError(this.translator.translate("email.notsent"));
        }
    }

    /**
     * Load and clean temporary keys with action "EMAIL_CHANGE".
     * 
     * @param serMailMap
     * @return
     */
    private TemporaryKey loadCleanTemporaryKey(final String serMailMap) {
        TemporaryKey tk = getRegistrationService().loadTemporaryKeyByEmail(serMailMap);
        if (tk == null) {
            final XStream xml = new XStream();
            final HashMap<String, String> mails = (HashMap<String, String>) xml.fromXML(serMailMap);
            final String currentEMail = mails.get("currentEMail");
            List<TemporaryKey> tks = getRegistrationService().loadTemporaryKeyByAction(RegistrationService.EMAIL_CHANGE);
            if (tks != null) {
                synchronized (tks) {
                    tks = getRegistrationService().loadTemporaryKeyByAction(RegistrationService.EMAIL_CHANGE);
                    int countCurrentEMail = 0;
                    for (final TemporaryKey temporaryKey : tks) {
                        final HashMap<String, String> tkMails = (HashMap<String, String>) xml.fromXML(temporaryKey.getEmailAddress());
                        if (tkMails.get("currentEMail").equals(currentEMail)) {
                            if (countCurrentEMail > 0) {
                                // clean
                                getRegistrationService().deleteTemporaryKeyWithId(temporaryKey.getRegistrationKey());
                            } else {
                                // load
                                tk = temporaryKey;
                            }
                            countCurrentEMail++;
                        }
                    }
                }
            }
        }
        return tk;
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // controllers disposed by basic controller
    }

    public void resetForm(final UserRequest ureq, final WindowControl wControl) {
        if (this.profileFormController != null) {
            this.myContent.remove(this.profileFormControllerComponent);
            removeAsListenerAndDispose(this.profileFormController);
        }
        this.profileFormController = new ProfileFormController(ureq, wControl, this.homePageConfig, this.identityToModify, isAdministrativeUser);
        listenTo(this.profileFormController);
        this.profileFormControllerComponent = this.profileFormController.getInitialComponent();
        this.myContent.put("homepageform", this.profileFormControllerComponent);
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

    private RegistrationService getRegistrationService() {
        return CoreSpringFactory.getBean(RegistrationService.class);
    }

    private UserProfileEBL getUserProfileEBL() {
        return CoreSpringFactory.getBean(UserProfileEBL.class);
    }
}
