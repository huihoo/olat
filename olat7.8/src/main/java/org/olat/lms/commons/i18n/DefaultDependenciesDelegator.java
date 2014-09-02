/**
 * 
 */
package org.olat.lms.commons.i18n;

import java.util.Locale;

import org.olat.system.commons.Settings;

/**
 * @author patrick
 * 
 */
public class DefaultDependenciesDelegator implements I18nManagerStaticDependenciesWrapper {

    @Override
    public String getFilePrefix() {
        return I18nModule.LOCAL_STRINGS_FILE_PREFIX;
    }

    @Override
    public String getFilePostfix() {
        return I18nModule.LOCAL_STRINGS_FILE_POSTFIX;
    }

    @Override
    public Locale getFallbackLocale() {
        return I18nModule.getFallbackLocale();
    }

    @Override
    public String getApplicationFallbackBundle() {
        return I18nModule.getApplicationFallbackBundle();
    }

    @Override
    public Locale getDefaultLocale() {
        return I18nModule.getDefaultLocale();
    }

    @Override
    public boolean isDebugging() {
        return Settings.isDebuging();
    }

}
