/**
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * Copyright (c) 2005-2009 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 * All rights reserved.
 */
package de.bps.course.nodes.cl;

import java.util.List;
import java.util.Locale;

import org.olat.presentation.framework.extensions.ExtensionResource;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.nodes.AbstractCourseNodeConfiguration;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;

import de.bps.course.nodes.ChecklistCourseNode;

/**
 * Description:<br>
 * Configuration of checklist course node
 * <P>
 * Initial Date: 23.07.2009 <br>
 * 
 * @author bja <bja@bps-system.de>
 */
public class ChecklistCourseNodeConfiguration extends AbstractCourseNodeConfiguration implements CourseNodeConfiguration {

	private ChecklistCourseNodeConfiguration() {}

	/**
	 * @see org.olat.course.nodes.CourseNodeConfiguration#getAlias()
	 */
	@Override
	public String getAlias() {
		return "cl";
	}

	/**
	 * @see org.olat.course.nodes.CourseNodeConfiguration#getIconCSSClass()
	 */
	@Override
	public String getIconCSSClass() {
		return "o_cl_icon";
	}

	/**
	 * @see org.olat.course.nodes.CourseNodeConfiguration#getInstance()
	 */
	@Override
	public CourseNode getInstance() {
		return new ChecklistCourseNode();
	}

	/**
	 * @see org.olat.course.nodes.CourseNodeConfiguration#getLinkCSSClass()
	 */
	@Override
	public String getLinkCSSClass() {
		return "o_cl_icon";
	}

	/**
	 * @see org.olat.course.nodes.CourseNodeConfiguration#getLinkText(java.util.Locale)
	 */
	@Override
	public String getLinkText(final Locale locale) {
		final Translator fallback = Util.createPackageTranslator(CourseNodeConfiguration.class, locale);
		final Translator translator = Util.createPackageTranslator(this.getClass(), locale, fallback);
		return translator.translate("title_cl");
	}

	/**
	 * @see org.olat.presentation.framework.extensions.OLATExtension#getExtensionCSS()
	 */
	public ExtensionResource getExtensionCSS() {
		return null;
	}

	/**
	 * @see org.olat.presentation.framework.extensions.OLATExtension#getExtensionResources()
	 */
	public List getExtensionResources() {
		return null;
	}

	/**
	 * @see org.olat.presentation.framework.extensions.OLATExtension#getName()
	 */
	public String getName() {
		return getAlias();
	}

}
