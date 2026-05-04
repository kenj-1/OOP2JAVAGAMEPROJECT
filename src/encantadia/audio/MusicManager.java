package encantadia.audio;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.net.URL;

public class MusicManager {

    private static volatile boolean isPlaying = false;
    private static Thread musicThread;
    private static MusicType currentType = null;

    // Default volume (1.0 = 100%, 0.0 = 0%)
    private static float volume = 1.0f;
    private static FloatControl currentMusicControl = null;

    public static void play(MusicType type) {
        if (type == currentType && isPlaying) return;

        stop();

        currentType = type;
        isPlaying = true;

        musicThread = new Thread(() -> streamMusic(type));
        musicThread.setDaemon(true);
        musicThread.start();
    }

    private static void streamMusic(MusicType type) {
        String path = switch (type) {
            case MENU -> "/resources/main.wav";
            case BATTLE -> "/resources/battle.wav";
        };

        while (isPlaying) {
            SourceDataLine localDataLine = null;
            AudioInputStream playbackStream = null;
            AudioInputStream rawStream = null;

            try {
                URL url = MusicManager.class.getResource(path);
                if (url == null) {
                    System.out.println("❌ Music not found: " + path);
                    isPlaying = false;
                    return;
                }

                rawStream = AudioSystem.getAudioInputStream(new BufferedInputStream(url.openStream()));
                AudioFormat baseFormat = rawStream.getFormat();

                AudioFormat targetFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16,
                        baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false
                );

                playbackStream = AudioSystem.getAudioInputStream(targetFormat, rawStream);

                DataLine.Info info = new DataLine.Info(SourceDataLine.class, targetFormat);
                localDataLine = (SourceDataLine) AudioSystem.getLine(info);
                localDataLine.open(targetFormat);

                // Hook the volume control to the newly opened line
                if (localDataLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    currentMusicControl = (FloatControl) localDataLine.getControl(FloatControl.Type.MASTER_GAIN);
                    setVolume(volume); // Apply current global volume setting
                }

                localDataLine.start();

                byte[] buffer = new byte[4096];
                int bytesRead;

                while (isPlaying && (bytesRead = playbackStream.read(buffer, 0, buffer.length)) != -1) {
                    localDataLine.write(buffer, 0, bytesRead);
                }

            } catch (Exception e) {
                System.out.println("❌ Music Streaming Error for: " + path);
                e.printStackTrace();
                isPlaying = false;
            } finally {
                currentMusicControl = null;
                if (localDataLine != null) {
                    if (isPlaying) localDataLine.drain(); else localDataLine.flush();
                    localDataLine.stop();
                    localDataLine.close();
                }
                try {
                    if (playbackStream != null) playbackStream.close();
                    if (rawStream != null) rawStream.close();
                } catch (Exception ignored) {}
            }
        }
    }

    public static void playWithDelay(MusicType type, int delayMs) {
        stop();
        new javax.swing.Timer(delayMs, e -> {
            ((javax.swing.Timer)e.getSource()).stop();
            play(type);
        }).start();
    }

    public static void stop() {
        isPlaying = false;
        if (musicThread != null) {
            musicThread.interrupt();
            musicThread = null;
        }
        currentType = null;
    }

    // ── Volume Control Methods ────────────────────────────────
    public static void setVolume(float v) {
        volume = Math.max(0f, Math.min(1f, v));
        if (currentMusicControl != null) {
            float dB = (volume <= 0.0001f) ? -80f : 20f * (float) Math.log10(volume);
            currentMusicControl.setValue(dB);
        }
    }

    public static float getVolume() { return volume; }
}