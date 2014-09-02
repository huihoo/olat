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
 * frentix GmbH, Switzerland, http://www.frentix.com
 * <p>
 */

package org.olat.presentation.course.nodes.cal;

import java.util.List;
import java.util.Locale;

import org.olat.lms.course.nodes.CalCourseNode;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.presentation.course.nodes.AbstractCourseNodeConfiguration;
import org.olat.presentation.course.nodes.CourseNodeConfiguration;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.framework.extensions.ExtensionResource;

/**
 * <h3>Description:</h3> Course node configuration for calendar
 * <p>
 * Initial Date: 4 nov. 2009 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class CalCourseNodeConfiguration extends AbstractCourseNodeConfiguration implements CourseNodeConfiguration {

    private CalCourseNodeConfiguration() {
        super();
    }

    @Override
    public CourseNode getInstance() {
        return new CalCourseNode();
    }

    /**
	 */
    @Override
    public String getLinkText(final Locale locale) {
        final Translator fallback = PackageUtil.createPackageTranslator(CourseNodeConfiguration.class, locale);
        final Translator translator = PackageUtil.createPackageTranslator(this.getClass(), locale, fallback);
        return translator.translate("calendar.title");
    }

    /**
	 */
    @Override
    public String getIconCSSClass() {
        return "o_cal_icon";
    }

    /**
	 */
    @Override
    public String getLinkCSSClass() {
        return null;
    }

    @Override
    public String getAlias() {
        return CalCourseNode.TYPE;
    }

    //
    // OLATExtension interface implementations.
    //
    public String getName() {
        return getAlias();
    }

    /**
	 */
    public List getExtensionResources() {
        // no resources, part of main css
        return null;
    }

    /**
	 */
    public ExtensionResource getExtensionCSS() {
        // no resources, part of main css
        return null;
    }

}
