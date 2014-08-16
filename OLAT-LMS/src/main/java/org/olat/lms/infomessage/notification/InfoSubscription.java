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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.lms.infomessage.notification;

import java.util.ArrayList;
import java.util.List;

import org.jfree.util.Log;
import org.olat.data.basesecurity.Identity;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Description:<br>
 * Helper class to manage the opt-out of info messages subscriptions
 * <P>
 * Initial Date: 3 aug. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InfoSubscription {

    private PropertyManager propertyManager;
    private Identity ident;
    private static final String INFO_SUBSCRIPTION_KEY_SUBSCRIBED = "InfoSubscription::subscribed";
    private static final String INFO_SUBSCRIPTION_KEY_UNSUBSCRIBED = "InfoSubscription::notdesired";
    private static final String SEPARATOR = ",";

    public InfoSubscription(PropertyManager propertyManager, Identity ident) {
        this.propertyManager = propertyManager;
        this.ident = ident;
        if (propertyManager == null || ident == null)
            throw new IllegalArgumentException("one of the constr. args is null!");
    }

    public boolean isSubscribed(final String businessPath) {
        return getSubscribedInfos().contains(businessPath);
    }

    public boolean subscribed(final String businessPath, final boolean force) {
        if (!isSubscribed(businessPath)) {
            // subscribe to the actual calendar
            final List<String> infoSubscriptions = getSubscribedInfos();
            final List<String> infoUnSubscriptions = getUnsubscribedInfos();
            if (!infoUnSubscriptions.contains(businessPath) || force) {
                infoSubscriptions.add(businessPath);
                infoUnSubscriptions.remove(businessPath);
                persistAllSubscriptionInfos(infoSubscriptions, infoUnSubscriptions);
            }
        }

        return getSubscribedInfos().contains(businessPath);
    }

    public void unsubscribed(final String businessPath) {
        // subscribe to the actual calendar
        final List<String> infoSubscriptions = getSubscribedInfos();
        final List<String> infoUnSubscriptions = getUnsubscribedInfos();
        infoSubscriptions.remove(businessPath);
        infoUnSubscriptions.add(businessPath);
        persistAllSubscriptionInfos(infoSubscriptions, infoUnSubscriptions);
    }

    private List<String> getSubscribedInfos() {
        return getProperty(INFO_SUBSCRIPTION_KEY_SUBSCRIBED);
    }

    private List<String> getUnsubscribedInfos() {
        return getProperty(INFO_SUBSCRIPTION_KEY_UNSUBSCRIBED);
    }

    /**
     * @return
     */
    private List<String> getProperty(String key) {
        List<String> infoSubscriptions = new ArrayList<String>();
        List<PropertyImpl> properties = propertyManager.findProperties(ident, null, null, null, key);

        if (properties.size() > 1) {
            Log.error("more than one property found, something went wrong, deleting them and starting over.");
            for (PropertyImpl prop : properties) {
                propertyManager.deleteProperty(prop);
            }

        } else if (properties.size() == 0l) {
            PropertyImpl p = propertyManager.createPropertyInstance(ident, null, null, null, key, null, null, null, null);
            propertyManager.saveProperty(p);
            properties = propertyManager.findProperties(ident, null, null, null, key);
        }
        String value = properties.get(0).getTextValue();

        if (value != null && !value.equals("")) {
            String[] subscriptions = properties.get(0).getTextValue().split(SEPARATOR);
            infoSubscriptions.addAll(Arrays.asList(subscriptions));
        }

        return infoSubscriptions;
    }

    private void persistAllSubscriptionInfos(final List<String> infoSubscriptions, final List<String> infoUnsubscriptions) {

        persistInfo(infoSubscriptions, INFO_SUBSCRIPTION_KEY_SUBSCRIBED);
        persistInfo(infoUnsubscriptions, INFO_SUBSCRIPTION_KEY_UNSUBSCRIBED);
    }

    /**
     * @param infoSubscriptions
     */
    private void persistInfo(final List<String> infoSubscriptions, String key) {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < infoSubscriptions.size(); i++) {
            sb.append(infoSubscriptions.get(i));
            if (i < infoSubscriptions.size() - 1) {
                sb.append(",");
            }
        }

        List<PropertyImpl> properties = propertyManager.findProperties(ident, null, null, null, key);
        PropertyImpl p = properties.get(0);
        p.setTextValue(sb.toString());
        propertyManager.saveProperty(p);

    }

}
