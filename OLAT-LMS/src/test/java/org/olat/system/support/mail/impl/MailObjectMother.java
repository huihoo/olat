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
package org.olat.system.support.mail.impl;

import org.olat.system.support.mail.service.SimpleMailTO;
import org.olat.system.support.mail.service.TemplateMailTO;

/**
 * Initial Date: 20.12.2011 <br>
 * 
 * @author cg
 */
public class MailObjectMother {

    public String toAddress = "to_address@test.tst";
    public String fromMailAddress = "from_address@test.tst";
    public String subject = "Test Subject";
    public String bodyText = "Test Body Text";

    public String ccAddress = "cc_address@test.tst";
    public String replyAddress = "reply_address@test.tst";

    // Used for template-based mail
    String templateLocation = "org/olat/system/support/mail/impl/_content/testMailTemplate.html";

    public SimpleMailTO createSimpleMailTO() {
        return SimpleMailTO.getValidInstance(toAddress, fromMailAddress, subject, bodyText);
    }

    public TemplateMailTO createTemplateMailTO() {
        return TemplateMailTO.getValidInstance(toAddress, fromMailAddress, subject, templateLocation);
    }

}
