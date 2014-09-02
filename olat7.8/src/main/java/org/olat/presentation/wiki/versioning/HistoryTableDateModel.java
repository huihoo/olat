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

package org.olat.presentation.wiki.versioning;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.lms.wiki.WikiPage;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableDataModel;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.wiki.WikiMainController;
import org.olat.system.commons.OutputEscapeType;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * TODO: guido Class Description for HistoryTableDateModel
 * <P>
 * Initial Date: May 30, 2006 <br>
 * 
 * @author guido
 */
public class HistoryTableDateModel extends DefaultTableDataModel implements TableDataModel {

    private final Translator trans;

    public HistoryTableDateModel(final List entries, final Translator trans) {
        super(entries);
        this.trans = trans;
    }

    private static final int COLUMN_COUNT = 3;

    /**
	 */
    @Override
    public int getColumnCount() {
        return COLUMN_COUNT;
    }

    /**
	 */
    @Override
    public Object getValueAt(final int row, final int col) {
        final WikiPage page = (WikiPage) objects.get(row);
        switch (col) {
        case 0:
            return String.valueOf(page.getVersion());
        case 1:
            return new Date(page.getModificationTime());
        case 2:
            return String.valueOf(page.getViewCount());
        case 3:
            final long key = page.getModifyAuthor();
            return key != 0 ? getBaseSecurity().loadIdentityByKey(Long.valueOf(page.getModifyAuthor())).getName() : "n/a";
            // TODO:gs:a loadIdenitiesByKeys(List keys) would be much more performant as each lookup get one database lookup
        case 4:
            final int v = page.getVersion();
            if (v == 0) {
                return new String("");
            }
            return String.valueOf(v - 1) + " " + trans.translate("to") + " " + String.valueOf(v);
        case 5:
            return page.getUpdateComment();
        default:
            return "ERROR";
        }
    }

    private BaseSecurity getBaseSecurity() {
        return (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
    }

    public void addColumnDescriptors(final TableController tableCtr) {
        final Locale loc = trans.getLocale();
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.version", 0, WikiMainController.ACTION_SHOW, loc));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.date", 1, null, loc));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.viewcount", 2, null, loc));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.author", 3, null, loc));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.compare", 4, WikiMainController.ACTION_COMPARE, loc));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.comment", 5, null, loc, OutputEscapeType.HTML));

    }

}
