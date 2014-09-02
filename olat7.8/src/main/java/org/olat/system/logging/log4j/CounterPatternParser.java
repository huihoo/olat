package org.olat.system.logging.log4j;

import org.apache.log4j.helpers.PatternParser;

/**
 * extended log4j for custom logging pattern: http://www.jajakarta.org/log4j/jakarta-log4j-1.1.3/docs/deepExtension.html Initial Date: 30.09.2011 <br>
 * 
 * @author guido
 */
public class CounterPatternParser extends PatternParser {

    public CounterPatternParser(String pattern) {
        super(pattern);
    }

    @Override
    public void finalizeConverter(char c) {
        if (c == '#') {
            addConverter(new LogNumberPatternConverter(formattingInfo));
            currentLiteral.setLength(0);
        } else {
            super.finalizeConverter(c);
        }
    }
}
