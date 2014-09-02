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

package org.olat.lms.ims.qti.exporter.helper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.olat.lms.ims.qti.process.ImsRepositoryResolver;
import org.olat.lms.ims.qti.process.Resolver;

/**
 * <pre>
 * Description:
 * 
 * method getCSV()
 * ===============
 * example with one item for type 1, 2 and 3:
 * 
 * iqtest (type1):
 * +----------------------------------+---------------------------------+
 * |HeaderRow1Intro                   |HeaderRow1                       |
 * |                                  |itemTitle                        |
 * +----------------------------------+---------------------------------+
 * |HeaderRow2Intro                   |HeaderRow2                       |
 * |# | Name, etc. |starttime|duration|1|...|n|points|starttime|duration|
 * +--+------------+---------+--------+-+---+-+------+---------+--------+
 * |  |            |         |        | |   | |      |         |        |
 * |          proband data            |            results              |
 * ...
 * 
 * iqself (type2):
 * +----------------------------------+---------------------------------+
 * |HeaderRow1Intro                   |HeaderRow1                       |
 * |                                  |itemTitle                        |
 * +----------------------------------+---------------------------------+
 * |HeaderRow2Intro                   |HeaderRow2                       |
 * |# |covernumber |starttime|duration|1|...|n|points|starttime|duration|
 * +--+------------+---------+--------+-+---+-+------+---------+--------+
 * |  |            |         |        | |   | |      |         |        |
 * |      proband data (anonym)       |            results              |
 * ...
 * 
 * iqsurv (type3):
 * +-----------------+-----------+
 * |HeaderRow1Intro  |HeaderRow1 |
 * |                 |itemTitle  | 
 * +-----------------+-----------+
 * |HeaderRow2Intro  |HeaderRow  |
 * |# |  starttime   | 1 |...| n |
 * +--+--------------+---+---+---+
 * |  |              |   |   |   |
 * |     datetime    |  results  |
 * ...
 * </pre>
 * 
 * @author Mike Stock, Alexander Schneider
 */

public class QTIObjectTreeBuilder {

    private final Long repositoryEntryKey;

    /**
     * Constructor for QTIObjectTreeBuilder
     * 
     * @param repositoryEntryKey
     * @param downloadtrans
     * @param type
     * @param anonymizerCallback
     */
    public QTIObjectTreeBuilder(final Long repositoryEntryKey) {
        this.repositoryEntryKey = repositoryEntryKey;
    }

    /**
	 * 
	 *
	 */
    public List<QTIItemObject> getQTIItemObjectList() {
        final List<QTIItemObject> itemList = new ArrayList<QTIItemObject>();

        final Resolver resolver = new ImsRepositoryResolver(repositoryEntryKey);
        final Document doc = resolver.getQTIDocument();
        final Element root = doc.getRootElement();
        final List<?> items = root.selectNodes("//item");
        for (final Iterator<?> iter = items.iterator(); iter.hasNext();) {
            final Element el_item = (Element) iter.next();
            if (el_item.selectNodes(".//response_lid").size() > 0) {
                itemList.add(new ItemWithResponseLid(el_item));
            } else if (el_item.selectNodes(".//response_str").size() > 0) {
                itemList.add(new ItemWithResponseStr(el_item));
            }
        }
        return itemList;
    }
}
