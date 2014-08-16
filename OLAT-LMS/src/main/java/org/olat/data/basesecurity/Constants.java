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

package org.olat.data.basesecurity;

import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;

/**
 * Description: <br>
 * 
 * Collection of constants stored in DB, and referenced in all layers.
 * 
 * @author Felix Jost
 */
public class Constants {

    private static final String BASE_SECURITY_TYPE = "BaseSecurityModule";

    private static final String COURSE_TYPE = "CourseModule";

    /**
     * <code>GROUP_OLATUSERS</code> predfined groups length restricted to 16 chars!
     */
    public static final String GROUP_OLATUSERS = "users";
    /**
     * <code>GROUP_ADMIN</code> predefined groups length restricted to 16 chars!
     */
    public static final String GROUP_ADMIN = "admins";
    /**
     * <code>GROUP_USERMANAGERS</code> predefined groups length restricted to 16 chars!
     */
    public static final String GROUP_USERMANAGERS = "usermanagers";
    /**
     * <code>GROUP_AUTHORS</code> predefined groups length restricted to 16 chars!
     */
    public static final String GROUP_AUTHORS = "authors";
    /**
     * <code>GROUP_INST_ORES_MANAGER</code> predefined groups length restricted to 16 chars!
     */
    public static final String GROUP_INST_ORES_MANAGER = "instoresmanager";
    /**
     * <code>GROUP_GROUPMANAGERS</code> predefined groups length restricted to 16 chars!
     */
    public static final String GROUP_GROUPMANAGERS = "groupmanagers";
    /**
     * <code>GROUP_ANONYMOUS</code> predefined groups length restricted to 16 chars!
     */
    public static final String GROUP_ANONYMOUS = "anonymous";

    /**
     * access a thing; means read, write, update, and delete <code>PERMISSION_ACCESS</code> predefined permissions length restricted to 16 chars!
     */
    public static final String PERMISSION_ACCESS = "access";

    /**
     * access a thing; means read, write, update, and delete <code>PERMISSION_READ</code> predefined permissions length restricted to 16 chars!
     */
    public static final String PERMISSION_READ = "read";
    /**
     * access a thing; means read, write, update, and delete <code>PERMISSION_WRITE</code> predefined permissions length restricted to 16 chars!
     */
    private static final String PERMISSION_WRITE = "write";
    /**
     * access a thing; means read, write, update, and delete <code>PERMISSION_UPDATE</code> predefined permissions length restricted to 16 chars!
     */
    private static final String PERMISSION_UPDATE = "update";
    /**
     * access a thing; means read, write, update, and delete <code>PERMISSION_DELETE</code> predefined permissions length restricted to 16 chars!
     */
    private static final String PERMISSION_DELETE = "delete";

    /**
     * group context permissions <code>PERMISSION_PARTI</code>
     */
    public static final String PERMISSION_PARTI = "participant";
    /**
     * <code>PERMISSION_COACH</code>
     */
    public static final String PERMISSION_COACH = "coach";

    /**
     * having a role; like being author <code>PERMISSION_HASROLE</code>
     */
    public static final String PERMISSION_HASROLE = "hasRole";

    /**
     * admin of e.g. the whole olat, or: a course, or: a buddy group <code>PERMISSION_ADMIN</code>
     */
    public static final String PERMISSION_ADMIN = "admin";

    /**
     * length restricted to 50 chars! <br>
     * TYPE resource for the whole olat system (e.g. used with permission PERMISSION_LOGINDENIED) <br>
     * <code>ORESOURCE_OLAT</code>
     */
    private static final OLATResourceable ORESOURCE_OLAT = OresHelper.createOLATResourceableType(BASE_SECURITY_TYPE, "WHOLE-OLAT");

    /**
     * resourceable TYPE for olat administrators <code>ORESOURCE_ADMIN</code>
     */
    public static final OLATResourceable ORESOURCE_ADMIN = OresHelper.createOLATResourceableType(BASE_SECURITY_TYPE, "RAdmins");

    /**
     * resourceable TYPE for authors <code>ORESOURCE_AUTHOR</code>
     */
    public static final OLATResourceable ORESOURCE_AUTHOR = OresHelper.createOLATResourceableType(BASE_SECURITY_TYPE, "RAuthor");

    /**
     * resourceable TYPE for groupmanagers <code>ORESOURCE_GROUPMANAGER</code>
     */
    public static final OLATResourceable ORESOURCE_GROUPMANAGER = OresHelper.createOLATResourceableType(BASE_SECURITY_TYPE, "RGroupmanager");

    /**
     * resourceable TYPE for usermanagers <code>ORESOURCE_USERMANAGER</code>
     */
    public static final OLATResourceable ORESOURCE_USERMANAGER = OresHelper.createOLATResourceableType(BASE_SECURITY_TYPE, "RUsermanager");

    /**
     * resourceable TYPE for institutionalresourcemanager <code>ORESOURCE_INSTORESMANAGER</code>
     */
    public static final OLATResourceable ORESOURCE_INSTORESMANAGER = OresHelper.createOLATResourceableType(BASE_SECURITY_TYPE, "RResmanager");

    /**
     * resourceable TYPE for all security groups <code>ORESOURCE_SECURITYGROUPS</code>
     */
    static final OLATResourceable ORESOURCE_SECURITYGROUPS = OresHelper.createOLATResourceableType(BASE_SECURITY_TYPE, "SecGroup");

    /**
     * resourceable TYPE for all courses <code>ORESOURCE_COURSES</code>
     */
    static final OLATResourceable ORESOURCE_COURSES = OresHelper.createOLATResourceableType(COURSE_TYPE, null);

    /**
     * resourceable TYPE for olat users (everybody but guests) <code>ORESOURCE_USERS</code>
     */
    static final OLATResourceable ORESOURCE_USERS = OresHelper.createOLATResourceableType(BASE_SECURITY_TYPE, "RUsers");

    /**
     * resourceable TYPE for olat guests (restricted functionality) <code>ORESOURCE_GUESTONLY</code>
     */
    public static final OLATResourceable ORESOURCE_GUESTONLY = OresHelper.createOLATResourceableType(BASE_SECURITY_TYPE, "RGuestOnly");

}
