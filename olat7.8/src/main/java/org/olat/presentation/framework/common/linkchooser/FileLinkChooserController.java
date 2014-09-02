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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.presentation.framework.common.linkchooser;

import java.util.HashSet;
import java.util.Set;

import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.vfs.Quota;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.filters.VFSItemExcludePrefixFilter;
import org.olat.data.commons.vfs.filters.VFSItemFileTypeFilter;
import org.olat.data.commons.vfs.filters.VFSItemFilter;
import org.olat.presentation.filebrowser.FileUploadController;
import org.olat.presentation.filebrowser.FolderEvent;
import org.olat.presentation.filebrowser.commands.FolderCommandStatus;
import org.olat.presentation.framework.common.filechooser.FileChoosenEvent;
import org.olat.presentation.framework.common.filechooser.FileChooserUIFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.commons.WebappHelper;
import org.olat.system.event.Event;

/**
 * enclosing_type Description: <br>
 * this controller is used to generate a component containing the provided menutree, the tool, and the content. its main use is to standardize the look and feel of
 * workflows that contain both a menu and a tool
 * <p>
 * Events fired by this controller:
 * <ul>
 * <li>Event.CANCELLED_EVENT
 * <li>URLChoosenEvent(URL) containing the selected file URL
 * </ul>
 * 
 * @author Felix Jost
 */
public class FileLinkChooserController extends BasicController {

    private VelocityContainer mainVC;

    private FileUploadController uploadCtr;
    private org.olat.presentation.framework.common.filechooser.FileChooserController fileChooserController;

    private final String fileName;
    private String[] suffixes;
    private VFSContainer rootDir;

    /**
     * @param ureq
     * @param wControl
     * @param rootDir
     *            The VFS root directory from which the linkable files should be read
     * @param uploadRelPath
     *            The relative path within the rootDir where uploaded files should be put into. If NULL, the root Dir is used
     * @param suffixes
     *            Array of allowed file types
     * @param fileName
     *            the path of the file currently edited (in order to compute the correct relative paths for links), e.g. bla/blu.html or index.html
     */
    public FileLinkChooserController(UserRequest ureq, WindowControl wControl, VFSContainer rootDir, String uploadRelPath, String[] suffixes, String fileName) {
        super(ureq, wControl);
        this.fileName = fileName;
        this.suffixes = suffixes;
        this.rootDir = rootDir;
        this.mainVC = createVelocityContainer("filechooser");

        // file uploads are relative to the currently edited file
        String[] dirs = this.fileName.split("/");
        VFSContainer fileUploadBase = rootDir;
        for (String subPath : dirs) {
            // try to resolve the given file path in the root container
            VFSItem subFolder = fileUploadBase.resolve(subPath);
            if (subFolder != null) {
                if (subFolder instanceof VFSContainer) {
                    // a higher level found, use this one unless a better one is found
                    fileUploadBase = (VFSContainer) subFolder;
                } else {
                    // it is not a container - leaf reached
                    break;
                }
            } else {
                // resolving was not possible??? stop here
                break;
            }
        }
        // create directory filter combined with suffix filter
        String[] dirFilters = { "_courseelementdata" };
        VFSItemFilter customFilter = null;
        VFSItemFilter dirFilter = new VFSItemExcludePrefixFilter(dirFilters);
        if (suffixes != null) {
            VFSItemFileTypeFilter typeFilter = new VFSItemFileTypeFilter(suffixes);
            typeFilter.setCompositeFilter(dirFilter);
            customFilter = typeFilter;
        } else {
            customFilter = dirFilter;
        }
        // hide file chooser title, we have our own title
        fileChooserController = FileChooserUIFactory.createFileChooserControllerWithoutTitle(ureq, getWindowControl(), rootDir, customFilter, true);
        listenTo(fileChooserController);
        mainVC.put("stTree", fileChooserController.getInitialComponent());

        // convert file endings to mime types as needed by file upload controller
        Set<String> mimeTypes = null;
        if (suffixes != null) {
            mimeTypes = new HashSet<String>();
            for (String suffix : suffixes) {
                String mimeType = WebappHelper.getMimeType("dummy." + suffix);
                if (mimeType != null) {
                    if (!mimeTypes.contains(mimeType))
                        mimeTypes.add(mimeType);
                }
            }
        }
        uploadCtr = new FileUploadController(wControl, fileUploadBase, ureq, (int) FolderConfig.getLimitULKB(), Quota.UNLIMITED, mimeTypes, true);
        listenTo(uploadCtr);
        // set specific upload path
        uploadCtr.setUploadRelPath(uploadRelPath);

        mainVC.put("uploader", uploadCtr.getInitialComponent());

        putInitialPanel(mainVC);
    }

    /**
	 */
    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        // no events to catch
    }

    /**
	 */
    @Override
    protected void event(UserRequest ureq, Controller source, Event event) {
        if (source == uploadCtr) {
            if (event instanceof FolderEvent) {
                FolderEvent folderEvent = (FolderEvent) event;
                if (isFileSuffixOk(folderEvent.getFilename())) {
                    fireEvent(ureq, new URLChoosenEvent(folderEvent.getFilename()));
                    return;
                } else {
                    setErrorMessage(folderEvent.getFilename());
                }
            }
            if (event == Event.DONE_EVENT) {
                if (uploadCtr.getStatus() == FolderCommandStatus.STATUS_CANCELED) {
                    fireEvent(ureq, Event.CANCELLED_EVENT);
                }
            } else if (event == Event.CANCELLED_EVENT) {
                fireEvent(ureq, Event.CANCELLED_EVENT);
            } else if (event == Event.FAILED_EVENT) {
                fireEvent(ureq, Event.CANCELLED_EVENT);
            }
        } else if (source == fileChooserController) {
            if (event instanceof FileChoosenEvent) {
                String relPath = FileChooserUIFactory.getSelectedRelativeItemPath((FileChoosenEvent) event, rootDir, fileName);
                // notify parent controller
                fireEvent(ureq, new URLChoosenEvent(relPath));

            } else if (event == Event.CANCELLED_EVENT) {
                fireEvent(ureq, Event.CANCELLED_EVENT);

            } else if (event == Event.FAILED_EVENT) {
                fireEvent(ureq, Event.CANCELLED_EVENT);
            }

        }

    }

    private boolean isFileSuffixOk(String fileName) {
        if (suffixes == null) {
            // no defined suffixes => all allowed
            return true;
        } else {
            // check if siffix one of allowed suffixes
            String suffix = getSuffix(fileName);
            for (String allowedSuffix : suffixes) {
                if (allowedSuffix.equals(suffix)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void setErrorMessage(String fileName) {
        StringBuilder allowedSuffixes = new StringBuilder();
        for (String allowedSuffix : suffixes) {
            allowedSuffixes.append(" .");
            allowedSuffixes.append(allowedSuffix);
        }
        String suffix = getSuffix(fileName);
        getWindowControl().setError(getTranslator().translate("upload.error.incorrect.filetype", new String[] { "." + suffix, allowedSuffixes.toString() }));
    }

    private String getSuffix(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // controllers autodisposed by basic controller
    }
}
