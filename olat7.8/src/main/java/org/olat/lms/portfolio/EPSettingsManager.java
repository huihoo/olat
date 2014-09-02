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
package org.olat.lms.portfolio;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.xml.XStreamHelper;
import org.olat.data.portfolio.artefact.EPFilterSettings;
import org.olat.data.portfolio.structure.PortfolioStructure;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.logging.log4j.LoggerHelper;

import com.thoughtworks.xstream.XStream;

/**
 * Description:<br>
 * Manager to handle users settings depending the ePortfolio
 * <P>
 * Initial Date: 30.11.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPSettingsManager extends BasicManager {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String EP_FILTER_SETTINGS = "EPFilterSettings";
    private static final String EPORTFOLIO_ARTEFACTS_ATTRIBUTES = "eportfolio-artAttrib";
    private static final String EPORTFOLIO_FILTER_SETTINGS = "eportfolio-filterSettings";
    private static final String EPORTFOLIO_LASTUSED_STRUCTURE = "eportfolio-lastStruct";
    private static final String EPORTFOLIO_ARTEFACTS_VIEWMODE = "eportfolio-artViewMode";
    private static final String EPORTFOLIO_CATEGORY = "eportfolio";

    /**
     * [spring]
     */
    private EPSettingsManager() {
        //
    }

    @SuppressWarnings("unchecked")
    public Map<String, Boolean> getArtefactAttributeConfig(final Identity ident) {
        final PropertyManager pm = PropertyManager.getInstance();
        final PropertyImpl p = pm.findProperty(ident, null, null, EPORTFOLIO_CATEGORY, EPORTFOLIO_ARTEFACTS_ATTRIBUTES);
        TreeMap<String, Boolean> disConfig;
        if (p == null) {
            disConfig = new TreeMap<String, Boolean>();
            // TODO: epf: maybe there is a better way to get the default set of
            // attributes from an artefact ?!
            disConfig.put("artefact.author", true);
            disConfig.put("artefact.description", false);
            disConfig.put("artefact.reflexion", false);
            disConfig.put("artefact.source", true);
            disConfig.put("artefact.sourcelink", false);
            disConfig.put("artefact.title", true);
            disConfig.put("artefact.date", true);
            disConfig.put("artefact.tags", true);
            disConfig.put("artefact.used.in.maps", true);
            disConfig.put("artefact.handlerdetails", false);
        } else {
            final XStream xStream = XStreamHelper.createXStreamInstance();
            disConfig = (TreeMap<String, Boolean>) xStream.fromXML(p.getTextValue());
        }
        return disConfig;
    }

    public void setArtefactAttributeConfig(final Identity ident, final Map<String, Boolean> artAttribConfig) {
        final PropertyManager pm = PropertyManager.getInstance();
        PropertyImpl p = pm.findProperty(ident, null, null, EPORTFOLIO_CATEGORY, EPORTFOLIO_ARTEFACTS_ATTRIBUTES);
        if (p == null) {
            p = pm.createUserPropertyInstance(ident, EPORTFOLIO_CATEGORY, EPORTFOLIO_ARTEFACTS_ATTRIBUTES, null, null, null, null);
        }
        final XStream xStream = XStreamHelper.createXStreamInstance();
        final String artAttribXML = xStream.toXML(artAttribConfig);
        p.setTextValue(artAttribXML);
        pm.saveProperty(p);
    }

    @SuppressWarnings("unchecked")
    public List<EPFilterSettings> getSavedFilterSettings(final Identity ident) {
        final PropertyManager pm = PropertyManager.getInstance();
        final PropertyImpl p = pm.findProperty(ident, null, null, EPORTFOLIO_CATEGORY, EPORTFOLIO_FILTER_SETTINGS);
        List<EPFilterSettings> result = new ArrayList<EPFilterSettings>();
        if (p == null) {
            result.add(new EPFilterSettings());
        } else {
            final XStream xStream = XStreamHelper.createXStreamInstance();
            xStream.aliasType(EP_FILTER_SETTINGS, EPFilterSettings.class);
            try {
                result = (List<EPFilterSettings>) xStream.fromXML(p.getTextValue());
            } catch (final Exception e) {
                // it's not a live critical part
                log.warn("Cannot read filter settings", e);
            }
        }
        return result;
    }

    public void setSavedFilterSettings(final Identity ident, final List<EPFilterSettings> filterList) {
        final PropertyManager pm = PropertyManager.getInstance();
        PropertyImpl p = pm.findProperty(ident, null, null, EPORTFOLIO_CATEGORY, EPORTFOLIO_FILTER_SETTINGS);
        if (p == null) {
            p = pm.createUserPropertyInstance(ident, EPORTFOLIO_CATEGORY, EPORTFOLIO_FILTER_SETTINGS, null, null, null, null);
        }
        // don't persist filters without a name
        for (final Iterator<EPFilterSettings> iterator = filterList.iterator(); iterator.hasNext();) {
            final EPFilterSettings epFilterSettings = iterator.next();
            if (!StringHelper.containsNonWhitespace(epFilterSettings.getFilterName())) {
                iterator.remove();
            }
        }
        final XStream xStream = XStreamHelper.createXStreamInstance();
        xStream.aliasType(EP_FILTER_SETTINGS, EPFilterSettings.class);
        final String filterListXML = xStream.toXML(filterList);
        p.setTextValue(filterListXML);
        pm.saveProperty(p);
    }

    public void deleteFilterFromUsersList(final Identity ident, final String filterID) {
        final List<EPFilterSettings> usersFilters = getSavedFilterSettings(ident);
        for (final Iterator<EPFilterSettings> iterator = usersFilters.iterator(); iterator.hasNext();) {
            final EPFilterSettings epFilterSettings = iterator.next();
            if (epFilterSettings.getFilterId().equals(filterID)) {
                iterator.remove();
            }
        }
        setSavedFilterSettings(ident, usersFilters);
    }

    public String getUsersPreferedArtefactViewMode(final Identity ident, final String context) {
        final PropertyManager pm = PropertyManager.getInstance();
        final PropertyImpl p = pm.findProperty(ident, null, null, EPORTFOLIO_CATEGORY, EPORTFOLIO_ARTEFACTS_VIEWMODE + "." + context);
        if (p != null) {
            final String preferedMode = p.getStringValue();
            return preferedMode;
        }
        return null;
    }

    public void setUsersPreferedArtefactViewMode(final Identity ident, final String preferedMode, final String context) {
        final PropertyManager pm = PropertyManager.getInstance();
        PropertyImpl p = pm.findProperty(ident, null, null, EPORTFOLIO_CATEGORY, EPORTFOLIO_ARTEFACTS_VIEWMODE + "." + context);
        if (p == null) {
            p = pm.createUserPropertyInstance(ident, EPORTFOLIO_CATEGORY, EPORTFOLIO_ARTEFACTS_VIEWMODE + "." + context, null, null, null, null);
        }
        p.setStringValue(preferedMode);
        pm.saveProperty(p);
    }

    public void setUsersLastUsedPortfolioStructure(final Identity ident, final PortfolioStructure struct) {
        final PropertyManager pm = PropertyManager.getInstance();
        PropertyImpl p = pm.findProperty(ident, null, null, EPORTFOLIO_CATEGORY, EPORTFOLIO_LASTUSED_STRUCTURE);
        if (p == null) {
            p = pm.createUserPropertyInstance(ident, EPORTFOLIO_CATEGORY, EPORTFOLIO_LASTUSED_STRUCTURE, null, null, null, null);
        }
        p.setLongValue(struct.getKey());
        pm.saveProperty(p);
    }

    public Long getUsersLastUsedPortfolioStructureKey(final Identity ident) {
        final PropertyManager pm = PropertyManager.getInstance();
        final PropertyImpl p = pm.findProperty(ident, null, null, EPORTFOLIO_CATEGORY, EPORTFOLIO_LASTUSED_STRUCTURE);
        if (p != null) {
            return p.getLongValue();
        }
        return null;
    }

}
