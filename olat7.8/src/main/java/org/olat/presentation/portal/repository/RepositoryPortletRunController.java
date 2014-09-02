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
 * Copyright (c) frentix GmbH, Switzerland,<br>
 * http://www.frentix.com
 * <p>
 */
package org.olat.presentation.portal.repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.olat.data.group.BusinessGroup;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.Windows;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
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
import org.olat.presentation.repository.DynamicTabHelper;
import org.olat.presentation.repository.RepositoryEntryTypeColumnDescriptor;
import org.olat.presentation.repository.RepositoyUIFactory;
import org.olat.presentation.repository.site.RepositorySite;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.event.Event;
import org.olat.system.event.GenericEventListener;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Runtime view that shows a list of courses, either as student or teacher
 * <P>
 * Initial Date: 06.03.2009 <br>
 * 
 * @author gnaegi
 */
public class RepositoryPortletRunController extends AbstractPortletRunController implements GenericEventListener {

    private static final String CMD_LAUNCH = "cmd.launch";

    private final TableController tableCtr;
    private RepositoryPortletTableDataModel repoEntryListModel;
    private final VelocityContainer repoEntriesVC;
    private final boolean studentView;

    private final Link showAllLink;

    /**
     * Constructor
     * 
     * @param wControl
     * @param ureq
     * @param trans
     * @param portletName
     * @param studentView
     *            true: show courses where I'm student; false: show courses where I'm teacher
     */
    public RepositoryPortletRunController(final WindowControl wControl, final UserRequest ureq, final Translator trans, final String portletName,
            final boolean studentView) {
        super(wControl, ureq, trans, portletName);
        this.studentView = studentView;

        sortingTermsList.add(SortingCriteria.ALPHABETICAL_SORTING);
        repoEntriesVC = this.createVelocityContainer("repositoryPortlet");
        showAllLink = LinkFactory.createLink("repositoryPortlet.showAll", repoEntriesVC, this);

        final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
        tableConfig.setTableEmptyMessage(trans.translate("repositoryPortlet.noentry"));
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
        tableCtr.addColumnDescriptor(new RepositoryEntryTypeColumnDescriptor("repositoryPortlet.img", 2, CMD_LAUNCH, trans.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("repositoryPortlet.name", 0, CMD_LAUNCH, trans.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT));

        this.sortingCriteria = getPersistentSortingConfiguration(ureq);
        reloadModel(this.sortingCriteria);
        repoEntriesVC.put("table", tableCtr.getInitialComponent());

        putInitialPanel(repoEntriesVC);

        // register for businessgroup type events
        // FIXME:RH:repo listen to changes
        CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), OresHelper.lookupType(BusinessGroup.class));
    }

    private List<PortletEntry> getAllPortletEntries() {
        if (studentView) {
            final List<RepositoryEntry> allRepoEntries = getRepositoryService().getLearningResourcesAsStudent(identity);
            return convertRepositoryEntriesToPortletEntryList(allRepoEntries);
        } else {
            final List<RepositoryEntry> allRepoEntries = getRepositoryService().getLearningResourcesAsTeacher(identity);
            return convertRepositoryEntriesToPortletEntryList(allRepoEntries);
        }
    }

    private List<PortletEntry> convertRepositoryEntriesToPortletEntryList(final List<RepositoryEntry> items) {
        final List<PortletEntry> convertedList = new ArrayList<PortletEntry>();
        for (final RepositoryEntry item : items) {
            final boolean closed = getRepositoryService().createRepositoryEntryStatus(item.getStatusCode()).isClosed();
            if (!closed) {
                convertedList.add(new RepositoryPortletEntry(item));
            }
        }
        return convertedList;
    }

    @Override
    protected void reloadModel(final SortingCriteria sortingCriteria) {
        if (sortingCriteria.getSortingType() == SortingCriteria.AUTO_SORTING) {
            List<PortletEntry> entries = getAllPortletEntries();
            entries = getSortedList(entries, sortingCriteria);

            repoEntryListModel = new RepositoryPortletTableDataModel(entries, getLocale());
            tableCtr.setTableDataModel(repoEntryListModel);
        } else {
            reloadModel(this.getPersistentManuallySortedItems());
        }
    }

    @Override
    protected void reloadModel(final List<PortletEntry> sortedItems) {
        repoEntryListModel = new RepositoryPortletTableDataModel(sortedItems, getLocale());
        tableCtr.setTableDataModel(repoEntryListModel);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == showAllLink) {
            // activate learning resource tab in top navigation and active my courses menu item
            final DTabs dts = Windows.getWindows(ureq).getWindow(ureq).getDynamicTabs();
            // attach controller / action extension dynamically to lr-site
            if (studentView) {
                dts.activateStatic(ureq, RepositorySite.class.getName(), "search.mycourses.student");
            } else {
                dts.activateStatic(ureq, RepositorySite.class.getName(), "search.mycourses.teacher");
            }
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
                    final int rowId = te.getRowId();
                    RepositoryEntry repoEntry = repoEntryListModel.getRepositoryEntry(rowId);
                    // refresh repo entry, attach to hibernate session
                    // TODO: could not be tested because not activated
                    repoEntry = getRepositoryService().loadRepositoryEntry(repoEntry);
                    // get run controller fro this repo entry and launch it in new tab
                    final DTabs dts = Windows.getWindows(ureq).getWindow(ureq).getDynamicTabs();
                    final Controller runCtr = RepositoyUIFactory.createLaunchController(repoEntry, null, ureq, dts.getWindowControl());
                    DynamicTabHelper.openRepoEntryTab(repoEntry, ureq, runCtr, repoEntry.getDisplayname(), null);
                }
            }
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        super.doDispose();
        // FIXME:RH:repo listen to changes
        // de-register for businessgroup type events
        // CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, OresHelper.lookupType(BusinessGroup.class));
        // POST: all firing event for the source just deregistered are finished
        // (listeners lock in EventAgency)
    }

    @Override
    public void event(final Event event) {
        // FIXME:RH:repo listen to changes
        // if (event instanceof BusinessGroupModifiedEvent) {
        // BusinessGroupModifiedEvent mev = (BusinessGroupModifiedEvent) event;
        // // TODO:fj:b this operation should not be too expensive since many other
        // // users have to be served also
        // // store the event and apply it only when the component validate event is
        // // fired.
        // // FIXME:fj:a check all such event that they do not say, execute more than
        // // 1-2 db queries : 100 listening users -> 100-200 db queries!
        // // TODO:fj:b concept of defering that event if this controller here is in
        // // the dispatchEvent - code (e.g. DefaultController implements
        // // GenericEventListener)
        // // -> to avoid rare race conditions like e.g. dispose->deregister and null
        // // controllers, but queue is still firing events
        // boolean modified = mev.updateBusinessGroupList(groupList, ident);
        // if (modified) tableCtr.modelChanged();
        // }
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
            final PortletDefaultTableDataModel tableDataModel = new RepositoryPortletTableDataModel(portletEntryList, ureq.getLocale());
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
                final RepositoryEntry repoEntry1 = ((RepositoryPortletEntry) o1).getValue();
                final RepositoryEntry repoEntry2 = ((RepositoryPortletEntry) o2).getValue();
                int comparisonResult = 0;
                if (sortingCriteria.getSortingTerm() == SortingCriteria.ALPHABETICAL_SORTING) {
                    comparisonResult = collator.compare(repoEntry1.getDisplayname(), repoEntry2.getDisplayname());
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
	 */
    @Override
    public boolean isControllerAndNotDisposed() {
        return !isDisposed();
    }

    private RepositoryService getRepositoryService() {
        return CoreSpringFactory.getBean(RepositoryServiceImpl.class);
    }

}
