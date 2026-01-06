package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Position;
import com.pdfdancer.common.model.text.TextLine;
import com.pdfdancer.common.model.text.Word;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Word model and TextLine.words property.
 */
public class WordTest extends BaseTest {
    private static final Logger log = LoggerFactory.getLogger(WordTest.class);

    @Test
    public void wordModelHasExpectedProperties() {
        Position position = Position.atPageCoordinates(1, 100, 200);
        Word word = new Word("test-id", "Hello", position);

        assertEquals("Hello", word.getText());
        assertEquals("test-id", word.getId());
        assertNotNull(word.getPosition());
    }

    @Test
    public void textLineCanHaveWords() {
        TextLine textLine = new TextLine();
        textLine.setText("Hello World");

        Word word1 = new Word("w1", "Hello", null);
        Word word2 = new Word("w2", "World", null);
        textLine.setWords(List.of(word1, word2));

        List<Word> words = textLine.getWords();
        assertNotNull(words);
        assertEquals(2, words.size());
        assertEquals("Hello", words.get(0).getText());
        assertEquals("World", words.get(1).getText());
    }

    @Test
    public void textLineWordsCanBeNull() {
        TextLine textLine = new TextLine();
        textLine.setText("Hello World");
        // Don't set words - should be null
        assertNull(textLine.getWords());
    }
}
