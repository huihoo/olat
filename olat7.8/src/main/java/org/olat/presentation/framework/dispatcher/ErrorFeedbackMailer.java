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

package org.olat.presentation.framework.dispatcher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.lms.core.notification.service.MailMessage;
import org.olat.lms.learn.notification.service.MailMessageLearnService;
import org.olat.system.commons.WebappHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Description:<br>
 * Send an Email to the support address
 * <P>
 * Initial Date: Jan 31, 2006 <br>
 * 
 * @author guido
 */
@Component("errorbean")
public class ErrorFeedbackMailer implements Dispatcher {

    private static final Logger LOG = LoggerHelper.getLogger();

    private static final ErrorFeedbackMailer INSTANCE = new ErrorFeedbackMailer();
    @Autowired
    private BaseSecurity baseSecurity;
    @Autowired
    private MailMessageLearnService mailMessageLearnService;

    private ErrorFeedbackMailer() {
        // private since singleton
    }

    protected static ErrorFeedbackMailer getInstance() {
        return INSTANCE;
    }

    /**
     * send email to olat support with user submitted error information
     * 
     * @param request
     */
    public void sendMail(HttpServletRequest request) {
        String feedback = request.getParameter("textarea");
        String username = request.getParameter("username");
        String errorNr = request.getParameter("errnum");
        String time = request.getParameter("time");
        try {
            Identity ident = baseSecurity.findIdentityByName(username);
            // if null, user may crashed before getting a valid session, try with guest user instead
            if (ident == null)
                ident = baseSecurity.findIdentityByName("guest");

            String errorNum = parseErrorNumber(errorNr);

            // logging statement for OLAT-6739
            if (LOG.isDebugEnabled()) {
                LOG.debug("Send error report from logfile (textarea: " + feedback + ", errorNr:" + errorNr + ", username:" + username + ", errorNum:" + errorNum + ")");
            }

            MailMessage message = createMailMessage(ident, feedback, errorNr, time, "");
            mailMessageLearnService.sendMessage(message);
        } catch (Exception e) {
            // error in recipient email address(es)
            handleException(request, e);
            return;
        }

    }

    private MailMessage createMailMessage(Identity fromIdentity, String feedback, String errorNr, String time, String logFileString) {
        String toEmail = WebappHelper.getMailConfig("mailSupport");
        List<String> toEmailAddresses = new ArrayList<String>();
        toEmailAddresses.add(toEmail);

        String subject = "Fehlerreport";
        String messageBody = getBodyText(fromIdentity, feedback, errorNr, time, logFileString);
        MailMessage mailMessage = new MailMessage(toEmailAddresses, fromIdentity, false, subject, messageBody, new ArrayList<File>());

        return mailMessage;
    }

    private String getBodyText(Identity fromIdentity, String feedback, String errorNr, String time, String logFileString) {
        StringBuffer sb = new StringBuffer();
        sb.append("Fehlernummer: ");
        sb.append(errorNr).append("\n");
        sb.append("Datum und Zeit: ");
        sb.append(time).append("\n\n");
        sb.append(feedback).append("\n\n");

        sb.append("--- from user: " + fromIdentity.getName()).append(" ---").append("\n");
        sb.append(logFileString);

        return sb.toString();
    }

    private String parseErrorNumber(String errorNr) {
        // try with format N<nodeId>-E<errorCode> first
        Pattern r1 = Pattern.compile("N[0-9]+-E[0-9]+");
        Matcher m1 = r1.matcher(errorNr);
        if (m1.find())
            return m1.group();

        Pattern r2 = Pattern.compile("E[0-9]+");
        Matcher m2 = r2.matcher(errorNr);
        if (m2.find())
            return m2.group();
        return "";

    }

    private void handleException(HttpServletRequest request, Exception e) {
        String feedback = request.getParameter("textarea");
        String username = request.getParameter("username");
        LOG.error("Error sending error feedback mail to olat support (" + WebappHelper.getMailConfig("mailSupport") + ") from: " + username + " with content: "
                + feedback, e);
    }

    /**
	 */
    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response, String uriPrefix) {
        sendMail(request);
        DispatcherAction.redirectToDefaultDispatcher(response);
    }

}
