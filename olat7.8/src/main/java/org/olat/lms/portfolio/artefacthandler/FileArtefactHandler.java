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

package org.olat.lms.portfolio.artefacthandler;

import org.apache.log4j.Logger;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.vfs.OlatRelPathImpl;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSManager;
import org.olat.data.commons.vfs.olatimpl.OlatRootFileImpl;
import org.olat.data.filebrowser.metadata.MetaInfo;
import org.olat.data.group.BusinessGroup;
import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.data.portfolio.artefact.FileArtefact;
import org.olat.lms.commons.filemetadata.FileMetadataInfoService;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.repository.RepositoryService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.util.CSSHelper;
import org.olat.presentation.portfolio.artefacts.run.details.FileArtefactDetailsController;
import org.olat.system.commons.StringHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Artefacthandler for collected or uploaded files
 * <P>
 * Initial Date: 25 jun. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class FileArtefactHandler extends EPAbstractHandler<FileArtefact> {

    private static final Logger log = LoggerHelper.getLogger();

    protected FileArtefactHandler() {
    }

    @Autowired
    private BusinessGroupService businessGroupService;
    @Autowired
    private FileMetadataInfoService metaInfoService;

    /**
	 */
    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);

        // en-/disable ePortfolio collecting link in folder component
        // needs to stay here in olat3-context, as olatcore's folder-comp. doesn't
        // know about ePortfolio itself!
        FolderConfig.setEPortfolioAddEnabled(enabled);
    }

    @Override
    public FileArtefact createArtefact() {
        final FileArtefact artefact = new FileArtefact();
        return artefact;
    }

    /**
	 */
    @Override
    public void prefillArtefactAccordingToSource(final AbstractArtefact artefact, final Object source) {
        super.prefillArtefactAccordingToSource(artefact, source);
        if (source instanceof VFSItem) {
            final VFSItem fileSource = (VFSItem) source;
            ((FileArtefact) artefact).setFilename(fileSource.getName());
            final MetaInfo meta = metaInfoService.createMetaInfoFor((OlatRelPathImpl) fileSource);
            if (StringHelper.containsNonWhitespace(meta.getTitle())) {
                artefact.setTitle(meta.getTitle());
            } else {
                artefact.setTitle(fileSource.getName());
            }
            if (StringHelper.containsNonWhitespace(meta.getComment())) {
                artefact.setDescription(meta.getComment());
            }
            artefact.setSignature(60);

            final String path = ((OlatRootFileImpl) fileSource).getRelPath();
            final String[] pathElements = FileUtils.splitPathPlatformIndependent(path);

            String finalBusinessPath = null;
            String sourceInfo = null;
            // used to rebuild businessPath and source for a file:
            if (pathElements[1].equals("homes") && pathElements[2].equals(meta.getAuthor())) {
                // from users briefcase
                String lastParts = "/";
                for (int i = 4; i < (pathElements.length - 1); i++) {
                    lastParts = lastParts + pathElements[i] + "/";
                }
                sourceInfo = "Home -> " + pathElements[3] + " -> " + lastParts + fileSource.getName();
            } else if (pathElements[3].equals("BusinessGroup")) {
                // out of a businessgroup
                String lastParts = "/";
                for (int i = 5; i < (pathElements.length - 1); i++) {
                    lastParts = lastParts + pathElements[i] + "/";
                }
                final BusinessGroup bGroup = businessGroupService.loadBusinessGroup(new Long(pathElements[4]), false);
                if (bGroup != null) {
                    sourceInfo = bGroup.getName() + " -> " + lastParts + " -> " + fileSource.getName();
                }
                finalBusinessPath = "[BusinessGroup:" + pathElements[4] + "][toolfolder:0][path=" + lastParts + fileSource.getName() + ":0]";
            } else if (pathElements[3].equals("coursefolder")) {
                // the course folder
                sourceInfo = CoreSpringFactory.getBean(RepositoryService.class).lookupDisplayNameByOLATResourceableId(new Long(pathElements[2])) + " -> "
                        + fileSource.getName();
            } else if (pathElements[1].equals("course") && pathElements[3].equals("foldernodes")) {
                // folders inside a course
                sourceInfo = CoreSpringFactory.getBean(RepositoryService.class).lookupDisplayNameByOLATResourceableId(new Long(pathElements[2])) + " -> "
                        + pathElements[4] + " -> " + fileSource.getName();
                finalBusinessPath = "[RepositoryEntry:" + pathElements[2] + "][CourseNode:" + pathElements[4] + "]";
            }

            if (sourceInfo == null) {
                // unknown source, keep full path
                sourceInfo = VFSManager.getRealPath(fileSource.getParentContainer()) + "/" + fileSource.getName();
            }

            artefact.setBusinessPath(finalBusinessPath);
            artefact.setSource(sourceInfo);
        }

    }

    @Override
    public Controller createDetailsController(final UserRequest ureq, final WindowControl wControl, final AbstractArtefact artefact, final boolean readOnlyMode) {
        return new FileArtefactDetailsController(ureq, wControl, artefact, readOnlyMode);
    }

    @Override
    public String getType() {
        return FileArtefact.FILE_ARTEFACT_TYPE;
    }

    @Override
    public String getIcon(AbstractArtefact artefact) {
        final FileArtefact fileArtefact = (FileArtefact) artefact;
        if (fileArtefact.getFilename() != null) {
            return CSSHelper.createFiletypeIconCssClassFor(fileArtefact.getFilename());
        }
        return "b_filetype_file";
    }
}
