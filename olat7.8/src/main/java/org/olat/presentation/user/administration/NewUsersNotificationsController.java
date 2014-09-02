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
 * Copyright (c) 2008 frentix GmbH, Switzerland<br>
 * <p>
 */
package org.olat.presentation.user.administration;

import java.util.Date;
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.presentation.events.SingleIdentityChosenEvent;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

import com.ibm.icu.util.Calendar;

/**
 * Description:<br>
 * This workflow show the latest created users based on notifications. Form the list an identity can be selected which results in a SingleIdentityChosenEvent.
 * <P>
 * Initial Date: 18 august 2009 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
public class NewUsersNotificationsController extends BasicController {

    private final DateChooserController dateChooserController;
    private UsermanagerUserSearchController searchController;

    private final VelocityContainer mainVC;

    public NewUsersNotificationsController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);

        mainVC = createVelocityContainer("newusersNotifications");

        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        dateChooserController = new DateChooserController(ureq, wControl, cal.getTime());
        listenTo(dateChooserController);
        mainVC.put("dateChooser", dateChooserController.getInitialComponent());

        updateUI(ureq, cal.getTime());
        putInitialPanel(mainVC);
    }

    private void updateUI(final UserRequest ureq, final Date compareDate) {
        if (searchController != null) {
            removeAsListenerAndDispose(searchController);
        }
        final List<Identity> identities = getBaseSecurityEBL().getNewIdentityCreated(compareDate);
        searchController = new UsermanagerUserSearchController(ureq, getWindowControl(), identities, Identity.STATUS_VISIBLE_LIMIT, true, false);
        listenTo(searchController);
        mainVC.put("notificationsList", searchController.getInitialComponent());

        if (identities.isEmpty()) {
            mainVC.contextPut("hasNews", "false");
        } else {
            mainVC.contextPut("hasNews", "true");
        }
    }

    private BaseSecurityEBL getBaseSecurityEBL() {
        return CoreSpringFactory.getBean(BaseSecurityEBL.class);
    }

    @Override
    protected void doDispose() {
        // controllers autodisposed by basic controller
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        //
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == searchController) {
            if (event instanceof SingleIdentityChosenEvent) {
                fireEvent(ureq, event);
            }
        } else if (source == dateChooserController) {
            if (Event.CHANGED_EVENT == event) {
                updateUI(ureq, dateChooserController.getChoosenDate());
            }
        }
    }
}
