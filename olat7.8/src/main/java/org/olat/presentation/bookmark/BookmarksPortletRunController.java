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

package org.olat.presentation.bookmark;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.olat.data.basesecurity.Identity;
import org.olat.data.bookmark.Bookmark;
import org.olat.data.bookmark.BookmarkImpl;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.bookmark.BookmarkService;
import org.olat.lms.repository.RepositoryService;
import org.olat.presentation.framework.common.ControllerFactory;
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
import org.olat.presentation.framework.core.translator.I18nPackage;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.home.site.HomeSite;
import org.olat.system.commons.OutputEscapeType;
import org.olat.system.commons.resource.OLATResourceable;
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
public class BookmarksPortletRunController extends AbstractPortletRunController implements GenericEventListener {

    private static final String CMD_LAUNCH = "cmd.launch";

    private final TableController tableCtr;
    private BookmarkPortletTableDataModel bookmarkListModel;
    private final VelocityContainer bookmarksVC;
    private final Link showAllLink;
    private final OLATResourceable eventBusAllIdentitiesOres;
    private final OLATResourceable eventBusThisIdentityOres;
    private final Locale locale;

    /**
     * Constructor
     * 
     * @param ureq
     * @param component
     */
    public BookmarksPortletRunController(final WindowControl wControl, final UserRequest ureq, final Translator trans, final String portletName) {
        super(wControl, ureq, trans, portletName);
        this.locale = ureq.getLocale();
        this.sortingTermsList.add(SortingCriteria.TYPE_SORTING);
        this.sortingTermsList.add(SortingCriteria.ALPHABETICAL_SORTING);
        this.sortingTermsList.add(SortingCriteria.DATE_SORTING);

        this.bookmarksVC = this.createVelocityContainer("bookmarksPortlet");
        showAllLink = LinkFactory.createLink("bookmarksPortlet.showAll", bookmarksVC, this);

        final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
        tableConfig.setTableEmptyMessage(trans.translate("bookmarksPortlet.nobookmarks"));
        tableConfig.setDisplayTableHeader(false);
        tableConfig.setCustomCssClass("b_portlet_table");
        tableConfig.setDisplayRowCount(false);
        tableConfig.setPageingEnabled(false);
        tableConfig.setDownloadOffered(false);
        // disable the default sorting for this table
        tableConfig.setSortingEnabled(false);
        tableCtr = new TableController(tableConfig, ureq, getWindowControl(), trans);
        listenTo(tableCtr);
        // dummy header key, won't be used since setDisplayTableHeader is set to false
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("bookmarksPortlet.bgname", 0, CMD_LAUNCH, trans.getLocale(), OutputEscapeType.HTML));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("bookmarksPortlet.type", 1, null, trans.getLocale(), ColumnDescriptor.ALIGNMENT_RIGHT));

        this.sortingCriteria = getPersistentSortingConfiguration(ureq);
        reloadModel(sortingCriteria);

        this.bookmarksVC.put("table", tableCtr.getInitialComponent());
        putInitialPanel(bookmarksVC);

        // register for events targeted at this Identity
        eventBusThisIdentityOres = OresHelper.createOLATResourceableInstance(Identity.class, identity.getKey());
        // TODO: LD: use this: //ureq.getUserSession().getSingleUserEventCenter().registerFor(this, ureq.getIdentity(), eventBusOres);
        CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), eventBusThisIdentityOres);

        // register for events targeted at all Identities (e.g. delete bookmark for a course if a course is deleted)
        eventBusAllIdentitiesOres = OresHelper.createOLATResourceableType(Identity.class);
        CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), eventBusAllIdentitiesOres);
    }

    /**
     * Gets all bookmarks for this identity and converts the list into an PortletEntry list.
     * 
     * @param ureq
     * @return
     */
    private List<PortletEntry> getAllPortletEntries() {
        final BookmarkService mb = geBookmarkService();
        final List bookmarkList = mb.findBookmarksByIdentity(identity);
        return convertBookmarkToPortletEntryList(bookmarkList);
    }

    private BookmarkService geBookmarkService() {
        return (BookmarkService) CoreSpringFactory.getBean(BookmarkService.class);
    }

    /**
     * Converts list.
     * 
     * @param items
     * @return
     */
    private List<PortletEntry> convertBookmarkToPortletEntryList(final List<Bookmark> items) {
        final List<PortletEntry> convertedList = new ArrayList<PortletEntry>();
        final Iterator<Bookmark> listIterator = items.iterator();
        while (listIterator.hasNext()) {
            convertedList.add(new BookmarkPortletEntry(listIterator.next()));
        }
        return convertedList;
    }

    /**
     * Reloads the bookmarks table model according with the input SortingCriteria. It first evaluate the sortingCriteria type; if auto get bookmarks from BookmarkManager
     * and sort the item list according with the sortingCriteria. Else get the manually sorted list.
     * 
     * @param identity
     * @param sortingCriteria
     */
    @Override
    protected void reloadModel(final SortingCriteria sortingCriteria) {
        if (sortingCriteria.getSortingType() == SortingCriteria.AUTO_SORTING) {
            final BookmarkService mb = geBookmarkService();
            List bookmarkList = mb.findBookmarksByIdentity(identity);

            bookmarkList = getSortedList(bookmarkList, sortingCriteria);

            final List<PortletEntry> entries = convertBookmarkToPortletEntryList(bookmarkList);
            bookmarkListModel = new BookmarkPortletTableDataModel(entries, this.locale);
            tableCtr.setTableDataModel(bookmarkListModel);
        } else {
            reloadModel(this.getPersistentManuallySortedItems());
        }
    }

    /**
     * Sets the table model if the sorted items list is already available.
     * 
     * @param ureq
     * @param sortedItems
     */
    @Override
    protected void reloadModel(final List<PortletEntry> sortedItems) {
        bookmarkListModel = new BookmarkPortletTableDataModel(sortedItems, this.locale);
        tableCtr.setTableDataModel(bookmarkListModel);
    }

    @Override
    public void event(final Event event) {
        if (event instanceof BookmarkEvent) {
            if (((BookmarkEvent) event).getUsername().equals(identity.getName()) || ((BookmarkEvent) event).isAllUsersEvent()) {
                reloadModel(sortingCriteria);
            }
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == showAllLink) {
            // activate homes tab in top navigation and active bookmarks menu item
            // was brasato:: getWindowControl().getDTabs().activateStatic(ureq, HomeSite.class.getName(), "bookmarks");
            final DTabs dts = Windows.getWindows(ureq).getWindow(ureq).getDynamicTabs();
            dts.activateStatic(ureq, HomeSite.class.getName(), "bookmarks");
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
                    final Bookmark bookmark = bookmarkListModel.getBookmarkAt(rowid);
                    geBookmarkService().launchBookmark(bookmark, ureq, getWindowControl());
                }
            }
        }
    }

    /**
     * Retrieve the persistent manually sorted items for the current portlet.
     * 
     * @param ureq
     * @return
     */
    private List getPersistentManuallySortedItems() {
        final List<PortletEntry> entries = getAllPortletEntries();
        return this.getPersistentManuallySortedItems(entries);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, eventBusThisIdentityOres);
        CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, eventBusAllIdentitiesOres);
        super.doDispose();
    }

    /**
     * Retrieves the persistent sortingCriteria and the persistent manually sorted, if any, creates the table model for the manual sorting, and instantiates the
     * PortletToolSortingControllerImpl.
     * 
     * @param ureq
     * @param wControl
     * @return a PortletToolSortingControllerImpl istance.
     */
    protected PortletToolSortingControllerImpl createSortingTool(final UserRequest ureq, final WindowControl wControl) {
        if (portletToolsController == null) {
            final List<PortletEntry> entries = getAllPortletEntries();
            final PortletDefaultTableDataModel tableDataModel = new BookmarkManualSortingTableDataModel(entries, ureq.getLocale());

            final List sortedItems = getPersistentManuallySortedItems();

            portletToolsController = new PortletToolSortingControllerImpl(ureq, wControl, getTranslator(), sortingCriteria, tableDataModel, sortedItems);
            listenTo(portletToolsController);
            portletToolsController.setConfigManualSorting(true);
            portletToolsController.setConfigAutoSorting(true);
        }
        return portletToolsController;
    }

    /**
     * Comparator implementation used for sorting bookmarks entries according with the input sortingCriteria.
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
                final BookmarkImpl bookmark1 = (BookmarkImpl) o1;
                final BookmarkImpl bookmark2 = (BookmarkImpl) o2;
                int comparisonResult = 0;
                if (sortingCriteria.getSortingTerm() == SortingCriteria.ALPHABETICAL_SORTING) {
                    comparisonResult = collator.compare(bookmark1.getTitle(), bookmark2.getTitle());
                } else if (sortingCriteria.getSortingTerm() == SortingCriteria.DATE_SORTING) {
                    comparisonResult = bookmark1.getCreationDate().compareTo(bookmark2.getCreationDate());
                } else if (sortingCriteria.getSortingTerm() == SortingCriteria.TYPE_SORTING) {
                    comparisonResult = bookmark1.getDisplayrestype().compareTo(bookmark2.getDisplayrestype());
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
    class BookmarkPortletTableDataModel extends PortletDefaultTableDataModel {
        private final Locale locale;

        public BookmarkPortletTableDataModel(final List<PortletEntry> objects, final Locale locale) {
            super(objects, 2);
            this.locale = locale;
        }

        @Override
        public Object getValueAt(final int row, final int col) {
            final PortletEntry entry = getObject(row);
            final Bookmark bookmark = (Bookmark) entry.getValue();
            switch (col) {
            case 0:
                return getBookmarkTitle(bookmark);
            case 1:
                final String resType = bookmark.getDisplayrestype();
                return ControllerFactory.translateResourceableTypeName(resType, getTranslator().getLocale());
            default:
                return "ERROR";
            }
        }

        public Bookmark getBookmarkAt(final int row) {
            return (Bookmark) getObject(row).getValue();
        }

        /**
         * Get displayname of a bookmark entry. If bookmark entry a RepositoryEntry and is this RepositoryEntry closed then add a prefix to the title.
         */
        private String getBookmarkTitle(final Bookmark bookmark) {
            String title = bookmark.getTitle();
            final RepositoryEntry repositoryEntry = CoreSpringFactory.getBean(RepositoryService.class).lookupRepositoryEntry(bookmark.getOlatreskey());
            if (repositoryEntry != null && CoreSpringFactory.getBean(RepositoryService.class).createRepositoryEntryStatus(repositoryEntry.getStatusCode()).isClosed()) {
                final Translator translator = PackageUtil.createPackageTranslator(I18nPackage.REPOSITORY_, locale);
                title = "[" + translator.translate("title.prefix.closed") + "] ".concat(title);
            }
            return title;
        }
    }

    /**
     * Description:<br>
     * TableDataModel implementation for the bookmark manual sorting.
     * <P>
     * Initial Date: 23.11.2007 <br>
     * 
     * @author Lavinia Dumitrescu
     */
    class BookmarkManualSortingTableDataModel extends PortletDefaultTableDataModel {
        private final Locale locale;

        /**
         * @param objects
         * @param locale
         */
        public BookmarkManualSortingTableDataModel(final List<PortletEntry> objects, final Locale locale) {
            super(objects, 4);
            this.locale = locale;
        }

        /**
		 */
        @Override
        public final Object getValueAt(final int row, final int col) {
            final PortletEntry entry = getObject(row);
            final Bookmark bm = (BookmarkImpl) entry.getValue();
            switch (col) {
            case 0:
                return bm.getTitle();
            case 1:
                final String desc = bm.getDescription();
                return (desc == null ? "n/a" : desc);
            case 2:
                final String resType = bm.getDisplayrestype();
                return (resType == null ? "n/a" : ControllerFactory.translateResourceableTypeName(resType, locale));
            case 3:
                final Date date = bm.getCreationDate();
                // return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, getTranslator().getLocale()).format(date);
                // return date else the sorting doesn't work properly
                return date;
            default:
                return "error";
            }
        }
    }

    /**
     * PortletEntry impl for Bookmark values.
     * <P>
     * Initial Date: 10.12.2007 <br>
     * 
     * @author Lavinia Dumitrescu
     */
    class BookmarkPortletEntry implements PortletEntry {
        private final Bookmark value;
        private final Long key;

        public BookmarkPortletEntry(final Bookmark bookmark) {
            value = bookmark;
            key = bookmark.getKey();
        }

        @Override
        public Long getKey() {
            return key;
        }

        @Override
        public Bookmark getValue() {
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
