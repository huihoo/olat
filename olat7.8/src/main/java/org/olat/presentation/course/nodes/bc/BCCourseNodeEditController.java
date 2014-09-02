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

package org.olat.presentation.course.nodes.bc;

import org.olat.data.commons.vfs.Quota;
import org.olat.data.commons.vfs.QuotaManager;
import org.olat.data.commons.vfs.callbacks.VFSSecurityCallback;
import org.olat.data.commons.vfs.olatimpl.OlatNamedContainerImpl;
import org.olat.lms.admin.quota.QuotaConstants;
import org.olat.lms.commons.vfs.securitycallbacks.FullAccessWithQuotaCallback;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.AssessmentHelper;
import org.olat.lms.course.condition.Condition;
import org.olat.lms.course.nodes.BCCourseNode;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.presentation.admin.quota.QuotaControllerFactory;
import org.olat.presentation.course.condition.ConditionEditController;
import org.olat.presentation.course.editor.NodeEditController;
import org.olat.presentation.filebrowser.FolderRunController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPane;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.ControllerEventListener;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.system.event.Event;

/**
 * Initial Date: Apr 28, 2004
 * 
 * @author gnaegi
 */
public class BCCourseNodeEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

    public static final String PANE_TAB_FOLDER = "pane.tab.folder";
    public static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";
    static final String[] paneKeys = { PANE_TAB_FOLDER, PANE_TAB_ACCESSIBILITY };

    private final ICourse course;// o_clusterOK by guido: inside course editor
                                 // its save to have a reference to the
                                 // course
    private final BCCourseNode bcNode;
    private final VelocityContainer accessabiliryContent, folderContent;

    private final ConditionEditController uploaderCondContr, downloaderCondContr;
    private Controller quotaContr;
    private TabbedPane myTabbedPane;
    private final Link vfButton;

    /**
     * Constructor for a folder course building block editor controller
     * 
     * @param bcNode
     * @param course
     * @param ureq
     * @param wControl
     */
    public BCCourseNodeEditController(final BCCourseNode bcNode, final ICourse course, final UserRequest ureq, final WindowControl wControl,
            final UserCourseEnvironment euce) {
        super(ureq, wControl);
        // o_clusterOK by guido: inside course editor its save to have a
        // reference to the course
        this.course = course;
        this.bcNode = bcNode;
        myTabbedPane = null;

        accessabiliryContent = this.createVelocityContainer("edit");

        // Uploader precondition
        final Condition uploadCondition = bcNode.getPreConditionUploaders();
        uploaderCondContr = new ConditionEditController(ureq, getWindowControl(), course.getCourseEnvironment().getCourseGroupManager(), uploadCondition,
                "uploaderConditionForm", AssessmentHelper.getAssessableNodes(course.getEditorTreeModel(), bcNode), euce);
        this.listenTo(uploaderCondContr);
        accessabiliryContent.put("uploadCondition", uploaderCondContr.getInitialComponent());

        // Uploader precondition
        final Condition downloadCondition = bcNode.getPreConditionDownloaders();
        downloaderCondContr = new ConditionEditController(ureq, getWindowControl(), course.getCourseEnvironment().getCourseGroupManager(), downloadCondition,
                "downloadConditionForm", AssessmentHelper.getAssessableNodes(course.getEditorTreeModel(), bcNode), euce);
        this.listenTo(downloaderCondContr);
        accessabiliryContent.put("downloadCondition", downloaderCondContr.getInitialComponent());

        folderContent = this.createVelocityContainer("folder");
        vfButton = LinkFactory.createButton("folder.view", folderContent, this);

        if ((ureq.getUserSession().getRoles().isOLATAdmin()) | ((ureq.getUserSession().getRoles().isInstitutionalResourceManager()))) {
            final String relPath = BCCourseNode.getFoldernodePathRelToFolderBase(course.getCourseEnvironment(), bcNode);
            quotaContr = QuotaControllerFactory.getQuotaEditorInstance(ureq, wControl, relPath, false);
            folderContent.put("quota", quotaContr.getInitialComponent());
            folderContent.contextPut("editQuota", Boolean.TRUE);
        } else {
            folderContent.contextPut("editQuota", Boolean.FALSE);
        }
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.system.event.control.Event)
     */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == vfButton) {
            final OlatNamedContainerImpl namedContainer = BCCourseNode.getNodeFolderContainer(bcNode, course.getCourseEnvironment());
            Quota quota = QuotaManager.getInstance().getCustomQuota(namedContainer.getRelPath());
            if (quota == null) {
                final Quota defQuota = QuotaManager.getInstance().getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_NODES);
                quota = QuotaManager.getInstance().createQuota(namedContainer.getRelPath(), defQuota.getQuotaKB(), defQuota.getUlLimitKB());
            }
            final VFSSecurityCallback secCallback = new FullAccessWithQuotaCallback(quota);
            namedContainer.setLocalSecurityCallback(secCallback);
            final CloseableModalController cmc = new CloseableModalController(getWindowControl(), translate("close"), new FolderRunController(namedContainer, false,
                    ureq, getWindowControl()).getInitialComponent());
            cmc.activate();
            return;
        }
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.system.event.control.Event)
     */
    @Override
    protected void event(final UserRequest urequest, final Controller source, final Event event) {
        if (source == uploaderCondContr) {
            if (event == Event.CHANGED_EVENT) {
                final Condition cond = uploaderCondContr.getCondition();
                bcNode.setPreConditionUploaders(cond);
                fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        } else if (source == downloaderCondContr) {
            if (event == Event.CHANGED_EVENT) {
                final Condition cond = downloaderCondContr.getCondition();
                bcNode.setPreConditionDownloaders(cond);
                fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        }
    }

    /**
	 */
    @Override
    public void addTabs(final TabbedPane tabbedPane) {
        myTabbedPane = tabbedPane;
        tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), accessabiliryContent);
        tabbedPane.addTab(translate(PANE_TAB_FOLDER), folderContent);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // child controllers registered with listenTo() get disposed in
        // BasicController
        if (quotaContr != null) {
            quotaContr.dispose();
            quotaContr = null;
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
