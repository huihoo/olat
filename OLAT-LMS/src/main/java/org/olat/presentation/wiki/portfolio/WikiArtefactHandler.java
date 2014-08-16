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

package org.olat.presentation.wiki.portfolio;

import org.olat.data.group.BusinessGroup;
import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.data.portfolio.artefact.WikiArtefact;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.portfolio.artefacthandler.EPAbstractHandler;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.wiki.Wiki;
import org.olat.lms.wiki.WikiManager;
import org.olat.lms.wiki.WikiPage;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.commons.resource.OLATResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * TODO: srosse Class Description for WikiArtefactHandler
 * <P>
 * Initial Date: 7 oct. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class WikiArtefactHandler extends EPAbstractHandler<WikiArtefact> {

    protected WikiArtefactHandler() {
    }

    @Autowired
    private BusinessGroupService businessGroupService;

    @Override
    public String getType() {
        return WikiArtefact.ARTEFACT_TYPE;
    }

    @Override
    public WikiArtefact createArtefact() {
        return new WikiArtefact();
    }

    /**
	 */
    @Override
    public void prefillArtefactAccordingToSource(final AbstractArtefact artefact, final Object source) {
        super.prefillArtefactAccordingToSource(artefact, source);

        WikiPage page = null;
        OLATResourceable ores = null;
        if (source instanceof OLATResourceable) {
            ores = (OLATResourceable) source;
            final Wiki wiki = WikiManager.getInstance().getOrLoadWiki(ores);
            final String pageName = getPageName(artefact.getBusinessPath());
            page = wiki.getPage(pageName, true);
        } else if (source instanceof WikiPage) {
            page = (WikiPage) source;
        }

        if (page != null) {
            artefact.setSource(getSourceInfo(artefact.getBusinessPath(), ores));
            artefact.setTitle(page.getPageName());
            artefact.setFulltextContent(page.getContent());
            artefact.setSignature(70);
        }
    }

    private String getSourceInfo(final String businessPath, final OLATResourceable ores) {
        String sourceInfo = null;
        final String[] parts = businessPath.split(":");
        if (parts.length < 2) {
            return sourceInfo;
        }
        final String id = parts[1].substring(0, parts[1].lastIndexOf("]"));
        if (parts[0].indexOf("BusinessGroup") != -1) {
            final BusinessGroup bGroup = businessGroupService.loadBusinessGroup(new Long(id), false);
            if (bGroup != null) {
                sourceInfo = bGroup.getName();
            }
        } else if (parts[0].indexOf("RepositoryEntry") != -1) {
            final RepositoryEntry repo = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(ores, false);
            if (repo != null) {
                sourceInfo = repo.getDisplayname();
            }
        }
        return sourceInfo;
    }

    private String getPageName(final String businessPath) {
        final int start = businessPath.lastIndexOf("page=");
        final int stop = businessPath.lastIndexOf(":0]");
        if (start < stop && start > 0 && stop > 0) {
            return businessPath.substring(start + 5, stop);
        } else {
            return null;
        }
    }

    @Override
    public Controller createDetailsController(final UserRequest ureq, final WindowControl wControl, final AbstractArtefact artefact, final boolean readOnlyMode) {
        return new WikiArtefactDetailsController(ureq, wControl, artefact);
    }

    @Override
    public String getIcon(final AbstractArtefact artefact) {
        return "o_wiki_icon";
    }
}
