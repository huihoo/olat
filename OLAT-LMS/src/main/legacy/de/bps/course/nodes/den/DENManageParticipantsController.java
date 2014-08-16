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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.form.flexible.impl.components.SimpleText;
import org.olat.presentation.framework.core.components.table.Table;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableEvent;
import org.olat.presentation.framework.core.components.table.TableMultiSelectEvent;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.Event;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailNotificationEditController;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.MailerWithTemplate;
import org.olat.presentation.contactform.ContactFormController;

import de.bps.course.nodes.DENCourseNode;

public class DENManageParticipantsController extends BasicController {

	private final DENCourseNode courseNode;
	private final OLATResourceable ores;
	private DENStatus status;

	// objects for list of participants view
	private List<KalendarEvent> dateList;
	private final DENListTableDataModel listTableData;
	private TableController tableListParticipants;

	// objects for manage participants view
	private VelocityContainer participantsVC;
	private DENParticipantsTableDataModel participantsTableData;
	private TableController tableManageParticipants;
	private DENManageParticipantsForm formManageParticipants;
	private UserSearchController userSearchCntrl;
	private CloseableModalController userSearchCMC;
	private KalendarEvent selectedEvent;

	// mail notification
	private MailNotificationEditController addedNotificationCtr, removedNotificationCtr;
	private ContactFormController contactCtr;
	private CloseableModalController notificationCmc;
	private List<Identity> added = new ArrayList<Identity>();
	private List<Identity> removed = new ArrayList<Identity>();

	private CloseableModalController manageParticipantsModalCntrl;

	private final DENManager denManager;

	public DENManageParticipantsController(final UserRequest ureq, final WindowControl wControl, final OLATResourceable ores, final DENCourseNode courseNode) {
		super(ureq, wControl);

		this.ores = ores;
		this.courseNode = courseNode;
		this.denManager = DENManager.getInstance();

		// prepare list of enrolled participants
		dateList = denManager.getDENEvents(ores.getResourceableId(), courseNode.getIdent());
		listTableData = new DENListTableDataModel(dateList, getTranslator());
		tableListParticipants = denManager.createListParticipantsTable(ureq, wControl, getTranslator(), listTableData);
		listenTo(tableListParticipants);

		putInitialPanel(tableListParticipants.getInitialComponent());
	}

	@Override
	protected void doDispose() {
		if (tableListParticipants != null) {
			removeAsListenerAndDispose(tableListParticipants);
			tableListParticipants = null;
		}
		if (tableManageParticipants != null) {
			removeAsListenerAndDispose(tableManageParticipants);
			tableManageParticipants = null;
		}
		if (formManageParticipants != null) {
			removeAsListenerAndDispose(formManageParticipants);
			formManageParticipants = null;
		}
	}

	@Override
	public void event(final UserRequest ureq, final Controller source, final Event event) {
		if (source == tableListParticipants) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				final TableEvent tableEvent = (TableEvent) event;
				// open window for choosen date to manage the enrolled users or manually add
				if (tableEvent.getActionId().equals(DENListTableDataModel.CHANGE_ACTION)) {
					selectedEvent = (KalendarEvent) listTableData.getObject(tableEvent.getRowId());
					final List<Identity> participants = denManager.getEventParticipants(selectedEvent);
					participantsTableData = new DENParticipantsTableDataModel(participants);

					removeAsListenerAndDispose(tableManageParticipants);
					tableManageParticipants = denManager.createParticipantsTable(ureq, getWindowControl(), getTranslator(), participantsTableData);
					listenTo(tableManageParticipants);

					removeAsListenerAndDispose(formManageParticipants);
					formManageParticipants = new DENManageParticipantsForm(ureq, getWindowControl());
					listenTo(formManageParticipants);

					participantsVC = createVelocityContainer("participants");
					final SimpleText dateTitle = new SimpleText("dateTitle", translate("dates.table.subject") + ": " + selectedEvent.getSubject());
					final DateFormat df = new SimpleDateFormat();
					final SimpleText dateTimeframe = new SimpleText("dateTimeframe", translate("dates.table.date") + ": " + df.format(selectedEvent.getBegin()) + " - "
							+ df.format(selectedEvent.getEnd()));
					participantsVC.put("dateTitle", dateTitle);
					participantsVC.put("dateTimeframe", dateTimeframe);
					participantsVC.put("participantsTable", tableManageParticipants.getInitialComponent());
					participantsVC.put("addParticipants", formManageParticipants.getInitialComponent());

					removeAsListenerAndDispose(manageParticipantsModalCntrl);
					manageParticipantsModalCntrl = new CloseableModalController(getWindowControl(), "close", participantsVC, true,
							translate("dates.table.participant.manage"));
					listenTo(manageParticipantsModalCntrl);

					manageParticipantsModalCntrl.activate();
				}
			} else {
				final TableMultiSelectEvent tmse = (TableMultiSelectEvent) event;
				final BitSet selection = tmse.getSelection();
				// delete all users from the selected dates
				if (tmse.getAction().equals(DENListTableDataModel.DELETE_ACTION) && selection.cardinality() > 0) {
					removed = denManager.getSelectedEventParticipants(dateList, selection);
					dateList = denManager.deleteParticipants(ores, courseNode, denManager.getSelectedEventIDs(dateList, selection));
					listTableData.setObjects(dateList);
					// send notification mail
					createRemovedNotificationMail(ureq, dateList.get(0).getSubject());
				} else if (tmse.getAction().equals(DENListTableDataModel.MAIL_ACTION) && selection.cardinality() > 0) {
					// send email to all users from the selected dates
					final List<Identity> participants = denManager.getSelectedEventParticipants(dateList, selection);
					createParticipantsMail(ureq, participants);
				} else if (selection.cardinality() == 0) {
					showWarning("participants.message.empty");
				}
			}
		} else if (source == formManageParticipants && event == DENManageParticipantsForm.ADD_PARTICIPANTS) {
			// open user search controller to manually add users in date
			removeAsListenerAndDispose(userSearchCntrl);
			userSearchCntrl = new UserSearchController(ureq, getWindowControl(), true, true, true);
			listenTo(userSearchCntrl);

			removeAsListenerAndDispose(userSearchCMC);
			userSearchCMC = new CloseableModalController(getWindowControl(), "close", userSearchCntrl.getInitialComponent());
			listenTo(userSearchCMC);

			userSearchCMC.activate();
		} else if (source == userSearchCntrl) {
			if (event == Event.CANCELLED_EVENT) {
				userSearchCMC.deactivate();
			} else {
				List<Identity> toAdd = null;
				added = new ArrayList<Identity>();
				if (event instanceof SingleIdentityChosenEvent) {
					final SingleIdentityChosenEvent singleEvent = (SingleIdentityChosenEvent) event;
					final Identity choosenIdentity = singleEvent.getChosenIdentity();
					toAdd = new ArrayList<Identity>();
					toAdd.add(choosenIdentity);
				} else if (event instanceof MultiIdentityChosenEvent) {
					final MultiIdentityChosenEvent multiEvent = (MultiIdentityChosenEvent) event;
					toAdd = multiEvent.getChosenIdentities();
				}
				boolean showMessage = false;
				if (toAdd != null && toAdd.size() > 0) {
					for (final Identity identity : toAdd) {
						status = denManager.doEnroll(identity, selectedEvent, ores, courseNode, true);
						if (!status.isEnrolled() && status.getErrorMessage().equals(DENStatus.ERROR_ALREADY_ENROLLED)) {
							showMessage = true;
						} else {
							added.add(identity);
						}
					}
					if (showMessage) {
						showWarning("enrollment.warning.manual");
					}
					refreshTables();
				}
				userSearchCMC.deactivate();
				if (added.size() > 0) {
					// write notification mail
					createAddedNotificationMail(ureq, dateList.get(0).getSubject());
				}
			}

		} else if (source == tableManageParticipants) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				final TableEvent tableEvent = (TableEvent) event;
				// delete single user from event
				if (tableEvent.getActionId().equals(DENParticipantsTableDataModel.REMOVE_ACTION)) {
					final Identity identity = participantsTableData.getEntryAt(tableEvent.getRowId());
					status = denManager.cancelEnroll(identity, selectedEvent, ores, courseNode);
					if (!status.isCancelled()) {
						showError();
					} else {
						removed.clear();
						removed.add(identity);
						createRemovedNotificationMail(ureq, dateList.get(0).getSubject());
					}
					refreshTables();
					// write email to single user
				} else if (tableEvent.getActionId().equals(DENParticipantsTableDataModel.MAIL_ACTION)) {
					final List<Identity> participants = new ArrayList<Identity>();
					participants.add(participantsTableData.getEntryAt(tableEvent.getRowId()));
					createParticipantsMail(ureq, participants);
				}
			}
		} else if (source == addedNotificationCtr && event == Event.DONE_EVENT) {
			if (addedNotificationCtr.getMailTemplate() != null) {
				List<Identity> ccIdentities = new ArrayList<Identity>();
				if (addedNotificationCtr.getMailTemplate().getCpfrom()) {
					ccIdentities.add(ureq.getIdentity());
				} else {
					ccIdentities = null;
				}
				final MailerResult mailerResult = MailerWithTemplate.getInstance().sendMailAsSeparateMails(added, null, null, addedNotificationCtr.getMailTemplate(),
						ureq.getIdentity());
				MailHelper.printErrorsAndWarnings(mailerResult, getWindowControl(), ureq.getLocale());
			}
			notificationCmc.deactivate();
			added.clear();
		} else if (source == removedNotificationCtr && event == Event.DONE_EVENT) {
			if (removedNotificationCtr.getMailTemplate() != null) {
				List<Identity> ccIdentities = new ArrayList<Identity>();
				if (addedNotificationCtr.getMailTemplate().getCpfrom()) {
					ccIdentities.add(ureq.getIdentity());
				} else {
					ccIdentities = null;
				}
				final MailerResult mailerResult = MailerWithTemplate.getInstance().sendMailAsSeparateMails(added, null, null, addedNotificationCtr.getMailTemplate(),
						ureq.getIdentity());
				MailHelper.printErrorsAndWarnings(mailerResult, getWindowControl(), ureq.getLocale());
			}
			notificationCmc.deactivate();
			removed.clear();
		} else if (source == contactCtr) {
			notificationCmc.deactivate();
		}
	}

	@Override
	public void event(final UserRequest ureq, final Component source, final Event event) {
		// nothing to do
	}

	private void showError() {
		final String message = status.getErrorMessage();
		if (DENStatus.ERROR_ALREADY_ENROLLED.equals(message)) {
			getWindowControl().setError("");
		} else if (DENStatus.ERROR_NOT_ENROLLED.equals(message)) {
			getWindowControl().setError("");
		} else if (DENStatus.ERROR_PERSISTING.equals(message)) {
			getWindowControl().setError("");
		} else if (DENStatus.ERROR_GENERAL.equals(message)) {
			getWindowControl().setError("");
		}
	}

	private void refreshTables() {
		// set all table datas new to refresh view
		final List<Identity> participants = denManager.getEventParticipants(selectedEvent);
		participantsTableData.setEntries((participants));
		tableManageParticipants.setTableDataModel(participantsTableData);
		listTableData.setObjects(dateList);
		tableListParticipants.setTableDataModel(listTableData);
	}

	private void createAddedNotificationMail(final UserRequest ureq, final String subjectStr) {
		final MailTemplate mailTempl = denManager.getAddedMailTemplate(ureq, subjectStr, getTranslator());
		removeAsListenerAndDispose(addedNotificationCtr);
		addedNotificationCtr = new MailNotificationEditController(getWindowControl(), ureq, mailTempl, false);
		listenTo(addedNotificationCtr);

		final VelocityContainer sendNotificationVC = createVelocityContainer("sendnotification");
		sendNotificationVC.put("notificationForm", addedNotificationCtr.getInitialComponent());

		removeAsListenerAndDispose(notificationCmc);
		notificationCmc = new CloseableModalController(getWindowControl(), "close", sendNotificationVC);
		listenTo(notificationCmc);

		notificationCmc.activate();
	}

	private void createRemovedNotificationMail(final UserRequest ureq, final String subjectStr) {
		final MailTemplate mailTempl = denManager.getRemovedMailTemplate(ureq, subjectStr, getTranslator());
		removeAsListenerAndDispose(addedNotificationCtr);
		addedNotificationCtr = new MailNotificationEditController(getWindowControl(), ureq, mailTempl, false);
		listenTo(addedNotificationCtr);

		final VelocityContainer sendNotificationVC = createVelocityContainer("sendnotification");
		sendNotificationVC.put("notificationForm", addedNotificationCtr.getInitialComponent());
		removeAsListenerAndDispose(notificationCmc);
		notificationCmc = new CloseableModalController(getWindowControl(), "close", sendNotificationVC);
		listenTo(notificationCmc);

		notificationCmc.activate();
	}

	private void createParticipantsMail(final UserRequest ureq, final List<Identity> participants) {
		final VelocityContainer sendMessageVC = createVelocityContainer("sendmessage");
		final ContactMessage cmsg = new ContactMessage(ureq.getIdentity());
		ContactList contactList = null;
		if (participants.size() == 1) {
			contactList = new ContactList(participants.get(0).getName());
		} else {
			contactList = new ContactList(translate("participants.message.to"));
		}
		contactList.addAllIdentites(participants);
		cmsg.addEmailTo(contactList);

		removeAsListenerAndDispose(contactCtr);
		contactCtr = new ContactFormController(ureq, getWindowControl(), false, false, false, false, cmsg);
		listenTo(contactCtr);

		sendMessageVC.contextPut("title", translate("participants.message"));
		sendMessageVC.put("contactForm", contactCtr.getInitialComponent());

		removeAsListenerAndDispose(notificationCmc);
		notificationCmc = new CloseableModalController(getWindowControl(), "close", sendMessageVC);
		listenTo(notificationCmc);

		notificationCmc.activate();
	}
}
