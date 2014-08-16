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
package org.olat.lms.course.nodes;

import java.io.File;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Constants;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.LocalFileImpl;
import org.olat.data.commons.vfs.LocalFolderImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.commons.fileresource.FileResourceManager;
import org.olat.lms.ims.qti.QTIExportManager;
import org.olat.lms.ims.qti.exporter.QTIExportFormatter;
import org.olat.lms.ims.qti.process.QTIHelper;
import org.olat.lms.repository.RepositoryService;
import org.olat.system.exception.AssertException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * TODO: Class Description for QtiEBL
 * 
 * <P>
 * Initial Date: 14.09.2011 <br>
 * 
 * @author guretzki
 */
@Component
public class QtiEBL {

    @Autowired
    private BaseSecurity baseSecurity;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private QTIExportManager qTIExportManager;

    public boolean isEditable(Identity identity, RepositoryEntry re) {
        return (baseSecurity.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_ADMIN)
                || repositoryService.isOwnerOfRepositoryEntry(identity, re) || repositoryService.isInstitutionalRessourceManagerFor(re, identity));
    }

    /**
     * @param res
     * @return
     */
    Document getDocument(final OLATResource res) {
        final FileResourceManager frm = FileResourceManager.getInstance();
        final File unzippedRoot = frm.unzipFileResource(res);
        // with VFS FIXME:pb:c: remove casts to LocalFileImpl and LocalFolderImpl if no longer needed.
        final VFSContainer vfsUnzippedRoot = new LocalFolderImpl(unzippedRoot);
        final VFSItem vfsQTI = vfsUnzippedRoot.resolve("qti.xml");
        if (vfsQTI == null) {
            throw new AssertException("qti file did not exist even it should be guaranteed by repositor check-in "
                    + ((LocalFileImpl) vfsQTI).getBasefile().getAbsolutePath());
        }
        // ensures that InputStream is closed in every case.
        final Document doc = QTIHelper.getDocument((LocalFileImpl) vfsQTI);
        if (doc == null) {
            // error reading qti file (existence check was made before)
            throw new AssertException("qti file could not be read " + ((LocalFileImpl) vfsQTI).getBasefile().getAbsolutePath());
        }
        return doc;
    }

    /**
     * @param res
     * @return
     */
    public TestConfiguration getTestConfiguration(final OLATResource res) {
        final Document doc = getDocument(res);
        // Extract min, max and cut value
        Float minValue = null, maxValue = null, cutValue = null;
        final Element decvar = (Element) doc.selectSingleNode("questestinterop/assessment/outcomes_processing/outcomes/decvar");
        if (decvar != null) {
            final Attribute minval = decvar.attribute("minvalue");
            if (minval != null) {
                final String mv = minval.getValue();
                try {
                    minValue = new Float(Float.parseFloat(mv));
                } catch (final NumberFormatException e1) {
                    // if not correct in qti file -> ignore
                }
            }
            final Attribute maxval = decvar.attribute("maxvalue");
            if (maxval != null) {
                final String mv = maxval.getValue();
                try {
                    maxValue = new Float(Float.parseFloat(mv));
                } catch (final NumberFormatException e1) {
                    // if not correct in qti file -> ignore
                }
            }
            final Attribute cutval = decvar.attribute("cutvalue");
            if (cutval != null) {
                final String cv = cutval.getValue();
                try {
                    cutValue = new Float(Float.parseFloat(cv));
                } catch (final NumberFormatException e1) {
                    // if not correct in qti file -> ignore
                }
            }
        }
        TestConfiguration testConfiguration = new TestConfiguration(minValue, maxValue, cutValue);
        return testConfiguration;
    }

    public boolean archiveIQTestCourseNode(final QTIExportFormatter formatter, final ModuleConfiguration moduleConfiguration, final Long courseResourceableId,
            final String shortTitle, final String ident, final File exportDirectory, final String charset) {

        final String repositorySoftKey = (String) moduleConfiguration.get(ModuleConfiguration.CONFIG_KEY_REPOSITORY_SOFTKEY);
        final Long repKey = repositoryService.lookupRepositoryEntryBySoftkey(repositorySoftKey, true).getKey();
        final boolean retVal = qTIExportManager.selectAndExportResults(formatter, courseResourceableId, shortTitle, ident, repKey, exportDirectory, charset, ".xls");
        return retVal;
    }

}
