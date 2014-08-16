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

package org.olat.data.course.nodes.projectbroker;

/**
 * @author guretzki
 */

public class CustomField {

    private String name;
    private String value;
    private boolean tableViewEnabled;

    public CustomField() {
    }

    public CustomField(final String name, final String value, final boolean tableViewEnabled) {
        super();
        this.name = name;
        this.value = value;
        this.tableViewEnabled = tableViewEnabled;
    }

    public CustomField(final String name, final String value) {
        super();
        this.name = name;
        this.value = value;
        this.tableViewEnabled = false;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public boolean isTableViewEnabled() {
        return tableViewEnabled;
    }

    public void setTableViewEnabled(final boolean tableViewEnabled) {
        this.tableViewEnabled = tableViewEnabled;
    }

}
