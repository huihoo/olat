package org.olat.system.logging.threadlog;

import org.olat.system.event.Event;
import org.olat.system.event.GenericEventListener;

/**
 * Listener passed on to the PersistentProperties for the UserBasedLogLevelManager.
 * <p>
 * The reason for having this as a separate class is simply the fact that OlatResourceable names are capped in terms of length
 * <P>
 * Initial Date: 13.09.2010 <br>
 * 
 * @author Stefan
 */
public class UserBasedListener implements GenericEventListener {

    /** the UserBasedLogLevelManager to which this listener is associated **/
    private UserBasedLogLevelManager manager;

    /**
     * [spring]
     */
    private UserBasedListener() {
        //
    }

    /**
     * Sets the UserBasedLogLevelManager to which this listener is associated.
     * <p>
     * Used by spring.
     * 
     * @param manager
     *            the UserBasedLogLevelManager to which this listener is associated.
     */
    public void setManager(UserBasedLogLevelManager manager) {
        this.manager = manager;
    }

    @Override
    public void event(Event event) {
        if (manager != null) {
            manager.init();
        }
    }

    /**
	 */
    @Override
    public boolean isControllerAndNotDisposed() {
        return false;
    }

}
