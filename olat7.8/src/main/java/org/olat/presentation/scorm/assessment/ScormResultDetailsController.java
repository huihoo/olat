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
 * Copyright (c) 2009 frentix GmbH, Switzerland<br>
 * <p>
 */
package org.olat.presentation.scorm.assessment;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olat.lms.course.nodes.ScormCourseNode;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.scorm.assessment.CmiData;
import org.olat.lms.scorm.assessment.ScormAssessmentManager;
import org.olat.lms.scorm.server.servermodels.ScoUtils;
import org.olat.lms.scorm.server.servermodels.SequencerModel;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.table.BaseTableDataModelWithoutFilter;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.StaticColumnDescriptor;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableDataModel;
import org.olat.presentation.framework.core.components.table.TableEvent;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * Show the assessment's details of SCORM course for a selected user. A popup shows all the CMI datas of the SCOs visited by the user
 * <P>
 * Initial Date: 17 août 2009 <br>
 * 
 * @author srosse
 */
public class ScormResultDetailsController extends BasicController {

    private static final String CMI_RAW_SCORE = "cmi.core.score.raw";
    private static final String CMI_TOTAL_TIME = "cmi.core.total_time";

    private VelocityContainer main;
    private TableController summaryTableCtr;
    private TableController cmiTableCtr;
    private CloseableModalController cmc;

    private final ScormCourseNode node;
    private final UserCourseEnvironment userCourseEnvironment;

    private List<CmiData> rawDatas;
    private SequencerModel sequencerModel;

    public ScormResultDetailsController(final UserRequest ureq, final WindowControl wControl, final ScormCourseNode node,
            final UserCourseEnvironment userCourseEnvironment) {
        super(ureq, wControl);

        this.node = node;
        this.userCourseEnvironment = userCourseEnvironment;
        init(ureq);
    }

    protected void init(final UserRequest ureq) {
        main = createVelocityContainer("scores");

        final TableGuiConfiguration summaryTableConfig = new TableGuiConfiguration();
        summaryTableConfig.setDownloadOffered(true);

        summaryTableCtr = new TableController(summaryTableConfig, ureq, getWindowControl(), getTranslator());
        summaryTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("summary.column.header.date", 0, null, ureq.getLocale()));
        summaryTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("summary.column.header.duration", 1, null, ureq.getLocale()));
        summaryTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("summary.column.header.assesspoints", 2, null, ureq.getLocale()));
        summaryTableCtr.addColumnDescriptor(new StaticColumnDescriptor("sel", "summary.column.header.details", getTranslator().translate("select")));

        final CourseEnvironment courseEnv = userCourseEnvironment.getCourseEnvironment();

        final String username = userCourseEnvironment.getIdentityEnvironment().getIdentity().getName();
        rawDatas = ScormAssessmentManager.getInstance().visitScoDatas(username, courseEnv, node);
        sequencerModel = ScormAssessmentManager.getInstance().getSequencerModel(username, courseEnv, node);

        summaryTableCtr.setTableDataModel(getSummaryTableDataModel(ureq, rawDatas, sequencerModel));
        listenTo(summaryTableCtr);

        main.put("summary", summaryTableCtr.getInitialComponent());

        putInitialPanel(main);
    }

    protected TableDataModel getSummaryTableDataModel(final UserRequest ureq, final List<CmiData> datas, final SequencerModel sequenceModel) {
        final SummaryTableDataModel model = new SummaryTableDataModel();

        double score = 0;
        String totalTime = null;
        for (final CmiData data : datas) {
            final String key = data.getKey();
            if (CMI_RAW_SCORE.equals(key)) {
                final String value = data.getValue();
                if (StringHelper.containsNonWhitespace(value)) {
                    try {
                        score += Double.parseDouble(value);
                    } catch (final NumberFormatException e) {
                        // fail silently
                    }
                }
            } else if (CMI_TOTAL_TIME.equals(key)) {
                final String value = data.getValue();
                if (StringHelper.containsNonWhitespace(value)) {
                    if (totalTime == null) {
                        totalTime = value;
                    } else {
                        totalTime = ScoUtils.addTimes(totalTime, value);
                    }
                }
            }
        }
        model.setScore(Double.toString(score));
        model.setTotalTime(totalTime);

        final String modifiedDateMs = sequenceModel == null ? "" : sequenceModel.getManifestModifiedDate();
        if (StringHelper.containsNonWhitespace(modifiedDateMs)) {
            final long timestamp = Long.parseLong(modifiedDateMs);
            final Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(timestamp);
            final Date lastModificationDate = cal.getTime();

            final DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, ureq.getLocale());
            final String formattedDate = format.format(lastModificationDate);
            model.setLastModificationDate(formattedDate);
        }

        return model;
    }

    @Override
    protected void doDispose() {
        //
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        //
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == summaryTableCtr) {
            final TableEvent tEvent = (TableEvent) event;
            if (tEvent.getActionId().equals("sel")) {

                final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
                tableConfig.setPreferencesOffered(true, "scormAssessmentDetails");

                removeAsListenerAndDispose(cmiTableCtr);
                cmiTableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
                listenTo(cmiTableCtr);

                cmiTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("cmis.column.header.itemId", 0, null, ureq.getLocale()));
                cmiTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("cmis.column.header.translatedKey", 1, null, ureq.getLocale()));
                cmiTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("cmis.column.header.key", 2, null, ureq.getLocale()));
                cmiTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("cmis.column.header.value", 3, null, ureq.getLocale()));

                cmiTableCtr.setTableDataModel(new CmiTableDataModel(getTranslator(), rawDatas));

                removeAsListenerAndDispose(cmc);
                cmc = new CloseableModalController(getWindowControl(), translate("close"), cmiTableCtr.getInitialComponent());
                listenTo(cmc);

                cmc.activate();
            }
        }
    }

    public class SummaryTableDataModel extends BaseTableDataModelWithoutFilter implements TableDataModel {
        private String score;
        private String totalTime;
        private String lastModificationDate;

        public SummaryTableDataModel() {
            //
        }

        public String getScore() {
            return score;
        }

        public void setScore(final String score) {
            this.score = score;
        }

        public String getTotalTime() {
            return totalTime;
        }

        public void setTotalTime(final String totalTime) {
            this.totalTime = totalTime;
        }

        public String getLastModificationDate() {
            return lastModificationDate;
        }

        public void setLastModificationDate(final String lastModificationDate) {
            this.lastModificationDate = lastModificationDate;
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public int getRowCount() {
            return 1;
        }

        @Override
        public Object getValueAt(final int row, final int col) {
            switch (col) {
            case 0:
                return lastModificationDate;
            case 1:
                return totalTime;
            case 2:
                return score;
            default:
                return "ERROR";
            }
        }
    }

    public class CmiTableDataModel extends BaseTableDataModelWithoutFilter implements TableDataModel {
        private final List<CmiData> datas;
        private final Translator translator;
        private final Pattern pattern = Pattern.compile("[0-9]");

        public CmiTableDataModel(final Translator translator, final List<CmiData> datas) {
            this.datas = datas;
            this.translator = translator;
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public int getRowCount() {
            return datas.size();
        }

        @Override
        public Object getValueAt(final int row, final int col) {
            final CmiData data = datas.get(row);
            switch (col) {
            case 0:
                return data.getItemId();
            case 1:
                String key = data.getKey();
                String translation;

                try {
                    final Matcher matcher = pattern.matcher(key);
                    if (matcher.find()) {
                        final String pos = key.substring(matcher.start(), matcher.end());
                        if (matcher.find()) {
                            final String pos2 = key.substring(matcher.start(), matcher.end());
                            key = key.replace(pos + ".", "").replace(pos2 + ".", "");
                            translation = translator.translate(key, new String[] { pos, pos2 });
                        } else {
                            key = key.replace(pos + ".", "");
                            translation = translator.translate(key, new String[] { pos });
                        }
                    } else {
                        translation = translator.translate(key);
                    }
                    if (translation == null || translation.length() > 150) {
                        return data.getKey();
                    }
                    return translation;
                } catch (final Exception ex) {
                    return key;
                }
            case 2:
                return data.getKey();
            case 3:
                return data.getValue();
            default:
                return "ERROR";
            }
        }
    }
}
