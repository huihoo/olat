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
package org.olat.system.commons.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * saves and loades properties to a certain file location
 * 
 * <P>
 * Initial Date: 30.05.2011 <br>
 * 
 * @author guido
 */
public class SystemPropertiesPersister {

    private static final Logger log = LoggerHelper.getLogger();

    private Properties props = new Properties();
    private File file;
    private boolean loaded;

    /**
	 * 
	 */
    protected SystemPropertiesPersister(File file) {
        this.file = file;
    }

    /**
     * Load the persisted properties from disk.
     */
    protected Properties loadProperties() {
        try {
            if (file.exists()) {
                FileInputStream is;
                is = new FileInputStream(file);
                props.load(is);
                is.close();
                loaded = true;
                return props;
            } else {
                File directory = file.getParentFile();
                if (!directory.exists())
                    directory.mkdirs();
            }
        } catch (FileNotFoundException e) {
            log.error("Could not load config file from path::" + file.getAbsolutePath(), e);
        } catch (IOException e) {
            log.error("Could not load config file from path::" + file.getAbsolutePath(), e);
        }
        loaded = true;
        // return empty props
        return new Properties();
    }

    /**
     * 
     * @param props
     * @param file
     */
    private void saveProperties(Properties props) {
        if (loaded) {
            OutputStream fileStream = null;
            try {
                fileStream = new FileOutputStream(file);
                props.store(fileStream, null);
                fileStream.flush();
                fileStream.close();
            } catch (IOException e) {
                log.error("Could not write config file from path::" + file.getAbsolutePath(), e);
            } finally {
                try {
                    if (fileStream != null)
                        fileStream.close();
                } catch (IOException e) {
                    log.error("Could not close stream after storing config to file::" + file.getAbsolutePath(), e);
                }
            }
        } else {
            throw new AssertException("You have to load properties first!");
        }
    }

    /**
     * @param key
     * @param value
     */
    public void saveProperty(String key, String value) {
        if (loaded) {
            props.setProperty(key, value);
            saveProperties(props);
        } else {
            throw new AssertException("You have to load properties first!");
        }

    }

}
