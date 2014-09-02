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
package org.olat.lms.core.notification.service;

import org.olat.lms.core.CoreService;
import org.olat.lms.core.notification.impl.channel.InvalidAddressException;

/**
 * Sends confirmations and messages. <br>
 * A confirmation is a message sent automatically upon completion of an user action.<br>
 * A message is either explicitly edited by a user or is a default text stored as translated string.
 * 
 * Initial Date: 19.09.2012 <br>
 * 
 * @author Branislav Balaz
 */
public interface ConfirmationService extends CoreService {

    /**
     * This sends "confirmations" (a.k.a Bestaetigung) emails.
     */
    boolean sendConfirmation(ConfirmationInfo confirmationInfo);

    /**
     * This sends "messages or activation" (a.k.a Mitteilung, Aktivierung) emails.
     */
    boolean sendMessage(MailMessage message) throws InvalidAddressException;

}
