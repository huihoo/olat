package org.olat.presentation.framework.core.components.form.flexible.impl.elements;

import org.olat.presentation.framework.core.components.ComponentRenderer;
import org.olat.presentation.framework.core.components.form.flexible.elements.StaticTextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBaseComponentImpl;

class StaticTextElementComponent extends FormBaseComponentImpl {

    private static final ComponentRenderer RENDERER = new StaticTextElementRenderer();
    private StaticTextElement wrapper;
    private boolean isTrustedText = false; // per default is NOT HTML escaped

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

    /**
     * This information is typically used by the Renderer.
     */
    public boolean isTrustedText() {
        return isTrustedText;
    }

    /**
     * Called if the value is already escape, this is only used for rendering.
     */
    public void setTrustedText() {
        this.isTrustedText = true;
    }
}
