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

package org.olat.lms.user.propertyhandler;

import java.util.Locale;

import org.olat.data.user.User;
import org.olat.data.user.UserImpl;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.components.table.ColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.system.commons.OutputEscapeType;
import org.olat.system.commons.StringHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Desciption:
 * <p>
 * The user field represents an attribute of a known OLAT user.
 * <p>
 * 
 * @author Carsten Weisse, Florian Gnägi
 */
public abstract class AbstractUserPropertyHandler implements UserPropertyHandler {

    private String name;
    private String group;
    private boolean deletable = true; // default

    /**
	 */
    @Override
    public String getUserProperty(final User user, final Locale locale) {
        return getInternalValue(user);
    }

    /**
	 */
    @Override
    public String getUserPropertyAsHTML(final User user, final Locale locale) {
        return StringHelper.escapeHtml(getUserProperty(user, locale));
    }

    /**
	 */
    @Override
    public void setUserProperty(final User user, final String value) {
        setInternalValue(user, value);
    }

    /**
	 */
    @Override
    public String getName() {
        return this.name;
    }

    /**
	 */
    @Override
    public String getGroup() {
        return this.group;
    }

    /**
	 */
    @Override
    public String i18nFormElementLabelKey() {
        return "form.name." + getName();
    }

    /**
	 */
    @Override
    public String i18nFormElementGroupKey() {
        return "form.group." + getGroup();
    }

    /**
	 */
    @Override
    public String i18nColumnDescriptorLabelKey() {
        return "table.name." + getName();
    }

    /**
	 */
    @Override
    public ColumnDescriptor getColumnDescriptor(final int position, final String action, final Locale locale) {
        return new DefaultColumnDescriptor(i18nColumnDescriptorLabelKey(), position, action, locale, OutputEscapeType.HTML);
    }

    /**
	 */
    @Override
    public boolean isDeletable() {
        return deletable;
    }

    /**
     * @return The non-i18-ified raw value from the database
     */
    protected String getInternalValue(final User user) {
        if (user != null) {
            return ((UserImpl) user).getProperties().get(name);
        }
        return null;
    }

    /**
     * @param value
     *            The raw value in a 18n independent form
     */
    protected void setInternalValue(final User user, final String value) {
        if (user != null) {
            // remove fields with null or empty value from o_userfield table (hibernate)
            // sparse data storage
            if (value == null || value.length() == 0) {
                ((UserImpl) user).getProperties().remove(name);
            } else {
                ((UserImpl) user).getProperties().put(name, value);
            }
        }
    }

    /**
     * Returns the user field database key, name and value .
     * 
     * @return String internal user field info
     */
    @Override
    public String toString() {
        final String quickinfo = "AbstractUserPropertyHandler(" + this.getClass().getName() + ")[" + getName() + "]";
        return quickinfo + "," + super.toString();
    }

    /**
     * Spring setter
     * 
     * @param group
     */
    public void setGroup(final String group) {
        this.group = group;
    }

    /**
     * Spring setter
     * 
     * @param isDeletable
     */
    public void setDeletable(final boolean deletable) {
        this.deletable = deletable;
    }

    /**
     * Spring setter
     * 
     * @param name
     */
    public void setName(final String name) {
        this.name = name;
    }

    protected UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
