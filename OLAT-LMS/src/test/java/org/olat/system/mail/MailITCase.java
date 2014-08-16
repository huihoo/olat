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
 * Description:<br>
 * jUnit tests for the mail package
 * <P>
 * Initial Date: 21.11.2006 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH<br>
 *         http://www.frentix.com
 */

package org.olat.system.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.security.OLATPrincipal;
import org.olat.system.security.PrincipalAttributes;
import org.olat.test.OlatTestCase;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@Ignore("test which are really sending emails are not allowed in uzh environment. Unittestable code will be moved to appropriate places during 73x Staging")
public class MailITCase extends OlatTestCase {
    private static final Logger log = LoggerHelper.getLogger();

    private OLATPrincipal id1, id2;

    // for local debugging you can set a systemproperty to a maildomain where
    // you immediately get the mails. If the property is not set the
    // mytrashmail domain is used. You can get the mails there with your
    // bowser
    static String maildomain = System.getProperty("junit.maildomain");
    static {
        if (maildomain == null) {
            maildomain = "thankyou2010.com";
        }
    }

    /**
     * SetUp is called before each test.
     */
    @Before
    public void setup() {
        id1 = createOlatPrincipalMock("one");
        id2 = createOlatPrincipalMock("two");
    }

    private OLATPrincipal createOlatPrincipalMock(String login) {
        OLATPrincipal olatPrincipal = mock(OLATPrincipal.class);
        when(olatPrincipal.getName()).thenReturn(login);
        PrincipalAttributes attributes = mock(PrincipalAttributes.class);
        when(attributes.getFirstName()).thenReturn(login + "first");
        when(attributes.getLastName()).thenReturn(login + "last");
        when(attributes.getEmail()).thenReturn(login + "olattest@" + maildomain);
        when(olatPrincipal.getAttributes()).thenReturn(attributes);

        return olatPrincipal;
    }


    /**
     * Test for the mail template and the context variable methods
     */
    @Test
    public void testMailAttachments() {
        String subject = "Subject: Hello $firstname with attachment";
        String body = "Body: \n\n Hey $login, here's a file for you: ";

        // some attachemnts
        File[] attachments = new File[1];
        File file1, file2;
        try {
            System.out.println("MailITCase.testMailAttachments Url1=" + MailITCase.class.getResource("MailITCase.class"));
            file1 = new File(MailITCase.class.getResource("MailITCase.class").toURI());
            attachments[0] = file1;
            // TODO: cg Properties file is in olat_core.jar and not be lookup as resource (jar:file:...)
            // System.out.println("MailITCase.testMailAttachments Url2=" + MailITCase.class.getResource("_i18n/LocalStrings_de.properties") );
            // file2 = new File(MailITCase.class.getResource("_i18n/LocalStrings_de.properties").toURI());
            // attachments[1] = file2;
        } catch (URISyntaxException e) {
            fail("ups, can't get testfiles from local path: MailITCase.class and _i18n/LocalStrings_de.properties");
        }

        MailTemplate template = new MailTemplate(subject, body, null, attachments) {
            @Override
            public void putVariablesInMailContext(VelocityContext context, OLATPrincipal principal) {
                // Put user variables
                context.put("firstname", principal.getAttributes().getFirstName());
                context.put("login", principal.getName());
            }
        };

        // some recipients data
        List<OLATPrincipal> recipients = new ArrayList<OLATPrincipal>();
        recipients.add(id1);

        MailerResult result = new MailerResult();
        result = MailerWithTemplate.getInstance().sendMailAsSeparateMails(recipients, null, null, template, id2);
        assertEquals(MailerResult.OK, result.getReturnCode());
    }

    /**
     * Test for the mail template and the context variable methods
     */
    @Test
    public void testMailAttachmentsInvalid() {
        String subject = "Subject: Hello $firstname with attachment";
        String body = "Body: \n\n Hey $login, here's a file for you: ";

        // some attachemnts - but no file
        File[] attachments = new File[1];

        MailTemplate template = new MailTemplate(subject, body, null, attachments) {
            @Override
            public void putVariablesInMailContext(VelocityContext context, OLATPrincipal principal) {
                // Put user variables
                context.put("firstname", principal.getAttributes().getFirstName());
                context.put("login", principal.getName());
            }
        };

        // some recipients data
        List<OLATPrincipal> recipients = new ArrayList<OLATPrincipal>();
        recipients.add(id1);

        MailerResult result = new MailerResult();
        result = MailerWithTemplate.getInstance().sendMailAsSeparateMails(recipients, null, null, template, id2);
        assertEquals(MailerResult.ATTACHMENT_INVALID, result.getReturnCode());
    }

            }


