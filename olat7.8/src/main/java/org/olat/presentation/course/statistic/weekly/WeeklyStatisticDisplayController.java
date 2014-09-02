package org.olat.presentation.course.statistic.weekly;

import org.apache.log4j.Logger;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.statistic.IStatisticManager;
import org.olat.lms.course.statistic.StatisticResult;
import org.olat.presentation.course.statistic.DateChooserForm;
import org.olat.presentation.course.statistic.StatisticDisplayController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;

public class WeeklyStatisticDisplayController extends StatisticDisplayController {

    /** the logging object used in this class **/
    private static final Logger log = LoggerHelper.getLogger();

    private VelocityContainer weeklyStatisticFormVc_;
    private VelocityContainer weeklyStatisticVc_;
    private DateChooserForm form_;

    public WeeklyStatisticDisplayController(final UserRequest ureq, final WindowControl windowControl, final ICourse course, final IStatisticManager statisticManager) {
        super(ureq, windowControl, course, statisticManager);
    }

    @Override
    protected Component createInitialComponent(final UserRequest ureq) {
        setVelocityRoot(PackageUtil.getPackageVelocityRoot(getClass()));

        weeklyStatisticVc_ = this.createVelocityContainer("weeklystatisticparent");

        weeklyStatisticFormVc_ = this.createVelocityContainer("weeklystatisticform");
        form_ = new DateChooserForm(ureq, getWindowControl(), 8 * 7);
        listenTo(form_);
        weeklyStatisticFormVc_.put("statisticForm", form_.getInitialComponent());
        weeklyStatisticFormVc_.contextPut("statsSince", getStatsSinceStr(ureq));

        weeklyStatisticVc_.put("weeklystatisticform", weeklyStatisticFormVc_);

        final Component parentInitialComponent = super.createInitialComponent(ureq);
        weeklyStatisticVc_.put("statistic", parentInitialComponent);

        return weeklyStatisticVc_;
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == form_ && event == Event.DONE_EVENT) {
            // need to regenerate the statisticResult
            // and now recreate the table controller
            recreateTableController(ureq);
        }
        super.event(ureq, source, event);
    }

    @Override
    protected StatisticResult recalculateStatisticResult(final UserRequest ureq) {
        // recalculate the statistic result based on the from and to dates.
        // do this by going via sql (see WeeklyStatisticManager)
        final IStatisticManager weeklyStatisticManager = getStatisticManager();
        final StatisticResult statisticResult = weeklyStatisticManager.generateStatisticResult(ureq.getLocale(), getCourse(), getCourseRepositoryEntryKey(),
                form_.getFromDate(), form_.getToDate());
        return statisticResult;
    }
}
