package managers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/**
 * Plays WAV sound effects and music.
 *
 * CheerpJ notes: this runs in the browser via CheerpJ, where the Java Sound
 * backend can be missing or restricted. Every javax.sound.sampled call is
 * therefore wrapped so that a failure degrades to silence instead of crashing
 * the game. Two browser realities are handled:
 *   - Autoplay policy: audio cannot start until the user interacts with the
 *     page, so early start()/loop() calls may throw — we swallow those and keep
 *     the clip cached so a later (post-gesture) call succeeds.
 *   - No audio backend at all: the first unrecoverable failure latches
 *     audioAvailable = false so we stop trying and stop logging.
 * Only WAV (PCM) is used, which is the format CheerpJ's Java Sound supports.
 */
public class SoundManager {

    private static final Map<String, Clip> cache = new HashMap<>();
    private static Clip currentMusic;
    private static String currentMusicPath;
    private static float masterVolume = 1.0f; // 0.0 to 1.0
    private static boolean audioAvailable = true; // latched off if audio can't work at all

    // Play a sound once
    public static void play(String path) {
        if (!audioAvailable) return;
        Clip clip = load(path);
        if (clip == null) return;
        try {
            clip.stop();
            clip.setFramePosition(0);
            setVolume(clip, masterVolume);
            clip.start();
        } catch (Exception e) {
            reportPlaybackError(path, e);
        }
    }

    // Play music on loop (stops any currently looping music first)
    public static void playMusic(String path) {
        if (!audioAvailable) return;
        stopMusic();
        Clip clip = load(path);
        if (clip == null) return;
        currentMusic = clip;
        currentMusicPath = path;
        try {
            clip.setFramePosition(0);
            setVolume(clip, masterVolume);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            // Likely the browser autoplay policy: keep the clip referenced so a
            // later resumeMusic() (after a user gesture) can start it.
            reportPlaybackError(path, e);
        }
    }

    // Stop the current looping music
    public static void stopMusic() {
        if (currentMusic != null) {
            try {
                if (currentMusic.isRunning()) currentMusic.stop();
            } catch (Exception e) {
                reportPlaybackError(currentMusicPath, e);
            }
        }
        currentMusic = null;
        currentMusicPath = null;
    }

    // Pause the current music
    public static void pauseMusic() {
        if (currentMusic == null) return;
        try {
            if (currentMusic.isRunning()) currentMusic.stop();
        } catch (Exception e) {
            reportPlaybackError(currentMusicPath, e);
        }
    }

    // Resume paused music
    public static void resumeMusic() {
        if (!audioAvailable || currentMusic == null) return;
        try {
            if (!currentMusic.isRunning()) {
                setVolume(currentMusic, masterVolume);
                currentMusic.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (Exception e) {
            reportPlaybackError(currentMusicPath, e);
        }
    }

    // Loop a sound a specific number of times (-1 = infinite, same as LOOP_CONTINUOUSLY)
    public static void loop(String path, int times) {
        if (!audioAvailable) return;
        Clip clip = load(path);
        if (clip == null) return;
        try {
            clip.stop();
            clip.setFramePosition(0);
            setVolume(clip, masterVolume);
            clip.loop(times == -1 ? Clip.LOOP_CONTINUOUSLY : times - 1);
        } catch (Exception e) {
            reportPlaybackError(path, e);
        }
    }

    // Stop a specific sound
    public static void stop(String path) {
        Clip clip = cache.get(path);
        if (clip == null) return;
        try {
            if (clip.isRunning()) clip.stop();
        } catch (Exception e) {
            reportPlaybackError(path, e);
        }
    }

    // Set master volume (0.0 = silent, 1.0 = full)
    public static void setMasterVolume(float volume) {
        masterVolume = Math.max(0f, Math.min(1f, volume));
        for (Clip clip : cache.values()) {
            setVolume(clip, masterVolume);
        }
    }

    public static float getMasterVolume() {
        return masterVolume;
    }

    // Pre-load a sound into cache without playing it
    public static void preload(String path) {
        load(path);
    }

    // Release all clips and clear cache
    public static void dispose() {
        stopMusic();
        for (Clip clip : cache.values()) {
            try {
                clip.stop();
                clip.close();
            } catch (Exception e) {
                // Nothing useful to do while tearing down; ignore.
            }
        }
        cache.clear();
    }

    private static Clip load(String path) {
        if (cache.containsKey(path)) return cache.get(path);
        if (!audioAvailable) return null;
        try {
            AudioInputStream stream = openAudioStream(path);
            if (stream == null) {
                System.err.println("SoundManager: sound not found — " + path);
                return null;
            }
            Clip clip = AudioSystem.getClip();
            clip.open(stream);
            cache.put(path, clip);
            return clip;
        } catch (Exception e) {
            // getClip()/open() failing usually means no usable audio backend
            // (common under CheerpJ). Latch audio off so we stop retrying.
            System.err.println("SoundManager: audio unavailable, disabling sound — "
                    + path + " — " + e.getMessage());
            audioAvailable = false;
            return null;
        }
    }

    private static AudioInputStream openAudioStream(String path) throws Exception {
        // CheerpJ: sounds are bundled in the JAR, so read them from the classpath.
        // AudioSystem needs a mark/reset-capable stream, hence the BufferedInputStream.
        String resource = path.startsWith("/") ? path : "/" + path;
        InputStream in = SoundManager.class.getResourceAsStream(resource);
        if (in != null) {
            return AudioSystem.getAudioInputStream(new BufferedInputStream(in));
        }
        // Desktop fallback: read straight from the working directory.
        File file = new File(path);
        if (file.exists()) {
            return AudioSystem.getAudioInputStream(file);
        }
        return null;
    }

    private static void setVolume(Clip clip, float volume) {
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                // Convert linear 0.0–1.0 to decibels
                float dB = volume > 0 ? (float)(20.0 * Math.log10(volume)) : gain.getMinimum();
                gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB)));
            }
        } catch (Exception e) {
            // Volume control is optional (CheerpJ may not expose MASTER_GAIN);
            // play at default level rather than failing.
        }
    }

    // A playback call failed (often the browser autoplay policy under CheerpJ).
    // Log it but keep audio enabled — a later, user-initiated call may succeed.
    private static void reportPlaybackError(String path, Exception e) {
        System.err.println("SoundManager: playback failed — " + path + " — " + e.getMessage());
    }

    private SoundManager() {}
}
