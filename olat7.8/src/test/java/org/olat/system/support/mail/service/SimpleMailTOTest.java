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
package org.olat.system.support.mail.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.olat.system.support.mail.impl.MailObjectMother;

/**
 * Initial Date: 20.12.2011 <br>
 * 
 * @author cg
 */
public class SimpleMailTOTest {

    private MailObjectMother mailObjectMother;

    @Before
    public void setup() {
        mailObjectMother = new MailObjectMother();
    }

    @Test
    public void getToMailAddress() {
        SimpleMailTO simpleMailTO = mailObjectMother.createSimpleMailTO();
        assertEquals("Wrong to-address", mailObjectMother.toAddress, simpleMailTO.getToMailAddress());
    }

    @Test
    public void getFromMailAddress() {
        SimpleMailTO simpleMailTO = mailObjectMother.createSimpleMailTO();
        assertEquals("Wrong from-address", mailObjectMother.fromMailAddress, simpleMailTO.getFromMailAddress());
    }

    @Test
    public void getSubject() {
        SimpleMailTO simpleMailTO = mailObjectMother.createSimpleMailTO();
        assertEquals("Wrong subject", mailObjectMother.subject, simpleMailTO.getSubject());
        assertEquals(mailObjectMother.bodyText, simpleMailTO.getBodyText());
    }

    @Test
    public void getBodyText() {
        SimpleMailTO simpleMailTO = mailObjectMother.createSimpleMailTO();
        assertEquals("Wrong body text", mailObjectMother.bodyText, simpleMailTO.getBodyText());
    }

    @Test
    public void simpleMailTO_defaultWithoutCc() {
        SimpleMailTO simpleMailTO = mailObjectMother.createSimpleMailTO();
        assertFalse("Default SimpleMailTo should not habe a CC-address", simpleMailTO.hasCcMailAddress());
    }

    @Test
    public void simpleMailTO_defaultWithoutReplyTo() {
        SimpleMailTO simpleMailTO = mailObjectMother.createSimpleMailTO();
        assertFalse("Default SimpleMailTo should not habe a reply-to-address", simpleMailTO.hasReplyTo());
    }

    @Test
    public void simpleMailTO_withCc() {
        SimpleMailTO simpleMailTO = mailObjectMother.createSimpleMailTO();
        simpleMailTO.setCcMailAddress(mailObjectMother.ccAddress);
        assertTrue("Missing cc-address after setting cc-address", simpleMailTO.hasCcMailAddress());
        assertFalse("Should not have reply-to-address after setting cc-address", simpleMailTO.hasReplyTo());
        assertEquals("Wrong cc-address", mailObjectMother.ccAddress, simpleMailTO.getCcMailAddress());
    }

    @Test
    public void simpleMailTO_withReplyTo() {
        SimpleMailTO simpleMailTO = mailObjectMother.createSimpleMailTO();
        simpleMailTO.setReplyTo(mailObjectMother.replyAddress);
        assertFalse("Should not have cc-address after setting reply-to-address", simpleMailTO.hasCcMailAddress());
        assertTrue("Missing reply-to-address after setting reply-to-address", simpleMailTO.hasReplyTo());
        assertEquals("Wrong reply-to-address", mailObjectMother.replyAddress, simpleMailTO.getReplyTo());
    }
}
