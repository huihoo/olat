package org.olat.presentation.framework.core.components.form.flexible.impl.elements;

import org.olat.presentation.framework.core.components.ComponentRenderer;
import org.olat.presentation.framework.core.components.form.flexible.elements.StaticTextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBaseComponentImpl;

class StaticTextElementComponent extends FormBaseComponentImpl {

    private static final ComponentRenderer RENDERER = new StaticTextElementRenderer();
    private StaticTextElement wrapper;

    public StaticTextElementComponent(StaticTextElement element) {
        super(element.getName());
        this.wrapper = element;
    }

    public String getValue() {
        return wrapper.getValue();
    }

    @Override
    public ComponentRenderer getHTMLRendererSingleton() {
        return RENDERER;
    }

    public Form getRootForm() {
        return wrapper.getRootForm();
    }

    public int getAction() {
        return wrapper.getAction();
    }

}
