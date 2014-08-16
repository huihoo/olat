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

package org.olat.presentation.examples.site;

import java.util.List;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.navigation.AbstractSiteDefinition;
import org.olat.presentation.framework.core.control.navigation.SiteDefinition;
import org.olat.presentation.framework.core.control.navigation.SiteInstance;
import org.olat.presentation.framework.extensions.ExtensionResource;
import org.olat.system.commons.Settings;

/**
 * Description:<br>
 * TODO: Lavinia Dumitrescu Class Description for GUIDemoSiteDef
 * <P>
 * Initial Date: 11.09.2007 <br>
 * 
 * @author Lavinia Dumitrescu
 */
public class GUIDemoSiteDef extends AbstractSiteDefinition implements SiteDefinition {

    /**
	 * 
	 */
    protected GUIDemoSiteDef() {
        //
    }

    /**
	 */
    public String getName() {
        return "testsite";
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

    /**
	 */
    @Override
    public SiteInstance createSite(final UserRequest ureq, final WindowControl wControl) {
        SiteInstance si = null;
        if (Settings.isDebuging() && ureq.getUserSession().getRoles().isOLATAdmin()) {
            // only open for olat-admins
            si = new GUIDemoSite(ureq.getLocale());
        }
        return si;
    }

}
