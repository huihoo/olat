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
package org.olat.presentation.notifications;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.data.notifications.Subscriber;
import org.olat.presentation.framework.common.ControllerFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.DateChooser;
import org.olat.presentation.framework.core.components.form.flexible.elements.FormLink;
import org.olat.presentation.framework.core.components.form.flexible.elements.SingleSelection;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * This controller provides a simple date chooser
 * <p>
 * Events fired by this controller:
 * <ul>
 * <li>Event.CHANGED_EVENT whenever the date has been changed</li>
 * </ul>
 * <P>
 * Initial Date: 22.12.2009 <br>
 * 
 * @author gnaegi
 */

public class DateChooserController extends FormBasicController {
    private DateChooser dateChooser;
    private final Date initDate;
    private FormLink link;

    private SingleSelection typeSelection;
    private String[] typeKeys;
    private String[] typeValues;

    public DateChooserController(final UserRequest ureq, final WindowControl wControl, final Date initDate) {
        super(ureq, wControl);
        this.initDate = initDate;

        typeKeys = new String[] { "all" };
        typeValues = new String[] { translate("news.type.all") };

        initForm(ureq);
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        dateChooser = uifactory.addDateChooser("news.since", "", formLayout);
        // FIXME: Can't use time format for now, only date format due to bug OLAT-4736
        // dateChooser.setDateChooserTimeEnabled(true);
        dateChooser.setDate(initDate);
        dateChooser.addActionListener(this, FormEvent.ONCHANGE);

        typeSelection = uifactory.addDropdownSingleselect("news.type", "news.type", formLayout, typeKeys, typeValues, null);
        typeSelection.addActionListener(this, FormEvent.ONCHANGE);
        typeSelection.select("all", true);

        // link = uifactory.addFormLink("news.since.link", formLayout);
    }

    /**
	 */
    @Override
    protected void formOK(final UserRequest ureq) {
        // nothing to do here
    }

    @Override
    protected boolean validateFormLogic(final UserRequest ureq) {
        final boolean isInputValid = true;
        if (dateChooser.hasError() || dateChooser.getDate() == null) {
            dateChooser.setErrorKey("error.date", new String[0]);
            return false;
        }
        return isInputValid;
    }

    /**
     * org.olat.presentation.framework.components.form.flexible.FormItem, org.olat.presentation.framework.components.form.flexible.impl.FormEvent)
     */
    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        if (source == link && !dateChooser.hasError()) {
            flc.getRootForm().submit(ureq);
            fireEvent(ureq, Event.CHANGED_EVENT);
        } else if (source == dateChooser && !dateChooser.hasError()) {
            fireEvent(ureq, Event.CHANGED_EVENT);
        } else if (source == typeSelection) {
            fireEvent(ureq, Event.CHANGED_EVENT);
        }
    }

    /**
     * Get the date that has been chosen
     * 
     * @return
     */
    public Date getChoosenDate() {
        return dateChooser.getDate();
    }

    public void setDate(final Date date) {
        dateChooser.setDate(date);
    }

    public String getType() {
        if (typeSelection.isSelected(0) || !typeSelection.isOneSelected()) {
            return null;
        }
        return typeSelection.getSelectedKey();
    }

    public void setType(final String type) {
        if (StringHelper.containsNonWhitespace(type)) {
            for (final String typeKey : typeKeys) {
                if (type.equals(typeKey)) {
                    typeSelection.select(type, true);
                    break;
                }
            }
        } else {
            typeSelection.select("all", true);
        }
    }

    public void setSubscribers(final List<Subscriber> subscribers) {
        final String selectedKey = typeSelection.isOneSelected() ? typeSelection.getSelectedKey() : null;

        final Set<String> types = new HashSet<String>();
        for (final Subscriber subscriber : subscribers) {
            final String type = subscriber.getPublisher().getType();
            types.add(type);
        }

        typeKeys = new String[types.size() + 1];
        typeValues = new String[types.size() + 1];

        typeKeys[0] = "all";
        typeValues[0] = translate("news.type.all");

        int count = 1;
        for (final String type : types) {
            final String typeName = ControllerFactory.translateResourceableTypeName(type, getLocale());
            typeKeys[count] = type;
            typeValues[count++] = typeName;
        }

        typeSelection.setKeysAndValues(typeKeys, typeValues, null);

        if (selectedKey != null) {
            // select the current key but check if it still exists
            for (final String typeKey : typeKeys) {
                if (typeKey.equals(selectedKey)) {
                    typeSelection.select(selectedKey, true);
                    break;
                }
            }
        }

        if (!typeSelection.isOneSelected()) {
            typeSelection.select("all", true);
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to dispose
    }

}
