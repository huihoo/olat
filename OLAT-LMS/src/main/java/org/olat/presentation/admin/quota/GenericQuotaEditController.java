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

package org.olat.presentation.admin.quota;

import org.olat.data.commons.vfs.Quota;
import org.olat.data.commons.vfs.QuotaManager;
import org.olat.lms.admin.quota.QuotaConstants;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;
import org.olat.system.exception.OLATSecurityException;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<BR>
 * Generic editor controller for quotas. Can be constructed from a quota or a folder path. When finished the controller fires the following events:<BR>
 * Event.CANCELLED_EVENT Event.CHANGED_EVENT
 * <P>
 * Initial Date: Dec 22, 2004
 * 
 * @author gnaegi
 */
public class GenericQuotaEditController extends BasicController {

    private VelocityContainer myContent;
    private QuotaForm quotaForm;
    private final boolean modalMode;

    private Quota currentQuota;
    private Link addQuotaButton;
    private Link delQuotaButton;
    private Link cancelButton;

    /**
     * Constructor for the generic quota edit controller used to change a quota anywhere in the system not using the generic quota management. Instead of using a quota
     * the constructor takes the folder path for which the quota will be changed.
     * <p>
     * To create an instance of this controller, use QuotaManager's factory method
     * 
     * @param ureq
     * @param wControl
     * @param quotaPath
     *            The path for which the quota should be edited
     * @param modalMode
     *            true: window will push to fullscreen and pop itself when finished. false: normal controller mode, get initial component using getInitialComponent()
     */
    GenericQuotaEditController(final UserRequest ureq, final WindowControl wControl, final String relPath, final boolean modalMode) {
        super(ureq, wControl);
        this.modalMode = modalMode;

        // check if quota foqf.cannot.del.defaultr this path already exists
        final QuotaManager qm = QuotaManager.getInstance();
        this.currentQuota = qm.getCustomQuota(relPath);
        // init velocity context
        initMyContent(ureq);
        if (currentQuota == null) {
            this.currentQuota = QuotaManager.getInstance().createQuota(relPath, null, null);
            myContent.contextPut("editQuota", Boolean.FALSE);
        } else {
            initQuotaForm(ureq, currentQuota);
        }
        putInitialPanel(myContent);
    }

    /**
     * Constructor for the generic quota edit controller used when an existing quota should be edited, as done in the admin quotamanagement
     * 
     * @param ureq
     * @param wControl
     * @param quota
     *            The existing quota or null. If null, a new quota is generated
     */
    public GenericQuotaEditController(final UserRequest ureq, final WindowControl wControl, final Quota quota) {
        super(ureq, wControl);
        this.modalMode = false;

        initMyContent(ureq);

        // start with neq quota if quota is empty
        if (quota == null) {
            this.currentQuota = QuotaManager.getInstance().createQuota(null, null, null);
            myContent.contextPut("isEmptyQuota", true);
        } else {
            this.currentQuota = quota;
        }
        initQuotaForm(ureq, currentQuota);

        putInitialPanel(myContent);
    }

    private void initMyContent(final UserRequest ureq) {
        if (!getBaseSecurityEBL().isIdentityPermittedOnResourceable(ureq.getIdentity(), OresHelper.lookupType(this.getClass()))) {
            throw new OLATSecurityException("Insufficient permissions to access QuotaController");
        }

        myContent = createVelocityContainer("edit");
        myContent.contextPut("modalMode", Boolean.valueOf(modalMode));
        addQuotaButton = LinkFactory.createButtonSmall("qf.new", myContent, this);
        delQuotaButton = LinkFactory.createButtonSmall("qf.del", myContent, this);
        cancelButton = LinkFactory.createButtonSmall("cancel", myContent, this);

        final QuotaManager qm = QuotaManager.getInstance();
        // TODO loop over QuotaManager.getDefaultQuotaIdentifyers instead
        myContent.contextPut("users", qm.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_USERS));
        myContent.contextPut("powerusers", qm.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_POWER));
        myContent.contextPut("groups", qm.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_GROUPS));
        myContent.contextPut("repository", qm.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_REPO));
        myContent.contextPut("coursefolder", qm.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_COURSE));
        myContent.contextPut("nodefolder", qm.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_NODES));
    }

    private BaseSecurityEBL getBaseSecurityEBL() {
        return (BaseSecurityEBL) CoreSpringFactory.getBean(BaseSecurityEBL.class);
    }

    private void initQuotaForm(final UserRequest ureq, final Quota quota) {
        if (quotaForm != null) {
            removeAsListenerAndDispose(quotaForm);
        }
        quotaForm = new QuotaForm(ureq, getWindowControl(), quota);
        listenTo(quotaForm);
        myContent.put("quotaform", quotaForm.getInitialComponent());
        myContent.contextPut("editQuota", Boolean.TRUE);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        initQuotaForm(ureq, currentQuota);
        if (source == delQuotaButton) {
            final boolean deleted = QuotaManager.getInstance().deleteCustomQuota(currentQuota);
            if (deleted) {
                myContent.remove(quotaForm.getInitialComponent());
                myContent.contextPut("editQuota", Boolean.FALSE);
                showInfo("qf.deleted", currentQuota.getPath());
                fireEvent(ureq, Event.CHANGED_EVENT);
            } else {
                showError("qf.cannot.del.default");
            }
        } else if (source == cancelButton) {
            fireEvent(ureq, Event.CANCELLED_EVENT);
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == quotaForm) {
            if (event == Event.DONE_EVENT) {
                final QuotaManager qm = QuotaManager.getInstance();
                currentQuota = QuotaManager.getInstance().createQuota(quotaForm.getPath(), new Long(quotaForm.getQuotaKB()), new Long(quotaForm.getULLimit()));
                qm.setCustomQuotaKB(currentQuota);
                fireEvent(ureq, Event.CHANGED_EVENT);
            }
        }
    }

    /**
     * @return Quota the edited quota
     */
    public Quota getQuota() {
        if (currentQuota == null) {
            throw new AssertException("getQuota called but currentQuota is null");
        }
        return currentQuota;
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }
}
