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
package org.olat.lms.monitoring;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * implementation of SimpleProbeService interface
 * 
 * <P>
 * Initial Date: 05.09.2011 <br>
 * 
 * @author Branislav Balaz
 */
@Service
public class SimpleProbeServiceImpl implements SimpleProbeService {

    @Autowired
    SimpleProbeBusinessObject simpleProbeBOImpl;

    SimpleProbeServiceImpl() {

    }

    @Override
    public SimpleProbeObject getSimpleProbe(final String key) {

        return simpleProbeBOImpl.getSimpleProbeByKey(key);

    }

    @Override
    public List<SimpleProbeObject> getSimpleProbeNonRegisteredList() {
        return simpleProbeBOImpl.getSimpleProbeNonRegisteredFromMapAsList();
    }

}
