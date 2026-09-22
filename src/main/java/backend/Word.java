package backend;

/**
 * One word the player has to type. Each keypress is checked against the next letter in {@code text}.
 */
public class Word {
    private String text;
    private Difficulty difficulty;
    private String typedSoFar;
    private boolean isComplete;

    /**
     * New word: nothing typed yet, not complete.
     *
     * @param text        the full string the player must type
     * @param difficulty  which pool the word came from (easy / medium / hard)
     */
    public Word(String text, Difficulty difficulty) {
        this.text = text;
        this.difficulty = difficulty;
        this.typedSoFar = "";
        this.isComplete = false;
    }

    /**
     * Call when the user presses a key. If it matches the next expected character we append it.
     *
     * @param c the character typed
     * @return {@code true} if that letter was correct, {@code false} if wrong or the word was already done
     */
    public boolean typeChar(char c) {
        if (isComplete)
            return false;

        int nextIndex = typedSoFar.length();
        if (nextIndex < text.length() && text.charAt(nextIndex) == c) {
            typedSoFar += c;
            if (typedSoFar.length() == text.length()) {
                isComplete = true;
            }
            return true;
        }
        return false;
    }

    /**
     * The full target word.
     *
     * @return the word text
     */
    public String getText() {
        return text;
    }

    /**
     * Correct prefix typed so far; the UI uses this plus {@link #getText()} for green / gray parts.
     *
     * @return substring of correct keystrokes
     */
    public String getTypedSoFar() {
        return typedSoFar;
    }

    /**
     * Whether the player finished every letter.
     *
     * @return {@code true} if the word is fully typed
     */
    public boolean isComplete() {
        return isComplete;
    }

    /**
     * Difficulty tag for this word.
     *
     * @return easy, medium, or hard
     */
    public Difficulty getDifficulty() {
        return difficulty;
    }

    /**
     * Clears progress so the same word can be reused (new order / reset).
     */
    public void reset() {
        typedSoFar = "";
        isComplete = false;
    }

}
