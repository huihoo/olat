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
package org.olat.connectors.campus;

import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.course.campus.DaoManager;
import org.olat.data.course.campus.ImportStatistic;
import org.olat.data.course.campus.ImportStatisticDao;
import org.olat.data.course.campus.SkipItem;
import org.olat.data.course.campus.SkipItemDao;
import org.olat.lms.core.course.campus.impl.metric.CampusNotifier;
import org.olat.lms.core.course.campus.impl.metric.CampusStatistics;
import org.olat.system.commons.configuration.PropertyLocator;
import org.olat.system.commons.configuration.SystemPropertiesService;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class is an implementation listener that will be notified in the case of:
 * <ul>
 * <li>skipped items while reading, processing and writing an item.
 * <li>before, after and in case of any exception thrown while writing a list of items.
 * <li>before and after the execution of the step's processing
 * </ul>
 * <br>
 * 
 * Initial Date: 11.06.2012 <br>
 * 
 * @author aabouc
 */

public class CampusInterceptor<T, S> implements StepExecutionListener, ItemWriteListener<S>, SkipListener<T, S>, ChunkListener {

    private static final Logger LOG = LoggerHelper.getLogger();

    @Autowired
    protected ImportStatisticDao statisticDao;
    @Autowired
    protected SkipItemDao skipItemDao;
    @Autowired
    private DaoManager daoManager;
    @Autowired
    private CampusNotifier campusNotifier;

    private StepExecution stepExecution;

    private int fixedNumberOfFilesToBeExported;

    private int chunkCount;

    private long chunkStartTime;

    /**
     * Processes some cleanups depending on the appropriate step.
     * 
     * @param se
     *            the StepExecution
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void beforeStep(StepExecution se) {
        LOG.info(se);

        setStepExecution(se);

        // chunk count and duration is beeing logged for sync step since this may be slow and potentially break timeout
        if (CampusProcessStep.CAMPUSSYNCHRONISATION.name().equalsIgnoreCase(se.getStepName())) {
            chunkCount = 0;
        }

        if (CampusProcessStep.IMPORT_TEXTS.name().equalsIgnoreCase(se.getStepName())) {
            daoManager.deleteAllTexts();
        }
        // DISABLED FOR NOW
        // if (CampusImportStep.IMPORT_EVENTS.name().equalsIgnoreCase(se.getStepName())) {
        // daoManager.deleteAllEvents();
        // }
    }

    /**
     * Generates an appropriate statistic of the processed, <br>
     * delegates the cleanup and the metric notification.
     * 
     * @param se
     *            the StepExecution
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ExitStatus afterStep(StepExecution se) {
        LOG.info(se);

        statisticDao.save(createImportStatistic(se));

        if (CampusProcessStep.IMPORT_CONTROLFILE.name().equalsIgnoreCase(se.getStepName())) {
            if (se.getWriteCount() != getFixedNumberOfFilesToBeExported()) {
                // if (se.getReadCount() != getFixedNumberOfFilesToBeExported() || se.getWriteCount() != getFixedNumberOfFilesToBeExported()) {
                notifyMetrics(se);
                return ExitStatus.FAILED;
            }
        }

        removeOldDataIfExist(se);
        notifyMetrics(se);

        return null;
    }

    /**
     * Prepares the metric data and delegates the actual notification to the {@link CampusNotifier}.
     * 
     * @param se
     *            the StepExecution
     */
    private void notifyMetrics(StepExecution se) {
        if (CampusProcessStep.IMPORT_CONTROLFILE.name().equalsIgnoreCase(se.getStepName())) {
            campusNotifier.notifyStartOfImportProcess();
            CampusStatistics.EXPORT_STATUS exportStatus = CampusStatistics.EXPORT_STATUS.OK;

            if (se.getReadCount() != getFixedNumberOfFilesToBeExported() && se.getReadCount() != 2 * getFixedNumberOfFilesToBeExported()) {
                // THE CASE THAT THE EXPORT FILE (CONTROL FILE) HASN'T BEEN CREATED YET
                if (se.getReadCount() == 0) {
                    exportStatus = CampusStatistics.EXPORT_STATUS.NO_EXPORT;
                }
                // THE CASE OF EXPORTING LESS THAN THE EXPECTED FILES (LESS THAN 8(ONLY CURRENT) OR LESS THAN 16 (CURRENT AND NEXT)
                else {
                    exportStatus = CampusStatistics.EXPORT_STATUS.INCOMPLETE_EXPORT;
                }
            }
            // THE CASE OF EXPORTING THE OLD FILES
            if (se.getWriteCount() != getFixedNumberOfFilesToBeExported()) {
                exportStatus = CampusStatistics.EXPORT_STATUS.NO_EXPORT;
            }

            campusNotifier.notifyExportStatus(exportStatus);
        }

        campusNotifier.notifyStepExecution(se);

    }

    /**
     * Delegates the actual deletion of old data to the {@link DaoManager} in the case of a successful batch processing.
     * 
     * @param se
     *            the StepExecution
     */
    private void removeOldDataIfExist(StepExecution se) {
        if (!BatchStatus.COMPLETED.equals(se.getStatus())) {
            return;
        }

        if (CampusProcessStep.IMPORT_ORGS.name().equalsIgnoreCase(se.getStepName())) {
            List<Long> orgsToBeRemoved = daoManager.getAllOrgsToBeDeleted(se.getStartTime());
            LOG.info("ORGS TO BE REMOVED [" + orgsToBeRemoved.size() + "]");
            if (!orgsToBeRemoved.isEmpty()) {
                daoManager.deleteByOrgIds(orgsToBeRemoved);
            }
            return;
        }

        if (CampusProcessStep.IMPORT_STUDENTS.name().equalsIgnoreCase(se.getStepName())) {
            List<Long> studentsToBeRemoved = daoManager.getAllStudentsToBeDeleted(se.getStartTime());
            LOG.info("STUDENTS TO BE REMOVED [" + studentsToBeRemoved.size() + "]");
            if (!studentsToBeRemoved.isEmpty()) {
                daoManager.deleteStudentsAndBookingsByStudentIds(studentsToBeRemoved);
            }
            return;
        }

        if (CampusProcessStep.IMPORT_LECTURERS.name().equalsIgnoreCase(se.getStepName())) {
            List<Long> lecturersToBeRemoved = daoManager.getAllLecturersToBeDeleted(se.getStartTime());
            LOG.info("LECTURERS TO BE REMOVED [" + lecturersToBeRemoved.size() + "]");
            if (!lecturersToBeRemoved.isEmpty()) {
                daoManager.deleteLecturersAndBookingsByLecturerIds(lecturersToBeRemoved);
            }
            return;
        }

        if (CampusProcessStep.IMPORT_COURSES.name().equalsIgnoreCase(se.getStepName())) {
            List<Long> coursesToBeRemoved = daoManager.getAllCoursesToBeDeleted(se.getStartTime());
            LOG.info("COURSES TO BE REMOVED[" + coursesToBeRemoved.size() + "]");
            if (!coursesToBeRemoved.isEmpty()) {
                daoManager.deleteCoursesAndBookingsByCourseIds(coursesToBeRemoved);
            }
            return;
        }

        if (CampusProcessStep.IMPORT_LECTURERS_COURSES.name().equalsIgnoreCase(se.getStepName())) {
            int stornos = daoManager.deleteAllNotUpdatedLCBooking(se.getStartTime());
            LOG.info("STORNOS(LECTURER_COURSE): " + stornos);
            return;
        }
        if (CampusProcessStep.IMPORT_STUDENTS_COURSES.name().equalsIgnoreCase(se.getStepName())) {
            int stornos = daoManager.deleteAllNotUpdatedSCBooking(se.getStartTime());
            LOG.info("STORNOS(STUDENT_COURSE): " + stornos);
            return;
        }

    }

    /**
     * @param items
     *            the list of items to be written in the database.
     */
    @Override
    public void afterWrite(List<? extends S> items) {
        LOG.debug("afterWrite: " + items);
    }

    /**
     * @param items
     *            the list of items to be written in the database.
     */
    @Override
    public void beforeWrite(List<? extends S> items) {
        LOG.debug("beforeWrite: " + items);
    }

    /**
     * @param items
     *            the list of items to be written in the database.
     */
    @Override
    public void onWriteError(Exception ex, List<? extends S> items) {
    }

    /**
     * Writes the skipped item with the caused failure while processing in the database.
     * 
     * @param item
     *            the failed item
     * @param ex
     *            the cause of failure
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onSkipInProcess(T item, Throwable ex) {
        LOG.debug("onSkipInWrite: " + item);
        skipItemDao.save(createSkipItem("PROCESS", item.toString(), ex.getMessage()));
    }

    /**
     * Writes the caused failure while reading in the database.
     * 
     * @param ex
     *            the cause of failure
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onSkipInRead(Throwable ex) {
        LOG.debug("onSkipInRead: ");
        skipItemDao.save(createSkipItem("READ", null, ex.getMessage()));
    }

    /**
     * Writes the skipped item with the caused failure while writing in the database.
     * 
     * @param item
     *            the failed item
     * @param ex
     *            the cause of failure
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onSkipInWrite(S item, Throwable ex) {
        LOG.debug("onSkipInWrite: " + item);
        skipItemDao.save(createSkipItem("WRITE", item.toString(), ex.getMessage()));
    }

    /**
     * Generates the statistic based on the StepExecution.
     * 
     * @param se
     *            the StepExecution
     */
    private ImportStatistic createImportStatistic(StepExecution se) {
        ImportStatistic statistic = new ImportStatistic();
        statistic.setStepId(se.getId());
        statistic.setStepName(se.getStepName());
        statistic.setStatus(se.getStatus().toString());
        statistic.setReadCount(se.getReadCount());
        statistic.setReadSkipCount(se.getReadSkipCount());
        statistic.setWriteCount(se.getWriteCount());
        statistic.setWriteSkipCount(se.getWriteSkipCount());
        statistic.setProcessSkipCount(se.getProcessSkipCount());
        statistic.setCommitCount(se.getCommitCount());
        statistic.setRollbackCount(se.getRollbackCount());
        statistic.setStartTime(se.getStartTime());
        statistic.setEndTime(se.getLastUpdated());
        return statistic;
    }

    /**
     * Creates a SkipItem based on the given parameters.
     * 
     * @param type
     *            the kind of subprocess (READ, PROCESS, WRITE)
     * @param item
     *            the name of the item be skipped
     * @param msg
     *            the description of the caused failure
     * 
     */
    private SkipItem createSkipItem(String type, String item, String msg) {
        SkipItem skipItem = new SkipItem();
        skipItem.setType(type);
        skipItem.setItem(item);
        skipItem.setMsg(msg);
        skipItem.setJobExecutionId(getStepExecution().getJobExecutionId());
        skipItem.setJobName(getStepExecution().getJobExecution().getJobInstance().getJobName());
        skipItem.setStepExecutionId(getStepExecution().getId());
        skipItem.setStepName(getStepExecution().getStepName());
        skipItem.setStepStartTime(getStepExecution().getStartTime());
        return skipItem;
    }

    /**
     * Sets the StepExecution.
     * 
     * @param stepExecution
     *            the StepExecution
     */
    private void setStepExecution(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    /**
     * Gets the StepExecution
     */
    private StepExecution getStepExecution() {
        return stepExecution;
    }

    /**
     * Gets the fixedNumberOfFilesToBeExported.
     */
    public int getFixedNumberOfFilesToBeExported() {
        return fixedNumberOfFilesToBeExported;
    }

    /**
     * Sets the fixedNumberOfFilesToBeExported.
     * 
     * @param fixedNumberOfFilesToBeExported
     *            the fixedNumberOfFilesToBeExported
     */
    public void setFixedNumberOfFilesToBeExported(int fixedNumberOfFilesToBeExported) {
        this.fixedNumberOfFilesToBeExported = fixedNumberOfFilesToBeExported;
    }

    @Override
    public void beforeChunk() {
        // chunk count and duration is beeing logged for sync step since this may be slow and potentially break timeout
        if (CampusProcessStep.CAMPUSSYNCHRONISATION.name().equalsIgnoreCase(getStepExecution().getStepName())) {
            chunkStartTime = System.currentTimeMillis();
        }
    }

    @Override
    public void afterChunk() {
        // chunk count and duration is beeing logged for sync step since this may be slow and potentially break timeout
        final int timeout = CoreSpringFactory.getBean(SystemPropertiesService.class).getIntProperty(PropertyLocator.DB_HIBERNATE_C3P0_UNRETURNEDCONNECTIONTIMEOUT);
        if (CampusProcessStep.CAMPUSSYNCHRONISATION.name().equalsIgnoreCase(getStepExecution().getStepName())) {
            chunkCount++;
            long chunkProcessingDuration = System.currentTimeMillis() - chunkStartTime;
            if (((float) (chunkProcessingDuration / 1000)) / timeout > 0.9) {
                LOG.warn("Chunk no "
                        + chunkCount
                        + " for campus synchronisation took "
                        + chunkProcessingDuration
                        + " ms which is more than 90% of configured database connection pool timeout of "
                        + timeout
                        + " sec. Please consider to take action in order to avoid a timeout (increase parameter 'db.hibernate.c3p0.unreturnedConnectionTimeout' or decrease chunk size).");
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Chunk no " + chunkCount + " for campus synchronisation took " + chunkProcessingDuration + " ms (timeout is " + timeout + " s).");
                }
            }
        }
    }

}
