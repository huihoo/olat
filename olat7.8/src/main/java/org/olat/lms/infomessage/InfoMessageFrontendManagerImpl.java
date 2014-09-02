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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.lms.infomessage;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.infomessage.InfoMessage;
import org.olat.data.infomessage.InfoMessageDao;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.event.MultiUserEvent;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description:<br>
 * <P>
 * Initial Date: 28 juil. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service
public class InfoMessageFrontendManagerImpl extends InfoMessageFrontendManager {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    private CoordinatorManager coordinatorManager;
    @Autowired
    private InfoMessageDao infoMessageManager;

    /**
     * [used by Spring]
     */
    private InfoMessageFrontendManagerImpl() {
        //
    }

    @Override
    public InfoMessage loadInfoMessage(final Long key) {
        return infoMessageManager.loadInfoMessageByKey(key);
    }

    @Override
    public InfoMessage createInfoMessage(final OLATResourceable ores, final String subPath, final String businessPath, final Identity author) {
        return infoMessageManager.createInfoMessage(ores, subPath, businessPath, author);
    }

    @Override
    public void saveInfoMessage(final InfoMessage infoMessage) {
        infoMessageManager.saveInfoMessage(infoMessage);

        final MultiUserEvent mue = new MultiUserEvent("new_info_message");
        coordinatorManager.getCoordinator().getEventBus().fireEventToListenersOf(mue, oresFrontend);
    }

    @Override
    public void deleteInfoMessage(final InfoMessage infoMessage) {
        infoMessageManager.deleteInfoMessage(infoMessage);
    }

    @Override
    public List<InfoMessage> loadInfoMessageByResource(final OLATResourceable ores, final String subPath, final String businessPath, final Date after, final Date before,
            final int firstResult, final int maxReturn) {
        return infoMessageManager.loadInfoMessageByResource(ores, subPath, businessPath, after, before, firstResult, maxReturn);
    }

    @Override
    public int countInfoMessageByResource(final OLATResourceable ores, final String subPath, final String businessPath, final Date after, final Date before) {
        return infoMessageManager.countInfoMessageByResource(ores, subPath, businessPath, after, before);
    }

}
