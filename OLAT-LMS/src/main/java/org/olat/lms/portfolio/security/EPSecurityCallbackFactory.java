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

package org.olat.lms.portfolio.security;

import org.olat.data.portfolio.structure.EPDefaultMap;
import org.olat.data.portfolio.structure.EPStructuredMap;
import org.olat.data.portfolio.structure.EPStructuredMapTemplate;
import org.olat.data.portfolio.structure.PortfolioStructureMap;
import org.olat.data.portfolio.structure.StructureStatusEnum;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * Description:<br>
 * EPSecurityCallbackFactory
 * <P>
 * Initial Date: 18 nov. 2010 <br>
 * 
 * @author srosse
 */
public class EPSecurityCallbackFactory {

    public static EPSecurityCallback getSecurityCallback(final UserRequest ureq, final PortfolioStructureMap map, final EPFrontendManager ePFMgr) {
        if (map instanceof EPDefaultMap) {
            return getSecurityCallbackMap(ureq, map, ePFMgr);
        } else if (map instanceof EPStructuredMap) {
            return getSecurityCallbackStructuredMap(ureq, map, ePFMgr);
        } else if (map instanceof EPStructuredMapTemplate) {
            return getSecurityCallbackTemplate(ureq, map, ePFMgr);
        }
        return new EPSecurityCallbackOwner(false, false, false);
    }

    public static boolean isLockNeeded(final EPSecurityCallback secCallback) {
        return secCallback.canAddArtefact() || secCallback.canAddPage() || secCallback.canAddStructure() || secCallback.canEditStructure();
    }

    public static EPSecurityCallback updateAfterFailedLock(final EPSecurityCallback secCallback) {
        final boolean canEditStructure = false;
        final boolean canShare = secCallback.canShareMap();
        final boolean canAddArtefact = false;
        final boolean canRemoveArtefactFromStruct = false;
        final boolean canAddStructure = false;
        final boolean canAddPage = false;
        final boolean canView = secCallback.canView();
        final boolean canCommentAndRate = secCallback.canCommentAndRate();
        final boolean canSubmitAssess = false;
        final boolean restrictionsEnabled = secCallback.isRestrictionsEnabled();
        final boolean isOwner = secCallback.isOwner();

        return new EPSecurityCallbackImpl(canEditStructure, canShare, canAddArtefact, canRemoveArtefactFromStruct, canAddStructure, canAddPage, canView,
                canCommentAndRate, canSubmitAssess, restrictionsEnabled, isOwner);
    }

    /**
     * EPDefault: owner can edit them (add structure, artefacts), viewers can comments
     * 
     * @param ureq
     * @param map
     * @param ePFMgr
     * @return
     */
    protected static EPSecurityCallback getSecurityCallbackMap(final UserRequest ureq, final PortfolioStructureMap map, final EPFrontendManager ePFMgr) {
        final boolean isOwner = ePFMgr.isMapOwner(ureq.getIdentity(), map.getOlatResource());
        final boolean isVisible = ePFMgr.isMapVisible(ureq.getIdentity(), map.getOlatResource());

        final boolean canEditStructure = isOwner;
        final boolean canShare = isOwner;
        final boolean canAddArtefact = isOwner;
        final boolean canRemoveArtefactFromStruct = isOwner;
        final boolean canAddStructure = isOwner;
        final boolean canAddPage = isOwner;
        final boolean canView = isVisible;
        final boolean canCommentAndRate = isVisible || isOwner;
        final boolean canSubmitAssess = false;
        final boolean restrictionsEnabled = false;

        return new EPSecurityCallbackImpl(canEditStructure, canShare, canAddArtefact, canRemoveArtefactFromStruct, canAddStructure, canAddPage, canView,
                canCommentAndRate, canSubmitAssess, restrictionsEnabled, isOwner);
    }

    /**
     * EPStructuredMap: owner can edit as long as map is not closed
     * 
     * @param ureq
     * @param map
     * @param ePFMgr
     * @return
     */
    protected static EPSecurityCallback getSecurityCallbackStructuredMap(final UserRequest ureq, final PortfolioStructureMap map, final EPFrontendManager ePFMgr) {
        final boolean isOwner = ePFMgr.isMapOwner(ureq.getIdentity(), map.getOlatResource());
        final boolean isCoach = false;
        final boolean isVisible = ePFMgr.isMapVisible(ureq.getIdentity(), map.getOlatResource());
        final boolean open = !StructureStatusEnum.CLOSED.equals(map.getStatus());

        final boolean canEditStructure = false;
        final boolean canShare = (isOwner || isCoach);
        final boolean canAddArtefact = isOwner && open;
        final boolean canRemoveArtefactFromStruct = isOwner && open;
        final boolean canAddStructure = false;
        final boolean canAddPage = false;
        final boolean canView = isVisible || isCoach;
        final boolean canCommentAndRate = isVisible || isCoach || isOwner;
        final boolean canSubmitAssess = isOwner;
        final boolean restrictionsEnabled = true;

        return new EPSecurityCallbackImpl(canEditStructure, canShare, canAddArtefact, canRemoveArtefactFromStruct, canAddStructure, canAddPage, canView,
                canCommentAndRate, canSubmitAssess, restrictionsEnabled, isOwner);
    }

    /**
     * Owner or admin have the right to edit structure if the flag CLOSED is not set. Their some restrictions if the map is already in use.
     * 
     * @param ureq
     * @param map
     * @param ePFMgr
     * @return
     */
    protected static EPSecurityCallback getSecurityCallbackTemplate(final UserRequest ureq, final PortfolioStructureMap map, final EPFrontendManager ePFMgr) {
        final OLATResourceable mres = map.getOlatResource();
        final RepositoryEntry repoEntry = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(mres, false);
        final boolean isAdmin = ureq.getUserSession().getRoles().isOLATAdmin();
        // owner of repository entry or owner of map is the same
        boolean isOwner = RepositoryServiceImpl.getInstance().isOwnerOfRepositoryEntry(ureq.getIdentity(), repoEntry);
        final boolean canLaunch = RepositoryServiceImpl.getInstance().isAllowedToLaunch(ureq.getIdentity(), ureq.getUserSession().getRoles(), repoEntry);

        isOwner |= ePFMgr.isMapOwner(ureq.getIdentity(), map.getOlatResource());
        final boolean open = !StructureStatusEnum.CLOSED.equals(map.getStatus());

        final boolean canEditStructure = (isOwner || isAdmin) && open;
        final boolean canShare = false;
        final boolean canAddArtefact = false; // (isOwner || isAdmin) && open;
        final boolean canRemoveArtefactFromStruct = (isOwner || isAdmin) && open;
        final boolean canAddStructure = (isOwner || isAdmin) && open;
        final boolean canAddPage = (isOwner || isAdmin) && open;
        final boolean canView = canLaunch;
        final boolean canCommentAndRate = false;
        final boolean canSubmitAssess = false;
        final boolean restrictionsEnabled = true;// for author

        return new EPSecurityCallbackImpl(canEditStructure, canShare, canAddArtefact, canRemoveArtefactFromStruct, canAddStructure, canAddPage, canView,
                canCommentAndRate, canSubmitAssess, restrictionsEnabled, isOwner);
    }
}
