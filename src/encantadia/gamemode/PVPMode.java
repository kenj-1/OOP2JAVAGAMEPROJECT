package encantadia.gamemode;

import encantadia.ui.frames.CharacterSelectionFrame;
import encantadia.ui.frames.MainMenuFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PVPMode extends JFrame {

    private JPanel pvpModePanelReal;
    private JButton yesButton;
    private JButton noButton;
    private JButton backToMainMenuButton;


    public PVPMode() {
        setContentPane(pvpModePanelReal);
        setTitle("Encantadia: Echoes of the Gem - PVP Mode");
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        noButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    new CharacterSelectionFrame();
                    dispose();
                } catch (Exception ex) {
                    ex.printStackTrace(); // check the Run console for the real error
                }
            }
        });
        backToMainMenuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                new MainMenuFrame();
                dispose();
            }
        });
        yesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {


            }
        });

        setVisible(true);
    }
}