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
package org.olat.presentation.admin.statistics;

import java.text.DateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.olat.lms.course.statistic.StatisticUpdateService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.scheduling.quartz.CronTriggerBean;

/**
 * Admin Controller for statistics - similar to the notifications controller.
 * <p>
 * The idea is that you can go on the single-service node to see how the statistic update cronjob is configured. Plus optionally (to be decided) whether you can trigger
 * the statistic update manually.
 * <P>
 * Initial Date: 12.02.2010 <br>
 * 
 * @author Stefan
 */
public class StatisticsAdminController extends BasicController {

    /** the logging object used in this class **/
    private static final Logger log = LoggerHelper.getLogger();

    private static final String STATISTICS_FULL_RECALCULATION_TRIGGER_BUTTON = "statistics.fullrecalculation.trigger.button";
    private static final String STATISTICS_UPDATE_TRIGGER_BUTTON = "statistics.update.trigger.button";

    private final VelocityContainer content;

    private DialogBoxController dialogCtr_;

    public StatisticsAdminController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        content = createVelocityContainer("index");
        LinkFactory.createButton(STATISTICS_FULL_RECALCULATION_TRIGGER_BUTTON, content, this);
        LinkFactory.createButton(STATISTICS_UPDATE_TRIGGER_BUTTON, content, this);

        refreshUIState();

        putInitialPanel(content);
    }

    private void refreshUIState() {
        boolean enabled = false;
        String cronExpression = "";
        if (CoreSpringFactory.containsBean("schedulerFactoryBean")) {
            log.info("refreshUIState: schedulerFactoryBean found");
            final Object schedulerFactoryBean = CoreSpringFactory.getBean("schedulerFactoryBean");
            if (schedulerFactoryBean != null && schedulerFactoryBean instanceof Scheduler) {
                final Scheduler schedulerBean = (Scheduler) schedulerFactoryBean;
                int triggerState;
                try {
                    triggerState = schedulerBean.getTriggerState("updateStatisticsTrigger", null/* trigger group */);
                    enabled = (triggerState != Trigger.STATE_NONE) && (triggerState != Trigger.STATE_ERROR);
                    log.info("refreshUIState: updateStatisticsTrigger state was " + triggerState + ", enabled now: " + enabled);
                } catch (final SchedulerException e) {
                    log.warn("refreshUIState: Got a SchedulerException while asking for the updateStatisticsTrigger's state", e);
                }
            }
            final CronTriggerBean triggerBean = (CronTriggerBean) CoreSpringFactory.getBean("updateStatisticsTrigger");
            final JobDetail jobDetail = triggerBean.getJobDetail();
            enabled &= jobDetail.getName().equals("statistics.job.enabled");
            log.info("refreshUIState: statistics.job.enabled check, enabled now: " + enabled);
            cronExpression = triggerBean.getCronExpression();
            final StatisticUpdateService statisticUpdateManager = getStatisticUpdateManager();
            if (statisticUpdateManager == null) {
                log.info("refreshUIState: statisticUpdateManager not configured");
                enabled = false;
            } else {
                enabled &= statisticUpdateManager.isEnabled();
                log.info("refreshUIState: statisticUpdateManager configured, enabled now: " + enabled);
            }
        } else {
            log.info("refreshUIState: schedulerFactoryBean not found");
        }
        if (enabled) {
            content.contextPut("status", getTranslator().translate("statistics.status.enabled", new String[] { cronExpression }));
        } else {
            content.contextPut("status", getTranslator().translate("statistics.status.disabled"));
        }
        content.contextPut("statisticEnabled", enabled);

        recalcLastUpdated();

        updateStatisticUpdateOngoingFlag();
    }

    private void updateStatisticUpdateOngoingFlag() {
        final StatisticUpdateService statisticUpdateManager = getStatisticUpdateManager();
        if (statisticUpdateManager == null) {
            log.info("event: UpdateStatisticsJob configured, but no StatisticManager available");
            content.contextPut("statisticUpdateOngoing", Boolean.TRUE);
        } else {
            content.contextPut("statisticUpdateOngoing", statisticUpdateManager.updateOngoing());
        }
    }

    private void recalcLastUpdated() {
        try {
            final long lastUpdated = getStatisticUpdateManager().getLastUpdated();
            if (lastUpdated == -1) {
                content.contextPut("lastupdated", getTranslator().translate("statistics.lastupdated.never", null));
            } else {
                final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, getLocale());
                content.contextPut("lastupdated", getTranslator().translate("statistics.lastupdated", new String[] { df.format(new Date(lastUpdated)) }));
            }
        } catch (final Exception e) {
            content.contextPut("lastupdated", getTranslator().translate("statistics.lastupdated", null));
        }
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == dialogCtr_) {
            if (DialogBoxUIFactory.isYesEvent(event)) {
                final StatisticUpdateService statisticUpdateManager = getStatisticUpdateManager();
                if (statisticUpdateManager == null) {
                    log.info("event: UpdateStatisticsJob configured, but no StatisticManager available");
                } else {
                    statisticUpdateManager.updateStatistics(true, getUpdateFinishedCallback());
                    refreshUIState();
                    content.put("updatecontrol", new JSAndCSSComponent("intervall", this.getClass(), null, null, false, null, 3000));
                    getInitialComponent().setDirty(true);
                }
            }
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (STATISTICS_FULL_RECALCULATION_TRIGGER_BUTTON.equals(event.getCommand())) {
            final StatisticUpdateService statisticUpdateManager = getStatisticUpdateManager();
            if (statisticUpdateManager == null) {
                log.info("event: UpdateStatisticsJob configured, but no StatisticManager available");
            } else {

                final String title = getTranslator().translate("statistics.fullrecalculation.really.title");
                final String text = getTranslator().translate("statistics.fullrecalculation.really.text");
                dialogCtr_ = DialogBoxUIFactory.createYesNoDialog(ureq, getWindowControl(), title, text);
                listenTo(dialogCtr_);
                dialogCtr_.activate();

            }
        } else if (STATISTICS_UPDATE_TRIGGER_BUTTON.equals(event.getCommand())) {
            final StatisticUpdateService statisticUpdateManager = getStatisticUpdateManager();
            if (statisticUpdateManager == null) {
                log.info("event: UpdateStatisticsJob configured, but no StatisticManager available");
            } else {
                statisticUpdateManager.updateStatistics(false, getUpdateFinishedCallback());
                refreshUIState();
                content.put("updatecontrol", new JSAndCSSComponent("intervall", this.getClass(), null, null, false, null, 3000));
                getInitialComponent().setDirty(true);
            }
        }
    }

    private Runnable getUpdateFinishedCallback() {
        return new Runnable() {
            @Override
            public void run() {
                final Component updatecontrol = content.getComponent("updatecontrol");
                if (updatecontrol != null) {
                    content.remove(updatecontrol);
                }
                refreshUIState();
                showInfo("statistics.generation.feedback");
                getInitialComponent().setDirty(true);
            }
        };
    }

    /**
     * Returns the StatisticUpdateManager bean (created via spring)
     * 
     * @return the StatisticUpdateManager bean (created via spring)
     */
    private StatisticUpdateService getStatisticUpdateManager() {
        if (CoreSpringFactory.containsBean(StatisticUpdateService.class)) {
            return CoreSpringFactory.getBean(StatisticUpdateService.class);
        } else {
            return null;
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to do
    }

}
