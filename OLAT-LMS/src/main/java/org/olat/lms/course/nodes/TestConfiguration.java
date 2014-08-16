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
package org.olat.lms.course.nodes;

/**
 * Used by QtiEBL as transfer object.
 * 
 * <P>
 * Initial Date: 22.09.2011 <br>
 * 
 * @author lavinia
 */
public class TestConfiguration {

    private Float minValue;
    private Float maxValue;
    private Float cutValue;

    /**
     * @param minValue
     * @param maxValue
     * @param cutValue
     */
    public TestConfiguration(Float minValue, Float maxValue, Float cutValue) {
        super();
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.cutValue = cutValue;
    }

    public Float getMinValue() {
        return minValue;
    }

    public Float getMaxValue() {
        return maxValue;
    }

    public Float getCutValue() {
        return cutValue;
    }

}
