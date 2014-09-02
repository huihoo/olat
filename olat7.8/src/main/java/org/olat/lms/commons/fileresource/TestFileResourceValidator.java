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
package org.olat.lms.commons.fileresource;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.olat.data.commons.vfs.LocalFileImpl;
import org.olat.data.commons.vfs.LocalFolderImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.lms.ims.qti.process.AssessmentInstance;
import org.olat.lms.ims.qti.process.QTIHelper;

/**
 * Description:<br>
 * TODO: patrickb Class Description for TestFileResourceValidator
 * <P>
 * Initial Date: 25.06.2010 <br>
 * 
 * @author patrickb
 */
public class TestFileResourceValidator implements QTIFileResourceValidator {

    /**
	 */

    protected TestFileResourceValidator() {
    }

    @Override
    public boolean validate(final File unzippedDir) {

        // with VFS FIXME:pb:c: remove casts to LocalFileImpl and LocalFolderImpl if
        // no longer needed.
        final VFSContainer vfsUnzippedRoot = new LocalFolderImpl(unzippedDir);
        final VFSItem vfsQTI = vfsUnzippedRoot.resolve("qti.xml");
        // getDocument(..) ensures that InputStream is closed in every case.
        final Document doc = QTIHelper.getDocument((LocalFileImpl) vfsQTI);
        // if doc is null an error loading the document occured
        if (doc == null) {
            return false;
        }
        // check if this is marked as test
        final List metas = doc.selectNodes("questestinterop/assessment/qtimetadata/qtimetadatafield");
        for (final Iterator iter = metas.iterator(); iter.hasNext();) {
            final Element el_metafield = (Element) iter.next();
            final Element el_label = (Element) el_metafield.selectSingleNode("fieldlabel");
            final String label = el_label.getText();
            if (label.equals(AssessmentInstance.QMD_LABEL_TYPE)) { // type meta
                final Element el_entry = (Element) el_metafield.selectSingleNode("fieldentry");
                final String entry = el_entry.getText();
                if (!(entry.equals(AssessmentInstance.QMD_ENTRY_TYPE_SELF) || entry.equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS))) {
                    return false;
                }
            }
        }

        // check if at least one section with one item
        final List sectionItems = doc.selectNodes("questestinterop/assessment/section/item");
        if (sectionItems.size() == 0) {
            return false;
        }

        for (final Iterator iter = sectionItems.iterator(); iter.hasNext();) {
            final Element it = (Element) iter.next();
            final List sv = it.selectNodes("resprocessing/outcomes/decvar[@varname='SCORE']");
            // the QTIv1.2 system relies on the SCORE variable of items
            if (sv.size() != 1) {
                return false;
            }
        }

        return true;
    }

}
