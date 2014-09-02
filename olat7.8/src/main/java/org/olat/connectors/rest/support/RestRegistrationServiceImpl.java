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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.connectors.rest.support;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * Register resource classes and singletons
 * <P>
 * Initial Date: 15 apr. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
// TODO: use spring to create objects (getBeansByType...) and not implement the wheel again
@Deprecated
public class RestRegistrationServiceImpl implements RestRegistrationService {

    private static final Logger log = LoggerHelper.getLogger();

    private final Set<Object> singletons = new HashSet<Object>();
    private final Set<Class<?>> classes = new HashSet<Class<?>>();

    protected RestRegistrationServiceImpl() {
        //
    }

    @Override
    public Set<Class<?>> getClasses() {
        return new HashSet<Class<?>>(classes);
    }

    // [for spring]
    public List<String> getClassnames() {
        final List<String> classnames = new ArrayList<String>(classes.size());
        for (final Class<?> cl : classes) {
            classnames.add(cl.getName());
        }
        return classnames;
    }

    public void setClassnames(final List<String> classnames) {
        for (final String classname : classnames) {
            try {
                final Class<?> cl = Class.forName(classname);
                classes.add(cl);
            } catch (final ClassNotFoundException e) {
                log.error("Class not found: " + classname, e);
            }
        }
    }

    @Override
    public void addClass(final Class<?> cl) {
        classes.add(cl);
    }

    @Override
    public void removeClass(final Class<?> cl) {
        classes.remove(cl);
    }

    @Override
    public Set<Object> getSingletons() {
        return new HashSet<Object>(singletons);
    }

    // [spring]
    public List<Object> getSingletonBeans() {
        final List<Object> beans = new ArrayList<Object>();
        beans.addAll(singletons);
        return beans;
    }

    public void setSingletonBeans(final List<Object> beans) {
        singletons.addAll(beans);
    }

    @Override
    public void addSingleton(final Object singleton) {
        singletons.add(singleton);
    }

    @Override
    public void removeSingleton(final Object singleton) {
        singletons.remove(singleton);
    }
}
