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

package org.olat.lms.ims.qti;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.olat.data.commons.fileutil.ExportUtil;
import org.olat.data.qti.QTIResult;
import org.olat.lms.ims.qti.exporter.QTIExportFormatter;
import org.olat.lms.ims.qti.exporter.QTIExportItemFactory;
import org.olat.lms.ims.qti.exporter.QTIExportItemFormatConfig;
import org.olat.lms.ims.qti.exporter.QTIExportSet;
import org.olat.lms.ims.qti.exporter.helper.QTIItemObject;
import org.olat.lms.ims.qti.exporter.helper.QTIObjectTreeBuilder;
import org.olat.lms.qti.QTIResultService;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.manager.BasicManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description: TODO
 * 
 * @author Alexander Schneider
 */
@Service
public class QTIExportManager extends BasicManager {

    @Autowired
    private QTIResultService qtiResultService;

    /**
     * @param locale
     * @param olatResource
     * @param shortTitle
     * @param olatResourceDetail
     * @param repositoryRef
     * @param type
     * @param exportDirectory
     * @param anonymizerCallback
     *            callback that should be used to anonymize the user names or NULL if row counter should be used (only for type 2 and 3)
     * @return
     */
    public boolean selectAndExportResults(final QTIExportFormatter qef, final Long olatResource, final String shortTitle, final String olatResourceDetail,
            final Long repositoryRef, final File exportDirectory, final String charset, final String fileNameSuffix) {
        boolean resultsFoundAndExported = false;
        final List<QTIResult> results = qtiResultService.selectResults(olatResource, olatResourceDetail, repositoryRef, qef.getType());
        if (results.size() > 0) {
            final QTIResult res0 = results.get(0);

            final QTIObjectTreeBuilder qotb = new QTIObjectTreeBuilder(new Long(res0.getResultSet().getRepositoryRef()));

            final List<QTIItemObject> qtiItemObjectList = qotb.getQTIItemObjectList();
            qef.setQTIItemObjectList(qtiItemObjectList);
            if (results.size() > 0) {
                createContentOfExportFile(results, qtiItemObjectList, qef);
                writeContentToFile(shortTitle, exportDirectory, charset, qef, fileNameSuffix);
                resultsFoundAndExported = true;
            }
        }
        return resultsFoundAndExported;
    }

    /**
     * @param qef
     * @param results
     * @param qtiItemObjectList
     * @param shortTitle
     * @param exportDirectory
     * @param charset
     * @param fileNameSuffix
     * @return
     */
    public String exportResults(final QTIExportFormatter qef, final List<QTIResult> results, final List<QTIItemObject> qtiItemObjectList, final String shortTitle,
            final File exportDirectory, final String charset, final String fileNameSuffix) {
        String targetFileName = null;

        qef.setQTIItemObjectList(qtiItemObjectList);
        if (results.size() > 0) {
            createContentOfExportFile(results, qtiItemObjectList, qef);
            targetFileName = writeContentToFile(shortTitle, exportDirectory, charset, qef, fileNameSuffix);
        }
        return targetFileName;
    }

    /**
     * @param locale
     *            Locale used for export file headers / default values
     * @param results
     * @param type
     * @param anonymizerCallback
     * @return String
     */
    private void createContentOfExportFile(final List<QTIResult> qtiResults, final List<QTIItemObject> qtiItemObjectList, final QTIExportFormatter qef) {

        qef.openReport();

        // formatter has information about how to format the different qti objects
        final Map<Class<? extends QTIExportItemFormatConfig>, QTIExportItemFormatConfig> mapWithConfigs = qef.getMapWithExportItemConfigs();
        final QTIExportItemFactory qeif = new QTIExportItemFactory(mapWithConfigs);

        while (qtiResults.size() > 0) {
            final List<QTIResult> assessIDresults = stripNextAssessID(qtiResults);

            qef.openResultSet(new QTIExportSet(assessIDresults.get(0)));

            for (final Iterator<QTIItemObject> iter = qtiItemObjectList.iterator(); iter.hasNext();) {
                final QTIItemObject element = iter.next();

                QTIResult qtir = element.extractQTIResult(assessIDresults);
                qef.visit(qeif.getExportItem(qtir, element));

            }
            qef.closeResultSet();
        }
        qef.closeReport();
    }

    /**
     * writes content of all results to a file
     */
    private String writeContentToFile(final String shortTitle, final File exportDirectory, final String charset, final QTIExportFormatter qef, final String fileNameSuffix) {
        // defining target filename
        final StringBuilder tf = new StringBuilder();
        tf.append(qef.getFileNamePrefix());
        tf.append(Formatter.makeStringFilesystemSave(shortTitle));
        tf.append("_");
        final DateFormat myformat = new SimpleDateFormat("yyyy-MM-dd__hh-mm-ss__SSS");
        final String timestamp = myformat.format(new Date());
        tf.append(timestamp);
        tf.append(fileNameSuffix);
        final String targetFileName = tf.toString();

        ExportUtil.writeContentToFile(targetFileName, qef.getReport(), exportDirectory, charset);

        return targetFileName;
    }

    /**
     * @param queryResult
     * @return List of results with the same assessmentid
     */
    private List<QTIResult> stripNextAssessID(final List<QTIResult> queryResult) {
        final List<QTIResult> result = new ArrayList<QTIResult>();

        if (queryResult.size() == 0) {
            return result;
        }

        QTIResult qtir = queryResult.remove(0);

        final long currentAssessmentID = qtir.getResultSet().getAssessmentID();
        result.add(qtir);

        while (queryResult.size() > 0) {
            qtir = queryResult.remove(0);
            if (qtir.getResultSet().getAssessmentID() == currentAssessmentID) {
                result.add(qtir);
            } else {
                queryResult.add(0, qtir);
                break;
            }
        }
        return result;
    }

}
