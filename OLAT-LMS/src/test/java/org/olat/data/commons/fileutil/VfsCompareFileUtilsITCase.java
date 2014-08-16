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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.olat.data.commons.vfs.LocalFolderImpl;
import org.olat.data.commons.vfs.VFSItem;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * Initial Date: 26.09.2011 <br>
 * Test whether FileUtils.deleteDirsAndFiles can be replaced with LocalFolderImpl.delete method (should be same functionality to work with File system)
 * 
 * @author Branislav Balaz
 */
@ContextConfiguration(locations = { "classpath*:**/fileTestContext.xml" })
public class VfsCompareFileUtilsITCase extends AbstractJUnit4SpringContextTests {

    private final String rootFolder = "rootfolder" + System.currentTimeMillis();
    private final String rootFirstFolder = rootFolder + File.separator + "firstfolder";
    private final String rootFirstFirstFolder = rootFirstFolder + File.separator + "firstfirstfolder";
    private final String rootFirstSecondFolder = rootFirstFolder + File.separator + "firstsecondfolder";
    private final String rootSecondFolder = rootFolder + File.separator + "secondfolder";
    private final String filePrefix = "file";
    private final String fileSuffix = "tmp";
    private final String fileName = filePrefix + "." + fileSuffix;

    @Test
    public void deleteFolderVfsTest() throws IOException {
        File rootFolder = createFolderStructure();
        VFSItem folderItem = new LocalFolderImpl(rootFolder);
        folderItem.delete();
        assertTrue(isWholeFileStructureDeleted());

    }

    @Test
    public void deleteFolderFileUtilsTest() throws IOException {
        File rootFolder = createFolderStructure();
        FileUtils.deleteDirsAndFiles(rootFolder, true, true);
        assertTrue(isWholeFileStructureDeleted());
    }

    private File createFolderInstanceWithFile(String folderPath) throws IOException {

        File folderInstance = createFolderInstance(folderPath);
        createFileInstance(folderInstance);
        return folderInstance;

    }

    private void createFileInstance(File folderInstance) throws IOException {
        File fileInstance = new File(folderInstance, fileName);
        fileInstance.createNewFile();
        System.out.println(fileInstance.getAbsolutePath());
    }

    private File createFolderInstance(String folderPath) {
        File folderInstance = new File(folderPath);
        folderInstance.mkdir();
        return folderInstance;
    }

    private File createFolderStructure() throws IOException {

        File folderRoot = createFolderInstanceWithFile(rootFolder);
        createFolderInstanceWithFile(rootFirstFolder);
        createFolderInstanceWithFile(rootFirstFirstFolder);
        createFolderInstanceWithFile(rootFirstSecondFolder);
        createFolderInstanceWithFile(rootSecondFolder);
        return folderRoot;
    }

    private boolean isWholeFileStructureDeleted() {
        if (existsFile(rootFolder + File.separator + fileName))
            return false;
        if (existsFile(rootFirstFolder + File.separator + fileName))
            return false;
        if (existsFile(rootFirstFirstFolder + File.separator + fileName))
            return false;
        if (existsFile(rootFirstSecondFolder + File.separator + fileName))
            return false;
        if (existsFile(rootSecondFolder + File.separator + fileName))
            return false;
        if (existsFile(rootFolder))
            return false;
        if (existsFile(rootFirstFolder))
            return false;
        if (existsFile(rootFirstFirstFolder))
            return false;
        if (existsFile(rootFirstSecondFolder))
            return false;
        if (existsFile(rootSecondFolder))
            return false;
        return true;
    }

    private boolean existsFile(String path) {
        File file = new File(path);
        return file.exists();
    }

}
