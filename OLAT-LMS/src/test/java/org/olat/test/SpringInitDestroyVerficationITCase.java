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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.junit.Test;
import org.olat.system.commons.configuration.Destroyable;
import org.olat.system.commons.configuration.Initializable;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * Description:<br>
 * tests if the init / destory method calls are in the spring config when the initializable or destoryable interfaces are used.
 * 
 * Checks also the @preDestory with the destory method or @postConstruct with the init method annotations when used instead of xml config
 * <P>
 * Initial Date: 17.03.2010 <br>
 * 
 * @author guido
 */
public class SpringInitDestroyVerficationITCase extends OlatTestCase {

    @Test
    public void testInitMethodCalls() {
        final XmlWebApplicationContext context = (XmlWebApplicationContext) applicationContext;
        final ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();

        final Map<String, Initializable> beans = applicationContext.getBeansOfType(Initializable.class);
        for (final Iterator iterator = beans.keySet().iterator(); iterator.hasNext();) {
            final String beanName = (String) iterator.next();
            try {
                final GenericBeanDefinition beanDef = (GenericBeanDefinition) beanFactory.getBeanDefinition(beanName);
                if (beanDef.getInitMethodName() != null) {

                    assertNotNull(
                            "Spring Bean (" + beanName + ") of type Initializable does not have the required init-method attribute or the method name is not init!",
                            beanDef.getInitMethodName());
                    if (beanDef.getDestroyMethodName() != null) {
                        assertTrue("Spring Bean (" + beanName
                                + ") of type Initializable does not have the required init-method attribute or the method name is not init!", beanDef.getInitMethodName()
                                .equals("init"));
                    }
                } else {
                    Initializable myObject = (Initializable) applicationContext.getBean(beanName);
                    assertNotNull("Did not find annotation '@PostConstruct' at the init method on bean: " + beanName + "",
                            AnnotationUtils.findAnnotation(myObject.getClass().getDeclaredMethod("init", null), PostConstruct.class));
                }
            } catch (final NoSuchBeanDefinitionException e) {
                System.out.println("testInitMethodCalls: Error while trying to analyze bean with name: " + beanName + " :" + e);
            } catch (final Exception e) {
                System.out.println("testInitMethodCalls: Error while trying to analyze bean with name: " + beanName + " :" + e);
            }
        }
    }

    @Test
    public void testDestroyMethodCalls() {

        final XmlWebApplicationContext context = (XmlWebApplicationContext) applicationContext;
        final ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();

        final Map<String, Destroyable> beans = applicationContext.getBeansOfType(Destroyable.class);
        for (final Iterator iterator = beans.keySet().iterator(); iterator.hasNext();) {
            final String beanName = (String) iterator.next();
            try {
                final GenericBeanDefinition beanDef = (GenericBeanDefinition) beanFactory.getBeanDefinition(beanName);

                if (beanDef.getDestroyMethodName() != null) {
                    assertNotNull("Spring Bean (" + beanName
                            + ") of type Destroyable does not have the required destroy-method attribute or the method name is not destroy!",
                            beanDef.getDestroyMethodName());
                    if (beanDef.getDestroyMethodName() != null) {
                        assertTrue("Spring Bean (" + beanName
                                + ") of type Destroyable does not have the required destroy-method attribute or the method name is not destroy!", beanDef
                                .getDestroyMethodName().equals("destroy"));
                    }
                } else {
                    Destroyable myObject = (Destroyable) applicationContext.getBean(beanName);
                    assertNotNull("Did not find annotation '@PreDestroy' at the destroy method on bean: " + beanName + "",
                            AnnotationUtils.findAnnotation(myObject.getClass().getDeclaredMethod("destroy", null), PreDestroy.class));
                }
            } catch (final NoSuchBeanDefinitionException e) {
                System.out.println("testDestroyMethodCalls: Error while trying to analyze bean with name: " + beanName + " :" + e);
            } catch (final Exception e) {
                System.out.println("testDestroyMethodCalls: Error while trying to analyze bean with name: " + beanName + " :" + e);
            }
        }
    }

}
