/**
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * Copyright (c) 2005-2008 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 * All rights reserved.
 */
package org.olat.presentation.course.nodes.iq;

import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.olat.data.basesecurity.Identity;
import org.olat.data.qti.QTIResult;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.mail.MailTemplateHelper;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.ExportResult;
import org.olat.lms.course.nodes.QtiExportEBL;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.commons.mail.MailNotificationEditController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.wizard.WizardController;
import org.olat.presentation.repository.ReferencableEntriesSearchController;
import org.olat.system.commons.Settings;
import org.olat.system.event.Event;
import org.olat.system.mail.MailTemplate;
import org.olat.system.mail.MailerWithTemplate;
import org.olat.system.security.OLATPrincipal;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Wizard for replacement of an already linked test
 * <P>
 * Initial Date: 21.10.2008 <br>
 * 
 * @author skoeber
 */
public class IQEditReplaceWizard extends WizardController {

    /** three steps: information + export results, search, mail */
    private static final int STEPS = 3;

    private final ICourse course;
    private final CourseNode courseNode;
    private RepositoryEntry selectedRepositoryEntry;
    private final List<Identity> learners;
    private final List<QTIResult> results;
    private final String[] types;

    // presentation
    private VelocityContainer vcStep1, vcStep2, vcStep3;
    private Link nextBtn, showFileButton;
    private MailNotificationEditController mailCtr;
    private ReferencableEntriesSearchController searchCtr;

    /**
     * a number of identities with qti.ser entry
     */
    private final int numberOfQtiSerEntries;

    private QtiExportEBL qtiExportEBL;

    private ExportResult exportResult;

    /**
     * Standard constructor
     * 
     * @param ureq
     * @param wControl
     * @param course
     * @param courseNode
     */
    public IQEditReplaceWizard(final UserRequest ureq, final WindowControl wControl, final ICourse course, final CourseNode courseNode, final String[] types,
            final List<Identity> learners, final List<QTIResult> results, final int numberOfQtiSerEntries) {
        super(ureq, wControl, STEPS);

        setBasePackage(IQEditReplaceWizard.class);

        this.course = course;
        this.courseNode = courseNode;
        this.types = types;
        this.learners = learners;
        this.results = results;
        this.numberOfQtiSerEntries = numberOfQtiSerEntries;
        this.qtiExportEBL = CoreSpringFactory.getBean(QtiExportEBL.class);

        setWizardTitle(translate("replace.wizard.title"));
        doStep1(ureq);
    }

    @Override
    public void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == nextBtn) {
            doStep3(ureq);
        } else if (source == showFileButton) {
            ureq.getDispatchResult().setResultingMediaResource(qtiExportEBL.getDownloadableMediaResource(exportResult));
        } else if (event.getCommand().equals("cmd.wizard.cancel")) {
            fireEvent(ureq, Event.CANCELLED_EVENT);
        }
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == mailCtr && event == Event.DONE_EVENT) {
            final MailTemplate mailTemplate = mailCtr.getMailTemplate();
            if (mailTemplate != null) {
                List<Identity> recipientsCC = null;
                if (mailTemplate.getCpfrom()) {
                    recipientsCC = new ArrayList<Identity>();
                    recipientsCC.add(ureq.getIdentity());
                }
                MailerWithTemplate.getInstance().sendMailAsSeparateMails(learners, recipientsCC, null, mailCtr.getMailTemplate(), ureq.getIdentity());
            }
            fireEvent(ureq, Event.DONE_EVENT);
        } else if (source == searchCtr && event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
            selectedRepositoryEntry = searchCtr.getSelectedEntry();
            doStep2(ureq);
        }
    }

    private void doStep1(final UserRequest ureq) {
        searchCtr = new ReferencableEntriesSearchController(getWindowControl(), ureq, types, translate("command.chooseTest"));
        searchCtr.addControllerListener(this);
        vcStep1 = createVelocityContainer("replacewizard_step1");
        vcStep1.put("search", searchCtr.getInitialComponent());
        setNextWizardStep(translate("replace.wizard.title.step1"), vcStep1);
    }

    private void doStep2(final UserRequest ureq) {
        final String nodeTitle = courseNode.getShortTitle();
        if (results != null && results.size() > 0) {
            exportResult = qtiExportEBL.exportResults(ureq.getIdentity(), ureq.getLocale(), results, course.getCourseTitle(), nodeTitle);
            vcStep2 = createVelocityContainer("replacewizard_step2");
            final String[] args1 = new String[] { Integer.toString(learners.size()) };
            vcStep2.contextPut("information", translate("replace.wizard.information.paragraph1", args1));
            final String[] args2 = new String[] { exportResult.getExportFileName() };
            vcStep2.contextPut("information_par2", translate("replace.wizard.information.paragraph2", args2));
            vcStep2.contextPut("nodetitle", nodeTitle);
            showFileButton = LinkFactory.createButton("replace.wizard.showfile", vcStep2, this);
        } else {
            // it exists no result
            final String[] args = new String[] { Integer.toString(numberOfQtiSerEntries) };
            vcStep2 = createVelocityContainer("replacewizard_step2");
            vcStep2.contextPut("information", translate("replace.wizard.information.empty.results", args));
            vcStep2.contextPut("nodetitle", nodeTitle);
        }
        nextBtn = LinkFactory.createButton("replace.wizard.next", vcStep2, this);
        setNextWizardStep(translate("replace.wizard.title.step2"), vcStep2);
    }

    private void doStep3(final UserRequest ureq) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Settings.getServerContextPathURI());
        final RepositoryEntry re = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(course, true);
        stringBuilder.append("/url/RepositoryEntry/").append(re.getKey());
        stringBuilder.append("/CourseNode/").append(courseNode.getIdent());

        final String[] bodyArgs = new String[] { course.getCourseTitle(), stringBuilder.toString() };

        final String subject = translate("inform.users.subject", bodyArgs);
        final String body = translate("inform.users.body", bodyArgs);

        final MailTemplate mailTempl = new MailTemplate(subject, body, MailTemplateHelper.getMailFooter(null, ureq.getIdentity()), null) {
            @Override
            @SuppressWarnings({ "unused" })
            public void putVariablesInMailContext(final VelocityContext context, final OLATPrincipal identity) {
                // nothing to do
            }
        };

        removeAsListenerAndDispose(mailCtr);
        mailCtr = new MailNotificationEditController(getWindowControl(), ureq, mailTempl, false);
        listenTo(mailCtr);

        vcStep3 = createVelocityContainer("replacewizard_step3");
        vcStep3.put("mailform", mailCtr.getInitialComponent());
        setNextWizardStep(translate("replace.wizard.title.step3"), vcStep3);
    }

    /**
     * @return the selected RepositoryEntry
     */
    protected RepositoryEntry getSelectedRepositoryEntry() {
        return selectedRepositoryEntry;
    }

}
