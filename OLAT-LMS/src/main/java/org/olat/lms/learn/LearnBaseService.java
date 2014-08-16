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
package org.olat.lms.learn;

import org.olat.system.commons.service.BaseService;
import org.olat.system.commons.service.ServiceContext;
import org.olat.system.commons.service.ServiceMetric;

/**
 * Initial Date: 08.11.2011 <br>
 * 
 * @author Branislav Balaz
 * 
 *         abstract class for common learn service functionality.
 * 
 */
public abstract class LearnBaseService<T extends ServiceMetric<K>, K extends ServiceContext> extends BaseService<T, K> implements LearnService {

}
