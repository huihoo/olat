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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.olat.data.basesecurity.Identity;
import org.olat.data.properties.PropertyImpl;
import org.olat.lms.course.assessment.AssessedIdentityWrapper;
import org.olat.lms.course.assessment.AssessmentHelper;
import org.olat.lms.course.nodes.AssessableCourseNode;
import org.olat.lms.course.nodes.ta.StatusManager;
import org.olat.lms.course.properties.CoursePropertyManager;
import org.olat.lms.course.run.scoring.ScoreEvaluation;
import org.olat.lms.user.UserService;
import org.olat.lms.user.propertyhandler.UserPropertyHandler;
import org.olat.presentation.course.nodes.ta.StatusForm;
import org.olat.presentation.framework.core.components.table.BooleanColumnDescriptor;
import org.olat.presentation.framework.core.components.table.ColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: Jun 23, 2004
 * 
 * @author gnaegi
 */
public class AssessedIdentitiesTableDataModel extends DefaultTableDataModel {

    private int colCount;
    private final AssessableCourseNode courseNode;

    private static final String COL_NAME = "name";
    private static final String COL_DETAILS = "details";
    private static final String COL_ATTEMPTS = "attempts";
    private static final String COL_SCORE = "score";
    private static final String COL_PASSED = "passed";
    private static final String COL_STATUS = "status";

    private final List colMapping;
    private final List<String> userPropertyNameList;
    private final List<UserPropertyHandler> userPropertyHandlers;
    private static final String usageIdentifyer = AssessedIdentitiesTableDataModel.class.getCanonicalName();
    private final Translator translator;

    /**
     * @param objects
     *            List of wrapped identities (AssessedIdentityWrapper)
     * @param courseNode
     *            the current courseNode
     */
    public AssessedIdentitiesTableDataModel(final List objects, final AssessableCourseNode courseNode, final Locale locale, final boolean isAdministrativeUser) {
        super(objects);
        this.courseNode = courseNode;
        this.setLocale(locale);
        this.translator = PackageUtil.createPackageTranslator(this.getClass(), locale);

        userPropertyHandlers = getUserService().getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);

        colCount = 0; // default
        colMapping = new ArrayList();
        // store all configurable column positions in a lookup array
        colMapping.add(colCount++, COL_NAME);
        final Iterator<UserPropertyHandler> propHandlerIterator = userPropertyHandlers.iterator();
        userPropertyNameList = new ArrayList<String>();
        while (propHandlerIterator.hasNext()) {
            final String propHandlerName = propHandlerIterator.next().getName();
            userPropertyNameList.add(propHandlerName);
            colMapping.add(colCount++, propHandlerName);
        }

        if (courseNode != null) {
            if (courseNode.hasDetails()) {
                colMapping.add(colCount++, COL_DETAILS);
            }
            if (courseNode.hasAttemptsConfigured()) {
                colMapping.add(colCount++, COL_ATTEMPTS);
            }
            if (courseNode.hasScoreConfigured()) {
                colMapping.add(colCount++, COL_SCORE);
            }
            if (courseNode.hasStatusConfigured()) {
                colMapping.add(colCount++, COL_STATUS);
            }
            if (courseNode.hasPassedConfigured()) {
                colMapping.add(colCount++, COL_PASSED);
            }
        }
    }

    /**
	 */
    @Override
    public int getColumnCount() {
        return colCount;
    }

    /**
     * @param row
     *            The row number
     * @return The wrapped identity for this row
     */
    public AssessedIdentityWrapper getWrappedIdentity(final int row) {
        return (AssessedIdentityWrapper) getObject(row);
    }

    /**
     * @param row
     *            The row number
     * @return The identity for this row
     */
    public Identity getIdentity(final int row) {
        final AssessedIdentityWrapper wrappedIdentity = getWrappedIdentity(row);
        return wrappedIdentity.getIdentity();
    }

    /**
	 */
    @Override
    public Object getValueAt(final int row, final int col) {
        final AssessedIdentityWrapper wrappedIdentity = (AssessedIdentityWrapper) getObject(row);
        final Identity identity = wrappedIdentity.getIdentity();

        // lookup the column name first and
        // deliver value based on the column name
        final String colName = (String) colMapping.get(col);
        if (colName.equals(COL_NAME)) {
            return identity.getName();
        } else if (userPropertyNameList.contains(colName)) {
            return getUserService().getUserProperty(identity.getUser(), colName, getLocale());
        } else if (colName.equals(COL_DETAILS)) {
            return wrappedIdentity.getDetailsListView();
        } else if (colName.equals(COL_ATTEMPTS)) {
            return wrappedIdentity.getNodeAttempts();
        } else if (colName.equals(COL_SCORE)) {
            ScoreEvaluation scoreEval = wrappedIdentity.getUserCourseEnvironment().getScoreAccounting().evalCourseNode(courseNode);
            if (scoreEval == null) {
                scoreEval = new ScoreEvaluation(null, null);
            }
            return AssessmentHelper.getRoundedScore(scoreEval.getScore());
        } else if (colName.equals(COL_STATUS)) {
            return getStatusFor(courseNode, wrappedIdentity);
        } else if (colName.equals(COL_PASSED)) {
            ScoreEvaluation scoreEval = wrappedIdentity.getUserCourseEnvironment().getScoreAccounting().evalCourseNode(courseNode);
            if (scoreEval == null) {
                scoreEval = new ScoreEvaluation(null, null);
            }
            return scoreEval.getPassed();
        } else {
            return "error";
        }
    }

    /**
     * Return task Status (not_ok,ok,working_on) for a certain user and course
     * 
     * @param courseNode
     * @param wrappedIdentity
     * @return
     */
    private String getStatusFor(final AssessableCourseNode courseNode, final AssessedIdentityWrapper wrappedIdentity) {

        final CoursePropertyManager cpm = wrappedIdentity.getUserCourseEnvironment().getCourseEnvironment().getCoursePropertyManager();
        PropertyImpl statusProperty;
        final Translator trans = PackageUtil.createPackageTranslator(StatusForm.class, getLocale());
        statusProperty = cpm.findCourseNodeProperty(courseNode, wrappedIdentity.getIdentity(), null, StatusManager.PROPERTY_KEY_STATUS);
        if (statusProperty == null) {
            final String value = trans.translate(StatusForm.PROPERTY_KEY_UNDEFINED);
            return value;
        } else {
            final String value = trans.translate(StatusForm.STATUS_LOCALE_PROPERTY_PREFIX + statusProperty.getStringValue());
            return value;
        }
    }

    /**
     * Adds all ColumnDescriptors to the userListCtr.
     * 
     * @param userListCtr
     * @param actionCommand
     * @param isNodeOrGroupFocus
     */
    public void addColumnDescriptors(final TableController userListCtr, final String actionCommand, final boolean isNodeOrGroupFocus) {
        String editCmd = null;
        if (courseNode == null || courseNode.isEditableConfigured()) {
            editCmd = actionCommand; // only selectable if editable
        }
        int colCount = 0;
        userListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.name", colCount++, editCmd, getLocale()));

        for (int i = 0; i < userPropertyHandlers.size(); i++) {
            final UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(i);
            userListCtr.addColumnDescriptor(userPropertyHandler.getColumnDescriptor(i + 1, null, getLocale()));
            colCount++;
        }
        if ((courseNode != null) && isNodeOrGroupFocus) {
            if (courseNode.hasDetails()) {
                userListCtr.addColumnDescriptor((courseNode.getDetailsListViewHeaderKey() == null ? false : true),
                        new DefaultColumnDescriptor(courseNode.getDetailsListViewHeaderKey(), colCount++, null, getLocale()));
            }
            if (courseNode.hasAttemptsConfigured()) {
                userListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.attempts", colCount++, null, getLocale(), ColumnDescriptor.ALIGNMENT_LEFT));
            }
            if (courseNode.hasScoreConfigured()) {
                userListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.score", colCount++, null, getLocale(), ColumnDescriptor.ALIGNMENT_LEFT));
            }
            if (courseNode.hasStatusConfigured()) {
                userListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.status", colCount++, null, getLocale(), ColumnDescriptor.ALIGNMENT_LEFT));
            }
            if (courseNode.hasPassedConfigured()) {
                userListCtr.addColumnDescriptor(new BooleanColumnDescriptor("table.header.passed", colCount++, translator.translate("passed.true"), translator
                        .translate("passed.false")));
            }
        }
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
