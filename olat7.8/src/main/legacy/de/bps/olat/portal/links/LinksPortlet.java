/**
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * Copyright (c) 2005-2008 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 * All rights reserved.
 * 
 * @author skoeber
 */
package de.bps.olat.portal.links;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.portal.AbstractPortlet;
import org.olat.presentation.framework.core.control.generic.portal.Portlet;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;

public class LinksPortlet extends AbstractPortlet {

	private String cssWrapperClass = "o_pt_w_if";

	protected static final String LANG_ALL = "*";
	protected static final String LANG_DE = "de";
	protected static final String LANG_EN = "en";
	protected static final String ACCESS_GUEST = "-";
	protected static final String ACCESS_REG = "+";
	protected static final String ACCESS_ALL = "*";

	// configuration file
	private static final String CONFIG_FILE = "/WEB-INF/olat_portals_links.xml";
	// configuration file xml elements
	private static final String ELEM_INSTITUTION = "University";
	private static final String ATTR_INSTITUTION_NAME = "name";
	private static final String ELEM_LINK = "Link";
	private static final String ELEM_LINK_TITLE = "Title";
	private static final String ELEM_LINK_URL = "URL";
	private static final String ELEM_LINK_DESC = "Description";
	private static final String ELEM_LINK_TARGET = "Target";
	private static final String ELEM_LINK_LANG = "Language";

	private static HashMap<String, PortletInstitution> content;
	private Controller runCtr;

	/**
	 * @see org.olat.presentation.framework.layout.generic.portal.AbstractPortlet#createInstance(org.olat.presentation.framework.layout.WindowControl, org.olat.gui.UserRequest, java.util.Map)
	 */
	@Override
	public Portlet createInstance(final WindowControl wControl, final UserRequest ureq, final Map configuration) {
		if (content == null) {
			init();
		}
		final LinksPortlet p = new LinksPortlet();
		p.setName(this.getName());
		p.setConfiguration(configuration);
		p.setTranslator(Util.createPackageTranslator(LinksPortlet.class, ureq.getLocale()));
		// override css class if configured
		final String cssClass = (String) configuration.get("cssWrapperClass");
		if (cssClass != null) {
			p.setCssWrapperClass(cssClass);
		}

		return p;
	}

	private void init() {
	private static final Logger log = LoggerHelper.getLogger();

		if (log.isDebugEnabled()) {
			log.debug("START: Loading remote portlets content.");
		}

		final File configurationFile = new File(WebappHelper.getContextRoot() + CONFIG_FILE);

		// this map contains the whole data
		final HashMap<String, PortletInstitution> portletMap = new HashMap<String, PortletInstitution>();

		final SAXReader reader = new SAXReader();
		try {
			final Document doc = reader.read(configurationFile);
			final Element rootElement = doc.getRootElement();
			final List<Element> lstInst = rootElement.elements(ELEM_INSTITUTION);
			for (final Element instElem : lstInst) {
				final String inst = instElem.attributeValue(ATTR_INSTITUTION_NAME);
				final List<Element> lstTmpLinks = instElem.elements(ELEM_LINK);
				final List<PortletLink> lstLinks = new ArrayList<PortletLink>(lstTmpLinks.size());
				for (final Element linkElem : lstTmpLinks) {
					final String title = linkElem.elementText(ELEM_LINK_TITLE);
					final String url = linkElem.elementText(ELEM_LINK_URL);
					final String target = linkElem.elementText(ELEM_LINK_TARGET);
					final String lang = linkElem.elementText(ELEM_LINK_LANG);
					final String desc = linkElem.elementText(ELEM_LINK_DESC);
					lstLinks.add(new PortletLink(title, url, target, lang, desc));
				}
				portletMap.put(inst, new PortletInstitution(inst, lstLinks));
			}
		} catch (final Exception e) {
			log.error("Error reading configuration file", e);
		} finally {
			content = portletMap;
		}
	}

	/**
	 * @see org.olat.presentation.framework.layout.generic.portal.Portlet#getTitle()
	 */
	@Override
	public String getTitle() {
		return getTranslator().translate("portlet.title");
	}

	/**
	 * @see org.olat.presentation.framework.layout.generic.portal.Portlet#getDescription()
	 */
	@Override
	public String getDescription() {
		return getTranslator().translate("portlet.description");
	}

	/**
	 * @see org.olat.presentation.framework.layout.generic.portal.Portlet#getInitialRunComponent(org.olat.presentation.framework.layout.WindowControl, org.olat.gui.UserRequest)
	 */
	@Override
	public Component getInitialRunComponent(final WindowControl wControl, final UserRequest ureq) {
		if (this.runCtr != null) {
			runCtr.dispose();
		}
		this.runCtr = new LinksPortletRunController(ureq, wControl);
		return runCtr.getInitialComponent();
	}

	/**
	 * @see org.olat.presentation.framework.layout.Disposable#dispose(boolean)
	 */
	@Override
	public void dispose() {
		disposeRunComponent();
	}

	/**
	 * @see org.olat.presentation.framework.layout.generic.portal.Portlet#getCssClass()
	 */
	@Override
	public String getCssClass() {
		return cssWrapperClass;
	}

	/**
	 * Helper used to overwrite the default css class with the configured class
	 * 
	 * @param cssWrapperClass
	 */
	void setCssWrapperClass(final String cssWrapperClass) {
		this.cssWrapperClass = cssWrapperClass;
	}

	/**
	 * @see org.olat.presentation.framework.layout.generic.portal.Portlet#disposeRunComponent(boolean)
	 */
	@Override
	public void disposeRunComponent() {
		if (runCtr != null) {
			runCtr.dispose();
			runCtr = null;
		}
	}

	/**
	 * @return Returns the content map.
	 */
	public static Map<String, PortletInstitution> getContent() {
		return content;
	}

}

/**
 * @author skoeber
 */
class PortletInstitution {

	private String name;
	private List<PortletLink> links;

	public PortletInstitution(final String name) {
		this.name = name;
		this.links = new ArrayList<PortletLink>();
	}

	public PortletInstitution(final String name, final List<PortletLink> links) {
		this.name = name;
		this.links = links;
	}

	public void addLink(final PortletLink link) {
		links.add(link);
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public List<PortletLink> getLinks() {
		return links;
	}

	public void setLinks(final List<PortletLink> links) {
		this.links = links;
	}
}

/**
 * @author skoeber
 */
class PortletLink {

	private String title, url, target, language, description;

	public PortletLink(final String title, final String url, final String target, final String language, final String description) {
		this.title = title;
		this.url = url;
		this.target = target;
		this.language = language;
		this.description = description;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(final String target) {
		this.target = target;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(final String language) {
		this.language = language;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}
}
