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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.presentation.course.nodes.info;

import java.util.List;

import org.olat.data.infomessage.InfoMessage;
import org.olat.lms.commons.context.BusinessControlFactory;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.infomessage.MailFormatter;
import org.olat.lms.notifications.NotificationHelper;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.Settings;
import org.olat.system.spring.CoreSpringFactory;

import com.ibm.icu.text.DateFormat;

/**
 * Description:<br>
 * Format the email send after the creation of an info message in a course
 * <P>
 * Initial Date: 24 aug. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SendMailFormatterForCourse implements MailFormatter {

    private final String courseTitle;
    private final String businessPath;
    private final Translator translator;

    public SendMailFormatterForCourse(final String courseTitle, final String businessPath, final Translator translator) {
        this.courseTitle = courseTitle;
        this.translator = translator;
        this.businessPath = businessPath;
    }

    @Override
    public String getSubject(final InfoMessage msg) {
        return msg.getTitle();
    }

    @Override
    public String getBody(final InfoMessage msg) {
        final BusinessControlFactory bCF = BusinessControlFactory.getInstance();
        final List<ContextEntry> ceList = bCF.createCEListFromString(businessPath);
        final String busPath = NotificationHelper.getBusPathStringAsURIFromCEList(ceList);

        final String author = getUserService().getFirstAndLastname(msg.getAuthor().getUser());
        final String date = DateFormat.getDateInstance(DateFormat.MEDIUM, translator.getLocale()).format(msg.getCreationDate());
        final String link = Settings.getServerContextPathURI() + "/url/" + busPath;
        return translator.translate("mail.body", new String[] { courseTitle, author, date, msg.getMessage(), link });
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
