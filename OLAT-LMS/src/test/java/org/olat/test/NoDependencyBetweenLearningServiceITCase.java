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
package org.olat.test;

import static org.junit.Assert.fail;

import java.lang.reflect.Field;

import org.junit.Test;
import org.olat.lms.learn.LearnService;
import org.springframework.beans.BeansException;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * Check dependencies between learning-services. Loop over all spring-beans and look for learning-services. Check all fields, other learning-service are not allowed to
 * use.
 * 
 * @author Christian Guretzki
 */
public class NoDependencyBetweenLearningServiceITCase extends OlatTestCase {

    @Test
    public void testLearningServiceDependency() {

        final XmlWebApplicationContext context = (XmlWebApplicationContext) applicationContext;
        String[] beanNames = context.getBeanDefinitionNames();
        for (int i = 0; i < beanNames.length; i++) {
            Object bean = null;
            try {
                bean = context.getBean(beanNames[i]);
                String className = bean.getClass().getCanonicalName();
                if (bean instanceof LearnService) {
                    // without marker-interface: if (className.startsWith("org.olat.lms.learning.") && className.endsWith("LearningService") ) {
                    Field[] fields = bean.getClass().getDeclaredFields();
                    for (int f = 0; f < fields.length; f++) {
                        if (fields[f].getType().getCanonicalName().startsWith("org.olat.lms.learning.")
                                && fields[f].getType().getCanonicalName().endsWith("LearningService")) {
                            fail("No dependencies between learning-services are allowed, learning-Service: " + bean.getClass().getCanonicalName());
                        }
                    }
                }
            } catch (BeansException e) {
                if (bean != null)
                    fail("could not access bean: " + bean.getClass().getCanonicalName());
            } catch (SecurityException e) {
                if (bean != null)
                    fail("could not access bean: " + bean.getClass().getCanonicalName());
            }

        }
    }

}
