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

package org.olat.lms.ims.qti.exporter;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.data.commons.filter.FilterFactory;
import org.olat.lms.ims.qti.exporter.helper.IdentityAnonymizerCallback;
import org.olat.lms.ims.qti.exporter.helper.QTIItemObject;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.StringHelper;
import org.olat.system.exception.OLATRuntimeException;

/**
 * Initial Date: May 23, 2006 <br>
 * 
 * @author Alexander Schneider
 */
public class QTIExportFormatterCSVType3 extends QTIExportFormatter {

    protected int row_counter = 1;
    protected int loop_counter = 0;
    protected Long keyBefore = null;

    /**
     * @param locale
     * @param type
     * @param anonymizerCallback
     * @param delimiter
     * @param Map
     *            qtiExportFormatConfig with (QTIExportItemXYZ.class,IQTIExportItemFormatConfig)
     */

    public QTIExportFormatterCSVType3(final Locale locale, final IdentityAnonymizerCallback anonymizerCallback,
            final Map<Class<? extends QTIExportItemFormatConfig>, QTIExportItemFormatConfig> exportItemConfig, final String sep, final String emb, final String esc,
            final String car, final boolean tagless) {
        super(locale, anonymizerCallback, exportItemConfig);
        this.sep = sep;
        this.emb = emb;
        this.esc = esc;
        this.car = car;
        this.tagless = tagless;

        fileNamePrefix = "QUEST_";
        type = 3;
    }

    @Override
    public void openReport() {
        if (qtiItemObjectList == null) {
            throw new OLATRuntimeException(null, "Can not format report when qtiItemObjectList is null", null);
        }

        if (exportItemConfig == null) {
            // while deleting a survey node the formatter has no config consisting of user input
            exportItemConfig = QTIItemConfigurations.getQTIItemConfigs(qtiItemObjectList);
        }

        final QTIExportItemFactory qeif = new QTIExportItemFactory(exportItemConfig);

        // // // Preparing HeaderRow 1 and HeaderRow2
        final StringBuilder hR1 = new StringBuilder();
        final StringBuilder hR2 = new StringBuilder();

        int i = 1;
        for (final Iterator<QTIItemObject> iter = qtiItemObjectList.iterator(); iter.hasNext();) {
            final QTIItemObject item = iter.next();
            if (displayItem(qeif.getExportItemConfig(item))) {
                hR1.append(emb);
                hR1.append(escape(item.getItemTitle()));

                // CELFI#107
                String question = item.getQuestionText();
                question = FilterFactory.getXSSFilter(-1).filter(question);
                question = FilterFactory.getHtmlTagsFilter().filter(question);

                if (question.length() > cut) {
                    question = question.substring(0, cut) + "...";
                }
                question = StringHelper.unescapeHtml(question);
                hR1.append(": " + escape(question));
                // CELFI#107 END

                hR1.append(emb);

                if (qeif.getExportItemConfig(item).hasResponseCols()) {
                    final List<String> responseColumnHeaders = item.getResponseColumnHeaders();
                    for (final Iterator<String> iterator = responseColumnHeaders.iterator(); iterator.hasNext();) {
                        // HeaderRow1
                        hR1.append(sep);
                        // HeaderRow2
                        final String columnHeader = iterator.next();
                        hR2.append(i);
                        hR2.append("_");
                        hR2.append(columnHeader);
                        hR2.append(sep);
                    }
                }

                if (qeif.getExportItemConfig(item).hasPositionsOfResponsesCol()) {
                    if (item.hasPositionsOfResponses()) {
                        // HeaderRow1
                        hR1.append(sep);
                        // HeaderRow2
                        hR2.append(i);
                        hR2.append("_");
                        hR2.append(translator.translate("item.positions"));
                        hR2.append(sep);
                    }
                }
                if (qeif.getExportItemConfig(item).hasTimeCols()) {
                    // HeaderRow1
                    hR1.append(sep + sep);
                    // HeaderRow2
                    hR2.append(i);
                    hR2.append("_");
                    hR2.append(translator.translate("item.start"));
                    hR2.append(sep);

                    hR2.append(i);
                    hR2.append("_");
                    hR2.append(translator.translate("item.duration"));
                    hR2.append(sep);
                }
                i++;
            }
        }
        // // // HeaderRow1Intro
        sb.append(createHeaderRow1Intro());

        // // // HeaderRow1
        sb.append(hR1.toString());
        sb.append(car);

        // // // HeaderRow2Intro
        sb.append(createHeaderRow2Intro());

        // // // HeaderRow2
        sb.append(hR2.toString());
        sb.append(car);

    }

    @Override
    public void openResultSet(final QTIExportSet set) {

        String instUsrIdent = set.getInstitutionalUserIdentifier();
        if (instUsrIdent == null) {
            instUsrIdent = translator.translate("column.field.notavailable");
        }

        if (anonymizerCallback == null) {
            sb.append(row_counter);
        } else {
            sb.append(anonymizerCallback.getAnonymizedUserName(set.getIdentity()));
        }
        sb.append(sep);

        // datatime
        final Date date = set.getLastModified();
        sb.append(Formatter.formatDatetime(date));
        sb.append(sep);
    }

    @Override
    public void visit(final QTIExportItem eItem) {
        final List<String> responseColumns = eItem.getResponseColumns();
        final QTIExportItemFormatConfig itemFormatConfig = eItem.getConfig();

        if (displayItem(itemFormatConfig)) {
            if (itemFormatConfig.hasResponseCols()) {
                for (final Iterator<String> iter = responseColumns.iterator(); iter.hasNext();) {
                    final String responseColumn = iter.next();
                    sb.append(emb);
                    sb.append(escape(responseColumn));
                    sb.append(emb);
                    sb.append(sep);
                }
            }
            if (itemFormatConfig.hasPositionsOfResponsesCol()) {
                if (eItem.hasPositionsOfResponses()) {
                    if (eItem.getPositionsOfResponses() != null) {
                        sb.append(eItem.getPositionsOfResponses());
                    } else {
                        sb.append("n/a");
                    }
                    sb.append(sep);
                }
            }
            if (eItem.hasResult()) {
                if (itemFormatConfig.hasTimeCols()) {
                    // startdatetime
                    if (eItem.getTimeStamp().getTime() > 0) {
                        sb.append(Formatter.formatDatetime(eItem.getTimeStamp()));
                    } else {
                        sb.append("n/a");
                    }
                    sb.append(sep);

                    // column duration
                    final Long itemDuration = eItem.getDuration();

                    if (itemDuration != null) {
                        sb.append(itemDuration.longValue() / 1000);
                    } else {
                        sb.append("n/a");
                    }
                    sb.append(sep);
                }
            } else {
                // startdatetime, column duration
                if (itemFormatConfig.hasTimeCols()) {
                    sb.append(sep + sep);
                }
            }
        }
    }

    @Override
    public void closeResultSet() {
        sb.append(car);
        row_counter++;
    }

    @Override
    protected void appendMinMaxAndCutValue(final QTIItemObject element) {
        // nothing to append
    }

    @Override
    protected String createHeaderRow1Intro() {
        return sep + sep;
    }

    /**
     * Creates header line for all types
     * 
     * @param theType
     * @return header line for download
     */
    @Override
    protected String createHeaderRow2Intro() {

        final StringBuilder hr2Intro = new StringBuilder();

        // header for personalized download (iqtest)
        final String sequentialNumber = translator.translate("column.header.seqnum");

        final String date = translator.translate("column.header.date");
        hr2Intro.append(sequentialNumber);
        hr2Intro.append(sep);
        hr2Intro.append(date);
        hr2Intro.append(sep);

        return hr2Intro.toString();
    }

    public void setKeyBefore(final Long keyBefore) {
        this.keyBefore = keyBefore;
    }

}
