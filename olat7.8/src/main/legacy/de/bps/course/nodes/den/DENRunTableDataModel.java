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

import java.util.Date;
import java.util.List;

import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;

import de.bps.course.nodes.DENCourseNode;

/**
 * TableDataModel for run view of date enrollment
 * 
 * @author skoeber
 */
public class DENRunTableDataModel extends DefaultTableDataModel {

	public static final String CMD_ENROLL_IN_DATE = "cmd.enroll.in.date";
	public static final String CMD_ENROLLED_CANCEL = "cmd.enrolled.cancel";

	// subject, begin, end, location, comment, enrolled/max participants, status, enrollBtn, cancelBtn
	private static final int COLUMN_COUNT = 7;

	private final DENManager denManager;
	private final Identity identity;
	private final DENCourseNode courseNode;
	private final Translator translator;
	private final Boolean cancelEnrollEnabled;

	/**
	 * Standard constructor of the table data model for run view
	 * 
	 * @param List of KalendarEvent objects
	 * @param ureq
	 * @param DENCourseNode courseNode
	 */
	public DENRunTableDataModel(final List objects, final UserRequest ureq, final DENCourseNode courseNode, final Boolean cancelEnrollEnabled, final Translator translator) {
		super(objects);
		denManager = DENManager.getInstance();
		identity = ureq.getIdentity();
		this.courseNode = courseNode;
		this.translator = translator;
		this.cancelEnrollEnabled = cancelEnrollEnabled;
	}

	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	@Override
	public Object getValueAt(final int row, final int col) {
		final KalendarEvent event = (KalendarEvent) objects.get(row);

		switch (col) {
			case 0:
				// subject
				return denManager.format(event.getSubject());
			case 1:
				final Formatter formatter = Formatter.getInstance(translator.getLocale());
				final String formattedDate = formatter.formatDateAndTime(event.getBegin());
				return denManager.format(formattedDate);
			case 2:
				final Date begin = event.getBegin();
				final Date end = event.getEnd();
				final long milliSeconds = denManager.getDuration(begin, end);

				return denManager.formatDuration(milliSeconds, translator);
			case 3:
				// location
				return denManager.format(event.getLocation());
			case 4:
				// comment
				return event.getComment();
			case 5:
				// enrolled & total
				final StringBuffer numStrBuf = new StringBuffer();
				final String[] participants = event.getParticipants();
				numStrBuf.append(participants == null ? "0" : participants.length);
				numStrBuf.append("/");
				numStrBuf.append(event.getNumParticipants());
				return numStrBuf.toString();
			case 6:
				// status
				final Boolean isEnrolled = denManager.isEnrolledInDate(identity, event);
				if (isEnrolled) {
					return translator.translate("dates.table.run.enrolled");
				} else if (denManager.isDateFull(event)) {
					return translator.translate("dates.table.run.full");
				} else {
					return translator.translate("dates.table.run.notenrolled");
				}
			case 7:
				// enroll
				if (denManager.isAlreadyEnrolled(identity, objects, courseNode)) { return Boolean.FALSE; }
				if ((event.getParticipants() != null) && (event.getParticipants().length >= event.getNumParticipants())) { return Boolean.FALSE; }
				return Boolean.TRUE;
			case 8:
				// cancel enrollment
				if (cancelEnrollEnabled) { return denManager.isEnrolledInDate(identity, event); }
				return Boolean.FALSE;

			default:
				return "error";
		}
	}

	public KalendarEvent getEntryAt(final int row) {
		return (KalendarEvent) objects.get(row);
	}

	public void setEntries(final List newEntries) {
		this.objects = newEntries;
	}
}
