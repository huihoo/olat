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

package org.olat.data.marking;

import java.util.Collection;
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * Description:<br>
 * TODO: srosse Class Description for MarkManager
 * <P>
 * Initial Date: 9 mars 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
public interface MarkDAO {

    public List<Mark> getMarks(OLATResourceable ores, Identity identity, Collection<String> subPaths);

    public boolean isMarked(OLATResourceable ores, Identity identity, String subPath);

    public Mark setMark(OLATResourceable ores, Identity identity, String subPath, String businessPath);

    public void moveMarks(OLATResourceable ores, String oldSubPath, String newSubPath);

    public void removeMark(OLATResourceable ores, Identity identity, String subPath);

    public void removeMark(Mark mark);

    public void deleteMark(OLATResourceable ores);

    public void deleteMark(OLATResourceable ores, String subPath);

    public List<MarkResourceStat> getStats(OLATResourceable ores, List<String> subPaths, Identity identity);
}
