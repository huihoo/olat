/**
 * 
 */
package org.olat.lms.commons.i18n;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

/**
 * @author patrick
 * 
 */
public class ObjectMother {

    public static TestI18nManagerInitializer setupI18nManagerForFallbackLocale(Locale fallback) {
        I18nManagerStaticDependenciesWrapper staticDependencies = mock(I18nManagerStaticDependenciesWrapper.class);
        when(staticDependencies.getFilePostfix()).thenReturn(I18nModule.LOCAL_STRINGS_FILE_PREFIX);
        when(staticDependencies.getFilePrefix()).thenReturn(I18nModule.LOCAL_STRINGS_FILE_PREFIX);
        when(staticDependencies.getApplicationFallbackBundle()).thenReturn("org.olat.presentation");
        // to unveal translation issues, set the fallbacks to the actual used Locale
        when(staticDependencies.getFallbackLocale()).thenReturn(fallback);
        when(staticDependencies.getDefaultLocale()).thenReturn(fallback);
        when(staticDependencies.isDebugging()).thenReturn(false);
        return new TestI18nManagerInitializer(staticDependencies);
    }

}
