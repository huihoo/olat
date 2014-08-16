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
 * Copyright (c) 2008 frentix GmbH, Switzerland<br>
 * <p>
 */

package org.olat.presentation.course.nodes.info;

import java.util.List;
import java.util.Locale;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.course.CourseGroupsEBL;
import org.olat.lms.course.ICourse;
import org.olat.lms.repository.RepositoryService;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.infomessage.SendMailOption;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Send mails to members, coaches and owner of the course
 * <P>
 * Initial Date: 29 jul. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SendMembersMailOption implements SendMailOption {

    public final ICourse course;
    public final RepositoryService rm;
    private final CourseGroupsEBL courseGroupsEBL;

    public SendMembersMailOption(final ICourse course, final RepositoryService rm) {
        this.course = course;
        this.rm = rm;
        courseGroupsEBL = CoreSpringFactory.getBean(CourseGroupsEBL.class);
    }

    @Override
    public String getOptionKey() {
        return "send-mail-course-members";
    }

    @Override
    public String getOptionTranslatedName(final Locale locale) {
        final Translator translator = PackageUtil.createPackageTranslator(SendMembersMailOption.class, locale);
        return translator.translate("wizard.step1.send_option.member");
    }

    public List<Identity> getSelectedIdentities() {
        return courseGroupsEBL.getSelectedIdentities(course);
    }
}
