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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.presentation.framework.core.formelements;

import org.olat.presentation.framework.core.translator.Translator;

/**
 * enclosing_type Description: <br>
 * 
 * @author Felix Jost
 */
public abstract class AbstractFormElement implements FormElement {
    private String name;
    private String error;
    private String errorKey;
    private String[] errorKeyParams;
    private String labelKey;
    private String example;
    private boolean readOnly;
    private boolean mandatory;
    private boolean visualMarkIsOn;

    /**
	 */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
	 */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Set a translated error message.
     * 
     * @param error
     */
    @Override
    public void setError(String error) {
        clearError();
        this.error = error;
    }

    /**
     * @return a translated error message.
     */
    @Override
    public String getError(Translator translator) {
        if (error != null) {
            return error;
        } else {
            return translator.translate(errorKey, errorKeyParams);
        }
    }

    /**
     * Set an error translation key.
     * 
     * @param errorKey
     */
    @Override
    public void setErrorKey(String errorKey) {
        clearError();
        this.errorKey = errorKey;
    }

    /**
     * Error translation key with parameters to be set in the translation.
     * 
     * @param errorKey
     * @param params
     */
    @Override
    public void setErrorKeyWithParams(String errorKey, String[] params) {
        clearError();
        this.errorKey = errorKey;
        this.errorKeyParams = params;
    }

    /**
     * @return true if an error or error key was set.
     */
    @Override
    public boolean isError() {
        return errorKey != null || error != null;
    }

    /**
     * Clear the error message. After that <code>isError()</code> returns <code>false</code>.
     * 
     */
    @Override
    public void clearError() {
        error = null;
        errorKey = null;
        errorKeyParams = null;
    }

    /**
     * @return String
     */
    @Override
    public String getLabelKey() {
        return labelKey;
    }

    /**
     * Sets the labelKey.
     * 
     * @param labelKey
     *            The labelKey to set
     */
    @Override
    public void setLabelKey(String labelKey) {
        this.labelKey = labelKey;
    }

    /**
     * Returns the readOnly.
     * 
     * @return boolean
     */
    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Sets the readOnly.
     * 
     * @param readOnly
     *            The readOnly to set
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Returns the mandatory.
     * 
     * @return boolean
     */
    @Override
    public boolean isMandatory() {
        return mandatory;
    }

    /**
     * Sets the mandatory.
     * 
     * @param mandatory
     *            The mandatory to set
     */
    @Override
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    /**
     * Returns an optional element example
     * 
     * @return The example
     */
    @Override
    public String getExample() {
        return example;
    }

    /**
     * Sets the optional element example
     * 
     * @param string
     *            The example
     */
    public void setExample(String string) {
        example = string;
    }

    /**
	 */
    @Override
    public boolean getVisualMarked() {
        return visualMarkIsOn;
    }

    /**
	 */
    @Override
    public void setVisualMarked(boolean on) {
        this.visualMarkIsOn = on;
    }
}
