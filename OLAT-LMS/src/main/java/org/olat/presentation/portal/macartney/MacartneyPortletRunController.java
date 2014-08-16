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

package org.olat.presentation.portal.macartney;

import java.text.DecimalFormat;
import java.util.Map;

import org.olat.lms.properties.PropertyManagerEBL;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.Settings;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Run view controller of macartney portlet
 * <P>
 * Initial Date: 11.07.2005 <br>
 * 
 * @author gnaegi
 */
public class MacartneyPortletRunController extends DefaultController {

    private static final String VELOCITY_ROOT = PackageUtil.getPackageVelocityRoot(MacartneyPortlet.class);
    private Translator trans;
    private VelocityContainer macartneyVC;

    private static final int maxEpisodes = 468;
    private static final long updateInterval = 86400000; // one day in milliseconds

    private static long comicStartDate = 0;

    /**
     * Constructor
     * 
     * @param ureq
     * @param imageBaseUri
     */
    // o_clusterOK by:se synchronized on MacartneyPortlet class as olatresourceable
    protected MacartneyPortletRunController(final UserRequest ureq, final WindowControl wControl, final Map configuration) {
        super(wControl);
        this.trans = new PackageTranslator(PackageUtil.getPackageName(MacartneyPortlet.class), ureq.getLocale());
        this.macartneyVC = new VelocityContainer("macartneyVC", VELOCITY_ROOT + "/macartneyPortlet.html", trans, this);

        if (comicStartDate == 0) {
            comicStartDate = getPropertyManagerEBL().getComicStartDate(comicStartDate, MacartneyPortletRunController.class.getSimpleName());
        }

        // time between comic start and now
        final long timeDelta = (System.currentTimeMillis() - comicStartDate);
        // number of increments since comic start
        final long incrementNumber = (timeDelta / updateInterval);
        // module with max episodes to start over again when finished
        // + 1 since comic starts at position 1 and not 0
        final long imageNumber = (incrementNumber % maxEpisodes) + 1;

        // calculate current episode url and push to velocity
        final DecimalFormat df = new DecimalFormat("0000");
        final String currentEpisodeImage = "loge_" + df.format(imageNumber) + ".jpg";
        final String configuredImageBaseUri = (String) configuration.get("imageBaseUri");
        if (configuredImageBaseUri.startsWith("http://") || configuredImageBaseUri.startsWith("https://")) {
            // feature: if the configured imageBaseUri contains http: or https: already, don't do
            // magic "trying-to-detect-the-correct-protocol-via-olat.properties" but use it right away
            this.macartneyVC.contextPut("imageBaseUri", configuredImageBaseUri);
        } else {
            // otherwise do the old magic
            this.macartneyVC.contextPut("imageBaseUri", Settings.getURIScheme() + configuredImageBaseUri);
        }
        this.macartneyVC.contextPut("currentEpisodeImage", currentEpisodeImage);

        setInitialComponent(this.macartneyVC);
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // no events to catch
    }

    @Override
    protected void doDispose() {
        // nothing to dispose
    }

    private PropertyManagerEBL getPropertyManagerEBL() {
        return CoreSpringFactory.getBean(PropertyManagerEBL.class);
    }

}
