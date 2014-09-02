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
package org.olat.presentation.portfolio.filter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.MultipleSelectionElementImpl;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * edit tag filter with all available tags of this user
 * <P>
 * Initial Date: 28.10.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPArtefactTagSelectionController extends FormBasicController {

    private List<String> selectedTagsList;
    private final EPFrontendManager ePFMgr;
    private MultipleSelectionElementImpl chkBox;

    public EPArtefactTagSelectionController(final UserRequest ureq, final WindowControl wControl, final List<String> selectedTagsList) {
        super(ureq, wControl);

        ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean(EPFrontendManager.class);
        this.selectedTagsList = selectedTagsList;
        initForm(ureq);
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @SuppressWarnings("unused")
    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        // setFormTitle("filter.tag.title");
        setFormDescription("filter.tag.intro");

        final Map<String, String> allUserTags = ePFMgr.getUsersMostUsedTags(getIdentity(), -1);
        final LinkedList<Entry<String, String>> sortEntrySet = new LinkedList<Entry<String, String>>(allUserTags.entrySet());
        final String[] keys = new String[sortEntrySet.size()];
        final String[] values = new String[sortEntrySet.size()];
        int i = 0;
        for (final Entry<String, String> entry : sortEntrySet) {
            final String tag = entry.getValue();
            keys[i] = tag;
            values[i] = tag;
            i++;
        }
        chkBox = (MultipleSelectionElementImpl) uifactory.addCheckboxesVertical("tag", null, formLayout, keys, values, null, 2);

        if (selectedTagsList != null) {
            final String[] selectedKeys = selectedTagsList.toArray(new String[0]);
            chkBox.setSelectedValues(selectedKeys);
        }
        chkBox.addActionListener(this, FormEvent.ONCHANGE);
        uifactory.addFormSubmitButton("filter.type.submit", formLayout);
    }

    /**
     * org.olat.presentation.framework.components.form.flexible.FormItem, org.olat.presentation.framework.components.form.flexible.impl.FormEvent)
     */
    @SuppressWarnings("unused")
    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        if (selectedTagsList == null) {
            selectedTagsList = new ArrayList<String>();
        }
        final Set<String> selectedKeys = chkBox.getSelectedKeys();
        final Set<String> allKeys = chkBox.getKeys();
        for (final String actTag : allKeys) {
            final boolean selected = selectedKeys.contains(actTag);
            if (selected && !selectedTagsList.contains(actTag)) {
                selectedTagsList.add(actTag);
            }
            if (!selected && selectedTagsList.contains(actTag)) {
                selectedTagsList.remove(actTag);
            }
        }
        if (selectedTagsList.size() == 0) {
            selectedTagsList = null;
        }

        fireEvent(ureq, Event.CHANGED_EVENT);
    }

    /**
	 */
    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing
    }

}
