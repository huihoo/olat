package org.olat.presentation.framework.core.components.date;

import java.util.List;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormItemImpl;

/**
 * Description:<br>
 * Wrapper for the DateComponent to use in flexi form layout.
 * <P>
 * Initial Date: 27 jul. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class DateElement extends FormItemImpl {

    private final DateComponent dateComponent;

    public DateElement(String name, DateComponent dateComponent) {
        super(name);
        this.dateComponent = dateComponent;
    }

    @Override
    protected Component getFormItemComponent() {
        return dateComponent;
    }

    @Override
    protected void rootFormAvailable() {
        //
    }

    @Override
    public void evalFormRequest(UserRequest ureq) {
        //
    }

    @Override
    public void validate(List validationResults) {
        //
    }

    @Override
    public void reset() {
        //
    }
}
