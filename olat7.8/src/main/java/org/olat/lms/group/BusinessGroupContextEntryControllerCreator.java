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
package org.olat.lms.group;

import org.olat.data.group.BusinessGroup;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.presentation.commons.context.ContextEntryControllerCreator;
import org.olat.presentation.framework.common.NewControllerFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.I18nPackage;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.group.BGControllerFactory;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.exception.OLATSecurityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <h3>Description:</h3>
 * <p>
 * This class can create run controllers for business groups for a given context entry
 * <p>
 * Initial Date: 19.08.2009 <br>
 * 
 * @author gnaegi, gnaegi@frentix.com, www.frentix.com
 */
@Service
public class BusinessGroupContextEntryControllerCreator implements ContextEntryControllerCreator {

    @Autowired
    BusinessGroupService businessGroupService;

    /**
     * [spring]
     */
    private BusinessGroupContextEntryControllerCreator() {
        NewControllerFactory.getInstance().addContextEntryControllerCreator(BusinessGroup.class.getSimpleName(), this);
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public Controller createController(final ContextEntry ce, final UserRequest ureq, final WindowControl wControl) {
        final OLATResourceable ores = ce.getOLATResourceable();

        final Long gKey = ores.getResourceableId();
        final BusinessGroup bgroup = businessGroupService.loadBusinessGroup(gKey, true);
        final boolean isOlatAdmin = ureq.getUserSession().getRoles().isOLATAdmin();
        // check if allowed to start (must be member or admin)
        if (isOlatAdmin || businessGroupService.isIdentityInBusinessGroup(ureq.getIdentity(), bgroup)) {
            // only olatadmins or admins of this group can administer this group
            return BGControllerFactory.getInstance().createRunControllerFor(ureq, wControl, bgroup, isOlatAdmin, null);
        }

        // access permitted
        final Translator trans = PackageUtil.createPackageTranslator(I18nPackage.REPOSITORY_, ureq.getLocale());
        wControl.setError(trans.translate("launch.noaccess"));
        throw new OLATSecurityException("User '" + ureq.getIdentity() + "' has no access to resource: " + ores);
    }

    /**
	 */
    @Override
    public String getTabName(final ContextEntry ce) {
        final OLATResourceable ores = ce.getOLATResourceable();
        final Long gKey = ores.getResourceableId();
        final BusinessGroup bgroup = businessGroupService.loadBusinessGroup(gKey, true);
        return bgroup.getName();
    }

    /**
	 */
    @Override
    public String getSiteClassName(final ContextEntry ce) {
        return null;
    }

    @Override
    public boolean validateContextEntryAndShowError(final ContextEntry ce, final UserRequest ureq, final WindowControl wControl) {
        final OLATResourceable ores = ce.getOLATResourceable();
        final Long gKey = ores.getResourceableId();
        final BusinessGroup bgroup = businessGroupService.loadBusinessGroup(gKey, false);
        return bgroup != null;
    }

}
