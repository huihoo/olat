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
 * Description:<br>
 * Helper methods to create identities that can be used in junit tests. Start the test case with -Djunit.maildomain=mydomain.com to create identities with mail accounts
 * that go to your domain, otherwhise mytrashmail.com will be used
 * <P>
 * Initial Date: 21.11.2006 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH<br>
 *         http://www.frentix.com
 */
package org.olat.test;

import static org.olat.system.security.AuthenticationConstants.AUTHENTICATION_PROVIDER_OLAT;

import java.io.File;
import java.util.List;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Constants;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.user.User;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.DeployableCourseExport;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.user.UserService;
import org.olat.system.commons.encoder.Encoder;
import org.olat.system.spring.CoreSpringFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class JunitTestHelper {

    static String maildomain = System.getProperty("junit.maildomain");
    static {
        if (maildomain == null) {
            maildomain = "mytrashmail.com";
        }
    }

    /**
     * Create an identity with user permissions
     * 
     * @param login
     * @return
     */
    public static final Identity createAndPersistIdentityAsUser(final String login) {
        final BaseSecurity securityManager = getBaseSecurity();
        Identity identity = securityManager.findIdentityByName(login);
        if (identity != null) {
            return identity;
        }
        SecurityGroup group = securityManager.findSecurityGroupByName(Constants.GROUP_OLATUSERS);
        if (group == null) {
            group = securityManager.createAndPersistNamedSecurityGroup(Constants.GROUP_OLATUSERS);
        }
        final User user = getUserService().createUser("first" + login, "last" + login, login + "@" + maildomain);
        identity = securityManager.createAndPersistIdentityAndUser(login, user, AUTHENTICATION_PROVIDER_OLAT, login, Encoder.encrypt("A6B7C8"));
        securityManager.addIdentityToSecurityGroup(identity, group);
        return identity;
    }

    private static BaseSecurity getBaseSecurity() {
        return CoreSpringFactory.getBean(BaseSecurity.class);
    }

    /**
     * Create an identity with author permissions
     * 
     * @param login
     * @return
     */
    public static final Identity createAndPersistIdentityAsAuthor(final String login) {
        final BaseSecurity securityManager = getBaseSecurity();
        Identity identity = securityManager.findIdentityByName(login);
        if (identity != null) {
            return identity;
        }
        SecurityGroup group = securityManager.findSecurityGroupByName(Constants.GROUP_AUTHORS);
        if (group == null) {
            group = securityManager.createAndPersistNamedSecurityGroup(Constants.GROUP_AUTHORS);
        }
        final User user = getUserService().createUser("first" + login, "last" + login, login + "@" + maildomain);
        identity = securityManager.createAndPersistIdentityAndUser(login, user, AUTHENTICATION_PROVIDER_OLAT, login, Encoder.encrypt("A6B7C8"));
        securityManager.addIdentityToSecurityGroup(identity, group);
        return identity;
    }

    /**
     * Create an identity with admin permissions
     * 
     * @param login
     * @return
     */
    public static final Identity createAndPersistIdentityAsAdmin(final String login) {
        final BaseSecurity securityManager = getBaseSecurity();
        Identity identity = securityManager.findIdentityByName(login);
        if (identity != null) {
            return identity;
        }
        SecurityGroup group = securityManager.findSecurityGroupByName(Constants.GROUP_ADMIN);
        if (group == null) {
            group = securityManager.createAndPersistNamedSecurityGroup(Constants.GROUP_ADMIN);
        }
        final User user = getUserService().createUser("first" + login, "last" + login, login + "@" + maildomain);
        identity = securityManager.createAndPersistIdentityAndUser(login, user, AUTHENTICATION_PROVIDER_OLAT, login, Encoder.encrypt("A6B7C8"));
        securityManager.addIdentityToSecurityGroup(identity, group);
        return identity;
    }

    /**
     * Remove identity from <code>Constants.GROUP_OLATUSERS</code> group.
     * 
     * @param identity
     */
    /*
     * public static void deleteIdentityFromUsersGroup(Identity identity) { Manager securityManager = ManagerFactory.getManager(); SecurityGroup group =
     * securityManager.findSecurityGroupByName(Constants.GROUP_OLATUSERS); if (group != null) { securityManager.removeIdentityFromSecurityGroup(identity, group); } }
     */

    /**
     * Deploys/imports the "Demo Course".
     * 
     * @return the created RepositoryEntry
     */
    public static RepositoryEntry deployDemoCourse() {

        RepositoryEntry re = null;
        final PropertyManager propertyManager = PropertyManager.getInstance();
        final List<PropertyImpl> l = propertyManager.findProperties(null, null, null, "_o3_", "deployedCourses");
        if (l.size() > 0) {
            re = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(l.get(0).getLongValue());
            if (re != null) {
                return re;
            }
        }
        createAndPersistIdentityAsAdmin("administrator");
        final DeployableCourseExport export = (DeployableCourseExport) new ClassPathXmlApplicationContext("/org/olat/test/_spring/demoCourseExport.xml")
                .getBean("demoCourse");

        if (!export.getDeployableCourseZipFile().exists()) {
            // do not throw exception as users may upload bad file
            System.out.println("Cannot deploy course from file: " + export.getIdentifier());
            return null;
        }
        File zip = export.getDeployableCourseZipFile();
        re = CourseFactory.deployCourseFromZIP(zip, 4);
        if (re != null) {
            final PropertyImpl prop = propertyManager.createPropertyInstance(null, null, null, "_o3_", "deployedCourses", export.getVersion(), re.getKey(),
                    export.getIdentifier(), null);
            propertyManager.saveProperty(prop);
        }
        zip.delete();
        return re;
    }

    private static UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
