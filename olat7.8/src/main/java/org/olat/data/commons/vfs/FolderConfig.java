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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.data.commons.vfs;

import org.olat.data.commons.vfs.version.FolderVersioningConfigurator;

/**
 * Initial Date: 18.12.2002
 * 
 * @author Mike Stock
 */
public class FolderConfig {

    public static final String FOLDERROOT_DIR = "bcroot";

    private static final String USERHOMES_DIR = "/homes";

    private static final String USERHOMEPAGES_DIR = "/homepages";

    private static final String REPOSITORY_DIR = "/repository";

    private static final String FORUM_DIR = "/forum";

    private static final String TMP_DIR = "/tmp";

    private static final String META_DIR = "/.meta";

    private static final String VERSION_DIR = "/.version";

    private static FolderVersioningConfigurator versioningConfigurator;

    private static String folderRoot = FOLDERROOT_DIR;

    private static boolean ePortfolioAddEnabled;

    // TODO move to quota manager
    public static final int QUOTA_UPLOAD_DEFAULT_MB = 50;

    public static final int QUOTA_FOLDER_DEFAULT_MB = 50;

    private static long limitULKB = QUOTA_UPLOAD_DEFAULT_MB * 1024;

    private static long quotaKB = QUOTA_FOLDER_DEFAULT_MB * 1024;

    private static boolean quotaCheckEnabled;

    private FolderConfig() {
        super();
    }

    /**
     * Returns briefcase homes root.
     * 
     * @return String
     */
    public static String getCanonicalRoot() {
        return folderRoot;
    }

    /**
     * Sets the folderRoot.
     * 
     * @param newFolderRoot
     *            The newFolderRoot to set
     */
    public static void setFolderRoot(String newFolderRoot) {
        folderRoot = newFolderRoot.replace('\\', '/');
    }

    /**
     * Returns the userHomes.
     * 
     * @return String
     */
    public static String getUserHomes() {
        return USERHOMES_DIR;
    }

    /**
     * Returns the userHome.
     * 
     * @param username
     *            an olat username
     * @return String
     */
    public static String getUserHome(String username) {
        return getUserHomes() + "/" + username;
    }

    /**
     * Returns the userHomePages.
     * 
     * @return String
     */
    public static String getUserHomePages() {
        return USERHOMEPAGES_DIR;
    }

    /**
     * Returns the userHomePage.
     * 
     * @param username
     *            an olat username
     * @return String
     */
    public static String getUserHomePage(String username) {
        return getUserHomePages() + "/" + username;
    }

    /**
     * @return repository path relative to root path
     */
    public static String getRepositoryHome() {
        return REPOSITORY_DIR;
    }

    /**
     * @return the canonical path to the repository root directory.
     */
    public static String getCanonicalRepositoryHome() {
        return getCanonicalRoot() + getRepositoryHome();
    }

    /**
     * @return forum path relative to root path
     */
    public static String getForumHome() {
        return FORUM_DIR;
    }

    /**
     * Returns canonical tmp dir.
     * 
     * @return String
     */
    public static String getCanonicalTmpDir() {
        return getCanonicalRoot() + TMP_DIR;
    }

    /**
     * Returns relative tmp dir.
     * 
     * @return String
     */
    public static String getRelativeTmpDir() {
        return TMP_DIR;
    }

    /**
     * @return the canonical path to the meta root directory.
     */
    public static String getCanonicalMetaRoot() {
        return getCanonicalRoot() + META_DIR;
    }

    /**
     * @return the canonical path to the version root directory
     */
    public static String getCanonicalVersionRoot() {
        return getCanonicalRoot() + VERSION_DIR;
    }

    /**
     * Returns the maxULBytes.
     * 
     * @return long
     */
    public static long getLimitULKB() {
        return limitULKB;
    }

    /**
     * Sets the maxULBytes.
     * 
     * @param newLimitULKB
     *            The maxULBytes to set
     */
    public static void setLimitULKB(long newLimitULKB) {
        limitULKB = newLimitULKB;
    }

    /**
     * @return default quota in KB
     */
    public static long getDefaultQuotaKB() {
        return quotaKB;
    }

    /**
     * @param l
     */
    public static void setDefaultQuotaKB(long l) {
        quotaKB = l;
    }

    /**
     * @return true if folder quota check is enabled
     */
    public static boolean isFolderQuotaCheckEnabled() {
        return FolderConfig.quotaCheckEnabled;
    }

    /**
     * @param folderQuotaCheckEnabled
     *            enable/disable folder quota check
     */
    public static void setFolderQuotaCheckEnabled(boolean folderQuotaCheckEnabled) {
        FolderConfig.quotaCheckEnabled = folderQuotaCheckEnabled;
    }

    public static FolderVersioningConfigurator getVersioningConfigurator() {
        return versioningConfigurator;
    }

    public static void setVersioningConfigurator(FolderVersioningConfigurator versioningConfigurator) {
        FolderConfig.versioningConfigurator = versioningConfigurator;
    }

    /**
     * @return true if versioning is enabled for the container
     */
    public static boolean versionsEnabled(VFSContainer container) {
        if (versioningConfigurator == null) {
            return false;
        }

        return versioningConfigurator.versionEnabled(container);
    }

    /**
     * @return -1 if the number of revisions for the file is unlimited; 0 if versions are not allowed; 1 - n is the maximum allowed number of revisions
     */
    public static int versionsAllowed(String relPath) {
        if (versioningConfigurator == null) {
            return 0;
        }
        return versioningConfigurator.versionAllowed(relPath);
    }

    /**
     * @return true if the file-artefact and eportfolio is enabled
     */
    public static boolean isEPortfolioAddEnabled() {
        return ePortfolioAddEnabled;
    }

    /**
     * @param ePortfolioAddEnabled
     *            The ePortfolioAddEnabled to set.
     */
    public static void setEPortfolioAddEnabled(boolean ePortfolioAddEnabled) {
        FolderConfig.ePortfolioAddEnabled = ePortfolioAddEnabled;
    }
}
