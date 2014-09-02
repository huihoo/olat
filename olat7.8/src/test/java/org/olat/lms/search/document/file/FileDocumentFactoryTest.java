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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.State;
import java.lang.reflect.Constructor;

import org.apache.lucene.document.Document;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.vfs.LocalFileImpl;
import org.olat.lms.search.SearchModule;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.SearchService;
import org.olat.lms.search.SearchServiceFactory;
import org.olat.lms.search.document.AbstractOlatDocument;
import org.olat.lms.search.document.file.FileDocumentFactory.MimeTypeProvider;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;

/**
 * Lucene document mapper.
 * 
 * @author Christian Guretzki
 * @author oliver.buehler@agility-informatik.ch
 */
public class FileDocumentFactoryTest {

    private static File SEARCH_TEST_FOLDER = new File(System.getProperty("java.io.tmpdir"), "SearchTestFolder");

    private static FileDocumentFactory fileDocumentFactory;

    private static OLATResourceable olatResourcable;

    @BeforeClass
    public static void setUpClass() throws Exception {
        if (!SEARCH_TEST_FOLDER.exists()) {
            SEARCH_TEST_FOLDER.mkdirs();
        }

        final SearchModule searchModuleMock = Mockito.mock(SearchModule.class);
        when(searchModuleMock.isExcelFileEnabled()).thenReturn(true);
        when(searchModuleMock.isPptFileEnabled()).thenReturn(true);
        when(searchModuleMock.isTextBufferEnabled()).thenReturn(false);
        when(searchModuleMock.getTextBufferPath()).thenReturn("");

        final SearchService searchServiceMock = Mockito.mock(SearchService.class);
        when(searchServiceMock.getSearchModuleConfig()).thenReturn(searchModuleMock);

        Constructor<SearchServiceFactory> constructor = SearchServiceFactory.class.getDeclaredConstructor(SearchService.class);
        constructor.setAccessible(true);
        constructor.newInstance(searchServiceMock);

        fileDocumentFactory = new FileDocumentFactory(searchModuleMock, new MimeTypeProvider() {

            @Override
            public String getMimeType(String fileName) {
                // fake MIME Type
                return "application/" + fileName;
            }
        });

        olatResourcable = OresHelper.createOLATResourceableType("testResourcable");
    }

    @AfterClass
    public static void tearDownClass() {
        FileUtils.deleteDirsAndFiles(SEARCH_TEST_FOLDER, true, true);
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
    public void testCreateHtmlDocument() throws Exception {

        final String htmlFileName = "test.html";
        final String htmlText = "<html><head><meta name=\"generator\" content=\"olat-tinymce-1\"><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></head><body>"
                + "<H1>Test HTML Seite fuer JUnit Test</H1>" + "Dies ist<br />der Test&nbsp;Text" + "</body></html>"; // Text = 'Dies ist der Test Text'
        final String text = "Test HTML Seite fuer JUnit Test Dies ist der Test\u00A0Text"; // must include '\u00A0' !!! 19.5.2010/cg

        // Create a test HTML File
        final File htmlFile = new File(SEARCH_TEST_FOLDER, htmlFileName);
        if (htmlFile.exists()) {
            htmlFile.delete();
        }

        String filePath = "anyFilePath";
        FileUtils.save(htmlFile, htmlText, "utf-8");
        try {
            final SearchResourceContext resourceContext = new SearchResourceContext();
            resourceContext.setBusinessControlFor(OresHelper.createOLATResourceableType("FileDocumentFactoryITCase"));
            resourceContext.setFilePath(filePath + "/" + htmlFileName);
            final Document htmlDocument = FileDocumentFactory.createDocument(resourceContext, new LocalFileImpl(htmlFile));
            // 1. Check content
            final String content = htmlDocument.get(AbstractOlatDocument.CONTENT_FIELD_NAME);
            assertEquals("Wrong HTML content=" + content.trim() + " , must be =" + text.trim(), text.trim(), content.trim());
            // 2. Check resourceUrl
            final String resourceUrl = htmlDocument.get(AbstractOlatDocument.RESOURCEURL_FIELD_NAME);
            assertEquals("Wrong ResourceUrl", "[FileDocumentFactoryITCase:0][path=" + filePath + "/" + htmlFileName + "]", resourceUrl);
            // 3. Check File-Type
            final String fileType = htmlDocument.get(AbstractOlatDocument.FILETYPE_FIELD_NAME);
            assertEquals("Wrong file-type", "type.file.html", fileType);
        } catch (final IOException e) {
            fail("IOException=" + e.getMessage());
        } catch (final DocumentException e) {
            fail("DocumentException=" + e.getMessage());
        }
    }

    @Test
    public void testCreateXMLDocument() {
        try {
            final SearchResourceContext resourceContext = new SearchResourceContext();
            resourceContext.setBusinessControlFor(olatResourcable);
            final Document xmlDocument = FileDocumentFactory.createDocument(resourceContext, new LocalFileImpl(getTestFile("XMLDocument.xml")));
            // check content
            final String content = xmlDocument.get(AbstractOlatDocument.CONTENT_FIELD_NAME);
            assertEquals("Wrong XML content", "content", content.trim());
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    @Test
    public void testCreateExcelOOXMLDocument() {
        try {
            final SearchResourceContext resourceContext = new SearchResourceContext();
            resourceContext.setBusinessControlFor(olatResourcable);
            final Document excelDocument = FileDocumentFactory.createDocument(resourceContext, new LocalFileImpl(getTestFile("ExcelOOXMLDocument.xlsx")));
            // check content
            final String content = excelDocument.get(AbstractOlatDocument.CONTENT_FIELD_NAME);
            assertTrue("Wrong Excel (xlsx) content", content.contains("Sheet1"));
            assertTrue("Wrong Excel (xlsx) content", content.contains("Sheet1:A1"));
            assertTrue("Wrong Excel (xlsx) content", content.contains("Sheet1:D4"));
            assertTrue("Wrong Excel (xlsx) content", content.contains("Sheet1:H8"));
            assertTrue("Wrong Excel (xlsx) content", content.contains("Sheet2"));
            assertTrue("Wrong Excel (xlsx) content", content.contains("Sheet2:A1"));
            assertTrue("Wrong Excel (xlsx) content", content.contains("Sheet2:D4"));
            assertTrue("Wrong Excel (xlsx) content", content.contains("Sheet2:H8"));
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    @Test
    public void testCreateExcelOOXMLDocumentSlow() {
        try {
            final SearchResourceContext resourceContext = new SearchResourceContext();
            resourceContext.setBusinessControlFor(olatResourcable);
            final Thread excelTextExtractionThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        FileDocumentFactory.createDocument(resourceContext, new LocalFileImpl(getTestFile("ExcelOOXMLDocumentSlow.xlsx")));
                    } catch (Exception e) {
                        // ignore
                    }
                }

            });
            excelTextExtractionThread.start();
            Thread.sleep(5000);
            assertFalse("Text extraction for Excel (xlsx) must not take longer than 2s.", excelTextExtractionThread.getState().equals(State.RUNNABLE));
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    @Test
    public void testCreatePowerPointOOXMLDocument() {
        try {
            final SearchResourceContext resourceContext = new SearchResourceContext();
            resourceContext.setBusinessControlFor(olatResourcable);
            final Document powerPointDocument = FileDocumentFactory.createDocument(resourceContext, new LocalFileImpl(getTestFile("PowerPointDocument.pptx")));
            // check content
            final String content = powerPointDocument.get(AbstractOlatDocument.CONTENT_FIELD_NAME);
            assertTrue("Wrong PowerPoint (pptx) content", content.contains("Slide1"));
            assertTrue("Wrong PowerPoint (pptx) content", content.contains("ContentSlide1"));
            assertTrue("Wrong PowerPoint (pptx) content", content.contains("Oliver: CommentSlide1"));
            assertTrue("Wrong PowerPoint (pptx) content", content.contains("NoteSlide1"));
            assertTrue("Wrong PowerPoint (pptx) content", content.contains("Slide2"));
            assertTrue("Wrong PowerPoint (pptx) content", content.contains("ContentSlide2"));
            assertTrue("Wrong PowerPoint (pptx) content", content.contains("Oliver: CommentSlide2"));
            assertTrue("Wrong PowerPoint (pptx) content", content.contains("NoteSlide2"));
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private File getTestFile(String fileName) {
        String filePath = getClass().getPackage().getName().replaceAll("\\.", "/") + "/" + fileName;
        return new File(getClass().getClassLoader().getResource(filePath).getFile());
    }
}
