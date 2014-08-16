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
package org.olat.lms.repository.handlers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.jazzlib.ZipEntry;
import net.sf.jazzlib.ZipInputStream;
import net.sf.jazzlib.ZipOutputStream;

import org.apache.log4j.Logger;
import org.hibernate.collection.PersistentList;
import org.hibernate.proxy.HibernateProxy;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.basesecurity.SecurityGroupImpl;
import org.olat.data.commons.database.Persistable;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.xml.XStreamHelper;
import org.olat.data.portfolio.restriction.CollectRestriction;
import org.olat.data.portfolio.structure.EPAbstractMap;
import org.olat.data.portfolio.structure.EPDefaultMap;
import org.olat.data.portfolio.structure.EPPage;
import org.olat.data.portfolio.structure.EPStructureElement;
import org.olat.data.portfolio.structure.EPStructureToArtefactLink;
import org.olat.data.portfolio.structure.EPStructureToStructureLink;
import org.olat.data.portfolio.structure.EPStructuredMap;
import org.olat.data.portfolio.structure.EPStructuredMapTemplate;
import org.olat.data.portfolio.structure.PortfolioStructure;
import org.olat.data.portfolio.structure.PortfolioStructureMap;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResourceImpl;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.commons.mediaresource.StreamMediaResource;
import org.olat.lms.coordinate.LockingService;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.lms.portfolio.EPTemplateMapResource;
import org.olat.lms.portfolio.security.EPSecurityCallback;
import org.olat.lms.portfolio.security.EPSecurityCallbackFactory;
import org.olat.lms.repository.RepositoryService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.layout.MainLayoutController;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.presentation.portfolio.CreateStructureMapTemplateController;
import org.olat.presentation.portfolio.EPUIFactory;
import org.olat.presentation.repository.IAddController;
import org.olat.presentation.repository.RepositoryAddCallback;
import org.olat.presentation.repository.WizardCloseResourceController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.coordinate.LockResult;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;

/**
 * Description:<br>
 * Handler wihich allow the portfolio map in repository to be opened and launched.
 * <P>
 * Initial Date: 12 oct. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Component
public class PortfolioRepositoryHandler implements RepositoryHandler {
    private static final Logger log = LoggerHelper.getLogger();

    public static final String PROCESS_CREATENEW = "create_new";
    public static final String PROCESS_UPLOAD = "upload";

    public static XStream myStream = XStreamHelper.createXStreamInstance();

    private static final boolean DOWNLOADABLE = false;
    private static final boolean EDITABLE = true;
    private static final boolean LAUNCHABLE = true;
    private static final boolean WIZARD_SUPPORT = false;
    private List<String> supportedTypes;
    @Autowired
    EPFrontendManager ePFrontendManager;
    @Autowired
    RepositoryService repositoryService;
    @Autowired
    LockingService lockingService;

    /**
	 * 
	 */
    protected PortfolioRepositoryHandler() {
        supportedTypes = new ArrayList<String>(1);
        supportedTypes.add(EPTemplateMapResource.TYPE_NAME);

        myStream.alias("defaultMap", EPDefaultMap.class);
        myStream.alias("structureMap", EPStructuredMap.class);
        myStream.alias("templateMap", EPStructuredMapTemplate.class);
        myStream.alias("structure", EPStructureElement.class);
        myStream.alias("page", EPPage.class);
        myStream.alias("structureToArtefact", EPStructureToArtefactLink.class);
        myStream.alias("structureToStructure", EPStructureToStructureLink.class);
        myStream.alias("collectionRestriction", CollectRestriction.class);

        myStream.alias("org.olat.resource.OLATResourceImpl", OLATResourceImpl.class);
        myStream.alias("OLATResource", OLATResourceImpl.class);

        myStream.alias("org.olat.basesecurity.SecurityGroupImpl", SecurityGroupImpl.class);
        myStream.alias("SecurityGroupImpl", SecurityGroupImpl.class);

        myStream.alias("org.olat.basesecurity.SecurityGroup", SecurityGroup.class);
        myStream.alias("SecurityGroup", SecurityGroup.class);

        myStream.alias("org.olat.core.id.Persistable", Persistable.class);
        myStream.alias("Persistable", Persistable.class);

        myStream.alias("org.hibernate.proxy.HibernateProxy", HibernateProxy.class);
        myStream.alias("HibernateProxy", HibernateProxy.class);

        myStream.omitField(EPStructuredMapTemplate.class, "ownerGroup");
        myStream.addDefaultImplementation(PersistentList.class, List.class);
        myStream.addDefaultImplementation(ArrayList.class, List.class);
        myStream.registerConverter(new CollectionConverter(myStream.getMapper()) {
            @Override
            public boolean canConvert(final Class type) {
                return PersistentList.class == type;
            }
        });
    }

    /**
	 */
    @Override
    public boolean supportsDownload(final RepositoryEntry repoEntry) {
        return DOWNLOADABLE;
    }

    /**
	 */
    @Override
    public boolean supportsEdit(final RepositoryEntry repoEntry) {
        return EDITABLE;
    }

    /**
	 */
    @Override
    public boolean supportsLaunch(final RepositoryEntry repoEntry) {
        return LAUNCHABLE;
    }

    /**
	 */
    @Override
    public boolean supportsWizard(final RepositoryEntry repoEntry) {
        return WIZARD_SUPPORT;
    }

    /**
	 */
    @Override
    public String archive(final Identity archiveOnBehalfOf, final String archivFilePath, final RepositoryEntry repoEntry) {
        // Apperantly, this method is used for backing up any user related content
        // (comments etc.) on deletion. Up to now, this doesn't exist in blogs.
        return null;
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public boolean readyToDelete(final OLATResourceable res, final UserRequest ureq, final WindowControl wControl) {
        final PortfolioStructure map = ePFrontendManager.loadPortfolioStructure(res);
        if (map != null) {
            // owner group has its constraints shared beetwen the repository entry and the template
            ((EPAbstractMap) map).setOwnerGroup(null);
        }
        if (map instanceof EPStructuredMapTemplate) {
            final EPStructuredMapTemplate exercise = (EPStructuredMapTemplate) map;
            if (ePFrontendManager.isTemplateInUse(exercise, null, null, null)) {
                return false;
            }
        }
        return true;
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public boolean cleanupOnDelete(final OLATResourceable res) {
        ePFrontendManager.deletePortfolioMapTemplate(res);
        return true;
    }

    /**
	 */
    @Override
    public OLATResourceable createCopy(final OLATResourceable res, final Identity identity) {
        final PortfolioStructure structure = ePFrontendManager.loadPortfolioStructure(res);
        final String stringuified = myStream.toXML(structure);
        final PortfolioStructure newStructure = (PortfolioStructure) myStream.fromXML(stringuified);
        final PortfolioStructureMap map = ePFrontendManager.importPortfolioMapTemplate(newStructure, identity);
        return map.getOlatResource();
    }

    /**
     * org.olat.presentation.framework.UserRequest, org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public IAddController createAddController(final RepositoryAddCallback callback, final Object userObject, final UserRequest ureq, final WindowControl wControl) {
        if (PROCESS_CREATENEW.equals(userObject)) {
            return new CreateStructureMapTemplateController(callback, ureq, wControl);
        }
        return new CreateStructureMapTemplateController(null, ureq, wControl);
    }

    /**
     * Transform the map in a XML file and zip it (Repository export want a zip)
     * 
     */
    @Override
    public MediaResource getAsMediaResource(final OLATResourceable res) {
        MediaResource mr = null;

        final PortfolioStructure structure = ePFrontendManager.loadPortfolioStructure(res);
        final String xmlStructure = myStream.toXML(structure);
        try {
            // prepare a zip
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final ZipOutputStream zipOut = new ZipOutputStream(out);
            zipOut.putNextEntry(new ZipEntry("map.xml"));
            final InputStream in = new ByteArrayInputStream(xmlStructure.getBytes("UTF8"));
            FileUtils.copy(in, zipOut);
            zipOut.closeEntry();
            zipOut.close();

            // prepare media resource
            final byte[] outArray = out.toByteArray();
            FileUtils.closeSafely(out);
            FileUtils.closeSafely(in);
            final InputStream inOut = new ByteArrayInputStream(outArray);
            mr = new StreamMediaResource(inOut, null, 0l, 0l);
        } catch (final IOException e) {
            log.error("Cannot export this map: " + structure, e);
        }

        return mr;
    }

    public static final PortfolioStructure getAsObject(final File fMapXml) {
        try {
            // extract from zip
            final InputStream in = new FileInputStream(fMapXml);
            final ZipInputStream zipIn = new ZipInputStream(in);
            final ZipEntry entry = zipIn.getNextEntry();

            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            FileUtils.copy(zipIn, out);
            zipIn.closeEntry();
            zipIn.close();

            // prepare decoding with xstream
            final byte[] outArray = out.toByteArray();
            final String xml = new String(outArray);
            return (PortfolioStructure) myStream.fromXML(xml);
        } catch (final IOException e) {
            log.error("Cannot export this map: " + fMapXml, e);
        }
        return null;
    }

    /**
	 */
    @Override
    public Controller createDetailsForm(final UserRequest ureq, final WindowControl wControl, final OLATResourceable res) {
        return null;
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public Controller createEditorController(final OLATResourceable res, final UserRequest ureq, final WindowControl control) {
        return createLaunchController(res, null, ureq, control);
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public MainLayoutController createLaunchController(final OLATResourceable res, final String initialViewIdentifier, final UserRequest ureq,
            final WindowControl wControl) {
        final RepositoryEntry repoEntry = repositoryService.lookupRepositoryEntry(res, false);
        final PortfolioStructureMap map = (PortfolioStructureMap) ePFrontendManager.loadPortfolioStructure(repoEntry.getOlatResource());
        final EPSecurityCallback secCallback = EPSecurityCallbackFactory.getSecurityCallback(ureq, map, ePFrontendManager);
        final Controller epCtr = EPUIFactory.createPortfolioStructureMapController(ureq, wControl, map, secCallback);
        final LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, wControl, null, null, epCtr.getInitialComponent(), null);
        layoutCtr.addDisposableChildController(epCtr);
        return layoutCtr;
    }

    /**
	 */
    @Override
    public List<String> getSupportedTypes() {
        return supportedTypes;
    }

    /**
	 */
    @Override
    public LockResult acquireLock(final OLATResourceable ores, final Identity identity) {
        return lockingService.acquireLock(ores, identity, "subkey");
    }

    /**
	 */
    @Override
    public void releaseLock(final LockResult lockResult) {
        if (lockResult != null) {
            lockingService.releaseLock(lockResult);
        }
    }

    /**
	 */
    @Override
    public boolean isLocked(final OLATResourceable ores) {
        return lockingService.isLocked(ores, "subkey");
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public Controller createWizardController(final OLATResourceable res, final UserRequest ureq, final WindowControl wControl) {
        throw new AssertException("Trying to get wizard where no creation wizard is provided for this type.");
    }

    /**
     * org.olat.data.repository.RepositoryEntry)
     */
    @Override
    public WizardCloseResourceController createCloseResourceController(final UserRequest ureq, final WindowControl control, final RepositoryEntry repositoryEntry) {
        // No specific close wizard is implemented.
        throw new AssertException("not implemented");
    }
}
