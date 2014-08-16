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
package org.olat.lms.properties;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jfree.util.Log;
import org.olat.data.forum.Forum;
import org.olat.data.portfolio.structure.PortfolioStructureMap;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.lms.forum.ForumService;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.SyncerCallback;
import org.olat.system.coordinate.SyncerExecutor;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * TODO: this class should be finally merged with already existing PropertyService
 * 
 * <P>
 * Initial Date: 16.09.2011 <br>
 * 
 * @author Branislav Balaz
 */
@Component
public class PropertyManagerEBL {

    @Autowired
    PropertyManager propertyManager;
    @Autowired
    CoordinatorManager coordinatorManager;
    @Autowired
    ForumService forumService;
    @Autowired
    EPFrontendManager ePFrontendManager;

    /**
     * <code>PROP_CAT_BG_COLLABTOOLS</code> identifies properties concerning Collaboration Tools
     */
    public static final String PROP_CAT_BG_COLLABTOOLS = "collabtools";
    public static final String KEY_PORTFOLIO = "portfolioMapKey";
    public static final String KEY_FORUM = "forumKey";
    public static final String PROPERTY_CAT_AFTER_LOGIN = "afterLogin";
    public static final String KEY_NEWS = "news";
    public static final String KEY_CALENDAR_ACCESS = "cal";
    public static final String KEY_UN_SUBSCRIPTION = "CourseCalendarSubscription::notdesired";
    public static final String KEY_SUBSCRIPTION = "CourseCalendarSubscription::subs";

    private final String SEPARATOR = ",";
    private final String PROPERTY_NAME_COMIC_START = "comicStart";
    private final String PROPERTY_CATEGORY_MACARTNEY_PORTAL = "macartneyPortal";
    private static final Logger log = LoggerHelper.getLogger();

    @SuppressWarnings("unchecked")
    public List<PropertyImpl> getProperties(final PropertyParameterObject propertyParameterObject) {

        return propertyManager.listProperties(propertyParameterObject.getIdentity(), propertyParameterObject.getGroup(), propertyParameterObject.getResourceTypeName(),
                propertyParameterObject.getResourceTypeId(), propertyParameterObject.getCategory(), propertyParameterObject.getName());

    }

    @SuppressWarnings("unchecked")
    public List<PropertyImpl> getAllResourceTypeNames() {
        return propertyManager.getAllResourceTypeNames();
    }

    public void createOrUpdatePropertyForCollaborationTool(final PropertyParameterObject propertyParameterObject) {

        coordinatorManager.getCoordinator().getSyncer().doInSync(propertyParameterObject.getResourceable(), new SyncerExecutor() {
            @Override
            public void execute() {
                // was: synchronized (CollaborationTools.class) {
                PropertyImpl property = findProperty(propertyParameterObject);
                ;
                if (property == null) {
                    // not existing -> create it
                    property = propertyManager.createPropertyInstance(null, null, propertyParameterObject.getResourceable(), propertyParameterObject.getCategory(),
                            propertyParameterObject.getName(), null, null, propertyParameterObject.getStringValue(), null);
                } else {
                    // if existing -> update to desired value
                    property.setStringValue(propertyParameterObject.getStringValue());
                }
                // property becomes persistent
                propertyManager.saveProperty(property);
            }
        });
    }

    /**
     * Generic find method.
     * 
     * @param identity
     * @param group
     * @param category
     * @param name
     * @return The property or null if no property found
     * @throws AssertException
     *             if more than one property matches.
     */
    public PropertyImpl findProperty(final PropertyParameterObject propertyParameterObject) {
        return propertyManager.findProperty(propertyParameterObject.getIdentity(), propertyParameterObject.getGroup(), propertyParameterObject.getResourceable(),
                propertyParameterObject.getCategory(), propertyParameterObject.getName());
    }

    private PropertyImpl createAndSaveProperty(final PropertyParameterObject propertyParameterObject) {
        PropertyImpl property = propertyManager.createPropertyInstance(propertyParameterObject.getIdentity(), propertyParameterObject.getGroup(),
                propertyParameterObject.getResourceable(), propertyParameterObject.getCategory(), propertyParameterObject.getName(),
                propertyParameterObject.getFloatValue(), propertyParameterObject.getLongValue(), propertyParameterObject.getStringValue(),
                propertyParameterObject.getTextValue());
        propertyManager.saveProperty(property);
        return property;

    }

    /**
     * Delete properties. IMPORTANT: if an argument is null, then it will be not considered in the delete statement, which means not only the record having a "null" value
     * will be deleted, but all. At least one of the arguments must be not null, otherwhise an assert exception will be thrown. If you want to delete all properties of
     * this ressource, then use the deleteAllProperties() method.
     * 
     * @param identity
     * @param group
     * @param category
     * @param name
     */

    public void deleteProperties(final PropertyParameterObject propertyParameterObject) {
        if (propertyParameterObject.getIdentity() == null && propertyParameterObject.getGroup() == null && propertyParameterObject.getCategory() == null
                && propertyParameterObject.getName() == null) {
            throw new AssertException("deleteProperties musst have at least one non-null parameter. Seems to be a programm bug");
        }
        propertyManager.deleteProperties(propertyParameterObject.getIdentity(), propertyParameterObject.getGroup(), propertyParameterObject.getResourceable(),
                propertyParameterObject.getCategory(), propertyParameterObject.getName());
    }

    public String lookupCollaborationToolsNews(final PropertyParameterObject propertyParameterObject) {
        final PropertyImpl property = findProperty(propertyParameterObject);
        if (property == null) { // no entry
            return null;
        }
        // read the text value of the existing property
        final String text = property.getTextValue();
        return text;
    }

    public void saveCollaborationToolsNews(final PropertyParameterObject propertyParameterObject) {
        final PropertyImpl property = findProperty(propertyParameterObject);
        if (property == null) { // create a new one
            createAndSaveProperty(propertyParameterObject);
        } else { // modify the existing one
            property.setTextValue(propertyParameterObject.getTextValue());
            updateProperty(property);
        }
    }

    public Long lookupCollaborationToolsCalendarAccess(final PropertyParameterObject propertyParameterObject) {
        final PropertyImpl property = findProperty(propertyParameterObject);
        if (property == null) { // no entry
            return null;
        }
        // read the long value of the existing property
        return property.getLongValue();
    }

    public void saveCollaborationToolsCalendarAccess(final PropertyParameterObject propertyParameterObject) {
        final PropertyImpl property = findProperty(propertyParameterObject);
        if (property == null) { // create a new one
            createAndSaveProperty(propertyParameterObject);
        } else { // modify the existing one
            property.setLongValue(propertyParameterObject.getLongValue());
            updateProperty(property);
        }
    }

    public Forum getCollaborationToolsForum(final PropertyParameterObject propertyParameterObject) {

        final Forum forum = coordinatorManager.getCoordinator().getSyncer().doInSync(propertyParameterObject.getResourceable(), new SyncerCallback<Forum>() {
            @Override
            public Forum execute() {

                // was: synchronized (CollaborationTools.class) {
                Forum aforum;
                Long forumKey;
                PropertyImpl forumKeyProperty = findProperty(propertyParameterObject);
                if (forumKeyProperty == null) {
                    // First call of forum, create new forum and save
                    aforum = forumService.addAForum();
                    forumKey = aforum.getKey();
                    if (log.isDebugEnabled()) {
                        log.debug("created new forum in collab tools: foid::" + forumKey.longValue() + " for ores::"
                                + propertyParameterObject.getResourceable().getResourceableTypeName() + "/"
                                + propertyParameterObject.getResourceable().getResourceableId());
                    }
                    PropertyParameterObject localPropertyParameterObject = new PropertyParameterObject.Builder().resourceable(propertyParameterObject.getResourceable())
                            .category(propertyParameterObject.getCategory()).name(propertyParameterObject.getName()).longValue(forumKey).build();
                    createAndSaveProperty(localPropertyParameterObject);

                } else {
                    // Forum does already exist, load forum with key
                    // from properties
                    forumKey = forumKeyProperty.getLongValue();
                    aforum = forumService.loadForum(forumKey);
                    if (aforum == null) {
                        throw new AssertException("Unable to load forum with key " + forumKey.longValue() + " for ores "
                                + propertyParameterObject.getResourceable().getResourceableTypeName() + " with key "
                                + propertyParameterObject.getResourceable().getResourceableId());
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("loading forum in collab tools from properties: foid::" + forumKey.longValue() + " for ores::"
                                + propertyParameterObject.getResourceable().getResourceableTypeName() + "/"
                                + propertyParameterObject.getResourceable().getResourceableId());
                    }
                }

                return aforum;
            }
        });

        return forum;

    }

    public PortfolioStructureMap getCollaborationToolsPortfolioStructureMap(final PropertyParameterObject propertyParameterObject) {

        final PortfolioStructureMap map = coordinatorManager.getCoordinator().getSyncer()
                .doInSync(propertyParameterObject.getResourceable(), new SyncerCallback<PortfolioStructureMap>() {
                    @Override
                    public PortfolioStructureMap execute() {
                        PortfolioStructureMap aMap;
                        Long mapKey;
                        PropertyImpl mapKeyProperty = findProperty(propertyParameterObject);
                        if (mapKeyProperty == null) {
                            // First call of forum, create new forum and save
                            aMap = ePFrontendManager.createAndPersistPortfolioDefaultMap(propertyParameterObject.getGroup(),
                                    propertyParameterObject.getGroup().getName(), propertyParameterObject.getGroup().getDescription());
                            mapKey = aMap.getKey();
                            if (log.isDebugEnabled()) {
                                log.debug("created new portfolio map in collab tools: foid::" + mapKey + " for ores::"
                                        + propertyParameterObject.getResourceable().getResourceableTypeName() + "/"
                                        + propertyParameterObject.getResourceable().getResourceableId());
                            }
                            PropertyParameterObject localPropertyParameterObject = new PropertyParameterObject.Builder()
                                    .resourceable(propertyParameterObject.getResourceable()).category(propertyParameterObject.getCategory())
                                    .name(propertyParameterObject.getName()).longValue(mapKey).group(propertyParameterObject.getGroup()).build();
                            createAndSaveProperty(localPropertyParameterObject);
                        } else {
                            // Forum does already exist, load forum with key
                            // from properties
                            mapKey = mapKeyProperty.getLongValue();
                            aMap = (PortfolioStructureMap) ePFrontendManager.loadPortfolioStructureByKey(mapKey);
                            if (aMap == null) {
                                throw new AssertException("Unable to load portfolio map with key " + mapKey + " for ores "
                                        + propertyParameterObject.getResourceable().getResourceableTypeName() + " with key "
                                        + propertyParameterObject.getResourceable().getResourceableId());
                            }
                            if (log.isDebugEnabled()) {
                                log.debug("loading portfolio map in collab tools from properties: foid::" + mapKey + " for ores::"
                                        + propertyParameterObject.getResourceable().getResourceableTypeName() + "/"
                                        + propertyParameterObject.getResourceable().getResourceableId());
                            }
                        }
                        return aMap;
                    }
                });

        return map;
    }

    private void updateProperty(final PropertyImpl p) {
        propertyManager.updateProperty(p);
    }

    @SuppressWarnings("unchecked")
    public List<String> getCourseCalendarSubscriptionProperty(final PropertyParameterObject propertyParameterObject) {
        List<String> infoSubscriptions = new ArrayList<String>();
        List<PropertyImpl> properties = findProperties(propertyParameterObject);

        if (properties.size() > 1) {
            Log.error("more than one property found, something went wrong, deleting them and starting over.");
            for (PropertyImpl prop : properties) {
                propertyManager.deleteProperty(prop);
            }

        } else if (properties.size() == 0l) {
            createAndSaveProperty(propertyParameterObject);
            properties = findProperties(propertyParameterObject);
        }
        String value = properties.get(0).getTextValue();

        if (value != null && !value.equals("")) {
            String[] subscriptions = properties.get(0).getTextValue().split(SEPARATOR);
            infoSubscriptions.addAll(Arrays.asList(subscriptions));
        }

        return infoSubscriptions;
    }

    public void persistCourseCalendarSubscriptions(final List<String> infoSubscriptions, final PropertyParameterObject propertyParameterObject) {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < infoSubscriptions.size(); i++) {
            sb.append(infoSubscriptions.get(i));
            if (i < infoSubscriptions.size() - 1) {
                sb.append(",");
            }
        }

        List<PropertyImpl> properties = findProperties(propertyParameterObject);
        PropertyImpl p = properties.get(0);
        p.setTextValue(sb.toString());
        propertyManager.saveProperty(p);

    }

    @SuppressWarnings("unchecked")
    private List<PropertyImpl> findProperties(final PropertyParameterObject propertyParameterObject) {
        return propertyManager.findProperties(propertyParameterObject.getIdentity(), propertyParameterObject.getGroup(), propertyParameterObject.getResourceable(),
                propertyParameterObject.getCategory(), propertyParameterObject.getName());
    }

    public long getComicStartDate(final long comicStartDate, String resourceableType) {

        Long returnedComicStartDate = CoordinatorManager.getInstance().getCoordinator().getSyncer()
                .doInSync(OresHelper.createOLATResourceableType(resourceableType), new SyncerCallback<Long>() {

                    @Override
                    @SuppressWarnings("synthetic-access")
                    public Long execute() {
                        if (comicStartDate != 0) {
                            // then we shouldn't have gotten here in the first place, but we were
                            // racing with another userrequest in the same VM!
                            // let's quit quickly ;)
                            return comicStartDate;
                        }
                        PropertyParameterObject propertyParameterObject = new PropertyParameterObject.Builder().category(PROPERTY_CATEGORY_MACARTNEY_PORTAL)
                                .name(PROPERTY_NAME_COMIC_START).build();
                        // load comic start date only once
                        PropertyImpl p = findProperty(propertyParameterObject);
                        if (p == null) {
                            // wow first time ever, save current date
                            p = createAndSaveProperty(propertyParameterObject);
                        }
                        return Long.valueOf(p.getCreationDate().getTime());
                    }

                });

        return returnedComicStartDate.longValue();
    }

    public Long getLastRunTimeForAfterLoginInterceptionController(final List<PropertyImpl> ctrlPropList, final String ctrlName) {
        for (final PropertyImpl prop : ctrlPropList) {
            if (prop.getName().equals(ctrlName)) {
                return new Long(prop.getLastModified().getTime() / 1000);
            }
        }
        return new Long(0);
    }

    public boolean getRunStateForAfterLoginInterceptionController(final List<PropertyImpl> ctrlPropList, final String ctrlName) {
        for (final PropertyImpl prop : ctrlPropList) {
            if (prop.getName().equals(ctrlName)) {
                return Boolean.parseBoolean(prop.getStringValue());
            }
        }
        return false;
    }

    public void saveOrUpdatePropertyForAfterLoginInterceptionController(final List<PropertyImpl> ctrlPropList, final PropertyParameterObject propertyParameterObject) {
        for (final PropertyImpl prop : ctrlPropList) {
            if (prop.getName().equals(propertyParameterObject.getName())) {
                prop.setStringValue(Boolean.TRUE.toString());
                propertyManager.updateProperty(prop);
                return;
            }
        }
        createAndSaveProperty(propertyParameterObject);
    }
}
