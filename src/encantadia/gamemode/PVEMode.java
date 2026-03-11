package encantadia.gamemode;

import javax.swing.*;

public class PVEMode extends JFrame {

    private JPanel panel;


    public PVEMode() {


        setContentPane(panel);
        setTitle("Encantadia: Echoes of the Gem - PVE Mode");
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
}