/**
 * 
 */
package org.olat.presentation.framework.core.control.generic.wizard;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;

/**
 * @author patrickb
 */
public abstract class StepFormBasicController extends FormBasicController implements StepFormController {

    private StepsRunContext runContext;
    private boolean usedInStepWizzard = true;

    /**
     * @param ureq
     * @param wControl
     * @param rootForm
     * @param runContext
     * @param layout
     *            The layout used as form layouter container. Use the public static variables of this class LAYOUT_DEFAULT, LAYOUT_HORIZONTAL and LAYOUT_VERTICAL
     * @param customLayoutPageName
     *            The page name if layout is set to LAYOUT_CUSTOM
     */
    public StepFormBasicController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext, int layout, String customLayoutPageName) {
        super(ureq, wControl, layout, customLayoutPageName, rootForm);
        this.runContext = runContext;
    }

    /**
     * @param ureq
     * @param wControl
     * @param pageName
     */
    public StepFormBasicController(UserRequest ureq, WindowControl wControl, String pageName) {
        super(ureq, wControl, pageName);
        usedInStepWizzard = false;
        runContext = null;
    }

    /**
     * @param ureq
     * @param wControl
     * @param pageName
     */
    public StepFormBasicController(UserRequest ureq, WindowControl wControl, int layout) {
        super(ureq, wControl, layout);
        usedInStepWizzard = false;
        runContext = null;
    }

    /**
     * @param ureq
     * @param wControl
     */
    public StepFormBasicController(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);
        usedInStepWizzard = false;
        runContext = null;
    }

    protected void addToRunContext(String key, Object value) {
        runContext.put(key, value);
    }

    protected boolean containsRunContextKey(String key) {
        return runContext.containsKey(key);
    }

    protected Object getFromRunContext(String key) {
        return runContext.get(key);
    }

    @Override
    public void back() {

    }

    @SuppressWarnings("unused")
    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        if (source == mainForm.getInitialComponent()) {
            // general form events
            if (event == Form.EVNT_VALIDATION_OK) {
                formOK(ureq);
                // set container dirty to remove potentially rendered error messages
                this.flc.setDirty(true);
            } else if (event == Form.EVNT_VALIDATION_NOK) {
                formNOK(ureq);
                // set container dirty to rendered error messages
                this.flc.setDirty(true);
            } else if (event == FormEvent.RESET) {
                formResetted(ureq);
                // set container dirty to render everything from scratch, remove error messages
                this.flc.setDirty(true);
            } else if (event instanceof FormEvent) {
                /*
                 * evaluate inner form events
                 */
                FormEvent fe = (FormEvent) event;
                FormItem fiSrc = fe.getFormItemSource();
                //
                formInnerEvent(ureq, fiSrc, fe);
                // no need to set container dirty, up to controller code if something is dirty
            }
        }
    }

    @Override
    abstract protected void doDispose();

    @Override
    abstract protected void formOK(UserRequest ureq);

    /**
     * @return Returns the usedInStepWizzard.
     */
    public boolean isUsedInStepWizzard() {
        return usedInStepWizzard;
    }

    @Override
    abstract protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq);

    /*
     * (non-Javadoc)
     */
    @Override
    public FormItem getStepFormItem() {
        return flc;
    }

}
