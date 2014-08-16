package org.olat.presentation.framework.layout;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.chiefcontrollers.LanguageChooserController;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;

public class OlatDmzTopNavController extends BasicController {

    private final VelocityContainer topNavVC;
    private LanguageChooserController languageChooserC;

    public OlatDmzTopNavController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);

        topNavVC = createVelocityContainer("dmztopnav");

        // choosing language
        languageChooserC = new LanguageChooserController(getWindowControl(), ureq);
        // DOKU:pb:2008-01 listenTo(languageChooserC); not necessary as LanguageChooser sends a MultiUserEvent
        // which is catched by the BaseFullWebappController. This one is then
        // responsible to recreate the GUI with the new Locale
        //
        topNavVC.put("languageChooser", languageChooserC.getInitialComponent());

        putInitialPanel(topNavVC);
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        //
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // no events yet
    }

    @Override
    protected void doDispose() {
        if (languageChooserC != null) {
            languageChooserC.dispose();
            languageChooserC = null;
        }
    }

}
