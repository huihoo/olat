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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * an olatProperty object hold all information whether is has been overwritten or still in its default state. You also can access usefull stuff like available default
 * values and comment if avaliable.
 * 
 * The overwrite chain is: olat.properties get overwritten by olat.local.properties gets overwritten by maven.build.properties gets overwritten by
 * olatdata/system/configuration/GUIManaged.propertes
 * 
 * <P>
 * Initial Date: 26.05.2011 <br>
 * 
 * @author guido
 */
public class OLATProperty implements Comparable<OLATProperty> {

    String key;
    String defaultValue;
    String value;
    String comment;

    int overwriteCount;
    boolean hasComment;
    private List<String> availableValues = new ArrayList<String>(3);
    private List<String> overwriteValues = new ArrayList<String>(3);

    protected OLATProperty(String key, String value) {
        this.key = key;
        this.defaultValue = value;
    }

    public boolean isOverwritten() {
        return (overwriteValues.size() > 0);
    }

    public List<String> getOverwriteValues() {
        return overwriteValues;
    }

    public void setOverwriteValue(String overwriteValue) {
        this.overwriteValues.add(overwriteValue);
        this.value = overwriteValue;
    }

    public boolean hasComment() {
        return hasComment;
    }

    public void setComment(String comment) {
        this.hasComment = true;
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public List<String> getAvailableValues() {
        return availableValues;
    }

    /**
     * value delimiter is a comma ","
     * 
     * @param availableValuesDelimited
     */
    public void setAvailableValues(String availableValuesDelimited) {
        StringTokenizer tokens = new StringTokenizer(availableValuesDelimited, ",");

        while (tokens.hasMoreElements()) {
            availableValues.add(tokens.nextToken());
        }
    }

    public String getKey() {
        return key;
    }

    /**
     * @return the last value in the overwrite chain. The one the user set with the in the GUI
     */
    public String getValue() {
        String ret = defaultValue;
        for (String elem : overwriteValues) {
            ret = elem;
        }
        return ret;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public int compareTo(OLATProperty prop) {
        return this.getKey().compareTo(prop.getKey());
    }

}
