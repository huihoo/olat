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
package org.olat.presentation.examples.guidemo;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.textboxlist.ResultMapProvider;
import org.olat.presentation.framework.core.components.textboxlist.TextBoxListComponent;
import org.olat.presentation.framework.core.components.textboxlist.TextBoxListEvent;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * A simple Demo for the TextBoxList Component
 * <P>
 * Initial Date: 23.07.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class GuiDemoTextBoxListController extends BasicController {

    private final TextBoxListComponent tblC;

    public GuiDemoTextBoxListController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);

        final Map<String, String> initialItems = new TreeMap<String, String>();
        initialItems.put("123 Demo", "demo");
        initialItems.put("try to delete me", "delete");
        tblC = new TextBoxListComponent("testTextbox", "textboxlist.hint", initialItems, getTranslator());
        final ResultMapProvider provider = new ResultMapProvider() {
            @Override
            public void getAutoCompleteContent(final String searchValue, final Map<String, String> resMap) {
                // put some dummy values as result. For real-world do your search-magic here!
                resMap.put("Hausvermietung" + searchValue, "10");
                resMap.put("Clown" + searchValue, "4");
                resMap.put("Suche nach: " + searchValue, "3");
            }
        };
        tblC.setMapperProvider(provider);
        // if no provider is needed (maybe only a small autocomplete-map) you could provide them directly
        // Map<String, Integer> autoCompleteContent = new HashMap<String, Integer>();
        // autoCompleteContent.put("Hausvermietung", 10);
        // autoCompleteContent.put("Clown", 4);
        // autoCompleteContent.put("Versicherung", 3);
        // tblC.setAutoCompleteContent(autoCompleteContent);

        // tblC.setEnabled(false);
        tblC.addListener(this);

        putInitialPanel(tblC);
    }

    /**
	 */
    @Override
    @SuppressWarnings("unused")
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (event instanceof TextBoxListEvent) {
            final TextBoxListEvent tblEv = (TextBoxListEvent) event;
            final List<String> all = tblEv.getAllItems();
            final List<String> newItems = tblEv.getNewOnly();
            getWindowControl().setInfo("the following items were submitted: " + all.toString() + "    some where even added: " + newItems.toString());
        }

    }

    /**
	 */
    @Override
    protected void doDispose() {
        // TODO Auto-generated method stub

    }

}
