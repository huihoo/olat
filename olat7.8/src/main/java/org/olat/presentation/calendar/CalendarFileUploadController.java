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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.presentation.calendar;

/* TODO: ORID-1007 'File' */
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.olat.data.calendar.CalendarDao;
import org.olat.lms.calendar.CalendarService;
import org.olat.lms.calendar.ImportCalendarManager;
import org.olat.lms.commons.LearnServices;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.commons.WebappHelper;
import org.olat.system.event.Event;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.logging.log4j.LoggerHelper;

import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.ParamPart;
import com.oreilly.servlet.multipart.Part;

/**
 * Description:<BR>
 * <P>
 * Initial Date: July 8, 2008
 * 
 * @author Udit Sajjanhar
 */
public class CalendarFileUploadController extends BasicController {

    private static final Logger log = LoggerHelper.getLogger();

    private final VelocityContainer calFileUploadVC;
    private static final String COMMAND_PROCESS_UPLOAD = "pul";
    private static final long fileUploadLimit = 1024;
    private final Link cancelButton;
    private ImportCalendarManager importCalendarManager;
    private CalendarService calendarService;

    CalendarFileUploadController(final UserRequest ureq, final Locale locale, final WindowControl wControl) {
        super(ureq, wControl);
        calendarService = getService(LearnServices.calendarService);
        importCalendarManager = getService(LearnServices.importCalendarManager);
        calFileUploadVC = createVelocityContainer("calFileUpload");
        cancelButton = LinkFactory.createButton("cancel", calFileUploadVC, this);
        putInitialPanel(calFileUploadVC);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == cancelButton) {
            fireEvent(ureq, Event.CANCELLED_EVENT);
        } else if (source == calFileUploadVC) { // those must be module links
            if (event.getCommand().equals(COMMAND_PROCESS_UPLOAD)) {
                // process calendar file upload
                processCalendarFileUpload(ureq);
            }
        }
    }

    private void processCalendarFileUpload(final UserRequest ureq) {
        // upload the file
        try {
            // don't worry about NullPointerExceptions.
            // we'll catch exceptions if any operation fails.
            final MultipartParser mpp = new MultipartParser(ureq.getHttpReq(), (int) fileUploadLimit * 1024);
            mpp.setEncoding("UTF-8");
            Part part;
            boolean fileWritten = false;
            while ((part = mpp.readNextPart()) != null) {
                if (part.isFile() && !fileWritten) {
                    final FilePart fPart = (FilePart) part;
                    final String type = fPart.getContentType();
                    // get file contents
                    log.warn(type + fPart.getFileName());
                    if (fPart != null && fPart.getFileName() != null && type.startsWith("text") && (type.toLowerCase().endsWith("calendar"))) {

                        // store the uploaded file by a temporary name
                        final String calID = importCalendarManager.getTempCalendarIDForUpload(ureq.getIdentity().getName());
                        final File tmpFile = calendarService.getCalendarFile(CalendarDao.TYPE_USER, calID);
                        fPart.writeTo(tmpFile);

                        // try to parse the tmp file
                        final Object calendar = calendarService.readCalendar(CalendarDao.TYPE_USER, calID);
                        if (calendar != null) {
                            fileWritten = true;
                        }

                        // the uploaded calendar file is ok.
                        fireEvent(ureq, Event.DONE_EVENT);
                    }
                } else if (part.isParam()) {
                    final ParamPart pPart = (ParamPart) part;
                    if (pPart.getName().equals("cancel")) {
                        // action cancelled
                        fireEvent(ureq, Event.CANCELLED_EVENT);
                    }
                }
            }

            if (!fileWritten) {
                getWindowControl().setError(getTranslator().translate("cal.import.form.format.error"));
            }

        } catch (final IOException ioe) {
            // exceeded UL limit
            log.warn("IOException in CalendarFileUploadController: ", ioe);
            final String slimitKB = String.valueOf(fileUploadLimit);
            final String supportAddr = WebappHelper.getMailConfig("mailSupport");// ->{0} für e-mail support e-mail adresse
            getWindowControl().setError(getTranslator().translate("cal.import.form.limit.error", new String[] { slimitKB, supportAddr }));
            return;
        } catch (final OLATRuntimeException e) {
            log.warn("Imported Calendar file not correct. Parsing failed.", e);
            getWindowControl().setError(getTranslator().translate("cal.import.parsing.failed"));
            return;
        } catch (final Exception e) {
            log.warn("Exception in CalendarFileUploadController: ", e);
            getWindowControl().setError(getTranslator().translate("cal.import.form.failed"));
            return;
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // do nothing here yet
    }

}
