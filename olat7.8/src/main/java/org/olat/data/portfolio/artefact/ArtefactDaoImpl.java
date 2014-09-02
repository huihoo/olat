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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.data.portfolio.artefact;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBQuery;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.VFSManager;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.data.portfolio.structure.EPStructureToArtefactLink;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Description:<br>
 * EPArtefactManager manage the artefacts
 * <P>
 * Initial Date: 11.06.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
@Repository("epArtefactManager")
public class ArtefactDaoImpl extends BasicManager implements ArtefactDao {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String ARTEFACT_FULLTEXT_ON_FS = "ARTEFACT_FULLTEXT_ON_FS";
    // those are here as instance variable, as mocking the tests won't be possible
    // without!
    // it also helps to find failures in loading a manager, as already spring
    // would warn and not
    // only later on, when a click happens.
    @Autowired
    private DB dbInstance;
    // end.

    private static final int ARTEFACT_FULLTEXT_DB_FIELD_LENGTH = 16384;
    public static final String ARTEFACT_CONTENT_FILENAME = "artefactContent.html";
    private static final String ARTEFACT_INTERNALDATA_FOLDER = "data";

    private VFSContainer portfolioRoot;
    private VFSContainer artefactsRoot;

    /**
	 * 
	 */
    private ArtefactDaoImpl() {
        //
    }

    /**
     * load the persisted artefact from FS
     */
    void loadFile() {
        //
    }

    /**
     * convert html/text to PDF and save in Filesystem
     */
    void persistAsPDF() {
        //
    }

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
    @SuppressWarnings("unchecked")
    public List<AbstractArtefact> getArtefacts(final Identity author, final List<Long> artefactIds, final int firstResult, final int maxResults) {
        final StringBuilder sb = new StringBuilder();
        sb.append("select artefact from ").append(AbstractArtefact.class.getName()).append(" artefact");
        boolean where = false;
        if (author != null) {
            where = true;
            sb.append(" where artefact.author=:author");
        }
        if (artefactIds != null && !artefactIds.isEmpty()) {
            if (where) {
                sb.append(" and ");
            } else {
                sb.append(" where ");
            }
            sb.append(" artefact.id in (:artefactIds)");
        }
        final DBQuery query = dbInstance.createQuery(sb.toString());
        if (maxResults > 0) {
            query.setMaxResults(maxResults);
        }
        if (firstResult >= 0) {
            query.setFirstResult(firstResult);
        }
        if (author != null) {
            query.setEntity("author", author);
        }
        if (artefactIds != null && !artefactIds.isEmpty()) {
            query.setParameterList("artefactIds", artefactIds);
        }

        final List<AbstractArtefact> artefacts = query.list();
        return artefacts;
    }

    public boolean isArtefactClosed(final AbstractArtefact artefact) {
        final StringBuilder sb = new StringBuilder();
        sb.append("select count(link) from ").append(EPStructureToArtefactLink.class.getName()).append(" link ").append(" inner join link.structureElement structure ")
                .append(" inner join structure.root rootStructure").append(" where link.artefact=:artefact and rootStructure.status='closed'");

        final DBQuery query = dbInstance.createQuery(sb.toString());
        query.setEntity("artefact", artefact);
        final Number count = (Number) query.uniqueResult();
        return count.intValue() > 0;
    }

    public List<AbstractArtefact> getArtefactPoolForUser(final Identity ident) {
        final long start = System.currentTimeMillis();
        final StringBuilder sb = new StringBuilder();
        sb.append("select artefact from ").append(AbstractArtefact.class.getName()).append(" artefact").append(" where author=:author");
        final DBQuery query = dbInstance.createQuery(sb.toString());
        query.setEntity("author", ident);
        @SuppressWarnings("unchecked")
        final List<AbstractArtefact> artefacts = query.list();
        if (artefacts.isEmpty()) {
            return null;
        }
        final long duration = System.currentTimeMillis() - start;
        if (log.isDebugEnabled()) {
            log.debug("loading the full artefact pool took " + duration + "ms");
        }
        return artefacts;
    }

    public VFSContainer getArtefactsRoot() {
        if (artefactsRoot == null) {
            final VFSContainer root = getPortfolioRoot();
            final VFSItem artefactsItem = root.resolve("artefacts");
            if (artefactsItem == null) {
                artefactsRoot = root.createChildContainer("artefacts");
            } else if (artefactsItem instanceof VFSContainer) {
                artefactsRoot = (VFSContainer) artefactsItem;
            } else {
                log.error("The root folder for artefact is a file and not a folder", null);
            }
        }
        return artefactsRoot;
    }

    public VFSContainer getPortfolioRoot() {
        if (portfolioRoot == null) {
            portfolioRoot = new OlatRootFolderImpl(File.separator + "portfolio", null);
        }
        return portfolioRoot;
    }

    public VFSContainer getArtefactsTempContainer(final Identity ident) {
        final VFSContainer artRoot = new OlatRootFolderImpl(File.separator + "tmp", null);
        VFSItem tmpI = artRoot.resolve("portfolio");
        if (tmpI == null) {
            tmpI = artRoot.createChildContainer("portfolio");
        }
        VFSItem userTmp = tmpI.resolve(ident.getName());
        if (userTmp == null) {
            userTmp = ((VFSContainer) tmpI).createChildContainer(ident.getName());
        }
        final String idFolder = String.valueOf(((System.currentTimeMillis() % 1000l)) * 100);
        final VFSContainer thisTmp = ((VFSContainer) userTmp).createChildContainer(idFolder);
        return thisTmp;
    }

    public AbstractArtefact saveArtefact(AbstractArtefact artefact) {
        dbInstance.saveObject(artefact);
        saveArtefactFulltextContent(artefact);
        return artefact;
    }

    public AbstractArtefact updateArtefact(final AbstractArtefact artefact) {
        if (artefact == null) {
            return null;
        }

        String tmpFulltext = artefact.getFulltextContent();
        if (StringHelper.containsNonWhitespace(tmpFulltext) && artefact.getFulltextContent().equals(ARTEFACT_FULLTEXT_ON_FS)) {
            tmpFulltext = getArtefactFullTextContent(artefact);
        }
        artefact.setFulltextContent("");
        if (artefact.getKey() == null) {
            dbInstance.saveObject(artefact);
        } else {
            dbInstance.updateObject(artefact);
        }
        artefact.setFulltextContent(tmpFulltext);
        saveArtefactFulltextContent(artefact);

        return artefact;
    }

    // decides itself if fulltext fits into db or will be written on fs
    protected boolean saveArtefactFulltextContent(final AbstractArtefact artefact) {
        final String fullText = artefact.getFulltextContent();
        if (StringHelper.containsNonWhitespace(fullText)) {
            if (fullText.length() > ARTEFACT_FULLTEXT_DB_FIELD_LENGTH) {
                // save the real content on FS
                try {
                    final VFSContainer container = getArtefactContainer(artefact);
                    VFSLeaf artData = (VFSLeaf) container.resolve(ARTEFACT_CONTENT_FILENAME);
                    if (artData == null) {
                        artData = container.createChildLeaf(ARTEFACT_CONTENT_FILENAME);
                    }
                    VFSManager.copyContent(new ByteArrayInputStream(fullText.getBytes()), artData, true);
                    artefact.setFulltextContent(ARTEFACT_FULLTEXT_ON_FS);
                    dbInstance.updateObject(artefact);
                } catch (final Exception e) {
                    log.error("could not really save the fulltext content of an artefact", e);
                    return false;
                }
            } else {
                // if length is shorter, but still a file there -> delete it (but only if loading included the long version from fs before, else its overwritten!)
                VFSLeaf artData = (VFSLeaf) getArtefactContainer(artefact).resolve(ARTEFACT_INTERNALDATA_FOLDER + "/" + ARTEFACT_CONTENT_FILENAME); // v.1 had /data/ in
                                                                                                                                                    // path
                if (artData != null) {
                    artData.delete();
                }
                artData = (VFSLeaf) getArtefactContainer(artefact).resolve(ARTEFACT_CONTENT_FILENAME);
                if (artData != null) {
                    artData.delete();
                }
                dbInstance.updateObject(artefact); // persist fulltext in db
            }
        }
        return true;
    }

    public String getArtefactFullTextContent(final AbstractArtefact artefact) {
        VFSLeaf artData = (VFSLeaf) getArtefactContainer(artefact).resolve(ARTEFACT_CONTENT_FILENAME);
        if (artData == null) {
            artData = (VFSLeaf) getArtefactContainer(artefact).resolve(ARTEFACT_INTERNALDATA_FOLDER + "/" + ARTEFACT_CONTENT_FILENAME); // fallback to
        }
        // v.1
        if (artData != null) {
            return FileUtils.load(artData.getInputStream(), "utf-8");
        } else {
            return artefact.getFulltextContent();
        }
    }

    /**
     * Load the artefact by its primary key
     * 
     * @param key
     *            The primary key
     * @return The artefact or null if nothing found
     */
    public AbstractArtefact loadArtefactByKey(final Long key) {
        if (key == null) {
            throw new NullPointerException();
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("select artefact from ").append(AbstractArtefact.class.getName()).append(" artefact").append(" where artefact=:key");

        final DBQuery query = dbInstance.createQuery(sb.toString());
        query.setLong("key", key);

        @SuppressWarnings("unchecked")
        final List<AbstractArtefact> artefacts = query.list();
        // if not found, it is an empty list
        if (artefacts.isEmpty()) {
            return null;
        }
        return artefacts.get(0);
    }

    public List<AbstractArtefact> loadArtefactsByBusinessPath(final String businessPath) {
        if (!StringHelper.containsNonWhitespace(businessPath)) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        sb.append("select artefact from ").append(AbstractArtefact.class.getName()).append(" artefact").append(" where artefact.businessPath=:bpath");

        final DBQuery query = dbInstance.createQuery(sb.toString());
        query.setString("bpath", businessPath);

        @SuppressWarnings("unchecked")
        final List<AbstractArtefact> artefacts = query.list();
        return artefacts;
    }

    public List<AbstractArtefact> loadArtefactsByBusinessPath(final String businessPath, final Identity author) {
        if (author == null) {
            return null;
        }
        if (!StringHelper.containsNonWhitespace(businessPath)) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        sb.append("select artefact from ").append(AbstractArtefact.class.getName()).append(" artefact")
                .append(" where artefact.businessPath=:bpath and artefact.author=:ident");

        final DBQuery query = dbInstance.createQuery(sb.toString());
        query.setString("bpath", businessPath);
        query.setEntity("ident", author);

        @SuppressWarnings("unchecked")
        final List<AbstractArtefact> artefacts = query.list();
        // if not found, it is an empty list
        if (artefacts.isEmpty()) {
            return null;
        }
        return artefacts;
    }

    public void deleteArtefact(final AbstractArtefact artefact) {
        getArtefactContainer(artefact).delete();

        dbInstance.deleteObject(artefact);
        log.info("Deleted artefact " + artefact.getTitle() + " with key: " + artefact.getKey());
    }

    public VFSContainer getArtefactContainer(final AbstractArtefact artefact) {
        final Long key = artefact.getKey();
        if (key == null) {
            throw new AssertException("artefact not yet persisted -> no key available!");
        }
        VFSContainer container = null;
        final VFSItem item = getArtefactsRoot().resolve(key.toString());
        if (item == null) {
            container = getArtefactsRoot().createChildContainer(key.toString());
        } else if (item instanceof VFSContainer) {
            container = (VFSContainer) item;
        } else {
            log.error("Cannot create a container for artefact: " + artefact, null);
        }
        return container;
    }

}
