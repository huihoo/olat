package org.olat.lms.commons.filemetadata;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.commons.vfs.OlatRelPathImpl;
import org.olat.data.filebrowser.metadata.MetaInfo;
import org.olat.data.filebrowser.metadata.MetaInfoFileImpl;
import org.olat.data.filebrowser.thumbnail.ThumbnailService;
import org.olat.presentation.framework.core.util.CSSHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileMetadataInfoServiceImpl implements FileMetadataInfoService {

    @Autowired
    private BaseSecurity baseSecurity;
    @Autowired
    private ThumbnailService thumbnailService;

    /**
     * [spring]
     * 
     * @param metaInfo
     */
    private FileMetadataInfoServiceImpl() {
        //
    }

    @Override
    public MetaInfo createMetaInfoFor(OlatRelPathImpl path) {
        return new MetaInfoFileImpl(thumbnailService, baseSecurity, path);

    }

    /**
	 */
    @Override
    public String getIconCssClass(MetaInfo meta) {
        String cssClass;
        if (meta.isDirectory()) {
            cssClass = CSSHelper.CSS_CLASS_FILETYPE_FOLDER;
        } else {
            cssClass = CSSHelper.createFiletypeIconCssClassFor(meta.getName());
        }
        return cssClass;
    }

}
