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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.olat.data.basesecurity.Roles;
import org.olat.data.group.BusinessGroup;
import org.olat.data.group.area.BGArea;
import org.olat.data.group.area.BGAreaDaoImpl;
import org.olat.data.group.context.BGContext;
import org.olat.data.group.context.BGContextDaoImpl;
import org.olat.lms.commons.mediaresource.CleanupAfterDeliveryFileMediaResource;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.user.UserService;
import org.olat.lms.user.propertyhandler.UserPropertyHandler;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.choice.Choice;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.table.GenericObjectArrayTableDataModel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.choice.ChoiceController;
import org.olat.presentation.framework.core.control.generic.wizard.WizardController;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Wizard for choosing the format of the member list archive.
 * <p>
 * First step: choose the interest groups/areas
 * <p>
 * Second step: choose the columns for the user info (e.g. username, firstname, lastname, ...)
 * <p>
 * Third step: choose the output format type, either output all members in a single .xls file, or create a zip with a .xls file per group.
 * <p>
 * Fourth step: Download file and cleanup temp file upon dispose.
 * <P>
 * Initial Date: 30.07.2007 <br>
 * 
 * @author Lavinia Dumitrescu
 */
public class MemberListWizardController extends BasicController {

    private final BGContext context;
    private final ChoiceController colsChoiceController;
    private final Choice groupsOrAreaChoice;
    private ChoiceController outputChoiceController;

    private final Panel main;
    private final WizardController wizardController;
    private final int wizardSteps = 4;

    public final static String GROUPS_MEMBERS = "g_m";
    public final static String AREAS_MEMBERS = "a_m";
    private String wizardType = GROUPS_MEMBERS; // default

    private final VelocityContainer velocityContainer2;
    private final VelocityContainer velocityContainer3;
    private final VelocityContainer velocityContainer4;
    private final Link backToFirstChoice;
    private final Link backToSecondChoice;
    private final Link showFileLink;

    private List<String> columList;
    private List<BusinessGroup> groupList;
    private List<BGArea> areaList;
    private String archiveType;
    private MediaResource archiveMediaResource;
    private static final String usageIdentifyer = MemberListWizardController.class.getCanonicalName();
    private final Translator propertyHandlerTranslator;
    private UserService userService;

    /**
     * @param ureq
     * @param wControl
     * @param context
     * @param type
     */
    public MemberListWizardController(final UserRequest ureq, final WindowControl wControl, final BGContext context, final String type) {
        super(ureq, wControl);

        this.context = context;
        userService = CoreSpringFactory.getBean(UserService.class);
        propertyHandlerTranslator = userService.getUserPropertiesConfig().getTranslator(getTranslator());

        if (GROUPS_MEMBERS.equals(type) || AREAS_MEMBERS.equals(type)) {
            this.wizardType = type;
        }

        columList = new ArrayList<String>();
        groupList = new ArrayList<BusinessGroup>();
        areaList = new ArrayList<BGArea>();
        main = new Panel("main");

        // init wizard step 1
        groupsOrAreaChoice = new Choice("groupsOrAreaChoice", getTranslator());
        groupsOrAreaChoice.setTableDataModel(getGroupOrAreaChoiceTableDataModel(context));
        groupsOrAreaChoice.addListener(this);
        groupsOrAreaChoice.setSubmitKey("next");

        // init wizard step 2
        final boolean singleSelection = false;
        final boolean layoutVertical = true;
        final String[] keys = getColsChoiceKeys(ureq);
        final String[] selectedKeys = getFirstN(keys, 4);
        colsChoiceController = new ChoiceController(ureq, getWindowControl(), keys, getTranslatedKeys(propertyHandlerTranslator, keys), selectedKeys, singleSelection,
                layoutVertical, "next");
        this.listenTo(colsChoiceController);

        wizardController = new WizardController(ureq, wControl, wizardSteps);
        this.listenTo(wizardController);
        wizardController.setWizardTitle(translate("memberlistwizard.title"));
        if (GROUPS_MEMBERS.equals(wizardType)) {
            wizardController.setNextWizardStep(translate("memberlistwizard.groupchoice"), groupsOrAreaChoice);
        } else if (AREAS_MEMBERS.equals(wizardType)) {
            wizardController.setNextWizardStep(translate("memberlistwizard.areachoice"), groupsOrAreaChoice);
        }
        main.setContent(wizardController.getInitialComponent());
        this.putInitialPanel(main);

        // step 2
        velocityContainer2 = this.createVelocityContainer("listWizardStep2");
        backToFirstChoice = LinkFactory.createLinkBack(velocityContainer2, this);
        // step 3
        velocityContainer3 = this.createVelocityContainer("listWizardStep3");
        backToSecondChoice = LinkFactory.createLinkBack(velocityContainer3, this);
        // last step
        velocityContainer4 = this.createVelocityContainer("listWizardStep4");
        showFileLink = LinkFactory.createButton("showfile", velocityContainer4, this);
        // mark that this link starts a download
        LinkFactory.markDownloadLink(showFileLink);
    }

    /**
     * @param keys
     * @param n
     * @return an array with the first n elements of the input array
     */
    private String[] getFirstN(final String[] keys, int n) {
        if (n < 0 || n > keys.length) {
            n = keys.length;
        }
        final String[] selKeys = new String[n];
        for (int i = 0; i < n; i++) {
            selKeys[i] = keys[i];
        }
        return selKeys;
    }

    private String[] getColsChoiceKeys(final UserRequest ureq) {
        final Roles roles = ureq.getUserSession().getRoles();
        final boolean isAdministrativeUser = roles.isAdministrativeUser();
        final List<UserPropertyHandler> userPropertyHandlers = userService.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
        final Iterator<UserPropertyHandler> propertyIterator = userPropertyHandlers.iterator();
        final ArrayList<String> array = new ArrayList<String>();
        // add username first, next the user properties
        array.add("username");
        while (propertyIterator.hasNext()) {
            array.add(propertyIterator.next().i18nColumnDescriptorLabelKey());
        }
        String[] keys = new String[array.size()];
        keys = array.toArray(keys);
        return keys;
    }

    private String[] getTranslatedKeys(final Translator keyTranslator, final String[] keys) {
        final int size = keys.length;
        final String[] translated = new String[size];
        for (int i = 0; i < size; i++) {
            translated[i] = keyTranslator.translate(keys[i]);
        }
        return translated;
    }

    /**
     * Creates a <code>Choice</code> <code>TableDataModel</code> for the group/area choice. <br>
     * It contains two columns: booleans (true per default) on the first column, and ObjectWrappers for the second column.
     * 
     * @param context
     * @return a GenericObjectArrayTableDataModel instead of a TableDataModel since it has to provide a setValueAt method.
     */
    private GenericObjectArrayTableDataModel getGroupOrAreaChoiceTableDataModel(final BGContext context) {
        final List objectArrays = new ArrayList();
        if (GROUPS_MEMBERS.equals(wizardType)) {
            final List<BusinessGroup> groups = BGContextDaoImpl.getInstance().getGroupsOfBGContext(context);
            Collections.sort(groups, new Comparator() {
                @Override
                public int compare(final Object o1, final Object o2) {
                    final BusinessGroup g1 = (BusinessGroup) o1;
                    final BusinessGroup g2 = (BusinessGroup) o2;
                    return g1.getName().compareTo(g2.getName());
                }
            });
            for (final Iterator iter = groups.iterator(); iter.hasNext();) {
                final BusinessGroup group = (BusinessGroup) iter.next();
                final Object[] groupChoiceRowData = new Object[2];
                groupChoiceRowData[0] = new Boolean(true);
                groupChoiceRowData[1] = new ObjectWrapper(group);
                objectArrays.add(groupChoiceRowData);
            }
        } else if (AREAS_MEMBERS.equals(wizardType)) {
            final List<BGArea> areas = BGAreaDaoImpl.getInstance().findBGAreasOfBGContext(context);
            Collections.sort(areas, new Comparator() {
                @Override
                public int compare(final Object o1, final Object o2) {
                    final BGArea a1 = (BGArea) o1;
                    final BGArea a2 = (BGArea) o2;
                    return a1.getName().compareTo(a2.getName());
                }
            });
            for (final Iterator iter = areas.iterator(); iter.hasNext();) {
                final BGArea area = (BGArea) iter.next();
                final Object[] groupChoiceRowData = new Object[2];
                groupChoiceRowData[0] = new Boolean(true);
                groupChoiceRowData[1] = new ObjectWrapper(area);
                objectArrays.add(groupChoiceRowData);
            }
        }
        final GenericObjectArrayTableDataModel tableModel = new GenericObjectArrayTableDataModel(objectArrays, 2);
        return tableModel;
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // default wizard will listen to cancel wizard event
        // wizardController.event(ureq, source, event);
        // wizard steps events
        if (source == groupsOrAreaChoice) {
            if (event == Choice.EVNT_VALIDATION_OK) {
                final List selRows = groupsOrAreaChoice.getSelectedRows();
                if (selRows.size() == 0) {
                    if (GROUPS_MEMBERS.equals(wizardType)) {
                        this.showError("error.selectatleastonegroup");
                    } else if (AREAS_MEMBERS.equals(wizardType)) {
                        this.showError("error.selectatleastonearea");
                    }
                } else {
                    if (GROUPS_MEMBERS.equals(wizardType)) {
                        this.setGroupList(getSelectedValues(groupsOrAreaChoice));
                    } else if (AREAS_MEMBERS.equals(wizardType)) {
                        this.setAreaList(getSelectedValues(groupsOrAreaChoice));
                    }
                    velocityContainer2.put("colsChoice", colsChoiceController.getInitialComponent());
                    wizardController.setNextWizardStep(translate("memberlistwizard.colchoice"), velocityContainer2);
                }
            }
        } else if (source == backToFirstChoice) {
            syncTableModelWithSelection(groupsOrAreaChoice);
            if (GROUPS_MEMBERS.equals(wizardType)) {
                wizardController.setBackWizardStep(translate("memberlistwizard.groupchoice"), groupsOrAreaChoice);
            } else if (AREAS_MEMBERS.equals(wizardType)) {
                wizardController.setBackWizardStep(translate("memberlistwizard.areachoice"), groupsOrAreaChoice);
            }
        } else if (source == backToSecondChoice) {
            wizardController.setBackWizardStep(translate("memberlistwizard.colchoice"), velocityContainer2);
        } else if (source == showFileLink) {
            ureq.getDispatchResult().setResultingMediaResource(this.getArchiveMediaResource());
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == wizardController) {
            if (event == Event.CANCELLED_EVENT) {
                fireEvent(ureq, event);
            }
        } else if (source == outputChoiceController) {
            if (event == Event.DONE_EVENT) {
                final List<String> selected = outputChoiceController.getSelectedEntries();
                if (selected.size() == 0) {
                    this.showError("error.selectonevalue");
                } else {
                    this.setArchiveType(selected.iterator().next());

                    final CleanupAfterDeliveryFileMediaResource fileMediaResource = archiveMembers(ureq);
                    velocityContainer4.contextPut("filename", fileMediaResource.getFileName());
                    wizardController.setWizardTitle(translate("memberlistwizard.finished.title"));
                    wizardController.setNextWizardStep(translate("memberlistwizard.finished"), velocityContainer4);
                    this.setArchiveMediaResource(fileMediaResource);
                }
            }
        } else if (source == colsChoiceController) {
            if (event == Event.DONE_EVENT) {
                final List<String> selected = colsChoiceController.getSelectedEntries();
                if (selected.size() == 0) {
                    this.showError("error.selectatleastonecolumn");
                } else {
                    this.setColumList(selected);

                    final boolean singleSelection = true;
                    final boolean layoutVertical = true;
                    final String[] keys = new String[] { "memberlistwizard.archive.type.filePerGroupOrAreaInclGroupMembership",
                            "memberlistwizard.archive.type.filePerGroupOrArea", "memberlistwizard.archive.type.allInOne" };
                    final String[] translatedKeys = new String[] { translate("memberlistwizard.archive.type.filePerGroupOrAreaInclGroupMembership"),
                            translate("memberlistwizard.archive.type.filePerGroupOrArea"), translate("memberlistwizard.archive.type.allInOne") };
                    final String[] selectedKeys = new String[] { "memberlistwizard.archive.type.allInOne" };
                    outputChoiceController = new ChoiceController(ureq, getWindowControl(), keys, translatedKeys, selectedKeys, singleSelection, layoutVertical, "next");
                    this.listenTo(outputChoiceController);
                    velocityContainer3.put("outputChoice", outputChoiceController.getInitialComponent());
                    wizardController.setNextWizardStep(translate("memberlistwizard.outputchoice"), velocityContainer3);
                }
            }
        }
    }

    /**
     * Calls the archiveMembers method on <code>BusinessGroupArchiver</code>.
     * 
     * @return the output file.
     */
    private CleanupAfterDeliveryFileMediaResource archiveMembers(final UserRequest ureq) {
        BusinessGroupService businessGroupService = CoreSpringFactory.getBean(BusinessGroupService.class);
        if (GROUPS_MEMBERS.equals(wizardType)) {
            return businessGroupService.archiveGroupMembers(context, getColumList(), getGroupList(), getArchiveType(), ureq.getLocale(),
                    userService.getUserCharset(ureq.getIdentity()));
        } else {
            return businessGroupService.archiveAreaMembers(context, getColumList(), getAreaList(), getArchiveType(), ureq.getLocale(),
                    userService.getUserCharset(ureq.getIdentity()));
        }
    }

    /**
     * Gets the list of the values in the second column of the tableDataModel of the input "choice", where the first column value is true.
     * 
     * @param choice
     * @return a list with the selected values of the input choice component.
     */
    private List getSelectedValues(final Choice choice) {
        final List selValues = new ArrayList();
        final List selRowsIndexes = choice.getSelectedRows();
        final int numRows = choice.getTableDataModel().getRowCount();
        for (int i = 0; i < numRows; i++) {
            if (selRowsIndexes.size() == 0) {
                final boolean booleanValue = ((Boolean) choice.getTableDataModel().getValueAt(i, 0)).booleanValue();
                if (booleanValue) {
                    final ObjectWrapper objWrapper = (ObjectWrapper) choice.getTableDataModel().getValueAt(i, 1);
                    selValues.add(objWrapper.getWrappedObj());
                }
            } else if (selRowsIndexes.contains(new Integer(i))) {
                final ObjectWrapper objWrapper = (ObjectWrapper) choice.getTableDataModel().getValueAt(i, 1);
                selValues.add(objWrapper.getWrappedObj());
            }
        }
        return selValues;
    }

    /**
     * Synchronizes the Choice's tableDataModel with its selection/removed status.
     * 
     * @param choice
     */
    private void syncTableModelWithSelection(final Choice choice) {
        final GenericObjectArrayTableDataModel tableDataModel = (GenericObjectArrayTableDataModel) choice.getTableDataModel();
        final List removedRowsIndexes = choice.getRemovedRows();
        if (removedRowsIndexes.size() > 0) {
            final int numRows = choice.getTableDataModel().getRowCount();
            for (int i = 0; i < numRows; i++) {
                if (removedRowsIndexes.contains(new Integer(i))) {
                    tableDataModel.setValueAt(new Boolean(false), i, 0);
                }
            }
        }
    }

    private String getArchiveType() {
        return archiveType;
    }

    private void setArchiveType(final String archiveType) {
        this.archiveType = archiveType;
    }

    private List<String> getColumList() {
        return columList;
    }

    private void setColumList(final List<String> columList) {
        this.columList = columList;
    }

    private List<BusinessGroup> getGroupList() {
        return groupList;
    }

    private void setGroupList(final List<BusinessGroup> groupList) {
        this.groupList = groupList;
    }

    private List<BGArea> getAreaList() {
        return areaList;
    }

    private void setAreaList(final List<BGArea> areaList) {
        this.areaList = areaList;
    }

    private MediaResource getArchiveMediaResource() {
        return archiveMediaResource;
    }

    private void setArchiveMediaResource(final MediaResource archiveMediaResource) {
        this.archiveMediaResource = archiveMediaResource;
    }

    @Override
    protected void doDispose() {
        // child controllers registrered with listenTo() are disposed in BasicController
    }

    /**
     * Description:<br>
     * Wraps <code>BusinessGroup</code>, <code>BGArea</code>, and Strings.
     * <p>
     * If more objects types to wrap adapt the toString method.
     * <P>
     * Initial Date: 30.07.2007 <br>
     * 
     * @author Lavinia Dumitrescu
     */
    private class ObjectWrapper {
        private Object wrappedObj;

        public ObjectWrapper(final Object wrappedObj) {
            this.wrappedObj = wrappedObj;
        }

        @Override
        public String toString() {
            if (wrappedObj instanceof BusinessGroup) {
                return ((BusinessGroup) wrappedObj).getName();
            } else if (wrappedObj instanceof BGArea) {
                return ((BGArea) wrappedObj).getName();
            } else if (wrappedObj instanceof String) {
                return translate((String) wrappedObj);
            } else {
                return wrappedObj.toString();
            }
        }

        public Object getWrappedObj() {
            return wrappedObj;
        }

        public void setWrappedObj(final Object wrappedObj) {
            this.wrappedObj = wrappedObj;
        }
    }

}
