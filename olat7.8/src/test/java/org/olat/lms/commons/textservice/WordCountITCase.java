package org.olat.lms.commons.textservice;

import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.data.commons.filter.Filter;
import org.olat.data.forum.TestTextCase;
import org.olat.lms.forum.QuoteAndTagFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations = { "classpath:/org/olat/lms/commons/textservice/textServiceMock.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class WordCountITCase {

    @Autowired
    TextService languageService;

    @Test
    public void testCleanMessage() {
        final Filter filter = new QuoteAndTagFilter();
        final String text = "<p>&nbsp;</p><div class=\"b_quote_wrapper\"><div class=\"b_quote_author mceNonEditable\">Am 23.11.09 12:29 hat OLAT Administrator geschrieben:</div><blockquote class=\"b_quote\"><p>Quelques mots que je voulais &eacute;crire. Et encore un ou deux.</p></blockquote></div><p>Et une r&eacute;ponse avec citation incorpor&eacute;e</p>";
        final String output = filter.filter(text);
        assertTrue("  Et une réponse avec citation incorporée".equals(output));
    }

    /**
     * Test pass if the detection is better as 80%
     */
    @Test
    public void testDetectLanguage() {
        double count = 0;
        for (final TestTextCase.Text text : TestTextCase.getCases()) {
            final Locale locale = languageService.detectLocale(text.getText());
            if (locale != null && locale.getLanguage().equals(text.getLanguage())) {
                count++;
            }
        }
        final double ratio = count / TestTextCase.getCases().length;
        assertTrue(ratio > 0.8d);
    }

    @Test
    public void testWordCount() {
        for (final TestTextCase.Text text : TestTextCase.getCases()) {
            final Locale locale = new Locale(text.getLanguage());
            final int words = languageService.wordCount(text.getText(), locale);
            assertTrue(words == text.getWords());
        }
    }

    @Test
    public void testCharacterCount() {
        for (final TestTextCase.Text text : TestTextCase.getCases()) {
            final Locale locale = new Locale(text.getLanguage());
            final int characters = languageService.characterCount(text.getText(), locale);
            assertTrue(characters == text.getCharacters());
        }
    }
}
