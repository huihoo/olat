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

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.commons.database.DBQuery;
import org.olat.data.commons.filter.impl.OWASPAntiSamyXSSFilter;
import org.olat.data.forum.Message;
import org.olat.data.group.BusinessGroup;
import org.olat.data.group.area.BGArea;
import org.olat.data.group.area.BGAreaDao;
import org.olat.data.group.area.BGAreaDaoImpl;
import org.olat.data.group.context.BGContext;
import org.olat.data.group.context.BGContextDao;
import org.olat.data.group.context.BGContextDaoImpl;
import org.olat.data.note.Note;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.data.repository.RepositoryDao;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.forum.ForumService;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.note.NoteService;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.upgrade.UpgradeHistoryData;
import org.olat.lms.upgrade.UpgradeManager;
import org.olat.lms.user.HomePageConfig;
import org.olat.lms.user.HomePageConfigManager;
import org.olat.lms.user.HomePageConfigManagerImpl;
import org.olat.presentation.collaboration.CollaborationTools;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.StringHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Upgrade to OLAT 6.2: - Migration of old wiki-fields to flexiform Code is already here for every update. Method calls will be commented out step by step when
 * corresponding new controllers are ready. As long as there will be other things to migrate Upgrade won't be set to DONE!
 * <P>
 * Initial Date: 20.06.09 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, www.frentix.com
 */
public class OLATUpgrade_6_2_0 extends OLATUpgrade {

    private static final Logger log = LoggerHelper.getLogger();
    private static final String VERSION = "OLAT_6.2";
    private static final String TASK_MIGRATE_WIKICODE_NOTES = "Migrate Wiki-field NOTE to new syntax";
    private static final String TASK_MIGRATE_WIKICODE_FORUM = "Migrate Wiki-field FORUM to new syntax";
    private static final String TASK_MIGRATE_WIKICODE_GROUPNEWS = "Migrate Wiki-field GROUP-INFORMATION/NEWS to new syntax";
    private static final String TASK_MIGRATE_WIKICODE_BGCONTEXT = "Migrate Wiki-field BUSINESSGROUPCONTEXT to new syntax";
    private static final String TASK_MIGRATE_WIKICODE_BGAREA = "Migrate Wiki-field BUSINESSGROUPAREA to new syntax";
    private static final String TASK_MIGRATE_WIKICODE_BG_DESC = "Migrate Wiki-field BUSINESSGROUPDESC to new syntax";
    private static final String TASK_MIGRATE_WIKICODE_HOMEPAGE = "Migrate Wiki-field in HOMEPAGE/BIO to new syntax";
    private static final String TASK_MIGRATE_WIKICODE_REPOENTRY = "Migrate Wiki-field in REPOSITORY ENTRY to new syntax";

    @Autowired
    private BusinessGroupService businessGroupService;
    @Autowired
    private BaseSecurity secMgr;
    @Autowired
    private ForumService forumService;
    @Autowired
    private NoteService noteService;
    @Autowired
    private RepositoryDao repositoryDao;
    @Autowired
    private BGContextDao bgContextDao;
    @Autowired
    private BGAreaDao bgAreaDao;
    @Autowired
    private PropertyManager propertyManager;
    @Autowired
    private RepositoryService repositoryService;

    /**
     * [spring managed]
     */
    private OLATUpgrade_6_2_0() {
        //
    }

    /**
	 */
    public boolean doPreSystemInitUpgrade(final UpgradeManager upgradeManager) {
        return false;
    }

    /**
	 */
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

        long startTime = 0;
        if (log.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
        }

        // migrationTest();

        // migrate old wiki htmlareas to new tinymce fields -> persist with html code instead wiki-syntax
        // userNotes
        migrateNotes(upgradeManager, uhd);
        if (log.isDebugEnabled()) {
            log.debug("OLATUpgrade_6_2_0: migrateNotes takes " + (System.currentTimeMillis() - startTime) + "ms");
            startTime = System.currentTimeMillis();
        }

        // forum posts
        migrateForum(upgradeManager, uhd);
        if (log.isDebugEnabled()) {
            log.debug("OLATUpgrade_6_2_0: migrateForum takes " + (System.currentTimeMillis() - startTime) + "ms");
            startTime = System.currentTimeMillis();
        }

        // newsform (information in groups)
        migrateGroupNews(upgradeManager, uhd);
        if (log.isDebugEnabled()) {
            log.debug("OLATUpgrade_6_2_0: migrateGroupNews takes " + (System.currentTimeMillis() - startTime) + "ms");
            startTime = System.currentTimeMillis();
        }

        // business group description
        migrateGroupDescription(upgradeManager, uhd);
        if (log.isDebugEnabled()) {
            log.debug("OLATUpgrade_6_2_0: migrateGroupDescription takes " + (System.currentTimeMillis() - startTime) + "ms");
            startTime = System.currentTimeMillis();
        }

        // BG Context Groups
        migrateBGContext(upgradeManager, uhd);
        if (log.isDebugEnabled()) {
            log.debug("OLATUpgrade_6_2_0: migrateBGContext takes " + (System.currentTimeMillis() - startTime) + "ms");
            startTime = System.currentTimeMillis();
        }

        // BG Areas
        migrateBGArea(upgradeManager, uhd);
        if (log.isDebugEnabled()) {
            log.debug("OLATUpgrade_6_2_0: migrateBGArea takes " + (System.currentTimeMillis() - startTime) + "ms");
            startTime = System.currentTimeMillis();
        }

        // Repository Entry
        migrateRepoEntry(upgradeManager, uhd);
        if (log.isDebugEnabled()) {
            log.debug("OLATUpgrade_6_2_0: migrateRepoEntry takes " + (System.currentTimeMillis() - startTime) + "ms");
            startTime = System.currentTimeMillis();
        }

        // homepage-bio / visitcard
        migrateHomepageBio(upgradeManager, uhd);
        if (log.isDebugEnabled()) {
            log.debug("OLATUpgrade_6_2_0: migrateHomepageBio takes " + (System.currentTimeMillis() - startTime) + "ms");
            startTime = System.currentTimeMillis();
        }

        // tests for xss-filter, needs to be done during upgrade (startup) as jUnit has other database without real data.
        // testXSSFilter();
        // if (log.isDebugEnabled()) {
        // log.debug("OLATUpgrade_6_2_0: testing the XSS Filter takes " + (System.currentTimeMillis() - startTime) + "ms");
        // startTime = System.currentTimeMillis();
        // }

        // now pre and post code was ok, finish installation
        uhd.setInstallationComplete(true);
        // persist infos
        upgradeManager.setUpgradesHistory(uhd, VERSION);
        return true;
    }

    public String getVersion() {
        return VERSION;
    }

    private void migrateNotes(final UpgradeManager upgradeManager, final UpgradeHistoryData uhd) {
        if (!uhd.getBooleanDataValue(TASK_MIGRATE_WIKICODE_NOTES)) {
            log.info("Audit:+---------------------------------------------------------------+");
            log.info("Audit:+... " + TASK_MIGRATE_WIKICODE_NOTES + "   ...+");
            log.info("Audit:+---------------------------------------------------------------+");

            final List<Identity> identitiesList = secMgr.getIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null, null, null);
            DBFactory.getInstance().intermediateCommit();
            int counter = 0;
            int usercounter = 0;
            if (log.isDebugEnabled()) {
                log.info("Migrating notes for " + identitiesList.size() + " Identities.");
            }
            for (final Iterator<Identity> iterator = identitiesList.iterator(); iterator.hasNext();) {
                final Identity identity = iterator.next();
                try {
                    final List<Note> allIdentityNotes = noteService.getUserNotes(identity);
                    if (log.isDebugEnabled()) {
                        log.info("Migrate " + allIdentityNotes.size() + " Notes for Identity: " + identity.getName());
                    }
                    if (!allIdentityNotes.isEmpty()) {
                        usercounter++;
                        for (final Iterator<Note> iterator2 = allIdentityNotes.iterator(); iterator2.hasNext();) {
                            try {
                                final Note note = iterator2.next();
                                String parsedText = note.getNoteText();
                                parsedText = migrateStringSavely(parsedText);
                                note.setNoteText(parsedText);
                                noteService.setNote(note);
                                counter++;
                                DBFactory.getInstance().intermediateCommit();
                            } catch (final Exception e) {
                                log.error("Error during Migration: " + e, e);
                                DBFactory.getInstance().rollback();
                            }

                            if (counter > 0 && counter % 150 == 0) {
                                if (log.isDebugEnabled()) {
                                    log.info("Audit:Another 150 items done");
                                }
                            }
                        }
                    }
                    DBFactory.getInstance().intermediateCommit();
                } catch (final Exception e) {
                    log.error("Error during Migration: " + e, e);
                    DBFactory.getInstance().rollback();
                }
            }
            DBFactory.getInstance().intermediateCommit();
            log.info("Audit:Migrated total " + counter + " notes of " + usercounter + " users with notes");
            uhd.setBooleanDataValue(TASK_MIGRATE_WIKICODE_NOTES, true);
            upgradeManager.setUpgradesHistory(uhd, VERSION);
        }
    }

    private void migrateForum(final UpgradeManager upgradeManager, final UpgradeHistoryData uhd) {
        if (!uhd.getBooleanDataValue(TASK_MIGRATE_WIKICODE_FORUM)) {
            log.info("Audit:+---------------------------------------------------------------+");
            log.info("Audit:+...     " + TASK_MIGRATE_WIKICODE_FORUM + "     ...+");
            log.info("Audit:+---------------------------------------------------------------+");

            final List<Long> allForumKeys = forumService.getAllForumKeys();
            int fCounter = 0;
            int totMCounter = 0;
            if (log.isDebugEnabled()) {
                log.info("Migrating " + allForumKeys.size() + " forums.");
            }
            for (final Iterator<Long> iterator = allForumKeys.iterator(); iterator.hasNext();) {
                try {
                    final Long forumKey = iterator.next();
                    // Long forumKey = new Long(338493441);
                    log.info("Audit:  Found forum with key: " + forumKey.toString() + " containing " + forumService.countMessagesByForumID(forumKey)
                            + " messages to migrate.");
                    final List<Message> allMessages = forumService.getMessagesByForumID(forumKey);
                    fCounter++;
                    int mCounter = 0;
                    for (final Iterator<Message> iterator2 = allMessages.iterator(); iterator2.hasNext();) {
                        try {
                            final Message message = iterator2.next();
                            if (log.isDebugEnabled()) {
                                log.info("Audit:    - Message inside: " + message.getTitle() + " key: " + message.getKey());
                            }
                            final String oldValue = message.getBody();
                            final String newMsgBody = migrateStringSavely(oldValue);
                            message.setBody(newMsgBody);
                            // Update message without ForumManager to prevent resetting the lastModifiedTime
                            DBFactory.getInstance().updateObject(message);
                            mCounter++;
                            DBFactory.getInstance().intermediateCommit();
                        } catch (final Exception e) {
                            log.error("Error during Migration: " + e, e);
                            DBFactory.getInstance().rollback();
                        }
                        if (mCounter > 0 && mCounter % 150 == 0) {
                            if (log.isDebugEnabled()) {
                                log.info("Audit:Another 150 items done");
                            }
                        }
                    }
                    totMCounter += mCounter;
                    // commit for each forum
                    DBFactory.getInstance().intermediateCommit();
                } catch (final Exception e) {
                    log.error("Error during Migration: " + e, e);
                    DBFactory.getInstance().rollback();
                }
            }
            log.info("Audit:**** Migrated " + fCounter + " forums with a total of " + totMCounter + " messages inside. ****");
            uhd.setBooleanDataValue(TASK_MIGRATE_WIKICODE_FORUM, true);
            upgradeManager.setUpgradesHistory(uhd, VERSION);
        }
    }

    private void migrateGroupNews(final UpgradeManager upgradeManager, final UpgradeHistoryData uhd) {
        if (!uhd.getBooleanDataValue(TASK_MIGRATE_WIKICODE_GROUPNEWS)) {
            log.info("Audit:+---------------------------------------------------------------+");
            log.info("Audit:+...     " + TASK_MIGRATE_WIKICODE_GROUPNEWS + "     ...+");
            log.info("Audit:+---------------------------------------------------------------+");

            final List<PropertyImpl> props = propertyManager.getCollaborationNewsProperties();
            if (log.isDebugEnabled()) {
                log.info("Found " + props.size() + " groupnews to migrate.");
            }

            int counter = 0;
            for (final PropertyImpl property : props) {
                try {
                    final String oldVal = property.getTextValue();
                    final String newVal = migrateStringSavely(oldVal);
                    property.setTextValue(newVal);
                    propertyManager.updateProperty(property);
                    counter++;
                    DBFactory.getInstance().intermediateCommit();
                } catch (final Exception e) {
                    log.error("Error during Migration: " + e, e);
                    DBFactory.getInstance().rollback();
                }

                if (counter > 0 && counter % 150 == 0) {
                    if (log.isDebugEnabled()) {
                        log.info("Audit:Another 150 items done");
                    }
                }
            }
            DBFactory.getInstance().intermediateCommit();
            log.info("Audit:**** Migrated " + counter + " group news. ****");
            uhd.setBooleanDataValue(TASK_MIGRATE_WIKICODE_GROUPNEWS, true);
            upgradeManager.setUpgradesHistory(uhd, VERSION);
        }
    }

    private void migrateRepoEntry(final UpgradeManager upgradeManager, final UpgradeHistoryData uhd) {
        if (!uhd.getBooleanDataValue(TASK_MIGRATE_WIKICODE_REPOENTRY)) {
            log.info("Audit:+---------------------------------------------------------------+");
            log.info("Audit:+...     " + TASK_MIGRATE_WIKICODE_REPOENTRY + "     ...+");
            log.info("Audit:+---------------------------------------------------------------+");

            List<RepositoryEntry> entries = repositoryDao.getAllRepositoryEntries();
            if (log.isDebugEnabled()) {
                log.info("Migrating " + entries.size() + " Repository Entires.");
            }
            int counter = 0;
            for (final RepositoryEntry entry : entries) {
                try {
                    final String oldDesc = entry.getDescription();
                    if (StringHelper.containsNonWhitespace(oldDesc)) {
                        final String newDesc = migrateStringSavely(oldDesc);
                        entry.setDescription(newDesc);
                        repositoryService.updateRepositoryEntry(entry);
                        counter++;
                    }
                    DBFactory.getInstance().intermediateCommit();
                } catch (final Exception e) {
                    log.error("Error during Migration: " + e, e);
                    DBFactory.getInstance().rollback();
                }
                if (counter > 0 && counter % 150 == 0) {
                    if (log.isDebugEnabled()) {
                        log.info("Audit:Another 150 items done");
                    }
                }
            }
            DBFactory.getInstance().intermediateCommit();
            log.info("Audit:**** Migrated " + counter + " repository entries. ****");
            uhd.setBooleanDataValue(TASK_MIGRATE_WIKICODE_REPOENTRY, true);
            upgradeManager.setUpgradesHistory(uhd, VERSION);
        }
    }

    private void migrateBGContext(final UpgradeManager upgradeManager, final UpgradeHistoryData uhd) {
        if (!uhd.getBooleanDataValue(TASK_MIGRATE_WIKICODE_BGCONTEXT)) {
            log.info("Audit:+---------------------------------------------------------------+");
            log.info("Audit:+...     " + TASK_MIGRATE_WIKICODE_BGCONTEXT + "     ...+");
            log.info("Audit:+---------------------------------------------------------------+");

            final List<BGContext> contexts = bgContextDao.getAllBGContext();
            if (log.isDebugEnabled()) {
                log.info("Migrating " + contexts.size() + " BG Contexts.");
            }
            int bgcounter = 0;
            for (final BGContext context : contexts) {
                try {
                    final String oldDesc = context.getDescription();
                    if (StringHelper.containsNonWhitespace(oldDesc)) {
                        final String newDesc = migrateStringSavely(oldDesc);
                        context.setDescription(newDesc);
                        bgContextDao.updateBGContext(context);
                        bgcounter++;
                    }
                    DBFactory.getInstance().intermediateCommit();
                } catch (final Exception e) {
                    log.error("Error during Migration: " + e, e);
                    DBFactory.getInstance().rollback();
                }
                if (bgcounter > 0 && bgcounter % 150 == 0) {
                    if (log.isDebugEnabled()) {
                        log.info("Audit:Another 150 items done");
                    }
                }
            }

            DBFactory.getInstance().intermediateCommit();
            log.info("Audit:**** Migrated " + bgcounter + " BGContext. ****");

            uhd.setBooleanDataValue(TASK_MIGRATE_WIKICODE_BGCONTEXT, true);
            upgradeManager.setUpgradesHistory(uhd, VERSION);
        }
    }

    private void migrateBGArea(final UpgradeManager upgradeManager, final UpgradeHistoryData uhd) {
        if (!uhd.getBooleanDataValue(TASK_MIGRATE_WIKICODE_BGAREA)) {
            log.info("Audit:+---------------------------------------------------------------+");
            log.info("Audit:+...     " + TASK_MIGRATE_WIKICODE_BGAREA + "     ...+");
            log.info("Audit:+---------------------------------------------------------------+");

            final List<BGArea> areas = bgAreaDao.getAllBGArea();
            if (log.isDebugEnabled()) {
                log.info("Migrating " + areas.size() + " BG areas.");
            }
            final BGAreaDao bgM = BGAreaDaoImpl.getInstance();
            int bgcounter = 0;

            for (final BGArea area : areas) {
                try {
                    final String oldDesc = area.getDescription();
                    if (StringHelper.containsNonWhitespace(oldDesc)) {
                        final String newDesc = migrateStringSavely(oldDesc);
                        area.setDescription(newDesc);
                        bgM.updateBGArea(area);
                        bgcounter++;
                    }
                    DBFactory.getInstance().intermediateCommit();
                } catch (final Exception e) {
                    log.error("Error during Migration: " + e, e);
                    DBFactory.getInstance().rollback();
                }
                if (bgcounter > 0 && bgcounter % 150 == 0) {
                    if (log.isDebugEnabled()) {
                        log.info("Audit:Another 150 items done");
                    }
                }

            }
            DBFactory.getInstance().intermediateCommit();
            log.info("Audit:**** Migrated " + bgcounter + " BGAreas. ****");
            uhd.setBooleanDataValue(TASK_MIGRATE_WIKICODE_BGAREA, true);
            upgradeManager.setUpgradesHistory(uhd, VERSION);
        }
    }

    private void migrateGroupDescription(final UpgradeManager upgradeManager, final UpgradeHistoryData uhd) {
        if (!uhd.getBooleanDataValue(TASK_MIGRATE_WIKICODE_BG_DESC)) {
            log.info("Audit:+---------------------------------------------------------------+");
            log.info("Audit:+...     " + TASK_MIGRATE_WIKICODE_BG_DESC + "     ...+");
            log.info("Audit:+---------------------------------------------------------------+");

            int bgcounter = 0;
            final List<BusinessGroup> allGroups = businessGroupService.getAllBusinessGroups();
            if (log.isDebugEnabled()) {
                log.info("Migrating " + allGroups.size() + " BusinessGroups.");
            }

            if (allGroups != null && allGroups.size() != 0) {
                for (final BusinessGroup group : allGroups) {
                    try {
                        final String oldDesc = group.getDescription();
                        if (StringHelper.containsNonWhitespace(oldDesc)) {
                            final String newDesc = migrateStringSavely(oldDesc);
                            group.setDescription(newDesc);
                            businessGroupService.updateBusinessGroup(group);
                            bgcounter++;
                        }
                        DBFactory.getInstance().intermediateCommit();
                    } catch (final Exception e) {
                        log.error("Error during Migration: " + e, e);
                        DBFactory.getInstance().rollback();
                    }
                    if (bgcounter > 0 && bgcounter % 150 == 0) {
                        if (log.isDebugEnabled()) {
                            log.info("Audit:Another 150 items done");
                        }
                    }

                }
                DBFactory.getInstance().intermediateCommit();
                log.info("Audit:**** Migrated " + bgcounter + " BusinessGroups. ****");

                uhd.setBooleanDataValue(TASK_MIGRATE_WIKICODE_BG_DESC, true);
                upgradeManager.setUpgradesHistory(uhd, VERSION);
            }
        }
    }

    private void migrateHomepageBio(final UpgradeManager upgradeManager, final UpgradeHistoryData uhd) {
        if (!uhd.getBooleanDataValue(TASK_MIGRATE_WIKICODE_HOMEPAGE)) {
            log.info("Audit:+---------------------------------------------------------------+");
            log.info("Audit:+...     " + TASK_MIGRATE_WIKICODE_HOMEPAGE + "     ...+");
            log.info("Audit:+---------------------------------------------------------------+");

            final List<Identity> identitiesList = secMgr.getIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null, null, null);
            final HomePageConfigManager hpcm = HomePageConfigManagerImpl.getInstance();
            int counter = 0;
            if (log.isDebugEnabled()) {
                log.info("Migrating homepage-bio for " + identitiesList.size() + " identities.");
            }

            for (final Identity identity : identitiesList) {
                try {
                    final HomePageConfig hpcfg = hpcm.loadConfigFor(identity.getName());
                    final String oldBio = hpcfg.getTextAboutMe();
                    if (StringHelper.containsNonWhitespace(oldBio)) {
                        final String newBio = migrateStringSavely(oldBio);
                        hpcfg.setTextAboutMe(newBio);
                        hpcm.saveConfigTo(identity.getName(), hpcfg);
                        counter++;
                    }
                    DBFactory.getInstance().intermediateCommit();
                } catch (final Exception e) {
                    log.error("Error during Migration: " + e, e);
                    DBFactory.getInstance().rollback();
                }
                if (counter % 150 == 0) {
                    if (log.isDebugEnabled()) {
                        log.info("Audit:Another 150 items done");
                    }
                }
            }

            DBFactory.getInstance().intermediateCommit();
            log.info("Audit:**** Migrated total " + counter + " Homepage-biography fields to new syntax. ****");
            uhd.setBooleanDataValue(TASK_MIGRATE_WIKICODE_HOMEPAGE, true);
            upgradeManager.setUpgradesHistory(uhd, VERSION);
        }
    }

    @SuppressWarnings("deprecation")
    // as it has to be used here (was before deprecation)!
    private String migrateStringSavely(final String oldValue) {
        if (log.isDebugEnabled()) {
            log.info("Audit:Old String before migration: " + oldValue);
        }
        String newValue = "";

        newValue = Formatter.formatWikiMarkup(oldValue);

        if (log.isDebugEnabled()) {
            log.info("Audit:New String after migration: " + newValue);
        }
        return newValue;
    }

    private void migrationTest() {
        final String testString = "Von support@olat.uzh.ch bin {$} oder {} oder [$] <> <$> ich auf diese Diskussion verwiesen worden. Mein ursprünglicher Beitrag von dem ein Teil aber hier schon besprochen wurde ist:> In Foren-Einträgen kann man kein <pre> verwenden, dafür aber viel tolle> Smileys.Wenn man Programmcode zeigen möchte, erhält man dann ein> unleserliches Durcheinander. Eckige Klammern der Form \"[]\" oder {} werden aus dem Beitrag gleich ganz gefiltert.> Es gibt auch keine Syntax um URLsabzugrenzen. Wenn eine URL zum> Beispiel am Ende eines Satzes steht, muss man den Punkt am Satzende> weglassen, damit er nicht als Teil der URL angesehen wird. BBCode oder> mehr von der Mediawiki-Syntax> (<http://de.wikipedia.org/wiki/Hilfe:Textgestaltung>) wäre sehr praktisch.Das Zitat illustriert auch schon die angesprochenen Probleme: Eckige Klammern und Backslash gefiltert, eine spitze schließende Klammer \">\" + eine runde schließende Klammer \")\", die zu einem zwinkernden Smiley wird, aber die spitze schließende Klammer stehen lässt (was nach dokumentierter Syntax nicht einmal korrekt ist) und die URL dessen abgrenzendes \">\" in die URL mitaufgenommen wird und deshalb auf eine nicht-existente Seite verweist. Man kann auch nicht korrekt auf URLs verweisen die ein \"&\" oder \"öffnende Eckige Klammer\" oder \"schließende eckige Klammer\" enthalten:Beispiel:http://www.srgdeutschschweiz.ch/omb_beanstandung.0.html?&tx_ttnews[pS]=1189036060&tx_ttnews[tt_news]=494&tx_ttnews[backPid]=186Die korrekte URL ist mittels http://tinyurl.com/2jq5wd ersichtlich.http://www.google.com/search?hl=en&q=search&btnG=Google+SearchDas korrekte Ergebnis wäre eine Google-Suche nach \"search\".Aus http://www.ietf.org/rfc/rfc2396.txt S. 33 \"Recommendations for Delimiting URI in Context\": > Using <> angle brackets around each URI is especially recommended as> a delimiting style for URI that contain whitespace.> (...)> For robustness, software that accepts user-typed URI should attempt> to recognize and strip both delimiters and embedded whitespace.Zu den Smileys: Wenn man schon autmatisch bestimmte Zeichenkombinationen in grafische Smileys umwandelt, muss man dem Benutzer auch die Möglichkeit geben dieses Verhalten per Checkbox vor dem Post eines Beitrages zu deaktivieren. Bilder mit zwinkernden Smileys in seinem Text zu haben, der zufällig eine der Zeichenketten enthält ist unschön. Die Syntax für ein Zitat sollte den Anfang und das Ende des Zitats kennzeichen und nicht wie derzeit vor jede zitierte Zeile hinzugefügt werden müssen und einen Parameter enthalten, der die Quelle des Zitats bzw. den Autor des Zitats angibt.Wenn ihr das jetzt also komplett überarbeitet, dann wäre es schon, wenn die angesprochenen Punkte vorher getestet würden. Es gibt ja genug funktionierende Webforen an denen man sehen kann, welche Funktionen sinnvoll sind und wie man diese mittels praktischer Syntax implementieren kann. Es sollte dann auch die Möglichkeit geben, Zeichen, die zur Syntax gehören, aber zum Beispiel auch in URLs vorkommen können, zu escapen. Ein solcher Fall wäre zum Beispiel die URL mit den eckigen Klammern in Verbindung mit BBCode. Eine Funktion wie <nowiki>...</nowiki> um über Syntax schreiben zu können, wäre auch praktisch. Mit BackslashGeschweifteKlammerAufCodeGeschweifteKlammerZu kann man zwar \\{code} erzeugen, aber man kann anscheinend einem anderen nicht sagen wie er das auch machen könnte, es sei denn man bedient sich einer umständlichen, ungenauen Umschreibung für die einzelnen Zeichen. Zweimal \"Backslash\"Code führt zu \\\\{code} .\n";
        final String newValue = migrateStringSavely(testString);
    }

    // testing the xss filter infrastructure by filtering forum messages and comparing to original value
    private void testXSSFilter() {
        log.info("Audit:+---------------------------------------------------------------+");
        log.info("Audit:                    Testing the XSS-Filter ");
        log.info("Audit:+---------------------------------------------------------------+");
        DBFactory.getInstance().intermediateCommit();

        final List<Long> allForumKeys = forumService.getAllForumKeys();
        int fCounter = 0;
        int totMCounter = 0;
        int sucCounter = 0;
        final OWASPAntiSamyXSSFilter xssFilter = new OWASPAntiSamyXSSFilter(-1, false);
        for (final Iterator<Long> iterator = allForumKeys.iterator(); iterator.hasNext();) {
            final Long forumKey = iterator.next();
            final List<Message> allMessages = forumService.getMessagesByForumID(forumKey);
            fCounter++;
            int mCounter = 0;
            for (final Iterator<Message> iterator2 = allMessages.iterator(); iterator2.hasNext();) {
                try {
                    final Message message = iterator2.next();
                    if (log.isDebugEnabled()) {
                        log.info("Audit:    - Message inside: " + message.getTitle() + " key: " + message.getKey());
                    }
                    final String msgBody = message.getBody();
                    final String filteredVal = xssFilter.filter(msgBody);
                    if (msgBody.equals(filteredVal)) {
                        sucCounter++;
                    } else {
                        final String errMsg = xssFilter.getOrPrintErrorMessages();
                        if (errMsg.equals("")) {
                            sucCounter++;
                        }
                    }
                    mCounter++;
                    if (mCounter > 0 && mCounter % 150 == 0) {
                        DBFactory.getInstance().rollback();
                        DBFactory.getInstance().closeSession();
                    }
                } catch (final Exception e) {
                    log.error("Error during XSS test: ", e);
                }
            }
            totMCounter += mCounter;
            DBFactory.getInstance().rollback();
            DBFactory.getInstance().closeSession();
        }
        final double percent = ((double) sucCounter / totMCounter * 100);
        log.info("Audit:**** Tested XSS Filter with " + fCounter + " forums with a total of " + totMCounter + " messages inside. ****");
        log.info("Audit:Successful on " + sucCounter + " messages. This is " + percent + "% correct.");
        log.info("Audit:Please send log to Roman, to fine-tune the XSSFilter");
    }

}
