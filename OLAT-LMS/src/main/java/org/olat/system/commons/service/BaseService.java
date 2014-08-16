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
package org.olat.system.commons.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Initial Date: 08.11.2011 <br>
 * 
 * @author Branislav Balaz
 * 
 *         abstract class for common service functionality like service metrics etc. For service metrics Observer pattern class represents Observable - calls notify
 *         method to iterate all Observers and calls update method with concrete ServiceContext as parameter. Implemented as generic class (strong type checking without
 *         instanceof operator etc.). Inheritance hierarchy for this class goes parallel with Service interface hierarchy
 * 
 */
public abstract class BaseService<T extends ServiceMetric<K>, K extends ServiceContext> implements Service {

    protected final List<T> metrics = new ArrayList<T>();

    protected abstract void setMetrics(List<T> metrics);

    protected void notifyMetrics(K serviceContext) {

        for (T metric : metrics) {
            metric.doUpdate(serviceContext);
        }

    }

    protected void attach(T metric) {
        metrics.add(metric);
    }

}
