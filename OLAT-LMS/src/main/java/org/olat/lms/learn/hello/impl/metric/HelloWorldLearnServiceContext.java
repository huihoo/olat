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
package org.olat.lms.learn.hello.impl.metric;

import java.util.Map;

import org.olat.system.commons.service.ServiceContext;

/**
 * Initial Date: 09.11.2011 <br>
 * 
 * @author Branislav Balaz
 * 
 *         concrete POJOs which transfer virtual stateless service status (status after every service methods call) to concrete service metric
 * 
 */
public class HelloWorldLearnServiceContext implements ServiceContext {

    private final boolean error;

    HelloWorldLearnServiceContext(Map<String, Object> helloWorldLearnServiceContextMap) {
        error = setError(helloWorldLearnServiceContextMap);
    }

    private boolean setError(Map<String, Object> helloWorldLearnServiceContextMap) {
        return (helloWorldLearnServiceContextMap.get(HelloWorldLearnServiceContextKeys.IS_ERROR.name()) instanceof Boolean) ? ((Boolean) helloWorldLearnServiceContextMap
                .get(HelloWorldLearnServiceContextKeys.IS_ERROR.name())).booleanValue() : false;
    }

    public boolean isError() {
        return error;
    }

    /**
     * 
     * Initial Date: 15.11.2011 <br>
     * 
     * @author Branislav Balaz
     * 
     *         enum contains concrete service specific map keys as base for creating relevant service context - creation of concrete service context is due to separation
     *         of concern done in Factory instead of service itself and therefore is necessary to transfer service methods call result to service factory - this is done
     *         in Map which content is service specific.
     */
    public static enum HelloWorldLearnServiceContextKeys {

        IS_ERROR;

    }

}
