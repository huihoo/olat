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
package org.olat.system.support.mail.service;

import java.util.HashMap;
import java.util.Map;

/**
 * Extends <code>CommonMailTO</code> with a <code>templateProperties</code> map and a <code>templateLocation</code>.
 * 
 * Initial Date: 19.12.2011 <br>
 * 
 * @author guretzki
 */
public class TemplateMailTO extends CommonMailTO {

    Map<String, Object> templateProperties;
    String templateLocation;

    protected TemplateMailTO(String toMailAddress, String fromMailAddress, String subject, String templateLocation) {
        super(toMailAddress, fromMailAddress, subject);
        this.templateProperties = new HashMap<String, Object>();
        this.templateLocation = templateLocation;
    }

    public static TemplateMailTO getValidInstance(String toMailAddress, String fromMailAddress, String subject, String templateLocation) {
        TemplateMailTO mail = new TemplateMailTO(toMailAddress, fromMailAddress, subject, templateLocation);
        mail.validate();
        return mail;
    }

    public Map<String, Object> getTemplateProperties() {
        return templateProperties;
    }

    public String getTemplateLocation() {
        return templateLocation;
    }

    public void addTemplateProperty(String key, Object value) {
        templateProperties.put(key, value);
    }

}
