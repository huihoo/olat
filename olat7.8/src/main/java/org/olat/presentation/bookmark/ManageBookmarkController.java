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

import java.util.List;
import java.util.Locale;

import org.olat.data.basesecurity.Identity;
import org.olat.data.bookmark.Bookmark;
import org.olat.data.bookmark.BookmarkImpl;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.bookmark.BookmarkService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.framework.common.ControllerFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;
import org.olat.presentation.framework.core.components.table.StaticColumnDescriptor;
import org.olat.presentation.framework.core.components.table.Table;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableEvent;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.presentation.framework.core.translator.I18nPackage;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.OutputEscapeType;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:
 * 
 * @author Sabina Jeger
 */
public class ManageBookmarkController extends BasicController {

    private BmTableDataModel tdm;
    private BookmarkImpl chosenBm = null;

    private final VelocityContainer myContent;

    private final Panel bmarea;
    private final String searchType; // can be set to a bookmark.olatrestype to
    // restrict the bookmark searches
    private final TableController tableCtr;
    private AddAndEditBookmarkController abc;
    private DialogBoxController dc;
    private CloseableModalController cmc;

    /** constructor constant to search for all repository entry types * */
    public static final String SEARCH_TYPE_ALL = "all";

    /**
     * Constructor for bookmark list and manage controller. The controller can be configured using the allowEdit flag in the constructor and restrict the search to
     * specific repository entry types using the type attribute.
     * 
     * @param ureq
     *            The user request
     * @param wControl
     *            The window controller
     * @param allowEdit
     *            true: table allows edit and delete of bookmarks, false: only launch possible
     * @param type
     *            Type of repository entries to be displayed or SEARCH_TYPE_ALL to display all bookmarks
     */
    public ManageBookmarkController(final UserRequest ureq, final WindowControl wControl, final boolean allowEdit, final String type) {
        super(ureq, wControl);

        myContent = createVelocityContainer("bookmarks");
        bmarea = new Panel("bmarea");
        myContent.put("bmarea", bmarea);

        final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
        tableConfig.setDownloadOffered(false);
        tableConfig.setTableEmptyMessage(translate("bookmarks.nobookmarks"));
        tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.bm.title", 0, "choose", ureq.getLocale(), OutputEscapeType.HTML));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.bm.resource", 1, null, ureq.getLocale()));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.bm.description", 2, null, ureq.getLocale(), OutputEscapeType.HTML));
        listenTo(tableCtr);

        if (allowEdit) {
            tableCtr.addColumnDescriptor(new StaticColumnDescriptor("edit", "table.header.edit", myContent.getTranslator().translate("action.edit")));
            tableCtr.addColumnDescriptor(new StaticColumnDescriptor("delete", "table.header.delete", myContent.getTranslator().translate("action.delete")));
        }
        // Set default search type to search for all bookmarks
        searchType = type;

        populateBmTable(ureq.getIdentity(), ureq.getLocale());
        bmarea.setContent(tableCtr.getInitialComponent());

        putInitialPanel(myContent);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // no events
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        // if row has been cklicked
        if (source == tableCtr) {
            if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
                final TableEvent te = (TableEvent) event;
                final String actionid = te.getActionId();
                final int rowid = te.getRowId();
                this.chosenBm = (BookmarkImpl) tdm.getObject(rowid);
                if (actionid.equals("choose")) {
                    // launch bookmark
                    geBookmarkService().launchBookmark(chosenBm, ureq, getWindowControl());
                    return;
                } else if (actionid.equals("edit")) {
                    if (abc != null) {
                        abc.dispose();
                    }
                    abc = new AddAndEditBookmarkController(ureq, getWindowControl(), chosenBm);
                    listenTo(abc);
                    cmc = new CloseableModalController(getWindowControl(), "close", abc.getInitialComponent());
                    cmc.insertHeaderCss();
                    cmc.activate();
                } else if (actionid.equals("delete")) {
                    dc = activateYesNoDialog(ureq, null, translate("bookmark.delete.willyou"), dc);
                    return;
                }
            }
        } else if (source == abc) {
            cmc.deactivate();
            chosenBm = null;
            if (event.getCommand().equals("done")) {
                // edit was done
                populateBmTable(ureq.getIdentity(), ureq.getLocale());
            }
        } else if (source == dc) {
            if (DialogBoxUIFactory.isYesEvent(event)) {
                geBookmarkService().deleteBookmark(chosenBm);
                showInfo("bookmark.delete.successfull");
                populateBmTable(ureq.getIdentity(), ureq.getLocale());
            }
            chosenBm = null;
        }
    }

    private BookmarkService geBookmarkService() {
        return (BookmarkService) CoreSpringFactory.getBean(BookmarkService.class);
    }

    private void populateBmTable(final Identity ident, final Locale locale) {
        List l;
        if (searchType.equals(SEARCH_TYPE_ALL)) {
            l = geBookmarkService().findBookmarksByIdentity(ident);
        } else {
            // in all other cases the sql query has a where clause that uses this type
            l = geBookmarkService().findBookmarksByIdentity(ident, searchType);
        }
        tdm = new BmTableDataModel(l, locale);
        tableCtr.setTableDataModel(tdm);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // disposed by BasicController
    }

}

/**
 * <pre>
 * 
 *   Initial Date:  Jul 29, 2003
 *  
 *   @author jeger
 *   
 *   Comment:  
 *   The bookmark table data model.
 * 
 * </pre>
 */

class BmTableDataModel extends DefaultTableDataModel {

    private final Locale locale;

    /**
     * @param objects
     * @param locale
     */
    public BmTableDataModel(final List objects, final Locale locale) {
        super(objects);
        this.locale = locale;
    }

    /**
     * The output escaping is delegated to the renderer not to the data model.
     */
    @Override
    public final Object getValueAt(final int row, final int col) {
        final Bookmark bm = (BookmarkImpl) getObject(row);
        switch (col) {
        case 0:
            return getBookmarkTitle(bm);
        case 1:
            final String resType = bm.getDisplayrestype();
            return (resType == null ? "n/a" : ControllerFactory.translateResourceableTypeName(resType, locale));
        case 2:
            final String desc = bm.getDescription();
            return (desc == null ? "n/a" : desc);
        default:
            return "error";
        }
    }

    /**
	 */
    @Override
    public int getColumnCount() {
        return 3;
    }

    /**
     * Get displayname of a bookmark entry. If bookmark entry a RepositoryEntry and is this RepositoryEntry closed then add a prefix to the title.
     */
    private String getBookmarkTitle(final Bookmark bookmark) {
        String title = bookmark.getTitle();
        final RepositoryEntry repositoryEntry = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(bookmark.getOlatreskey());
        if (repositoryEntry != null && RepositoryServiceImpl.getInstance().createRepositoryEntryStatus(repositoryEntry.getStatusCode()).isClosed()) {
            final Translator pT = PackageUtil.createPackageTranslator(I18nPackage.REPOSITORY_, locale);
            title = "[" + pT.translate("title.prefix.closed") + "] ".concat(title);
        }
        return title;
    }
}
