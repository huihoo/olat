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

package org.olat.presentation.course.nodes.co;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.olat.data.group.context.BGContext;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.commons.mail.MailTemplateHelper;
import org.olat.lms.course.editor.CourseEditorEnv;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.presentation.course.condition.GroupOrAreaSelectionController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.FormLink;
import org.olat.presentation.framework.core.components.form.flexible.elements.SelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.SpacerElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.FormSubmit;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.group.BGControllerFactory;
import org.olat.presentation.group.NewAreaController;
import org.olat.presentation.group.NewBGController;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;
import org.olat.system.mail.MailHelper;

/**
 * Description:<BR/>
 * Configuration form for the contact form building block Initial Date: Oct 13, 2004
 * 
 * @author Felix Jost
 */
public class COConfigForm extends FormBasicController {

    private SelectionElement wantEmail;
    private TextElement teArElEmailToAdresses;

    private TextElement teElSubject;
    private TextElement teArElBody;

    private SelectionElement wantGroup;
    private SelectionElement coaches;
    private SelectionElement partips;
    private FormLayoutContainer coachesAndPartips;

    private SpacerElement s1, s2, s3, s4;

    private TextElement easyGroupTE;
    private FormLink chooseGroupsLink;
    private FormLink createGroupsLink;

    private TextElement easyAreaTE;
    private FormLink chooseAreasLink;
    private FormLink createAreasLink;

    private NewAreaController areaCreateCntrllr;
    private NewBGController groupCreateCntrllr;
    private GroupOrAreaSelectionController areaChooseC;
    private GroupOrAreaSelectionController groupChooseC;

    private FormLayoutContainer areaChooseSubContainer, groupChooseSubContainer;
    private FormItemContainer groupsAndAreasSubContainer;
    private FormItemContainer recipentsContainer;

    private FormLink fixGroupError;
    private FormLink fixAreaError;

    private FormSubmit subm;

    private boolean hasAreas;
    private boolean hasGroups;

    private CloseableModalController cmc;

    private List eList;
    private final ModuleConfiguration config;
    private final CourseEditorEnv cev;

    /**
     * Form constructor
     * 
     * @param name
     *            The form name
     * @param config
     *            The module configuration
     * @param withCancel
     *            true: cancel button is rendered, false: no cancel button
     */
    protected COConfigForm(final UserRequest ureq, final WindowControl wControl, final ModuleConfiguration config, final UserCourseEnvironment uce) {
        super(ureq, wControl);
        this.config = config;
        this.cev = uce.getCourseEditorEnv();
        initForm(ureq);
    }

    /**
	 */
    @Override
    protected boolean validateFormLogic(final UserRequest ureq) {
        if (!wantGroup.isSelected(0) && !wantEmail.isSelected(0)) {
            s3.setVisible(true);
            recipentsContainer.setErrorKey("no.recipents.specified", null);
            return false;
        }
        s3.setVisible(false);
        recipentsContainer.clearError();

        coachesAndPartips.clearError();
        if (wantGroup.isSelected(0)) {
            if (!coaches.isSelected(0) && !partips.isSelected(0)) {
                coachesAndPartips.setErrorKey("form.choose.coachesandpartips", null);
            }
            if (!validateGroupFields()) {
                return false;
            }
        }

        /*
         * somehow e-mail recipients must be specified, checking each of the possibility, at least one must be configured resulting in some e-mails. The case that the
         * specified groups can contain zero members must be handled by the e-mail controller!
         */

        final String emailToAdresses = teArElEmailToAdresses.getValue();
        final String[] emailAdress = emailToAdresses.split("\\s*\\r?\\n\\s*");

        teArElEmailToAdresses.clearError();
        if (wantEmail.isSelected(0) && (emailAdress == null || emailAdress.length == 0 || "".equals(emailAdress[0]))) {
            // otherwise the entry field shows that no e-mails are specified
            teArElEmailToAdresses.setErrorKey("email.not.specified", null);
            return false;
        }

        /*
         * check validity of manually provided e-mails
         */

        if ((emailAdress != null) && (emailAdress.length > 0) && (!"".equals(emailAdress[0]))) {
            this.eList = new ArrayList();
            for (int i = 0; i < emailAdress.length; i++) {
                final String eAd = emailAdress[i].trim();
                final boolean emailok = MailHelper.isValidEmailAddress(eAd);
                if (emailok == false) {
                    teArElEmailToAdresses.setErrorKey("email.not.valid", null);
                    return false;
                }
                eList.add(eAd);
            }
        }
        return true;
    }

    private boolean validateGroupFields() {
        boolean retVal = true;
        String[] activeGroupSelection = new String[0];
        String[] activeAreaSelection = new String[0];

        if (!easyGroupTE.isEmpty()) {
            // check whether groups exist
            activeGroupSelection = easyGroupTE.getValue().split(",");
            boolean exists = false;
            final Set<String> missingGroups = new HashSet<String>();

            for (int i = 0; i < activeGroupSelection.length; i++) {
                final String trimmed = activeGroupSelection[i].trim();
                exists = cev.existsGroup(trimmed);
                if (!exists && trimmed.length() > 0 && !missingGroups.contains(trimmed)) {
                    missingGroups.add(trimmed);
                }
            }

            if (missingGroups.size() > 0) {
                retVal = false;
                final String labelKey = missingGroups.size() == 1 ? "error.notfound.name" : "error.notfound.names";
                final String csvMissGrps = StringHelper.formatAsCSVString(missingGroups);
                final String[] params = new String[] { "-", csvMissGrps };

                /*
                 * create error with link to fix it
                 */
                final String vc_errorPage = velocity_root + "/erroritem.html";
                final FormLayoutContainer errorGroupItemLayout = FormLayoutContainer.createCustomFormLayout("errorgroupitem", getTranslator(), vc_errorPage);

                final boolean hasDefaultContext = getDefaultBGContext() != null;
                if (hasDefaultContext) {
                    groupChooseSubContainer.setErrorComponent(errorGroupItemLayout, this.flc);
                    // FIXING LINK ONLY IF A DEFAULTCONTEXT EXISTS
                    fixGroupError = new FormLinkImpl("error.fix", "create");
                    // link
                    fixGroupError.setCustomEnabledLinkCSS("b_button");
                    errorGroupItemLayout.add(fixGroupError);

                    fixGroupError.setErrorKey(labelKey, params);
                    fixGroupError.showError(true);
                    fixGroupError.showLabel(false);
                    // hinty to pass the information if one group is
                    // missing or if 2 or more groups are missing
                    // (see fixGroupErrer.getUserObject to understand)
                    // e.g. if userobject String[].lenght == 1 -> one group only
                    // String[].lenght > 1 -> show bulkmode creation group
                    if (missingGroups.size() > 1) {
                        fixGroupError.setUserObject(new String[] { csvMissGrps, "dummy" });
                    } else {
                        fixGroupError.setUserObject(new String[] { csvMissGrps });
                    }
                } else {
                    // fix helper link not possible -> errortext only
                    groupChooseSubContainer.setErrorKey(labelKey, params);
                }
                /*
				 * 
				 */
                groupChooseSubContainer.showError(true);
            } else {
                // no more errors
                groupChooseSubContainer.clearError();
            }
        }
        if (!easyAreaTE.isEmpty()) {
            // check whether areas exist
            activeAreaSelection = easyAreaTE.getValue().split(",");
            boolean exists = false;
            final Set<String> missingAreas = new HashSet<String>();
            for (int i = 0; i < activeAreaSelection.length; i++) {
                final String trimmed = activeAreaSelection[i].trim();
                exists = cev.existsArea(trimmed);
                if (!exists && trimmed.length() > 0 && !missingAreas.contains(trimmed)) {
                    missingAreas.add(trimmed);
                }
            }
            if (missingAreas.size() > 0) {
                retVal = false;
                final String labelKey = missingAreas.size() == 1 ? "error.notfound.name" : "error.notfound.names";
                final String csvMissAreas = StringHelper.formatAsCSVString(missingAreas);
                final String[] params = new String[] { "-", csvMissAreas };

                /*
                 * create error with link to fix it
                 */
                final String vc_errorPage = velocity_root + "/erroritem.html";
                final FormLayoutContainer errorAreaItemLayout = FormLayoutContainer.createCustomFormLayout("errorareaitem", getTranslator(), vc_errorPage);

                final boolean hasDefaultContext = getDefaultBGContext() != null;
                if (hasDefaultContext) {
                    areaChooseSubContainer.setErrorComponent(errorAreaItemLayout, this.flc);
                    // FXINGIN LINK ONLY IF DEFAULT CONTEXT EXISTS
                    fixAreaError = new FormLinkImpl("error.fix", "create");// erstellen
                    // link
                    fixAreaError.setCustomEnabledLinkCSS("b_button");
                    errorAreaItemLayout.add(fixAreaError);

                    fixAreaError.setErrorKey(labelKey, params);
                    fixAreaError.showError(true);
                    fixAreaError.showLabel(false);

                    // hint to pass the information if one area is
                    // missing or if 2 or more areas are missing
                    // (see fixGroupErrer.getUserObject to understand)
                    // e.g. if userobject String[].lenght == 1 -> one group only
                    // String[].lenght > 1 -> show bulkmode creation group
                    if (missingAreas.size() > 1) {
                        fixAreaError.setUserObject(new String[] { csvMissAreas, "dummy" });
                    } else {
                        fixAreaError.setUserObject(new String[] { csvMissAreas });
                    }
                } else {
                    // fixing help link not possible -> text only
                    areaChooseSubContainer.setErrorKey(labelKey, params);
                }

                areaChooseSubContainer.showError(true);
            } else {
                areaChooseSubContainer.clearError();
            }
        }

        final boolean easyGroupOK = (!easyGroupTE.isEmpty() && activeGroupSelection.length != 0);
        final boolean easyAreaOK = (!easyAreaTE.isEmpty() && activeAreaSelection.length != 0);
        if (easyGroupOK || easyAreaOK) {
            // clear general error
            this.flc.clearError();
        } else {
            // error concerns both fields -> set it as switch error
            this.groupsAndAreasSubContainer.setErrorKey("form.noGroupsOrAreas", null);
            retVal = false;
        }

        //
        final boolean needsGroupsOrAreas = coaches.isSelected(0) || partips.isSelected(0);
        if (needsGroupsOrAreas && !easyGroupOK && !easyAreaOK) {
            groupsAndAreasSubContainer.setErrorKey("form.noGroupsOrAreas", null);
        } else {
            groupsAndAreasSubContainer.clearError();
        }

        if (retVal) {
            areaChooseSubContainer.clearError();
            groupChooseSubContainer.clearError();
            groupsAndAreasSubContainer.clearError();
        }

        return retVal;
    }

    /**
     * @return the message subject
     */
    protected String getMSubject() {
        return teElSubject.getValue();
    }

    /**
     * @return the meesage body
     */
    protected String getMBody() {
        return teArElBody.getValue();
    }

    /**
     * @return the email list
     */
    protected List getEmailList() {
        return eList;
    }

    /**
     * returns the choosen groups, or null if no groups were choosen.
     * 
     * @return
     */
    protected String getEmailGroups() {
        if (StringHelper.containsNonWhitespace(easyGroupTE.getValue())) {
            return easyGroupTE.getValue();
        }
        return null;
    }

    /**
     * returns the choosen learning areas, or null if no ares were choosen.
     */
    protected String getEmailAreas() {
        if (StringHelper.containsNonWhitespace(easyAreaTE.getValue())) {
            return easyAreaTE.getValue();
        }
        return null;
    }

    protected boolean sendToCoaches() {
        return coaches.isSelected(0);
    }

    protected boolean sendToPartips() {
        return partips.isSelected(0);
    }

    /*
     * find default context if one is present
     */
    private BGContext getDefaultBGContext() {
        final CourseGroupManager courseGrpMngr = cev.getCourseGroupManager();
        final List courseLGContextes = courseGrpMngr.getLearningGroupContexts(cev.getCourseOlatResourceable());
        for (final Iterator iter = courseLGContextes.iterator(); iter.hasNext();) {
            final BGContext bctxt = (BGContext) iter.next();
            if (bctxt.isDefaultContext()) {
                return bctxt;
            }
        }
        return null;
        // not found! -> disable easy creation of groups! (no workflows for choosing
        // contexts

    }

    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        setFormTitle("header", null);
        setFormContextHelp("org.olat.presentation.course.nodes.co", "ced-co.html", "help.hover.co");

        wantGroup = uifactory.addCheckboxesVertical("wantGroup", "message.want.group", formLayout, new String[] { "xx" }, new String[] { null }, null, 1);
        wantGroup.addActionListener(this, FormEvent.ONCLICK);

        coaches = uifactory.addCheckboxesVertical("coaches", "form.message.chckbx.coaches", formLayout, new String[] { "xx" }, new String[] { null }, null, 1);
        coaches.select("xx", config.getBooleanEntry(COEditController.CONFIG_KEY_EMAILTOCOACHES));
        partips = uifactory.addCheckboxesVertical("partips", "form.message.chckbx.partips", formLayout, new String[] { "xx" }, new String[] { null }, null, 1);
        partips.select("xx", config.getBooleanEntry(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS));

        wantGroup.select("xx", coaches.isSelected(0) || partips.isSelected(0));

        coachesAndPartips = FormLayoutContainer.createHorizontalFormLayout("coachesAndPartips", getTranslator());
        formLayout.add(coachesAndPartips);
        s1 = uifactory.addSpacerElement("s1", formLayout, true);

        // groups
        groupChooseSubContainer = FormLayoutContainer.createHorizontalFormLayout("groupChooseSubContainer", getTranslator());
        groupChooseSubContainer.setLabel("form.message.group", null);

        formLayout.add(groupChooseSubContainer);
        final String groupInitVal = (String) config.get(COEditController.CONFIG_KEY_EMAILTOGROUPS);
        easyGroupTE = uifactory.addTextElement("group", null, 1024, groupInitVal, groupChooseSubContainer);
        easyGroupTE.setDisplaySize(24);
        easyGroupTE.setExampleKey("form.message.example.group", null);

        chooseGroupsLink = uifactory.addFormLink("choose", groupChooseSubContainer, "b_form_groupchooser");
        createGroupsLink = uifactory.addFormLink("create", groupChooseSubContainer, "b_form_groupchooser");

        hasGroups = cev.getCourseGroupManager().getAllLearningGroupsFromAllContexts(cev.getCourseOlatResourceable()).size() > 0;

        // areas
        areaChooseSubContainer = FormLayoutContainer.createHorizontalFormLayout("areaChooseSubContainer", getTranslator());
        areaChooseSubContainer.setLabel("form.message.area", null);
        formLayout.add(areaChooseSubContainer);

        groupsAndAreasSubContainer = FormLayoutContainer.createHorizontalFormLayout("groupSubContainer", getTranslator());
        formLayout.add(groupsAndAreasSubContainer);

        final String areaInitVal = (String) config.get(COEditController.CONFIG_KEY_EMAILTOAREAS);
        easyAreaTE = uifactory.addTextElement("area", null, 1024, areaInitVal, areaChooseSubContainer);
        easyAreaTE.setDisplaySize(24);
        easyAreaTE.setExampleKey("form.message.example.area", null);

        chooseAreasLink = uifactory.addFormLink("choose", areaChooseSubContainer, "b_form_groupchooser");
        createAreasLink = uifactory.addFormLink("create", areaChooseSubContainer, "b_form_groupchooser");

        hasAreas = cev.getCourseGroupManager().getAllAreasFromAllContexts(cev.getCourseOlatResourceable()).size() > 0;

        s2 = uifactory.addSpacerElement("s2", formLayout, false);

        wantEmail = uifactory.addCheckboxesVertical("wantEmail", "message.want.email", formLayout, new String[] { "xx" }, new String[] { null }, null, 1);
        wantEmail.addActionListener(this, FormEvent.ONCLICK);

        // recipients
        this.eList = (List) config.get(COEditController.CONFIG_KEY_EMAILTOADRESSES);
        String emailToAdresses = "";
        if (eList != null) {
            emailToAdresses = MailTemplateHelper.formatIdentitesAsEmailToString(eList, "\n");
            wantEmail.select("xx", eList.size() > 0);
        }
        teArElEmailToAdresses = uifactory.addTextAreaElement("email", "message.emailtoadresses", -1, 3, 60, true, emailToAdresses, formLayout);
        teArElEmailToAdresses.setMandatory(true);

        s3 = uifactory.addSpacerElement("s3", formLayout, true);

        // for displaying error message in case neither group stuff nor email is selected
        recipentsContainer = FormLayoutContainer.createHorizontalFormLayout("recipents", getTranslator());
        formLayout.add(recipentsContainer);

        s4 = uifactory.addSpacerElement("s4", formLayout, false);

        // subject
        final String mS = (String) config.get(COEditController.CONFIG_KEY_MSUBJECT_DEFAULT);
        final String mSubject = (mS != null) ? mS : "";
        teElSubject = uifactory.addTextElement("mSubject", "message.subject", 255, mSubject, formLayout);

        // messagebody
        final String mB = (String) config.get(COEditController.CONFIG_KEY_MBODY_DEFAULT);
        final String mBody = (mB != null) ? mB : "";
        teArElBody = uifactory.addTextAreaElement("mBody", "message.body", 10000, 8, 60, true, mBody, formLayout);

        subm = uifactory.addFormSubmitButton("save", formLayout);

        update();
    }

    private void update() {

        final boolean wg = wantGroup.isSelected(0);
        coaches.setVisible(wg);
        partips.setVisible(wg);
        coachesAndPartips.setVisible(wg);

        groupChooseSubContainer.setVisible(wg);
        areaChooseSubContainer.setVisible(wg);

        s1.setVisible(wg);
        s2.setVisible(wg);

        if (!wg) {
            coaches.select("xx", false);
            partips.select("xx", false);
            easyAreaTE.setValue("");
            easyGroupTE.setValue("");
        }

        hasGroups = cev.getCourseGroupManager().getAllLearningGroupsFromAllContexts(cev.getCourseOlatResourceable()).size() > 0;
        chooseGroupsLink.setVisible(wg && hasGroups);
        createGroupsLink.setVisible(wg && !hasGroups);

        hasAreas = cev.getCourseGroupManager().getAllAreasFromAllContexts(cev.getCourseOlatResourceable()).size() > 0;
        chooseAreasLink.setVisible(wg && hasAreas);
        createAreasLink.setVisible(wg && !hasAreas);

        teArElEmailToAdresses.setVisible(wantEmail.isSelected(0));
        teArElEmailToAdresses.clearError();
        if (!wantEmail.isSelected(0)) {
            teArElEmailToAdresses.setValue("");
            eList = null;
        }

        recipentsContainer.clearError();
    }

    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {

        if (source == chooseGroupsLink) {

            removeAsListenerAndDispose(groupChooseC);
            groupChooseC = new GroupOrAreaSelectionController(cev.getCourseOlatResourceable(), 0, getWindowControl(), ureq, "group", cev.getCourseGroupManager(),
                    easyGroupTE.getValue());
            listenTo(groupChooseC);

            removeAsListenerAndDispose(cmc);
            cmc = new CloseableModalController(getWindowControl(), "close", groupChooseC.getInitialComponent());
            listenTo(cmc);

            cmc.activate();
            subm.setEnabled(false);

        } else if (source == createGroupsLink) {
            // no groups in group management -> directly show group create dialog
            final String[] csvGroupName = easyGroupTE.isEmpty() ? new String[0] : easyGroupTE.getValue().split(",");

            removeAsListenerAndDispose(groupCreateCntrllr);
            groupCreateCntrllr = BGControllerFactory.getInstance().createNewBGController(ureq, getWindowControl(), true, getDefaultBGContext(), true,
                    easyGroupTE.getValue());
            listenTo(groupCreateCntrllr);

            removeAsListenerAndDispose(cmc);
            cmc = new CloseableModalController(getWindowControl(), "close", groupCreateCntrllr.getInitialComponent());
            listenTo(cmc);

            cmc.activate();
            subm.setEnabled(false);

        } else if (source == chooseAreasLink) {

            // already areas -> choose areas
            removeAsListenerAndDispose(areaChooseC);
            areaChooseC = new GroupOrAreaSelectionController(cev.getCourseOlatResourceable(), 1, getWindowControl(), ureq, "area", cev.getCourseGroupManager(),
                    easyAreaTE.getValue());
            listenTo(areaChooseC);

            removeAsListenerAndDispose(cmc);
            cmc = new CloseableModalController(getWindowControl(), "close", areaChooseC.getInitialComponent());
            listenTo(cmc);

            cmc.activate();
            subm.setEnabled(false);

        } else if (source == createAreasLink) {
            // no areas -> directly show creation dialog
            final BGContext bgContext = getDefaultBGContext();

            removeAsListenerAndDispose(areaCreateCntrllr);
            areaCreateCntrllr = BGControllerFactory.getInstance().createNewAreaController(ureq, getWindowControl(), bgContext, true, easyAreaTE.getValue());
            listenTo(areaCreateCntrllr);

            removeAsListenerAndDispose(cmc);
            cmc = new CloseableModalController(getWindowControl(), "close", areaCreateCntrllr.getInitialComponent());
            listenTo(cmc);

            cmc.activate();
            subm.setEnabled(false);

        } else if (source == fixGroupError) {
            /*
             * user wants to fix problem with fixing group error link e.g. create one or more group at once.
             */
            final BGContext bgContext = getDefaultBGContext();

            final String[] csvGroupName = (String[]) fixGroupError.getUserObject();

            easyGroupTE.setEnabled(false);
            removeAsListenerAndDispose(groupCreateCntrllr);
            groupCreateCntrllr = BGControllerFactory.getInstance().createNewBGController(ureq, getWindowControl(), true, bgContext, true, csvGroupName[0]);
            listenTo(groupCreateCntrllr);

            removeAsListenerAndDispose(cmc);
            cmc = new CloseableModalController(getWindowControl(), "close", groupCreateCntrllr.getInitialComponent());
            listenTo(cmc);

            cmc.activate();
            subm.setEnabled(false);

        } else if (source == fixAreaError) {
            /*
             * user wants to fix problem with fixing area error link e.g. create one or more areas at once.
             */
            final BGContext bgContext = getDefaultBGContext();
            final String[] csvAreaName = (String[]) fixAreaError.getUserObject();

            easyAreaTE.setEnabled(false);
            removeAsListenerAndDispose(areaCreateCntrllr);
            areaCreateCntrllr = BGControllerFactory.getInstance().createNewAreaController(ureq, getWindowControl(), bgContext, true, csvAreaName[0]);
            listenTo(areaCreateCntrllr);

            removeAsListenerAndDispose(cmc);
            cmc = new CloseableModalController(getWindowControl(), "close", areaCreateCntrllr.getInitialComponent());
            listenTo(cmc);

            cmc.activate();
            subm.setEnabled(false);

        }

        update();
    }

    @Override
    @SuppressWarnings("unused")
    protected void event(final UserRequest ureq, final Controller source, final Event event) {

        subm.setEnabled(true);

        if (source == groupChooseC) {
            if (event == Event.DONE_EVENT) {
                cmc.deactivate();
                easyGroupTE.setValue(StringHelper.formatAsSortUniqCSVString(groupChooseC.getSelectedEntries()));
                easyGroupTE.getRootForm().submit(ureq);

            } else if (Event.CANCELLED_EVENT == event) {
                cmc.deactivate();
                return;

            } else if (event == Event.CHANGED_EVENT && !hasGroups) {
                // singleUserEventCenter.fireEventToListenersOf(new MultiUserEvent("changed"), groupConfigChangeEventOres);
                // why? fireEvent(ureq, new BGContextEvent(BGContextEvent.RESOURCE_ADDED,getDefaultBGContext()));
            }

        } else if (source == areaChooseC) {
            if (event == Event.DONE_EVENT) {
                cmc.deactivate();
                easyAreaTE.setValue(StringHelper.formatAsSortUniqCSVString(areaChooseC.getSelectedEntries()));
                easyAreaTE.getRootForm().submit(ureq);

            } else if (event == Event.CANCELLED_EVENT) {
                cmc.deactivate();
                return;

            } else if (event == Event.CHANGED_EVENT && !hasAreas) {
                // singleUserEventCenter.fireEventToListenersOf(new MultiUserEvent("changed"), groupConfigChangeEventOres);
                // why? fireEvent(ureq, new BGContextEvent(BGContextEvent.RESOURCE_ADDED,getDefaultBGContext()));
            }

        } else if (source == groupCreateCntrllr) {

            easyGroupTE.setEnabled(true);
            cmc.deactivate();

            if (event == Event.DONE_EVENT) {

                final List<String> c = new ArrayList<String>();
                c.addAll(Arrays.asList(easyGroupTE.getValue().split(",")));
                if (fixGroupError != null && fixGroupError.getUserObject() != null) {
                    c.removeAll(Arrays.asList((String[]) fixGroupError.getUserObject()));
                }
                c.addAll(groupCreateCntrllr.getCreatedGroupNames());

                easyGroupTE.setValue(StringHelper.formatAsSortUniqCSVString(c));

                if (groupCreateCntrllr.getCreatedGroupNames().size() > 0 && !hasGroups) {
                    chooseGroupsLink.setVisible(true);
                    createGroupsLink.setVisible(false);
                    // singleUserEventCenter.fireEventToListenersOf(new MultiUserEvent("changed"), groupConfigChangeEventOres);
                }

                easyGroupTE.getRootForm().submit(ureq);
            }

        } else if (source == areaCreateCntrllr) {

            easyAreaTE.setEnabled(true);
            cmc.deactivate();

            if (event == Event.DONE_EVENT) {
                final List<String> c = new ArrayList<String>();
                c.addAll(Arrays.asList(easyAreaTE.getValue().split(",")));
                if (fixAreaError != null && fixAreaError.getUserObject() != null) {
                    c.removeAll(Arrays.asList((String[]) fixAreaError.getUserObject()));
                }
                c.addAll(areaCreateCntrllr.getCreatedAreaNames());

                easyAreaTE.setValue(StringHelper.formatAsSortUniqCSVString(c));

                if (areaCreateCntrllr.getCreatedAreaNames().size() > 0 && !hasAreas) {
                    chooseAreasLink.setVisible(true);
                    createAreasLink.setVisible(false);
                    // singleUserEventCenter.fireEventToListenersOf(new MultiUserEvent("changed"), groupConfigChangeEventOres);
                }
                easyAreaTE.getRootForm().submit(ureq);
            }
        }
    }

    @Override
    protected void doDispose() {
        //
    }
}
