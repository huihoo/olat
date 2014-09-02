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
package org.olat.lms.search.indexer;

import java.io.IOException;

import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.document.file.DocumentException;

/**
 * Initial Date: 04.04.2013 <br>
 * 
 * @author oliver.buehler@agility-informatik.ch
 */
public abstract class SubLevelIndexer<T> extends Indexer {

    /**
     * Do index the part of an indexer.
     * 
     * @param searchResourceContext
     *            Current search-resource-context is used to build resouceUrl of an index-element.
     * @param indexerObject
     *            Index parent object. Each indexer must cast to the known type e.g. BusinessGroup.
     * @param indexer
     *            Callback object to add documents to the index.
     * @throws IOException
     *             Can not write index file.
     */
    public abstract void doIndex(SearchResourceContext searchResourceContext, T parentObject, OlatFullIndexer indexWriter) throws IOException, DocumentException,
            FolderIndexerTimeoutException;

}
