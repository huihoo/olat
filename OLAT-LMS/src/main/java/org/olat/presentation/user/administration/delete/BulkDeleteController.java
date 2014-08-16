/**
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * Copyright (c) 2005-2008 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 * All rights reserved.
 */
package org.olat.presentation.user.administration.delete;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.lms.commons.mail.MailTemplateHelper;
import org.olat.lms.user.administration.delete.BulkDeleteModel;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableDataModel;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.group.securitygroup.UserControllerFactory;
import org.olat.system.commons.WebappHelper;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.mail.ContactList;
import org.olat.system.mail.Emailer;

public class BulkDeleteController extends BasicController {

    private static final Logger log = LoggerHelper.getLogger();
    private final VelocityContainer vc;

    private final String userlist, reason;
    private BulkDeleteModel model;
    private TableController tblCtrFound, tblCtrNotfound;
    private Link btnNext;

    public BulkDeleteController(final UserRequest ureq, final WindowControl wControl, final String userlist, final String reason) {
        super(ureq, wControl);

        this.userlist = userlist;
        this.reason = reason;

        vc = createVelocityContainer("bulkdelete");
        getBulkDeleteModel(this.userlist);

        if (model.hasDeletable()) {
            tblCtrFound = UserControllerFactory.createTableControllerFor(new TableGuiConfiguration(), model.getDeletable(), ureq, getWindowControl(), null);
            listenTo(tblCtrFound);
            btnNext = LinkFactory.createButton("next", vc, this);
            vc.put("table.users.found", tblCtrFound.getInitialComponent());
        }

        if (model.hasNotFound()) {
            tblCtrNotfound = new TableController(null, ureq, wControl, getTranslator());
            listenTo(tblCtrNotfound);
            tblCtrNotfound.addColumnDescriptor(new DefaultColumnDescriptor("table.col.login", 0, null, ureq.getLocale()));
            final TableDataModel tblData = new LoginTableDataModel(model.getNotFoundUsernames());
            tblCtrNotfound.setTableDataModel(tblData);

            vc.put("table.users.notfound", tblCtrNotfound.getInitialComponent());
        }

        vc.contextPut("reason", this.reason);

        putInitialPanel(vc);
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == btnNext) {
            fireEvent(ureq, event);
        }
    }

    /**
     * Separate logins that are found in system and not found
     * 
     * @param loginsString
     */
    private void getBulkDeleteModel(final String loginsString) {
        final String[] logins = loginsString.split("\r?\n");
        model = new BulkDeleteModel(logins);
    }

    /**
     * Send the mail with informations: who deletes, when, list of deleted users list of not deleted users, reason for deletion
     */
    public void sendMail(final UserRequest ureq) {
        final StringBuffer loginsFound = new StringBuffer();
        for (final String login : model.getFoundUsernames()) {
            loginsFound.append(login + "\n");
        }
        final StringBuffer loginsNotfound = new StringBuffer();
        for (final String login : model.getNotFoundUsernames()) {
            loginsNotfound.append(login + "\n");
        }
        final DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, ureq.getLocale());

        final String[] bodyArgs = new String[] { ureq.getIdentity().getName(), loginsFound.toString(), loginsNotfound.toString(), reason, df.format(new Date()) };

        final String subject = translate("mail.subject");
        final String body = getTranslator().translate("mail.body", bodyArgs);

        final ContactList cl = new ContactList(WebappHelper.getMailConfig("mailSupport"));
        cl.add(WebappHelper.getMailConfig("mailSupport"));
        cl.add(ureq.getIdentity());
        final List<ContactList> lstAddrTO = new ArrayList<ContactList>();
        lstAddrTO.add(cl);

        final Emailer mailer = new Emailer(MailTemplateHelper.getMailTemplateWithFooterNoUserData(ureq.getLocale()));
        try {
            mailer.sendEmail(lstAddrTO, subject, body);
        } catch (final AddressException e) {
            log.error("Notificatoin mail for bulk deletion could not be sent");
        } catch (final MessagingException e) {
            log.error("Notificatoin mail for bulk deletion could not be sent");
        }
    }

    public List<Identity> getToDelete() {
        return model.getDeletable();
    }

    @Override
    protected void doDispose() {
        // nothing
    }
}

class LoginTableDataModel extends DefaultTableDataModel {

    public LoginTableDataModel(final List logins) {
        super(logins);
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public Object getValueAt(final int row, final int col) {
        final String login = (String) getObject(row);

        switch (col) {
        case 0:
            return login;
        default:
            return "error";
        }
    }

}
