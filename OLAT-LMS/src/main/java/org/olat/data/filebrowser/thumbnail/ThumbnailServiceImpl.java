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

package org.olat.data.filebrowser.thumbnail;

import java.util.ArrayList;
import java.util.List;

import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.system.commons.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Description:<br>
 * The implementation delegate all the job to the different SPIs
 * <P>
 * Initial Date: 30 mar. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
@Service
public class ThumbnailServiceImpl implements ThumbnailService {

    @Autowired
    private final List<ThumbnailSPI> thumbnailSPIes = new ArrayList<ThumbnailSPI>();

    @Value("${thumbnail.generation.service.enabled}")
    private boolean enabled;

    /**
     * spring
     */
    private ThumbnailServiceImpl() {
        //
    }

    public List<ThumbnailSPI> getThumbnailSPIes() {
        return thumbnailSPIes;
    }

    public void addThumbnailSPI(ThumbnailSPI thumbnailSPI) {
        this.thumbnailSPIes.add(thumbnailSPI);
    }

    @Override
    public boolean isThumbnailPossible(VFSLeaf file) {
        String extension = FileUtils.getFileSuffix(file.getName());
        if (StringHelper.containsNonWhitespace(extension)) {
            for (ThumbnailSPI thumbnailSPI : thumbnailSPIes) {
                if (thumbnailSPI.isEnabled() && thumbnailSPI.getExtensions().contains(extension)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public FinalSize generateThumbnail(VFSLeaf file, VFSLeaf thumbnailFile, int maxWidth, int maxHeight) throws CannotGenerateThumbnailException {
        String extension = FileUtils.getFileSuffix(file.getName()).toLowerCase();
        for (ThumbnailSPI thumbnailSPI : thumbnailSPIes) {
            if (thumbnailSPI.isEnabled() && thumbnailSPI.getExtensions().contains(extension)) {
                FinalSize finalSize = thumbnailSPI.generateThumbnail(file, thumbnailFile, maxWidth, maxHeight);
                if (finalSize != null) {
                    return finalSize;
                }// else, try to find an other SPI which can thumbnailed this file
            }
        }
        return null;
    }

    /**
     * @see org.olat.system.commons.configuration.ConfigOnOff#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
