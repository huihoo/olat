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
package org.olat.lms.portfolio.artefacthandler;

import java.util.Date;

import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.lms.search.SearchResourceContext;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * Description:<br>
 * reason to have this abstract between interface and concrete implementation is to swap out common code here.
 * <P>
 * Initial Date: 11.06.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public abstract class EPAbstractHandler<U extends AbstractArtefact> implements EPArtefactHandler<U> {

    private boolean enabled = true;

    public EPAbstractHandler() {
        super();
    }

    /**
	 */
    @Override
    public void prefillArtefactAccordingToSource(final AbstractArtefact artefact, final Object source) {
        if (source instanceof OLATResourceable) {
            final OLATResourceable ores = (OLATResourceable) source;
            artefact.setSource(ores.getResourceableTypeName());
        }
        artefact.setCollectionDate(new Date());
    }

    /**
	 */
    @Override
    public abstract String getType();

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    /**
	 */
    @Override
    public PackageTranslator getHandlerTranslator(final Translator fallBackTrans) {
        final PackageTranslator pT = new PackageTranslator(this.getClass().getPackage().getName(), fallBackTrans.getLocale(), fallBackTrans);
        return pT;
    }

    @Override
    public Controller createDetailsController(final UserRequest ureq, final WindowControl wControl, final AbstractArtefact artefact, final boolean readOnlyMode) {
        return null;
    }

    @Override
    public abstract U createArtefact();

    /**
     * @return Returns the providesSpecialMapViewController.
     */
    @Override
    public boolean isProvidingSpecialMapViewController() {
        return false;
    }

    @Override
    public Controller getSpecialMapViewController(final UserRequest ureq, final WindowControl wControl, final AbstractArtefact artefact) {
        return null;
    }

    protected void getContent(final AbstractArtefact artefact, final StringBuilder sb, final SearchResourceContext context, final EPFrontendManager ePFManager) {
        final String content = ePFManager.getArtefactFullTextContent(artefact);
        if (content != null) {
            sb.append(content);
        }
    }

}
