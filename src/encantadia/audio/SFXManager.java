package encantadia.audio;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

public class SFXManager {

    private static final ConcurrentHashMap<String, Clip> clipCache = new ConcurrentHashMap<>();
    private static Clip currentSkillClip = null;
    private static Clip victoryClip = null;

    // Default SFX volume (1.0 = 100%)
    private static float volume = 1.0f;

    private static AudioInputStream convertToStandardFormat(AudioInputStream sourceStream) {
        AudioFormat baseFormat = sourceStream.getFormat();
        if (baseFormat.getSampleSizeInBits() <= 16 && baseFormat.getEncoding() == AudioFormat.Encoding.PCM_SIGNED) {
            return sourceStream;
        }
        AudioFormat targetFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16,
                baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false
        );
        return AudioSystem.getAudioInputStream(targetFormat, sourceStream);
    }

    public static void preload(String path) {
        if (path == null || clipCache.containsKey(path)) return;
        new Thread(() -> {
            try {
                URL url = SFXManager.class.getResource(path);
                if (url == null) return;
                AudioInputStream rawStream = AudioSystem.getAudioInputStream(new BufferedInputStream(url.openStream()));
                AudioInputStream standardStream = convertToStandardFormat(rawStream);
                Clip clip = AudioSystem.getClip();
                clip.open(standardStream);
                clipCache.put(path, clip);
            } catch (Exception e) {
                System.out.println("❌ Failed to preload SFX: " + path);
            }
        }).start();
    }

    public static void playSkillSFX(String path) {
        if (path == null) return;
        new Thread(() -> {
            try {
                if (!clipCache.containsKey(path)) preload(path);
                int attempts = 0;
                while (!clipCache.containsKey(path) && attempts < 10) { Thread.sleep(10); attempts++; }

                Clip clip = clipCache.get(path);
                if (clip != null) {
                    stopSkillSFX();
                    currentSkillClip = clip;
                    applyVolume(clip); // Apply custom volume before playing
                    clip.setFramePosition(0);
                    clip.start();
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    public static void stopSkillSFX() {
        if (currentSkillClip != null && currentSkillClip.isRunning()) {
            currentSkillClip.stop();
        }
        currentSkillClip = null;
    }

    public static void playVictoryFanfare() {
        new Thread(() -> {
            try {
                URL url = SFXManager.class.getResource("/resources/winnerSfxwav.wav");
                if (url == null) return;

                stopVictoryFanfare();

                AudioInputStream rawStream = AudioSystem.getAudioInputStream(new BufferedInputStream(url.openStream()));
                AudioInputStream standardStream = convertToStandardFormat(rawStream);
                victoryClip = AudioSystem.getClip();
                victoryClip.open(standardStream);
                applyVolume(victoryClip); // Apply custom volume before playing
                victoryClip.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void stopVictoryFanfare() {
        if (victoryClip != null && victoryClip.isRunning()) {
            victoryClip.stop();
            victoryClip.close();
        }
        victoryClip = null;
    }

    public static void stopAll() {
        stopSkillSFX();
        stopVictoryFanfare();
    }

    // ── Volume Control Methods ────────────────────────────────
    public static void setVolume(float v) {
        volume = Math.max(0f, Math.min(1f, v));
        // Update clips if they are currently playing
        applyVolume(currentSkillClip);
        applyVolume(victoryClip);
    }

    public static float getVolume() { return volume; }

    private static void applyVolume(Clip clip) {
        if (clip != null && clip.isOpen() && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (volume <= 0.0001f) ? -80f : 20f * (float) Math.log10(volume);
            gainControl.setValue(dB);
        }
    }
}