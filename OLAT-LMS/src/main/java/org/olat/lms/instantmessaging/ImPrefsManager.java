/**
 * OLAT - Online Learning and Training<br />
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br />
 * you may not use this file except in compliance with the License.<br />
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br />
 * software distributed under the License is distributed on an "AS IS" BASIS, <br />
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br />
 * See the License for the specific language governing permissions and <br />
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br />
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.lms.instantmessaging;

import org.jivesoftware.smack.packet.Presence;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.xml.XStreamHelper;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.SyncerCallback;
import org.olat.system.coordinate.SyncerExecutor;
import org.olat.system.exception.AssertException;

/**
 * Description:<br />
 * load and set instant messaging preferences. Manager is used when logging in or looping over all users for awareness information. The later case need to be threadsave.
 * <P>
 * Initial Date: 11.08.2007 <br />
 * 
 * @author patrickb
 * @author guido
 */
public class ImPrefsManager extends BasicManager {

    private static ImPrefsManager INSTANCE;
    private final static String LOCK_KEY = ImPrefsManager.class.toString();

    private ImPrefsManager() {
        INSTANCE = this;
    }

    /**
     * singleton, return instance
     * 
     * @return
     */
    public static ImPrefsManager getInstance() {
        return INSTANCE;
    }

    /**
     * create new property for user with default IM prefs, or load an existing IM prefs from the property store.
     * 
     * @param identity
     * @return
     */
    public ImPreferences loadOrCreatePropertiesFor(final Identity identity) {
        // o_clusterOK by guido
        return CoordinatorManager.getInstance().getCoordinator().getSyncer()
                .doInSync(OresHelper.createOLATResourceableInstanceWithoutCheck(LOCK_KEY, identity.getKey()), new SyncerCallback<ImPreferences>() {

                    @Override
                    public ImPreferences execute() {
                        // search x-stream serialization in properties table, create new if not
                        ImPreferences imPrefs = findPropertiesFor(identity);
                        if (imPrefs == null) {
                            // no imPrefs => create new default imPrefs
                            imPrefs = new ImPreferences(identity);
                            imPrefs.setVisibleToOthers(true);
                            imPrefs.setOnlineTimeVisible(false);
                            imPrefs.setAwarenessVisible(false);
                            imPrefs.setRosterDefaultStatus(Presence.Mode.available.toString());
                            final String props = XStreamHelper.toXML(imPrefs);
                            final PropertyImpl imProperty = PropertyManager.getInstance().createPropertyInstance(identity, null, null, null,
                                    ImPreferences.USER_PROPERTY_KEY, null, null, null, props);
                            PropertyManager.getInstance().saveProperty(imProperty);
                            imPrefs.dbProperty = imProperty;
                        }
                        return imPrefs;
                    }

                });
    }

    /**
     * update / save changed IM Preferences. Preferences must already exist
     * 
     * @param identity
     * @param toUpdate
     */
    public void updatePropertiesFor(final Identity identity, final ImPreferences toUpdate) {
        // o_clusterOK by guido
        CoordinatorManager.getInstance().getCoordinator().getSyncer()
                .doInSync(OresHelper.createOLATResourceableInstanceWithoutCheck(LOCK_KEY, identity.getKey()), new SyncerExecutor() {

                    @Override
                    public void execute() {
                        final PropertyManager pm = PropertyManager.getInstance();
                        // generate x-stream serialization of this object
                        final String props = XStreamHelper.toXML(toUpdate);
                        final PropertyImpl imProperty = PropertyManager.getInstance().findProperty(identity, null, null, null, ImPreferences.USER_PROPERTY_KEY);
                        if (imProperty == null) {
                            throw new AssertException("Try to update Im Prefs for (" + identity.getName() + ") but they do not exist!!");
                        }
                        imProperty.setTextValue(props);
                        pm.updateProperty(imProperty);
                    }

                });

    }

    /**
     * @param identity
     * @return the imPreferences or null if not found
     */
    private ImPreferences findPropertiesFor(final Identity identity) {
        final PropertyImpl imProperty = PropertyManager.getInstance().findProperty(identity, null, null, null, ImPreferences.USER_PROPERTY_KEY);
        if (imProperty == null) {
            return null;
        } else {
            final ImPreferences imPrefs = (ImPreferences) XStreamHelper.fromXML(imProperty.getTextValue());
            imPrefs.owner = identity; // reset transient value
            imPrefs.dbProperty = imProperty;
            return imPrefs;
        }
    }
}
