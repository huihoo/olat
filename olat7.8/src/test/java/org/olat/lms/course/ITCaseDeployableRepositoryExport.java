package org.olat.lms.course;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * Description:<br>
 * Testing the url file download
 * <P>
 * Initial Date: 22.12.2010 <br>
 * 
 * @author guido
 */
@ContextConfiguration(locations = { "classpath:org/olat/lms/course/_spring/textContextDeployableRepoExport.xml" })
public class ITCaseDeployableRepositoryExport extends AbstractJUnit4SpringContextTests {

    @Ignore
    @Test
    public void testZipDownloadNormalCase() {
        final DeployableCourseExport bean = (DeployableCourseExport) applicationContext.getBean("normalzip");
        assertNotNull(bean);
        assertFalse(bean.isHelpCourse());
        assertEquals(bean.getAccess(), 4);
        assertEquals(bean.getVersion(), Float.valueOf(1));

        final File file = bean.getDeployableCourseZipFile();
        assertTrue(file.getName().matches(DeployableCourseExport.DOWNLOAD_FILE_PREFIX + ".*" + DeployableCourseExport.DOWNLOAD_FILE_SUFFIX));
        assertNotNull(file);
        assertTrue(file.exists());
        file.delete();
    }

    @Test
    public void testZipDownloadBadUrl() {
        final DeployableCourseExport bean = (DeployableCourseExport) applicationContext.getBean("badurl");
        assertNotNull(bean);
        assertNull(bean.getDeployableCourseZipFile());
    }

    @Test
    public void testZipDownloadTextFile() {
        final DeployableCourseExport bean = (DeployableCourseExport) applicationContext.getBean("textfile");
        assertNotNull(bean);
        assertNull(bean.getDeployableCourseZipFile());
    }

}
