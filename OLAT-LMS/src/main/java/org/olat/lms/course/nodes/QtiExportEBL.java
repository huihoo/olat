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
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.data.basesecurity.Identity;
import org.olat.data.qti.QTIResult;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.commons.mediaresource.DownloadeableMediaResource;
import org.olat.lms.commons.mediaresource.FileMediaResource;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.ims.qti.QTIExportManager;
import org.olat.lms.ims.qti.exporter.ExportFormatConfig;
import org.olat.lms.ims.qti.exporter.QTIExportFormatter;
import org.olat.lms.ims.qti.exporter.QTIExportFormatterCSVType1;
import org.olat.lms.ims.qti.exporter.QTIExportFormatterCSVType2;
import org.olat.lms.ims.qti.exporter.QTIExportFormatterCSVType3;
import org.olat.lms.ims.qti.exporter.QTIItemConfigurations;
import org.olat.lms.ims.qti.exporter.helper.QTIObjectTreeBuilder;
import org.olat.lms.qti.QTIResultService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Stateful.
 * 
 * <P>
 * Initial Date: 23.09.2011 <br>
 * 
 * @author lavinia
 */
@Component
public class QtiExportEBL {

    public static String FILE_NAME_SUFFIX = ".xls";
    public static String FILEDS_SEPARATOR = "\\t";
    public static String CARRIAGE_RETURN = "\\r\\n";
    public static String FIELDS_EMBEDDED_BY = "\"";
    public static String FIELDS_ESCAPED_BY = "\\";

    @Autowired
    private UserService userService;
    @Autowired
    private QTIResultService qtiResultService;

    public QtiExportEBL() {
    }

    /**
     * 
     * @param identity
     * @param locale
     * @param results
     * @return Returns a formatted string to be displayed on GUI.
     */
    // TODO: ORID-1007 Code duplication with 'exportResultsForArchiveWithCustomFormat'
    // 'exportResults' method set tagless=false
    // 'exportResultsForArchiveWithDefaultFormat' method is default tagless=true
    public ExportResult exportResults(Identity identity, Locale locale, List<QTIResult> results, String courseTitle, String courseNodeShortTitle) {
        File exportDir = CourseFactory.getOrCreateDataExportDirectory(identity, courseTitle);
        final String charset = userService.getUserCharset(identity);
        final QTIExportManager qTIExportManager = QTIExportManager.getInstance();
        final Long repositoryRef = results.get(0).getResultSet().getRepositoryRef();
        final QTIObjectTreeBuilder qotb = new QTIObjectTreeBuilder(repositoryRef);
        final List qtiItemObjectList = qotb.getQTIItemObjectList();

        final QTIExportFormatter formatter = new QTIExportFormatterCSVType1(locale, "\t", "\"", "\\", "\r\n", false);
        QTIItemConfigurations qTIItemConfigurations = new QTIItemConfigurations();
        final Map qtiItemConfigs = qTIItemConfigurations.getQTIItemConfigs(qtiItemObjectList);
        formatter.setMapWithExportItemConfigs(qtiItemConfigs);
        String resultExportFile = qTIExportManager.exportResults(formatter, results, qtiItemObjectList, courseNodeShortTitle, exportDir, charset, FILE_NAME_SUFFIX);
        return new ExportResult(exportDir.getAbsolutePath(), resultExportFile); // exportDir.getName() + "/" + resultExportFile;
    }

    public DownloadeableMediaResource getDownloadableMediaResource(ExportResult exportResult) {
        return new DownloadeableMediaResource(new File(exportResult.getExportDirectoryPath(), exportResult.getExportFileName()));
    }

    public FileMediaResource getFileMediaResourceAsAttachment(ExportResult exportResult) {
        return new FileMediaResource(new File(exportResult.getExportDirectoryPath(), exportResult.getExportFileName()), true);
    }

    /**
     * @param identity
     * @param locale
     * @param results
     */
    public ExportResult exportResultsForArchiveWithDefaultFormat(Identity identity, Locale locale, List results, CourseNode courseNode, String courseTitle) {

        ExportFormatConfig exportFormatConfig = new ExportFormatConfig();
        exportFormatConfig.setSeparator(FILEDS_SEPARATOR);
        exportFormatConfig.setEmbeddedBy(FIELDS_EMBEDDED_BY);
        exportFormatConfig.setEscapedBy(FIELDS_ESCAPED_BY);
        exportFormatConfig.setCarriageReturn(CARRIAGE_RETURN);
        exportFormatConfig.setFileNameSuffix(FILE_NAME_SUFFIX);
        exportFormatConfig.setTagless(true);

        return exportResultsForArchiveWithCustomFormat(identity, locale, results, exportFormatConfig, courseNode, courseTitle);
    }

    public Map getQtiItemConfig(QTIResult result) {
        QTIItemConfigurations qTIItemConfigurations = new QTIItemConfigurations();
        return qTIItemConfigurations.getQTIItemConfigs(getQtiItemObjectList(result));
    }

    public List getQtiItemObjectList(QTIResult result) {
        QTIObjectTreeBuilder qotb = new QTIObjectTreeBuilder(new Long(result.getResultSet().getRepositoryRef()));
        return qotb.getQTIItemObjectList();
    }

    public QTIResult getFirstResult(List results) {
        return (QTIResult) results.get(0);
    }

    private QTIExportFormatter getFormatter(final CourseNode courseNode, final Locale locale, final String se, final String em, final String es, final String ca,
            final boolean tagless) {
        QTIExportFormatter frmtr = null;
        if (getType(courseNode) == 1) {
            frmtr = new QTIExportFormatterCSVType1(locale, se, em, es, ca, tagless);
        } else if (getType(courseNode) == 2) {
            frmtr = new QTIExportFormatterCSVType2(locale, null, se, em, es, ca, tagless);
        } else { // type == 3
            frmtr = new QTIExportFormatterCSVType3(locale, null, se, em, es, ca, tagless);
        }
        return frmtr;
    }

    public int getType(CourseNode courseNode) {
        int type;
        if (courseNode instanceof IQTESTCourseNode) {
            type = 1;
        } else if (courseNode instanceof IQSELFCourseNode) {
            type = 2;
        } else {
            type = 3;
        }
        return type;
    }

    /**
     * @param identity
     * @param locale
     * @param results
     * @param exportFormatConfig
     * @return
     */
    public ExportResult exportResultsForArchiveWithCustomFormat(Identity identity, Locale locale, List results, ExportFormatConfig exportFormatConfig,
            CourseNode courseNode, String courseTitle) {

        QTIExportFormatter formatter = getFormatter(courseNode, locale, exportFormatConfig.getSeparatedBy(), exportFormatConfig.getEmbeddedBy(),
                exportFormatConfig.getEscapedBy(), exportFormatConfig.getCarriageReturn(), exportFormatConfig.isTagless());

        formatter.setMapWithExportItemConfigs(getQtiItemConfig(getFirstResult(results)));

        File exportDir = CourseFactory.getOrCreateDataExportDirectory(identity, courseTitle);
        final String charset = userService.getUserCharset(identity);

        final QTIExportManager qem = QTIExportManager.getInstance();

        String resultExportFile = qem.exportResults(formatter, results, getQtiItemObjectList(getFirstResult(results)), courseNode.getShortTitle(), exportDir, charset,
                exportFormatConfig.getFileNameSuffix());
        return new ExportResult(exportDir.getAbsolutePath(), resultExportFile);
    }

    public boolean hasResultSets(Long olatResource, CourseNode currentCourseNode) {
        final String repositorySoftKey = (String) currentCourseNode.getModuleConfiguration().get(ModuleConfiguration.CONFIG_KEY_REPOSITORY_SOFTKEY);
        final Long repKey = RepositoryServiceImpl.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftKey, true).getKey();
        return qtiResultService.hasResultSets(olatResource, currentCourseNode.getIdent(), repKey);
    }

    public List getResults(Long resourceableId, CourseNode currentCourseNode) {
        final String repositorySoftKey = (String) currentCourseNode.getModuleConfiguration().get(ModuleConfiguration.CONFIG_KEY_REPOSITORY_SOFTKEY);
        final Long repKey = RepositoryServiceImpl.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftKey, true).getKey();

        return qtiResultService.selectResults(resourceableId, currentCourseNode.getIdent(), repKey, getType(currentCourseNode));
    }

}
