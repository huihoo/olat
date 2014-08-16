/**
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * Copyright (c) 2005-2009 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 * All rights reserved.
 */
package de.bps.course.nodes.den;

import javax.sound.midi.ControllerEventListener;
import javax.sound.midi.ShortMessage;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPane;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.Event;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.condition.Condition;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.NodeEditController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;

import de.bps.course.nodes.DENCourseNode;

public class DENEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

	public static final String PANE_TAB_DENCONFIG = "pane.tab.denconfig";
	private static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";

	private ModuleConfiguration moduleConfiguration;
	private final OLATResourceable ores;
	private final DENCourseNode courseNode;
	private final VelocityContainer editVc;
	private Translator translator;

	private CloseableModalController manageDatesModalCntrll, listParticipantsModalCntrll;
	private final Link manageDatesButton, manageParticipantsButton;

	private ConditionEditController accessibilityCondContr;
	private DENEditForm dateFormContr;
	private TabbedPane tabPane;
	final static String[] paneKeys = { PANE_TAB_DENCONFIG, PANE_TAB_ACCESSIBILITY };

	public DENEditController(final ModuleConfiguration moduleConfiguration, final UserRequest ureq, final WindowControl wControl, final DENCourseNode courseNode,
			final OLATResourceable ores, final UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);

		this.moduleConfiguration = moduleConfiguration;
		this.ores = ores;
		this.courseNode = courseNode;

		editVc = this.createVelocityContainer("edit");

		final Condition accessCondition = courseNode.getPreConditionAccess();
		final ICourse course = CourseFactory.loadCourse(ores);
		moduleConfiguration.set(DENCourseNode.CONF_COURSE_ID, course.getResourceableId());

		accessibilityCondContr = new ConditionEditController(ureq, wControl, course.getCourseEnvironment().getCourseGroupManager(), accessCondition,
				"accessabilityConditionForm", AssessmentHelper.getAssessableNodes(course.getEditorTreeModel(), courseNode), userCourseEnv);
		this.listenTo(accessibilityCondContr);

		dateFormContr = new DENEditForm(ureq, getWindowControl(), this.moduleConfiguration);
		dateFormContr.addControllerListener(this);

		manageDatesButton = LinkFactory.createButton("config.dates", editVc, this);
		manageParticipantsButton = LinkFactory.createButton("run.enrollment.list", editVc, this);

		editVc.put("dateform", dateFormContr.getInitialComponent());
	}

	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}

	@Override
	public TabbedPane getTabbedPane() {
		return tabPane;
	}

	@Override
	protected void doDispose() {
		if (dateFormContr != null) {
			dateFormContr.dispose();
			dateFormContr = null;
		}
		if (manageDatesModalCntrll != null) {
			manageDatesModalCntrll.dispose();
			manageDatesModalCntrll = null;
		}
		if (listParticipantsModalCntrll != null) {
			listParticipantsModalCntrll.dispose();
			listParticipantsModalCntrll = null;
		}
		if (accessibilityCondContr != null) {
			accessibilityCondContr.dispose();
			accessibilityCondContr = null;
		}
	}

	@Override
	protected void event(final UserRequest ureq, final Controller source, final Event event) {
		if (source == accessibilityCondContr) {
			if (event == Event.CHANGED_EVENT) {
				final Condition cond = accessibilityCondContr.getCondition();
				courseNode.setPreConditionAccess(cond);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == dateFormContr) {
			moduleConfiguration = dateFormContr.getModuleConfiguration();
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
		}
		if (source == manageDatesModalCntrll) {
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
		}
		if (source == listParticipantsModalCntrll) {
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
		}
	}

	@Override
	public void event(final UserRequest ureq, final Component source, final Event event) {
		if (source == manageDatesButton) {
			// management of dates
			final DENManageDatesController datesCtr = new DENManageDatesController(ureq, getWindowControl(), ores, courseNode);
			manageDatesModalCntrll = new CloseableModalController(getWindowControl(), "close", datesCtr.getInitialComponent(), true, translate("config.dates"));
			manageDatesModalCntrll.addControllerListener(this);
			manageDatesModalCntrll.activate();
		} else if (source == manageParticipantsButton) {
			// list of participants
			final DENManageParticipantsController partsCtr = new DENManageParticipantsController(ureq, getWindowControl(), ores, courseNode);
			listParticipantsModalCntrll = new CloseableModalController(getWindowControl(), "close", partsCtr.getInitialComponent(), true, translate("dates.table.list"));
			listParticipantsModalCntrll.addControllerListener(this);
			listParticipantsModalCntrll.activate();
		}
	}

	@Override
	public void controlChange(final ShortMessage event) {
		// nothing to do
	}

	@Override
	public void addTabs(final TabbedPane tabbedPane) {
		tabPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), accessibilityCondContr.getWrappedDefaultAccessConditionVC(translate("condition.accessibility.title")));
		tabbedPane.addTab(translate(PANE_TAB_DENCONFIG), editVc);
	}

	public static boolean isConfigValid(final ModuleConfiguration moduleConfig) {
		final DENManager manager = DENManager.getInstance();
		final Long courseId = (Long) moduleConfig.get(DENCourseNode.CONF_COURSE_ID);
		final String courseNodeId = (String) moduleConfig.get(DENCourseNode.CONF_COURSE_NODE_ID);

		return (manager.getEventCount(courseId, courseNodeId) != 0);
	}

}
