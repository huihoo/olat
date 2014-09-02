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
 * Copyright (c) 1999-2008 at frentix GmbH, Switzerland, http://www.frentix.com
 * <p>
 */
package org.olat.presentation.glossary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.activitylogging.CoreLoggingResourceable;
import org.olat.lms.activitylogging.LearningResourceLoggingAction;
import org.olat.lms.activitylogging.OlatResourceableType;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.coordinate.LockingService;
import org.olat.lms.glossary.GlossaryItem;
import org.olat.lms.glossary.GlossaryItemManager;
import org.olat.lms.repository.RepositoryService;
import org.olat.presentation.commons.DisposedRepoEntryRestartController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.control.generic.dtabs.Activateable;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.presentation.framework.core.translator.I18nPackage;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.presentation.repository.EntryChangedEvent;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.LockResult;
import org.olat.system.event.Event;
import org.olat.system.event.GenericEventListener;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Displays a List of all glossary-entries. If the user is author or administrator, he will get Links to add, edit or delete Items. The list is sortable by an
 * alphabetical register.
 * 
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
public class GlossaryMainController extends BasicController implements Activateable, GenericEventListener {

    private static final String CMD_EDIT = "cmd.edit.";
    private static final String CMD_DELETE = "cmd.delete.";
    private static final String REGISTER_LINK = "register.link.";

    private final VFSContainer glossaryFolder;
    private final OLATResourceable resourceable;
    private final boolean editModeEnabled;
    private final RepositoryEntry glossaryRepoEntry;

    private final VelocityContainer glistVC;
    private final Link addButton;
    private LockResult lockEntry = null;
    private DialogBoxController deleteDialogCtr;
    private CloseableModalController cmc;

    private ArrayList<GlossaryItem> glossaryItemList;
    private GlossaryItem currentDeleteItem;
    private String filterIndex = "";

    public GlossaryMainController(WindowControl wControl, UserRequest ureq, VFSContainer glossaryFolder, OLATResourceable res, boolean allowGlossaryEditing) {
        super(ureq, wControl);
        this.glossaryFolder = glossaryFolder;
        this.resourceable = res;
        this.editModeEnabled = allowGlossaryEditing;
        glossaryRepoEntry = CoreSpringFactory.getBean(RepositoryService.class).lookupRepositoryEntry(resourceable, true);

        glossaryItemList = GlossaryItemManager.getInstance().getGlossaryItemListByVFSItem(glossaryFolder);
        addLoggingResourceable(CoreLoggingResourceable.wrap(res, OlatResourceableType.genRepoEntry));
        ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_OPEN, getClass());
        glistVC = createVelocityContainer("glossarylist");

        addButton = LinkFactory.createButtonSmall("cmd.add", glistVC, this);
        initEditView(ureq, allowGlossaryEditing);

        Properties glossProps = GlossaryItemManager.getInstance().getGlossaryConfig(glossaryFolder);
        Boolean registerEnabled = Boolean.valueOf(glossProps.getProperty(GlossaryItemManager.REGISTER_ONOFF));
        glistVC.contextPut("registerEnabled", registerEnabled);
        if (!registerEnabled) {
            filterIndex = "all";
        }
        updateRegisterAndGlossaryItems();

        Link showAllLink = LinkFactory.createCustomLink(REGISTER_LINK + "all", REGISTER_LINK + "all", "glossary.list.showall", Link.LINK, glistVC, this);
        glistVC.contextPut("showAllLink", showAllLink);

        // add javascript and css file
        JSAndCSSComponent tmJs = new JSAndCSSComponent("glossaryJS", this.getClass(), null, "glossary.css", true);
        glistVC.put("glossaryJS", tmJs);

        putInitialPanel(glistVC);

        // disposed message controller
        final Panel empty = new Panel("empty");// empty panel set as "menu" and "tool"
        final Translator trans = PackageUtil.createPackageTranslator(I18nPackage.GLOSSARY_, ureq.getLocale());
        final Controller disposedRestartController = new DisposedRepoEntryRestartController(ureq, wControl, glossaryRepoEntry,
                trans.translate("glossary.disposed.title"), trans.translate("glossary.disposed.message"), trans.translate("glossary.disposed.command.restart"),
                trans.translate("glossary.deleted.title"), trans.translate("glossary.deleted.text"));
        final Controller layoutController = new LayoutMain3ColsController(ureq, wControl, empty, empty, disposedRestartController.getInitialComponent(),
                "disposed glossary" + glossaryRepoEntry.getResourceableId());
        setDisposedMsgController(layoutController);

        // add as listener to glossary so we are being notified about events:
        // - deletion (OLATResourceableJustBeforeDeletedEvent)
        // - modification (EntryChangedEvent)
        final Identity identity = ureq.getIdentity();
        CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, identity, res);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_CLOSE, getClass());

        // controllers get disposed itself
        // release edit lock
        if (lockEntry != null) {
            getLockingService().releaseLock(lockEntry);
            lockEntry = null;
        }
    }

    private LockingService getLockingService() {
        return CoreSpringFactory.getBean(LockingService.class);
    }

    /**
     * create a List with all Indexes in this Glossary
     * 
     * @param gIList
     * @return List containing the Links.
     */
    protected List<Link> getIndexLinkList(ArrayList<GlossaryItem> gIList) {
        List<Link> indexLinkList = new ArrayList<Link>(gIList.size());
        Set<String> addedKeys = new HashSet<String>();
        // get existing indexes
        for (GlossaryItem gi : gIList) {
            String indexChar = gi.getIndex();
            if (!addedKeys.contains(indexChar)) {
                addedKeys.add(indexChar);
            }
        }
        // build register, first found should be used later on
        char alpha;
        boolean firstIndexFound = false;
        for (alpha = 'A'; alpha <= 'Z'; alpha++) {
            String indexChar = String.valueOf(alpha);
            Link indexLink = LinkFactory.createCustomLink(REGISTER_LINK + indexChar, REGISTER_LINK + indexChar, indexChar, Link.NONTRANSLATED, glistVC, this);
            if (!addedKeys.contains(indexChar)) {
                indexLink.setEnabled(false);
            } else if (!filterIndex.equals("all") && !firstIndexFound && !addedKeys.contains(filterIndex)) {
                filterIndex = indexChar;
                firstIndexFound = true;
            }
            indexLinkList.add(indexLink);
        }

        return indexLinkList;
    }

    /**
	 */
    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        Controller glossEditCtrl = null;
        if (source == addButton) {
            removeAsListenerAndDispose(glossEditCtrl);
            glossEditCtrl = new GlossaryItemEditorController(ureq, getWindowControl(), glossaryFolder, glossaryItemList, null);
            listenTo(glossEditCtrl);
            removeAsListenerAndDispose(cmc);
            cmc = new CloseableModalController(getWindowControl(), "close", glossEditCtrl.getInitialComponent());
            cmc.activate();
            listenTo(cmc);
        } else if (source instanceof Link) {
            Link button = (Link) source;
            String cmd = button.getCommand();
            if (button.getUserObject() instanceof GlossaryItem) {
                GlossaryItem currentGlossaryItem = (GlossaryItem) button.getUserObject();
                if (cmd.startsWith(CMD_EDIT)) {
                    removeAsListenerAndDispose(glossEditCtrl);
                    glossEditCtrl = new GlossaryItemEditorController(ureq, getWindowControl(), glossaryFolder, glossaryItemList, currentGlossaryItem);
                    listenTo(glossEditCtrl);
                    removeAsListenerAndDispose(cmc);
                    cmc = new CloseableModalController(getWindowControl(), "close", glossEditCtrl.getInitialComponent());
                    cmc.activate();
                    listenTo(cmc);
                } else if (button.getCommand().startsWith(CMD_DELETE)) {
                    currentDeleteItem = currentGlossaryItem;
                    if (deleteDialogCtr != null) {
                        deleteDialogCtr.dispose();
                    }
                    String text = translate("glossary.delete.dialog", StringHelper.escapeHtml(currentGlossaryItem.getGlossTerm()));
                    deleteDialogCtr = activateYesNoDialog(ureq, null, text, deleteDialogCtr);
                }
            } else if (button.getCommand().startsWith(REGISTER_LINK)) {
                filterIndex = cmd.substring(cmd.lastIndexOf(".") + 1);

                updateRegisterAndGlossaryItems();
            }
        }

    }

    @Override
    protected void event(UserRequest ureq, Controller source, Event event) {
        super.event(ureq, source, event);
        if (source == cmc) {
            // modal dialog closed -> persist changes on glossaryitem
            GlossaryItemManager.getInstance().saveGlossaryItemList(glossaryFolder, glossaryItemList);
            CoordinatorManager.getInstance().getCoordinator().getEventBus()
                    .fireEventToListenersOf(new EntryChangedEvent(glossaryRepoEntry, EntryChangedEvent.MODIFIED), resourceable);
            glossaryItemList = GlossaryItemManager.getInstance().getGlossaryItemListByVFSItem(glossaryFolder);
            updateRegisterAndGlossaryItems();
        } else if (source == deleteDialogCtr) {
            if (DialogBoxUIFactory.isYesEvent(event)) {
                glossaryItemList.remove(currentDeleteItem);
                GlossaryItemManager.getInstance().saveGlossaryItemList(glossaryFolder, glossaryItemList);
                CoordinatorManager.getInstance().getCoordinator().getEventBus()
                        .fireEventToListenersOf(new EntryChangedEvent(glossaryRepoEntry, EntryChangedEvent.MODIFIED), resourceable);
                updateRegisterAndGlossaryItems();
            }
        }

    }

    /**
	 */
    @Override
    public void activate(UserRequest ureq, String viewIdentifier) {
        // if already open from LR and tab gets activated from course:
        if (viewIdentifier != null) {
            boolean allowEdit = Boolean.parseBoolean(viewIdentifier);
            initEditView(ureq, allowEdit);
        }
    }

    private void updateRegisterAndGlossaryItems() {
        glistVC.contextPut("registerLinkList", getIndexLinkList(glossaryItemList));
        glistVC.contextPut("editAndDelButtonList", updateView(glossaryItemList, filterIndex));
    }

    /**
     * @param List
     *            with GlossaryItems
     * @return a list (same size as GlossaryItems) which contains again lists with one editButton and one deleteButton
     */
    private List<List<Link>> updateView(ArrayList<GlossaryItem> gIList, String choosenFilterIndex) {
        List<List<Link>> editAndDelButtonList = new ArrayList<List<Link>>(gIList.size());
        int linkNum = 1;
        Set<String> keys = new HashSet<String>();
        StringBuilder bufDublicates = new StringBuilder();
        Collections.sort(gIList);

        glistVC.contextPut("filterIndex", choosenFilterIndex);
        if (!filterIndex.equals("all")) {
            // highlight filtered index
            Link indexLink = (Link) glistVC.getComponent(REGISTER_LINK + choosenFilterIndex);
            if (indexLink != null) {
                indexLink.setCustomEnabledLinkCSS("o_glossary_register_active");
            }
        }

        for (GlossaryItem gi : gIList) {
            Link tmpEditButton = LinkFactory.createCustomLink(CMD_EDIT + linkNum, CMD_EDIT + linkNum, "cmd.edit", Link.BUTTON_SMALL, glistVC, this);
            tmpEditButton.setUserObject(gi);
            Link tmpDelButton = LinkFactory.createCustomLink(CMD_DELETE + linkNum, CMD_DELETE + linkNum, "cmd.delete", Link.BUTTON_SMALL, glistVC, this);
            tmpDelButton.setUserObject(gi);
            List<Link> tmpList = new ArrayList<Link>(2);
            tmpList.add(tmpEditButton);
            tmpList.add(tmpDelButton);

            if (keys.contains(gi.getGlossTerm()) && (bufDublicates.indexOf(gi.getGlossTerm()) == -1)) {
                bufDublicates.append(gi.getGlossTerm());
                bufDublicates.append(" ");
            } else {
                keys.add(gi.getGlossTerm());
            }
            editAndDelButtonList.add(tmpList);
            linkNum++;
        }

        if ((bufDublicates.length() > 0) && editModeEnabled) {
            showWarning("warning.contains.dublicates", bufDublicates.toString());
        }
        return editAndDelButtonList;
    }

    /**
     * show edit buttons only if there is not yet a lock on this glossary
     * 
     * @param ureq
     * @param allowGlossaryEditing
     */
    private void initEditView(UserRequest ureq, boolean allowGlossaryEditing) {

        glistVC.contextPut("editModeEnabled", Boolean.valueOf(allowGlossaryEditing));
        if (allowGlossaryEditing) {
            // try to get lock for this glossary
            lockEntry = getLockingService().acquireLock(resourceable, ureq.getIdentity(), "GlossaryEdit");
            if (!lockEntry.isSuccess()) {
                showInfo("glossary.locked", lockEntry.getOwner().getName());
                glistVC.contextPut("editModeEnabled", Boolean.FALSE);
            }
        }
    }

    @Override
    public void event(Event event) {
        if (event instanceof OLATResourceableJustBeforeDeletedEvent) {
            final OLATResourceableJustBeforeDeletedEvent ojde = (OLATResourceableJustBeforeDeletedEvent) event;
            if (ojde.targetEquals(resourceable, true)) {
                dispose();
            }
        } else if (event instanceof EntryChangedEvent) {
            final EntryChangedEvent repoEvent = (EntryChangedEvent) event;
            if (glossaryRepoEntry.getKey().equals(repoEvent.getChangedEntryKey()) && repoEvent.getChange() == EntryChangedEvent.MODIFIED && !editModeEnabled) {
                dispose();
            }
        }
    }

    @Override
    public boolean isControllerAndNotDisposed() {
        return !isDisposed();
    }

}
