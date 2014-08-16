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
package org.olat.system.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;
import org.olat.system.security.OLATPrincipal;

/**
 * TODO: Class Description for MailerWithTemplateTest
 * 
 * <P>
 * Initial Date: Oct 5, 2011 <br>
 * 
 * @author patrick
 */
public class MailerWithTemplateTest {

    private MailerWithTemplate mailerWithTemplate;
    private OLATPrincipal helene;
    private OLATPrincipal mia;
    private OLATPrincipal nicolas;
    private OLATPrincipal peter;
    private OLATPrincipal heidi;
    private OLATPrincipal ruedi;
    private MailPackageStaticDependenciesWrapper webappAndMailhelperMock;
    private String subject;
    private String body;
    private String coursename;
    private String courseURL;
    private MailTemplate template;
    private ArrayList<OLATPrincipal> recipients;
    private ArrayList<OLATPrincipal> recipientsCC;
    private ArrayList<OLATPrincipal> recipientsBCC;
    private InternetAddress[] recipientsAsInternetAddresses;

    @Before
    public void setupMailerWithTemplate() {
        webappAndMailhelperMock = mock(MailPackageStaticDependenciesWrapper.class);
        when(webappAndMailhelperMock.getSystemEmailAddress()).thenReturn(ObjectMother.OLATADMIN_EMAIL);
        when(webappAndMailhelperMock.getMailhost()).thenReturn("disabled");
        configureWebappHelperMockToReturnAMimeMessageOnCreateMessage();

        MailerWithTemplate.setUnittestingInstanceWith(webappAndMailhelperMock);
        mailerWithTemplate = MailerWithTemplate.getInstance();
    }

    private void configureWebappHelperMockToReturnAMimeMessageOnCreateMessage() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(
                webappAndMailhelperMock.createMessage(any(InternetAddress.class), any(InternetAddress[].class), any(InternetAddress[].class),
                        any(InternetAddress[].class), any(String.class), any(String.class), any(File[].class), any(MailerResult.class))).thenReturn(mimeMessage);
    }

    @Before
    public void setupMailTemplate() throws AddressException {

        helene = ObjectMother.createHeleneMeyerPrincipial();
        mia = ObjectMother.createMiaBrennerPrincipal();
        nicolas = ObjectMother.createNicolas33Principal();
        peter = ObjectMother.createPeterBichselPrincipal();
        heidi = ObjectMother.createHeidiBirkenstockPrincipal();
        ruedi = ObjectMother.createRuediZimmermannPrincipal();

        subject = "For Each Subject: Hello $firstname $lastname";
        body = "For Each Body: \n\n You ($login) should go to	 \n\n'$coursename' @ $courseURL$login";

        coursename = "my course";
        courseURL = "http://www.mytrashmail.com/myTrashMail_inbox.aspx?email=";

        template = new MailTemplate(subject, body, null, null) {
            @Override
            public void putVariablesInMailContext(VelocityContext context, OLATPrincipal principal) {
                if (principal == null) {
                    // DESIGNCRITIQUE
                    // MailTemplate does not prescribe not null constraint for principal
                    // e.g. each MailTemplate implementer has to deal with this correctly.
                    // at least it should be noted in the abstract method declaration that null principals have to be handled.
                    return;
                }
                // Put user variables
                context.put("firstname", principal.getAttributes().getFirstName());
                context.put("lastname", principal.getAttributes().getLastName());
                context.put("login", principal.getName());
                // Put variables from greater context, eg. course id, group name etc.
                context.put("coursename", coursename);
                context.put("courseURL", courseURL);
            }
        };

        // some recipients data
        recipients = new ArrayList<OLATPrincipal>();
        recipients.add(helene);
        recipients.add(mia);
        recipients.add(nicolas);
        recipientsAsInternetAddresses = ObjectMother.getPrivateEmailAsInternetAddressesFor(nicolas, mia, helene);

        recipientsCC = new ArrayList<OLATPrincipal>();
        recipientsCC.add(peter);
        recipientsCC.add(heidi);
        recipientsBCC = new ArrayList<OLATPrincipal>();
        recipientsBCC.add(ruedi);
    }

    @Test
    public void testEachRecipientOnToForSendMailAsSeparateMails() {
        // Setup
        MailerResult result = null;
        int numberOfSeparateMails = recipients.size();

        // Execute
        result = mailerWithTemplate.sendMailAsSeparateMails(recipients, null, null, template, ruedi);
        // verify
        assertEquals(MailerResult.OK, result.getReturnCode());
        verifyTimesCallingCreateAndSendMessageInvocationsFor(numberOfSeparateMails, recipients, null, null);
    }

    @Test
    public void testEachRecipientOnToAndCCForSendMailAsSeparateMails() {
        // Setup
        MailerResult result = new MailerResult();
        int numberOfSeparateMails = recipients.size();
        numberOfSeparateMails += recipientsCC.size();
        // Execute
        result = mailerWithTemplate.sendMailAsSeparateMails(recipients, recipientsCC, null, template, ruedi);
        // verify
        assertEquals(MailerResult.OK, result.getReturnCode());
        verifyTimesCallingCreateAndSendMessageInvocationsFor(numberOfSeparateMails, recipients, recipientsCC, null);
    }

    @Test
    public void testEachRecipientOnToAndBCCForSendMailAsSeparateMails() {
        // Setup
        MailerResult result = new MailerResult();
        int numberOfSeparateMails = recipients.size();
        numberOfSeparateMails += recipientsBCC.size();
        // Execute
        result = mailerWithTemplate.sendMailAsSeparateMails(recipients, null, recipientsBCC, template, ruedi);
        // verify
        assertEquals(MailerResult.OK, result.getReturnCode());
        verifyTimesCallingCreateAndSendMessageInvocationsFor(numberOfSeparateMails, recipients, null, recipientsBCC);
    }

    private void verifyTimesCallingCreateAndSendMessageInvocationsFor(int numberOfSeparateMails, List<OLATPrincipal> recipients, List<OLATPrincipal> recipientsCC,
            List<OLATPrincipal> recipientsBCC) {
        InternetAddress[] nullAddresses = null;
        File[] nullFiles = null;

        verify(webappAndMailhelperMock, times(numberOfSeparateMails)).createMessage(any(InternetAddress.class),
                (recipients == null ? aryEq(nullAddresses) : any(InternetAddress[].class)), (recipientsCC == null ? aryEq(nullAddresses) : any(InternetAddress[].class)),
                (recipientsBCC == null ? aryEq(nullAddresses) : any(InternetAddress[].class)), any(String.class), any(String.class), aryEq(nullFiles),
                any(MailerResult.class));
        verify(webappAndMailhelperMock, times(numberOfSeparateMails)).send(any(MimeMessage.class), any(MailerResult.class));
    }

    @Test
    public void testWithoutMailhostConfiguredTheSendAsSeparateMailsFails() {
        // setup
        when(webappAndMailhelperMock.getMailhost()).thenReturn(null);
        // exercise
        MailerResult result = mailerWithTemplate.sendMailAsSeparateMails(recipients, null, null, template, ruedi);
        // verify
        assertEquals(MailerResult.MAILHOST_UNDEFINED, result.getReturnCode());
    }

    @Test
    public void testForRecipientAddressErrorIfNoRecipientsProvidedForSendAsSeparateMails() {
        // setup
        // exercise
        MailerResult result = mailerWithTemplate.sendMailAsSeparateMails(null, null, null, template, ruedi);
        // verify
        assertEquals(MailerResult.RECIPIENT_ADDRESS_ERROR, result.getReturnCode());
        verifyTimesCallingCreateAndSendMessageInvocationsFor(0, null, null, null);
    }

    @Test
    public void testForRecipientAddressErrorIfInvalidRecipientProvidedForSendAsSeparateMails() {
        // setup
        OLATPrincipal invalidRecipient = ObjectMother.createInvalidPrincipal();

        List<OLATPrincipal> invalidRecipients = new ArrayList<OLATPrincipal>();
        invalidRecipients.add(invalidRecipient);
        // exercise
        MailerResult result = mailerWithTemplate.sendMailAsSeparateMails(invalidRecipients, null, null, template, ruedi);
        // verify
        assertEquals(MailerResult.RECIPIENT_ADDRESS_ERROR, result.getReturnCode());
        verifyTimesCallingCreateAndSendMessageInvocationsFor(0, null, null, null);
    }

    @Test
    public void testForRecipientAddressErrorIfInvalidSenderProvidedForSendAsSeparateMails() {
        // setup
        OLATPrincipal invalidRecipient = ObjectMother.createInvalidPrincipal();
        List<OLATPrincipal> invalidRecipients = new ArrayList<OLATPrincipal>();
        invalidRecipients.add(invalidRecipient);
        // exercise
        MailerResult result = mailerWithTemplate.sendMailAsSeparateMails(invalidRecipients, null, null, template, ruedi);
        // verify
        assertEquals(MailerResult.RECIPIENT_ADDRESS_ERROR, result.getReturnCode());
        verifyTimesCallingCreateAndSendMessageInvocationsFor(0, null, null, null);
    }

    @Test
    public void testSendMailFromRuediToPeterUsingTemplateContextWithoutOtherRecipients() {
        // setup
        // exercise
        MailerResult result = mailerWithTemplate.sendMailUsingTemplateContext(peter, null, null, template, ruedi);
        // verify
        assertEquals(MailerResult.OK, result.getReturnCode());
        List<OLATPrincipal> recipient = new ArrayList<OLATPrincipal>();
        recipient.add(peter);
        verifyTimesCallingCreateAndSendMessageInvocationsFor(1, recipient, null, null);
    }

    @Test
    public void testSendMailFromSystemToPeterUsingTemplateContextWithoutOtherRecipients() {
        // setup
        OLATPrincipal systemAsSender = null;
        // exercise
        MailerResult result = mailerWithTemplate.sendMailUsingTemplateContext(peter, null, null, template, systemAsSender);
        // verify
        assertEquals(MailerResult.OK, result.getReturnCode());
        List<OLATPrincipal> recipient = new ArrayList<OLATPrincipal>();
        recipient.add(peter);
        verifyTimesCallingCreateAndSendMessageInvocationsFor(1, recipient, null, null);
    }

    @Test
    public void testValidAndInvalidPrincipalsInRecipientToForSendAsSeparateMails() {
        // setup
        OLATPrincipal invalidRecipient = ObjectMother.createInvalidPrincipal();
        recipients.add(invalidRecipient);
        int numberOfSeparateMails = recipients.size();
        MailerResult result = new MailerResult();
        //
        result = mailerWithTemplate.sendMailAsSeparateMails(recipients, null, recipientsBCC, template, ruedi);
        // verify
        assertEquals(MailerResult.OK, result.getReturnCode());
        assertNotNull("failed email must be reported, hence not null", result.getFailedIdentites());
        assertEquals(1, result.getFailedIdentites().size());
        verifyTimesCallingCreateAndSendMessageInvocationsFor(numberOfSeparateMails, recipients, null, recipientsBCC);
    }

    @Test
    public void testValidRecipientsButInvalidSenderForSendAsSeparateMails() {
        // setup
        OLATPrincipal invalidSender = ObjectMother.createInvalidPrincipal();
        recipients.add(invalidSender);
        MailerResult result = new MailerResult();
        //
        result = mailerWithTemplate.sendMailAsSeparateMails(recipients, null, recipientsBCC, template, invalidSender);
        // verify
        assertEquals(MailerResult.SENDER_ADDRESS_ERROR, result.getReturnCode());
        verifyTimesCallingCreateAndSendMessageInvocationsFor(0, null, null, null);
    }

    @Test
    public void testValidToAndInvalidCCAndInvalidBCCForSendAsSeparateMails() {
        // setup
        OLATPrincipal invalidPrincipal = ObjectMother.createInvalidPrincipal();
        recipientsCC.add(invalidPrincipal);
        recipientsBCC.add(invalidPrincipal);
        int failedIdentities = 2;
        int numberOfSeparateMails = recipients.size();
        numberOfSeparateMails += recipientsCC.size();

        // BCC Senders are in reality sent as "TO" inside the mailer.
        // This means, that for invalid recipients on the BCC
        // no MIME Message is created. We have to subtract the invalid users
        // on the BCC from the expected numberOfSeparateMails.
        // DESIGNCRITIQUE: In the MailerWithTemplate there are BCC arrays of Principals
        // which are handed around. But the MailerWithTemplate.sendMailAsSeparateMails Method
        // transforms the EMailaddresses handed in as BCC into EMailaddresses which go
        // into the TO Field.
        numberOfSeparateMails += recipientsBCC.size() - 1;

        MailerResult result = new MailerResult();
        //
        result = mailerWithTemplate.sendMailAsSeparateMails(recipients, recipientsCC, recipientsBCC, template, ruedi);
        // verify
        assertEquals(MailerResult.OK, result.getReturnCode());
        assertNotNull("failed email must be reported, hence not null", result.getFailedIdentites());
        assertEquals(failedIdentities, result.getFailedIdentites().size());
        verifyTimesCallingCreateAndSendMessageInvocationsFor(numberOfSeparateMails, recipients, recipientsBCC, recipientsBCC);
    }

}
