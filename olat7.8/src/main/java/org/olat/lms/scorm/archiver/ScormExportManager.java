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
 * Copyright (c) 2009 frentix GmbH, Switzerland<br>
 * <p>
 */
package org.olat.lms.scorm.archiver;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.fileutil.ExportUtil;
import org.olat.data.commons.vfs.LocalFileImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.filters.VFSItemFilter;
import org.olat.lms.course.archiver.ScoreAccountingHelper;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.ScormCourseNode;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.lms.scorm.ScormDirectoryHelper;
import org.olat.lms.scorm.server.servermodels.ScoDocument;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.logging.log4j.LoggerHelper;

public class ScormExportManager extends BasicManager {
    private static final String CMI_OBJECTIVES = "cmi.objectives.";
    private static final String CMI_INTERACTIONS = "cmi.interactions.";
    private static final String CMI_RAW_SCORE = "cmi.core.score.raw";
    private static final String CMI_LESSON_STATUS = "cmi.core.lesson_status";
    private static final String CMI_COMMENTS = "cmi.comments";
    private static final String CMI_TOTAL_TIME = "cmi.core.total_time";

    private static final String CMI_ID = "id";
    private static final String CMI_SCORE_RAW = "score.raw";
    private static final String CMI_SCORE_MIN = "score.min";
    private static final String CMI_SCORE_MAX = "score.max";
    private static final String CMI_RESULT = "result";
    private static final String CMI_STUDENT_RESPONSE = "student_response";
    private static final String CMI_CORRECT_RESPONSE = "correct_responses.";
    private static final String OBJECTIVES = "objectives.";
    private static final String CMI_COUNT = "_count";

    private static final Logger log = LoggerHelper.getLogger();

    private static final ScormExportManager instance = new ScormExportManager();

    private ScormExportManager() {
        //
    }

    public static ScormExportManager getInstance() {
        return instance;
    }

    /**
     * Export the results of a SCORM course
     * 
     * @param courseEnv
     * @param node
     * @param translator
     * @param exportDirPath
     * @param charset
     * @return the name of the file, if any created or empty string otherwise
     */
    public String exportResults(final CourseEnvironment courseEnv, final ScormCourseNode node, final Translator translator, final String exportDirPath,
            final String charset) {
        final ScormExportVisitor visitor = new ScormExportFormatter(translator);

        final boolean dataFound = visitScoDatas(courseEnv, node, visitor);

        if (dataFound) {
            final String content = visitor.toString();
            final String fileName = ExportUtil.createFileNameWithTimeStamp("SCORM_" + node.getShortTitle(), "xls");
            ExportUtil.writeContentToFile(fileName, content, new File(exportDirPath), charset);
            return fileName;
        }
        return "";
    }

    /**
     * Finds out if any results available.
     * 
     * @param courseEnv
     * @param node
     * @param translator
     * @return
     */
    public boolean hasResults(final CourseEnvironment courseEnv, final CourseNode node, final Translator translator) {
        final ScormExportVisitor visitor = new ScormExportFormatter(translator);
        final boolean dataFound = visitScoDatas(courseEnv, (ScormCourseNode) node, visitor);
        return dataFound;
    }

    /**
     * Visit the scos user's datamodel of a SCORM course. The users must be in a group.
     * 
     * @param courseEnv
     * @param node
     * @param visitor
     */
    public boolean visitScoDatas(final CourseEnvironment courseEnv, final ScormCourseNode node, final ScormExportVisitor visitor) {
        boolean dataFound = false;
        final Long courseId = courseEnv.getCourseResourceableId();
        final String scoDirectoryName = courseId.toString() + "-" + node.getIdent();

        final VFSContainer scormRoot = ScormDirectoryHelper.getScormRootFolder();
        final List<Identity> users = ScoreAccountingHelper.loadUsers(courseEnv);
        for (final Identity identity : users) {
            final String username = identity.getName();
            final VFSItem userFolder = scormRoot.resolve(username);
            if (userFolder instanceof VFSContainer) {
                final VFSItem scosFolder = ((VFSContainer) userFolder).resolve(scoDirectoryName);
                if (scosFolder instanceof VFSContainer) {
                    collectData(username, (VFSContainer) scosFolder, visitor);
                    dataFound = true;
                }
            }
        }
        return dataFound;
    }

    private void collectData(final String username, final VFSContainer scoFolder, final ScormExportVisitor visitor) {
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

                final String[][] scoModel = document.getScoModel();
                final ScoDatas parsedDatas = parseScoModel(file.getName(), username, scoModel);
                visitor.visit(parsedDatas);
            } catch (final Exception e) {
                log.error("Cannot load a SCO Datamodel", e);
            }
        }
    }

    /**
     * Parse the raw cmi datas in a java friendly object.
     * 
     * @param scoId
     * @param username
     * @param scoModel
     * @return
     */
    private ScoDatas parseScoModel(final String scoId, final String username, final String[][] scoModel) {
        final ScoDatas datas = new ScoDatas(scoId, username);

        for (final String[] line : scoModel) {
            String key = null;
            try {
                key = line[0];
                if (key == null) {
                    continue;
                }

                final String value = line[1];
                if (key.equals(CMI_RAW_SCORE)) {
                    datas.setRawScore(value);
                } else if (key.equals(CMI_LESSON_STATUS)) {
                    datas.setLessonStatus(value);
                } else if (key.equals(CMI_COMMENTS)) {
                    datas.setComments(value);
                } else if (key.equals(CMI_TOTAL_TIME)) {
                    datas.setTotalTime(value);
                } else if (key.startsWith(CMI_OBJECTIVES)) {
                    final String endStr = key.substring(CMI_OBJECTIVES.length());
                    final int nextPoint = endStr.indexOf('.');
                    if (nextPoint < 0) {
                        // cmi.objectives._count
                        continue;
                    }
                    final String interactionNr = endStr.substring(0, nextPoint);
                    final int nr = Integer.valueOf(interactionNr).intValue();
                    final ScoObjective objective = datas.getObjective(nr);

                    final String endKey = endStr.substring(nextPoint + 1);
                    if (CMI_ID.equals(endKey)) {
                        objective.setId(value);
                    }
                    if (CMI_SCORE_RAW.equals(endKey)) {
                        objective.setScoreRaw(value);
                    } else if (CMI_SCORE_MIN.equals(endKey)) {
                        objective.setScoreMin(value);
                    } else if (CMI_SCORE_MAX.equals(endKey)) {
                        objective.setScoreMax(value);
                    }
                } else if (key.startsWith(CMI_INTERACTIONS)) {
                    final String endStr = key.substring(CMI_INTERACTIONS.length());
                    final int nextPoint = endStr.indexOf('.');
                    if (nextPoint < 0) {
                        continue;
                    }

                    final String interactionNr = endStr.substring(0, nextPoint);
                    final int nr = Integer.valueOf(interactionNr).intValue();

                    final ScoInteraction interaction = datas.getInteraction(nr);

                    final String endKey = endStr.substring(nextPoint + 1);
                    if (CMI_ID.equals(endKey)) {
                        interaction.setInteractionId(value);
                    } else if (CMI_RESULT.equals(endKey)) {
                        interaction.setResult(value);
                    } else if (CMI_STUDENT_RESPONSE.equals(endKey)) {
                        interaction.setStudentResponse(value);
                    } else if (endKey.startsWith(CMI_CORRECT_RESPONSE)) {
                        interaction.setCorrectResponse(value);
                    } else if (endKey.indexOf(OBJECTIVES) >= 0 && endKey.indexOf(CMI_COUNT) < 0) {
                        interaction.getObjectiveIds().add(value);
                    }
                }
            } catch (final Exception ex) {
                log.debug("Error parse this cmi data: " + key);
            }
        }

        return datas;
    }

    public class XMLFilter implements VFSItemFilter {
        @Override
        public boolean accept(final VFSItem file) {
            final String name = file.getName();
            if (name.endsWith(".xml") && !(name.equals("reload-settings.xml"))) {
                return true;
            }
            return false;
        }
    }
}
