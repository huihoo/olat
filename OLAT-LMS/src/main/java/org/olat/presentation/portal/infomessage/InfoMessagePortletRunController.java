package org.olat.presentation.portal.infomessage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.data.infomessage.InfoMessage;
import org.olat.lms.infomessage.InfoMessageFrontendManager;
import org.olat.lms.notifications.NotificationService;
import org.olat.lms.notifications.SubscriptionInfo;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.Windows;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.table.BaseTableDataModelWithoutFilter;
import org.olat.presentation.framework.core.components.table.ColumnDescriptor;
import org.olat.presentation.framework.core.components.table.CustomCellRenderer;
import org.olat.presentation.framework.core.components.table.CustomRenderColumnDescriptor;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableDataModel;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.dtabs.DTabs;
import org.olat.presentation.framework.core.control.generic.portal.AbstractPortletRunController;
import org.olat.presentation.framework.core.control.generic.portal.PortletDefaultTableDataModel;
import org.olat.presentation.framework.core.control.generic.portal.PortletEntry;
import org.olat.presentation.framework.core.control.generic.portal.PortletToolSortingControllerImpl;
import org.olat.presentation.framework.core.control.generic.portal.SortingCriteria;
import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.home.site.HomeSite;
import org.olat.presentation.notifications.SubscriptionListItem;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.StringHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.event.Event;
import org.olat.system.event.GenericEventListener;
import org.olat.system.spring.CoreSpringFactory;

import com.ibm.icu.util.Calendar;

/**
 * Description:<br>
 * Show the last five infos
 * <P>
 * Initial Date: 27 juil. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InfoMessagePortletRunController extends AbstractPortletRunController implements GenericEventListener {

    private final Link showAllLink;
    private TableController tableController;
    private final VelocityContainer portletVC;

    public InfoMessagePortletRunController(final WindowControl wControl, final UserRequest ureq, final Translator trans, final String portletName) {
        super(wControl, ureq, trans, portletName);

        portletVC = createVelocityContainer("infosPortlet");
        showAllLink = LinkFactory.createLink("portlet.showall", portletVC, this);

        final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
        tableConfig.setTableEmptyMessage(translate("portlet.no_messages"));
        tableConfig.setDisplayTableHeader(false);
        tableConfig.setCustomCssClass("b_portlet_table");
        tableConfig.setDisplayRowCount(false);
        tableConfig.setPageingEnabled(false);
        tableConfig.setDownloadOffered(false);
        tableConfig.setSortingEnabled(false);

        removeAsListenerAndDispose(tableController);
        tableController = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
        tableController.addColumnDescriptor(new CustomRenderColumnDescriptor("peekview.title", 0, null, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT,
                new InfoNodeRenderer(Formatter.getInstance(getLocale()))));

        listenTo(tableController);

        sortingTermsList.add(SortingCriteria.DATE_SORTING);
        sortingCriteria = getPersistentSortingConfiguration(ureq);
        sortingCriteria.setSortingTerm(SortingCriteria.DATE_SORTING);
        reloadModel(sortingCriteria);

        portletVC.put("table", tableController.getInitialComponent());

        CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, getIdentity(), InfoMessageFrontendManager.oresFrontend);

        putInitialPanel(portletVC);
    }

    @Override
    protected SortingCriteria createDefaultSortingCriteria() {
        final SortingCriteria sortingCriteria = new SortingCriteria(this.sortingTermsList);
        sortingCriteria.setAscending(false);
        return sortingCriteria;
    }

    @Override
    public synchronized void doDispose() {
        CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, InfoMessageFrontendManager.oresFrontend);
        super.doDispose();
    }

    @Override
    public void event(final Event event) {
        if ("new_info_message".equals(event.getCommand())) {
            reloadModel(sortingCriteria);
        }
    }

    @Override
    protected Comparator<InfoPortletEntry> getComparator(final SortingCriteria criteria) {
        return new InfoPortletEntryComparator(criteria);
    }

    /**
     * @param items
     * @return
     */
    private List<PortletEntry> convertToPortletEntryList(final List<InfoSubscriptionItem> infos) {
        final List<PortletEntry> convertedList = new ArrayList<PortletEntry>();
        long i = 0;
        for (final InfoSubscriptionItem info : infos) {
            convertedList.add(new InfoPortletEntry(i++, info));
        }
        return convertedList;
    }

    @Override
    protected void reloadModel(final SortingCriteria criteria) {
        final List<SubscriptionInfo> infos = getNotificationService().getSubscriptionInfos(getIdentity(), "InfoMessage");
        final List<InfoSubscriptionItem> items = new ArrayList<InfoSubscriptionItem>();
        for (final SubscriptionInfo info : infos) {
            for (final SubscriptionListItem item : info.getSubscriptionListItems()) {
                items.add(new InfoSubscriptionItem(info, item));
            }
        }
        List<PortletEntry> entries = convertToPortletEntryList(items);
        entries = getSortedList(entries, criteria);
        final InfosTableModel model = new InfosTableModel(entries);
        tableController.setTableDataModel(model);
    }

    private static NotificationService getNotificationService() {
        return (NotificationService) CoreSpringFactory.getBean(NotificationService.class);
    }

    @Override
    protected void reloadModel(final List<PortletEntry> sortedItems) {
        final InfosTableModel model = new InfosTableModel(sortedItems);
        tableController.setTableDataModel(model);
    }

    protected PortletToolSortingControllerImpl createSortingTool(final UserRequest ureq, final WindowControl wControl) {
        if (portletToolsController == null) {
            final List<PortletEntry> empty = Collections.<PortletEntry> emptyList();
            final PortletDefaultTableDataModel defaultModel = new PortletDefaultTableDataModel(empty, 2) {
                @Override
                public Object getValueAt(final int row, final int col) {
                    return null;
                }
            };
            portletToolsController = new PortletToolSortingControllerImpl(ureq, wControl, getTranslator(), sortingCriteria, defaultModel, empty);
            portletToolsController.setConfigManualSorting(false);
            portletToolsController.setConfigAutoSorting(true);
            portletToolsController.addControllerListener(this);
        }
        return portletToolsController;
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == showAllLink) {
            final DateFormat format = new SimpleDateFormat("yyyyMMdd");
            final Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.MONTH, -1);
            // the end is businessPath compatible
            final String activationCmd = "adminnotifications.[news:0][type=" + InfoMessage.class.getSimpleName() + ":0][date=" + format.format(cal.getTime()) + ":0]";
            final DTabs dts = (DTabs) Windows.getWindows(ureq).getWindow(ureq).getAttribute("DTabs");
            dts.activateStatic(ureq, HomeSite.class.getName(), activationCmd);
        }
    }

    public class InfosTableModel extends BaseTableDataModelWithoutFilter implements TableDataModel {
        private final List<PortletEntry> infos;

        public InfosTableModel(final List<PortletEntry> infos) {
            this.infos = infos;
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public int getRowCount() {
            return infos.size();
        }

        @Override
        public Object getValueAt(final int row, final int col) {
            final InfoPortletEntry entry = (InfoPortletEntry) infos.get(row);
            switch (col) {
            case 0:
                return entry.getValue();
            default:
                return entry;
            }
        }
    }

    public class InfoNodeRenderer implements CustomCellRenderer {
        private final Formatter formatter;

        public InfoNodeRenderer(final Formatter formatter) {
            this.formatter = formatter;
        }

        @Override
        public void render(final StringOutput sb, final Renderer renderer, final Object val, final Locale locale, final int alignment, final String action) {
            if (val instanceof InfoSubscriptionItem) {
                final InfoSubscriptionItem isi = (InfoSubscriptionItem) val;
                final SubscriptionListItem item = isi.getItem();
                final SubscriptionInfo info = isi.getInfo();
                // title
                final String title = info.getTitle(SubscriptionInfo.MIME_PLAIN);

                String tip = null;
                final boolean tooltip = StringHelper.containsNonWhitespace(item.getDescriptionTooltip());
                if (tooltip) {
                    final StringBuilder tipSb = new StringBuilder();
                    tipSb.append("<b>").append(title).append(":</b>").append("<br/>").append(Formatter.escWithBR(Formatter.truncate(item.getDescriptionTooltip(), 256)));
                    tip = StringEscapeUtils.escapeHtml(tipSb.toString());
                    sb.append("<span ext:qtip=\"").append(tip).append("\">");
                } else {
                    sb.append("<span>");
                }
                sb.append(Formatter.truncate(title, 30)).append("</span>&nbsp;");
                // link
                final String infoTitle = Formatter.truncate(item.getDescription(), 30);
                sb.append("<a href=\"").append(item.getLink()).append("\" class=\"o_portlet_infomessage_link\"");
                if (tooltip) {
                    sb.append("ext:qtip=\"").append(tip).append("\"");
                }
                sb.append(">").append(infoTitle).append("</a>");
            } else {
                sb.append("-");
            }
        }
    }

    /**
	 */
    @Override
    public boolean isControllerAndNotDisposed() {
        return !isDisposed();
    }

}
