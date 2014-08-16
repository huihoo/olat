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
package org.olat.presentation.filebrowser.version;

import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.version.VFSRevision;
import org.olat.data.commons.vfs.version.Versionable;
import org.olat.data.commons.vfs.version.Versions;
import org.olat.data.filebrowser.metadata.MetaInfo;
import org.olat.data.filebrowser.metadata.tagged.MetaTagged;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.commons.mediaresource.VFSMediaResource;
import org.olat.lms.commons.mediaresource.VFSRevisionMediaResource;
import org.olat.presentation.filebrowser.commands.FolderCommand;
import org.olat.presentation.filebrowser.commands.FolderCommandStatus;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.table.BaseTableDataModelWithoutFilter;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.StaticColumnDescriptor;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableDataModel;
import org.olat.presentation.framework.core.components.table.TableEvent;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.table.TableMultiSelectEvent;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * This controller shows the list of revisions from a versioned file.<br>
 * Events:
 * <ul>
 * <li>FOLDERCOMMAND_FINISHED</li>
 * </ul>
 * <P>
 * Initial Date: 15 sept. 2009 <br>
 * 
 * @author srosse
 */
public class RevisionListController extends BasicController {

    private static final String CMD_DOWNLOAD = "download";
    private static final String CMD_RESTORE = "restore";
    private static final String CMD_DELETE = "delete";
    private static final String CMD_CANCEL = "cancel";

    private int status = FolderCommandStatus.STATUS_SUCCESS;

    private final Versionable versionedFile;
    private TableController revisionListTableCtr;
    private DialogBoxController confirmDeleteBoxCtr;
    private final VelocityContainer mainVC;

    public RevisionListController(UserRequest ureq, WindowControl wControl, Versionable versionedFile) {
        this(ureq, wControl, versionedFile, null, null);
    }

    public RevisionListController(UserRequest ureq, WindowControl wControl, Versionable versionedFile, String title, String description) {
        super(ureq, wControl);
        this.versionedFile = versionedFile;

        TableGuiConfiguration summaryTableConfig = new TableGuiConfiguration();
        summaryTableConfig.setDownloadOffered(true);
        summaryTableConfig.setTableEmptyMessage(getTranslator().translate("version.noRevisions"));

        revisionListTableCtr = new TableController(summaryTableConfig, ureq, getWindowControl(), getTranslator());
        revisionListTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("version.nr", 0, null, ureq.getLocale()) {
            @Override
            public int compareTo(int rowa, int rowb) {
                Object a = table.getTableDataModel().getValueAt(rowa, dataColumn);
                Object b = table.getTableDataModel().getValueAt(rowb, dataColumn);
                if (a == null || b == null) {
                    boolean bb = (b == null);
                    return (a == null) ? (bb ? 0 : -1) : (bb ? 1 : 0);
                }
                try {
                    Long la = new Long((String) a);
                    Long lb = new Long((String) b);
                    return la.compareTo(lb);
                } catch (NumberFormatException e) {
                    return super.compareTo(rowa, rowb);
                }
            }
        });
        revisionListTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("version.author", 1, null, ureq.getLocale()));
        revisionListTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("version.comment", 2, null, ureq.getLocale()));
        revisionListTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("version.date", 3, null, ureq.getLocale()));
        revisionListTableCtr.addColumnDescriptor(new StaticColumnDescriptor(CMD_DOWNLOAD, "version.download", getTranslator().translate("version.download")));
        revisionListTableCtr.addColumnDescriptor(new StaticColumnDescriptor(CMD_RESTORE, "version.restore", getTranslator().translate("version.restore")));

        revisionListTableCtr.addMultiSelectAction("delete", CMD_DELETE);
        revisionListTableCtr.addMultiSelectAction("cancel", CMD_CANCEL);
        revisionListTableCtr.setMultiSelect(true);

        List<VFSRevision> revisions = new ArrayList<VFSRevision>(versionedFile.getVersions().getRevisions());
        revisions.add(new CurrentRevision((VFSLeaf) versionedFile));

        revisionListTableCtr.setTableDataModel(new RevisionListDataModel(revisions, ureq.getLocale()));
        listenTo(revisionListTableCtr);

        mainVC = createVelocityContainer("revisions");
        mainVC.put("revisionList", revisionListTableCtr.getInitialComponent());

        if (StringHelper.containsNonWhitespace(title)) {
            mainVC.contextPut("title", title);
        }
        if (StringHelper.containsNonWhitespace(description)) {
            mainVC.contextPut("description", description);
        }

        putInitialPanel(mainVC);
    }

    @Override
    protected void doDispose() {
        // disposed by BasicController
    }

    public int getStatus() {
        return status;
    }

    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        // nothing to track
    }

    @Override
    protected void event(UserRequest ureq, Controller source, Event event) {
        if (source == revisionListTableCtr) {
            if (event instanceof TableEvent) {
                TableEvent tEvent = (TableEvent) event;
                int row = tEvent.getRowId();
                if (CMD_DOWNLOAD.equals(tEvent.getActionId())) {

                    MediaResource resource;
                    if (row < versionedFile.getVersions().getRevisions().size()) {
                        // restore current, do nothing
                        VFSRevision version = versionedFile.getVersions().getRevisions().get(row);
                        resource = new VFSRevisionMediaResource(version, true);
                    } else {
                        resource = new VFSMediaResource((VFSLeaf) versionedFile);
                        ((VFSMediaResource) resource).setDownloadable(true);
                    }
                    ureq.getDispatchResult().setResultingMediaResource(resource);
                } else if (CMD_RESTORE.equals(tEvent.getActionId())) {
                    if (row >= versionedFile.getVersions().getRevisions().size()) {
                        // restore current, do nothing
                        status = FolderCommandStatus.STATUS_SUCCESS;
                        fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
                    } else {
                        VFSRevision version = versionedFile.getVersions().getRevisions().get(row);
                        String comment = translate("version.restore.comment", new String[] { version.getRevisionNr() });
                        if (versionedFile.getVersions().restore(ureq.getIdentity(), version, comment)) {
                            status = FolderCommandStatus.STATUS_SUCCESS;
                            fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
                        } else {
                            status = FolderCommandStatus.STATUS_FAILED;
                            showError("version.restore.failed");
                            fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
                        }
                    }
                }
            } else if (event instanceof TableMultiSelectEvent) {
                TableMultiSelectEvent tEvent = (TableMultiSelectEvent) event;
                if (CMD_CANCEL.equals(tEvent.getAction())) {
                    status = FolderCommandStatus.STATUS_CANCELED;
                    fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
                } else {
                    List<VFSRevision> selectedVersions = getSelectedRevisions(tEvent.getSelection());
                    if (!selectedVersions.isEmpty()) {
                        if (CMD_DELETE.equals(tEvent.getAction())) {
                            String numOfVersionToDelete = Integer.toString(selectedVersions.size());
                            confirmDeleteBoxCtr = activateYesNoDialog(ureq, null, translate("version.confirmDelete", new String[] { numOfVersionToDelete }),
                                    confirmDeleteBoxCtr);
                            confirmDeleteBoxCtr.setUserObject(selectedVersions);
                        }
                    }
                }
            }
        } else if (source == confirmDeleteBoxCtr) {
            if (DialogBoxUIFactory.isYesEvent(event)) {
                List<VFSRevision> selectedVersions = (List<VFSRevision>) confirmDeleteBoxCtr.getUserObject();
                versionedFile.getVersions().delete(ureq.getIdentity(), selectedVersions);
                status = FolderCommandStatus.STATUS_SUCCESS;
            } else {
                status = FolderCommandStatus.STATUS_CANCELED;
            }
            fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
        }
    }

    private List<VFSRevision> getSelectedRevisions(BitSet objectMarkers) {
        List<VFSRevision> allVersions = versionedFile.getVersions().getRevisions();

        List<VFSRevision> results = new ArrayList<VFSRevision>();
        for (int i = objectMarkers.nextSetBit(0); i >= 0; i = objectMarkers.nextSetBit(i + 1)) {
            if (i >= 0 && i < allVersions.size()) {
                VFSRevision elem = allVersions.get(i);
                results.add(elem);
            }
        }

        return results;
    }

    public class RevisionListDataModel extends BaseTableDataModelWithoutFilter implements TableDataModel {
        private final DateFormat format;
        private final List<VFSRevision> versionList;
        private final Calendar cal = Calendar.getInstance();

        public RevisionListDataModel(List<VFSRevision> versionList, Locale locale) {
            this.versionList = versionList;
            format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public int getRowCount() {
            return versionList.size();
        }

        @Override
        public Object getValueAt(int row, int col) {
            VFSRevision version = versionList.get(row);
            switch (col) {
            case 0:
                return version.getRevisionNr();
            case 1:
                return version.getAuthor();
            case 2: {
                String comment = version.getComment();
                if (StringHelper.containsNonWhitespace(comment)) {
                    return comment;
                } else if ("1".equals(version.getRevisionNr())) {
                    return translate("version.initialRevision");
                }
                return "";
            }
            case 3:
                cal.setTimeInMillis(version.getLastModified());
                return format.format(cal.getTime());
            default:
                return "";
            }
        }
    }

    public class CurrentRevision implements VFSRevision {
        private final VFSLeaf versionFile;

        public CurrentRevision(VFSLeaf versionFile) {
            this.versionFile = versionFile;
        }

        @Override
        public String getAuthor() {
            if (versionFile instanceof MetaTagged) {
                MetaInfo info = ((MetaTagged) versionFile).getMetaInfo();
                return info.getAuthor();
            }
            return "-";
        }

        @Override
        public String getComment() {
            Versions versions = ((Versionable) versionFile).getVersions();
            String comment = versions.getComment();
            if (StringHelper.containsNonWhitespace(comment)) {
                return comment;
            } else if ("1".equals(versions.getRevisionNr())) {
                return translate("version.initialRevision");
            }
            return "";
        }

        @Override
        public InputStream getInputStream() {
            return versionFile.getInputStream();
        }

        @Override
        public long getLastModified() {
            return versionFile.getLastModified();
        }

        @Override
        public String getName() {
            return versionFile.getName();
        }

        @Override
        public String getRevisionNr() {
            return ((Versionable) versionFile).getVersions().getRevisionNr();
        }

        @Override
        public long getSize() {
            return versionFile.getSize();
        }
    }
}
