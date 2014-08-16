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

package org.olat.lms.admin.quota;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Constants;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.vfs.Quota;
import org.olat.data.commons.vfs.QuotaManager;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSManager;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.system.commons.configuration.Initializable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <h3>Description:</h3> Quota manager implementation for the OLAT LMS. This is a singleton that must be specified in the spring configuration and be properly
 * initialized!
 * <p>
 * Initial Date: 23.05.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
@Service
public class QuotaManagerImpl extends QuotaManager implements Initializable {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String QUOTA_CATEGORY = "quot";
    private OLATResource quotaResource;
    @Autowired(required = true)
    private OLATResourceManager resourceManager;
    @Autowired(required = true)
    private PropertyManager propertyManager;
    private static Map<String, Quota> defaultQuotas;

    @Autowired
    private BaseSecurity baseSecurity;

    /**
     * [used by spring]
     */
    private QuotaManagerImpl() {
        INSTANCE = this;
    }

    /**
     * java.lang.Long, java.lang.Long)
     */
    @Override
    public Quota createQuota(final String path, final Long quotaKB, final Long ulLimitKB) {
        return new QuotaImpl(path, quotaKB, ulLimitKB);
    }

    /**
     * [called by spring]
     */
    @Override
    @PostConstruct
    public void init() {
        quotaResource = resourceManager.findOrPersistResourceable(OresHelper.lookupType(Quota.class));
        initDefaultQuotas(); // initialize default quotas
        DBFactory.getInstance(false).intermediateCommit();
        log.info("Successfully initialized Quota Manager");
    }

    private void initDefaultQuotas() {
        defaultQuotas = new HashMap<String, Quota>();
        final Quota defaultQuotaUsers = initDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_USERS);
        defaultQuotas.put(QuotaConstants.IDENTIFIER_DEFAULT_USERS, defaultQuotaUsers);
        final Quota defaultQuotaPowerusers = initDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_POWER);
        defaultQuotas.put(QuotaConstants.IDENTIFIER_DEFAULT_POWER, defaultQuotaPowerusers);
        final Quota defaultQuotaGroups = initDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_GROUPS);
        defaultQuotas.put(QuotaConstants.IDENTIFIER_DEFAULT_GROUPS, defaultQuotaGroups);
        final Quota defaultQuotaRepository = initDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_REPO);
        defaultQuotas.put(QuotaConstants.IDENTIFIER_DEFAULT_REPO, defaultQuotaRepository);
        final Quota defaultQuotaCourseFolder = initDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_COURSE);
        defaultQuotas.put(QuotaConstants.IDENTIFIER_DEFAULT_COURSE, defaultQuotaCourseFolder);
        final Quota defaultQuotaNodeFolder = initDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_NODES);
        defaultQuotas.put(QuotaConstants.IDENTIFIER_DEFAULT_NODES, defaultQuotaNodeFolder);
    }

    private Quota initDefaultQuota(final String quotaIdentifier) {
        Quota q = null;
        final PropertyImpl p = propertyManager.findProperty(null, null, quotaResource, QUOTA_CATEGORY, quotaIdentifier);
        if (p != null) {
            q = parseQuota(p);
        }
        if (q != null) {
            return q;
        }
        // initialize default quota
        q = createQuota(quotaIdentifier, new Long(FolderConfig.getDefaultQuotaKB()), new Long(FolderConfig.getLimitULKB()));
        setCustomQuotaKB(q);
        return q;
    }

    /**
     * Get the identifyers for the default quotas
     * 
     * @return
     */
    @Override
    public Set getDefaultQuotaIdentifyers() {
        if (defaultQuotas == null) {
            throw new OLATRuntimeException(QuotaManagerImpl.class, "Quota manager has not been initialized properly! Must call init() first.", null);
        }
        return defaultQuotas.keySet();
    }

    /**
     * Get the default quota for the given identifyer or NULL if no such quota found
     * 
     * @param identifyer
     * @return
     */
    @Override
    public Quota getDefaultQuota(final String identifyer) {
        if (defaultQuotas == null) {
            throw new OLATRuntimeException(QuotaManagerImpl.class, "Quota manager has not been initialized properly! Must call init() first.", null);
        }
        return defaultQuotas.get(identifyer);
    }

    /**
     * Get the quota (in KB) for this path. Important: Must provide a path with a valid base.
     * 
     * @param path
     * @return Quota object.
     */
    @Override
    public Quota getCustomQuota(final String path) {
        if (defaultQuotas == null) {
            throw new OLATRuntimeException(QuotaManagerImpl.class, "Quota manager has not been initialized properly! Must call init() first.", null);
        }
        final PropertyManager pm = PropertyManager.getInstance();
        final PropertyImpl p = pm.findProperty(null, null, quotaResource, QUOTA_CATEGORY, path);
        if (p == null) {
            return null;
        } else {
            return parseQuota(p);
        }
    }

    /**
     * Sets or updates the quota (in KB) for this path. Important: Must provide a path with a valid base.
     * 
     * @param quota
     */
    @Override
    public void setCustomQuotaKB(final Quota quota) {
        if (defaultQuotas == null) {
            throw new OLATRuntimeException(QuotaManagerImpl.class, "Quota manager has not been initialized properly! Must call init() first.", null);
        }
        final PropertyManager pm = PropertyManager.getInstance();
        PropertyImpl p = pm.findProperty(null, null, quotaResource, QUOTA_CATEGORY, quota.getPath());
        if (p == null) { // create new entry
            p = pm.createPropertyInstance(null, null, quotaResource, QUOTA_CATEGORY, quota.getPath(), null, null, assembleQuota(quota), null);
            pm.saveProperty(p);
        } else {
            p.setStringValue(assembleQuota(quota));
            pm.updateProperty(p);
        }
        // if the quota is a default quota, rebuild the default quota list
        if (quota.getPath().startsWith(QuotaConstants.IDENTIFIER_DEFAULT)) {
            initDefaultQuotas();
        }
    }

    /**
     * @param quota
     *            to be deleted
     * @return true if quota successfully deleted or no such quota, false if quota not deleted because it was a default quota that can not be deleted
     */
    @Override
    public boolean deleteCustomQuota(final Quota quota) {
        if (defaultQuotas == null) {
            throw new OLATRuntimeException(QuotaManagerImpl.class, "Quota manager has not been initialized properly! Must call init() first.", null);
        }
        // do not allow to delete default quotas!
        if (quota.getPath().startsWith(QuotaConstants.IDENTIFIER_DEFAULT)) {
            return false;
        }
        final PropertyManager pm = PropertyManager.getInstance();
        final PropertyImpl p = pm.findProperty(null, null, quotaResource, QUOTA_CATEGORY, quota.getPath());
        if (p != null) {
            pm.deleteProperty(p);
        }
        return true;
    }

    /**
     * Get a list of all objects which have an individual quota.
     * 
     * @return list of quotas.
     */
    @Override
    public List listCustomQuotasKB() {
        if (defaultQuotas == null) {
            throw new OLATRuntimeException(QuotaManagerImpl.class, "Quota manager has not been initialized properly! Must call init() first.", null);
        }
        final List results = new ArrayList();
        final PropertyManager pm = PropertyManager.getInstance();
        final List props = pm.listProperties(null, null, quotaResource, QUOTA_CATEGORY, null);
        if (props == null || props.size() == 0) {
            return results;
        }
        for (final Iterator iter = props.iterator(); iter.hasNext();) {
            final PropertyImpl prop = (PropertyImpl) iter.next();
            results.add(parseQuota(prop));
        }
        return results;
    }

    /**
     * @param p
     * @return Parsed quota object.
     */
    private Quota parseQuota(final PropertyImpl p) {
        final String s = p.getStringValue();
        final int delim = s.indexOf(':');
        if (delim == -1) {
            return null;
        }
        Quota q = null;
        try {
            final Long quotaKB = new Long(s.substring(0, delim));
            final Long ulLimitKB = new Long(s.substring(delim + 1));
            q = createQuota(p.getName(), quotaKB, ulLimitKB);
        } catch (final NumberFormatException e) {
            // will return null if quota parsing failed
        }
        return q;
    }

    private String assembleQuota(final Quota quota) {
        return quota.getQuotaKB() + ":" + quota.getUlLimitKB();
    }

    /**
     * call to get appropriate quota depending on role. Authors have normally bigger quotas than normal users.
     * 
     * @param identity
     * @return
     */
    @Override
    public Quota getDefaultQuotaDependingOnRole(final Identity identity) {
        if (baseSecurity.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_AUTHOR)) {
            return getDefaultQuotaPowerUsers();
        }
        if (baseSecurity.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_ADMIN)) {
            return getDefaultQuotaPowerUsers();
        }
        return getDefaultQuotaUsers();
    }

    /**
     * call to get appropriate quota depending on role. Authors have normally bigger quotas than normal users. The method checks also if the user has a custom quota on
     * the path specified. If yes the custom quota is retuned
     * 
     * @param identity
     * @return custom quota or quota depending on role
     */
    @Override
    public Quota getCustomQuotaOrDefaultDependingOnRole(final Identity identity, final String relPath) {
        final Quota quota = getCustomQuota(relPath);
        if (quota == null) { // no custom quota
            if (baseSecurity.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_AUTHOR)) {
                return createQuota(relPath, getDefaultQuotaPowerUsers().getQuotaKB(), getDefaultQuotaPowerUsers().getUlLimitKB());
            }
            if (baseSecurity.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_ADMIN)) {
                return createQuota(relPath, getDefaultQuotaPowerUsers().getQuotaKB(), getDefaultQuotaPowerUsers().getUlLimitKB());
            }
            return createQuota(relPath, getDefaultQuotaUsers().getQuotaKB(), getDefaultQuotaUsers().getUlLimitKB());
        }
        return quota;
    }

    /**
     * get default quota for normal users. On places where you have users with different roles use
     * 
     * @return Quota
     */
    private Quota getDefaultQuotaUsers() {
        if (defaultQuotas == null) {
            throw new OLATRuntimeException(QuotaManagerImpl.class, "Quota manager has not been initialized properly! Must call init() first.", null);
        }
        return defaultQuotas.get(QuotaConstants.IDENTIFIER_DEFAULT_USERS);
    }

    /**
     * get default quota for power users (authors). On places where you have users with different roles use
     * 
     * @return Quota
     */
    private Quota getDefaultQuotaPowerUsers() {
        if (defaultQuotas == null) {
            throw new OLATRuntimeException(QuotaManagerImpl.class, "Quota manager has not been initialized properly! Must call init() first.", null);
        }
        return defaultQuotas.get(QuotaConstants.IDENTIFIER_DEFAULT_POWER);
    }

    /**
     * Return upload-limit depending on quota-limit and upload-limit values.
     * 
     * @param quotaKB2
     *            Quota limit in KB, can be Quota.UNLIMITED
     * @param uploadLimitKB2
     *            Upload limit in KB, can be Quota.UNLIMITED
     * @param currentContainer2
     *            Upload container (folder)
     * @return Upload limit on KB
     */
    @Override
    public int getUploadLimitKB(final long quotaKB2, final long uploadLimitKB2, final VFSContainer currentContainer2) {
        if (quotaKB2 == Quota.UNLIMITED) {
            if (uploadLimitKB2 == Quota.UNLIMITED) {
                return Quota.UNLIMITED; // quote & upload un-limited
            } else {
                return (int) uploadLimitKB2; // only upload limited
            }
        } else {
            // initialize default UL limit
            // prepare quota checks
            long quotaLeftKB = VFSManager.getQuotaLeftKB(currentContainer2);
            if (quotaLeftKB < 0) {
                quotaLeftKB = 0;
            }
            if (uploadLimitKB2 == Quota.UNLIMITED) {
                return (int) quotaLeftKB;// quote:limited / upload:unlimited
            } else {
                // quote:limited / upload:limited
                if (quotaLeftKB > uploadLimitKB2) {
                    return (int) uploadLimitKB2; // upload limit cut the upload
                } else {
                    return (int) quotaLeftKB; // quota-left space cut the upload
                }
            }
        }
    }

    /**
     * Check if a quota path is valid
     * 
     * @param path
     * @return
     */
    @Override
    public boolean isValidQuotaPath(final String path) {
        if (path.startsWith(QuotaConstants.IDENTIFIER_DEFAULT) && !defaultQuotas.containsKey(path)) {
            return false;
        }
        return true;
    }

}
