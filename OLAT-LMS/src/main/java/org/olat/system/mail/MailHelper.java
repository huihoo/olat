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
package org.olat.system.mail;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.WebappHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.security.OLATPrincipal;

/**
 * Description:<br>
 * Some mail helpers
 * <P>
 * Initial Date: 21.11.2006 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH<br>
 *         http://www.frentix.com
 */
public class MailHelper {

    private static final Logger log = LoggerHelper.getLogger();

    private static String mailhost;
    private static String mailhostTimeout;
    private static Authenticator smtpAuth; // null or the username/pwd used for
                                           // authentication
    private static boolean sslEnabled = false;
    private static boolean sslCheckCertificate = false;

    private static int maxSizeOfAttachments = 5;

    static {
        mailhost = WebappHelper.getMailConfig("mailhost");
        mailhostTimeout = WebappHelper.getMailConfig("mailTimeout");
        sslEnabled = Boolean.parseBoolean(WebappHelper.getMailConfig("sslEnabled"));
        sslCheckCertificate = Boolean.parseBoolean(WebappHelper.getMailConfig("sslCheckCertificate"));

        String smtpUser = null, smtpPwd = null;
        if (WebappHelper.isMailHostAuthenticationEnabled()) {
            smtpUser = WebappHelper.getMailConfig("smtpUser");
            smtpPwd = WebappHelper.getMailConfig("smtpPwd");
            smtpAuth = new MailerSMTPAuthenticator(smtpUser, smtpPwd);
        } else {
            smtpAuth = null;
        }

        if (log.isDebugEnabled()) {
            log.debug("using smtp host::" + mailhost + " with timeout::" + mailhostTimeout + " , smtpUser::" + smtpUser + " and smtpPwd::" + smtpPwd);
        }

        String maxSizeStr = WebappHelper.getMailConfig("mailAttachmentMaxSize");
        if (StringHelper.containsNonWhitespace(maxSizeStr)) {
            maxSizeOfAttachments = Integer.parseInt(maxSizeStr);
        }

    }

    /**
     * Create a configures mail message object that is ready to use
     * 
     * @return MimeMessage
     */
	static MimeMessage createMessage() {
        Properties p = new Properties();
        p.put("mail.smtp.host", mailhost);
        p.put("mail.smtp.timeout", mailhostTimeout);
        p.put("mail.smtp.connectiontimeout", mailhostTimeout);
        p.put("mail.smtp.ssl.enable", sslEnabled);
        p.put("mail.smtp.ssl.checkserveridentity", sslCheckCertificate);
        Session mailSession;
        if (smtpAuth == null) {
            mailSession = javax.mail.Session.getInstance(p);
        } else {
            // use smtp authentication from configuration
            p.put("mail.smtp.auth", "true");
            mailSession = Session.getDefaultInstance(p, smtpAuth);
        }
        if (log.isDebugEnabled()) {
            // enable mail session debugging on console
            mailSession.setDebug(true);
        }
        return new MimeMessage(mailSession);
    }

    /**
     * create MimeMessage from given fields, this may be used for creation of the email but sending it later. E.g. previewing the email first.
     * 
     * @param from
     * @param recipients
     * @param recipientsCC
     * @param recipientsBCC
     * @param body
     * @param subject
     * @param attachments
     * @param result
     * @return
     */
	static MimeMessage createMessage(Address from, Address[] recipients, Address[] recipientsCC, Address[] recipientsBCC, String body, String subject,
            File[] attachments, MailerResult result) {
        if (log.isDebugEnabled()) {
            doDebugMessage(from, recipients, recipientsCC, recipientsBCC, body, subject, attachments);
        }

        MimeMessage msg = MailHelper.createMessage();
        try {
            // TO, CC and BCC
            msg.setFrom(from);
            msg.setRecipients(RecipientType.TO, recipients);
            if (recipientsCC != null) {
                msg.setRecipients(RecipientType.CC, recipientsCC);
            }
            if (recipientsBCC != null) {
                msg.setRecipients(RecipientType.BCC, recipientsBCC);
            }
            // message data
            msg.setSubject(subject, "utf-8");

            if (attachments != null && attachments.length > 0) {
                // with attachment use multipart message
                Multipart multipart = new MimeMultipart();
                // 1) add body part
                BodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setText(body);
                multipart.addBodyPart(messageBodyPart);
                // 2) add attachments
                for (File attachmentFile : attachments) {
                    // abort if attachment does not exist
                    if (attachmentFile == null || !attachmentFile.exists()) {
                        result.setReturnCode(MailerResult.ATTACHMENT_INVALID);
                        log.error("Tried to send mail wit attachment that does not exist::" + (attachmentFile == null ? null : attachmentFile.getAbsolutePath()));
                        return msg;
                    }
                    messageBodyPart = new MimeBodyPart();
                    DataSource source = new FileDataSource(attachmentFile);
                    messageBodyPart.setDataHandler(new DataHandler(source));
                    messageBodyPart.setFileName(attachmentFile.getName());
                    multipart.addBodyPart(messageBodyPart);
                }
                // Put parts in message
                msg.setContent(multipart);
            } else {
                // without attachment everything is easy, just set as text
                msg.setText(body, "utf-8");
            }
            msg.setSentDate(new Date());
            msg.saveChanges();
        } catch (MessagingException e) {
            result.setReturnCode(MailerResult.SEND_GENERAL_ERROR);
            log.warn("Could not create MimeMessage", e);
        }
        //
        return msg;
    }

    /**
     * Send an email message to the given TO, CC and BCC address. The result will be stored in the result object. The message can contain attachments.<br>
     * At this point HTML mails are not supported.
     * 
     * @param from
     *            Address used as sender address. Must not be NULL
     * @param recipients
     *            Address array used as sender addresses. Must not be NULL and contain at lease one address
     * @param recipientsCC
     *            Address array used as CC addresses. Can be NULL
     * @param recipientsBCC
     *            Address array used as BCC addresses. Can be NULL
     * @param body
     *            Body text of message. Must not be NULL
     * @param subject
     *            Subject text of message. Must not be NULL
     * @param attachments
     *            File array used as attachments. Can be NULL
     * @param result
     *            MailerResult object that stores the result code
     */
    public static void sendMessage(Address from, Address[] recipients, Address[] recipientsCC, Address[] recipientsBCC, String body, String subject, File[] attachments,
            MailerResult result) {
        //
        MimeMessage msg = createMessage(from, recipients, recipientsCC, recipientsBCC, body, subject, attachments, result);
        sendMessage(msg, result);
    }

    /**
     * send email with MimeMessage available
     * 
     * @param msg
     * @param result
     */
	static void sendMessage(MimeMessage msg, MailerResult result) {
        try {
            if (mailhost == null || mailhost.length() == 0 || mailhost.equalsIgnoreCase("disabled")) {
                result.setReturnCode(MailerResult.MAILHOST_UNDEFINED);
                log.info("Did not send mail , mailhost undefined");
                return;
            }
            if (mailhost.equalsIgnoreCase("testing")) {
                result.setReturnCode(MailerResult.OK);
                log.info("Mail not sent , mailhost TESTING mode.");
                return;
            }
            if (result.getReturnCode() == MailerResult.OK) {
                // now send the mail
                Transport.send(msg);
            }
        } catch (MessagingException e) {
            result.setReturnCode(MailerResult.SEND_GENERAL_ERROR);
            log.warn("Could not send mail", e);
        }
    }

    /**
     * @return the maximum size allowed for attachements in MB (default 5MB)
     */
    public static int getMaxSizeForAttachement() {
        return maxSizeOfAttachments;
    }

    /**
     * @return the configured mail host. Can be null, indicating that the system should not send any mail at all
     */
	static Object getMailhost() {
        return mailhost;
    }

    /**
     * Checks if the given mail address is potentially a valid email address that can be used to send emails. It does NOT check if the mail address exists, it checks only
     * for syntactical validity.
     * 
     * @param mailAddress
     * @return
     */
    public static boolean isValidEmailAddress(String mailAddress) {
        return EmailAddressValidator.isValidEmailAddress(mailAddress);
    }

    /**
     * Internal helper
     * 
     * @param from
     * @param recipients
     * @param recipientsCC
     * @param recipientsBCC
     * @param body
     * @param subject
     * @param attachments
     */
    private static void doDebugMessage(Address from, Address[] recipients, Address[] recipientsCC, Address[] recipientsBCC, String body, String subject,
            File[] attachments) {
        String to = new String();
        String cc = new String();
        String bcc = new String();
        String att = new String();
        for (Address addr : recipients) {
            to = to + "'" + addr.toString() + "' ";
        }
        if (recipientsCC != null) {
            for (Address addr : recipientsCC) {
                cc = cc + "'" + addr.toString() + "' ";
            }
        }
        if (recipientsBCC != null) {
            for (Address addr : recipientsBCC) {
                bcc = bcc + "'" + addr.toString() + "' ";
            }
        }
        if (attachments != null) {
            for (File file : attachments) {
                if (file != null)
                    att = att + "'" + file.getAbsolutePath() + "' ";
            }
        }
        log.debug("Sending mail from::'" + from + "' to::" + to + " CC::" + cc + " BCC::" + bcc + " subject::" + subject + " body::" + body + " attachments::" + att);
    }

    /**
     * check for disabled mail address
     * 
     * @param recipients
     * @param result
     * @return
     */
    public static MailerResult removeDisabledMailAddress(List<? extends OLATPrincipal> identities, MailerResult result) {
        boolean value = false;
        if (identities != null) {
            for (OLATPrincipal principal : identities) {
                value = principal.getAttributes().isEmailDisabled();
                if (value) {
                    result.addFailedIdentites(principal);
                    if (result.getReturnCode() != MailerResult.RECIPIENT_ADDRESS_ERROR) {
                        result.setReturnCode(MailerResult.RECIPIENT_ADDRESS_ERROR);
                    }
                }
            }
        }
        return result;
    }
}
