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

package org.olat.lms.group.right;

import java.util.ArrayList;
import java.util.List;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Policy;
import org.olat.data.group.BusinessGroup;
import org.olat.data.group.context.BGContext;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.exception.AssertException;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<BR>
 * TODO: Class Description for BGRightManagerImpl Initial Date: Aug 24, 2004
 * 
 * @author gnaegi
 */
public class BGRightManagerImpl extends BasicManager implements BGRightManager {

    private static BGRightManagerImpl INSTANCE;
    static {
        INSTANCE = new BGRightManagerImpl();
    }

    /**
     * @return singleton instance
     */
    @Deprecated
    public static BGRightManagerImpl getInstance() {
        return INSTANCE;
    }

    private BGRightManagerImpl() {
        // no public constructor
    }

    /**
	 */
    @Override
    public void addBGRight(final String bgRight, final BusinessGroup rightGroup) {
        if (bgRight.indexOf(BG_RIGHT_PREFIX) == -1) {
            throw new AssertException("Groups rights must start with prefix '" + BG_RIGHT_PREFIX + "', but given right is ::" + bgRight);
        }
        if (BusinessGroup.TYPE_RIGHTGROUP.equals(rightGroup.getType())) {
            final BaseSecurity secm = getBaseSecurity();
            final BGContext context = rightGroup.getGroupContext();
            secm.createAndPersistPolicy(rightGroup.getPartipiciantGroup(), bgRight, context);
        } else {
            throw new AssertException("Only right groups can have bg rights, but type was ::" + rightGroup.getType());
        }
    }

    private BaseSecurity getBaseSecurity() {
        return CoreSpringFactory.getBean(BaseSecurity.class);
    }

    /**
	 */
    @Override
    public void removeBGRight(final String bgRight, final BusinessGroup rightGroup) {
        if (BusinessGroup.TYPE_RIGHTGROUP.equals(rightGroup.getType())) {
            final BaseSecurity secm = getBaseSecurity();
            final BGContext context = rightGroup.getGroupContext();
            secm.deletePolicy(rightGroup.getPartipiciantGroup(), bgRight, context);
        } else {
            throw new AssertException("Only right groups can have bg rights, but type was ::" + rightGroup.getType());
        }
    }

    /*
     * public boolean hasBGRight(String bgRight, BusinessGroup rightGroup) { if (BusinessGroup.TYPE_RIGHTGROUP.equals(rightGroup.getType())) { Manager secm =
     * ManagerFactory.getManager(); return secm.isGroupPermittedOnResourceable(rightGroup.getPartipiciantGroup(), bgRight, rightGroup.getGroupContext()); } throw new
     * AssertException("Only right groups can have bg rights, but type was ::" + rightGroup.getType()); }
     */

    /**
	 */
    @Override
    public boolean hasBGRight(final String bgRight, final Identity identity, final BGContext bgContext) {
        if (BusinessGroup.TYPE_RIGHTGROUP.equals(bgContext.getGroupType())) {
            final BaseSecurity secm = getBaseSecurity();
            return secm.isIdentityPermittedOnResourceable(identity, bgRight, bgContext);
        }
        throw new AssertException("Only right groups can have bg rights, but type was ::" + bgContext.getGroupType());
    }

    /**
	 */
    @Override
    public List<String> findBGRights(final BusinessGroup rightGroup) {
        final BaseSecurity secm = getBaseSecurity();
        final List<Policy> results = secm.getPoliciesOfSecurityGroup(rightGroup.getPartipiciantGroup());
        // filter all business group rights permissions. group right permissions
        // start with bgr.
        final List<String> rights = new ArrayList<String>();
        for (int i = 0; i < results.size(); i++) {
            final Policy rightPolicy = results.get(i);
            final String right = rightPolicy.getPermission();
            if (right.indexOf(BG_RIGHT_PREFIX) == 0) {
                rights.add(right);
            }
        }
        return rights;
    }
}
