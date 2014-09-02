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

package org.olat.presentation.calendar;

import java.util.Date;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.DateChooser;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;

public class SearchAllCalendarsForm extends FormBasicController {

    private TextElement subject, location;
    private DateChooser beginPeriod, endPeriod;

    public SearchAllCalendarsForm(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        initForm(ureq);
    }

    public boolean validate() {
        boolean valid = false;
        valid = !subject.isEmpty() || !location.isEmpty();
        final Date begin = beginPeriod.getDate();
        final Date end = endPeriod.getDate();
        if (begin != null && end != null && begin.after(end)) {
            beginPeriod.setErrorKey("cal.search.errorPeriodOverlap", null);
        }
        if (!valid) {
            subject.setErrorKey("cal.search.errorNoParams", null);
            return false;
        }
        return true;
    }

    public String getSubject() {
        return subject.getValue();
    }

    public String getLocation() {
        return location.getValue();
    }

    public Date getBeginPeriod() {
        return beginPeriod.getDate();
    }

    public Date getEndPeriod() {
        // End period is inclusive, therefore adjust end period by one day
        final Date end = endPeriod.getDate();
        if (end != null) {
            return new Date(end.getTime() + 24 * 60 * 60 * 1000);
        }
        return null;
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        setFormTitle("cal.search.title");
        subject = uifactory.addTextElement("cal.search.subject", "cal.search.subject", 40, "", formLayout);
        location = uifactory.addTextElement("cal.search.location", "cal.search.location", 40, "", formLayout);
        beginPeriod = uifactory.addDateChooser("begin", "cal.search.beginPeriod", "", formLayout);
        endPeriod = uifactory.addDateChooser("end", "cal.search.endPeriod", "", formLayout);

        beginPeriod.setExampleKey("cal.form.recurrence.end.example", null);
        endPeriod.setExampleKey("cal.form.recurrence.end.example", null);

        uifactory.addFormSubmitButton("submit", "cal.search.submit", formLayout);
    }

    @Override
    protected void doDispose() {
        //
    }
}
