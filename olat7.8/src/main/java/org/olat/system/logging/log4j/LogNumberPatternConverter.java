package org.olat.system.logging.log4j;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Priority;
import org.apache.log4j.helpers.FormattingInfo;
import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.spi.LoggingEvent;
import org.olat.system.logging.Tracing;

/**
 * custom implementation of a log4j patternConverter to have separate counters for each logging level. Initial Date: 03.10.2011 <br>
 * 
 * @author guido
 */
public class LogNumberPatternConverter extends PatternConverter {

    public static AtomicLong errorCounter = new AtomicLong();
    private AtomicLong warnCounter = new AtomicLong();
    private AtomicLong infoCounter = new AtomicLong();
    private AtomicLong debugCounter = new AtomicLong();

    LogNumberPatternConverter(FormattingInfo formattingInfo) {
        super(formattingInfo);
    }

    @Override
    public String convert(LoggingEvent event) {
        if (event.getLevel().toInt() == Priority.ERROR_INT) {
            return "N" + Tracing.nodeId + "-E" + String.valueOf(errorCounter.incrementAndGet());
        } else if (event.getLevel().toInt() == Priority.WARN_INT) {
            return "N" + Tracing.nodeId + "-W" + String.valueOf(warnCounter.incrementAndGet());
        } else if (event.getLevel().toInt() == Priority.INFO_INT) {
            return "N" + Tracing.nodeId + "-I" + String.valueOf(infoCounter.incrementAndGet());
        } else if (event.getLevel().toInt() == Priority.DEBUG_INT) {
            return "N" + Tracing.nodeId + "-D" + String.valueOf(debugCounter.incrementAndGet());
        }

        return "n/a";
    }

}
