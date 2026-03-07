package encantadia.ui.ux;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainMenuFrame extends JFrame {
    private JPanel MainMenuFramePanel;
    private JTextField textField1;
    private JButton niggaButton;
    private JButton PVPButton;
    private JButton PVEButton;


    public MainMenuFrame() {
        setContentPane(MainMenuFramePanel);
        setTitle("Encantadia: Echoes of the Gem - Main Menu");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(300, 300);
        setLocationRelativeTo(null);
        setVisible(true);
        niggaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(MainMenuFrame.this, "Hello all my niggas say what");
            }
        });
    }
}
