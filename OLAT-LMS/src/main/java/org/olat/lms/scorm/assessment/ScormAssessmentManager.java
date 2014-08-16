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
 * Copyright (c) 2008 frentix GmbH, Switzerland<br>
 * <p>
 */
package org.olat.lms.scorm.assessment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.commons.vfs.LocalFileImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.filters.VFSItemFilter;
import org.olat.lms.course.nodes.ScormCourseNode;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.lms.scorm.ScormDirectoryHelper;
import org.olat.lms.scorm.server.servermodels.ScoDocument;
import org.olat.lms.scorm.server.servermodels.SequencerModel;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * <P>
 * Initial Date: 13 august 2009 <br>
 * 
 * @author srosse
 */
public class ScormAssessmentManager extends BasicManager {

    public static final String RELOAD_SETTINGS_FILE = "reload-settings.xml";

    private static final ScormAssessmentManager instance = new ScormAssessmentManager();
    private static final Logger log = LoggerHelper.getLogger();

    public static ScormAssessmentManager getInstance() {
        return instance;
    }

    /**
     * Load the SequencerModel
     * 
     * @param username
     * @param courseEnv
     * @param node
     * @return can be null if the user hasn't visited the course
     */
    public SequencerModel getSequencerModel(final String username, final CourseEnvironment courseEnv, final ScormCourseNode node) {
        final VFSContainer scoDirectory = ScormDirectoryHelper.getScoDirectory(username, courseEnv, node);
        if (scoDirectory == null) {
            return null;
        }

        final VFSItem reloadSettingsFile = scoDirectory.resolve(RELOAD_SETTINGS_FILE);
        if (reloadSettingsFile instanceof LocalFileImpl) {
            final LocalFileImpl fileImpl = (LocalFileImpl) reloadSettingsFile;
            return new SequencerModel(fileImpl.getBasefile(), null);
        } else if (reloadSettingsFile != null) {
            throw new OLATRuntimeException(this.getClass(), "Programming error, SCORM results must be file based", null);
        }
        return null;
    }

    /**
     * Return all the datas in the sco datamodels of a SCORM course
     * 
     * @param username
     * @param courseEnv
     * @param node
     * @return
     */
    public List<CmiData> visitScoDatas(final String username, final CourseEnvironment courseEnv, final ScormCourseNode node) {
        final VFSContainer scoContainer = ScormDirectoryHelper.getScoDirectory(username, courseEnv, node);
        if (scoContainer == null) {
            return Collections.emptyList();
        }

        final List<CmiData> datas = collectData(scoContainer);
        Collections.sort(datas, new CmiDataComparator());
        return datas;
    }

    private List<CmiData> collectData(final VFSContainer scoFolder) {
        final List<CmiData> datas = new ArrayList<CmiData>();

        final List<VFSItem> contents = scoFolder.getItems(new XMLFilter());
        for (final VFSItem file : contents) {
            final ScoDocument document = new ScoDocument(null);
            try {
                if (file instanceof LocalFileImpl) {
                    document.loadDocument(((LocalFileImpl) file).getBasefile());
                } else {
                    log.warn("Cannot use this type of VSFItem to load a SCO Datamodel: " + file.getClass().getName(), null);
                    continue;
                }

                final String fileName = file.getName();
                final String itemId = fileName.substring(0, fileName.length() - 4);
                final String[][] scoModel = document.getScoModel();
                for (final String[] line : scoModel) {
                    datas.add(new CmiData(itemId, line[0], line[1]));
                }
            } catch (final Exception e) {
                log.error("Cannot load a SCO Datamodel", e);
            }
        }

        return datas;
    }

    public class XMLFilter implements VFSItemFilter {
        @Override
        public boolean accept(final VFSItem file) {
            final String name = file.getName();
            if (name.endsWith(".xml") && !(name.equals(RELOAD_SETTINGS_FILE))) {
                return true;
            }
            return false;
        }
    }
}
