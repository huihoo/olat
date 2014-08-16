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
package org.olat.lms.instantmessaging;

import java.io.File;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.lms.user.UserService;
import org.olat.system.commons.WebappHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <P>
 * Initial Date: 13.09.2011 <br>
 * 
 * @author cg
 */
@Component
public class Chat_EBL {

    private static final String THEMES_DEFAULT_PATH = "/themes/default";
    private static final String THEMES_PATH = "/themes/";
    private static final String SOUNDS_NEW_MESSAGE_WAV_PATH = "/sounds/new_message.wav";
    @Autowired
    private UserService userService;
    @Autowired
    private BaseSecurity baseSecurity;

    public String getFullUserName(final String username) {
        final Identity ident = baseSecurity.findIdentityByName(username);
        if (ident != null) {
            final StringBuilder sb = new StringBuilder();
            sb.append(userService.getFirstAndLastname(ident.getUser())).append(" ");
            sb.append("(").append(ident.getName()).append(")");
            return sb.toString();
        }
        return username;
    }

    public String getNewMessageSoundURL(final String guiThemeIdentifier, final String guiThemeBaseUri) {

        String newMessageSoundURL = guiThemeBaseUri + SOUNDS_NEW_MESSAGE_WAV_PATH;
        final File soundFile = new File(WebappHelper.getContextRoot() + THEMES_PATH + guiThemeIdentifier + SOUNDS_NEW_MESSAGE_WAV_PATH);
        if (!soundFile.exists()) {
            newMessageSoundURL = newMessageSoundURL.replace(THEMES_PATH + guiThemeIdentifier, THEMES_DEFAULT_PATH);
        }

        return newMessageSoundURL;

    }

}
