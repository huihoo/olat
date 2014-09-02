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

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.data.commons.filter.FilterFactory;
import org.olat.lms.ims.qti.exporter.helper.IdentityAnonymizerCallback;
import org.olat.lms.ims.qti.exporter.helper.QTIItemObject;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.StringHelper;
import org.olat.system.exception.OLATRuntimeException;

/**
 * Initial Date: May 23, 2006 <br>
 * 
 * @author Alexander Schneider
 */
public abstract class QTIExportFormatter {
    protected static final String PACKAGE = PackageUtil.getPackageName(QTIExportFormatter.class);

    protected final StringBuilder sb;
    protected final Translator translator;
    protected final IdentityAnonymizerCallback anonymizerCallback;
    protected Map<Class<? extends QTIExportItemFormatConfig>, QTIExportItemFormatConfig> exportItemConfig;

    protected List<QTIItemObject> qtiItemObjectList;
    protected String fileNamePrefix;
    protected int type;

    // Delimiters and file name suffix for the export file
    protected String sep; // fields separated by
    protected String emb; // fields embedded by
    protected String esc; // fields escaped by
    protected String car; // carriage return

    // CELFI#107
    protected final int cut = 30;
    // CELFI#107 END

    // Author can export the mattext without HTML tags
    // especially used for the results export of matrix questions created by QANT
    protected boolean tagless;

    public QTIExportFormatter(final Locale locale, final IdentityAnonymizerCallback anonymizerCallback,
            final Map<Class<? extends QTIExportItemFormatConfig>, QTIExportItemFormatConfig> exportItemConfig) {
        this.sb = new StringBuilder();
        this.translator = new PackageTranslator(PACKAGE, locale);
        this.anonymizerCallback = anonymizerCallback;
        this.exportItemConfig = exportItemConfig;
    }

    public void openReport() {
        if (qtiItemObjectList == null) {
            throw new OLATRuntimeException(null, "Can not format report when qtiItemObjectList is null", null);
        }

        if (exportItemConfig == null) {
            // while deleting a test node the formatter has no config consisting of user input
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
                // question = FilterFactory.getHtmlTagsFilter().filter(question);
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
                    // HeaderRow1
                    hR1.append(sep);
                    // HeaderRow2
                    hR2.append(i);
                    hR2.append("_");
                    hR2.append(translator.translate("item.positions"));
                    hR2.append(sep);
                }

                if (qeif.getExportItemConfig(item).hasPointCol()) {
                    // HeaderRow1
                    hR1.append(sep);
                    // HeaderRow2
                    hR2.append(i);
                    hR2.append("_");
                    hR2.append(translator.translate("item.score"));
                    hR2.append(sep);
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

    protected abstract String createHeaderRow1Intro();

    protected abstract String createHeaderRow2Intro();

    public abstract void openResultSet(QTIExportSet set);

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
                sb.append(eItem.getPositionsOfResponses());
                sb.append(sep);
            }

            if (eItem.hasResult()) {
                if (itemFormatConfig.hasPointCol()) {
                    // points
                    sb.append(eItem.getScore());
                    sb.append(sep);
                }
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
                // points
                if (itemFormatConfig.hasPointCol()) {
                    sb.append(sep);
                }
                // startdatetime, column duration
                if (itemFormatConfig.hasTimeCols()) {
                    sb.append(sep + sep);
                }
            }
        }
    }

    protected boolean displayItem(final QTIExportItemFormatConfig c) {
        return !(!c.hasResponseCols() && !c.hasPointCol() && !c.hasTimeCols() && !c.hasPositionsOfResponsesCol());
    }

    protected String escape(final String s) {
        return s.replaceAll(emb, esc + emb);
    }

    public abstract void closeResultSet();

    public void closeReport() {
        if (qtiItemObjectList == null) {
            throw new OLATRuntimeException(null, "Can not format report when qtiItemObjectList is null", null);
        }
        final String legend = translator.translate("legend");
        sb.append(car + car);
        sb.append(legend);
        sb.append(car + car);
        int y = 1;
        for (final Iterator<QTIItemObject> iter = qtiItemObjectList.iterator(); iter.hasNext();) {
            final QTIItemObject element = iter.next();

            sb.append(element.getItemIdent());
            sb.append(sep);
            sb.append(emb);
            sb.append(escape(element.getItemTitle()));
            sb.append(emb);
            sb.append(car);

            appendMinMaxAndCutValue(element);

            // CELFI#107
            sb.append(sep + sep + sep + sep);
            String question = element.getQuestionText();
            if (tagless) {
                question = FilterFactory.getXSSFilter(-1).filter(question);
                question = FilterFactory.getHtmlTagsFilter().filter(question);
            }
            question = StringHelper.unescapeHtml(question);
            sb.append(question);
            sb.append(car);
            // CELFI#107 END

            final List<String> responseLabelMaterials = element.getResponseLabelMaterials();

            for (int i = 0; i < element.getResponseIdentifier().size(); i++) {
                sb.append(sep + sep);
                sb.append(y);
                sb.append("_");
                sb.append(element.getItemType());
                sb.append(i + 1);
                sb.append(sep);
                sb.append(element.getResponseIdentifier().get(i));
                sb.append(sep);

                if (responseLabelMaterials != null) {
                    String s = responseLabelMaterials.get(i);
                    s = StringHelper.unescapeHtml(s);
                    if (tagless) {
                        s = s.replaceAll("\\<.*?\\>", "");
                    }
                    sb.append(Formatter.stripTabsAndReturns(s));
                }
                sb.append(car);
            }
            y++;
        }

        sb.append(car + car);
        sb.append("SCQ");
        sb.append(sep);
        sb.append("Single Choice Question");
        sb.append(car);
        sb.append("MCQ");
        sb.append(sep);
        sb.append("Multiple Choice Question");
        sb.append(car);
        sb.append("FIB");
        sb.append(sep);
        sb.append("Fill in the blank");
        sb.append(car);
        sb.append("ESS");
        sb.append(sep);
        sb.append("Essay");
        sb.append(car);
        sb.append("KPR");
        sb.append(sep);
        sb.append("Kprim (K-Type)");

        sb.append(car + car);
        sb.append("R:");
        sb.append(sep);
        sb.append("Radio button (SCQ)");
        sb.append(car);
        sb.append("C:");
        sb.append(sep);
        sb.append("Check box (MCQ or KPR)");
        sb.append(car);
        sb.append("B:");
        sb.append(sep);
        sb.append("Blank (FIB)");
        sb.append(car);
        sb.append("A:");
        sb.append(sep);
        sb.append("Area (ESS)");

        sb.append(car + car);
        sb.append("x_Ry");
        sb.append(sep);
        sb.append("Radio Button y of SCQ x, e.g. 1_R1");
        sb.append(car);
        sb.append("x_Cy");
        sb.append(sep);
        sb.append("Check Box y of MCQ x or two Radio Buttons y of KPR x, e.g. 3_C2");
        sb.append(car);
        sb.append("x_By");
        sb.append(sep);
        sb.append("Blank y of FIB x, e.g. 17_B2");
        sb.append(car);
        sb.append("x_Ay");
        sb.append(sep);
        sb.append("Area y of ESS x, e.g. 4_A1");

        sb.append(car + car);
        sb.append("Kprim:");
        sb.append(sep);
        sb.append("'+' = yes");
        sb.append(sep);
        sb.append("'-' = no");
        sb.append(sep);
        sb.append("'.' = no answer");
        sb.append(sep);

    }

    /**
     * @param element
     */
    protected void appendMinMaxAndCutValue(final QTIItemObject element) {
        sb.append(sep + sep);
        sb.append("minValue");
        sb.append(sep);
        sb.append(element.getItemMinValue());
        sb.append(car);

        sb.append(sep + sep);
        sb.append("maxValue");
        sb.append(sep);
        sb.append(element.getItemMaxValue());
        sb.append(car);

        sb.append(sep + sep);
        sb.append("cutValue");
        sb.append(sep);
        sb.append(element.getItemCutValue());
        sb.append(car);
    }

    public String getReport() {
        return sb.toString();
    }

    public String getFileNamePrefix() {
        return fileNamePrefix;
    }

    public int getType() {
        return type;
    }

    /**
     * @param qtiItemObjectList
     */
    public void setQTIItemObjectList(final List<QTIItemObject> qtiItemObjectList) {
        this.qtiItemObjectList = qtiItemObjectList;
    }

    /**
     * @return
     */
    public Map<Class<? extends QTIExportItemFormatConfig>, QTIExportItemFormatConfig> getMapWithExportItemConfigs() {
        return this.exportItemConfig;
    }
}
