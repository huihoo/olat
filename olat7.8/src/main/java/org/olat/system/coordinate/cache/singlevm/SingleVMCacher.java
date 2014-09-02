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
 * Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.system.coordinate.cache.singlevm;

import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.Cacher;
import org.olat.system.coordinate.cache.CacheConfig;
import org.olat.system.coordinate.cache.CacheWrapper;
import org.olat.system.exception.AssertException;

/**
 * Description:<br>
 * single java vm implementation of the cacher interface
 * <P>
 * Initial Date: 16.10.2007 <br>
 * 
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class SingleVMCacher implements Cacher {
    private CacheWrapperImpl rootCacheWrapperImpl;
    private CacheConfig rootConfig;

    public SingleVMCacher() {
        // needed for spring
    }

    public void init() {
        if (rootConfig == null) {
            throw new AssertException("rootConfig property must not be null!");
        }
        rootCacheWrapperImpl = new CacheWrapperImpl(this.getClass().getName(), rootConfig);
    }

    @Override
    public CacheWrapper getOrCreateCache(Class ownerClass, String name) {
        OLATResourceable ores = OresHelper.createOLATResourceableInstanceWithoutCheck(CacheConfig.getCacheName(ownerClass, name), new Long(0));
        return rootCacheWrapperImpl.getOrCreateChildCacheWrapper(ores);
    }

    /**
     * [used by spring]
     * 
     * @param rootConfig
     */
    public void setRootConfig(CacheConfig rootConfig) {
        this.rootConfig = rootConfig;
    }
}
