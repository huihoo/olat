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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.lms.commons.fileresource;

import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;
import java.util.Set;

/**
 * Description:<br>
 * Initial Date: Aug 25, 2004 <br>
 * 
 * @author pellmont
 * @author oliver.buehler@agility-informatik.ch
 *         <p>
 */
public class SuffixFilter implements FileFilter {

    private final Set<String> suffixes;

    /**
     * @param suffixes
     */
    public SuffixFilter(final String... suffixes) {
        this.suffixes = new HashSet<String>(suffixes.length);
        for (final String suffix : suffixes) {
            this.suffixes.add(suffix);
        }
    }

    @Override
    public boolean accept(File file) {
        String name = file.getName().toLowerCase();
        if (file.isDirectory()) {
            return false;
        }
        int idx = name.lastIndexOf('.');
        if (idx >= 0) {
            return suffixes.contains(name.substring(idx + 1));
        }
        return suffixes.contains(name);
    }
}
