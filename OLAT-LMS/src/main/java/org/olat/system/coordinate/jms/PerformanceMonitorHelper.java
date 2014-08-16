package org.olat.system.coordinate.jms;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.olat.testutils.codepoints.client.CodepointClient;
import org.olat.testutils.codepoints.client.CommunicationException;
import org.olat.testutils.codepoints.client.Probe;
import org.olat.testutils.codepoints.client.StatId;
import org.olat.testutils.codepoints.server.Codepoint;

/**
 * PerformanceMonitorHelper based on codepoints. <br/>
 * 
 * Warning: contains hard-coded paths to classes and codepoints in these classes. Check if the codepoint names still correct after refactoring the referenced classes!
 * 
 * <P>
 * 
 * @author Stefan
 */
public class PerformanceMonitorHelper {

    private static boolean isStarted_ = false;

    private static CodepointClient codepointClient = null;

    private static Map<String, Probe> probes_ = new HashMap<String, Probe>();

    public static synchronized boolean isStarted() {
        return isStarted_;
    }

    public static synchronized List<PerfItem> getPerfItems() {
        if (!isStarted_ || (codepointClient == null)) {
            return new LinkedList<PerfItem>();
        }
        final List<PerfItem> ll = new LinkedList<PerfItem>();
        try {
            ll.add(new PerfItem("DBImpl session initialize initialize", 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, codepointClient.getCodepoint(
                    "org.olat.data.commons.database.DBImpl.initializeSession").getHitCount()));
            ll.add(new PerfItem("DBImpl session initialize close", 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, codepointClient.getCodepoint(
                    "org.olat.data.commons.database.DBImpl.closeSession").getHitCount()));
            ll.add(new PerfItem("ClusterCacher received event", 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, codepointClient.getCodepoint(
                    "org.olat.system.coordinate.cache.cluster.ClusterCacher.event").getHitCount()));
            ll.add(new PerfItem("ClusterCacher invalidate cache entry", 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, codepointClient.getCodepoint(
                    "org.olat.system.coordinate.cache.cluster.ClusterCacher.invalidateKeys").getHitCount()));
            ll.add(new PerfItem("ClusterCacher send 'invalidate cache entry' event", 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, codepointClient.getCodepoint(
                    "org.olat.system.coordinate.cache.cluster.ClusterCacher.sendChangedKeys").getHitCount()));

            final Set<Entry<String, Probe>> s = probes_.entrySet();
            for (final Iterator<Entry<String, Probe>> it = s.iterator(); it.hasNext();) {
                final Entry<String, Probe> entry = it.next();
                final Probe p = entry.getValue();
                ll.add(new PerfItem(entry.getKey(), p.getStatValue(StatId.MIN_TIME_ELAPSED), p.getStatValue(StatId.MAX_TIME_ELAPSED), p
                        .getStatValue(StatId.LAST_TIME_ELAPSED), p.getStatValue(StatId.TOTAL_AVERAGE_TIME_ELAPSED), p
                        .getStatValue(StatId.LAST_10MEASUREMENT_AVERAGE_TIME_ELAPSED), p.getStatValue(StatId.LAST_100MEASUREMENT_AVERAGE_TIME_ELAPSED), p
                        .getStatValue(StatId.LAST_1000MEASUREMENT_AVERAGE_TIME_ELAPSED), p.getStatValue(StatId.TOTAL_FREQUENCY),
                        p.getStatValue(StatId.LAST_10_FREQUENCY), p.getStatValue(StatId.LAST_100_FREQUENCY), p.getStatValue(StatId.LAST_1000_FREQUENCY), p.getStart()
                                .getHitCount()));
            }
        } catch (final CommunicationException e) {
            ll.add(new PerfItem("Codepoint problem - no stats available there", -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1));
        }
        return ll;
    }

    public static synchronized boolean toggleStartStop() {
        if (isStarted_) {
            // then stop
            isStarted_ = false;
            for (final Iterator<Probe> it = probes_.values().iterator(); it.hasNext();) {
                final Probe p = it.next();
                try {
                    p.close();
                } catch (final CommunicationException e) {
                    // ignore
                }
            }
            probes_.clear();
            codepointClient.close();
            codepointClient = null;
            return true;
        } else {
            // then start
            isStarted_ = true;
            try {
                codepointClient = Codepoint.getLocalLoopCodepointClient();
            } catch (final RuntimeException re) {
                isStarted_ = false;
                return false;
            }
            try {
                codepointClient.setAllHitCounts(0);
                probes_.put("ClusterSyncer.doInSync", codepointClient.startProbingBetween(
                        "org.olat.data.coordinate.ClusterSyncer.doInSync-before-sync.org.olat.data.coordinate.ClusterSyncer.doInSync",
                        "org.olat.data.coordinate.ClusterSyncer.doInSync-in-sync.org.olat.data.coordinate.ClusterSyncer.doInSync"));
                probes_.put("DBImpl session initialize->close", codepointClient.startProbingBetween("org.olat.data.commons.database.DBImpl.initializeSession",
                        "org.olat.data.commons.database.DBImpl.closeSession"));
                probes_.put("DispatcherAction.execute", codepointClient.startProbingBetween("org.olat.presentation.framework.dispatcher.DispatcherAction.execute-start",
                        "org.olat.presentation.framework.dispatcher.DispatcherAction.execute-end"));
                probes_.put("DBQueryImpl.list", codepointClient.startProbingBetween("org.olat.data.commons.database.DBQueryImpl.list-entry",
                        "org.olat.data.commons.database.DBQueryImpl.list-exit"));
            } catch (final CommunicationException e) {
                // ignore ?
            }
            return true;
        }
    }

    public static synchronized void resetStats() {
        if (!isStarted_) {
            return;
        }
        final Set<Entry<String, Probe>> s = probes_.entrySet();
        for (final Iterator<Entry<String, Probe>> it = s.iterator(); it.hasNext();) {
            final Entry<String, Probe> entry = it.next();
            try {
                entry.getValue().clearStats();
                entry.getValue().getStart().setHitCount(0);
                entry.getValue().getEnd().setHitCount(0);
            } catch (final CommunicationException e) {
                // well, ignore
            }
        }

    }
}
