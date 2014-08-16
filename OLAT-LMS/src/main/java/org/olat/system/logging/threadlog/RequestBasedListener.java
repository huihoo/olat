package org.olat.system.logging.threadlog;

import org.olat.system.event.Event;
import org.olat.system.event.GenericEventListener;

/**
 * Listener passed on to the PersistentProperties for the RequestBasedLogLevelManager.
 * <p>
 * The reason for having this as a separate class is simply the fact that OlatResourceable names are capped in terms of length
 * <P>
 * Initial Date: 13.09.2010 <br>
 * 
 * @author Stefan
 */
public class RequestBasedListener implements GenericEventListener {

    /** the RequestBasedLogLevelManager to which this listener is associated **/
    private RequestBasedLogLevelManager manager;

    /**
     * [spring]
     */
    private RequestBasedListener() {
        //
    }

    /**
     * Sets the RequestBasedLogLevelManager to which this listener is associated.
     * <p>
     * Used by spring.
     * 
     * @param manager
     *            the RequestBasedLogLevelManager to which this listener is associated.
     */
    public void setManager(RequestBasedLogLevelManager manager) {
        this.manager = manager;
    }

    @Override
    public void event(Event event) {
        if (manager != null)
            manager.init();
    }

    /**
	 */
    @Override
    public boolean isControllerAndNotDisposed() {
        return false;
    }

}
