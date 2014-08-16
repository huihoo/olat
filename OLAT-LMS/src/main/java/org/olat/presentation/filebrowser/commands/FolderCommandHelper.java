package org.olat.presentation.filebrowser.commands;

import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSManager;
import org.olat.lms.commons.filemetadata.FileMetadataInfoHelper;
import org.olat.presentation.filebrowser.FileSelection;
import org.olat.presentation.filebrowser.components.FolderComponent;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.WindowControl;

/**
 * Description:<br>
 * <P>
 * Initial Date: 29 sept. 2009 <br>
 * 
 * @author srosse
 */
public class FolderCommandHelper {

    /**
     * Check if the FolderComponent is ok
     * 
     * @param wControl
     * @param fc
     * @return
     */
    public static final int sanityCheck(WindowControl wControl, FolderComponent fc) {
        if (!VFSManager.exists(fc.getCurrentContainer())) {
            wControl.setError(fc.getTranslator().translate("FileDoesNotExist"));
            return FolderCommandStatus.STATUS_FAILED;
        }
        return FolderCommandStatus.STATUS_SUCCESS;
    }

    /**
     * Check if an item exists and is not locked
     * 
     * @param wControl
     * @param fc
     * @param ureq
     * @param currentItem
     * @return
     */
    public static final int sanityCheck2(WindowControl wControl, FolderComponent fc, UserRequest ureq, VFSItem currentItem) {
        if (!VFSManager.exists(currentItem)) {
            wControl.setError(fc.getTranslator().translate("FileDoesNotExist"));
            return FolderCommandStatus.STATUS_FAILED;
        }
        if (FileMetadataInfoHelper.isLocked(currentItem, ureq.getIdentity(), ureq.getUserSession().getRoles().isOLATAdmin())) {
            wControl.setError(fc.getTranslator().translate("lock.title"));
            return FolderCommandStatus.STATUS_FAILED;
        }
        return FolderCommandStatus.STATUS_SUCCESS;
    }

    /**
     * Check if a FileSelection exist
     * 
     * @param wControl
     * @param fc
     * @param selection
     * @return
     */
    public static final int sanityCheck3(WindowControl wControl, FolderComponent fc, FileSelection selection) {
        VFSContainer container = fc.getCurrentContainer();
        for (String filename : selection.getFiles()) {
            if (container.resolve(filename) == null) {
                wControl.setError(fc.getTranslator().translate("FileDoesNotExist"));
                return FolderCommandStatus.STATUS_FAILED;
            }
        }
        return FolderCommandStatus.STATUS_SUCCESS;
    }
}
