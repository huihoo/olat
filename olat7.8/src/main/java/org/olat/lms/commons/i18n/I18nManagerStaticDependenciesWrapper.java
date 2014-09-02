/**
 * 
 */
package org.olat.lms.commons.i18n;

import java.util.Locale;

/**
 * @author patrick
 * 
 */
public interface I18nManagerStaticDependenciesWrapper {

    String getFilePrefix();

    String getFilePostfix();

    Locale getFallbackLocale();

    String getApplicationFallbackBundle();

    Locale getDefaultLocale();

    boolean isDebugging();

}
