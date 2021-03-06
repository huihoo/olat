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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.presentation.framework.core.translator;

import java.util.Locale;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class PackageUtil {

    /**
     * @deprecated please use createPackageTranslator.... Returns the package name for this class
     * @param clazz
     * @return the package name
     */
    @Deprecated
    public static String getPackageName(Class clazz) {
        return clazz.getPackage().getName();
    }

    /**
     * Converts the package name to the velocity root path by replacing .'s with /'s. The prefix org will be stripped away. Use this to set velocity pages in a velocity
     * container.
     * 
     * @param clazz
     * @return the velocity root path
     */
    public static String getPackageVelocityRoot(Class clazz) {
        return getPackageVelocityRoot(getPackageName(clazz));
    }

    /**
     * Converts the package name to the velocity root path by replacing .'s with /'s. The prefix org will be stripped away. Use this to set velocity pages in a velocity
     * container.
     * 
     * @param packageName
     * @return the velocity root path
     */
    public static String getPackageVelocityRoot(String packageName) {
        // TASK:fj:b compress velocity code with ant task. for debug mode: use _content, for productive mode, use _cleanedcontent or _compressedcontent (which does not
        // mean gzip, but removing unnecessary whitespaces and such)
        // ch.goodsolutions.bla -> ch/goodsolutions/bla/_content where the velocity pages are found
        String rep = packageName.replace('.', '/') + "/_content";
        return rep;

    }

    public static Translator createPackageTranslator(Class baseClass, Locale locale) {
        return createPackageTranslator(baseClass, locale, null);
    }

    /**
     * returns a Translator for the given baseclass and locale
     * 
     * @param baseClass
     *            the location of the class will be taken to resolve the relative resource "_i18n/LocalStrings_(localehere).properties"
     * @param locale
     * @param fallback
     *            The fallback translator that should be used
     * @return
     */
    public static Translator createPackageTranslator(Class baseClass, Locale locale, Translator fallback) {
        String transpackage = PackageUtil.getPackageName(baseClass);
        Translator trans;
        if (fallback != null) {
            trans = new PackageTranslator(transpackage, locale, fallback);
        } else {
            trans = new PackageTranslator(transpackage, locale);
        }

        return trans;
    }

    /**
     * testing ground guido
     * 
     * @param i18nPackage
     * @param locale
     * @return
     */
    public static Translator createPackageTranslator(I18nPackage i18nPackage, Locale locale) {
        return new PackageTranslator(i18nPackage.getBaseLocation(), locale);

    }

    public static Translator createPackageTranslator(I18nPackage i18nPackage, Locale locale, Translator fallback) {
        return new PackageTranslator(i18nPackage.getBaseLocation(), locale, fallback);

    }

}
