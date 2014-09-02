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

package org.olat.presentation.repository;

/* TODO: ORID-1007 'File' */
import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.olat.data.commons.vfs.LocalFileImpl;
import org.olat.data.commons.vfs.QuotaManager;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.filters.VFSItemFileTypeFilter;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.admin.quota.QuotaConstants;
import org.olat.lms.commons.fileresource.AddingResourceException;
import org.olat.lms.commons.fileresource.FileResource;
import org.olat.lms.commons.fileresource.FileResourceManager;
import org.olat.presentation.commons.filechooser.FileChooserController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;

/**
 * <!--**************--> <h3>Responsability:</h3> Offers to choose a file which is then added to the repository. Chooseable files can be restricted to specific file
 * types.
 * <p>
 * <!--**************-->
 * <h3>Events fired:</h3> <i>No events are fired!</i><br>
 * Signals are sent via {@link org.olat.presentation.repository.RepositoryAddCallback RepositoryAddCallback}
 * <p>
 * <!--**************-->
 * <h3>Workflow:</h3>
 * <ul>
 * <li><i>Mainflow:</i><br>
 * Choose the file from a personal folder or upload one.<br>
 * Add file resource to repository.<br>
 * Signal success via {@link org.olat.presentation.repository.RepositoryAddCallback#finished(UserRequest) finished(..)}</li>
 * <li><i>Failure:</i><br>
 * Wrong type of file or file creation failed.<br>
 * Signal failure via {@link org.olat.presentation.repository.RepositoryAddCallback#failed(UserRequest) failure(..)}.</li>
 * <li><i>Cancel:</i><br>
 * Choosing file is canceled<br>
 * Signal cancel via {@link org.olat.presentation.repository.RepositoryAddCallback#canceled(UserRequest) canceled(..)}</li>
 * </ul>
 * <p>
 * <!--**************-->
 * <h3>Hints:</h3> It is a special kind of controller as it uses the {@link org.olat.presentation.repository.RepositoryAddCallback RepositoryAddCallback} to signal state
 * changes TODO:pb:a link to the doc book explaining how the repository is used.
 * <p>
 * 
 * @author Felix Jost
 */
public class AddFileResourceController extends BasicController implements IAddController {

    private FileChooserController cfc;
    private RepositoryAddCallback addCallback;
    private List limitTypes;
    private FileResource newFileResource;

    /**
     * Controller implementing "Repository Add"-workflow for all file resources.
     * 
     * @param addCallback
     *            the channel to signal success, failure, cancel
     * @param limitTypes
     *            may be <code>null</code> indicating no limits.
     * @param ureq
     * @param wControl
     */
    public AddFileResourceController(final RepositoryAddCallback addCallback, final List limitTypes, final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        init(addCallback, limitTypes, null, ureq);
    }

    /**
     * Same as above, but able to restrict filetypes, this controller accepts.
     * 
     * @param addCallback
     *            the channel to signal success, failure, cancel
     * @param limitTypes
     *            may be <code>null</code> inidicating no limits.
     * @param suffixFilter
     *            may be <code>null</code> for having no filter.
     * @param ureq
     * @param wControl
     */
    public AddFileResourceController(final RepositoryAddCallback addCallback, final List limitTypes, final String[] suffixFilter, final UserRequest ureq,
            final WindowControl wControl) {
        super(ureq, wControl);
        init(addCallback, limitTypes, suffixFilter, ureq);
    }

    private void init(final RepositoryAddCallback rac, final List lt, final String[] suffixFilter, final UserRequest ureq) {

        this.addCallback = rac;
        this.limitTypes = lt;

        // prepare generic filechoser for add file
        removeAsListenerAndDispose(cfc);
        cfc = new FileChooserController(ureq, getWindowControl(), (int) QuotaManager.getInstance().getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_REPO).getUlLimitKB()
                .longValue(), false);
        listenTo(cfc);

        if (suffixFilter != null) {
            final VFSItemFileTypeFilter fypeFilter = new VFSItemFileTypeFilter(suffixFilter);
            cfc.setSuffixFilter(fypeFilter);
        }
        putInitialPanel(cfc.getInitialComponent());
    }

    /**
	 */
    @Override
    public Component getTransactionComponent() {
        return getInitialComponent();
    }

    /**
	 */
    @Override
    public boolean transactionFinishBeforeCreate() {
        // Nothing to do here. file resource already created.
        return true;
    }

    /**
	 */
    @Override
    public void transactionAborted() {
        // File resource already created. Cleanup file resource on abort.
        if (newFileResource != null) {
            FileResourceManager.getInstance().deleteFileResource(newFileResource);
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // nothing to process here.
    }

    /**
	 */
    @Override
    protected void event(final UserRequest urequest, final Controller source, final Event event) {
        if (source == cfc) {
            if (event.equals(Event.DONE_EVENT)) {
                // create new repository entry

                File f;
                String fName;
                if (cfc.isFileFromFolder()) {
                    final VFSLeaf vfsLeaf = cfc.getFileSelection();
                    if (!(vfsLeaf instanceof LocalFileImpl)) {
                        showError("add.failed");
                        addCallback.failed(urequest);
                        return;
                    }
                    f = ((LocalFileImpl) vfsLeaf).getBasefile();
                    fName = f.getName();
                } else {
                    f = cfc.getUploadedFile();
                    fName = cfc.getUploadedFileName();
                }
                final FileResourceManager frm = FileResourceManager.getInstance();
                if (f != null && !f.exists()) {
                    // if f does not exist at this point then something went wrong with the upload
                    // inform the user rather than issuing a log.error in FileUtils (OLAT-5383/OLAT-5384)
                    showError("Failed");
                    addCallback.failed(urequest);
                    return;
                }
                try {
                    newFileResource = frm.addFileResource(f, fName);
                } catch (final AddingResourceException e) {
                    showError(e.getErrorKey());
                    addCallback.failed(urequest);
                    return;
                }
                if (newFileResource == null) {
                    showError("add.failed");
                    addCallback.failed(urequest);
                    return;
                }

                if (limitTypes != null && limitTypes.size() > 0) { // check for type
                    boolean validType = false;
                    for (final Iterator iter = limitTypes.iterator(); iter.hasNext();) {
                        final String limitType = (String) iter.next();
                        if (newFileResource.getResourceableTypeName().equals(limitType)) {
                            validType = true;
                        }
                    }
                    if (!validType) { // rollback
                        frm.deleteFileResource(newFileResource);
                        showError("add.wrongtype");
                        addCallback.failed(urequest);
                        return;
                    }
                }

                // cleanup tmp files and directories
                // cfc.release(); happens in dispose of cfc

                addCallback.setResourceable(newFileResource);
                addCallback.setResourceName(fName);
                String displayName = fName;
                final int lastDot = displayName.lastIndexOf('.');
                if (lastDot != -1) {
                    displayName = displayName.substring(0, lastDot);
                }
                addCallback.setDisplayName(displayName);
                addCallback.finished(urequest);
                return;
            } else if (event.equals(Event.CANCELLED_EVENT)) {
                addCallback.canceled(urequest);
                return;
            }
        }
    }

    /**
	 */
    @Override
    public void repositoryEntryCreated(final RepositoryEntry re) {
        // nothing to do here.
        return;
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }
}
