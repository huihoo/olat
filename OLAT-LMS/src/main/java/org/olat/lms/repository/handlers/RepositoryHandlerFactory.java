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

package org.olat.lms.repository.handlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.olat.data.repository.RepositoryEntry;
import org.olat.system.commons.configuration.Initializable;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.exception.AssertException;
import org.olat.system.spring.CoreSpringFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial Date: Apr 6, 2004
 * 
 * @author Mike Stock Comment:
 */
@Service("repositoryHandlerFactory")
public class RepositoryHandlerFactory implements Initializable {

    private static RepositoryHandlerFactory INSTANCE;
    private static Map<String, RepositoryHandler> handlerMap;
    @Autowired
    private CoreSpringFactory factory;
    @Autowired
    List<RepositoryHandler> handlers;

    /**
	 * 
	 */
    private RepositoryHandlerFactory() {
        INSTANCE = this;
    }

    /**
     * @return Singleton.
     */
    public static RepositoryHandlerFactory getInstance() {
        return INSTANCE;
    }

    /**
     * @see org.olat.system.commons.configuration.Initializable#init()
     */
    @Override
    @PostConstruct
    public void init() {
        handlerMap = new HashMap<String, RepositoryHandler>(20);

        for (RepositoryHandler repositoryHandler : handlers) {
            registerHandler(repositoryHandler);
        }

    }

    public void registerHandler(final RepositoryHandler handler) {
        for (final String type : handler.getSupportedTypes()) {
            handlerMap.put(type, handler);
        }
    }

    /**
     * Get the repository handler for this repository entry.
     * 
     * @param re
     * @return the handler or null if no appropriate handler could be found
     */
    public RepositoryHandler getRepositoryHandler(final RepositoryEntry re) {
        final OLATResourceable ores = re.getOlatResource();
        if (ores == null) {
            throw new AssertException("No handler found for resource. ores is null.");
        }
        return getRepositoryHandler(ores.getResourceableTypeName());
    }

    /**
     * Get a repository handler which supports the given resourceable type.
     * 
     * @param resourceableTypeName
     * @return the handler or null if no appropriate handler could be found
     */
    public RepositoryHandler getRepositoryHandler(final String resourceableTypeName) {
        return handlerMap.get(resourceableTypeName);
    }

    /**
     * Get a set of types this factory supports.
     * 
     * @return Set of supported types.
     */
    public static Set<String> getSupportedTypes() {
        return handlerMap.keySet();
    }

}
