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
 * Copyright (c) 2009 frentix GmbH, www.frentix.com
 * <p>
 */
package org.olat.presentation.forum;

import java.util.Arrays;

import org.olat.lms.preferences.Preferences;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.SingleSelection;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;

/**
 * Description:<br>
 * Provides a Switch for all supported forum-view-modes and fires Event to ForumController.
 * <P>
 * Initial Date: 25.06.2009 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, frentix GmbH
 */
public class ForumThreadViewModeController extends FormBasicController {
    protected static final String VIEWMODE_THREAD = "thread";
    protected static final String VIEWMODE_FLAT = "flat";
    protected static final String VIEWMODE_MESSAGE = "message";
    protected static final String VIEWMODE_MARKED = "marked";
    protected static final String VIEWMODE_NEW = "new";

    private static final String[] viewKeys = new String[] { VIEWMODE_THREAD, VIEWMODE_FLAT, VIEWMODE_MESSAGE, VIEWMODE_MARKED, VIEWMODE_NEW };
    private static final String GUI_PREFS_VIEWMODE_KEY = "forum.threadview.mode";

    private SingleSelection viewRadio;
    private final String startingViewMode;

    /**
     * @param ureq
     * @param control
     */
    public ForumThreadViewModeController(final UserRequest ureq, final WindowControl control, final String startingViewMode) {
        super(ureq, control);
        setFormStyle("o_forum_switch b_float_right");
        this.startingViewMode = startingViewMode;
        initForm(ureq);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing
    }

    /**
	 */
    @Override
    protected void formOK(final UserRequest ureq) {
        // nothing to do, handled with innerEvents
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        final String[] viewValues = new String[] { translate("viewswitch.threadview"), translate("viewswitch.flatview"), translate("viewswitch.messageview"),
                translate("viewswitch.marked"), translate("viewswitch.new") };
        viewRadio = uifactory.addRadiosHorizontal("viewswitch.title", formLayout, viewKeys, viewValues);
        viewRadio.addActionListener(listener, FormEvent.ONCLICK);
        // preselect according to user-settings

        if (startingViewMode == null) {
            viewRadio.select(getThreadViewMode(ureq), true);
        } else {
            viewRadio.select(startingViewMode, true);
        }
    }

    /**
     * org.olat.presentation.framework.components.form.flexible.FormItem, org.olat.presentation.framework.components.form.flexible.impl.FormEvent)
     */
    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        if (source == viewRadio) {
            final String newViewMode = viewRadio.getSelectedKey();
            if (Arrays.asList(viewKeys).contains(newViewMode)) {
                saveThreadViewModePrefs(ureq, newViewMode);
                fireEvent(ureq, Event.CHANGED_EVENT);
            } else {
                throw new AssertException("ForumViewController doesn't support this view-mode.");
            }
        }
    }

    private void saveThreadViewModePrefs(final UserRequest ureq, final String viewMode) {
        if (viewMode.equals(VIEWMODE_THREAD) || viewMode.equals(VIEWMODE_FLAT) || viewMode.equals(VIEWMODE_MESSAGE)) {
            final Preferences prefs = ureq.getUserSession().getGuiPreferences();
            prefs.putAndSave(this.getClass(), GUI_PREFS_VIEWMODE_KEY, viewMode);
        }
    }

    public String getSelectedViewMode() {
        if (viewRadio == null) {
            return null;
        }
        return viewRadio.getSelectedKey();
    }

    // TODO:RH:forum: move this to upgrade code or to manager, to migrate settings, or kill
    // them all???
    public String getThreadViewMode(final UserRequest ureq) {
        final Preferences prefs = ureq.getUserSession().getGuiPreferences();
        // migrate old settings, keep appropriate to new possibilities
        final Boolean threadview = (Boolean) prefs.get(ForumController.class, ForumController.GUI_PREFS_THREADVIEW_KEY);
        if (threadview != null) {
            // remove old pref
            prefs.putAndSave(ForumController.class, ForumController.GUI_PREFS_THREADVIEW_KEY, null);
            if (threadview) {
                prefs.putAndSave(this.getClass(), GUI_PREFS_VIEWMODE_KEY, VIEWMODE_FLAT);
                return VIEWMODE_FLAT;
            } else {
                prefs.putAndSave(this.getClass(), GUI_PREFS_VIEWMODE_KEY, VIEWMODE_MESSAGE);
                return VIEWMODE_MESSAGE;
            }
        }
        return (String) prefs.get(this.getClass(), GUI_PREFS_VIEWMODE_KEY, VIEWMODE_THREAD);
    }
}
