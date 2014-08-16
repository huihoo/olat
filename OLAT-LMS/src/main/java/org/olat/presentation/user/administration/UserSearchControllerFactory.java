package org.olat.presentation.user.administration;

import org.olat.data.basesecurity.Identity;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;

public interface UserSearchControllerFactory {
    /**
     * creates a user search workflow
     * 
     * @param withCancelButton
     *            true, if a cancel button should be offered, false otherwise
     * @param ureq
     *            the userrequest
     * @param wControl
     *            the windowcontrol
     * @return a usersearch controller
     */
    public Controller createUserSearchController(boolean withCancelButton, UserRequest ureq, WindowControl wControl);

    /**
     * returns the chosen identity
     * 
     * @param event
     *            the received event
     * @return the chosen Identity or null if the workflow was cancelled
     */
    public Identity getChosenUser(Event event);

}
