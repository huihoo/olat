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

package org.olat.lms.ims.qti.navigator;

import org.olat.lms.ims.qti.QTIConstants;
import org.olat.lms.ims.qti.container.AssessmentContext;
import org.olat.lms.ims.qti.container.ItemContext;
import org.olat.lms.ims.qti.container.ItemsInput;
import org.olat.lms.ims.qti.container.Output;
import org.olat.lms.ims.qti.container.SectionContext;
import org.olat.lms.ims.qti.process.AssessmentInstance;

/**
 * Navigator used for the case: <br>
 * 1. Navigation (via Menu) visible and enabled, one question per page <br>
 * 
 * @author Felix Jost
 */
public class MenuItemNavigator extends DefaultNavigator {

    /**
     * @param assessmentContext
     */
    public MenuItemNavigator(final AssessmentInstance assessmentInstance) {
        super(assessmentInstance);
    }

    @Override
    public void startAssessment() {
        getInfo().setStatus(QTIConstants.ASSESSMENT_RUNNING);
        getInfo().setMessage(QTIConstants.MESSAGE_ASSESSMENT_INFODEMANDED);
        getInfo().setRenderItems(false);
        getAssessmentInstance().start();
        getAssessmentInstance().persist();
    }

    /**
	 */
    @Override
    public void submitItems(final ItemsInput curitsinp) {
        clearInfo();
        final int st = submitOneItem(curitsinp);
        if (st != QTIConstants.ITEM_SUBMITTED) {
            // time expired or too many attempts-> display a message above the next item or assessment-finished-text
            getInfo().setError(st);
            getInfo().setRenderItems(false);
        } else { // ok, eval the whole assessment here each time (so after a submitted item, one sees overall progress)
            // getAssessmentContext().eval();
            getInfo().setMessage(QTIConstants.MESSAGE_ITEM_SUBMITTED);
            getInfo().setRenderItems(false);
            final ItemContext itc = getAssessmentContext().getCurrentSectionContext().getCurrentItemContext();
            // check on section feedbacks
            final Output outp = itc.getOutput();
            if (outp != null) {
                getInfo().setCurrentOutput(outp);
            }

            // check on item feedback
            if (itc.isFeedback()) { // feedback allowed
                getInfo().setFeedback(itc.getOutput().hasItem_Responses());
            }
            if (itc.isHints()) { // hints allowed
                if (itc.getOutput().getHint() != null) { // feedback existing on item
                    getInfo().setHint(true);
                }
            }
            if (itc.isSolutions()) { // solution allowed
                if (itc.getOutput().getSolution() != null) { // solution existing on item
                    getInfo().setSolution(true);
                }
            }

        }
        getAssessmentInstance().persist();
    }

    /**
	 */
    @Override
    public void goToItem(final int sectionPos, final int itemPos) {
        if (getInfo().getStatus() != QTIConstants.ASSESSMENT_RUNNING) {
            throw new RuntimeException("assessment is NOT running yet or anymore");
        }
        clearInfo();
        final AssessmentContext ac = getAssessmentContext();
        final SectionContext sc = ac.getSectionContext(sectionPos);
        final ItemContext target = sc.getItemContext(itemPos);
        // check if targeted item is still open

        if (!ac.isOpen()) {
            getInfo().setError(QTIConstants.ERROR_ASSESSMENT_OUTOFTIME);
            getInfo().setRenderItems(false);
        } else if (!sc.isOpen()) {
            getInfo().setError(QTIConstants.ERROR_SECTION_OUTOFTIME);
            getInfo().setRenderItems(false);
        } else if (!target.isOpen()) {
            getInfo().setError(QTIConstants.ERROR_ITEM_OUTOFTIME);
            getInfo().setRenderItems(false);
        } else {
            getInfo().setStatus(QTIConstants.ASSESSMENT_RUNNING);
            getInfo().setRenderItems(true);
            ac.setCurrentSectionContextPos(sectionPos);
            sc.start();
            sc.setCurrentItemContextPos(itemPos);
            sc.getCurrentItemContext().start();
        }
        getAssessmentInstance().persist();
    }

    /**
     * go to the section (not the item yet): display the objectives of the section
     * 
     */
    @Override
    public void goToSection(final int sectionPos) {
        if (getInfo().getStatus() != QTIConstants.ASSESSMENT_RUNNING) {
            throw new RuntimeException("assessment is NOT running yet or anymore");
        }
        clearInfo();
        final AssessmentContext ac = getAssessmentContext();
        ac.setCurrentSectionContextPos(sectionPos);
        final SectionContext sc = ac.getCurrentSectionContext();
        if (!ac.isOpen()) {
            getInfo().setError(QTIConstants.ERROR_ASSESSMENT_OUTOFTIME);
            getInfo().setRenderItems(false);
        } else if (!sc.isOpen()) {
            getInfo().setError(QTIConstants.ERROR_SECTION_OUTOFTIME);
            getInfo().setRenderItems(false);
        } else {
            sc.setCurrentItemContextPos(-1); // no current item position, since we display section info only
            sc.start();
            getInfo().setStatus(QTIConstants.ASSESSMENT_RUNNING);
            getInfo().setRenderItems(false); // only section info
            getInfo().setMessage(QTIConstants.MESSAGE_SECTION_INFODEMANDED);
        }
        getAssessmentInstance().persist();
    }

}
