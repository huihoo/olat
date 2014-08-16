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

package org.olat.lms.search.indexer;

import org.olat.data.portfolio.structure.EPStructuredMap;
import org.olat.data.portfolio.structure.ElementType;

/**
 * Description:<br>
 * Index the map of a user
 * <P>
 * Initial Date: 15 nov. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PortfolioStructuredMapIndexer extends AbstractPortfolioMapIndexer {

    public static final String STRUCTURED_TYPE = "type.db." + EPStructuredMap.class.getSimpleName();
    public static final String STRUCTURED_ORES_TYPE = EPStructuredMap.class.getSimpleName();

    @Override
    protected String getDocumentType() {
        return STRUCTURED_TYPE;
    }

    @Override
    protected ElementType getElementType() {
        return ElementType.STRUCTURED_MAP;
    }

    @Override
    public String getSupportedTypeName() {
        return STRUCTURED_ORES_TYPE;
    }
}
