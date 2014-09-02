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

package org.olat.data.filebrowser.metadata;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.filter.FilterFactory;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.vfs.LocalFileImpl;
import org.olat.data.commons.vfs.OlatRelPathImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.xml.XMLParser;
import org.olat.data.filebrowser.thumbnail.CannotGenerateThumbnailException;
import org.olat.data.filebrowser.thumbnail.FinalSize;
import org.olat.data.filebrowser.thumbnail.ThumbnailService;
import org.olat.system.commons.StringHelper;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Initial Date: 08.07.2003
 * 
 * @author Mike Stock<br>
 *         Comment: Meta files are in a shadow file system with the same directory structure as their original files. Meta info for directories is stored in a file called
 *         ".xml" residing in the respective directory. Meta info for files is stored in a file with ".xml" appended to its filename.
 * @author oliver.buehler@agility-informatik.ch
 */
public class MetaInfoFileImpl extends DefaultHandler implements MetaInfo {

    private static final Logger log = LoggerHelper.getLogger();

    private static SAXParserFactory SAX_PARSER_FACTORY = SAXParserFactory.newInstance();

    private final ThumbnailService thumbnailService;
    private final BaseSecurity baseSecurity;
    private final File originFile;
    private final File metaFile;

    private SAXParser saxParser;

    // meta data
    private Long authorIdentKey = null;
    private Long lockedByIdentKey = null;
    private String comment = "";
    private String title, publisher, creator, source, city, pages, language, url, pubMonth, pubYear;
    private Date lockedDate;
    private int downloadCount;
    private boolean locked;

    private boolean cannotGenerateThumbnail = false;
    private final List<Thumbnail> thumbnails = new ArrayList<Thumbnail>();

    /**
     * always access via metaInfoFactory
     * 
     * @param metaFile
     */
    public MetaInfoFileImpl(ThumbnailService thumbnailService, BaseSecurity baseSecurity, final OlatRelPathImpl olatRelPathImpl) {
        this.thumbnailService = thumbnailService;
        this.baseSecurity = baseSecurity;

        originFile = new File(FolderConfig.getCanonicalRoot() + olatRelPathImpl.getRelPath());
        if (!originFile.exists()) {
            throw new AssertException("Base file for meta information does not exist [" + originFile.getAbsolutePath() + "].");
        }

        final String canonicalMetaPath = getCanonicalMetaPath(olatRelPathImpl);
        metaFile = new File(canonicalMetaPath);

        // create meta data file if necessary
        if (!metaFile.exists() || metaFile.isDirectory()) {
            final String metaDirPath = canonicalMetaPath.substring(0, canonicalMetaPath.lastIndexOf('/'));
            new File(metaDirPath).mkdirs();
            write();
        }

        parseSAX(metaFile);
    }

    /**
     * Get the canonical path to the file's meta file.
     * 
     * @param bcPath
     * @return String
     */
    private String getCanonicalMetaPath(final OlatRelPathImpl olatRelPathImpl) {
        if (originFile.isDirectory()) {
            return FolderConfig.getCanonicalMetaRoot() + olatRelPathImpl.getRelPath() + "/.xml";
        }
        return FolderConfig.getCanonicalMetaRoot() + olatRelPathImpl.getRelPath() + ".xml";
    }

    /**
     * Rename the given meta info file
     * 
     * @param meta
     * @param newName
     */
    @Override
    public void rename(final String newName) {
        // rename meta info file name
        if (isDirectory()) { // rename the directory, which is the parent of the actual ".xml" file
            final File metaFileDirectory = metaFile.getParentFile();
            metaFileDirectory.renameTo(new File(metaFileDirectory.getParentFile(), newName));
        } else { // rename the file
            metaFile.renameTo(new File(metaFile.getParentFile(), newName + ".xml"));
        }
    }

    /**
     * Move/Copy the given meta info to the target directory.
     * 
     * @param targetDir
     * @param move
     */
    @Override
    public void moveCopyToDir(final OlatRelPathImpl target, final boolean move) {
        File fSource = metaFile;

        if (isDirectory()) { // move/copy whole meta directory
            File fTarget = new File(getCanonicalMetaPath(target));
            fSource = fSource.getParentFile();
            fTarget = fTarget.getParentFile();
            move(move, fSource, fTarget);
        } else if (target instanceof VFSContainer) {
            File fTarget = new File(FolderConfig.getCanonicalMetaRoot() + target.getRelPath());
            // getCanonicalMetaPath give the path to the xml file where the metadatas are saved
            if (fTarget.getName().equals(".xml")) {
                fTarget = fTarget.getParentFile();
            }
            move(move, fSource, fTarget);
        }

    }

    private void move(boolean move, File fSource, File fTarget) {
        if (move) {
            FileUtils.moveFileToDir(fSource, fTarget);
        } else {
            FileUtils.copyFileToDir(fSource, fTarget, "meta info");
        }
    }

    /**
     * Delete all associated meta info including sub files/directories
     * 
     * @param meta
     */
    @Override
    public void deleteAll() {
        if (isDirectory()) { // delete whole meta directory (where the ".xml" resides within)
            FileUtils.deleteDirsAndFiles(metaFile.getParentFile(), true, true);
        } else { // delete this single meta file
            delete();
        }
    }

    /**
     * Copy values from fromMeta into this object except name.
     * 
     * @param fromMeta
     */
    @Override
    public void copyValues(final MetaInfo fromMeta) {
        this.setAuthor(fromMeta.getAuthor());
        this.setComment(fromMeta.getComment());
        this.setCity(fromMeta.getCity());
        this.setCreator(fromMeta.getCreator());
        this.setLanguage(fromMeta.getLanguage());
        this.setPages(fromMeta.getPages());
        this.setPublicationDate(fromMeta.getPublicationDate()[1], fromMeta.getPublicationDate()[0]);
        this.setPublisher(fromMeta.getPublisher());
        this.setSource(fromMeta.getSource());
        this.setTitle(fromMeta.getTitle());
        this.setUrl(fromMeta.getUrl());
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    @Override
    public void setLocked(final boolean locked) {
        this.locked = locked;
        if (!locked) {
            lockedByIdentKey = null;
            lockedDate = null;
        }
    }

    @Override
    public Identity getLockedByIdentity() {
        if (lockedByIdentKey != null) {
            final Identity identity = baseSecurity.loadIdentityByKey(lockedByIdentKey);
            return identity;
        }
        return null;
    }

    @Override
    public Long getLockedBy() {
        return lockedByIdentKey;
    }

    @Override
    public void setLockedBy(final Long lockedBy) {
        this.lockedByIdentKey = lockedBy;
    }

    @Override
    public Date getLockedDate() {
        return lockedDate;
    }

    @Override
    public void setLockedDate(final Date lockedDate) {
        this.lockedDate = lockedDate;
    }

    /**
     * Writes the meta data to file. If no changes have been made, does not write anything.
     * 
     * @return True upon success.
     */
    @Override
    public boolean write() {
        BufferedOutputStream bos = null;
        if (metaFile == null) {
            return false;
        }
        try {
            bos = new BufferedOutputStream(new FileOutputStream(metaFile));
            final OutputStreamWriter sw = new OutputStreamWriter(bos, Charset.forName("UTF-8"));
            sw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            sw.write("<meta>");
            sw.write("<author><![CDATA[" + (authorIdentKey == null ? "" : authorIdentKey.toString()) + "]]></author>");
            sw.write("<lock locked=\"" + locked + "\"" + (lockedDate == null ? "" : " date=\"" + lockedDate.getTime() + "\"") + "><![CDATA["
                    + (lockedByIdentKey == null ? "" : lockedByIdentKey) + "]]></lock>");
            sw.write("<comment><![CDATA[" + filterForCData(comment) + "]]></comment>");
            sw.write("<title><![CDATA[" + filterForCData(title) + "]]></title>");
            sw.write("<publisher><![CDATA[" + filterForCData(publisher) + "]]></publisher>");
            sw.write("<creator><![CDATA[" + filterForCData(creator) + "]]></creator>");
            sw.write("<source><![CDATA[" + filterForCData(source) + "]]></source>");
            sw.write("<city><![CDATA[" + filterForCData(city) + "]]></city>");
            sw.write("<pages><![CDATA[" + filterForCData(pages) + "]]></pages>");
            sw.write("<language><![CDATA[" + filterForCData(language) + "]]></language>");
            sw.write("<url><![CDATA[" + filterForCData(url) + "]]></url>");
            sw.write("<publicationDate><month><![CDATA[" + (pubMonth != null ? pubMonth.trim() : "") + "]]></month><year><![CDATA["
                    + (pubYear != null ? pubYear.trim() : "") + "]]></year></publicationDate>");
            sw.write("<downloadCount><![CDATA[" + downloadCount + "]]></downloadCount>");
            sw.write("<thumbnails cannotGenerateThumbnail=\"" + cannotGenerateThumbnail + "\">");
            for (final Thumbnail thumbnail : thumbnails) {
                sw.write("<thumbnail maxHeight=\"");
                sw.write(Integer.toString(thumbnail.getMaxHeight()));
                sw.write("\" maxWidth=\"");
                sw.write(Integer.toString(thumbnail.getMaxWidth()));
                sw.write("\" finalHeight=\"");
                sw.write(Integer.toString(thumbnail.getFinalHeight()));
                sw.write("\" finalWidth=\"");
                sw.write(Integer.toString(thumbnail.getFinalWidth()));
                sw.write("\">");
                sw.write("<![CDATA[" + thumbnail.getThumbnailFile().getName() + "]]>");
                sw.write("</thumbnail>");
            }
            sw.write("</thumbnails>");
            sw.write("</meta>");
            sw.close();
        } catch (final Exception e) {
            return false;
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (final IOException e) {
                    log.warn("Can not close stream, " + e.getMessage());
                }
            }
        }
        return true;
    }

    private String filterForCData(final String original) {
        if (StringHelper.containsNonWhitespace(original)) {
            return FilterFactory.getXMLValidCharacterFilter().filter(original);
        }
        return "";
    }

    /**
     * Delete this meta info
     * 
     * @return True upon success.
     */
    @Override
    public boolean delete() {
        if (metaFile == null) {
            return false;
        }
        for (final Thumbnail thumbnail : thumbnails) {
            final File file = thumbnail.getThumbnailFile();
            if (file != null && file.exists()) {
                file.delete();
            }
        }
        return metaFile.delete();
    }

    /**
     * @param fMeta
     * @return
     */
    private void parseSAX(final File fMeta) {
        try {
            saxParser = SAX_PARSER_FACTORY.newSAXParser();
        } catch (Exception e) {
            // should really not happen since we have a default parser configuration
            log.error("Unexpected error creating SAXParser", e);
            return;
        }

        InputStream in = null;
        try {
            in = new FileInputStream(fMeta);
            saxParser.parse(in, this);
        } catch (final SAXParseException ex) {
            if (!parseSAXFiltered(fMeta)) {
                // OLAT-5383,OLAT-5468: lowered error to warn to reduce error noise
                log.warn("SAX Parser error while parsing " + fMeta, ex);
            }
        } catch (final Exception ex) {
            log.error("Error while parsing " + fMeta, ex);
        } finally {
            FileUtils.closeSafely(in);
        }
    }

    /**
     * Try to rescue xml files with invalid characters
     * 
     * @param fMeta
     * @return true if rescue is successful
     */
    private boolean parseSAXFiltered(final File fMeta) {
        final String original = FileUtils.load(fMeta, "UTF-8");
        if (original == null) {
            return false;
        }

        final String filtered = FilterFactory.getXMLValidCharacterFilter().filter(original);
        if (original != null && !original.equals(filtered)) {
            try {
                final InputSource in = new InputSource(new StringReader(filtered));
                saxParser.parse(in, this);
                write();// update with the new filtered write method
                return true;
            } catch (final Exception e) {
                // only a fallback, fail silently
            }
        }
        return false;
    }

    /**
     * Parse XML from file with SAX and fill-in MetaInfo attributes.
     * 
     * @param fMeta
     */
    @Deprecated
    public boolean parseXMLdom(final File fMeta) {
        if (fMeta == null || !fMeta.exists()) {
            return false;
        }
        InputStream is;
        try {
            is = new BufferedInputStream(new FileInputStream(fMeta));
        } catch (final FileNotFoundException e) {
            return false;
        }

        try {
            final XMLParser xmlp = new XMLParser();
            final Document doc = xmlp.parse(is, false);
            if (doc == null) {
                return false;
            }

            // extract data from XML
            final Element root = doc.getRootElement();
            Element n;
            n = root.element("author");
            if (n == null) {
                authorIdentKey = null;
            } else {
                if (n.getText().length() == 0) {
                    authorIdentKey = null;
                } else {
                    try {
                        authorIdentKey = Long.valueOf(n.getText());
                    } catch (final NumberFormatException nEx) {
                        authorIdentKey = null;
                    }
                }
            }
            n = root.element("comment");
            comment = (n != null) ? n.getText() : "";
            final Element lockEl = root.element("lock");
            if (lockEl != null) {
                locked = "true".equals(lockEl.attribute("locked").getValue());
                try {
                    lockedByIdentKey = new Long(n.getText());
                } catch (final NumberFormatException nEx) {
                    lockedByIdentKey = null;
                }
            }
            n = root.element("title");
            title = (n != null) ? n.getText() : "";
            n = root.element("publisher");
            publisher = (n != null) ? n.getText() : "";
            n = root.element("source");
            source = (n != null) ? n.getText() : "";
            n = root.element("creator");
            creator = (n != null) ? n.getText() : "";
            n = root.element("city");
            city = (n != null) ? n.getText() : "";
            n = root.element("pages");
            pages = (n != null) ? n.getText() : "";
            n = root.element("language");
            language = (n != null) ? n.getText() : "";
            n = root.element("url");
            url = (n != null) ? n.getText() : "";
            n = root.element("downloadCount");
            downloadCount = (n != null) ? Integer.valueOf(n.getText()) : 0;
            n = root.element("publicationDate");
            if (n != null) {
                Element m = n.element("month");
                pubMonth = (m != null) ? m.getText() : "";
                m = n.element("year");
                pubYear = (m != null) ? m.getText() : "";
            }
            return true;
        } catch (final Exception ex) {
            log.warn("Corrupted metadata file: " + fMeta);
            return false;
        }
    }

    /* ------------------------- Getters ------------------------------ */

    /**
     * @return name of the initial author
     */
    @Override
    public String getAuthor() {
        if (authorIdentKey == null) {
            return "-";
        } else {
            try {
                final Identity identity = baseSecurity.loadIdentityByKey(authorIdentKey);
                if (identity == null) {
                    log.warn("Found no idenitiy with key='" + authorIdentKey + "'");
                    return "-";
                }
                return identity.getName();
            } catch (final Exception e) {
                return "-";
            }
        }
    }

    @Override
    public Identity getAuthorIdentity() {
        if (authorIdentKey == null) {
            return null;
        } else {
            try {
                return baseSecurity.loadIdentityByKey(this.authorIdentKey);
            } catch (final Exception e) {
                log.warn("Found no identity with key='" + authorIdentKey + "'");
                return null;
            }
        }
    }

    /**
     * @return comment
     */
    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public String getName() {
        return originFile.getName();
    }

    /**
     * @return True if this is a directory
     */
    @Override
    public boolean isDirectory() {
        return originFile.isDirectory();
    }

    /**
     * @return Last modified timestamp
     */
    @Override
    public long getLastModified() {
        return originFile.lastModified();
    }

    /**
     * @return size of file
     */
    @Override
    public long getSize() {
        return originFile.length();
    }

    /**
     * @return formatted representation of size of file
     */
    @Override
    public String getFormattedSize() {
        return StringHelper.formatMemory(getSize());
    }

    /* ------------------------- Setters ------------------------------ */

    /**
     * @param string
     */
    @Override
    public void setAuthor(final String username) {
        final Identity identity = baseSecurity.findIdentityByName(username);
        if (identity == null) {
            log.warn("Found no identity with username='" + username + "'");
            authorIdentKey = null;
            return;
        }
        authorIdentKey = identity.getKey();
    }

    /**
     * @param string
     */
    @Override
    public void setComment(final String string) {
        comment = string;
    }

    /**
	 */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Name [" + getName());
        sb.append("] Author [" + getAuthor());
        sb.append("] Comment [" + getComment());
        sb.append("] IsDirectory [" + isDirectory());
        sb.append("] Size [" + getFormattedSize());
        sb.append("] LastModified [" + new Date(getLastModified()) + "]");
        return sb.toString();
    }

    /**
	 */
    @Override
    public String getCity() {
        return city;
    }

    /**
	 */
    @Override
    public String getLanguage() {
        return language;
    }

    /**
	 */
    @Override
    public String getPages() {
        return pages;
    }

    /**
	 */
    @Override
    public String[] getPublicationDate() {
        return new String[] { pubYear, pubMonth };
    }

    /**
	 */
    @Override
    public String getPublisher() {
        return publisher;
    }

    /**
	 */
    @Override
    public String getCreator() {
        return creator;
    }

    /**
	 */
    @Override
    public String getSource() {
        return source;
    }

    /**
	 */
    @Override
    public String getTitle() {
        return title;
    }

    /**
	 */
    @Override
    public String getUrl() {
        return url;
    }

    /**
	 */
    @Override
    public void setCity(final String city) {
        this.city = city;
    }

    /**
	 */
    @Override
    public void setLanguage(final String language) {
        this.language = language;
    }

    /**
	 */
    @Override
    public void setPages(final String pages) {
        this.pages = pages;
    }

    /**
	 */
    @Override
    public void setPublicationDate(final String month, final String year) {
        this.pubMonth = month;
        this.pubYear = year;
    }

    /**
	 */
    @Override
    public void setPublisher(final String publisher) {
        this.publisher = publisher;
    }

    /**
	 */
    public void setWriter(final String writer) {
        this.creator = writer;
    }

    /**
	 */
    @Override
    public void setCreator(final String creator) {
        this.creator = creator;
    }

    /**
	 */
    @Override
    public void setSource(final String source) {
        this.source = source;
    }

    /**
	 */
    @Override
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
	 */
    @Override
    public void setUrl(final String url) {
        this.url = url;
    }

    @Override
    public boolean isThumbnailAvailable() {
        if (isDirectory()) {
            return false;
        }
        if (originFile.isHidden()) {
            return false;
        }
        if (cannotGenerateThumbnail) {
            return false;
        }

        final VFSLeaf originLeaf = new LocalFileImpl(originFile);
        if (thumbnailService.isEnabled()) {
            return thumbnailService.isThumbnailPossible(originLeaf);
        }
        return false;
    }

    @Override
    public VFSLeaf getThumbnail(final int maxWidth, final int maxHeight) {
        if (isDirectory()) {
            return null;
        }
        final Thumbnail thumbnailInfo = getThumbnailInfo(maxWidth, maxHeight);
        if (thumbnailInfo == null) {
            return null;
        }
        return new LocalFileImpl(thumbnailInfo.getThumbnailFile());
    }

    @Override
    public void clearThumbnails() {
        thumbnails.clear();
        write();
    }

    private Thumbnail getThumbnailInfo(final int maxWidth, final int maxHeight) {
        for (final Thumbnail thumbnail : thumbnails) {
            if (maxHeight == thumbnail.getMaxHeight() && maxWidth == thumbnail.getMaxWidth()) {
                if (thumbnail.exists()) {
                    return thumbnail;
                }
            }
        }

        // generate a file name
        final File metaLoc = metaFile.getParentFile();
        final String name = originFile.getName();
        final String extension = FileUtils.getFileSuffix(name);
        final String nameOnly = name.substring(0, name.length() - extension.length() - 1);
        final String uuid = UUID.randomUUID().toString();
        final String thumbnailExtension = preferedThumbnailType(extension);
        final File thumbnailFile = new File(metaLoc, nameOnly + "_" + uuid + "_" + maxHeight + "x" + maxWidth + "." + thumbnailExtension);

        // generate thumbnail
        long start = 0l;
        if (log.isDebugEnabled()) {
            start = System.currentTimeMillis();
        }

        final VFSLeaf thumbnailLeaf = new LocalFileImpl(thumbnailFile);
        final VFSLeaf originLeaf = new LocalFileImpl(originFile);
        if (thumbnailService.isEnabled() && thumbnailService.isThumbnailPossible(thumbnailLeaf)) {
            try {
                final FinalSize finalSize = thumbnailService.generateThumbnail(originLeaf, thumbnailLeaf, maxHeight, maxWidth);
                if (finalSize == null) {
                    return null;
                } else {

                    final Thumbnail thumbnail = new Thumbnail();
                    thumbnail.setMaxHeight(maxHeight);
                    thumbnail.setMaxWidth(maxWidth);
                    thumbnail.setFinalHeight(finalSize.getHeight());
                    thumbnail.setFinalWidth(finalSize.getWidth());
                    thumbnail.setThumbnailFile(thumbnailFile);
                    thumbnails.add(thumbnail);
                    write();
                    log.info("Create thumbnail: " + thumbnailLeaf);
                    if (log.isDebugEnabled()) {
                        log.debug("Creation of thumbnail takes (ms): " + (System.currentTimeMillis() - start));
                    }
                    return thumbnail;
                }
            } catch (final CannotGenerateThumbnailException e) {
                // don't try every time to create the thumbnail.
                cannotGenerateThumbnail = true;
                write();
                return null;
            }
        }
        return null;
    }

    private String preferedThumbnailType(final String extension) {
        if (extension.equalsIgnoreCase("png") || extension.equalsIgnoreCase("gif")) {
            return extension;
        }
        return "jpg";
    }

    /**
	 */
    @Override
    public void increaseDownloadCount() {
        this.downloadCount++;
    }

    /**
	 */
    @Override
    public int getDownloadCount() {
        return downloadCount;
    }

    public void setAuthorIdentKey(final Long authorIdentKey) {
        this.authorIdentKey = authorIdentKey;
    }

    private StringBuilder current;

    // //////////////////////////////////
    // SAX Handler for max. performance
    // //////////////////////////////////

    @Override
    public final void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
        if ("lock".equals(qName)) {
            locked = "true".equals(attributes.getValue("locked"));
            final String date = attributes.getValue("date");
            if (date != null && date.length() > 0) {
                lockedDate = new Date(Long.parseLong(date));
            }
        } else if ("thumbnails".equals(qName)) {
            final String valueStr = attributes.getValue("cannotGenerateThumbnail");
            if (StringHelper.containsNonWhitespace(valueStr)) {
                cannotGenerateThumbnail = new Boolean(valueStr);
            }
        } else if ("thumbnail".equals(qName)) {
            final Thumbnail thumbnail = new Thumbnail();
            thumbnail.setMaxHeight(Integer.parseInt(attributes.getValue("maxHeight")));
            thumbnail.setMaxWidth(Integer.parseInt(attributes.getValue("maxWidth")));
            thumbnail.setFinalHeight(Integer.parseInt(attributes.getValue("finalHeight")));
            thumbnail.setFinalWidth(Integer.parseInt(attributes.getValue("finalWidth")));
            thumbnails.add(thumbnail);
        }
    }

    @Override
    public final void characters(final char[] ch, final int start, final int length) {
        if (length == 0) {
            return;
        }
        if (current == null) {
            current = new StringBuilder();
        }
        current.append(ch, start, length);
    }

    @Override
    public final void endElement(final String uri, final String localName, final String qName) {
        if (current == null) {
            return;
        }

        if ("comment".equals(qName)) {
            comment = current.toString();
        } else if ("author".equals(qName)) {
            try {
                authorIdentKey = Long.valueOf(current.toString());
            } catch (final NumberFormatException nEx) {
                // nothing to say
            }
        } else if ("lock".equals(qName)) {
            try {
                lockedByIdentKey = new Long(current.toString());
            } catch (final NumberFormatException nEx) {
                // nothing to say
            }
        } else if ("title".equals(qName)) {
            title = current.toString();
        } else if ("publisher".equals(qName)) {
            publisher = current.toString();
        } else if ("source".equals(qName)) {
            source = current.toString();
        } else if ("city".equals(qName)) {
            city = current.toString();
        } else if ("pages".equals(qName)) {
            pages = current.toString();
        } else if ("language".equals(qName)) {
            language = current.toString();
        } else if ("downloadCount".equals(qName)) {
            try {
                downloadCount = Integer.valueOf(current.toString());
            } catch (final NumberFormatException nEx) {
                // nothing to say
            }
        } else if ("month".equals(qName)) {
            pubMonth = current.toString();
        } else if ("year".equals(qName)) {
            pubYear = current.toString();
        } else if (qName.equals("creator")) {
            this.creator = current.toString();
        } else if (qName.equals("url")) {
            this.url = current.toString();
        } else if (qName.equals("thumbnail")) {
            final String finalName = current.toString();
            final File thumbnailFile = new File(metaFile.getParentFile(), finalName);
            thumbnails.get(thumbnails.size() - 1).setThumbnailFile(thumbnailFile);
        }
        current = null;
    }

    public class Thumbnail {
        private int maxWidth;
        private int maxHeight;
        private int finalWidth;
        private int finalHeight;
        private File thumbnailFile;

        public int getMaxWidth() {
            return maxWidth;
        }

        public void setMaxWidth(final int maxWidth) {
            this.maxWidth = maxWidth;
        }

        public int getMaxHeight() {
            return maxHeight;
        }

        public void setMaxHeight(final int maxHeight) {
            this.maxHeight = maxHeight;
        }

        public int getFinalWidth() {
            return finalWidth;
        }

        public void setFinalWidth(final int finalWidth) {
            this.finalWidth = finalWidth;
        }

        public int getFinalHeight() {
            return finalHeight;
        }

        public void setFinalHeight(final int finalHeight) {
            this.finalHeight = finalHeight;
        }

        public File getThumbnailFile() {
            return thumbnailFile;
        }

        public void setThumbnailFile(final File thumbnailFile) {
            this.thumbnailFile = thumbnailFile;
        }

        public boolean exists() {
            return thumbnailFile == null ? false : thumbnailFile.exists();
        }
    }

}
