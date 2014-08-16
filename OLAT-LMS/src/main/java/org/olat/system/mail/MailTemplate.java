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

import org.apache.velocity.VelocityContext;
import org.olat.system.security.OLATPrincipal;

/**
 * Description:<br>
 * The MailTemplate holds a mail subject/body template and the according methods to populate the velocity contexts with the user values
 * <P>
 * Usage:<br>
 * See MailerWithTemplateTest to learn how you can use this abstract class and how you have to implement the putVariablesInMailContext() method.
 * <p>
 * Initial Date: 21.11.2006 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH<br>
 *         http://www.frentix.com
 */
public abstract class MailTemplate {
    private String subjectTemplate;
    private String bodyTemplate;
    private File[] attachments;
    private VelocityContext context;
    private Boolean cpfrom;
    private String footerTemplate;

    /**
     * Constructor for a mail using a template
     * 
     * @param subjectTemplate
     *            Template for mail subject. Must not be NULL
     * @param bodyTemplate
     *            Template for mail body. Must not be NULL
     * @param attachments
     *            File array for mail attachments. Can be NULL
     */
    public MailTemplate(String subjectTemplate, String bodyTemplate, String footerTemplate, File[] attachments) {
        this.subjectTemplate = subjectTemplate;
        this.bodyTemplate = bodyTemplate;
        this.attachments = attachments;
        this.context = new VelocityContext();
        this.cpfrom = true;
        this.footerTemplate = footerTemplate;
    }

    /**
     * 
     * @param footer
     */
    public MailTemplate(String footer) {
        this.footerTemplate = footer;
    }

    /**
     * @return Returns the footerTemplate.
     */
    public String getFooterTemplate() {
        return footerTemplate;
    }

    /**
     * @param footerTemplate
     *            The footerTemplate to set.
     */
    public void setFooterTemplate(String footerTemplate) {
        this.footerTemplate = footerTemplate;
    }

    /**
     * @return
     */
    public Boolean getCpfrom() {
        return cpfrom;
    }

    /**
     * @param cpfrom
     */
    public void setCpfrom(Boolean cpfrom) {
        this.cpfrom = cpfrom;
    }

    /**
     * @return mail subject template as string
     */
    public String getSubjectTemplate() {
        return subjectTemplate;
    }

    /**
     * @return mail body template as string
     */
    public String getBodyTemplate() {
        return bodyTemplate;
    }

    /**
     * @return attachments as File array
     */
    public File[] getAttachments() {
        return attachments;
    }

    /**
     * @param attachments
     *            set file attachments
     */
    public void setAttachments(File[] attachments) {
        this.attachments = attachments;
    }

    /**
     * @param bodyTemplate
     *            Set body template
     */
    public void setBodyTemplate(String bodyTemplate) {
        this.bodyTemplate = bodyTemplate;
    }

    /**
     * @param subjectTemplate
     *            Set subject template
     */
    public void setSubjectTemplate(String subjectTemplate) {
        this.subjectTemplate = subjectTemplate;
    }

    /**
     * Method that puts all necessary variables for those templates into the give velocity context. This method must match all variables used in the subject and body
     * template.
     * 
     * "DESIGNCRITIQUE: MailTemplate does not define how to deal with null values for recipients, hence each client has to redefine and not to forget handling this situation."
     * 
     * @param context
     *            The context where to put the variables
     * @param recipient
     *            The current identity which will get the email
     */
    public abstract void putVariablesInMailContext(VelocityContext context, OLATPrincipal recipient);

    public void addToContext(String name, String value) {
        context.put(name, value);
    }

    public VelocityContext getContext() {
        return context;
    }
}
