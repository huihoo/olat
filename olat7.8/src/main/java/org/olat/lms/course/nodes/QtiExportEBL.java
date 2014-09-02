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
import org.olat.lms.ims.qti.exporter.QTIExportItemFormatConfig;
import org.olat.lms.ims.qti.exporter.QTIItemConfigurations;
import org.olat.lms.ims.qti.exporter.helper.QTIItemObject;
import org.olat.lms.ims.qti.exporter.helper.QTIObjectTreeBuilder;
import org.olat.lms.qti.QTIResultService;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.user.UserService;
import org.olat.system.spring.CoreSpringFactory;
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

    public static final String FILE_NAME_SUFFIX = ".xls";
    public static final String FILEDS_SEPARATOR = "\\t";
    public static final String CARRIAGE_RETURN = "\\r\\n";
    public static final String FIELDS_EMBEDDED_BY = "\"";
    public static final String FIELDS_ESCAPED_BY = "\\";

    private static final int TEST = 1;

    private static final int SELFTEST = 2;

    private static final int SURVEY = 3;

    @Autowired
    private UserService userService;
    @Autowired
    private QTIResultService qtiResultService;

    /**
     * 
     * @param identity
     * @param locale
     * @param results
     * @return Returns a formatted string to be displayed on GUI.
     */
    public ExportResult exportResults(Identity identity, Locale locale, List<QTIResult> results, String courseTitle, CourseNode courseNode) {
        final File exportDir = CourseFactory.getOrCreateDataExportDirectory(identity, courseTitle);
        final String charset = userService.getUserCharset(identity);
        final QTIExportManager qTIExportManager = CoreSpringFactory.getBean(QTIExportManager.class);
        final Long repositoryRef = results.get(0).getResultSet().getRepositoryRef();
        final QTIObjectTreeBuilder qotb = new QTIObjectTreeBuilder(repositoryRef);
        final List<QTIItemObject> qtiItemObjectList = qotb.getQTIItemObjectList();

        final boolean anonymized = getType(courseNode) == SELFTEST || getType(courseNode) == SURVEY;
        final QTIExportFormatter formatter = new QTIExportFormatterCSVType1(locale, null, "\t", "\"", "\\", "\r\n", false, anonymized);
        final String resultExportFile = qTIExportManager.exportResults(formatter, results, qtiItemObjectList, courseNode.getShortTitle(), exportDir, charset,
                FILE_NAME_SUFFIX);
        return new ExportResult(exportDir.getAbsolutePath(), resultExportFile); // exportDir.getName() + "/" + resultExportFile;
    }

    /**
     * @param identity
     * @param locale
     * @param results
     */
    public ExportResult exportResultsForArchiveWithDefaultFormat(Identity identity, Locale locale,
            final Map<Class<? extends QTIExportItemFormatConfig>, QTIExportItemFormatConfig> exportItemConfig, List<QTIResult> results, CourseNode courseNode,
            String courseTitle) {
        final ExportFormatConfig exportFormatConfig = new ExportFormatConfig();
        exportFormatConfig.setSeparator(FILEDS_SEPARATOR);
        exportFormatConfig.setEmbeddedBy(FIELDS_EMBEDDED_BY);
        exportFormatConfig.setEscapedBy(FIELDS_ESCAPED_BY);
        exportFormatConfig.setCarriageReturn(CARRIAGE_RETURN);
        exportFormatConfig.setFileNameSuffix(FILE_NAME_SUFFIX);
        exportFormatConfig.setTagless(true);

        return exportResultsForArchiveWithCustomFormat(identity, locale, exportItemConfig, results, exportFormatConfig, courseNode, courseTitle);
    }

    /**
     * @param identity
     * @param locale
     * @param results
     * @param exportFormatConfig
     * @return
     */
    public ExportResult exportResultsForArchiveWithCustomFormat(Identity identity, Locale locale,
            final Map<Class<? extends QTIExportItemFormatConfig>, QTIExportItemFormatConfig> exportItemConfig, List<QTIResult> results,
            ExportFormatConfig exportFormatConfig, CourseNode courseNode, String courseTitle) {

        final QTIExportFormatter formatter = getFormatter(courseNode, locale, exportItemConfig, exportFormatConfig.getSeparatedBy(), exportFormatConfig.getEmbeddedBy(),
                exportFormatConfig.getEscapedBy(), exportFormatConfig.getCarriageReturn(), exportFormatConfig.isTagless());

        final File exportDir = CourseFactory.getOrCreateDataExportDirectory(identity, courseTitle);
        final String charset = userService.getUserCharset(identity);

        final QTIExportManager qem = CoreSpringFactory.getBean(QTIExportManager.class);

        final String resultExportFile = qem.exportResults(formatter, results, getQtiItemObjectList(getFirstResult(results)), courseNode.getShortTitle(), exportDir,
                charset, exportFormatConfig.getFileNameSuffix());
        return new ExportResult(exportDir.getAbsolutePath(), resultExportFile);
    }

    public DownloadeableMediaResource getDownloadableMediaResource(ExportResult exportResult) {
        return new DownloadeableMediaResource(new File(exportResult.getExportDirectoryPath(), exportResult.getExportFileName()));
    }

    public FileMediaResource getFileMediaResourceAsAttachment(ExportResult exportResult) {
        return new FileMediaResource(new File(exportResult.getExportDirectoryPath(), exportResult.getExportFileName()), true);
    }

    public Map<Class<? extends QTIExportItemFormatConfig>, QTIExportItemFormatConfig> getQtiItemConfig(QTIResult result) {
        return QTIItemConfigurations.getQTIItemConfigs(getQtiItemObjectList(result));
    }

    public QTIResult getFirstResult(List<QTIResult> results) {
        return results.get(0);
    }

    public boolean hasResultSets(Long olatResource, CourseNode currentCourseNode) {
        final String repositorySoftKey = (String) currentCourseNode.getModuleConfiguration().get(ModuleConfiguration.CONFIG_KEY_REPOSITORY_SOFTKEY);
        final Long repKey = CoreSpringFactory.getBean(RepositoryService.class).lookupRepositoryEntryBySoftkey(repositorySoftKey, true).getKey();
        return qtiResultService.hasResultSets(olatResource, currentCourseNode.getIdent(), repKey);
    }

    public List<QTIResult> getResults(Long resourceableId, CourseNode currentCourseNode) {
        final String repositorySoftKey = (String) currentCourseNode.getModuleConfiguration().get(ModuleConfiguration.CONFIG_KEY_REPOSITORY_SOFTKEY);
        final Long repKey = CoreSpringFactory.getBean(RepositoryService.class).lookupRepositoryEntryBySoftkey(repositorySoftKey, true).getKey();

        return qtiResultService.selectResults(resourceableId, currentCourseNode.getIdent(), repKey, getType(currentCourseNode));
    }

    private QTIExportFormatter getFormatter(final CourseNode courseNode, final Locale locale,
            final Map<Class<? extends QTIExportItemFormatConfig>, QTIExportItemFormatConfig> exportItemConfig, final String se, final String em, final String es,
            final String ca, final boolean tagless) {
        QTIExportFormatter frmtr = null;
        if (getType(courseNode) == TEST) {
            frmtr = new QTIExportFormatterCSVType1(locale, exportItemConfig, se, em, es, ca, tagless, false);
        } else if (getType(courseNode) == SELFTEST) {
            frmtr = new QTIExportFormatterCSVType2(locale, null, exportItemConfig, se, em, es, ca, tagless);
        } else { // type == SURVEY
            frmtr = new QTIExportFormatterCSVType3(locale, null, exportItemConfig, se, em, es, ca, tagless);
        }
        return frmtr;
    }

    private int getType(CourseNode courseNode) {
        if (courseNode instanceof IQTESTCourseNode) {
            return TEST;
        } else if (courseNode instanceof IQSELFCourseNode) {
            return SELFTEST;
        } else {
            return SURVEY;
        }
    }

    private List<QTIItemObject> getQtiItemObjectList(QTIResult result) {
        QTIObjectTreeBuilder qotb = new QTIObjectTreeBuilder(new Long(result.getResultSet().getRepositoryRef()));
        return qotb.getQTIItemObjectList();
    }

}
