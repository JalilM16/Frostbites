package backend;

/**
 * Rolling totals for one player: games played, time, chars typed, errors, bests, etc.
 * Gson loads and saves this on {@link PlayerAccount}.
 */
public class PlayerStats {
    private int gamesPlayed;
    private int totalTimeSeconds;
    private int totalErrorCount;
    private int totalTypedChars;
    private int totalCorrectChars;

    private double bestWpm;
    private double bestAccuracyPct;

    /** We add each game's WPM here so we can divide by gamesPlayed for an average. */
    private double wpmSumForRunningAverage;

    /**
     * Empty stats; Gson needs this no-arg constructor.
     */
    public PlayerStats() {
        this.gamesPlayed = 0;
        this.totalTimeSeconds = 0;
        this.totalErrorCount = 0;
        this.totalTypedChars = 0;
        this.totalCorrectChars = 0;
        this.bestWpm = 0.0;
        this.bestAccuracyPct = 0.0;
        this.wpmSumForRunningAverage = 0.0;
    }

    /**
     * Builds stats from existing numbers (e.g. tests or loading).
     *
     * @param gamesPlayed              total games finished
     * @param totalTimeSeconds         sum of session lengths in seconds
     * @param totalErrorCount          sum of mistake counts
     * @param totalTypedChars          all keystrokes counted
     * @param totalCorrectChars        correct keystrokes
     * @param bestWpm                  highest single-game WPM seen
     * @param bestAccuracyPct          highest single-game accuracy percent seen
     * @param wpmSumForRunningAverage  sum of per-game WPM for averaging
     */
    public PlayerStats(
            int gamesPlayed,
            int totalTimeSeconds,
            int totalErrorCount,
            int totalTypedChars,
            int totalCorrectChars,
            double bestWpm,
            double bestAccuracyPct,
            double wpmSumForRunningAverage) {
        this.gamesPlayed = gamesPlayed;
        this.totalTimeSeconds = totalTimeSeconds;
        this.totalErrorCount = totalErrorCount;
        this.totalTypedChars = totalTypedChars;
        this.totalCorrectChars = totalCorrectChars;
        this.bestWpm = bestWpm;
        this.bestAccuracyPct = bestAccuracyPct;
        this.wpmSumForRunningAverage = wpmSumForRunningAverage;
    }

    /**
     * @return how many games were recorded
     */
    public int getGamesPlayed() {
        return gamesPlayed;
    }

    /**
     * @param gamesPlayed new total games count
     */
    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    /**
     * @return combined play time in seconds
     */
    public int getTotalTimeSeconds() {
        return totalTimeSeconds;
    }

    /**
     * @param totalTimeSeconds new total seconds
     */
    public void setTotalTimeSeconds(int totalTimeSeconds) {
        this.totalTimeSeconds = totalTimeSeconds;
    }

    /**
     * @return total mistakes across games
     */
    public int getTotalErrorCount() {
        return totalErrorCount;
    }

    /**
     * @param totalErrorCount new total error count
     */
    public void setTotalErrorCount(int totalErrorCount) {
        this.totalErrorCount = totalErrorCount;
    }

    /**
     * @return all keystrokes logged (correct + wrong)
     */
    public int getTotalTypedChars() {
        return totalTypedChars;
    }

    /**
     * @param totalTypedChars new total typed character count
     */
    public void setTotalTypedChars(int totalTypedChars) {
        this.totalTypedChars = totalTypedChars;
    }

    /**
     * @return subset of keystrokes that matched the target
     */
    public int getTotalCorrectChars() {
        return totalCorrectChars;
    }

    /**
     * @param totalCorrectChars new total correct character count
     */
    public void setTotalCorrectChars(int totalCorrectChars) {
        this.totalCorrectChars = totalCorrectChars;
    }

    /**
     * @return best WPM from any single saved game
     */
    public double getBestWpm() {
        return bestWpm;
    }

    /**
     * @param bestWpm new stored peak WPM
     */
    public void setBestWpm(double bestWpm) {
        this.bestWpm = bestWpm;
    }

    /**
     * @return best accuracy percent from any single saved game
     */
    public double getBestAccuracyPct() {
        return bestAccuracyPct;
    }

    /**
     * @param bestAccuracyPct new stored peak accuracy
     */
    public void setBestAccuracyPct(double bestAccuracyPct) {
        this.bestAccuracyPct = bestAccuracyPct;
    }

    /**
     * Average WPM across games. Uses {@code wpmSumForRunningAverage} / games if we have that;
     * otherwise falls back to {@link #getAverageWpmEstimated()} for old saves.
     *
     * @return average WPM, or 0.0 if we cannot compute it
     */
    public double getAverageWpm() {
        if (gamesPlayed <= 0) {
            return 0.0;
        }
        if (wpmSumForRunningAverage > 0.0) {
            return wpmSumForRunningAverage / gamesPlayed;
        }
        if (totalTimeSeconds > 0 && totalTypedChars > 0) {
            return getAverageWpmEstimated();
        }
        return 0.0;
    }

    /**
     * Same as {@link #getBestWpm()}; name reads nicer on dashboards.
     *
     * @return highest single-game WPM stored
     */
    public double getPeakWpm() {
        return bestWpm;
    }

    /**
     * Delegates to {@link #getAverageAccuracyPct()}.
     *
     * @return overall accuracy as a percent
     */
    public double getAccuracyPercentage() {
        return getAverageAccuracyPct();
    }

    /**
     * @return same as {@link #getTotalErrorCount()}
     */
    public int getErrorCount() {
        return totalErrorCount;
    }

    /**
     * @return estimated words from total chars (5 chars per word)
     */
    public int getWordsTyped() {
        return getEstimatedWordsTyped();
    }

    /**
     * Overall accuracy: correctChars / typedChars.
     *
     * @return percent 0-100, or 0.0 if nothing typed yet
     */
    public double getAverageAccuracyPct() {
        if (totalTypedChars <= 0)
            return 0.0;
        return (100.0 * totalCorrectChars) / totalTypedChars;
    }

    /**
     * @return {@code totalTimeSeconds} converted to minutes
     */
    public double getTotalTimeMinutes() {
        return totalTimeSeconds / 60.0;
    }

    /**
     * Typing-test style: 5 keystrokes counts as one word.
     *
     * @return rounded word estimate
     */
    public int getEstimatedWordsTyped() {
        return (int) Math.round(totalTypedChars / 5.0);
    }

    /**
     * Rough WPM from lifetime chars and time: (chars / 5) / minutes.
     * Used when older JSON does not have {@code wpmSumForRunningAverage}.
     *
     * @return estimated average WPM, or 0 if time is missing
     */
    public double getAverageWpmEstimated() {
        if (totalTimeSeconds <= 0) {
            return 0;
        }
        double minutes = totalTimeSeconds / 60.0;
        if (minutes <= 0) {
            return 0;
        }
        double words = totalTypedChars / 5.0;
        return words / minutes;
    }

    /**
     * Call after a game ends: bumps counters, updates bests, adds this session's WPM to the running sum.
     * Integer inputs are clamped to non-negative so bad data does not break averages.
     *
     * @param wpm           session WPM to fold into averages and bests
     * @param accuracyPct   session accuracy percent
     * @param errorCount    mistakes this session
     * @param timeSeconds   session length in seconds
     * @param typedChars    keystrokes this session
     * @param correctChars  correct keystrokes this session
     */
    public void recordGame(double wpm, double accuracyPct, int errorCount, int timeSeconds, int typedChars,
            int correctChars) {
        gamesPlayed += 1;
        totalTimeSeconds += Math.max(0, timeSeconds);
        totalErrorCount += Math.max(0, errorCount);
        totalTypedChars += Math.max(0, typedChars);
        totalCorrectChars += Math.max(0, correctChars);
        wpmSumForRunningAverage += wpm;
        bestWpm = Math.max(bestWpm, wpm);
        bestAccuracyPct = Math.max(bestAccuracyPct, accuracyPct);
    }
}
