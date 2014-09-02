package org.olat.lms.upgrade;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.commons.xml.XStreamHelper;
import org.olat.lms.upgrade.upgrades.OLATUpgrade;
import org.olat.system.commons.WebappHelper;
import org.olat.system.commons.configuration.Initializable;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.event.Event;
import org.olat.system.event.FrameworkStartedEvent;
import org.olat.system.event.FrameworkStartupEventChannel;
import org.olat.system.event.GenericEventListener;
import org.olat.system.exception.StartupException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.thoughtworks.xstream.XStream;

/**
 * Description:<br>
 * Class to execute upgrade code during system startup. The idea is to have a place in the code where you can manage your migration code that is necessary because of new
 * code or because of buggy old code that left some dead stuff in the database or the fileSystem.
 * <P>
 * Initial Date: 11.09.2008 <br>
 * 
 * @author guido
 */

public abstract class UpgradeManager extends BasicManager implements Initializable, GenericEventListener {

    private static final Logger log = LoggerHelper.getLogger();

    static final String INSTALLED_UPGRADES_XML = "installed_upgrades.xml";
    static final String SYSTEM_DIR = "system";
    // WARNING: If you change those aliases your persisted data will be lost as unmarschalling will not work anymore
    private static final String HISTORYDATA_ALIAS = "org.olat.upgrade.UpgradeHistoryData";
    private static final String HISTORYDATA_ALIAS_SHORT = "UpgradeHistoryData";

    List<OLATUpgrade> upgrades;
    SortedMap<String, UpgradeHistoryData> upgradesHistories;
    private UpgradesDefinitions upgradesDefinitions;
    protected DriverManagerDataSource dataSource;
    private boolean needsUpgrade = true;
    private boolean autoUpgradeDatabase = true;
    private XStream xstream;

    /**
     * [used by spring]
     * 
     * @param dataSource
     */
    public void setDataSource(final DriverManagerDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DriverManagerDataSource getDataSource() {
        return dataSource;
    }

    /**
     * [used by spring]
     * 
     * @param upgradesDefinitions
     */
    public void setUpgradesDefinitions(final UpgradesDefinitions upgradesDefinitions) {
        this.upgradesDefinitions = upgradesDefinitions;
    }

    /**
     * [used by spring]
     * 
     * @param autoUpgradeDatabase
     */
    public void setAutoUpgradeDatabase(final boolean autoUpgradeDatabase) {
        this.autoUpgradeDatabase = autoUpgradeDatabase;
    }

    /**
     * Initialize the upgrade manager: get all upgrades from the configuration file and load the upgrade history from the olatdata directory
     */
    @Override
    public void init() {

        xstream = XStreamHelper.createXStreamInstance();
        xstream.alias(HISTORYDATA_ALIAS, UpgradeHistoryData.class);
        xstream.alias(HISTORYDATA_ALIAS_SHORT, UpgradeHistoryData.class);

        FrameworkStartupEventChannel.registerForStartupEvent(this);
        // load upgrades using spring framework
        upgrades = upgradesDefinitions.getUpgrades();
        // load history of previous upgrades using xstream
        initUpgradesHistories();
        if (needsUpgrade) {
            if (autoUpgradeDatabase) {
                runAlterDbStatements();
            } else {
                log.info("Auto upgrade of the database is disabled. Make sure you do it manually by applying the "
                        + "alter*.sql scripts and adding an entry to system/installed_upgrades.xml file.");
            }
            doPreSystemInitUpgrades();

            // post system init task are triggered by an event
            DBFactory.getInstance().commitAndCloseSession();
        }
    }

    /**
     * Execute alter db sql statements
     */
    public abstract void runAlterDbStatements();

    /**
     * Execute the pre system init code of all upgrades in the order as they were configured in the configuration file
     */
    public abstract void doPreSystemInitUpgrades();

    /**
     * Execute the post system init code of all upgrades in the order as they were configured in the configuration file
     */
    public abstract void doPostSystemInitUpgrades();

    /**
     * @param version
     *            identifier of UpgradeHistoryData
     * @return UpgradeHistoryData of the given version or null if no such history object exists
     */
    public UpgradeHistoryData getUpgradesHistory(final String version) {
        return upgradesHistories.get(version);
    }

    /**
     * Persists the UpgradeHistoryData on the file system
     * 
     * @param upgradeHistoryData
     *            UpgradeHistoryData of the given version
     * @param version
     *            identifier of UpgradeHistoryData
     */
    public void setUpgradesHistory(final UpgradeHistoryData upgradeHistoryData, final String version) {
        this.upgradesHistories.put(version, upgradeHistoryData);
        final File upgradesDir = new File(WebappHelper.getUserDataRoot(), SYSTEM_DIR);
        upgradesDir.mkdirs(); // create if not exists
        final File upgradesHistoriesFile = new File(upgradesDir, INSTALLED_UPGRADES_XML);
        XStreamHelper.writeObject(xstream, upgradesHistoriesFile, this.upgradesHistories);
    }

    /**
     * Load all persisted UpgradeHistoryData objects from the fileSystem
     */
    @SuppressWarnings("unchecked")
    protected void initUpgradesHistories() {
        final File upgradesDir = new File(WebappHelper.getUserDataRoot(), SYSTEM_DIR);
        final File upgradesHistoriesFile = new File(upgradesDir, INSTALLED_UPGRADES_XML);
        if (upgradesHistoriesFile.exists()) {
            // upgradesHistories has been (unsorted) Map before: check if already saved as SortedMap, create new TreeMap otherwise
            Map<String, UpgradeHistoryData> existingUpgradeHistory = (Map<String, UpgradeHistoryData>) XStreamHelper.readObject(xstream, upgradesHistoriesFile);
            if (existingUpgradeHistory instanceof SortedMap) {
                this.upgradesHistories = (SortedMap<String, UpgradeHistoryData>) existingUpgradeHistory;
            } else {
                this.upgradesHistories = new TreeMap<String, UpgradeHistoryData>(existingUpgradeHistory);
            }
        } else {
            if (this.upgradesHistories == null) {
                this.upgradesHistories = new TreeMap<String, UpgradeHistoryData>();
            }
            needsUpgrade = false; // looks like a new install, no upgrade necessary
            log.info("This looks like a new install or droped data, will not do any upgrades.");
            createUpgradeData();
        }
    }

    /**
     * create fake upgrade data as this is a new installation
     */
    private void createUpgradeData() {
        for (final OLATUpgrade upgrade : upgrades) {
            final UpgradeHistoryData uhd = new UpgradeHistoryData();
            uhd.setInstallationComplete(true);
            uhd.setBooleanDataValue(OLATUpgrade.TASK_DP_UPGRADE, true);
            setUpgradesHistory(uhd, upgrade.getVersion());
        }

    }

    /**
     * On any RuntimeExceptions during init. Abort loading of application. Modules should throw RuntimeExceptions if they can't live with a the given state of
     * configuration.
     * 
     * @param e
     */
    protected void abort(final Throwable e) {
        if (e instanceof StartupException) {
            final StartupException se = (StartupException) e;
            log.warn("Message: " + se.getLogMsg(), se);
            final Throwable cause = se.getCause();
            log.warn("Cause: " + (cause != null ? cause.getMessage() : "n/a"), se);
        }
        throw new RuntimeException("*** CRITICAL ERROR IN UPGRADE MANAGER. Loading aborted.", e);
    }

    @Override
    public void event(final Event event) {
        if (event instanceof FrameworkStartedEvent) {
            doPostSystemInitUpgrades();
        }
    }

}
