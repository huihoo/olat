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

package org.olat.presentation.commons.filechooser;

import org.apache.log4j.Logger;
import org.olat.data.commons.fileutil.ZipUtil;
import org.olat.data.commons.vfs.LocalFileImpl;
import org.olat.data.commons.vfs.NamedContainerImpl;
import org.olat.data.commons.vfs.OlatRelPathImpl;
import org.olat.data.commons.vfs.Quota;
import org.olat.data.commons.vfs.VFSConstants;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.VFSManager;
import org.olat.data.commons.vfs.filters.VFSItemExcludePrefixFilter;
import org.olat.data.commons.vfs.filters.VFSItemFileTypeFilter;
import org.olat.data.commons.vfs.filters.VFSItemFilter;
import org.olat.data.commons.vfs.util.ContainerAndFile;
import org.olat.data.commons.vfs.util.VFSUtil;
import org.olat.data.filebrowser.FolderModule;
import org.olat.data.filebrowser.metadata.MetaInfo;
import org.olat.lms.commons.filemetadata.FileMetadataInfoService;
import org.olat.lms.commons.filemetadata.HtmlXmlFileFilterEBL;
import org.olat.presentation.filebrowser.commands.CmdUpload;
import org.olat.presentation.filebrowser.commands.FolderCommand;
import org.olat.presentation.filebrowser.components.FolderComponent;
import org.olat.presentation.framework.common.filechooser.FileChoosenEvent;
import org.olat.presentation.framework.common.filechooser.FileChooserController;
import org.olat.presentation.framework.common.filechooser.FileChooserUIFactory;
import org.olat.presentation.framework.common.htmleditor.HTMLEditorController;
import org.olat.presentation.framework.common.htmleditor.WysiwygFactory;
import org.olat.presentation.framework.common.htmlpageview.SinglePageController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.FormUIFactory;
import org.olat.presentation.framework.core.components.form.flexible.elements.SelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.Submit;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.FormSubmit;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsPreviewController;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description: <br>
 * Use the setIframeEnabled for configuration of preview behaviour
 * 
 * @author alex
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class FileChooseCreateEditController extends BasicController {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String ACTION_CHANGE = "changefile";

    private static final String VC_ENABLEEDIT = "enableEdit";
    private static final String VC_ENABLEDELETE = "enableDelete";
    private static final String VC_FILE_IS_CHOSEN = "fileIsChoosen";
    private static final String VC_CHANGE = "fileHasChanged";
    private static final String VC_CHOSENFILE = "chosenFile";
    private static final String VC_FIELDSETLEGEND = "fieldSetLegend";
    private static final String VC_DEFAULT = "default";

    // NLS support

    private static final String NLS_FIELDSET_CHOSECREATEEDITFILE = "fieldset.chosecreateeditfile";
    private static final String NLS_UNZIP_ALREADYEXISTS = "unzip.alreadyexists";
    private static final String NLS_FOLDER_DISPLAYNAME = "folder.displayname";
    private static final String NLS_ERROR_CHOOSEFILEFIRST = "error.choosefilefirst";
    private static final String NLS_ERROR_FILEDOESNOTEXIST = "error.filedoesnotexist";
    private static final String NLS_NO_FILE_CHOSEN = "no.file.chosen";
    private static final String NLS_ERROR_FILETYPE = "error.filetype";
    private static final String NLS_QUOTAEXEEDED = "QuotaExceeded";

    private VelocityContainer myContent;
    private VelocityContainer fileChooser;

    private NewFileForm newFileForm;
    private AllowRelativeLinksForm allowRelativeLinksForm;

    private FileChooserController fileChooserCtr;
    private String chosenFile;
    private VFSContainer rootContainer;
    private Boolean allowRelativeLinks;

    private CloseableModalController cmcFileChooser;
    private CloseableModalController cmcSelectionTree;
    private CloseableModalController cmcWysiwygCtr;
    private CmdUpload cmdUpload;
    private Controller wysiwygCtr;
    private LayoutMain3ColsPreviewController previewLayoutCtr;

    private boolean fileChooserActive = false;

    /** Event fired when another file has been choosen (filename has changed) **/
    public static final Event FILE_CHANGED_EVENT = new Event("filechanged");
    /** Event fired when the content of the file has been changed with the editor **/
    public static final Event FILE_CONTENT_CHANGED_EVENT = new Event("filecontentchanged");
    /** Event fired when configuration option to allow relative links has been changed **/
    public static final Event ALLOW_RELATIVE_LINKS_CHANGED_EVENT = new Event("allowrelativelinkschanged");
    private Link editButton;
    private Link deleteButton;
    private Link changeFileButtonOne;
    private Link changeFileButtonTwo;
    private Link previewLink;
    private Link chooseFileButton;

    private String[] allowedFileSuffixes = HtmlXmlFileFilterEBL.INITIAL_ALLOWED_FILE_SUFFIXES;

    private boolean allFileSuffixesAllowed = false;

    /**
     * @param ureq
     * @param wControl
     * @param chosenFile
     * @param allowRelativeLinks
     * @param rootContainer
     * @param target
     * @param fieldSetLegend
     */
    public FileChooseCreateEditController(final UserRequest ureq, final WindowControl wControl, final String chosenFile, final Boolean allowRelativeLinks,
            final VFSContainer rootContainer, final String target, final String fieldSetLegend) {
        // use folder module fallback translator
        super(ureq, wControl, PackageUtil.createPackageTranslator(FolderModule.class, ureq.getLocale()));
        init(chosenFile, allowRelativeLinks, rootContainer, target, fieldSetLegend, ureq, wControl);
    }

    /**
     * @param ureq
     * @param wControl
     * @param chosenFile
     * @param allowRelativeLinks
     * @param rootContainer
     */
    public FileChooseCreateEditController(final UserRequest ureq, final WindowControl wControl, final String chosenFile, final Boolean allowRelativeLinks,
            final VFSContainer rootContainer) {
        super(ureq, wControl, PackageUtil.createPackageTranslator(FolderModule.class, ureq.getLocale()));
        final String fieldSetLegend = getTranslator().translate(NLS_FIELDSET_CHOSECREATEEDITFILE);
        init(chosenFile, allowRelativeLinks, rootContainer, VC_DEFAULT, fieldSetLegend, ureq, wControl);
    }

    private void init(final String file, final Boolean allowRelLinks, final VFSContainer rContainer, final String target, final String fieldSetLegend,
            final UserRequest ureq, final WindowControl wControl) {
        if (log.isDebugEnabled()) {
            log.debug("Constructing FileChooseCreateEditController using the current velocity root");
        }

        this.chosenFile = file;
        this.rootContainer = rContainer;
        this.allowRelativeLinks = allowRelLinks;
        this.myContent = createVelocityContainer("chosenfile");
        editButton = LinkFactory.createButtonSmall("command.edit", myContent, this);
        deleteButton = LinkFactory.createButtonSmall("command.delete", myContent, this);
        changeFileButtonOne = LinkFactory.createButtonSmall("command.changefile", myContent, this);
        changeFileButtonTwo = LinkFactory.createButtonSmall("command.choosecreatefile", myContent, this);
        previewLink = LinkFactory.createCustomLink("command.preview", "command.preview", getTranslator().translate(NLS_FOLDER_DISPLAYNAME) + chosenFile,
                Link.NONTRANSLATED, myContent, this);
        previewLink.setCustomEnabledLinkCSS("b_preview");
        previewLink.setTitle(getTranslator().translate("command.preview"));

        this.fileChooser = createVelocityContainer("filechoosecreateedit");
        chooseFileButton = LinkFactory.createButtonSmall("command.choosefile", fileChooser, this);

        fileChooser.contextPut(VC_FIELDSETLEGEND, fieldSetLegend);
        myContent.contextPut(VC_FIELDSETLEGEND, fieldSetLegend);
        fileChooser.contextPut("target", target);
        myContent.contextPut("target", target);

        newFileForm = new NewFileForm(ureq, wControl, getTranslator(), rootContainer);
        listenTo(newFileForm);
        fileChooser.put("newfileform", newFileForm.getInitialComponent());

        allowRelativeLinksForm = new AllowRelativeLinksForm(ureq, wControl, allowRelativeLinks);
        listenTo(allowRelativeLinksForm);

        final VFSContainer namedCourseFolder = new NamedContainerImpl(getTranslator().translate(NLS_FOLDER_DISPLAYNAME), rContainer);
        rootContainer = namedCourseFolder;
        final FolderComponent folderComponent = new FolderComponent(ureq, "foldercomp", namedCourseFolder, null, null);
        folderComponent.addListener(this);
        cmdUpload = new CmdUpload(ureq, getWindowControl(), false, false);
        cmdUpload.execute(folderComponent, ureq, getWindowControl(), true);
        cmdUpload.hideFieldset();
        listenTo(cmdUpload);
        final Panel mainPanel = new Panel("upl");
        mainPanel.pushContent(cmdUpload.getInitialComponent());
        fileChooser.put(mainPanel.getComponentName(), mainPanel);
        fileChooserActive = false;
        updateVelocityVariables(chosenFile);
        putInitialPanel(myContent);
    }

    private VFSContainer doUnzip(final VFSLeaf vfsItem, final VFSContainer currentContainer, final WindowControl wControl, final UserRequest ureq) {
        final String name = vfsItem.getName();
        // we make a new folder with the same name as the zip file
        final String sZipContainer = name.substring(0, name.length() - 4);
        final VFSContainer zipContainer = currentContainer.createChildContainer(sZipContainer);
        if (zipContainer == null) {
            // folder already exists... issue warning
            wControl.setError(getTranslator().translate(NLS_UNZIP_ALREADYEXISTS, new String[] { sZipContainer }));
            // selectionTree must be set here since it fires events which will get caught in event methods below
            initFileSelectionController(ureq);
            return null;
        }
        if (!ZipUtil.unzip(vfsItem, zipContainer)) {
            // operation failed - rollback
            zipContainer.delete();
            return null;
        } else {
            // check quota
            final long quotaLeftKB = VFSManager.getQuotaLeftKB(currentContainer);
            if (quotaLeftKB != Quota.UNLIMITED && quotaLeftKB < 0) {
                // quota exceeded - rollback
                zipContainer.delete();
                wControl.setError(getTranslator().translate(NLS_QUOTAEXEEDED));
                return null;
            }
        }
        return zipContainer;
    }

    /**
     * This method generates a selection tree for choosing one file.
     * 
     * @param ureq
     * @param vfsContainer
     * @return
     */
    private void initFileSelectionController(final UserRequest ureq) {
        final VFSContainer vfsRoot = new NamedContainerImpl(getTranslator().translate(NLS_FOLDER_DISPLAYNAME), rootContainer);
        VFSItemFilter filter = null;
        if (!allFileSuffixesAllowed && allowedFileSuffixes != null) {
            filter = new VFSItemFileTypeFilter(allowedFileSuffixes);
        } else {
            // at least filter out the automatically created metadata dirs
            filter = new VFSItemExcludePrefixFilter(new String[] { "_courseelementdata" });
        }
        // Clanup old file chooser and open up new one
        removeAsListenerAndDispose(fileChooserCtr);
        fileChooserCtr = FileChooserUIFactory.createFileChooserController(ureq, getWindowControl(), vfsRoot, filter, true);
        listenTo(fileChooserCtr);
        // open modal dialog for file chooser
        removeAsListenerAndDispose(cmcSelectionTree);
        cmcSelectionTree = new CloseableModalController(getWindowControl(), getTranslator().translate("close"), fileChooserCtr.getInitialComponent());
        cmcSelectionTree.activate();
        listenTo(cmcSelectionTree);
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == wysiwygCtr) {
            if (event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT) {
                cmcWysiwygCtr.deactivate();
                if (event == Event.DONE_EVENT) {
                    fireEvent(ureq, FILE_CHANGED_EVENT);
                    if (fileChooserActive) {
                        cmcFileChooser.deactivate();
                    }
                    fileChooserActive = false;
                }
            }
        } else if (source == this.cmdUpload) {
            if (event == FolderCommand.FOLDERCOMMAND_FINISHED) {
                String fileName = cmdUpload.getFileName();
                if (fileName == null) { // cancel button pressed
                    cmcSelectionTree.deactivate();
                    fileChooserActive = false;
                    return;
                }
                fileName = fileName.toLowerCase();
                if (!isAllowedFileSuffixes(fileName)) {
                    this.showError(NLS_ERROR_FILETYPE);
                    if (cmdUpload.fileWasOverwritten().booleanValue()) {
                        return;
                    }
                    // delete file
                    final VFSItem item = rootContainer.resolve(cmdUpload.getFileName());
                    if (item != null && (item.canDelete() == VFSConstants.YES)) {
                        if (item instanceof OlatRelPathImpl) {
                            // delete all meta info
                            FileMetadataInfoService metaInfoService = CoreSpringFactory.getBean(FileMetadataInfoService.class);
                            final MetaInfo meta = metaInfoService.createMetaInfoFor((OlatRelPathImpl) item);
                            if (meta != null) {
                                meta.deleteAll();
                            }
                        }
                        // delete the item itself
                        item.delete();
                    }
                    return;
                } else {
                    if (fileName.endsWith("zip")) {
                        // unzip zip file
                        final VFSContainer zipContainer = doUnzip((VFSLeaf) rootContainer.resolve(cmdUpload.getFileName()), this.rootContainer, getWindowControl(), ureq);
                        // choose start file
                        if (zipContainer != null) {
                            // selectionTree must be set here since it fires events which will get caught in event methods below
                            initFileSelectionController(ureq);
                        }
                    } else {
                        // HTML file
                        this.chosenFile = "/" + cmdUpload.getFileName();
                        cmcFileChooser.deactivate();
                        fileChooserActive = false;
                    }
                    updateVelocityVariables(chosenFile);
                    fireEvent(ureq, FILE_CHANGED_EVENT);
                }
                return;
            }
        } else if (source == cmcFileChooser) {
            updateVelocityVariables(chosenFile);
            fileChooserActive = false;
            if (event == CloseableModalController.CLOSE_MODAL_EVENT) {
                newFileForm.formResetted(ureq);
            }

        } else if (source == newFileForm) { // make new file
            if (event == Event.DONE_EVENT) {
                final String fileName = newFileForm.getNewFileName();
                rootContainer.createChildLeaf(fileName);
                this.chosenFile = fileName;

                removeAsListenerAndDispose(wysiwygCtr);
                wysiwygCtr = createWysiwygController(ureq, getWindowControl(), rootContainer, chosenFile);
                listenTo(wysiwygCtr);
                removeAsListenerAndDispose(cmcWysiwygCtr);
                cmcWysiwygCtr = new CloseableModalController(getWindowControl(), getTranslator().translate("close"), wysiwygCtr.getInitialComponent());
                listenTo(cmcWysiwygCtr);
                cmcWysiwygCtr.activate();

                updateVelocityVariables(chosenFile);
                fireEvent(ureq, FILE_CHANGED_EVENT);
            }

        } else if (source == fileChooserCtr) { // the user chose a file or cancelled file selection
            cmcSelectionTree.deactivate();
            if (event instanceof FileChoosenEvent) {
                chosenFile = FileChooserUIFactory.getSelectedRelativeItemPath((FileChoosenEvent) event, rootContainer, null);
                updateVelocityVariables(chosenFile);
                fireEvent(ureq, FILE_CHANGED_EVENT);
                cmcFileChooser.deactivate();
                fileChooserActive = false;
            }
        } else if (source == allowRelativeLinksForm) {
            if (event == Event.DONE_EVENT) {
                allowRelativeLinks = allowRelativeLinksForm.getAllowRelativeLinksConfig();
                fireEvent(ureq, ALLOW_RELATIVE_LINKS_CHANGED_EVENT);
            }
        }
        // issue OLAT-7090
        else if (source == cmcWysiwygCtr) {
            if (wysiwygCtr instanceof HTMLEditorController && CloseableModalController.CLOSE_MODAL_EVENT.equals(event)) {
                ((HTMLEditorController) wysiwygCtr).releaseLock();
            }
        }

    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {

        if (source == previewLink) {
            removeAsListenerAndDispose(previewLayoutCtr);
            final SinglePageController previewController = new SinglePageController(ureq, getWindowControl(), rootContainer, chosenFile, null,
                    allowRelativeLinks.booleanValue());
            previewLayoutCtr = new LayoutMain3ColsPreviewController(ureq, getWindowControl(), null, null, previewController.getInitialComponent(), null);
            previewLayoutCtr.addDisposableChildController(previewController);
            previewLayoutCtr.activate();
            listenTo(previewLayoutCtr);
        }
        // edit chosen file
        else if (source == editButton) { // edit the chosen file in the rich text editor
            if (chosenFile == null) {
                showError(NLS_ERROR_CHOOSEFILEFIRST);
                return;
            }
            final VFSItem vfsItem = rootContainer.resolve(chosenFile);
            if (vfsItem == null || !(vfsItem instanceof VFSLeaf)) {
                showError(NLS_ERROR_FILEDOESNOTEXIST);
                return;
            }

            String editFile;
            VFSContainer editRoot;
            if (allowRelativeLinks.booleanValue()) {
                editRoot = rootContainer;
                editFile = chosenFile;
            } else {
                final ContainerAndFile caf = VFSUtil.calculateSubRoot(rootContainer, chosenFile);
                editRoot = caf.getContainer();
                editFile = caf.getFileName();
            }

            removeAsListenerAndDispose(wysiwygCtr);
            wysiwygCtr = createWysiwygController(ureq, getWindowControl(), editRoot, editFile);
            listenTo(wysiwygCtr);
            removeAsListenerAndDispose(cmcWysiwygCtr);
            cmcWysiwygCtr = new CloseableModalController(getWindowControl(), getTranslator().translate("close"), wysiwygCtr.getInitialComponent());
            listenTo(cmcWysiwygCtr);
            cmcWysiwygCtr.activate();
            updateVelocityVariables(chosenFile);
            fireEvent(ureq, FILE_CONTENT_CHANGED_EVENT);
        }
        // delete the chosen file
        else if (source == deleteButton) {
            if (chosenFile == null) {
                showError(NLS_ERROR_CHOOSEFILEFIRST);
                return;
            }
            final VFSItem vfsItem = rootContainer.resolve(chosenFile);
            if (vfsItem == null || !(vfsItem instanceof LocalFileImpl)) {
                showError(NLS_ERROR_FILEDOESNOTEXIST);
                return;
            }
            if (!vfsItem.exists()) {
                showError(NLS_ERROR_FILEDOESNOTEXIST);
                return;
            }
            vfsItem.delete();
            chosenFile = null;
            updateVelocityVariables(chosenFile);
            fireEvent(ureq, FILE_CHANGED_EVENT);
        }
        // change the chosen file or choose it the first time
        else if (source == changeFileButtonOne || source == changeFileButtonTwo) {
            updateVelocityVariables(chosenFile);
            removeAsListenerAndDispose(cmcFileChooser);
            cmcFileChooser = new CloseableModalController(getWindowControl(), getTranslator().translate("close"), fileChooser);
            listenTo(cmcFileChooser);
            cmcFileChooser.insertHeaderCss();
            cmcFileChooser.activate();

            fileChooserActive = true;
        } else if (source == fileChooser) {
            if (event.getCommand().equals(ACTION_CHANGE)) {
                if (chosenFile == null) {
                    showError(NLS_ERROR_CHOOSEFILEFIRST);
                    return;
                }
                cmcFileChooser.deactivate();
                updateVelocityVariables(chosenFile);
            }
        }
        // file chosen or "rechoose" pressed
        else if (source == chooseFileButton) {
            initFileSelectionController(ureq);
        }
    }

    /**
     * @return The choosen file name
     */
    public String getChosenFile() {
        return this.chosenFile;
    }

    /**
     * @return The configuration for the allow relative links flag
     */
    public Boolean getAllowRelativeLinks() {
        return this.allowRelativeLinks;
    }

    /**
     * Update all velocity variables: push file, push / remove form etc
     * 
     * @param chosenFile
     */
    private void updateVelocityVariables(final String file) {
        cmdUpload.refreshActualFolderUsage();
        if (file != null) {
            previewLink.setCustomDisplayText(getTranslator().translate(NLS_FOLDER_DISPLAYNAME) + file);
            myContent.contextPut(VC_CHANGE, Boolean.TRUE);
            myContent.contextPut(VC_CHOSENFILE, file);
            fileChooser.contextPut(VC_CHOSENFILE, file);
            myContent.contextPut(VC_FILE_IS_CHOSEN, Boolean.TRUE);
            fileChooser.contextPut(VC_FILE_IS_CHOSEN, Boolean.TRUE);
            myContent.contextPut(VC_ENABLEDELETE, Boolean.TRUE);
            // add form to velocity
            myContent.put("allowRelativeLinksForm", allowRelativeLinksForm.getInitialComponent());
            if (file.toLowerCase().endsWith(".html") || file.toLowerCase().endsWith(".htm")) {
                myContent.contextPut(VC_ENABLEEDIT, Boolean.TRUE);
            } else {
                myContent.contextPut(VC_ENABLEEDIT, Boolean.FALSE);
            }
        } else {
            myContent.contextPut(VC_CHANGE, Boolean.FALSE);
            fileChooser.contextPut(VC_CHANGE, Boolean.FALSE);
            myContent.contextPut(VC_CHOSENFILE, getTranslator().translate(NLS_NO_FILE_CHOSEN));
            fileChooser.contextPut(VC_CHOSENFILE, getTranslator().translate(NLS_NO_FILE_CHOSEN));
            myContent.contextPut(VC_FILE_IS_CHOSEN, Boolean.FALSE);
            fileChooser.contextPut(VC_FILE_IS_CHOSEN, Boolean.FALSE);
            myContent.contextPut(VC_ENABLEEDIT, Boolean.FALSE);
            myContent.contextPut(VC_ENABLEDELETE, Boolean.FALSE);
            // remove form from velocity
            myContent.remove(allowRelativeLinksForm.getInitialComponent());
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // child controllers autodisposed by basic controller
    }

    /**
     * All types of files are allowed.
     * 
     * @param allowed
     */
    public void setAllFileSuffixesAllowed(final boolean allowed) {
        this.allFileSuffixesAllowed = allowed;
    }

    protected Controller createWysiwygController(final UserRequest ureq, final WindowControl windowControl, final VFSContainer rootContainer, final String chosenFile) {
        return WysiwygFactory.createWysiwygController(ureq, windowControl, rootContainer, chosenFile, true);
    }

    /**
     * Check if a filename has a valid suffix. Allowed suffix are e.g. '.zip','.html','.xml' ZIP files are allways allowed, all other suffix depends on
     * allowedFileSuffixes array or from the flag allFileSuffixesAllowed.
     * 
     * @param fileName
     * @return true : Suffix allowed false: Suffix NOT allowed
     */
    private boolean isAllowedFileSuffixes(String fileName) {
        if (allFileSuffixesAllowed) {
            return true;
        }
        return (new HtmlXmlFileFilterEBL()).acceptFileName(fileName);
    }

}

class NewFileForm extends FormBasicController {

    private TextElement textElement;
    private Submit createFile;
    private final VFSContainer rootContainer;
    private String newFileName;

    public NewFileForm(final UserRequest ureq, final WindowControl wControl, final Translator translator, final VFSContainer rootContainer) {
        super(ureq, wControl);
        this.rootContainer = rootContainer;
        setTranslator(translator);

        initForm(ureq);
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        textElement = FormUIFactory.getInstance().addTextElement("fileName", "newfile", 20, "", formLayout);
        textElement.setMandatory(true);

        createFile = new FormSubmit("submit", "button.create");
        formLayout.add(createFile);
    }

    @Override
    protected void doDispose() {
        // nothing to dispose
    }

    @Override
    protected boolean validateFormLogic(final UserRequest ureq) {
        boolean isInputValid = true;
        String fileName = textElement.getValue();
        if (fileName == null || fileName.trim().equals("")) {
            textElement.setErrorKey("error.name.empty", new String[0]);
            isInputValid = false;
        } else {
            fileName = fileName.toLowerCase();
            // check if there are any unwanted path denominators in the name
            if (!validateFileName(fileName)) {
                textElement.setErrorKey("error.filename", new String[0]);
                isInputValid = false;
                return isInputValid;
            } else if (!fileName.endsWith(".html") && !fileName.endsWith(".htm")) {
                // add html extension if missing
                fileName = fileName + ".html";
            }
            if (fileName.charAt(0) != '/') {
                fileName = '/' + fileName;
            }
            final VFSItem vfsItem = rootContainer.resolve(fileName);
            if (vfsItem != null) {
                textElement.setErrorKey("error.fileExists", new String[] { fileName });
                isInputValid = false;
            } else {
                newFileName = fileName;
                isInputValid = true;
            }
        }
        return isInputValid;
    }

    private boolean validateFileName(final String name) {
        boolean isValid = true;
        // check if there are any unwanted path denominators in the name
        if (name.indexOf("..") > -1 || name.indexOf('/') > -1 || name.indexOf('\\') > -1) {
            isValid = false;
        }
        return isValid;
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void formResetted(final UserRequest ureq) {
        textElement.reset();
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

    /**
     * @return the new file name
     */
    public String getNewFileName() {
        return newFileName;
    }

}

class AllowRelativeLinksForm extends FormBasicController {

    private SelectionElement allowRelativeLinks;
    private final boolean isOn;

    /**
     * @param allowRelativeLinksConfig
     * @param trans
     */
    AllowRelativeLinksForm(final UserRequest ureq, final WindowControl wControl, final Boolean allowRelativeLinksConfig) {
        super(ureq, wControl);
        isOn = allowRelativeLinksConfig != null && allowRelativeLinksConfig.booleanValue();
        initForm(ureq);
    }

    /**
     * @return Boolean new configuration
     */
    Boolean getAllowRelativeLinksConfig() {
        return Boolean.valueOf(allowRelativeLinks.isSelected(0));
    }

    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        // no explicit submit button, DONE event fired every time the checkbox is clicked
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        allowRelativeLinks = uifactory.addCheckboxesVertical("allowRelativeLinks", "allowRelativeLinks", formLayout, new String[] { "xx" }, new String[] { null }, null,
                1);
        allowRelativeLinks.select("xx", isOn);
        allowRelativeLinks.addActionListener(this, FormEvent.ONCLICK);
    }

    @Override
    protected void doDispose() {
        //
    }

}
