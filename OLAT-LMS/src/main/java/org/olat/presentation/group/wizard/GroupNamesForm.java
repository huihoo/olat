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

package org.olat.presentation.group.wizard;

import java.util.ArrayList;
import java.util.List;

import org.olat.data.group.BusinessGroup;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;

/**
 * Description:<BR>
 * Form to enter multiple group names
 * <P>
 * Initial Date: Oct 20, 2004
 * 
 * @author gnaegi
 */
public class GroupNamesForm extends FormBasicController {

    private TextElement groupNames;
    private TextElement bgMax;
    private List groupNamesList;

    private final Integer defaultMaxValue;

    public GroupNamesForm(final UserRequest ureq, final WindowControl wControl, final Integer defaultMaxValue) {
        super(ureq, wControl);
        this.defaultMaxValue = defaultMaxValue;
        ;
        initForm(ureq);
    }

    @Override
    protected boolean validateFormLogic(final UserRequest ureq) {
        final List namesList = new ArrayList();
        final String groupNamesString = groupNames.getValue();
        final String[] groups = groupNamesString.split("[\t\n\f\r]");

        for (int i = 0; i < groups.length; i++) {
            final String groupName = groups[i].trim();
            if (groupName.length() > BusinessGroup.MAX_GROUP_NAME_LENGTH) {
                groupNames.setErrorKey("bgcopywizard.multiple.groupnames.tooLongGroupname", null);
                return false;
            } else if (!groupName.matches(BusinessGroup.VALID_GROUPNAME_REGEXP)) {
                groupNames.setErrorKey("bgcopywizard.multiple.groupnames.illegalGroupname", null);
                return false;
            }
            // ignore lines that contains only whitespace, groupname must have
            // non-whitespace
            if (StringHelper.containsNonWhitespace(groupName)) {
                namesList.add(groupName);
            }
        }
        // list seems to be valid. store for later retrival
        this.groupNamesList = namesList;

        if (namesList.size() == 0) {
            groupNames.setErrorKey("create.form.error.emptylist", null);
            return false;
        }

        return true;
    }

    /**
     * @return A valid list of groupnames. The list is only valid if the form returned the validation ok event!
     */
    public List getGroupNamesList() {
        if (this.groupNamesList == null) {
            throw new AssertException("getGroupNamesList() called prior to form EVENT_VALIDATION_OK event");
        }
        return this.groupNamesList;
    }

    /**
     * @return Integer max number of group participants
     */
    public Integer getGroupMax() {
        final String result = bgMax.getValue();
        if (result.length() == 0) {
            return null;
        }
        return new Integer(Integer.parseInt(result));
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        groupNames = uifactory.addTextAreaElement("groupNames", "bgcopywizard.multiple.groupnames", -1, 4, 10, true, "", formLayout);
        bgMax = uifactory.addTextElement("fe_bgMax", "create.form.title.max", 3, "", formLayout);
        if (defaultMaxValue != null) {
            bgMax.setValue(defaultMaxValue.toString());
        }
        bgMax.setRegexMatchCheck("^[0-9]*$", "create.form.error.numberOrNull");
        bgMax.setDisplaySize(3);
        uifactory.addFormSubmitButton("finish", formLayout);
    }

    @Override
    protected void doDispose() {
        //
    }

}
