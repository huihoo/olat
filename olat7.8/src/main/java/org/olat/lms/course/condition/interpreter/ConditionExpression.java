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

package org.olat.lms.course.condition.interpreter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Description:<br>
 * TODO: guido Class Description for ConditionExpression
 */
public class ConditionExpression {
    private String expressionString;
    private final String id;
    private final Stack errorStack;
    private final Map softReferences;

    public ConditionExpression(final String idName, final String expression) {
        this(idName);
        this.expressionString = expression;
    }

    public ConditionExpression(final String idName) {
        this.id = idName;
        errorStack = new Stack();
        softReferences = new HashMap();
    }

    public String getId() {
        return id;
    }

    public String getExptressionString() {
        return expressionString;
    }

    public void setExpressionString(final String expression) {
        expressionString = expression;
    }

    public void pushError(final Exception e) {
        errorStack.push(e);
    }

    public void addSoftReference(final String category, final String softReference) {
        Set catSoftRefs;
        if (softReferences.containsKey(category)) {
            catSoftRefs = (HashSet) softReferences.get(category);
        } else {
            catSoftRefs = new HashSet();
        }
        catSoftRefs.add(softReference);
        softReferences.put(category, catSoftRefs);
    }

    public Set<String> getSoftReferencesOf(final String category) {
        Set<String> catSoftRefs;
        if (softReferences.containsKey(category)) {
            catSoftRefs = (HashSet) softReferences.get(category);
        } else {
            catSoftRefs = new HashSet<String>();
        }
        return catSoftRefs;
    }

    public Exception[] getExceptions() {
        final Exception[] retVal = new Exception[errorStack.size()];
        return (Exception[]) errorStack.toArray(retVal);
    }

    @Override
    public String toString() {
        String retVal = "";
        String softRefStr = "";
        final Set keys = softReferences.keySet();
        for (final Iterator iter = keys.iterator(); iter.hasNext();) {
            final String category = (String) iter.next();
            softRefStr += "[" + category + "::";
            final Set catSoftRefs = (Set) softReferences.get(category);
            for (final Iterator iterator = catSoftRefs.iterator(); iterator.hasNext();) {
                final String srs = (String) iterator.next();
                softRefStr += srs + ",";
            }
            softRefStr += "]";
        }
        retVal += "<ConditionExpression id='" + this.id + "' errorCnt ='" + errorStack.size() + "'>" + softRefStr + "</ConditionExpression>";

        return retVal;
    }

}
