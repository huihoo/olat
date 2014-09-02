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

package org.olat.lms.ims.qti.container;

/**
 * 
 */
public class DecimalVariable extends Variable {
    private float floatValue = 0.0f;
    private float minValue, maxValue, cutValue, defaultValue;
    private boolean mindeclared = false, maxdeclared = false, cutdeclared = false, defaultdeclared = false;

    public DecimalVariable(final String varName, final String maxValue, final String minValue, final String cutValue, final String defaultValue) {
        super(varName);
        if (maxValue != null) {
            this.maxValue = parseFloat(maxValue);
            maxdeclared = true;
        }
        if (minValue != null) {
            this.minValue = parseFloat(minValue);
            mindeclared = true;
        }
        if (cutValue != null) {
            this.cutValue = parseFloat(cutValue);
            cutdeclared = true;
        }
        if (defaultValue != null) {
            final float def = parseFloat(defaultValue);
            this.defaultValue = def;
            this.floatValue = def;
            defaultdeclared = true;
        }
    }

    @Override
    public void reset() {
        if (defaultdeclared) {
            floatValue = defaultValue;
        } else {
            floatValue = 0.0f;
        }
    }

    @Override
    public void add(final String value) {
        floatValue += parseFloat(value);
    }

    @Override
    public void subtract(final String value) {
        floatValue -= parseFloat(value);
    }

    @Override
    public void multiply(final String value) {
        floatValue *= parseFloat(value);
    }

    @Override
    public void divide(final String value) {
        floatValue /= parseFloat(value);
    }

    private float parseFloat(String value) {
        value = value.trim();
        final float f = Float.parseFloat(value);
        return f;
    }

    @Override
    public float getTruncatedValue() {
        float tmp = floatValue;
        if (maxdeclared) {
            if (tmp > maxValue) {
                tmp = maxValue;
            }
        }
        if (mindeclared) {
            if (tmp < minValue) {
                tmp = minValue;
            }
        }
        return tmp;
    }

    @Override
    public String toString() {
        return "(float)" + floatValue + ":" + super.toString();
    }

    @Override
    public void setValue(final String value) {
        floatValue = parseFloat(value);
    }

    /**
     * Returns the cutValue.
     * 
     * @return float
     */
    @Override
    public float getCutValue() {
        return cutValue;
    }

    /**
     * Returns the defaultValue.
     * 
     * @return float
     */
    @Override
    public float getDefaultValue() {
        return defaultValue;
    }

    /**
     * Returns the maxValue.
     * 
     * @return float
     */
    @Override
    public float getMaxValue() {
        return maxValue;
    }

    /**
     * Returns the minValue.
     * 
     * @return float
     */
    @Override
    public float getMinValue() {
        return minValue;
    }

    /**
	 */
    @Override
    public boolean hasCutValue() {
        return cutdeclared;
    }

    /**
	 */
    @Override
    public boolean hasDefaultValue() {
        return defaultdeclared;
    }

    /**
	 */
    @Override
    public boolean hasMaxValue() {
        return maxdeclared;
    }

    /**
	 */
    @Override
    public boolean hasMinValue() {
        return mindeclared;
    }

    /**
	 */
    @Override
    public float getValue() {
        return floatValue;
    }

    /**
	 */
    @Override
    public boolean isLessThan(final String operand) {
        final float cmp = parseFloat(operand);
        return cmp < floatValue;
    }

    /**
	 */
    @Override
    public boolean isMoreThan(final String operand) {
        final float cmp = parseFloat(operand);
        return cmp > floatValue;
    }

    /**
	 */
    @Override
    public boolean isEqual(final String operand) {
        final float cmp = parseFloat(operand);
        return cmp == floatValue;
    }
}
