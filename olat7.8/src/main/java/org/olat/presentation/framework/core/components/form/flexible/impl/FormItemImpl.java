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
package org.olat.presentation.framework.core.components.form.flexible.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.lms.commons.validation.ValidationStatus;
import org.olat.presentation.framework.core.GUIInterna;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.form.flexible.FormBaseComponentIdProvider;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.FormLayouter;
import org.olat.presentation.framework.core.components.form.flexible.elements.InlineElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.components.SimpleExampleText;
import org.olat.presentation.framework.core.components.form.flexible.impl.components.SimpleFormErrorText;
import org.olat.presentation.framework.core.components.form.flexible.impl.components.SimpleLabelText;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.Settings;
import org.olat.system.exception.AssertException;

/**
 * <h2>Description:</h2>
 * <P>
 * Initial Date: 22.11.2006 <br>
 * 
 * @author patrickb
 */
public abstract class FormItemImpl implements FormItem, InlineElement {
    private static final String PREFIX = "PANEL_";
    private Map<String, List<String>> actionListeners = new HashMap<String, List<String>>(5);
    private boolean componentIsMandatory;
    private String errorKey;
    private String[] errorParams;
    private Component errorComponent;
    private Panel errorPanel;
    private String[] exampleParams;
    private String exampleKey;
    private Component exampleC;
    private Panel examplePanel;
    private String[] labelParams;
    private String labelKey;
    private Component labelC;
    private Panel labelPanel;
    protected Translator translator;
    private String name;
    private boolean hasLabel = false;
    private boolean hasExample = false;
    protected boolean hasError = false;
    private Form rootForm = null;
    protected int action;
    private Object userObject;
    private boolean hasFocus = false;
    private boolean formItemIsEnabled = true;
    private boolean isInlineEditingElement;
    private boolean isInlineEditingOn;
    private Component inlineEditingComponent;
    private String i18nKey4EmptyText = "inline.empty.click.for.edit";

    /**
     * @param name
     */
    public FormItemImpl(String name) {
        this(name, false);// default is not inline
    }

    public FormItemImpl(String name, boolean asInlineEditingElement) {
        this.name = name;
        this.isInlineEditingElement = asInlineEditingElement;
        /*
         * prepare three panels as placeholder for label, example, error
         */
        errorPanel = new Panel(PREFIX + name + FormItem.ERRORC);
        examplePanel = new Panel(PREFIX + name + FormItem.EXAMPLEC);
        labelPanel = new Panel(PREFIX + name + FormItem.LABELC);

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isInlineEditingOn() {
        if (!isInlineEditingElement)
            throw new AssertException("isInlineEditingOn called although it is not a inlineEditingElement");
        return isInlineEditingOn;
    }

    @Override
    public FormItem setEmptyDisplayText(String i18nKey4Text) {
        this.i18nKey4EmptyText = i18nKey4Text;
        return this;
    }

    @Override
    public String getEmptyDisplayText() {
        if (getTranslator() == null)
            throw new AssertException("getEmptyDisplayText called to early, no translator available");
        return translate(i18nKey4EmptyText, null);
    }

    protected void isInlineEditingOn(boolean isOn) {
        if (!isInlineEditingElement)
            throw new AssertException("isInlineEditingOn(..) called although it is not a inlineEditingElement");
        isInlineEditingOn = isOn;
    }

    protected Component getInlineEditingComponent() {
        if (!isInlineEditingElement)
            throw new AssertException("getInlineEditingComponent called although it is not a inlineEditingElement");
        return inlineEditingComponent;
    }

    protected void setInlineEditingComponent(Component inlineEditingComponent) {
        if (!isInlineEditingElement)
            throw new AssertException("getInlineEditingComponent called although it is not a inlineEditingElement");
        this.inlineEditingComponent = inlineEditingComponent;
    }

    protected boolean isInlineEditingElement() {
        return isInlineEditingElement;
    }

    /**
	 */
    @Override
    public Component getComponent() {
        //
        return isInlineEditingElement ? getInlineEditingComponent() : getFormItemComponent();
    }

    protected abstract Component getFormItemComponent();

    /**
	 */
    @Override
    public Form getRootForm() {
        return rootForm;
    }

    @Override
    public void setRootForm(Form rootForm) {
        this.rootForm = rootForm;
        rootFormAvailable();
    }

    protected abstract void rootFormAvailable();

    protected boolean translateLabel() {
        return true;
    }

    @Override
    public void setTranslator(Translator translator) {
        this.translator = translator;
        // (re)translate label, error, example
        // typically setTranslator is called form parent container if the FormItem
        // is added.
        String labelTrsl = translateLabel() ? translate(labelKey, labelParams) : labelKey;
        if (Settings.isDebuging()) {
            // in develmode, check that either translation for labelkey is available
            // or that the other method to add the element is used.
            // other in the sense of using the LabelI18nKey set to null, this
            // avoids false messages in the logfile concering missng translations.
            if (labelTrsl == null && hasLabel()) {
                throw new AssertException("Your label " + labelKey + " for formitem " + getName()
                        + " is not available, please use the addXXX method with labelI18nKey and set it to null.");
            }
        }
        labelC = new SimpleLabelText(labelKey, labelTrsl);
        errorComponent = new SimpleFormErrorText(errorKey, translate(errorKey, errorParams));
        exampleC = new SimpleExampleText(exampleKey, translate(exampleKey, exampleParams));
        labelPanel.setContent(labelC);
        errorPanel.setContent(errorComponent);
        examplePanel.setContent(exampleC);
    }

    @Override
    public Translator getTranslator() {
        return translator;
    }

    @Override
    public Component getLabelC() {
        return labelPanel;
    }

    @Override
    public String getLabelText() {
        return translate(labelKey, labelParams);
    }

    @Override
    public void setLabel(String label, String[] params) {
        if (label == null) {
            hasLabel = false;
        }
        hasLabel = true;
        labelKey = label;
        labelParams = params;
        // set label may be called before the translator is available
        if (getTranslator() != null) {
            labelC = new SimpleLabelText(label, translate(label, params));
            labelPanel.setContent(labelC);
        }
    }

    /**
     * @param labelComponent
     * @param container
     * @return this
     */
    @Override
    public FormItem setLabelComponent(FormItem labelComponent, FormItemContainer container) {
        if (labelComponent == null) {
            throw new AssertException("do not clear error by setting null, instead use showLabel(false).");
        }

        this.hasLabel = true;
        // initialize root form of form item
        FormLayoutContainer flc = (FormLayoutContainer) container;// TODO:pb: fix this hierarchy mismatch
        flc.register(labelComponent);// errorFormItem must be part of the composite chain, that it gets dispatched

        labelC = labelComponent.getComponent();
        labelPanel.setContent(labelC);

        return this;
    }

    @Override
    public void setFocus(boolean hasFocus) {
        this.hasFocus = hasFocus;
    }

    @Override
    public boolean isMandatory() {
        return componentIsMandatory;
    }

    @Override
    public boolean hasFocus() {
        return hasFocus;
    }

    @Override
    public void setMandatory(boolean isMandatory) {
        componentIsMandatory = isMandatory;
    }

    /**
	 */
    @Override
    public Component getExampleC() {
        return examplePanel;
    }

    /**
	 */
    @Override
    public String getExampleText() {
        return translate(exampleKey, exampleParams);
    }

    /**
	 */
    @Override
    public void setExampleKey(String exampleKey, String[] params) {
        hasExample = true;
        this.exampleKey = exampleKey;
        this.exampleParams = params;
        if (getTranslator() != null) {
            exampleC = new SimpleExampleText(exampleKey, translate(exampleKey, params));
            examplePanel.setContent(exampleC);
        }
    }

    /**
	 */
    @Override
    public void setErrorKey(String errorKey, String... params) {
        this.hasError = true;
        this.errorKey = errorKey;
        this.errorParams = params;
        if (getTranslator() != null) {
            errorComponent = new SimpleFormErrorText(errorKey, translate(errorKey, errorParams));
            errorPanel.setContent(errorComponent);
        }
        this.showError(hasError);
        this.getRootForm().getInitialComponent().setDirty(true);
    }

    /**
     * convenience
     * 
     * @param key
     * @param params
     * @return
     */
    private String translate(String key, String[] params) {
        String retVal = null;
        if (key != null && params != null) {
            retVal = translator.translate(key, params);
        } else if (key != null) {
            retVal = translator.translate(key);
        }
        return retVal;
    }

    /**
	 */
    @Override
    public void setErrorComponent(FormItem errorFormItem, FormLayouter container) {
        if (errorFormItem == null) {
            throw new AssertException("do not clear error by setting null, instead use showError(false).");
        }
        // initialize root form of form item
        FormLayoutContainer flc = (FormLayoutContainer) container;// TODO:pb: fix this hierarchy mismatch
        flc.register(errorFormItem);// errorFormItem must be part of the composite chain, that it gets dispatched

        this.hasError = true;
        this.errorComponent = errorFormItem.getComponent();
        errorPanel.setContent(this.errorComponent);
    }

    /**
	 */
    @Override
    public Component getErrorC() {
        return errorPanel;
    }

    /**
	 */
    @Override
    public String getErrorText() {
        return translate(errorKey, errorParams);
    }

    @Override
    public void setEnabled(boolean isEnabled) {
        getErrorC().setEnabled(isEnabled);
        if (errorComponent != null)
            errorComponent.setEnabled(isEnabled);
        getExampleC().setEnabled(isEnabled);
        if (exampleC != null)
            exampleC.setEnabled(isEnabled);
        getLabelC().setEnabled(isEnabled);
        if (labelC != null)
            labelC.setEnabled(isEnabled);
        this.formItemIsEnabled = isEnabled;
        if (getComponent() == null)
            return;
        getComponent().setEnabled(isEnabled);
    }

    @Override
    public boolean isEnabled() {
        return formItemIsEnabled;
    }

    @Override
    public void setVisible(boolean isVisible) {
        // FIXME:pb: getComponent can be null in the case of FormLink for example
        if (getComponent() == null)
            return;
        getComponent().setVisible(isVisible);
        showError(isVisible && hasError);
        showExample(isVisible && hasExample);
        showLabel(isVisible && hasLabel);
    }

    @Override
    public boolean isVisible() {
        if (getComponent() == null)
            return false;
        return getComponent().isVisible();
    }

    @Override
    public boolean hasError() {
        return hasError;
    }

    @Override
    public boolean hasLabel() {
        return hasLabel;
    }

    @Override
    public boolean hasExample() {
        return hasExample;
    }

    @Override
    public void showLabel(boolean show) {
        if (show) {
            labelPanel.setContent(labelC);
        } else {
            labelPanel.setContent(null);
        }
        labelPanel.setVisible(show);
        labelPanel.setEnabled(show);
    }

    @Override
    public void showError(boolean show) {
        if (show) {
            errorPanel.setContent(errorComponent);
        } else {
            errorPanel.setContent(null);
        }
        errorPanel.setVisible(show);
        errorPanel.setEnabled(show);
    }

    @Override
    public void clearError() {
        showError(false);
        hasError = false;
    }

    @Override
    public void showExample(boolean show) {
        if (show) {
            examplePanel.setContent(exampleC);
        } else {
            examplePanel.setContent(null);
        }
        examplePanel.setVisible(show);
        examplePanel.setEnabled(show);
    }

    /**
	 */
    @Override
    public void addActionListener(Controller listener, int action) {
        /*
         * for simplicity only one action and listener per item (at the moment)
         */
        this.action = action;
        // for (int i = 0; i < FormEvent.ON_DOTDOTDOT.length; i++) {
        // if(action - FormEvent.ON_DOTDOTDOT[i] == 0){
        // String key = String.valueOf(FormEvent.ON_DOTDOTDOT[i]);
        // if(actionListeners.containsKey(key)){
        // List listeners = (List)actionListeners.get(key);
        // if(!listeners.contains(listener)){
        // listeners.add(listener);
        // }
        // }else{
        String key = String.valueOf(this.action);
        List<String> listeners = new ArrayList<String>(1);
        actionListeners.put(key, listeners);
        // }
        // }
        //
        // action = action - FormEvent.ON_DOTDOTDOT[i];
        // }
    }

    @Override
    public List<String> getActionListenersFor(int event) {
        return actionListeners.get(String.valueOf(event));
    }

    @Override
    public int getAction() {
        return action;
    }

    /**
     * gets called if the implementing component is part of a form which gets partly submitted -> extract data for you and store it temporarly for redisplay without a
     * validation
     * 
     * @param ureq
     */
    @Override
    public abstract void evalFormRequest(UserRequest ureq);

    /**
     * gets called if the implementing component was clicked.
     * 
     * @param ureq
     * @param formId
     */
    @Override
    public void doDispatchFormRequest(UserRequest ureq) {
        // first let implementor do its job
        dispatchFormRequest(ureq);
        if (getRootForm().hasAlreadyFired()) {
            // dispatchFormRequest did fire already
            // in this case we do not try to fire the general events
            return;
        }
        // before/ after pattern
        int action = getRootForm().getAction();
        switch (action) {
        case FormEvent.ONCLICK:
            getRootForm().fireFormEvent(ureq, new FormEvent("ONCLICK", this, FormEvent.ONCLICK));
            break;
        case FormEvent.ONDBLCLICK:
            getRootForm().fireFormEvent(ureq, new FormEvent("ONDBLCLICK", this, FormEvent.ONDBLCLICK));
            break;
        case FormEvent.ONCHANGE:
            getRootForm().fireFormEvent(ureq, new FormEvent("ONCHANGE", this, FormEvent.ONCHANGE));
            break;
        default:
            // nothing to do, default is handled
        }
    }

    /**
	 */
    @Override
    public Object getUserObject() {
        return userObject;
    }

    /**
	 */
    @Override
    public void setUserObject(Object userObject) {
        this.userObject = userObject;
    }

    /**
	 * 
	 */
    @Override
    public String getFormDispatchId() {

        Component comp = getComponent();

        if (comp instanceof FormBaseComponentIdProvider) {
            return ((FormBaseComponentIdProvider) comp).getFormDispatchId();
        } else {
            // do the same as the FormBaseComponentIdProvider would do
            if (GUIInterna.isLoadPerformanceMode()) {
                return DISPPREFIX + getRootForm().getReplayableDispatchID(comp);
            } else {
                return DISPPREFIX + comp.getDispatchID();
            }
        }
    }

    /**
     * override to implement your behaviour
     * 
     * @param ureq
     */
    protected void dispatchFormRequest(UserRequest ureq) {
        // default implementation does nothing
    }

    /**
	 */
    @Override
    public abstract void validate(List<ValidationStatus> validationResults);

    /**
	 */
    @Override
    public abstract void reset();

    @Override
    public String toString() {
        return "FoItem:" + getName() + "[ena:" + isEnabled() + ", vis:" + isVisible() + ", err:" + hasError() + ", exa:" + hasExample() + ", lab:" + hasLabel();
    }

}
