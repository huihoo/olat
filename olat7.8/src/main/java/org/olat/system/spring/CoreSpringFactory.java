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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.system.spring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.olat.system.exception.AssertException;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * Description:<br>
 * The core spring factory is used to load resources and spring beans. The application context is generated from XML files. Normally you should not use this class and
 * instead inject dependencies at xml level or with autowire!
 * <P>
 * Initial Date: 12.06.2006 <br>
 * 
 * @author patrickb
 */
@Service
public class CoreSpringFactory implements ServletContextAware, BeanFactoryAware {
    // Access servletContext only for spring beans admin-functions
    public static ServletContext servletContext;
    private static List<String> beanNamesCalledFromSource = new ArrayList<String>();
    private static final Logger log = LoggerHelper.getLogger();

    private static DefaultListableBeanFactory beanFactory;

    /**
     * [used by spring only]
     */
    private CoreSpringFactory() {
        //
    }

    /**
     * wrapper to the applicationContext (we are facading spring's applicationContext)
     * 
     * @param path
     *            a path in spring notation (e.g. "classpath*:/*.hbm.xml", see springframework.org)
     * @return the resources found
     */
    public static Resource[] getResources(String path) {
        Resource[] res;
        try {
            ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(CoreSpringFactory.servletContext);
            res = context.getResources(path);
        } catch (IOException e) {
            throw new AssertException("i/o error while asking for resources, path:" + path);
        }
        return res;
    }

    /**
     * @param beanName
     *            The bean name to check for. Be sure the bean does exist, otherwise an NoSuchBeanDefinitionException will be thrown
     * @return The bean
     */
    public static Object getBean(String beanName) {
        ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(CoreSpringFactory.servletContext);
        Object o = context.getBean(beanName);
        beanNamesCalledFromSource.add(beanName);
        // printClassNamesOfAllBeans(context);
        return o;
    }

    /**
     * @param beanDefinitionNames
     * 
     *            bash script to find spring managed classes with corespring factory in it: run it inside src/main/java
     * 
     *            cat out.txt | sort | uniq | while read a; do count=`cat ${a//./\/}.java | grep -c 'CoreSpringFactory.get'` if [ $count -gt 0 ] then
     * 
     *            echo "found $count in " echo $a echo ""
     * 
     *            fi done
     * 
     */
    private static void printClassNamesOfAllBeans(ApplicationContext context) {
        String[] beanNames = context.getBeanDefinitionNames();
        for (int i = 0; i < beanNames.length; i++) {
            try {
                Object o = context.getBean(beanNames[i]);
                System.out.println(o.getClass().getName());
            } catch (BeansException e) {
                System.out.println("error accessing bean: " + beanNames[i]);
            }
        }

    }

    /**
     * @param beanName
     *            The bean name to check for. Be sure the bean does exist, otherwise an NoSuchBeanDefinitionException will be thrown
     * @return The bean
     * @throws RuntimeException
     *             when more than one bean of the same type is registered.
     */
    public static <T> T getBean(Class<T> beanType) {
        // log.info("beanType=" + beanType);
        // log.info("beanType.getInterfaces()=" + beanType.getInterfaces());

        ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(CoreSpringFactory.servletContext);
        Map<String, T> m = context.getBeansOfType(beanType);
        if (m.size() > 1) {
            // with following code it is possible to configure multiple implementations for an interface (e.g. adapted spring config files for testing)
            // and to give it preference by setting primary="true" in <bean> definition tag.
            if (context instanceof XmlWebApplicationContext) {
                ConfigurableListableBeanFactory clbf = ((XmlWebApplicationContext) context).getBeanFactory();
                String[] beanNames = clbf.getBeanNamesForType(beanType);
                for (String beanName : beanNames) {
                    BeanDefinition bd = clbf.getBeanDefinition(beanName);
                    if (bd.isPrimary()) {
                        return context.getBean(beanName, beanType);
                    }
                }
            }

            // more than one bean found -> exception
            throw new OLATRuntimeException("found more than one bean for: " + beanType + ". Calling this method should only find one bean!", null);
        } else if (m.size() == 1) {
            return m.values().iterator().next();
        }

        // fallback for beans named like the fully qualified path (legacy)
        Object o = context.getBean(beanType.getName());
        beanNamesCalledFromSource.add(beanType.getName());
        return (T) o;
    }

    /**
     * Prototype-method : Get service which is annotated with '@Service'.
     * 
     * @param <T>
     * @param serviceType
     * @return Service of requested type, must not be casted.
     * @throws RuntimeException
     *             when more than one service of the same type is registered. RuntimeException when servie is not annotated with '@Service'.
     * 
     *             *******not yet in use********
     */
    private static <T> T getService(Class<T> serviceType) {
        ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(CoreSpringFactory.servletContext);
        Map<String, T> m = context.getBeansOfType(serviceType);
        if (m.size() > 1) {
            throw new OLATRuntimeException("found more than one service for: " + serviceType + ". Calling this method should only find one service-bean!", null);
        }
        T service = context.getBean(serviceType);
        Map<String, ?> services = context.getBeansWithAnnotation(org.springframework.stereotype.Service.class);
        if (services.containsValue(service)) {
            return service;
        } else {
            throw new OLATRuntimeException("Try to get Service which is not annotated with '@Service', services must have '@Service'", null);
        }
    }

    /**
     * @param beanName
     * @return
     */
    public static boolean containsSingleton(String beanName) {
        return beanFactory.containsSingleton(beanName);
    }

    /**
     * @param beanName
     *            The bean name to check for
     * @return true if such a bean does exist, false otherwhise. But if such a bean definition exists it will get created! Use the containsSingleton to check for lazy
     *         init beans whether they are instantiated or not.
     */
    public static boolean containsBean(String beanName) {
        ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(CoreSpringFactory.servletContext);
        return context.containsBean(beanName);
    }

    /**
     * @param classz
     * @return
     */
    public static boolean containsBean(Class classz) {
        ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(CoreSpringFactory.servletContext);
        Map beans = context.getBeansOfType(classz);
        return beans.size() > 0;
    }

    /**
     * normally you should not use this! At the moment it is used for calling the shutdown hook in Spring
     * 
     * @return the OLAT Spring application Context
     */
    public static XmlWebApplicationContext getContext() {
        return (XmlWebApplicationContext) WebApplicationContextUtils.getWebApplicationContext(CoreSpringFactory.servletContext);
    }

    /**
     * [used by spring]
     * 
     */
    @Override
    public void setServletContext(ServletContext servletContext) {
        CoreSpringFactory.servletContext = servletContext;
    }

    public static Map<String, Object> getBeansOfType(BeanType extensionType) {
        XmlWebApplicationContext context = (XmlWebApplicationContext) WebApplicationContextUtils.getWebApplicationContext(CoreSpringFactory.servletContext);
        Map beans = context.getBeansOfType(extensionType.getExtensionTypeClass());
        Map<String, Object> clone = new HashMap<String, Object>(beans);
        return clone;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        CoreSpringFactory.beanFactory = (DefaultListableBeanFactory) beanFactory;

    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> beanType, Object... args) {
        ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(CoreSpringFactory.servletContext);
        String[] names = context.getBeanNamesForType(beanType);
        if (names.length == 0) {
            throw new OLATRuntimeException("found no bean name for: " + beanType + ". Calling this method should find one bean name!", null);
        } else if (names.length > 1) {
            throw new OLATRuntimeException("found more bean names for: " + beanType + ". Calling this method should find one bean name!", null);
        }
        Object o = context.getBean(names[0], args);
        return (T) o;
    }

}
