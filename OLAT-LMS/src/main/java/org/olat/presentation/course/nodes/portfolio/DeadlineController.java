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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.presentation.course.nodes.portfolio;

import java.util.Date;

import org.olat.data.portfolio.structure.EPStructuredMap;
import org.olat.data.portfolio.structure.StructureStatusEnum;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.DateChooser;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

import com.ibm.icu.util.Calendar;

/**
 * Description:<br>
 * Small controller to use in a popup to change the deadline.
 * <P>
 * Initial Date: 11 nov. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class DeadlineController extends FormBasicController {

    private DateChooser deadlineChooser;
    private EPStructuredMap map;
    private final EPFrontendManager ePFMgr;

    public DeadlineController(final UserRequest ureq, final WindowControl wControl, final EPStructuredMap map) {
        super(ureq, wControl);
        this.map = map;

        ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean(EPFrontendManager.class);

        initForm(ureq);
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        setFormTitle("map.deadline.change.title");
        setFormDescription("map.deadline.change.description");

        deadlineChooser = uifactory.addDateChooser("map.deadline", "", formLayout);
        if (map.getDeadLine() == null) {
            final Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DATE, 7);
            deadlineChooser.setDate(cal.getTime());
        } else {
            deadlineChooser.setDate(map.getDeadLine());
        }
        deadlineChooser.setValidDateCheck("map.deadline.invalid");

        final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("ok-cancel", getTranslator());
        buttonLayout.setRootForm(mainForm);
        formLayout.add(buttonLayout);
        uifactory.addFormSubmitButton("ok", buttonLayout);
        uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
    }

    @Override
    protected void doDispose() {
        //
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        final Date newDeadLine = deadlineChooser.getDate();
        map = (EPStructuredMap) ePFMgr.loadPortfolioStructureByKey(map.getKey()); // OLAT-6335: refresh map in case it was changed meanwhile
        map.setDeadLine(newDeadLine);
        map.setStatus(StructureStatusEnum.OPEN);
        ePFMgr.savePortfolioStructure(map);
        fireEvent(ureq, Event.CHANGED_EVENT);
    }

    @Override
    protected void formCancelled(final UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

    /**
	 */
    @Override
    protected boolean validateFormLogic(final UserRequest ureq) {
        if (super.validateFormLogic(ureq)) {
            final Date newDeadLine = deadlineChooser.getDate();
            if (newDeadLine != null && newDeadLine.before(new Date())) {
                deadlineChooser.setErrorKey("map.deadline.invalid.before", null);
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
}
