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
package org.olat.lms.core.notification.impl;

import org.springframework.stereotype.Component;

/**
 * Initial Date: 21.03.2012 <br>
 * 
 * @author guretzki
 */
@Component
public class UnknownPublisherTypeHandler extends AbstractPublisherTypeHandler {

    public final static String DUMMY_SOURCE_TYPE = "DUMMY_SOURCE";

    private final static String DUMMY_PUBLISHER_DATA_TYPE = "DUMMY_PUBLISHER";

    UnknownPublisherTypeHandler() {
        super(DUMMY_SOURCE_TYPE, DUMMY_PUBLISHER_DATA_TYPE);
    }

    @Override
    public String getSourceEntryPath(String sourceEntryId) {
        return "/Message/" + sourceEntryId;
    }

}
