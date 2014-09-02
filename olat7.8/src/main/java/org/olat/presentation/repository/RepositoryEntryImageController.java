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
import java.util.HashSet;
import java.util.Set;

import org.olat.data.commons.fileutil.ImageHelper;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.vfs.LocalFolderImpl;
import org.olat.data.commons.vfs.Quota;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.filebrowser.FileUploadController;
import org.olat.presentation.filebrowser.FolderEvent;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.image.ImageComponent;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.event.Event;

/**
 * <h3>Description:</h3>
 * <p>
 * The repository entry image upload controller offers a workflow to upload an image for a learning resource
 * <p>
 * Events fired by this controller:
 * <ul>
 * <li>CANCELLED_EVENT</li>
 * <li>DONE_EVENT</li>
 * </ul>
 * 
 * @author Ingmar Kroll
 */
public class RepositoryEntryImageController extends BasicController {
    private final VelocityContainer vContainer;
    private final Link deleteButton;
    private final FileUploadController uploadCtr;
    private final RepositoryEntry repositoryEntry;

    private File repositoryEntryImageFile = null;
    private File newFile = null;
    private final int PICTUREWIDTH = 570;

    /**
     * Display upload form to upload a file to the given currentPath.
     * 
     * @param uploadDir
     * @param wControl
     * @param translator
     * @param limitKB
     */
    public RepositoryEntryImageController(final UserRequest ureq, final WindowControl wControl, final RepositoryEntry repositoryEntry, final Translator translator,
            final int limitKB) {
        super(ureq, wControl, translator);

        this.repositoryEntryImageFile = new File(new File(FolderConfig.getCanonicalRoot() + FolderConfig.getRepositoryHome()),
                RepositoryServiceImpl.getImageFilename(repositoryEntry));
        this.repositoryEntry = repositoryEntry;
        this.vContainer = createVelocityContainer("imageupload");
        // Init upload controller
        final Set<String> mimeTypes = new HashSet<String>();
        mimeTypes.add("image/gif");
        mimeTypes.add("image/jpg");
        mimeTypes.add("image/jpeg");
        mimeTypes.add("image/png");
        final File uploadDir = new File(FolderConfig.getCanonicalRoot() + FolderConfig.getRepositoryHome());
        final VFSContainer uploadContainer = new LocalFolderImpl(uploadDir);
        uploadCtr = new FileUploadController(getWindowControl(), uploadContainer, ureq, limitKB, Quota.UNLIMITED, mimeTypes, false, false, false, true);
        uploadCtr.hideTitleAndFieldset();
        listenTo(uploadCtr);
        vContainer.put("uploadCtr", uploadCtr.getInitialComponent());
        // init the delete button
        deleteButton = LinkFactory.createButtonSmall("cmd.delete", this.vContainer, this);
        // init the image itself
        vContainer.contextPut("hasPortrait", Boolean.FALSE);
        displayImage();
        // finished
        putInitialPanel(vContainer);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == deleteButton) {
            repositoryEntryImageFile.delete();
        }
        displayImage();
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == uploadCtr) {
            // catch upload event
            if (event instanceof FolderEvent && event.getCommand().equals(FolderEvent.UPLOAD_EVENT)) {
                final FolderEvent folderEvent = (FolderEvent) event;
                // Get file from temp folder location
                final String uploadFileName = folderEvent.getFilename();
                final File uploadDir = new File(FolderConfig.getCanonicalRoot() + FolderConfig.getRepositoryHome());
                newFile = new File(uploadDir, uploadFileName);
                if (!newFile.exists()) {
                    showError("Failed");
                } else {
                    // Scale uploaded image
                    final File pBigFile = new File(uploadDir, RepositoryServiceImpl.getImageFilename(repositoryEntry));
                    final boolean ok = ImageHelper.scaleImage(newFile, pBigFile, PICTUREWIDTH, PICTUREWIDTH);
                    // Cleanup original file
                    newFile.delete();
                    // And finish workflow
                    if (ok) {
                        fireEvent(ureq, Event.DONE_EVENT);
                    } else {
                        showError("NoImage");
                    }
                }
            }
            // redraw image
            displayImage();
        }
    }

    /**
     * Internal helper to create the image component and push it to the view
     */
    private void displayImage() {
        /* STATIC_METHOD_REFACTORING */
        final ImageComponent ic = RepositoryServiceImpl.getInstance().getImageComponentForRepositoryEntry("image", this.repositoryEntry);
        if (ic != null) {
            // display only within 400x200 in form
            ic.setMaxWithAndHeightToFitWithin(400, 200);
            vContainer.put("image", ic);
            vContainer.contextPut("hasImage", Boolean.TRUE);
        } else {
            vContainer.contextPut("hasImage", Boolean.FALSE);
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // controllers autodisposed by basic controller
    }

}
