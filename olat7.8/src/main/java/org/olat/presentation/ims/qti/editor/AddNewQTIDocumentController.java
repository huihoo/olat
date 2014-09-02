/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.presentation.ims.qti.editor;

/* TODO: ORID-1007 'File' */
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.fileresource.AddingResourceException;
import org.olat.lms.commons.fileresource.FileResource;
import org.olat.lms.commons.fileresource.FileResourceManager;
import org.olat.lms.commons.fileresource.SurveyFileResource;
import org.olat.lms.commons.fileresource.TestFileResource;
import org.olat.lms.ims.qti.editor.QTIEditHelperEBL;
import org.olat.lms.ims.qti.editor.QTIEditorPackageEBL;
import org.olat.lms.ims.qti.editor.localstrings.QtiEditorLocalStrings;
import org.olat.lms.ims.qti.objects.Assessment;
import org.olat.lms.ims.qti.objects.Item;
import org.olat.lms.ims.qti.objects.Section;
import org.olat.lms.ims.qti.process.AssessmentInstance;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.ControllerEventListener;
import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.ims.qti.QTIResultDetailsController;
import org.olat.presentation.ims.qti.editor.localstrings.QtiEditorLocalStringsAbstractFactory;
import org.olat.presentation.ims.qti.editor.localstrings.ScItemLocalStringsFactory;
import org.olat.presentation.ims.qti.editor.localstrings.SectionLocalStringsFactory;
import org.olat.presentation.repository.IAddController;
import org.olat.presentation.repository.RepositoryAddCallback;
import org.olat.system.commons.CodeHelper;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: 14.01.2005
 * 
 * @author Mike Stock
 */
public class AddNewQTIDocumentController extends DefaultController implements IAddController, ControllerEventListener {

    private static final Logger log = LoggerHelper.getLogger();
    private static final String PACKAGE_REPOSITORY = PackageUtil.getPackageName(IAddController.class);
    private static final String DUMMY_TITLE = "__DUMMYTITLE__";
    private final String type;
    private FileResource resource;
    private final Translator translator;
    QTIEditorPackageEBL tmpPackage;
    final QtiEditorLocalStringsAbstractFactory sectionLocalStringsFactory;
    final QtiEditorLocalStringsAbstractFactory scItemLocalStringsFactory;

    /**
     * @param type
     * @param addCallback
     * @param ureq
     * @param wControl
     */
    public AddNewQTIDocumentController(final String type, final RepositoryAddCallback addCallback, final UserRequest ureq, final WindowControl wControl) {
        super(wControl);
        this.type = type;
        this.translator = new PackageTranslator(PACKAGE_REPOSITORY, ureq.getLocale());
        sectionLocalStringsFactory = CoreSpringFactory.getBean(SectionLocalStringsFactory.class,
                PackageUtil.createPackageTranslator(QTIResultDetailsController.class, ureq.getLocale()));
        scItemLocalStringsFactory = CoreSpringFactory.getBean(ScItemLocalStringsFactory.class,
                PackageUtil.createPackageTranslator(QTIResultDetailsController.class, ureq.getLocale()));
        if (type.equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS)) {
            resource = CoreSpringFactory.getBean(TestFileResource.class);
        } else if (type.equals(AssessmentInstance.QMD_ENTRY_TYPE_SURVEY)) {
            resource = CoreSpringFactory.getBean(SurveyFileResource.class);
        }
        if (addCallback != null) {
            addCallback.setResourceable(resource);
            addCallback.setDisplayName(translator.translate(resource.getResourceableTypeName()));
            addCallback.setResourceName("-");
            addCallback.finished(ureq);
        }
    }

    /**
	 */
    @Override
    public Component getTransactionComponent() {
        return null;
    }

    /**
	 */
    @Override
    public boolean transactionFinishBeforeCreate() {
        final File fTempQTI = new File(FolderConfig.getCanonicalTmpDir() + "/" + CodeHelper.getGlobalForeverUniqueID() + ".zip");
        Assessment assessment = createAssesment(DUMMY_TITLE);
        tmpPackage = CoreSpringFactory.getBean(QTIEditorPackageEBL.class, new Object[] { assessment });
        // we need to save the package in order to be able to create a file resource entry.
        // package will be created again after changing title.
        if (!tmpPackage.savePackageTo(fTempQTI)) {
            return false;
        }
        try {
            return (FileResourceManager.getInstance().addFileResource(fTempQTI, "qti.zip", resource) != null);
        } catch (final AddingResourceException e) {
            log.warn("Error while adding new qti.zip resource", e);
            return false;
        }
    }

    /**
	 */
    @Override
    public void repositoryEntryCreated(final RepositoryEntry re) {
        // change title
        tmpPackage.getQTIDocument().getAssessment().setTitle(re.getDisplayname());
        // re-save package into the repository, entry has not been defalted yet, so
        // saving the entry is all it needs to update.
        final File fRepositoryQTI = new File(FileResourceManager.getInstance().getFileResourceRoot(re.getOlatResource()), "qti.zip");
        fRepositoryQTI.delete();
        tmpPackage.savePackageTo(fRepositoryQTI);
        // cleanup temp files
        tmpPackage.cleanupTmpPackageDir();
    }

    /**
	 */
    @Override
    public void transactionAborted() {
        // Nothing to do here... no file has been created yet.
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        //
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }

    private Assessment createAssesment(String title) {

        final Section section = createSection();

        final List<Section> sectionList = createSectionList(section);

        final Assessment assessment = QTIEditHelperEBL.createAssessment(title, type);
        assessment.setSections(sectionList);

        return assessment;
    }

    private List<Section> createSectionList(final Section section) {
        final List<Section> sectionList = new ArrayList<Section>();
        sectionList.add(section);
        return sectionList;
    }

    private Section createSection() {
        QtiEditorLocalStrings qtiEditorLocalStrings = sectionLocalStringsFactory.createLocalStrings();
        final Section section = QTIEditHelperEBL.createSection(qtiEditorLocalStrings);
        final List<Item> itemList = new ArrayList<Item>();
        qtiEditorLocalStrings = scItemLocalStringsFactory.createLocalStrings();
        itemList.add(QTIEditHelperEBL.createSCItem(qtiEditorLocalStrings));
        section.setItems(itemList);
        return section;
    }
}
