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
package org.olat.lms.admin.jmx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.olat.system.commons.manager.BasicManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * TODO:
 * <P>
 * Initial Date: 01.10.2007 <br>
 * 
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class JMXManager extends BasicManager {
    private static JMXManager INSTANCE;
    private boolean initDone = true;
    private final MBeanServer mBeanServer;
    @Autowired
    private MBeanServer server;

    /**
     * access via spring
     * 
     * @return
     */
    @Deprecated
    public static JMXManager getInstance() {
        return INSTANCE;
    }

    /**
     * [spring]
     */
    private JMXManager(final MBeanServer mBeanServer) {
        this.mBeanServer = mBeanServer;
        INSTANCE = this;
        // mercurial testchange
    }

    public boolean isActive() {
        return initDone;
    }

    void init() {
        initDone = true;
    }

    public List<String> dumpJmx(final String objectName) {
        try {
            final ObjectName on = new ObjectName(objectName);
            final MBeanAttributeInfo[] ainfo = mBeanServer.getMBeanInfo(on).getAttributes();
            final List<MBeanAttributeInfo> mbal = Arrays.asList(ainfo);

            Collections.sort(mbal, new Comparator<MBeanAttributeInfo>() {
                @Override
                public int compare(final MBeanAttributeInfo o1, final MBeanAttributeInfo o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });

            final List<String> l = new ArrayList<String>();
            for (final MBeanAttributeInfo info : mbal) {
                final String name = info.getName();
                final Object res = mBeanServer.getAttribute(on, name);
                l.add(name + "=" + res);
            }
            return l;
        } catch (final Exception e) {
            final List l = new ArrayList();
            l.add("error while retrieving jmx values: " + e.getClass().getName() + ":" + e.getMessage());
            // TODO: this is just version 0.1 of dumping jmx values... need a better interface
            return l;
        }
    }

    public String dumpAll() {
        try {
            final StringBuilder sb = new StringBuilder();
            final Set<ObjectInstance> mbeansset = server.queryMBeans(null, null);
            final List<ObjectInstance> mbeans = new ArrayList<ObjectInstance>(mbeansset);
            Collections.sort(mbeans, new Comparator<ObjectInstance>() {
                @Override
                public int compare(final ObjectInstance o1, final ObjectInstance o2) {
                    return o1.getObjectName().getCanonicalName().compareTo(o2.getObjectName().getCanonicalName());
                }
            });

            for (final ObjectInstance instance : mbeans) {
                final ObjectName on = instance.getObjectName();
                final MBeanAttributeInfo[] ainfo = server.getMBeanInfo(on).getAttributes();
                final List<MBeanAttributeInfo> mbal = Arrays.asList(ainfo);
                Collections.sort(mbal, new Comparator<MBeanAttributeInfo>() {
                    @Override
                    public int compare(final MBeanAttributeInfo o1, final MBeanAttributeInfo o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
                final String oname = on.getCanonicalName();
                // dump all attributes with their values (simply toString()'ed)

                for (final MBeanAttributeInfo info : mbal) {
                    final String name = info.getName();
                    try {
                        final Object res = server.getAttribute(on, name);
                        sb.append("<br />" + oname + "-> " + name + "=" + res);
                    } catch (final Exception e) {
                        sb.append("<br />ERROR: for attribute '" + name + "', exception:" + e + ", message:" + e.getMessage());
                    }
                }
            }
            return sb.toString();
        } catch (final Exception e) {
            return "error:" + e.getMessage();
        }

    }

}
