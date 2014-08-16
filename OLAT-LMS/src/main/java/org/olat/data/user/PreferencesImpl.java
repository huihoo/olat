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

package org.olat.data.user;

import java.util.Locale;

/**
 * Desciption: Implementation of the user preferences. When creating a new preferences object all attributes are set to null! One needs to populate the object with the
 * setter methods. This is a component object of a user.
 * 
 * @author guido
 */
public class PreferencesImpl implements Preferences {
    private String language;
    private String fontsize;
    private String notificationInterval;
    boolean informSessionTimeout;
    private boolean presenceMessagesPublic;

    /**
     * Default constructor.
     */
    public PreferencesImpl() {
        super();
    }

    /**
     * Get users language settings
     * 
     * @return Users language
     */
    @Override
    public String getLanguage() {
        return this.language;
    }

    /**
     * Set users language settings
     * 
     * @param l
     *            new language
     */
    @Override
    public void setLanguage(final String language) {
        this.language = language;
    }

    /**
     * Set users language settings
     * 
     * @param l
     *            new language
     */
    @Override
    public void setLanguage(Locale locale) {
        setLanguage(locale.toString());
    }

    /**
     * Get users fontsize settings
     * 
     * @return Users fontsize
     */
    @Override
    public String getFontsize() {
        if (this.fontsize == null || this.fontsize.equals("")) {
            fontsize = "100"; // 100% is default
        }
        return this.fontsize;
    }

    /**
     * Set users fontsize settings
     * 
     * @param f
     *            new fontsize
     */
    @Override
    public void setFontsize(final String f) {
        // since OLAT 6 the font size is not text but a number. It is the relative
        // size to the default size
        try {
            Integer.parseInt(f);
            this.fontsize = f;
        } catch (final NumberFormatException e) {
            this.fontsize = "100"; // default value
        }
    }

    /**
     * @param notificationInterval
     *            The notificationInterval to set.
     */
    @Override
    public void setNotificationInterval(final String notificationInterval) {
        this.notificationInterval = notificationInterval;
    }

    /**
     * @return Returns the notificationInterval.
     */
    @Override
    public String getNotificationInterval() {
        return notificationInterval;
    }

    /**
     * @return True if user wants to be informed about the session timeout (popup)
     */
    @Override
    public boolean getInformSessionTimeout() {
        return informSessionTimeout;
    }

    /**
     * @param b
     *            Set information about wether session timeout should be displayed or not
     */
    @Override
    public void setInformSessionTimeout(final boolean b) {
        informSessionTimeout = b;
    }

    /**
	 */
    @Override
    public boolean getPresenceMessagesPublic() {
        return presenceMessagesPublic;
    }

    /**
	 */
    @Override
    public void setPresenceMessagesPublic(final boolean b) {
        this.presenceMessagesPublic = b;

    }

}
