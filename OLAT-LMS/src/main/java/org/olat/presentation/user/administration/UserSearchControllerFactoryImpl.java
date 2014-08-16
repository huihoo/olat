package org.olat.presentation.user.administration;

import org.olat.data.basesecurity.Identity;
import org.olat.presentation.events.SingleIdentityChosenEvent;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;

public class UserSearchControllerFactoryImpl implements UserSearchControllerFactory {
    UserSearchControllerFactoryImpl() {
        // needed for spring
    }

    @Override
    public Controller createUserSearchController(final boolean withCancelButton, final UserRequest ureq, final WindowControl wControl) {
        return new UserSearchController(ureq, wControl, withCancelButton);
    }

    @Override
    public Identity getChosenUser(final Event event) {
        if (event == Event.CANCELLED_EVENT) {
            return null;
        } else {
            final SingleIdentityChosenEvent sice = (SingleIdentityChosenEvent) event;
            return sice.getChosenIdentity();
        }
    }

}
