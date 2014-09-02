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

package org.olat.lms.upgrade.upgrades;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.LocalFolderImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.filters.VFSContainerFilter;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.fileresource.FileResource;
import org.olat.lms.commons.fileresource.SurveyFileResource;
import org.olat.lms.commons.fileresource.TestFileResource;
import org.olat.lms.ims.qti.editor.QTIEditorPackageEBL;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.upgrade.UpgradeHistoryData;
import org.olat.lms.upgrade.UpgradeManager;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.exception.StartupException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * - Creates all efficiency statements for all users for all courses
 * <P>
 * Initial Date: 15.08.2005 <br>
 * 
 * @author gnaegi
 */
public class OLATUpgrade_4_1_0 extends OLATUpgrade {

    private static final Logger log = LoggerHelper.getLogger();
    private static final String TASK_CHECK_OPEN_QTI_EDITOR_SESSIONS = "check open qti editor sessions";
    private static final String VERSION = "OLAT_4.1.0";
    private static final String TASK_CLEAN_UP_MSGREAD_PROPERTIES_DONE = "unused message properties deleted";
    private static final String TASK_REPLACE_OLDINTERNALLINKS = "replace internal links with new form";

    /**
	 */
    @Override
    public boolean doPreSystemInitUpgrade(final UpgradeManager upgradeManager) {
        // nothing to do here
        return false;
    }

    /**
	 */
    @Override
    public boolean doPostSystemInitUpgrade(final UpgradeManager upgradeManager) {
        UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
        if (uhd == null) {
            // has never been called, initialize
            uhd = new UpgradeHistoryData();
        } else {
            if (uhd.isInstallationComplete()) {
                return false;
            }
        }

        // each message generates a property entry for each user if he read a
        // message in a forum. The deletion of the message did not delete the
        // property entry.
        cleanupUnusedMessageProperties(upgradeManager, uhd);

        // the qti editor creates a persistent lock with the help of the
        // repository entry metadata. This upgrade method searches the
        // olatdata/tmp/qtieditor folder for open qti editor sessions and
        // creates the needed changelog folder andalso the metadata lock.
        checkForOpenQTIEditorSessions(upgradeManager, uhd);

        //
        uhd.setInstallationComplete(true);
        upgradeManager.setUpgradesHistory(uhd, VERSION);

        return true;
    }

    private void checkForOpenQTIEditorSessions(final UpgradeManager upgradeManager, final UpgradeHistoryData uhd) {
        // the qti editor creates a persistent lock with the help of the
        // repository entry metadata. This upgrade method searches the
        // olatdata/tmp/qtieditor folder for open qti editor sessions and
        // creates the needed changelog folder andalso the metadata lock.
        if (!uhd.getBooleanDataValue(TASK_CHECK_OPEN_QTI_EDITOR_SESSIONS)) {
            log.info("Audit:+------------------------------------------+");
            log.info("Audit:+... LOCKS FOR OPEN QTI EDITOR SESSIONS ...+");
            log.info("Audit:+------------------------------------------+");
            //
            final BaseSecurity manager = (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
            final RepositoryService rm = RepositoryServiceImpl.getInstance();
            RepositoryEntry myEntry;
            final HashMap logmsg = new HashMap();
            final VFSContainer qtiTmpDir = new LocalFolderImpl(QTIEditorPackageEBL.getTmpBaseDir());
            final VFSContainerFilter foldersOnly = new VFSContainerFilter();
            // folders in ../tmp/qtieditor hold the usernames
            final List foldersUsername = qtiTmpDir.getItems(foldersOnly);
            for (final Iterator iter = foldersUsername.iterator(); iter.hasNext();) {
                final VFSContainer folderOfUser = (VFSContainer) iter.next();
                // the users folders holds folders with ids of OLATResourceable's
                final List oResFolders = folderOfUser.getItems(foldersOnly);
                for (final Iterator resources = oResFolders.iterator(); resources.hasNext();) {
                    final VFSContainer folderOfResource = (VFSContainer) resources.next();
                    folderOfResource.createChildContainer(QTIEditorPackageEBL.FOLDERNAMEFOR_CHANGELOG);

                    // these are eiterh surveys or tests
                    // try it as testresource then as survey, after this give up
                    final Long oresId = new Long(folderOfResource.getName());
                    FileResource fr = CoreSpringFactory.getBean(TestFileResource.class);
                    fr.overrideResourceableId(oresId);
                    myEntry = rm.lookupRepositoryEntry(fr, false);
                    if (myEntry == null) {
                        // no qti test found, try the qti survey
                        fr = CoreSpringFactory.getBean(SurveyFileResource.class);
                        fr.overrideResourceableId(oresId);
                        myEntry = rm.lookupRepositoryEntry(fr, false);
                    }
                    //
                    if (myEntry != null) {
                        final List identites = manager.getVisibleIdentitiesByPowerSearch(folderOfUser.getName(), null, false, null, null, null, null, null);
                        if (identites != null && identites.size() == 1) {
                            // found exact one user, which is the expected case
                            // a qti resource was found, update its metadata entry to generate a lock
                            final String repoEntry = myEntry.getDisplayname();
                            final String oresIdS = myEntry.getOlatResource().getResourceableId().toString();
                            final String oresIdT = myEntry.getOlatResource().getResourceableTypeName();
                            if (logmsg.containsKey(oresIdS)) {
                                // collision! two or more sessions open on same resource!
                                final String users = (String) logmsg.get(oresIdS);
                                logmsg.put(oresIdS, users + ", " + folderOfUser.getName());
                            } else {
                                // mde = new MetaDataElement("editedBy",folderOfUser.getName());
                                // myEntry.getMetaDataElements().add(mde);
                                // rm.updateRepositoryEntry(myEntry);
                                addQTIEditorSessionLock(fr, (Identity) identites.get(0));
                                log.info("Audit:created persistent lock for user <" + folderOfUser.getName() + "> <" + repoEntry + " [ references " + oresIdS
                                        + " of type:" + oresIdT + "]>");
                                logmsg.put(oresIdS, "[ " + repoEntry + "] " + folderOfUser.getName());
                            }
                        } else if (identites != null && identites.size() > 1) {
                            // found more then one user?? for the userlogin??
                            log.info("Audit:\t*** NO *** persistent lock for user <" + folderOfUser.getName() + "> and entry <" + oresId.toString()
                                    + "> ! Cause: Found more then one identity for user!");
                        } else {
                            // found not user with given login??? as far as user deletion is not implemented, this will never happen.
                            log.info("Audit:\t*** NO *** persistent lock for user <" + folderOfUser.getName() + "> and entry <" + oresId.toString()
                                    + "> ! Cause: User not found!");
                        }
                    } else {
                        // no qti resource found?! deleted already
                        log.info("Audit:\t*** NO *** persistent lock for user <" + folderOfUser.getName() + "> and entry <" + oresId.toString()
                                + "> ! Cause: Entry not found!");
                    }
                }
            }
            // write to the audit log which qti editor sessions are problematic
            final Set keys = logmsg.keySet();
            if (keys != null && keys.size() > 0) {
                log.info("Audit:List of (colliding) QTI Editor Sessions.");
                log.info("Audit:(colliding if more then one user is listed on the same resource)");
                log.info("Audit:\tQTI Resource id\t[Repository entry ] <list of users , where the first one holds a lock now>");
                for (final Iterator iter = keys.iterator(); iter.hasNext();) {
                    final String key = (String) iter.next();
                    log.info("Audit:\t" + key + "\t" + (String) logmsg.get(key));
                }
            } else {
                log.info("Audit:No colliding qti editor sessions detected.");
            }

            log.info("Audit:+----------------------------------------+");
            log.info("Audit:+----------------------------------------+");
            log.info("Audit:+----------------------------------------+");

            uhd.setBooleanDataValue(TASK_CHECK_OPEN_QTI_EDITOR_SESSIONS, true);
            upgradeManager.setUpgradesHistory(uhd, VERSION);
        }

    }

    private void cleanupUnusedMessageProperties(final UpgradeManager upgradeManager, final UpgradeHistoryData uhd) {
        // BEGIN MSG CLEAN UP
        // each message generates a property entry for each user if he read a
        // message in a forum. The deletion of the message did not delete the
        // property entry.
        if (!uhd.getBooleanDataValue(TASK_CLEAN_UP_MSGREAD_PROPERTIES_DONE)) {
            String query = "select o_property.id " + "from o_property LEFT JOIN o_message " + "ON o_property.longvalue=o_message.message_id "
                    + "where o_message.message_id is NULL " + "AND o_property.category='rvst' " + "AND o_property.resourcetypename='Forum'; ";

            try {
                Connection con = upgradeManager.getDataSource().getConnection();
                final Statement stmt = con.createStatement();
                final ResultSet results = stmt.executeQuery(query);

                // delete each property and do logging
                query = "delete from o_property where id = ?";
                final PreparedStatement deleteStmt = con.prepareStatement(query);
                while (results.next()) {
                    final long id = results.getLong("id");
                    log.info("Audit:Deleting unused property (see: bugs.olat.org/jira/browse/OLAT-1273) from table (o_property) with id = " + id);
                    deleteStmt.setLong(1, id);
                    deleteStmt.execute();
                }

                con.close();
                con = null;
            } catch (final SQLException e) {
                log.warn("Could not execute system upgrade sql query. Query:" + query, e);
                throw new StartupException("Could not execute system upgrade sql query. Query:" + query, e);
            }
            uhd.setBooleanDataValue(TASK_CLEAN_UP_MSGREAD_PROPERTIES_DONE, true);
            upgradeManager.setUpgradesHistory(uhd, VERSION);
        }
    }

    private void addQTIEditorSessionLock(final FileResource fr, final Identity user) {
        final PropertyManager pm = PropertyManager.getInstance();
        final String derivedLockString = OresHelper.createStringRepresenting(fr);
        final PropertyImpl newp = pm.createPropertyInstance(null, null, null, "o_lock", derivedLockString, null, user.getKey(), null, null);
        pm.saveProperty(newp);
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    /**
	 */
    @Override
    public String getAlterDbStatements() {
        return null; // till 6.1 was manual upgrade
    }

}
