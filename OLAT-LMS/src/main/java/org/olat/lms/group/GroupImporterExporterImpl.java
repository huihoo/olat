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
package org.olat.lms.group;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.group.BusinessGroup;
import org.olat.data.group.area.BGArea;
import org.olat.data.group.area.BGAreaDao;
import org.olat.data.group.area.BGAreaDaoImpl;
import org.olat.data.group.context.BGContext;
import org.olat.data.group.context.BGContextDao;
import org.olat.data.group.context.BGContextDaoImpl;
import org.olat.presentation.collaboration.CollaborationTools;
import org.olat.presentation.collaboration.CollaborationToolsFactory;
import org.olat.system.exception.AssertException;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.anthonyeden.lib.config.Configuration;
import com.anthonyeden.lib.config.ConfigurationException;
import com.anthonyeden.lib.config.Dom4jConfiguration;
import com.anthonyeden.lib.config.MutableConfiguration;
import com.anthonyeden.lib.config.XMLConfiguration;

/**
 * import / export of groups
 * 
 * <P>
 * Initial Date: 30.06.2011 <br>
 * 
 * @author guido
 */
@Component
public class GroupImporterExporterImpl implements GroupImporterExporter {

    private static final Logger log = LoggerHelper.getLogger();

    static final String EXPORT_ATTR_NAME = "name";
    static final String EXPORT_ATTR_MAX_PARTICIPATS = "maxParticipants";
    static final String EXPORT_ATTR_MIN_PARTICIPATS = "minParticipants";
    static final String EXPORT_ATTR_WAITING_LIST = "waitingList";
    static final String EXPORT_ATTR_AUTO_CLOSE_RANKS = "autoCloseRanks";
    static final String EXPORT_KEY_AREA_RELATION = "AreaRelation";
    static final String EXPORT_KEY_GROUP = "Group";
    static final String EXPORT_KEY_GROUP_COLLECTION = "GroupCollection";
    static final String EXPORT_KEY_AREA = "Area";
    static final String EXPORT_KEY_AREA_COLLECTION = "AreaCollection";
    static final String EXPORT_KEY_ROOT = "OLATGroupExport";
    static final String EXPORT_KEY_DESCRIPTION = "Description";
    static final String EXPORT_KEY_COLLABTOOLS = "CollabTools";
    static final String EXPORT_KEY_SHOW_OWNERS = "showOwners";
    static final String EXPORT_KEY_SHOW_PARTICIPANTS = "showParticipants";
    static final String EXPORT_KEY_SHOW_WAITING_LIST = "showWaitingList";
    static final String EXPORT_KEY_CALENDAR_ACCESS = "calendarAccess";
    static final String EXPORT_KEY_NEWS = "info";

    @Autowired
    private BusinessGroupService businessGroupService;

    /**
     * [spring only]
     */
    private GroupImporterExporterImpl() {
        //
    }

    /**
	 */
    @Override
    public void importGroups(final BGContext context, final File fGroupExportXML) {
        if (!fGroupExportXML.exists()) {
            return;
        }

        Configuration groupConfig = null;
        try {
            groupConfig = new XMLConfiguration(fGroupExportXML);
        } catch (final ConfigurationException ce) {
            throw new OLATRuntimeException("Error importing group config.", ce);
        }
        if (!groupConfig.getName().equals(EXPORT_KEY_ROOT)) {
            throw new AssertException("Invalid group export file. Root does not match.");
        }

        // get areas
        final BGAreaDao am = BGAreaDaoImpl.getInstance();
        final Configuration confAreas = groupConfig.getChild(EXPORT_KEY_AREA_COLLECTION);
        if (confAreas != null) {
            final List areas = confAreas.getChildren(EXPORT_KEY_AREA);
            for (final Iterator iter = areas.iterator(); iter.hasNext();) {
                final Configuration area = (Configuration) iter.next();
                final String areaName = area.getAttribute(EXPORT_ATTR_NAME);
                final String areaDesc = area.getChildValue(EXPORT_KEY_DESCRIPTION);
                am.createAndPersistBGAreaIfNotExists(areaName, areaDesc, context);
            }
        }

        // TODO fg: import group rights

        // get groups
        final Configuration confGroups = groupConfig.getChild(EXPORT_KEY_GROUP_COLLECTION);
        if (confGroups != null) {
            final List groups = confGroups.getChildren(EXPORT_KEY_GROUP);
            for (final Iterator iter = groups.iterator(); iter.hasNext();) {
                // create group
                final Configuration group = (Configuration) iter.next();
                final String groupName = group.getAttribute(EXPORT_ATTR_NAME);
                final String groupDesc = group.getChildValue(EXPORT_KEY_DESCRIPTION);

                // get min/max participants
                Integer groupMinParticipants = null;
                final String sMinParticipants = group.getAttribute(EXPORT_ATTR_MIN_PARTICIPATS);
                if (sMinParticipants != null) {
                    groupMinParticipants = new Integer(sMinParticipants);
                }
                Integer groupMaxParticipants = null;
                final String sMaxParticipants = group.getAttribute(EXPORT_ATTR_MAX_PARTICIPATS);
                if (sMaxParticipants != null) {
                    groupMaxParticipants = new Integer(sMaxParticipants);
                }

                // waiting list configuration
                final String waitingListConfig = group.getAttribute(EXPORT_ATTR_WAITING_LIST);
                Boolean waitingList = null;
                if (waitingListConfig == null) {
                    waitingList = Boolean.FALSE;
                } else {
                    waitingList = Boolean.valueOf(waitingListConfig);
                }
                final String enableAutoCloseRanksConfig = group.getAttribute(EXPORT_ATTR_AUTO_CLOSE_RANKS);
                Boolean enableAutoCloseRanks = null;
                if (enableAutoCloseRanksConfig == null) {
                    enableAutoCloseRanks = Boolean.FALSE;
                } else {
                    enableAutoCloseRanks = Boolean.valueOf(enableAutoCloseRanksConfig);
                }

                final BusinessGroup newGroup = businessGroupService.createAndPersistBusinessGroup(context.getGroupType(), null, groupName, groupDesc,
                        groupMinParticipants, groupMaxParticipants, waitingList, enableAutoCloseRanks, context);

                // get tools config
                final Configuration toolsConfig = group.getChild(EXPORT_KEY_COLLABTOOLS);
                final CollaborationTools ct = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(newGroup);
                for (int i = 0; i < CollaborationTools.TOOLS.length; i++) {
                    final String sTool = toolsConfig.getAttribute(CollaborationTools.TOOLS[i]);
                    if (sTool != null) {
                        ct.setToolEnabled(CollaborationTools.TOOLS[i], sTool.equals("true") ? true : false);
                    }
                }
                if (group.getAttribute(EXPORT_KEY_CALENDAR_ACCESS) != null) {
                    final Long calendarAccess = Long.valueOf(group.getAttribute(EXPORT_KEY_CALENDAR_ACCESS));
                    ct.saveCalendarAccess(calendarAccess);
                }
                if (group.getAttribute(EXPORT_KEY_NEWS) != null) {
                    final String info = group.getAttribute(EXPORT_KEY_NEWS);
                    ct.saveNews(info);
                }

                // get memberships
                final List memberships = group.getChildren(EXPORT_KEY_AREA_RELATION);
                for (final Iterator iterator = memberships.iterator(); iterator.hasNext();) {
                    final Configuration areaRelation = (Configuration) iterator.next();
                    final BGArea area = am.findBGArea(areaRelation.getValue(), context);
                    if (area == null) {
                        throw new AssertException("Group-Area-Relationship in export, but area was not created during import.");
                    }
                    am.addBGToBGArea(newGroup, area);
                }

                // get properties
                boolean showOwners = true;
                boolean showParticipants = true;
                boolean showWaitingList = true;
                if (group.getAttribute(EXPORT_KEY_SHOW_OWNERS) != null) {
                    showOwners = Boolean.valueOf(group.getAttribute(EXPORT_KEY_SHOW_OWNERS));
                }
                if (group.getAttribute(EXPORT_KEY_SHOW_PARTICIPANTS) != null) {
                    showParticipants = Boolean.valueOf(group.getAttribute(EXPORT_KEY_SHOW_PARTICIPANTS));
                }
                if (group.getAttribute(EXPORT_KEY_SHOW_WAITING_LIST) != null) {
                    showWaitingList = Boolean.valueOf(group.getAttribute(EXPORT_KEY_SHOW_WAITING_LIST));
                }
                final BusinessGroupPropertyManager bgPropertyManager = new BusinessGroupPropertyManager(newGroup);
                bgPropertyManager.updateDisplayMembers(showOwners, showParticipants, showWaitingList);
            }
        }
    }

    /**
	 */
    @Override
    public void exportGroups(final BGContext context, final File fExportFile) {
        if (context == null) {
            return; // nothing to do... says Florian.
        }
        final Dom4jConfiguration root = new Dom4jConfiguration(EXPORT_KEY_ROOT);

        // export areas
        final MutableConfiguration confAreas = root.addChild(EXPORT_KEY_AREA_COLLECTION);
        final BGAreaDao am = BGAreaDaoImpl.getInstance();
        final List areas = am.findBGAreasOfBGContext(context);
        for (final Iterator iter = areas.iterator(); iter.hasNext();) {
            final BGArea area = (BGArea) iter.next();
            final MutableConfiguration newArea = confAreas.addChild(EXPORT_KEY_AREA);
            newArea.addAttribute(EXPORT_ATTR_NAME, area.getName());
            newArea.addChild(EXPORT_KEY_DESCRIPTION, area.getDescription());
        }

        // TODO fg: export group rights

        // export groups
        final MutableConfiguration confGroups = root.addChild(EXPORT_KEY_GROUP_COLLECTION);
        final BGContextDao cm = BGContextDaoImpl.getInstance();
        final List groups = cm.getGroupsOfBGContext(context);
        for (final Iterator iter = groups.iterator(); iter.hasNext();) {
            final BusinessGroup group = (BusinessGroup) iter.next();
            exportGroup(fExportFile, confGroups, group);
        }

        saveGroupConfiguration(fExportFile, root);
    }

    @Override
    public void exportGroup(final BusinessGroup group, final File fExportFile) {
        final Dom4jConfiguration root = new Dom4jConfiguration(EXPORT_KEY_ROOT);
        final MutableConfiguration confGroups = root.addChild(EXPORT_KEY_GROUP_COLLECTION);
        exportGroup(fExportFile, confGroups, group);
        saveGroupConfiguration(fExportFile, root);
    }

    private void exportGroup(final File fExportFile, final MutableConfiguration confGroups, final BusinessGroup group) {
        final MutableConfiguration newGroup = confGroups.addChild(EXPORT_KEY_GROUP);
        newGroup.addAttribute(EXPORT_ATTR_NAME, group.getName());
        if (group.getMinParticipants() != null) {
            newGroup.addAttribute(EXPORT_ATTR_MIN_PARTICIPATS, group.getMinParticipants());
        }
        if (group.getMaxParticipants() != null) {
            newGroup.addAttribute(EXPORT_ATTR_MAX_PARTICIPATS, group.getMaxParticipants());
        }
        if (group.getWaitingListEnabled() != null) {
            newGroup.addAttribute(EXPORT_ATTR_WAITING_LIST, group.getWaitingListEnabled());
        }
        if (group.getAutoCloseRanksEnabled() != null) {
            newGroup.addAttribute(EXPORT_ATTR_AUTO_CLOSE_RANKS, group.getAutoCloseRanksEnabled());
        }
        newGroup.addChild(EXPORT_KEY_DESCRIPTION, group.getDescription());
        // collab tools
        final MutableConfiguration toolsConfig = newGroup.addChild(EXPORT_KEY_COLLABTOOLS);
        final CollaborationTools ct = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(group);
        for (int i = 0; i < CollaborationTools.TOOLS.length; i++) {
            toolsConfig.addAttribute(CollaborationTools.TOOLS[i], ct.isToolEnabled(CollaborationTools.TOOLS[i]) ? "true" : "false");
        }
        final Long calendarAccess = ct.lookupCalendarAccess();
        if (calendarAccess != null) {
            newGroup.addAttribute(EXPORT_KEY_CALENDAR_ACCESS, calendarAccess);
        }
        final String info = ct.lookupNews();
        if (info != null && !info.trim().equals("")) {
            newGroup.addAttribute(EXPORT_KEY_NEWS, info.trim());
        }

        log.debug("fExportFile.getParent()=" + fExportFile.getParent());
        ct.archive(fExportFile.getParent());
        // export membership
        final List bgAreas = BGAreaDaoImpl.getInstance().findBGAreasOfBusinessGroup(group);
        for (final Iterator iterator = bgAreas.iterator(); iterator.hasNext();) {
            final BGArea areaRelation = (BGArea) iterator.next();
            final MutableConfiguration newGroupAreaRel = newGroup.addChild(EXPORT_KEY_AREA_RELATION);
            newGroupAreaRel.setValue(areaRelation.getName());
        }
        // export properties
        final BusinessGroupPropertyManager bgPropertyManager = new BusinessGroupPropertyManager(group);
        final boolean showOwners = bgPropertyManager.showOwners();
        final boolean showParticipants = bgPropertyManager.showPartips();
        final boolean showWaitingList = bgPropertyManager.showWaitingList();

        newGroup.addAttribute(EXPORT_KEY_SHOW_OWNERS, showOwners);
        newGroup.addAttribute(EXPORT_KEY_SHOW_PARTICIPANTS, showParticipants);
        newGroup.addAttribute(EXPORT_KEY_SHOW_WAITING_LIST, showWaitingList);
    }

    private void saveGroupConfiguration(final File fExportFile, final Dom4jConfiguration root) {
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(fExportFile);
            final BufferedOutputStream bos = FileUtils.getBos(fOut);
            root.save(bos);
            bos.flush();
            bos.close();
        } catch (final IOException ioe) {
            throw new OLATRuntimeException("Error writing group configuration during group export.", ioe);
        } catch (final ConfigurationException cfe) {
            throw new OLATRuntimeException("Error writing group configuration during group export.", cfe);
        } finally {
            FileUtils.closeSafely(fOut);
        }
    }

}
