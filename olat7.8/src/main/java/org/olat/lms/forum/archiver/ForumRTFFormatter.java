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

package org.olat.lms.forum.archiver;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.fileutil.ZipUtil;
import org.olat.data.commons.filter.FilterFactory;
import org.olat.data.commons.vfs.LocalFileImpl;
import org.olat.data.commons.vfs.LocalFolderImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.lms.commons.tree.INode;
import org.olat.lms.forum.ForumService;
import org.olat.lms.forum.MessageNode;
import org.olat.lms.user.UserService;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.WebappHelper;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: Nov 09, 2005 <br>
 * 
 * @author Patrick Brunner, Alexander Schneider
 */
public class ForumRTFFormatter extends ForumFormatter {

    private static final Logger log = LoggerHelper.getLogger();

    private static final Pattern PATTERN_RTX_RESERVED_BACKSLASH = Pattern.compile("\\\\");
    private static final Pattern PATTERN_RTX_RESERVED_BRACE_OPEN = Pattern.compile("\\{");
    private static final Pattern PATTERN_RTX_RESERVED_BRACE_CLOSE = Pattern.compile("\\}");

    private static final Pattern PATTERN_HTML_BOLD = Pattern.compile("<strong>(.*?)</strong>", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_HTML_ITALIC = Pattern.compile("<em>(.*?)</em>", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_HTML_BREAK = Pattern.compile("<br />", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_HTML_PARAGRAPH = Pattern.compile("<p>(.*?)</p>", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_HTML_AHREF = Pattern.compile("<a href=\"([^\"]+)\"[^>]*>(.*?)</a>", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_HTML_LIST = Pattern.compile("(<ol>(.*?)</ol>)|(<ul>(.*?)</ul>)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_HTML_LIST_ELEMENT = Pattern.compile("<li>(.*?)</li>", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_HTML_SPACE = Pattern.compile("&nbsp;");

    private static final Pattern PATTERN_CSS_O_FOQUOTE = Pattern
            .compile(
                    "<div class=\"b_quote_wrapper\">\\s*<div class=\"b_quote_author mceNonEditable\">(.*?)</div>\\s*<blockquote class=\"b_quote\">\\s*(.*?)\\s*</blockquote>\\s*</div>",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern PATTERN_THREEPOINTS = Pattern.compile("&#8230;", Pattern.CASE_INSENSITIVE);
    private static final String THREEPOINTS = "...";
    // TODO: (LD) translate this!
    private static final String HIDDEN_STR = "VERBORGEN";

    ForumService forumService;
    UserService userService;

    private final VFSContainer container;
    private VFSItem vfsFil = null;
    private VFSContainer tempContainer;

    /**
     * @param container
     * @param filePerThread
     */
    public ForumRTFFormatter(final VFSContainer container, final boolean filePerThread) {
        super();
        this.container = container;
        super.filePerThread = filePerThread;
        /*
         * Translator translator = new PackageTranslator(PACKAGE, locale); HIDDEN_STR = translator.translate("fo_hidden");
         */
    }

    /**
	 */
    @Override
    public void visit(final INode node) {
        final MessageNode mn = (MessageNode) node;

        if (isTopThread) {
            if (filePerThread) {
                // make a file per thread
                // to have a meaningful filename we create the file here
                final String filName = "Thread_" + mn.getKey().toString();
                tempContainer = makeTempVFSContainer();
                this.vfsFil = tempContainer.resolve(filName + ".rtf");
                if (vfsFil == null) {
                    tempContainer.createChildLeaf(filName + ".rtf");
                    this.vfsFil = tempContainer.resolve(filName + ".rtf");
                }
            }
            // important!
            isTopThread = false;
        }
        // Message Title
        sb.append("{\\pard \\brdrb\\brdrs\\brdrw10 \\f2\\fs30\\b ");
        sb.append(getImageRTF(mn));
        sb.append(getTitlePrefix(mn));
        sb.append(mn.getTitle());
        sb.append("\\par}");
        // Message Body
        sb.append("{\\pard \\f0");
        sb.append(convertHTMLMarkupToRTF(mn.getBody()));
        sb.append("\\par}");
        // Message key
        sb.append("{\\pard \\f0\\fs15 Message key: ");
        sb.append(mn.getKey());
        sb.append("} \\line ");
        sb.append("{\\pard \\f0\\fs15 created: ");
        // Creator and creation date
        sb.append(getUserService().getFirstAndLastname(mn.getCreator().getUser()));
        sb.append(" ");
        sb.append(mn.getCreationDate().toString());
        // Modifier and modified date
        final Identity modifier = mn.getModifier();
        if (modifier != null) {
            sb.append(" \\line modified: ");
            sb.append(getUserService().getFirstAndLastname(modifier.getUser()));
            sb.append(" ");
            sb.append(mn.getModifiedDate().toString());
        }
        sb.append(" \\par}");
        // attachment(s)
        final VFSContainer msgContainer = getForumService().getMessageContainer((Long) (getMetainfo(ForumFormatter.MANDATORY_METAINFO_KEY)), mn.getKey());
        final List attachments = msgContainer.getItems();
        if (attachments != null && attachments.size() > 0) {
            VFSItem item = container.resolve("attachments");
            if (item == null) {
                item = container.createChildContainer("attachments");
            }
            final VFSContainer attachmentContainer = (VFSContainer) item;
            attachmentContainer.copyFrom(msgContainer);

            sb.append("{\\pard \\f0\\fs15 Attachment(s): ");
            boolean commaFlag = false;
            for (final Iterator iter = attachments.iterator(); iter.hasNext();) {
                final VFSItem attachment = (VFSItem) iter.next();
                if (commaFlag) {
                    sb.append(", ");
                }
                sb.append(attachment.getName());
                commaFlag = true;
            }
            sb.append("} \\line");
        }
        sb.append("{\\pard \\brdrb\\brdrs\\brdrw10 \\par}");

    }

    /**
	 */
    @Override
    public void openThread() {
        super.openThread();
        if (filePerThread) {
            sb.append("{\\rtf1\\ansi\\deff0");
            sb.append("{\\fonttbl {\\f0\\fswiss Arial;}{\\f1\\froman\\fprq2\\fcharset2 Symbol;}} ");
            sb.append("\\deflang1033\\plain");
        }
        sb.append("{\\pard \\brdrb \\brdrs \\brdrdb \\brsp20 \\par}{\\pard\\par}");
    }

    /**
	 */
    @Override
    public StringBuilder closeThread() {
        final boolean append = !filePerThread;
        final String footerThread = "{\\pard \\brdrb \\brdrs \\brdrw20 \\brsp20 \\par}{\\pard\\par}";
        sb.append(footerThread);
        if (filePerThread) {
            sb.append("}");
        }
        writeToFile(append, sb);
        if (this.filePerThread) {
            zipContainer(tempContainer);
            tempContainer.delete();
        }
        return super.closeThread();
    }

    /**
	 */
    @Override
    public void openForum() {
        if (!filePerThread) {
            // make one ForumFile
            final Long forumKey = (Long) metaInfo.get(ForumFormatter.MANDATORY_METAINFO_KEY);
            String filName = forumKey.toString();
            filName = "Threads_" + filName + ".rtf";

            tempContainer = makeTempVFSContainer();
            this.vfsFil = tempContainer.resolve(filName);
            if (vfsFil == null) {
                tempContainer.createChildLeaf(filName);
                this.vfsFil = tempContainer.resolve(filName);
            }
            sb.append("{\\rtf1\\ansi\\deff0");
            sb.append("{\\fonttbl {\\f0\\fswiss Arial;}{\\f1\\froman\\fprq2\\fcharset2 Symbol;}} ");
            sb.append("\\deflang1033\\plain");
        }
    }

    /**
	 */
    @Override
    public StringBuilder closeForum() {
        if (!filePerThread) {
            final boolean append = !filePerThread;
            final String footerForum = "}";
            sb.append(footerForum);
            writeToFile(append, sb);
            zipContainer(tempContainer);
            tempContainer.delete();
        }
        return sb;
    }

    /**
     * @param append
     * @param buff
     */
    private void writeToFile(final boolean append, final StringBuilder buff) {
        final BufferedOutputStream bos = new BufferedOutputStream(((VFSLeaf) vfsFil).getOutputStream(append));
        OutputStreamWriter w;
        try {
            w = new OutputStreamWriter(bos, "utf-8");
            final BufferedWriter bw = new BufferedWriter(w);
            final String s = buff.toString();
            final StringBuilder out = new StringBuilder();
            final int len = s.length();
            for (int i = 0; i < len; i++) {
                final char c = s.charAt(i);
                final int val = c;
                if (val > 127) {
                    out.append("\\u").append(String.valueOf(val)).append("?");
                } else {
                    out.append(c);
                }
            }

            final String encoded = out.toString();
            bw.write(encoded);
            bw.close();
            bos.close();
        } catch (final UnsupportedEncodingException ueEx) {
            throw new AssertException("could not encode stream from forum export file: " + ueEx);
        } catch (final IOException e) {
            throw new AssertException("could not write to forum export file: " + e);
        }
    }

    /**
     * @param originalText
     * @return
     */
    private String convertHTMLMarkupToRTF(final String originalText) {
        String htmlText = originalText;

        // escape reserved chars in RTF: '{', '}', '\'
        final Matcher mbs = PATTERN_RTX_RESERVED_BACKSLASH.matcher(htmlText);
        final StringBuffer backslashes = new StringBuffer();
        while (mbs.find()) {
            mbs.appendReplacement(backslashes, "\\\\\\\\");
        }
        mbs.appendTail(backslashes);
        htmlText = backslashes.toString();

        final Matcher mbro = PATTERN_RTX_RESERVED_BRACE_OPEN.matcher(htmlText);
        final StringBuffer openBraces = new StringBuffer();
        while (mbro.find()) {
            mbro.appendReplacement(openBraces, "\\\\{ ");
        }
        mbro.appendTail(openBraces);
        htmlText = openBraces.toString();

        final Matcher mbrc = PATTERN_RTX_RESERVED_BRACE_CLOSE.matcher(htmlText);
        final StringBuffer closedBraces = new StringBuffer();
        while (mbrc.find()) {
            mbrc.appendReplacement(closedBraces, "\\\\} ");
        }
        mbrc.appendTail(closedBraces);
        htmlText = closedBraces.toString();

        // search for lists and convert in RTF
        final StringBuffer rtfLists = new StringBuffer();
        final List<ListDefinition> lists = new ArrayList<ListDefinition>();
        final Matcher mli = PATTERN_HTML_LIST.matcher(htmlText);
        int counter = 1;
        while (mli.find()) {
            final String listDefinition = mli.group();
            final boolean orderedList = listDefinition.contains("<ol>");
            final ListDefinition list = new ListDefinition(orderedList);

            final String listElements = mli.group(orderedList ? 2 : 3);
            final Matcher le = PATTERN_HTML_LIST_ELEMENT.matcher(listElements);
            while (le.find()) {
                // ticket I-130710-0028
                // $ character causes problems calling Matcher.appendReplacement
                list.elements.add(le.group(1).replace("$", "&#36"));
            }
            lists.add(list);
            mli.appendReplacement(rtfLists, getListRTF(list, counter));
            counter++;
        }
        mli.appendTail(rtfLists);
        // unescape $ characters again
        htmlText = rtfLists.toString().replace("&#36", "$");

        if (!lists.isEmpty()) {
            htmlText = prepareListRTF(lists) + htmlText;
        }

        final Matcher mb = PATTERN_HTML_BOLD.matcher(htmlText);
        final StringBuffer bolds = new StringBuffer();
        while (mb.find()) {
            mb.appendReplacement(bolds, "{\\\\b $1} ");
        }
        mb.appendTail(bolds);
        htmlText = bolds.toString();

        final Matcher mi = PATTERN_HTML_ITALIC.matcher(htmlText);
        final StringBuffer italics = new StringBuffer();
        while (mi.find()) {
            mi.appendReplacement(italics, "{\\\\i $1} ");
        }
        mi.appendTail(italics);
        htmlText = italics.toString();

        final Matcher mbr = PATTERN_HTML_BREAK.matcher(htmlText);
        final StringBuffer breaks = new StringBuffer();
        while (mbr.find()) {
            mbr.appendReplacement(breaks, "\\\\line ");
        }
        mbr.appendTail(breaks);
        htmlText = breaks.toString();

        final Matcher mofo = PATTERN_CSS_O_FOQUOTE.matcher(htmlText);
        final StringBuffer foquotes = new StringBuffer();
        while (mofo.find()) {
            mofo.appendReplacement(foquotes, "\\\\line {\\\\i $1} {\\\\pard $2\\\\par}");
        }
        mofo.appendTail(foquotes);
        htmlText = foquotes.toString();

        final Matcher mp = PATTERN_HTML_PARAGRAPH.matcher(htmlText);
        final StringBuffer paragraphs = new StringBuffer();
        while (mp.find()) {
            mp.appendReplacement(paragraphs, "\\\\line $1 \\\\line");
        }
        mp.appendTail(paragraphs);
        htmlText = paragraphs.toString();

        final Matcher mahref = PATTERN_HTML_AHREF.matcher(htmlText);
        final StringBuffer ahrefs = new StringBuffer();
        while (mahref.find()) {
            mahref.appendReplacement(ahrefs, "{\\\\field{\\\\*\\\\fldinst{HYPERLINK\"$1\"}}{\\\\fldrslt{\\\\ul $2}}}");
        }
        mahref.appendTail(ahrefs);
        htmlText = ahrefs.toString();

        final Matcher mtp = PATTERN_THREEPOINTS.matcher(htmlText);
        final StringBuffer tps = new StringBuffer();
        while (mtp.find()) {
            mtp.appendReplacement(tps, THREEPOINTS);
        }
        mtp.appendTail(tps);
        htmlText = tps.toString();

        // strip all other html-fragments, because not convertable that easy
        htmlText = FilterFactory.getHtmlTagsFilter().filter(htmlText);

        // Remove all &nbsp;
        final Matcher tmp = PATTERN_HTML_SPACE.matcher(htmlText);
        htmlText = tmp.replaceAll(" ");
        htmlText = StringHelper.unescapeHtml(htmlText);

        return htmlText;
    }

    /**
     * All lists in RTF are defined in \listtable and \listoverridetable <br>
     * http://msdn.microsoft.com/de-de/library/aa140277%28office.10%29.aspx
     */
    private String prepareListRTF(List<ListDefinition> lists) {
        final StringBuilder listtable = new StringBuilder();
        final StringBuilder listoverridetable = new StringBuilder();
        // OpenOffice seems to require a stylesheet for rendering
        listtable.append("{\\stylesheet {\\s1 Standard;}}");

        listtable.append("{\\*\\listtable");
        listoverridetable.append("{\\listoverridetable");
        int counter = 1;
        for (ListDefinition listDefinition : lists) {
            listtable.append("{\\list");
            if (listDefinition.ordered) {
                listtable.append("{\\listlevel\\levelnfc0\\leveljc0\\levelstartat1\\levelfollow0{\\leveltext \\'02\\'00.;}{\\levelnumbers\\'01;}\\f1\\fi-360\\li720}");
            } else {
                listtable.append("{\\listlevel\\levelnfc0\\leveljc0\\levelstartat1\\levelfollow0{\\leveltext \\'01\\u61623 ?;}{\\levelnumbers;}\\f1\\fi-360\\li720}");
            }
            listtable.append("\\listid" + counter + "}");

            listoverridetable.append("{\\listoverride\\listid" + counter + "\\listoverridecount0\\ls" + counter + "}");
            counter++;
        }
        listtable.append("}");
        listoverridetable.append("}");

        listtable.append(listoverridetable);
        return listtable.toString();
    }

    private String getListRTF(ListDefinition list, int listNumber) {
        final StringBuilder listtext = new StringBuilder();
        for (String element : list.elements) {
            listtext.append("\\\\par");
            listtext.append("\\\\ls" + listNumber);
            listtext.append("{\\\\listtext");
            if (!list.ordered) {
                listtext.append("\\\\u61623");
            }
            listtext.append("\\\\tab}");
            listtext.append("{\\\\rtlch \\\\ltrch\\\\loch\\\\f0\\\\fs22\\\\i0\\\\b0 ");
            listtext.append(element);
            listtext.append("}");

        }

        return listtext.toString();
    }

    /**
     * @param messageNode
     * @return title prefix for hidden forum threads.
     */
    private String getTitlePrefix(final MessageNode messageNode) {
        final StringBuffer stringBuffer = new StringBuffer();
        if (messageNode.isHidden()) {
            stringBuffer.append(HIDDEN_STR);
        }
        if (stringBuffer.length() > 1) {
            stringBuffer.append(": ");
        }
        return stringBuffer.toString();
    }

    /**
     * Gets the RTF image section for the input messageNode.
     * 
     * @param messageNode
     * @return the RTF image section for the input messageNode.
     */
    private String getImageRTF(final MessageNode messageNode) {

        final StringBuffer stringBuffer = new StringBuffer();
        final List<String> fileNameList = addImagesToVFSContainer(messageNode, tempContainer);
        final Iterator<String> listIterator = fileNameList.iterator();
        while (listIterator.hasNext()) {
            final String fileName = listIterator.next();

            stringBuffer.append("{\\field\\fldedit{\\*\\fldinst { INCLUDEPICTURE ");
            stringBuffer.append("\"").append(fileName).append("\"");
            stringBuffer.append(" \\\\d }}{\\fldrslt {}}}");
        }
        return stringBuffer.toString();
    }

    /**
     * Retrieves the appropriate images for the input messageNode, if any, and adds it to the input container.
     * 
     * @param messageNode
     * @param container
     * @return
     */
    private List<String> addImagesToVFSContainer(final MessageNode messageNode, final VFSContainer container) {
        final List<String> fileNameList = new ArrayList<String>();
        String iconPath = null;
        if (messageNode.isClosed() && messageNode.isSticky()) {
            iconPath = getImagePath("fo_sticky_closed");
        } else if (messageNode.isClosed()) {
            iconPath = getImagePath("fo_closed");
        } else if (messageNode.isSticky()) {
            iconPath = getImagePath("fo_sticky");
        }
        if (iconPath != null) {
            final File file = new File(iconPath);
            if (file.exists()) {
                final LocalFileImpl imgFile = new LocalFileImpl(file);
                container.copyFrom(imgFile);
                fileNameList.add(file.getName());
            } else {
                log.error("Could not find image for forum RTF formatter::" + iconPath);
            }
        }
        return fileNameList;
    }

    /**
     * TODO: LD: to clarify whether there it a better way to get the image path? Gets the image path.
     * 
     * @param val
     * @return the path of the static icon image.
     */
    private String getImagePath(final Object val) {
        return WebappHelper.getContextRoot() + "/static/images/forum/" + val.toString() + ".png";
    }

    /**
     * Generates a new temporary VFSContainer.
     * 
     * @return the temp container.
     */
    private VFSContainer makeTempVFSContainer() {
        final Long forumKey = (Long) metaInfo.get(ForumFormatter.MANDATORY_METAINFO_KEY);
        final String dateStamp = String.valueOf(System.currentTimeMillis());
        final String fileName = "forum" + forumKey.toString() + "_" + dateStamp;
        final LocalFolderImpl tempFolder = new OlatRootFolderImpl("/tmp/" + fileName, null);
        return tempFolder;
    }

    /**
     * Zips the input vFSContainer into the container.
     * 
     * @param vFSContainer
     */
    private void zipContainer(final VFSContainer vFSContainer) {
        final String dateStamp = Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()));
        final VFSLeaf zipVFSLeaf = container.createChildLeaf("forum_archive-" + dateStamp + ".zip");
        ZipUtil.zip(vFSContainer.getItems(), zipVFSLeaf, true);
    }

    private UserService getUserService() {
        if (userService == null) {
            userService = CoreSpringFactory.getBean(UserService.class);
        }
        return userService;
    }

    private ForumService getForumService() {
        if (forumService == null) {
            forumService = CoreSpringFactory.getBean(ForumService.class);
        }
        return forumService;
    }

    private static final class ListDefinition {
        private List<String> elements = new ArrayList<String>();
        private final boolean ordered;

        private ListDefinition(boolean ordered) {
            this.ordered = ordered;
        }

        private ListDefinition(List<String> elements, boolean ordered) {
            this.elements = elements;
            this.ordered = ordered;
        }
    }

}
