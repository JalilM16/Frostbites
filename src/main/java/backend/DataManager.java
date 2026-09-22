package backend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.*;

/**
 * Reads and writes persistent game data as JSON under the {@code data/} folder
 * ({@code Players.json} for accounts, {@code admin.json} for the admin PIN).
 * Gson is configured with pretty-printing so files are easy to inspect or grade by hand.
 */
public class DataManager {
    /** Relative path to the JSON array of {@link PlayerAccount} records*/
    private static final String FILE_PATH = "data/Players.json";
    /** Relative path to admin-only settings (currently just the PIN)*/
    private static final String ADMIN_FILE_PATH = "data/admin.json";
    /** Used when no admin file exists yet or the file is unreadable / missing a PIN */
    private static final String DEFAULT_PIN = "1234";
    private final Gson gson;

    /**
     * Gson shape for {@code admin.json}: a single object with a {@code pin} field.
     */
    private static class AdminConfig {
        String pin;
    }

    /**
     * Builds a Gson instance used for all load/save operations in this manager
     */
    public DataManager() {
        // indented JSON is nicer when you open the file in an editor or diff
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Writes the full player list to disk, replacing {@code data/Players.json}
     * Failures are logged to stderr, the in-memory list in {@link AccountManager} is unchanged
     *
     * @param players every account to persist (typically the whole registry)
     */
    public void savePlayers(List<PlayerAccount> players) {
        // FileWriter overwrites the file; try-with-resources closes it even if toJson throws
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(players, writer);
        } catch (IOException e) {
            System.err.println("Error saving to " + FILE_PATH);
        }
    }

    /**
     * Loads all players from {@code data/Players.json}. If the file is missing, empty, or invalid JSON,
     * returns an empty list (caller treats that as “no accounts yet”).
     *
     * @return a mutable list of accounts, never {@code null}
     */
    public List<PlayerAccount> loadPlayers() {
        File file = new File(FILE_PATH);
        // first run: no file yet
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(FILE_PATH)) {
            // Gson deserializes a JSON array into a Java array; we convert to ArrayList for AccountManager
            PlayerAccount[] playersArray = gson.fromJson(reader, PlayerAccount[].class);
            if (playersArray == null) {
                return new ArrayList<>();
            }
            return new ArrayList<>(Arrays.asList(playersArray));
        } catch (Exception e) {
            // bad UTF-8, wrong shape, truncated file — don’t crash the app
            System.err.println("Data corrupted or empty. Returning empty list.");
            return new ArrayList<>();
        }
    }

    /**
     * Reads the admin PIN from {@code data/admin.json} 
     * If the file is missing then it creates it w the default pin
     * If the file exists but has no usable PIN, returns that same default
     * Generated this w/ Claude, so not my code
     * @return the PIN string admins must enter (never {@code null} from this method)
     */
    public String loadAdminPin() {
        File file = new File(ADMIN_FILE_PATH);
        if (!file.exists()) {
            // bootstrap admin.json so verifyAdminPin always has something on disk
            saveAdminPin(DEFAULT_PIN);
            return DEFAULT_PIN;
        }
        try (Reader reader = new FileReader(ADMIN_FILE_PATH)) {
            AdminConfig config = gson.fromJson(reader, AdminConfig.class);
            if (config == null || config.pin == null || config.pin.isEmpty()) {
                return DEFAULT_PIN;
            }
            return config.pin;
        } catch (Exception e) {
            System.err.println("Error loading admin config. Using default PIN.");
            return DEFAULT_PIN;
        }
    }

    /**
     * Writes {@code {"pin":"..."}} to {@code data/admin.json}, creating the {@code data/} directory if needed.
     *
     * @param pin the PIN to store (caller should validate length/format if required)
     */
    public void saveAdminPin(String pin) {
        File file = new File(ADMIN_FILE_PATH);
        // ensure data/ exists before we open the writer
        file.getParentFile().mkdirs();
        AdminConfig config = new AdminConfig();
        config.pin = pin;
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(config, writer);
        } catch (IOException e) {
            System.err.println("Error saving admin config.");
        }
    }
}