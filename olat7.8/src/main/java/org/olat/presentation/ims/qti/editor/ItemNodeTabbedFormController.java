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

package org.olat.presentation.ims.qti.editor;

import org.olat.lms.ims.qti.editor.QTIEditorPackageEBL;
import org.olat.lms.ims.qti.objects.Item;
import org.olat.lms.ims.qti.objects.Question;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPane;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPaneChangedEvent;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.tabbable.TabbableDefaultController;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.event.Event;

/**
 * Initial Date: Nov 21, 2004 <br>
 * 
 * @author patrick
 */
public class ItemNodeTabbedFormController extends TabbableDefaultController {

    private final Item item;
    private final QTIEditorPackageEBL qtiPackage;
    private final ItemMetadataFormController metadataCtr;
    private FeedbackFormController feedbackCtr;
    private final Panel feedbackPanel = new Panel("feedbackPanel");

    private final UserRequest ureq;
    private final boolean restrictedEdit;

    /**
     * @param item
     * @param qtiPackage
     * @param wControl
     * @param trnsltr
     */
    public ItemNodeTabbedFormController(final Item item, final QTIEditorPackageEBL qtiPackage, final UserRequest ureq, final WindowControl wControl,
            final Translator trnsltr, final boolean restrictedEdit) {
        super(ureq, wControl);
        this.ureq = ureq;
        this.restrictedEdit = restrictedEdit;
        metadataCtr = new ItemMetadataFormController(ureq, getWindowControl(), item, qtiPackage, restrictedEdit);
        this.listenTo(metadataCtr);
        this.item = item;
        this.qtiPackage = qtiPackage;
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == metadataCtr && event.equals(Event.DONE_EVENT)) {
            qtiPackage.serializeQTIDocument();
        } else {
            // Pass events over to the parent controller
            fireEvent(ureq, event);
        }

    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (event instanceof TabbedPaneChangedEvent) {
            final TabbedPaneChangedEvent tabbedPaneEvent = (TabbedPaneChangedEvent) event;
            if (feedbackPanel.equals(tabbedPaneEvent.getNewComponent())) {
                if (feedbackCtr != null) {
                    removeAsListenerAndDispose(feedbackCtr);
                }
                feedbackCtr = new FeedbackFormController(ureq, getWindowControl(), qtiPackage, item, restrictedEdit);
                // feedback controller sends out NodeBeforeChangeEvents which must be propagated
                this.listenTo(feedbackCtr);
                feedbackPanel.setContent(feedbackCtr.getInitialComponent());
            }
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // metadataCtr and feedbackCtr are registered as child controllers
    }

    /**
	 */
    @Override
    public void addTabs(final TabbedPane tabbedPane) {
        // add as listener to get tab activation events
        tabbedPane.addListener(this);

        if (item.isAlient()) {
            // this is an unknown type.
            tabbedPane.addTab(translate("tab.metadata"), this.createVelocityContainer("tab_itemAlien"));
            return;
        }

        final boolean isSurvey = qtiPackage.getQTIDocument().isSurvey();
        final int questionType = item.getQuestion().getType();

        tabbedPane.addTab(translate("tab.metadata"), metadataCtr.getInitialComponent());

        Controller ctrl = null;

        switch (questionType) {
        case Question.TYPE_SC:
            ctrl = new ChoiceItemController(item, qtiPackage, getTranslator(), ureq, getWindowControl(), restrictedEdit);
            break;
        case Question.TYPE_MC:
            ctrl = new ChoiceItemController(item, qtiPackage, getTranslator(), ureq, getWindowControl(), restrictedEdit);
            break;
        case Question.TYPE_KPRIM:
            ctrl = new ChoiceItemController(item, qtiPackage, getTranslator(), ureq, getWindowControl(), restrictedEdit);
            break;
        case Question.TYPE_FIB:
            ctrl = new FIBItemController(item, qtiPackage, getTranslator(), getWindowControl(), restrictedEdit);
            break;
        case Question.TYPE_ESSAY:
            ctrl = new EssayItemController(item, qtiPackage, getTranslator(), getWindowControl(), restrictedEdit);
            break;
        }
        if (ctrl != null) { // if item was identified
            tabbedPane.addTab(translate("tab.question"), ctrl.getInitialComponent());
            this.listenTo(ctrl);
            if (!isSurvey) {
                tabbedPane.addTab(translate("tab.feedback"), feedbackPanel);
            }
            final Controller itemPreviewController = new ItemPreviewController(getWindowControl(), item, qtiPackage, getTranslator());
            tabbedPane.addTab(translate("tab.preview"), itemPreviewController.getInitialComponent());
            tabbedPane.addListener(itemPreviewController);
        }
    }
}
