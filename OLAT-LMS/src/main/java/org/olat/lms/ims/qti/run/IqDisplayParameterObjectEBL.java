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
package org.olat.lms.ims.qti.run;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.ims.qti.process.Persister;
import org.olat.lms.ims.qti.process.Resolver;

/**
 * Initial Date: 12.10.2011 <br>
 * 
 * @author Branislav Balaz
 */
public class IqDisplayParameterObjectEBL {

    private final String repositorySoftKey;
    private final long callingResourceId;
    private final String callingResourceDetail;
    private final Identity identity;
    private final ModuleConfiguration moduleConfiguration;
    private final boolean preview;
    private final Resolver resolver;
    private final Persister persister;

    public IqDisplayParameterObjectEBL(String repositorySoftKey, long callingResourceId, String callingResourceDetail, Identity identity,
            ModuleConfiguration moduleConfiguration, boolean preview, Resolver resolver, Persister persister) {
        this.repositorySoftKey = repositorySoftKey;
        this.callingResourceId = callingResourceId;
        this.callingResourceDetail = callingResourceDetail;
        this.identity = identity;
        this.moduleConfiguration = moduleConfiguration;
        this.preview = preview;
        this.resolver = resolver;
        this.persister = persister;
    }

    public String getRepositorySoftKey() {
        return repositorySoftKey;
    }

    public long getCallingResourceId() {
        return callingResourceId;
    }

    public String getCallingResourceDetail() {
        return callingResourceDetail;
    }

    public Identity getIdentity() {
        return identity;
    }

    public ModuleConfiguration getModuleConfiguration() {
        return moduleConfiguration;
    }

    public boolean isPreview() {
        return preview;
    }

    public Resolver getResolver() {
        return resolver;
    }

    public Persister getPersister() {
        return persister;
    }

}
