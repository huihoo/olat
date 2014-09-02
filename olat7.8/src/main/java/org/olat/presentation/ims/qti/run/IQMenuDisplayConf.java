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

package org.olat.presentation.ims.qti.run;

/**
 * Initial Date: Aug 11, 2005 <br>
 * 
 * @author patrick
 */
public class IQMenuDisplayConf {

    private final boolean renderSectionsOnly;
    private final boolean enabledMenu;
    private final boolean itemPageSequence;

    public IQMenuDisplayConf(final boolean renderSectionsOnly, final boolean enabledMenu, final boolean itemPageSequence) {
        this.renderSectionsOnly = renderSectionsOnly;
        this.enabledMenu = enabledMenu;
        this.itemPageSequence = itemPageSequence;
    }

    /**
     * @return Returns the renderSectionsOnly.
     */
    public boolean isRenderSectionsOnly() {
        return renderSectionsOnly;
    }

    /**
     * @return Returns the enabledMenu.
     */
    public boolean isEnabledMenu() {
        return enabledMenu;
    }

    /**
     * ItemPage sequence means that the navigator shows one question per page. <br>
     * SectionPage sequence means that the navigator shows one section per page. <br>
     * 
     * @return true if the sequence is itemPage (a.k.a. one question per page), false otherwise.
     */
    public boolean isItemPageSequence() {
        return itemPageSequence;
    }

}