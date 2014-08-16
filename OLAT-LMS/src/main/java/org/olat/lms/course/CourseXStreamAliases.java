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
package org.olat.lms.course;

import org.olat.data.commons.xml.XStreamHelper;
import org.olat.lms.course.assessment.EfficiencyStatement;
import org.olat.lms.course.condition.Condition;
import org.olat.lms.course.condition.ExtendedCondition;
import org.olat.lms.course.condition.operators.AttributeEndswithOperator;
import org.olat.lms.course.condition.operators.AttributeStartswithOperator;
import org.olat.lms.course.condition.operators.EqualsOperator;
import org.olat.lms.course.condition.operators.GreaterThanEqualsOperator;
import org.olat.lms.course.condition.operators.GreaterThanOperator;
import org.olat.lms.course.condition.operators.HasAttributeOperator;
import org.olat.lms.course.condition.operators.HasNotAttributeOperator;
import org.olat.lms.course.condition.operators.IsInAttributeOperator;
import org.olat.lms.course.condition.operators.IsNotInAttributeOperator;
import org.olat.lms.course.condition.operators.LowerThanEqualsOperator;
import org.olat.lms.course.condition.operators.LowerThanOperator;
import org.olat.lms.course.config.CourseConfig;
import org.olat.lms.course.nodes.AssessableCourseNode;
import org.olat.lms.course.nodes.BCCourseNode;
import org.olat.lms.course.nodes.BasicLTICourseNode;
import org.olat.lms.course.nodes.BlogCourseNode;
import org.olat.lms.course.nodes.COCourseNode;
import org.olat.lms.course.nodes.CPCourseNode;
import org.olat.lms.course.nodes.CalCourseNode;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.DialogCourseNode;
import org.olat.lms.course.nodes.ENCourseNode;
import org.olat.lms.course.nodes.FOCourseNode;
import org.olat.lms.course.nodes.IQSELFCourseNode;
import org.olat.lms.course.nodes.IQSURVCourseNode;
import org.olat.lms.course.nodes.IQTESTCourseNode;
import org.olat.lms.course.nodes.InfoCourseNode;
import org.olat.lms.course.nodes.MSCourseNode;
import org.olat.lms.course.nodes.PodcastCourseNode;
import org.olat.lms.course.nodes.PortfolioCourseNode;
import org.olat.lms.course.nodes.ProjectBrokerCourseNode;
import org.olat.lms.course.nodes.SPCourseNode;
import org.olat.lms.course.nodes.STCourseNode;
import org.olat.lms.course.nodes.ScormCourseNode;
import org.olat.lms.course.nodes.TACourseNode;
import org.olat.lms.course.nodes.TUCourseNode;
import org.olat.lms.course.nodes.WikiCourseNode;
import org.olat.lms.course.tree.CourseEditorTreeModel;
import org.olat.lms.course.tree.CourseEditorTreeNode;

import com.thoughtworks.xstream.XStream;

/**
 * Helper class for course related aliases.
 * 
 * <P>
 * Initial Date: 11.04.2011 <br>
 * 
 * @author lavinia
 */
public class CourseXStreamAliases {

    /**
     * Used for reading editortreemodel.xml and runstructure.xml. Creates a new XStream with the aliases used in the mentioned xml files.
     * 
     * @return
     */
    public static XStream getCourseXStream() {
        XStream xstream = XStreamHelper.createXStreamInstance();

        xstream.alias("org.olat.course.tree.CourseEditorTreeModel", CourseEditorTreeModel.class);
        xstream.alias("CourseEditorTreeModel", CourseEditorTreeModel.class);

        xstream.alias("org.olat.course.tree.CourseEditorTreeNode", CourseEditorTreeNode.class);
        xstream.alias("CourseEditorTreeNode", CourseEditorTreeNode.class);

        xstream.alias("org.olat.course.Structure", Structure.class);
        xstream.alias("Structure", Structure.class);

        xstream.alias("org.olat.course.nodes.AssessableCourseNode", AssessableCourseNode.class);
        xstream.alias("AssessableCourseNode", AssessableCourseNode.class);

        xstream.alias("org.olat.course.nodes.BasicLTICourseNode", BasicLTICourseNode.class);
        xstream.alias("BasicLTICourseNode", BasicLTICourseNode.class);

        xstream.alias("org.olat.course.nodes.BCCourseNode", BCCourseNode.class);
        xstream.alias("BCCourseNode", BCCourseNode.class);

        xstream.alias("org.olat.course.nodes.BlogCourseNode", BlogCourseNode.class);
        xstream.alias("BlogCourseNode", BlogCourseNode.class);

        xstream.alias("org.olat.course.nodes.CalCourseNode", CalCourseNode.class);
        xstream.alias("CalCourseNode", CalCourseNode.class);

        xstream.alias("org.olat.course.nodes.COCourseNode", COCourseNode.class);
        xstream.alias("COCourseNode", COCourseNode.class);

        xstream.alias("org.olat.course.nodes.CourseNode", CourseNode.class);
        xstream.alias("CourseNode", CourseNode.class);

        xstream.alias("org.olat.course.nodes.CPCourseNode", CPCourseNode.class);
        xstream.alias("CPCourseNode", CPCourseNode.class);

        xstream.alias("org.olat.course.nodes.DialogCourseNode", DialogCourseNode.class);
        xstream.alias("DialogCourseNode", DialogCourseNode.class);

        xstream.alias("org.olat.course.nodes.ENCourseNode", ENCourseNode.class);
        xstream.alias("ENCourseNode", ENCourseNode.class);

        xstream.alias("org.olat.course.nodes.FOCourseNode", FOCourseNode.class);
        xstream.alias("FOCourseNode", FOCourseNode.class);

        xstream.alias("org.olat.course.nodes.InfoCourseNode", InfoCourseNode.class);
        xstream.alias("InfoCourseNode", InfoCourseNode.class);

        xstream.alias("org.olat.course.nodes.IQSELFCourseNode", IQSELFCourseNode.class);
        xstream.alias("IQSELFCourseNode", IQSELFCourseNode.class);

        xstream.alias("org.olat.course.nodes.IQSURVCourseNode", IQSURVCourseNode.class);
        xstream.alias("IQSURVCourseNode", IQSURVCourseNode.class);

        xstream.alias("org.olat.course.nodes.IQTESTCourseNode", IQTESTCourseNode.class);
        xstream.alias("IQTESTCourseNode", IQTESTCourseNode.class);

        xstream.alias("org.olat.course.nodes.MSCourseNode", MSCourseNode.class);
        xstream.alias("MSCourseNode", MSCourseNode.class);

        xstream.alias("org.olat.course.nodes.PodcastCourseNode", PodcastCourseNode.class);
        xstream.alias("PodcastCourseNode", PodcastCourseNode.class);

        xstream.alias("org.olat.course.nodes.PortfolioCourseNode", PortfolioCourseNode.class);
        xstream.alias("PortfolioCourseNode", PortfolioCourseNode.class);

        xstream.alias("org.olat.course.nodes.ProjectBrokerCourseNode", ProjectBrokerCourseNode.class);
        xstream.alias("ProjectBrokerCourseNode", ProjectBrokerCourseNode.class);

        xstream.alias("org.olat.course.nodes.ScormCourseNode", ScormCourseNode.class);
        xstream.alias("ScormCourseNode", ScormCourseNode.class);

        xstream.alias("org.olat.course.nodes.SPCourseNode", SPCourseNode.class);
        xstream.alias("SPCourseNode", SPCourseNode.class);

        xstream.alias("org.olat.course.nodes.STCourseNode", STCourseNode.class);
        xstream.alias("STCourseNode", STCourseNode.class);

        xstream.alias("org.olat.course.nodes.TACourseNode", TACourseNode.class);
        xstream.alias("TACourseNode", TACourseNode.class);

        xstream.alias("org.olat.course.nodes.TUCourseNode", TUCourseNode.class);
        xstream.alias("TUCourseNode", TUCourseNode.class);

        xstream.alias("org.olat.course.nodes.WikiCourseNode", WikiCourseNode.class);
        xstream.alias("WikiCourseNode", WikiCourseNode.class);

        xstream.alias("org.olat.course.condition.ExtendedCondition", ExtendedCondition.class);
        xstream.alias("ExtendedCondition", ExtendedCondition.class);

        xstream.alias("org.olat.lms.course.condition.Condition", Condition.class);
        xstream.alias("Condition", Condition.class);

        // conditions can hold operators and they get serialized as well. So we need all of the as aliases

        xstream.alias("org.olat.course.condition.operators.IsInAttributeOperator", IsInAttributeOperator.class);
        xstream.alias("IsInAttributeOperator", IsInAttributeOperator.class);

        xstream.alias("org.olat.course.condition.operators.EqualsOperator", EqualsOperator.class);
        xstream.alias("EqualsOperator", EqualsOperator.class);

        xstream.alias("org.olat.course.condition.operators.GreaterThanEqualsOperator", GreaterThanEqualsOperator.class);
        xstream.alias("GreaterThanEqualsOperator", GreaterThanEqualsOperator.class);

        xstream.alias("org.olat.course.condition.operators.GreaterThanOperator", GreaterThanOperator.class);
        xstream.alias("GreaterThanOperator", GreaterThanOperator.class);

        xstream.alias("org.olat.course.condition.operators.LowerThanEqualsOperator", LowerThanEqualsOperator.class);
        xstream.alias("LowerThanEqualsOperator", LowerThanEqualsOperator.class);

        xstream.alias("org.olat.course.condition.operators.LowerThanOperator", LowerThanOperator.class);
        xstream.alias("LowerThanOperator", LowerThanOperator.class);

        xstream.alias("org.olat.course.condition.operators.IsNotInAttributeOperator", IsNotInAttributeOperator.class);
        xstream.alias("IsNotInAttributeOperator", IsNotInAttributeOperator.class);

        xstream.alias("org.olat.course.condition.operators.HasAttributeOperator", HasAttributeOperator.class);
        xstream.alias("HasAttributeOperator", HasAttributeOperator.class);

        xstream.alias("org.olat.course.condition.operators.HasNotAttributeOperator", HasNotAttributeOperator.class);
        xstream.alias("HasNotAttributeOperator", HasNotAttributeOperator.class);

        xstream.alias("org.olat.course.condition.operators.AttributeStartswithOperator", AttributeStartswithOperator.class);
        xstream.alias("AttributeStartswithOperator", AttributeStartswithOperator.class);

        xstream.alias("org.olat.course.condition.operators.AttributeEndswithOperator", AttributeEndswithOperator.class);
        xstream.alias("AttributeEndswithOperator", AttributeEndswithOperator.class);

        return xstream;
    }

    /**
     * Used for reading CourseConfig.xml. Creates a new XStream with the aliases used in the mentioned xml file.
     * 
     * @return
     */
    public static XStream getCourseConfigXStream() {
        XStream xstream = XStreamHelper.createXStreamInstance();

        xstream.alias("org.olat.course.config.CourseConfig", CourseConfig.class);
        xstream.alias("CourseConfig", CourseConfig.class);

        return xstream;
    }

    /**
     * Used for serialize/deserialize EfficiencyStatement from xml stored in DB.
     * 
     * @return
     */
    public static XStream getEfficiencyStatementXStream() {
        XStream xstream = XStreamHelper.createXStreamInstance();

        xstream.alias("org.olat.course.assessment.EfficiencyStatement", EfficiencyStatement.class);
        xstream.alias("EfficiencyStatement", EfficiencyStatement.class);

        return xstream;
    }

}
