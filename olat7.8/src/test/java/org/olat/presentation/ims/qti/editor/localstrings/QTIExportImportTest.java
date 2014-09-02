package org.olat.presentation.ims.qti.editor.localstrings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.apache.log4j.Level;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.olat.data.commons.xml.XMLParser;
import org.olat.lms.ims.qti.editor.QTIEditHelperEBL;
import org.olat.lms.ims.qti.editor.localstrings.QtiEditorLocalStrings;
import org.olat.lms.ims.qti.objects.Assessment;
import org.olat.lms.ims.qti.objects.ChoiceResponse;
import org.olat.lms.ims.qti.objects.Item;
import org.olat.lms.ims.qti.objects.QTIDocument;
import org.olat.lms.ims.qti.objects.Question;
import org.olat.lms.ims.qti.objects.Response;
import org.olat.lms.ims.qti.objects.Section;
import org.olat.lms.ims.qti.parser.ParserManager;
import org.olat.lms.ims.resources.IMSEntityResolver;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.CodeHelperInitalizer;

import com.thoughtworks.xstream.XStream;

public class QTIExportImportTest {

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    private static final Translator TRANSLATOR = new TestTranslator();

    private static QtiEditorLocalStringsAbstractFactory scItemLocalStringsFactory;
    private static QtiEditorLocalStringsAbstractFactory mcItemLocalStringsFactory;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        scItemLocalStringsFactory = new ScItemLocalStringsFactory(TRANSLATOR);
        mcItemLocalStringsFactory = new McItemLocalStringsFactory(TRANSLATOR);
        new CodeHelperInitalizer();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSCQOneResponse() throws IOException {
        QTIDocument qtiDocOrig = createTest("SCQOneResponse");

        addSingleChoice(qtiDocOrig, ResponseDefinition.STANDARD_POINTS, ResponseDefinition.correctResponse());

        QTIDocument qtiDocRestored = exportAndImportToQTIFormat(qtiDocOrig);
        compareQTIDocuments(qtiDocOrig, qtiDocRestored, true);
    }

    @Test
    public void testSCQMultipleResponses() throws IOException {
        QTIDocument qtiDocOrig = createTest("SCQMultipleResponses");

        addSingleChoice(qtiDocOrig, ResponseDefinition.STANDARD_POINTS, ResponseDefinition.wrongResponse(), ResponseDefinition.correctResponse(),
                ResponseDefinition.wrongResponse());

        QTIDocument qtiDocRestored = exportAndImportToQTIFormat(qtiDocOrig);
        compareQTIDocuments(qtiDocOrig, qtiDocRestored, true);
    }

    // multiple correct responses are not allowed
    // @Test(expected = AssertException.class)
    @Test
    public void testSCQMultipleCorrectResponses() throws IOException {
        QTIDocument qtiDocOrig = createTest("SCQMultipleCorrectResponses");

        addSingleChoice(qtiDocOrig, ResponseDefinition.STANDARD_POINTS, ResponseDefinition.correctResponse(), ResponseDefinition.correctResponse());

        QTIDocument qtiDocRestored = exportAndImportToQTIFormat(qtiDocOrig);
        // OLAT-6852: failure expected
        try {
            compareQTIDocuments(qtiDocOrig, qtiDocRestored, true);
            Assert.fail("QTIDocument expected to be different after exporting/importing since only one correct answer will be saved.");
        } catch (AssertionFailedError ex) {
            // expected
        }
    }

    // one correct response must be defined
    // @Test(expected = AssertException.class)
    @Test
    public void testSCQNoCorrectResponses() throws IOException {
        QTIDocument qtiDocOrig = createTest("SCQNoCorrectResponses");

        addSingleChoice(qtiDocOrig, ResponseDefinition.STANDARD_POINTS, ResponseDefinition.wrongResponse(), ResponseDefinition.wrongResponse());

        QTIDocument qtiDocRestored = exportAndImportToQTIFormat(qtiDocOrig);
        // OLAT-6852: failure expected
        try {
            compareQTIDocuments(qtiDocOrig, qtiDocRestored, true);
            Assert.fail("QTIDocument expected to be different after exporting/importing since one correct answer is necessary.");
        } catch (AssertionFailedError ex) {
            // expected
        }
    }

    @Test
    public void testMCQOneResponseCorrectSingle() throws IOException {
        QTIDocument qtiDocOrig = createTest("MCQOneResponseCorrectSingle");

        addMultipleChoiceSingleCorrect(qtiDocOrig, ResponseDefinition.STANDARD_POINTS, ResponseDefinition.correctResponse());

        QTIDocument qtiDocRestored = exportAndImportToQTIFormat(qtiDocOrig);
        compareQTIDocuments(qtiDocOrig, qtiDocRestored, true);
    }

    @Test
    public void testMCQOneResponseWrongSingle() throws IOException {
        QTIDocument qtiDocOrig = createTest("MCQOneResponseWrongSingle");

        addMultipleChoiceSingleCorrect(qtiDocOrig, ResponseDefinition.STANDARD_POINTS, ResponseDefinition.wrongResponse());

        QTIDocument qtiDocRestored = exportAndImportToQTIFormat(qtiDocOrig);
        compareQTIDocuments(qtiDocOrig, qtiDocRestored, true);
    }

    @Test
    public void testMCQMultipleResponsesSingle() throws IOException {
        QTIDocument qtiDocOrig = createTest("MCQMultipleResponsesSingle");

        addMultipleChoiceSingleCorrect(qtiDocOrig, ResponseDefinition.STANDARD_POINTS, ResponseDefinition.correctResponse(), ResponseDefinition.wrongResponse(),
                ResponseDefinition.wrongResponse(), ResponseDefinition.correctResponse());

        QTIDocument qtiDocRestored = exportAndImportToQTIFormat(qtiDocOrig);
        compareQTIDocuments(qtiDocOrig, qtiDocRestored, true);
    }

    @Test
    public void testMCQAllCorrectResponsesSingle() throws IOException {
        QTIDocument qtiDocOrig = createTest("MCQAllCorrectResponsesSingle");

        addMultipleChoiceSingleCorrect(qtiDocOrig, ResponseDefinition.STANDARD_POINTS, ResponseDefinition.correctResponse(), ResponseDefinition.correctResponse(),
                ResponseDefinition.correctResponse());

        QTIDocument qtiDocRestored = exportAndImportToQTIFormat(qtiDocOrig);
        compareQTIDocuments(qtiDocOrig, qtiDocRestored, true);
    }

    @Test
    public void testMCQNoCorrectResponsesSingle() throws IOException {
        QTIDocument qtiDocOrig = createTest("MCQNoCorrectResponsesSingle");

        addMultipleChoiceSingleCorrect(qtiDocOrig, ResponseDefinition.STANDARD_POINTS, ResponseDefinition.wrongResponse(), ResponseDefinition.wrongResponse(),
                ResponseDefinition.wrongResponse());

        QTIDocument qtiDocRestored = exportAndImportToQTIFormat(qtiDocOrig);
        compareQTIDocuments(qtiDocOrig, qtiDocRestored, true);
    }

    @Test
    public void testMCQOneResponseCorrectSpecific() throws IOException {
        QTIDocument qtiDocOrig = createTest("MCQOneResponseCorrectSpecific");

        addMultipleChoiceSpecificPoints(qtiDocOrig, 0.0f, ResponseDefinition.STANDARD_POINTS, ResponseDefinition.correctResponse());

        QTIDocument qtiDocRestored = exportAndImportToQTIFormat(qtiDocOrig);
        compareQTIDocuments(qtiDocOrig, qtiDocRestored, true);
    }

    @Test(expected = AssertionError.class)
    public void testMCQOneResponseWrongSpecific() throws IOException {
        QTIDocument qtiDocOrig = createTest("MCQOneResponseWrongSpecific");

        addMultipleChoiceSpecificPoints(qtiDocOrig, 0.0f, ResponseDefinition.STANDARD_POINTS, ResponseDefinition.wrongResponse());

        QTIDocument qtiDocRestored = exportAndImportToQTIFormat(qtiDocOrig);
        compareQTIDocuments(qtiDocOrig, qtiDocRestored, false);
    }

    @Test(expected = AssertionError.class)
    public void testMCQMultipleResponsesSpecific() throws IOException {
        QTIDocument qtiDocOrig = createTest("MCQMultipleResponsesSpecific");

        addMultipleChoiceSpecificPoints(qtiDocOrig, -1.0f, ResponseDefinition.STANDARD_POINTS + 1.0f, ResponseDefinition.correctResponse(),
                ResponseDefinition.wrongResponse(), new ResponseDefinition(false, -1.0f), new ResponseDefinition(true, 1.0f));

        QTIDocument qtiDocRestored = exportAndImportToQTIFormat(qtiDocOrig);
        compareQTIDocuments(qtiDocOrig, qtiDocRestored, false);
    }

    @Test(expected = AssertionError.class)
    public void testMCQResponseZeroPointsCorrectSpecific() throws IOException {
        QTIDocument qtiDocOrig = createTest("MCQResponseZeroPointsCorrectSpecific");

        addMultipleChoiceSpecificPoints(qtiDocOrig, 0.0f, ResponseDefinition.STANDARD_POINTS, ResponseDefinition.correctResponse(), ResponseDefinition.wrongResponse(),
                new ResponseDefinition(true, 0.0f));

        QTIDocument qtiDocRestored = exportAndImportToQTIFormat(qtiDocOrig);
        compareQTIDocuments(qtiDocOrig, qtiDocRestored, false);
    }

    @Test
    public void testMCQResponseZeroPointsWrongSpecific() throws IOException {
        QTIDocument qtiDocOrig = createTest("MCQResponseZeroPointsWrongSpecific");

        addMultipleChoiceSpecificPoints(qtiDocOrig, 0.0f, ResponseDefinition.STANDARD_POINTS, ResponseDefinition.correctResponse(), ResponseDefinition.wrongResponse(),
                new ResponseDefinition(false, 0.0f));

        QTIDocument qtiDocRestored = exportAndImportToQTIFormat(qtiDocOrig);
        compareQTIDocuments(qtiDocOrig, qtiDocRestored, true);
    }

    @Test
    public void testMCQAllCorrectResponsesSpecific() throws IOException {
        QTIDocument qtiDocOrig = createTest("MCQAllCorrectResponsesSpecific");

        addMultipleChoiceSpecificPoints(qtiDocOrig, 0.0f, ResponseDefinition.STANDARD_POINTS + 1.0f + 3.0f, ResponseDefinition.correctResponse(), new ResponseDefinition(
                true, 1.0f), new ResponseDefinition(true, 3.0f));

        QTIDocument qtiDocRestored = exportAndImportToQTIFormat(qtiDocOrig);
        compareQTIDocuments(qtiDocOrig, qtiDocRestored, true);
    }

    @Test(expected = AssertionError.class)
    public void testMCQNoCorrectResponsesSpecific() throws IOException {
        QTIDocument qtiDocOrig = createTest("MCQNoCorrectResponsesSpecific");

        addMultipleChoiceSpecificPoints(qtiDocOrig, 0.0f, 1.0f, ResponseDefinition.wrongResponse(), ResponseDefinition.wrongResponse(),
                ResponseDefinition.wrongResponse());

        QTIDocument qtiDocRestored = exportAndImportToQTIFormat(qtiDocOrig);
        compareQTIDocuments(qtiDocOrig, qtiDocRestored, false);
    }

    private static QTIDocument createTest(String testName) {
        QTIDocument qtiDoc = new QTIDocument();
        Assessment test = new Assessment();
        test.setTitle(testName);
        Section section = new Section();
        section.setIdent("Section1");
        section.setTitle("Section1");
        section.setSections(new ArrayList<Section>());
        List<Section> sections = new ArrayList<Section>();
        sections.add(section);
        test.setSections(sections);
        qtiDoc.setAssessment(test);
        return qtiDoc;
    }

    private static void addSingleChoice(QTIDocument qtiDoc, float points, ResponseDefinition... responseDefinitions) {
        final List<Section> sections = qtiDoc.getAssessment().getSections();
        final int lastSectionNumber = sections.size();
        final Section lastSection = sections.get(lastSectionNumber - 1);

        final List<Item> items = lastSection.getItems();
        final int lastItemNumber = items.size();

        QtiEditorLocalStrings qtiEditorLocalStrings = scItemLocalStringsFactory.createLocalStrings();

        final Item item = QTIEditHelperEBL.createSCItem(qtiEditorLocalStrings);
        item.setTitle("Question" + lastSectionNumber + (lastItemNumber + 1));
        item.setLabel(null);
        lastSection.getItems().add(item);

        final Question question = item.getQuestion();
        question.setLable(null);
        question.setSingleCorrectScore(points);
        final List<Response> responses = new ArrayList<Response>();
        if (responseDefinitions != null) {
            int counter = 0;
            for (ResponseDefinition responseDef : responseDefinitions) {
                counter++;
                ChoiceResponse response = new ChoiceResponse();
                response.setIdent("Answer" + lastSectionNumber + (lastItemNumber + 1) + ":" + counter);
                response.setCorrect(responseDef.correct);
                if (responseDef.correct) {
                    response.setPoints(points);
                }
                responses.add(response);
            }
        }
        question.setResponses(responses);
    }

    private static void addMultipleChoiceSingleCorrect(QTIDocument qtiDoc, float points, ResponseDefinition... responseDefinitions) {
        final List<Section> sections = qtiDoc.getAssessment().getSections();
        final int lastSectionNumber = sections.size();
        final Section lastSection = sections.get(lastSectionNumber - 1);

        final List<Item> items = lastSection.getItems();
        final int lastItemNumber = items.size();

        QtiEditorLocalStrings qtiEditorLocalStrings = mcItemLocalStringsFactory.createLocalStrings();

        final Item item = QTIEditHelperEBL.createMCItem(qtiEditorLocalStrings);
        item.setTitle("Question" + lastSectionNumber + (lastItemNumber + 1));
        item.setLabel(null);
        lastSection.getItems().add(item);

        final Question question = item.getQuestion();
        question.setLable(null);
        question.setSingleCorrect(true);
        question.setSingleCorrectScore(points);
        final List<Response> responses = new ArrayList<Response>();
        if (responseDefinitions != null) {
            int counter = 0;
            for (ResponseDefinition responseDef : responseDefinitions) {
                counter++;
                ChoiceResponse response = new ChoiceResponse();
                response.setIdent("Answer" + lastSectionNumber + (lastItemNumber + 1) + ":" + counter);
                response.setCorrect(responseDef.correct);
                if (responseDef.correct) {
                    response.setPoints(points);
                }
                responses.add(response);
            }
        }
        question.setResponses(responses);
    }

    private static void addMultipleChoiceSpecificPoints(QTIDocument qtiDoc, float minValue, float maxValue, ResponseDefinition... responseDefinitions) {
        final List<Section> sections = qtiDoc.getAssessment().getSections();
        final int lastSectionNumber = sections.size();
        final Section lastSection = sections.get(lastSectionNumber - 1);

        final List<Item> items = lastSection.getItems();
        final int lastItemNumber = items.size();

        QtiEditorLocalStrings qtiEditorLocalStrings = mcItemLocalStringsFactory.createLocalStrings();

        final Item item = QTIEditHelperEBL.createMCItem(qtiEditorLocalStrings);
        item.setTitle("Question" + lastSectionNumber + (lastItemNumber + 1));
        item.setLabel(null);
        lastSection.getItems().add(item);

        final Question question = item.getQuestion();
        question.setLable(null);
        question.setSingleCorrect(false);
        question.setMinValue(minValue);
        question.setMaxValue(maxValue);
        final List<Response> responses = new ArrayList<Response>();
        if (responseDefinitions != null) {
            int counter = 0;
            for (ResponseDefinition responseDef : responseDefinitions) {
                counter++;
                ChoiceResponse response = new ChoiceResponse();
                response.setIdent("Answer" + lastSectionNumber + (lastItemNumber + 1) + ":" + counter);
                response.setCorrect(responseDef.correct);
                response.setPoints(responseDef.points);
                responses.add(response);
            }
        }
        question.setResponses(responses);
    }

    private static QTIDocument exportAndImportToQTIFormat(QTIDocument qtiDocOrig) throws IOException {
        Document qtiXmlDoc = qtiDocOrig.getDocument();
        OutputFormat outformat = OutputFormat.createPrettyPrint();

        String fileName = qtiDocOrig.getAssessment().getTitle() + "QTIFormat.xml";
        OutputStreamWriter qtiXmlOutput = new OutputStreamWriter(new FileOutputStream(new File(TEMP_DIR, fileName)), Charset.forName("UTF-8"));
        XMLWriter writer = new XMLWriter(qtiXmlOutput, outformat);
        writer.write(qtiXmlDoc);
        writer.flush();
        writer.close();

        XMLParser xmlParser = new XMLParser(new IMSEntityResolver());
        Document doc = xmlParser.parse(new FileInputStream(new File(TEMP_DIR, fileName)), true);
        ParserManager parser = new ParserManager();
        QTIDocument qtiDocRestored = (QTIDocument) parser.parse(doc);
        return qtiDocRestored;
    }

    private static void compareQTIDocuments(QTIDocument doc1, QTIDocument doc2, boolean streamFailureResult) throws IOException {
        try {
            if (doc1.getAssessment().getSections().size() != doc2.getAssessment().getSections().size()) {
                throw new AssertionError("Number of sections different.");
            }

            for (int sectionCounter = 0; sectionCounter < doc1.getAssessment().getSections().size(); sectionCounter++) {
                final Section s1 = doc1.getAssessment().getSections().get(sectionCounter);
                final Section s2 = doc2.getAssessment().getSections().get(sectionCounter);
                if (!s1.getIdent().equals(s2.getIdent())) {
                    throw new AssertionError("Identifier of section different.");
                }

                if (s1.getItems().size() != s2.getItems().size()) {
                    throw new AssertionError("Number of items different.");
                }

                for (int itemCounter = 0; itemCounter < s1.getItems().size(); itemCounter++) {
                    final Item item1 = s1.getItems().get(itemCounter);
                    final Item item2 = s2.getItems().get(itemCounter);
                    if (!item1.getIdent().equals(item2.getIdent())) {
                        throw new AssertionError("Identifier of item different.");
                    }

                    final Question question1 = item1.getQuestion();
                    final Question question2 = item2.getQuestion();
                    if (!question1.getIdent().equals(question2.getIdent())) {
                        throw new AssertionError("Identifier of question different.");
                    }
                    if (question1.isSingleCorrect() != question2.isSingleCorrect()) {
                        throw new AssertionError("SingleCorrect for question " + question1.getIdent() + " different.");
                    }
                    if (question1.getSingleCorrectScore() != question2.getSingleCorrectScore()) {
                        throw new AssertionError("SingleCorrectScore for question " + question1.getIdent() + " different.");
                    }
                    if (question1.getMinValue() != question2.getMinValue()) {
                        throw new AssertionError("MinValue for question " + question1.getIdent() + " different.");
                    }
                    if (question1.getMaxValue() != question2.getMaxValue()) {
                        throw new AssertionError("MaxValue for question " + question1.getIdent() + " different.");
                    }

                    if (question2.getResponses().size() != question2.getResponses().size()) {
                        throw new AssertionError("Number of responses different.");
                    }
                    for (int responseCounter = 0; responseCounter < question1.getResponses().size(); responseCounter++) {
                        final Response response1 = question1.getResponses().get(responseCounter);
                        final Response response2 = question2.getResponses().get(responseCounter);
                        if (!response1.getIdent().equals(response2.getIdent())) {
                            throw new AssertionError("Identifier of response different.");
                        }
                        if (response1.isCorrect() != response2.isCorrect()) {
                            throw new AssertionError("Correct for response " + response1.getIdent() + " different.");
                        }
                        if (response1.getPoints() != response2.getPoints()) {
                            throw new AssertionError("Points for response " + response1.getIdent() + " different.");
                        }
                    }
                }
            }
        } catch (AssertionError ex) {
            if (streamFailureResult) {
                String qtiDocOrigXml = serializeQTIDocument(doc1);
                String qtiDocRestoredXml = serializeQTIDocument(doc2);
                writeToFile("qtiDocOriginal.xml", qtiDocOrigXml);
                writeToFile("qtiDocRestored.xml", qtiDocRestoredXml);
            }
            Assert.fail("QTIDocument not the same after exporting/importing. See " + TEMP_DIR + "/qtiDocOriginal.xml and " + TEMP_DIR + "/qtiDocRestored.xml ["
                    + ex.getMessage() + "]");
        }
    }

    private static String serializeQTIDocument(QTIDocument qtiDoc) {
        return new XStream().toXML(qtiDoc);
    }

    private static void writeToFile(String fileName, String content) throws IOException {
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(new File(TEMP_DIR, fileName)), Charset.forName("UTF-8"));
        osw.write(content);
        osw.flush();
        osw.close();
    }

    private static final class ResponseDefinition {
        private static final float STANDARD_POINTS = 2.0f;

        private boolean correct;
        private float points;

        private static ResponseDefinition wrongResponse() {
            return new ResponseDefinition(false, 0.0f);
        }

        // set 2.0 points for differentiation to default value 1.0
        private static ResponseDefinition correctResponse() {
            return new ResponseDefinition(true, STANDARD_POINTS);
        }

        private ResponseDefinition() {
            super();
        }

        private ResponseDefinition(boolean correct, float points) {
            this.correct = correct;
            this.points = points;
        }
    }

    private static final class TestTranslator implements Translator {

        @Override
        public String translate(String key) {
            if (key.equals("editor.newquestion") || key.equals("editor.newquestiontext")) {
                return "Neue Frage";
            } else if (key.equals("editor.newresponsetext")) {
                return "Neue Antwort";
            }
            throw new IllegalArgumentException("Key '" + key + "' not supported.");
        }

        @Override
        public String translate(String key, String[] args) {
            return translate(key);
        }

        @Override
        public String translate(String key, String[] args, Level missingTranslationLogLevel) {
            return translate(key);
        }

        @Override
        public String translate(String key, String[] args, boolean fallBackToDefaultLocale) {
            return translate(key);
        }

        @Override
        public Locale getLocale() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setLocale(Locale locale) {
            throw new UnsupportedOperationException();
        }
    }

}
