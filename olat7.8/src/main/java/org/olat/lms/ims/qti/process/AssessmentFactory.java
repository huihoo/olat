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

package org.olat.lms.ims.qti.process;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.system.commons.CodeHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * @author Felix Jost
 */
public class AssessmentFactory {

    private static final Logger log = LoggerHelper.getLogger();

    /**
     * Create an assessment instance from a Repository resource referenced by repoPointer.
     * 
     * @param subj
     * @param resourcePathInfo
     * @return
     */
    public static AssessmentInstance createAssessmentInstance(final Identity subj, final ModuleConfiguration modConfig, final boolean preview,
            final String resourcePathInfo) {
        AssessmentInstance ai = null;
        Persister persister = null;

        final String repositorySoftkey = (String) modConfig.get(ModuleConfiguration.CONFIG_KEY_REPOSITORY_SOFTKEY);
        final RepositoryEntry re = RepositoryServiceImpl.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftkey, true);
        if (re == null) {
            return null;
        }

        if (!preview) {
            // try to resume the assessment instance
            persister = CoreSpringFactory.getBean(FilePersister.class, new Object[] { subj, resourcePathInfo });
            ai = (AssessmentInstance) persister.toRAM();
            if (ai == null) {
                // nothing found => try with older V5.0 (shorter Repo-ID) as key
                final FilePersister oldPersister = CoreSpringFactory.getBean(FilePersister.class, new Object[] { subj, re.getKey().toString() });
                ai = (AssessmentInstance) oldPersister.toRAM();
                if (ai != null) {
                    log.info("Audit:Read assessment instance from old path version ,");
                }
            }
        }

        if (ai == null) {
            // no assessment/survey... to resume, launch a new one
            final Resolver resolver = new ImsRepositoryResolver(re.getKey());
            final long aiID = CodeHelper.getForeverUniqueID();
            try {
                ai = new AssessmentInstance(re.getKey().longValue(), aiID, resolver, persister, modConfig);
            } catch (final Exception e) {
                return null;
            }
        } else {
            // continue with the latest non-finished test, mark it as resumed
            ai.setResuming(true);
            final Resolver resolver = new ImsRepositoryResolver(new Long(ai.getRepositoryEntryKey()));
            ai.setResolver(resolver);
            ai.setPersister(persister);
        }
        return ai;

    }

    /**
     * Create an assessment instance from a document passed by the session.
     * 
     * @param subj
     * @param doc
     * @return
     */
    public static AssessmentInstance createAssessmentInstance(final Resolver resolver, final Persister persister, final ModuleConfiguration modConfig) {
        final long aiID = CodeHelper.getForeverUniqueID();
        return new AssessmentInstance(0, aiID, resolver, persister, modConfig);
    }

}
