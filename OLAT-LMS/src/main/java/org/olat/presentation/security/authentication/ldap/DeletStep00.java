package org.olat.presentation.security.authentication.ldap;

import java.util.List;
import java.util.Set;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.FormLink;
import org.olat.presentation.framework.core.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.wizard.BasicStep;
import org.olat.presentation.framework.core.control.generic.wizard.PrevNextFinishConfig;
import org.olat.presentation.framework.core.control.generic.wizard.StepFormBasicController;
import org.olat.presentation.framework.core.control.generic.wizard.StepFormController;
import org.olat.presentation.framework.core.control.generic.wizard.StepsEvent;
import org.olat.presentation.framework.core.control.generic.wizard.StepsRunContext;
import org.olat.presentation.user.administration.UserShortDescriptionController;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * second step: show (TreeModel) all useres deleted out of LDAP but still existing in OLAT
 * <P>
 * Initial Date: 30.07.2008 <br>
 * 
 * @author mrohrer
 */
public class DeletStep00 extends BasicStep {

    List<Identity> identitiesToDelete;
    boolean hasIdentitesToDelete;

    public DeletStep00(final UserRequest ureq, final boolean hasIDToDelete, final List<Identity> iDToDelete) {
        super(ureq);
        setI18nTitleAndDescr("delete.step0.description", null);
        setNextStep(new DeletStep01(ureq));
        identitiesToDelete = iDToDelete;
        hasIdentitesToDelete = hasIDToDelete;
    }

    /**
	 */
    @Override
    public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
        return new PrevNextFinishConfig(true, true, true);
    }

    /**
     * org.olat.presentation.framework.control.generic.wizard.StepsRunContext, org.olat.presentation.framework.components.form.flexible.impl.Form)
     */
    @Override
    public StepFormController getStepController(final UserRequest ureq, final WindowControl windowControl, final StepsRunContext stepsRunContext, final Form form) {
        final StepFormController stepI = new DeletStepForm00(ureq, windowControl, form, stepsRunContext);
        return stepI;
    }

    private final class DeletStepForm00 extends StepFormBasicController {
        private FormLayoutContainer textContainer;
        private MultipleSelectionElement multiSelectTree;
        private IdentitySelectionTreeModel deleteIdentityTreeModel;
        private FormLink selectAllLink;
        private FormLink uncheckallLink;

        public DeletStepForm00(final UserRequest ureq, final WindowControl control, final Form rootForm, final StepsRunContext runContext) {
            super(ureq, control, rootForm, runContext, LAYOUT_VERTICAL, null);
            setTranslator(getUserService().getUserPropertiesConfig().getTranslator(getTranslator()));
            initForm(ureq);
            addToRunContext("hasIdentitiesToDelete", hasIdentitesToDelete);
            addToRunContext("identitiesToDelete", identitiesToDelete);
        }

        @Override
        protected void doDispose() {
            // TODO Auto-generated method stub

        }

        @Override
        protected void formOK(final UserRequest ureq) {
            final Set<String> selected = multiSelectTree.getSelectedKeys();
            final List<Identity> rem = deleteIdentityTreeModel.getIdentities(selected);
            hasIdentitesToDelete = (rem.size() == 0 ? false : true);
            addToRunContext("hasIdentitiesToDelete", hasIdentitesToDelete);
            addToRunContext("identitiesToDelete", rem);
            fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
        }

        @Override
        @SuppressWarnings("unused")
        protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
            if (source == selectAllLink) {
                multiSelectTree.selectAll();
                this.flc.setDirty(true);
            }
            if (source == uncheckallLink) {
                multiSelectTree.uncheckAll();
                this.flc.setDirty(true);
            }
        }

        @Override
        protected boolean validateFormLogic(@SuppressWarnings("unused") final UserRequest ureq) {
            return true;
        }

        @SuppressWarnings("unused")
        @Override
        protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
            textContainer = FormLayoutContainer.createCustomFormLayout("index", getTranslator(), this.velocity_root + "/delet_step00.html");
            formLayout.add(textContainer);
            // Create selection tree and model
            // Note: since the flexi table is not finished, we have to use the tree here as alternative
            // identitiesToDelete = (List<Identity>) getFromRunContext("identitiesToDelete");
            // use the user short description and not an own identifyer
            deleteIdentityTreeModel = new IdentitySelectionTreeModel(identitiesToDelete, UserShortDescriptionController.class.getCanonicalName(), getLocale());
            multiSelectTree = uifactory.addTreeMultiselect("seltree", null, formLayout, deleteIdentityTreeModel, deleteIdentityTreeModel);

            selectAllLink = uifactory.addFormLink("checkall", formLayout);
            selectAllLink.addActionListener(this, FormEvent.ONCLICK);
            uncheckallLink = uifactory.addFormLink("uncheckall", formLayout);
            uncheckallLink.addActionListener(this, FormEvent.ONCLICK);
        }
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
