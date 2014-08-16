package org.olat.presentation.portal.infomessage;

import java.util.Map;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.portal.AbstractPortlet;
import org.olat.presentation.framework.core.control.generic.portal.Portlet;
import org.olat.presentation.framework.core.control.generic.portal.PortletToolController;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;

/**
 * Description:<br>
 * TODO: srosse Class Description for InfoMessagePortlet
 * <P>
 * Initial Date: 27 juil. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InfoMessagePortlet extends AbstractPortlet {

    private InfoMessagePortletRunController runCtrl;

    protected InfoMessagePortlet() {
    }

    @Override
    public String getTitle() {
        return getTranslator().translate("portlet.title");
    }

    @Override
    public String getDescription() {
        return getTranslator().translate("portlet.title");
    }

    @Override
    public Portlet createInstance(final WindowControl wControl, final UserRequest ureq, final Map portletConfig) {
        final Translator translator = PackageUtil.createPackageTranslator(InfoMessagePortlet.class, ureq.getLocale());
        final Portlet p = new InfoMessagePortlet();
        p.setName(getName());
        p.setTranslator(translator);
        return p;
    }

    @Override
    public Component getInitialRunComponent(final WindowControl wControl, final UserRequest ureq) {
        if (runCtrl != null) {
            runCtrl.dispose();
        }
        runCtrl = new InfoMessagePortletRunController(wControl, ureq, getTranslator(), getName());
        return runCtrl.getInitialComponent();
    }

    @Override
    public void dispose() {
        disposeRunComponent();
    }

    @Override
    public void disposeRunComponent() {
        if (this.runCtrl != null) {
            this.runCtrl.dispose();
            this.runCtrl = null;
        }
    }

    @Override
    public String getCssClass() {
        return "o_portlet_infomessages";
    }

    @Override
    public PortletToolController getTools(final UserRequest ureq, final WindowControl wControl) {
        if (runCtrl == null) {
            runCtrl = new InfoMessagePortletRunController(wControl, ureq, getTranslator(), getName());
        }
        return runCtrl.createSortingTool(ureq, wControl);
    }
}
