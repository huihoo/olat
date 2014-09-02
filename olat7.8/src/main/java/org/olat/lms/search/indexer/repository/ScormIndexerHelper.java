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
package org.olat.lms.search.indexer.repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.data.commons.vfs.LocalFolderImpl;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.document.file.DocumentException;
import org.olat.lms.search.indexer.FolderIndexer;
import org.olat.lms.search.indexer.FolderIndexerAccess;
import org.olat.lms.search.indexer.FolderIndexerTimeoutException;
import org.olat.lms.search.indexer.OlatFullIndexer;

/**
 * Initial Date: 15.07.2013 <br>
 * 
 * @author oliver.buehler@agility-informatik.ch
 */
public final class ScormIndexerHelper {

    private static Set<String> stopWords = new HashSet<String>();
    static {
        stopWords.add("LOMv1.0");
        stopWords.add("yes");
        stopWords.add("NA");
    }
    private static final List<String> forbiddenExtensions = new ArrayList<String>();
    static {
        forbiddenExtensions.add("LOMv1.0");
        forbiddenExtensions.add(".xsd");
        forbiddenExtensions.add(".js");
    }

    private ScormIndexerHelper() {
        super();
    }

    public static void doIndex(final SearchResourceContext resourceContext, final OlatFullIndexer indexWriter, final File cpRoot) throws IOException,
            FolderIndexerTimeoutException, DocumentException {
        final LocalFolderImpl container = new LocalFolderImpl(cpRoot);
        final ScormFileAccess accessRule = new ScormFileAccess();
        FolderIndexer.indexVFSContainer(resourceContext, container, indexWriter, accessRule);
    }

    private static final class ScormFileAccess implements FolderIndexerAccess {

        @Override
        public boolean allowed(final VFSItem item) {
            final String name = item.getName();
            for (final String forbiddenExtension : forbiddenExtensions) {
                if (name.endsWith(forbiddenExtension)) {
                    return false;
                }
            }
            return true;
        }
    }

}
