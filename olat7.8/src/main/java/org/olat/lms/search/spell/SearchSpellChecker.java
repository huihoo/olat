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

package org.olat.lms.search.spell;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.lms.search.SearchModule;
import org.olat.lms.search.document.AbstractOlatDocument;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Spell-checker part inside of search-service. Service to check certain search-query for similar available search.queries.
 * 
 * @author Christian Guretzki
 */
public class SearchSpellChecker {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String CONTENT_PATH = "_content";
    private static final String TITLE_PATH = "_title";
    private static final String DESCRIPTION_PATH = "_description";
    private static final String AUTHOR_PATH = "_author";

    /**
     * Creates a new spell-check index based on search-index
     */
    public static void createSpellIndex(final SearchModule searchModule) {
        final String tempSearchIndexPath = searchModule.getTempSearchIndexPath();
        final String tempSpellCheckIndexPath = searchModule.getTempSpellCheckerIndexPath();

        IndexReader indexReader = null;
        try {
            log.info("Start generating spell check index ...");

            long startSpellIndexTime = 0;
            if (log.isDebugEnabled()) {
                startSpellIndexTime = System.currentTimeMillis();
            }
            final Directory indexDir = FSDirectory.open(new File(tempSearchIndexPath, "main"));
            indexReader = IndexReader.open(indexDir);

            // 1. Create content spellIndex
            log.info("Generating 'content' spell check index ...");
            final File contentSpellIndexPath = new File(tempSpellCheckIndexPath + CONTENT_PATH);
            FileUtils.deleteDirsAndFiles(contentSpellIndexPath, true, true);
            final Directory contentSpellIndexDirectory = FSDirectory.open(contentSpellIndexPath);
            final SpellChecker contentSpellChecker = new SpellChecker(contentSpellIndexDirectory);
            final Dictionary contentDictionary = new LuceneDictionary(indexReader, AbstractOlatDocument.CONTENT_FIELD_NAME);
            contentSpellChecker.indexDictionary(contentDictionary);

            // 2. Create title spellIndex
            log.info("Generating 'title' spell check index ...");
            final File titleSpellIndexPath = new File(tempSpellCheckIndexPath + TITLE_PATH);
            FileUtils.deleteDirsAndFiles(titleSpellIndexPath, true, true);
            final Directory titleSpellIndexDirectory = FSDirectory.open(titleSpellIndexPath);
            final SpellChecker titleSpellChecker = new SpellChecker(titleSpellIndexDirectory);
            final Dictionary titleDictionary = new LuceneDictionary(indexReader, AbstractOlatDocument.TITLE_FIELD_NAME);
            titleSpellChecker.indexDictionary(titleDictionary);

            // 3. Create description spellIndex
            log.info("Generating 'description' spell check index ...");
            final File descriptionSpellIndexPath = new File(tempSpellCheckIndexPath + DESCRIPTION_PATH);
            FileUtils.deleteDirsAndFiles(descriptionSpellIndexPath, true, true);
            final Directory descriptionSpellIndexDirectory = FSDirectory.open(descriptionSpellIndexPath);
            final SpellChecker descriptionSpellChecker = new SpellChecker(descriptionSpellIndexDirectory);
            final Dictionary descriptionDictionary = new LuceneDictionary(indexReader, AbstractOlatDocument.DESCRIPTION_FIELD_NAME);
            descriptionSpellChecker.indexDictionary(descriptionDictionary);

            // 4. Create author spellIndex
            log.info("Generating 'author' spell check index ...");
            final File authorSpellIndexPath = new File(tempSpellCheckIndexPath + AUTHOR_PATH);
            FileUtils.deleteDirsAndFiles(authorSpellIndexPath, true, true);
            final Directory authorSpellIndexDirectory = FSDirectory.open(authorSpellIndexPath);
            final SpellChecker authorSpellChecker = new SpellChecker(authorSpellIndexDirectory);
            final Dictionary authorDictionary = new LuceneDictionary(indexReader, AbstractOlatDocument.AUTHOR_FIELD_NAME);
            authorSpellChecker.indexDictionary(authorDictionary);

            log.info("Merging spell check indices ...");
            // Merge all part spell indexes (content,title etc.) to one common spell index
            final File tempSpellCheckIndexDir = new File(tempSpellCheckIndexPath);
            FileUtils.deleteDirsAndFiles(tempSpellCheckIndexDir, true, true);
            final Directory tempSpellIndexDirectory = FSDirectory.open(tempSpellCheckIndexDir);
            final IndexWriter merger = new IndexWriter(tempSpellIndexDirectory, new StandardAnalyzer(Version.LUCENE_30), true, IndexWriter.MaxFieldLength.UNLIMITED);
            final Directory[] directories = { contentSpellIndexDirectory, titleSpellIndexDirectory, descriptionSpellIndexDirectory, authorSpellIndexDirectory };
            merger.addIndexesNoOptimize(directories);

            log.info("Optimizing spell check index ...");
            merger.optimize();
            merger.close();

            tempSpellIndexDirectory.close();

            contentSpellChecker.close();
            contentSpellIndexDirectory.close();

            titleSpellChecker.close();
            titleSpellIndexDirectory.close();

            descriptionSpellChecker.close();
            descriptionSpellIndexDirectory.close();

            authorSpellChecker.close();
            authorSpellIndexDirectory.close();

            FileUtils.deleteDirsAndFiles(contentSpellIndexPath, true, true);
            FileUtils.deleteDirsAndFiles(titleSpellIndexPath, true, true);
            FileUtils.deleteDirsAndFiles(descriptionSpellIndexPath, true, true);
            FileUtils.deleteDirsAndFiles(authorSpellIndexPath, true, true);

            if (log.isDebugEnabled()) {
                log.debug("Spell check index created in " + (System.currentTimeMillis() - startSpellIndexTime) + " ms.");
            }
        } catch (final IOException ioEx) {
            log.warn("Can not create spell check index.", ioEx);
        } finally {
            if (indexReader != null) {
                try {
                    indexReader.close();
                } catch (final IOException e) {
                    log.warn("Can not close indexReader properly", e);
                }
            }
        }
    }
}
