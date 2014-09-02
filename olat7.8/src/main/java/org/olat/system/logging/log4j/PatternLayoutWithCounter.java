package org.olat.system.logging.log4j;

import org.apache.log4j.EnhancedPatternLayout;
import org.apache.log4j.helpers.PatternParser;

/**
 * extended log4j for custom logging pattern: http://www.jajakarta.org/log4j/jakarta-log4j-1.1.3/docs/deepExtension.html Initial Date: 30.09.2011 <br>
 * 
 * @author guido
 */
public class PatternLayoutWithCounter extends EnhancedPatternLayout {

    public PatternLayoutWithCounter() {
        this(DEFAULT_CONVERSION_PATTERN);
    }

    public PatternLayoutWithCounter(String pattern) {
        super(pattern);
    }

    @Override
    public PatternParser createPatternParser(String pattern) {
        return new CounterPatternParser(pattern == null ? DEFAULT_CONVERSION_PATTERN : pattern);
    }
}
