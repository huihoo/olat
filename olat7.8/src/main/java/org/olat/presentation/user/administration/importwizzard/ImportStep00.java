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
package org.olat.presentation.user.administration.importwizzard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.user.UserConstants;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.lms.commons.i18n.I18nModule;
import org.olat.lms.commons.mediaresource.ExcelMediaResource;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.user.UserService;
import org.olat.lms.user.propertyhandler.UserPropertyHandler;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.ValidationError;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.wizard.BasicStep;
import org.olat.presentation.framework.core.control.generic.wizard.PrevNextFinishConfig;
import org.olat.presentation.framework.core.control.generic.wizard.StepFormBasicController;
import org.olat.presentation.framework.core.control.generic.wizard.StepFormController;
import org.olat.presentation.framework.core.control.generic.wizard.StepsEvent;
import org.olat.presentation.framework.core.control.generic.wizard.StepsRunContext;
import org.olat.presentation.framework.dispatcher.mapper.Mapper;
import org.olat.system.commons.StringHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * First step in user import wizard.
 * <P>
 * Initial Date: 30.01.2008 <br>
 * 
 * @author rhaag
 */
class ImportStep00 extends BasicStep {

    boolean canCreateOLATPassword;
    static final String usageIdentifyer = UserImportController.class.getCanonicalName();
    Mapper excelMapper;
    String formatexplanationPart2;
    TextElement textAreaElement;

    public ImportStep00(final UserRequest ureq, final boolean canCreateOLATPassword) {
        super(ureq);
        this.canCreateOLATPassword = canCreateOLATPassword;
        setI18nTitleAndDescr("step0.description", "step0.short.descr");
        setNextStep(new ImportStep01(ureq, canCreateOLATPassword, false));
    }

    private BaseSecurity getBaseSecurity() {
        return (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
    }

    /**
	 */
    @Override
    public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
        return new PrevNextFinishConfig(false, true, false);
    }

    /**
     * org.olat.presentation.framework.control.generic.wizard.StepsRunContext, org.olat.presentation.framework.components.form.flexible.impl.Form)
     */
    @Override
    public StepFormController getStepController(final UserRequest ureq, final WindowControl windowControl, final StepsRunContext stepsRunContext, final Form form) {
        final StepFormController stepI = new ImportStepForm00(ureq, windowControl, form, stepsRunContext);
        return stepI;
    }

    private final class ImportStepForm00 extends StepFormBasicController {

        private FormLayoutContainer textContainer;
        private List<UserPropertyHandler> userPropertyHandlers;
        private ArrayList<List<String>> newIdents;
        private List<Object> idents;

        public ImportStepForm00(final UserRequest ureq, final WindowControl control, final Form rootForm, final StepsRunContext runContext) {
            super(ureq, control, rootForm, runContext, LAYOUT_VERTICAL, null);
            flc.setTranslator(getTranslator());
            initForm(ureq);
        }

        @Override
        protected void doDispose() {
            // TODO Auto-generated method stub
        }

        @SuppressWarnings("synthetic-access")
        @Override
        protected void formOK(final UserRequest ureq) {
            addToRunContext("idents", idents);
            addToRunContext("newIdents", newIdents);
            addToRunContext("validImport", Boolean.TRUE);
            boolean newUsers = false;
            if (newIdents.size() != 0) {
                setNextStep(new ImportStep01(ureq, canCreateOLATPassword, true));
                newUsers = true;
            }
            addToRunContext("newUsers", newUsers);
            fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
        }

        @Override
        protected boolean validateFormLogic(@SuppressWarnings("unused") final UserRequest ureq) {
            final String inp = textAreaElement.getValue();
            final String defaultlang = I18nModule.getDefaultLocale().toString();
            final List<String> importedEmails = new ArrayList<String>();

            boolean importDataError = false;

            idents = new ArrayList<Object>();
            newIdents = new ArrayList<List<String>>();
            // Note: some values are fix and required: login, pwd and lang, those
            // can not be configured in the config file
            // because they are not user properties.
            // all fields out of UserImportController
            // are required and have to be submitted in the right order
            // - pwd can be enabled / disabled by configuration
            final Set<String> languages = I18nModule.getEnabledLanguageKeys();
            final String[] lines = inp.split("\r?\n");
            for (int i = 0; i < lines.length; i++) {
                final String line = lines[i];
                if (!line.equals("")) { // ignore empty lines
                    String delimiter = "\t";
                    // use comma as fallback delimiter, e.g. for better testing
                    if (line.indexOf(delimiter) == -1) {
                        delimiter = ",";
                    }
                    final String[] parts = line.split(delimiter);
                    String login, pwd, lang;

                    int columnId = 0;

                    // login row
                    if (parts.length > columnId) {
                        login = parts[columnId].trim();
                        if (!getUserService().syntaxCheckOlatLogin(login)) {
                            textAreaElement.setErrorKey("error.login", new String[] { String.valueOf(i + 1), login });
                            importDataError = true;
                            break;
                        }
                    } else {
                        textAreaElement.setErrorKey("error.columncount", new String[] { String.valueOf(i + 1) });
                        importDataError = true;
                        break;
                    }
                    columnId++;

                    // pwd row
                    if (canCreateOLATPassword) {
                        if (parts.length > columnId) {
                            pwd = parts[columnId].trim();
                            if (StringHelper.containsNonWhitespace(pwd)) {
                                if (!getUserService().verifyPasswordStrength("", pwd, login)) {
                                    textAreaElement.setErrorKey("error.pwd", new String[] { String.valueOf(i + 1), pwd });
                                    importDataError = true;
                                    break;
                                }
                            } else {
                                // treat all white-space-only passwords as non-passwords.
                                // the user generation code below will then generate no
                                // authentication token for this user
                                pwd = null;
                            }
                        } else {
                            textAreaElement.setErrorKey("error.columncount", new String[] { String.valueOf(i + 1) });
                            importDataError = true;
                            break;
                        }
                    } else {
                        pwd = null;
                    }
                    columnId++;

                    // optional language fields
                    if (parts.length > columnId) {
                        lang = parts[columnId].trim();
                        if (lang.equals("")) {
                            lang = defaultlang;
                        } else if (!languages.contains(lang)) {
                            textAreaElement.setErrorKey("error.lang", new String[] { String.valueOf(i + 1), lang });
                            importDataError = true;
                            break;
                        }
                    } else {
                        lang = defaultlang;
                    }
                    columnId++;

                    final Identity ident = getBaseSecurity().findIdentityByName(login);
                    if (ident != null) {
                        // ignore existing accounts, add info message
                        idents.add(ident);
                    } else {
                        // no identity/user yet, create
                        UserPropertyHandler userPropertyHandler;
                        String thisKey = "";
                        String thisValue = "";
                        // check that no user with same login name is already in list
                        for (final Iterator<List<String>> it_news = newIdents.iterator(); it_news.hasNext();) {
                            final List<String> singleUser = it_news.next();
                            if (singleUser.get(1).equalsIgnoreCase(login)) {
                                textAreaElement.setErrorKey("error.login.douplicate", new String[] { String.valueOf(i + 1), login });
                                importDataError = true;
                                break;
                            }
                        }

                        final List<String> ud = new ArrayList<String>();
                        // insert fix fields: login, pwd, lang from above
                        ud.add("false"); // used for first column in model (user existing)
                        ud.add(login);
                        ud.add(pwd);
                        ud.add(lang);

                        for (int j = 0; j < userPropertyHandlers.size(); j++) {
                            userPropertyHandler = userPropertyHandlers.get(j);
                            thisKey = userPropertyHandler.getName();
                            // last columns may be empty if not mandatory
                            if (parts.length <= columnId) {
                                thisValue = "";
                            } else {
                                thisValue = parts[columnId].trim();
                            }
                            final boolean isMandatoryField = getUserService().isMandatoryUserProperty(usageIdentifyer, userPropertyHandler);
                            if (isMandatoryField && !StringHelper.containsNonWhitespace(thisValue)) {
                                textAreaElement.setErrorKey("error.mandatory",
                                        new String[] { String.valueOf(i + 1), translate(userPropertyHandler.i18nFormElementLabelKey()) });
                                importDataError = true;
                                break;
                            }
                            // used for call-back value depending on PropertyHandler
                            final ValidationError validationError = new ValidationError();
                            if (!userPropertyHandler.isValidValue(thisValue, validationError, getLocale())) {
                                textAreaElement.setErrorKey(
                                        "error.lengthorformat",
                                        new String[] { String.valueOf(i + 1), translate(userPropertyHandler.i18nFormElementLabelKey()),
                                                translate(validationError.getErrorKey()) });
                                importDataError = true;
                                break;
                            }
                            // check that no user with same (institutional) e-mail is already in OLAT
                            if ((thisKey.equals(UserConstants.INSTITUTIONALEMAIL) || thisKey.equals(UserConstants.EMAIL)) && !thisValue.isEmpty()) {
                                // check that no user with same email is already in OLAT
                                final Identity identity = getUserService().findIdentityByEmail(thisValue);
                                if (identity != null) {
                                    textAreaElement.setErrorKey("error.email.exists", new String[] { String.valueOf(i + 1), thisValue });
                                    importDataError = true;
                                    break;
                                }
                            }
                            // check that no user with same email is already in list
                            if (thisKey.equals(UserConstants.EMAIL)) {
                                // check that no user with same email is already in list
                                Integer mailPos = importedEmails.indexOf(thisValue);
                                if (mailPos != -1) {
                                    mailPos++;
                                    textAreaElement.setErrorKey("error.email.douplicate", new String[] { String.valueOf(i + 1), thisValue, mailPos.toString() });
                                    importDataError = true;
                                    break;
                                } else {
                                    importedEmails.add(thisValue);
                                }
                            }
                            ud.add(thisValue);
                            columnId++;
                        }
                        idents.add(ud);
                        newIdents.add(ud);
                    }
                }
            }

            if (importDataError) {
                return false;
            }

            return true;
        }

        @Override
        protected void initForm(final FormItemContainer formLayout, @SuppressWarnings("unused") final Controller listener, final UserRequest ureq) {
            setFormTitle("title");

            textContainer = FormLayoutContainer.createCustomFormLayout("index", getTranslator(), this.velocity_root + "/step0.html");
            formLayout.add(textContainer);
            textContainer.contextPut("canCreateOLATPassword", canCreateOLATPassword);

            userPropertyHandlers = getUserService().getUserPropertyHandlersFor(usageIdentifyer, true);
            excelMapper = createMapper(ureq);
            final String mapperURI = registerMapper(excelMapper);
            textContainer.contextPut("mapperURI", mapperURI);

            // get mandatory user-properties and set as text
            setTranslator(getUserService().getUserPropertiesConfig().getTranslator(getTranslator()));
            UserPropertyHandler userPropertyHandler;
            String mandatoryProperties = "";
            for (int i = 0; i < userPropertyHandlers.size(); i++) {
                userPropertyHandler = userPropertyHandlers.get(i);
                String mandatoryChar = "";
                if (getUserService().isMandatoryUserProperty(usageIdentifyer, userPropertyHandler)) {
                    mandatoryChar = " *";
                }
                mandatoryProperties += ", " + translate(userPropertyHandler.i18nColumnDescriptorLabelKey()) + mandatoryChar;
            }
            textContainer.contextPut("mandatoryProperties", mandatoryProperties);

            textAreaElement = uifactory.addTextAreaElement("importform", "form.importdata", -1, 10, 100, false, "", formLayout);
            textAreaElement.setMandatory(true);
            textAreaElement.setNotEmptyCheck("error.emptyform");
        }

        /**
         * creates a Mapper which generates an import example file for Excel
         * 
         * @return
         */
        private Mapper createMapper(final UserRequest ureq) {
            final String charset = getUserService().getUserCharset(ureq.getIdentity());
            final Mapper m = new Mapper() {
                @Override
                @SuppressWarnings({ "unused", "synthetic-access" })
                public MediaResource handle(final String relPath, final HttpServletRequest request) {
                    setTranslator(getUserService().getUserPropertiesConfig().getTranslator(getTranslator()));
                    String headerLine = translate("table.user.login") + " *";
                    String dataLine = "demo";
                    if (canCreateOLATPassword) {
                        headerLine += "\t" + translate("table.user.pwd");
                        dataLine += "\t" + "olat4you";
                    }
                    headerLine += "\t" + translate("table.user.lang");
                    dataLine += "\t" + I18nManager.getInstance().getLocaleKey(getLocale());
                    UserPropertyHandler userPropertyHandler;
                    for (int i = 0; i < userPropertyHandlers.size(); i++) {
                        userPropertyHandler = userPropertyHandlers.get(i);
                        String mandatoryChar = "";
                        if (getUserService().isMandatoryUserProperty(usageIdentifyer, userPropertyHandler)) {
                            mandatoryChar = " *";
                        }
                        headerLine += "\t" + translate(userPropertyHandler.i18nColumnDescriptorLabelKey()) + mandatoryChar;
                        dataLine += "\t" + translate("import.example." + userPropertyHandler.getName());
                    }
                    final String writeToFile = headerLine + "\n" + dataLine;

                    final ExcelMediaResource emr = new ExcelMediaResource(writeToFile, charset);
                    emr.setFilename("UserImportExample");
                    return emr;
                }
            };
            return m;
        }

    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
