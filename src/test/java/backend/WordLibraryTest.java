package backend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class WordLibraryTest {

    private WordLibrary library;

    @BeforeEach
    void setUp() {
        library = new WordLibrary();
        library.loadDefaults();
    }

    @Test
    void testTotalWordCountAfterDefaults() {
        assertEquals(151, library.getTotalWordCount());
    }

    @Test
    void testGetRandomWordReturnsCorrectDifficulty() {
        Word w = library.getRandomWord(Difficulty.EASY);
        assertEquals(Difficulty.EASY, w.getDifficulty());
    }

    @Test
    void testGetRandomWordMedium() {
        Word w = library.getRandomWord(Difficulty.MEDIUM);
        assertEquals(Difficulty.MEDIUM, w.getDifficulty());
    }

    @Test
    void testGetRandomWordHard() {
        Word w = library.getRandomWord(Difficulty.HARD);
        assertEquals(Difficulty.HARD, w.getDifficulty());
    }
}