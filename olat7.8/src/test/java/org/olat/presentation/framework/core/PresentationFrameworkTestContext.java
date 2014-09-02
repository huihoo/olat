/**
 * 
 */
package org.olat.presentation.framework.core;

import org.olat.presentation.framework.core.control.WindowControl;

/**
 * @author patrick
 * 
 */
public class PresentationFrameworkTestContext {

    private UserRequest ureq;
    private WindowControl wControl;

    public PresentationFrameworkTestContext(UserRequest ureq, WindowControl wControl) {
        this.ureq = ureq;
        this.wControl = wControl;
    }

    public UserRequest getUserRequest() {
        return ureq;
    }

    public WindowControl getWindowControl() {
        return wControl;
    }

}
