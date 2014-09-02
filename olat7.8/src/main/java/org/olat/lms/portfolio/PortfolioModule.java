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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.lms.portfolio;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.data.basesecurity.Identity;
import org.olat.data.portfolio.structure.ElementType;
import org.olat.data.portfolio.structure.PortfolioStructure;
import org.olat.data.properties.PropertyImpl;
import org.olat.lms.group.DeletableReference;
import org.olat.lms.portfolio.artefacthandler.EPAbstractHandler;
import org.olat.lms.portfolio.artefacthandler.EPArtefactHandler;
import org.olat.lms.properties.NarrowedPropertyManager;
import org.olat.lms.user.administration.delete.UserDeletionManager;
import org.olat.presentation.collaboration.CollaborationTools;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.configuration.AbstractOLATModule;
import org.olat.system.commons.configuration.ConfigOnOff;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.group.BusinessGroup;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * The PortfolioModule contains the configurations for the e-Portfolio feature
 * <P>
 * Initial Date: 11.06.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class PortfolioModule extends AbstractOLATModule implements ConfigOnOff, PortfolioAbstractHandler {

    private static final Logger log = LoggerHelper.getLogger();

    private final List<EPArtefactHandler<?>> artefactHandlers = new ArrayList<EPArtefactHandler<?>>();
    private boolean enabled;
    private VFSContainer portfolioRoot;
    private List<String> availableMapStyles = new ArrayList<String>();
    private boolean offerPublicMapList;

    protected PortfolioModule() {
    }

    @Override
    public void initialize() {
        // portfolio enabled/disabled
        final String enabledObj = getStringPropertyValue("portfolio.enabled", true);
        if (StringHelper.containsNonWhitespace(enabledObj)) {
            enabled = "true".equals(enabledObj);
        }

        for (final EPArtefactHandler<?> handler : artefactHandlers) {
            final String enabledHandler = getStringPropertyValue("handler." + handler.getClass().getName(), true);
            if (StringHelper.containsNonWhitespace(enabledHandler)) {
                ((EPAbstractHandler<?>) handler).setEnabled("true".equals(enabledHandler));
            }
        }

        final String styles = getStringPropertyValue("portfolio.map.styles", true);
        if (StringHelper.containsNonWhitespace(styles)) {
            this.availableMapStyles = new ArrayList<String>();
            for (final String style : styles.split(",")) {
                availableMapStyles.add(style);
            }
        }

        final String offerPublicSetting = getStringPropertyValue("portfolio.offer.public.map.list", true);
        if (StringHelper.containsNonWhitespace(offerPublicSetting)) {
            setOfferPublicMapList("true".equals(offerPublicSetting));
        }

        log.info("ePortfolio is enabled: " + Boolean.toString(enabled));
    }

    @Override
    protected void initDefaultProperties() {
        enabled = getBooleanConfigParameter("portfolio.enabled", true);

        for (final EPArtefactHandler<?> handler : artefactHandlers) {
            final boolean enabledHandler = getBooleanConfigParameter("handler." + handler.getClass().getName(), true);
            ((EPAbstractHandler<?>) handler).setEnabled(enabledHandler);
        }

        this.availableMapStyles = new ArrayList<String>();
        final String styles = this.getStringConfigParameter("portfolio.map.styles", "default", false);
        if (StringHelper.containsNonWhitespace(styles)) {
            for (final String style : styles.split(",")) {
                availableMapStyles.add(style);
            }
        }

        setOfferPublicMapList(getBooleanConfigParameter("portfolio.offer.public.map.list", true));
    }

    @Override
    protected void initFromChangedProperties() {
        init();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        if (this.enabled != enabled) {
            setStringProperty("portfolio.enabled", Boolean.toString(enabled), true);
            this.enabled = enabled;
        }
    }

    /**
	 */
    @Override
    public List<EPArtefactHandler<?>> getAllAvailableArtefactHandlers() {
        final List<EPArtefactHandler<?>> handlers = new ArrayList<EPArtefactHandler<?>>(artefactHandlers.size());
        handlers.addAll(artefactHandlers);
        return handlers;
    }

    public void setEnableArtefactHandler(final EPArtefactHandler<?> handler, final boolean enabled) {
        setStringProperty("handler." + handler.getClass().getName(), Boolean.toString(enabled), true);
        handler.setEnabled(enabled);
    }

    /**
	 */
    @Override
    public List<EPArtefactHandler<?>> getArtefactHandlers() {
        final List<EPArtefactHandler<?>> handlers = new ArrayList<EPArtefactHandler<?>>(artefactHandlers.size());
        for (final EPArtefactHandler<?> handler : artefactHandlers) {
            if (handler.isEnabled()) {
                handlers.add(handler);
            }
        }
        return handlers;
    }

    /**
     * [used by Spring]
     * 
     * @param artefacthandlers
     */
    public void setArtefactHandlers(final List<EPArtefactHandler<?>> artefacthandlers) {
        this.artefactHandlers.addAll(artefacthandlers);
    }

    /**
	 */
    @Override
    public EPArtefactHandler<?> getArtefactHandler(final String type) {
        for (final EPArtefactHandler<?> handler : artefactHandlers) {
            if (type.equals(handler.getType())) {
                return handler;
            }
        }
        log.warn("Either tried to get a disabled handler or could not return a handler for artefact-type: " + type, null);
        return null;
    }

    /**
	 */
    @Override
    public EPArtefactHandler<?> getArtefactHandler(final AbstractArtefact artefact) {
        return getArtefactHandler(artefact.getResourceableTypeName());
    }

    public void addArtefactHandler(final EPArtefactHandler<?> artefacthandler) {
        artefactHandlers.add(artefacthandler);

        final String settingName = "handler." + artefacthandler.getClass().getName();
        final String propEnabled = getStringPropertyValue(settingName, true);
        if (StringHelper.containsNonWhitespace(propEnabled)) {
            // system properties settings
            ((EPAbstractHandler<?>) artefacthandler).setEnabled("true".equals(propEnabled));
        } else {
            // default settings
            final boolean defEnabled = getBooleanConfigParameter(settingName, true);
            ((EPAbstractHandler<?>) artefacthandler).setEnabled(defEnabled);
        }
    }

    public boolean removeArtefactHandler(final EPArtefactHandler<?> artefacthandler) {
        return artefactHandlers.remove(artefacthandler);
    }

    public VFSContainer getPortfolioRoot() {
        if (portfolioRoot == null) {
            portfolioRoot = new OlatRootFolderImpl(File.separator + "portfolio", null);
        }
        return portfolioRoot;
    }

    /**
     * @param availableMapStyles
     *            The availableMapStyles to set.
     */
    public void setAvailableMapStylesStr(final String availableMapStylesStr) {
        this.availableMapStyles = new ArrayList<String>();
        if (StringHelper.containsNonWhitespace(availableMapStylesStr)) {
            final String[] styles = availableMapStylesStr.split(",");
            for (final String style : styles) {
                availableMapStyles.add(style);
            }
        }
    }

    /**
     * @return Returns the availableMapStyles.
     */
    public List<String> getAvailableMapStyles() {
        return availableMapStyles;
    }

    public void setAvailableMapStylesS(final List<String> availableMapStyles) {
        this.availableMapStyles = availableMapStyles;
    }

    /**
     * @param offerPublicMapList
     *            The offerPublicMapList to set.
     */
    public void setOfferPublicMapList(final boolean offerPublicMapList) {
        this.offerPublicMapList = offerPublicMapList;
    }

    /**
     * Return setting for public map list. Systems with more than 500 public maps, should disable this feature as it gets too slow!
     * 
     * @return Returns the offerPublicMapList.
     */
    public boolean isOfferPublicMapList() {
        return offerPublicMapList;
    }

    /*
     * @Autowired(required = true) public void setUserDeletionManager(final UserDeletionManager userDeletionManager) {
     * userDeletionManager.registerDeletableUserData(this); }
     * 
     * // used for user deletion
     * 
     * @Override public void deleteUserData(Identity identity, String newDeletedUserName) { EPFrontendManager ePFMgr = (EPFrontendManager)
     * CoreSpringFactory.getBean("epFrontendManager"); ePFMgr.deleteUsersArtefacts(identity);
     * 
     * List<PortfolioStructure> userPersonalMaps = ePFMgr.getStructureElementsForUser(identity, ElementType.DEFAULT_MAP, ElementType.STRUCTURED_MAP); for
     * (PortfolioStructure portfolioStructure : userPersonalMaps) {
     * 
     * ePFMgr.deletePortfolioStructure(portfolioStructure); }
     * 
     * }
     * 
     * // used for group deletion
     * 
     * @Override public DeletableReference checkIfReferenced(BusinessGroup group, Locale locale) { return DeletableReference.createNoDeletableReference(); // dont show
     * special reference info, just delete a linked map }
     * 
     * 
     * // used for group deletion
     * 
     * @Override public boolean deleteGroupDataFor(BusinessGroup group) { EPFrontendManager ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
     * final NarrowedPropertyManager npm = NarrowedPropertyManager.getInstance(group); final PropertyImpl mapKeyProperty = npm.findProperty(null, null,
     * CollaborationTools.PROP_CAT_BG_COLLABTOOLS, CollaborationTools.KEY_PORTFOLIO); if (mapKeyProperty != null) { final Long mapKey = mapKeyProperty.getLongValue();
     * final PortfolioStructure map = ePFMgr.loadPortfolioStructureByKey(mapKey); ePFMgr.deletePortfolioStructure(map); return true; } return false; }
     */
}
