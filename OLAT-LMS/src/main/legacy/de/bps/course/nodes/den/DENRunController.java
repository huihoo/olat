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

import java.util.List;

import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.table.Table;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableEvent;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.Event;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Util;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.notifications.ContextualSubscriptionController;
import org.olat.core.util.notifications.NotificationsManager;
import org.olat.core.util.notifications.PublisherData;
import org.olat.core.util.notifications.SubscriptionContext;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.ObjectivesHelper;
import org.olat.modules.ModuleConfiguration;

import de.bps.course.nodes.DENCourseNode;

/**
 * Date enrollment run controller
 * 
 * @author skoeber
 */
public class DENRunController extends BasicController implements GenericEventListener {

	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(DENRunController.class);

	private final DENCourseNode courseNode;
	private DENStatus status;

	// objects for run view
	private DENRunTableDataModel runTableData;
	private List<KalendarEvent> runTableDataList;
	private TableController runDENTable;
	private FormBasicController authorOptions;
	private CloseableModalController manageDatesModalCntrll, listParticipantsModalCntrll;

	private final DENManager denManager;
	private final Boolean cancelEnrollEnabled;
	private ContextualSubscriptionController csc;
	private SubscriptionContext subsContext;

	private final VelocityContainer runVC;
	private final OLATResourceable ores;

	/**
	 * Standard constructor for Date Enrollment run controller
	 * 
	 * @param ureq
	 * @param wControl
	 * @param moduleConfig
	 * @param denCourseNode
	 * @param userCourseEnv
	 */
	public DENRunController(final UserRequest ureq, final WindowControl wControl, final ModuleConfiguration moduleConfig, final DENCourseNode denCourseNode) {
		super(ureq, wControl);
		this.courseNode = denCourseNode;
		this.ores = CourseFactory.loadCourse((Long) moduleConfig.get(DENCourseNode.CONF_COURSE_ID));
		this.cancelEnrollEnabled = ((Boolean) moduleConfig.get(DENCourseNode.CONF_CANCEL_ENROLL_ENABLED)).booleanValue();

		denManager = DENManager.getInstance();

		// prepare table for run view
		createOrUpdateDateTable(ureq, getWindowControl(), denCourseNode);
		runDENTable = denManager.createRunDatesTable(ureq, wControl, getTranslator(), runTableData);
		listenTo(runDENTable);

		runVC = new VelocityContainer("dateVC", VELOCITY_ROOT + "/run.html", getTranslator(), this);

		// show only the options for managing dates and participants if user is admin or course coach
		final ICourse course = CourseFactory.loadCourse(ores);
		final CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
		if (cgm.isIdentityCourseAdministrator(ureq.getIdentity()) || cgm.isIdentityCourseCoach(ureq.getIdentity())) {
			// subscription
			subsContext = new SubscriptionContext(course, courseNode.getIdent());
			// if sc is null, then no subscription is desired
			if (subsContext != null) {
				final String businessPath = wControl.getBusinessControl().getAsString();
				final PublisherData pdata = new PublisherData(OresHelper.calculateTypeName(DENCourseNode.class), String.valueOf(course.getResourceableId()), businessPath);
				csc = new ContextualSubscriptionController(ureq, getWindowControl(), subsContext, pdata);
				runVC.put("subscription", csc.getInitialComponent());
			}

			authorOptions = new AuthorOptionsForm(ureq, getWindowControl());
			authorOptions.addControllerListener(this);
			runVC.contextPut("showAuthorBtns", Boolean.TRUE);
			runVC.put("authorOptions", authorOptions.getInitialComponent());
		} else {
			runVC.contextPut("showAuthorBtns", Boolean.FALSE);
		}

		// Adding learning objectives
		final String learningObj = denCourseNode.getLearningObjectives();
		if (learningObj != null) {
			final Component learningObjectives = ObjectivesHelper.createLearningObjectivesComponent(learningObj, ureq);
			runVC.put("learningObjectives", learningObjectives);
			runVC.contextPut("hasObjectives", learningObj);
		}

		runVC.put("datesTable", runDENTable.getInitialComponent());

		putInitialPanel(runVC);
	}

	/**
	 * prepare table for run view
	 * 
	 * @param ureq
	 * @param wControl
	 * @param denCourseNode
	 * @param initialize
	 */
	private void createOrUpdateDateTable(final UserRequest ureq, final WindowControl wControl, final DENCourseNode denCourseNode) {
		// prepare table for run view
		runTableDataList = denManager.getDENEvents(ores.getResourceableId(), denCourseNode.getIdent());
		runTableData = new DENRunTableDataModel(runTableDataList, ureq, denCourseNode, cancelEnrollEnabled, getTranslator());
	}

	@Override
	protected void doDispose() {
		if (runTableData != null) {
			runTableData = null;
		}
		if (runTableDataList != null) {
			runTableDataList = null;
		}
		if (csc != null) {
			removeAsListenerAndDispose(csc);
			csc = null;
		}
		if (authorOptions != null) {
			removeAsListenerAndDispose(authorOptions);
			authorOptions = null;
		}
		if (runDENTable != null) {
			removeAsListenerAndDispose(runDENTable);
			runDENTable = null;
		}
		if (manageDatesModalCntrll != null) {
			removeAsListenerAndDispose(manageDatesModalCntrll);
			manageDatesModalCntrll = null;
		}
		if (listParticipantsModalCntrll != null) {
			removeAsListenerAndDispose(listParticipantsModalCntrll);
			listParticipantsModalCntrll = null;
		}
	}

	@Override
	public void event(final Event event) {
		// nothing to do
	}

	@Override
	public void event(final UserRequest ureq, final Component source, final Event event) {
		// nothing to do
	}

	@Override
	protected void event(final UserRequest ureq, final Controller source, final Event event) {
		if (runDENTable == source) {
			// the link to enroll or cancel enrollment is clicked
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				final TableEvent tableEvent = (TableEvent) event;
				final KalendarEvent calEvent = (KalendarEvent) runTableData.getObject(tableEvent.getRowId());
				if (tableEvent.getActionId().equals(DENRunTableDataModel.CMD_ENROLL_IN_DATE)) {
					// do enroll
					status = denManager.doEnroll(ureq.getIdentity(), calEvent, ores, courseNode);
					if (!status.isEnrolled()) {
						showError();
					}
				} else if (tableEvent.getActionId().equals(DENRunTableDataModel.CMD_ENROLLED_CANCEL)) {
					// cancel enrollment
					status = denManager.cancelEnroll(ureq.getIdentity(), calEvent, ores, courseNode);
					if (!status.isCancelled()) {
						showError();
					}
				}
				createOrUpdateDateTable(ureq, getWindowControl(), courseNode);
				runDENTable.setTableDataModel(runTableData);
				fireEvent(ureq, Event.DONE_EVENT);
				// inform subscription context about changes
				NotificationsManager.getInstance().markPublisherNews(subsContext, ureq.getIdentity());
				// </OPAL-122>
			}
		} else if (authorOptions == source) {
			if (event == AuthorOptionsForm.MANAGE_EVENT) {
				// management of dates
				final DENManageDatesController datesCtr = new DENManageDatesController(ureq, getWindowControl(), ores, courseNode);
				manageDatesModalCntrll = new CloseableModalController(getWindowControl(), "close", datesCtr.getInitialComponent(), true, translate("config.dates"));
				manageDatesModalCntrll.addControllerListener(this);
				manageDatesModalCntrll.activate();
			} else if (event == AuthorOptionsForm.LIST_EVENT) {
				// list of participants
				final DENManageParticipantsController partsCtr = new DENManageParticipantsController(ureq, getWindowControl(), ores, courseNode);
				listParticipantsModalCntrll = new CloseableModalController(getWindowControl(), "close", partsCtr.getInitialComponent(), true,
						translate("dates.table.list"));
				listParticipantsModalCntrll.addControllerListener(this);
				listParticipantsModalCntrll.activate();
			}
		}
	}

	private void showError() {
		final String message = status.getErrorMessage();
		if (DENStatus.ERROR_ALREADY_ENROLLED.equals(message)) {
			getWindowControl().setError(translate("enrollment.error.enrolled"));
		} else if (DENStatus.ERROR_NOT_ENROLLED.equals(message)) {
			getWindowControl().setError(translate("enrollment.error.notenrolled"));
		} else if (DENStatus.ERROR_PERSISTING.equals(message)) {
			getWindowControl().setError(translate("enrollment.error.persisting"));
		} else if (DENStatus.ERROR_GENERAL.equals(message)) {
			getWindowControl().setError(translate("enrollment.error.general"));
		} else if (DENStatus.ERROR_FULL.equals(message)) {
			getWindowControl().setError(translate("enrollment.error.full"));
		}
	}

}

class AuthorOptionsForm extends FormBasicController {

	public static final Event MANAGE_EVENT = new Event("manage");
	public static final Event LIST_EVENT = new Event("list");

	private FormLinkImpl manageDatesBtn, enrollmentListBtn;

	public AuthorOptionsForm(final UserRequest ureq, final WindowControl wControl) {
		super(ureq, wControl);
		initForm(this.flc, this, ureq);
	}

	@Override
	protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
		final FormLayoutContainer horLayout = FormLayoutContainer.createHorizontalFormLayout("", getTranslator());
		formLayout.add(horLayout);
		manageDatesBtn = new FormLinkImpl("preferencesButton", MANAGE_EVENT.getCommand(), "config.dates", Link.BUTTON);
		enrollmentListBtn = new FormLinkImpl("enrollmentListButton", LIST_EVENT.getCommand(), "run.enrollment.list", Link.BUTTON);
		horLayout.add(manageDatesBtn);
		horLayout.add(enrollmentListBtn);
	}

	@Override
	protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
		if (source == manageDatesBtn) {
			fireEvent(ureq, AuthorOptionsForm.MANAGE_EVENT);
		} else if (source == enrollmentListBtn) {
			fireEvent(ureq, AuthorOptionsForm.LIST_EVENT);
		}
	}

	@Override
	protected void doDispose() {
		// nothing to do
	}

	@Override
	protected void formOK(final UserRequest ureq) {
		// nothing to do
	}

}
