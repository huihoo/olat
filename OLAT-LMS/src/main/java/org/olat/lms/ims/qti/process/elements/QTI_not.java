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

package org.olat.lms.ims.qti.process.elements;

import org.dom4j.Element;
import org.olat.lms.ims.qti.container.ItemContext;
import org.olat.lms.ims.qti.process.QTIHelper;

public class QTI_not implements BooleanEvaluable {

    /**
     * not element ims qti 1.2 <!ELEMENT not (and | or | not | unanswered | other | varequal | varlt | varlte | vargt | vargte | varsubset | varinside | varsubstring |
     * durequal | durlt | durlte | durgt | durgte)>
     */
    @Override
    public boolean eval(final Element boolElement, final ItemContext userContext, final EvalContext ect) {
        // since the dom is validated against the dtd, we know the not element has exactly one child element.
        final Element child = (Element) boolElement.elements().get(0);
        final String name = child.getName();
        final BooleanEvaluable bev = QTIHelper.getBooleanEvaluableInstance(name);
        final boolean res = !bev.eval(child, userContext, ect);
        return res;
    }
}
