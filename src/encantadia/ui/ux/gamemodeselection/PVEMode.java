package encantadia.ui.ux.gamemodeselection;

import javax.swing.*;

public class PVEMode extends JFrame {

    private JPanel PVEModepanel;

    public PVEMode() {
        setContentPane(PVEModepanel);
        setTitle("Encantadia: Echoes of the Gem - Arcade Mode");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}