package encantadia.ui.frames;
import encantadia.battle.EnemyFactory;

import encantadia.characters.*;
import encantadia.characters.Character;
import encantadia.ui.frames.battleModeFrames.PVEBattleFrame;


import javax.swing.*;
import java.awt.*;        // ← add this

public class CharacterSelectionFrame extends JFrame {

    private JPanel characterSelectionFrame;
    private JButton selectDirk;
    private JButton selectMary;
    private JButton selectMakelanShere;
    private JButton selectTyrone;
    private JButton selectAdamus;
    private JButton selectTera;
    private JButton selectFlamara;
    private JButton selectDea;
    private JLabel labelDirk;
    private JLabel labelMary;
    private JLabel labelMakelanShere;
    private JLabel labelTyrone;
    private JLabel labelDea;
    private JLabel labelFlamara;
    private JLabel labelTera;
    private JLabel labelAdamus;
    private JLabel dirkCharacter;
    private JLabel maryCharacter;
    private JLabel makelanShereCharacter;
    private JLabel tyroneCharacter;
    private JLabel deaCharacter;
    private JLabel flamaraCharacter;
    private JLabel teraCharacter;
    private JLabel adamusCharacter;


    public CharacterSelectionFrame(){
        setContentPane(characterSelectionFrame);
        setTitle("Encantadia: Echoes of the Gem - Select your Fighter");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);

        ImageIcon icon = new ImageIcon("src/assets/Portrait_Placeholder.png");
        Image img = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);


        dirkCharacter.setText(
                "<html><center>" +
                        "Health: 5000HP<br>" +
                        "Ultimate: Reduces enemy damage by 65% next turn" +
                        "</center></html>"
        );


        maryCharacter.setText(
                "<html><center>" +
                        "Health: 5000HP<br>" +
                        "Ultimate: Reduces enemy damage by 65% next turn" +
                        "</center></html>"
        );

        makelanShereCharacter.setText(
                "<html><center>" +
                        "Health: 5000HP<br>" +
                        "Ultimate: Reduces enemy damage by 65% next turn" +
                        "</center></html>"
        );

        tyroneCharacter.setText(
                "<html><center>" +
                        "Health: 5000HP<br>" +
                        "Ultimate: Reduces enemy damage by 65% next turn" +
                        "</center></html>"
        );

        deaCharacter.setText(
                "<html><center>" +
                        "Health: 5000HP<br>" +
                        "Ultimate: Reduces enemy damage by 65% next turn" +
                        "</center></html>"
        );
        flamaraCharacter.setText(
                "<html><center>" +
                        "Health: 5000HP<br>" +
                        "Ultimate: Reduces enemy damage by 65% next turn" +
                        "</center></html>"
        );

        adamusCharacter.setText(
                "<html><center>" +
                        "Health: 5000HP<br>" +
                        "Ultimate: Reduces enemy damage by 65% next turn" +
                        "</center></html>"
        );

        teraCharacter.setText(
                "<html><center>" +
                        "Health: 5000HP<br>" +
                        "Ultimate: Reduces enemy damage by 65% next turn" +
                        "</center></html>"
        );
        selectDirk.setText("Select Dirk");
        selectMary.setText("Select Mary");
        selectDea.setText("Select Dea");
        selectFlamara.setText("Select Flamara");
        selectTera.setText("Select Tera");
        selectAdamus.setText("Select Adamus");
        selectTyrone.setText("Select Tyrone");
        selectMakelanShere.setText("Select Makelan Shere");


        labelDirk.setIcon(new ImageIcon(img));
        labelMary.setIcon(new ImageIcon(img));
        labelMakelanShere.setIcon(new ImageIcon(img));
        labelTyrone.setIcon(new ImageIcon(img));
        labelDea.setIcon(new ImageIcon(img));
        labelFlamara.setIcon(new ImageIcon(img));
        labelTera.setIcon(new ImageIcon(img));
        labelAdamus.setIcon(new ImageIcon(img));

        selectDirk.addActionListener(e -> showCharacterBackstory(new Dirk()));
        selectTyrone.addActionListener(e -> showCharacterBackstory(new Tyrone()));
        selectMary.addActionListener(e -> showCharacterBackstory(new Mary()));
        selectMakelanShere.addActionListener(e -> showCharacterBackstory(new MakelanShere()));
        selectAdamus.addActionListener(e -> showCharacterBackstory(new Adamus()));
        selectTera.addActionListener(e -> showCharacterBackstory(new Tera()));
        selectFlamara.addActionListener(e -> showCharacterBackstory(new Flamara()));
        selectDea.addActionListener(e -> showCharacterBackstory(new Dea()));

        setVisible(true);
    }


    private void showCharacterBackstory(Character character) {

        String message =
                character.getName() + "\n" +
                        character.getTitle() + "\n\n" +
                        character.getBackstory();

        int choice = JOptionPane.showConfirmDialog(
                this,
                message,
                "Chosen Fighter",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE
        );

        if(choice == JOptionPane.OK_OPTION){

            Character enemy = EnemyFactory.getRandomEnemy(character);

            String enemyMessage =
                    enemy.getName() + "\n" +
                            enemy.getTitle() + "\n\n" +
                            enemy.getBackstory();

            JOptionPane.showMessageDialog(
                    this,
                    enemyMessage,
                    "Enemy Appears!",
                    JOptionPane.INFORMATION_MESSAGE
            );

            new PVEBattleFrame(character, enemy);

            dispose();
        }
    }
    private void createUIComponents() {
        // TODO: place custom component creation code here
    }


}
