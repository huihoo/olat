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
package org.olat.presentation.course.nodes.feed.blog;

import java.util.List;
import java.util.Locale;

import org.olat.lms.course.nodes.BlogCourseNode;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.presentation.course.nodes.AbstractCourseNodeConfiguration;
import org.olat.presentation.course.nodes.CourseNodeConfiguration;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.framework.extensions.ExtensionResource;

/**
 * The blog course node configuration class
 * <P>
 * Initial Date: Mar 31, 2009 <br>
 * 
 * @author gwassmann
 */
public class BlogCourseNodeConfiguration extends AbstractCourseNodeConfiguration implements CourseNodeConfiguration {

    private static final String ICON_CSS_CLASS = "o_blog_icon";

    private BlogCourseNodeConfiguration() {
        super();
    }

    /**
	 */
    @Override
    public String getAlias() {
        return BlogCourseNode.TYPE;
    }

    /**
	 */
    @Override
    public String getIconCSSClass() {
        return ICON_CSS_CLASS;
    }

    /**
	 */
    @Override
    public CourseNode getInstance() {
        return new BlogCourseNode();
    }

    /**
	 */
    @Override
    public String getLinkCSSClass() {
        // No particular styles
        return null;
    }

    /**
	 */
    @Override
    public String getLinkText(final Locale locale) {
        final Translator fallback = PackageUtil.createPackageTranslator(CourseNodeConfiguration.class, locale);
        final Translator translator = PackageUtil.createPackageTranslator(this.getClass(), locale, fallback);
        return translator.translate("title_blog");
    }

    /**
	 */
    public ExtensionResource getExtensionCSS() {
        return null;
    }

    /**
	 */
    public List getExtensionResources() {
        // TODO: What is this? No extensions so far.
        return null;
    }

    /**
	 */
    public String getName() {
        return getAlias();
    }

}
