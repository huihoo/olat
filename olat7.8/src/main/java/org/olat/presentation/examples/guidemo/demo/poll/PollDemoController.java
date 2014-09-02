package org.olat.presentation.examples.guidemo.demo.poll;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowBackOffice;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.dev.controller.SourceViewController;
import org.olat.system.event.Event;

public class PollDemoController extends BasicController {
    private final VelocityContainer mainVC;
    private final Link appearLater;

    private final Panel updatePanel;
    private final Link msgLink;

    public PollDemoController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        mainVC = createVelocityContainer("index");

        updatePanel = new Panel("updater");
        mainVC.put("updater", updatePanel);

        // TODO felix -> put in a factory?
        final JSAndCSSComponent jsc = new JSAndCSSComponent("intervall", this.getClass(), null, null, false, null, 2000);
        mainVC.put("updatecontrol", jsc);

        msgLink = LinkFactory.createButton("link.message", mainVC, this);

        // just a no operation link
        LinkFactory.createButton("link.noop", mainVC, this);

        // prepare for polling.
        // create a html fragment
        final VelocityContainer updateVc = createVelocityContainer("update");
        updateVc.contextPut("msg", "0");
        // set it into a panel
        updatePanel.setContent(updateVc);
        appearLater = LinkFactory.createButtonXSmall("appearLater", null, this);

        // hold the windowcontrol
        final WindowBackOffice wbo = getWindowControl().getWindowBackOffice();
        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 20; i++) {
                    final int j = i;
                    wbo.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            updateVc.contextPut("msg", "" + j);
                        }
                    });
                    try {
                        Thread.sleep(500);
                    } catch (final InterruptedException e) {
                        //
                    }
                }
                updatePanel.setContent(appearLater);

            }
        });
        t.setDaemon(true);
        t.start();

        // add source view control
        final Controller sourceview = new SourceViewController(ureq, wControl, this.getClass(), mainVC);
        listenTo(sourceview);

        mainVC.put("sourceview", sourceview.getInitialComponent());
        putInitialPanel(mainVC);
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == appearLater) {
            getWindowControl().setInfo("well here we go: 21 and finally, the answer is always 42");
        } else if (source == msgLink) {
            showInfo("testmsg");
        }
    }

    @Override
    protected void doDispose() {
        //
    }

}
