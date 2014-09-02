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
 * Copyright (c) 2008 frentix GmbH, Switzerland<br>
 * <p>
 */

package org.olat.presentation.course.nodes.info;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

import org.olat.data.infomessage.InfoMessage;
import org.olat.lms.commons.context.BusinessControlFactory;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.course.CourseModule;
import org.olat.lms.course.nodes.InfoCourseNode;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.infomessage.InfoMessageFrontendManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.table.BaseTableDataModelWithoutFilter;
import org.olat.presentation.framework.core.components.table.ColumnDescriptor;
import org.olat.presentation.framework.core.components.table.CustomCellRenderer;
import org.olat.presentation.framework.core.components.table.CustomRenderColumnDescriptor;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableDataModel;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.Settings;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Peekview for info messages
 * <P>
 * Initial Date: 3 aug. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InfoPeekViewController extends BasicController {

    private final OLATResourceable ores;
    private final InfoCourseNode courseNode;

    private TableController tableController;

    public InfoPeekViewController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv, final InfoCourseNode courseNode) {
        super(ureq, wControl);

        this.courseNode = courseNode;
        final Long resId = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
        ores = OresHelper.createOLATResourceableInstance(CourseModule.class, resId);

        init(ureq);

        putInitialPanel(tableController.getInitialComponent());
    }

    private void init(final UserRequest ureq) {
        final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
        tableConfig.setTableEmptyMessage(translate("peekview.noInfos"));
        tableConfig.setDisplayTableHeader(false);
        tableConfig.setCustomCssClass("b_portlet_table");
        tableConfig.setDisplayRowCount(false);
        tableConfig.setPageingEnabled(false);
        tableConfig.setDownloadOffered(false);
        tableConfig.setSortingEnabled(false);

        removeAsListenerAndDispose(tableController);
        tableController = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
        tableController.addColumnDescriptor(new CustomRenderColumnDescriptor("peekview.title", 0, null, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT,
                new InfoNodeRenderer()));

        final String resSubPath = this.courseNode.getIdent();
        final InfoMessageFrontendManager infoMessageFrontendManager = (InfoMessageFrontendManager) CoreSpringFactory.getBean(InfoMessageFrontendManager.class);
        final List<InfoMessage> infos = infoMessageFrontendManager.loadInfoMessageByResource(ores, resSubPath, null, null, null, 0, 5);

        final InfosTableModel model = new InfosTableModel(infos);
        tableController.setTableDataModel(model);
        listenTo(tableController);
    }

    @Override
    protected void doDispose() {
        //
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        //
    }

    private class InfosTableModel extends BaseTableDataModelWithoutFilter implements TableDataModel {
        private final List<InfoMessage> infos;

        public InfosTableModel(final List<InfoMessage> infos) {
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
            final InfoMessage info = infos.get(row);
            switch (col) {
            case 0:
                return info;
            default:
                return null;
            }
        }
    }

    public String getUrl(final String businessPath) {
        final BusinessControlFactory bCF = BusinessControlFactory.getInstance();
        final List<ContextEntry> ceList = bCF.createCEListFromString(businessPath);
        final StringBuilder retVal = new StringBuilder();
        retVal.append(Settings.getServerContextPathURI()).append("/url/");
        for (final ContextEntry contextEntry : ceList) {
            String ceStr = contextEntry.toString();
            ceStr = ceStr.replace(':', '/');
            ceStr = ceStr.replaceFirst("\\]", "/");
            ceStr = ceStr.replaceFirst("\\[", "");
            retVal.append(ceStr);
        }
        return retVal.substring(0, retVal.length() - 1);
    }

    public class InfoNodeRenderer implements CustomCellRenderer {
        private DateFormat formatter;

        public InfoNodeRenderer() {
            //
        }

        @Override
        public void render(final StringOutput sb, final Renderer renderer, final Object val, final Locale locale, final int alignment, final String action) {
            if (val instanceof InfoMessage) {
                final InfoMessage item = (InfoMessage) val;
                // date
                if (formatter == null) {
                    formatter = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
                }
                sb.append(formatter.format(item.getCreationDate())).append(": ");
                // title
                final boolean tooltip = StringHelper.containsNonWhitespace(item.getMessage());
                if (tooltip) {
                    final String message = Formatter.escWithBR(Formatter.truncate(item.getMessage(), 255)).toString();
                    sb.append("<span ext:qtip=\"").append(StringHelper.escapeHtml(message)).append("\">");
                } else {
                    sb.append("<span>");
                }
                String title = StringHelper.escapeHtml(item.getTitle());
                title = Formatter.truncate(title, 64);
                sb.append(title).append("</span>&nbsp;");
                // link
                if (StringHelper.containsNonWhitespace(item.getBusinessPath())) {
                    final String url = getUrl(item.getBusinessPath());
                    sb.append("<a href=\"").append(url).append("\" class=\"o_peekview_infomsg_link\">").append(translate("peekview.more")).append("</a>");
                }
            } else {
                sb.append("-");
            }
        }
    }
}
