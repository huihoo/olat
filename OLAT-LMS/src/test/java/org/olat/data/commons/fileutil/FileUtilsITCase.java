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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

/**
 * TODO: Class Description for FileUtilsTest
 * 
 * <P>
 * Initial Date: 29.06.2011 <br>
 * 
 * @author lavinia
 */
public class FileUtilsITCase {

    private String RELATIVE_PATH_1;
    private String ABSOLUTE_PATH_1;

    @Before
    public void setUp() {
        RELATIVE_PATH_1 = File.separator + "course" + File.separator + "83786236730883" + "/foldernodes/82817051271953/bla.pdf";
        try {
            File tmp = File.createTempFile("prefix", "suffix");
            ABSOLUTE_PATH_1 = tmp.getAbsolutePath();
            if (tmp.exists())
                tmp.delete();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void splitPathPlatformIndependent_relativePath() {
        String path = RELATIVE_PATH_1;
        String[] pathElements = FileUtils.splitPathPlatformIndependent(path);
        int i = 0;
        for (String pathComponent : pathElements) {
            // System.out.println(pathElements[i++]);
        }
        assertEquals(pathElements.length, 5);
    }

    @Test
    public void splitPathPlatformIndependent_absolutePath() {
        String path = ABSOLUTE_PATH_1;
        System.out.println("path: " + path);
        String[] pathElements = FileUtils.splitPathPlatformIndependent(path);
        if (pathElements.length == 0) {
            System.out.println("empty pathElements");
            assertTrue(path == null || path.trim().equals(""));
            return;
        }
        int i = 0;
        System.out.println("path elements: ");
        for (String pathComponent : pathElements) {
            System.out.println(pathElements[i++]);
        }
        String lastPathElem = pathElements[pathElements.length - 1];
        assertTrue(lastPathElem.startsWith("prefix"));
    }
}
