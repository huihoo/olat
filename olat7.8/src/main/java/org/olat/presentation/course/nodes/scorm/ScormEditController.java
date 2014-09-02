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

package org.olat.presentation.course.nodes.scorm;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.commons.fileresource.ScormCPFileResource;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.AssessmentHelper;
import org.olat.lms.course.condition.Condition;
import org.olat.lms.course.nodes.ScormCourseNode;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.scorm.ScormConstants;
import org.olat.presentation.course.condition.ConditionEditController;
import org.olat.presentation.course.editor.NodeEditController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.IntegerElement;
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
import org.olat.presentation.repository.ReferencableEntriesSearchController;
import org.olat.presentation.scorm.ScormAPIandDisplayController;
import org.olat.system.event.Event;

/**
 * Description:<BR/>
 * Edit controller for content packaging course nodes
 * <P/>
 * Initial Date: Oct 13, 2004
 * 
 * @author Felix Jost
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class ScormEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

    public static final String PANE_TAB_CPCONFIG = "pane.tab.cpconfig";
    private static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";

    public static final String CONFIG_SHOWMENU = "showmenu";
    public static final String CONFIG_SHOWNAVBUTTONS = "shownavbuttons";
    public static final String CONFIG_ISASSESSABLE = "isassessable";
    public static final String CONFIG_CUTVALUE = "cutvalue";
    public static final String CONFIG_HEIGHT = "height";
    public final static String CONFIG_HEIGHT_AUTO = "auto";

    private static final String VC_CHOSENCP = "chosencp";

    private static final String[] paneKeys = { PANE_TAB_CPCONFIG, PANE_TAB_ACCESSIBILITY };

    // NLS support:

    private static final String NLS_ERROR_CPREPOENTRYMISSING = "error.cprepoentrymissing";
    private static final String NLS_NO_CP_CHOSEN = "no.cp.chosen";
    private static final String NLS_CONDITION_ACCESSIBILITY_TITLE = "condition.accessibility.title";

    private final Panel main;
    private final VelocityContainer cpConfigurationVc;

    private final ModuleConfiguration config;
    private ReferencableEntriesSearchController searchController;
    private CloseableModalController cmc;

    private final ConditionEditController accessibilityCondContr;
    private final ScormCourseNode scormNode;

    private LayoutMain3ColsPreviewController previewLayoutCtr;

    private TabbedPane myTabbedPane;

    private final ICourse course;

    private final VarForm scorevarform;

    private Link previewLink;
    private final Link chooseCPButton;
    private final Link changeCPButton;

    /**
     * @param cpNode
     *            CourseNode
     * @param ureq
     * @param wControl
     * @param course
     *            Course Interface
     * @param euce
     *            User course environment
     */
    public ScormEditController(final ScormCourseNode scormNode, final UserRequest ureq, final WindowControl wControl, final ICourse course,
            final UserCourseEnvironment euce) {
        super(ureq, wControl);
        // o_clusterOk by guido: save to hold reference to course inside editor
        this.course = course;
        this.scormNode = scormNode;
        this.config = scormNode.getModuleConfiguration();
        main = new Panel("cpmain");
        cpConfigurationVc = this.createVelocityContainer("edit");

        chooseCPButton = LinkFactory.createButtonSmall("command.importcp", cpConfigurationVc, this);
        changeCPButton = LinkFactory.createButtonSmall("command.changecp", cpConfigurationVc, this);

        if (config.get(ModuleConfiguration.CONFIG_KEY_REPOSITORY_REF) != null) {
            // fetch repository entry to display the repository entry title of the
            // chosen cp
            final RepositoryEntry re = scormNode.getReferencedRepositoryEntry();
            if (re == null) { // we cannot display the entries name, because the repository entry had been deleted
                              // between the time when it was chosen here, and now
                this.showError(NLS_ERROR_CPREPOENTRYMISSING);
                cpConfigurationVc.contextPut("showPreviewButton", Boolean.FALSE);
                cpConfigurationVc.contextPut(VC_CHOSENCP, translate(NLS_NO_CP_CHOSEN));
            } else {
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

        // add the form for choosing the score variable
        final boolean showMenu = config.getBooleanSafe(CONFIG_SHOWMENU, true);
        final boolean showNavButtons = config.getBooleanSafe(CONFIG_SHOWNAVBUTTONS, true);
        final boolean assessable = config.getBooleanSafe(CONFIG_ISASSESSABLE);
        final int cutvalue = config.getIntegerSafe(CONFIG_CUTVALUE, 0);
        final String height = (String) config.get(CONFIG_HEIGHT);
        final String encContent = (String) config.get(NodeEditController.CONFIG_CONTENT_ENCODING);
        final String encJS = (String) config.get(NodeEditController.CONFIG_JS_ENCODING);

        // = conf.get(CONFIG_CUTVALUE);
        scorevarform = new VarForm(ureq, wControl, showMenu, showNavButtons, height, encContent, encJS, assessable, cutvalue);
        listenTo(scorevarform);
        cpConfigurationVc.put("scorevarform", scorevarform.getInitialComponent());

        // Accessibility precondition
        final Condition accessCondition = scormNode.getPreConditionAccess();
        accessibilityCondContr = new ConditionEditController(ureq, getWindowControl(), course.getCourseEnvironment().getCourseGroupManager(), accessCondition,
                "accessabilityConditionForm", AssessmentHelper.getAssessableNodes(course.getEditorTreeModel(), scormNode), euce);
        this.listenTo(accessibilityCondContr);

        main.setContent(cpConfigurationVc);
    }

    /**
	 */
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == chooseCPButton || source == changeCPButton) { // those must be links
            removeAsListenerAndDispose(searchController);
            searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq, ScormCPFileResource.TYPE_NAME, translate("command.choosecp"));
            listenTo(searchController);
            removeAsListenerAndDispose(cmc);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), searchController.getInitialComponent(), true, translate("command.importcp"));
            listenTo(cmc);
            cmc.activate();
        } else if (source == previewLink) {
            // Preview as modal dialogue
            // only if the config is valid
            final RepositoryEntry re = scormNode.getReferencedRepositoryEntry();
            if (re == null) { // we cannot preview it, because the repository entry
                              // had been deleted between the time when it was
                              // chosen here, and now
                this.showError("error.cprepoentrymissing");
            } else {
                final boolean showMenu = config.getBooleanSafe(CONFIG_SHOWMENU, true);

                if (previewLayoutCtr != null) {
                    previewLayoutCtr.dispose();
                }
                ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapScormRepositoryEntry(re));
                final ScormAPIandDisplayController previewController = new ScormAPIandDisplayController(ureq, getWindowControl(), showMenu, null, re.getOlatResource(),
                        null, course.getResourceableId().toString(), ScormConstants.SCORM_MODE_BROWSE, ScormConstants.SCORM_MODE_NOCREDIT, true, true);
                // configure some display options
                final boolean showNavButtons = config.getBooleanSafe(ScormEditController.CONFIG_SHOWNAVBUTTONS, true);
                previewController.showNavButtons(showNavButtons);
                final String height = (String) config.get(ScormEditController.CONFIG_HEIGHT);
                if (!height.equals(ScormEditController.CONFIG_HEIGHT_AUTO)) {
                    previewController.setHeightPX(Integer.parseInt(height));
                }
                final String contentEncoding = (String) config.get(NodeEditController.CONFIG_CONTENT_ENCODING);
                if (!contentEncoding.equals(NodeEditController.CONFIG_CONTENT_ENCODING_AUTO)) {
                    previewController.setContentEncoding(contentEncoding);
                }
                final String jsEncoding = (String) config.get(NodeEditController.CONFIG_JS_ENCODING);
                if (!jsEncoding.equals(NodeEditController.CONFIG_JS_ENCODING_AUTO)) {
                    previewController.setJSEncoding(jsEncoding);
                }
            }
        }
    }

    /**
	 */
    protected void event(final UserRequest urequest, final Controller source, final Event event) {
        if (source == searchController) {
            cmc.deactivate();
            if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
                // search controller done
                final RepositoryEntry re = searchController.getSelectedEntry();
                if (re != null) {
                    scormNode.setRepositoryReference(re);
                    cpConfigurationVc.contextPut("showPreviewButton", Boolean.TRUE);
                    previewLink = LinkFactory.createCustomLink("command.preview", "command.preview", re.getDisplayname(), Link.NONTRANSLATED, cpConfigurationVc, this);
                    previewLink.setCustomEnabledLinkCSS("b_preview");
                    previewLink.setTitle(getTranslator().translate("command.preview"));
                    // fire event so the updated config is saved by the
                    // editormaincontroller
                    fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
                }
                // else cancelled repo search
            }
        } else if (source == accessibilityCondContr) {
            if (event == Event.CHANGED_EVENT) {
                final Condition cond = accessibilityCondContr.getCondition();
                scormNode.setPreConditionAccess(cond);
                fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        } else if (source == scorevarform) {
            if (event == Event.DONE_EVENT) {
                final boolean showmenu = scorevarform.isShowMenu();
                config.setBooleanEntry(CONFIG_SHOWMENU, showmenu);
                final boolean showNavButtons = scorevarform.isShowNavButtons();
                config.setBooleanEntry(CONFIG_SHOWNAVBUTTONS, showNavButtons);
                final boolean assessable = scorevarform.isAssessable();
                config.setBooleanEntry(CONFIG_ISASSESSABLE, assessable);
                final int cutvalue = scorevarform.getCutValue();
                config.setIntValue(CONFIG_CUTVALUE, cutvalue);
                final String height = scorevarform.getHeightValue();
                config.set(CONFIG_HEIGHT, height);
                final String encContent = scorevarform.getEncodingContentValue();
                config.set(NodeEditController.CONFIG_CONTENT_ENCODING, encContent);
                final String encJS = scorevarform.getEncodingJSValue();
                config.set(NodeEditController.CONFIG_JS_ENCODING, encJS);
                // fire event so the updated config is saved by the
                // editormaincontroller
                fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        }
    }

    /**
	 */
    public void addTabs(final TabbedPane tabbedPane) {
        myTabbedPane = tabbedPane;
        tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), accessibilityCondContr.getWrappedDefaultAccessConditionVC(translate(NLS_CONDITION_ACCESSIBILITY_TITLE)));
        tabbedPane.addTab(translate(PANE_TAB_CPCONFIG), main); // the choose learning content tab
    }

    /**
	 */
    protected void doDispose() {
        // child controllers registered with listenTo() get disposed in BasicController
        if (previewLayoutCtr != null) {
            previewLayoutCtr.dispose();
            previewLayoutCtr = null;
        }
    }

    public String[] getPaneKeys() {
        return paneKeys;
    }

    public TabbedPane getTabbedPane() {
        return myTabbedPane;
    }
}

class VarForm extends FormBasicController {
    private SelectionElement showMenuEl;
    private SelectionElement showNavButtonsEl;
    private SelectionElement isAssessableEl;
    private IntegerElement cutValueEl;
    private SingleSelection heightEl;
    private SingleSelection encodingContentEl;
    private SingleSelection encodingJSEl;

    private final boolean showMenu, showNavButtons, isAssessable;
    private final String height;
    private final String encodingContent;
    private final String encodingJS;
    private final int cutValue;
    private final String[] keys, values;
    private final String[] encodingContentKeys, encodingContentValues;
    private final String[] encodingJSKeys, encodingJSValues;

    /**
     * @param name
     *            Name of the form
     */
    public VarForm(final UserRequest ureq, final WindowControl wControl, final boolean showMenu, final boolean showNavButtons, final String height,
            final String encodingContent, final String encodingJS, final boolean isAssessable, final int cutValue) {
        super(ureq, wControl);
        this.showMenu = showMenu;
        this.showNavButtons = showNavButtons;
        this.isAssessable = isAssessable;
        this.cutValue = cutValue;
        this.height = height;
        this.encodingContent = encodingContent;
        this.encodingJS = encodingJS;

        keys = new String[] { ScormEditController.CONFIG_HEIGHT_AUTO, "460", "480", "500", "520", "540", "560", "580", "600", "620", "640", "660", "680", "700", "720",
                "730", "760", "780", "800", "820", "840", "860", "880", "900", "920", "940", "960", "980", "1000", "1020", "1040", "1060", "1080", "1100", "1120",
                "1140", "1160", "1180", "1200", "1220", "1240", "1260", "1280", "1300", "1320", "1340", "1360", "1380" };

        values = new String[] { translate("height.auto"), "460px", "480px", "500px", "520px", "540px", "560px", "580px", "600px", "620px", "640px", "660px", "680px",
                "700px", "720px", "730px", "760px", "780px", "800px", "820px", "840px", "860px", "880px", "900px", "920px", "940px", "960px", "980px", "1000px",
                "1020px", "1040px", "1060px", "1080px", "1100px", "1120px", "1140px", "1160px", "1180px", "1200px", "1220px", "1240px", "1260px", "1280px", "1300px",
                "1320px", "1340px", "1360px", "1380px" };

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

    /**
     * @return
     */
    public int getCutValue() {
        return cutValueEl.getIntValue();
    }

    public boolean isShowMenu() {
        return showMenuEl.isSelected(0);
    }

    public boolean isShowNavButtons() {
        return showNavButtonsEl.isSelected(0);
    }

    public boolean isAssessable() {
        return isAssessableEl.isSelected(0);
    }

    public String getHeightValue() {
        return heightEl.getSelectedKey();
    }

    public String getEncodingContentValue() {
        return encodingContentEl.getSelectedKey();
    }

    public String getEncodingJSValue() {
        return encodingJSEl.getSelectedKey();
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        setFormTitle("headerform");
        setFormContextHelp("org.olat.presentation.course.nodes.scorm", "ced-scorm-settings.html", "help.hover.scorm-settings-filename");

        showMenuEl = uifactory.addCheckboxesVertical("showmenu", "showmenu.label", formLayout, new String[] { "xx" }, new String[] { null }, null, 1);
        showMenuEl.select("xx", showMenu);

        showNavButtonsEl = uifactory.addCheckboxesVertical("shownavbuttons", "shownavbuttons.label", formLayout, new String[] { "xx" }, new String[] { null }, null, 1);
        showNavButtonsEl.select("xx", showNavButtons);

        heightEl = uifactory.addDropdownSingleselect("height", "height.label", formLayout, keys, values, null);
        if (Arrays.asList(keys).contains(height)) {
            heightEl.select(height, true);
        } else {
            heightEl.select(ScormEditController.CONFIG_HEIGHT_AUTO, true);
        }

        encodingContentEl = uifactory.addDropdownSingleselect("encoContent", "encoding.content", formLayout, encodingContentKeys, encodingContentValues, null);
        if (Arrays.asList(encodingContentKeys).contains(encodingContent)) {
            encodingContentEl.select(encodingContent, true);
        } else {
            encodingContentEl.select(NodeEditController.CONFIG_CONTENT_ENCODING_AUTO, true);
        }

        encodingJSEl = uifactory.addDropdownSingleselect("encoJS", "encoding.js", formLayout, encodingJSKeys, encodingJSValues, null);
        if (Arrays.asList(encodingJSKeys).contains(encodingJS)) {
            encodingJSEl.select(encodingJS, true);
        } else {
            encodingJSEl.select(NodeEditController.CONFIG_JS_ENCODING_AUTO, true);
        }

        isAssessableEl = uifactory.addCheckboxesVertical("isassessable", "assessable.label", formLayout, new String[] { "xx" }, new String[] { null }, null, 1);
        isAssessableEl.select("xx", isAssessable);

        cutValueEl = uifactory.addIntegerElement("cutvalue", "cutvalue.label", 0, formLayout);
        cutValueEl.setIntValue(cutValue);
        cutValueEl.setDisplaySize(3);
        uifactory.addFormSubmitButton("save", formLayout);
    }

    @Override
    protected void doDispose() {
        //
    }
}
