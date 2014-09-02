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

package org.olat.presentation.portal.group;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.filter.FilterFactory;
import org.olat.data.group.BusinessGroup;
import org.olat.lms.group.BusinessGroupService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.Windows;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.table.ColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.Table;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableEvent;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.dtabs.DTabs;
import org.olat.presentation.framework.core.control.generic.portal.AbstractPortletRunController;
import org.olat.presentation.framework.core.control.generic.portal.PortletDefaultTableDataModel;
import org.olat.presentation.framework.core.control.generic.portal.PortletEntry;
import org.olat.presentation.framework.core.control.generic.portal.PortletToolSortingControllerImpl;
import org.olat.presentation.framework.core.control.generic.portal.SortingCriteria;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.group.BGControllerFactory;
import org.olat.presentation.group.edit.BusinessGroupModifiedEvent;
import org.olat.presentation.group.site.GroupsSite;
import org.olat.system.commons.OutputEscapeType;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.event.Event;
import org.olat.system.event.GenericEventListener;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Run view controller for the groups list portlet
 * <P>
 * Initial Date: 11.07.2005 <br>
 * 
 * @author gnaegi
 */
public class GroupsPortletRunController extends AbstractPortletRunController implements GenericEventListener {

    private static final String CMD_LAUNCH = "cmd.launch";

    private final Panel panel;
    private final TableController tableCtr;
    // private GroupListMiniModel groupListModel;
    private GroupTableDataModel groupListModel;
    private final VelocityContainer groupsVC;
    private List groupList;
    private final Identity ident;
    private final Link showAllLink;
    private BusinessGroupService businessGroupService;

    /**
     * Constructor
     * 
     * @param ureq
     * @param component
     */
    public GroupsPortletRunController(final WindowControl wControl, final UserRequest ureq, final Translator trans, final String portletName) {
        super(wControl, ureq, trans, portletName);

        businessGroupService = (BusinessGroupService) CoreSpringFactory.getBean(BusinessGroupService.class);

        sortingTermsList.add(SortingCriteria.TYPE_SORTING);
        sortingTermsList.add(SortingCriteria.ALPHABETICAL_SORTING);
        sortingTermsList.add(SortingCriteria.DATE_SORTING);

        this.ident = ureq.getIdentity();

        this.groupsVC = this.createVelocityContainer("groupsPortlet");
        showAllLink = LinkFactory.createLink("groupsPortlet.showAll", groupsVC, this);

        final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
        tableConfig.setTableEmptyMessage(trans.translate("groupsPortlet.nogroups"));
        tableConfig.setDisplayTableHeader(false);
        tableConfig.setCustomCssClass("b_portlet_table");
        tableConfig.setDisplayRowCount(false);
        tableConfig.setPageingEnabled(false);
        tableConfig.setDownloadOffered(false);
        // disable the default sorting for this table
        tableConfig.setSortingEnabled(false);
        tableCtr = new TableController(tableConfig, ureq, getWindowControl(), trans);
        listenTo(tableCtr);

        // dummy header key, won't be used since setDisplayTableHeader is set to
        // false
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("groupsPortlet.bgname", 0, CMD_LAUNCH, trans.getLocale(), OutputEscapeType.HTML));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("groupsPortlet.type", 1, null, trans.getLocale(), ColumnDescriptor.ALIGNMENT_RIGHT));

        this.sortingCriteria = getPersistentSortingConfiguration(ureq);
        reloadModel(this.sortingCriteria);

        this.groupsVC.put("table", tableCtr.getInitialComponent());
        panel = this.putInitialPanel(groupsVC);

        // register for businessgroup type events
        CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), OresHelper.lookupType(BusinessGroup.class));
    }

    /**
     * Gets all groups for this portlet and wraps them into PortletEntry impl.
     * 
     * @param ureq
     * @return the PortletEntry list.
     */
    private List<PortletEntry> getAllPortletEntries() {
        groupList = businessGroupService.findBusinessGroupsAttendedBy(null, identity, null);
        groupList.addAll(businessGroupService.findBusinessGroupsOwnedBy(null, identity, null));
        final List<PortletEntry> entries = convertBusinessGroupToPortletEntryList(groupList);
        return entries;
    }

    private List<PortletEntry> convertBusinessGroupToPortletEntryList(final List<BusinessGroup> items) {
        final List<PortletEntry> convertedList = new ArrayList<PortletEntry>();
        final Iterator<BusinessGroup> listIterator = items.iterator();
        while (listIterator.hasNext()) {
            convertedList.add(new GroupPortletEntry(listIterator.next()));
        }
        return convertedList;
    }

    @Override
    protected void reloadModel(final SortingCriteria sortingCriteria) {
        if (sortingCriteria.getSortingType() == SortingCriteria.AUTO_SORTING) {
            groupList = businessGroupService.findBusinessGroupsAttendedBy(null, identity, null);
            groupList.addAll(businessGroupService.findBusinessGroupsOwnedBy(null, identity, null));

            groupList = getSortedList(groupList, sortingCriteria);

            final List<PortletEntry> entries = convertBusinessGroupToPortletEntryList(groupList);

            groupListModel = new GroupTableDataModel(entries);
            tableCtr.setTableDataModel(groupListModel);
        } else {
            reloadModel(this.getPersistentManuallySortedItems());
        }
    }

    @Override
    protected void reloadModel(final List<PortletEntry> sortedItems) {
        groupListModel = new GroupTableDataModel(sortedItems);
        tableCtr.setTableDataModel(groupListModel);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == showAllLink) {
            // activate group tab in top navigation
            final DTabs dts = Windows.getWindows(ureq).getWindow(ureq).getDynamicTabs();
            // was brasato:: getWindowControl().getDTabs().activateStatic(ureq, GroupsSite.class.getName(), null);
            dts.activateStatic(ureq, GroupsSite.class.getName(), null);
        }
    }

    /**
     * org.olat.system.event.control.Event)
     */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        super.event(ureq, source, event);
        if (source == tableCtr) {
            if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
                final TableEvent te = (TableEvent) event;
                final String actionid = te.getActionId();
                if (actionid.equals(CMD_LAUNCH)) {
                    final int rowid = te.getRowId();
                    final BusinessGroup currBusinessGroup = groupListModel.getBusinessGroupAt(rowid);
                    final boolean isInBusinessGroup = businessGroupService.isIdentityInBusinessGroup(ureq.getIdentity(), currBusinessGroup);
                    if (isInBusinessGroup) {
                        BGControllerFactory.getInstance().createRunControllerAsTopNavTab(currBusinessGroup, ureq, getWindowControl(), false, null);
                    } else {
                        showInfo("groupsPortlet.no_member");
                    }
                }
            }
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        super.doDispose();
        // de-register for businessgroup type events
        CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, OresHelper.lookupType(BusinessGroup.class));
        // POST: all firing event for the source just deregistered are finished
        // (listeners lock in EventAgency)
    }

    @Override
    public void event(final Event event) {
        if (event instanceof BusinessGroupModifiedEvent) {
            final BusinessGroupModifiedEvent mev = (BusinessGroupModifiedEvent) event;
            // TODO:fj:b this operation should not be too expensive since many other
            // users have to be served also
            // store the event and apply it only when the component validate event is
            // fired.
            // FIXME:fj:a check all such event that they do not say, execute more than
            // 1-2 db queries : 100 listening users -> 100-200 db queries!
            // TODO:fj:b concept of defering that event if this controller here is in
            // the dispatchEvent - code (e.g. DefaultController implements
            // GenericEventListener)
            // -> to avoid rare race conditions like e.g. dispose->deregister and null
            // controllers, but queue is still firing events
            final boolean modified = mev.updateBusinessGroupList(groupList, ident);
            if (modified) {
                tableCtr.modelChanged();
            }
        }
    }

    /**
     * Retrieves the persistent sortingCriteria and the persistent manually sorted, if any, creates the table model for the manual sorting, and instantiates the
     * PortletToolSortingControllerImpl.
     * 
     * @param ureq
     * @param wControl
     * @return a PortletToolSortingControllerImpl instance.
     */
    protected PortletToolSortingControllerImpl createSortingTool(final UserRequest ureq, final WindowControl wControl) {
        if (portletToolsController == null) {

            final List<PortletEntry> portletEntryList = getAllPortletEntries();
            final PortletDefaultTableDataModel tableDataModel = new GroupsManualSortingTableDataModel(portletEntryList);
            final List sortedItems = getPersistentManuallySortedItems();

            portletToolsController = new PortletToolSortingControllerImpl(ureq, wControl, getTranslator(), sortingCriteria, tableDataModel, sortedItems);
            portletToolsController.setConfigManualSorting(true);
            portletToolsController.setConfigAutoSorting(true);
            portletToolsController.addControllerListener(this);
        }
        return portletToolsController;
    }

    /**
     * Retrieves the persistent manually sorted items for the current portlet.
     * 
     * @param ureq
     * @return
     */
    private List<PortletEntry> getPersistentManuallySortedItems() {
        final List<PortletEntry> portletEntryList = getAllPortletEntries();
        return this.getPersistentManuallySortedItems(portletEntryList);
    }

    /**
     * Comparator implementation used for sorting BusinessGroup entries according with the input sortingCriteria.
     * <p>
     * 
     * @param sortingCriteria
     * @return a Comparator for the input sortingCriteria
     */
    @Override
    protected Comparator getComparator(final SortingCriteria sortingCriteria) {
        return new Comparator() {
            @Override
            public int compare(final Object o1, final Object o2) {
                final BusinessGroup group1 = (BusinessGroup) o1;
                final BusinessGroup group2 = (BusinessGroup) o2;
                int comparisonResult = 0;
                if (sortingCriteria.getSortingTerm() == SortingCriteria.ALPHABETICAL_SORTING) {
                    comparisonResult = collator.compare(group1.getName(), group2.getName());
                } else if (sortingCriteria.getSortingTerm() == SortingCriteria.DATE_SORTING) {
                    comparisonResult = group1.getCreationDate().compareTo(group2.getCreationDate());
                } else if (sortingCriteria.getSortingTerm() == SortingCriteria.TYPE_SORTING) {
                    comparisonResult = group1.getType().compareTo(group2.getType());
                }
                if (!sortingCriteria.isAscending()) {
                    // if not isAscending return (-comparisonResult)
                    return -comparisonResult;
                }
                return comparisonResult;
            }
        };
    }

    /**
     * PortletDefaultTableDataModel implementation for the current portlet.
     * <P>
     * Initial Date: 10.12.2007 <br>
     * 
     * @author Lavinia Dumitrescu
     */
    private class GroupTableDataModel extends PortletDefaultTableDataModel {
        public GroupTableDataModel(final List<PortletEntry> objects) {
            super(objects, 2);
        }

        @Override
        public Object getValueAt(final int row, final int col) {
            final PortletEntry entry = getObject(row);
            final BusinessGroup businessGroup = (BusinessGroup) entry.getValue();
            switch (col) {
            case 0:
                return businessGroup.getName();
            case 1:
                return getTranslator().translate(businessGroup.getType());
            default:
                return "ERROR";
            }
        }

        public BusinessGroup getBusinessGroupAt(final int row) {
            return (BusinessGroup) getObject(row).getValue();
        }
    }

    /**
     * PortletDefaultTableDataModel implementation for the manual sorting component.
     * <P>
     * Initial Date: 10.12.2007 <br>
     * 
     * @author Lavinia Dumitrescu
     */
    private class GroupsManualSortingTableDataModel extends PortletDefaultTableDataModel {
        /**
         * @param objects
         * @param locale
         */
        public GroupsManualSortingTableDataModel(final List<PortletEntry> objects) {
            super(objects, 4);
        }

        /**
		 */
        @Override
        public final Object getValueAt(final int row, final int col) {
            final PortletEntry portletEntry = getObject(row);
            final BusinessGroup group = (BusinessGroup) portletEntry.getValue();
            switch (col) {
            case 0:
                return group.getName();
            case 1:
                String description = group.getDescription();
                // group description is escaped because is edited via TinyMce editor, so must unescape it because the escaping is done later in the table renderer
                description = FilterFactory.getHtmlTagsFilter().filter(StringHelper.unescapeHtml(description));
                return (description == null ? "n/a" : description);
            case 2:
                final String resType = group.getType();
                return (resType == null ? "n/a" : translate(resType));
            case 3:
                final Date date = group.getCreationDate();
                // return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, getTranslator().getLocale()).format(date);
                return date;
            default:
                return "error";
            }
        }
    }

    private class GroupPortletEntry implements PortletEntry {
        private final BusinessGroup value;
        private final Long key;

        public GroupPortletEntry(final BusinessGroup group) {
            value = group;
            key = group.getKey();
        }

        @Override
        public Long getKey() {
            return key;
        }

        @Override
        public BusinessGroup getValue() {
            return value;
        }
    }

    /**
	 */
    @Override
    public boolean isControllerAndNotDisposed() {
        return !isDisposed();
    }

}
