package org.olat.lms.core.course.campus.impl.syncer;

import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.course.campus.DaoManager;
import org.olat.lms.core.course.campus.CampusCourseImportTO;
import org.olat.lms.core.course.campus.service.CampusCourseCoreService;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@link Tasklet} for creating all campus courses automatically
 * 
 * Initial Date: 31.10.2012 <br>
 * 
 * @author aabouc
 */
@Component("campusCourseCreatorTasklet")
public class CampusCourseCreatorTasklet implements Tasklet {
    private static final Logger LOG = LoggerHelper.getLogger();
    @Autowired
    DaoManager daoManager;
    @Autowired
    private CampusCourseCoreService campusCourseCoreService;

    @Override
    public RepeatStatus execute(StepContribution arg0, ChunkContext arg1) throws Exception {
        LOG.info("Methode execute started...");
        createAllCampusCoursesFromTemplate();
        LOG.info("Methode execute finished.");
        return RepeatStatus.FINISHED;
    }

    @SuppressWarnings("deprecation")
    public void createAllCampusCoursesFromTemplate() {
        List<Long> courseIds = daoManager.getAllNotCreatedSapCourcesIds();
        for (Long courseId : courseIds) {
            CampusCourseImportTO campusCourseImportData = daoManager.getSapCampusCourse(courseId);
            if (campusCourseImportData.isOlatResourceableIdUndefined()) {
                if (!campusCourseImportData.getLecturers().isEmpty()) {
                    try {
                        Identity creator = campusCourseImportData.getLecturers().get(0);
                        if (creator != null) {
                            campusCourseCoreService.createCampusCourse(null, courseId, creator, campusCourseImportData);
                        }
                    } catch (Exception ex) {
                        LOG.error(ex.getMessage());
                    } finally {
                        DBFactory.getInstance(false).intermediateCommit();
                    }
                }
            }
        }
    }

}
