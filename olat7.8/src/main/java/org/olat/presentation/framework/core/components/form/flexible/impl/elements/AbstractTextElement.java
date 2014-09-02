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
package org.olat.presentation.framework.core.components.form.flexible.impl.elements;

import java.util.List;
import java.util.Locale;

import org.olat.data.commons.filter.Filter;
import org.olat.lms.commons.validation.ValidationStatus;
import org.olat.lms.commons.validation.ValidationStatusImpl;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.form.ValidationError;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormItemImpl;

/**
 * Description:<br>
 * Abstract base class for all text elements
 * <P>
 * Initial Date: 27.11.2006 <br>
 * 
 * @author patrickb
 */
public abstract class AbstractTextElement extends FormItemImpl implements TextElement {

    public AbstractTextElement(String name) {
        this(name, false);
    }

    protected AbstractTextElement(String name, boolean asInlineEditingElement) {
        super(name, asInlineEditingElement);
        displaySize = 35;
    }

    protected String original;
    protected String value;
    private boolean checkForNotEmpty = false;
    private boolean checkForLength = false;
    private boolean checkForEquals = false;
    private boolean checkForMatchRegexp = false;
    private boolean checkForCustomItemValidator = false;
    private String notEmptyErrorKey;
    private int notLongerLength;
    protected int displaySize;
    protected int maxlength = -1; // default no maxlength restriction
    private String notLongerThanErrorKey;
    private String checkForOtherValue;
    private String otherValueErrorKey;
    private String checkRegexp;
    private String checkRegexpErrorKey;
    private ItemValidatorProvider itemValidatorProvider;
    private boolean originalInitialised = false;

    @Override
    public void validate(List<ValidationStatus> validationResults) {
        if (checkForNotEmpty && !notEmpty()) {
            validationResults.add(new ValidationStatusImpl(ValidationStatus.ERROR));
            return;
        }
        if (checkForLength && !notLongerThan()) {
            validationResults.add(new ValidationStatusImpl(ValidationStatus.ERROR));
            return;
        }
        if (checkForEquals && !checkForIsEqual()) {
            validationResults.add(new ValidationStatusImpl(ValidationStatus.ERROR));
            return;
        }
        if (checkForMatchRegexp && !checkRegexMatch()) {
            validationResults.add(new ValidationStatusImpl(ValidationStatus.ERROR));
            return;
        }
        if (checkForCustomItemValidator && !checkItemValidatorIsValid()) {
            validationResults.add(new ValidationStatusImpl(ValidationStatus.ERROR));
            return;
        }
        // else no error
        clearError();
    }

    @Override
    public void reset() {
        // reset to original value and clear error msg's
        setValue(original);
        clearError();
    }

    @Override
    protected void rootFormAvailable() {
        //
    }

    /**
	 */
    @Override
    public String getValue() {
        return value;
    }

    /**
	 */
    @Override
    public String getValue(Filter filter) {
        return filter.filter(value);
    }

    /**
     * Sets the value. if null is given, an empty string is assumed.
     * 
     * @param value
     *            The value to set
     */
    @Override
    public void setValue(String value) {
        if (value == null)
            value = "";
        else {
            value = value.trim();
            // Remember original value for dirty evaluation.
            // null value is not regarded as initial value. only
            // real values are used inital values
            if (!originalInitialised) {
                original = new String(value);
                originalInitialised = true;
            }
        }
        this.value = value;
        Component c = getComponent();
        if (c != null) {
            // c may be null since it is only created when this formelement is added to a FormItemContainer
            c.setDirty(true);
        }
    }

    /**
     * Set a new value as the original value that is used when resetting the form. This can be used when a form is saved and in a later form should be resetted to the
     * intermediate save state.
     * <p>
     * Does not change the value of the element, just the reset-value!
     * 
     * @param value
     *            The new original value
     */
    @Override
    public void setNewOriginalValue(String value) {
        if (value == null)
            value = "";
        original = new String(value);
        originalInitialised = true;
        String theValue = getValue();
        if (theValue != null && !theValue.equals(value)) {
            getComponent().setDirty(true);
        }
    }

    /**
	 */
    @Override
    public void setDisplaySize(int displaySize) {
        this.displaySize = displaySize;
    }

    /**
	 */
    @Override
    public void setMaxLength(int maxLength) {
        this.maxlength = maxLength;
    }

    /**
     * @param errorKey
     * @return
     */
    @Override
    public void setNotEmptyCheck(String errorKey) {
        checkForNotEmpty = true;
        notEmptyErrorKey = errorKey;
    }

    private boolean notEmpty() {
        if (value == null || value.equals("")) {
            setErrorKey(notEmptyErrorKey, null);
            return false;
        } else {
            clearError();
            return true;
        }
    }

    /**
     * @param maxLength
     *            if value is -1 maxlength will not be checked
     * @param errorKey
     * @return
     */
    @Override
    public void setNotLongerThanCheck(int maxLength, String errorKey) {
        if (maxLength == -1) {
            checkForLength = false;
            return;
        }
        checkForLength = true;
        notLongerThanErrorKey = errorKey;
        notLongerLength = maxLength;
    }

    private boolean notLongerThan() {
        clearError();
        if (value.length() > notLongerLength) {
            setErrorKey(notLongerThanErrorKey, new String[] { notLongerLength + "" });
            return false;
        }

        return true;
    }

    /**
     * compares a text value with another value
     * 
     * @param otherValue
     * @param errorKey
     * @return true if they are equal
     */
    @Override
    public void setIsEqualCheck(String otherValue, String errorKey) {
        checkForEquals = true;
        checkForOtherValue = otherValue;
        otherValueErrorKey = errorKey;
    }

    private boolean checkForIsEqual() {
        if (value == null || !value.equals(checkForOtherValue)) {
            setErrorKey(otherValueErrorKey, null);
            return false;
        } else {
            return true;
        }
    }

    /**
     * Check if the text element is empty
     * 
     * @return boolean true if is empty, false otherwhise
     */
    @Override
    public boolean isEmpty() {
        return value.equals("");
    }

    /**
     * Check if the text element is empty
     * 
     * @param errorKey
     * @return boolean true if is empty, false otherwise
     */
    @Override
    public boolean isEmpty(String errorKey) {
        if (isEmpty()) {
            setErrorKey(errorKey, null);
            return true;
        }
        return false;
    }

    /**
     * @param regExp
     * @param errorKey
     * @return
     */
    @Override
    public void setRegexMatchCheck(String regExp, String errorKey) {
        checkForMatchRegexp = true;
        checkRegexp = regExp;
        checkRegexpErrorKey = errorKey;
    }

    private boolean checkRegexMatch() {
        if (value == null || !value.matches(checkRegexp)) {
            setErrorKey(checkRegexpErrorKey, null);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void setItemValidatorProvider(ItemValidatorProvider itemValidatorProvider) {
        checkForCustomItemValidator = true;
        this.itemValidatorProvider = itemValidatorProvider;
    }

    private boolean checkItemValidatorIsValid() {
        Locale locale = getTranslator().getLocale();
        ValidationError validationErrorCallback = new ValidationError();
        boolean isValid = itemValidatorProvider.isValidValue(value, validationErrorCallback, locale);
        if (isValid)
            return true;
        else {
            setErrorKey(validationErrorCallback.getErrorKey(), null);
            return false;
        }
    }

}
