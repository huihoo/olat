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

import java.io.Serializable;
import java.util.Iterator;

import org.olat.lms.ims.qti.QTIConstants;
import org.olat.lms.ims.qti.container.AssessmentContext;
import org.olat.lms.ims.qti.container.ItemContext;
import org.olat.lms.ims.qti.container.ItemInput;
import org.olat.lms.ims.qti.container.ItemsInput;
import org.olat.lms.ims.qti.container.Output;
import org.olat.lms.ims.qti.container.SectionContext;
import org.olat.lms.ims.qti.process.AssessmentInstance;

/**
 */
public abstract class DefaultNavigator implements Navigator, Serializable {

    private static final long serialVersionUID = 1L;

    private final AssessmentInstance assessmentInstance;

    private final Info info;

    public DefaultNavigator(final AssessmentInstance assessmentInstance) {
        this.assessmentInstance = assessmentInstance;
        info = new Info();
    }

    /**
     * @return AssessmentContext
     */
    protected AssessmentContext getAssessmentContext() {
        return assessmentInstance.getAssessmentContext();
    }

    /**
     * @return
     */
    protected AssessmentInstance getAssessmentInstance() {
        return assessmentInstance;
    }

    /**
     * @param curitsinp
     * @return the status of the operation like success or error
     */
    protected int submitOneItem(final ItemsInput curitsinp) {
        if (info.getStatus() != QTIConstants.ASSESSMENT_RUNNING) {
            throw new RuntimeException("assessment is NOT running yet or anymore");
        }
        final int cnt = curitsinp.getItemCount();
        if (cnt == 0) {
            handleNoItemInputError(curitsinp);
        }
        if (cnt > 1) {
            throw new RuntimeException("may only submit 1 item");
        }
        final ItemInput itemInput = curitsinp.getItemInputIterator().next();
        final String ident = itemInput.getIdent();
        final AssessmentContext ac = getAssessmentContext();
        final SectionContext sc = ac.getCurrentSectionContext();
        final ItemContext it = sc.getCurrentItemContext();
        final ItemContext ict = sc.getItemContext(ident);
        if (ict == null) {
            throw new RuntimeException("submitted item id (" + ident + ")not found in xml");
        }
        if (ict != it) {
            throw new RuntimeException("answering to a non-current item [ident=" + ident + ", currentItemContext=" + it.getIdent() + ", itemContext=" + ict.getIdent()
                    + "]");
        }
        if (!ac.isOpen()) {
            // assessment must also be open (=on time)
            return QTIConstants.ERROR_ASSESSMENT_OUTOFTIME;
        }
        if (!sc.onTime()) {
            // section of the current item must also be open (=on time)
            return QTIConstants.ERROR_SUBMITTEDSECTION_OUTOFTIME;
        }
        if (!ict.isOnTime()) {
            // current item must be on time
            return QTIConstants.ERROR_SUBMITTEDITEM_OUTOFTIME;
        }
        if (!ict.isUnderMaxAttempts()) {
            // current item must be below maxattempts
            return QTIConstants.ERROR_SUBMITTEDITEM_TOOMANYATTEMPTS;
        }
        final int subres = ict.addItemInput(itemInput);
        ict.eval(); // to have an up-to-date score
        return subres;
    }

    protected int submitMultipleItems(final ItemsInput curitsinp) {
        // = submit a whole section at once
        if (info.getStatus() != QTIConstants.ASSESSMENT_RUNNING) {
            throw new RuntimeException("assessment is NOT running yet or anymore");
        }
        final int cnt = curitsinp.getItemCount();
        if (cnt == 0) {
            handleNoItemInputError(curitsinp);
        }
        final AssessmentContext ac = getAssessmentContext();
        final SectionContext sc = ac.getCurrentSectionContext();

        if (!ac.isOpen()) {
            return QTIConstants.ERROR_ASSESSMENT_OUTOFTIME;
        }
        if (!sc.isOpen()) {
            return QTIConstants.ERROR_SUBMITTEDSECTION_OUTOFTIME;
        }

        int sectionResult = QTIConstants.SECTION_SUBMITTED;
        for (final Iterator<ItemInput> it_inp = curitsinp.getItemInputIterator(); it_inp.hasNext();) {
            final ItemInput itemInput = it_inp.next();
            final String ident = itemInput.getIdent();
            final ItemContext ict = sc.getItemContext(ident);
            if (ict == null) {
                throw new RuntimeException("submitted item id (" + ident + ") not found in section sectioncontext " + sc.getIdent());
            }
            final int subres = ict.addItemInput(itemInput);
            ict.eval(); // to be up-to-date with the scores
            if (subres != QTIConstants.ITEM_SUBMITTED) {
                // item had a timelimit or maxattempts, which is nonsense if displaymode = sectionPage
                // throw new
                // RuntimeException("section "+sc.getIdent()+" was submitted, but item "+ict.getIdent()+"  could not be submitted, because it had a timelimit or maxattempts, which is nonsense if displaymode = sectionPage");
                sectionResult = QTIConstants.ERROR_SECTION_PART_OUTOFTIME;
            }
        }
        return sectionResult;
    }

    // remove as soon cause for OLAT-6824 is found and fixed
    @Deprecated
    private void handleNoItemInputError(final ItemsInput curitsinp) {
        final StringBuilder detailInfo = new StringBuilder();
        detailInfo.append("userRequest=").append(curitsinp.getRequest()).append(", ");
        detailInfo.append("repoEntryKey=").append(assessmentInstance.getRepositoryEntryKey()).append(", ");
        detailInfo.append("assessId=").append(assessmentInstance.getAssessID()).append(", ");
        detailInfo.append("contextIdent=").append(assessmentInstance.getAssessmentContext().getIdent()).append(", ");
        detailInfo.append("contextAnsweredCount=").append(assessmentInstance.getAssessmentContext().getItemsAnsweredCount()).append(", ");
        detailInfo.append("contextAttemptedCount=").append(assessmentInstance.getAssessmentContext().getItemsAttemptedCount()).append(", ");
        detailInfo.append("contextPresentedCount=").append(assessmentInstance.getAssessmentContext().getItemsPresentedCount()).append(", ");
        detailInfo.append("sectionIdent=").append(assessmentInstance.getAssessmentContext().getCurrentSectionContext().getIdent()).append(", ");
        detailInfo.append("sectionAnsweredCount=").append(assessmentInstance.getAssessmentContext().getCurrentSectionContext().getItemsAnsweredCount()).append(", ");
        detailInfo.append("sectionAttemptedCount=").append(assessmentInstance.getAssessmentContext().getCurrentSectionContext().getItemsAttemptedCount()).append(", ");
        detailInfo.append("sectionPresentedCount=").append(assessmentInstance.getAssessmentContext().getCurrentSectionContext().getItemsPresentedCount());
        throw new RuntimeException("OLAT-6824: not even one iteminput in the answer [" + detailInfo.toString() + "]");
    }

    @Override
    public void submitAssessment() {
        getAssessmentInstance().close();
        final AssessmentContext ac = getAssessmentContext();
        if (ac.isFeedbackavailable()) {
            final Output outp = ac.getOutput();
            getInfo().setCurrentOutput(outp);
            getInfo().setFeedback(true);
        }
        info.clear();
        info.setMessage(QTIConstants.MESSAGE_ASSESSMENT_SUBMITTED);
        info.setStatus(QTIConstants.ASSESSMENT_FINISHED);
        info.setRenderItems(false);
    }

    @Override
    public void cancelAssessment() {
        getAssessmentInstance().close();
        info.clear();
        info.setMessage(QTIConstants.MESSAGE_ASSESSMENT_CANCELED);
        info.setStatus(QTIConstants.ASSESSMENT_CANCELED);
        info.setRenderItems(false);
    }

    /**
     * @return Info
     */
    @Override
    public Info getInfo() {
        return info;
    }

    protected void clearInfo() {
        info.clear();
    }

}
