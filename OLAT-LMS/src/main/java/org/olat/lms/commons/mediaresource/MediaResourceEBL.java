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
package org.olat.lms.commons.mediaresource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Workbook;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.system.commons.CodeHelper;
import org.olat.system.exception.AssertException;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 03.10.2011 <br>
 * 
 * @author Branislav Balaz
 */
@Component
public class MediaResourceEBL {

    public MediaResource createMediaResourceFromDocument(final Workbook wb) {
        FileOutputStream fos = null;
        try {
            String tmpFilePath = FolderConfig.getCanonicalTmpDir() + File.separator + "TableExport" + CodeHelper.getRAMUniqueID() + ".xls";
            File f = new File(tmpFilePath);
            fos = new FileOutputStream(f);
            wb.write(fos);
            fos.close();
            return new CleanupAfterDeliveryFileMediaResource(f);
        } catch (IOException e) {
            throw new AssertException("error preparing media resource for XLS Table Export", e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    throw new AssertException("error preparing media resource for XLS Table Export and closing stream", e1);
                }
            }
        }
    }

}
