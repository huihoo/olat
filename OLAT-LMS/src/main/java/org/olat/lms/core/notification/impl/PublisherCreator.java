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

import org.olat.data.notification.Publisher;
import org.olat.data.notification.Publisher.ContextType;
import org.olat.data.notification.PublisherDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 08.02.2012 <br>
 * 
 * @author guretzki
 */
@Component
public class PublisherCreator {

    @Autowired
    protected PublisherDao publisherDao;

    // Hint: REQUIRES_NEW must be in a new class and can not be called inside a class!
    // @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = org.springframework.dao.DataIntegrityViolationException.class)
    public Publisher tryToCreatePublisher(Long contextId, ContextType contextType, Long sourceId, String sourceType, Long subcontextId) {
        return publisherDao.createAndSavePublisher(contextId, contextType, sourceId, sourceType, subcontextId);
    }

}
