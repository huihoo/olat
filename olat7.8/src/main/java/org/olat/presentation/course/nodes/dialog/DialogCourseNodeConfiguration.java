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

package org.olat.presentation.course.nodes.dialog;

import java.util.List;
import java.util.Locale;

import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.DialogCourseNode;
import org.olat.presentation.course.nodes.AbstractCourseNodeConfiguration;
import org.olat.presentation.course.nodes.CourseNodeConfiguration;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.framework.extensions.ExtensionResource;

/**
 * Description:<br>
 * Entry point to get a course node of type "dialog" see olat_extensions.xml and olat_buildingblocks.xml for enabling or disabling this node
 * <P>
 * Initial Date: 02.11.2005 <br>
 * 
 * @author guido
 */
public class DialogCourseNodeConfiguration extends AbstractCourseNodeConfiguration implements CourseNodeConfiguration {

    private DialogCourseNodeConfiguration() {
        super();
    }

    /**
	 */
    @Override
    public String getAlias() {
        return DialogCourseNode.TYPE;
    }

    /**
	 */
    @Override
    public CourseNode getInstance() {
        return new DialogCourseNode();
    }

    /**
	 */
    @Override
    public String getLinkText(final Locale locale) {
        final Translator fallback = PackageUtil.createPackageTranslator(CourseNodeConfiguration.class, locale);
        final Translator translator = PackageUtil.createPackageTranslator(this.getClass(), locale, fallback);
        return translator.translate("title_" + DialogCourseNode.TYPE);
    }

    /**
	 */
    @Override
    public String getIconCSSClass() {
        return "o_" + DialogCourseNode.TYPE + "_icon";
    }

    /**
	 */
    @Override
    public String getLinkCSSClass() {
        return null;
    }

    /**
	 */
    public String getName() {
        return getAlias();
    }

    /**
	 */
    public List getExtensionResources() {
        // no ressources, part of main css
        return null;
    }

    /**
	 */
    public ExtensionResource getExtensionCSS() {
        // no ressources, part of main css
        return null;
    }

}
