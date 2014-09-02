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

package org.olat.presentation.course.nodes.cp;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.commons.fileresource.ImsCPFileResource;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.AssessmentHelper;
import org.olat.lms.course.condition.Condition;
import org.olat.lms.course.nodes.CPCourseNode;
import org.olat.lms.course.nodes.CourseNodeFactory;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.presentation.course.condition.ConditionEditController;
import org.olat.presentation.course.editor.NodeEditController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.SelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.SingleSelection;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPane;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.ControllerEventListener;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsPreviewController;
import org.olat.presentation.ims.cp.CPUIFactory;
import org.olat.presentation.repository.ReferencableEntriesSearchController;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<BR/>
 * Edit controller for content packaging course nodes
 * <P/>
 * Initial Date: Oct 13, 2004
 * 
 * @author Felix Jost
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class CPEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

    public static final String PANE_TAB_CPCONFIG = "pane.tab.cpconfig";
    private static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";

    private static final String VC_CHOSENCP = "chosencp";

    // NLS support:
    private static final String NLS_ERROR_CPREPOENTRYMISSING = "error.cprepoentrymissing";
    private static final String NLS_NO_CP_CHOSEN = "no.cp.chosen";
    private static final String NLS_CONDITION_ACCESSIBILITY_TITLE = "condition.accessibility.title";
    private static final String NLS_COMMAND_CHOOSECP = "command.choosecp";
    private static final String NLS_COMMAND_CREATECP = "command.createcp";
    private static final String NLS_COMMAND_CHANGECP = "command.changecp";

    private final Panel main;
    private final VelocityContainer cpConfigurationVc;

    private final ModuleConfiguration config;
    private ReferencableEntriesSearchController searchController;

    private final ConditionEditController accessibilityCondContr;
    private final CPCourseNode cpNode;
    private final CompMenuForm cpMenuForm;

    private TabbedPane myTabbedPane;

    final static String[] paneKeys = { PANE_TAB_CPCONFIG, PANE_TAB_ACCESSIBILITY };

    private Link previewLink;
    private Link editLink;
    private final Link chooseCPButton;
    private final Link changeCPButton;

    private LayoutMain3ColsPreviewController previewCtr;
    private CloseableModalController cmc;

    /**
     * @param cpNode
     * @param ureq
     * @param wControl
     * @param course
     */
    public CPEditController(final CPCourseNode cpNode, final UserRequest ureq, final WindowControl wControl, final ICourse course, final UserCourseEnvironment euce) {
        super(ureq, wControl);
        this.cpNode = cpNode;
        this.config = cpNode.getModuleConfiguration();

        main = new Panel("cpmain");

        cpConfigurationVc = this.createVelocityContainer("edit");
        chooseCPButton = LinkFactory.createButtonSmall(NLS_COMMAND_CREATECP, cpConfigurationVc, this);
        changeCPButton = LinkFactory.createButtonSmall(NLS_COMMAND_CHANGECP, cpConfigurationVc, this);

        if (config.get(ModuleConfiguration.CONFIG_KEY_REPOSITORY_REF) != null) {
            // fetch repository entry to display the repository entry title of the chosen cp
            final RepositoryEntry re = cpNode.getReferencedRepositoryEntry();
            if (re == null) { // we cannot display the entries name, because the
                // repository entry had been deleted between the time when it was chosen here, and now
                this.showError(NLS_ERROR_CPREPOENTRYMISSING);
                cpConfigurationVc.contextPut("showPreviewButton", Boolean.FALSE);
                cpConfigurationVc.contextPut(VC_CHOSENCP, translate("no.cp.chosen"));
            } else {
                if (getBaseSecurityEBL().isRepoEntryEditable(ureq.getIdentity(), re)) {
                    editLink = LinkFactory.createButtonSmall("edit", cpConfigurationVc, this);
                }
                cpConfigurationVc.contextPut("showPreviewButton", Boolean.TRUE);
                previewLink = LinkFactory.createCustomLink("command.preview", "command.preview", re.getDisplayname(), Link.NONTRANSLATED, cpConfigurationVc, this);
                previewLink.setCustomEnabledLinkCSS("b_preview");
                previewLink.setTitle(getTranslator().translate("command.preview"));
            }
        } else {
            // no valid config yet
            cpConfigurationVc.contextPut("showPreviewButton", Boolean.FALSE);
            cpConfigurationVc.contextPut(VC_CHOSENCP, translate(NLS_NO_CP_CHOSEN));
        }

        final boolean cpMenu = config.getBooleanEntry(NodeEditController.CONFIG_COMPONENT_MENU);
        final String contentEncoding = (String) config.get(NodeEditController.CONFIG_CONTENT_ENCODING);
        final String jsEncoding = (String) config.get(NodeEditController.CONFIG_JS_ENCODING);
        cpMenuForm = new CompMenuForm(ureq, wControl, cpMenu, contentEncoding, jsEncoding);
        listenTo(cpMenuForm);

        cpConfigurationVc.put("cpMenuForm", cpMenuForm.getInitialComponent());

        // Accessibility precondition
        final Condition accessCondition = cpNode.getPreConditionAccess();
        accessibilityCondContr = new ConditionEditController(ureq, getWindowControl(), course.getCourseEnvironment().getCourseGroupManager(), accessCondition,
                "accessabilityConditionForm", AssessmentHelper.getAssessableNodes(course.getEditorTreeModel(), cpNode), euce);
        listenTo(accessibilityCondContr);

        main.setContent(cpConfigurationVc);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == chooseCPButton || source == changeCPButton) {
            removeAsListenerAndDispose(searchController);
            searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq, ImsCPFileResource.TYPE_NAME, translate(NLS_COMMAND_CHOOSECP));
            listenTo(searchController);

            removeAsListenerAndDispose(cmc);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), searchController.getInitialComponent(), true, translate(NLS_COMMAND_CREATECP));
            listenTo(cmc);
            cmc.activate();

        } else if (source == previewLink) {
            // Preview as modal dialogue only if the config is valid
            final RepositoryEntry re = cpNode.getReferencedRepositoryEntry();
            if (re == null) { // we cannot preview it, because the repository entry
                // had been deleted between the time when it was chosen here, and now
                showError(NLS_ERROR_CPREPOENTRYMISSING);
            } else {
                final Boolean showMenuB = config.getBooleanEntry(NodeEditController.CONFIG_COMPONENT_MENU);
                // pre: showMenuB != null
                removeAsListenerAndDispose(previewCtr);
                previewCtr = CPUIFactory.getInstance().createMainLayoutPreviewController(ureq, getWindowControl(),
                        getLocalFolderFactoryEBL().getLocalFolderImplForOlatResource(re.getOlatResource()), showMenuB.booleanValue());
                previewCtr.activate();
            }
        } else if (source == editLink) {
            CourseNodeFactory.getInstance().launchReferencedRepoEntryEditor(ureq, cpNode);
        }
    }

    private LocalFolderFactoryEBL getLocalFolderFactoryEBL() {
        return CoreSpringFactory.getBean(LocalFolderFactoryEBL.class);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest urequest, final Controller source, final Event event) {
        if (source == searchController) {
            if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
                // search controller done
                // -> close closeable modal controller
                cmc.deactivate();
                final RepositoryEntry re = searchController.getSelectedEntry();
                if (re != null) {
                    cpNode.setRepositoryReference(re);
                    cpConfigurationVc.contextPut("showPreviewButton", Boolean.TRUE);
                    previewLink = LinkFactory.createCustomLink("command.preview", "command.preview", re.getDisplayname(), Link.NONTRANSLATED, cpConfigurationVc, this);
                    previewLink.setCustomEnabledLinkCSS("b_preview");
                    previewLink.setTitle(getTranslator().translate("command.preview"));
                    // remove existing edit link, add new one if user is allowed to edit this CP
                    if (editLink != null) {
                        cpConfigurationVc.remove(editLink);
                        editLink = null;
                    }
                    if (getBaseSecurityEBL().isRepoEntryEditable(urequest.getIdentity(), re)) {
                        editLink = LinkFactory.createButtonSmall("edit", cpConfigurationVc, this);
                    }
                    // fire event so the updated config is saved by the editormaincontroller
                    fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
                }
            }
            // else cancelled repo search
        } else if (source == accessibilityCondContr) {
            if (event == Event.CHANGED_EVENT) {
                final Condition cond = accessibilityCondContr.getCondition();
                cpNode.setPreConditionAccess(cond);
                fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        } else if (source == cpMenuForm) {
            if (event == Event.DONE_EVENT) {
                config.setBooleanEntry(NodeEditController.CONFIG_COMPONENT_MENU, cpMenuForm.isCpMenu());
                config.set(NodeEditController.CONFIG_CONTENT_ENCODING, cpMenuForm.getContentEncoding());
                config.set(NodeEditController.CONFIG_JS_ENCODING, cpMenuForm.getJSEncoding());

                fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        }
    }

    private BaseSecurityEBL getBaseSecurityEBL() {
        return (BaseSecurityEBL) CoreSpringFactory.getBean(BaseSecurityEBL.class);
    }

    /**
	 */
    @Override
    public void addTabs(final TabbedPane tabbedPane) {
        myTabbedPane = tabbedPane;

        tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), accessibilityCondContr.getWrappedDefaultAccessConditionVC(translate(NLS_CONDITION_ACCESSIBILITY_TITLE)));
        tabbedPane.addTab(translate(PANE_TAB_CPCONFIG), main);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // child controllers registered with listenTo() get disposed in BasicController
        if (previewCtr != null) {
            previewCtr.dispose();
            previewCtr = null;
        }
    }

    @Override
    public String[] getPaneKeys() {
        return paneKeys;
    }

    @Override
    public TabbedPane getTabbedPane() {
        return myTabbedPane;
    }

}

class CompMenuForm extends FormBasicController {

    /**
     * Simple form for asking whether component menu should be shown or not.
     * 
     * @author Lars Eberle (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
     */

    // NLS support:
    private static final String NLS_DISPLAY_CONFIG_COMPMENU = "display.config.compMenu";

    private SelectionElement cpMenu;
    private SingleSelection encodingContentEl;
    private SingleSelection encodingJSEl;

    private final boolean compMenuConfig;
    private final String contentEncoding;
    private final String jsEncoding;

    private final String[] encodingContentKeys, encodingContentValues;
    private final String[] encodingJSKeys, encodingJSValues;

    CompMenuForm(final UserRequest ureq, final WindowControl wControl, final Boolean compMenuConfig, final String contentEncoding, final String jsEncoding) {
        super(ureq, wControl);
        this.compMenuConfig = compMenuConfig == null ? true : compMenuConfig.booleanValue();
        this.contentEncoding = contentEncoding;
        this.jsEncoding = jsEncoding;

        final Map<String, Charset> charsets = Charset.availableCharsets();
        final int numOfCharsets = charsets.size() + 1;

        encodingContentKeys = new String[numOfCharsets];
        encodingContentKeys[0] = NodeEditController.CONFIG_CONTENT_ENCODING_AUTO;

        encodingContentValues = new String[numOfCharsets];
        encodingContentValues[0] = translate("encoding.auto");

        encodingJSKeys = new String[numOfCharsets];
        encodingJSKeys[0] = NodeEditController.CONFIG_JS_ENCODING_AUTO;

        encodingJSValues = new String[numOfCharsets];
        encodingJSValues[0] = translate("encoding.same");

        int count = 1;
        final Locale locale = ureq.getLocale();
        for (final Map.Entry<String, Charset> charset : charsets.entrySet()) {
            encodingContentKeys[count] = charset.getKey();
            encodingContentValues[count] = charset.getValue().displayName(locale);
            encodingJSKeys[count] = charset.getKey();
            encodingJSValues[count] = charset.getValue().displayName(locale);
            count++;
        }

        initForm(ureq);
    }

    public Object getJSEncoding() {
        return encodingJSEl.getSelectedKey();
    }

    public Object getContentEncoding() {
        return encodingContentEl.getSelectedKey();
    }

    public boolean isCpMenu() {
        return cpMenu.isSelected(0);
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        cpMenu = uifactory.addCheckboxesVertical("cpMenu", NLS_DISPLAY_CONFIG_COMPMENU, formLayout, new String[] { "xx" }, new String[] { null }, null, 1);
        cpMenu.select("xx", compMenuConfig);

        encodingContentEl = uifactory.addDropdownSingleselect("encoContent", "encoding.content", formLayout, encodingContentKeys, encodingContentValues, null);
        if (Arrays.asList(encodingContentKeys).contains(contentEncoding)) {
            encodingContentEl.select(contentEncoding, true);
        } else {
            encodingContentEl.select(NodeEditController.CONFIG_CONTENT_ENCODING_AUTO, true);
        }

        encodingJSEl = uifactory.addDropdownSingleselect("encoJS", "encoding.js", formLayout, encodingJSKeys, encodingJSValues, null);
        if (Arrays.asList(encodingJSKeys).contains(jsEncoding)) {
            encodingJSEl.select(jsEncoding, true);
        } else {
            encodingJSEl.select(NodeEditController.CONFIG_JS_ENCODING_AUTO, true);
        }

        uifactory.addFormSubmitButton("submit", formLayout);
    }

    @Override
    protected void doDispose() {
        //
    }
}
