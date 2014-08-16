/**
 * 
 */
package org.olat.presentation.framework.layout.fullWebApp;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.layout.MainLayoutController;
import org.olat.presentation.framework.core.control.generic.popup.PopupBrowserWindowController;

/**
 * @author patrickb
 */
public class BaseFullWebappPopupBrowserWindow extends BaseFullWebappController implements PopupBrowserWindowController {

    /**
     * @param ureq
     * @param ouisc_wControl
     * @param baseFullWebappControllerParts
     */
    public BaseFullWebappPopupBrowserWindow(UserRequest ureq, WindowControl wControl, BaseFullWebappControllerParts baseFullWebappControllerParts) {
        super(ureq, wControl, baseFullWebappControllerParts);
        // apply custom css if available
        if (contentCtrl != null && contentCtrl instanceof MainLayoutController) {
            MainLayoutController mainLayoutCtr = (MainLayoutController) contentCtrl;
            addCurrentCustomCSSToView(mainLayoutCtr.getCustomCSS());
        }
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public void open(UserRequest ureq) {
        ureq.getDispatchResult().setResultingWindow(getWindowControl().getWindowBackOffice().getWindow());
    }

    /**
	 */
    @Override
    public WindowControl getPopupWindowControl() {
        return getWindowControl();
    }

}
