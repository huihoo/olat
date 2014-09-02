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
 * http://www.frentix.com
 * <p>
 */
package org.olat.presentation.security.authentication.ldap;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.lms.security.authentication.ldap.LDAPError;
import org.olat.lms.security.authentication.ldap.LDAPEvent;
import org.olat.lms.security.authentication.ldap.LDAPLoginManager;
import org.olat.presentation.admin.sysinfo.logging.LogRealTimeViewerController;
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
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.event.Event;
import org.olat.system.event.GenericEventListener;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * The LDAPAdminController offers an administrative panel to tweak some parameters and manually run an LDAP sync job
 * <P>
 * Initial Date: 21.08.2008 <br>
 * 
 * @author gnaegi
 */
public class LDAPAdminController extends BasicController implements GenericEventListener {

    private static final Logger log = LoggerHelper.getLogger();
    private final VelocityContainer ldapAdminVC;
    private final DateFormat dateFormatter;
    private final Link syncStartLink;
    private final Link deletStartLink;
    private StepsMainRunController deleteStepController;
    private boolean hasIdentitiesToDelete;
    private boolean hasIdentitiesToDeleteAfterRun;
    private Integer amountUsersToDelete;
    private List<Identity> identitiesToDelete;
    private final LDAPLoginManager ldapLoginManager;

    protected LDAPAdminController(final UserRequest ureq, final WindowControl control) {
        super(ureq, control);

        ldapLoginManager = (LDAPLoginManager) CoreSpringFactory.getBean(LDAPLoginManager.class);
        ldapAdminVC = createVelocityContainer("ldapadmin");
        dateFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, getLocale());
        updateLastSyncDateInVC();
        // Create start LDAP sync link
        syncStartLink = LinkFactory.createButton("sync.button.start", ldapAdminVC, this);
        // Create start delete User link
        deletStartLink = LinkFactory.createButton("delete.button.start", ldapAdminVC, this);
        // Create real-time log viewer
        final LogRealTimeViewerController logViewController = new LogRealTimeViewerController(ureq, control, "org.olat.presentation.security.authentication.ldap",
                Level.INFO, true);
        listenTo(logViewController);
        ldapAdminVC.put("logViewController", logViewController.getInitialComponent());
        //
        CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, null, LDAPLoginManager.ldapSyncLockOres);

        putInitialPanel(ldapAdminVC);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // Controller autodisposed by basic controller
        CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, LDAPLoginManager.ldapSyncLockOres);
    }

    @Override
    public void event(final Event event) {
        if (event instanceof LDAPEvent) {
            final LDAPEvent ldapEvent = (LDAPEvent) event;
            if (LDAPEvent.SYNCHING_ENDED.equals(ldapEvent.getCommand())) {
                syncTaskFinished(ldapEvent.isSuccess(), ldapEvent.getErrors());
            }
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == syncStartLink) {
            // Start sync job
            // Disable start link during sync
            syncStartLink.setEnabled(false);
            final LDAPEvent ldapEvent = new LDAPEvent(LDAPEvent.DO_SYNCHING);
            CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(ldapEvent, LDAPLoginManager.ldapSyncLockOres);
            showInfo("admin.synchronize.started");
        }

        else if (source == deletStartLink) {
            // cancel if some one else is making sync or delete job
            if (!ldapLoginManager.acquireSyncLock()) {
                showError("delete.error.lock");
            } else {
                deletStartLink.setEnabled(false);

                // check and get LDAP connection
                final LdapContext ctx = ldapLoginManager.bindSystem();
                if (ctx == null) {
                    showError("LDAP connection ERROR");
                    return;
                }
                // get deleted users
                identitiesToDelete = ldapLoginManager.getIdentitysDeletedInLdap(ctx);
                try {
                    ctx.close();
                } catch (final NamingException e) {
                    showError("Could not close LDAP connection on manual delete sync");
                    log.error("Could not close LDAP connection on manual delete sync", e);
                }
                if (identitiesToDelete != null && identitiesToDelete.size() != 0) {
                    hasIdentitiesToDelete = true;
                    /*
                     * start step which spawns the whole wizard
                     */
                    final Step start = new DeletStep00(ureq, hasIdentitiesToDelete, identitiesToDelete);
                    /*
                     * wizard finish callback called after "finish" is called
                     */
                    final StepRunnerCallback finishCallback = new StepRunnerCallback() {
                        @Override
                        public Step execute(final UserRequest ureq, final WindowControl control, final StepsRunContext runContext) {
                            hasIdentitiesToDeleteAfterRun = ((Boolean) runContext.get("hasIdentitiesToDelete")).booleanValue();
                            if (hasIdentitiesToDeleteAfterRun) {
                                final List<Identity> identitiesToDelete = (List<Identity>) runContext.get("identitiesToDelete");
                                amountUsersToDelete = identitiesToDelete.size();
                                // Delete all identities now and tell everybody that
                                // we are finished
                                ldapLoginManager.deletIdentities(identitiesToDelete);
                                return StepsMainRunController.DONE_MODIFIED;
                            } else {
                                return StepsMainRunController.DONE_UNCHANGED;
                            }
                            // otherwhise return without deleting anything
                        }

                    };
                    deleteStepController = new StepsMainRunController(ureq, getWindowControl(), start, finishCallback, null, translate("admin.deleteUser.title"));
                    listenTo(deleteStepController);
                    getWindowControl().pushAsModalDialog(deleteStepController.getInitialComponent());
                } else {
                    hasIdentitiesToDelete = false;
                    showInfo("delete.step.noUsers");
                    deletStartLink.setEnabled(true);
                    ldapLoginManager.freeSyncLock();
                }
            }
        }

    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == deleteStepController) {
            if (event == Event.CANCELLED_EVENT || event == Event.FAILED_EVENT) {
                getWindowControl().pop();
                removeAsListenerAndDispose(deleteStepController);
                showInfo("delete.step.cancel");
                ldapLoginManager.freeSyncLock();
                deletStartLink.setEnabled(true);
            } else if (event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
                getWindowControl().pop();
                removeAsListenerAndDispose(deleteStepController);
                if (hasIdentitiesToDeleteAfterRun) {
                    showInfo("delete.step.finish.users", amountUsersToDelete.toString());
                } else {
                    showInfo("delete.step.finish.noUsers");
                }
                ldapLoginManager.freeSyncLock();
                deletStartLink.setEnabled(true);
            }
        }
    }

    /**
     * Callback method for asynchronous sync thread. Called when sync is finished
     * 
     * @param success
     * @param errors
     */
    void syncTaskFinished(final boolean success, final LDAPError errors) {
        if (success) {
            showWarning("admin.synchronize.finished.success");
            log.info("LDAP user synchronize job finished successfully", null);
        } else {
            showError("admin.synchronize.finished.failure", errors.get());
            log.info("LDAP user synchronize job finished with errors::" + errors.get(), null);
        }
        // re-enable start link
        syncStartLink.setEnabled(true);
        // update last sync date
        updateLastSyncDateInVC();
    }

    /**
     * Internal helper to push the last sync date to velocity
     */
    private void updateLastSyncDateInVC() {
        final Date date = ldapLoginManager.getLastSyncDate();
        if (date != null) {
            ldapAdminVC.contextPut("lastSyncDate", dateFormatter.format(date));
        }
    }

    /**
	 */
    @Override
    public boolean isControllerAndNotDisposed() {
        return !isDisposed();
    }

}
