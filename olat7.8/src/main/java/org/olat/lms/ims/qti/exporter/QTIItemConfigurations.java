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
package org.olat.lms.ims.qti.exporter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.olat.lms.ims.qti.exporter.helper.QTIItemObject;
import org.olat.lms.ims.qti.parser.ItemParser;
import org.olat.system.exception.OLATRuntimeException;

/**
 * Provides a map with QTIExportItemFormatConfig objects.
 * 
 * @author lavinia
 */
public class QTIItemConfigurations {

    private QTIItemConfigurations() {
        super();
    }

    public static <T> Map<Class<? extends QTIExportItemFormatConfig>, QTIExportItemFormatConfig> getQTIItemConfigs(final List<QTIItemObject> qtiItemObjectList) {
        final Map<Class<? extends QTIExportItemFormatConfig>, QTIExportItemFormatConfig> itConfigs = new HashMap<Class<? extends QTIExportItemFormatConfig>, QTIExportItemFormatConfig>();

        for (final Iterator<QTIItemObject> iter = qtiItemObjectList.iterator(); iter.hasNext();) {
            final QTIItemObject item = iter.next();
            if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_SCQ)) {
                if (itConfigs.get(QTIExportSCQItemFormatConfig.class) == null) {
                    final QTIExportSCQItemFormatConfig confSCQ = new QTIExportSCQItemFormatConfig(true, false, false, false);
                    itConfigs.put(QTIExportSCQItemFormatConfig.class, confSCQ);
                }
            } else if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_MCQ)) {
                if (itConfigs.get(QTIExportMCQItemFormatConfig.class) == null) {
                    final QTIExportMCQItemFormatConfig confMCQ = new QTIExportMCQItemFormatConfig(true, false, false, false);
                    itConfigs.put(QTIExportMCQItemFormatConfig.class, confMCQ);
                }
            } else if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_KPRIM)) {
                if (itConfigs.get(QTIExportKPRIMItemFormatConfig.class) == null) {
                    final QTIExportKPRIMItemFormatConfig confKPRIM = new QTIExportKPRIMItemFormatConfig(true, false, false, false);
                    itConfigs.put(QTIExportKPRIMItemFormatConfig.class, confKPRIM);
                }
            } else if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_ESSAY)) {
                if (itConfigs.get(QTIExportEssayItemFormatConfig.class) == null) {
                    final QTIExportEssayItemFormatConfig confEssay = new QTIExportEssayItemFormatConfig(true, false);
                    itConfigs.put(QTIExportEssayItemFormatConfig.class, confEssay);
                }
            } else if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_FIB)) {
                if (itConfigs.get(QTIExportFIBItemFormatConfig.class) == null) {
                    final QTIExportFIBItemFormatConfig confFIB = new QTIExportFIBItemFormatConfig(true, false, false);
                    itConfigs.put(QTIExportFIBItemFormatConfig.class, confFIB);
                }
            }
            // if cannot find the type via the ItemParser, look for the QTIItemObject type
            else if (item.getItemType().equals(QTIItemObject.TYPE.A)) {
                final QTIExportEssayItemFormatConfig confEssay = new QTIExportEssayItemFormatConfig(true, false);
                itConfigs.put(QTIExportEssayItemFormatConfig.class, confEssay);
            } else if (item.getItemType().equals(QTIItemObject.TYPE.R)) {
                final QTIExportSCQItemFormatConfig confSCQ = new QTIExportSCQItemFormatConfig(true, false, false, false);
                itConfigs.put(QTIExportSCQItemFormatConfig.class, confSCQ);
            } else if (item.getItemType().equals(QTIItemObject.TYPE.C)) {
                final QTIExportMCQItemFormatConfig confMCQ = new QTIExportMCQItemFormatConfig(true, false, false, false);
                itConfigs.put(QTIExportMCQItemFormatConfig.class, confMCQ);
            } else if (item.getItemType().equals(QTIItemObject.TYPE.B)) {
                final QTIExportFIBItemFormatConfig confFIB = new QTIExportFIBItemFormatConfig(true, false, false);
                itConfigs.put(QTIExportFIBItemFormatConfig.class, confFIB);
            } else {
                throw new OLATRuntimeException(null, "Can not resolve QTIItem type='" + item.getItemType() + "'", null);
            }
        }
        return itConfigs;
    }
}
