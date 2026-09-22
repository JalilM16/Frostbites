package backend;

import com.frostbites.game.Level;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AccountManagerTest {

    private AccountManager accountManager;

    // Unique prefix so these test accounts don't collide with real player data
    private static final String TEST_USER = "tc11_testplayer";
    private static final String TEST_PASS = "tc11pass";

    @BeforeEach
    void setUp() {
        accountManager = new AccountManager();
        // Remove any leftover account from a previous test run so each test starts clean
        PlayerAccount existing = accountManager.findAccount(TEST_USER);
        if (existing != null) {
            accountManager.resetStats(TEST_USER);
            // resetStats keeps the account; we use a fresh AccountManager per test
            // and rely on the unique username to track state
        }
    }

    // ---------------------------------------------------------------------------
    // Test Case 11 – Admin Can Create Player Account (FR-3.1.5)
    // ---------------------------------------------------------------------------

    /**
     * Step 1: Admin enters the correct PIN.
     * verifyAdminPin must return true for the default PIN 1234
     */
    @Test
    void testAdminPinIsVerifiedCorrectly() {
        assertTrue(accountManager.verifyAdminPin("1234"),
                "Default admin PIN '1234' should be accepted");
    }

    /**
     * Step 2: Wrong PIN is rejected
     */
    @Test
    void testWrongAdminPinIsRejected() {
        assertFalse(accountManager.verifyAdminPin("0000"),
                "Incorrect PIN should not be accepted");
    }

    /**
     * Steps 3-5: Admin creates a new player account.
     * registerPlayer must return true and the account must be found afterwards.
     */
    @Test
    void testAdminCanCreatePlayerAccount() {
        // Remove the test account if it already exists from a previous run
        boolean alreadyExists = accountManager.usernameExists(TEST_USER);

        boolean created = accountManager.registerPlayer(TEST_USER, TEST_PASS);

        if (alreadyExists) {
            // If it existed before, registerPlayer should have returned false
            // (duplicate usernames are not allowed)
            assertFalse(created, "Duplicate username should not be created");
        } else {
            assertTrue(created, "registerPlayer should return true for a new username");
            assertNotNull(accountManager.findAccount(TEST_USER),
                    "Newly created account should be findable");
        }
    }

    /**
     * Steps 6-7: After creation, the player can log in with the correct credentials.
     * Simulates what LoginScreen should do: find the account and verify the password.
     *
     * <p>Also maps to integration doc TC4 / TC5 (login) at the service layer via
     * {@link AccountManager#verifyCredentials}.</p>
     */
    @Test
    void testNewPlayerCanLogInWithCreatedCredentials() {
        // Ensure the account exists (create it if not already there)
        if (!accountManager.usernameExists(TEST_USER)) {
            accountManager.registerPlayer(TEST_USER, TEST_PASS);
        }

        PlayerAccount account = accountManager.findAccount(TEST_USER);
        assertNotNull(account, "Account must exist before attempting login");
        assertEquals(TEST_PASS, account.getPassword(),
                "Password stored on the account must match what was used at creation");
        assertTrue(accountManager.verifyCredentials(TEST_USER, TEST_PASS),
                "verifyCredentials should accept matching username and password");
    }

    @Test
    void verifyCredentials_rejectsWrongPassword() {
        if (!accountManager.usernameExists(TEST_USER)) {
            accountManager.registerPlayer(TEST_USER, TEST_PASS);
        }
        assertFalse(accountManager.verifyCredentials(TEST_USER, "not_the_password"));
    }

    @Test
    void verifyCredentials_rejectsUnknownUser() {
        assertFalse(accountManager.verifyCredentials("definitely_no_such_user_tc11_xyz", TEST_PASS));
    }

    @Test
    void verifyCredentials_trimsPassword() {
        if (!accountManager.usernameExists(TEST_USER)) {
            accountManager.registerPlayer(TEST_USER, TEST_PASS);
        }
        assertTrue(accountManager.verifyCredentials(TEST_USER, "  " + TEST_PASS + "  "));
    }

    // ---------------------------------------------------------------------------
    // Validation doc TC12 — stats persisted via recordTypingResult (FR-3.1.12)
    // Service-layer check; full JSON/dashboard path remains manual / E2E.
    // ---------------------------------------------------------------------------

    @Test
    void recordTypingResult_updatesStatsAndHighScore() {
        if (!accountManager.usernameExists(TEST_USER)) {
            accountManager.registerPlayer(TEST_USER, TEST_PASS);
        }
        accountManager.resetStats(TEST_USER);

        accountManager.recordTypingResult(
                TEST_USER,
                175,
                42.0,
                96.0,
                3,
                90,
                200,
                190,
                Level.ICE_CREAM_SHOP,
                false
        );

        PlayerAccount account = accountManager.findAccount(TEST_USER);
        assertNotNull(account);
        assertEquals(175, account.getHighScore());
        assertEquals(1, account.getStats().getGamesPlayed());
        assertEquals(3, account.getStats().getTotalErrorCount());
    }

    @Test
    void recordTypingResult_updatesHighestLevelWhenCompletedRunIsHarder() {
        if (!accountManager.usernameExists(TEST_USER)) {
            accountManager.registerPlayer(TEST_USER, TEST_PASS);
        }
        accountManager.resetStats(TEST_USER);
        PlayerAccount account = accountManager.findAccount(TEST_USER);
        account.setHighestLevelReached(Level.ICE_CREAM_SHOP);

        accountManager.recordTypingResult(
                TEST_USER,
                50,
                30.0,
                90.0,
                1,
                60,
                50,
                48,
                Level.BUSY_MALL,
                true
        );

        account = accountManager.findAccount(TEST_USER);
        assertEquals(Level.BUSY_MALL, account.getHighestLevelReached());
    }

    /**
     * Negative case: creating an account with an empty username is rejected.
     */
    @Test
    void testCreateAccountWithBlankUsernameIsRejected() {
        assertFalse(accountManager.registerPlayer("", TEST_PASS),
                "Blank username should be rejected");
    }

    /**
     * Negative case: creating an account with an empty password is rejected.
     */
    @Test
    void testCreateAccountWithBlankPasswordIsRejected() {
        assertFalse(accountManager.registerPlayer("someuser", ""),
                "Blank password should be rejected");
    }

    /**
     * Negative case: duplicate usernames are rejected.
     */
    @Test
    void testDuplicateUsernameIsRejected() {
        if (!accountManager.usernameExists(TEST_USER)) {
            accountManager.registerPlayer(TEST_USER, TEST_PASS);
        }
        boolean secondAttempt = accountManager.registerPlayer(TEST_USER, "differentpass");
        assertFalse(secondAttempt,
                "Registering the same username a second time should return false");
    }
}
