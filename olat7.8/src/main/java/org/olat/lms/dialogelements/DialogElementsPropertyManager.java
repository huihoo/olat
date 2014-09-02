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

package org.olat.lms.dialogelements;

/**
 * Description:<br>
 * TODO: guido Class Description for DialogElement
 * <P>
 * Initial Date: 14.11.2005 <br>
 * 
 * @author guido
 */
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.commons.xml.XStreamHelper;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.lms.core.notification.service.NotificationService;
import org.olat.lms.core.notification.service.PublishEventTO;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.DialogCourseNode;
import org.olat.lms.course.properties.CoursePropertyManager;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.SyncerExecutor;
import org.olat.system.exception.AssertException;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

import com.thoughtworks.xstream.XStream;

public class DialogElementsPropertyManager extends BasicManager {

    private static final Logger log = LoggerHelper.getLogger();

    public static final String PROPERTY_NAME = "fileDialog";
    // create with spring
    private static final DialogElementsPropertyManager dialogElementsManager = new DialogElementsPropertyManager();
    private final XStream xStream;
    // WARNING: do not change these alias names!
    private final static String ELEMENTS_ALIAS = "org.olat.modules.dialog.DialogPropertyElements";
    private final static String ELEMENTS_ALIAS_SHORT = "DialogPropertyElements";
    private final static String ELEMENT_ALIAS = "org.olat.modules.dialog.DialogElement";
    private final static String ELEMENT_ALIAS_SHORT = "DialogElement";

    // private constr. for singleton
    private DialogElementsPropertyManager() {
        xStream = XStreamHelper.createXStreamInstance();
        xStream.alias(ELEMENTS_ALIAS, DialogPropertyElements.class);
        xStream.alias(ELEMENTS_ALIAS_SHORT, DialogPropertyElements.class);
        xStream.alias(ELEMENT_ALIAS, DialogElement.class);
        xStream.alias(ELEMENT_ALIAS_SHORT, DialogElement.class);
    }

    /**
     * @return single instance
     */
    public static DialogElementsPropertyManager getInstance() {
        return dialogElementsManager;
    }

    /**
     * @param userCourseEnv
     * @param courseNode
     * @return an empty DialogPropertyElements if noting found or the populated object
     */
    public DialogPropertyElements findDialogElements(final CoursePropertyManager coursePropMgr, final CourseNode courseNode) {
        final PropertyImpl property = coursePropMgr.findCourseNodeProperty(courseNode, null, null, PROPERTY_NAME);
        if (property == null) {
            return new DialogPropertyElements(PROPERTY_NAME);
        } else {
            return (DialogPropertyElements) xStream.fromXML(property.getTextValue());
        }
    }

    /**
     * Find all DialogElements for a certain coursenode
     * 
     * @param courseId
     * @param courseNodeId
     * @return a Object containing a collection of DialogElements
     */
    public DialogPropertyElements findDialogElements(final Long courseId, final String courseNodeId) {
        final PropertyImpl prop = findProperty(courseId, courseNodeId);
        if (prop == null) {
            return new DialogPropertyElements(PROPERTY_NAME);
        }
        return (DialogPropertyElements) xStream.fromXML(prop.getTextValue());
    }

    /**
     * @param userCourseEnv
     * @param courseNode
     * @param fileDialogId
     * @return
     */
    public DialogElement findDialogElement(final CoursePropertyManager coursePropMgr, final CourseNode courseNode, final Long forumKey) {
        final DialogPropertyElements elements = findDialogElements(coursePropMgr, courseNode);
        if (elements != null) {
            final List list = elements.getDialogPropertyElements();
            for (final Iterator iter = list.iterator(); iter.hasNext();) {
                final DialogElement element = (DialogElement) iter.next();
                if (element.getForumKey().equals(forumKey)) {
                    return element;
                }
            }
        } else {
            throw new OLATRuntimeException(this.getClass(), "trying to find property element, but no properties yet exists: coursenode " + courseNode.getShortTitle()
                    + "(" + courseNode.getIdent() + ") and forum with key: " + forumKey, null);
        }
        // no match
        return null;
    }

    /**
     * Deletes a single dialog element which are all in one property stored. The property will still exist even if it contains no elements
     * 
     * @param userCourseEnv
     * @param courseNode
     * @param fileDialogId
     */
    public void deleteDialogElement(final CoursePropertyManager coursePropMgr, final CourseNode courseNode, final Long forumKey) {
        final DialogPropertyElements elements = findDialogElements(coursePropMgr, courseNode);
        if (elements != null) {
            final List list = elements.getDialogPropertyElements();
            for (final Iterator iter = list.iterator(); iter.hasNext();) {
                final DialogElement element = (DialogElement) iter.next();
                if (element.getForumKey().equals(forumKey)) {
                    // o_clusterOK by:ld
                    final OLATResourceable courseNodeResourceable = OresHelper.createOLATResourceableInstance(DialogCourseNode.class, new Long(courseNode.getIdent()));
                    CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(courseNodeResourceable, new SyncerExecutor() {
                        @Override
                        public void execute() {
                            list.remove(element);
                            final String dialogElementsAsXML = xStream.toXML(elements);
                            final PropertyImpl property = coursePropMgr.findCourseNodeProperty(courseNode, null, null, PROPERTY_NAME);

                            property.setTextValue(dialogElementsAsXML);
                            coursePropMgr.updateProperty(property);
                        }
                    });
                    break;
                }
            }
        } else {
            throw new OLATRuntimeException(this.getClass(), "trying to delete property element, but no properties yet exist for course node: " + courseNode.getIdent(),
                    null);
        }
    }

    /**
     * persits a new added dialog element in the course node property
     * 
     * @param userCourseEnv
     * @param courseNode
     * @param identity
     * @param subsContext
     * @param forumKey
     * @param fileName
     * @param authorUsername
     * @param fileDialogId
     */
    public void addDialogElement(final CoursePropertyManager coursePropMgr, final CourseNode courseNode, final DialogElement element) {
        // o_clusterOK by:ld (it was assumed that the courseNodeId - used for constructing the olatResourceable - is unique over all courses)
        final OLATResourceable courseNodeResourceable = OresHelper.createOLATResourceableInstance(DialogCourseNode.class, new Long(courseNode.getIdent()));
        CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(courseNodeResourceable, new SyncerExecutor() {
            @Override
            public void execute() {
                final DialogPropertyElements dialogProps = findDialogElements(coursePropMgr, courseNode);

                dialogProps.addElement(element);
                final String dialogElementsAsXML = xStream.toXML(dialogProps);
                PropertyImpl property = coursePropMgr.findCourseNodeProperty(courseNode, null, null, PROPERTY_NAME);
                if (property == null) {
                    property = coursePropMgr.createCourseNodePropertyInstance(courseNode, null, null, PROPERTY_NAME, null, null, null, dialogElementsAsXML);
                    coursePropMgr.saveProperty(property);
                } else {
                    property.setTextValue(dialogElementsAsXML);
                    coursePropMgr.updateProperty(property);
                }
            }
        });

    }

    public void addDialogElementAndNotify(final CoursePropertyManager coursePropMgr, final CourseNode courseNode, final DialogElement element,
            PublishEventTO publishEventTO) {
        addDialogElement(coursePropMgr, courseNode, element);
        publishEvent(publishEventTO);
    }

    private void publishEvent(PublishEventTO publishEventTO) {
        try {
            getNotificationService().publishEvent(publishEventTO);
        } catch (RuntimeException e) {
            log.error("publishEvent failed: ", e);
        }
    }

    private static NotificationService getNotificationService() {
        return (NotificationService) CoreSpringFactory.getBean(NotificationService.class);
    }

    private PropertyImpl findProperty(final Long courseId, final String courseNodeId) {
        final PropertyManager propMrg = PropertyManager.getInstance();
        final String category = "NID:dial::" + courseNodeId;
        final List elements = propMrg.findProperties(null, null, "CourseModule", courseId, category, PROPERTY_NAME);
        if (elements.size() == 0) {
            return null; // no match
        }
        if (elements.size() != 1) {
            throw new AssertException("Found more then one property for a course node 'dialog element' which should never happen!");
        }
        return (PropertyImpl) elements.get(0);
    }

    /**
     * Delete the file dialog course node poperty
     * 
     * @param courseId
     * @param courseNodeId
     */
    public void deleteProperty(final Long courseId, final String courseNodeId) {
        final PropertyImpl prop = findProperty(courseId, courseNodeId);
        final PropertyManager propMrg = PropertyManager.getInstance();
        if (prop != null) {
            propMrg.deleteProperty(prop);
        }
    }

}
