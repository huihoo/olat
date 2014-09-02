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
package org.olat.presentation.portfolio.artefacts.run;

import java.util.List;

import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;
import org.olat.system.commons.StringHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * datamodel for a table with artefacts in it
 * <P>
 * Initial Date: 20.08.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class ArtefactTableDataModel extends DefaultTableDataModel {

    private final EPFrontendManager ePFMgr;

    public ArtefactTableDataModel(final List<AbstractArtefact> artefacts) {
        super(artefacts);
        ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean(EPFrontendManager.class);
    }

    /**
	 */
    @Override
    public int getColumnCount() {
        return 6;
    }

    /**
	 */
    @Override
    public Object getValueAt(final int row, final int col) {
        final AbstractArtefact artefact = (AbstractArtefact) objects.get(row);
        switch (col) {
        case 0:
            return artefact.getTitle();
        case 1:
            return artefact.getDescription();
        case 2:
            return artefact.getCreationDate();
        case 3:
            return artefact.getAuthor().getName();
        case 4:
            final List<String> artTags = ePFMgr.getArtefactTags(artefact);
            return StringHelper.formatAsCSVString(artTags);
        case 5:
            return artefact;
        default:
            return "ERROR";
        }

    }

}
