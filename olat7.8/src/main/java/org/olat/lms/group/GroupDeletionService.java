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
package org.olat.lms.group;

import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.group.BusinessGroup;
import org.olat.presentation.framework.core.translator.Translator;

/**
 * <P>
 * Initial Date: 29.06.2011 <br>
 * 
 * @author guido
 */
public interface GroupDeletionService {

    public void setLastUsageDuration(final int lastUsageDuration);

    /**
     * @param object
     */
    public void setLastUsageNowFor(BusinessGroup group);

    /**
     * @param groupsReadyToDelete
     */
    public void deleteGroups(List<BusinessGroup> groupsReadyToDelete);

    /**
     * @return
     */
    public int getDeleteEmailDuration();

    /**
     * @param deleteEmailDuration
     * @return
     */
    public List<BusinessGroup> getGroupsReadyToDelete(int deleteEmailDuration);

    /**
     * @param deleteEmailDuration
     * @return
     */
    public List<BusinessGroup> getGroupsInDeletionProcess(int deleteEmailDuration);

    /**
     * @return
     */
    public int getLastUsageDuration();

    /**
     * @param deleteEmailDuration
     */
    public void setDeleteEmailDuration(int deleteEmailDuration);

    /**
     * @return warning text in case of mail send errors
     */
    public String sendDeleteEmailTo(List<BusinessGroup> selectedGroups, Identity sender, Translator translator);

    /**
     * @param lastUsageDuration
     * @return
     */
    public List<BusinessGroup> getDeletableGroups(int lastUsageDuration);

}
