package encantadia.ui.ux.gamemodeselection;

import javax.swing.*;

public class ArcadeMode extends JFrame {
    private JPanel panel;
    private JLabel label;

    public ArcadeMode() {

        panel = new JPanel();
        label = new JLabel("PVE MODE");

        panel.add(label);

        setContentPane(panel);
        setTitle("Encantadia: Echoes of the Gem - Arcade Mode");
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
}
