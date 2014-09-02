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
package org.olat.lms.ims.qti.exporter;

/**
 * Initial Date: 05.10.2011 <br>
 * 
 * @author guretzki
 */
public class ExportFormatConfig {

    private String separatedBy;
    private String embeddedBy;
    private String escapedBy;
    private String carriageReturn;
    private String fileNameSuffix;
    private boolean tagless;

    public void setSeparator(String separatedBy) {
        this.separatedBy = convert2CtrlChars(separatedBy);
    }

    public void setEmbeddedBy(String embeddedBy) {
        this.embeddedBy = embeddedBy;
    }

    public void setEscapedBy(String escapedBy) {
        this.escapedBy = escapedBy;
    }

    public void setCarriageReturn(String carriageReturn) {
        this.carriageReturn = convert2CtrlChars(carriageReturn);
    }

    public void setFileNameSuffix(String fileNameSuffix) {
        this.fileNameSuffix = fileNameSuffix;
    }

    public void setTagless(boolean tagless) {
        this.tagless = tagless;
    }

    public String getSeparatedBy() {
        return separatedBy;
    }

    public void setSeparatedBy(String separatedBy) {
        this.separatedBy = separatedBy;
    }

    public String getEmbeddedBy() {
        return embeddedBy;
    }

    public String getEscapedBy() {
        return escapedBy;
    }

    public String getCarriageReturn() {
        return carriageReturn;
    }

    public String getFileNameSuffix() {
        return fileNameSuffix;
    }

    public boolean isTagless() {
        return tagless;
    }

    private String convert2CtrlChars(final String source) {
        if (source == null) {
            return null;
        }
        final StringBuilder sb = new StringBuilder(300);
        final int len = source.length();
        final char[] cs = source.toCharArray();
        for (int i = 0; i < len; i++) {
            final char c = cs[i];
            switch (c) {
            case '\\':
                // check on \\ first
                if (i < len - 1 && cs[i + 1] == 't') { // we have t as next char
                    sb.append("\t");
                    i++;
                } else if (i < len - 1 && cs[i + 1] == 'r') { // we have r as next char
                    sb.append("\r");
                    i++;
                } else if (i < len - 1 && cs[i + 1] == 'n') { // we have n as next char
                    sb.append("\n");
                    i++;
                } else {
                    sb.append("\\");
                }
                break;
            default:
                sb.append(c);
            }
        }
        return sb.toString();
    }

}
