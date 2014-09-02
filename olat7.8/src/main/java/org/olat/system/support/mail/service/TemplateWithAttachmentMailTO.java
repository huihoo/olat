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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Mail template with user defined body text, attachments, and default footer.
 * 
 * Initial Date: 27.09.2012 <br>
 * 
 * @author lavinia
 */
public class TemplateWithAttachmentMailTO extends TemplateMailTO {

    private String body; // TODO: is this needed?
    private List<File> attachments;

    private TemplateWithAttachmentMailTO(String toMailAddress, String fromMailAddress, String subject, String body, String templateLocation) {
        super(toMailAddress, fromMailAddress, subject, templateLocation);

        attachments = new ArrayList<File>();
        this.body = body;
        this.validate();
    }

    public static TemplateWithAttachmentMailTO getValidInstance(String toMailAddress, String fromMailAddress, String subject, String body, String templateLocation) {
        return new TemplateWithAttachmentMailTO(toMailAddress, fromMailAddress, subject, body, templateLocation);
    }

    public List<File> getAttachments() {
        return attachments;
    }

    public void addAttachment(File file) {
        attachments.add(file);
    }

    public void validate() {
        super.validate();
        if (body == null || body.isEmpty()) {
            throw new IllegalArgumentException("body is not set.");
        }
    }

}
