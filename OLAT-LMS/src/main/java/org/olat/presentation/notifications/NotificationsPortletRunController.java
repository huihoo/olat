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

package org.olat.presentation.notifications;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.olat.data.notifications.Publisher;
import org.olat.data.notifications.Subscriber;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.BusinessControlFactory;
import org.olat.lms.course.CourseModule;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.notifications.NotificationService;
import org.olat.lms.notifications.NotificationServiceEBL;
import org.olat.lms.notifications.SubscriptionInfo;
import org.olat.lms.notifications.event.PersistsEvent;
import org.olat.presentation.calendar.CalendarController;
import org.olat.presentation.framework.common.ControllerFactory;
import org.olat.presentation.framework.common.NewControllerFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.Windows;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.table.ColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.Table;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableEvent;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.util.ComponentUtil;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.dtabs.DTab;
import org.olat.presentation.framework.core.control.generic.dtabs.DTabs;
import org.olat.presentation.framework.core.control.generic.portal.AbstractPortletRunController;
import org.olat.presentation.framework.core.control.generic.portal.PortletDefaultTableDataModel;
import org.olat.presentation.framework.core.control.generic.portal.PortletEntry;
import org.olat.presentation.framework.core.control.generic.portal.PortletToolSortingControllerImpl;
import org.olat.presentation.framework.core.control.generic.portal.SortingCriteria;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.home.site.HomeSite;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.event.Event;
import org.olat.system.event.GenericEventListener;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Run view controller for the notifications list portlet
 * <P>
 * Initial Date: 11.07.2005 <br>
 * 
 * @author gnaegi
 */
public class NotificationsPortletRunController extends AbstractPortletRunController implements GenericEventListener {
    private static final Logger log = LoggerHelper.getLogger();

    private static final String CMD_LAUNCH = "cmd.launch";

    private final TableController tableCtr;
    // private NotificationsMiniTableModel notificationListModel;
    private NotificationsPortletTableDataModel notificationListModel;
    private final VelocityContainer notificationsVC;
    private boolean needsModelReload = false;
    private List<Subscriber> notificationsList;
    private final Link showAllLink;
    private BusinessGroupService businessGroupService;
    private final Date compareDate;
    private final NotificationServiceEBL notificationServiceEBL;

    /**
     * Constructor
     * 
     * @param ureq
     * @param component
     */
    public NotificationsPortletRunController(final WindowControl wControl, final UserRequest ureq, final Translator trans, final String portletName) {
        super(wControl, ureq, trans, portletName);
        notificationServiceEBL = new NotificationServiceEBL();

        businessGroupService = (BusinessGroupService) CoreSpringFactory.getBean(BusinessGroupService.class);
        sortingTermsList.add(SortingCriteria.TYPE_SORTING);
        sortingTermsList.add(SortingCriteria.ALPHABETICAL_SORTING);
        sortingTermsList.add(SortingCriteria.DATE_SORTING);

        this.notificationsVC = this.createVelocityContainer("notificationsPortlet");
        showAllLink = LinkFactory.createLink("notificationsPortlet.showAll", notificationsVC, this);

        final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
        tableConfig.setTableEmptyMessage(trans.translate("notificationsPortlet.nonotifications"));
        tableConfig.setDisplayTableHeader(false);
        tableConfig.setCustomCssClass("b_portlet_table");
        tableConfig.setDisplayRowCount(false);
        tableConfig.setPageingEnabled(false);
        tableConfig.setDownloadOffered(false);
        // disable the default sorting for this table
        tableConfig.setSortingEnabled(false);
        tableCtr = new TableController(tableConfig, ureq, getWindowControl(), trans);
        listenTo(tableCtr);

        // dummy header key, won't be used since setDisplayTableHeader is set to false
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("notificationsPortlet.bgname", 0, CMD_LAUNCH, trans.getLocale()));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("notificationsPortlet.type", 1, null, trans.getLocale(), ColumnDescriptor.ALIGNMENT_RIGHT));

        this.sortingCriteria = getPersistentSortingConfiguration(ureq);
        NotificationService notificationService = getNotificationService();
        // default use the interval
        compareDate = notificationServiceEBL.getNewsStartDate(ureq.getIdentity());
        reloadModel(sortingCriteria);

        this.notificationsVC.put("table", tableCtr.getInitialComponent());
        // notify us whenever we will be shown on screen shortly, so that we can reload the model if we received a subscription changed event in the meantime
        ComponentUtil.registerForValidateEvents(notificationsVC, this);

        putInitialPanel(notificationsVC);

        notificationService.registerAsListener(this, ureq.getIdentity());
    }

    private static NotificationService getNotificationService() {
        return (NotificationService) CoreSpringFactory.getBean(NotificationService.class);
    }

    private List<PortletEntry> getAllPortletEntries() {
        notificationsList = notificationServiceEBL.getSubscribersWithNews(identity, locale, compareDate);
        return convertNotificationToPortletEntryList(notificationsList);
    }

    private List<PortletEntry> convertNotificationToPortletEntryList(final List<Subscriber> items) {
        final List<PortletEntry> convertedList = new ArrayList<PortletEntry>();
        final Iterator<Subscriber> listIterator = items.iterator();
        while (listIterator.hasNext()) {
            convertedList.add(new SubscriberPortletEntry(listIterator.next()));
        }
        return convertedList;
    }

    /**
     * org.olat.presentation.framework.control.generic.portal.SortingCriteria)
     */
    @Override
    protected void reloadModel(final SortingCriteria sortingCriteria) {
        if (sortingCriteria.getSortingType() == SortingCriteria.AUTO_SORTING) {
            notificationsList = notificationServiceEBL.getSubscribersWithNews(identity, locale, compareDate);
            notificationsList = getSortedList(notificationsList, sortingCriteria);
            final List<PortletEntry> entries = convertNotificationToPortletEntryList(notificationsList);

            final Map subscriptionMap = notificationServiceEBL.getSubscriptionMap(getIdentity(), getLocale(), compareDate);
            notificationListModel = new NotificationsPortletTableDataModel(entries, locale, subscriptionMap);
            tableCtr.setTableDataModel(notificationListModel);
        } else {
            reloadModel(this.getPersistentManuallySortedItems());
        }
    }

    /**
	 */
    @Override
    protected void reloadModel(final List<PortletEntry> sortedItems) {
        final Map subscriptionMap = notificationServiceEBL.getSubscriptionMap(getIdentity(), getLocale(), compareDate);
        notificationListModel = new NotificationsPortletTableDataModel(sortedItems, locale, subscriptionMap);
        tableCtr.setTableDataModel(notificationListModel);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == showAllLink) {
            // activate homes tab in top navigation and active bookmarks menu item
            final DTabs dts = (DTabs) Windows.getWindows(ureq).getWindow(ureq).getAttribute("DTabs");
            // was brasato:: getWindowControl().getDTabs().activateStatic(ureq, HomeSite.class.getName(), "adminnotifications");
            dts.activateStatic(ureq, HomeSite.class.getName(), "adminnotifications");
        } else if (event == ComponentUtil.VALIDATE_EVENT && needsModelReload) {
            // updateTableModel(ureq.getLocale(), ureq.getIdentity());
            reloadModel(sortingCriteria);
            needsModelReload = false;
        }
    }

    /**
     * org.olat.system.event.control.Event)
     */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        super.event(ureq, source, event);
        if (source == tableCtr) {
            if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
                final TableEvent te = (TableEvent) event;
                final String actionid = te.getActionId();
                if (actionid.equals(CMD_LAUNCH)) {
                    final int rowid = te.getRowId();
                    final Subscriber sub = notificationListModel.getSubscriberAt(rowid);
                    if (actionid.equals(CMD_LAUNCH)) {
                        final Publisher publisher = sub.getPublisher();
                        if (!getNotificationService().isPublisherValid(publisher)) {
                            getWindowControl().setError(getTranslator().translate("error.publisherdeleted"));
                        } else {
                            final Long resId = publisher.getResId();
                            String resName = getResourceName(publisher);
                            final OLATResourceable ores = OresHelper.createOLATResourceableInstance(resName, resId);
                            final String title = getNotificationService().getNotificationsHandler(publisher).createTitleInfo(sub, getLocale());
                            final DTabs dts = (DTabs) Windows.getWindows(ureq).getWindow(ureq).getAttribute("DTabs");
                            // was brasato:: DTabs dts = getWindowControl().getDTabs();
                            DTab dt = dts.getDTab(ores);
                            final String subidentifier = publisher.getSubidentifier();
                            if (dt == null) {
                                // does not yet exist -> create and add
                                dt = dts.createDTab(ores, title);
                                if (dt == null) {
                                    return;
                                }
                                final Controller launchController = ControllerFactory.createLaunchController(ores, subidentifier, ureq, dt.getWindowControl(), false);

                                // try with the new factory controller too
                                boolean newFactory = false;
                                if (launchController == null) {
                                    try {
                                        final String resourceUrl = "[" + resName + ":0][notifications]";
                                        final BusinessControl bc = BusinessControlFactory.getInstance().createFromString(resourceUrl);
                                        final WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, dt.getWindowControl());
                                        NewControllerFactory.getInstance().launch(ureq, bwControl);
                                        newFactory = true;
                                    } catch (final Exception ex) {
                                        // fail silently
                                    }
                                }

                                if (newFactory) {
                                    // hourra
                                } else if (launchController == null) {
                                    // not possible to launch anymore
                                    getWindowControl().setWarning(getTranslator().translate("warn.nolaunch"));
                                } else {
                                    dt.setController(launchController);
                                    dts.addDTab(dt);
                                    // null: do not reactivate to a certain view here, this happened in ControllerFactory.createLaunchController
                                    dts.activate(ureq, dt, null);
                                }
                            } else {
                                dts.activate(ureq, dt, subidentifier);
                            }
                        }
                    }
                }
            }
        }
    }

    private String getResourceName(Publisher publisher) {
        String resName = publisher.getResName();
        final String subidentifier = publisher.getSubidentifier();
        if (subidentifier.equals(CalendarController.ACTION_CALENDAR_COURSE)) {
            resName = CourseModule.ORES_TYPE_COURSE;
        }
        if (subidentifier.equals(CalendarController.ACTION_CALENDAR_GROUP)) {
            resName = businessGroupService.loadBusinessGroup(publisher.getResId(), true).getResourceableTypeName();
        }
        return resName;
    }

    /**
	 */
    @Override
    protected void doDispose() {
        super.doDispose();
        getNotificationService().deregisterAsListener(this);
    }

    @Override
    public void event(final Event event) {
        // check if our tablemodel -is- affected (see NotificationsManagerImpl where the event is fired),
        // (if we are subscriber of the publisher which data has changed)
        if (event instanceof PersistsEvent) {
            final PersistsEvent pe = (PersistsEvent) event;
            if (pe.isAtLeastOneKeyInList(notificationsList)) {
                needsModelReload = true;
            }
        }
    }

    /**
     * Retrieves the persistent sortingCriteria and the persistent manually sorted, if any, creates the table model for the manual sorting, and instantiates the
     * PortletToolSortingControllerImpl.
     * 
     * @param ureq
     * @param wControl
     * @return a PortletToolSortingControllerImpl instance.
     */
    protected PortletToolSortingControllerImpl createSortingTool(final UserRequest ureq, final WindowControl wControl) {
        if (portletToolsController == null) {

            final Map<Subscriber, SubscriptionInfo> subscriptionMap = notificationServiceEBL.getSubscriptionMap(ureq.getIdentity(), ureq.getLocale(), compareDate);
            final List<PortletEntry> entries = getAllPortletEntries();
            final PortletDefaultTableDataModel tableDataModel = new NotificationsManualSortingTableDataModel(entries, ureq.getLocale(), subscriptionMap);
            final List sortedItems = getPersistentManuallySortedItems();

            portletToolsController = new PortletToolSortingControllerImpl(ureq, wControl, getTranslator(), sortingCriteria, tableDataModel, sortedItems);
            portletToolsController.setConfigManualSorting(true);
            portletToolsController.setConfigAutoSorting(true);
            portletToolsController.addControllerListener(this);
        }
        return portletToolsController;
    }

    private List getPersistentManuallySortedItems() {
        final List<PortletEntry> entries = getAllPortletEntries();
        return this.getPersistentManuallySortedItems(entries);
    }

    @Override
    protected Comparator getComparator(final SortingCriteria sortingCriteria) {
        return new Comparator() {
            @Override
            public int compare(final Object o1, final Object o2) {
                final Subscriber subscriber1 = (Subscriber) o1;
                final Subscriber subscriber2 = (Subscriber) o2;
                int comparisonResult = 0;
                if (sortingCriteria.getSortingTerm() == SortingCriteria.ALPHABETICAL_SORTING) {
                    comparisonResult = collator.compare(subscriber1.getPublisher().getResName(), subscriber1.getPublisher().getResName());
                } else if (sortingCriteria.getSortingTerm() == SortingCriteria.DATE_SORTING) {
                    comparisonResult = subscriber1.getLastModified().compareTo(subscriber1.getLastModified());
                } else if (sortingCriteria.getSortingTerm() == SortingCriteria.TYPE_SORTING) {
                    final String type1 = ControllerFactory.translateResourceableTypeName(subscriber1.getPublisher().getType(), getTranslator().getLocale());
                    final String type2 = ControllerFactory.translateResourceableTypeName(subscriber2.getPublisher().getType(), getTranslator().getLocale());
                    comparisonResult = type1.compareTo(type2);
                }
                if (!sortingCriteria.isAscending()) {
                    // if not isAscending return (-comparisonResult)
                    return -comparisonResult;
                }
                return comparisonResult;
            }
        };
    }

    /**
     * PortletDefaultTableDataModel implementation for the current portlet.
     * <P>
     * Initial Date: 10.12.2007 <br>
     * 
     * @author Lavinia Dumitrescu
     */
    private class NotificationsPortletTableDataModel extends PortletDefaultTableDataModel {
        private final Locale locale;
        private final Map subToSubInfo;

        public NotificationsPortletTableDataModel(final List<PortletEntry> objects, final Locale locale, final Map subToSubInfo) {
            super(objects, 2);
            this.locale = locale;
            this.subToSubInfo = subToSubInfo;
        }

        @Override
        public Object getValueAt(final int row, final int col) {
            final PortletEntry entry = getObject(row);
            final Subscriber subscriber = (Subscriber) entry.getValue();
            final Publisher pub = subscriber.getPublisher();
            switch (col) {
            case 0:
                final Object subsInfoObj = subToSubInfo.get(subscriber);
                if (subsInfoObj instanceof SubscriptionInfo) {
                    final SubscriptionInfo subsInfo = (SubscriptionInfo) subsInfoObj;
                    final int newsCount = subsInfo.countSubscriptionListItems();
                    if (newsCount == 1) {
                        return translate("notificationsPortlet.single.news.in", subsInfo.getTitle(SubscriptionInfo.MIME_PLAIN));
                    } else {
                        return translate("notificationsPortlet.multiple.news.in", new String[] { newsCount + "", subsInfo.getTitle(SubscriptionInfo.MIME_PLAIN) });
                    }
                }
                return "";
            case 1:
                final String innerType = pub.getType();
                final String typeName = ControllerFactory.translateResourceableTypeName(innerType, locale);
                return typeName;
            default:
                return "ERROR";
            }
        }

        public Subscriber getSubscriberAt(final int row) {
            final Subscriber subscriber = (Subscriber) getObject(row).getValue();
            return subscriber;
        }
    }

    /**
     * Description:<br>
     * TODO: Lavinia Dumitrescu Class Description for NotificationsManualSortingTableDataModel
     * <P>
     * Initial Date: 04.12.2007 <br>
     * 
     * @author Lavinia Dumitrescu
     */
    private class NotificationsManualSortingTableDataModel extends PortletDefaultTableDataModel {
        private final Locale locale;
        private final Map<Subscriber, SubscriptionInfo> subToSubInfo;

        /**
         * @param objects
         * @param locale
         */
        public NotificationsManualSortingTableDataModel(final List<PortletEntry> objects, final Locale locale, final Map<Subscriber, SubscriptionInfo> subToSubInfo) {
            super(objects, 3);
            this.locale = locale;
            this.subToSubInfo = subToSubInfo;
        }

        /**
		 */
        @Override
        public final Object getValueAt(final int row, final int col) {
            final PortletEntry entry = getObject(row);
            final Subscriber subscriber = (Subscriber) entry.getValue();
            final Publisher pub = subscriber.getPublisher();
            switch (col) {
            case 0: {
                final SubscriptionInfo subsInfo = subToSubInfo.get(subscriber);
                return subsInfo.getTitle(SubscriptionInfo.MIME_PLAIN);
            }
            case 1: {
                final SubscriptionInfo subsInfo = subToSubInfo.get(subscriber);
                if (!subsInfo.hasNews()) {
                    return "-";
                }
                return subsInfo.getSpecificInfo(SubscriptionInfo.MIME_HTML, locale);
            }
            case 2:
                final String innerType = pub.getType();
                final String typeName = ControllerFactory.translateResourceableTypeName(innerType, locale);
                return typeName;
            default:
                return "error";
            }
        }

    }

    /**
     * PortletEntry
     * <P>
     * Initial Date: 10.12.2007 <br>
     * 
     * @author Lavinia Dumitrescu
     */
    private class SubscriberPortletEntry implements PortletEntry<Subscriber> {
        private final Subscriber value;
        private final Long key;

        public SubscriberPortletEntry(final Subscriber group) {
            value = group;
            key = group.getKey();
        }

        @Override
        public Long getKey() {
            return key;
        }

        @Override
        public Subscriber getValue() {
            return value;
        }
    }

    /**
	 */
    @Override
    public boolean isControllerAndNotDisposed() {
        return !isDisposed();
    }

}
