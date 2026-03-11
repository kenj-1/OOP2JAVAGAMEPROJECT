package encantadia.ui.frames;
import encantadia.battle.EnemyFactory;

import encantadia.characters.*;
import encantadia.characters.Character;


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
        Image img = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);

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

            new BattleFrame(character, enemy);

            dispose();
        }
    }
    private void createUIComponents() {
        // TODO: place custom component creation code here
    }


}
