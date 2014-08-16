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
package org.olat.lms.glossary;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.vfs.LocalFolderImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 03.10.2011 <br>
 * 
 * @author Branislav Balaz
 */
@Component
public class GlossaryEBL {

    private static final Logger log = LoggerHelper.getLogger();

    // it doesn't seem this would ever change, so have it static in core also
    public static final String INTERNAL_FOLDER_NAME = "_glossary_";

    @Autowired
    GlossaryItemManager glossaryItemManager;

    public GlossaryDataObjectEBL processGlossaryDefinitionRelativePath(String relativePath) {

        return processGlossaryRelativePath(relativePath, GlossaryProcessType.DEFINITION);

    }

    public GlossaryDataObjectEBL processGlossaryTermRelativePath(String relativePath) {

        return processGlossaryRelativePath(relativePath, GlossaryProcessType.TERM);

    }

    private File getGlossaryFolderFile(String relativePath) {
        String[] splittedPath = splitRelativePath(relativePath);
        String glossaryId = splittedPath[1];
        String glossaryFolderString = FolderConfig.getCanonicalRoot() + FolderConfig.getRepositoryHome() + "/" + glossaryId + "/" + INTERNAL_FOLDER_NAME;
        File glossaryFolderFile = new File(glossaryFolderString);
        return glossaryFolderFile;
    }

    private String[] splitRelativePath(String relativePath) {
        return relativePath.split("/");
    }

    private String getGlossaryMainTerm(String relativePath) {
        String[] splittedPath = splitRelativePath(relativePath);
        // cut away ".html"
        String glossaryMainTerm = splittedPath[2].substring(0, splittedPath[2].length() - 5).replace("+", " ");
        return glossaryMainTerm;
    }

    private GlossaryDataObjectEBL processGlossaryRelativePath(String relativePath, GlossaryProcessType processType) {
        File glossaryFolderFile = getGlossaryFolderFile(relativePath);

        if (!glossaryFolderFile.isDirectory()) {
            log.warn("GlossaryDefinition delivery failed; path to glossaryFolder not existing: " + relativePath, null);
            return new GlossaryDataObjectEBL("", new LocalFolderImpl(new File(System.getProperty("java.io.tmpdir"))), relativePath, true, System.currentTimeMillis(),
                    new ArrayList<GlossaryItem>());
        }
        VFSContainer glossaryFolder = new LocalFolderImpl(glossaryFolderFile);
        if (!glossaryItemManager.isFolderContainingGlossary(glossaryFolder)) {
            log.warn("GlossaryDefinition delivery failed; glossaryFolder doesn't contain a valid Glossary: " + glossaryFolder, null);
            return new GlossaryDataObjectEBL("", glossaryFolder, relativePath, true, System.currentTimeMillis(), new ArrayList<GlossaryItem>());
        }

        String glossaryMainTerm = "";
        List<GlossaryItem> glossItems = new ArrayList<GlossaryItem>();

        if (processType == GlossaryProcessType.DEFINITION) {
            glossaryMainTerm = getGlossaryMainTerm(relativePath);
            glossItems = glossaryItemManager.getGlossaryItemListByVFSItem(glossaryFolder);
        }

        long lastModifiedTime = glossaryItemManager.getGlossaryLastModifiedTime(glossaryFolder);

        return new GlossaryDataObjectEBL(glossaryMainTerm, glossaryFolder, relativePath, false, lastModifiedTime, glossItems);
    }

    private enum GlossaryProcessType {
        TERM, DEFINITION
    }

}
