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
package org.olat.data.commons.database;

import org.hsqldb.util.DatabaseManagerSwing;

/**
 * Description:<br>
 * opens a GUI for accessing the database see http://hsqldb.org/web/hsqlDocsFrame.html for more documentation about this feature Usage: java DatabaseManagerSwing
 * [--options] where options include: --help show this message --driver <classname> jdbc driver class --url <name> jdbc url --user <name> username used for connection
 * --password <password> password for this user --urlid <urlid> use url/user/password/driver in rc file --rcfile <file> (defaults to 'dbmanager.rc' in home dir) --dir
 * <path> default directory --script <file> reads from script file --noexit do not call system.exit()
 * <P>
 * Initial Date: 02.03.2010 <br>
 * 
 * @author guido
 */
public class HsqldbDatabaseManagerGUI {
    // do not implement the initializable / destoryable interfaces otherwise the GUI pops up always when running unit tests

    private Thread thread;
    private final String datasourceUrl;
    private final boolean debug;

    protected HsqldbDatabaseManagerGUI(final String datasourceUrl, final boolean debug) {
        this.datasourceUrl = datasourceUrl;
        this.debug = debug;
    }

    public String getDBUrl() {
        return this.datasourceUrl;
    }

    public void stop() {
        thread.interrupt();
    }

    public void start() {
        if (debug) {
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    System.out.println("LAUNCHING HSQL DBMANAGERSWING");
                    final String[] args = { "--user", "SA", "--url", datasourceUrl };
                    try {
                        DatabaseManagerSwing.main(args);
                    } catch (final Exception e) {
                        System.out.println("Could not start hsqldb database manager GUI: " + e.getMessage());
                    }
                }
            };

            thread = new Thread(r);
            thread.start();

        }
    }

}
