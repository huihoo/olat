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

package org.olat.lms.commons.i18n;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.olat.data.commons.fileutil.FileUtils;

/**
 * Description:<br>
 * 
 * @author Felix Jost
 */
public class CheckTranslationKeys {
    private static Map fileToProp = new HashMap();
    private static Map fileToCont = new HashMap();

    /**
     * @param args
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {
        final String path = "c:/workspace/olat3/webapp/";
        final File basedir = new File(path);
        final List res = new ArrayList();
        buildList(res, basedir);
        for (final Iterator iter = res.iterator(); iter.hasNext();) {
            final File file = (File) iter.next();
            final String name = file.getName();
            if (name.startsWith("LocalStrings") && name.endsWith(".properties")) {
                //
                final Properties p = new Properties();
                p.load(new FileInputStream(file));
                fileToProp.put(file, p);
                System.out.println("read prop " + file.getAbsolutePath());
            } else if (name.toLowerCase().endsWith(".html") || name.toLowerCase().endsWith(".java")) {
                final ByteArrayOutputStream bos = new ByteArrayOutputStream();
                FileUtils.copy(new FileInputStream(file), bos);
                final String cont = bos.toString();
                fileToCont.put(file, cont);
                System.out.println("read java/html " + file.getAbsolutePath());
            }
        }
        // all in RAM now...
        // check
        findInHTMLorJava("sdfsdfsdfaaaaa" + "aaaaaaaaaaaafsdf");

        final List dispList = new ArrayList();
        for (final Iterator iter = fileToProp.keySet().iterator(); iter.hasNext();) {
            final File fil = (File) iter.next();
            final Properties p = (Properties) fileToProp.get(fil);
            for (final Iterator iterator = p.keySet().iterator(); iterator.hasNext();) {
                final String key = (String) iterator.next();
                final boolean ok = findInHTMLorJava(key);
                final String value = p.getProperty(key);
                if (!ok) {
                    final String msg = fil.getAbsolutePath().substring(path.length()) + " unused key " + key + "=" + value;
                    dispList.add(msg);
                }
            }
        }
        Collections.sort(dispList);
        for (final Iterator iter = dispList.iterator(); iter.hasNext();) {
            final String out = (String) iter.next();
            System.out.println(out);
        }

    }

    /**
     * @param key
     * @return True if key found.
     */
    private static boolean findInHTMLorJava(final String key) {
        final String search = "\"" + key + "\"";
        for (final Iterator iter = fileToCont.keySet().iterator(); iter.hasNext();) {
            final File fil = (File) iter.next();
            final String cont = (String) fileToCont.get(fil);
            if (cont.indexOf(search) != -1) {
                return true;
            }
        }
        return false;
    }

    private static void buildList(final List fileList, final File cur) {
        if (cur.isDirectory()) {
            final File[] children = cur.listFiles();
            for (int i = 0; i < children.length; i++) {
                final File curChd = children[i];
                buildList(fileList, curChd);
            }
        } else { // regular file
            fileList.add(cur);
        }
    }
}
