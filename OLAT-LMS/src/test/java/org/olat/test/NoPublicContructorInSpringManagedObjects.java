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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import javassist.Modifier;

import org.junit.Test;
import org.olat.data.coordinate.singlevm.SingleVMLocker;
import org.olat.lms.webfeed.FeedManagerImpl;
import org.olat.testutils.codepoints.server.impl.JMSCodepointServer;
import org.springframework.beans.BeansException;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * Tests for public constructors in spring managed objects. Test will fail if it detects public constructors. Public constructors are a bad smell as objects could gets
 * created outside of spring and e.g @autowire stuff would not be done as the spring context does not know about this creation. This can lead into hard to find bugs whith
 * nullpointers and such.
 * 
 * <P>
 * Initial Date: 20.07.2011 <br>
 * 
 * @author guido
 */
public class NoPublicContructorInSpringManagedObjects extends OlatTestCase {

    private List<String> whitelist = new ArrayList<String>();

    @Test
    public void testForPulicContructors() {

        whitelist.add(SingleVMLocker.class.getCanonicalName());
        whitelist.add(FeedManagerImpl.class.getCanonicalName());
        whitelist.add(JMSCodepointServer.class.getCanonicalName());

        final XmlWebApplicationContext context = (XmlWebApplicationContext) applicationContext;
        String[] beanNames = context.getBeanDefinitionNames();

        for (int i = 0; i < beanNames.length; i++) {
            Object o = null;
            Constructor[] allConstructors;
            try {
                o = context.getBean(beanNames[i]);
                allConstructors = o.getClass().getDeclaredConstructors();
            } catch (BeansException e) {
                if (o != null)
                    System.out.println("could not access bean: " + o.getClass().getCanonicalName());
                continue;
            } catch (SecurityException e) {
                if (o != null)
                    System.out.println("could not access bean: " + o.getClass().getCanonicalName());
                continue;
            }
            for (Constructor ctor : allConstructors) {
                if (ctor.getModifiers() == Modifier.PUBLIC && o.getClass().getCanonicalName().startsWith("org.olat.")) {
                    String beanClassName = o.getClass().getCanonicalName();
                    if (beanClassName.indexOf("$$") != -1) {
                        if (!whitelist.contains(beanClassName.substring(0, beanClassName.indexOf("$$")))) {
                            fail("No public constructors are allowed in spring managed object: " + o.getClass().getCanonicalName());
                        }
                    } else {
                        if (!whitelist.contains(beanClassName)) {
                            fail("No public constructors are allowed in spring managed object: " + o.getClass().getCanonicalName());
                        }
                    }

                }
            }

        }
    }

}
