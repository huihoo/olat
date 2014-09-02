/**
 * This software is based on OLAT, www.olat.org
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
 * Copyright (c) JLS goodsolutions GmbH, Zurich, Switzerland. http://www.goodsolutions.ch <br>
 * All rights reserved.
 * <p>
 */
package org.olat.lms.commons.util;

import org.olat.data.commons.fileutil.FileUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * 
 * @author Christian Guretzki
 */
public class License {

    /**
     * @return The full OLAT licencse as a string
     */
    public static String getOlatLicense() {
        Resource license = new ClassPathResource("NOTICE.TXT");
        String licenseS = "";
        if (license.exists()) {
            licenseS = FileUtils.load(license, "UTF-8");
        }
        Resource copyLicense = new ClassPathResource("COPYING");
        String copyLicenseS = "";
        if (copyLicense.exists()) {
            copyLicenseS = FileUtils.load(copyLicense, "UTF-8");
        }
        return licenseS + "<br /><br />" + copyLicenseS;
    }

}
