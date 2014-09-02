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

/**
 * Hosts some informations about the enrollment process
 * 
 * @author skoeber
 */
public class DENStatus {

	public static final String ERROR_ALREADY_ENROLLED = "alreadyEnrolled";
	public static final String ERROR_NOT_ENROLLED = "notEnrolled";
	public static final String ERROR_GENERAL = "generalError";
	public static final String ERROR_PERSISTING = "persistingError";
	public static final String ERROR_FULL = "isFull";

	private String errorMessage;
	private boolean isEnrolled, isCancelled;

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(final String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public boolean isEnrolled() {
		return isEnrolled;
	}

	public void setEnrolled(final boolean isEnrolled) {
		this.isEnrolled = isEnrolled;
	}

	public boolean isCancelled() {
		return isCancelled;
	}

	public void setCancelled(final boolean isCancelled) {
		this.isCancelled = isCancelled;
	}

}
