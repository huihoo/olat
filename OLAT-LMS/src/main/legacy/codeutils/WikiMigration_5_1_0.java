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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olat.core.util.FileUtils;
import org.olat.modules.wiki.WikiManager;
import org.olat.modules.wiki.gui.components.wikiToHtml.FilterUtil;

/**
 * @author Christian Guretzki
 */
public class WikiMigration_5_1_0 {

	private static boolean testMode = false;
	private static boolean debugMode = false;
	private static Hashtable fileRenamingList;

	public WikiMigration_5_1_0() {

	}

	public static void main(final String[] args) {
		System.out.println("> WikiMigration_5_1_0  V1.0.6  19.02.2007");
		System.out.println("> -------------------");
		if (testMode) {
			doTest();
			System.exit(0);
		}
		// 1. Read Filelist
		if (args.length == 0) {
			System.err.println("Missing argument filename. java ch.unizh.codeutils.WikiMigration_5_1_0 inputFileName");
			System.err.println("  Options : -DEBUGMODE java ch.unizh.codeutils.WikiMigration_5_1_0 inputFileName -DEBUGMODE");
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
			String path = null;
			fileRenamingList = new Hashtable();

			while ((path = inputFile.readLine()) != null) {
				testOut("process path=" + path);
				// get pagename from wiki.properties file
				final String wikiPropertiesFileName = path.substring(0, path.length() - ".wp".length()) + ".properties";
				final Properties wikiProperties = new Properties();
				wikiProperties.load(new FileInputStream(wikiPropertiesFileName));
				final String pageName = wikiProperties.getProperty("pagename");
				log("migrate wiki with pagename=" + pageName, path);
				// 2.1. Read File Content
				final FileInputStream fis = new FileInputStream(path);
				final BufferedInputStream bis = new BufferedInputStream(fis);
				final String content = FileUtils.load(bis, "utf-8");
				final String migratedContent = doMigrate(content, path);
				// 2.2 Write migrated Content to File
				FileUtils.save(new File(path), migratedContent, "utf-8");
			}
			// ok now all file-content is migrated, now we can rename wiki files => loop over all files in fileRenamingList
			log.debug("TEST: fileRenamingList.size()=" + fileRenamingList.size(), "");
			final Enumeration enumeration = fileRenamingList.keys();
			while (enumeration.hasMoreElements()) {
				final String oldWikiFileName = (String) enumeration.nextElement();
				final String newWikiFileName = (String) fileRenamingList.get(oldWikiFileName);
				renameFile(oldWikiFileName, newWikiFileName);
			}

		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private static String doMigrate(final String content, final String path) {
		testOut("Input content:" + content);
		testOut("---");

		// 2.2 Migrate content
		final String migratedContentColon = doMigrateColon(content, path);
		testOut("migratedContentColon:" + migratedContentColon);
		testOut("---");
		final String migratedContentImage = doMigrateImageTag(migratedContentColon, path);
		testOut("migratedContentImage:" + migratedContentImage);
		testOut("---");
		final String migratedDoubleQuote = doMigrateDoubleQuote(migratedContentImage, path);
		testOut("migratedDoubleQuote:" + migratedDoubleQuote);
		testOut("---");
		final String migratedContent = doMigrateAmpersand(migratedDoubleQuote, path);
		testOut("Migrated content:" + migratedContent);
		testOut("---");
		final String migratedQuestionMark = doMigrateQuestionmark(migratedContent, path);
		testOut("Migrated content:" + migratedQuestionMark);
		testOut("---");
		return migratedQuestionMark;
	}

	private static String doMigrateAmpersand(String content, final String path) {
		final Pattern WIKI_LINK_PATTERN = Pattern.compile("\\[\\[[^\\]]*[^\\]]*\\]\\]");
		final Matcher m = WIKI_LINK_PATTERN.matcher(content);
		final List links = new ArrayList();

		while (m.find()) {
			final String link = content.substring(m.start(), m.end());
			if (!link.startsWith("[[Image:") && !link.startsWith("[[Media:") && (link.indexOf("http://") == -1) && (link.indexOf('&') != -1)) {
				testOut("Migrate ampersand in: " + link);
				links.add(link); // no image, link has '&'
			}
		}
		for (final Iterator iter = links.iterator(); iter.hasNext();) {
			final String link = (String) iter.next();
			if (link.indexOf("&amp;") == -1) {
				final String newWikiLink = link.replace("&", "&amp;");
				content = content.replace(link, newWikiLink);
				log("Replace '&' in wiki link old='" + link + "' new='" + newWikiLink + "'", path);
			} else {
				log("Replace '&' : link=" + link + "  has already '&amp;' !", path);
			}
		}
		return content;
	}

	private static String doMigrateColon(String content, final String path) {
		final Pattern WIKI_LINK_PATTERN = Pattern.compile("\\[\\[[^\\]]*[^\\]]*\\]\\]");
		final Matcher m = WIKI_LINK_PATTERN.matcher(content);
		final List links = new ArrayList();

		while (m.find()) {
			final String link = content.substring(m.start(), m.end());
			if (!link.startsWith("[[Image:") && !link.startsWith("[[Media:") && (link.indexOf("http://") == -1) && (link.indexOf(':') != -1)) {
				testOut("Migrate colon in: " + link);
				links.add(link); // no image, link has ':'
			}
		}
		for (final Iterator iter = links.iterator(); iter.hasNext();) {
			final String link = (String) iter.next();
			final String newWikiLink = link.replace(':', '/');
			content = content.replace(link, newWikiLink);
			log("Replace ':'  in wiki link old='" + link + "' new='" + newWikiLink + "'", path);
		}
		return content;
	}

	/**
	 * New Wiki parser default alignment for images is left. In version 5.0 is was per default right. Search for all [[Image:xxxx.yyy]] and migrate this to
	 * [[Image:xxxx.yyy|right]]
	 * 
	 * @param content
	 * @return migrated content
	 */
	private static String doMigrateImageTag(String content, final String path) {
		final Pattern WIKI_LINK_PATTERN = Pattern.compile("\\[\\[[^\\]]*Image:[^\\]]*\\]\\]");
		final Matcher m = WIKI_LINK_PATTERN.matcher(content);
		final List links = new ArrayList();

		while (m.find()) {
			final String link = content.substring(m.start(), m.end());
			if (!link.endsWith("|right]]") && !link.endsWith("|left]]") && !link.endsWith("|center]]")) {
				testOut("ImageTag to be replaced: " + link);
				links.add(link);
			}
		}
		for (final Iterator iter = links.iterator(); iter.hasNext();) {
			final String link = (String) iter.next();
			final String newWikiLink = link.substring(0, link.length() - 2) + "|right]]";
			content = content.replace(link, newWikiLink);
			log("Append '|right' in wiki link old='" + link + "' new='" + newWikiLink + "'", path);
		}
		return content;
	}

	private static String doMigrateDoubleQuote(String content, final String path) {
		final Pattern WIKI_LINK_PATTERN = Pattern.compile("\\[\\[[^\\]]*[^\\]]*\\]\\]");

		final Matcher m = WIKI_LINK_PATTERN.matcher(content);

		final List links = new ArrayList();

		while (m.find()) {
			String link = content.substring(m.start(), m.end());
			if (!link.startsWith("[[Image:") && !link.startsWith("[[Media:") && (link.indexOf('"') != -1)) {
				testOut("Migrate DoubleQuote in: " + link);
				// no image and link with doubleQuote
				// check for '|' => [[link|text]] extract link
				if (link.indexOf('|') != -1) {
					link = link.substring("[[".length(), link.indexOf('|'));
					if (link.indexOf('"') != -1) {
						links.add(link);// doubleQuote in link
					} else {
						// doubleQuote in text => do not add link
					}
				} else {
					links.add(link); // no '|' => doubleQuote in link
				}
			}
		}
		for (final Iterator iter = links.iterator(); iter.hasNext();) {
			String link = (String) iter.next(); // link = [[Name]]

			final String newWikiWord = link.replace('"', '\'');
			content = content.replace(link, newWikiWord);
			log("Replace '\"' in wiki link old='" + link + "' new='" + newWikiWord + "'", path);

			// Replace " with '&quot;' to generate wiki filename
			link = link.replaceAll("\"", "&quot;");
			final String oldWikiFileName = generateWikiFileName(link);
			final String oldWikiPropertiesFileName = generateWikiPropertiesFileName(link);
			final String newWikiFileName = generateWikiFileName(newWikiWord);
			final String newWikiPropertiesFileName = generateWikiPropertiesFileName(newWikiWord);
			log("Old Wiki word='" + link + "' new Wiki word='" + newWikiWord + "'", path);
			final String dirPath = path.substring(0, path.lastIndexOf("/"));
			renamePageNameInPropertiesFile(dirPath + File.separator + oldWikiPropertiesFileName, newWikiWord);
			fileRenamingList.put(dirPath + File.separator + oldWikiFileName, dirPath + File.separator + newWikiFileName);
			fileRenamingList.put(dirPath + File.separator + oldWikiPropertiesFileName, dirPath + File.separator + newWikiPropertiesFileName);
			log.debug("fileRenamingList put key='" + dirPath + File.separator + oldWikiFileName + "' value='" + dirPath + File.separator + newWikiFileName + "'", "");
		}
		return content;
	}

	private static String doMigrateQuestionmark(String content, final String path) {
		final Pattern WIKI_LINK_PATTERN = Pattern.compile("\\[\\[[^\\]]*[^\\]]*\\]\\]");

		final Matcher m = WIKI_LINK_PATTERN.matcher(content);

		final List links = new ArrayList();

		while (m.find()) {
			String link = content.substring(m.start(), m.end());
			if (!link.startsWith("[[Image:") && !link.startsWith("[[Media:") && (link.indexOf("?") != -1)) {
				testOut("Migrate Questionmark in: " + link);
				// no image and link with doubleQuote
				// check for '|' => [[link|text]] extract link
				if (link.indexOf('|') != -1) {
					link = link.substring("[[".length(), link.indexOf('|'));
					if (link.indexOf("?") != -1) {
						links.add(link);// Questionmark in link
					} else {
						// Questionmark in text => do not add link
					}
				} else {
					links.add(link); // no '|' => doubleQuote in link
				}
			}
		}
		for (final Iterator iter = links.iterator(); iter.hasNext();) {
			final String link = (String) iter.next(); // link = [[Name]]

			final String newWikiWord = link.replace("?", "");
			content = content.replace(link, newWikiWord);
			log("Replace '?' in wiki link old='" + link + "' new='" + newWikiWord + "'", path);

			// link = link.replaceAll("\"", "&quot;");
			final String oldWikiFileName = generateWikiFileName(link);
			final String oldWikiPropertiesFileName = generateWikiPropertiesFileName(link);
			final String newWikiFileName = generateWikiFileName(newWikiWord);
			final String newWikiPropertiesFileName = generateWikiPropertiesFileName(newWikiWord);
			log("Old Wiki word='" + link + "' new Wiki word='" + newWikiWord + "'", path);
			final String dirPath = path.substring(0, path.lastIndexOf("/"));
			renamePageNameInPropertiesFile(dirPath + File.separator + oldWikiPropertiesFileName, newWikiWord);
			fileRenamingList.put(dirPath + File.separator + oldWikiFileName, dirPath + File.separator + newWikiFileName);
			fileRenamingList.put(dirPath + File.separator + oldWikiPropertiesFileName, dirPath + File.separator + newWikiPropertiesFileName);
			log.debug("fileRenamingList put key='" + dirPath + File.separator + oldWikiFileName + "' value='" + dirPath + File.separator + newWikiFileName + "'", "");
		}
		return content;
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

	private static void testOut(final String output) {
		if (testMode) {
			System.out.println(">" + output);
		}
	}

	private static void log(final String output, final String path) {
		System.out.println(path + ":" + output);
	}

	private static void doTest() {
		final String content = "In diesem Winter hat es [[Davos]] und [[Unter:Engadin:Sent]] wenig Schnee [[Image:snow.jpg]] aber ev."
				+ " kommt er noch\n[[Image:blabla_bli.jpg]]. Weiter Wiki Woerter mit [[\"Doppelten\"Anfuehrungszeichen]] und so fort [[\"Zitat\"]]"
				+ " und so fort [[StandardWort]]";

		final String migratedContent = doMigrate(content, "test");

	}

}
