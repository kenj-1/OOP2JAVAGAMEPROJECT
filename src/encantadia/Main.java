package encantadia;

import encantadia.ui.frames.WelcomeScreenPage;
import javax.swing.*;
import encantadia.audio.MusicManager;
import encantadia.audio.MusicType;

public class Main {
    public static void main(String[] x) {

        ScreenManager.init();

        MusicManager.stop();
        //START MUSIC IMMEDIATELY
        MusicManager.play(MusicType.MENU);

        SwingUtilities.invokeLater(() -> {
            new WelcomeScreenPage();
            System.out.println("[ScreenManager] F11 = toggle fullscreen");
        });
    }
}