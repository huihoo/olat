package org.olat.presentation.examples.guidemo;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.floatingresizabledialog.FloatingResizableDialogController;
import org.olat.presentation.framework.core.dev.controller.SourceViewController;
import org.olat.presentation.user.administration.UserSearchController;
import org.olat.system.event.Event;

public class GuiDemoFloatingPanelController extends BasicController {

    private final VelocityContainer panelVc = createVelocityContainer("panel");
    private final VelocityContainer openerVc = createVelocityContainer("opener");
    private final VelocityContainer localContent = createVelocityContainer("localContent");
    private final Panel panel = new Panel("panel");
    private final Link open;
    private final Link open2;
    private Link contentLink;
    private FloatingResizableDialogController dialog;

    public GuiDemoFloatingPanelController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);

        panel.setContent(openerVc);
        open = LinkFactory.createLink("open", openerVc, this);
        open2 = LinkFactory.createLink("open2", openerVc, this);

        // add source view control
        final Controller sourceview = new SourceViewController(ureq, wControl, this.getClass(), openerVc);
        openerVc.put("sourceview", sourceview.getInitialComponent());

        putInitialPanel(panel);
    }

    @Override
    protected void doDispose() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == open) {
            final UserSearchController userSearch = new UserSearchController(ureq, getWindowControl(), true);
            dialog = new FloatingResizableDialogController(ureq, getWindowControl(), userSearch.getInitialComponent(), "Your title", 350, 350, 400, 200, null, "", true,
                    false);
            dialog.addControllerListener(this);
            panelVc.put("panel", dialog.getInitialComponent());
            panel.setContent(panelVc);
        } else if (source == open2) {
            dialog = new FloatingResizableDialogController(ureq, getWindowControl(), localContent, "Your title", 350, 350, 400, 200,
                    createVelocityContainer("localContent2"), "", true, false);
            dialog.addControllerListener(this);
            panelVc.put("panel", dialog.getInitialComponent());
            contentLink = LinkFactory.createLink("link4", localContent, this);
            panel.setContent(panelVc);
        } else if (source == contentLink) {
            getWindowControl().setInfo("Congratulations! You won a trip to Lorem Ipsum.");
        }

    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == dialog) {
            if (event == Event.DONE_EVENT) {
                panel.setContent(openerVc);
            }
        }
    }

}
