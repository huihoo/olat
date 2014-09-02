package org.olat.lms.activitylogging;

import java.lang.reflect.Field;

public class StatisticLoggingAction extends BaseLoggingAction {

    public static final ILoggingAction VIEW_NODE_STATISTIC = new StatisticLoggingAction(ActionType.admin, CrudAction.retrieve, ActionVerb.view, ActionObject.statistic)
            .setTypeList(new ResourceableTypeList().addMandatory(OlatResourceableType.course, StringResourceableType.statisticManager,
                    StringResourceableType.statisticType, OlatResourceableType.node));

    public static final ILoggingAction VIEW_TOTAL_OF_NODES_STATISTIC = new StatisticLoggingAction(ActionType.admin, CrudAction.retrieve, ActionVerb.view,
            ActionObject.statistic).setTypeList(new ResourceableTypeList().addMandatory(OlatResourceableType.course, StringResourceableType.statisticManager,
            StringResourceableType.statisticType));

    public static final ILoggingAction VIEW_TOTAL_BY_VALUE_STATISTIC = new StatisticLoggingAction(ActionType.admin, CrudAction.retrieve, ActionVerb.view,
            ActionObject.statistic).setTypeList(new ResourceableTypeList().addMandatory(OlatResourceableType.course, StringResourceableType.statisticManager,
            StringResourceableType.statisticType, StringResourceableType.statisticColumn));

    public static final ILoggingAction VIEW_TOTAL_TOTAL_STATISTIC = new StatisticLoggingAction(ActionType.admin, CrudAction.retrieve, ActionVerb.view,
            ActionObject.statistic).setTypeList(new ResourceableTypeList().addMandatory(OlatResourceableType.course, StringResourceableType.statisticManager,
            StringResourceableType.statisticType));

    /**
     * This static constructor's only use is to set the javaFieldIdForDebug on all of the LoggingActions defined in this class.
     * <p>
     * This is used to simplify debugging - as it allows to issue (technical) log statements where the name of the LoggingAction Field is written.
     */
    static {
        final Field[] fields = StatisticLoggingAction.class.getDeclaredFields();
        if (fields != null) {
            for (int i = 0; i < fields.length; i++) {
                final Field field = fields[i];
                if (field.getType() == StatisticLoggingAction.class) {
                    try {
                        final StatisticLoggingAction aLoggingAction = (StatisticLoggingAction) field.get(null);
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
    StatisticLoggingAction(final ActionType resourceActionType, final CrudAction action, final ActionVerb actionVerb, final ActionObject actionObject) {
        super(resourceActionType, action, actionVerb, actionObject.name());
    }

}
