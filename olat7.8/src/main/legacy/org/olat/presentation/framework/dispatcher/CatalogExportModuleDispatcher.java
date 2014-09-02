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

package org.olat.presentation.framework.dispatcher;

import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.catalog.CatalogEntry;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.catalog.CatalogService;
import org.olat.lms.commons.i18n.I18nModule;
import org.olat.lms.commons.mediaresource.NotFoundMediaResource;
import org.olat.lms.commons.mediaresource.ServletUtil;
import org.olat.lms.framework.dispatcher.CatalogExportModuleEBL;
import org.olat.presentation.framework.common.ControllerFactory;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.repository.RepositoryDetailsController;
import org.olat.presentation.repository.RepositoryEntryIconRenderer;
import org.olat.system.commons.Settings;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CatalogExportModuleDispatcher implements Dispatcher {
    private static final Logger log = LoggerHelper.getLogger();

    private static final String XML_CAT = "catalog"; // catalog root tag
    private static final String XML_NODE = "node"; // node element (catalog hierarchy structure)
    private static final String XML_LEAF = "leaf"; // leaf element (catalog entries (courses, files, ...))
    private static final String XML_CHILDREN = "children"; // child elements of nodes
    private static final String XML_DESCR = "description"; // catalog(!) description of nodes and leaves
    private static final String XML_TYPE = "type"; // catalog entry type (course, PDF, ...) -> translated to installations default language!
    private static final String XML_TYPE_CSS = "iconCSS"; // CSS class for catalog entry type icon
    private static final String XML_LINKS = "links"; // links to entries container
    private static final String XML_LINK = "link"; // link to the entry
    private static final String XML_LINKTYPE = "type"; // type of the link to the entry (see following)
    private static final String XML_LINKTYPE_GUEST = "guest"; // -> link for guest access (only there if accessible by guests)
    private static final String XML_LINKTYPE_LOGIN = "login"; // -> link with login required
    private static final String XML_LINKTYPE_DETAIL = "detail"; // -> link to entry detail page
    private static final String XML_LINKTYPE_ENROLL = "cbb_enrollment"; // -> link to enrollment course buildings blocks in entry (there may be many of this!!!)
    private static final String XML_ACC = "access"; // entry access settings attribute name
    private static final String XML_OWN = "owners"; // entry owners container element name
    private static final String XML_USR = "user"; // entry owners user subelements
    private static final String XML_CUSTOM = "custom"; // custom info (empty for now)

    // NLS:

    private static final String NLS_CIF_TYPE_NA = "cif.type.na";
    private static final String NLS_TABLE_HEADER_ACCESS_GUEST = "access.guest";
    private static final String NLS_TABLE_HEADER_ACCESS_USER = "access.user";
    private static final String NLS_TABLE_HEADER_ACCESS_AUTHOR = "access.author";
    private static final String NLS_TABLE_HEADER_ACCESS_OWNER = "access.owner";

    private static DocumentBuilderFactory domFactory = null;
    private static DocumentBuilder domBuilder = null;
    private static BaseSecurity securityManager = null;

    private static Translator repoTypeTranslator = null;
    private static Translator catalogExportTranslator = null;

    private TimerTask tt;
    private long updateInterval;
    private CatalogService catalogService;
    private static boolean instance = false;

    CatalogExportModuleEBL catalogExportModuleEBL;

    /**
     * @return
     */
    private CatalogExportModuleDispatcher(final Long updateInterval) {
        this.updateInterval = updateInterval * 60 * 1000;
        if (this.updateInterval < 60000) {
            // interval is smaller than one minute -> inform and go to default
            this.updateInterval = 5 * 60 * 1000;
            log.info("Update interval is to small , increasing to default of 5min!");
        }
        catalogService = CoreSpringFactory.getBean(CatalogService.class);
    }

    synchronized private boolean reInitialize() {// o_clusterOK by:fj
        boolean retVal = true;
        if (instance) {
            return retVal;
        }
        // TODO there is a new way of creating package translator
        repoTypeTranslator = new PackageTranslator(PackageUtil.getPackageName(RepositoryDetailsController.class), I18nModule.getDefaultLocale());
        catalogExportTranslator = new PackageTranslator(PackageUtil.getPackageName(CatalogExportModuleDispatcher.class), I18nModule.getDefaultLocale());
        securityManager = (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
        try {
            domFactory = DocumentBuilderFactory.newInstance(); // init
            domBuilder = domFactory.newDocumentBuilder();
        } catch (final Exception e) {
            retVal = false;
        }
        tt = new TimerTask() {
            @Override
            public void run() {
                Thread.currentThread().setPriority(Thread.MIN_PRIORITY); // don't disturb other things going on
                createXML();
            }
        };
        final Timer timer = new Timer();
        timer.schedule(tt, (new GregorianCalendar()).getTime(), updateInterval);
        instance = true;
        return retVal;
    }

    protected void createXML() {
        log.debug("Creating catalog export XML file...");
        final Document doc = domBuilder.newDocument(); // create new XML document
        final Element cat = doc.createElement(XML_CAT); // catalog element
        doc.appendChild(cat);
        cat.setAttribute("date", String.valueOf(System.currentTimeMillis())); // set date in catalog element
        final Element root = doc.createElement(XML_NODE); // root node
        root.setAttribute("name", "root");
        cat.appendChild(root);

        final List ces = catalogService.getRootCatalogEntries();
        for (final Iterator it = ces.iterator(); it.hasNext();) { // for every root entry (currently only one)
            final CatalogEntry ce = (CatalogEntry) it.next();
            getCatalogSubStructure(doc, root, catalogService, ce); // scan this entry
        }

        try {
            catalogExportModuleEBL.transformCatalogXml(doc);
        } catch (final Exception e) {
            log.error("Error writing catalog export file.", e);
        }
    }

    private void getCatalogSubStructure(final Document doc, final Element parent, final CatalogService catalogService, final CatalogEntry ce) {
        Element cur = null; // tmp. element
        final List l = catalogService.getChildrenOf(ce); // get catalog children
        // all nodes
        for (final Iterator it = l.iterator(); it.hasNext();) { // scan for node entries
            final CatalogEntry c = (CatalogEntry) it.next();
            if (c.getType() == CatalogEntry.TYPE_NODE) { // it's a node

                final Element node = doc.createElement(XML_NODE); // node element
                node.setAttribute("name", c.getName());
                parent.appendChild(node);

                cur = doc.createElement(XML_DESCR); // description element
                cur.appendChild(doc.createTextNode(c.getDescription()));
                node.appendChild(cur);

                if (catalogService.getChildrenOf(c).size() > 0) { // children element containing all subentries
                    cur = doc.createElement(XML_CHILDREN);
                    node.appendChild(cur);
                    getCatalogSubStructure(doc, cur, catalogService, c); // recursive scan
                }

                cur = doc.createElement(XML_CUSTOM);
                /*
                 * Insert custom info here!
                 */
                node.appendChild(cur);

            }
        }
        // all leafes
        for (final Iterator it = l.iterator(); it.hasNext();) { // scan for leaf entries
            final CatalogEntry c = (CatalogEntry) it.next();
            if (c.getType() == CatalogEntry.TYPE_LEAF) {
                final RepositoryEntry re = c.getRepositoryEntry(); // get repo entry
                if (re.getAccess() > RepositoryEntry.ACC_OWNERS_AUTHORS) { // just show entries visible for registered users
                    final Element leaf = doc.createElement(XML_LEAF); // leaf element
                    leaf.setAttribute("name", c.getName());
                    parent.appendChild(leaf);

                    cur = doc.createElement(XML_DESCR); // description element
                    cur.appendChild(doc.createTextNode(c.getDescription()));
                    leaf.appendChild(cur);

                    cur = doc.createElement(XML_TYPE);
                    final String typeName = re.getOlatResource().getResourceableTypeName(); // add the resource type
                    final StringOutput typeDisplayText = new StringOutput(100);
                    if (typeName != null) { // add typename code
                        final RepositoryEntryIconRenderer reir = new RepositoryEntryIconRenderer();
                        cur.setAttribute(XML_TYPE_CSS, reir.getIconCssClass(re));
                        final String tName = ControllerFactory.translateResourceableTypeName(typeName, repoTypeTranslator.getLocale());
                        typeDisplayText.append(tName);
                    } else {
                        typeDisplayText.append(repoTypeTranslator.translate(NLS_CIF_TYPE_NA));
                    }
                    cur.appendChild(doc.createTextNode(typeDisplayText.toString()));
                    leaf.appendChild(cur);

                    final Element links = doc.createElement(XML_LINKS); // links container
                    String tmp = "";
                    String url = Settings.getServerContextPathURI() + "/url/CatalogEntry/" + re.getKey();
                    switch (re.getAccess()) { // Attention! This uses the switch-case-fall-through mechanism!
                    case RepositoryEntry.ACC_USERS_GUESTS:
                        tmp = catalogExportTranslator.translate(NLS_TABLE_HEADER_ACCESS_GUEST) + tmp;
                        appendLinkElement(doc, links, XML_LINKTYPE_GUEST, url + "&guest=true&amp;lang=" + I18nModule.getDefaultLocale().toString().toLowerCase());
                    case RepositoryEntry.ACC_USERS:
                        tmp = catalogExportTranslator.translate(NLS_TABLE_HEADER_ACCESS_USER) + tmp;
                    case RepositoryEntry.ACC_OWNERS_AUTHORS:
                        tmp = catalogExportTranslator.translate(NLS_TABLE_HEADER_ACCESS_AUTHOR) + tmp;
                    case RepositoryEntry.ACC_OWNERS:
                        tmp = catalogExportTranslator.translate(NLS_TABLE_HEADER_ACCESS_OWNER) + tmp;
                        appendLinkElement(doc, links, XML_LINKTYPE_LOGIN, url);
                        break;
                    default:
                        tmp = catalogExportTranslator.translate(NLS_TABLE_HEADER_ACCESS_USER);
                        break;
                    }

                    // when implemented in OLAT, add link to detail page and enrollment entries here
                    // appendLinkElement(doc, links, XML_LINKTYPE_DETAIL, RepoJumpInHandlerFactory.buildRepositoryDispatchURI2DeatilPage(re));
                    // appendALotOfLinkElements4EnrollmentCBBsNeverthelessTheyAreVisibleAndOrAccessibleOrNot(doc, links, XML_LINKTYPE_ENROLL, re);

                    leaf.setAttribute(XML_ACC, tmp); // access rights as attribute
                    leaf.appendChild(links); // append links container

                    final Element owners = doc.createElement(XML_OWN); // owners node
                    leaf.appendChild(owners);
                    final SecurityGroup sg = re.getOwnerGroup();
                    final List m = securityManager.getIdentitiesOfSecurityGroup(sg);
                    for (final Iterator iter = m.iterator(); iter.hasNext();) {
                        final Identity i = (Identity) iter.next();
                        cur = doc.createElement(XML_USR); // get all users
                        cur.appendChild(doc.createTextNode(i.getName()));
                        owners.appendChild(cur);
                    }

                    cur = doc.createElement(XML_CUSTOM);
                    /*
                     * Insert custom info here!
                     */
                    leaf.appendChild(cur);

                }
            }
        }
    }

    private void appendLinkElement(final Document doc, final Element parent, final String type, final String URL) {
        final Element link = doc.createElement(XML_LINK);
        link.appendChild(doc.createTextNode(URL));
        link.setAttribute(XML_LINKTYPE, type);
        parent.appendChild(link);
    }

    @Override
    public void execute(final HttpServletRequest request, final HttpServletResponse response, final String uriPrefix) {
        catalogExportModuleEBL = getCatalogExportModuleEBL();
        if (!this.reInitialize()) {
            log.error("Some Failsaves in reInitialization needed !");
        }
        try {
            log.info("Catalog XML file requested by " + request.getRemoteAddr());
            ServletUtil.serveResource(request, response, catalogExportModuleEBL.getCatalogXmlFileMediaResource());
        } catch (final Exception e) {
            log.error("Error requesting catalog export file: ", e);
            try {
                ServletUtil.serveResource(request, response, new NotFoundMediaResource(request.getRequestURI()));
            } catch (final Exception e1) {
                // what now???
                log.error("What now ???");
            }
        }
    }

    private CatalogExportModuleEBL getCatalogExportModuleEBL() {
        return CoreSpringFactory.getBean(CatalogExportModuleEBL.class);
    }
}
