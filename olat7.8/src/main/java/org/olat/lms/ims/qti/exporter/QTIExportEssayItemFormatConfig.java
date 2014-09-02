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

package org.olat.lms.ims.qti.exporter;

import org.olat.system.exception.OLATRuntimeException;

public class QTIExportEssayItemFormatConfig implements QTIExportItemFormatConfig {
    private boolean responseCols;
    private boolean timeCols;

    public QTIExportEssayItemFormatConfig(final boolean resCols, final boolean timeCols) {
        this.responseCols = resCols;
        this.timeCols = timeCols;
    }

    @Override
    public boolean hasResponseCols() {
        return responseCols;
    }

    @Override
    public boolean hasPointCol() {
        return false;
    }

    @Override
    public boolean hasTimeCols() {
        return timeCols;
    }

    @Override
    public boolean hasPositionsOfResponsesCol() {
        return false;
    }

    @Override
    public void setResponseCols(final boolean responseColsConfigured) {
        this.responseCols = responseColsConfigured;
    }

    @Override
    public void setTimeCols(final boolean timeColsConfigured) {
        this.timeCols = timeColsConfigured;
    }

    @Override
    public void setPointCol(final boolean pointColConfigured) {
        throw new OLATRuntimeException(null, "pointCol is not configureable for QTIType Essay", null);
    }

    @Override
    public void setPositionsOfResponsesCol(final boolean positionsOfResponsesColConfigured) {
        throw new OLATRuntimeException(null, "responseCols is not configureable for QTIType Essay", null);
    }
}
