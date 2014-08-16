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

import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.data.portfolio.artefact.EPTextArtefact;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.portfolio.artefacts.run.details.TextArtefactDetailsController;

/**
 * Description:<br>
 * Handler for the text-artefact
 * <P>
 * Initial Date: 01.09.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPTextArtefactHandler extends EPAbstractHandler<EPTextArtefact> {

    protected EPTextArtefactHandler() {
    }

    @Override
    public String getType() {
        return EPTextArtefact.TEXT_ARTEFACT_TYPE;
    }

    @Override
    public EPTextArtefact createArtefact() {
        final EPTextArtefact textArtefact = new EPTextArtefact();
        return textArtefact;
    }

    @Override
    public Controller createDetailsController(final UserRequest ureq, final WindowControl wControl, final AbstractArtefact artefact, final boolean readOnlyMode) {
        return new TextArtefactDetailsController(ureq, wControl, artefact, readOnlyMode);
    }

    @Override
    public String getIcon(AbstractArtefact artefact) {
        return "b_filetype_txt";
    }

}
