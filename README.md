# FrostBites

FrostBites is a desktop typing game built with Java and Swing, designed mainly for kids. Players complete different shop themed orders by typing words shown on screen before time runs out. The game includes **multiple levels**, **lives (hearts)**, **score goals**, **power-ups** (freeze time and auto-clean, limited uses per run), **player accounts** with saved statistics, **high scores**, **settings** (volume and mute), and an in-app **Manager’s Office** (PIN-protected) for administrators or parents/teachers to manage accounts and scores.

Originally built for **COMPSCI 2212 (Group 11)** at Western University.

## Team

- **Jalil Matthews** — [github.com/JalilM16](https://github.com/JalilM16)
- **Harry Yang**
- **Justin Fung**
- **Ethan Ung**

---

## Required libraries and third-party tools (with versions)

These are declared in `pom.xml` and resolved automatically by Maven from Maven Central (no manual JAR download needed for normal development).

| Component                 | Version     | Role                                                        |
|---------------------------|-------------|-------------------------------------------------------------| 
| **JDK**                   | **17**      | Compile and run                                             |
| **Apache Maven**          | **3.6+**    | Build, dependencies, tests, run via `exec` plugin           |
| **Google Gson**           | **2.10.1**  | Load/save JSON (`Players.json`, `admin.json`)               |
| **JUnit Jupiter**         | **5.10.0**  | Unit and integration tests (test scope only)                |
| **mp3spi** (Soundlibs)    | **1.9.5.4** | Lets `javax.sound.sampled` play **MP3** assets              |
| **Maven Surefire Plugin** | **3.0.0**   | Runs JUnit tests (`mvn test`)                               |
| **exec-maven-plugin**     | **3.1.0**   | Runs the app with correct classpath (`mvn exec:java`)       |

**Transitive libraries:** Gson and mp3spi may pull in additional small dependencies; Maven downloads them automatically.

**Standard library:** Swing, AWT, `javax.sound.sampled`, etc. are part of the JDK.

---

## How to obtain and install third-party tools

### 1. Java Development Kit (JDK) 17

1. Download a **JDK 17** build (e.g. [Eclipse Temurin](https://adoptium.net/) or [Oracle JDK](https://www.oracle.com/java/technologies/downloads/)).
2. Run the installer.
3. Confirm in a terminal: `java -version` and `javac -version` both report **17** (or compatible).
4. Optional: set `JAVA_HOME` to the JDK install folder if your IDE or Maven needs it.

### 2. Apache Maven

1. Download Maven from [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi) (binary zip).
2. Unzip to a folder (e.g. `C:\Program Files\Apache\maven`).
3. Add Maven’s `bin` directory to your system **PATH**.
4. Confirm: `mvn -version` prints Maven **3.x** and uses your JDK 17.

**You do not need to manually download Gson, JUnit, or mp3spi** — the first `mvn compile` (or `mvn test`) will fetch them into your local Maven cache (`~/.m2/repository`).


---

## Building from source (step-by-step)

All steps assume a terminal open in the **repository root** (the folder that contains `pom.xml`).

1. **Clone or extract** the project so you have the full tree (`src/`, `assets/`, `pom.xml`, etc.).
3. **Compile** (downloads dependencies if needed, compiles main sources):
   ```bash
   mvn compile
   ```
4. **Run tests** (recommended before submission):
   ```bash
   mvn test
   ```
5. **Build the project JAR** (main artifact only — runtime dependencies are **not** bundled inside this JAR):
   ```bash
   mvn package
   ```
   Output: `target/frostbites-1.0.jar`.

6. **Generate Javadoc**:
   ```bash
   mvn javadoc:javadoc
   ```
   HTML output: `target/reports/apidocs/` (open `index.html` **after** extracting or from that folder — not from inside a ZIP preview).

## Running the already built software
From the project root :

```bash
mvn compile
mvn exec:java
```

Or in one step to have an executable .jar file on your desktop:

```bash
mvn package -DskipTests
java -jar target/frostbites-1.0.jar
```

**Important:** Some menu images are loaded from the **filesystem** under `assets/images/` relative to the **current working directory**, not only from the JAR. Always run the app with the working directory set to the **project root** (so `assets/` is visible), or place a copy of the `assets` folder next to the JAR and run from that directory. Otherwise you may see “Missing: …png” placeholders. Packaged resources still supply audio and other assets on the classpath.



## Javadoc

```bash
mvn javadoc:javadoc
```

HTML output is written under **`target/reports/apidocs/`** (open `index.html` there).

## User guide

### Main menu

- **Log In / Continue:** Opens the login screen if you are not logged in; if already logged in, goes to the **Dashboard**.
- **Log Out:** Clears the current player session.
- **Tutorial:** In-game explanation of typing, orders, scoring, and power-ups.
- **High Scores:** View the score table.
- **Settings:** Adjust **master volume** and **mute**; preferences apply to gameplay audio.
- **Manager’s Office:** PIN-protected area for account and score management (see **Parental / admin access** below).
- **Close Game:** Exits the application.

### Logging in and starting a level

- From the main menu, choose **Log In** and enter a valid **username** and **password** (see **Accounts** above for the demo account).
- After a successful login, the main button becomes **Continue** and opens the **Dashboard**.
- On the **Dashboard**, pick a **level** to start a typing session (**gameplay** screen).

### Gameplay (typing session)

- Type the **current word** to match the prompt; correct letters advance; wrong input can cost **patience** and eventually **hearts** depending on rules for the session.
- Reach the **target score** before time runs out or you run out of **hearts** to **complete the level**.
- **Pause** if available in the UI during a run.
- **Power-ups (per level run):** You get the use of **Freeze time** and of **Auto-clean** to do each respective powerup.

### After a session

- Results show score and related stats. If logged in, results can be **saved** to the player profile.

---

## Accounts, passwords, and PIN 

### Player login

- Player accounts are stored in **`data/Players.json`** in the **working directory** when the app runs.
- The login screen shows a **demo hint**: username **`test`**, password **`123`**.  
  **Note:** That account exists only if it is present in `Players.json`. If you ship a **fresh** install with **no** `Players.json`, create an account first using the **Manager’s Office** (below) or copy a known `Players.json` for testing.
- **Passwords are stored in plain text** in JSON for this project.

### Manager’s Office (admin) PIN

- **Default PIN:** **`1234`**
- Stored in **`data/admin.json`** as `"pin": "1234"`. If `admin.json` is missing on first use, the game creates it with this default (see `DataManager` / `loadAdminPin`).
- To **change the PIN**, edit `data/admin.json` while the game is **closed**, or use whatever workflow your team documents; the file must remain valid JSON with a `pin` string field.


---

## Parental controls / admin (Manager’s Office)

**There is no separate program to install.** Parental/admin features are **inside FrostBites** on the **Manager’s Office** screen.

### How to access

1. Launch FrostBites.
2. From the **main menu**, click **Manager’s Office**.
3. When prompted, enter the **Manager PIN** (default **`1234`**).
4. If the PIN is wrong, access is denied and you return to the menu flow.

### What administrators can do

- **Create player accounts** (username + password).
- **Reset a player’s password** (after selecting the player).
- **Reset a player’s statistics** (and their high score for that account, per in-game confirmation text).
- **Reset the global high score table** (all players’ high scores set to zero; confirm in dialog).

**Build/install:** Not applicable — same app as the main game; no extra build step.

---

## Data files

On first run the app creates JSON under `data/` in the working directory (for example `Players.json`, `admin.json`). 

## Project layout

|                Path             |                Purpose                                      |
|---------------------------------|-------------------------------------------------------------|
| `src/main/java/com/frostbites/` | Application entry, UI, gameplay session wiring              |
| `src/main/java/backend/`        | Words, accounts, stats, power-up model, persistence helpers |
| `assets/`                       | Images, sounds, and level art used at runtime               |
| `src/test/java/`                | Automated tests                                             |
| `pom.xml`                       | Maven build and dependencies (Gson, JUnit 5, MP3 SPI)       |