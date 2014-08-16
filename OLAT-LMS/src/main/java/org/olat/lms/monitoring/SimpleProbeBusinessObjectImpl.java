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
package org.olat.lms.monitoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.olat.data.commons.database.DBQueryImpl;
import org.olat.data.commons.database.SimpleProbe;
import org.springframework.stereotype.Component;

/**
 * implementation class for SimpleProbeBOImpl
 * 
 * <P>
 * Initial Date: 05.09.2011 <br>
 * 
 * @author Branislav Balaz
 */
@Component
public class SimpleProbeBusinessObjectImpl implements SimpleProbeBusinessObject {

    @Override
    public SimpleProbeObject getSimpleProbeByKey(String key) {
        SimpleProbe probe = DBQueryImpl.listTableStatsMap_.get(key);
        if (probe == null) {
            return null;
        }
        SimpleProbeObject simpleProbe = new SimpleProbeObject(key, probe.getSum(), probe.getSum());
        probe.reset();
        return simpleProbe;
    }

    /*
     * TODO: ORID-1007 this method is copied from legacy code and should be also refactored in sense of Clean Code
     */
    @Override
    public List<SimpleProbeObject> getSimpleProbeNonRegisteredFromMapAsList() {

        final Set<Entry<String, org.olat.data.commons.database.SimpleProbe>> entries = DBQueryImpl.listTableStatsMap_.entrySet();
        final Set<Entry<String, org.olat.data.commons.database.SimpleProbe>> nonRegisteredEntries = new HashSet<Entry<String, org.olat.data.commons.database.SimpleProbe>>();
        long sum = 0;
        for (final Iterator<Entry<String, org.olat.data.commons.database.SimpleProbe>> it = entries.iterator(); it.hasNext();) {
            final Entry<String, org.olat.data.commons.database.SimpleProbe> entry = it.next();
            if (!DBQueryImpl.registeredTables_.contains(entry.getKey())) {
                nonRegisteredEntries.add(entry);
                sum += entry.getValue().getSum();
            }
        }
        final List<Entry<String, org.olat.data.commons.database.SimpleProbe>> list = new LinkedList<Entry<String, org.olat.data.commons.database.SimpleProbe>>(
                nonRegisteredEntries);
        Collections.sort(list, new Comparator<Entry<String, org.olat.data.commons.database.SimpleProbe>>() {

            @Override
            public int compare(final Entry<String, org.olat.data.commons.database.SimpleProbe> o1, final Entry<String, org.olat.data.commons.database.SimpleProbe> o2) {
                if (o1.getValue().getSum() > o2.getValue().getSum()) {
                    return 1;
                } else if (o1.getValue().getSum() == o2.getValue().getSum()) {
                    return 0;
                } else {
                    return -1;
                }
            }

        });

        List<SimpleProbeObject> simpleProbeList = new ArrayList<SimpleProbeObject>();
        for (final Iterator<Entry<String, org.olat.data.commons.database.SimpleProbe>> it = list.iterator(); it.hasNext();) {
            final Entry<String, org.olat.data.commons.database.SimpleProbe> entry = it.next();
            SimpleProbeObject simpleProbe = new SimpleProbeObject(entry.getKey(), entry.getValue().getSum(), sum);
            simpleProbeList.add(simpleProbe);
            entry.getValue().reset();
        }

        return simpleProbeList;

    }

}
