package org.olat.system.coordinate.jms;

/**
 * 
 * Performance Item.
 * 
 * <P>
 * 
 * @author Stefan
 */
public class PerfItem {

    private final String itemName_;
    private final float minTime_;
    private final float maxTime_;
    private final float avgTime_;
    private final float last10AvgTime_;
    private final float last100AvgTime_;
    private final float last1000AvgTime_;
    private final float lastTime_;
    private final float frequency_;
    private final float last10Frequency_;
    private final float last100Frequency_;
    private final float last1000Frequency_;
    private final long count_;

    /**
     * <td>$perf.itemName</td> <td>$perf.minTime</td> <td>$perf.maxTime</td> <td>$perf.avgTime</td> <td>$perf.lastTime</td> <td>$perf.count</td>
     */
    public PerfItem(final String itemName, final float minTime, final float maxTime, final float lastTime, final float avgTime, final float last10AvgTime,
            final float last100AvgTime, final float last1000AvgTime, final float frequency, final float last10Frequency, final float last100Frequency,
            final float last1000Frequency, final long count) {
        itemName_ = itemName;
        minTime_ = minTime;
        maxTime_ = maxTime;
        lastTime_ = lastTime;
        avgTime_ = avgTime;
        last10AvgTime_ = last10AvgTime;
        last100AvgTime_ = last100AvgTime;
        last1000AvgTime_ = last1000AvgTime;
        frequency_ = frequency;
        last10Frequency_ = last10Frequency;
        last100Frequency_ = last100Frequency;
        last1000Frequency_ = last1000Frequency;
        count_ = count;
    }

    public String getItemName() {
        return itemName_;
    }

    public String getMinTime() {
        if (minTime_ < 0) {
            return "";
        }
        return String.valueOf(minTime_);
    }

    public String getMaxTime() {
        if (maxTime_ < 0) {
            return "";
        }
        return String.valueOf(maxTime_);
    }

    public String getAvgTime() {
        if (avgTime_ < 0) {
            return "";
        }
        return String.valueOf(avgTime_);
    }

    public String getLast10AvgTime() {
        if (last10AvgTime_ < 0) {
            return "";
        }
        return String.valueOf(last10AvgTime_);
    }

    public String getLast100AvgTime() {
        if (last100AvgTime_ < 0) {
            return "";
        }
        return String.valueOf(last100AvgTime_);
    }

    public String getLast1000AvgTime() {
        if (last1000AvgTime_ < 0) {
            return "";
        }
        return String.valueOf(last1000AvgTime_);
    }

    public String getLastTime() {
        if (lastTime_ < 0) {
            return "";
        }
        return String.valueOf(lastTime_);
    }

    public String getCount() {
        if (count_ < 0) {
            return "";
        }
        return String.valueOf(count_);
    }

    public String getFrequency() {
        if (frequency_ <= 0) {
            return "";
        }
        return String.valueOf((float) Math.round(frequency_ * 100) / 100);
    }

    public String getLast10Frequency() {
        if (last10Frequency_ < 0) {
            return "";
        }
        return String.valueOf((float) Math.round(last10Frequency_ * 100) / 100);
    }

    public String getLast100Frequency() {
        if (last100Frequency_ < 0) {
            return "";
        }
        return String.valueOf((float) Math.round(last100Frequency_ * 100) / 100);
    }

    public String getLast1000Frequency() {
        if (last1000Frequency_ < 0) {
            return "";
        }
        return String.valueOf((float) Math.round(last1000Frequency_ * 100) / 100);
    }

}
