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

import org.olat.data.basesecurity.Identity;
import org.olat.data.infomessage.InfoMessage;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;

/**
 * Description:<br>
 * <P>
 * Initial Date: 28 jul. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public abstract class InfoMessageFrontendManager extends BasicManager {

    public static final OLATResourceable oresFrontend = OresHelper.lookupType(InfoMessageFrontendManager.class);

    public abstract InfoMessage loadInfoMessage(Long key);

    public abstract InfoMessage createInfoMessage(OLATResourceable ores, String subPath, String businessPath, Identity author);

    public abstract void saveInfoMessage(final InfoMessage infoMessage);

    public abstract void deleteInfoMessage(InfoMessage infoMessage);

    public abstract List<InfoMessage> loadInfoMessageByResource(OLATResourceable ores, String subPath, String businessPath, Date after, Date before, int firstResult,
            int maxReturn);

    public abstract int countInfoMessageByResource(OLATResourceable ores, String subPath, String businessPath, Date after, Date before);

}
