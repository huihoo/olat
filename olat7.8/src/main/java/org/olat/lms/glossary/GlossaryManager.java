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
 * Copyright (c) 1999-2008 at frentix GmbH, Switzerland, http://www.frentix.com
 * <p>
 */
package org.olat.lms.glossary;

import java.io.File;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.fileresource.GlossaryResource;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.repository.RepositoryEntryImportExport;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * Description:<br>
 * Manager to create, delete etc. glossary learning resources. The OLAT glossary functionality is based on the core framework glossary / textmarker functions. in case new
 * glossary should be replaced by old one, use GlossaryManagerOld --> see olat_extensions.xml
 * <P>
 * Initial Date: 16.01.2009 <br>
 * 
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
public abstract class GlossaryManager extends BasicManager {
    protected static GlossaryManager INSTANCE;
    public static final String GLOSSARY_REPO_REF_IDENTIFYER = "glossary.repo.ref.identifyer";
    public static final String INTERNAL_FOLDER_NAME = "_glossary_";

    @Deprecated
    public static GlossaryManager getInstance() {
        return INSTANCE;
    }

    public abstract OlatRootFolderImpl getGlossaryRootFolder(OLATResourceable res);

    public abstract boolean exportGlossary(String glossarySoftkey, File exportedDataDir);

    public abstract MediaResource getAsMediaResource(OLATResourceable res);

    public abstract GlossaryResource createGlossary();

    public abstract RepositoryEntryImportExport getRepositoryImportExport(File importDataDir);

    public abstract void deleteGlossary(OLATResourceable res);

    public abstract OLATResourceable createCopy(OLATResourceable res, Identity identity);

    public abstract String archive(String archivFilePath, RepositoryEntry repoEntry);

}
