package backend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class WordTest {

    private Word word;

    @BeforeEach
    void setUp() {
        word = new Word("cone", Difficulty.EASY);
    }

    @Test
    void testCorrectCharAdvancesProgress() {
        assertTrue(word.typeChar('c'));
        assertEquals("c", word.getTypedSoFar());
    }

    @Test
    void testWrongCharDoesNotAdvance() {
        assertFalse(word.typeChar('x'));
        assertEquals("", word.getTypedSoFar());
    }

    @Test
    void testWordCompletesWhenFullyTyped() {
        word.typeChar('c');
        word.typeChar('o');
        word.typeChar('n');
        word.typeChar('e');
        assertTrue(word.isComplete());
    }

    @Test
    void testResetClearsProgress() {
        word.typeChar('c');
        word.reset();
        assertEquals("", word.getTypedSoFar());
        assertFalse(word.isComplete());
    }
}