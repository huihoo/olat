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
import java.util.Locale;
import java.util.Map;

import org.olat.lms.ims.qti.exporter.helper.IdentityAnonymizerCallback;
import org.olat.system.commons.Formatter;

/**
 * Initial Date: May 23, 2006 <br>
 * 
 * @author Alexander Schneider
 */
public class QTIExportFormatterCSVType2 extends QTIExportFormatter {

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

    public QTIExportFormatterCSVType2(final Locale locale, final IdentityAnonymizerCallback anonymizerCallback,
            final Map<Class<? extends QTIExportItemFormatConfig>, QTIExportItemFormatConfig> exportItemConfig, final String sep, final String emb, final String esc,
            final String car, final boolean tagless) {
        super(locale, anonymizerCallback, exportItemConfig);
        this.sep = sep;
        this.emb = emb;
        this.esc = esc;
        this.car = car;
        this.tagless = tagless;

        fileNamePrefix = "SELF_";
        type = 2;
    }

    @Override
    public void openResultSet(final QTIExportSet set) {
        String instUsrIdent = set.getInstitutionalUserIdentifier();
        if (instUsrIdent == null) {
            instUsrIdent = translator.translate("column.field.notavailable");
        }
        final float assessPoints = set.getScore();

        final Long key = set.getIdentity().getKey();
        if (!key.equals(keyBefore)) {
            loop_counter++;
            keyBefore = key;
        }
        if (anonymizerCallback == null) {
            sb.append(row_counter);
        } else {
            sb.append(anonymizerCallback.getAnonymizedUserName(set.getIdentity()));
        }
        sb.append(sep);
        sb.append(loop_counter);
        sb.append(sep);
        sb.append(assessPoints);
        sb.append(sep);

        // datatime
        final Date date = set.getLastModified();
        sb.append(Formatter.formatDatetime(date));
        sb.append(sep);

        final Long assessDuration = set.getDuration();
        // since there are resultsets created before alter table adding the field duration
        if (assessDuration != null) {
            sb.append(Math.round(assessDuration.longValue() / 1000));
        } else {
            sb.append("n/a");
        }
        sb.append(sep);
    }

    @Override
    public void closeResultSet() {
        sb.append(car);
        row_counter++;
    }

    @Override
    protected String createHeaderRow1Intro() {
        return sep + sep + sep + sep + sep;
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

        final String num = translator.translate("column.header.number");
        final String assessPoint = translator.translate("column.header.assesspoints");
        final String date = translator.translate("column.header.date");
        final String duration = translator.translate("column.header.duration");

        hr2Intro.append(sequentialNumber);
        hr2Intro.append(sep);
        hr2Intro.append(num);
        hr2Intro.append(sep);
        hr2Intro.append(assessPoint);
        hr2Intro.append(sep);
        hr2Intro.append(date);
        hr2Intro.append(sep);
        hr2Intro.append(duration);
        hr2Intro.append(sep);
        return hr2Intro.toString();
    }

    public void setKeyBefore(final Long keyBefore) {
        this.keyBefore = keyBefore;
    }

}
