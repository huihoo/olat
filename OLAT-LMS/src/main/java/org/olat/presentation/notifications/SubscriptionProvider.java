/**
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * Copyright (c) 2005-2009 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 * All rights reserved.
 */
package org.olat.presentation.notifications;

import org.olat.lms.notifications.SubscriptionContext;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.WindowControl;

/**
 * Description:<br>
 * Managed different subscription sources.
 * <P>
 * Initial Date: 29.04.2009 <br>
 * 
 * @author bja
 */
public interface SubscriptionProvider {
    public SubscriptionContext getSubscriptionContext();

    public ContextualSubscriptionController getContextualSubscriptionController(UserRequest ureq, WindowControl wControl);
}
