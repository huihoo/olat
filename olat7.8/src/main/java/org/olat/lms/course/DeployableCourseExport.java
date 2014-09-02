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
package org.olat.lms.course;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * holds information how to deploy and download a zipped repo entry.
 * <P>
 * Initial Date: 21.12.2010 <br>
 * 
 * @author guido
 */
public class DeployableCourseExport {

    private String courseUrl;
    // default is open for all inkl. guests
    private int access = 4;
    private Float version;
    private String identifier;
    // default for a demo course is false
    private boolean helpCourse = false;
    private static final Logger log = LoggerHelper.getLogger();

    private File file;
    public static final String DOWNLOAD_FILE_PREFIX = "downloadedCourse";
    public static final String DOWNLOAD_FILE_SUFFIX = ".tmp";

    /**
     * [spring]
     */
    private DeployableCourseExport() {
        //
    }

    /**
     * Downloads the file from the net given the url
     * 
     * @return the file or null in case of error
     */
    public File getDeployableCourseZipFile() {
        if (file == null) {
            URL url;
            try {
                url = new URL(courseUrl);
            } catch (final MalformedURLException e) {
                log.error("Url is not valid: " + courseUrl);
                return null;
            }
            file = downloadZipFromUrl(url);
            return file;
        }
        return file;
    }

    public void setCourseUrl(final String courseUrl) {
        this.courseUrl = courseUrl;
    }

    public int getAccess() {
        return access;
    }

    public void setAccess(final int access) {
        this.access = access;
    }

    public boolean isHelpCourse() {
        return helpCourse;
    }

    public void setHelpCourse(final boolean helpCourse) {
        this.helpCourse = helpCourse;
    }

    private File downloadZipFromUrl(final URL url) {
        try {
            log.info("Downloading demo course file: " + url);
            final HttpURLConnection uc = (HttpURLConnection) url.openConnection();
            final int responseCode = uc.getResponseCode();
            final String contentType = uc.getContentType();
            final int contentLength = uc.getContentLength();
            if (responseCode != 200 || !contentType.startsWith("application/") || contentLength == -1) {
                if (responseCode != 200) {
                    log.warn("Server response was not successful code: " + responseCode + " with url: " + courseUrl);
                } else if (contentLength == -1) {
                    log.warn("File is empty!");
                } else if (!contentType.startsWith("application/")) {
                    log.warn("File is not a binary file! ContentType is: " + contentType + " from url:" + courseUrl);
                }
                return null;
            }
            final InputStream raw = uc.getInputStream();
            final InputStream in = new BufferedInputStream(raw);
            final byte[] data = new byte[contentLength];
            int bytesRead = 0;
            int offset = 0;
            System.out.print("downloading[");
            final int tenPercent = contentLength / 10;
            int tenPercentRead = 0;
            while (offset < contentLength) {
                bytesRead = in.read(data, offset, data.length - offset);
                if (bytesRead == -1) {
                    break;
                }
                offset += bytesRead;
                tenPercentRead += bytesRead;
                if (tenPercentRead > tenPercent) {
                    tenPercentRead = 0;
                    System.out.print("===========");
                }
            }
            System.out.print("=>]");
            System.out.println("");
            in.close();
            if (offset != contentLength) {
                throw new IOException("Only read " + offset + " bytes; Expected " + contentLength + " bytes");
            }

            /*
             * String filename = url.getFile().substring(url.getFile().lastIndexOf('/') + 1); filename = System.getProperty("java.io.tmpdir") + "/" + filename;
             */
            String filename = File.createTempFile(DOWNLOAD_FILE_PREFIX, DOWNLOAD_FILE_SUFFIX, new File(System.getProperty("java.io.tmpdir"))).getAbsolutePath();
            final FileOutputStream out = new FileOutputStream(filename);
            out.write(data);
            out.flush();
            out.close();
            return new File(filename);
        } catch (final Exception e) {
            log.error("Could not read file from url: " + courseUrl, e);
            return null;
        }

    }

    public Float getVersion() {
        return version;
    }

    public void setVersion(final Float version) {
        this.version = version;
    }

    public void setIdentifier(final String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

}
