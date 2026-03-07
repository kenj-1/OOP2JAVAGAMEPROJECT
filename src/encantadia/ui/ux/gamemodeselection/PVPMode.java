package encantadia.ui.ux.gamemodeselection;

import javax.swing.*;

public class PVPMode extends JFrame {

    private JPanel PVPModepanel;

    public PVPMode() {
        setContentPane(PVPModepanel);
        setTitle("Encantadia: Echoes of the Gem - Arcade Mode");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
