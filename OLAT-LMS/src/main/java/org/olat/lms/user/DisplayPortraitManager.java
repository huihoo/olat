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

package org.olat.lms.user;

import java.io.File;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.lms.commons.mediaresource.FileMediaResource;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description: <br>
 * TODO: alex Class Description
 * <P>
 * Initial Date: Sept 08, 2005 <br>
 * 
 * @author Alexander Schneider
 */
public class DisplayPortraitManager extends BasicManager implements UserDataDeletable {

    private static final Logger log = LoggerHelper.getLogger();

    private static DisplayPortraitManager singleton;

    public static final String PORTRAIT_BIG_FILENAME = "portrait_big.jpg";
    public static final String PORTRAIT_SMALL_FILENAME = "portrait_small.jpg";
    // The following class names refer to CSS class names in olat.css
    public static final String DUMMY_BIG_CSS_CLASS = "o_portrait_dummy";
    public static final String DUMMY_SMALL_CSS_CLASS = "o_portrait_dummy_small";
    public static final String DUMMY_FEMALE_BIG_CSS_CLASS = "o_portrait_dummy_female_big";
    public static final String DUMMY_FEMALE_SMALL_CSS_CLASS = "o_portrait_dummy_female_small";
    public static final String DUMMY_MALE_BIG_CSS_CLASS = "o_portrait_dummy_male_big";
    public static final String DUMMY_MALE_SMALL_CSS_CLASS = "o_portrait_dummy_male_small";

    // If you change the following widths, don't forget to change them in olat.css as well.
    public static final int WIDTH_PORTRAIT_BIG = 100; // 4-8 kbytes (jpeg)
    public static final int WIDTH_PORTRAIT_SMALL = 50; // 2-4

    /**
     * [spring]
     */
    private DisplayPortraitManager() {
        singleton = this;
    }

    /**
     * Singleton pattern
     * 
     * @return instance
     */
    public static DisplayPortraitManager getInstance() {
        return singleton;
    }

    /**
     * @param identity
     * @return imageResource portrait
     */
    public MediaResource getPortrait(final Identity identity, final String portraitName) {
        MediaResource imageResource = null;
        final File imgFile = new File(getPortraitDir(identity), portraitName);
        if (imgFile.exists()) {
            imageResource = new FileMediaResource(imgFile);
        }
        return imageResource;
    }

    /**
     * @param identity
     * @return imageResource portrait
     */
    public MediaResource getPortrait(final File uploadDir, final String portraitName) {
        MediaResource imageResource = null;
        final File imgFile = new File(uploadDir, portraitName);
        if (imgFile.exists()) {
            imageResource = new FileMediaResource(imgFile);
        }
        return imageResource;
    }

    /**
     * @param identity
     * @return
     */
    public File getPortraitDir(final Identity identity) {
        final String portraitPath = FolderConfig.getCanonicalRoot() + FolderConfig.getUserHomePage(identity.getName()) + "/portrait";
        final File portraitDir = new File(portraitPath);
        portraitDir.mkdirs();
        return portraitDir;
    }

    /**
     * Delete home-page config-file of a certain user.
     * 
     */
    @Override
    public void deleteUserData(final Identity identity, final String newDeletedUserName) {
        FileUtils.deleteDirsAndFiles(getPortraitDir(identity), true, true);
        log.debug("Homepage-config file deleted for identity=" + identity);
    }

}
