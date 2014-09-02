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

/**
 * This Exception can be used as business or wrapper exception for campuskurs.<br>
 * 
 * Initial Date: 12.07.2012 <br>
 * 
 * @author aabouc
 */
@SuppressWarnings("serial")
public class CampusException extends Exception {

    /**
     * Create a new CampusException with the given message.
     * 
     * @param msg
     *            the descriptive message
     */
    public CampusException(final String msg) {
        super(msg);
    }
}
