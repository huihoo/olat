package de.bps.course.nodes.den;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.velocity.VelocityContext;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.commons.calendar.CalendarManager;

import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarEventLink;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.table.BooleanColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.UserConstants;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.mail.MailTemplate;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.presentation.contactform.ContactFormController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

import de.bps.course.nodes.DENCourseNode;

/**
 * Manager for date enrollments, provides methods for the enrollment procedure and persisting
 * 
 * @author skoeber
 */
public class DENManager {

	private final static DENManager denManager = new DENManager();
	private final CalendarManager calManager;

	private DENManager() {
		calManager = CalendarManager.getInstance();
	}

	/**
	 * This method returns a reference of the static date enrollment manager (singleton pattern)
	 * 
	 * @return instance of DENManager
	 */
	public static DENManager getInstance() {
		return denManager;
	}

	/**
	 * Enrolls an user into a specific calendar event
	 * 
	 * @param identity
	 * @param event
	 * @param course
	 * @param courseNode
	 * @return status
	 */
	public DENStatus doEnroll(final Identity identity, final KalendarEvent event, final OLATResourceable ores, final DENCourseNode courseNode) {
		return doEnroll(identity, event, ores, courseNode, false);
	}

	/**
	 * Enrolls an user into a specific calendar event
	 * 
	 * @param identity
	 * @param event
	 * @param course
	 * @param courseNode
	 * @param allowOverfill
	 * @return status
	 */
	public DENStatus doEnroll(final Identity identity, final KalendarEvent event, final OLATResourceable ores, final DENCourseNode courseNode, final boolean allowOverfill) {
		final DENStatus status = new DENStatus();
		final ICourse course = CourseFactory.loadCourse(ores);
		final Kalendar cal = calManager.getCourseCalendar(course).getKalendar();
		final OLATResourceable calRes = calManager.getOresHelperFor(cal);
		// reload calendar events
		final List<KalendarEvent> denEvents = getDENEvents(ores.getResourceableId(), courseNode.getIdent());
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(calRes, new SyncerExecutor() {
			@Override
			public void execute() {
				boolean error = false;

				// try to find choosen calendar event in the reloaded event list
				KalendarEvent reloadEvent = event;
				for (final Iterator<KalendarEvent> iterator = denEvents.iterator(); iterator.hasNext();) {
					final KalendarEvent kalendarEvent = iterator.next();
					if (event.getID().equals(kalendarEvent.getID())) {
						reloadEvent = kalendarEvent;
						break;
					} else if (!iterator.hasNext()) {
						// cannot find reloaded calendar event
						status.setEnrolled(false);
						status.setErrorMessage(DENStatus.ERROR_GENERAL);
						error = true;
					}
				}

				final Collection<KalendarEvent> collEvents = cal.getEvents();
				// check if date is already full
				if (!error && !allowOverfill && isDateFull(reloadEvent)) {
					status.setEnrolled(false);
					status.setErrorMessage(DENStatus.ERROR_FULL);
					error = true;
				}
				// check if identity is already enrolled
				if (!error && isAlreadyEnrolled(identity, collEvents, courseNode)) {
					status.setEnrolled(false);
					status.setErrorMessage(DENStatus.ERROR_ALREADY_ENROLLED);
					error = true;
				}
				// enroll in event
				if (!error) {
					if (reloadEvent.getParticipants() != null && reloadEvent.getParticipants().length > 0) {
						final int currLength = reloadEvent.getParticipants().length;
						final String[] partsNew = new String[currLength + 1]; // one to add
						final String[] partsOld = reloadEvent.getParticipants();
						for (int i = 0; i < partsOld.length; i++) {
							partsNew[i] = partsOld[i];
						}
						partsNew[partsNew.length - 1] = identity.getName();
						reloadEvent.setParticipants(partsNew);
					} else {
						final String[] partsNew = new String[] { identity.getName() };
						reloadEvent.setParticipants(partsNew);
					}
					// save calendar event
					final boolean successfullyDone = calManager.updateEventAlreadyInSync(cal, reloadEvent);
					if (!successfullyDone) {
						status.setEnrolled(false);
						status.setErrorMessage(DENStatus.ERROR_PERSISTING);
					}
					status.setEnrolled(true);
				}
			}
		});
		// success
		return status;
	}

	/**
	 * Deletes the already enrolled user from the date
	 * 
	 * @param identity
	 * @param event
	 * @param course
	 * @param userCourseEnv
	 * @return status
	 */
	public DENStatus cancelEnroll(final Identity identity, final KalendarEvent event, final OLATResourceable ores, final DENCourseNode courseNode) {
		final DENStatus status = new DENStatus();
		final ICourse course = CourseFactory.loadCourse(ores);
		final Kalendar cal = calManager.getCourseCalendar(course).getKalendar();
		final Collection<KalendarEvent> collEvents = cal.getEvents();
		// check if identity is enrolled
		if (!isEnrolledInDate(identity, event)) {
			status.setCancelled(false);
			status.setErrorMessage(DENStatus.ERROR_NOT_ENROLLED);
		}
		// cancel enroll in calendar entry
		if (event.getParticipants() != null) {
			final int currLength = event.getParticipants().length;
			if (currLength > 1) {
				// more than one are enrolled
				final String[] partsNew = new String[currLength - 1]; // one to delete
				final String[] partsOld = event.getParticipants();
				final String identityName = identity.getName();
				for (int i = 0, j = 0; i < partsOld.length; i++) {
					if (!(partsOld[i].equals(identityName))) {
						partsNew[j] = partsOld[i];
						j++; // only increment if new entry was made
					}
				}
				event.setParticipants(partsNew);
			} else if (currLength == 1) {
				// only one is enrolled, only simple reset needed
				event.setParticipants(new String[0]);
			}
			// save calendar event
			final boolean successfullyDone = calManager.updateEventFrom(cal, event);
			if (!successfullyDone) {
				status.setCancelled(false);
				status.setErrorMessage(DENStatus.ERROR_PERSISTING);
				return status;
			}
		} else {
			// no one to cancel
			status.setCancelled(false);
			status.setErrorMessage(DENStatus.ERROR_GENERAL);
			return status;
		}
		status.setCancelled(true);
		// delete date from the users calendar
		final Kalendar userCal = calManager.getPersonalCalendar(identity).getKalendar();
		final Collection<KalendarEvent> userEvents = userCal.getEvents();
		final String sourceNodeId = event.getSourceNodeId();
		for (final KalendarEvent userEvent : userEvents) {
			final String eventSourceNodeId = userEvent.getSourceNodeId();
			if (eventSourceNodeId != null && eventSourceNodeId.equals(sourceNodeId)) {
				calManager.removeEventFrom(userCal, userEvent);
				break;
			}
		}
		// success
		return status;
	}

	/**
	 * Persists settings of the date enrollment
	 * 
	 * @param lstEvents
	 * @param course
	 * @param denNode
	 */
	public void persistDENSettings(final List<KalendarEvent> lstEvents, final OLATResourceable ores, final DENCourseNode denNode) {
		final ICourse course = CourseFactory.loadCourse(ores);
		final Kalendar cal = calManager.getCourseCalendar(course).getKalendar();
		final String sourceNode = denNode.getIdent();
		// remove deleted events
		final Collection<KalendarEvent> allEvents = new ArrayList<KalendarEvent>(cal.getEvents());
		for (final KalendarEvent event : allEvents) {
			if (event.getSourceNodeId() != null) {
				if (event.getSourceNodeId().equals(sourceNode) && !lstEvents.contains(event)) {
					removeDateInUserCalendar(event);
					calManager.removeEventFrom(cal, event);
				}
			}
		}

		for (final KalendarEvent newEvent : lstEvents) {
			createKalendarEventLinks(course, denNode, newEvent);
			final KalendarEvent oldEvent = cal.getEvent(newEvent.getID());
			// new event?
			if (oldEvent != null) {
				// event is already in the calendar so first remove it
				calManager.removeEventFrom(cal, oldEvent);
			}
			// now add the event to the course calendar
			calManager.addEventTo(cal, newEvent);
			// update event in user calendars
			addDateInUserCalendar(newEvent);
		}
	}

	/**
	 * Checks if person is enrolled in specific calendar entry
	 * 
	 * @param identity
	 * @param event
	 * @return true if person is enrolled, otherwise false
	 */
	protected boolean isEnrolledInDate(final Identity identity, final KalendarEvent event) {
		if (event.getParticipants() == null) { return false; }
		return isSubstringInStringArray(identity.getName(), event.getParticipants());
	}

	/**
	 * Checks if person is already enrolled in any calendar entry of this enrollment or not
	 * 
	 * @param identity
	 * @param course
	 * @param userCourseEnv
	 * @return true if person already enrolled in one date, otherwise false
	 */
	protected boolean isAlreadyEnrolled(final Identity identity, final Collection<KalendarEvent> collEvents, final DENCourseNode courseNode) {
		boolean alreadyIn = false;
		for (final KalendarEvent event : collEvents) {
			// first check the correct course node and following if person is in
			if (event.getSourceNodeId() != null) {
				if (event.getSourceNodeId().equals(courseNode.getIdent())) {
					alreadyIn = isEnrolledInDate(identity, event);// check every event if the identity is already in
				}
			}
			if (alreadyIn == true) {
				break;// if identity is in any date enrolled, break and return true
			}
		}
		return alreadyIn;
	}

	/**
	 * Checks if the number of maximum participants is reached
	 * 
	 * @param event
	 * @return true if it is full, otherwise false
	 */
	protected boolean isDateFull(final KalendarEvent event) {
		if (event.getParticipants() != null) {
			if (event.getNumParticipants() == event.getParticipants().length) { return true; }
		}

		return false;
	}

	/**
	 * Returns all generated Events for this course and node
	 * 
	 * @param courseId
	 * @param sourseNodeId
	 * @return List of all KalendarEvent in this date enrollment
	 */
	protected List<KalendarEvent> getDENEvents(final Long courseId, final String sourceNodeId) {
		final List<KalendarEvent> denEvents = new ArrayList<KalendarEvent>();
		final ICourse course = CourseFactory.loadCourse(courseId);
		final Kalendar cal = calManager.getCourseCalendar(course).getKalendar();
		final Collection<KalendarEvent> colEvents = cal.getEvents();
		for (final KalendarEvent event : colEvents) {
			final String eventSourceNodeId = event.getSourceNodeId();
			if (eventSourceNodeId != null) {
				if (eventSourceNodeId.equals(sourceNodeId)) {
					denEvents.add(event);
				}
			}
		}

		return denEvents;
	}

	/**
	 * Count of dates for this date enrollment
	 * 
	 * @param courseRessourcableId
	 * @param courseNodeIdent
	 * @return number of dates
	 */
	public int getEventCount(final Long courseId, final String courseNodeId) {
		if (courseId != null && courseNodeId != null) {
			List events = null;
			try {
				events = getDENEvents(courseId, courseNodeId);
			} catch (final Exception e) {
				// nothing to do
			}
			if (events != null) { return events.size(); }
		}

		return 0;
	}

	/**
	 * This method generates the needed dates
	 * 
	 * @param retakes
	 * @param durationStr
	 * @param pauseStr
	 * @param subjectStr
	 * @param commentStr
	 * @param locationStr
	 * @param begin
	 * @param numParticipants
	 * @param dataList
	 * @param denCourseNode
	 * @return List<KalendarEvent> with the generated dates
	 */
	protected List<KalendarEvent> generateDates(final String subjectStr, final String commentStr, final String locationStr, final String durationStr,
			final String pauseStr, final Date begin, final int retakes, final int numParticipants, final List<KalendarEvent> dataList, final String denCourseNodeId) {
		Date nextEvent = null;
		for (int i = 0; i < retakes; i++) {
			StringTokenizer strTok = new StringTokenizer(durationStr, ":", false);
			// duration in milliseconds
			final int duration = 1000 * 60 * 60 * Integer.parseInt(strTok.nextToken()) + 1000 * 60 * Integer.parseInt(strTok.nextToken());
			strTok = new StringTokenizer(pauseStr, ":", false);
			// pause in milliseconds
			final int pause = 1000 * 60 * 60 * Integer.parseInt(strTok.nextToken()) + 1000 * 60 * Integer.parseInt(strTok.nextToken());
			KalendarEvent newEvent;
			if (nextEvent == null) {
				newEvent = new KalendarEvent(CodeHelper.getGlobalForeverUniqueID(), subjectStr, begin, duration);
			} else {
				newEvent = new KalendarEvent(CodeHelper.getGlobalForeverUniqueID(), subjectStr, nextEvent, duration);
			}
			newEvent.setNumParticipants(numParticipants);
			newEvent.setLocation(locationStr);
			newEvent.setComment(commentStr);
			newEvent.setSourceNodeId(denCourseNodeId);
			dataList.add(newEvent);
			// prepare next Event
			final long newTime = newEvent.getEnd().getTime() + pause;
			nextEvent = new Date(newTime);
		}

		return dataList;
	}

	/**
	 * Update one date in a list of KalendarEvents with the given informations from a manage dates form
	 * 
	 * @param datesForm of type DENEditManageDatesForm
	 * @param dataList
	 * @param index
	 * @return list with actualized KalendarEvents
	 */
	protected List<KalendarEvent> updateDateInList(final String subjectStr, final String commentStr, final String locationStr, final String durationStr,
			final Date begin, final int numParticipants, final List<KalendarEvent> dataList, final int index) {
		final StringTokenizer strTok = new StringTokenizer(durationStr, ":", false);
		final int duration = 1000 * 60 * 60 * Integer.parseInt(strTok.nextToken()) + 1000 * 60 * Integer.parseInt(strTok.nextToken());
		final KalendarEvent oldEvent = dataList.get(index);
		final KalendarEvent newEvent = new KalendarEvent(oldEvent.getID(), subjectStr, begin, duration);
		newEvent.setNumParticipants(numParticipants);
		newEvent.setLocation(locationStr);
		newEvent.setComment(commentStr);
		newEvent.setSourceNodeId(oldEvent.getSourceNodeId());
		newEvent.setParticipants(oldEvent.getParticipants());
		dataList.remove(index);
		dataList.add(index, newEvent);

		return dataList;
	}

	/**
	 * Update multiple dates in a list of KalendarEvents with the given informations from a manage dates form
	 * 
	 * @param datesForm of type DENEditManageDatesForm
	 * @param dataList
	 * @param selectedDates
	 * @return list with actualized KalendarEvents
	 */
	protected List<KalendarEvent> updateMultipleDatesInList(final String subjectStr, final String commentStr, final String locationStr, final String movementGapStr,
			final int numParticipants, final List<KalendarEvent> dataList, final BitSet selectedDates) {
		for (int i = 0; i < dataList.size(); i++) {
			if (selectedDates.get(i)) {
				final KalendarEvent oldEvent = dataList.get(i);
				final StringTokenizer strTok = new StringTokenizer(movementGapStr.substring(1), ":", false);
				final int gap = 1000 * 60 * 60 * Integer.parseInt(strTok.nextToken()) + 1000 * 60 * Integer.parseInt(strTok.nextToken());
				Date newBegin, newEnd;
				if (movementGapStr.startsWith("+")) {
					newBegin = new Date(oldEvent.getBegin().getTime() + gap);
					newEnd = new Date(oldEvent.getEnd().getTime() + gap);
				} else {
					newBegin = new Date(oldEvent.getBegin().getTime() - gap);
					newEnd = new Date(oldEvent.getEnd().getTime() - gap);
				}
				final KalendarEvent newEvent = new KalendarEvent(dataList.get(i).getID(), subjectStr.equals(new String()) ? oldEvent.getSubject() : subjectStr, newBegin,
						newEnd);
				if (numParticipants != 0) {
					newEvent.setNumParticipants(numParticipants);
				} else {
					newEvent.setNumParticipants(oldEvent.getNumParticipants());
				}
				if (!locationStr.equals(new String())) {
					newEvent.setLocation(locationStr);
				} else {
					newEvent.setLocation(oldEvent.getLocation());
				}
				if (!commentStr.equals(new String())) {
					newEvent.setComment(commentStr);
				} else {
					newEvent.setComment(oldEvent.getComment());
				}
				newEvent.setParticipants(oldEvent.getParticipants());
				newEvent.setSourceNodeId(oldEvent.getSourceNodeId());
				dataList.remove(i);
				dataList.add(i, newEvent);
			}
		}

		return dataList;
	}

	/**
	 * Add this event in the calendar of an enrolled user
	 * 
	 * @param newEvent
	 */
	private void addDateInUserCalendar(final KalendarEvent newEvent) {
		final String[] participants = newEvent.getParticipants();
		if (participants == null) { return;// no users to update, cancel
		}
		final BaseSecurity manager = BaseSecurityManager.getInstance();
		for (final String participant : participants) {
			final Identity identity = manager.findIdentityByName(participant);
			final Kalendar userCal = calManager.getPersonalCalendar(identity).getKalendar();
			final Collection<KalendarEvent> userEvents = new ArrayList<KalendarEvent>();
			userEvents.addAll(userCal.getEvents());
			final KalendarEvent userNewEvent = new KalendarEvent(CodeHelper.getGlobalForeverUniqueID(), newEvent.getSubject(), newEvent.getBegin(), newEvent.getEnd());
			userNewEvent.setLocation(newEvent.getLocation());
			userNewEvent.setSourceNodeId(newEvent.getSourceNodeId());
			userNewEvent.setClassification(KalendarEvent.CLASS_PRIVATE);
			final List kalendarEventLinks = userNewEvent.getKalendarEventLinks();
			kalendarEventLinks.clear();
			kalendarEventLinks.addAll(newEvent.getKalendarEventLinks());
			calManager.addEventTo(userCal, userNewEvent);
		}
	}

	/**
	 * Removes this event in all calendars of enrolled users
	 * 
	 * @param oldEvent
	 */
	private void removeDateInUserCalendar(final KalendarEvent oldEvent) {
		final String[] participants = oldEvent.getParticipants();
		if (participants == null) { return;// no users to update, cancel
		}
		final BaseSecurity manager = BaseSecurityManager.getInstance();
		for (final String participant : participants) {
			final Identity identity = manager.findIdentityByName(participant);
			final Kalendar userCal = calManager.getPersonalCalendar(identity).getKalendar();
			final Collection<KalendarEvent> userEvents = new ArrayList<KalendarEvent>();
			userEvents.addAll(userCal.getEvents());
			for (final KalendarEvent userEvent : userEvents) {
				final String sourceNodeId = userEvent.getSourceNodeId();
				if (sourceNodeId != null && sourceNodeId.equals(oldEvent.getSourceNodeId())) {
					calManager.removeEventFrom(userCal, userEvent);
				}
			}
		}
	}

	/**
	 * Create the table for the manage dates view
	 * 
	 * @param ureq
	 * @param wControl
	 * @param trans
	 * @param listener
	 * @param tableData DENEditTableDataModel
	 * @return TableController
	 */
	protected TableController createManageDatesTable(final UserRequest ureq, final WindowControl wControl, final Translator trans, final DENEditTableDataModel tableData) {
		final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setColumnMovingOffered(true);
		tableConfig.setResultsPerPage(15);
		tableConfig.setShowAllLinkEnabled(true);
		final TableController tableCntrl = new TableController(tableConfig, ureq, wControl, trans);
		tableCntrl.addColumnDescriptor(new DefaultColumnDescriptor("dates.table.date", 0, null, ureq.getLocale()));
		tableCntrl.addColumnDescriptor(new DefaultColumnDescriptor("dates.table.begin", 1, null, ureq.getLocale()));
		tableCntrl.addColumnDescriptor(new DefaultColumnDescriptor("dates.table.location", 3, null, ureq.getLocale()));
		tableCntrl.addColumnDescriptor(new DefaultColumnDescriptor("dates.table.duration", 2, null, ureq.getLocale()));
		tableCntrl.addColumnDescriptor(new DefaultColumnDescriptor("dates.table.comment", 4, null, ureq.getLocale()));
		tableCntrl.addColumnDescriptor(new DefaultColumnDescriptor("dates.table.participants.num", 5, null, ureq.getLocale()));
		tableCntrl.addMultiSelectAction("dates.table.edit.change", DENEditTableDataModel.CHANGE_ACTION);
		tableCntrl.addMultiSelectAction("dates.table.edit.delete", DENEditTableDataModel.DELETE_ACTION);
		tableCntrl.setMultiSelect(true);
		tableCntrl.setTableDataModel(tableData);
		tableCntrl.setSortColumn(2, true);// begin + multi select column

		return tableCntrl;
	}

	/**
	 * Create the table for the run view
	 * 
	 * @param ureq
	 * @param wControl
	 * @param trans
	 * @param listener
	 * @param tableData DENRunTableDataModel
	 * @return TableController
	 */
	protected TableController createRunDatesTable(final UserRequest ureq, final WindowControl wControl, final Translator trans, final DENRunTableDataModel tableData) {
		final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(trans.translate("dates.table.empty"));
		tableConfig.setColumnMovingOffered(true);
		final TableController tableCntrl = new TableController(tableConfig, ureq, wControl, trans);
		tableCntrl.addColumnDescriptor(new DefaultColumnDescriptor("dates.table.date", 0, null, ureq.getLocale()));
		tableCntrl.addColumnDescriptor(new DefaultColumnDescriptor("dates.table.begin", 1, null, ureq.getLocale()));
		tableCntrl.addColumnDescriptor(new DefaultColumnDescriptor("dates.table.location", 3, null, ureq.getLocale()));
		tableCntrl.addColumnDescriptor(new DefaultColumnDescriptor("dates.table.duration", 2, null, ureq.getLocale()));
		tableCntrl.addColumnDescriptor(new DefaultColumnDescriptor("dates.table.comment", 4, null, ureq.getLocale()));
		tableCntrl.addColumnDescriptor(new DefaultColumnDescriptor("dates.table.reserved", 5, null, ureq.getLocale()));
		tableCntrl.addColumnDescriptor(new DefaultColumnDescriptor("dates.table.status", 6, null, ureq.getLocale()));
		tableCntrl.addColumnDescriptor(new BooleanColumnDescriptor("dates.table.sign.in", 7, DENRunTableDataModel.CMD_ENROLL_IN_DATE, trans
				.translate("dates.table.sign.in"), trans.translate("dates.table.run.no_action")));
		tableCntrl.addColumnDescriptor(new BooleanColumnDescriptor("dates.table.sign.out", 8, DENRunTableDataModel.CMD_ENROLLED_CANCEL, trans
				.translate("dates.table.sign.out"), trans.translate("dates.table.run.no_action")));
		tableCntrl.setTableDataModel(tableData);
		tableCntrl.setSortColumn(1, true);// timeframe

		return tableCntrl;
	}

	/**
	 * Create the table for the list of participants view
	 * 
	 * @param ureq
	 * @param wControl
	 * @param trans
	 * @param listener
	 * @param tableData DENListTableDataModel
	 * @return TableController
	 */
	protected TableController createListParticipantsTable(final UserRequest ureq, final WindowControl wControl, final Translator trans,
			final DENListTableDataModel tableData) {
		final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(trans.translate("dates.table.empty"));
		tableConfig.setColumnMovingOffered(true);
		final TableController tableCntrl = new TableController(tableConfig, ureq, wControl, trans);
		tableCntrl.addColumnDescriptor(new DefaultColumnDescriptor("dates.table.date", 0, null, ureq.getLocale()));
		tableCntrl.addColumnDescriptor(new DefaultColumnDescriptor("dates.table.begin", 1, null, ureq.getLocale()));
		tableCntrl.addColumnDescriptor(new DefaultColumnDescriptor("dates.table.location", 3, null, ureq.getLocale()));
		tableCntrl.addColumnDescriptor(new DefaultColumnDescriptor("dates.table.duration", 2, null, ureq.getLocale()));
		tableCntrl.addColumnDescriptor(new DefaultColumnDescriptor("dates.table.comment", 4, null, ureq.getLocale()));
		tableCntrl.addColumnDescriptor(new DefaultColumnDescriptor("dates.table.participant.name", 5, null, ureq.getLocale()));
		tableCntrl.addColumnDescriptor(new DefaultColumnDescriptor("dates.table.participant.username", 6, null, ureq.getLocale()));
		tableCntrl.addColumnDescriptor(new BooleanColumnDescriptor("participants", 7, DENListTableDataModel.CHANGE_ACTION, trans
				.translate("dates.table.participant.manage"), ""));
		tableCntrl.addMultiSelectAction("dates.table.list.email", DENListTableDataModel.MAIL_ACTION);
		tableCntrl.addMultiSelectAction("dates.table.list.delete", DENListTableDataModel.DELETE_ACTION);
		tableCntrl.setMultiSelect(true);
		tableCntrl.setTableDataModel(tableData);
		tableCntrl.setSortColumn(2, true);// timeframe + multi select column

		return tableCntrl;
	}

	/**
	 * Create the table for the participants management view
	 * 
	 * @param ureq
	 * @param wControl
	 * @param trans
	 * @param listener
	 * @param tableData DENParticipantsTableDataModel
	 * @return TableController
	 */
	protected TableController createParticipantsTable(final UserRequest ureq, final WindowControl wControl, final Translator trans,
			final DENParticipantsTableDataModel tableData) {
		final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(trans.translate("dates.table.empty"));
		tableConfig.setColumnMovingOffered(true);
		final TableController tableCntrl = new TableController(tableConfig, ureq, wControl, trans);
		tableCntrl.addColumnDescriptor(new DefaultColumnDescriptor("dates.table.participant.firstname", 0, null, ureq.getLocale()));
		tableCntrl.addColumnDescriptor(new DefaultColumnDescriptor("dates.table.participant.lastname", 1, null, ureq.getLocale()));
		tableCntrl.addColumnDescriptor(new DefaultColumnDescriptor("dates.table.participant.email", 2, null, ureq.getLocale()));
		tableCntrl.addColumnDescriptor(new BooleanColumnDescriptor("dates.table.participant.email.send", 3, DENParticipantsTableDataModel.MAIL_ACTION, trans
				.translate("dates.table.participant.email.send"), ""));
		tableCntrl.addColumnDescriptor(new BooleanColumnDescriptor("dates.table.list.delete", 3, DENParticipantsTableDataModel.REMOVE_ACTION, trans
				.translate("dates.table.list.delete"), ""));
		tableCntrl.setTableDataModel(tableData);

		return tableCntrl;
	}

	/**
	 * Delete all participants in the events with the given IDs
	 * 
	 * @param course
	 * @param courseNode
	 * @param eventIDs of the events, where all participants should be deleted
	 * @return list with the actualized events
	 */
	protected List<KalendarEvent> deleteParticipants(final OLATResourceable ores, final DENCourseNode courseNode, final List<String> eventIDs) {
		final List<KalendarEvent> events = getDENEvents(ores.getResourceableId(), courseNode.getIdent());
		for (final KalendarEvent event : events) {
			if (eventIDs.contains(event.getID())) {
				removeDateInUserCalendar(event);
				event.setParticipants(new String[0]);
			}
		}
		persistDENSettings(events, ores, courseNode);
		return events;
	}

	/**
	 * Little helper method, that gives you all IDs of the selected events in the list
	 * 
	 * @param dataList
	 * @param selection BitSet
	 * @return list of strings with IDs
	 */
	protected List<String> getSelectedEventIDs(final List<KalendarEvent> dataList, final BitSet selection) {
		final List<String> lstIDs = new ArrayList<String>();
		for (int i = 0; i < dataList.size(); i++) {
			if (selection.get(i)) {
				lstIDs.add(dataList.get(i).getID());
			}
		}

		return lstIDs;
	}

	/**
	 * Little helper method, that gives you all participants of the selected events in the list
	 * 
	 * @param dataList
	 * @param selection BitSet
	 * @return list of Identities
	 */
	protected List<Identity> getSelectedEventParticipants(final List<KalendarEvent> dataList, final BitSet selection) {
		final List<Identity> identities = new ArrayList<Identity>();
		final BaseSecurity manager = BaseSecurityManager.getInstance();
		for (int i = 0; i < dataList.size(); i++) {
			if (selection.get(i)) {
				final String[] parts = dataList.get(i).getParticipants();
				if (parts != null) {
					for (final String participant : parts) {
						final Identity identity = manager.findIdentityByName(participant);
						if (identity != null) {
							identities.add(identity);
						}
					}
				}
			}
		}
		return identities;
	}

	/**
	 * Little helper method, that gives you all participants of a specific event
	 * 
	 * @param event
	 * @return list of Identities
	 */
	protected List<Identity> getEventParticipants(final KalendarEvent event) {
		List<Identity> identities = new ArrayList<Identity>();
		final List<KalendarEvent> lstEvent = new ArrayList<KalendarEvent>();
		lstEvent.add(event);
		final BitSet selection = new BitSet(1);
		selection.set(0);
		identities = getSelectedEventParticipants(lstEvent, selection);
		return identities;
	}

	/**
	 * Generates the mail window
	 * 
	 * @param ureq
	 * @param wControl
	 * @param listener
	 * @param velocity_root
	 * @param trans
	 * @param participants
	 * @return VelocityContainer
	 */
	protected VelocityContainer sendParticipantsMessage(final UserRequest ureq, final WindowControl wControl, final DefaultController listener,
			final String velocity_root, final Translator trans, final List<Identity> participants) {
		final VelocityContainer sendMessageVC = new VelocityContainer("sendmessage", velocity_root + "/sendmessage.html", trans, listener);
		final ContactMessage cmsg = new ContactMessage(ureq.getIdentity());
		ContactList contactList = null;
		if (participants.size() == 1) {
			contactList = new ContactList(participants.get(0).getUser().getProperty(UserConstants.EMAIL, ureq.getLocale()));
		} else {
			contactList = new ContactList(trans.translate("participants.message.to"));
		}
		contactList.addAllIdentites(participants);
		cmsg.addEmailTo(contactList);
		final ContactFormController contactCtr = new ContactFormController(ureq, wControl, false, false, false, false, cmsg);
		contactCtr.addControllerListener(listener);
		sendMessageVC.contextPut("title", trans.translate("participants.message"));
		sendMessageVC.put("contactForm", contactCtr.getInitialComponent());

		return sendMessageVC;
	}

	/**
	 * Generates the duration as string
	 * 
	 * @param event
	 * @return String with duration in format "hh:mm"
	 */
	protected String getDurationAsString(final KalendarEvent event) {
		final Date begin = event.getBegin();
		final Date end = event.getEnd();
		final long duration = end.getTime() - begin.getTime();
		final long hours = (long) Math.floor(duration / 1000 / 60 / 60);
		final long minutes = (duration / 1000 / 60 % 60);
		final StringBuilder sbHours = new StringBuilder();
		final StringBuilder sbMinutes = new StringBuilder();
		if (hours < 10) {
			sbHours.append(0);
		}
		if (minutes < 10) {
			sbMinutes.append(0);
		}
		sbHours.append(hours);
		sbMinutes.append(minutes);
		// in case of failure
		if (sbHours.length() > 2 || sbMinutes.length() > 2) { return "00:00"; }

		return new String(sbHours + ":" + sbMinutes);
	}

	/**
	 * Generates a mail template for users who were added to a date
	 * 
	 * @param identity
	 * @param event
	 * @param translator
	 * @return MailTemplate
	 */
	protected MailTemplate getAddedMailTemplate(final UserRequest ureq, final String subjectStr, final Translator translator) {
		final Identity identity = ureq.getIdentity();

		final String[] bodyArgs = new String[] { identity.getUser().getProperty(UserConstants.FIRSTNAME, ureq.getLocale()),
				identity.getUser().getProperty(UserConstants.LASTNAME, ureq.getLocale()), identity.getUser().getProperty(UserConstants.EMAIL, ureq.getLocale()),
				identity.getName(), subjectStr };

		final String subject = translator.translate("mail.participants.add.subject", bodyArgs);
		final String body = translator.translate("mail.participants.add.body", bodyArgs);

		final MailTemplate mailTempl = new MailTemplate(subject, body, null) {
			@Override
			public void putVariablesInMailContext(final VelocityContext context, final Identity identity) {
				// User user = identity.getUser();
				// context.put("firstname", user.getProperty(UserConstants.FIRSTNAME, ureq.getLocale()));
				// context.put("lastname", user.getProperty(UserConstants.LASTNAME, ureq.getLocale()));
				// context.put("login", identity.getName());
			}
		};

		return mailTempl;
	}

	/**
	 * Generates a mail template for users who were removed from a date
	 * 
	 * @param identity
	 * @param event
	 * @param trans
	 * @return MailTemplate
	 */
	protected MailTemplate getRemovedMailTemplate(final UserRequest ureq, final String subjectStr, final Translator trans) {
		final Identity identity = ureq.getIdentity();

		final String[] bodyArgs = new String[] { identity.getUser().getProperty(UserConstants.FIRSTNAME, ureq.getLocale()),
				identity.getUser().getProperty(UserConstants.LASTNAME, ureq.getLocale()), identity.getUser().getProperty(UserConstants.EMAIL, ureq.getLocale()),
				identity.getName(), subjectStr };

		final String subject = trans.translate("mail.participants.remove.subject", bodyArgs);
		final String body = trans.translate("mail.participants.remove.body", bodyArgs);

		final MailTemplate mailTempl = new MailTemplate(subject, body, null) {
			@Override
			public void putVariablesInMailContext(final VelocityContext context, final Identity identity) {
				// User user = identity.getUser();
				// context.put("firstname", user.getFirstName());
				// context.put("lastname", user.getLastName());
				// context.put("login", identity.getName());
			}
		};

		return mailTempl;
	}

	/*
	 * Helper methods
	 */
	private boolean isSubstringInStringArray(final String str, final String[] strArr) {
		for (final String tmpStr : strArr) {
			if (str.equals(tmpStr)) { return true; }
		}
		return false;
	}

	private void createKalendarEventLinks(final ICourse course, final DENCourseNode courseNode, final KalendarEvent event) {
		final List kalendarEventLinks = event.getKalendarEventLinks();
		final StringBuilder extLink = new StringBuilder();
		extLink.append(WebappHelper.getContextRoot()).append("/auth/repo/go?rid=");
		final RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(course, true);
		extLink.append(re.getKey()).append("&amp;par=").append(courseNode.getIdent());
		final String iconCssClass = CourseNodeFactory.getInstance().getCourseNodeConfiguration(courseNode.getType()).getIconCSSClass();
		final KalendarEventLink link = new KalendarEventLink("COURSE", courseNode.getIdent(), courseNode.getShortTitle(), extLink.toString(), iconCssClass);
		kalendarEventLinks.clear();
		kalendarEventLinks.add(link);
	}

	public String format(final String unformattedText) {
		return "<strong>" + unformattedText + "</strong>";
	}

	public String formatDuration(final long ms, final Translator translator) {
		boolean setEntry = false;
		final int day = (int) (ms / 1000 / 60 / 60 / 24);
		final int hour = (int) ((ms - (day * 24 * 60 * 60 * 1000)) / 1000 / 60 / 60);
		final int min = (int) ((ms - (day * 24 * 60 * 60 * 1000) - (hour * 60 * 60 * 1000)) / 1000 / 60);

		final StringBuilder sb = new StringBuilder();
		if (day != 0) {
			sb.append(day);
			sb.append(" ");
			if (day > 1) {
				sb.append(translator.translate("table.model.duration.days"));
			} else {
				sb.append(translator.translate("table.model.duration.day"));
			}
			setEntry = true;
		}
		if (hour != 0) {
			if (day != 0) {
				sb.append(" ");
			}
			sb.append(hour);
			sb.append(" ");
			if (hour > 1) {
				sb.append(translator.translate("table.model.duration.hours"));
			} else {
				sb.append(translator.translate("table.model.duration.hour"));
			}
			setEntry = true;
		}
		if (min != 0) {
			if (day != 0 || hour != 0) {
				sb.append(" ");
			}
			sb.append(min);
			sb.append(" ");
			if (min > 1) {
				sb.append(translator.translate("table.model.duration.mins"));
			} else {
				sb.append(translator.translate("table.model.duration.min"));
			}
			setEntry = true;
		}
		if (!setEntry) {
			sb.append("0 ");
			sb.append(translator.translate("table.model.duration.min"));
		}
		return sb.toString();
	}

	public long getDuration(final Date begin, final Date end) {
		if (end.before(begin)) { return getDuration(end, begin); }

		return end.getTime() - begin.getTime();
	}
}
