package org.olat.presentation.course.statistic.daily;

import org.apache.log4j.Logger;
import org.olat.data.course.statistic.daily.DailyStatisticDaoImpl;
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

public class DailyStatisticDisplayController extends StatisticDisplayController {

    /** the logging object used in this class **/
    private static final Logger log = LoggerHelper.getLogger();

    private VelocityContainer dailyStatisticFormVc_;
    private VelocityContainer dailyStatisticVc_;
    private DateChooserForm form_;

    public DailyStatisticDisplayController(final UserRequest ureq, final WindowControl windowControl, final ICourse course, final IStatisticManager statisticManager) {
        super(ureq, windowControl, course, statisticManager);
    }

    @Override
    protected Component createInitialComponent(final UserRequest ureq) {
        setVelocityRoot(PackageUtil.getPackageVelocityRoot(getClass()));

        dailyStatisticVc_ = this.createVelocityContainer("dailystatisticparent");

        dailyStatisticFormVc_ = this.createVelocityContainer("dailystatisticform");
        form_ = new DateChooserForm(ureq, getWindowControl(), 7);
        listenTo(form_);
        dailyStatisticFormVc_.put("statisticForm", form_.getInitialComponent());
        dailyStatisticFormVc_.contextPut("statsSince", getStatsSinceStr(ureq));

        dailyStatisticVc_.put("dailystatisticform", dailyStatisticFormVc_);

        final Component parentInitialComponent = super.createInitialComponent(ureq);
        dailyStatisticVc_.put("statistic", parentInitialComponent);

        return dailyStatisticVc_;
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == form_ && event == Event.DONE_EVENT) {
            // need to regenerate the statisticResult

            if (!(getStatisticManager() instanceof DailyStatisticDaoImpl)) {
                // should not occur - config error!
                showWarning("datechooser.error");
                return;
            }

            // and now recreate the table controller
            recreateTableController(ureq);
        }
        super.event(ureq, source, event);
    }

    @Override
    protected StatisticResult recalculateStatisticResult(final UserRequest ureq) {
        // recalculate the statistic result based on the from and to dates.
        // do this by going via sql (see DailyStatisticManager)
        final DailyStatisticDaoImpl dailyStatisticManager = (DailyStatisticDaoImpl) getStatisticManager();
        final StatisticResult statisticResult = dailyStatisticManager.generateStatisticResult(ureq.getLocale(), getCourse(), getCourseRepositoryEntryKey(),
                form_.getFromDate(), form_.getToDate());
        return statisticResult;
    }
}
