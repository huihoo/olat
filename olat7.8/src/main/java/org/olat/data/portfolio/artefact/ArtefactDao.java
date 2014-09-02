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
package org.olat.data.portfolio.artefact;

import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.VFSContainer;

/**
 * TODO: Class Description for ArtefactManager
 * 
 * <P>
 * Initial Date: 23.06.2011 <br>
 * 
 * @author lavinia
 */
public interface ArtefactDao {

    public static final String ARTEFACT_CONTENT_FILENAME = "artefactContent.html";

    /**
     * Used by the indexer to retrieve all the artefacts
     * 
     * @param artefactIds
     *            List of ids to seek (optional)
     * @param firstResult
     *            First position
     * @param maxResults
     *            Max number of returned artefacts (0 or below for all)
     * @return
     */
    public abstract List<AbstractArtefact> getArtefacts(final Identity author, final List<Long> artefactIds, final int firstResult, final int maxResults);

    public abstract boolean isArtefactClosed(final AbstractArtefact artefact);

    public abstract List<AbstractArtefact> getArtefactPoolForUser(final Identity ident);

    public abstract VFSContainer getArtefactsRoot();

    public abstract VFSContainer getPortfolioRoot();

    public abstract VFSContainer getArtefactsTempContainer(final Identity ident);

    public AbstractArtefact saveArtefact(AbstractArtefact artefact);

    public abstract AbstractArtefact updateArtefact(final AbstractArtefact artefact);

    public abstract String getArtefactFullTextContent(final AbstractArtefact artefact);

    /**
     * Load the artefact by its primary key
     * 
     * @param key
     *            The primary key
     * @return The artefact or null if nothing found
     */
    public abstract AbstractArtefact loadArtefactByKey(final Long key);

    /**
     * Load all artefacts with given businesspath
     * 
     * @param businessPath
     * @return list of artifacts (emtpy if none found)
     */
    public abstract List<AbstractArtefact> loadArtefactsByBusinessPath(final String businessPath);

    public abstract List<AbstractArtefact> loadArtefactsByBusinessPath(final String businessPath, final Identity author);

    public abstract void deleteArtefact(final AbstractArtefact artefact);

    public abstract VFSContainer getArtefactContainer(final AbstractArtefact artefact);

}
