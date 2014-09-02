/**
 * 
 */
package org.olat.presentation.framework.core;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.olat.presentation.commons.session.UserSession;
import org.olat.presentation.framework.core.control.WindowBackOffice;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.dispatcher.mapper.MapperRegistry;
import org.olat.system.commons.CodeHelperInitalizer;

/**
 * @author patrick
 * 
 */
public class ObjectMother {

    public static PresentationFrameworkTestContext createPresentationFrameworkEnvironment(Locale locale) {

        new CodeHelperInitalizer();

        UserSession userSession = mock(UserSession.class);
        MapperRegistry mapperRegistry = mock(MapperRegistry.class);// is not instatiatable, maybe use a MapperRegistryInitializer in same package
        when(userSession.getEntry(MapperRegistry.CNAME)).thenReturn(mapperRegistry);

        UserRequest ureq = mock(UserRequest.class);
        when(ureq.getLocale()).thenReturn(locale);
        when(ureq.getUserSession()).thenReturn(userSession);

        WindowControl wControl = mock(WindowControl.class);
        WindowBackOffice windowBackOffice = mock(WindowBackOffice.class);
        when(wControl.getWindowBackOffice()).thenReturn(windowBackOffice);

        return new PresentationFrameworkTestContext(ureq, wControl);
    }

}
