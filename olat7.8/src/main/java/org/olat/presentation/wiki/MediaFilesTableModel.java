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
package org.olat.presentation.wiki;

import java.util.Date;
import java.util.List;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.presentation.framework.core.components.table.CustomCssCellRenderer;
import org.olat.presentation.framework.core.components.table.CustomRenderColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.Formatter;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * <P>
 * Initial Date: Nov 6, 2006 <br>
 * 
 * @author guido
 */
public class MediaFilesTableModel extends DefaultTableDataModel {
    private static final int COLUMN_COUNT = 5;
    private final Formatter formatter;

    public MediaFilesTableModel(final List objects, final Translator trans) {
        super(objects);
        setLocale(trans.getLocale());
        formatter = Formatter.getInstance(trans.getLocale());
    }

    public void addColumnDescriptors(final TableController tableCtr) {
        tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.filename", 0, WikiMainController.ACTION_SHOW_MEDIA, getLocale(), 1,
                new StrikeThroughCellRenderer()));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.created.by", 1, null, getLocale()));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.creation.date", 2, null, getLocale()));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.deleted.by", 3, null, getLocale()));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.deleted.at", 4, null, getLocale()));
    }

    @Override
    public int getColumnCount() {
        return COLUMN_COUNT;
    }

    @Override
    public Object getValueAt(final int row, final int col) {
        final MediaFileElement entry = getEntryAt(row);
        switch (col) {
        case 0:
            final String filename = entry.getFilename();
            if (filename.length() > 40) {
                if (filename.endsWith(WikiMainController.METADATA_SUFFIX)) {
                    return entry.getFilename().substring(0, 40) + WikiMainController.METADATA_SUFFIX;
                }
                return entry.getFilename().substring(0, 40) + "...";
            }
            return entry.getFilename();
        case 1:
            final long identKey = entry.getCreatedBy();
            if (identKey == 0) {
                return "---";
            }
            return getBaseSecurity().loadIdentityByKey(identKey).getName();
        case 2:
            return formatter.formatDateAndTime(new Date(entry.getCreationDate()));
        case 3:
            final long key = entry.getDeletedBy();
            if (key == 0) {
                return "---";
            }
            return getBaseSecurity().loadIdentityByKey(key).getName();
        case 4:
            final long delDate = entry.getDeletionDate();
            if (delDate == 0) {
                return "---";
            }
            return formatter.formatDateAndTime(new Date(delDate));
        default:
            return "ERROR";
        }
    }

    private BaseSecurity getBaseSecurity() {
        return (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
    }

    private MediaFileElement getEntryAt(final int row) {
        return (MediaFileElement) this.objects.get(row);
    }

    private class StrikeThroughCellRenderer extends CustomCssCellRenderer {

        @Override
        protected String getCssClass(final Object val) {
            final String filename = (String) val;
            if (filename.endsWith(WikiMainController.METADATA_SUFFIX)) {
                return "wiki-file-deleted";
            }
            return "";
        }

        @Override
        protected String getCellValue(final Object val) {
            final String filename = (String) val;
            if (filename.endsWith(WikiMainController.METADATA_SUFFIX)) {
                return filename.substring(0, filename.lastIndexOf("."));
            }
            return filename;
        }

        @Override
        protected String getHoverText(final Object val) {
            return null;
        }

    }

}
