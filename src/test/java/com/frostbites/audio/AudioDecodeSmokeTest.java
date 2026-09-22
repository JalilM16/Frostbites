package com.frostbites.audio;

import java.io.BufferedInputStream;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import org.junit.jupiter.api.Test;

/** Ensures MP3 SPI + classpath resources can build a Clip (no speaker required). */
class AudioDecodeSmokeTest {

    /**
     * Same decode shape as production audio — proves SPI + resources work in {@code mvn test}.
     *
     * <p>Note: this method was created using ai assistance.
     *
     * @throws Exception if open/decode fails
     */
    @Test
    void dingMp3OpensAsClip() throws Exception {
        InputStream raw = AudioManager.class.getResourceAsStream("/assets/sounds/ding.mp3");
        if (raw == null) {
            throw new AssertionError("Run mvn test from project root so resources are on classpath");
        }
        AudioInputStream audioIn = null;
        AudioInputStream pcmIn = null;
        try {
            audioIn = AudioSystem.getAudioInputStream(new BufferedInputStream(raw));
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
            clip.close();
        } finally {
            if (pcmIn != null) {
                pcmIn.close();
            } else if (audioIn != null) {
                audioIn.close();
            }
        }
    }
}
