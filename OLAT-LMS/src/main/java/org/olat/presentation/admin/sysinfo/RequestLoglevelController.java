package org.olat.presentation.admin.sysinfo;

import org.apache.log4j.Logger;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.logging.threadlog.RequestBasedLogLevelManager;
import org.olat.system.logging.threadlog.UserBasedLogLevelManager;

/**
 * A rather simply kept controller used for the requestloglevel feature which was added to debug special cases with 'slow requests'.
 * <p>
 * It allows to mark particular requests (based on ip address or username - dont overlap those two though!) with a specific loglevel and even an appender.
 * <p>
 * That way you can have all requests from say user 'administrator' logged with log level DEBUG and sent to appender 'DebugLog' (which is a standard log4j appender and
 * can therefore for example be writing to a different file than the rest of the log events).
 * <P>
 * Initial Date: 13.09.2010 <br>
 * 
 * @author Stefan
 */
public class RequestLoglevelController extends BasicController implements Controller {

    private RequestLoglevelForm form;
    private static final Logger log = LoggerHelper.getLogger();
    private final RequestBasedLogLevelManager requestBasedLogLevelManager;

    private final UserBasedLogLevelManager userBasedLogLevelManager;

    protected RequestLoglevelController(final UserRequest ureq, final WindowControl control) {
        super(ureq, control);

        requestBasedLogLevelManager = RequestBasedLogLevelManager.getInstance();
        userBasedLogLevelManager = UserBasedLogLevelManager.getInstance();

        if (requestBasedLogLevelManager == null && userBasedLogLevelManager == null) {
            final VelocityContainer requestlogleveldisabled = createVelocityContainer("requestlogleveldisabled");
            putInitialPanel(requestlogleveldisabled);
        } else {
            form = new RequestLoglevelForm("requestloglevelform", getTranslator());
            form.addListener(this);
            putInitialPanel(form);
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // TODO Auto-generated method stub

    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == form) {
            final String[] usernames = form.getUsernamesAndLevels();
            final String[] ips = form.getIpsAndLevels();

            if (requestBasedLogLevelManager != null) {
                requestBasedLogLevelManager.reset();

                requestBasedLogLevelManager.storeIpsAndLevels(form.getRawIpsAndLevels());

                for (int i = 0; i < ips.length; i++) {
                    final String ip = ips[i];
                    if (ip != null && ip.length() > 0 && ip.contains("=")) {
                        try {
                            requestBasedLogLevelManager.setLogLevelAndAppender(ip);
                        } catch (final Exception e) {
                            log.warn("Couldnt set loglevel for remote address: " + ip, e);
                        }
                    }
                }
            }

            if (userBasedLogLevelManager != null) {
                userBasedLogLevelManager.storeUsernameAndLevels(form.getRawUsernames());

                userBasedLogLevelManager.reset();
                for (int i = 0; i < usernames.length; i++) {
                    final String username = usernames[i];
                    if (username != null && username.length() > 0 && username.contains("=")) {
                        try {
                            userBasedLogLevelManager.setLogLevelAndAppender(username);
                        } catch (final Exception e) {
                            log.warn("Couldnt set loglevel for username: " + username, e);
                        }
                    }
                }
            }

        }

    }

}
