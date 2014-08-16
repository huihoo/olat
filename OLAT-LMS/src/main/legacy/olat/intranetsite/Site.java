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

package ch.goodsolutions.olat.intranetsite;

import java.util.Locale;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.layout.MainLayoutController;
import org.olat.presentation.framework.core.control.navigation.DefaultNavElement;
import org.olat.presentation.framework.core.control.navigation.NavElement;
import org.olat.presentation.framework.core.control.navigation.SiteInstance;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.run.RunMainController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

/**
 * Description:<br>
 * TODO: Felix Jost Class Description for HomeSite
 * <P>
 * Initial Date: 19.07.2005 <br>
 * 
 * @author Felix Jost
 */
public class Site implements SiteInstance {
	private final NavElement origNavElem;
	private NavElement curNavElem;

	private final String repositorySoftKey;

	/**
	 * @param loc
	 */
	public Site(final Locale loc, final String repositorySoftKey) {
		this.repositorySoftKey = repositorySoftKey;
		final Translator trans = Util.createPackageTranslator(Site.class, loc);
		origNavElem = new DefaultNavElement(trans.translate("site.title"), trans.translate("site.title.alt"), "site_demo_icon");
		curNavElem = new DefaultNavElement(origNavElem);
	}

	/**
	 * @see org.olat.navigation.SiteInstance#getNavElement()
	 */
	@Override
	public NavElement getNavElement() {
		return curNavElem;
	}

	/**
	 * @see org.olat.navigation.SiteInstance#createController(org.olat.presentation.framework.UserRequest, org.olat.presentation.framework.control.WindowControl)
	 */
	@Override
	public MainLayoutController createController(final UserRequest ureq, final WindowControl wControl) {
		final RepositoryManager rm = RepositoryManager.getInstance();
		final RepositoryEntry entry = rm.lookupRepositoryEntryBySoftkey(repositorySoftKey, true);
		final ICourse course = CourseFactory.loadCourse(entry.getOlatResource());
		final RunMainController c = new RunMainController(ureq, wControl, course, null, false, true);
		// needed for css style reasons: a site own the whole content area and needs either to use a MenuAndToolController for the 3-columns layout or,
		// like here, the contentOnlyController
		// ContentOnlyController coc = new ContentOnlyController(ureq, wControl, c);
		return c;
	}

	/**
	 * @see org.olat.navigation.SiteInstance#isKeepState()
	 */
	@Override
	public boolean isKeepState() {
		return true;
	}

	/**
	 * @see org.olat.navigation.SiteInstance#reset()
	 */
	@Override
	public void reset() {
		curNavElem = new DefaultNavElement(origNavElem);
	}

}
