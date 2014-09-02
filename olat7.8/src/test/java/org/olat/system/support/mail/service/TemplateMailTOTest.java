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
public class TemplateMailTOTest {

    private MailObjectMother mailObjectMother;

    @Before
    public void setup() {
        mailObjectMother = new MailObjectMother();
    }

    @Test
    public void getToMailAddress() {
        TemplateMailTO templateMailTO = mailObjectMother.createTemplateMailTO();
        assertEquals("Wrong to-address", mailObjectMother.toAddress, templateMailTO.getToMailAddress());
    }

    @Test
    public void getFromMailAddress() {
        TemplateMailTO templateMailTO = mailObjectMother.createTemplateMailTO();
        assertEquals("Wrong from-address", mailObjectMother.fromMailAddress, templateMailTO.getFromMailAddress());
    }

    @Test
    public void getSubject() {
        TemplateMailTO templateMailTO = mailObjectMother.createTemplateMailTO();
        assertEquals("Wrong subject", mailObjectMother.subject, templateMailTO.getSubject());
    }

    @Test
    public void hasCcMailAddress_defaultWithoutCc() {
        TemplateMailTO templateMailTO = mailObjectMother.createTemplateMailTO();
        assertFalse("Default TemplateMailTo should not habe a CC-address", templateMailTO.hasCcMailAddress());
    }

    @Test
    public void hasReplyTo_defaultWithoutReplyTo() {
        TemplateMailTO templateMailTO = mailObjectMother.createTemplateMailTO();
        assertFalse("Default TemplateMailTo should not habe a Reply-to-address", templateMailTO.hasReplyTo());
    }

    @Test
    public void setCcMailAddress_withCc() {
        TemplateMailTO templateMailTO = mailObjectMother.createTemplateMailTO();
        templateMailTO.setCcMailAddress(mailObjectMother.ccAddress);
        assertTrue("Missing cc-address after setting cc-address", templateMailTO.hasCcMailAddress());
        assertFalse("Should not have reply-to-address after setting cc-address", templateMailTO.hasReplyTo());
        assertEquals("Wrong cc-address", mailObjectMother.ccAddress, templateMailTO.getCcMailAddress());
    }

    @Test
    public void setReplyTo_withCcAndReplyTo() {
        TemplateMailTO templateMailTO = mailObjectMother.createTemplateMailTO();
        templateMailTO.setReplyTo(mailObjectMother.replyAddress);
        assertFalse("Should not have cc-address after setting cc-address", templateMailTO.hasCcMailAddress());
        assertTrue("Missing cc-address after setting reply-to-address", templateMailTO.hasReplyTo());
        assertEquals("Wrong reply-to-address", mailObjectMother.replyAddress, templateMailTO.getReplyTo());
    }

}
