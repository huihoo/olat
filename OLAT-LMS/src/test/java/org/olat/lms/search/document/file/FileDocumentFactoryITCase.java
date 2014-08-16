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

package org.olat.lms.search.document.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.vfs.LocalFileImpl;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.olatimpl.OlatNamedContainerImpl;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.SearchServiceFactory;
import org.olat.lms.search.document.AbstractOlatDocument;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.test.OlatTestCase;

/**
 * Lucene document mapper.
 * 
 * @author Christian Guretzki
 */
public class FileDocumentFactoryITCase extends OlatTestCase {

    private static final Logger log = LoggerHelper.getLogger();

    // variables for test fixture
    FileDocumentFactory fileDocumentFactory;
    private String rootPath;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setup() throws Exception {
        // clear database from errors
        rootPath = "/search_junit_test_folder";
        fileDocumentFactory = SearchServiceFactory.getFileDocumentFactory();
    }

    /**
     * TearDown is called after each test
     */
    @After
    public void tearDown() {
        try {
            final DB db = DBFactory.getInstance();
            db.closeSession();
        } catch (final Exception e) {
            log.error("Exception in tearDown(): " + e);
        }
    }

    @Test
    public void testIsFileSupported() {
        assertTrue("html must be supported", fileDocumentFactory.isFileSupported(new LocalFileImpl(new File("test.html"))));
        assertTrue("htm must be supported", fileDocumentFactory.isFileSupported(new LocalFileImpl(new File("test.htm"))));
        assertTrue("HTML must be supported", fileDocumentFactory.isFileSupported(new LocalFileImpl(new File("test.HTML"))));
        assertTrue("HTM must be supported", fileDocumentFactory.isFileSupported(new LocalFileImpl(new File("test.HTM"))));
        assertTrue("HTM must be supported", fileDocumentFactory.isFileSupported(new LocalFileImpl(new File("test.xhtml"))));
        assertTrue("HTM must be supported", fileDocumentFactory.isFileSupported(new LocalFileImpl(new File("test.XHTML"))));

        assertTrue("pdf must be supported", fileDocumentFactory.isFileSupported(new LocalFileImpl(new File("test.pdf"))));
        assertTrue("PDF must be supported", fileDocumentFactory.isFileSupported(new LocalFileImpl(new File("test.PDF"))));

        assertTrue("DOC must be supported", fileDocumentFactory.isFileSupported(new LocalFileImpl(new File("test.DOC"))));
        assertTrue("doc must be supported", fileDocumentFactory.isFileSupported(new LocalFileImpl(new File("test.doc"))));

        assertTrue("TXT must be supported", fileDocumentFactory.isFileSupported(new LocalFileImpl(new File("test.TXT"))));
        assertTrue("txt must be supported", fileDocumentFactory.isFileSupported(new LocalFileImpl(new File("test.txt"))));
        assertTrue("txt must be supported", fileDocumentFactory.isFileSupported(new LocalFileImpl(new File("test.readme"))));
        assertTrue("txt must be supported", fileDocumentFactory.isFileSupported(new LocalFileImpl(new File("test.README"))));
        assertTrue("txt must be supported", fileDocumentFactory.isFileSupported(new LocalFileImpl(new File("test.csv"))));
        assertTrue("txt must be supported", fileDocumentFactory.isFileSupported(new LocalFileImpl(new File("test.CSV"))));
        assertTrue("XML must be supported", fileDocumentFactory.isFileSupported(new LocalFileImpl(new File("test.XML"))));
        assertTrue("xml must be supported", fileDocumentFactory.isFileSupported(new LocalFileImpl(new File("test.xml"))));
    }

    @Test
    public void testCreateHtmlDocument() {
        final String filePath = "SearchTestFolder";
        final String htmlFileName = "test.html";
        final String htmlText = "<html><head><meta name=\"generator\" content=\"olat-tinymce-1\"><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></head><body>"
                + "<H1>Test HTML Seite fuer JUnit Test</H1>" + "Dies ist<br />der Test&nbsp;Text" + "</body></html>"; // Text = 'Dies ist der Test Text'
        final String text = "Test HTML Seite fuer JUnit Test Dies ist der Test\u00A0Text"; // must include '\u00A0' !!! 19.5.2010/cg
        // Create a test HTML File
        final OlatRootFolderImpl rootFolder = new OlatRootFolderImpl(rootPath, null);
        final OlatNamedContainerImpl namedFolder = new OlatNamedContainerImpl(filePath, rootFolder);
        VFSLeaf leaf = (VFSLeaf) namedFolder.resolve(htmlFileName);
        if (leaf != null) {
            leaf.delete();
        }
        leaf = namedFolder.createChildLeaf(htmlFileName);
        FileUtils.save(leaf.getOutputStream(false), htmlText, "utf-8");
        try {

            final SearchResourceContext resourceContext = new SearchResourceContext();
            resourceContext.setBusinessControlFor(OresHelper.createOLATResourceableType("FileDocumentFactoryITCase"));
            resourceContext.setFilePath(filePath + "/" + leaf.getName());
            final Document htmlDocument = FileDocumentFactory.createDocument(resourceContext, leaf);
            // 1. Check content
            final String content = htmlDocument.get(AbstractOlatDocument.CONTENT_FIELD_NAME);
            assertEquals("Wrong HTML content=" + content.trim() + " , must be =" + text.trim(), text.trim(), content.trim());
            // 2. Check resourceUrl
            final String resourceUrl = htmlDocument.get(AbstractOlatDocument.RESOURCEURL_FIELD_NAME);
            assertEquals("Wrong ResourceUrl", "[FileDocumentFactoryITCase:0][path=" + filePath + "/" + htmlFileName + "]", resourceUrl);
            // 3. Check File-Type
            final String fileType = htmlDocument.get(AbstractOlatDocument.FILETYPE_FIELD_NAME);
            assertEquals("Wrong file-type", "type.file.html", fileType);

        } catch (final DocumentNotImplementedException e) {
            fail("DocumentNotImplementedException=" + e.getMessage());
        } catch (final IOException e) {
            fail("IOException=" + e.getMessage());
        } catch (final DocumentException e) {
            fail("DocumentException=" + e.getMessage());
        } catch (final DocumentAccessException e) {
            fail("DocumentAccessException=" + e.getMessage());
        }
    }

}
