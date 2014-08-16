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

package org.olat.presentation.course.assessment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * TODO: guido Class Description for BulkAssessmentMainController
 */
public class BulkAssessmentMainController extends BasicController {

    private final VelocityContainer myContent;
    private BulkAssessmentWizardController bawCtr;

    private final List<Identity> allowedIdentities;
    private final Link startBulkwizardButton;
    private final OLATResourceable ores;

    private CloseableModalController cmc;

    public BulkAssessmentMainController(final UserRequest ureq, final WindowControl wControl, final OLATResourceable ores, final List<Identity> allowedIdentities) {
        super(ureq, wControl);
        this.myContent = this.createVelocityContainer("bulkstep0");
        this.ores = ores;
        startBulkwizardButton = LinkFactory.createButtonSmall("command.start.bulkwizard", myContent, this);

        this.allowedIdentities = allowedIdentities;

        putInitialPanel(myContent);
    }

    /**
     * This dispatches component events...
     * 
     */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == startBulkwizardButton) {
            final List<Long> allowedIdKeys = new ArrayList<Long>(allowedIdentities.size());

            for (final Iterator<Identity> iter = allowedIdentities.iterator(); iter.hasNext();) {
                final Identity identity = iter.next();
                allowedIdKeys.add(identity.getKey());
            }

            removeAsListenerAndDispose(bawCtr);
            bawCtr = new BulkAssessmentWizardController(ureq, getWindowControl(), ores, allowedIdKeys);
            listenTo(bawCtr);

            removeAsListenerAndDispose(cmc);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), bawCtr.getInitialComponent());
            listenTo(cmc);

            cmc.activate();
        }
    }

    /**
     * This dispatches controller events...
     */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == bawCtr) {
            if (event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT) {
                cmc.deactivate();
            }
        }
    }

    @Override
    protected void doDispose() {
        //
    }
}
