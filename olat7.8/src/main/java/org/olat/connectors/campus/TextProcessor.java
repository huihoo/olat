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
package org.olat.connectors.campus;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.olat.data.course.campus.Text;
import org.springframework.batch.item.ItemProcessor;

/**
 * This is an implementation of {@link ItemProcessor} that modifies the input Text item <br>
 * according to some criteria and returns it as output Text item. <br>
 * 
 * Initial Date: 11.06.2012 <br>
 * 
 * @author aabouc
 */
public class TextProcessor implements ItemProcessor<Text, Text> {

    /**
     * Modifies the input text and returns it as output
     * 
     * @param text
     *            the Text to be processed
     * 
     */
    public Text process(Text text) throws Exception {
        text.setLine(StringUtils.replace(text.getLine(), CampusUtils.SEMICOLON_REPLACEMENT, CampusUtils.SEMICOLON));
        text.setModifiedDate(new Date());
        return text;
    }
}
