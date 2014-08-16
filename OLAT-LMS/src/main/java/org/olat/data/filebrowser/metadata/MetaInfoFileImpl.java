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
import org.olat.system.logging.log4j.LoggerHelper;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Initial Date: 08.07.2003
 * 
 * @author Mike Stock<br>
 *         Comment: Meta files are in a shadow filesystem with the same directory structure as their original files. Meta info for directories is stored in a file called
 *         ".xml" residing in the respective directory. Meta info for files is stored in a file with ".xml" appended to its filename.
 */
public class MetaInfoFileImpl extends DefaultHandler implements MetaInfo {
    private static final Logger log = LoggerHelper.getLogger();

    private static SAXParser saxParser;
    static {
        try {
            saxParser = SAXParserFactory.newInstance().newSAXParser();
        } catch (final Exception ex) {
            log.error("", ex);
        }
    }

    // meta data
    private Long authorIdentKey = null;
    private Long lockedByIdentKey = null;
    private String comment = "";
    private String title, publisher, creator, source, city, pages, language, url, pubMonth, pubYear;
    private Date lockedDate;
    private int downloadCount;
    private boolean locked;

    // internal
    private File originFile = null;
    private File metaFile = null;

    private boolean cannotGenerateThumbnail = false;
    private final List<Thumbnail> thumbnails = new ArrayList<Thumbnail>();

    private ThumbnailService thumbnailService;
    private BaseSecurity baseSecurity;

    /**
     * always access via metaInfoFactory
     * 
     * @param metaFile
     */
    public MetaInfoFileImpl(ThumbnailService thumbnailService, BaseSecurity baseSecurity, final OlatRelPathImpl olatRelPathImpl) {
        super();
        this.baseSecurity = baseSecurity;
        this.thumbnailService = thumbnailService;
        init(olatRelPathImpl);
    }

    private void init(final OlatRelPathImpl olatRelPathImpl) {
        final String canonicalMetaPath = getCanonicalMetaPath(olatRelPathImpl);
        if (canonicalMetaPath == null) {
            throw new IllegalArgumentException();
        }
        originFile = getOriginFile(olatRelPathImpl);
        metaFile = new File(canonicalMetaPath);
        // set
        if (!parseSAX(metaFile)) {
            final String metaDirPath = canonicalMetaPath.substring(0, canonicalMetaPath.lastIndexOf('/'));
            new File(metaDirPath).mkdirs();
            write();
        }
    }

    private File getOriginFile(final OlatRelPathImpl olatRelPathImpl) {
        return new File(FolderConfig.getCanonicalRoot() + olatRelPathImpl.getRelPath());
    }

    /**
     * Get the canonical path to the file's meta file.
     * 
     * @param bcPath
     * @return String
     */
    private String getCanonicalMetaPath(final OlatRelPathImpl olatRelPathImpl) {
        final File f = getOriginFile(olatRelPathImpl);
        if (!f.exists()) {
            return null;
        }
        if (f.isDirectory()) {
            return FolderConfig.getCanonicalMetaRoot() + olatRelPathImpl.getRelPath() + "/.xml";
        } else {
            return FolderConfig.getCanonicalMetaRoot() + olatRelPathImpl.getRelPath() + ".xml";
        }
    }

    /**
     * Rename the given meta info file
     * 
     * @param meta
     * @param newName
     */
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
    public void moveCopyToDir(final OlatRelPathImpl target, final boolean move) {
        File fSource = metaFile;
        File fTarget = new File(getCanonicalMetaPath(target));
        if (isDirectory()) { // move/copy whole meta directory
            fSource = fSource.getParentFile();
            fTarget = fTarget.getParentFile();
        } else if (target instanceof VFSContainer) {
            // getCanonicalMetaPath give the path to the xml file where the metadatas are saved
            if (fTarget.getName().equals(".xml")) {
                fTarget = fTarget.getParentFile();
            }
        }

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
    public void deleteAll() {
        if (isDirectory()) { // delete whole meta directory (where the ".xml" resides within)
            FileUtils.deleteDirsAndFiles(metaFile.getParentFile(), true, true);
        } else { // delete this single meta file
            delete();
        }
    }

    /**
     * Copy values from froMeta into this object except name.
     * 
     * @param fromMeta
     */
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

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(final boolean locked) {
        this.locked = locked;
        if (!locked) {
            lockedByIdentKey = null;
            lockedDate = null;
        }
    }

    public Identity getLockedByIdentity() {
        if (lockedByIdentKey != null) {
            final Identity identity = baseSecurity.loadIdentityByKey(lockedByIdentKey);
            return identity;
        }
        return null;
    }

    public Long getLockedBy() {
        return lockedByIdentKey;
    }

    public void setLockedBy(final Long lockedBy) {
        this.lockedByIdentKey = lockedBy;
    }

    public Date getLockedDate() {
        return lockedDate;
    }

    public void setLockedDate(final Date lockedDate) {
        this.lockedDate = lockedDate;
    }

    /**
     * Writes the meta data to file. If no changes have been made, does not write anything.
     * 
     * @return True upon success.
     */
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
     * The parser is synchronized. Normally for such small files, this is the quicker way. Creation of a SAXParser is really time consuming. An other possibilty would be
     * to use a pool of parser.
     * 
     * @param fMeta
     * @return
     */
    private boolean parseSAX(final File fMeta) {
        if (fMeta == null || !fMeta.exists() || fMeta.isDirectory()) {
            return false;
        }

        InputStream in = null;
        try {
            // the performance gain of the SAX Parser over the DOM Parser allow
            // this to be synchronized (factory 5 to 10 quicker)
            synchronized (saxParser) {
                in = new FileInputStream(fMeta);
                saxParser.parse(in, this);
            }
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
        return true;
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
                synchronized (saxParser) {
                    final InputSource in = new InputSource(new StringReader(filtered));
                    saxParser.parse(in, this);
                }
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
    public String getComment() {
        return comment;
    }

    public String getName() {
        return originFile.getName();
    }

    /**
     * @return True if this is a directory
     */
    public boolean isDirectory() {
        return originFile.isDirectory();
    }

    /**
     * @return Last modified timestamp
     */
    public long getLastModified() {
        return originFile.lastModified();
    }

    /**
     * @return size of file
     */
    public long getSize() {
        return originFile.length();
    }

    /**
     * @return formatted representation of size of file
     */
    public String getFormattedSize() {
        return StringHelper.formatMemory(getSize());
    }

    /* ------------------------- Setters ------------------------------ */

    /**
     * @param string
     */
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
    public String getCity() {
        return city;
    }

    /**
	 */
    public String getLanguage() {
        return language;
    }

    /**
	 */
    public String getPages() {
        return pages;
    }

    /**
	 */
    public String[] getPublicationDate() {
        return new String[] { pubYear, pubMonth };
    }

    /**
	 */
    public String getPublisher() {
        return publisher;
    }

    /**
	 */
    public String getCreator() {
        return creator;
    }

    /**
	 */
    public String getSource() {
        return source;
    }

    /**
	 */
    public String getTitle() {
        return title;
    }

    /**
	 */
    public String getUrl() {
        return url;
    }

    /**
	 */
    public void setCity(final String city) {
        this.city = city;
    }

    /**
	 */
    public void setLanguage(final String language) {
        this.language = language;
    }

    /**
	 */
    public void setPages(final String pages) {
        this.pages = pages;
    }

    /**
	 */
    public void setPublicationDate(final String month, final String year) {
        this.pubMonth = month;
        this.pubYear = year;
    }

    /**
	 */
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
    public void setCreator(final String creator) {
        this.creator = creator;
    }

    /**
	 */
    public void setSource(final String source) {
        this.source = source;
    }

    /**
	 */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
	 */
    public void setUrl(final String url) {
        this.url = url;
    }

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
    public void increaseDownloadCount() {
        this.downloadCount++;
    }

    /**
	 */
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
