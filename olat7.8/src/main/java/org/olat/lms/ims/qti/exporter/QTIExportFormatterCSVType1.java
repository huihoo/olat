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

import org.olat.system.commons.Formatter;

/**
 * Export formatter for 'Test' course nodes (IQTESTCourseNode)
 * 
 * Initial Date: May 23, 2006 <br>
 * 
 * @author Alexander Schneider
 * @author oliver.buehler@agility-informatik.ch
 */
public class QTIExportFormatterCSVType1 extends QTIExportFormatter {

    private final boolean anonymized;

    private int row_counter = 1;

    /**
     * 
     * @param locale
     * @param type
     * @param anonymizerCallback
     * @param delimiter
     * @param Map
     *            qtiExportFormatConfig with (QTIExportItemXYZ.class,IQTIExportItemFormatConfig)
     */

    public QTIExportFormatterCSVType1(final Locale locale, final Map<Class<? extends QTIExportItemFormatConfig>, QTIExportItemFormatConfig> exportItemConfig,
            final String sep, final String emb, final String esc, final String car, final boolean tagless, final boolean anonymized) {
        super(locale, null, exportItemConfig);
        this.sep = sep;
        this.emb = emb;
        this.esc = esc;
        this.car = car;
        this.tagless = tagless;
        this.anonymized = anonymized;

        fileNamePrefix = "TEST_";
        type = 1;
    }

    @Override
    public void openResultSet(final QTIExportSet set) {

        final String firstName = set.getFirstName();
        final String lastName = set.getLastName();
        final String login = set.getLogin();
        String instUsrIdent = set.getInstitutionalUserIdentifier();
        if (instUsrIdent == null) {
            instUsrIdent = translator.translate("column.field.notavailable");
        }
        final float assessPoints = set.getScore();
        final boolean isPassed = set.getIsPassed();

        sb.append(row_counter);
        sb.append(sep);
        sb.append(anonymized ? "-" : lastName);
        sb.append(sep);
        sb.append(anonymized ? "-" : firstName);
        sb.append(sep);
        sb.append(anonymized ? "-" : login);
        sb.append(sep);
        sb.append(anonymized ? "-" : instUsrIdent);
        sb.append(sep);
        sb.append(assessPoints);
        sb.append(sep);
        sb.append(isPassed);
        sb.append(sep);
        sb.append(set.getIp());
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
        return sep + sep + sep + sep + sep + sep + sep + sep + sep + sep;
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

        final String lastName = translator.translate("column.header.name");
        final String firstName = translator.translate("column.header.vorname");
        final String login = translator.translate("column.header.login");
        final String instUsrIdent = translator.translate("column.header.instUsrIdent");
        final String assessPoint = translator.translate("column.header.assesspoints");
        final String passed = translator.translate("column.header.passed");
        final String ipAddress = translator.translate("column.header.ipaddress");
        final String date = translator.translate("column.header.date");
        final String duration = translator.translate("column.header.duration");

        hr2Intro.append(sequentialNumber);
        hr2Intro.append(sep);
        hr2Intro.append(lastName);
        hr2Intro.append(sep);
        hr2Intro.append(firstName);
        hr2Intro.append(sep);
        hr2Intro.append(login);
        hr2Intro.append(sep);
        hr2Intro.append(instUsrIdent);
        hr2Intro.append(sep);
        hr2Intro.append(assessPoint);
        hr2Intro.append(sep);
        hr2Intro.append(passed);
        hr2Intro.append(sep);
        hr2Intro.append(ipAddress);
        hr2Intro.append(sep);
        hr2Intro.append(date);
        hr2Intro.append(sep);
        hr2Intro.append(duration);
        hr2Intro.append(sep);

        return hr2Intro.toString();
    }

}
