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
package org.olat.presentation.portfolio.artefacts.run;

import java.util.Locale;

import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.lms.portfolio.PortfolioAbstractHandler;
import org.olat.lms.portfolio.artefacthandler.EPArtefactHandler;
import org.olat.presentation.framework.core.components.table.CustomCellRenderer;
import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.portfolio.filter.PortfolioFilterController;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * presents image for artefact type in table
 * <P>
 * Initial Date: 02.12.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class ArtefactTypeImageCellRenderer implements CustomCellRenderer {

    /**
     * java.util.Locale, int, java.lang.String)
     */
    @SuppressWarnings("unused")
    @Override
    public void render(final StringOutput sb, final Renderer renderer, final Object val, final Locale locale, final int alignment, final String action) {
        if (val instanceof AbstractArtefact) {
            final AbstractArtefact artefact = (AbstractArtefact) val;
            final PortfolioAbstractHandler portfolioModule = (PortfolioAbstractHandler) CoreSpringFactory.getBean(PortfolioAbstractHandler.class);
            final EPArtefactHandler<?> artHandler = portfolioModule.getArtefactHandler(artefact.getResourceableTypeName());
            final PackageTranslator pT = new PackageTranslator(this.getClass().getPackage().getName(), locale);
            final Translator handlerTrans = artHandler.getHandlerTranslator(pT);
            final String handlerClass = PortfolioFilterController.HANDLER_PREFIX + artHandler.getClass().getSimpleName() + PortfolioFilterController.HANDLER_TITLE_SUFFIX;
            final String artType = handlerTrans.translate(handlerClass);
            final String artIcon = artHandler.getIcon(artefact);

            sb.append("<span class=\"");
            sb.append(artIcon);
            sb.append("\" title=\"");
            sb.append(artType);
            sb.append("\" style=\"background-repeat: no-repeat; background-position: center center; padding: 2px 8px;\">&nbsp;</span>");
        }
    }

}
