package org.olat.lms.search.indexer;

import org.olat.data.commons.vfs.VFSItem;

public interface FolderIndexerAccess {

    public static final FolderIndexerFullAccess FULL_ACCESS = new FolderIndexerFullAccess();

    public boolean allowed(VFSItem item);
}
