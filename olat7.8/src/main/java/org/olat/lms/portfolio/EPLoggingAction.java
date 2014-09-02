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
 * Copyright (c) 1999-2009 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.lms.portfolio;

import java.lang.reflect.Field;

import org.olat.lms.activitylogging.ActionObject;
import org.olat.lms.activitylogging.ActionType;
import org.olat.lms.activitylogging.ActionVerb;
import org.olat.lms.activitylogging.BaseLoggingAction;
import org.olat.lms.activitylogging.CrudAction;
import org.olat.lms.activitylogging.ILoggingAction;
import org.olat.lms.activitylogging.OlatResourceableType;
import org.olat.lms.activitylogging.ResourceableTypeList;
import org.olat.lms.activitylogging.StringResourceableType;

/**
 * LoggingActions used by the Portfolio
 * <P>
 * PLEASE BE CAREFUL WHEN EDITING IN HERE.
 * <p>
 * Especially when modifying the ResourceableTypeList - which is a exercise where we try to predict/document/define which ResourceableTypes will later on - at runtime -
 * be available to the IUserActivityLogger.log() method.
 * <p>
 * The names of the LoggingAction should be self-describing.
 * <p>
 * Initial Date: 25.01.2011 <br>
 * 
 * @author Christian
 */
public class EPLoggingAction extends BaseLoggingAction {

    /**
     * Allow any resourceable type before portfolio [*][*]...[portfolio]
     */
    private static final ResourceableTypeList EP_RESOURCEABLE_LIST = new ResourceableTypeList().addMandatory(StringResourceableType.anyBefore,
            OlatResourceableType.portfolio);

    public static final ILoggingAction EPORTFOLIO_ARTEFACT_ADDED = new EPLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.add,
            ActionObject.portfolioartefact).setTypeList(EP_RESOURCEABLE_LIST);
    public static final ILoggingAction EPORTFOLIO_ARTEFACT_REMOVED = new EPLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.remove,
            ActionObject.portfolioartefact).setTypeList(EP_RESOURCEABLE_LIST);
    public static final ILoggingAction EPORTFOLIO_ARTEFACT_SELECTED = new EPLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.perform,
            ActionObject.portfolioartefact).setTypeList(EP_RESOURCEABLE_LIST);

    public static final ILoggingAction EPORTFOLIO_TASK_STARTED = new EPLoggingAction(ActionType.admin, CrudAction.retrieve, ActionVerb.open, ActionObject.portfoliotask)
            .setTypeList(EP_RESOURCEABLE_LIST);
    public static final ILoggingAction EPORTFOLIO_TASK_FINISHED = new EPLoggingAction(ActionType.admin, CrudAction.create, ActionVerb.perform, ActionObject.portfoliotask)
            .setTypeList(EP_RESOURCEABLE_LIST);

    public static final ILoggingAction EPORTFOLIO_MAP_CREATED = new EPLoggingAction(ActionType.admin, CrudAction.create, ActionVerb.add, ActionObject.portfoliomap)
            .setTypeList(EP_RESOURCEABLE_LIST);
    public static final ILoggingAction EPORTFOLIO_MAP_REMOVED = new EPLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.remove, ActionObject.portfoliomap)
            .setTypeList(EP_RESOURCEABLE_LIST);

    /**
     * This static constructor's only use is to set the javaFieldIdForDebug on all of the LoggingActions defined in this class.
     * <p>
     * This is used to simplify debugging - as it allows to issue (technical) log statements where the name of the LoggingAction Field is written.
     */
    static {
        final Field[] fields = EPLoggingAction.class.getDeclaredFields();
        if (fields != null) {
            for (int i = 0; i < fields.length; i++) {
                final Field field = fields[i];
                if (field.getType() == EPLoggingAction.class) {
                    try {
                        final EPLoggingAction aLoggingAction = (EPLoggingAction) field.get(null);
                        aLoggingAction.setJavaFieldIdForDebug(field.getName());
                    } catch (final IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (final IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Simple wrapper calling super<init>
     * 
     */
    EPLoggingAction(final ActionType resourceActionType, final CrudAction action, final ActionVerb actionVerb, final ActionObject actionObject) {
        super(resourceActionType, action, actionVerb, actionObject.name());
    }

}
