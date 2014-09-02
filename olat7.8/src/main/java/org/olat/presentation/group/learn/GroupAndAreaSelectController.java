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

package org.olat.presentation.group.learn;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.choice.Choice;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.event.Event;
import org.olat.system.exception.OLATRuntimeException;

/**
 * Description:<BR/>
 * Controller to handle a popup business group or business group area selection as a checkbox list. After selecting the items, the values will be put together to a comma
 * separated string and written to the original window via javascript. Initial Date: Oct 5, 2004
 * 
 * @author gnaegi
 */
public class GroupAndAreaSelectController extends DefaultController {
    private static final String PACKAGE = PackageUtil.getPackageName(GroupAndAreaSelectController.class);
    private static final String VELOCITY_ROOT = PackageUtil.getPackageVelocityRoot(PACKAGE);

    /** Configuration flag: use strings for business groups */
    public static final int TYPE_GROUP = 1;
    /** Configuration flag: use strings for business group areas */
    public static final int TYPE_AREA = 2;

    private StringListTableDataModel stringModel;
    private final VelocityContainer main;
    private Choice stringChoice;
    private final String htmlElemId;

    /**
     * @param ureq
     *            The user request
     * @param cgm
     *            The course group manager
     * @param type
     *            The choice type: group or area (use controller public constants)
     * @param preselectedNames
     *            String containing the comma separated names that should be preselected
     * @param htmlElemId
     *            the name of the html id of the window that opened this popup controller
     */
    public GroupAndAreaSelectController(final OLATResourceable ores, final UserRequest ureq, final WindowControl wControl, final CourseGroupManager cgm, final int type,
            final String preselectedNames, final String htmlElemId) {
        super(wControl);
        final Translator trans = new PackageTranslator(PACKAGE, ureq.getLocale());
        List namesList;
        this.htmlElemId = htmlElemId;
        // main window containg title and the cooser list
        main = new VelocityContainer("main", VELOCITY_ROOT + "/groupandareaselect.html", trans, this);

        // initialize some type specific stuff
        switch (type) {
        case 1:
            namesList = cgm.getUniqueLearningGroupNamesFromAllContexts(ores);
            main.contextPut("title", trans.translate("groupandareaselect.groups.title"));
            main.contextPut("noChoicesText", trans.translate("groupandareaselect.groups.nodata"));
            break;
        case 2:
            namesList = cgm.getUniqueAreaNamesFromAllContexts(ores);
            main.contextPut("title", trans.translate("groupandareaselect.areas.title"));
            main.contextPut("noChoicesText", trans.translate("groupandareaselect.areas.nodata"));
            break;
        default:
            throw new OLATRuntimeException("Must use valid type. type::" + type, null);
        }

        // get preselected List from the comma separated string
        List preselectedNamesList;
        if (preselectedNames == null) {
            preselectedNamesList = new ArrayList();
        } else {
            preselectedNamesList = stringToList(preselectedNames);
        }

        if (namesList.size() > 0) {
            stringModel = new StringListTableDataModel(namesList, preselectedNamesList);
            stringChoice = new Choice("stringChoice", trans);
            stringChoice.setSubmitKey("select");
            stringChoice.setCancelKey("cancel");
            stringChoice.setTableDataModel(stringModel);
            stringChoice.addListener(this);
            main.put("stringChoice", stringChoice);
            main.contextPut("hasChoices", Boolean.TRUE);
        } else {
            main.contextPut("hasChoices", Boolean.FALSE);
        }
        setInitialComponent(main);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == stringChoice) {
            if (event == Choice.EVNT_FORM_CANCELLED) {
                main.setPage(VELOCITY_ROOT + "/cancelled.html");
            } else {
                final List selectedList = stringChoice.getSelectedRows();
                final List selectedEntries = new ArrayList();
                for (final Iterator iter = selectedList.iterator(); iter.hasNext();) {
                    final Integer sel = (Integer) iter.next();
                    final String obj = stringModel.getString(sel.intValue());
                    selectedEntries.add(obj);
                }
                final String selectedString = listToString(selectedEntries);
                main.setPage(VELOCITY_ROOT + "/closing.html");
                main.contextPut("var", htmlElemId);
                main.contextPut("val", selectedString);
            }
        }
    }

    /**
     * Converts a list of strings to a comma separated string
     * 
     * @param myList
     * @return String
     */
    private String listToString(final List myList) {
        boolean first = true;
        final StringBuilder sb = new StringBuilder();
        final Iterator iterator = myList.iterator();
        while (iterator.hasNext()) {
            final String name = (String) iterator.next();
            if (!first) {
                sb.append(",");
            } else {
                first = false;
            }
            sb.append(name);
        }
        return sb.toString();
    }

    /**
     * Converts a coma separated string to a list containing the strings
     * 
     * @param s
     *            the comma separated string
     * @return the List of strings
     */
    private List stringToList(final String s) {
        final String[] sArray = s.split(",");
        final List result = new ArrayList();
        for (int i = 0; i < sArray.length; i++) {
            result.add(sArray[i].trim());
        }
        return result;
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to dispose
    }

}
