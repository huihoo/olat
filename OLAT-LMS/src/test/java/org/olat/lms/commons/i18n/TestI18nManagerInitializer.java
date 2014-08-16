/**
 * 
 */
package org.olat.lms.commons.i18n;

/**
 * @author patrick
 *
 */
public class TestI18nManagerInitializer {

	private I18nManager i18nManager;

	public TestI18nManagerInitializer(I18nManagerStaticDependenciesWrapper staticDependencies){
		i18nManager = new I18nManager(staticDependencies);
	}
	
}
