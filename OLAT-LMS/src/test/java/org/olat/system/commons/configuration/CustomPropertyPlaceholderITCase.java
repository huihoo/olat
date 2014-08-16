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
package org.olat.system.commons.configuration;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * todo: introduce a custom property like blabla.testutil.do.not.delete which can be written
 * 
 * <P>
 * Initial Date: 09.06.2011 <br>
 * 
 * @author guido
 */
@ContextConfiguration(locations = { "classpath:/org/olat/system/commons/configuration/olatCustomPropTestCont.xml" })
public class CustomPropertyPlaceholderITCase extends AbstractJUnit4SpringContextTests {

    private SystemPropertiesLoader propLoader;

    /**
     * custom setup that should run before spring comes into play
     */
		public CustomPropertyPlaceholderITCase() {
        propLoader = new SystemPropertiesLoader();
        propLoader.init();
        propLoader.setProperty("db.vendor", "unittest");
    }

    @Autowired
    SimpleBean bean;

    @Test
    public void testOverriddenProperty() {
        assertEquals("spring falied to override the property 'db.vendor'", "unittest", bean.getInjectedValue());
    }

    @After
    public void shutdown() {
        propLoader.setProperty("db.vendor", "");
    }

}
