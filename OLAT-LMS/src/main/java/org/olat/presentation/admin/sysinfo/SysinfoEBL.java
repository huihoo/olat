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
package org.olat.presentation.admin.sysinfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.olat.connectors.webdav.WebDAVManager;
import org.olat.system.commons.Settings;
import org.olat.system.commons.WebappHelper;
import org.springframework.stereotype.Component;

import com.anthonyeden.lib.config.Configuration;
import com.anthonyeden.lib.config.ConfigurationException;
import com.anthonyeden.lib.config.XMLConfiguration;

/**
 * Initial Date: 28.09.2011 <br>
 * 
 * @author Branislav Balaz
 */
@Component
public class SysinfoEBL {

    public Map<String, Object> getVelocityContextMap() {

        final List<Map<String, String>> properties = new LinkedList<Map<String, String>>();
        Map<String, String> m = new HashMap<String, String>();
        m.put("key", "Version");
        m.put("value", Settings.getFullVersionInfo());
        properties.add(m);

        m = new HashMap<String, String>();
        m.put("key", "isClusterMode");
        m.put("value", Settings.getClusterMode().equals("Cluster") ? "true" : "false");
        properties.add(m);

        m = new HashMap<String, String>();
        m.put("key", "nodeId");
        m.put("value", Settings.getNodeInfo().equals("") ? "N1" : Settings.getNodeInfo());
        properties.add(m);

        m = new HashMap<String, String>();
        m.put("key", "serverStartTime");
        final Date timeOfServerStartup = new Date(WebappHelper.getTimeOfServerStartup());
        m.put("value", String.valueOf(timeOfServerStartup));
        properties.add(m);

        m = new HashMap<String, String>();
        m.put("key", "Build date");
        m.put("value", String.valueOf(Settings.getBuildDate()));
        properties.add(m);

        final File baseDir = new File(WebappHelper.getContextRoot(), "..");
        m = new HashMap<String, String>();
        try {
            m.put("key", "baseDir");
            m.put("value", baseDir.getCanonicalPath());
        } catch (final IOException e1) {
            // then fall back to unresolved path
            m.put("key", "baseDir");
            m.put("value", baseDir.getAbsolutePath());
        }
        properties.add(m);

        m = new HashMap<String, String>();
        m.put("key", "WebDAVEnabled");
        final boolean webDavEnabled = WebDAVManager.getInstance().isEnabled();
        m.put("value", Boolean.toString(webDavEnabled));
        properties.add(m);

        Map<String, Object> velocityContextMap = createVelocityContextMap(properties, timeOfServerStartup);

        return velocityContextMap;
    }

    /**
     * @param properties
     * @param timeOfServerStartup
     * @return
     */
    private Map<String, Object> createVelocityContextMap(final List<Map<String, String>> properties, final Date timeOfServerStartup) {
        Map<String, Object> velocityContextMap = new HashMap<String, Object>();

        velocityContextMap.put("properties", properties);

        final File deploymentInfoProperties = new File(WebappHelper.getContextRoot(), "deployment-info.properties");

        // defaults
        velocityContextMap.put("existsActivePatchFile", false);
        velocityContextMap.put("existsDeploymentInfoProperties", false);
        velocityContextMap.put("existsPatchFile", false);

        if (deploymentInfoProperties.exists()) {
            velocityContextMap.put("existsDeploymentInfoProperties", true);
            velocityContextMap.put("fileDateDeploymentInfoProperties", new Date(deploymentInfoProperties.lastModified()));
            final List<Map<String, String>> deploymentInfoPropertiesLines = getDeploymentInfoPropertiesLines(deploymentInfoProperties);
            velocityContextMap.put("deploymentInfoPropertiesLines", deploymentInfoPropertiesLines);

            final File patchesNewest = new File(WebappHelper.getContextRoot(), "patches.xml.newest");
            if (!patchesNewest.exists()) {
                velocityContextMap.put("existsPatchFile", false);
            } else {
                velocityContextMap.put("existsPatchFile", true);
                final Date patchesFileDate = new Date(patchesNewest.lastModified());
                velocityContextMap.put("patchesFileDate", patchesFileDate);

                final boolean patchesActive = patchesFileDate.before(timeOfServerStartup);
                if (patchesActive) {
                    velocityContextMap.put("patchesActive", "yes, patch(es) active");
                } else {
                    velocityContextMap.put("patchesActive", "probably not: they are deployed but server hasn't been restarted since. Will be active after restart!");
                }

                final List<Map<String, String>> patches = new LinkedList<Map<String, String>>();
                final String baseTag = readPatchesXml(patchesNewest, patches);
                velocityContextMap.put("patchesBaseTag", baseTag);
                velocityContextMap.put("patches", patches);

                if (!patchesActive) {
                    final File[] allPatches = getAllPatches();
                    File activePatchFile = getActivePatchFile(timeOfServerStartup, allPatches);
                    if (activePatchFile != null) {
                        velocityContextMap.put("existsActivePatchFile", true);
                        velocityContextMap.put("activePatchFileName", activePatchFile.getName());
                        final Date activePatchesFileDate = new Date(activePatchFile.lastModified());
                        velocityContextMap.put("activePatchesFileDate", activePatchesFileDate);
                        final List<Map<String, String>> activePatches = new LinkedList<Map<String, String>>();
                        final String activeBaseTag = readPatchesXml(activePatchFile, activePatches);
                        velocityContextMap.put("activePatchesBaseTag", activeBaseTag);
                        velocityContextMap.put("activePatches", activePatches);
                    }
                }
            }
        }
        return velocityContextMap;
    }

    private List<Map<String, String>> getDeploymentInfoPropertiesLines(final File deploymentInfoProperties) {
        final List<Map<String, String>> deploymentInfoPropertiesLines = new LinkedList<Map<String, String>>();
        try {
            final BufferedReader r = new BufferedReader(new FileReader(deploymentInfoProperties));
            while (true) {
                final String line = r.readLine();
                if (line == null) {
                    break;
                }
                final Map<String, String> lineMap = new HashMap<String, String>();
                lineMap.put("line", line);
                deploymentInfoPropertiesLines.add(lineMap);
            }
        } catch (final IOException ioe) {
            final Map<String, String> lineMap = new HashMap<String, String>();
            lineMap.put("line", "Problems reading deployment-info.properties: " + ioe);
            deploymentInfoPropertiesLines.add(lineMap);
        }
        return deploymentInfoPropertiesLines;
    }

    private File getActivePatchFile(final Date timeOfServerStartup, final File[] allPatches) {
        File activePatchFile = null;
        for (int i = 0; i < allPatches.length; i++) {
            final File aPatchFile = allPatches[i];
            if (new Date(aPatchFile.lastModified()).before(timeOfServerStartup)) {
                // then it was potentially active at some point. Let's see if it is the newest before the
                // timeOfServerStartup
                if (activePatchFile == null) {
                    activePatchFile = aPatchFile;
                } else if (new Date(activePatchFile.lastModified()).before(new Date(aPatchFile.lastModified()))) {
                    activePatchFile = aPatchFile;
                }
            }
        }
        return activePatchFile;
    }

    private File[] getAllPatches() {
        final File[] allPatches = new File(WebappHelper.getContextRoot()).listFiles(new FilenameFilter() {

            public boolean accept(final File dir, final String name) {
                if (name == null) {
                    return false;
                } else {
                    return name.startsWith("patches.xml.");
                }
            }

        });
        return allPatches;
    }

    private String readPatchesXml(final File patchesNewest, final List<Map<String, String>> patches) {
        XMLConfiguration patchConfig = null;
        Map<String, String> m;
        try {
            patchConfig = new XMLConfiguration(patchesNewest);
            for (final Iterator<Configuration> it = patchConfig.getChildren().iterator(); it.hasNext();) {
                final Configuration aPatchConfig = it.next();
                m = new HashMap<String, String>();
                m.put("id", aPatchConfig.getAttribute("patch-id"));
                m.put("enabled", aPatchConfig.getAttribute("enabled"));
                m.put("jira", aPatchConfig.getAttribute("jira"));
                m.put("tag", aPatchConfig.getAttribute("tag"));
                m.put("title", aPatchConfig.getChildValue("description"));
                patches.add(m);
            }
            return patchConfig.getAttribute("basetag");
        } catch (final ConfigurationException e) {
            m = new HashMap<String, String>();
            m.put("id", "Problems reading patches.xml.newest: " + e);
            m.put("enabled", "");
            m.put("jira", "");
            m.put("tag", "");
            m.put("title", "");
            patches.add(m);
            return "";
        }
    }

}
