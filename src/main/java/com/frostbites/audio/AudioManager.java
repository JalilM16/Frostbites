package com.frostbites.audio;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

/**
 * Plays MP3s from {@code assets/sounds/}: bundled on the classpath after {@code mvn compile}, or loaded
 * from disk if your IDE working directory is not the project root.
 *
 * <p>The {@code SFX_*} and {@code BGM_*} string constants are meant to be passed into {@link #playSFX(String)}
 * so the rest of the app does not hard-code filenames. {@link #setVolume(float)} / {@link #setMuted(boolean)}
 * tie into the settings screen.
 *
 * <p><b>Disclosure:</b> a lot of the decode / {@link javax.sound.sampled.Clip} plumbing below was built with
 * AI assistance; the routing, volume flags, and file lookup are the parts we wired ourselves.
 */
public final class AudioManager {

    /** {@link #playSFX(String)} — word finished correctly */
    public static final String SFX_WORD_COMPLETE = "WORD_COMPLETE";
    /** {@link #playSFX(String)} — wrong key */
    public static final String SFX_TYPING_ERROR = "TYPING_ERROR";
    /** {@link #playSFX(String)} — order done */
    public static final String SFX_ORDER_SERVED = "ORDER_SERVED";
    /** {@link #playSFX(String)} — button click */
    public static final String SFX_UI_CLICK = "UI_CLICK";

    /** {@link #playSFX(String)} — easy level BGM */
    public static final String BGM_EASY = "BGM_EASY";
    /** {@link #playSFX(String)} — medium level BGM */
    public static final String BGM_MEDIUM = "BGM_MEDIUM";
    /** {@link #playSFX(String)} — hard level BGM */
    public static final String BGM_HARD = "BGM_HARD";
    /** {@link #playSFX(String)} — stop background music */
    public static final String BGM_STOP = "BGM_STOP";
    /** {@link #playSFX(String)} — pause BGM (game pause menu) */
    public static final String BGM_PAUSE = "BGM_PAUSE";
    /** {@link #playSFX(String)} — resume BGM after pause */
    public static final String BGM_RESUME = "BGM_RESUME";

    /** Classpath prefix inside the JAR / {@code target/classes} (leading slash for {@link Class#getResourceAsStream}). */
    private static final String PREFIX = "/assets/sounds/";

    private float masterVolume = 0.85f;
    private boolean muted;

    private Clip musicClip;
    private boolean musicWasPlaying;

    private static boolean printedMissingHint;
    private static boolean printedDecodeHint;

    /**
     * Current master volume from the last {@link #setVolume(float)} call (default {@code 0.85}).
     *
     * @return level in {@code [0, 1]}
     */
    public float getMasterVolume() {
        return masterVolume;
    }

    /**
     * Stores the settings-slider value and reapplies it to the looping BGM clip if music is playing.
     *
     * @param value requested level; clamped to {@code [0, 1]}
     */
    public void setVolume(float value) {
        masterVolume = clamp(value, 0f, 1f);
        applyVolume(musicClip);
    }

    /**
     * @return whether all audio is silenced (BGM is stopped while muted)
     */
    public boolean isMuted() {
        return muted;
    }

    /**
     * Turns mute on or off. When muting, background music is stopped immediately.
     *
     * @param muted {@code true} to silence playback
     */
    public void setMuted(boolean muted) {
        this.muted = muted;
        if (muted) {
            stopMusic();
        }
    }

    /**
     * Main entry from the UI/game code: maps a symbolic tag to a sound file or a music command.
     * Unknown values are ignored; {@code null} is ignored.
     *
     * <p>Recognized tags: {@link #SFX_WORD_COMPLETE}, {@link #SFX_TYPING_ERROR}, {@link #SFX_ORDER_SERVED},
     * {@link #SFX_UI_CLICK}, {@link #BGM_EASY}, {@link #BGM_MEDIUM}, {@link #BGM_HARD}, {@link #BGM_STOP},
     * {@link #BGM_PAUSE}, {@link #BGM_RESUME}.
     *
     * @param type one of the {@code SFX_*} / {@code BGM_*} constants, or {@code null}
     */
    public void playSFX(String type) {
        if (type == null) {
            return;
        }
        if (type.equals(SFX_WORD_COMPLETE)) {
            playOnce("ding.mp3");
        } else if (type.equals(SFX_TYPING_ERROR)) {
            playOnce("splat.mp3");
        } else if (type.equals(SFX_ORDER_SERVED)) {
            playOnce("kaching.mp3");
        } else if (type.equals(SFX_UI_CLICK)) {
            playOnce("click.mp3");
        } else if (type.equals(BGM_EASY)) {
            startMusic("slow-track.mp3");
        } else if (type.equals(BGM_MEDIUM)) {
            startMusic("medium-track.mp3");
        } else if (type.equals(BGM_HARD)) {
            startMusic("fast-track.mp3");
        } else if (type.equals(BGM_STOP)) {
            stopMusic();
        } else if (type.equals(BGM_PAUSE)) {
            pauseMusic();
        } else if (type.equals(BGM_RESUME)) {
            resumeMusic();
        }
    }

    // --- short sounds ---

    /**
     * Plays a short MP3 once on a background thread so decoding/opening the clip does not block the Swing EDT.
     *
     * <p>Note: this method was created using ai assistance.
     *
     * @param fileName basename under {@code assets/sounds/} (e.g. {@code "ding.mp3"})
     */
    private void playOnce(String fileName) {
        if (muted || masterVolume <= 0.0001f) {
            return;
        }
        Thread t = new Thread(new Runnable() {
            /** decodes/plays one sfx off the EDT */
            @Override
            public void run() {
                try {
                    tryPlayClip(fileName, false);
                } catch (Exception ex) {
                    printHelpOnce(ex);
                }
            }
        }, "audio-once");
        t.start();
    }

    // --- background music (one file, loops) ---

    /**
     * Stops any current BGM, then opens and loops {@code fileName}. Synchronized so we do not overlap
     * with {@link #stopMusic()} / another {@code startMusic} on a different thread.
     *
     * <p>Note: this method was created using ai assistance.
     *
     * @param fileName basename under {@code assets/sounds/}
     */
    private synchronized void startMusic(String fileName) {
        if (muted) {
            return;
        }
        stopMusic();
        try {
            musicClip = tryPlayClip(fileName, true);
            musicWasPlaying = musicClip != null;
        } catch (Exception ex) {
            printHelpOnce(ex);
            musicClip = null;
            musicWasPlaying = false;
        }
    }

    /**
     * Stops background music, closes the line, and clears {@link #musicClip}.
     */
    private synchronized void stopMusic() {
        musicWasPlaying = false;
        if (musicClip != null) {
            try {
                musicClip.stop();
                musicClip.close();
            } catch (Exception ignored) {
            }
            musicClip = null;
        }
    }

    /**
     * Pauses BGM by stopping the clip but leaves {@link #musicWasPlaying} set so {@link #resumeMusic()} can restart.
     */
    private synchronized void pauseMusic() {
        if (musicClip != null && musicClip.isRunning()) {
            musicClip.stop();
            musicWasPlaying = true;
        }
    }

    /**
     * Resumes BGM after pause: reapplies volume, seeks to the start, and loops. No-op if not paused or muted.
     */
    private synchronized void resumeMusic() {
        if (muted || musicClip == null || !musicWasPlaying) {
            return;
        }
        applyVolume(musicClip);
        musicClip.setMicrosecondPosition(0);
        musicClip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    /**
     * The gnarly part: mp3 -&gt; decode -&gt; shove into a PCM format Clip actually accepts, then play.
     * One-shots get a LineListener so when they hit STOP we close the line (leaks otherwise).
     *
     * <p>Note: this method was created using ai assistance.
     */
    private Clip tryPlayClip(String fileName, boolean loop) throws Exception {
        InputStream raw = openSound(fileName);
        if (raw == null) {
            return null;
        }
        AudioInputStream audioIn = null;
        AudioInputStream pcmIn = null;
        try {
            BufferedInputStream buffered = new BufferedInputStream(raw);
            audioIn = AudioSystem.getAudioInputStream(buffered);
            // mp3spi gives you a stream that isn't always in the shape Clip wants — force signed 16-bit PCM
            AudioFormat base = audioIn.getFormat();
            AudioFormat pcmFmt = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    base.getSampleRate(),
                    16,
                    base.getChannels(),
                    base.getChannels() * 2,
                    base.getSampleRate(),
                    false);
            pcmIn = AudioSystem.getAudioInputStream(pcmFmt, audioIn);
            Clip clip = AudioSystem.getClip();
            clip.open(pcmIn);
            applyVolume(clip);
            if (!loop) {
                // fire and forget sfx — close the line when it stops or it leaks handles
                clip.addLineListener(new LineListener() {
                    /** closes the line after a one-shot sound so we dont leak handles */
                    @Override
                    public void update(LineEvent e) {
                        if (e.getType() == LineEvent.Type.STOP) {
                            e.getLine().close();
                        }
                    }
                });
                clip.start();
            } else {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
            return clip;
        } finally {
            if (pcmIn != null) {
                try {
                    pcmIn.close();
                } catch (IOException ignored) {
                }
            } else if (audioIn != null) {
                try {
                    audioIn.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * Opens a sound file: tries the classpath resource first (Maven copies assets into {@code target/classes}),
     * then {@link #findOnDisk(String)}. Prints a one-time hint if nothing is found.
     *
     * <p>Note: this method was created using ai assistance.
     *
     * @param fileName basename (e.g. {@code "click.mp3"})
     * @return open stream, or {@code null} if the file cannot be located
     * @throws IOException if the on-disk file exists but cannot be opened
     */
    private static InputStream openSound(String fileName) throws IOException {
        InputStream fromJar = AudioManager.class.getResourceAsStream(PREFIX + fileName);
        if (fromJar != null) {
            return fromJar;
        }
        File disk = findOnDisk(fileName);
        if (disk != null) {
            return new FileInputStream(disk);
        }
        printFileHelp(fileName);
        return null;
    }

    /**
     * Looks for {@code assets/sounds/} plus the basename under the JVM working directory, then walks up to
     * eight parent directories so running from a subfolder in the IDE still works.
     *
     * <p>Note: this method was created using ai assistance.
     *
     * @param fileName basename to append under {@code assets/sounds/}
     * @return the file if it exists, or {@code null}
     */
    private static File findOnDisk(String fileName) {
        Path inFolder = Paths.get("assets", "sounds", fileName);
        if (Files.isRegularFile(inFolder)) {
            return inFolder.toFile();
        }
        Path walk = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        for (int i = 0; i < 8; i++) {
            Path p = walk.resolve("assets").resolve("sounds").resolve(fileName);
            if (Files.isRegularFile(p)) {
                return p.toFile();
            }
            if (walk.getParent() == null) {
                break;
            }
            walk = walk.getParent();
        }
        return null;
    }

    /**
     * Prints a short troubleshooting message to stderr the first time a sound file is missing
     * (avoids flooding the console if something calls audio in a tight loop).
     *
     * @param fileName basename that was not found
     */
    private static void printFileHelp(String fileName) {
        if (printedMissingHint) {
            return;
        }
        printedMissingHint = true;
        System.err.println("[Audio] Missing: " + PREFIX + fileName);
        System.err.println("[Audio] Run: mvn compile   (copies assets into target/classes)");
        System.err.println("[Audio] Or set Run \"Working Directory\" to the project root.");
    }

    /**
     * Prints decode/open failure help once (typical causes: missing mp3spi on classpath or bad run config).
     *
     * @param ex the error from opening or decoding the clip
     */
    private static void printHelpOnce(Exception ex) {
        if (printedDecodeHint) {
            return;
        }
        printedDecodeHint = true;
        System.err.println("[Audio] Problem: " + ex.getClass().getSimpleName() + " — " + ex.getMessage());
        System.err.println("[Audio] Use Maven run (mp3spi must be on classpath). Example: mvn -q exec:java -Dexec.mainClass=com.frostbites.FrostBitesApp");
    }

    /**
     * Our slider is 0..1 but MASTER_GAIN wants decibels — stackoverflow / Cursor said use 20*log10.
     * Falls back to VOLUME control on weird drivers.
     *
     * <p>Note: this method was created using ai assistance.
     */
    private void applyVolume(Clip clip) {
        if (clip == null) {
            return;
        }
        float level = muted ? 0f : masterVolume;

        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl g = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            if (level <= 0.0001f) {
                g.setValue(g.getMinimum());
            } else {
                float db = (float) (20.0 * Math.log10(level));
                g.setValue(clamp(db, g.getMinimum(), g.getMaximum()));
            }
        } else if (clip.isControlSupported(FloatControl.Type.VOLUME)) {
            FloatControl v = (FloatControl) clip.getControl(FloatControl.Type.VOLUME);
            v.setValue(clamp(level, v.getMinimum(), v.getMaximum()));
        }
    }

    /**
     * @param x   value to constrain
     * @param lo  minimum result
     * @param hi  maximum result
     * @return {@code lo}, {@code hi}, or {@code x} if it already lies between them
     */
    private static float clamp(float x, float lo, float hi) {
        if (x < lo) {
            return lo;
        }
        if (x > hi) {
            return hi;
        }
        return x;
    }
}
