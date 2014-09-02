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

package org.olat.lms.search;

/**
 * Search module config.
 * 
 * @author Christian Guretzki
 */
import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.system.commons.configuration.AbstractOLATModule;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * Initial Date: 15.06.200g <br>
 * 
 * @author Christian Guretzki
 * @author oliver.buehler@agility-informatik.ch
 */
public class SearchModule extends AbstractOLATModule {
    private static final Logger log = LoggerHelper.getLogger();

    // Definitions config parameter names in module-config
    private final static String CONF_INDEX_PATH = "indexPath";
    private final static String CONF_TEMP_INDEX_PATH = "tempIndexPath";

    private final static String CONF_INDEX_PRIO = "indexPrio";

    private final static String CONF_TIMEOUT = "indexTimeout";
    private final static String CONF_TIMEOUT_FOLDER = "indexTimeoutFolder";
    private final static String CONF_TIMEOUT_FILE = "indexTimeoutFile";

    private static final String CONF_MAX_HITS = "maxHits";
    private static final String CONF_MAX_RESULTS = "maxResults";

    private static final String CONF_INDEXER_DOCUMENT_QUEUE_SIZE = "indexer.document.queue.size";
    private static final String CONF_INDEXER_WRITER_NUMBER = "indexer.writer.number";
    private static final String CONF_INDEXER_WRITER_MERGE_FACTOR = "indexer.writer.merge.factor";
    private static final String CONF_INDEXER_WRITER_RAMBUFFER_MB = "indexer.writer.rambuffer.mb";

    private static final String CONF_MAX_FILE_SIZE = "maxFileSize";

    private static final String CONF_PPT_FILE_ENABLED = "pptFileEnabled";
    private static final String CONF_EXCEL_FILE_ENABLED = "excelFileEnabled";
    private static final String CONF_SPELL_CHECK_ENABLED = "spellCheckEnabled";
    private static final String CONF_TEXT_BUFFER_ENABLED = "textBufferEnabled";
    private static final String CONF_TEXT_BUFFER_CLEANUP_ENABLED = "textBufferCleanupEnabled";

    // Default values
    private static final int DEFAULT_MAX_HITS = 1000;
    private static final int DEFAULT_MAX_RESULTS = 100;
    private static final String DEFAULT_RAM_BUFFER_SIZE_MB = "48";

    private static final int DEFAULT_TIMEOUT_SEC = 60 * 60 * 10;

    private static final String SEARCH_INDEX_NAME = "search_index";
    private static final String SPELL_CHECKER_INDEX_NAME = "spellcheck_index";

    private String indexPath;
    private String searchIndexPath;
    private String tempSearchIndexPath;
    private String spellCheckerIndexPath;
    private String tempSpellCheckerIndexPath;

    private int indexerPrio;

    private int timeoutSeconds = DEFAULT_TIMEOUT_SEC;
    private int timeoutFolderSeconds;
    private int timeoutFileSeconds;

    private int maxHits;
    private int maxResults;
    private List<String> fileBlackList;

    private int indexerDocumentQueueSize;
    private int indexerWriterNumber;
    private int indexerWriterMergeFactor;
    private double indexerWriterRambuffer;

    private boolean pptFileEnabled;
    private boolean excelFileEnabled;
    private boolean spellCheckEnabled;
    private boolean textBufferEnabled;
    private boolean textBufferCleanupEnabled;

    private String textBufferPath;
    private List<String> fileSizeSuffixes;

    private long maxFileSize;
    private List<Long> repositoryBlackList;

    /**
     * [used by spring]
     */
    private SearchModule() {
        super();
    }

    /**
     * [used by spring]
     * 
     * @param fileSizeSuffixes
     */
    public void setFileSizeSuffixes(List<String> fileSizeSuffixes) {
        this.fileSizeSuffixes = fileSizeSuffixes;
    }

    /**
     * [used by spring]
     * 
     * @param fileBlackList
     */
    public void setFileBlackList(List<String> fileBlackList) {
        this.fileBlackList = fileBlackList;
    }

    /**
     * [used by spring]
     * 
     * @param fileBlackList
     */
    public void setRepositoryBlackList(List<Long> repositoryBlackList) {
        this.repositoryBlackList = repositoryBlackList;
    }

    /**
     * Read config-parameter from configuration and store this locally.
     */
    @Override
    public void initialize() {
        log.debug("init start...");

        indexPath = getStringConfigParameter(CONF_INDEX_PATH, FolderConfig.getCanonicalRoot() + File.separator + "index", false);
        searchIndexPath = indexPath + File.separator + SEARCH_INDEX_NAME;
        spellCheckerIndexPath = indexPath + File.separator + SPELL_CHECKER_INDEX_NAME;
        final String tempIndexPath = getStringConfigParameter(CONF_TEMP_INDEX_PATH, FolderConfig.getCanonicalTmpDir() + File.separator + "index_tmp", false);
        tempSearchIndexPath = tempIndexPath + File.separator + SEARCH_INDEX_NAME;
        tempSpellCheckerIndexPath = tempIndexPath + File.separator + SPELL_CHECKER_INDEX_NAME;

        indexerPrio = getIntConfigParameter(CONF_INDEX_PRIO, Thread.NORM_PRIORITY);
        if (indexerPrio < Thread.MIN_PRIORITY || indexerPrio > Thread.MAX_PRIORITY) {
            indexerPrio = Thread.NORM_PRIORITY;
            log.warn("Indexer prio has to be set between " + Thread.MIN_PRIORITY + " and " + Thread.MAX_PRIORITY);
        }

        final String timeoutString = getStringConfigParameter(CONF_TIMEOUT, "", false);
        if (!timeoutString.isEmpty()) {
            timeoutSeconds = parseTimeoutString(timeoutString);
        }
        final String timeouFoldertString = getStringConfigParameter(CONF_TIMEOUT_FOLDER, "", false);
        if (!timeouFoldertString.isEmpty()) {
            timeoutFolderSeconds = parseTimeoutString(timeouFoldertString);
        }
        final String timeoutFileString = getStringConfigParameter(CONF_TIMEOUT_FILE, "", false);
        if (!timeoutFileString.isEmpty()) {
            timeoutFileSeconds = parseTimeoutString(timeoutFileString);
        }

        spellCheckEnabled = getBooleanConfigParameter(CONF_SPELL_CHECK_ENABLED, true);
        textBufferEnabled = getBooleanConfigParameter(CONF_TEXT_BUFFER_ENABLED, true);
        textBufferCleanupEnabled = getBooleanConfigParameter(CONF_TEXT_BUFFER_CLEANUP_ENABLED, true);
        textBufferPath = indexPath + File.separator + "text_buffer";

        maxHits = getIntConfigParameter(CONF_MAX_HITS, DEFAULT_MAX_HITS);
        maxResults = getIntConfigParameter(CONF_MAX_RESULTS, DEFAULT_MAX_RESULTS);

        indexerDocumentQueueSize = getIntConfigParameter(CONF_INDEXER_DOCUMENT_QUEUE_SIZE, 10000);
        indexerWriterNumber = getIntConfigParameter(CONF_INDEXER_WRITER_NUMBER, 0);
        indexerWriterMergeFactor = getIntConfigParameter(CONF_INDEXER_WRITER_MERGE_FACTOR, 1000);
        indexerWriterRambuffer = Double.parseDouble(getStringConfigParameter(CONF_INDEXER_WRITER_RAMBUFFER_MB, DEFAULT_RAM_BUFFER_SIZE_MB, false));

        excelFileEnabled = getBooleanConfigParameter(CONF_EXCEL_FILE_ENABLED, true);
        pptFileEnabled = getBooleanConfigParameter(CONF_PPT_FILE_ENABLED, true);
        maxFileSize = Integer.parseInt(getStringConfigParameter(CONF_MAX_FILE_SIZE, "0", false));
    }

    /**
     * @return Absolute file path for index data.
     */
    public String getIndexPath() {
        return indexPath;
    }

    /**
     * @return Absolute file path for the search index.
     */
    public String getSearchIndexPath() {
        return searchIndexPath;
    }

    /**
     * @return Absolute file path for the spell checker index.
     */
    public String getSpellCheckerIndexPath() {
        return spellCheckerIndexPath;
    }

    /**
     * @return Absolute file path for the temporary search index (during creation).
     */
    public String getTempSearchIndexPath() {
        return tempSearchIndexPath;
    }

    /**
     * @return runtime priority of indexer thread (mapped to Java thread priorities)
     */
    public int getIndexerPrio() {
        return indexerPrio;
    }

    /**
     * @return Absolute file path for the temporary spell checker index (during creation).
     */
    public String getTempSpellCheckerIndexPath() {
        return tempSpellCheckerIndexPath;
    }

    /**
     * @return TRUE: Spell-checker is enabled.
     */
    public boolean isSpellCheckEnabled() {
        return spellCheckEnabled;
    }

    public int getIndexerDocumentQueueSize() {
        return indexerDocumentQueueSize;
    }

    public int getIndexerWriterNumber() {
        return indexerWriterNumber;
    }

    public int getIndexerWriterMergeFactor() {
        return indexerWriterMergeFactor;
    }

    public double getIndexerWriterRamBuffer() {
        return indexerWriterRambuffer;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public int getTimeoutFolderSeconds() {
        return timeoutFolderSeconds;
    }

    public int getTimeoutFileSeconds() {
        return timeoutFileSeconds;
    }

    public boolean isTextBufferEnabled() {
        return textBufferEnabled;
    }

    public boolean isTextBufferCleanupEnabled() {
        return textBufferCleanupEnabled;
    }

    public String getTextBufferPath() {
        return textBufferPath;
    }

    /**
     * @return Number of maximal hits before filtering of results for a certain search-query.
     */
    public int getMaxHits() {
        return maxHits;
    }

    /**
     * @return Number of maximal displayed results for a certain search-query.
     */
    public int getMaxResults() {
        return maxResults;
    }

    /**
     * @return Space seperated list of non indexed files.
     */
    public List<String> getFileBlackList() {
        return fileBlackList;
    }

    /**
     * @return TRUE: index Power-Point-files.
     */
    public boolean isPptFileEnabled() {
        return pptFileEnabled;
    }

    /**
     * @return TRUE: index Excel-files.
     */
    public boolean isExcelFileEnabled() {
        return excelFileEnabled;
    }

    public List<String> getFileSizeSuffixes() {
        return fileSizeSuffixes;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public List<Long> getRepositoryBlackList() {
        return repositoryBlackList;
    }

    @Override
    protected void initDefaultProperties() {
        // not implemented
    }

    @Override
    protected void initFromChangedProperties() {
        // not implemented
    }

    private static int parseTimeoutString(String timeoutString) {
        try {
            if (timeoutString.contains("h")) {
                return 60 * 60 * Integer.parseInt(timeoutString.substring(0, timeoutString.indexOf("h")));
            } else if (timeoutString.contains("m")) {
                return 60 * Integer.parseInt(timeoutString.substring(0, timeoutString.indexOf("m")));
            } else if (timeoutString.contains("s")) {
                return Integer.parseInt(timeoutString.substring(0, timeoutString.indexOf("s")));
            } else {
                return Integer.parseInt(timeoutString);
            }
        } catch (NumberFormatException ex) {
            log.error("Couldn't parse timeout for search configuration");
            return DEFAULT_TIMEOUT_SEC;
        }
    }

}
