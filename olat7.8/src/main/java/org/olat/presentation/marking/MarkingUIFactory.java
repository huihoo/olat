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
package org.olat.presentation.marking;

import org.olat.data.marking.Mark;
import org.olat.data.marking.MarkDAO;
import org.olat.data.marking.MarkResourceStat;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.spring.CoreSpringFactory;

/**
 * TODO: Class Description for MarkingUIFactory
 * 
 * <P>
 * Initial Date: 14.06.2011 <br>
 * 
 * @author guido
 */
public class MarkingUIFactory {

    public static Controller getMarkController(UserRequest ureq, WindowControl wControl, OLATResourceable ores, String subPath, String businessPath) {
        MarkDAO markDAO = (MarkDAO) CoreSpringFactory.getBean(MarkDAO.class);
        Controller controller = new MarkController(ureq, wControl, ores, subPath, businessPath, markDAO);
        return controller;
    }

    public static Controller getMarkController(UserRequest ureq, WindowControl wControl, Mark mark) {
        MarkDAO markDAO = (MarkDAO) CoreSpringFactory.getBean(MarkDAO.class);
        Controller controller = new MarkController(ureq, wControl, mark.getOLATResourceable(), mark.getResSubPath(), mark.getBusinessPath(), markDAO);
        return controller;
    }

    public static Controller getMarkController(UserRequest ureq, WindowControl wControl, Mark mark, MarkResourceStat stat, OLATResourceable ores, String subPath,
            String businessPath) {
        MarkDAO markDAO = (MarkDAO) CoreSpringFactory.getBean(MarkDAO.class);
        Controller controller = new MarkController(ureq, wControl, mark, stat, ores, subPath, businessPath, markDAO);
        return controller;
    }

}
