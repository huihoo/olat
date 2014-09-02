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
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package ch.unizh.codeutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Properties;

import org.olat.modules.wiki.WikiManager;
import org.olat.modules.wiki.gui.components.wikiToHtml.FilterUtil;

/**
 * @author Christian Guretzki
 */
public class WikiWordMigration_5_1_0 {

	private static boolean debugMode = false;

	public static void main(final String[] args) {
		System.out.println("> WikiWordMigration_5_1_0  V1.0.0  22.02.2007");
		System.out.println("> -------------------------------------------");
		// 1. Read Filelist
		if (args.length == 0) {
			System.err.println("Missing argument filename. java ch.unizh.codeutils.WikiWordMigration_5_1_0 inputFileName");
			System.err.println("  Options : -DEBUGMODE java ch.unizh.codeutils.WikiWordMigration_5_1_0 inputFileName -DEBUGMODE");
			System.exit(1);
		}
		// check for argumwnt debug Mode
		if (args.length > 1) {
			if (args[1].equalsIgnoreCase("-DEBUGMODE")) {
				debugMode = true;
			}
		}

		try {
			final String inputFileName = args[0];
			final RandomAccessFile inputFile = new RandomAccessFile(inputFileName, "r");
			// 2. Loop over all files
			String wikiPropertiesFileName = null;

			while ((wikiPropertiesFileName = inputFile.readLine()) != null) {
				log("process path=" + wikiPropertiesFileName, "");
				// get pagename from wiki.properties file
				final String oldWikiFileName = wikiPropertiesFileName.substring(0, wikiPropertiesFileName.length() - ".properties".length()) + ".wp";
				final Properties wikiProperties = new Properties();
				wikiProperties.load(new FileInputStream(wikiPropertiesFileName));
				final String pageName = wikiProperties.getProperty("pagename");
				if (pageName.endsWith("_")) {
					final String newPageName = pageName.substring(0, pageName.length() - 1); // remove '_' at the end
					doMigrate(wikiPropertiesFileName, newPageName, pageName, oldWikiFileName);
				} else if (pageName.indexOf("?") != -1) {
					final String newPageName = pageName.replaceAll("\\?", ""); // replace all ?
					doMigrate(wikiPropertiesFileName, newPageName, pageName, oldWikiFileName);
				}
			}
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private static void doMigrate(final String wikiPropertiesFileName, final String newPageName, final String pageName, final String oldWikiFileName) {
		log("migrate wiki with pagename=" + pageName + " to pagename=" + newPageName, wikiPropertiesFileName);
		final String dirPath = wikiPropertiesFileName.substring(0, wikiPropertiesFileName.lastIndexOf("/"));
		renamePageNameInPropertiesFile(wikiPropertiesFileName, newPageName);
		final String newWikiFileName = generateWikiFileName(newPageName);
		final String newWikiPropertiesFileName = generateWikiPropertiesFileName(newPageName);
		renameFile(wikiPropertiesFileName, dirPath + File.separator + newWikiPropertiesFileName);
		renameFile(oldWikiFileName, dirPath + File.separator + newWikiFileName);
	}

	private static void renamePageNameInPropertiesFile(final String oldWikiPropertiesFileName, final String newWikiWord) {
		final Properties p = new Properties();
		try {
			final FileInputStream fis = new FileInputStream(new File(oldWikiPropertiesFileName));
			p.load(fis);
			fis.close();
			p.setProperty(WikiManager.PAGENAME, removeLinkTags(newWikiWord));
			log.debug("TEST.renamePageNameInPropertiesFile: oldWikiPropertiesFileName=" + oldWikiPropertiesFileName + "  newWikiWord=" + newWikiWord, "");
			final FileOutputStream fos = new FileOutputStream(new File(oldWikiPropertiesFileName));
			p.store(fos, "wiki page meta properties");
			fos.close();
		} catch (final IOException e) {
			log("WARN: Wiki properties couldn't be read! Pagename:" + oldWikiPropertiesFileName, "");
		}
	}

	private static void renameFile(final String oldWikiFileName, final String newWikiFileName) {
		log("RenameFile oldWikiFileName='" + oldWikiFileName + "'  newWikiFileName='" + newWikiFileName + "'", "");
		final File existingWikiFile = new File(oldWikiFileName);
		if (existingWikiFile.exists()) {
			final File renamedWikiFile = new File(newWikiFileName);
			if (renamedWikiFile.exists()) {
				log("WARN: New Wiki File already exists; Rename wiki file from '" + oldWikiFileName + "' to '" + newWikiFileName + "'", "");
			}
			existingWikiFile.renameTo(renamedWikiFile);
		} else {
			log("File oldWikiFileName='" + oldWikiFileName + "' does not exit", "");
		}
	}

	private static String generateWikiFileName(final String wikiLink) {
		return generatePageId(wikiLink) + ".wp";
	}

	private static String generateWikiPropertiesFileName(final String wikiLink) {
		return generatePageId(wikiLink) + ".properties";
	}

	private static String generatePageId(final String wikiLink) {
		final String wikiWord = removeLinkTags(wikiLink);
		final String pageId = WikiManager.generatePageId(FilterUtil.normalizeWikiLink(wikiWord));
		log.debug("TEST.generatePageId wikiWord='" + wikiWord + "'  pageId=" + pageId, "");
		return pageId;
	}

	private static void log.debug(final String message, final String path) {
		if (debugMode) {
			log(message, path);
		}
	}

	private static String removeLinkTags(String wikiLink) {
		if (wikiLink.startsWith("[[")) {
			wikiLink = wikiLink.substring("[[".length(), wikiLink.length());
		}
		if (wikiLink.endsWith("]]")) {
			wikiLink = wikiLink.substring(0, wikiLink.length() - "]]".length());
		}
		return wikiLink;
	}

	private static void log(final String output, final String path) {
		System.out.println(path + ":" + output);
	}

}
