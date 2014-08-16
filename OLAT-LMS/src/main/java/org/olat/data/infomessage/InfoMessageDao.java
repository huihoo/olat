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

package org.olat.data.infomessage;

import java.util.Date;
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.system.commons.resource.OLATResourceable;

public abstract class InfoMessageDao {

    public abstract InfoMessage createInfoMessage(OLATResourceable ores, String subPath, String businessPath, Identity author);

    public abstract void saveInfoMessage(InfoMessage infoMessage);

    public abstract void deleteInfoMessage(InfoMessage infoMessage);

    public abstract InfoMessage loadInfoMessageByKey(Long key);

    public abstract List<InfoMessage> loadInfoMessageByResource(OLATResourceable ores, String subPath, String businessPath, Date after, Date before, int firstResult,
            int maxReturn);

    public abstract int countInfoMessageByResource(OLATResourceable ores, String subPath, String businessPath, Date after, Date before);
}
