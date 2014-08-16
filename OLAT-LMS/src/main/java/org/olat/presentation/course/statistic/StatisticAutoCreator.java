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
package org.olat.presentation.course.statistic;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.olat.lms.course.ICourse;
import org.olat.lms.course.statistic.IStatisticManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.creator.AutoCreator;
import org.olat.system.exception.AssertException;

public class StatisticAutoCreator extends AutoCreator {

    @SuppressWarnings("unchecked")
    private final Class[] ARGCLASSES = new Class[] { UserRequest.class, WindowControl.class, ICourse.class, IStatisticManager.class };
    private String className;
    private IStatisticManager statisticManager_;

    @Override
    public Controller createController(final UserRequest lureq, final WindowControl lwControl) {
        return super.createController(lureq, lwControl);
    }

    /**
     * [used by spring]
     * 
     * @param className
     */
    @Override
    public void setClassName(final String className) {
        this.className = className;
    }

    /**
     * @return Returns the className of the Controller which is created
     */
    @Override
    public String getClassName() {
        return className;
    }

    /** set by spring **/
    public void setStatisticManager(final IStatisticManager statisticManager) {
        statisticManager_ = statisticManager;
    }

    public Controller createController(final UserRequest lureq, final WindowControl lwControl, final ICourse course) {
        Exception re = null;
        try {
            final Class cclazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            final Constructor con = cclazz.getConstructor(ARGCLASSES);
            final Object o = con.newInstance(new Object[] { lureq, lwControl, course, statisticManager_ });
            final Controller c = (Controller) o;
            return c;
        } catch (final ClassNotFoundException e) {
            re = e;
        } catch (final SecurityException e) {
            re = e;
        } catch (final NoSuchMethodException e) {
            re = e;
        } catch (final IllegalArgumentException e) {
            re = e;
        } catch (final InstantiationException e) {
            re = e;
        } catch (final IllegalAccessException e) {
            re = e;
        } catch (final InvocationTargetException e) {
            re = e;
        } finally {
            if (re != null) {
                throw new AssertException("could not create controller via reflection. classname:" + className, re);
            }
        }
        return null;
    }

}
