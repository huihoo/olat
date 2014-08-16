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

package org.olat.lms.course.condition.operators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.Translator;

/**
 * Description:<br>
 * Manager class for the operators in extended easy mode.
 * <P>
 * Initial Date: 23.10.2006 <br>
 * 
 * @author Lars Eberle (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class OperatorManager {

    private static List<Operator> ops = new ArrayList<Operator>();

    static {

        // FIXME:laeb:Springify this!

        // these commented out operators won't work with Shibboleth attributes, but they are there in case
        // that other attributes become available here, too

        ops.add(new EqualsOperator());
        ops.add(new GreaterThanEqualsOperator());
        ops.add(new GreaterThanOperator());
        ops.add(new LowerThanEqualsOperator());
        ops.add(new LowerThanOperator());
        //
        ops.add(new IsInAttributeOperator());
        ops.add(new IsNotInAttributeOperator());
        ops.add(new HasAttributeOperator());
        ops.add(new HasNotAttributeOperator());
        ops.add(new AttributeStartswithOperator());
        ops.add(new AttributeEndswithOperator());
    }

    /**
     * @return The List of registered operators
     */
    public static List<Operator> getAvailableOperators() {
        return ops;
    }

    /**
     * @param l
     *            the locale for translating the operators labels
     * @return the translated labels for all registered operators
     */
    /*
     * public static String[] getAllRegisteredAndAlreadyTranslatedOperatorLabels(final Locale l) { final Translator t = new
     * PackageTranslator(OperatorManager.class.getPackage().getName(), l); final String[] tmp = new String[ops.size()]; int i = 0; for (final Operator o : ops) { tmp[i++]
     * = t.translate(o.getLabelKey()); } return tmp; }
     */

    /**
     * @return an array of all registered operators (exactly: of their keys)
     */
    public static String[] getAllRegisteredOperatorKeys() {
        final String[] tmp = new String[ops.size()];
        int i = 0;
        for (final Operator o : ops) {
            tmp[i++] = o.getOperatorKey();
        }
        return tmp;
    }

    public static String[] getRegisteredOperatorKeys(final List<String> operatorKeys) {
        final List<String> registeredOperatorKeys = Arrays.asList(getAllRegisteredOperatorKeys());
        final String[] tmp = new String[operatorKeys.size()];
        int i = 0;
        for (final String operatorKey : operatorKeys) {
            if (registeredOperatorKeys.contains(operatorKey)) {
                tmp[i++] = operatorKey;
            }
        }
        return tmp;
    }

    public static String[] getRegisteredAndAlreadyTranslatedOperatorLabels(final Locale locale, final String[] operatorKeys) {
        final List<String> keys = Arrays.asList(operatorKeys);
        final Translator t = new PackageTranslator(OperatorManager.class.getPackage().getName(), locale);
        final String[] tmp = new String[ops.size()];
        int i = 0;
        for (final Operator o : ops) {
            if (keys.contains(o.getOperatorKey())) {
                tmp[i++] = t.translate(o.getLabelKey());
            }
        }
        return tmp;
    }

}
