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

package org.olat.presentation.course.assessment;

import java.util.List;
import java.util.Map;

import org.olat.lms.course.assessment.AssessmentHelper;
import org.olat.lms.course.assessment.EfficiencyStatement;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;
import org.olat.system.commons.StringHelper;

/**
 * Description:<br>
 * Table model to display a list of efficiency statements
 * <P>
 * Initial Date: 12.08.2005 <br>
 * 
 * @author gnaegi
 */
public class EfficiencyStatementsListModel extends DefaultTableDataModel {

    /**
     * @param list
     *            of efficiencyStatements
     */
    public EfficiencyStatementsListModel(final List efficiencyStatements) {
        super(efficiencyStatements);
    }

    /**
	 */
    @Override
    public int getColumnCount() {
        return 2;
    }

    /**
	 */
    @Override
    public Object getValueAt(final int row, final int col) {
        final EfficiencyStatement efficiencyStatement = getEfficiencyStatementAt(row);
        final List nodeData = efficiencyStatement.getAssessmentNodes();
        final Map rootNode = (Map) nodeData.get(0);
        switch (col) {
        case 0:
            return StringHelper.escapeHtml(efficiencyStatement.getCourseTitle());
        case 1:
            return rootNode.get(AssessmentHelper.KEY_SCORE);
        case 2:
            return rootNode.get(AssessmentHelper.KEY_PASSED);
        default:
            return "ERROR";
        }
    }

    /**
     * @param row
     * @return the efficiencyStatement at the given row
     */
    public EfficiencyStatement getEfficiencyStatementAt(final int row) {
        return (EfficiencyStatement) objects.get(row);
    }
}
