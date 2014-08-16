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

import java.util.Locale;

import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.InfoCourseNode;
import org.olat.presentation.course.nodes.AbstractCourseNodeConfiguration;
import org.olat.presentation.course.nodes.CourseNodeConfiguration;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;

/**
 * Description:<br>
 * The configuration of the info message course node
 * <P>
 * Initial Date: 3 aug. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InfoCourseNodeConfiguration extends AbstractCourseNodeConfiguration implements CourseNodeConfiguration {

    public static final String CONFIG_DURATION = "duration";
    public static final String CONFIG_LENGTH = "length";

    // public static final String CONFIG_AUTOSUBSCRIBE = "autosubscribe"; //removed during 1011 - any migration necessary?

    protected InfoCourseNodeConfiguration() {
    }

    @Override
    public String getAlias() {
        return "info";
    }

    @Override
    public CourseNode getInstance() {
        return new InfoCourseNode();
    }

    @Override
    public String getLinkText(final Locale locale) {
        final Translator fallback = PackageUtil.createPackageTranslator(CourseNodeConfiguration.class, locale);
        final Translator translator = PackageUtil.createPackageTranslator(this.getClass(), locale, fallback);
        return translator.translate("title_info");
    }

    @Override
    public String getIconCSSClass() {
        return "o_infomsg_icon";
    }

    @Override
    public String getLinkCSSClass() {
        return null;
    }
}
