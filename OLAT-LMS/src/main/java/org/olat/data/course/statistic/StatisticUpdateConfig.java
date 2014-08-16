package org.olat.data.course.statistic;

import java.util.List;

/**
 * Spring helper class to manage different SQL flavours
 * <P>
 * Initial Date: 01.03.2010 <br>
 * 
 * @author Stefan
 */
public class StatisticUpdateConfig {

    private List<StatisticUpdaterDao> updaters_;

    protected StatisticUpdateConfig() {
        // nothing to be done here
    }

    public void setUpdaters(final List<StatisticUpdaterDao> updaters) {
        updaters_ = updaters;
    }

    public List<StatisticUpdaterDao> getUpdaters() {
        return updaters_;
    }

}
