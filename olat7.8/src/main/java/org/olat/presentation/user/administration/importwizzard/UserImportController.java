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

package org.olat.presentation.user.administration.importwizzard;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.lms.security.ImportableUserParameter;
import org.olat.lms.user.UserService;
import org.olat.lms.user.propertyhandler.UserPropertyHandler;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.wizard.Step;
import org.olat.presentation.framework.core.control.generic.wizard.StepRunnerCallback;
import org.olat.presentation.framework.core.control.generic.wizard.StepsMainRunController;
import org.olat.presentation.framework.core.control.generic.wizard.StepsRunContext;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * TODO: Felix Class Description for UserImportController
 * <P>
 * Initial Date: 17.08.2005 <br>
 * 
 * @author Felix, Roman Haag
 */
public class UserImportController extends BasicController {

    private static final Logger LOG = LoggerHelper.getLogger();

    private List<UserPropertyHandler> userPropertyHandlers;
    private static final String usageIdentifyer = UserImportController.class.getCanonicalName();
    private List<List<String>> newIdents;
    private final boolean canCreateOLATPassword;
    private final VelocityContainer mainVC;
    private final Link startLink;
    private UserService userService;
    StepsMainRunController importStepsController;

    /**
     * @param ureq
     * @param wControl
     * @param canCreateOLATPassword
     *            true: workflow offers column to create passwords; false: workflow does not offer pwd column
     */
    public UserImportController(final UserRequest ureq, final WindowControl wControl, final boolean canCreateOLATPassword) {
        super(ureq, wControl);
        this.canCreateOLATPassword = canCreateOLATPassword;
        userService = CoreSpringFactory.getBean(UserService.class);
        mainVC = createVelocityContainer("importindex");
        startLink = LinkFactory.createButton("import.start", mainVC, this);
        putInitialPanel(mainVC);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == importStepsController) {
            if (event == Event.CANCELLED_EVENT) {
                getWindowControl().pop();
                removeAsListenerAndDispose(importStepsController);
            } else if (event == Event.CHANGED_EVENT) {
                getWindowControl().pop();
                removeAsListenerAndDispose(importStepsController);
                showInfo("import.success");
            }
        }
    }

    private Identity doCreateAndPersistIdentity(final Roles roles, final List<String> singleUser) {
        // Create new user and identity and put user to users group
        final String login = singleUser.get(1); // pos 0 is used for existing/non-existing user flag
        String pwd = singleUser.get(2);
        final String lang = singleUser.get(3);
        List<String> userPropertiesInput = singleUser.subList(4, singleUser.size());

        ImportableUserParameter importableUserParameter = new ImportableUserParameter(login, pwd, lang, getLocale(), userPropertyHandlers, userPropertiesInput);
        return getBaseSecurityEBL().createUser(roles, importableUserParameter);
    }

    private BaseSecurityEBL getBaseSecurityEBL() {
        return CoreSpringFactory.getBean(BaseSecurityEBL.class);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // child controllers disposed by basic controller
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == startLink) {
            // use fallback translator for user property translation
            setTranslator(userService.getUserPropertiesConfig().getTranslator(getTranslator()));
            userPropertyHandlers = userService.getUserPropertyHandlersFor(usageIdentifyer, true);

            final Step start = new ImportStep00(ureq, canCreateOLATPassword);
            // callback executed in case wizard is finished.
            final StepRunnerCallback finish = new StepRunnerCallback() {
                @Override
                public Step execute(final UserRequest ureq1, final WindowControl wControl1, final StepsRunContext runContext) {
                    // all information to do now is within the runContext saved
                    boolean hasChanges = false;
                    try {
                        if (runContext.containsKey("validImport") && ((Boolean) runContext.get("validImport")).booleanValue()) {
                            // create new users and persist
                            newIdents = (List<List<String>>) runContext.get("newIdents");
                            LOG.info("importable users: " + newIdents);
                            for (final Iterator<List<String>> it_news = newIdents.iterator(); it_news.hasNext();) {
                                final List<String> singleUser = it_news.next();
                                doCreateAndPersistIdentity(ureq.getUserSession().getRoles(), singleUser);
                            }
                            hasChanges = true;
                        }
                    } catch (final Exception ex) {
                        LOG.error("Cannot import users: " + newIdents, ex);
                    }
                    // signal correct completion and tell if changes were made or not.
                    return hasChanges ? StepsMainRunController.DONE_MODIFIED : StepsMainRunController.DONE_UNCHANGED;
                }
            };

            importStepsController = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, translate("title"));
            listenTo(importStepsController);
            getWindowControl().pushAsModalDialog(importStepsController.getInitialComponent());
        }
    }

}
