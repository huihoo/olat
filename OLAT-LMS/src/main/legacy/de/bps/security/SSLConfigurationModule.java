/**
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * Copyright (c) 2005-2008 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 * All rights reserved.
 */
package de.bps.security;

import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.olat.core.configuration.Initializable;
import org.olat.core.logging.Tracing;

public class SSLConfigurationModule implements Initializable {

	private static String keyStoreFile;
	private static String keyStorePass;
	private static String keyStoreType;
	private static String trustStoreFile;
	private static String trustStorePass;
	private static String trustStoreType;

	/**
	 * @param keyStoreFile The keyStoreFile to set.
	 */
	public void setKeyStoreFile(final String keyStoreFile) {
		SSLConfigurationModule.keyStoreFile = keyStoreFile;
	}

	/**
	 * @param keyStorePass The keyStorePass to set.
	 */
	public void setKeyStorePass(final String keyStorePass) {
		SSLConfigurationModule.keyStorePass = keyStorePass;
	}

	/**
	 * @param keyStoreType The keyStoreType to set.
	 */
	public void setKeyStoreType(final String keyStoreType) {
		SSLConfigurationModule.keyStoreType = keyStoreType;
	}

	/**
	 * @param trustStoreFile The trustStoreFile to set.
	 */
	public void setTrustStoreFile(final String trustStoreFile) {
		SSLConfigurationModule.trustStoreFile = trustStoreFile;
	}

	/**
	 * @param trustStorePass The trustStorePass to set.
	 */
	public void setTrustStorePass(final String trustStorePass) {
		SSLConfigurationModule.trustStorePass = trustStorePass;
	}

	/**
	 * @param trustStoreType The trustStoreType to set.
	 */
	public void setTrustStoreType(final String trustStoreType) {
		SSLConfigurationModule.trustStoreType = trustStoreType;
	}

	public SSLConfigurationModule() {
		super();
	}

	public static String getKeyStoreFile() {
		return keyStoreFile;
	}

	public static String getKeyStorePass() {
		return keyStorePass;
	}

	public static String getKeyStoreType() {
		return keyStoreType;
	}

	public static String getTrustStoreFile() {
		return trustStoreFile;
	}

	public static String getTrustStorePass() {
		return trustStorePass;
	}

	public static String getTrustStoreType() {
		return trustStoreType;
	}

	/**
	 * @see org.olat.core.configuration.Initializable#init()
	 */
	@Override
	public void init() {
		System.setProperty("javax.net.ssl.trustStore", SSLConfigurationModule.getTrustStoreFile());
		System.setProperty("javax.net.ssl.trustStorePassword", SSLConfigurationModule.getTrustStorePass());
		System.setProperty("javax.net.ssl.keyStore", SSLConfigurationModule.getKeyStoreFile());
		System.setProperty("javax.net.ssl.keyStorePassword", SSLConfigurationModule.getKeyStorePass());
	}

	public static KeyManager[] getKeyManagers() {
		try {
			final KeyStore keyStore = KeyStore.getInstance(keyStoreType);
			final FileInputStream kStream = new FileInputStream(keyStoreFile);
			keyStore.load(kStream, keyStorePass.toCharArray());
			final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			keyManagerFactory.init(keyStore, keyStorePass.toCharArray());
			return keyManagerFactory.getKeyManagers();
		} catch (final Exception e) {
	private static final Logger log = LoggerHelper.getLogger();

			e.printStackTrace();
			return null;
		}
	}

	public static TrustManager[] getTrustManagers() {
		try {
			final KeyStore trustStore = KeyStore.getInstance(trustStoreType);
			final FileInputStream tStream = new FileInputStream(trustStoreFile);
			trustStore.load(tStream, trustStorePass.toCharArray());
			final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
			trustManagerFactory.init(trustStore);
			return trustManagerFactory.getTrustManagers();
		} catch (final Exception e) {
	private static final Logger log = LoggerHelper.getLogger();

			return null;
		}
	}
}
