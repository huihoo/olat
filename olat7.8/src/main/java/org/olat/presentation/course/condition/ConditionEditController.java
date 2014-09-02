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

package org.olat.presentation.course.condition;

import java.util.List;

import org.olat.lms.course.condition.Condition;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.presentation.course.editor.EditorMainController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.group.context.BGContextEvent;
import org.olat.system.event.Event;
import org.olat.system.exception.OLATRuntimeException;

/**
 * Initial Date: Apr 7, 2004
 * 
 * @author gnaegi Comment: This controller can be used to display and edit OLAT node conditions. When the condition experssion has been changed, the controller will fire
 *         a Event.CHANGED_EVENT. See STCourseNodeEditController to get a usage example.
 */
public class ConditionEditController extends BasicController {

    private static final String PACKAGE_EDITOR = PackageUtil.getPackageName(EditorMainController.class);
    private static final String VELOCITY_ROOT = PackageUtil.getPackageVelocityRoot(ConditionEditController.class);

    private final VelocityContainer myContent;

    private ConditionConfigExpertForm conditionExpertForm;
    private Condition condition, validatedCondition;
    private final List<CourseNode> nodeIdentList;
    private final String formName;

    private final UserCourseEnvironment euce;
    private final Link easyModeButton;
    private final Link expertModeButton;
    private ConditionConfigEasyController conditionEasyCtrllr;

    /**
     * Create a controller which can edit a condition in easy and expert mode
     * 
     * @param ureq
     *            User Request data
     * @param groupMgr
     *            The course group manager
     * @param cond
     *            The condition which should be used to initialize the forms can then be used to embedd the condition form in your velocity container with
     *            $r.render("mainComponentName")
     * @param formName
     *            Name of the condition form - must be unique within a HTML page
     * @param nodeIdentList
     * @param euce
     */
    public ConditionEditController(final UserRequest ureq, final WindowControl wControl, final CourseGroupManager groupMgr, final Condition cond, final String formName,
            final List<CourseNode> nodeIdentList, final UserCourseEnvironment euce) {
        super(ureq, wControl);
        this.condition = cond;
        this.validatedCondition = condition.cloneCondition();
        if (this.condition == null) {
            throw new OLATRuntimeException("CondititionEditController called with a NULL condition", new IllegalArgumentException());
        }
        this.euce = euce;
        this.nodeIdentList = nodeIdentList;
        this.formName = formName;

        // Main component is a velocity container. It has a name choosen by the controller who
        // called this constructor
        this.myContent = createVelocityContainer("condedit");
        easyModeButton = LinkFactory.createButtonSmall("command.activate.easyMode", myContent, this);
        expertModeButton = LinkFactory.createButtonSmall("command.activate.expertMode", myContent, this);

        // init easy or expert mode form
        if (condition.isExpertMode()) {
            doExpertMode(ureq);
        } else {
            doEasyMode(ureq);
        }

        putInitialPanel(myContent);
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == conditionEasyCtrllr) {
            if (event == Event.CHANGED_EVENT) {
                // successfully changed condition - get condition - dispose and init cond
                validatedCondition = conditionEasyCtrllr.getCondition();
                condition = validatedCondition.cloneCondition();
                // Inform all listeners about the changed condition
                // this event goes to the NodeEditController and from there to the
                // CourseEditorController which then saves the condition
                fireEvent(ureq, Event.CHANGED_EVENT);
            } else if (event instanceof BGContextEvent) {
                // fired by condition easy ctrllr in the case a group/area was created
                // within default context. As more then one condition edit controller
                // may be on the screen -> see Forum node => those most be informed about
                // the changed condition, e.g. toggle the create link to choose link.
                fireEvent(ureq, event);
            }
        } else if (source == conditionExpertForm) {
            if (event == Event.CANCELLED_EVENT) {
                this.validatedCondition = condition.cloneCondition();
                initExpertForm(ureq);
            } else if (event == Event.DONE_EVENT) {
                // Update condition data and switch to read only mode
                validatedCondition = conditionExpertForm.updateConditionFromFormData(validatedCondition);
                condition = validatedCondition.cloneCondition();
                // we have copied the easy form data into the expert mode, we can clear now easy mode.
                validatedCondition.clearEasyConfig();

                // Inform all listeners about the changed condition
                fireEvent(ureq, Event.CHANGED_EVENT);
            }
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == easyModeButton) {
            doEasyMode(ureq);
        } else if (source == expertModeButton) {
            doExpertMode(ureq);
        }
    }

    private void doEasyMode(final UserRequest ureq) {
        // cleanup first
        if (conditionExpertForm != null) {
            myContent.remove(conditionExpertForm.getInitialComponent());
        }

        // now initialize easy form
        initEasyForm(ureq);
        myContent.contextPut("isExpertMode", Boolean.FALSE);
    }

    private void initEasyForm(final UserRequest ureq) {
        removeAsListenerAndDispose(conditionEasyCtrllr);
        conditionEasyCtrllr = new ConditionConfigEasyController(ureq, getWindowControl(), validatedCondition, formName, formName, nodeIdentList,
                euce.getCourseEditorEnv());
        listenTo(conditionEasyCtrllr);
        myContent.put("conditionEasyForm", conditionEasyCtrllr.getInitialComponent());
    }

    private void doExpertMode(final UserRequest ureq) {
        // cleanup first
        if (conditionEasyCtrllr != null) {
            myContent.remove(conditionEasyCtrllr.getInitialComponent());
            removeAsListenerAndDispose(conditionEasyCtrllr);
            conditionEasyCtrllr = null;
        }
        // now initialize expert form
        initExpertForm(ureq);
        myContent.contextPut("isExpertMode", Boolean.TRUE);
    }

    private void initExpertForm(final UserRequest ureq) {
        removeAsListenerAndDispose(conditionExpertForm);
        conditionExpertForm = new ConditionConfigExpertForm(ureq, getWindowControl(), validatedCondition, euce);
        listenTo(conditionExpertForm);
        myContent.put("conditionExpertForm", conditionExpertForm.getInitialComponent());
    }

    /**
     * Get the condition that has been configured by this controller
     * 
     * @return Condition the configured condition
     */
    public Condition getCondition() {
        return condition;
    }

    /**
     * Get the condition editor wrapped as a generic accessability condition editor
     * 
     * @param title
     *            The title of this access condition, displayed as header in the surrounding fieldset
     * @return The wrapped condition editor component
     */
    public VelocityContainer getWrappedDefaultAccessConditionVC(final String title) {
        final Translator accessTranslator = new PackageTranslator(PACKAGE_EDITOR, getLocale());
        final VelocityContainer defaultAccessConditionView = new VelocityContainer("defaultAccessConditionView", VELOCITY_ROOT + "/defaultaccessedit.html",
                accessTranslator, null);
        defaultAccessConditionView.put("defaultAccessConditionView", myContent);
        defaultAccessConditionView.contextPut("title", title);
        return defaultAccessConditionView;
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // child controller disposed by basic controller
    }

}
