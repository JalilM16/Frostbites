package backend;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Library of typing words grouped by difficulty.
 * Ice Cream Shop uses EASY, Busy Mall uses MEDIUM, Mega Dessert Factory uses HARD.
 */
public class WordLibrary {

    private List<Word> easyWords;
    private List<Word> mediumWords;
    private List<Word> hardWords;
    private Random random;

    /** empty pools; call {@link #loadDefaults()} before {@link #getRandomWord} */
    public WordLibrary() {
        easyWords = new ArrayList<>();
        mediumWords = new ArrayList<>();
        hardWords = new ArrayList<>();
        random = new Random();
    }

    /**
     * Loads default word pools if lists are empty. Call once at startup.
     */
    public void loadDefaults() {
        if (!easyWords.isEmpty() || !mediumWords.isEmpty() || !hardWords.isEmpty()) {
            return;
        }

        String[] easy = buildEasyWordList();
        String[] medium = buildMediumWordList();
        String[] hard = buildHardWordList();

        for (int i = 0; i < easy.length; i++) {
            easyWords.add(new Word(easy[i], Difficulty.EASY));
        }
        for (int i = 0; i < medium.length; i++) {
            mediumWords.add(new Word(medium[i], Difficulty.MEDIUM));
        }
        for (int i = 0; i < hard.length; i++) {
            hardWords.add(new Word(hard[i], Difficulty.HARD));
        }
    }

    /**
     * Short, simple words for the first level (Ice Cream Shop).
     */
    private String[] buildEasyWordList() {
        return new String[] {
            "cone", "milk", "drip", "swirl", "fudge", "cup", "scoop", "ice", "cold",
            "sweet", "dish", "bowl", "chip", "mint", "pink", "blue", "soft", "cool",
            "bite", "sip", "yum", "pop", "nut", "jam", "pie", "hot", "kid",
            "shop", "line", "wait", "fast", "nice", "fresh", "cream", "sugar", "salt",
            "flake", "drop", "dip", "mix", "blend", "shake", "foam", "glaze", "tart"
        };
    }

    /**
     * Medium-length words for the second level (Busy Mall).
     */
    private String[] buildMediumWordList() {
        return new String[] {
            "sundae", "caramel", "vanilla", "sherbet", "topping", "waffle", "sprinkle",
            "brownie", "cookie", "banana", "cherry", "peach", "mango", "lemon", "berry",
            "frozen", "dessert", "parfait", "gelato", "sorbet", "smooth", "creamy",
            "double", "triple", "single", "order", "serve", "build", "happy", "quick",
            "busy", "mall", "crowd", "queue", "counter", "register", "receipt", "change",
            "customer", "waiting", "hurry", "minute", "second", "timer", "patience",
            "flavor", "recipe", "kitchen", "station", "window", "display", "bright",
            "sweeten", "drizzle", "layered", "scooped", "chilled", "treat", "snack"
        };
    }

    /**
     * Longer, harder words for the third level (Mega Dessert Factory).
     */
    private String[] buildHardWordList() {
        return new String[] {
            "chocolate", "strawberry", "pistachio", "butterscotch", "marshmallow",
            "raspberry", "blackberry", "watermelon", "cappuccino", "espresso",
            "cheesecake", "fudgecake", "browniesundae", "neapolitan", "rockyroad",
            "cookiescream", "mintchocolate", "saltedcaramel", "vanillabean",
            "production", "conveyor", "factory", "assembly", "quality", "inspection",
            "packaging", "shipment", "inventory", "warehouse", "machine", "operator",
            "supervisor", "deadline", "overtime", "efficiency", "throughput", "batch",
            "ingredient", "preservative", "temperature", "refrigeration", "sterile",
            "industrial", "automation", "precision", "calibration", "maintenance"
        };
    }

    /**
     * Returns a random word for the given difficulty tier.
     * Always returns a new Word so typing progress does not carry over.
     */
    public Word getRandomWord(Difficulty d) {
        List<Word> pool;

        if (d == Difficulty.EASY) {
            pool = easyWords;
        } else if (d == Difficulty.MEDIUM) {
            pool = mediumWords;
        } else {
            pool = hardWords;
        }

        if (pool.isEmpty()) {
            loadDefaults();
        }

        Word selected = pool.get(random.nextInt(pool.size()));
        return new Word(selected.getText(), selected.getDifficulty());
    }

    /**
     * Total number of words loaded across all difficulties.
     */
    public int getTotalWordCount() {
        return easyWords.size() + mediumWords.size() + hardWords.size();
    }

    /**
     * Adds a custom word to the correct pool.
     */
    public void addWord(Word w) {
        if (w.getDifficulty() == Difficulty.EASY) {
            easyWords.add(w);
        } else if (w.getDifficulty() == Difficulty.MEDIUM) {
            mediumWords.add(w);
        } else {
            hardWords.add(w);
        }
    }
}
