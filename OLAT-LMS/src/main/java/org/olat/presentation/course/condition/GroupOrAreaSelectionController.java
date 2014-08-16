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
package org.olat.presentation.course.condition;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.olat.data.group.context.BGContext;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.Reset;
import org.olat.presentation.framework.core.components.form.flexible.elements.Submit;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.FormReset;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.FormSubmit;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.group.BGControllerFactory;
import org.olat.presentation.group.NewAreaController;
import org.olat.presentation.group.NewBGController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * TODO: patrickb Class Description for MultiSelectColumnController
 * <P>
 * Initial Date: 15.06.2007 <br>
 * 
 * @author patrickb
 */
public class GroupOrAreaSelectionController extends FormBasicController {

    private MultipleSelectionElement entrySelector;
    protected String[] entries;
    private final String title;
    private FormLinkImpl createNew;
    private final CourseGroupManager courseGrpMngr;
    private final BGContext bgContext;
    private final boolean inGroupMode;
    private NewBGController groupCreateCntrllr;
    private NewAreaController areaCreateCntrllr;
    private CloseableModalController cmc;
    private OLATResourceable ores;

    public GroupOrAreaSelectionController(final OLATResourceable ores, final int groupOrArea, final WindowControl wControl, final UserRequest ureq, final String title,
            final CourseGroupManager courseGrpMngr, final String selectionAsCsvStr) {
        super(ureq, wControl, "group_or_area_selection");
        this.ores = ores;
        /*
         * before initialising the element, prepare data
         */
        this.courseGrpMngr = courseGrpMngr;
        // group or area mode
        this.inGroupMode = groupOrArea == 0;
        //
        this.bgContext = getDefaultBGContext();
        // unique names from list to arry
        List<String> uniqueNames = null;
        if (inGroupMode) {
            uniqueNames = courseGrpMngr.getUniqueLearningGroupNamesFromAllContexts(ores);
        } else {
            uniqueNames = courseGrpMngr.getUniqueAreaNamesFromAllContexts(ores);
        }

        entries = new String[uniqueNames.size()];
        uniqueNames.toArray(entries);
        this.title = title;
        /*
         * init form elements
         */
        initForm(this.flc, this, ureq);
        /*
         * after initialising the element, select the entries
         */
        final String[] activeSelection = selectionAsCsvStr != null ? selectionAsCsvStr.split(",") : new String[] {};
        for (int i = 0; i < activeSelection.length; i++) {
            entrySelector.select(activeSelection[i].trim(), true);
        }
    }

    /*
     * find default context if one is present
     */
    private BGContext getDefaultBGContext() {
        final List courseLGContextes = courseGrpMngr.getLearningGroupContexts(ores);
        for (final Iterator iter = courseLGContextes.iterator(); iter.hasNext();) {
            final BGContext bctxt = (BGContext) iter.next();
            if (bctxt.isDefaultContext()) {
                return bctxt;
            }
        }
        return null;
        // not found! this is inacceptable! -> disable creation of groups!

    }

    @Override
    @SuppressWarnings("unused")
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        if (source == createNew) {
            if (inGroupMode) {

                // user wants to create a new group -> show group create form

                removeAsListenerAndDispose(groupCreateCntrllr);
                groupCreateCntrllr = BGControllerFactory.getInstance().createNewBGController(ureq, getWindowControl(), true, bgContext, true, null);
                listenTo(groupCreateCntrllr);

                removeAsListenerAndDispose(cmc);
                cmc = new CloseableModalController(getWindowControl(), "close", groupCreateCntrllr.getInitialComponent());
                listenTo(cmc);

                cmc.activate();

            } else {

                // user wants to create a new area -> show new area create form

                removeAsListenerAndDispose(areaCreateCntrllr);
                areaCreateCntrllr = BGControllerFactory.getInstance().createNewAreaController(ureq, getWindowControl(), bgContext);
                listenTo(areaCreateCntrllr);

                removeAsListenerAndDispose(cmc);
                cmc = new CloseableModalController(getWindowControl(), "close", areaCreateCntrllr.getInitialComponent());
                listenTo(cmc);

                cmc.activate();
            }
        }
    }

    @Override
    @SuppressWarnings("unused")
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == groupCreateCntrllr) {

            cmc.deactivate();

            if (event == Event.DONE_EVENT) {
                List<String> uniqueNames = null;
                uniqueNames = courseGrpMngr.getUniqueLearningGroupNamesFromAllContexts(ores);
                // update entries
                entries = new String[uniqueNames.size()];
                uniqueNames.toArray(entries);
                entrySelector.setKeysAndValues(entries, entries, null);
                //
                // select new value
                entrySelector.select(groupCreateCntrllr.getCreatedGroup().getName(), true);

                // inform condition config easy about new groups -> which informs further
                fireEvent(ureq, Event.CHANGED_EVENT);
            }
        } else if (source == areaCreateCntrllr) {

            cmc.deactivate();

            if (event == Event.DONE_EVENT) {
                List<String> uniqueNames = null;
                uniqueNames = courseGrpMngr.getUniqueAreaNamesFromAllContexts(ores);
                // update entries
                entries = new String[uniqueNames.size()];
                uniqueNames.toArray(entries);
                entrySelector.setKeysAndValues(entries, entries, null);
                //
                // select new value
                entrySelector.select(areaCreateCntrllr.getCreatedAreaName(), true);

                // inform condition config easy about new groups -> which informs further
                fireEvent(ureq, Event.CHANGED_EVENT);
            }
        }
    }

    /**
	 */
    @Override
    @SuppressWarnings("unused")
    protected void doDispose() {
        //
    }

    @Override
    @SuppressWarnings("unused")
    protected void initForm(final FormItemContainer boundTo, final Controller listener, final UserRequest ureq) {
        /*
         * // | [] group 1 | create group | // | [] group 2 | | // | submit | cancel | | //
         */
        if (bgContext != null) {
            // easy creation only possible if a default group context available
            if (inGroupMode) {
                createNew = new FormLinkImpl("create");
            } else {
                createNew = new FormLinkImpl("create");
            }
            // is a button
            createNew.setCustomEnabledLinkCSS("b_button");
            createNew.setCustomDisabledLinkCSS("b_button b_disabled");
            // create new group/area on the right side
            boundTo.add(createNew);
        }

        entrySelector = uifactory.addCheckboxesVertical("entries", null, boundTo, entries, entries, null, 1);
        // submitCancel after checkboxes
        //
        final Submit subm = new FormSubmit("subm", "apply");
        final Reset reset = new FormReset("reset", "cancel");
        boundTo.add(subm);
        boundTo.add(reset);
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void formResetted(final UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

    public Set getSelectedEntries() {
        return entrySelector.getSelectedKeys();
    }

}
