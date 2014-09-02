/**
 * 
 */
package org.olat.presentation.framework.core.control.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.presentation.framework.core.control.Disposable;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;

/**
 * @author patrick
 * 
 */
public abstract class BasicView implements Disposable {

    // visbility protected choosen
    // to allow access to ureq and wControl to subclasses for
    // i.e. controller creation in the getInitalComponent call.
    // public getter access is restricted to one access call, i.e. to allow Controllers access these during controller construction time.
    protected final UserRequest ureq;
    protected final WindowControl wControl;
    private boolean ureqAccessed = false;
    private boolean wControlAccessed = false;

    private Translator translator;
    private String velocity_root;
    private List<Controller> childControllers = new ArrayList<Controller>();

    public BasicView(UserRequest ureq, WindowControl wControl) {

        this.ureq = ureq;
        this.wControl = wControl;
        this.velocity_root = PackageUtil.getPackageVelocityRoot(this.getClass());

        // set translator with fall back translator.
        final Translator fallback = PackageUtil.createPackageTranslator(this.getClass(), ureq.getLocale());
        this.translator = PackageUtil.createPackageTranslator(this.getClass(), ureq.getLocale(), fallback);
    }

    /**
     * ureq is known in in the view, hence the locale is known here. Know-how of translation a responsability of the view? provide the translator for a view for the
     * controller/presenter and UIModel, maybe this should not be needed?
     * 
     * @return
     */
    public Translator getTranslator() {
        return translator;
    }

    protected String translate(String key) {
        return translator.translate(key);
    }

    /**
     * only once consumable. This is the seam / integration path to brasato.
     * 
     * @return
     */
    public UserRequest getUreq() {
        // usecase that inside of constructor other controllers are generated, these need the Ureq access several times
        // if(ureqAccessed){
        // throw new IllegalStateException("UserRequest was already consumed");
        // }
        ureqAccessed = true;
        return ureq;
    }

    /**
     * only once consumable, during super call to BasicController etc.
     * 
     * @return
     */
    public WindowControl getWindowControl() {
        if (wControlAccessed) {
            throw new IllegalStateException("WindowControl was already consumed");
        }
        wControlAccessed = true;
        return wControl;
    }

    protected Locale getLocale() {
        return ureq.getLocale();
    }

    public void dispose() {
        for (Controller controller : childControllers) {
            controller.dispose();
        }
    }

    /**
     * Duplicate from the BasicController
     * 
     * @param page
     * @return
     */
    protected VelocityContainer createVelocityContainer(String page) {
        return new VelocityContainer("vc_" + page, velocity_root + "/" + page + ".html", translator, null);
    }

    protected void listenTo(DefaultController listeningController, Controller observedController) {
        if (observedController == null) {
            throw new IllegalArgumentException("The observed Controller can not be null.");
        }
        if (listeningController == null) {
            throw new IllegalArgumentException("The listening Controller can not be null.");
        }
        observedController.addControllerListener(listeningController);
        childControllers.add(observedController);
    }

    abstract public Component getInitialComponent(DefaultController listeningController);

}
