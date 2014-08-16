package org.olat.presentation.forum;

import org.olat.system.event.MultiUserEvent;

/**
 * Description:<br>
 * MultiUserEvent fired at close/open or hide/show forum thread, or add/delete thread.
 * <P>
 * Initial Date: 09.07.2009 <br>
 * 
 * @author Lavinia Dumitrescu
 */
public class ForumChangedEvent extends MultiUserEvent {

    public ForumChangedEvent(final String command) {
        super(command);
    }
}
