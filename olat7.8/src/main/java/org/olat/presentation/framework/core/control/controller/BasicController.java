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
package org.olat.presentation.framework.core.control.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.commons.LearnServices;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.creator.ControllerCreator;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.presentation.framework.core.control.generic.popup.PopupBrowserWindow;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.framework.dispatcher.mapper.Mapper;
import org.olat.presentation.framework.dispatcher.mapper.MapperRegistry;
import org.olat.system.commons.StringHelper;
import org.olat.system.exception.AssertException;
import org.olat.system.spring.CoreSpringFactory;
import org.olat.system.spring.ServiceLocator;

/**
 * Description:<br>
 * BasicController is a controller which serves as convenient superclass for controllers. it offers things like easy access to locale, identity, and generation of
 * velocitypages.<br>
 * New added methods must have visibility <code>protected</code>
 * <p>
 * The alternative for escaping here would be to escape everything into <code>GUIMessage</code>.
 * <p>
 * Initial Date: 15.12.2006 <br>
 * 
 * @author Felix Jost, www.goodsolutions.ch
 */
public abstract class BasicController extends DefaultController {

    protected String velocity_root;
    private Locale locale;
    private final Identity identity;
    private Translator translator;
    private Translator fallbackTranslator;

    private MapperRegistry mreg;
    private List<Mapper> mappers = null;
    ServiceLocator serviceLocator;
    private List<Controller> childControllers = null;

    /**
     * easy to use controller template. Extending the BasicController allows to create velocity pages and translate keys without cumbersome creation of the corresponding
     * objects.
     * 
     * @param ureq
     * @param wControl
     */
    protected BasicController(UserRequest ureq, WindowControl wControl) {
        super(wControl);
        this.locale = ureq.getLocale();
        this.identity = ureq.getIdentity();
        this.translator = PackageUtil.createPackageTranslator(this.getClass(), locale);
        this.fallbackTranslator = null;
        this.velocity_root = PackageUtil.getPackageVelocityRoot(this.getClass());
        this.mreg = MapperRegistry.getInstanceFor(ureq.getUserSession());

    }

    /**
     * brasato:::: omit - move to setTranslator() profit from the easy of use, but have the flexibility of using a translator chain. As a main translator this packages
     * translator is used and then fallbacked to the provided fallbacktranslator.
     * 
     * @param ureq
     * @param wControl
     * @param fallBackTranslator
     */
    protected BasicController(UserRequest ureq, WindowControl wControl, Translator fallBackTranslator) {
        super(wControl);
        this.locale = ureq.getLocale();
        this.identity = ureq.getIdentity();
        if (fallBackTranslator == null) {
            throw new AssertException("please provide a fall translator if using this constructor!!");
        }
        this.fallbackTranslator = fallBackTranslator;
        this.translator = PackageUtil.createPackageTranslator(this.getClass(), locale, fallBackTranslator);
        this.velocity_root = PackageUtil.getPackageVelocityRoot(this.getClass());
        this.mreg = MapperRegistry.getInstanceFor(ureq.getUserSession());

    }

    @Override
    protected void doPreDispose() {
        // deregister all mappers if needed
        if (mappers != null) {
            for (Mapper m : mappers) {
                mreg.deregister(m);
            }
        }

        // dispose child controller if needed
        if (childControllers != null) {
            for (Controller c : childControllers) {
                c.dispose();
            }
        }
    }

    /**
     * brasato:: do some code examples
     * 
     * @param controller
     * @return the same instance of the controller - used for easy and compact code
     * @throws AssertException
     *             if the controller to be added is already contained.
     */
    protected Controller listenTo(Controller controller) {
        controller.addControllerListener(this);
        if (childControllers == null)
            childControllers = new ArrayList<Controller>(4);
        /*
         * REVIEW:pb this is for quality and will be re-enabled after the OLAT 6.0.0 Release if(childControllers.contains(controller)){ throw new AssertException("already
         * added Controller, this a workflow bug: "+controller.getClass().getCanonicalName()); }
         */
        childControllers.add(controller);
        return controller;
    }

    /**
     * Remove this from the given controller as listener and dispose the controller. This should only be used for controllers that have previously been added with the
     * listenTo() method.
     * 
     * @param controller
     *            the controller to be disposed. When controller is NULL the method returns doing nothing, no exception will be thrown.
     * @throws AssertException
     *             if the controller is not contained.
     */

    protected void removeAsListenerAndDispose(Controller controller) {
        if (controller == null)
            return;
        /*
         * REVIEW:pb this is for quality and will be re-enabled after the OLAT 6.0.0 Release if(childControllers == null){ throw new AssertException("the controller you
         * want to remove was not added via listenTo(..) method"+controller.getClass().getCanonicalName()); } if(!childControllers.contains(controller)){ throw new
         * AssertException("the controller you want to remove does not or no longer reside here, this a workflow bug: "+controller.getClass().getCanonicalName()); }
         */
        childControllers.remove(controller);
        controller.dispose();
    }

    /**
     * convenience method: registers a mapper which will be automatically deregistered upon dispose of the controller
     * 
     * @param m
     *            the mapper that delivers the resources
     * @return The mapper base URL
     */

    protected String registerMapper(Mapper m) {
        return registerCacheableMapper(null, m);
    }

    /**
     * convenience method: registers a cacheable mapper which will be automatically deregistered upon dispose of the controller
     * 
     * @param cacheableMapperID
     *            the mapper ID that is used in the url to identify this mapper. Should be something that is derived from the context or resource that is delivered by the
     *            mapper
     * @param m
     *            the mapper that delivers the resources
     * @return The mapper base URL
     */
    protected String registerCacheableMapper(String cacheableMapperID, Mapper m) {
        if (mappers == null)
            mappers = new ArrayList<Mapper>(2);
        String mapperBaseURL;
        if (cacheableMapperID == null) {
            // use non cacheable as fallback
            mapperBaseURL = mreg.register(m);
        } else {
            mapperBaseURL = mreg.registerCacheable(cacheableMapperID, m);
        }
        // registration was successful, add to our mapper list
        mappers.add(m);
        return mapperBaseURL;
    }

    /**
     * Note: must not be called from doDispose(), all registered mappers are disposed automatically
     * 
     * @param m
     */
    protected void deregisterMapper(Mapper m) {
        boolean success = mappers != null && mappers.remove(m);
        if (!success)
            throw new AssertException("removing a mapper which was not registered");
        mreg.deregister(m);
    }

    /**
     * convenience method to generate a velocitycontainer
     * 
     * @param page
     *            the velocity page to use in the _content folder, e.g. "index", or "edit". The suffix ".html" gets automatically added to the page name e.g.
     *            "index.html".
     */
    protected VelocityContainer createVelocityContainer(String page) {
        return new VelocityContainer("vc_" + page, velocity_root + "/" + page + ".html", translator, this);
    }

    protected Panel putInitialPanel(Component initialContent) {
        Panel p = new Panel("mainPanel");
        p.setContent(initialContent);
        super.setInitialComponent(p);
        return p;
    }

    @Override
    protected void setInitialComponent(Component initialComponent) {
        throw new AssertException("please use method putInitialPanel!");
    }

    /**
     * creates and activates a dialog with YES / NO Buttons. usage:<br>
     * Do not call it from within a controllers constructor.<br>
     * call it as last method call of a workflow.<br>
     * <code>dialogBoxOne = createYesNoDialog(ureq, "Hello World", "Lorem ipsum dolor sit amet, sodales?", dialogBoxOne);</code> where the <code>dialogBoxOne</code> is
     * provided as parameter to be cleaned up correctly, e.g. removed as listener and disposed. The returned DialogBoxController should be hold as instance variable for
     * two reasons:
     * <ul>
     * <li>in the <code>event</code> method if then else block like <code>source == dialogBoxOne</code></li>
     * <li>to be cleaned up correctly if the "same" dialog box is showed again</li>
     * </ul>
     * <p>
     * The <code>title</code> is escaped at rendering but <code>text</code> should be escaped by the calling client.
     * 
     * @param ureq
     * @param title
     * @param text
     * @param dialogCtr
     * @return
     */
    protected DialogBoxController activateYesNoDialog(UserRequest ureq, String title, String text, DialogBoxController dialogCtr) {
        if (dialogCtr != null) {
            removeAsListenerAndDispose(dialogCtr);
        }
        dialogCtr = DialogBoxUIFactory.createYesNoDialog(ureq, getWindowControl(), title, text);
        listenTo(dialogCtr);
        dialogCtr.activate();
        return dialogCtr;
    }

    /**
     * creates and activates a dialog with OK / CANCEL Buttons. usage:<br>
     * Do not call it from within a controllers constructor.<br>
     * call it as last method call of a workflow.<br>
     * <code>dialogBoxOne = createYesNoDialog(ureq, "Hello World", "Lorem ipsum dolor sit amet, sodales?", dialogBoxOne);</code> where the <code>dialogBoxOne</code> is
     * provided as parameter to be cleaned up correctly, e.g. removed as listener and disposed. The returned DialogBoxController should be hold as instance variable for
     * two reasons:
     * <ul>
     * <li>in the <code>event</code> method if then else block like <code>source == dialogBoxOne</code></li>
     * <li>to be cleaned up correctly if the "same" dialog box is showed again</li>
     * </ul>
     * <p>
     * The <code>title</code> is escaped at rendering but <code>text</code> should be escaped by the calling client.
     * 
     * @param ureq
     * @param title
     * @param text
     * @param dialogCtr
     * @return
     */
    protected DialogBoxController activateOkCancelDialog(UserRequest ureq, String title, String text, DialogBoxController dialogCtr) {
        if (dialogCtr != null) {
            removeAsListenerAndDispose(dialogCtr);
        }
        dialogCtr = DialogBoxUIFactory.createOkCancelDialog(ureq, getWindowControl(), title, text);
        listenTo(dialogCtr);
        dialogCtr.activate();
        return dialogCtr;
    }

    /**
     * creates and activates a dialog with buttons provided in the <code>buttons</code> list parameter. usage:<br>
     * Do not call it from within a controllers constructor.<br>
     * call it as last method call of a workflow.<br>
     * <code>dialogBoxOne = createYesNoDialog(ureq, "Hello World", "Lorem ipsum dolor sit amet, sodales?", buttonLabelList, dialogBoxOne);</code> where the
     * <code>dialogBoxOne</code> is provided as parameter to be cleaned up correctly, e.g. removed as listener and disposed. The returned DialogBoxController should be
     * hold as instance variable for two reasons:
     * <ul>
     * <li>in the <code>event</code> method if then else block like <code>source == dialogBoxOne</code></li>
     * <li>to be cleaned up correctly if the "same" dialog box is showed again</li>
     * </ul>
     * <p>
     * The <code>title</code> is escaped at rendering but <code>text</code> should be escaped by the calling client.
     * 
     * @param ureq
     * @param title
     * @param text
     * @param buttonLabels
     * @param dialogCtr
     * @return
     */
    protected DialogBoxController activateGenericDialog(UserRequest ureq, String title, String text, List<String> buttonLabels, DialogBoxController dialogCtr) {
        if (dialogCtr != null) {
            removeAsListenerAndDispose(dialogCtr);
        }
        dialogCtr = DialogBoxUIFactory.createGenericDialog(ureq, getWindowControl(), title, text, buttonLabels);
        listenTo(dialogCtr);
        dialogCtr.activate();
        return dialogCtr;
    }

    /**
     * if you want to open a new browser window with content as defined with the ControllerCreator parameter. The new PopupBrowserWindow is created and opened. This
     * should be the last call in a method, make this clear by return immediate after the openInNewWindow line.<br>
     * 
     * @param ureq
     * @param windowContentCreator
     *            a creator which is used to create the windows content
     */
    protected PopupBrowserWindow openInNewBrowserWindow(UserRequest ureq, ControllerCreator windowContentCreator) {
        // open in new browser window
        PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, windowContentCreator);
        pbw.open(ureq);
        return pbw;
    }

    /**
     * @return Returns the identity.
     */
    protected Identity getIdentity() {
        return identity;
    }

    /**
     * @return Returns the locale.
     */
    protected Locale getLocale() {
        return locale;
    }

    /**
     * @return Returns the translator.
     */
    protected Translator getTranslator() {
        return translator;
    }

    /**
     * convenience method to inform the user. this will call
     * 
     * <pre>
     * getWindowControl().setInfo(getTranslator().translate(key));
     * </pre>
     * 
     * @param key
     *            the key to use (in the LocalStrings_curlanguage file of your controller)
     */
    protected void showInfo(String key) {
        getWindowControl().setInfo(getTranslator().translate(key));
    }

    /**
     * convenience method to inform the user. this will call
     * 
     * <pre>
     * getWindowControl().setInfo(getTranslator().translate(key, new String[] { arg }));
     * </pre>
     * <p>
     * This escapes <code> arg </code> because could be untrusted text <br>
     * and since the translation string could contain style, so we don't want to escape everything at rendering.
     * 
     * 
     * 
     * @param key
     *            the key to use (in the LocalStrings_curlanguage file of your controller)
     * @param arg
     */
    protected void showInfo(String key, String arg) {
        getWindowControl().setInfo(getTranslator().translate(key, new String[] { StringHelper.escapeHtml(arg) }));
    }

    /**
     * <p>
     * This escapes <code> arg </code> because could be untrusted text <br>
     * and since the translation string could contain style, so we don't want to escape everything.
     */
    protected void showInfo(String key, String[] args) {
        if (args != null) {
            // escape args
            for (int i = 0; i < args.length; i++) {
                args[i] = StringHelper.escapeHtml(args[i]);
            }
        }
        getWindowControl().setInfo(getTranslator().translate(key, args));
    }

    /**
     * convenience method to inform the user with a warning message. this will call
     * 
     * <pre>
     * getWindowControl().setWarning(getTranslator().translate(key, new String[] { arg }));
     * </pre>
     * 
     * @param key
     *            the key to use (in the LocalStrings_curlanguage file of your controller)
     */
    protected void showWarning(String key) {
        getWindowControl().setWarning(getTranslator().translate(key));
    }

    /**
     * convenience method to inform the user with a warning message. this will call
     * 
     * <pre>
     * getWindowControl().setWarning(getTranslator().translate(key, new String[] { arg }));
     * </pre>
     * <p>
     * This escapes <code> arg </code> because could be untrusted text <br>
     * and since the translation string could contain style, so we don't want to escape everything.
     * 
     * @param key
     *            the key to use (in the LocalStrings_curlanguage file of your controller)
     * @param arg
     */
    protected void showWarning(String key, String arg) {
        getWindowControl().setWarning(getTranslator().translate(key, new String[] { StringHelper.escapeHtml(arg) }));
    }

    /**
     * convenience method to send an error msg to the user. this will call
     * 
     * <pre>
     * getWindowControl().setError(getTranslator().translate(key));
     * </pre>
     * 
     * @param key
     *            the key to use (in the LocalStrings_curlanguage file of your controller)
     */
    protected void showError(String key) {
        getWindowControl().setError(getTranslator().translate(key));
    }

    /**
     * convenience method to send an error msg to the user. this will call
     * 
     * <pre>
     * getWindowControl().setError(getTranslator().translate(key, new String[] { arg }));
     * </pre>
     * <p>
     * This escapes <code> arg </code> because could be untrusted text <br>
     * and since the translation string could contain style, so we don't want to escape everything.
     * 
     * @param key
     *            the key to use (in the LocalStrings_curlanguage file of your controller)
     * @param arg
     */
    protected void showError(String key, String arg) {
        getWindowControl().setError(getTranslator().translate(key, new String[] { StringHelper.escapeHtml(arg) }));
    }

    /**
     * convenience method to translate with the built-in package translator of the controller
     * 
     * @param key
     *            the key to translate
     * @param arg
     *            the argument to pass in for the translation. if you have more than one argument, please use getTranslator().translate(....)
     * @return the translated string
     */
    protected String translate(String key, String arg) {
        return getTranslator().translate(key, new String[] { arg });
    }

    /**
     * convenience method to translate with the built-in package translator of the controller
     * 
     * @param key
     *            the key to translate
     * @return the translated string
     */
    protected String translate(String key) {
        return getTranslator().translate(key);
    }

    /**
     * convenience method to translate with the built-in package translator of the controller
     * 
     * @param key
     *            The key to translate
     * @param args
     *            Optional strings to insert into the translated string
     * @return The translated string
     */
    protected String translate(String key, String[] args) {
        return getTranslator().translate(key, args);
    }

    /**
     * provide your custom translator here if needed.<br>
     * <i>Hint</i><br>
     * try to avoid using this - and if, comment why.
     * 
     * @param translator
     */
    protected void setTranslator(Translator translator) {
        this.translator = translator;
    }

    /**
     * provide your custom velocity root here if needed<br>
     * <i>Hint</i><br>
     * try to avoid using this - and if, comment why.
     * 
     * @param velocityRoot
     */
    protected void setVelocityRoot(String velocityRoot) {
        this.velocity_root = velocityRoot;
    }

    /**
     * override the default base package, this is where all velocity pages and translations come from, etc. <br>
     * <i>Hint</i><br>
     * try to avoid using this - and if, comment why.
     */
    protected void setBasePackage(Class clazz) {
        setVelocityRoot(PackageUtil.getPackageVelocityRoot(clazz));
        if (fallbackTranslator != null) {
            setTranslator(PackageUtil.createPackageTranslator(clazz, getLocale(), fallbackTranslator));
        } else {
            setTranslator(PackageUtil.createPackageTranslator(clazz, getLocale()));
        }
    }

    /**
     * override the default locale.
     * 
     * @param locale
     *            The new locale
     * @param setLocaleOnTranslator
     *            true: the new locale is applied to the translator; false: the new locale is not applied to the translator
     */
    protected void setLocale(Locale locale, boolean setLocaleOnTranslator) {
        this.locale = locale;
        if (setLocaleOnTranslator) {
            getTranslator().setLocale(locale);
        }
    }

    /**
     * 
     * @param <T>
     * @param serviceInterface
     * @return
     */
    protected <T> T getService(LearnServices endUserService) {
        if (serviceLocator != null) {
            return (T) serviceLocator.getService(endUserService.getService());
        }
        return (T) CoreSpringFactory.getBean(endUserService.getService());
    }

}
