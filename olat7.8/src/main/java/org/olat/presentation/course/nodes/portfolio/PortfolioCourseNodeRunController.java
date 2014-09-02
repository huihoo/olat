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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.presentation.course.nodes.portfolio;

import java.util.Date;

import org.apache.log4j.Logger;
import org.olat.data.portfolio.structure.EPStructuredMap;
import org.olat.data.portfolio.structure.PortfolioStructureMap;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.course.CourseModule;
import org.olat.lms.course.nodes.AssessableCourseNode;
import org.olat.lms.course.nodes.PortfolioCourseNode;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.lms.portfolio.EPLoggingAction;
import org.olat.presentation.course.nodes.ms.MSCourseNodeRunController;
import org.olat.presentation.course.nodes.portfolio.PortfolioCourseNodeConfiguration.DeadlineType;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.Windows;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.dtabs.DTabs;
import org.olat.presentation.home.site.HomeSite;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

import com.ibm.icu.util.Calendar;

/**
 * Description:<br>
 * Portfolio run controller. You can take a map if you are in some learning groups of the course. The controller check if there is a deadline for the map and if yes, set
 * it.
 * <P>
 * Initial Date: 6 oct. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PortfolioCourseNodeRunController extends BasicController {
    private static final Logger log = LoggerHelper.getLogger();
    private final EPFrontendManager ePFMgr;
    private final PortfolioCourseNode courseNode;
    private final ModuleConfiguration config;
    private final UserCourseEnvironment userCourseEnv;
    private final OLATResourceable courseOres;

    private PortfolioStructureMap copy;
    private PortfolioStructureMap template;

    private MSCourseNodeRunController scoringController;

    private Link newMapLink;
    private Link selectMapLink;

    private VelocityContainer vcContainer;

    public PortfolioCourseNodeRunController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv, final NodeEvaluation ne,
            final PortfolioCourseNode courseNode) {
        super(ureq, wControl);

        this.courseNode = courseNode;
        this.config = courseNode.getModuleConfiguration();
        this.userCourseEnv = userCourseEnv;

        final Long courseResId = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
        courseOres = OresHelper.createOLATResourceableInstance(CourseModule.class, courseResId);

        ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean(EPFrontendManager.class);

        final RepositoryEntry mapEntry = courseNode.getReferencedRepositoryEntry();
        if (mapEntry != null) {
            template = (PortfolioStructureMap) ePFMgr.loadPortfolioStructure(mapEntry.getOlatResource());
        }

        vcContainer = createVelocityContainer("run");
        displayPortfolioConfig(ureq);

        putInitialPanel(vcContainer);
    }

    private void displayPortfolioConfig(UserRequest ureq) {
        final Object text = config.get(PortfolioCourseNodeConfiguration.NODE_TEXT);
        final String explanation = (text instanceof String) ? (String) text : "";
        vcContainer.contextPut("explanation", explanation);

        // deadline config
        final String deadlineType = (String) config.get(PortfolioCourseNodeConfiguration.DEADLINE_TYPE);
        if (deadlineType != null && !DeadlineType.none.name().equals(deadlineType)) {
            vcContainer.contextPut("deadlineType", deadlineType);

            // show deadline-config
            String deadlineInfo = "";
            if (deadlineType.equals(DeadlineType.absolut.name())) {
                final Formatter f = Formatter.getInstance(getLocale());
                deadlineInfo = f.formatDate((Date) config.get(PortfolioCourseNodeConfiguration.DEADLINE_DATE));
            } else {
                deadlineInfo = getDeadlineRelativeInfo();
            }
            vcContainer.contextPut("deadlineType", deadlineType);
            vcContainer.contextPut("deadlineInfo", deadlineInfo);
        }

        if (template != null) {
            displayPortfolioStatus(ureq);
        }
    }

    private void displayPortfolioStatus(UserRequest ureq) {
        vcContainer.contextRemove("portfolioTaskAvailable");

        final Formatter formatter = Formatter.getInstance(ureq.getLocale());
        copy = ePFMgr.loadPortfolioStructureMap(getIdentity(), template, courseOres, courseNode.getIdent(), null);
        if (copy == null) {
            vcContainer.contextPut("portfolioTaskAvailable", true);
            final String title = template.getTitle();
            final String msg = translate("map.available", new String[] { title });
            vcContainer.contextPut("newMapText", msg);

            if (newMapLink == null) {
                newMapLink = LinkFactory.createButton("availableTaskLink", vcContainer, this);
                newMapLink.setCustomDisplayText(translate("map.new"));
            }
        } else {
            final EPStructuredMap structuredMap = (EPStructuredMap) copy;

            // show absolute deadline when task is taken. nothing if taken map still has a deadline configured.
            if (structuredMap.getDeadLine() != null) {
                final String deadline = formatter.formatDateAndTime(structuredMap.getDeadLine());
                vcContainer.contextPut("deadlineType", DeadlineType.absolut);
                vcContainer.contextPut("deadlineInfo", deadline);
            }

            // display link
            if (selectMapLink == null) {
                selectMapLink = LinkFactory.createLink("collectedTaskLink", vcContainer, this);
                selectMapLink.setCustomDisplayText(copy.getTitle());
            }

            // show copied date
            if (structuredMap.getCopyDate() != null) {
                String copyDate = formatter.formatDateAndTime(structuredMap.getCopyDate());
                vcContainer.contextPut("copyDate", copyDate);
            }

            // show return date and result
            if (structuredMap.getReturnDate() != null) {
                String returnDate = formatter.formatDateAndTime(structuredMap.getReturnDate());
                vcContainer.contextPut("returnDate", returnDate);

                if (courseNode.hasPassedConfigured()) {
                    scoringController = new MSCourseNodeRunController(ureq, getWindowControl(), userCourseEnv, (AssessableCourseNode) courseNode, false);
                    vcContainer.put("scoringController", scoringController.getInitialComponent());
                    vcContainer.contextPut("hasScoring", Boolean.TRUE);
                }
            }
        }

        if (selectMapLink != null) {
            selectMapLink.setVisible(copy != null);
        }
        if (newMapLink != null) {
            newMapLink.setVisible(copy == null);
        }
    }

    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        if (source == newMapLink) {
            copy = ePFMgr.assignStructuredMapToUser(getIdentity(), template, courseOres, courseNode.getIdent(), null, getDeadline());
            if (copy != null) {
                showInfo("map.copied", template.getTitle());
                ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapPortfolioOres(copy));
                ThreadLocalUserActivityLogger.log(EPLoggingAction.EPORTFOLIO_TASK_STARTED, getClass());
            }
            displayPortfolioStatus(ureq);
        } else if (source == selectMapLink) {
            final String activationCmd = copy.getClass().getSimpleName() + ":" + copy.getResourceableId();
            final DTabs dts = Windows.getWindows(ureq).getWindow(ureq).getDynamicTabs();
            dts.activateStatic(ureq, HomeSite.class.getName(), activationCmd);
        }
    }

    @Override
    protected void doDispose() {
        if (scoringController != null) {
            scoringController.dispose();
        }
    }

    private String getDeadlineRelativeInfo() {
        final String[] args = new String[3];
        final String month = (String) config.get(PortfolioCourseNodeConfiguration.DEADLINE_MONTH);
        if (StringHelper.containsNonWhitespace(month)) {
            args[0] = translate("map.deadline.info.month", month);
        } else {
            args[0] = "";
        }
        final String week = (String) config.get(PortfolioCourseNodeConfiguration.DEADLINE_WEEK);
        if (StringHelper.containsNonWhitespace(week)) {
            args[1] = translate("map.deadline.info.week", week);
        } else {
            args[1] = "";
        }
        final String day = (String) config.get(PortfolioCourseNodeConfiguration.DEADLINE_DAY);
        if (StringHelper.containsNonWhitespace(day)) {
            args[2] = translate("map.deadline.info.day", day);
        } else {
            args[2] = "";
        }
        final String deadLineInfo = translate("map.deadline.info", args);
        return deadLineInfo;
    }

    private Date getDeadline() {
        final String type = (String) config.get(PortfolioCourseNodeConfiguration.DEADLINE_TYPE);
        if (StringHelper.containsNonWhitespace(type)) {
            switch (DeadlineType.valueOf(type)) {
            case none:
                return null;
            case absolut:
                final Date date = (Date) config.get(PortfolioCourseNodeConfiguration.DEADLINE_DATE);
                return date;
            case relative:
                final Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                boolean applied = applyRelativeToDate(cal, PortfolioCourseNodeConfiguration.DEADLINE_MONTH, Calendar.MONTH, 1);
                applied |= applyRelativeToDate(cal, PortfolioCourseNodeConfiguration.DEADLINE_WEEK, Calendar.DATE, 7);
                applied |= applyRelativeToDate(cal, PortfolioCourseNodeConfiguration.DEADLINE_DAY, Calendar.DATE, 1);
                if (applied) {
                    return cal.getTime();
                }
                return null;
            default:
                return null;
            }
        }
        return null;
    }

    private boolean applyRelativeToDate(final Calendar cal, final String time, final int calTime, final int factor) {
        final String t = (String) config.get(time);
        if (StringHelper.containsNonWhitespace(t)) {
            int timeToApply;
            try {
                timeToApply = Integer.parseInt(t) * factor;
            } catch (final NumberFormatException e) {
                log.warn("Not a number: " + t, e);
                return false;
            }
            cal.add(calTime, timeToApply);
            return true;
        }
        return false;
    }
}
