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
package org.olat.data.commons.fileutil;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * TODO: Class Description for ZipUtilTest
 * 
 * <P>
 * Initial Date: 29.07.2011 <br>
 * 
 * @author lavinia
 */
public class ZipUtilITCase {

    private File dirToBeZipped; // contains one single dir as child
    private File targetZipFile;
    private File unzipTargetDir;

    @Before
    public void setUp() {
        try {
            // have to make sure that java.io.tmpdir exists ...
            File tmpDir = new File(System.getProperty("java.io.tmpdir"));
            if (!tmpDir.exists()) {
                tmpDir.mkdirs();
            }

            dirToBeZipped = createTempDir();
            File subDir = new File(dirToBeZipped, "child_dir");
            subDir.mkdir();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private File createTempDir() throws IOException {
        File tmpDir = File.createTempFile("junit", "output");
        delete(tmpDir);
        tmpDir.mkdir();
        return tmpDir;
    }

    /**
     * creates and deletes file, just for purpose of getting an temporary file name.
     * 
     * @param prefix
     * @return
     * @throws IOException
     */
    private File getTempFileName(String prefix) throws IOException {
        File tempFile = File.createTempFile(prefix, "output");
        delete(tempFile);
        return tempFile;
    }

    @Test
    public void zipAndUnzip_dirWithOneChildDir() throws IOException {

        targetZipFile = getTempFileName("zip");
        assertTrue(ZipUtil.zipAll(dirToBeZipped, targetZipFile));

        unzipTargetDir = getTempFileName("unzip");

        assertTrue(ZipUtil.unzip(targetZipFile, unzipTargetDir));
        assertTrue(unzipTargetDir.listFiles().length == 1);

        if (File.separator.equals("/")) {
            assertTrue(unzipTargetDir.listFiles()[0].isDirectory());
            assertFalse(unzipTargetDir.listFiles()[0].isFile());
        } else {
            assertFalse(unzipTargetDir.listFiles()[0].isDirectory());
            assertTrue(unzipTargetDir.listFiles()[0].isFile());
        }
    }

    @After
    public void tearDown() {
        delete(targetZipFile);
        delete(dirToBeZipped);
        delete(unzipTargetDir);
    }

    private void delete(File file) {
        if (file == null)
            return;
        if (!file.exists())
            return;
        if (file.isDirectory()) {
            for (File c : file.listFiles()) {
                delete(c);
            }
        }
        file.delete();
    }
}
