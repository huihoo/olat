package org.olat.presentation.user.administration.importwizzard;

import java.util.ArrayList;
import java.util.List;

import org.olat.lms.user.UserService;
import org.olat.lms.user.propertyhandler.UserPropertyHandler;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.FlexiTableElment;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.table.CSSIconFlexiCellRenderer;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.table.FlexiTableDataModel;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.wizard.BasicStep;
import org.olat.presentation.framework.core.control.generic.wizard.PrevNextFinishConfig;
import org.olat.presentation.framework.core.control.generic.wizard.Step;
import org.olat.presentation.framework.core.control.generic.wizard.StepFormBasicController;
import org.olat.presentation.framework.core.control.generic.wizard.StepFormController;
import org.olat.presentation.framework.core.control.generic.wizard.StepsEvent;
import org.olat.presentation.framework.core.control.generic.wizard.StepsRunContext;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.spring.CoreSpringFactory;

class ImportStep01 extends BasicStep {

    boolean canCreateOLATPassword;
    boolean newUsers;
    static final String usageIdentifyer = UserImportController.class.getCanonicalName();

    public ImportStep01(final UserRequest ureq, final boolean canCreateOLATPassword, final boolean newUsers) {
        super(ureq);
        this.canCreateOLATPassword = canCreateOLATPassword;
        this.newUsers = newUsers;
        setI18nTitleAndDescr("step1.description", "step1.short.description");
        setNextStep(Step.NOSTEP);
    }

    @Override
    public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
        if (newUsers) {
            return new PrevNextFinishConfig(true, false, true);
        } else {
            return new PrevNextFinishConfig(true, false, false);
        }
    }

    @Override
    public StepFormController getStepController(final UserRequest ureq, final WindowControl windowControl, final StepsRunContext stepsRunContext, final Form form) {
        final StepFormController stepI = new ImportStepForm01(ureq, windowControl, form, stepsRunContext);
        return stepI;
    }

    private final class ImportStepForm01 extends StepFormBasicController {
        private ArrayList<List<String>> newIdents;
        private List<Object> idents;
        private FormLayoutContainer textContainer;
        private List<UserPropertyHandler> userPropertyHandlers;

        public ImportStepForm01(final UserRequest ureq, final WindowControl control, final Form rootForm, final StepsRunContext runContext) {
            super(ureq, control, rootForm, runContext, LAYOUT_VERTICAL, null);
            // use custom translator with fallback to user properties translator
            setTranslator(getUserService().getUserPropertiesConfig().getTranslator(getTranslator()));
            flc.setTranslator(getTranslator());
            initForm(ureq);
        }

        @Override
        protected void doDispose() {
            // TODO Auto-generated method stub
        }

        @Override
        protected void formOK(final UserRequest ureq) {
            fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
        }

        @SuppressWarnings({ "unused", "unchecked" })
        @Override
        protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
            final FormLayoutContainer formLayoutVertical = FormLayoutContainer.createVerticalFormLayout("vertical", getTranslator());
            formLayout.add(formLayoutVertical);

            idents = (List<Object>) getFromRunContext("idents");
            newIdents = (ArrayList<List<String>>) getFromRunContext("newIdents");
            textContainer = FormLayoutContainer.createCustomFormLayout("step1", getTranslator(), this.velocity_root + "/step1.html");
            formLayoutVertical.add(textContainer);

            final int cntall = idents.size();
            final int cntNew = newIdents.size();
            final int cntOld = cntall - cntNew;
            textContainer.contextPut("newusers", newUsers);
            final String overview = getTranslator().translate("import.confirm", new String[] { "" + cntall, "" + cntNew, "" + cntOld });
            textContainer.contextPut("overview", overview);

            final FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
            int colPos = 0;
            // add special column with information about whether this user
            // exists already or not
            final FlexiColumnModel newUserCustomColumnModel = new DefaultFlexiColumnModel("table.user.existing");
            newUserCustomColumnModel.setCellRenderer(new UserNewOldCustomFlexiCellRenderer());
            newUserCustomColumnModel.setAlignment(FlexiColumnModel.ALIGNMENT_CENTER);
            tableColumnModel.addFlexiColumnModel(newUserCustomColumnModel);
            colPos++;

            // fixed fields:
            tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.user.login"));
            colPos++;
            if (canCreateOLATPassword) {
                tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.user.pwd"));
            }
            colPos++;
            tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.user.lang"));
            colPos++;
            // followed by all properties configured
            // if only mandatory required: check for um.isMandatoryUserProperty(usageIdentifyer, userPropertyHandler);
            userPropertyHandlers = getUserService().getUserPropertyHandlersFor(usageIdentifyer, true);
            for (int i = 0; i < userPropertyHandlers.size(); i++) {
                final UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(i);
                tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(userPropertyHandler.i18nColumnDescriptorLabelKey()));
                colPos++;
            }

            final FlexiTableDataModel tableDataModel = FlexiTableDataModelFactory.createFlexiTableDataModel(new ImportTableModel(idents, colPos), tableColumnModel);
            final FlexiTableElment fte = uifactory.addTableElement("newUsers", tableDataModel, formLayoutVertical);

        }

    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}

/**
 * Description:<br>
 * Special cell renderer that uses a css class icon to display the new user type
 * <P>
 * Initial Date: 21.03.2008 <br>
 * 
 * @author gnaegi
 */
class UserNewOldCustomFlexiCellRenderer extends CSSIconFlexiCellRenderer {

    @Override
    @SuppressWarnings("unused")
    protected String getCellValue(final Object cellValue) {
        return "";
    }

    @Override
    protected String getCssClass(final Object cellValue) {
        if (cellValue instanceof Boolean) {
            if (((Boolean) cellValue).booleanValue()) {
                return "b_new_icon";
            } else {
                return "b_warn_icon";
            }
        }
        return "b_error_icon";
    }

    @Override
    protected String getHoverText(final Object cellValue, final Translator translator) {
        if (cellValue instanceof Boolean) {
            if (((Boolean) cellValue).booleanValue()) {
                return translator.translate("import.user.new.alt");
            } else {
                return translator.translate("import.user.existing.alt");
            }
        }
        return translator.translate("error");
    }

}
