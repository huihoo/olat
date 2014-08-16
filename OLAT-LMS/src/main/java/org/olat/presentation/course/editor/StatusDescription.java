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

package org.olat.presentation.course.editor;

import java.util.Locale;
import java.util.logging.Level;

import org.olat.lms.commons.validation.ValidationAction;
import org.olat.lms.commons.validation.ValidationStatus;
import org.olat.presentation.course.condition.ConditionEditController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;

/**
 * Initial Date: Jun 20, 2005 <br>
 * 
 * @author patrick
 */
public class StatusDescription implements ValidationStatus {
    public final static StatusDescription NOERROR = new StatusDescription();

    private static String fallbackTranslatorStr = PackageUtil.getPackageName(ConditionEditController.class);

    private final Level theLevel;
    private final String shortDesc;
    private final String longDesc;
    private String transPckg;
    private String[] params;
    private String isForUnit;

    private String resolveIssueViewIdentifier = null;

    private StatusDescription() {
        theLevel = Level.OFF;
        shortDesc = null;
        longDesc = null;
    }

    public StatusDescription(final Level severity, final String shortDescKey, final String longDescKey, final String[] descParams, final String translatorPackage) {
        theLevel = severity;
        shortDesc = shortDescKey;
        longDesc = longDescKey;
        transPckg = translatorPackage;
        params = descParams;
    }

    /**
     * @return getLevel() == ERROR
     */
    @Override
    public boolean isError() {
        return theLevel.equals(ERROR);
    }

    /**
     * @return getLevel() == WARNING
     */
    @Override
    public boolean isWarning() {
        return theLevel.equals(WARNING);
    }

    /**
     * @return getLevel() == INFO
     */
    @Override
    public boolean isInfo() {
        return theLevel.equals(INFO);
    }

    /**
     * the status level corresponds to the definitions in the java util logging spec.
     * 
     * @return
     */
    @Override
    public Level getLevel() {
        return theLevel;
    }

    /**
     * localized short description of the status providing a summary (line).
     * 
     * @param locale
     * @return
     */
    public String getShortDescription(final Locale locale) {
        final Translator f = new PackageTranslator(fallbackTranslatorStr, locale);
        final Translator t = new PackageTranslator(transPckg, locale, f);
        return t.translate(shortDesc, params);
    }

    public String getShortDescriptionKey() {
        return shortDesc;
    }

    /**
     * localized long description of the status containing details, references etc.
     * 
     * @param locale
     * @return
     */
    public String getLongDescription(final Locale locale) {
        final Translator f = new PackageTranslator(fallbackTranslatorStr, locale);
        final Translator t = new PackageTranslator(transPckg, locale, f);
        return t.translate(longDesc, params);
    }

    public String getLongDescriptionKey() {
        return longDesc;
    }

    /**
     * set the unit identifier for which the status description is. I.e. a course node id
     * 
     * @param name
     */
    public void setDescriptionForUnit(final String name) {
        this.isForUnit = name;
    }

    /**
     * @return the unit identifier for which the status description is.
     */
    public String getDescriptionForUnit() {
        return isForUnit;
    }

    /**
     * TODO: future use to enable a button/link starting a issue helper to resolve problems, or the like.
     * 
     * @param ureq
     * @param wControl
     * @return
     */
    public Controller getHelperWizard(final UserRequest ureq, final WindowControl wControl) {
        return null;
    }

    /**
     * It is not always needed to create a complete helper wizard but sufficient to just activate an exisiting component. I.e. a tab in in tabbed pane as it is the case
     * in the course editor.
     * 
     * @return view identifier for calling activate of an activateable
     */
    public String getActivateableViewIdentifier() {
        return resolveIssueViewIdentifier;
    }

    public void setActivateableViewIdentifier(final String viewIdent) {
        resolveIssueViewIdentifier = viewIdent;
    }

    public String[] getDescriptionParams() {
        return params;
    }

    /**
     * status description may change their meaning. I.e. the same error/warning in the course editor means something different during publish.
     * 
     * @param longDescKey
     * @param shortDescKey
     * @param paramsNew
     * @return
     */
    public StatusDescription transformTo(final String longDescKey, final String shortDescKey, final String[] paramsNew) {
        final String[] theParams = paramsNew != null ? paramsNew : params;
        final StatusDescription retVal = new StatusDescription(theLevel, shortDescKey, longDescKey, theParams, transPckg);
        retVal.isForUnit = this.isForUnit;
        retVal.resolveIssueViewIdentifier = this.resolveIssueViewIdentifier;
        return retVal;
    }

    @Override
    public ValidationAction getAction() {
        return null;
    }

}
