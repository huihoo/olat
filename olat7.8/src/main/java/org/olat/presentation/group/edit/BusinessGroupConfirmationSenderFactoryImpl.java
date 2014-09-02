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
package org.olat.presentation.group.edit;

import org.olat.data.basesecurity.Identity;
import org.olat.data.group.BusinessGroup;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.core.notification.service.AbstractGroupConfirmationInfo;
import org.olat.lms.group.BusinessGroupService;
import org.olat.presentation.group.securitygroup.confirmation.AbstractGroupConfirmationSender;
import org.olat.presentation.group.securitygroup.confirmation.AbstractGroupConfirmationSenderInfo;
import org.olat.presentation.group.securitygroup.confirmation.AbstractWaitingListGroupConfirmationSender;
import org.olat.presentation.group.securitygroup.confirmation.BuddyGroupConfirmationSender;
import org.olat.presentation.group.securitygroup.confirmation.BuddyGroupConfirmationSenderInfo;
import org.olat.presentation.group.securitygroup.confirmation.RightLearningGroupConfirmationSender;
import org.olat.presentation.group.securitygroup.confirmation.RightLearningGroupConfirmationSenderInfo;
import org.olat.presentation.group.securitygroup.confirmation.WaitingListLearningGroupConfirmationSender;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: Nov 8, 2012 <br>
 * 
 * @author Branislav Balaz
 */
public class BusinessGroupConfirmationSenderFactoryImpl implements BusinessGroupConfirmationSenderFactory {

    private final BusinessGroup currBusinessGroup;
    private final RepositoryEntry repositoryEntry;
    private final Identity identity;

    public BusinessGroupConfirmationSenderFactoryImpl(Identity identity, BusinessGroup currBusinessGroup) {
        this.currBusinessGroup = currBusinessGroup;
        this.identity = identity;
        repositoryEntry = getBusinessGroupService().getCourseRepositoryEntryForBusinessGroup(currBusinessGroup);
    }

    private BusinessGroupService getBusinessGroupService() {
        return CoreSpringFactory.getBean(BusinessGroupService.class);
    }

    @Override
    public AbstractGroupConfirmationSender<? extends AbstractGroupConfirmationSenderInfo, ? extends AbstractGroupConfirmationInfo> getOwnersConfirmationSender() {
        return getGroupConfirmationSender();
    }

    @Override
    public AbstractGroupConfirmationSender<? extends AbstractGroupConfirmationSenderInfo, ? extends AbstractGroupConfirmationInfo> getMemberConfirmationSender() {
        return getGroupConfirmationSender();
    }

    @Override
    public AbstractWaitingListGroupConfirmationSender<? extends AbstractGroupConfirmationSenderInfo, ? extends AbstractGroupConfirmationInfo> getWaitingListConfirmationSender() {
        return getWaitingListGroupConfirmationSender();
    }

    private AbstractWaitingListGroupConfirmationSender<? extends AbstractGroupConfirmationSenderInfo, ? extends AbstractGroupConfirmationInfo> getWaitingListGroupConfirmationSender() {
        AbstractWaitingListGroupConfirmationSender<? extends AbstractGroupConfirmationSenderInfo, ? extends AbstractGroupConfirmationInfo> abstractWaitingListGroupConfirmationSender;
        abstractWaitingListGroupConfirmationSender = getWaitingListLearningGroupConfirmationSender();
        return abstractWaitingListGroupConfirmationSender;
    }

    private AbstractWaitingListGroupConfirmationSender<? extends AbstractGroupConfirmationSenderInfo, ? extends AbstractGroupConfirmationInfo> getWaitingListLearningGroupConfirmationSender() {
        AbstractWaitingListGroupConfirmationSender<? extends AbstractGroupConfirmationSenderInfo, ? extends AbstractGroupConfirmationInfo> abstractWaitingListGroupConfirmationSender;
        RightLearningGroupConfirmationSenderInfo confirmationSenderInfo = new RightLearningGroupConfirmationSenderInfo(identity, currBusinessGroup, repositoryEntry);
        abstractWaitingListGroupConfirmationSender = new WaitingListLearningGroupConfirmationSender(confirmationSenderInfo);
        return abstractWaitingListGroupConfirmationSender;
    }

    private AbstractGroupConfirmationSender<? extends AbstractGroupConfirmationSenderInfo, ? extends AbstractGroupConfirmationInfo> getGroupConfirmationSender() {
        AbstractGroupConfirmationSender<? extends AbstractGroupConfirmationSenderInfo, ? extends AbstractGroupConfirmationInfo> abstractGroupConfirmationSender;
        if (BusinessGroup.TYPE_BUDDYGROUP.equals(currBusinessGroup.getType())) {
            abstractGroupConfirmationSender = getBuddyGroupConfirmationSender(identity);
        } else {
            abstractGroupConfirmationSender = getRightLearningGroupConfirmationSender(identity, repositoryEntry);
        }
        return abstractGroupConfirmationSender;
    }

    private AbstractGroupConfirmationSender<? extends AbstractGroupConfirmationSenderInfo, ? extends AbstractGroupConfirmationInfo> getRightLearningGroupConfirmationSender(
            Identity identity, RepositoryEntry repositoryEntry) {
        AbstractGroupConfirmationSender<? extends AbstractGroupConfirmationSenderInfo, ? extends AbstractGroupConfirmationInfo> abstractGroupConfirmationSender;
        RightLearningGroupConfirmationSenderInfo confirmationSenderInfo = new RightLearningGroupConfirmationSenderInfo(identity, currBusinessGroup, repositoryEntry);
        abstractGroupConfirmationSender = new RightLearningGroupConfirmationSender(confirmationSenderInfo);
        return abstractGroupConfirmationSender;
    }

    private AbstractGroupConfirmationSender<? extends AbstractGroupConfirmationSenderInfo, ? extends AbstractGroupConfirmationInfo> getBuddyGroupConfirmationSender(
            Identity identity) {
        AbstractGroupConfirmationSender<? extends AbstractGroupConfirmationSenderInfo, ? extends AbstractGroupConfirmationInfo> abstractGroupConfirmationSender;
        BuddyGroupConfirmationSenderInfo confirmationSenderInfo = new BuddyGroupConfirmationSenderInfo(identity, currBusinessGroup);
        abstractGroupConfirmationSender = new BuddyGroupConfirmationSender(confirmationSenderInfo);
        return abstractGroupConfirmationSender;
    }

}
