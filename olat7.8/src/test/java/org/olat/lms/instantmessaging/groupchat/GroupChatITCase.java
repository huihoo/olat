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
package org.olat.lms.instantmessaging.groupchat;

import static org.junit.Assert.assertNotSame;

import org.junit.Test;
import org.olat.system.spring.CoreSpringFactory;
import org.olat.test.MockServletContextWebContextLoader;
import org.springframework.test.context.ContextConfiguration;

/**
 * <P>
 * Initial Date: 07.09.2011 <br>
 * 
 * @author guretzki
 */
@ContextConfiguration(loader = MockServletContextWebContextLoader.class, locations = { "classpath:/org/olat/system/logging/threadlog/_spring/threadlogCorecontext.xml",
        "classpath*:**/_spring/*Context.xml", "classpath*:*Context.xml", "classpath*:**/*TestContext.xml" })
public class GroupChatITCase { // extends AbstractJUnit4SpringContextTests {

    @Test
    public void testCreateGroupChat_NotSameInstance() {
        GroupChat_EBL firstChatRoster = CoreSpringFactory.getBean(GroupChat_EBL.class);
        GroupChat_EBL secondChatRoster = CoreSpringFactory.getBean(GroupChat_EBL.class);
        assertNotSame(firstChatRoster, secondChatRoster);
    }

}
