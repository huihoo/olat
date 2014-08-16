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

package org.olat.lms.portfolio;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.lms.portfolio.artefacthandler.EPAbstractHandler;
import org.olat.lms.portfolio.artefacthandler.EPArtefactHandler;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Integration test of the PortfolioModule
 * <P>
 * Initial Date: 23 . 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class PortfolioModuleITCase extends OlatTestCase {

    @Autowired
    private PortfolioModule portfolioModule;

    @Test
    public void testGetModule() {
        assertNotNull(portfolioModule);
    }

    @Test
    public void tesAddArtefactHandler() {
        final DummyArtefactHandler artefactHandler = new DummyArtefactHandler();
        portfolioModule.addArtefactHandler(artefactHandler);

        final List<EPArtefactHandler<?>> handlers = portfolioModule.getAllAvailableArtefactHandlers();
        boolean found = false;
        for (final EPArtefactHandler<?> handler : handlers) {
            if (handler == artefactHandler) {
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    public void testRemoveArtefactHandler() {
        // prepare the dummy artefact handler
        EPArtefactHandler<?> handler = portfolioModule.getArtefactHandler(DummyArtefact.TYPE);
        if (handler == null) {
            handler = new DummyArtefactHandler();
            portfolioModule.addArtefactHandler(handler);
        }

        // remove it
        portfolioModule.removeArtefactHandler(handler);

        // check if
        final EPArtefactHandler<?> removedHandler = portfolioModule.getArtefactHandler(DummyArtefact.TYPE);
        assertNull(removedHandler);
    }

    @Test
    public void testGetArtefactHandlers() {
        final List<EPArtefactHandler<?>> handlers = portfolioModule.getArtefactHandlers();
        assertFalse(handlers.isEmpty());
    }

    @Test
    public void testGetAllAvailableArtefactHandlers() {
        final List<EPArtefactHandler<?>> handlers = portfolioModule.getAllAvailableArtefactHandlers();
        assertFalse(handlers.isEmpty());
    }

    @Test
	@Ignore("TODO:2011-10-13:PB " +
			"Observation: was instable on hudson, stable on local hudson -> deactivated." +
			"Relevance: irrelevant, testing enabling/disabling in a short interval. Enabling/Disabling is done during configuration time and not online." +
			"OnOff Config is written to Filesystem and may cause this instability." +
			"The test shows a design flaw of the persisted properties and not of portfolio enabling handler problems.")
    public void testSetEnableArtefactHandler() {
        // prepare the dummy artefact handler
        EPArtefactHandler<?> dummyHandler = portfolioModule.getArtefactHandler(DummyArtefact.TYPE);
        if (dummyHandler == null) {
            dummyHandler = new DummyArtefactHandler();
            portfolioModule.addArtefactHandler(dummyHandler);
        }

        // ////////////////////////////////
        // disable
        // ////////////////////////////////
        portfolioModule.setEnableArtefactHandler(dummyHandler, false);

        // found in the list of all available handlers
        List<EPArtefactHandler<?>> allHandlers = portfolioModule.getAllAvailableArtefactHandlers();
        boolean foundInAll = false;
        for (final EPArtefactHandler<?> handler : allHandlers) {
            if (handler == dummyHandler) {
                foundInAll = true;
                assertFalse(handler.isEnabled());
            }
        }
        assertTrue(foundInAll);

        // not found in the list of handlers
        List<EPArtefactHandler<?>> enabledHandlers = portfolioModule.getArtefactHandlers();
        boolean foundInEnabled = false;
        for (final EPArtefactHandler<?> handler : enabledHandlers) {
            if (handler == dummyHandler) {
                foundInEnabled = true;
                assertFalse(handler.isEnabled());
            }
        }
        assertFalse(foundInEnabled);

        // found but disabled in get with type
        final EPArtefactHandler<?> disabledDummyHandler = portfolioModule.getArtefactHandler(DummyArtefact.TYPE);
        assertFalse(disabledDummyHandler.isEnabled());

        // ////////////////////////////////
        // enable
        // ////////////////////////////////
        portfolioModule.setEnableArtefactHandler(dummyHandler, false);

        // found in the list of all available handlers
        allHandlers = portfolioModule.getAllAvailableArtefactHandlers();
        foundInAll = false;
        for (final EPArtefactHandler<?> handler : allHandlers) {
            if (handler == dummyHandler) {
                foundInAll = true;
                assertFalse(handler.isEnabled());
            }
        }
        assertTrue(foundInAll);

        // not found in the list of handlers
        enabledHandlers = portfolioModule.getArtefactHandlers();
        foundInEnabled = false;
        for (final EPArtefactHandler<?> handler : enabledHandlers) {
            if (handler == dummyHandler) {
                foundInEnabled = true;
                assertFalse(handler.isEnabled());
            }
        }
        assertFalse(foundInEnabled);

        // found but disabled in get with type
        final EPArtefactHandler<?> enabledDummyHandler = portfolioModule.getArtefactHandler(DummyArtefact.TYPE);
        assertFalse(enabledDummyHandler.isEnabled());
    }

    public class DummyArtefactHandler extends EPAbstractHandler<DummyArtefact> {
        @Override
        public String getType() {
            return DummyArtefact.TYPE;
        }

        @Override
        public DummyArtefact createArtefact() {
            return new DummyArtefact();
        }

        @Override
        public PackageTranslator getHandlerTranslator(final Translator fallBackTrans) {
            return null;
        }

        @Override
        public boolean isProvidingSpecialMapViewController() {
            return false;
        }

        @Override
        public String getIcon(final AbstractArtefact artefact) {
            return "o_ep_dummy";
        }
    }

    public class DummyArtefact extends AbstractArtefact {
        public static final String TYPE = "dummy";

        @Override
        public String getResourceableTypeName() {
            return TYPE;
        }

    }
}
